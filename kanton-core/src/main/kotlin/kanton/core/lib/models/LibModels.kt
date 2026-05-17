package kanton.core.lib.models

data class LibCoords(val group: String, val artifact: String, val version: String, val help: String)

data class LibDepEntry(val coord: String, val config: String, val imports: List<String>)

data class LibraryScaffoldResult(val projectDir: java.io.File, val artifact: String, val coords: LibCoords)
