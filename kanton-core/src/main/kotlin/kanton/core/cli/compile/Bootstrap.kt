package kanton.core.cli.compile

import java.io.File
import java.security.MessageDigest

fun main(args: Array<String>) {
    val projectRoot = File(System.getProperty("user.dir"))
    val binDir = File(System.getProperty("user.home"), ".kanton/bin").also { it.mkdirs() }
    val scriptsDir = File(projectRoot, "kanton-core/scripts")

    val targets = if (args.isNotEmpty()) {
        args.map { File(it) }
    } else {
        listOf(
            File(scriptsDir, "kanton-compile.kt.md"),
            File(scriptsDir, "kanton-scaffold.kt.md"),
        )
    }

    for (script in targets) {
        val binary = File(binDir, script.nameWithoutExtension.removeSuffix(".kt"))
        val checksumFile = File(binDir, "${binary.name}.md5")
        val currentChecksum = MessageDigest.getInstance("MD5").let { md ->
            md.update(script.readBytes())
            md.digest().joinToString("") { "%02x".format(it) }
        }
        val savedChecksum = if (checksumFile.exists()) checksumFile.readText().trim() else null
        if (binary.exists() && currentChecksum == savedChecksum) {
            println("${binary.name}: up-to-date, skipping")
            continue
        }
        if (binary.exists()) {
            println("${binary.name}: source changed, recompiling...")
        }
        println("Bootstrapping ${script.name}...")
        val out = Compile.run(script, binDir)
        checksumFile.writeText(currentChecksum)
        println("  -> ${out.absolutePath}")
    }
}
