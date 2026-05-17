package kanton.core.steps

import io.cucumber.java.After
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.security.MessageDigest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kanton.core.cli.compile.main as bootstrapMain

class BootstrapSteps {

    private val fixtureSource = """
        ```cli
        bootstrap-fixture:A minimal script for bootstrap acceptance tests
        --name, -n, Name to greet = "World"

        echo("Hello, ${'$'}name!")
        ```

        # dependencies
        com.github.ajalt.clikt:clikt:5.1.0
          kanton.Script
    """.trimIndent()

    private val fixtureName = "bootstrap-fixture"
    private val binDir = File(System.getProperty("user.home"), ".kanton/bin")
    private val cacheDir get() = File(System.getProperty("user.home"), ".kanton/cache/$fixtureName")
    private val binaryFile get() = File(binDir, fixtureName)
    private val checksumFile get() = File(binDir, "$fixtureName.md5")

    private var fixtureFile: File? = null
    private var capturedOutput: String = ""

    @After
    fun cleanupBootstrap() {
        fixtureFile?.delete()
        binaryFile.delete()
        checksumFile.delete()
        cacheDir.deleteRecursively()
        fixtureFile = null
        capturedOutput = ""
    }

    private fun computeMd5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(file.readBytes())
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    // --- Given ---

    @Given("a valid bootstrap fixture file")
    fun aValidBootstrapFixtureFile() {
        fixtureFile = File(System.getProperty("java.io.tmpdir"), "$fixtureName.kt.md")
            .also { it.writeText(fixtureSource) }
    }

    @Given("a compiled binary already exists for the fixture")
    fun aCompiledBinaryAlreadyExistsForTheFixture() {
        binDir.mkdirs()
        binaryFile.writeText("#!/bin/sh\necho fake")
        binaryFile.setExecutable(true)
    }

    @Given("the saved checksum matches the current source")
    fun theSavedChecksumMatchesTheCurrentSource() {
        checksumFile.writeText(computeMd5(fixtureFile!!))
    }

    @Given("the saved checksum does not match the current source")
    fun theSavedChecksumDoesNotMatchTheCurrentSource() {
        checksumFile.writeText("0000000000000000000000000000dead")
    }

    @Given("no compiled binary exists for the fixture")
    fun noCompiledBinaryExistsForTheFixture() {
        binaryFile.delete()
        checksumFile.delete()
        assertFalse(binaryFile.exists(), "Pre-condition: binary should not exist")
    }

    // --- When ---

    @When("I bootstrap the fixture")
    fun iBootstrapTheFixture() {
        val baos = ByteArrayOutputStream()
        val oldOut = System.out
        System.setOut(PrintStream(baos))
        try {
            bootstrapMain(arrayOf(fixtureFile!!.absolutePath))
        } finally {
            System.setOut(oldOut)
        }
        capturedOutput = baos.toString()
    }

    // --- Then ---

    @Then("the binary is not recompiled")
    fun theBinaryIsNotRecompiled() {
        assertTrue("up-to-date, skipping" in capturedOutput,
            "Expected skip message but got: $capturedOutput")
    }

    @Then("the binary is recompiled")
    fun theBinaryIsRecompiled() {
        assertTrue("recompiling" in capturedOutput || "Bootstrapping" in capturedOutput,
            "Expected recompile message but got: $capturedOutput")
    }

    @And("the saved checksum is updated to match the current source")
    fun theSavedChecksumIsUpdatedToMatchTheCurrentSource() {
        val expected = computeMd5(fixtureFile!!)
        val actual = checksumFile.readText().trim()
        assertEquals(expected, actual, "Checksum should match current source")
    }

    @Then("a compiled binary is produced for the fixture")
    fun aCompiledBinaryIsProducedForTheFixture() {
        assertTrue(binaryFile.exists(), "Binary should exist: ${binaryFile.absolutePath}")
        assertTrue(binaryFile.canExecute(), "Binary should be executable: ${binaryFile.absolutePath}")
    }

    @And("a checksum is saved alongside the binary")
    fun aChecksumIsSavedAlongsideTheBinary() {
        assertTrue(checksumFile.exists(), "Checksum file should exist: ${checksumFile.absolutePath}")
        assertTrue(checksumFile.readText().isNotBlank(), "Checksum file should not be empty")
    }
}
