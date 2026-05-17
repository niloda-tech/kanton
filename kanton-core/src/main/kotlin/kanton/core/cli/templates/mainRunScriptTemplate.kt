package kanton.core.cli.templates

import kanton.core.shared.Template
import kanton.core.shared.bind
import kanton.core.shared.template

fun mainRunScriptTemplate(className: String): Template =
    mainRunScriptTemplate.bind("className" to className)

private val mainRunScriptTemplate = """

    fun main(args: Array<String>) = cli.runScript({{className}}(), args)

""".template
