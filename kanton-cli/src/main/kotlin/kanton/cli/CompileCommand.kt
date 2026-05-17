package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import kanton.core.cli.compile.Compile

class CompileCommand : CliktCommand(name = "compile") {
    override fun help(context: Context) = "Compile a .kt.md file into a self-executing binary"

    private val file by argument(help = "Path to the .kt.md source file").file(mustExist = true)
    private val output by option("-o", "--output", help = "Output directory for the binary").file()

    override fun run() {
        val outDir = output ?: file.parentFile
        val result = Compile.run(file, outDir)
        echo("Binary: ${result.absolutePath}")
    }
}
