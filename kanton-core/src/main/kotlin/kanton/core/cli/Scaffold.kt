package kanton.core.cli

import kanton.core.cli.scaffold.ScaffoldResult
import kanton.core.cli.scaffold.scaffoldProject
import java.io.File

object Scaffold {
    fun run(file: File): ScaffoldResult {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        val source = file.readText()
        return scaffoldProject(source, sourceFile = file)
            ?: error("Cannot parse script name or run body from ${file.name}")
    }
}
