package kanton.core.cli.parsing

import kanton.core.cli.models.DepEntry

fun parseDeps(lines: List<String>): List<DepEntry> {
    val result = mutableListOf<DepEntry>()
    var currentCoord: String? = null
    var currentImports = mutableListOf<String>()

    for (line in lines) {
        when {
            line.isBlank() -> Unit
            line.trimStart().startsWith("//") -> Unit
            line.startsWith("  ") -> {
                val imp = line.trim()
                if (imp.isNotEmpty()) currentImports.add(imp)
            }
            else -> {
                if (currentCoord != null) result.add(DepEntry(currentCoord, currentImports.toList()))
                currentCoord = line.trim()
                currentImports = mutableListOf()
            }
        }
    }

    if (currentCoord != null) result.add(DepEntry(currentCoord, currentImports.toList()))
    return result
}
