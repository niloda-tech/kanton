package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kanton.core.shared.repos.RepoRegistry
import java.io.File

class RepoCommand : CliktCommand(name = "repo") {
    override fun help(context: Context) = "Manage script repositories (namespaces)"
    override fun run() = Unit

    init {
        subcommands(RepoAddCommand(), RepoRemoveCommand(), RepoListCommand())
    }
}

class RepoAddCommand : CliktCommand(name = "add") {
    override fun help(context: Context) = "Register a local directory as a script namespace"

    private val name by argument(help = "Namespace name (lowercase, e.g. utils)")
    private val path by argument(help = "Path to directory containing .kt.md scripts").file(mustExist = true, canBeFile = false)

    override fun run() {
        RepoRegistry.add(name, path)
        val count = RepoRegistry.scriptsIn(name).size
        echo("Added repo '$name' → ${path.absolutePath} ($count scripts)")
    }
}

class RepoRemoveCommand : CliktCommand(name = "remove") {
    override fun help(context: Context) = "Unregister a script namespace"

    private val name by argument(help = "Namespace name to remove")

    override fun run() {
        if (RepoRegistry.remove(name)) {
            echo("Removed repo '$name'")
        } else {
            echo("No repo named '$name'", err = true)
        }
    }
}

class RepoListCommand : CliktCommand(name = "list") {
    override fun help(context: Context) = "List registered script repositories"

    override fun run() {
        val repos = RepoRegistry.list()
        if (repos.isEmpty()) {
            echo("No repositories configured. Use 'kanton repo add <name> <path>' to register one.")
            return
        }
        for (repo in repos) {
            val count = RepoRegistry.scriptsIn(repo.name).size
            echo("${repo.name} → ${repo.path.absolutePath} ($count scripts)")
        }
    }
}
