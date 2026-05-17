package kanton.core.cli.parsing

import kanton.core.cli.models.OptionConfig

private val NAME_PATTERN = """(-[-\w]+(?:,\s+-[-\w]+)*)"""

private val WITH_TYPE_AND_DEFAULT = Regex("""^$NAME_PATTERN,\s+(.+?)\s+:\s+(\w+\(.*?\))\s+=\s+(.+)$""")

private val WITH_TYPE_ONLY = Regex("""^$NAME_PATTERN,\s+(.+?)\s+:\s+(\w+\(.*?\))$""")

private val WITH_DEFAULT = Regex("""^$NAME_PATTERN,\s+(.+?)\s+=\s+(.+)$""")

private val NAMES_AND_HELP = Regex("""^$NAME_PATTERN,\s+(.+)$""")

private val NAMES_ONLY = Regex("""^$NAME_PATTERN$""")

private fun parseNames(raw: String): List<String> = raw.split(", ").map { it.trim() }

fun parseOptionLine(line: String): OptionConfig {
    val a = WITH_TYPE_AND_DEFAULT.matchEntire(line)
    val b = WITH_TYPE_ONLY.matchEntire(line)
    val c = WITH_DEFAULT.matchEntire(line)
    val d = NAMES_AND_HELP.matchEntire(line)
    val e = NAMES_ONLY.matchEntire(line)
    return when {
        a != null -> a.groupValues.let { OptionConfig(names = parseNames(it[1]), help = it[2], typeFunc = it[3], default = it[4]) }
        b != null -> b.groupValues.let { OptionConfig(names = parseNames(it[1]), help = it[2], typeFunc = it[3], default = null) }
        c != null -> c.groupValues.let { OptionConfig(names = parseNames(it[1]), help = it[2], typeFunc = null, default = it[3]) }
        d != null -> d.groupValues.let { OptionConfig(names = parseNames(it[1]), help = it[2], typeFunc = null, default = null) }
        e != null -> e.groupValues.let { OptionConfig(names = parseNames(it[1]), help = "", typeFunc = null, default = null) }
        else -> OptionConfig(names = listOf(line.trim()), help = "", typeFunc = null, default = null)
    }
}
