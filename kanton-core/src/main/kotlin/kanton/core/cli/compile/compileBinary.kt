package kanton.core.cli.compile

import kanton.core.cli.scaffold.scaffoldProject
import java.io.File

fun compileBinary(source: String, outputDir: File, outputName: String? = null): File {
    val scaffold = scaffoldProject(source)
        ?: error("Cannot parse script name or run body from source")

    val (projectDir, scriptName) = scaffold

    if (!File(projectDir, "gradlew").exists()) {
        val wrapperProcess = ProcessBuilder("gradle", "wrapper")
            .directory(projectDir)
            .redirectErrorStream(true)
            .start()
        val wrapperOutput = wrapperProcess.inputStream.bufferedReader().readText()
        if (wrapperProcess.waitFor() != 0) error("gradle wrapper failed:\n$wrapperOutput")
    }

    val buildProcess = ProcessBuilder("./gradlew", "shadowJar")
        .directory(projectDir)
        .redirectErrorStream(true)
        .start()
    val buildOutput = buildProcess.inputStream.bufferedReader().readText()
    if (buildProcess.waitFor() != 0) error("./gradlew shadowJar failed:\n$buildOutput")

    val jarFile = File(projectDir, "build/libs/$scriptName-all.jar")
    if (!jarFile.exists()) error("Shadow JAR not found: ${jarFile.path}")

    val outputFile = File(outputDir, outputName ?: scriptName)
    val tmpFile = File(outputDir, ".${outputFile.name}.tmp")
    val stub = "#!/bin/sh\nexec java -jar \"\$0\" \"\$@\"\n"
    tmpFile.outputStream().use { out ->
        out.write(stub.toByteArray(Charsets.UTF_8))
        out.write(jarFile.readBytes())
    }
    tmpFile.setExecutable(true)
    tmpFile.renameTo(outputFile)

    return outputFile
}
