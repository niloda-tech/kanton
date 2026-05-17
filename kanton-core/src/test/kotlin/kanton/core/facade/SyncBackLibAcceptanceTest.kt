package kanton.core.facade

import kanton.core.lib.ScaffoldLib
import kanton.core.lib.SyncBackLib
import kanton.core.lib.SyncBackLibResult
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SyncBackLibAcceptanceTest : LibFacadeBase() {

    @Test
    fun `run returns Failed when source file does not exist`() {
        val result = SyncBackLib.run(File("/tmp/no-such-lib-facade-test-99994.kt.md"))
        val failed = assertIs<SyncBackLibResult.Failed>(result)
        assertTrue("not found" in failed.reason.lowercase(), "reason: ${failed.reason}")
    }

    @Test
    fun `run returns Failed when no scaffold exists`() {
        val file = libFixtureFile()
        try {
            libCacheDir.deleteRecursively()
            val result = SyncBackLib.run(file)
            val failed = assertIs<SyncBackLibResult.Failed>(result)
            assertTrue(
                "scaffold" in failed.reason.lowercase() || "no exploded" in failed.reason.lowercase(),
                "reason: ${failed.reason}"
            )
        } finally {
            file.delete()
        }
    }

    @Test
    fun `run returns NoChanges when scaffold is unmodified`() {
        val file = libFixtureFile()
        try {
            ScaffoldLib.run(file)
            val result = SyncBackLib.run(file)
            assertIs<SyncBackLibResult.NoChanges>(result, "expected NoChanges, got: $result")
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run returns Synced and updates source when library body changes`() {
        val file = libFixtureFile()
        try {
            ScaffoldLib.run(file)

            val libKtFile = File(libCacheDir, "$libFixtureKtPath")
            libKtFile.writeText(
                libKtFile.readText().replace(
                    "fun greet(): String = \"hello\"",
                    "fun greet(): String = \"hello (edited)\""
                )
            )

            val result = SyncBackLib.run(file)
            val synced = assertIs<SyncBackLibResult.Synced>(result, "expected Synced, got: $result")
            assertEquals(libFixtureArtifact, synced.artifact)

            val updated = file.readText()
            assertTrue("(edited)" in updated, "edited body not reflected in source:\n$updated")
            assertTrue("  org.example.Bar" in updated, "preserved import missing:\n$updated")
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run removes deps from source when coord is deleted from build file`() {
        val file = libFixtureFile()
        try {
            ScaffoldLib.run(file)

            val gradleKts = File(libCacheDir, "build.gradle.kts")
            val stripped = gradleKts.readText().replace(
                Regex("dependencies \\{[^}]*\\}"),
                "dependencies { }"
            )
            gradleKts.writeText(stripped)

            val result = SyncBackLib.run(file)
            assertIs<SyncBackLibResult.Synced>(result, "expected Synced, got: $result")

            val updated = file.readText()
            assertFalse(
                "api org.example:dep:1.0" in updated,
                "removed coord should no longer appear in source:\n$updated"
            )
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }
}
