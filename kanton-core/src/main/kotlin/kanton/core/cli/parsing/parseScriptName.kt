package kanton.core.cli.parsing

fun parseScriptName(source: String): String? {
    val cliSection = parseCliMd(source).firstOrNull { it.tag == "cli" } ?: return null
    val firstLine = cliSection.lines.firstOrNull() ?: return null
    val colonIdx = firstLine.indexOf(':')
    return if (colonIdx >= 0) firstLine.substring(0, colonIdx).trim() else firstLine.trim()
}
