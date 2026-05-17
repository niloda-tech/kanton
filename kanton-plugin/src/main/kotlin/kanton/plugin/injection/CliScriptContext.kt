package kanton.plugin.injection

object CliScriptContext {

    val DEPENDENCIES_PREFIX = ""
    val DEPENDENCIES_SUFFIX = ""

    val IMPORTS_PREFIX = ""
    val IMPORTS_SUFFIX = ""

    val STANDARD_IMPORTS = buildString {
        appendLine("import com.github.ajalt.clikt.core.CliktCommand")
        appendLine("import com.github.ajalt.clikt.parameters.options.*")
        appendLine("import com.github.ajalt.clikt.parameters.arguments.*")
    }

    val CLIKT_CLASS_PREFIX = buildString {
        appendLine()
        appendLine("object __script : com.github.ajalt.clikt.core.CliktCommand() {")
        appendLine("    override fun run() {")
    }

    val CLIKT_CLASS_SUFFIX = buildString {
        appendLine()
        appendLine("    }")
        appendLine("}")
    }

    val KOTLIN_PREFIX = STANDARD_IMPORTS + CLIKT_CLASS_PREFIX
    val KOTLIN_SUFFIX = CLIKT_CLASS_SUFFIX

    val RUN_SUFFIX = buildString {
        appendLine()
        appendLine("    }")
        appendLine("}")
    }

    fun buildRunPrefix(depsContext: String, optionDeclarations: String): String = buildString {
        appendLine("object __script : com.github.ajalt.clikt.core.CliktCommand() {")
        if (depsContext.isNotEmpty()) {
            append(depsContext)
        }
        if (optionDeclarations.isNotEmpty()) {
            append(optionDeclarations)
        }
        appendLine("    override fun run() {")
    }
}
