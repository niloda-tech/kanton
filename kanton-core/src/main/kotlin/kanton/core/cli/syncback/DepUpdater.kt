package kanton.core.cli.syncback

import kanton.core.cli.models.DepEntry
import kanton.core.cli.parsing.parseDeps
import kanton.core.cli.parsing.parseCliMd
import kotlin.collections.plusAssign

fun extractDepImportsFromMainKt(mainKt: String): List<String> {
    val excluded = setOf(
        "com.github.ajalt.clikt.core.CliktCommand",
        "com.github.ajalt.clikt.parameters.options.*",
        "com.github.ajalt.clikt.parameters.arguments.*"
    )
    return mainKt.lines()
        .filter { it.startsWith("import ") }
        .map { it.removePrefix("import ").trim() }
        .filter { it !in excluded }
}

fun updateDepsInCliMd(source: String, newCoords: List<String>, newImports: List<String>): String {
    val sections = parseCliMd(source)
    val existingEntries = sections.firstOrNull { it.tag == "deps" }?.let { parseDeps(it.lines) } ?: emptyList()

    val handledImports = mutableSetOf<String>()
    val updatedEntries = newCoords.map { coord ->
        val existing = existingEntries.firstOrNull { it.coord == coord }
        val imports = (existing?.imports ?: emptyList())
            .filter { it in newImports || it in PRESERVED_IMPORTS }
            .also { handledImports += it }
            .toMutableList()
        DepEntry(coord, imports)
    }.toMutableList()

    val coords = updatedEntries.map { it.coord }
    val toRelocate = mutableListOf<Pair<Int, String>>()
    for ((fromIdx, entry) in updatedEntries.withIndex()) {
        for (imp in entry.imports) {
            val bestIdx = findBestCoordIndex(imp, coords)
            if (bestIdx >= 0 && bestIdx != fromIdx) {
                toRelocate += fromIdx to imp
            }
        }
    }
    for ((fromIdx, imp) in toRelocate) {
        val bestIdx = findBestCoordIndex(imp, coords)
        val fromEntry = updatedEntries[fromIdx]
        updatedEntries[fromIdx] = DepEntry(fromEntry.coord, fromEntry.imports - imp)
        val toEntry = updatedEntries[bestIdx]
        updatedEntries[bestIdx] = DepEntry(toEntry.coord, toEntry.imports + imp)
    }

    val orphans = newImports.filter { it !in handledImports }
    for (orphan in orphans) {
        val bestIdx = findBestCoordIndex(orphan, updatedEntries.map { it.coord })
        val targetIdx = bestIdx.takeIf { it >= 0 } ?: (updatedEntries.size - 1)
        if (targetIdx >= 0) {
            val entry = updatedEntries[targetIdx]
            updatedEntries[targetIdx] = DepEntry(entry.coord, entry.imports + orphan)
        }
    }

    val newDepsContent = buildString {
        for (entry in updatedEntries) {
            appendLine(entry.coord)
            for (imp in entry.imports) appendLine("  $imp")
        }
    }.trimEnd()

    return replaceDepsSectionContent(source, newDepsContent)
}

private fun findBestCoordIndex(import: String, coords: List<String>): Int {
    var bestIdx = -1
    var bestLen = 0
    coords.forEachIndexed { idx, coord ->
        val groupId = coord.substringBefore(":")
        if ((import == groupId || import.startsWith("$groupId.")) && groupId.length > bestLen) {
            bestIdx = idx
            bestLen = groupId.length
        }
    }
    return bestIdx
}

private val PRESERVED_IMPORTS = setOf("cli.CliScript")

private fun replaceDepsSectionContent(source: String, newContent: String): String {
    val lines = source.lines()
    val headerIdx = lines.indexOfFirst { it.matches(Regex("^# dependencies\\s*$", RegexOption.IGNORE_CASE)) }
    if (headerIdx < 0) {
        return source.trimEnd() + "\n\n# dependencies\n" + newContent + "\n"
    }
    val nextHeaderIdx = (headerIdx + 1 until lines.size)
        .firstOrNull { lines[it].matches(Regex("^# .*")) }
        ?: lines.size
    return (lines.subList(0, headerIdx + 1) +
            newContent.lines() +
            if (nextHeaderIdx < lines.size) listOf("") + lines.subList(nextHeaderIdx, lines.size)
            else emptyList()).joinToString("\n")
}
