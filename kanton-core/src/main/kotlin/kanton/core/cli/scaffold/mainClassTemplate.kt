package kanton.core.cli.scaffold

import kanton.core.cli.models.OptionConfig
import kanton.core.cli.parsing.isArgumentLine
import kanton.core.cli.parsing.parseArgumentLine
import kanton.core.cli.parsing.parseOptionLine
import kanton.core.cli.templates.mainRunScriptTemplate
import kanton.core.cli.templates.runTemplate
import kanton.core.shared.*
import kanton.core.shared.models.Section
import kanton.core.shared.models.kebabToCamelCase

fun mainClassTemplate(
    hasArgs: Boolean,
    hasOptions: Boolean,
    depImports: List<String>,
    scriptName: String,
    help: String,
    className: String,
    cli: Section,
    run: Section
): String = buildString {
    val escapedName = scriptName.replace("\"", "\\\"")
    val escapedHelp = help.replace("\"", "\\\"")
    val paramsImport: Template = optionsTemplate(hasOptions = hasOptions, hasArgs = hasArgs)
    val help = if (escapedHelp.isNotEmpty()) helpTemplate(escapedHelp) else Template.Empty

    appendLine(
        """
    import com.github.ajalt.clikt.core.CliktCommand
    {{paramsImport}}
    {{implLines}}
    {{commandLine}} {
        {{help}}
        {{paramLines}}
        {{run}}
    }
    {{mainRunScript}}
    """.template
            .bind(
                "help" with help,
                "paramLines" to paramLines(cli).lines,
                "commandLine" to """class $className : CliktCommand(name = "$escapedName")""",
                "run" to runTemplate(run).value,
                "mainRunScript" to mainRunScriptTemplate(className).value,
                "paramsImport" with paramsImport,
                "implLines" to "import {{imp}}".template.bindAll("imp", depImports).lines
            )

    )
}

private fun paramLines(cli: Section): List<Template> =
    cli.lines.drop(1).mapNotNull { optLine ->
        if (optLine.isBlank()) null
        else {
            if (isArgumentLine(optLine)) {
                val argument = parseArgumentLine(optLine).formatArgument()
                """
                {{argument}}

                """.template.bind("argument" to argument.value)
            } else {
                val opt = parseOptionLine(optLine).formatOption()
                if (opt == Template.Empty) Template.Empty
                else """
                {{opt}}

            """.template.bind("opt" to opt.value)
            }
        }
    }


fun OptionConfig.formatOption(): Template {
    val varName =
        names.firstOrNull { it.startsWith("--") }?.removePrefix("--")?.kebabToCamelCase()
    return if (varName != null) {
        val namesStr = names.joinToString(", ") { "\"$it\"" }
        val escapedOptHelp = help.replace("\"", "\\\"")
        optionTemplate.bind(
            "varName" to varName,
            "namesStr" to namesStr,
            "escapedOptHelp" to escapedOptHelp,
            "typeFunc" to (typeFunc?.let { ".$it" } ?: ""),
            "default" to (default?.let { ".default($it)" } ?: "")
        )
    } else {
        Template.Empty
    }
}

private val optionTemplate = """
private val {{varName}} by option({{namesStr}}, help = "{{escapedOptHelp}}"){{typeFunc}}{{default}}"""
    .template

private val helpTemplate = """
override fun commandHelp(context: com.github.ajalt.clikt.core.Context) = "{{escapedHelp}}"""".template

fun helpTemplate(escapedHelp: String) = helpTemplate.bind("escapedHelp" to escapedHelp)
