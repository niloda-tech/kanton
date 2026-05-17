package kanton.core.shared.parsing

import kanton.core.shared.models.ActionLink

fun parseActions(source: String): List<ActionLink> {
    val linkRegex = Regex("""\[([^\]]+)\]\((kanton|cli|lib)://([^)]+)\)""")
    val result = mutableListOf<ActionLink>()
    var inActions = false
    for (line in source.lines()) {
        when {
            line.matches(Regex("^# actions\\s*$", RegexOption.IGNORE_CASE)) -> inActions = true
            inActions && line.matches(Regex("^# .*")) -> inActions = false
            inActions -> linkRegex.find(line)?.let {
                result.add(ActionLink(it.groupValues[1], it.groupValues[3], it.groupValues[2]))
            }
        }
    }
    return result
}
