package kanton.core.facade

import kanton.core.lib.ScaffoldLib
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScaffoldLibAcceptanceTest : LibFacadeBase() {

    @Test
    fun `run throws when file does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            ScaffoldLib.run(File("/tmp/no-such-lib-facade-test-99991.kt.md"))
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }

    @Test
    fun `run creates cache dir with library source and build files`() {
        val file = libFixtureFile()
        try {
            val result = ScaffoldLib.run(file)
            assertEquals(libFixtureArtifact, result.artifact)
            assertTrue(libCacheDir.isDirectory, "cache dir missing: ${libCacheDir.absolutePath}")
            assertTrue(File(libCacheDir, "settings.gradle.kts").exists(), "settings.gradle.kts missing")
            assertTrue(File(libCacheDir, "build.gradle.kts").exists(), "build.gradle.kts missing")
            assertTrue(
                File(libCacheDir, "$libFixtureKtPath").exists(),
                "library source file missing"
            )
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run generates library source with class and imports from deps`() {
        val file = libFixtureFile()
        try {
            ScaffoldLib.run(file)
            val libKt = File(libCacheDir, "$libFixtureKtPath").readText()
            assertTrue("class FacadeLibFixture" in libKt, "generated class missing:\n$libKt")
            assertTrue("import org.example.Bar" in libKt, "dep import missing:\n$libKt")
            assertTrue("fun greet()" in libKt, "body content missing:\n$libKt")
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run generates build file with correct coords and api dep`() {
        val file = libFixtureFile()
        try {
            ScaffoldLib.run(file)
            val gradle = File(libCacheDir, "build.gradle.kts").readText()
            assertTrue("group = \"com.example\"" in gradle, "group missing:\n$gradle")
            assertTrue("version = \"0.1.0\"" in gradle, "version missing:\n$gradle")
            assertTrue("api(\"org.example:dep:1.0\")" in gradle, "api dep missing:\n$gradle")
            assertTrue("artifactId = \"facade-lib-fixture\"" in gradle, "publishing artifactId missing")
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }

    @Test
    fun `run creates symlink from cache dir back to source file`() {
        val file = libFixtureFile()
        try {
            ScaffoldLib.run(file)
            val symlink = File(libCacheDir, file.name)
            assertTrue(symlink.exists(), "symlink back to source missing: ${symlink.absolutePath}")
            assertEquals(file.canonicalPath, symlink.canonicalPath, "symlink does not resolve to source")
        } finally {
            file.delete()
            libCacheDir.deleteRecursively()
        }
    }
}
