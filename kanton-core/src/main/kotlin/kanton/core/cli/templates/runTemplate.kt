package kanton.core.cli.templates

import kanton.core.shared.models.Section
import kanton.core.shared.bind
import kanton.core.shared.template

fun runTemplate(run: Section) =
    runTemplate.bind(
        "lines" to run.lines.joinToString("\n") {
            it.ifBlank { "" }
        }
    )

private val runTemplate = """
override fun run() {
    {{lines}}
}
""".template
