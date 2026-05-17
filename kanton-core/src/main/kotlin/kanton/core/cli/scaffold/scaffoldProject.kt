package kanton.core.cli.scaffold

import kanton.core.cli.parsing.parseDeps
import kanton.core.cli.parsing.parseCliMd
import kanton.core.cli.templates.CLI_STUBS
import kanton.core.cli.templates.buildgradlekts.buildGradleKts
import kanton.core.shared.createSymlink
import kanton.core.shared.models.parseScriptHeader
import kanton.core.shared.templates.settingsGradleKts
import java.io.File

data class ScaffoldResult(val projectDir: File, val scriptName: String)

fun scaffoldProject(source: String, sourceFile: File? = null): ScaffoldResult? {
    val sections = parseCliMd(source)
    val cliSection = sections.firstOrNull { it.tag == "cli" } ?: return null
    val firstLine = cliSection.lines.firstOrNull() ?: return null
    val header = parseScriptHeader(firstLine)

    val depsSection = sections.firstOrNull { it.tag == "deps" }
    val depEntries = depsSection?.let { parseDeps(it.lines) } ?: emptyList()
    val mavenCoords = depEntries.map { it.coord }
    val needsSerialization = depEntries.any { dep -> dep.imports.any { it.startsWith("kotlinx.serialization") } }

    val mainKt = buildExplodedKotlin(source) ?: return null

    val projectDir = File(System.getProperty("user.home"), ".kanton/cache/${header.scriptName}")
    val srcDir = File(projectDir, "src/main/kotlin")
    val testDir = File(projectDir, "src/test/kotlin")
    val kantonDir = File(srcDir, "kanton")
    srcDir.mkdirs()
    testDir.mkdirs()
    kantonDir.mkdirs()

    File(projectDir, "settings.gradle.kts").writeText(
        settingsGradleKts(header.scriptName)
    )
    File(projectDir, "build.gradle.kts").writeText(
        buildGradleKts(
            scriptName = header.scriptName,
            mavenCoords = mavenCoords,
            needsSerialization = needsSerialization,
            kotlinVersion = "2.3.0",
            shadowVersion = "9.4.0",
        ).value
    )
    File(kantonDir, "Stubs.kt").writeText(CLI_STUBS)
    File(srcDir, "Main.kt").writeText(mainKt)

    val testFile = File(testDir, "${header.className}Test.kt")
    if (!testFile.exists()) {
        testFile.writeText(testTemplate(header.className, header.scriptName).value)
    }

    createSymlink(sourceFile, projectDir)

    return ScaffoldResult(projectDir, header.scriptName)
}
