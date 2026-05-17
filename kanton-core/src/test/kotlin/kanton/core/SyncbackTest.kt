package kanton.core

import kanton.core.cli.syncback.buildInjectionContextFromCliKts
import kanton.core.cli.syncback.buildInjectionContextFromMd
import kanton.core.cli.syncback.explodeCliKts
import kanton.core.cli.syncback.extractDepImportsFromMainKt
import kanton.core.cli.syncback.extractMavenCoordsFromGradleKts
import kanton.core.cli.syncback.knownCliReturnType
import kanton.core.cli.syncback.replaceInCliBlock
import kanton.core.cli.syncback.replaceRunSectionInCliMd
import kanton.core.cli.syncback.updateDepsInCliMd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyncbackTest {

    // --- replaceInCliBlock ---

    @Test
    fun `replaceInCliBlock replaces run body in cli block`() {
        val source = "```cli\ngreet:Hello\n--name, Name\n\nold body\n```"
        val result = replaceInCliBlock(source, "new body")
        assertTrue("new body" in result)
        assertTrue("old body" !in result)
    }

    @Test
    fun `replaceInCliBlock preserves cli header lines`() {
        val source = "```cli\ngreet:Hello\n--name, Name\n\nold body\n```"
        val result = replaceInCliBlock(source, "new body")
        assertTrue("greet:Hello" in result)
        assertTrue("--name, Name" in result)
    }

    @Test
    fun `replaceInCliBlock returns original when no cli block`() {
        val source = "no fences here"
        assertEquals(source, replaceInCliBlock(source, "replacement"))
    }

    @Test
    fun `replaceInCliBlock returns original when no blank separator`() {
        val source = "```cli\ngreet:Hello\n```"
        assertEquals(source, replaceInCliBlock(source, "replacement"))
    }

    // --- replaceRunSectionInCliMd ---

    @Test
    fun `replaceRunSectionInCliMd uses cli block`() {
        val source = "```cli\ngreet:Hello\n\nold\n```"
        val result = replaceRunSectionInCliMd(source, "new")
        assertTrue("new" in result)
        assertTrue("old" !in result)
    }

    // --- extractMavenCoordsFromGradleKts ---

    @Test
    fun `extractMavenCoordsFromGradleKts extracts single coord`() {
        val content = """implementation("com.example:lib:1.0")"""
        assertEquals(listOf("com.example:lib:1.0"), extractMavenCoordsFromGradleKts(content))
    }

    @Test
    fun `extractMavenCoordsFromGradleKts extracts multiple coords`() {
        val content = """
            implementation("com.a:lib:1.0")
            testImplementation("com.b:test:2.0")
            implementation("com.c:util:3.0")
        """.trimIndent()
        val result = extractMavenCoordsFromGradleKts(content)
        assertEquals(listOf("com.a:lib:1.0", "com.c:util:3.0"), result)
    }

    @Test
    fun `extractMavenCoordsFromGradleKts returns empty for no matches`() {
        assertEquals(emptyList(), extractMavenCoordsFromGradleKts("plugins { kotlin(\"jvm\") }"))
    }

    @Test
    fun `extractMavenCoordsFromGradleKts ignores testImplementation`() {
        val content = """testImplementation("com.example:test:1.0")"""
        assertEquals(emptyList(), extractMavenCoordsFromGradleKts(content))
    }

    // --- extractDepImportsFromMainKt ---

    @Test
    fun `extractDepImportsFromMainKt extracts user imports`() {
        val mainKt = """
            import kanton.stdinText
            import com.example.Foo
            class MyScript : Script() {
                override fun run() {}
            }
        """.trimIndent()
        val imports = extractDepImportsFromMainKt(mainKt)
        assertTrue("kanton.stdinText" in imports)
        assertTrue("com.example.Foo" in imports)
    }

    @Test
    fun `extractDepImportsFromMainKt excludes standard clikt imports`() {
        val mainKt = """
            import com.github.ajalt.clikt.core.CliktCommand
            import com.github.ajalt.clikt.parameters.options.*
            import com.github.ajalt.clikt.parameters.arguments.*
            import com.example.Foo
        """.trimIndent()
        val imports = extractDepImportsFromMainKt(mainKt)
        assertTrue("com.github.ajalt.clikt.core.CliktCommand" !in imports)
        assertTrue("com.github.ajalt.clikt.parameters.options.*" !in imports)
        assertTrue("com.github.ajalt.clikt.parameters.arguments.*" !in imports)
        assertTrue("com.example.Foo" in imports)
    }

    @Test
    fun `extractDepImportsFromMainKt returns empty when no imports`() {
        val mainKt = "class Foo : Script() { override fun run() {} }"
        assertEquals(emptyList(), extractDepImportsFromMainKt(mainKt))
    }

    // --- knownCliReturnType ---

    @Test
    fun `knownCliReturnType returns String type for stdinText`() {
        assertEquals(": String", knownCliReturnType("kanton.stdinText"))
    }

    @Test
    fun `knownCliReturnType returns empty string for unknown fqn`() {
        assertEquals("", knownCliReturnType("kanton.someUnknown"))
        assertEquals("", knownCliReturnType("com.example.Foo"))
    }

    // --- explodeCliKts ---

    @Test
    fun `explodeCliKts strips shebang line`() {
        val source = "#!/bin/bash\nclass Foo : Script() {}"
        assertFalse("#!/bin/bash" in explodeCliKts(source))
    }

    @Test
    fun `explodeCliKts strips file annotations`() {
        val source = """
            @file:DependsOn("com.example:lib:1.0")
            @file:Repository("https://repo.example.com")
            @file:Import("some.import")
            @file:CompilerOptions("-jvm-target", "17")
            class Foo : Script() {}
        """.trimIndent()
        val result = explodeCliKts(source)
        assertFalse("@file:DependsOn" in result)
        assertFalse("@file:Repository" in result)
        assertFalse("@file:Import" in result)
        assertFalse("@file:CompilerOptions" in result)
        assertTrue("class Foo" in result)
    }

    @Test
    fun `explodeCliKts appends main when class found and no main`() {
        val source = "class MyScript : Script() {\n    override fun run() {}\n}"
        val result = explodeCliKts(source)
        assertTrue("fun main(args: Array<String>)" in result)
        assertTrue("MyScript()" in result)
    }

    @Test
    fun `explodeCliKts does not double-append main when already present`() {
        val source = "class Foo : Script() {}\nfun main() = kanton.runScript(Foo())\n"
        val result = explodeCliKts(source)
        assertEquals(1, result.lines().count { "fun main()" in it })
    }

    @Test
    fun `explodeCliKts drops leading blank lines`() {
        val source = "\n\n\nclass Foo : Script() {}"
        val result = explodeCliKts(source)
        assertFalse(result.startsWith("\n"))
    }

    // --- buildInjectionContextFromCliKts ---

    @Test
    fun `buildInjectionContextFromCliKts returns null when no class found`() {
        val source = "val x = 1\n"
        assertNull(buildInjectionContextFromCliKts(source))
    }

    @Test
    fun `buildInjectionContextFromCliKts returns null when no override fun run`() {
        val source = "class Foo : Script() { fun other() {} }"
        assertNull(buildInjectionContextFromCliKts(source))
    }

    @Test
    fun `buildInjectionContextFromCliKts returns context with class name`() {
        val source = """
            class Greeter : Script(name = "greet") {
                private val name by option("--name")
                override fun run() {
                    echo(name ?: "World")
                }
            }
        """.trimIndent()
        val ctx = buildInjectionContextFromCliKts(source)
        assertNotNull(ctx)
        assertEquals("Greeter", ctx!!.className)
    }

    @Test
    fun `buildInjectionContextFromCliKts replaces Script with CliktCommand in prefix`() {
        val source = """
            class Greeter : Script(name = "greet") {
                override fun run() {
                }
            }
        """.trimIndent()
        val ctx = buildInjectionContextFromCliKts(source)
        assertNotNull(ctx)
        assertTrue("com.github.ajalt.clikt.core.CliktCommand" in ctx!!.prefix)
        assertFalse("Script(" in ctx.prefix)
    }

    @Test
    fun `buildInjectionContextFromCliKts strips import lines from prefix`() {
        val source = """
            import com.example.Foo
            class Greeter : Script(name = "greet") {
                override fun run() {
                }
            }
        """.trimIndent()
        val ctx = buildInjectionContextFromCliKts(source)
        assertNotNull(ctx)
        assertFalse("import " in ctx!!.prefix)
    }

    @Test
    fun `buildInjectionContextFromCliKts suffix contains closing braces`() {
        val source = """
            class Greeter : Script(name = "greet") {
                override fun run() {
                }
            }
        """.trimIndent()
        val ctx = buildInjectionContextFromCliKts(source)
        assertNotNull(ctx)
        assertTrue("}" in ctx!!.suffix)
        assertFalse("fun main()" in ctx.suffix)
        assertFalse("kanton.runScript" in ctx.suffix)
    }

    // --- buildInjectionContextFromMd ---

    @Test
    fun `buildInjectionContextFromMd returns null when no cli section`() {
        val source = "just some text, no fences"
        assertNull(buildInjectionContextFromMd(source))
    }

    @Test
    fun `buildInjectionContextFromMd returns context from cli-md source`() {
        val source = """
            ```cli
            greet:Say hello to someone
            --name, -n, Name to greet = "World"

            echo("Hello, ${'$'}name!")
            ```
        """.trimIndent()
        val ctx = buildInjectionContextFromMd(source)
        assertNotNull(ctx)
        assertEquals("Greet", ctx!!.className)
    }

    @Test
    fun `buildInjectionContextFromMd prefix contains CliktCommand base class`() {
        val source = """
            ```cli
            greet:Hello

            echo("hi")
            ```
        """.trimIndent()
        val ctx = buildInjectionContextFromMd(source)
        assertNotNull(ctx)
        assertTrue("com.github.ajalt.clikt.core.CliktCommand" in ctx!!.prefix)
    }

    @Test
    fun `buildInjectionContextFromMd prefix contains option declarations`() {
        val source = """
            ```cli
            greet:Hello
            --name, -n, Name = "World"
            --repeat, -r, Times : int() = 1

            echo("hi")
            ```
        """.trimIndent()
        val ctx = buildInjectionContextFromMd(source)
        assertNotNull(ctx)
        assertTrue("val name" in ctx!!.prefix)
        assertTrue("val repeat" in ctx.prefix)
    }

    @Test
    fun `buildInjectionContextFromMd suffix closes class without main`() {
        val source = """
            ```cli
            greet:Hello

            echo("hi")
            ```
        """.trimIndent()
        val ctx = buildInjectionContextFromMd(source)
        assertNotNull(ctx)
        assertTrue("}" in ctx!!.suffix)
        assertFalse("fun main()" in ctx.suffix)
        assertFalse("kanton.runScript" in ctx.suffix)
    }

    @Test
    fun `buildInjectionContextFromMd escapes double quotes in name and help`() {
        val source = """
            ```cli
            greet:Say "hello" there

            echo("hi")
            ```
        """.trimIndent()
        val ctx = buildInjectionContextFromMd(source)
        assertNotNull(ctx)
        assertTrue("\\\"hello\\\"" in ctx!!.prefix)
    }

    // --- updateDepsInCliMd ---

    @Test
    fun `updateDepsInCliMd replaces existing deps section`() {
        val source = """
            ```cli
            greet:Hello

            echo("hi")
            ```

            # dependencies
            com.old:lib:1.0
              com.old.Foo
        """.trimIndent()
        val result = updateDepsInCliMd(source, listOf("com.new:lib:2.0"), listOf("com.new.Bar"))
        assertTrue("com.new:lib:2.0" in result)
        assertTrue("com.new.Bar" in result)
        assertTrue("com.old:lib:1.0" !in result)
    }

    @Test
    fun `updateDepsInCliMd appends deps section when absent`() {
        val source = """
            ```cli
            greet:Hello

            echo("hi")
            ```
        """.trimIndent()
        val result = updateDepsInCliMd(source, listOf("com.example:lib:1.0"), listOf("com.example.Foo"))
        assertTrue("# dependencies" in result.lowercase())
        assertTrue("com.example:lib:1.0" in result)
    }

    @Test
    fun `updateDepsInCliMd places orphan import under matching groupId coord not last coord`() {
        val source = """
            ```cli
            run-cmd:Run a command

            process("ls")
            ```

            # dependencies
            com.github.ajalt.clikt:clikt:5.1.0
              kanton.Script
            com.github.pgreze:kotlin-process:1.5.1
            org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0
              kotlinx.coroutines.runBlocking
        """.trimIndent()
        val result = updateDepsInCliMd(
            source,
            listOf(
                "com.github.ajalt.clikt:clikt:5.1.0",
                "com.github.pgreze:kotlin-process:1.5.1",
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0"
            ),
            listOf("kanton.Script", "com.github.pgreze.process.process", "kotlinx.coroutines.runBlocking")
        )
        val lines = result.lines()
        val processLine = lines.indexOfFirst { "com.github.pgreze.process.process" in it }
        val kotlinProcessCoordLine = lines.indexOfFirst { "com.github.pgreze:kotlin-process" in it }
        val coroutinesCoordLine = lines.indexOfFirst { "kotlinx-coroutines-core" in it }
        assertTrue(processLine > kotlinProcessCoordLine, "process.process should appear after kotlin-process coord")
        assertTrue(processLine < coroutinesCoordLine, "process.process should appear before coroutines coord")
    }

    @Test
    fun `updateDepsInCliMd relocates import listed under wrong coord to best-matching coord`() {
        val source = """
            ```cli
            run-cmd:Run a command

            process("ls")
            ```

            # dependencies
            com.github.ajalt.clikt:clikt:5.1.0
              kanton.Script
            com.github.pgreze:kotlin-process:1.5.1
            org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0
              com.github.pgreze.process.process
              kotlinx.coroutines.runBlocking
        """.trimIndent()
        val result = updateDepsInCliMd(
            source,
            listOf(
                "com.github.ajalt.clikt:clikt:5.1.0",
                "com.github.pgreze:kotlin-process:1.5.1",
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0"
            ),
            listOf("kanton.Script", "com.github.pgreze.process.process", "kotlinx.coroutines.runBlocking")
        )
        val lines = result.lines()
        val processLine = lines.indexOfFirst { "com.github.pgreze.process.process" in it }
        val kotlinProcessCoordLine = lines.indexOfFirst { "com.github.pgreze:kotlin-process" in it }
        val coroutinesCoordLine = lines.indexOfFirst { "kotlinx-coroutines-core" in it }
        assertTrue(processLine > kotlinProcessCoordLine, "process.process should appear after kotlin-process coord")
        assertTrue(processLine < coroutinesCoordLine, "process.process should appear before coroutines coord")
    }

    @Test
    fun `updateDepsInCliMd preserves kanton Script import`() {
        val source = """
            ```cli
            greet:Hello

            echo("hi")
            ```

            # dependencies
            com.example:lib:1.0
              kanton.Script
              com.example.Foo
        """.trimIndent()
        val result = updateDepsInCliMd(source, listOf("com.example:lib:1.0"), emptyList())
        assertTrue("kanton.Script" in result)
    }
}
