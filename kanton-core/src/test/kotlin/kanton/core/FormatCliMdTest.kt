package kanton.core

import kanton.core.cli.format.CliSpec
import kanton.core.cli.format.ensureBaseImports
import kanton.core.cli.format.formatArgumentLine
import kanton.core.cli.format.formatCliMd
import kanton.core.cli.format.formatOptionLine
import kanton.core.cli.models.ArgumentConfig
import kanton.core.cli.models.DepEntry
import kanton.core.cli.models.OptionConfig
import kanton.core.cli.parsing.parseCliMd
import kanton.core.cli.parsing.parseOptionLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertTrue

class FormatCliMdTest {

    @Test
    fun `formatOptionLine with help and default`() {
        val opt = OptionConfig(listOf("--name", "-n"), "Name to greet", null, "\"World\"")
        assertEquals("--name, -n, Name to greet = \"World\"", formatOptionLine(opt))
    }

    @Test
    fun `formatOptionLine with typeFunc and default`() {
        val opt = OptionConfig(listOf("--repeat", "-r"), "Times to repeat", "int()", "1")
        assertEquals("--repeat, -r, Times to repeat : int() = 1", formatOptionLine(opt))
    }

    @Test
    fun `formatOptionLine with no default`() {
        val opt = OptionConfig(listOf("--verbose", "-v"), "Enable verbose output", null, null)
        assertEquals("--verbose, -v, Enable verbose output", formatOptionLine(opt))
    }

    @Test
    fun `formatArgumentLine basic`() {
        val arg = ArgumentConfig("FILE", "Input file", null, null)
        assertEquals("FILE, Input file", formatArgumentLine(arg))
    }

    @Test
    fun `formatArgumentLine with type and default`() {
        val arg = ArgumentConfig("COUNT", "Number of items", "int()", "1")
        assertEquals("COUNT, Number of items : int() = 1", formatArgumentLine(arg))
    }

    @Test
    fun `ensureBaseImports adds clikt when missing`() {
        val result = ensureBaseImports(emptyList(), hasOptions = false)
        assertEquals(1, result.size)
        assertEquals("com.github.ajalt.clikt:clikt:5.1.0", result[0].coord)
        assertContains(result[0].imports, "kanton.Script")
    }

    @Test
    fun `ensureBaseImports adds option imports when hasOptions`() {
        val result = ensureBaseImports(emptyList(), hasOptions = true)
        assertContains(result[0].imports, "com.github.ajalt.clikt.parameters.options.option")
        assertContains(result[0].imports, "com.github.ajalt.clikt.parameters.options.default")
    }

    @Test
    fun `ensureBaseImports preserves existing clikt imports`() {
        val deps = listOf(DepEntry("com.github.ajalt.clikt:clikt:5.1.0", listOf("kanton.Script", "kanton.stdinText")))
        val result = ensureBaseImports(deps, hasOptions = true)
        assertEquals(1, result.size)
        assertContains(result[0].imports, "kanton.Script")
        assertContains(result[0].imports, "kanton.stdinText")
        assertContains(result[0].imports, "com.github.ajalt.clikt.parameters.options.option")
    }

    @Test
    fun `formatCliMd produces valid structure`() {
        val spec = CliSpec(
            name = "greet",
            help = "Say hello to someone",
            title = "greet - simple greeter",
            description = "A friendly greeter script.",
            options = listOf(
                OptionConfig(listOf("--name", "-n"), "Name to greet", null, "\"World\"")
            )
        )
        val result = formatCliMd(spec, "echo(\"Hello, \$name!\")")

        assertContains(result, "#!/usr/bin/env bash")
        assertContains(result, "exec ~/.kanton/bin/kanton-executor \"greet.kt.md\"")
        assertContains(result, "# greet - simple greeter")
        assertContains(result, "A friendly greeter script.")
        assertContains(result, "```cli")
        assertContains(result, "greet:Say hello to someone")
        assertContains(result, "--name, -n, Name to greet = \"World\"")
        assertContains(result, "echo(\"Hello, \$name!\")")
        assertContains(result, "# dependencies")
        assertContains(result, "kanton.Script")
    }

    @Test
    fun `formatCliMd output re-parses correctly`() {
        val spec = CliSpec(
            name = "test-roundtrip",
            help = "Round-trip test",
            title = "test-roundtrip - verifies formatting",
            options = listOf(
                OptionConfig(listOf("--count", "-c"), "Repeat count", "int()", "3"),
                OptionConfig(listOf("--verbose", "-v"), "Verbose mode", null, "\"false\"")
            ),
            dependencies = listOf(
                DepEntry("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1", listOf("kotlinx.coroutines.runBlocking"))
            )
        )
        val runCode = "repeat(count ?: 3) { echo(\"hello\") }"
        val formatted = formatCliMd(spec, runCode)

        val sections = parseCliMd(formatted)
        val cli = sections.first { it.tag == "cli" }
        val run = sections.first { it.tag == "run" }
        val deps = sections.first { it.tag == "deps" }

        assertEquals("test-roundtrip:Round-trip test", cli.lines[0])
        assertEquals(3, cli.lines.size)

        val reparsedOpt = parseOptionLine(cli.lines[1])
        assertEquals(listOf("--count", "-c"), reparsedOpt.names)
        assertEquals("Repeat count", reparsedOpt.help)
        assertEquals("int()", reparsedOpt.typeFunc)
        assertEquals("3", reparsedOpt.default)

        assertEquals(listOf("repeat(count ?: 3) { echo(\"hello\") }"), run.lines)

        assertTrue(deps.lines.any { it.trim() == "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1" })
        assertTrue(deps.lines.any { it.trim() == "kotlinx.coroutines.runBlocking" })
    }

    @Test
    fun `formatCliMd without executable preamble`() {
        val spec = CliSpec(
            name = "lib-helper",
            help = "A helper",
            title = "lib-helper - no preamble",
            executable = false
        )
        val result = formatCliMd(spec, "echo(\"done\")")

        assertTrue(!result.startsWith("#!/usr/bin/env bash"))
        assertContains(result, "# lib-helper - no preamble")
        assertContains(result, "```cli")
    }

    @Test
    fun `formatCliMd blank line separates options from run block`() {
        val spec = CliSpec(
            name = "sep-test",
            help = "Test separator",
            title = "sep-test",
            options = listOf(
                OptionConfig(listOf("--flag"), "A flag", null, null)
            )
        )
        val result = formatCliMd(spec, "echo(\"hi\")")
        val lines = result.lines()
        val optIdx = lines.indexOfFirst { it.startsWith("--flag") }
        assertTrue(optIdx > 0)
        assertEquals("", lines[optIdx + 1], "Expected blank line after last option")
    }
}
