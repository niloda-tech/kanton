package kanton.core.lib.parsing

import kanton.core.lib.models.LibCoords
import kanton.core.lib.models.LibDepEntry
import kanton.core.shared.models.Section

fun isLibFormat(source: String): Boolean =
    source.lines().any { it.matches(Regex("^```lib\\s*$")) }

fun parseLibMd(source: String): List<Section> {
    val libLines = mutableListOf<String>()
    val depsLines = mutableListOf<String>()

    var inLibFence = false
    var fenceClosed = false
    var inDepsSection = false

    for (line in source.lines()) {
        when {
            !fenceClosed && line.matches(Regex("^```lib\\s*$")) -> inLibFence = true
            inLibFence && line.trimEnd() == "```" -> {
                inLibFence = false
                fenceClosed = true
            }
            inLibFence -> libLines.add(line)
            fenceClosed && line.matches(Regex("^# dependencies\\s*$", RegexOption.IGNORE_CASE)) -> inDepsSection = true
            inDepsSection && line.matches(Regex("^# .*")) -> inDepsSection = false
            inDepsSection -> depsLines.add(line)
        }
    }

    val blankIdx = libLines.indexOfFirst { it.isBlank() }
    val (header, body) = if (blankIdx >= 0) {
        libLines.subList(0, blankIdx) to libLines.subList(blankIdx + 1, libLines.size)
    } else {
        libLines to emptyList<String>()
    }

    val result = mutableListOf<Section>()
    if (depsLines.isNotEmpty()) result.add(Section("deps", emptyList(), depsLines.toList()))
    if (header.isNotEmpty()) result.add(Section("lib", emptyList(), header.toList()))
    if (body.isNotEmpty()) result.add(Section("body", emptyList(), body.toList()))
    return result
}

fun parseLibCoords(headerLine: String): LibCoords? {
    val parts = headerLine.split(":")
    if (parts.size < 3) return null
    val group = parts[0].trim()
    val artifact = parts[1].trim()
    val version = parts[2].trim()
    val help = parts.drop(3).joinToString(":").trim()
    if (group.isEmpty() || artifact.isEmpty() || version.isEmpty()) return null
    return LibCoords(group, artifact, version, help)
}

private val knownConfigs = setOf("api", "implementation", "compileOnly", "runtimeOnly", "testImplementation", "testCompileOnly")

fun parseLibDepLine(line: String): Pair<String, String> {
    val trimmed = line.trim()
    val spaceIdx = trimmed.indexOf(' ')
    if (spaceIdx > 0) {
        val maybeConfig = trimmed.substring(0, spaceIdx)
        if (maybeConfig in knownConfigs) {
            return maybeConfig to trimmed.substring(spaceIdx + 1).trim()
        }
    }
    return "implementation" to trimmed
}

fun parseLibDeps(lines: List<String>): List<LibDepEntry> {
    val result = mutableListOf<LibDepEntry>()
    var currentCoord: String? = null
    var currentConfig = "implementation"
    var currentImports = mutableListOf<String>()

    for (line in lines) {
        when {
            line.isBlank() -> Unit
            line.trimStart().startsWith("//") -> Unit
            line.startsWith("  ") -> {
                val imp = line.trim()
                if (imp.isNotEmpty()) currentImports.add(imp)
            }
            else -> {
                if (currentCoord != null) result.add(LibDepEntry(currentCoord, currentConfig, currentImports.toList()))
                val (config, coord) = parseLibDepLine(line)
                currentCoord = coord
                currentConfig = config
                currentImports = mutableListOf()
            }
        }
    }

    if (currentCoord != null) result.add(LibDepEntry(currentCoord, currentConfig, currentImports.toList()))
    return result
}
