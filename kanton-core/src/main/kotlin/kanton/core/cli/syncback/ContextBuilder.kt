package kanton.core.cli.syncback

import kanton.core.cli.models.InjectionContext
import kanton.core.cli.parsing.*
import kanton.core.cli.templates.mainRunScriptTemplate
import kanton.core.shared.models.kebabToCamelCase
import kanton.core.shared.models.parseScriptHeader

private data class CliStub(val returnType: String, val inlineBody: String)

private val CLI_STUBS = mapOf(
    "cli.stdinText" to CliStub(": String", """generateSequence(::readLine).joinToString("\n")""")
)

fun knownCliReturnType(fqn: String): String = CLI_STUBS[fqn]?.returnType ?: ""

fun knownCliInlineBody(fqn: String): String = CLI_STUBS[fqn]?.inlineBody ?: ""

fun explodeCliKts(cliKtsContent: String): String {
    val filteredLines = cliKtsContent.lines().filter { line ->
        !line.startsWith("#!/") &&
        !line.startsWith("@file:DependsOn(") &&
        !line.startsWith("@file:Repository(") &&
        !line.startsWith("@file:Import(") &&
        !line.startsWith("@file:CompilerOptions(")
    }

    val trimmedLines = filteredLines.dropWhile { it.isBlank() }
    val result = trimmedLines.joinToString("\n")

    val classNameMatch = trimmedLines.firstNotNullOfOrNull {
        Regex("^class ([A-Za-z0-9_]+)").find(it)
    }

    return if (classNameMatch != null && !result.contains("fun main(")) {
        val className = classNameMatch.groupValues[1]
        result + mainRunScriptTemplate(className)
    } else {
        result
    }
}

fun buildInjectionContextFromCliKts(cliKtsContent: String): InjectionContext? {
    val exploded = explodeCliKts(cliKtsContent)
    val lines = exploded.lines()

    val classNameMatch = lines.firstNotNullOfOrNull {
        Regex("^class ([A-Za-z0-9_]+)").find(it)
    } ?: return null
    val className = classNameMatch.groupValues[1]

    val runLineIdx = lines.indexOfFirst { it == "    override fun run() {" }
    if (runLineIdx < 0) return null

    val strippedCliImports = lines
        .filter { it.startsWith("import cli.") }
        .map { it.removePrefix("import ").trim() }

    var prefix = lines.subList(0, runLineIdx + 1).joinToString("\n") + "\n"
    prefix = prefix.lines().filter { !it.startsWith("import ") }.joinToString("\n") + "\n"
    prefix = prefix.replace(": CliScript(", ": com.github.ajalt.clikt.core.CliktCommand(")
    prefix = prefix.replace(": CliktCommand(", ": com.github.ajalt.clikt.core.CliktCommand(")

    val delegates = buildString {
        for (fqn in strippedCliImports) {
            val simpleName = fqn.substringAfterLast('.')
            val returnType = knownCliReturnType(fqn)
            val body = knownCliInlineBody(fqn)
            if (body.isNotEmpty()) {
                appendLine("    private fun $simpleName()$returnType = $body")
            }
        }
    }
    if (delegates.isNotEmpty()) {
        prefix = prefix.replace("    override fun run() {", delegates + "    override fun run() {")
    }

    val suffix = "    }\n}\n"

    return InjectionContext(prefix, suffix, className)
}

fun buildInjectionContextFromMd(source: String): InjectionContext? {
    val sections = parseCliMd(source)
    val cli = sections.firstOrNull { it.tag == "cli" } ?: return null
    val depsSection = sections.firstOrNull { it.tag == "deps" }

    val firstLine = cli.lines.firstOrNull() ?: return null
    val (scriptName, help, className) = parseScriptHeader(firstLine)

    val depEntries = depsSection?.let { parseDeps(it.lines) } ?: emptyList()
    val allDepImports = depEntries.flatMap { it.imports }

    val prefix = buildString {
        val escapedName = scriptName.replace("\"", "\\\"")
        val escapedHelp = help.replace("\"", "\\\"")
        appendLine("class $className : com.github.ajalt.clikt.core.CliktCommand(name = \"$escapedName\") {")
        if (escapedHelp.isNotEmpty()) {
            appendLine("    override fun commandHelp(context: com.github.ajalt.clikt.core.Context) = \"$escapedHelp\"")
        }
        val filteredImports = allDepImports.filter { it.isNotEmpty() && it != "cli.CliScript" }
        for (imp in filteredImports) {
            val simpleName = imp.substringAfterLast('.')
            val returnType = knownCliReturnType(imp)
            val body = knownCliInlineBody(imp).ifEmpty { "$imp()" }
            appendLine("    private fun $simpleName()$returnType = $body")
        }
        for (optLine in cli.lines.drop(1)) {
            if (optLine.isBlank()) continue
            if (isArgumentLine(optLine)) {
                val arg = parseArgumentLine(optLine)
                val varName = arg.name.lowercase().kebabToCamelCase()
                val escapedArgHelp = arg.help.replace("\"", "\\\"")
                val helpParam = if (escapedArgHelp.isNotEmpty()) ", help = \"$escapedArgHelp\"" else ""
                append("    private val $varName by argument(\"${arg.name}\"$helpParam)")
                val transform = buildArgumentTransform(arg.typeFunc, arg.default)
                if (transform.isNotEmpty()) append(transform)
                appendLine()
            } else {
                val opt = parseOptionLine(optLine)
                val varName = opt.names.firstOrNull { it.startsWith("--") }?.removePrefix("--")?.kebabToCamelCase() ?: continue
                val namesStr = opt.names.joinToString(", ") { "\"$it\"" }
                val transform = buildTransform(opt.typeFunc, opt.default)
                val escapedOptHelp = opt.help.replace("\"", "\\\"")
                append("    private val $varName by option($namesStr, help = \"$escapedOptHelp\")")
                if (transform.isNotEmpty()) append(transform)
                appendLine()
            }
        }
        appendLine()
        appendLine("    override fun run() {")
    }

    val suffix = "    }\n}\n"

    return InjectionContext(prefix, suffix, className)
}
