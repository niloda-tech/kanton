package kanton.core.facade

import kanton.core.cli.Scaffold
import kanton.core.cli.SyncBack
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SyncBackAcceptanceTest : FacadeBase() {

    @Test
    fun `run throws when file does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            SyncBack.run(File("/tmp/no-such-facade-test-99994.kt.md"))
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }

    @Test
    fun `run throws when no scaffold exists`() {
        val file = fixtureFile()
        try {
            assertFalse(cacheDir.exists(), "Pre-condition: cache dir should not exist")
            val ex = assertThrows<IllegalArgumentException> {
                SyncBack.run(file)
            }
            assertTrue(
                "scaffold" in ex.message!!.lowercase() || "not found" in ex.message!!.lowercase(),
                "message: ${ex.message}"
            )
        } finally {
            file.delete()
        }
    }

    @Test
    fun `run returns NoChanges on unmodified scaffold`() {
        val file = fixtureFile()
        try {
            Scaffold.run(file)
            val result = SyncBack.run(file)
            assertIs<SyncBack.Result.NoChanges>(result, "Expected NoChanges but got: $result")
        } finally {
            file.delete()
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run returns Synced and updates file when run body changes`() {
        val file = fixtureFile()
        try {
            Scaffold.run(file)

            val mainKtFile = File(cacheDir, "src/main/kotlin/Main.kt")
            val original = mainKtFile.readText()
            val modified = original.replace(
                "echo(\"Hello, \$name!\")",
                "echo(\"Hello, \$name! (edited)\")"
            )
            mainKtFile.writeText(modified)

            val result = SyncBack.run(file)
            assertIs<SyncBack.Result.Synced>(result, "Expected Synced but got: $result")
            assertEquals(file.absolutePath, result.file.absolutePath)
            assertTrue("(edited)" in file.readText(), "Expected edited run body in synced file")
        } finally {
            file.delete()
            cacheDir.deleteRecursively()
        }
    }
}
