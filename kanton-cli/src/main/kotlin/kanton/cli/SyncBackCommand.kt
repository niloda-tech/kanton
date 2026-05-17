package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kanton.core.cli.SyncBack

class SyncBackCommand : CliktCommand(name = "sync-back") {
    override fun help(context: Context) = "Sync changes from exploded project back to .kt.md source"

    private val file by argument(help = "Path to the .kt.md source file").file(mustExist = true)

    override fun run() {
        when (val result = SyncBack.run(file)) {
            is SyncBack.Result.NoChanges -> echo("No changes to sync.")
            is SyncBack.Result.Synced -> echo("Synced: ${result.file.absolutePath}")
        }
    }
}
