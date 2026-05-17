package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import kanton.core.cli.DeleteScaffold
import java.io.File

class DeleteCommand : CliktCommand(name = "delete") {
    override fun help(context: Context) = "Remove a scaffolded project from the cache"

    private val target by argument(help = "Script name or path to .kt.md file")

    override fun run() {
        val targetFile = File(target)
        val deleted = if (targetFile.exists() && targetFile.isFile) {
            DeleteScaffold.run(targetFile)
        } else {
            DeleteScaffold.run(target)
        }
        echo("Deleted: ${deleted.absolutePath}")
    }
}
