package kanton.core.cli

import kanton.core.cli.parsing.parseScriptName
import java.io.File

object DeleteScaffold {
    fun run(name: String): File {
        val projectDir = File(System.getProperty("user.home"), ".kanton/cache/$name")
        require(projectDir.exists()) { "Project not found: ${projectDir.absolutePath}" }
        projectDir.deleteRecursively()
        return projectDir
    }

    fun run(file: File): File {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        val name = parseScriptName(file.readText())
            ?: error("Cannot parse script name from ${file.name}")
        return run(name)
    }
}
