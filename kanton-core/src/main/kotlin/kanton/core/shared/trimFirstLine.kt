package kanton.core.shared

fun String.removeBlankFirstLine(): String =
    if (substringBefore("\n").isBlank()) substringAfter("\n") else this
