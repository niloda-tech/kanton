package kanton.core.cli.templates.buildgradlekts

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
    needsSerialization: Boolean = false,
): Template =
    bind(
        "deps" to implementation(mavenCoords).lines,
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
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  {{deps}}
  testImplementation(kotlin("test"))
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

""".template
