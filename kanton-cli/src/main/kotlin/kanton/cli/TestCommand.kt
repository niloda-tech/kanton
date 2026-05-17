package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kanton.core.cli.Scaffold
import java.io.File

class TestCommand : CliktCommand(name = "test") {
    override fun help(context: Context) = "Run tests for a .kt.md script"

    private val file by argument(help = "Path to the .kt.md source file").file(mustExist = true)

    override fun run() {
        val result = Scaffold.run(file)
        val projectDir = result.projectDir

        if (!File(projectDir, "gradlew").exists()) {
            echo("Error: No Gradle wrapper in ${projectDir.absolutePath}", err = true)
            throw kotlin.system.exitProcess(1)
        }

        echo("Running tests for ${file.name}...")
        val process = ProcessBuilder("./gradlew", "test")
            .directory(projectDir)
            .inheritIO()
            .start()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw kotlin.system.exitProcess(exitCode)
        }
        echo("Tests passed.")
    }
}
