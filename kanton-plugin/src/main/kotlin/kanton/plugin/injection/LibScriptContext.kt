package kanton.plugin.injection

import kanton.core.lib.models.LibDepEntry

object LibScriptContext {
    const val DEPENDENCIES_PREFIX = ""
    const val DEPENDENCIES_SUFFIX = ""

    fun buildDepsPrefix(deps: List<LibDepEntry>): String = buildString {
        for (dep in deps) {
            appendLine("""@file:DependsOn("${dep.coord}")""")
        }
    }
}
