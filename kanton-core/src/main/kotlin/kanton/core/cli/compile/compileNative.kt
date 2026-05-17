package kanton.core.cli.compile

import kanton.core.cli.scaffold.scaffoldProject
import java.io.File

fun compileNative(source: String, outputDir: File, outputName: String? = null): File {
    val scaffold = scaffoldProject(source, nativeImage = true)
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

    val buildProcess = ProcessBuilder("./gradlew", "nativeCompile")
        .directory(projectDir)
        .redirectErrorStream(true)
        .start()
    val buildOutput = buildProcess.inputStream.bufferedReader().readText()
    if (buildProcess.waitFor() != 0) error("./gradlew nativeCompile failed:\n$buildOutput")

    val nativeBinary = File(projectDir, "build/native/nativeCompile/$scriptName")
    if (!nativeBinary.exists()) error("Native binary not found: ${nativeBinary.path}")

    val outputFile = File(outputDir, outputName ?: scriptName)
    nativeBinary.copyTo(outputFile, overwrite = true)
    outputFile.setExecutable(true)

    return outputFile
}
