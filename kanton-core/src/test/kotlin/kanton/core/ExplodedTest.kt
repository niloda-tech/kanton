package kanton.core

import kanton.core.cli.scaffold.buildExplodedKotlin
import kanton.core.cli.syncback.extractRunBodyFromMainKt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExplodedTest {

    private val minimalSource = """
        ```cli
        greet:Say hello

        echo("Hello!")
        ```
    """.trimIndent()

    private val sourceWithOptions = $$"""
        ```cli
        greet:Say hello
        --name, -n, Name = "World"
        --repeat, -r, Times : int() = 1

        echo("Hello, $name!")
        ```

        # dependencies
        com.example:lib:1.0
          com.example.Foo
    """.trimIndent()

    @Test
    fun `buildExplodedKotlin returns null when no cli section`() {
        assertNull(buildExplodedKotlin("just text, no fences"))
    }

    @Test
    fun `buildExplodedKotlin returns null when no run section`() {
        val source = "```cli\ngreet:Hello\n```"
        assertNull(buildExplodedKotlin(source))
    }

    @Test
    fun `buildExplodedKotlin emits CliktCommand import`() {
        val result = buildExplodedKotlin(minimalSource)
        assertNotNull(result)
        assertTrue("import com.github.ajalt.clikt.core.CliktCommand" in result!!)
    }

    @Test
    fun `buildExplodedKotlin emits options imports when options are present`() {
        val result = buildExplodedKotlin(sourceWithOptions)
        assertNotNull(result)
        assertTrue("import com.github.ajalt.clikt.parameters.options.*" in result!!)
    }

    @Test
    fun `buildExplodedKotlin skips options imports when no options`() {
        val result = buildExplodedKotlin(minimalSource)
        assertNotNull(result)
        assertTrue("parameters.options" !in result!!)
    }

    @Test
    fun `buildExplodedKotlin emits dep imports`() {
        val result = buildExplodedKotlin(sourceWithOptions)
        assertNotNull(result)
        assertTrue("import com.example.Foo" in result!!)
    }

    @Test
    fun `buildExplodedKotlin does not emit kanton Script as import`() {
        val source = """
            ```cli
            greet:Hello

            echo("hi")
            ```

            # dependencies
            com.example:lib:1.0
              kanton.Script
        """.trimIndent()
        val result = buildExplodedKotlin(source)
        assertNotNull(result)
        assertTrue("import kanton.Script" !in result!!)
    }

    @Test
    fun `buildExplodedKotlin generates class declaration`() {
        val result = buildExplodedKotlin(minimalSource)
        assertNotNull(result)
        assertTrue("""class Greet : CliktCommand(name = "greet"""" in result!!)
    }

    @Test
    fun `buildExplodedKotlin capitalises class name from script name`() {
        val result = buildExplodedKotlin(minimalSource)
        assertNotNull(result)
        assertTrue("class Greet" in result!!)
    }

    @Test
    fun `buildExplodedKotlin generates option properties`() {
        val result = buildExplodedKotlin(sourceWithOptions)
        assertNotNull(result)
        assertTrue("""private val name by option("--name", "-n"""" in result!!)
        assertTrue("""private val repeat by option("--repeat", "-r"""" in result)
    }

    @Test
    fun `buildExplodedKotlin injects run body`() {
        val result = buildExplodedKotlin(minimalSource)
        assertNotNull(result)
        assertTrue("echo(\"Hello!\")" in result!!)
    }

    @Test
    fun `buildExplodedKotlin emits override fun run`() {
        val result = buildExplodedKotlin(minimalSource)
        assertNotNull(result)
        assertTrue("override fun run()" in result!!)
    }

    @Test
    fun `buildExplodedKotlin emits main function`() {
        val result = buildExplodedKotlin(minimalSource)
        assertNotNull(result)
        assertTrue("fun main(args: Array<String>)" in result!!)
        assertTrue("Greet()" in result)
    }

    @Test
    fun `buildExplodedKotlin escapes double quotes in name and help`() {
        val source = """
            ```cli
            my-script:Does "stuff" well

            echo("ok")
            ```
        """.trimIndent()
        val result = buildExplodedKotlin(source)
        assertNotNull(result)
        assertTrue("\\\"stuff\\\"" in result!!)
    }

    // --- buildExplodedKotlin with arguments ---

    private val sourceWithArguments = """
        ```cli
        process:Process a file
        FILE, The file to process
        --verbose, -v, Enable verbose output

        echo("Processing ${'$'}file")
        ```
    """.trimIndent()

    private val sourceWithArgumentOnly = """
        ```cli
        cat:Print file contents
        FILE, The file to print

        echo("File: ${'$'}file")
        ```
    """.trimIndent()

    @Test
    fun `buildExplodedKotlin emits argument declaration`() {
        val result = buildExplodedKotlin(sourceWithArguments)
        assertNotNull(result)
        assertTrue("private val file by argument(\"FILE\", help = \"The file to process\")" in result!!)
    }

    @Test
    fun `buildExplodedKotlin emits arguments import when arguments are present`() {
        val result = buildExplodedKotlin(sourceWithArgumentOnly)
        assertNotNull(result)
        assertTrue("import com.github.ajalt.clikt.parameters.arguments.*" in result!!)
    }

    @Test
    fun `buildExplodedKotlin skips options import when only arguments`() {
        val result = buildExplodedKotlin(sourceWithArgumentOnly)
        assertNotNull(result)
        assertTrue("parameters.options" !in result!!)
    }

    @Test
    fun `buildExplodedKotlin emits both options and arguments imports`() {
        val result = buildExplodedKotlin(sourceWithArguments)
        assertNotNull(result)
        assertTrue("import com.github.ajalt.clikt.parameters.options.*" in result!!)
        assertTrue("import com.github.ajalt.clikt.parameters.arguments.*" in result)
    }

    @Test
    fun `buildExplodedKotlin emits argument with type function`() {
        val source = """
            ```cli
            sum:Sum numbers
            COUNT, Number of items : int()

            echo("Count: ${'$'}count")
            ```
        """.trimIndent()
        val result = buildExplodedKotlin(source)
        assertNotNull(result)
        assertTrue("by argument(\"COUNT\", help = \"Number of items\").int()" in result)
    }

    @Test
    fun `buildExplodedKotlin emits argument with default`() {
        val source = $$"""
            ```cli
            greet:Greet someone
            NAME, Name to greet = "World"

            echo("Hello, $name!")
            ```
        """.trimIndent()
        val result = buildExplodedKotlin(source)
        assertNotNull(result)
        assertTrue("""by argument("NAME", help = "Name to greet").default("World")""" in result!!)
    }

    // --- extractRunBodyFromMainKt ---

    @Test
    fun `extractRunBodyFromMainKt extracts body of run method`() {
        val mainKt = """
            class Greeter : CliktCommand() {
                override fun run() {
                    echo("Hello!")
                    println("Done")
                }
            }
        """.trimIndent()
        val body = extractRunBodyFromMainKt(mainKt)
        assertNotNull(body)
        assertTrue("echo(\"Hello!\")" in body!!)
        assertTrue("println(\"Done\")" in body)
    }

    @Test
    fun `extractRunBodyFromMainKt returns null when no run method`() {
        val mainKt = """
            class Greeter : CliktCommand() {
                fun other() { println("nope") }
            }
        """.trimIndent()
        assertNull(extractRunBodyFromMainKt(mainKt))
    }

    @Test
    fun `extractRunBodyFromMainKt returns empty string for empty run body`() {
        val mainKt = """
            class Greeter : CliktCommand() {
                override fun run() {
                }
            }
        """.trimIndent()
        val body = extractRunBodyFromMainKt(mainKt)
        assertNotNull(body)
        assertEquals("", body!!.trim())
    }

    @Test
    fun `extractRunBodyFromMainKt handles nested braces correctly`() {
        val mainKt = """
            class Greeter : CliktCommand() {
                override fun run() {
                    if (true) {
                        echo("inner")
                    }
                }
            }
        """.trimIndent()
        val body = extractRunBodyFromMainKt(mainKt)
        assertNotNull(body)
        assertTrue("if (true)" in body!!)
        assertTrue("echo(\"inner\")" in body)
    }

    @Test
    fun `extractRunBodyFromMainKt does not include closing brace of run method`() {
        val mainKt = """
            class Greeter : CliktCommand() {
                override fun run() {
                    echo("hi")
                }
            }
        """.trimIndent()
        val body = extractRunBodyFromMainKt(mainKt)
        assertNotNull(body)
        val lines = body!!.lines().map { it.trim() }.filter { it.isNotEmpty() }
        assertEquals(1, lines.size)
        assertEquals("echo(\"hi\")", lines[0])
    }
}
