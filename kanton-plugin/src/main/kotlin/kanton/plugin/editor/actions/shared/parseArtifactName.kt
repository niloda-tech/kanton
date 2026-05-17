package kanton.plugin.editor.actions.shared

import kanton.core.lib.parsing.parseLibMd
import kanton.core.lib.parsing.parseLibCoords

fun parseArtifactName(source: String): String? {
    val sections = parseLibMd(source)
    val headerLine = sections.firstOrNull { it.tag == "lib" }?.lines?.firstOrNull() ?: return null
    return parseLibCoords(headerLine)?.artifact
}
