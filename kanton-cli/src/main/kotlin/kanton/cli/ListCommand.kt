package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import kanton.core.shared.repos.RepoRegistry

class ListCommand : CliktCommand(name = "list") {
    override fun help(context: Context) = "List available scripts (optionally filter by namespace)"

    private val namespace by argument(help = "Namespace to list scripts from").optional()

    override fun run() {
        val repos = if (namespace != null) {
            RepoRegistry.list().filter { it.name == namespace }
        } else {
            RepoRegistry.list()
        }

        if (repos.isEmpty()) {
            if (namespace != null) {
                echo("No repo named '$namespace'. Use 'kanton repo list' to see registered repos.", err = true)
            } else {
                echo("No repositories configured. Use 'kanton repo add <name> <path>' to register one.")
            }
            return
        }

        for (repo in repos) {
            val scripts = RepoRegistry.scriptsIn(repo.name)
            if (scripts.isEmpty()) {
                echo("${repo.name}: (no scripts)")
            } else {
                for (script in scripts) {
                    val name = script.name.removeSuffix(".kt.md")
                    echo("${repo.name}:$name")
                }
            }
        }
    }
}
