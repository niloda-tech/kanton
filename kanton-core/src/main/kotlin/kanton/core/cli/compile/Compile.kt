package kanton.core.cli.compile

import java.io.File

object Compile {
    fun run(file: File, outputDir: File? = null, outputName: String? = null): File {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        val source = file.readText()
        val dir = outputDir ?: file.parentFile
        return compileBinary(source, dir, outputName)
    }
}
