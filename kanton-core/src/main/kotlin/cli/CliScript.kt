package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main

abstract class CliScript(name: String? = null) : CliktCommand(name = name)

fun stdinText(): String = ""

fun runScript(command: CliktCommand, args: Array<String> = emptyArray()): Unit = command.main(args)
