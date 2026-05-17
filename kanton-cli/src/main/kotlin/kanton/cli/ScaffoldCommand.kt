package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kanton.core.cli.Scaffold

class ScaffoldCommand : CliktCommand(name = "scaffold") {
    override fun help(context: Context) = "Scaffold a Gradle project from a .kt.md file"

    private val file by argument(help = "Path to the .kt.md source file").file(mustExist = true)

    override fun run() {
        val result = Scaffold.run(file)
        echo("Scaffolded: ${result.projectDir.absolutePath}")
        echo("Open in IDE: open ${result.projectDir.absolutePath}")
    }
}
