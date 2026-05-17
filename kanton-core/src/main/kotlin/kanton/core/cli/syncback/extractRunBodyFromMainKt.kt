package kanton.core.cli.syncback

fun extractRunBodyFromMainKt(mainKt: String): String? {
    val lines = mainKt.lines()
    val startIdx = lines.indexOfFirst { it.trimStart().startsWith("override fun run()") }
    if (startIdx < 0) return null
    val result = mutableListOf<String>()
    var depth = 1
    for (i in (startIdx + 1) until lines.size) {
        val line = lines[i]
        depth += line.count { it == '{' } - line.count { it == '}' }
        if (depth <= 0) break
        result.add(line)
    }
    val minIndent = result.filter { it.isNotBlank() }.minOfOrNull { it.length - it.trimStart().length } ?: 0
    return result.joinToString("\n") { if (it.isBlank()) "" else it.drop(minIndent) }
}
