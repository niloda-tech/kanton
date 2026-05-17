package kanton.core.cli.parsing

fun buildTransform(typeFunc: String?, default: String?): String = buildString {
    if (typeFunc != null) append(".$typeFunc")
    if (default != null) append(".default($default)")
}
