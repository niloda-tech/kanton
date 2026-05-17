package kanton.plugin.steps

import kanton.plugin.editor.actions.shared.findKantonBinary
import kanton.plugin.editor.actions.shared.runBinary
import io.cucumber.java.After
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class BinarySteps {

    private val ctx get() = PluginScenarioContext
    private val tempDirs = mutableListOf<File>()

    @After
    fun cleanUpTempDirs() {
        tempDirs.forEach { it.deleteRecursively() }
        tempDirs.clear()
    }

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("I have a source file nested below a directory that contains the kanton binary")
    fun sourceFileNestedBelowDirectoryWithBinary() {
        val root = Files.createTempDirectory("kanton-binary-root").toFile()
        tempDirs.add(root)

        val binDir = File(root, "scripts")
        binDir.mkdirs()
        val binary = File(binDir, "kanton")
        binary.writeText("#!/bin/sh\necho 'Done'\n")
        binary.setExecutable(true)

        val nested = File(root, "level1/level2")
        nested.mkdirs()
        val sourceFile = File(nested, "my-script.kt.md")
        sourceFile.writeText("# placeholder")

        ctx.discoveredBinary = null
        ctx.input = sourceFile.absolutePath
    }

    @Given("I have a source file in a directory with no kanton binary nearby")
    fun sourceFileWithNoBinaryNearby() {
        val root = Files.createTempDirectory("kanton-no-binary").toFile()
        tempDirs.add(root)

        val sourceFile = File(root, "my-script.kt.md")
        sourceFile.writeText("# placeholder")

        ctx.discoveredBinary = null
        ctx.input = sourceFile.absolutePath
    }

    @Given("I have a kanton binary that reports success")
    fun binaryThatSucceeds() {
        val binDir = Files.createTempDirectory("kanton-binary-success").toFile()
        tempDirs.add(binDir)

        val binary = File(binDir, "kanton")
        binary.writeText("#!/bin/sh\necho 'Done'\n")
        binary.setExecutable(true)

        ctx.discoveredBinary = binary
    }

    @Given("I have a kanton binary that exits with an error")
    fun binaryThatFails() {
        val binDir = Files.createTempDirectory("kanton-binary-failure").toFile()
        tempDirs.add(binDir)

        val binary = File(binDir, "kanton")
        binary.writeText("#!/bin/sh\necho 'something went wrong' >&2\nexit 1\n")
        binary.setExecutable(true)

        ctx.discoveredBinary = binary
    }

    @Given("I have a source file to process")
    fun sourceFileToProcess() {
        val sourceDir = Files.createTempDirectory("kanton-source").toFile()
        tempDirs.add(sourceDir)

        val sourceFile = File(sourceDir, "my-script.kt.md")
        sourceFile.writeText("# placeholder")

        ctx.input = sourceFile.absolutePath
    }

    // ── When ──────────────────────────────────────────────────────────────────

    @When("the IDE searches for the kanton binary")
    fun ideSearchesForBinary() {
        val sourceFile = File(ctx.input)
        ctx.discoveredBinary = findKantonBinary(sourceFile, systemPath = "", homeDir = null)
    }

    @When("the IDE invokes the binary with subcommand {string}")
    fun ideInvokesBinaryWithSubcommand(subcommand: String) {
        val binary = checkNotNull(ctx.discoveredBinary) { "No binary was set up for this scenario" }
        val sourceFile = File(ctx.input)
        ctx.binaryResult = runBinary(binary, subcommand, "--file", sourceFile.absolutePath)
    }

    @When("the IDE invokes the binary")
    fun ideInvokesBinary() {
        val binary = checkNotNull(ctx.discoveredBinary) { "No binary was set up for this scenario" }
        val sourceFile = File(ctx.input)
        ctx.binaryResult = runBinary(binary, "--file", sourceFile.absolutePath)
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("the binary is found")
    fun binaryIsFound() {
        assertNotNull("Expected a binary to be found but none was", ctx.discoveredBinary)
    }

    @Then("no binary is found")
    fun noBinaryIsFound() {
        assertNull("Expected no binary to be found but one was: ${ctx.discoveredBinary}", ctx.discoveredBinary)
    }

    @Then("the invocation succeeds")
    fun invocationSucceeds() {
        val result = checkNotNull(ctx.binaryResult)
        assertEquals("Expected exit code 0 but got ${result.exitCode}", 0, result.exitCode)
    }

    @Then("the invocation indicates failure")
    fun invocationIndicatesFailure() {
        val result = checkNotNull(ctx.binaryResult)
        assertNotEquals("Expected a non-zero exit code but got 0", 0, result.exitCode)
    }

    @And("the error output from the binary is captured")
    fun errorOutputIsCaptured() {
        val result = checkNotNull(ctx.binaryResult)
        assertTrue("Expected stderr to be non-empty but it was blank", result.stderr.isNotBlank())
    }
}
