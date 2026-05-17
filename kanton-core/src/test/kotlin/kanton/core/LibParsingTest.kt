package kanton.core

import kanton.core.lib.parsing.isLibFormat
import kanton.core.lib.parsing.parseLibMd
import kanton.core.lib.parsing.parseLibCoords
import kanton.core.lib.parsing.parseLibDepLine
import kanton.core.lib.parsing.parseLibDeps
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LibParsingTest {

    // --- isLibFormat ---

    @Test
    fun `isLibFormat returns true for a lib fence`() {
        val source = """
            ```lib
            com.example:lib:1.0

            class Foo
            ```
        """.trimIndent()
        assertTrue(isLibFormat(source))
    }

    @Test
    fun `isLibFormat returns false for a cli fence`() {
        val source = """
            ```cli
            greet:Hello

            echo("hi")
            ```
        """.trimIndent()
        assertFalse(isLibFormat(source))
    }

    @Test
    fun `isLibFormat returns false for plain text`() {
        assertFalse(isLibFormat("just text, no fences"))
    }

    // --- parseLibCoords ---

    @Test
    fun `parseLibCoords parses minimal group artifact version`() {
        val coords = parseLibCoords("com.example:lib:1.0")
        assertNotNull(coords)
        assertEquals("com.example", coords!!.group)
        assertEquals("lib", coords.artifact)
        assertEquals("1.0", coords.version)
        assertEquals("", coords.help)
    }

    @Test
    fun `parseLibCoords parses help text after version`() {
        val coords = parseLibCoords("com.example:lib:1.0:A simple library")
        assertNotNull(coords)
        assertEquals("A simple library", coords!!.help)
    }

    @Test
    fun `parseLibCoords preserves colons inside help text`() {
        val coords = parseLibCoords("com.example:lib:1.0:Usage: foo:bar")
        assertNotNull(coords)
        assertEquals("Usage: foo:bar", coords!!.help)
    }

    @Test
    fun `parseLibCoords returns null when fewer than three tokens`() {
        assertNull(parseLibCoords("com.example:lib"))
    }

    @Test
    fun `parseLibCoords returns null when group is blank`() {
        assertNull(parseLibCoords(":lib:1.0"))
    }

    @Test
    fun `parseLibCoords returns null when artifact is blank`() {
        assertNull(parseLibCoords("com.example::1.0"))
    }

    @Test
    fun `parseLibCoords returns null when version is blank`() {
        assertNull(parseLibCoords("com.example:lib:"))
    }

    // --- parseLibDepLine ---

    @Test
    fun `parseLibDepLine recognises api prefix`() {
        assertEquals("api" to "org.example:lib:1.0", parseLibDepLine("api org.example:lib:1.0"))
    }

    @Test
    fun `parseLibDepLine recognises implementation prefix`() {
        assertEquals(
            "implementation" to "org.example:lib:1.0",
            parseLibDepLine("implementation org.example:lib:1.0")
        )
    }

    @Test
    fun `parseLibDepLine recognises compileOnly prefix`() {
        assertEquals("compileOnly" to "org.example:lib:1.0", parseLibDepLine("compileOnly org.example:lib:1.0"))
    }

    @Test
    fun `parseLibDepLine recognises runtimeOnly prefix`() {
        assertEquals("runtimeOnly" to "org.example:lib:1.0", parseLibDepLine("runtimeOnly org.example:lib:1.0"))
    }

    @Test
    fun `parseLibDepLine recognises testImplementation prefix`() {
        assertEquals(
            "testImplementation" to "org.example:lib:1.0",
            parseLibDepLine("testImplementation org.example:lib:1.0")
        )
    }

    @Test
    fun `parseLibDepLine recognises testCompileOnly prefix`() {
        assertEquals(
            "testCompileOnly" to "org.example:lib:1.0",
            parseLibDepLine("testCompileOnly org.example:lib:1.0")
        )
    }

    @Test
    fun `parseLibDepLine defaults to implementation when no prefix`() {
        assertEquals("implementation" to "org.example:lib:1.0", parseLibDepLine("org.example:lib:1.0"))
    }

    @Test
    fun `parseLibDepLine treats unknown leading token as part of coord`() {
        val result = parseLibDepLine("weird org.example:lib:1.0")
        assertEquals("implementation", result.first)
        assertEquals("weird org.example:lib:1.0", result.second)
    }

    // --- parseLibDeps ---

    @Test
    fun `parseLibDeps returns empty list for empty input`() {
        assertEquals(emptyList(), parseLibDeps(emptyList()))
    }

    @Test
    fun `parseLibDeps parses a bare coord with no imports`() {
        val entries = parseLibDeps(listOf("org.example:lib:1.0"))
        assertEquals(1, entries.size)
        assertEquals("org.example:lib:1.0", entries[0].coord)
        assertEquals("implementation", entries[0].config)
        assertEquals(emptyList(), entries[0].imports)
    }

    @Test
    fun `parseLibDeps parses indented imports under a coord`() {
        val entries = parseLibDeps(
            listOf(
                "org.example:lib:1.0",
                "  org.example.Foo",
                "  org.example.Bar"
            )
        )
        assertEquals(1, entries.size)
        assertEquals(listOf("org.example.Foo", "org.example.Bar"), entries[0].imports)
    }

    @Test
    fun `parseLibDeps parses multiple coords with mixed configs`() {
        val entries = parseLibDeps(
            listOf(
                "api org.example:lib-a:1.0",
                "  org.example.A",
                "org.example:lib-b:2.0",
                "testImplementation org.example:lib-c:3.0"
            )
        )
        assertEquals(3, entries.size)
        assertEquals("api" to "org.example:lib-a:1.0", entries[0].config to entries[0].coord)
        assertEquals(listOf("org.example.A"), entries[0].imports)
        assertEquals("implementation" to "org.example:lib-b:2.0", entries[1].config to entries[1].coord)
        assertEquals("testImplementation" to "org.example:lib-c:3.0", entries[2].config to entries[2].coord)
    }

    @Test
    fun `parseLibDeps ignores blank lines between coords`() {
        val entries = parseLibDeps(
            listOf(
                "org.example:lib-a:1.0",
                "",
                "org.example:lib-b:2.0"
            )
        )
        assertEquals(2, entries.size)
    }

    // --- parseLibMd ---

    @Test
    fun `parseLibMd returns lib and body sections when no deps`() {
        val source = """
            ```lib
            com.example:lib:1.0

            class Foo
            ```
        """.trimIndent()
        val sections = parseLibMd(source)
        val lib = sections.firstOrNull { it.tag == "lib" }
        val body = sections.firstOrNull { it.tag == "body" }
        val deps = sections.firstOrNull { it.tag == "deps" }
        assertNotNull(lib)
        assertNotNull(body)
        assertNull(deps)
        assertEquals(listOf("com.example:lib:1.0"), lib!!.lines)
        assertEquals(listOf("class Foo"), body!!.lines)
    }

    @Test
    fun `parseLibMd returns lib body and deps sections`() {
        val source = """
            ```lib
            com.example:lib:1.0

            class Foo
            ```

            # dependencies
            api org.example:dep:1.0
              org.example.Dep
        """.trimIndent()
        val sections = parseLibMd(source)
        assertEquals(3, sections.size)
        assertNotNull(sections.firstOrNull { it.tag == "lib" })
        assertNotNull(sections.firstOrNull { it.tag == "body" })
        val deps = sections.firstOrNull { it.tag == "deps" }
        assertNotNull(deps)
        assertTrue(deps!!.lines.any { "api org.example:dep:1.0" in it })
        assertTrue(deps.lines.any { "  org.example.Dep" in it })
    }

    @Test
    fun `parseLibMd returns empty body when fence has header only`() {
        val source = """
            ```lib
            com.example:lib:1.0
            ```
        """.trimIndent()
        val sections = parseLibMd(source)
        val body = sections.firstOrNull { it.tag == "body" }
        assertNull(body, "No body section expected when fence has no blank-line separator")
    }

    @Test
    fun `parseLibMd stops deps section at next markdown header`() {
        val source = """
            ```lib
            com.example:lib:1.0

            class Foo
            ```

            # dependencies
            org.example:dep:1.0

            # other section
            not a dep
        """.trimIndent()
        val sections = parseLibMd(source)
        val deps = sections.firstOrNull { it.tag == "deps" }
        assertNotNull(deps)
        assertFalse(deps!!.lines.any { "not a dep" in it }, "deps section leaked past next header")
    }
}
