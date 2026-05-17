package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import kanton.core.cli.Scaffold
import kanton.core.shared.repos.RepoRegistry
import java.io.File

class TestCommand : CliktCommand(name = "test") {
    override fun help(context: Context) = "Run tests for a .kt.md script"

    private val target by argument(help = "Path to .kt.md file, or namespace:script")

    override fun run() {
        val file = resolveTarget(target)
        val result = Scaffold.run(file)
        val projectDir = result.projectDir

        if (!File(projectDir, "gradlew").exists()) {
            echo("Generating Gradle wrapper...")
            val wrapper = ProcessBuilder("gradle", "wrapper")
                .directory(projectDir)
                .redirectErrorStream(true)
                .start()
            val wrapperOutput = wrapper.inputStream.bufferedReader().readText()
            if (wrapper.waitFor() != 0) {
                echo("Error: gradle wrapper failed:\n$wrapperOutput", err = true)
                throw kotlin.system.exitProcess(1)
            }
        }

        echo("Running tests in ${projectDir.absolutePath}...")
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

    private fun resolveTarget(target: String): File {
        val ref = RepoRegistry.parseRef(target)
        if (ref != null) {
            return RepoRegistry.resolve(ref)
                ?: error("Script not found: $ref (check 'kanton repo list' for registered namespaces)")
        }
        val file = File(target)
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        return file
    }
}
