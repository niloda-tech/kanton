package kanton.core

import kanton.core.lib.models.LibCoords
import kanton.core.lib.models.LibDepEntry
import kanton.core.lib.scaffold.buildExplodedLibraryKotlin
import kanton.core.lib.templates.buildLibraryGradleKts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LibExplodedTest {

    // --- buildExplodedLibraryKotlin ---

    private val minimalSource = """
        ```lib
        com.example:lib:1.0

        class Foo
        ```
    """.trimIndent()

    private val sourceWithImports = """
        ```lib
        com.example:lib:1.0

        class Foo : Bar()
        ```

        # dependencies
        api org.example:dep:1.0
          org.example.Bar
          org.example.Baz
    """.trimIndent()

    @Test
    fun `buildExplodedLibraryKotlin returns null when source is not lib`() {
        assertNull(buildExplodedLibraryKotlin("no fences here"))
    }

    @Test
    fun `buildExplodedLibraryKotlin returns null when body section is missing`() {
        val source = "```lib\ncom.example:lib:1.0\n```"
        assertNull(buildExplodedLibraryKotlin(source))
    }

    @Test
    fun `buildExplodedLibraryKotlin emits body verbatim when no imports and no package`() {
        val result = buildExplodedLibraryKotlin(minimalSource)
        assertNotNull(result)
        assertEquals("class Foo\n", result)
    }

    @Test
    fun `buildExplodedLibraryKotlin emits package directive when packageName provided`() {
        val result = buildExplodedLibraryKotlin(minimalSource, "com.example")
        assertNotNull(result)
        assertTrue(result!!.startsWith("package com.example\n"))
        assertTrue("class Foo" in result)
    }

    @Test
    fun `buildExplodedLibraryKotlin emits imports followed by blank line and body`() {
        val result = buildExplodedLibraryKotlin(sourceWithImports)
        assertNotNull(result)
        assertTrue("import org.example.Bar" in result!!)
        assertTrue("import org.example.Baz" in result)
        assertTrue("class Foo : Bar()" in result)
        val lines = result.lines()
        val lastImportIdx = lines.indexOfLast { it.startsWith("import ") }
        assertTrue(lastImportIdx >= 0, "expected at least one import line")
        assertTrue(lines[lastImportIdx + 1].isBlank(), "expected blank line after imports")
    }

    @Test
    fun `buildExplodedLibraryKotlin filters out kanton imports`() {
        val source = """
            ```lib
            com.example:lib:1.0

            class Foo
            ```

            # dependencies
            org.example:dep:1.0
              kanton.Script
              org.example.Keep
        """.trimIndent()
        val result = buildExplodedLibraryKotlin(source)
        assertNotNull(result)
        assertFalse("import kanton.Script" in result!!, "kanton.* imports should be filtered")
        assertTrue("import org.example.Keep" in result)
    }

    // --- buildLibraryGradleKts ---

    private val coords = LibCoords(group = "com.example", artifact = "my-lib", version = "0.1.0", help = "help")

    @Test
    fun `buildLibraryGradleKts declares java-library and maven-publish plugins`() {
        val gradle = buildLibraryGradleKts(coords, emptyList())
        assertTrue("`java-library`" in gradle)
        assertTrue("`maven-publish`" in gradle)
    }

    @Test
    fun `buildLibraryGradleKts sets group and version from coords`() {
        val gradle = buildLibraryGradleKts(coords, emptyList())
        assertTrue("group = \"com.example\"" in gradle, "missing group declaration")
        assertTrue("version = \"0.1.0\"" in gradle, "missing version declaration")
    }

    @Test
    fun `buildLibraryGradleKts omits dependencies block when deps are empty`() {
        val gradle = buildLibraryGradleKts(coords, emptyList())
        assertFalse("dependencies {" in gradle, "unexpected dependencies block")
    }

    @Test
    fun `buildLibraryGradleKts emits each dep under the correct config call`() {
        val deps = listOf(
            LibDepEntry(coord = "org.example:api-lib:1.0", config = "api", imports = emptyList()),
            LibDepEntry(coord = "org.example:impl-lib:2.0", config = "implementation", imports = emptyList()),
            LibDepEntry(coord = "org.example:test-lib:3.0", config = "testImplementation", imports = emptyList())
        )
        val gradle = buildLibraryGradleKts(coords, deps)
        assertTrue("api(\"org.example:api-lib:1.0\")" in gradle)
        assertTrue("implementation(\"org.example:impl-lib:2.0\")" in gradle)
        assertTrue("testImplementation(\"org.example:test-lib:3.0\")" in gradle)
    }

    @Test
    fun `buildLibraryGradleKts publishing block references coords`() {
        val gradle = buildLibraryGradleKts(coords, emptyList())
        assertTrue("publishing {" in gradle)
        assertTrue("groupId = \"com.example\"" in gradle)
        assertTrue("artifactId = \"my-lib\"" in gradle)
        assertTrue("version = \"0.1.0\"" in gradle)
    }
}
