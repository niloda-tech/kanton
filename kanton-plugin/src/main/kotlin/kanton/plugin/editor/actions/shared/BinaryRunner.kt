package kanton.plugin.editor.actions.shared

import java.io.File

fun findKantonBinary(
    sourceFile: File,
    systemPath: String? = System.getenv("PATH"),
    homeDir: File? = File(System.getProperty("user.home"), ".kanton/bin/kanton")
): File? {
    if (homeDir != null && homeDir.exists() && homeDir.canExecute()) return homeDir

    var dir = sourceFile.parentFile
    while (dir != null) {
        for (rel in listOf("scripts/kanton", "bin/kanton")) {
            val candidate = File(dir, rel)
            if (candidate.exists() && candidate.canExecute()) return candidate
        }
        dir = dir.parentFile
    }
    if (systemPath == null) return null
    for (entry in systemPath.split(File.pathSeparator)) {
        val candidate = File(entry, "kanton")
        if (candidate.exists() && candidate.canExecute()) return candidate
    }
    return null
}

data class BinaryResult(val stdout: String, val stderr: String, val exitCode: Int)

fun runBinary(binary: File, vararg args: String): BinaryResult {
    val proc = ProcessBuilder(listOf(binary.absolutePath) + args.toList())
        .redirectErrorStream(false)
        .start()
    val stdout = proc.inputStream.bufferedReader().readText()
    val stderr = proc.errorStream.bufferedReader().readText()
    val exitCode = proc.waitFor()
    return BinaryResult(stdout, stderr, exitCode)
}
