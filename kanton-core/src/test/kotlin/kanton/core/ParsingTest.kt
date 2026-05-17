package kanton.core

import kanton.core.cli.parsing.buildArgumentTransform
import kanton.core.cli.parsing.buildTransform
import kanton.core.cli.parsing.isArgumentLine
import kanton.core.cli.parsing.parseArgumentLine
import kanton.core.cli.parsing.parseDeps
import kanton.core.cli.parsing.parseCliMd
import kanton.core.cli.parsing.parseOptionLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParsingTest {

    // --- parseDeps ---

    @Test
    fun `parseDeps returns empty list for empty input`() {
        assertEquals(emptyList(), parseDeps(emptyList()))
    }

    @Test
    fun `parseDeps parses bare coord with no imports`() {
        val entries = parseDeps(listOf("org.example:lib:1.0"))
        assertEquals(1, entries.size)
        assertEquals("org.example:lib:1.0", entries[0].coord)
        assertEquals(emptyList(), entries[0].imports)
    }

    @Test
    fun `parseDeps parses coord with two-space-indented import`() {
        val entries = parseDeps(listOf("org.example:lib:1.0", "  org.example.Foo"))
        assertEquals("org.example:lib:1.0", entries[0].coord)
        assertEquals(listOf("org.example.Foo"), entries[0].imports)
    }

    @Test
    fun `parseDeps parses multiple coords`() {
        val lines = listOf(
            "com.a:lib:1.0",
            "  com.a.Foo",
            "com.b:lib:2.0",
            "  com.b.Bar",
            "  com.b.Baz"
        )
        val entries = parseDeps(lines)
        assertEquals(2, entries.size)
        assertEquals("com.a:lib:1.0", entries[0].coord)
        assertEquals(listOf("com.a.Foo"), entries[0].imports)
        assertEquals("com.b:lib:2.0", entries[1].coord)
        assertEquals(listOf("com.b.Bar", "com.b.Baz"), entries[1].imports)
    }

    @Test
    fun `parseDeps skips blank lines`() {
        val lines = listOf("", "org.example:lib:1.0", "", "  org.example.Foo", "")
        val entries = parseDeps(lines)
        assertEquals(1, entries.size)
        assertEquals(listOf("org.example.Foo"), entries[0].imports)
    }

    @Test
    fun `parseDeps trims trailing whitespace from coords and imports`() {
        val entries = parseDeps(listOf("org.example:lib:1.0  ", "  org.example.Foo  "))
        assertEquals("org.example:lib:1.0", entries[0].coord)
        assertEquals("org.example.Foo", entries[0].imports[0])
    }

    // --- parseOptionLine ---

    @Test
    fun `parseOptionLine parses names, help and inline default`() {
        val opt = parseOptionLine("--name, -n, Name to greet = \"World\"")
        assertEquals(listOf("--name", "-n"), opt.names)
        assertEquals("Name to greet", opt.help)
        assertNull(opt.typeFunc)
        assertEquals("\"World\"", opt.default)
    }

    @Test
    fun `parseOptionLine parses type function and explicit default`() {
        val opt = parseOptionLine("--repeat, -r, Repeat count : int() = 1")
        assertEquals(listOf("--repeat", "-r"), opt.names)
        assertEquals("Repeat count", opt.help)
        assertEquals("int()", opt.typeFunc)
        assertEquals("1", opt.default)
    }

    @Test
    fun `parseOptionLine parses option with no default`() {
        val opt = parseOptionLine("--verbose, -v, Enable verbose output")
        assertEquals(listOf("--verbose", "-v"), opt.names)
        assertEquals("Enable verbose output", opt.help)
        assertNull(opt.typeFunc)
        assertNull(opt.default)
    }

    @Test
    fun `parseOptionLine parses type function with no default`() {
        val opt = parseOptionLine("--count, -c, Count value : int()")
        assertEquals("int()", opt.typeFunc)
        assertNull(opt.default)
    }

    @Test
    fun `parseOptionLine parses single long name only`() {
        val opt = parseOptionLine("--output, Output path = \"out.txt\"")
        assertEquals(listOf("--output"), opt.names)
        assertEquals("Output path", opt.help)
        assertEquals("\"out.txt\"", opt.default)
    }

    // --- isArgumentLine ---

    @Test
    fun `isArgumentLine returns true for uppercase name`() {
        assertTrue(isArgumentLine("FILE, The file to process"))
    }

    @Test
    fun `isArgumentLine returns false for option line`() {
        assertFalse(isArgumentLine("--name, -n, Name"))
    }

    @Test
    fun `isArgumentLine returns false for blank line`() {
        assertFalse(isArgumentLine("   "))
    }

    // --- parseArgumentLine ---

    @Test
    fun `parseArgumentLine parses name and help`() {
        val arg = parseArgumentLine("FILE, The file to process")
        assertEquals("FILE", arg.name)
        assertEquals("The file to process", arg.help)
        assertNull(arg.typeFunc)
        assertNull(arg.default)
    }

    @Test
    fun `parseArgumentLine parses inline default`() {
        val arg = parseArgumentLine("NAME, Name to greet : \"World\"")
        assertEquals("NAME", arg.name)
        assertEquals("Name to greet", arg.help)
        assertEquals("\"World\"", arg.default)
    }

    @Test
    fun `parseArgumentLine parses type function and default`() {
        val arg = parseArgumentLine("COUNT, Number of items : int() = 1")
        assertEquals("COUNT", arg.name)
        assertEquals("Number of items", arg.help)
        assertEquals("int()", arg.typeFunc)
        assertEquals("1", arg.default)
    }

    @Test
    fun `parseArgumentLine parses name only`() {
        val arg = parseArgumentLine("FILE")
        assertEquals("FILE", arg.name)
        assertEquals("", arg.help)
        assertNull(arg.typeFunc)
        assertNull(arg.default)
    }

    // --- buildArgumentTransform ---

    @Test
    fun `buildArgumentTransform returns empty string for no type and no default`() {
        assertEquals("", buildArgumentTransform(null, null))
    }

    @Test
    fun `buildArgumentTransform emits default when default is present`() {
        assertEquals(".default(\"World\")", buildArgumentTransform(null, "\"World\""))
    }

    @Test
    fun `buildArgumentTransform emits type then default`() {
        assertEquals(".int().default(1)", buildArgumentTransform("int()", "1"))
    }

    @Test
    fun `buildArgumentTransform emits only type when no default`() {
        assertEquals(".int()", buildArgumentTransform("int()", null))
    }

    // --- buildTransform ---

    @Test
    fun `buildTransform returns empty string for null type and null default`() {
        assertEquals("", buildTransform(null, null))
    }

    @Test
    fun `buildTransform emits only default when no type`() {
        assertEquals(""".default("World")""", buildTransform(null, "\"World\""))
    }

    @Test
    fun `buildTransform emits type then default`() {
        assertEquals(".int().default(1)", buildTransform("int()", "1"))
    }

    @Test
    fun `buildTransform emits only type when no default`() {
        assertEquals(".int()", buildTransform("int()", null))
    }

    // --- parseCliMd ---

    @Test
    fun `parseCliMd extracts cli and run sections from cli fence`() {
        val source = """
            # greet - a greeter

            ```cli
            greet:Say hello
            --name, -n, Name = "World"

            echo("Hello, ${'$'}name!")
            ```
        """.trimIndent()
        val sections = parseCliMd(source)
        val tags = sections.map { it.tag }
        assertTrue("cli" in tags)
        assertTrue("run" in tags)
    }

    @Test
    fun `parseCliMd cli section contains name-help line and option lines`() {
        val source = """
            ```cli
            greet:Say hello
            --name, -n, Name = "World"

            echo("hi")
            ```
        """.trimIndent()
        val cli = parseCliMd(source).first { it.tag == "cli" }
        assertEquals("greet:Say hello", cli.lines[0])
        assertTrue(cli.lines.any { "--name" in it })
    }

    @Test
    fun `parseCliMd run section contains run body`() {
        val source = """
            ```cli
            greet:Say hello

            echo("Hello!")
            println("Done")
            ```
        """.trimIndent()
        val run = parseCliMd(source).first { it.tag == "run" }
        assertTrue(run.lines.any { "echo" in it })
        assertTrue(run.lines.any { "println" in it })
    }

    @Test
    fun `parseCliMd extracts deps from dependencies section`() {
        val source = """
            ```cli
            greet:Hello

            echo("hi")
            ```

            # dependencies
            org.example:lib:1.0
              org.example.Foo
        """.trimIndent()
        val deps = parseCliMd(source).firstOrNull { it.tag == "deps" }
        assertTrue(deps != null)
        assertTrue(deps!!.lines.any { "org.example:lib:1.0" in it })
    }

    @Test
    fun `parseCliMd handles source with no run body`() {
        val source = """
            ```cli
            greet:Hello
            --name, -n, Name
            ```
        """.trimIndent()
        val sections = parseCliMd(source)
        assertTrue(sections.none { it.tag == "run" })
    }

}
