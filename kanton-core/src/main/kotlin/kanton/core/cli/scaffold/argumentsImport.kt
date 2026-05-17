package kanton.core.cli.scaffold

import kanton.core.shared.bind
import kanton.core.shared.template

const val argumentsImport = "import com.github.ajalt.clikt.parameters.arguments.*"
const val optionsImport = "import com.github.ajalt.clikt.parameters.options.*"
private val optionsTemplate = """
{{optionsImport}}
{{argumentsImport}}
""".template
fun optionsTemplate(hasOptions: Boolean, hasArgs: Boolean) =
    optionsTemplate.bind(
        "optionsImport" to if(hasOptions) optionsImport else null,
        "argumentsImport" to if(hasArgs) argumentsImport else null
    )
