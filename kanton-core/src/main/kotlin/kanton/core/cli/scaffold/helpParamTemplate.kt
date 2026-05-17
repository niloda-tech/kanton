package kanton.core.cli.scaffold

import kanton.core.shared.bind
import kanton.core.shared.template

fun helpParamTemplate(escapedArgHelp: String) =
    helpParamTemplate
        .bind("escapedArgHelp" to escapedArgHelp)

private val helpParamTemplate = """, help = "{{escapedArgHelp}}"""".template
