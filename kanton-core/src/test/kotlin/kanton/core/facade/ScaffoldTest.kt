package kanton.core.facade

import kanton.core.cli.Scaffold
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScaffoldAcceptanceTest : FacadeBase() {

    @Test
    fun `run throws when file does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            Scaffold.run(File("/tmp/no-such-facade-test-99991.kt.md"))
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }

    @Test
    fun `run creates cache dir with Main-kt and build files`() {
        val file = fixtureFile()
        try {
            val result = Scaffold.run(file)
            assertEquals(fixtureName, result.scriptName)
            assertTrue(cacheDir.isDirectory, "cache dir missing: ${cacheDir.absolutePath}")
            assertTrue(File(cacheDir, "src/main/kotlin/Main.kt").exists(), "Main.kt missing")
            assertTrue(File(cacheDir, "build.gradle.kts").exists(), "build.gradle.kts missing")
            assertTrue(File(cacheDir, "src/main/kotlin/kanton/Stubs.kt").exists(), "Stubs.kt missing")
        } finally {
            file.delete()
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run produces Main-kt with correct class name`() {
        val file = fixtureFile()
        try {
            Scaffold.run(file)
            val mainKt = File(cacheDir, "src/main/kotlin/Main.kt").readText()
            assertTrue("class FacadeTestFixture" in mainKt, "Expected 'class FacadeTestFixture' in Main.kt:\n$mainKt")
            assertTrue("override fun run()" in mainKt, "Expected run() in Main.kt")
        } finally {
            file.delete()
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run produces Main-kt with declared options`() {
        val file = fixtureFile()
        try {
            Scaffold.run(file)
            val mainKt = File(cacheDir, "src/main/kotlin/Main.kt").readText()
            assertTrue("val name" in mainKt, "Expected val name option in Main.kt:\n$mainKt")
        } finally {
            file.delete()
            cacheDir.deleteRecursively()
        }
    }
}
