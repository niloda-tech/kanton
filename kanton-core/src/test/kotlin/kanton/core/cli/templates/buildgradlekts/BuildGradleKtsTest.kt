package kanton.core.cli.templates.buildgradlekts

import kotlin.test.Test

class BuildGradleKtsTest {

    @Test
    fun `build with single dependency`() {
        val result = buildGradleKts(
            scriptName = "my-script",
            mavenCoords = listOf("com.github.ajalt.clikt:clikt:5.1.0"),
            kotlinVersion = "2.3.0",
            shadowVersion = "9.4.0",
        )
        println(result)
    }

    @Test
    fun `build with multiple dependencies`() {
        val result = buildGradleKts(
            scriptName = "fetcher",
            mavenCoords = listOf(
                "com.github.ajalt.clikt:clikt:5.1.0",
                "io.ktor:ktor-client-core:3.1.0",
                "io.ktor:ktor-client-cio:3.1.0"
            ),
            kotlinVersion = "2.3.0",
            shadowVersion = "9.4.0",
        )
        println(result)
    }

    @Test
    fun `build with no dependencies`() {
        val result = buildGradleKts(
            scriptName = "bare",
            mavenCoords = emptyList(),
            kotlinVersion = "2.3.0",
            shadowVersion = "9.4.0",
        )
        println(result)
    }

    @Test
    fun `build with serialization plugin`() {
        val result = buildGradleKts(
            scriptName = "json-tool",
            mavenCoords = listOf("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1"),
            needsSerialization = true,
            kotlinVersion = "2.3.0",
            shadowVersion = "9.4.0",
        )
        println(result)
    }
}
