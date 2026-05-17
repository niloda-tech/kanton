package kanton.core.cli.parsing

import kanton.core.shared.models.Section

fun isCliFormat(source: String): Boolean =
    source.lines().any { it.matches(Regex("^```cli\\s*$")) }

fun parseCliMd(source: String): List<Section> {
    val lines = source.lines()
    val cliLines = mutableListOf<String>()
    val depsLines = mutableListOf<String>()

    var inCliFence = false
    var fenceClosed = false
    var inDepsSection = false

    for (line in lines) {
        when {
            !fenceClosed && line.matches(Regex("^```cli\\s*$")) -> {
                inCliFence = true
            }
            inCliFence && line.trimEnd() == "```" -> {
                inCliFence = false
                fenceClosed = true
                inDepsSection = false
            }
            inCliFence -> {
                cliLines.add(line)
            }
            fenceClosed && line.matches(Regex("^# dependencies\\s*$", RegexOption.IGNORE_CASE)) -> {
                inDepsSection = true
            }
            inDepsSection && line.matches(Regex("^# .*")) -> {
                inDepsSection = false
            }
            inDepsSection -> {
                depsLines.add(line)
            }
        }
    }

    val blankIdx = cliLines.indexOfFirst { it.isBlank() }
    val (cli, run) = if (blankIdx >= 0) {
        cliLines.subList(0, blankIdx) to cliLines.subList(blankIdx + 1, cliLines.size)
    } else {
        cliLines to emptyList<String>()
    }

    val result = mutableListOf<Section>()
    if (depsLines.isNotEmpty()) result.add(Section("deps", emptyList(), depsLines.toList()))
    if (cli.isNotEmpty()) result.add(Section("cli", emptyList(), cli.toList()))
    if (run.isNotEmpty()) result.add(Section("run", emptyList(), run.toList()))
    return result
}
