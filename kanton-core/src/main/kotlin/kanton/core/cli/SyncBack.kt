package kanton.core.cli

import kanton.core.cli.parsing.parseScriptName
import kanton.core.cli.syncback.extractDepImportsFromMainKt
import kanton.core.cli.syncback.extractMavenCoordsFromGradleKts
import kanton.core.cli.syncback.extractRunBodyFromMainKt
import kanton.core.cli.syncback.replaceRunSectionInCliMd
import kanton.core.cli.syncback.updateDepsInCliMd
import java.io.File

object SyncBack {
    sealed class Result {
        object NoChanges : Result()
        data class Synced(val file: File) : Result()
    }

    fun run(file: File): Result {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        val source = file.readText()
        val name = parseScriptName(source)
            ?: error("Cannot parse script name from ${file.name}")

        val projectRoot = File(System.getProperty("user.home"), ".kanton/cache/$name")
        val mainKtFile = File(projectRoot, "src/main/kotlin/Main.kt")
        require(mainKtFile.exists()) {
            "No exploded project found at ${projectRoot.absolutePath}. Run Scaffold first."
        }
        val mainKt = mainKtFile.readText()

        val newRunBody = extractRunBodyFromMainKt(mainKt)
            ?: error("Cannot extract run body from Main.kt")

        val gradleKts = File(projectRoot, "build.gradle.kts").takeIf { it.exists() }
        val newCoords = gradleKts?.let { extractMavenCoordsFromGradleKts(it.readText()) } ?: emptyList()
        val newImports = extractDepImportsFromMainKt(mainKt)

        val updated = updateDepsInCliMd(
            replaceRunSectionInCliMd(source, newRunBody),
            newCoords,
            newImports
        )

        if (updated == source) return Result.NoChanges
        file.writeText(updated)
        return Result.Synced(file)
    }
}
