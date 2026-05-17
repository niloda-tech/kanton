package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kanton.core.cli.compile.Compile
import kanton.core.shared.repos.RepoRegistry
import java.io.File
import java.security.MessageDigest

class InstallCommand : CliktCommand(name = "install") {
    override fun help(context: Context) = "Compile and install a script to ~/.kanton/bin/"

    private val target by argument(help = "Path to .kt.md file, or namespace:script")
    private val force by option("-f", "--force", help = "Recompile even if up-to-date").flag()

    override fun run() {
        val file = resolveTarget(target)
        val binDir = File(System.getProperty("user.home"), ".kanton/bin").also { it.mkdirs() }
        val scriptName = File(file.nameWithoutExtension).nameWithoutExtension
        val binPath = File(binDir, scriptName)
        val checksumPath = File(binDir, "$scriptName.md5")

        val currentChecksum = MessageDigest.getInstance("MD5").let { md ->
            md.update(file.readBytes())
            md.digest().joinToString("") { "%02x".format(it) }
        }
        val savedChecksum = if (checksumPath.exists()) checksumPath.readText().trim() else null

        if (!force && binPath.exists() && currentChecksum == savedChecksum) {
            echo("$scriptName: up-to-date")
            return
        }

        echo("Installing $scriptName...")
        val result = Compile.run(file, binDir)
        checksumPath.writeText(currentChecksum)
        echo("Installed: ${result.absolutePath}")
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
