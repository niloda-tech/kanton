package kanton.core.cli.syncback

fun replaceInCliBlock(source: String, newRunBody: String): String {
    val lines = source.lines()
    val openIdx = lines.indexOfFirst { it.matches(Regex("^```cli\\s*$")) }
    if (openIdx < 0) return source
    val closeIdx = (openIdx + 1 until lines.size).firstOrNull { lines[it].trimEnd() == "```" }
        ?: return source
    val blankIdx = lines.subList(openIdx + 1, closeIdx).indexOfFirst { it.isBlank() }
    if (blankIdx < 0) return source
    return (lines.subList(0, openIdx + 1 + blankIdx + 1) +
            newRunBody.trimEnd().lines() +
            lines.subList(closeIdx, lines.size)).joinToString("\n")
}

fun replaceRunSectionInCliMd(source: String, newRunBody: String): String =
    replaceInCliBlock(source, newRunBody)
