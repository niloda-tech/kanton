package kanton.core.cli.parsing

import kanton.core.cli.models.ArgumentConfig

private val WITH_TYPE_AND_DEFAULT = Regex("""^(\w+),\s+(.+?)\s+:\s+(\w+\(.*?\))\s+=\s+(.+)$""")

private val WITH_TYPE_ONLY = Regex("""^(\w+),\s+(.+?)\s+:\s+(\w+\(.*?\))$""")

private val WITH_COLON_DEFAULT = Regex("""^(\w+),\s+(.+?)\s+:\s+(.+)$""")

private val WITH_EQUALS_DEFAULT = Regex("""^(\w+),\s+(.+?)\s+=\s+(.+)$""")

private val NAME_AND_HELP = Regex("""^(\w+),\s+(.+)$""")

fun parseArgumentLine(line: String): ArgumentConfig {
    val a = WITH_TYPE_AND_DEFAULT.matchEntire(line)
    val b = WITH_TYPE_ONLY.matchEntire(line)
    val c = WITH_COLON_DEFAULT.matchEntire(line)
    val d = WITH_EQUALS_DEFAULT.matchEntire(line)
    val e = NAME_AND_HELP.matchEntire(line)
    return when {
        a != null -> a.groupValues.let { ArgumentConfig(name = it[1], help = it[2], typeFunc = it[3], default = it[4]) }
        b != null -> b.groupValues.let { ArgumentConfig(name = it[1], help = it[2], typeFunc = it[3], default = null) }
        c != null -> c.groupValues.let { ArgumentConfig(name = it[1], help = it[2], typeFunc = null, default = it[3]) }
        d != null -> d.groupValues.let { ArgumentConfig(name = it[1], help = it[2], typeFunc = null, default = it[3]) }
        e != null -> e.groupValues.let { ArgumentConfig(name = it[1], help = it[2], typeFunc = null, default = null) }
        else -> ArgumentConfig(name = line.trim(), help = "", typeFunc = null, default = null)
    }
}
