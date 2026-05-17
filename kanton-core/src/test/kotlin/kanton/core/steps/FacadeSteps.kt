package kanton.core.steps

import io.cucumber.java.After
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kanton.core.cli.compile.Compile
import kanton.core.cli.DeleteScaffold
import kanton.core.cli.Scaffold
import kanton.core.cli.SyncBack
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FacadeSteps {

    private val fixtureSource = """
        # facade-test-fixture - acceptance test script

        ```cli
        facade-test-fixture:A minimal script for facade acceptance tests
        --name, -n, Name to greet = "World"

        echo("Hello, ${'$'}name!")
        ```

        # dependencies
        com.github.ajalt.clikt:clikt:5.1.0
          kanton.Script
    """.trimIndent()

    private val fixtureWithArgs = """
        ```cli
        facade-test-fixture:A script with positional arguments
        FILE, The file to process

        echo("Processing ${'$'}file")
        ```

        # dependencies
        com.github.ajalt.clikt:clikt:5.1.0
          kanton.Script
    """.trimIndent()

    private val fixtureWithBoth = """
        ```cli
        facade-test-fixture:A script with options and arguments
        FILE, The file to process
        --verbose, -v, Enable verbose output

        echo("Processing ${'$'}file verbose=${'$'}verbose")
        ```

        # dependencies
        com.github.ajalt.clikt:clikt:5.1.0
          kanton.Script
    """.trimIndent()

    private val fixtureName = "facade-test-fixture"
    private val cacheDir get() = File(System.getProperty("user.home"), ".kanton/cache/$fixtureName")
    private val mainKt get() = File(cacheDir, "src/main/kotlin/Main.kt").readText()

    private var fixtureFile: File? = null
    private var lastException: Exception? = null
    private var lastSyncBackResult: SyncBack.Result? = null
    private var lastCompileResult: File? = null

    @After
    fun cleanup() {
        fixtureFile?.delete()
        lastCompileResult?.delete()
        cacheDir.deleteRecursively()
        fixtureFile = null
        lastException = null
        lastSyncBackResult = null
        lastCompileResult = null
    }

    // --- Given ---

    @Given("a valid .kt.md fixture file")
    fun aValidFixtureFile() {
        fixtureFile = File.createTempFile("facade-test-fixture", ".kt.md")
            .also { it.writeText(fixtureSource) }
    }

    @Given("a .kt.md fixture file with positional arguments")
    fun aFixtureFileWithPositionalArgs() {
        fixtureFile = File.createTempFile("facade-test-fixture", ".kt.md")
            .also { it.writeText(fixtureWithArgs) }
    }

    @Given("a .kt.md fixture file with options and positional arguments")
    fun aFixtureFileWithOptionsAndPositionalArgs() {
        fixtureFile = File.createTempFile("facade-test-fixture", ".kt.md")
            .also { it.writeText(fixtureWithBoth) }
    }

    @Given("a .kt.md file with no run body")
    fun aFixtureFileWithNoRunBody() {
        fixtureFile = File.createTempFile("facade-test-fixture", ".kt.md")
            .also { it.writeText("# not a valid script\n\nJust some text.\n") }
    }

    @Given("no source file exists")
    fun noSourceFileExists() {
        fixtureFile = File(System.getProperty("java.io.tmpdir"), "no-such-facade-test-99991.kt.md")
    }

    @Given("a scaffolded project exists")
    fun aScaffoldedProjectExists() {
        cacheDir.mkdirs()
    }

    @Given("no scaffolded project exists")
    fun noScaffoldedProjectExists() {
        cacheDir.deleteRecursively()
        assertFalse(cacheDir.exists(), "Pre-condition: cache dir should not exist")
    }

    @Given("no scaffold exists for the fixture")
    fun noScaffoldExistsForFixture() {
        cacheDir.deleteRecursively()
        assertFalse(cacheDir.exists(), "Pre-condition: cache dir should not exist")
    }

    @Given("the fixture has been scaffolded")
    fun theFixtureHasBeenScaffolded() {
        Scaffold.run(fixtureFile!!)
    }

    @Given("the run body in Main.kt has been edited to add {string}")
    fun theRunBodyHasBeenEdited(marker: String) {
        val mainKtFile = File(cacheDir, "src/main/kotlin/Main.kt")
        mainKtFile.writeText(
            mainKtFile.readText().replace(
                "echo(\"Hello, \$name!\")",
                "echo(\"Hello, \$name! $marker\")"
            )
        )
    }

    // --- When ---

    @When("I scaffold the script")
    fun iScaffoldTheScript() {
        runCatching { Scaffold.run(fixtureFile!!) }
            .onFailure { lastException = it as? Exception }
    }

    @When("I delete the scaffold by name")
    fun iDeleteTheScaffoldByName() {
        runCatching { DeleteScaffold.run(fixtureName) }
            .onFailure { lastException = it as? Exception }
    }

    @When("I delete the scaffold by source file")
    fun iDeleteTheScaffoldBySourceFile() {
        runCatching { DeleteScaffold.run(fixtureFile!!) }
            .onFailure { lastException = it as? Exception }
    }

    @When("I compile the script")
    fun iCompileTheScript() {
        lastCompileResult = runCatching { Compile.run(fixtureFile!!) }
            .onFailure { lastException = it as? Exception }
            .getOrNull()
    }

    @When("I attempt to sync back")
    fun iAttemptToSyncBack() {
        lastSyncBackResult = runCatching { SyncBack.run(fixtureFile!!) }
            .onFailure { lastException = it as? Exception }
            .getOrNull()
    }

    @When("I sync back")
    fun iSyncBack() {
        lastSyncBackResult = SyncBack.run(fixtureFile!!)
    }

    // --- Then ---

    @Then("the process should fail")
    fun theProcessShouldFail() {
        assertNotNull(lastException, "Expected the process to fail but no exception was thrown")
    }

    @Then("the error message should indicate the source file was not found")
    fun errorIndicatesSourceFileNotFound() {
        assertTrue("not found" in lastException!!.message!!.lowercase(), "message: ${lastException!!.message}")
    }

    @Then("the error message should indicate the project was not found")
    fun errorIndicatesProjectNotFound() {
        assertTrue("not found" in lastException!!.message!!.lowercase(), "message: ${lastException!!.message}")
    }

    @Then("the error message should indicate the project needs to be scaffolded first")
    fun errorIndicatesScaffoldNeeded() {
        assertTrue("scaffold" in lastException!!.message!!.lowercase(), "message: ${lastException!!.message}")
    }

    @Then("the error message should indicate the script could not be parsed")
    fun errorIndicatesScriptUnparseable() {
        assertTrue("cannot parse" in lastException!!.message!!.lowercase(), "message: ${lastException!!.message}")
    }

    @Then("an editable project is created for the script")
    fun editableProjectCreated() {
        assertTrue(cacheDir.isDirectory, "Expected project directory at ${cacheDir.absolutePath}")
    }

    @And("the project contains a build file")
    fun projectContainsBuildFile() {
        assertTrue(File(cacheDir, "build.gradle.kts").exists(), "build.gradle.kts missing")
    }

    @And("the project contains CLI stubs")
    fun projectContainsCliStubs() {
        assertTrue(File(cacheDir, "src/main/kotlin/kanton/Stubs.kt").exists(), "Stubs.kt missing")
    }

    @Then("the generated class is named after the script")
    fun generatedClassNamedAfterScript() {
        assertTrue("class FacadeTestFixture" in mainKt, "Expected class named after script in Main.kt")
    }

    @And("the generated class has a run method")
    fun generatedClassHasRunMethod() {
        assertTrue("override fun run()" in mainKt, "Expected run() in Main.kt")
    }

    @Then("the generated class includes the declared options")
    fun generatedClassIncludesDeclaredOptions() {
        assertTrue("val name" in mainKt || "val verbose" in mainKt, "Expected declared option in Main.kt")
    }

    @Then("the generated class includes the declared positional arguments")
    fun generatedClassIncludesDeclaredPositionalArguments() {
        assertTrue("by argument(\"FILE\"" in mainKt, "Expected declared argument 'FILE' in Main.kt")
    }

    @Then("the project directory no longer exists")
    fun projectDirectoryGone() {
        assertFalse(cacheDir.exists(), "Expected project directory to be gone: ${cacheDir.absolutePath}")
    }

    @Then("a self-contained executable is produced")
    fun selfContainedExecutableProduced() {
        val result = assertNotNull(lastCompileResult, "Compile produced no output file: ${lastException?.message}")
        assertTrue(result.exists(), "Output binary not found: ${result.absolutePath}")
        assertTrue(result.canExecute(), "Output binary is not executable: ${result.absolutePath}")
    }

    @Then("no changes are written to the source file")
    fun noChangesWritten() {
        assertIs<SyncBack.Result.NoChanges>(lastSyncBackResult, "Expected NoChanges but got: $lastSyncBackResult")
    }

    @Then("the source file is updated with the edits")
    fun sourceFileUpdatedWithEdits() {
        assertIs<SyncBack.Result.Synced>(lastSyncBackResult, "Expected Synced but got: $lastSyncBackResult")
    }

    @And("the fixture file contains {string}")
    fun fixtureFileContains(text: String) {
        assertTrue(text in fixtureFile!!.readText(), "Expected '$text' in fixture file")
    }
}
