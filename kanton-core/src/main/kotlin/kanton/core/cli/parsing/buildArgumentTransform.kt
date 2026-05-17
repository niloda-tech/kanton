package kanton.core.cli.parsing

fun buildArgumentTransform(typeFunc: String?, default: String?): String = buildString {
    if (typeFunc != null) append(".$typeFunc")
    if (default != null) append(".default($default)")
}
