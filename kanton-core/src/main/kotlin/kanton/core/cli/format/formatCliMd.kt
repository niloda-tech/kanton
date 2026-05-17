package kanton.core.cli.format

import kanton.core.cli.models.ArgumentConfig
import kanton.core.cli.models.DepEntry
import kanton.core.cli.models.OptionConfig

data class CliSpec(
    val name: String,
    val help: String,
    val title: String,
    val description: String? = null,
    val options: List<OptionConfig> = emptyList(),
    val arguments: List<ArgumentConfig> = emptyList(),
    val dependencies: List<DepEntry> = emptyList(),
    val executable: Boolean = true
)

fun formatCliMd(spec: CliSpec, runBlock: String): String = buildString {
    if (spec.executable) {
        appendLine("#!/usr/bin/env bash")
        appendLine("exec ~/.kanton/bin/kanton-executor \"${spec.name}.kt.md\" \"\$@\"")
        appendLine()
    }

    appendLine("# ${spec.title}")
    appendLine()
    if (!spec.description.isNullOrBlank()) {
        appendLine(spec.description)
        appendLine()
    }

    appendLine("# actions")
    appendLine("[Open exploded Kotlin ↗](kanton://scaffold)")
    appendLine("[← Sync back](kanton://sync-back)")
    appendLine("[Compile binary ⚡](kanton://compile)")
    appendLine("[Delete scaffold](kanton://delete-scaffold)")
    appendLine()

    appendLine("```cli")
    appendLine("${spec.name}:${spec.help}")
    for (opt in spec.options) {
        appendLine(formatOptionLine(opt))
    }
    for (arg in spec.arguments) {
        appendLine(formatArgumentLine(arg))
    }
    appendLine()
    append(runBlock.trimEnd())
    appendLine()
    appendLine("```")
    appendLine()

    appendLine("# dependencies")
    val deps = ensureBaseImports(spec.dependencies, spec.options.isNotEmpty())
    for (dep in deps) {
        appendLine(dep.coord)
        for (imp in dep.imports) {
            appendLine("  $imp")
        }
    }
}

internal fun formatOptionLine(opt: OptionConfig): String = buildString {
    append(opt.names.joinToString(", "))
    if (opt.help.isNotBlank()) {
        append(", ")
        append(opt.help)
    }
    if (opt.typeFunc != null) {
        append(" : ${opt.typeFunc}")
    }
    if (opt.default != null) {
        append(" = ${opt.default}")
    }
}

internal fun formatArgumentLine(arg: ArgumentConfig): String = buildString {
    append(arg.name)
    if (arg.help.isNotBlank()) {
        append(", ")
        append(arg.help)
    }
    if (arg.typeFunc != null) {
        append(" : ${arg.typeFunc}")
    }
    if (arg.default != null) {
        append(" = ${arg.default}")
    }
}

private val CLIKT_COORD = "com.github.ajalt.clikt:clikt:5.1.0"
private val BASE_IMPORTS = listOf("cli.CliScript")
private val OPTION_IMPORTS = listOf(
    "com.github.ajalt.clikt.parameters.options.option",
    "com.github.ajalt.clikt.parameters.options.default"
)

internal fun ensureBaseImports(deps: List<DepEntry>, hasOptions: Boolean): List<DepEntry> {
    val cliktIdx = deps.indexOfFirst { it.coord == CLIKT_COORD }
    if (cliktIdx < 0) {
        val required = BASE_IMPORTS + if (hasOptions) OPTION_IMPORTS else emptyList()
        return listOf(DepEntry(CLIKT_COORD, required)) + deps
    }

    val existing = deps[cliktIdx]
    val missing = buildList {
        for (imp in BASE_IMPORTS) {
            if (imp !in existing.imports) add(imp)
        }
        if (hasOptions) {
            for (imp in OPTION_IMPORTS) {
                if (imp !in existing.imports) add(imp)
            }
        }
    }
    if (missing.isEmpty()) return deps

    val updated = existing.copy(imports = existing.imports + missing)
    return deps.toMutableList().apply { set(cliktIdx, updated) }
}
