package kanton.core.cli.templates

fun String.templateReplace(variable: String, replacement: String): String {
    val pattern = Regex("""( *)\{\{$variable}}""")
    return pattern.replace(this) { match ->
        val indent = match.groupValues[1]
        replacement.lines().joinToString("\n") { line ->
            if (line.isEmpty()) line else indent + line
        }
    }
}
