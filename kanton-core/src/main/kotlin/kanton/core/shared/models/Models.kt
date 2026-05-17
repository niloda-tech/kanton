package kanton.core.shared.models

data class Section(val tag: String, val args: List<String>, val lines: List<String>)

data class ActionLink(val label: String, val action: String, val scheme: String = "kanton")

data class ScriptHeader(val scriptName: String, val help: String, val className: String)

fun parseScriptHeader(firstLine: String): ScriptHeader {
    val colonIdx = firstLine.indexOf(':')
    val scriptName = if (colonIdx >= 0) firstLine.substring(0, colonIdx).trim() else firstLine.trim()
    val help = if (colonIdx >= 0) firstLine.substring(colonIdx + 1).trim() else ""
    return ScriptHeader(scriptName, help, scriptNameToClassName(scriptName))
}

fun scriptNameToClassName(name: String): String =
    name.split("-").joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }

fun String.kebabToCamelCase(): String =
    split("-").mapIndexed { i, part -> if (i == 0) part else part.replaceFirstChar { it.uppercaseChar() } }.joinToString("")
