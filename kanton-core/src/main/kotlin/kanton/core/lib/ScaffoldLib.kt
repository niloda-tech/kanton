package kanton.core.lib

import kanton.core.lib.models.LibraryScaffoldResult
import kanton.core.lib.scaffold.scaffoldLibraryProject
import java.io.File

object ScaffoldLib {
    fun run(file: File): LibraryScaffoldResult {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        val source = file.readText()
        return scaffoldLibraryProject(source, sourceFile = file)
            ?: error("Cannot parse library coordinates or body from ${file.name}")
    }
}
