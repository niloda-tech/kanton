package kanton.core.shared.repos

import java.io.File

data class RepoEntry(val name: String, val path: File)

data class ScriptRef(val namespace: String, val script: String) {
    override fun toString() = "$namespace:$script"
}

object RepoRegistry {

    private val configFile: File
        get() = File(System.getProperty("user.home"), ".kanton/repos.toml")

    fun parseRef(address: String): ScriptRef? {
        val parts = address.split(":", limit = 2)
        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) return null
        return ScriptRef(parts[0].trim(), parts[1].trim())
    }

    fun resolve(ref: ScriptRef): File? {
        val repo = list().find { it.name == ref.namespace } ?: return null
        val scriptFile = File(repo.path, "${ref.script}.kt.md")
        return if (scriptFile.exists()) scriptFile else null
    }

    fun list(): List<RepoEntry> {
        if (!configFile.exists()) return emptyList()
        return parseToml(configFile.readText())
    }

    fun add(name: String, path: File) {
        require(name.matches(Regex("[a-z][a-z0-9_-]*"))) {
            "Namespace must be lowercase alphanumeric (with - or _), starting with a letter"
        }
        require(path.isDirectory) { "Path must be an existing directory: ${path.absolutePath}" }
        val entries = list().toMutableList()
        entries.removeAll { it.name == name }
        entries.add(RepoEntry(name, path))
        writeToml(entries)
    }

    fun remove(name: String): Boolean {
        val entries = list().toMutableList()
        val removed = entries.removeAll { it.name == name }
        if (removed) writeToml(entries)
        return removed
    }

    fun scriptsIn(namespace: String): List<File> {
        val repo = list().find { it.name == namespace } ?: return emptyList()
        return repo.path.listFiles()
            ?.filter { it.extension == "md" && it.name.endsWith(".kt.md") }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    private fun parseToml(text: String): List<RepoEntry> {
        val entries = mutableListOf<RepoEntry>()
        var inRepos = false
        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed == "[repos]") { inRepos = true; continue }
            if (trimmed.startsWith("[")) { inRepos = false; continue }
            if (!inRepos || trimmed.isBlank() || trimmed.startsWith("#")) continue
            val eq = trimmed.indexOf('=')
            if (eq < 0) continue
            val key = trimmed.substring(0, eq).trim()
            val value = trimmed.substring(eq + 1).trim().removeSurrounding("\"")
            if (key.isNotBlank() && value.isNotBlank()) {
                entries.add(RepoEntry(key, File(value)))
            }
        }
        return entries
    }

    private fun writeToml(entries: List<RepoEntry>) {
        configFile.parentFile.mkdirs()
        val sb = StringBuilder()
        sb.appendLine("[repos]")
        for (entry in entries.sortedBy { it.name }) {
            sb.appendLine("${entry.name} = \"${entry.path.absolutePath}\"")
        }
        configFile.writeText(sb.toString())
    }
}
