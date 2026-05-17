package kanton.core.cli.executor

import kanton.core.cli.compile.Compile
import java.io.File
import java.security.MessageDigest

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("Usage: kanton-executor <script.kt.md> [args...]")
        kotlin.system.exitProcess(1)
    }
    val scriptPath = args[0]
    val scriptArgs = args.drop(1)

    val binDir = File(System.getProperty("user.home"), ".kanton/bin")
    val scriptFile = File(scriptPath).canonicalFile
    if (!scriptFile.exists()) {
        System.err.println("Error: File not found: ${scriptFile.absolutePath}")
        kotlin.system.exitProcess(1)
    }
    val scriptName = File(scriptFile.nameWithoutExtension).nameWithoutExtension
    val binPath = File(binDir, scriptName)
    val checksumPath = File(binDir, "$scriptName.md5")

    val currentChecksum = MessageDigest.getInstance("MD5").let { md ->
        md.update(scriptFile.readBytes())
        md.digest().joinToString("") { "%02x".format(it) }
    }
    val savedChecksum = if (checksumPath.exists()) checksumPath.readText().trim() else null
    val needsCompile = !binPath.exists() || currentChecksum != savedChecksum

    if (needsCompile) {
        val reason = if (!binPath.exists()) "Binary not found" else "Source changed"
        println("$reason — compiling $scriptName...")
        val result = Compile.run(scriptFile, binDir)
        checksumPath.writeText(currentChecksum)
        println("Compiled: ${result.absolutePath}")
    }

    val pb = ProcessBuilder(listOf(binPath.absolutePath) + scriptArgs)
    pb.inheritIO()
    val exitCode = pb.start().waitFor()
    kotlin.system.exitProcess(exitCode)
}
