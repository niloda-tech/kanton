package kanton.core.cli.templates.buildgradlekts

import kanton.core.cli.templates.nativePlugin
import kanton.core.shared.Template
import kanton.core.shared.bind
import kanton.core.shared.lines
import kanton.core.shared.template
import kanton.core.shared.toTemplate
import kotlin.String

fun buildGradleKts(
    mavenCoords: List<String>,
    scriptName: String,
    kotlinVersion: String,
    shadowVersion: String,
    nativeVersion: String,
    nativeImage: Boolean = false,
    needsSerialization: Boolean = false,
): Template =
    bind(
        "deps" to implementation(mavenCoords).lines,
        "nativeBlock" to if (nativeImage) nativeBlock(scriptName).value else "",
        "nativePlugin" to if (nativeImage) nativePlugin(nativeVersion).value else "",
        "serializationPlugin" to if (needsSerialization) """kotlin("plugin.serialization") ${pluginVersion(kotlinVersion)}""" else "",
        "kotlinVersion" to pluginVersion(kotlinVersion),
        "shadowVersion" to pluginVersion(shadowVersion)
    ) toTemplate $$"""
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  kotlin("jvm") {{kotlinVersion}}
  {{serializationPlugin}}
  application
  id("com.gradleup.shadow") {{shadowVersion}}
  {{nativePlugin}}
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  {{deps}}
  components.all {
      if (id.group.startsWith("io.ktor")) {
          belongsTo("io.ktor:ktor-bom:${id.version}", false)
      }
  }
}

application { mainClass.set("MainKt") }
kotlin { jvmToolchain(17) }

tasks.named<ShadowJar>("shadowJar") {
  archiveClassifier.set("all")
  mergeServiceFiles()
}

{{nativeBlock}}

""".template
