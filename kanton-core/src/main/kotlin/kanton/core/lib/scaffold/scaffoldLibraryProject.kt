package kanton.core.lib.scaffold

import kanton.core.lib.models.LibCoords
import kanton.core.lib.models.LibDepEntry
import kanton.core.lib.models.LibraryScaffoldResult
import kanton.core.lib.parsing.isLibFormat
import kanton.core.lib.parsing.parseLibMd
import kanton.core.lib.parsing.parseLibCoords
import kanton.core.lib.parsing.parseLibDeps
import kanton.core.lib.templates.buildLibraryGradleKts
import kanton.core.shared.createSymlink
import kanton.core.shared.models.Section
import kanton.core.shared.models.scriptNameToClassName
import kanton.core.shared.templates.settingsGradleKts
import java.io.File

fun scaffoldLibraryProject(source: String, sourceFile: File? = null): LibraryScaffoldResult? {
    if (!isLibFormat(source)) return null
    val sections = parseLibMd(source)
    val coords = parseCoordsFrom(sections) ?: return null
    val deps = parseDepsFrom(sections)
    val libKt = buildExplodedLibraryKotlin(source, coords.group) ?: return null
    val projectDir = writeProjectTo(cacheDir(coords.artifact), coords, deps, libKt)
    createSymlink(sourceFile, projectDir)
    return LibraryScaffoldResult(projectDir, coords.artifact, coords)
}

private fun sectionByTag(sections: List<Section>, tag: String) =
    sections.firstOrNull { it.tag == tag }

private fun parseCoordsFrom(sections: List<Section>): LibCoords? =
    sectionByTag(sections, "lib")?.lines?.firstOrNull()?.let(::parseLibCoords)

private fun parseDepsFrom(sections: List<Section>): List<LibDepEntry> =
    sectionByTag(sections, "deps")?.lines?.let(::parseLibDeps) ?: emptyList()

private fun cacheDir(artifact: String) =
    File(System.getProperty("user.home"), ".kanton/cache/$artifact")

private fun writeProjectTo(projectDir: File, coords: LibCoords, deps: List<LibDepEntry>, libKt: String): File {
    val srcDir = makeSrcDir(projectDir, coords.group)
    writeGradleFiles(projectDir, coords, deps)
    File(srcDir, "${scriptNameToClassName(coords.artifact)}.kt").writeText(libKt)
    return projectDir
}

private fun makeSrcDir(projectDir: File, group: String): File {
    val dir = File(projectDir, "src/main/kotlin/${group.replace('.', '/')}")
    dir.mkdirs()
    return dir
}

private fun writeGradleFiles(projectDir: File, coords: LibCoords, deps: List<LibDepEntry>) {
    File(projectDir, "settings.gradle.kts").writeText(settingsGradleKts(coords.artifact))
    File(projectDir, "build.gradle.kts").writeText(buildLibraryGradleKts(coords, deps))
}
