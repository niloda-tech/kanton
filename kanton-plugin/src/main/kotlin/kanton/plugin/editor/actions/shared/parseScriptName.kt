package kanton.plugin.editor.actions.shared

import kanton.core.cli.parsing.parseCliMd

fun parseScriptName(source: String): String? {
    val sections = parseCliMd(source)
    val firstLine = sections.firstOrNull { it.tag == "cli" }?.lines?.firstOrNull() ?: return null
    val colonIdx = firstLine.indexOf(':')
    return if (colonIdx >= 0) firstLine.substring(0, colonIdx).trim() else firstLine.trim()
}
