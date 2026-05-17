package kanton.core.facade

import kanton.core.cli.DeleteScaffold
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteScaffoldAcceptanceTest : FacadeBase() {

    @Test
    fun `run by name throws when cache dir does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            DeleteScaffold.run("nonexistent-facade-test-99992")
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }

    @Test
    fun `run by name deletes cache dir`() {
        cacheDir.mkdirs()
        try {
            val deleted = DeleteScaffold.run(fixtureName)
            assertEquals(cacheDir.absolutePath, deleted.absolutePath)
            assertFalse(cacheDir.exists(), "Cache dir should be gone: ${cacheDir.absolutePath}")
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run by file throws when source file does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            DeleteScaffold.run(File("/tmp/no-such-facade-test-99993.kt.md"))
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }

    @Test
    fun `run by file deletes cache dir`() {
        val file = fixtureFile()
        cacheDir.mkdirs()
        try {
            val deleted = DeleteScaffold.run(file)
            assertEquals(cacheDir.absolutePath, deleted.absolutePath)
            assertFalse(cacheDir.exists(), "Cache dir should be gone")
        } finally {
            file.delete()
            cacheDir.deleteRecursively()
        }
    }
}
