package kanton.core.lib.scaffold

import kanton.core.lib.parsing.isLibFormat
import kanton.core.lib.parsing.parseLibMd
import kanton.core.lib.parsing.parseLibDeps

fun buildExplodedLibraryKotlin(source: String, packageName: String? = null): String? {
    if (!isLibFormat(source)) return null
    val sections = parseLibMd(source)
    val body = sections.firstOrNull { it.tag == "body" } ?: return null
    val depsSection = sections.firstOrNull { it.tag == "deps" }

    val depEntries = depsSection?.let { parseLibDeps(it.lines) } ?: emptyList()
    val imports = depEntries.flatMap { it.imports }
        .filter { it.isNotEmpty() && !(it.startsWith("kanton.") && !it.startsWith("kanton.core.")) }

    return buildString {
        if (!packageName.isNullOrBlank()) {
            appendLine("package $packageName")
            appendLine()
        }
        for (imp in imports) appendLine("import $imp")
        if (imports.isNotEmpty()) appendLine()
        for (line in body.lines) appendLine(line)
    }
}
