package kanton.core.cli.templates

val CLI_STUBS = """
package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main

fun stdinText(): String = generateSequence(::readLine).joinToString("\n")
fun runScript(command: CliktCommand, args: Array<String> = emptyArray()): Unit = command.main(args)
""".trimStart()
