package kanton.core

import kanton.core.cli.format.CliSpec
import kanton.core.cli.format.formatCliMd
import kanton.core.cli.models.ArgumentConfig
import kanton.core.cli.models.DepEntry
import kanton.core.cli.models.OptionConfig
import kanton.core.cli.parsing.isArgumentLine
import kanton.core.cli.parsing.parseArgumentLine
import kanton.core.cli.parsing.parseCliMd
import kanton.core.cli.parsing.parseOptionLine
import kanton.core.cli.parsing.parseDeps
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertTrue

class JsonSpecWorkflowTest {

    private fun specFromJson(
        scriptName: String,
        shortDescription: String,
        helpText: String,
        proseDescription: String? = null,
        options: List<String> = emptyList(),
        arguments: List<String> = emptyList(),
        dependencies: List<DepEntry> = emptyList(),
    ): Pair<CliSpec, List<String>> {
        val parsedOptions = options.map { parseOptionLine(it) }
        val parsedArguments = arguments.map { parseArgumentLine(it) }
        return CliSpec(
            name = scriptName,
            help = helpText,
            title = "$scriptName - $shortDescription",
            description = proseDescription,
            options = parsedOptions,
            arguments = parsedArguments,
            dependencies = dependencies,
        ) to (arguments + options)
    }

    private fun formatAndParse(spec: CliSpec, runBody: String): FormatResult {
        val formatted = formatCliMd(spec, runBody)
        val sections = parseCliMd(formatted)
        return FormatResult(
            raw = formatted,
            cli = sections.firstOrNull { it.tag == "cli" },
            run = sections.firstOrNull { it.tag == "run" },
            deps = sections.firstOrNull { it.tag == "deps" },
        )
    }

    private data class FormatResult(
        val raw: String,
        val cli: kanton.core.shared.models.Section?,
        val run: kanton.core.shared.models.Section?,
        val deps: kanton.core.shared.models.Section?,
    )

    @Test
    fun `word-count example round-trips correctly`() {
        val (spec, _) = specFromJson(
            scriptName = "word-count",
            shortDescription = "count words and lines in stdin",
            helpText = "Count words, lines, and characters from stdin",
            proseDescription = "Reads piped text from stdin and reports word count, line count, and character count.",
            options = listOf(
                "--verbose, -v, Show per-line breakdown = \"false\""
            ),
            dependencies = listOf(
                DepEntry("com.github.ajalt.clikt:clikt:5.1.0", listOf("cli.stdinText"))
            ),
        )

        val runBody = """
val input = stdinText()
val lines = input.lines().filter { it.isNotBlank() }
val words = input.split(Regex("\\s+")).filter { it.isNotBlank() }
val chars = input.length

if (verbose == "true") {
    lines.forEachIndexed { i, line ->
        echo("Line ${'$'}{i + 1} (${'$'}{line.split(Regex("\\s+")).filter { it.isNotBlank() }.size} words): ${'$'}line")
    }
    echo("")
}
echo("Lines: ${'$'}{lines.size}")
echo("Words: ${'$'}{words.size}")
echo("Chars: ${'$'}chars")
        """.trimIndent()

        val result = formatAndParse(spec, runBody)

        assertContains(result.raw, "#!/usr/bin/env bash")
        assertContains(result.raw, "exec ~/.kanton/bin/kanton-executor \"word-count.kt.md\"")
        assertContains(result.raw, "# word-count - count words and lines in stdin")
        assertContains(result.raw, "Reads piped text from stdin")

        assertEquals("word-count:Count words, lines, and characters from stdin", result.cli!!.lines[0])

        val reparsedOpt = parseOptionLine(result.cli.lines[1])
        assertEquals(listOf("--verbose", "-v"), reparsedOpt.names)
        assertEquals("Show per-line breakdown", reparsedOpt.help)
        assertEquals("\"false\"", reparsedOpt.default)

        assertTrue(result.run!!.lines.any { "stdinText()" in it })

        val depEntries = parseDeps(result.deps!!.lines)
        val cliktDep = depEntries.first { it.coord == "com.github.ajalt.clikt:clikt:5.1.0" }
        assertContains(cliktDep.imports, "cli.CliScript")
        assertContains(cliktDep.imports, "cli.stdinText")
        assertContains(cliktDep.imports, "com.github.ajalt.clikt.parameters.options.option")
        assertContains(cliktDep.imports, "com.github.ajalt.clikt.parameters.options.default")
    }

    @Test
    fun `minimal spec with no options, arguments, or dependencies`() {
        val (spec, _) = specFromJson(
            scriptName = "hello",
            shortDescription = "say hello",
            helpText = "Print a greeting",
        )

        val result = formatAndParse(spec, "echo(\"Hello!\")")

        assertEquals("hello:Print a greeting", result.cli!!.lines[0])
        assertEquals(1, result.cli.lines.size)
        assertTrue(result.run!!.lines.any { "echo(\"Hello!\")" in it })

        val depEntries = parseDeps(result.deps!!.lines)
        val cliktDep = depEntries.first { it.coord == "com.github.ajalt.clikt:clikt:5.1.0" }
        assertContains(cliktDep.imports, "cli.CliScript")
        assertEquals(1, depEntries.size)
    }

    @Test
    fun `spec with arguments only`() {
        val (spec, _) = specFromJson(
            scriptName = "greet",
            shortDescription = "greet by name",
            helpText = "Greet someone by name",
            arguments = listOf(
                "NAME, Name to greet = \"World\"",
                "COUNT, Number of times : int() = 1",
            ),
        )

        val result = formatAndParse(spec, "repeat(count) { echo(\"Hello, \$name!\") }")

        assertEquals("greet:Greet someone by name", result.cli!!.lines[0])

        val argLines = result.cli.lines.drop(1).filter { isArgumentLine(it) }
        assertEquals(2, argLines.size)

        val arg1 = parseArgumentLine(argLines[0])
        assertEquals("NAME", arg1.name)
        assertEquals("Name to greet", arg1.help)

        val arg2 = parseArgumentLine(argLines[1])
        assertEquals("COUNT", arg2.name)
        assertEquals("int()", arg2.typeFunc)
        assertEquals("1", arg2.default)
    }

    @Test
    fun `spec with multiple options and external dependencies`() {
        val (spec, _) = specFromJson(
            scriptName = "fetch-data",
            shortDescription = "fetch and process data",
            helpText = "Fetch data from a URL and process it",
            options = listOf(
                "--url, -u, URL to fetch",
                "--timeout, -t, Timeout in seconds : int() = 30",
                "--verbose, -v, Enable verbose output = \"false\"",
            ),
            dependencies = listOf(
                DepEntry("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1", listOf("kotlinx.coroutines.runBlocking")),
            ),
        )

        val runBody = """
runBlocking {
    echo("Fetching ${'$'}url with timeout ${'$'}timeout")
}
        """.trimIndent()

        val result = formatAndParse(spec, runBody)

        assertEquals("fetch-data:Fetch data from a URL and process it", result.cli!!.lines[0])
        assertEquals(4, result.cli.lines.size)

        val opt1 = parseOptionLine(result.cli.lines[1])
        assertEquals(listOf("--url", "-u"), opt1.names)
        assertEquals(null, opt1.default)

        val opt2 = parseOptionLine(result.cli.lines[2])
        assertEquals("int()", opt2.typeFunc)
        assertEquals("30", opt2.default)

        val depEntries = parseDeps(result.deps!!.lines)
        assertTrue(depEntries.any { it.coord == "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1" })
        val coroutinesDep = depEntries.first { "coroutines" in it.coord }
        assertContains(coroutinesDep.imports, "kotlinx.coroutines.runBlocking")
    }

    @Test
    fun `clikt dep with extra imports is not duplicated`() {
        val (spec, _) = specFromJson(
            scriptName = "stdin-reader",
            shortDescription = "read stdin",
            helpText = "Read and echo stdin",
            options = listOf("--prefix, -p, Line prefix = \"\""),
            dependencies = listOf(
                DepEntry("com.github.ajalt.clikt:clikt:5.1.0", listOf("cli.stdinText")),
            ),
        )

        val result = formatAndParse(spec, "echo(stdinText())")

        val depEntries = parseDeps(result.deps!!.lines)
        val cliktDeps = depEntries.filter { it.coord == "com.github.ajalt.clikt:clikt:5.1.0" }
        assertEquals(1, cliktDeps.size, "clikt dependency should appear exactly once")
        assertContains(cliktDeps[0].imports, "cli.CliScript")
        assertContains(cliktDeps[0].imports, "cli.stdinText")
        assertContains(cliktDeps[0].imports, "com.github.ajalt.clikt.parameters.options.option")
    }

    @Test
    fun `prose description appears between title and actions`() {
        val (spec, _) = specFromJson(
            scriptName = "documented",
            shortDescription = "well-documented script",
            helpText = "A script with prose",
            proseDescription = "This paragraph explains what the script does in detail.",
        )

        val result = formatAndParse(spec, "echo(\"done\")")

        val lines = result.raw.lines()
        val titleIdx = lines.indexOfFirst { it == "# documented - well-documented script" }
        val proseIdx = lines.indexOfFirst { it == "This paragraph explains what the script does in detail." }
        val actionsIdx = lines.indexOfFirst { it == "# actions" }

        assertTrue(titleIdx >= 0, "title must be present")
        assertTrue(proseIdx >= 0, "prose must be present")
        assertTrue(actionsIdx >= 0, "actions must be present")
        assertTrue(proseIdx > titleIdx, "prose must come after title")
        assertTrue(actionsIdx > proseIdx, "actions must come after prose")
    }

    @Test
    fun `blank line separator present between options and run block`() {
        val (spec, _) = specFromJson(
            scriptName = "sep-check",
            shortDescription = "separator check",
            helpText = "Verify blank separator",
            options = listOf(
                "--alpha, -a, First option",
                "--beta, -b, Second option = \"x\"",
            ),
        )

        val result = formatAndParse(spec, "echo(alpha ?: \"\")")

        val lines = result.raw.lines()
        val lastOptIdx = lines.indexOfLast { it.startsWith("--") }
        assertTrue(lastOptIdx > 0, "must have option lines")
        assertEquals("", lines[lastOptIdx + 1], "blank separator must follow last option")
    }

    @Test
    fun `non-executable spec omits preamble`() {
        val spec = CliSpec(
            name = "lib-util",
            help = "Utility function",
            title = "lib-util - utility",
            executable = false,
        )

        val result = formatAndParse(spec, "echo(\"ok\")")

        assertTrue(!result.raw.contains("#!/usr/bin/env bash"), "no shebang in non-executable")
        assertTrue(!result.raw.contains("kanton-executor"), "no executor line in non-executable")
        assertContains(result.raw, "```cli")
        assertContains(result.raw, "lib-util:Utility function")
    }

    @Test
    fun `mixed arguments and options round-trip`() {
        val (spec, _) = specFromJson(
            scriptName = "process",
            shortDescription = "process files",
            helpText = "Process input files with options",
            arguments = listOf("FILE, Input file path"),
            options = listOf("--dry-run, -d, Simulate without writing = \"false\""),
        )

        val result = formatAndParse(spec, "echo(\"Processing \$file, dryRun=\$dryRun\")")

        val headerLines = result.cli!!.lines.drop(1)
        assertEquals(2, headerLines.size)

        val argLine = headerLines.first { isArgumentLine(it) }
        val optLine = headerLines.first { !isArgumentLine(it) }

        val arg = parseArgumentLine(argLine)
        assertEquals("FILE", arg.name)

        val opt = parseOptionLine(optLine)
        assertEquals(listOf("--dry-run", "-d"), opt.names)
        assertEquals("\"false\"", opt.default)
    }

    @Test
    fun `multiline run block preserves all lines`() {
        val (spec, _) = specFromJson(
            scriptName = "multi-line",
            shortDescription = "multiline body test",
            helpText = "Test multiline run block",
        )

        val runBody = """
val a = 1
val b = 2
val c = a + b
echo("Result: ${'$'}c")
if (c > 2) {
    echo("greater")
}
        """.trimIndent()

        val result = formatAndParse(spec, runBody)

        val runLines = result.run!!.lines
        assertEquals(7, runLines.size)
        assertEquals("val a = 1", runLines[0])
        assertEquals("}", runLines[6])
    }

    @Test
    fun `grok dependency wired correctly`() {
        val (spec, _) = specFromJson(
            scriptName = "ask-grok",
            shortDescription = "query Grok AI",
            helpText = "Send a prompt to Grok",
            options = listOf("--prompt, -p, Prompt text"),
            dependencies = listOf(
                DepEntry("com.niloda:grok-caller:0.1.0", listOf("com.niloda.GrokCaller")),
            ),
        )

        val result = formatAndParse(spec, "echo(GrokCaller().call(prompt ?: \"\"))")

        val depEntries = parseDeps(result.deps!!.lines)
        assertTrue(depEntries.any { it.coord == "com.niloda:grok-caller:0.1.0" })
        val grokDep = depEntries.first { "grok" in it.coord }
        assertContains(grokDep.imports, "com.niloda.GrokCaller")

        val cliktDep = depEntries.first { "clikt" in it.coord }
        assertContains(cliktDep.imports, "cli.CliScript")
    }
}
