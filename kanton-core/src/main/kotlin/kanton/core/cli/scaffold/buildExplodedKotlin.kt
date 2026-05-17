package kanton.core.cli.scaffold

import kanton.core.cli.parsing.isArgumentLine
import kanton.core.cli.parsing.parseDeps
import kanton.core.cli.parsing.parseCliMd
import kanton.core.shared.models.parseScriptHeader

fun buildExplodedKotlin(source: String): String? {
    val sections = parseCliMd(source)

    val cli = sections.firstOrNull { it.tag == "cli" } ?: return null
    val run = sections.firstOrNull { it.tag == "run" } ?: return null
    val depsSection = sections.firstOrNull { it.tag == "deps" }

    val firstLine = cli.lines.firstOrNull() ?: return null
    val (scriptName, help, className) = parseScriptHeader(firstLine)

    val depEntries = depsSection?.let { parseDeps(it.lines) } ?: emptyList()
    val depImports = depEntries.flatMap { it.imports }.filter { it.isNotEmpty() && it != "cli.CliScript" }
    val headerLines = cli.lines.drop(1).filter { it.isNotBlank() }

    val hasOptions = headerLines.any { !isArgumentLine(it) }
    val hasArguments = headerLines.any { isArgumentLine(it) }

    return mainClassTemplate(
        hasArgs = hasArguments,
        hasOptions = hasOptions,
        depImports = depImports,
        scriptName = scriptName,
        help = help,
        className = className,
        cli = cli,
        run = run
    )
}
