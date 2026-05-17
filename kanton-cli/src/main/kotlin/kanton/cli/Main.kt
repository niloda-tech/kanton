package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

class Kanton : CliktCommand(name = "kanton") {
    override fun help(context: Context) = "Precision multi-tool for Kotlin scripts"
    override fun run() = Unit
}

fun main(args: Array<String>) {
    Kanton()
        .subcommands(
            ScaffoldCommand(),
            CompileCommand(),
            InstallCommand(),
            SyncBackCommand(),
            DeleteCommand(),
            TestCommand(),
            FormatCommand(),
            ListCommand(),
            RepoCommand(),
        )
        .main(args)
}
