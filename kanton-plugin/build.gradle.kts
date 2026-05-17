import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

val cucumberVersion = "7.22.2"

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("org.jetbrains.kotlin")
        testFramework(TestFrameworkType.Platform)
    }
    implementation(project(":kanton-core"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("org.junit.platform:junit-platform-suite:1.11.4")
}

intellijPlatform {
    pluginConfiguration {
        id = "com.kanton.plugin"
        name = "Kanton Language Support"
        version = "0.1.0"
        description = "Language support for .kt.md files with Kotlin language injection in fenced code blocks."
        ideaVersion {
            sinceBuild = "243"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.jar {
    from(project(":kanton-core").sourceSets.main.get().output)
}
