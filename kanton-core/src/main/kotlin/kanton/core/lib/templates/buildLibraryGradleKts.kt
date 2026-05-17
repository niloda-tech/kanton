package kanton.core.lib.templates

import kanton.core.lib.models.LibCoords
import kanton.core.lib.models.LibDepEntry

fun buildLibraryGradleKts(coords: LibCoords, deps: List<LibDepEntry>) = buildString {
    appendLine("plugins {")
    appendLine("    kotlin(\"jvm\") version \"2.3.0\"")
    appendLine("    `java-library`")
    appendLine("    `maven-publish`")
    appendLine("}")
    appendLine()
    appendLine("group = \"${coords.group}\"")
    appendLine("version = \"${coords.version}\"")
    appendLine()
    appendLine("repositories {")
    appendLine("    mavenLocal()")
    appendLine("    mavenCentral()")
    appendLine("}")
    appendLine()
    if (deps.isNotEmpty()) {
        appendLine("dependencies {")
        val grouped = deps.groupBy { it.config }
        for (config in listOf("api", "implementation", "compileOnly", "runtimeOnly", "testImplementation", "testCompileOnly")) {
            grouped[config]?.forEach { appendLine("    $config(\"${it.coord}\")") }
        }
        appendLine("}")
        appendLine()
    }
    appendLine("kotlin { jvmToolchain(17) }")
    appendLine("java { withSourcesJar() }")
    appendLine()
    appendLine("publishing {")
    appendLine("    publications {")
    appendLine("        create<MavenPublication>(\"maven\") {")
    appendLine("            from(components[\"java\"])")
    appendLine("            groupId = \"${coords.group}\"")
    appendLine("            artifactId = \"${coords.artifact}\"")
    appendLine("            version = \"${coords.version}\"")
    appendLine("        }")
    appendLine("    }")
    appendLine("}")
}
