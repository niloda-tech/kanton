package kanton.core.lib

import kanton.core.lib.scaffold.scaffoldLibraryProject
import java.io.File

sealed class PublishResult {
    data class Published(val projectDir: File, val artifact: String) : PublishResult()
    data class Failed(val reason: String) : PublishResult()
}

object Publish {
    fun run(file: File): PublishResult {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        val source = file.readText()
        val scaffold = scaffoldLibraryProject(source, sourceFile = file)
            ?: return PublishResult.Failed("Cannot parse library coordinates or body from ${file.name}")

        val projectDir = scaffold.projectDir

        if (!File(projectDir, "gradlew").exists()) {
            val wrapperProcess = ProcessBuilder("gradle", "wrapper")
                .directory(projectDir)
                .redirectErrorStream(true)
                .start()
            val wrapperOutput = wrapperProcess.inputStream.bufferedReader().readText()
            if (wrapperProcess.waitFor() != 0)
                return PublishResult.Failed("gradle wrapper failed:\n$wrapperOutput")
        }

        val publishProcess = ProcessBuilder("./gradlew", "publishToMavenLocal")
            .directory(projectDir)
            .redirectErrorStream(true)
            .start()
        val publishOutput = publishProcess.inputStream.bufferedReader().readText()
        if (publishProcess.waitFor() != 0)
            return PublishResult.Failed("./gradlew publishToMavenLocal failed:\n$publishOutput")

        return PublishResult.Published(projectDir, scaffold.artifact)
    }
}
