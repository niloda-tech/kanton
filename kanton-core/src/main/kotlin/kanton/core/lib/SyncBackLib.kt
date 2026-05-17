package kanton.core.lib

import kanton.core.lib.models.LibDepEntry
import kanton.core.lib.parsing.parseLibMd
import kanton.core.lib.parsing.parseLibCoords
import kanton.core.lib.parsing.parseLibDeps
import kanton.core.lib.syncback.extractLibBodyFromKotlin
import kanton.core.lib.syncback.extractMavenCoordsWithConfigFromGradleKts
import kanton.core.lib.syncback.replaceLibBodyInLibMd
import kanton.core.lib.syncback.updateLibDepsInLibMd
import kanton.core.shared.models.scriptNameToClassName
import java.io.File

sealed class SyncBackLibResult {
    object NoChanges : SyncBackLibResult()
    data class Synced(val file: File, val artifact: String) : SyncBackLibResult()
    data class Failed(val reason: String) : SyncBackLibResult()
}

object SyncBackLib {
    fun run(file: File): SyncBackLibResult {
        if (!file.exists()) return SyncBackLibResult.Failed("File not found: ${file.absolutePath}")
        val source = file.readText()

        val sections = parseLibMd(source)
        val libSection = sections.firstOrNull { it.tag == "lib" }
            ?: return SyncBackLibResult.Failed("Cannot find lib fence in ${file.name}")
        val headerLine = libSection.lines.firstOrNull()
            ?: return SyncBackLibResult.Failed("Empty lib fence header in ${file.name}")
        val coords = parseLibCoords(headerLine)
            ?: return SyncBackLibResult.Failed("Cannot parse library coordinates from ${file.name}")

        val artifact = coords.artifact
        val projectRoot = File(System.getProperty("user.home"), ".kanton/cache/$artifact")
        val className = scriptNameToClassName(artifact)
        val packageDir = coords.group.replace('.', '/')
        val libKtFile = File(projectRoot, "src/main/kotlin/$packageDir/$className.kt")

        if (!libKtFile.exists())
            return SyncBackLibResult.Failed(
                "No exploded project found at ${projectRoot.absolutePath}. Run Scaffold first."
            )

        val libKt = libKtFile.readText()
        val newBody = extractLibBodyFromKotlin(libKt)

        val gradleKts = File(projectRoot, "build.gradle.kts")
        val coordsWithConfig = if (gradleKts.exists())
            extractMavenCoordsWithConfigFromGradleKts(gradleKts.readText())
        else emptyList()

        val existingDepsSection = sections.firstOrNull { it.tag == "deps" }
        val existingDeps = existingDepsSection?.let { parseLibDeps(it.lines) } ?: emptyList()

        val newDeps: List<LibDepEntry> = coordsWithConfig.map { (config, coord) ->
            val existing = existingDeps.firstOrNull { it.coord == coord }
            LibDepEntry(coord, config, existing?.imports ?: emptyList())
        }

        var updated = replaceLibBodyInLibMd(source, newBody)
        updated = updateLibDepsInLibMd(updated, newDeps)

        if (updated == source) return SyncBackLibResult.NoChanges
        file.writeText(updated)
        return SyncBackLibResult.Synced(file, artifact)
    }
}
