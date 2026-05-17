package kanton.core.cli.scaffold

import kanton.core.cli.models.ArgumentConfig
import kanton.core.shared.Template
import kanton.core.shared.bind
import kanton.core.shared.models.kebabToCamelCase
import kanton.core.shared.template

fun ArgumentConfig.formatArgument(): Template {
    val escapedArgHelp = help.replace("\"", "\\\"")
    return valOptionTemplate.bind(
        "valName" to name.lowercase().kebabToCamelCase(),
        "name" to name,
        "helpParam" to if (escapedArgHelp.isNotEmpty()) helpParamTemplate(escapedArgHelp).value else "",
        "typeFunc" to (typeFunc?.let { ".$it" } ?: ""),
        "required" to "",
        "default" to (default?.let { ".default($it)" } ?: "")
    )
}

private val valOptionTemplate = """
private val {{valName}} by argument("{{name}}"{{helpParam}}){{required}}{{typeFunc}}{{default}}
""".template
