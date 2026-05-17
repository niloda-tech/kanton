package kanton.core.shared

import kanton.core.cli.templates.templateReplace


@JvmInline
value class Template(val value: String) {
    override fun toString() = value
    companion object {
        val Empty = Template("")
    }
}

val String.template get() = Template(this)

private val unescapedParameter = Regex("""\{\{(\w+)}}""")

infix fun String.with(template: Template) = this to template.value

fun Template.bind(args: Map<String, String?>): Template =
    bind(*args.map { (k, v) -> k to v }.toTypedArray())

fun Template.bind(arg: String, template: Template): Template =
    bind(arg to template.value)

fun Template.bind(vararg args: Pair<String, String?>): Template =
    args
        .fold(value) { acc, (k, v) -> acc.templateReplace(k, v ?: "") }
        .removeBlankFirstLine()
        .validateAllTokensReplaced()
        .let { Template(it) }

private fun String.validateAllTokensReplaced(): String =
    unescapedParameter
        .find(this)
        ?.let { error("Unresolved template token: {{${it.groupValues[1]}}}") }
        ?: this

fun Template.bindAll(key: String, values: List<String?>): List<Template> =
    values.map { v -> bind(key to v) }

fun List<Template>.bindAll(key: String, values: List<String?>): List<Template> =
    zip(values).map { (template, value) -> template.bind(key to value) }

val List<Template>.lines: String get() = joinToString("\n")

fun bind(vararg args: Pair<String, String?>): Map<String, String?> = args.toMap()

infix fun Map<String, String?>.toTemplate(template: Template): Template =
    template.bind(this)
