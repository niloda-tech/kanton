package kanton.core.lib.syncback

import kanton.core.lib.models.LibDepEntry

fun extractLibBodyFromKotlin(kotlinSrc: String): String {
    val lines = kotlinSrc.lines()
    var i = 0
    if (i < lines.size && lines[i].startsWith("package ")) i++
    if (i < lines.size && lines[i].isBlank()) i++
    while (i < lines.size && lines[i].startsWith("import ")) i++
    if (i < lines.size && lines[i].isBlank()) i++
    val bodyLines = lines.drop(i)
    return bodyLines.joinToString("\n").trimEnd()
}

fun replaceLibBodyInLibMd(source: String, newBody: String): String {
    val lines = source.lines()
    val fenceStart = lines.indexOfFirst { it.matches(Regex("^```lib\\s*$")) }
    if (fenceStart < 0) return source
    val relIdx = lines.subList(fenceStart + 1, lines.size).indexOfFirst { it.trimEnd() == "```" }
    val fenceEnd = if (relIdx < 0) -1 else fenceStart + 1 + relIdx
    if (fenceEnd < 0) return source

    val fenceLines = lines.subList(fenceStart + 1, fenceEnd)
    val blankIdx = fenceLines.indexOfFirst { it.isBlank() }
    val headerCount = if (blankIdx >= 0) blankIdx else fenceLines.size

    val before = lines.subList(0, fenceStart + 1) +
            fenceLines.subList(0, headerCount) +
            listOf("") +
            newBody.lines()
    val after = lines.subList(fenceEnd, lines.size)
    return (before + after).joinToString("\n")
}

fun extractMavenCoordsWithConfigFromGradleKts(content: String): List<Pair<String, String>> {
    val configs = listOf("api", "implementation", "compileOnly", "runtimeOnly", "testImplementation", "testCompileOnly")
    val configPattern = configs.joinToString("|")
    val regex = Regex("""($configPattern)\("([^"]+)"\)""")
    return content.lines().mapNotNull { line ->
        regex.find(line.trim())?.let { it.groupValues[1] to it.groupValues[2] }
    }
}

fun updateLibDepsInLibMd(source: String, deps: List<LibDepEntry>): String {
    val newContent = buildString {
        for (dep in deps) {
            val prefix = if (dep.config == "implementation") "" else "${dep.config} "
            appendLine("$prefix${dep.coord}")
            for (imp in dep.imports) appendLine("  $imp")
        }
    }.trimEnd()

    return replaceLibDepsSectionContent(source, newContent)
}

private fun replaceLibDepsSectionContent(source: String, newContent: String): String {
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
