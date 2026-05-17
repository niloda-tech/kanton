package kanton.core.cli.parsing

fun isArgumentLine(line: String): Boolean =
    line.isNotBlank() && !line.trimStart().startsWith("-")
