package kanton.core

import kanton.core.lib.models.LibDepEntry
import kanton.core.lib.syncback.extractLibBodyFromKotlin
import kanton.core.lib.syncback.extractMavenCoordsWithConfigFromGradleKts
import kanton.core.lib.syncback.replaceLibBodyInLibMd
import kanton.core.lib.syncback.updateLibDepsInLibMd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibSyncbackTest {

    // --- extractLibBodyFromKotlin ---

    @Test
    fun `extractLibBodyFromKotlin strips leading imports and blank separator`() {
        val src = """
            import org.example.Foo
            import org.example.Bar

            class Thing {
                fun greet() = "hi"
            }
        """.trimIndent()
        val body = extractLibBodyFromKotlin(src)
        assertFalse(body.startsWith("import "))
        assertTrue(body.startsWith("class Thing"))
        assertTrue("fun greet()" in body)
    }

    @Test
    fun `extractLibBodyFromKotlin returns body verbatim when no imports`() {
        val src = "class Thing"
        assertEquals("class Thing", extractLibBodyFromKotlin(src))
    }

    @Test
    fun `extractLibBodyFromKotlin strips package directive and imports`() {
        val src = """
            package com.example

            import org.example.Foo

            class Thing
        """.trimIndent()
        val body = extractLibBodyFromKotlin(src)
        assertFalse(body.startsWith("package "))
        assertFalse(body.startsWith("import "))
        assertEquals("class Thing", body)
    }

    @Test
    fun `extractLibBodyFromKotlin strips package directive when no imports`() {
        val src = """
            package com.example

            class Thing
        """.trimIndent()
        assertEquals("class Thing", extractLibBodyFromKotlin(src))
    }

    @Test
    fun `extractLibBodyFromKotlin trims trailing whitespace`() {
        val src = "class Thing\n\n\n"
        assertEquals("class Thing", extractLibBodyFromKotlin(src))
    }

    // --- replaceLibBodyInLibMd ---

    private val source = """
        # my-lib - a library

        ```lib
        com.example:my-lib:1.0

        class Old
        ```

        # dependencies
        org.example:dep:1.0
    """.trimIndent()

    @Test
    fun `replaceLibBodyInLibMd replaces body inside fence`() {
        val result = replaceLibBodyInLibMd(source, "class New")
        assertTrue("class New" in result)
        assertFalse("class Old" in result)
    }

    @Test
    fun `replaceLibBodyInLibMd preserves header line and surrounding text`() {
        val result = replaceLibBodyInLibMd(source, "class New")
        assertTrue("com.example:my-lib:1.0" in result, "header line lost")
        assertTrue("# my-lib - a library" in result, "prose above fence lost")
        assertTrue("# dependencies" in result, "deps header lost")
    }

    @Test
    fun `replaceLibBodyInLibMd returns source unchanged when no fence present`() {
        val text = "just some markdown\nwith no fence"
        assertEquals(text, replaceLibBodyInLibMd(text, "class New"))
    }

    // --- extractMavenCoordsWithConfigFromGradleKts ---

    @Test
    fun `extractMavenCoordsWithConfigFromGradleKts extracts all six configs`() {
        val content = """
            dependencies {
                api("org.example:a:1.0")
                implementation("org.example:b:1.0")
                compileOnly("org.example:c:1.0")
                runtimeOnly("org.example:d:1.0")
                testImplementation("org.example:e:1.0")
                testCompileOnly("org.example:f:1.0")
            }
        """.trimIndent()
        val pairs = extractMavenCoordsWithConfigFromGradleKts(content)
        assertEquals(6, pairs.size)
        assertTrue("api" to "org.example:a:1.0" in pairs)
        assertTrue("implementation" to "org.example:b:1.0" in pairs)
        assertTrue("compileOnly" to "org.example:c:1.0" in pairs)
        assertTrue("runtimeOnly" to "org.example:d:1.0" in pairs)
        assertTrue("testImplementation" to "org.example:e:1.0" in pairs)
        assertTrue("testCompileOnly" to "org.example:f:1.0" in pairs)
    }

    @Test
    fun `extractMavenCoordsWithConfigFromGradleKts ignores non-dep lines`() {
        val content = """
            plugins { kotlin("jvm") version "2.3.0" }
            group = "com.example"
            dependencies {
                api("org.example:only:1.0")
            }
        """.trimIndent()
        val pairs = extractMavenCoordsWithConfigFromGradleKts(content)
        assertEquals(listOf("api" to "org.example:only:1.0"), pairs)
    }

    // --- updateLibDepsInLibMd ---

    @Test
    fun `updateLibDepsInLibMd omits implementation prefix and keeps others`() {
        val src = """
            ```lib
            com.example:lib:1.0

            class Foo
            ```

            # dependencies
            old:dep:0.0
        """.trimIndent()
        val deps = listOf(
            LibDepEntry("org.example:a:1.0", "implementation", emptyList()),
            LibDepEntry("org.example:b:2.0", "api", emptyList())
        )
        val result = updateLibDepsInLibMd(src, deps)
        assertTrue("org.example:a:1.0" in result)
        assertFalse("implementation org.example:a:1.0" in result, "implementation prefix should be omitted")
        assertTrue("api org.example:b:2.0" in result, "api prefix should be kept")
        assertFalse("old:dep:0.0" in result, "old deps should be replaced")
    }

    @Test
    fun `updateLibDepsInLibMd emits imports with two-space indent`() {
        val src = """
            ```lib
            com.example:lib:1.0

            class Foo
            ```

            # dependencies
            placeholder:x:0
        """.trimIndent()
        val deps = listOf(
            LibDepEntry("org.example:a:1.0", "implementation", listOf("org.example.Foo", "org.example.Bar"))
        )
        val result = updateLibDepsInLibMd(src, deps)
        assertTrue("\n  org.example.Foo" in result, "import should be 2-space indented")
        assertTrue("\n  org.example.Bar" in result)
    }

    @Test
    fun `updateLibDepsInLibMd creates dependencies section when missing`() {
        val src = """
            ```lib
            com.example:lib:1.0

            class Foo
            ```
        """.trimIndent()
        val deps = listOf(LibDepEntry("org.example:a:1.0", "api", emptyList()))
        val result = updateLibDepsInLibMd(src, deps)
        assertTrue("# dependencies" in result, "deps section should be created")
        assertTrue("api org.example:a:1.0" in result)
    }
}
