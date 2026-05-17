package kanton.core.facade

import kanton.core.lib.DeleteScaffoldLib
import kanton.core.lib.ScaffoldLib
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteScaffoldLibAcceptanceTest : LibFacadeBase() {

    @Test
    fun `run by artifact throws when cache dir does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            DeleteScaffoldLib.run("nonexistent-lib-facade-test-99992")
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }

    @Test
    fun `run by artifact deletes cache dir`() {
        val file = libFixtureFile()
        try {
            ScaffoldLib.run(file)
            assertTrue(libCacheDir.exists(), "precondition: scaffold should exist")
            val deleted = DeleteScaffoldLib.run(libFixtureArtifact)
            assertEquals(libCacheDir.absolutePath, deleted.absolutePath)
            assertFalse(libCacheDir.exists(), "cache dir should be gone")
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run by file throws when source file does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            DeleteScaffoldLib.run(File("/tmp/no-such-lib-facade-test-99993.kt.md"))
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }

    @Test
    fun `run by file resolves artifact and deletes cache dir`() {
        val file = libFixtureFile()
        try {
            ScaffoldLib.run(file)
            val deleted = DeleteScaffoldLib.run(file)
            assertEquals(libCacheDir.absolutePath, deleted.absolutePath)
            assertFalse(libCacheDir.exists(), "cache dir should be gone")
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }
}
