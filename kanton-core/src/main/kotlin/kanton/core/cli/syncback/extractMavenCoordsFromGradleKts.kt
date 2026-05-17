package kanton.core.cli.syncback

fun extractMavenCoordsFromGradleKts(content: String): List<String> =
    content.lines().mapNotNull { line ->
        Regex("""implementation\("([^"]+)"\)""").find(line.trim())?.groupValues?.get(1)
    }
