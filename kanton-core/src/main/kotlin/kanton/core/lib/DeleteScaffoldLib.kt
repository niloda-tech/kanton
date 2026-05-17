package kanton.core.lib

import kanton.core.lib.parsing.parseLibMd
import kanton.core.lib.parsing.parseLibCoords
import java.io.File

object DeleteScaffoldLib {
    fun run(artifact: String): File {
        val projectDir = File(System.getProperty("user.home"), ".kanton/cache/$artifact")
        require(projectDir.exists()) { "Project not found: ${projectDir.absolutePath}" }
        projectDir.deleteRecursively()
        return projectDir
    }

    fun run(file: File): File {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        val source = file.readText()
        val sections = parseLibMd(source)
        val headerLine = sections.firstOrNull { it.tag == "lib" }?.lines?.firstOrNull()
            ?: error("Cannot find lib fence in ${file.name}")
        val coords = parseLibCoords(headerLine)
            ?: error("Cannot parse library coordinates from ${file.name}")
        return run(coords.artifact)
    }
}
