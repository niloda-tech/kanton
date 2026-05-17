package kanton.core.steps

import io.cucumber.java.After
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kanton.core.lib.DeleteScaffoldLib
import kanton.core.lib.ScaffoldLib
import kanton.core.lib.SyncBackLib
import kanton.core.lib.SyncBackLibResult
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LibFacadeSteps {

    private val libFixtureSource = """
        # facade-lib-fixture - acceptance test library

        ```lib
        com.example:facade-lib-fixture:0.1.0:A minimal library for facade acceptance tests

        class FacadeLibFixture {
            fun greet(): String = "hello"
        }
        ```

        # dependencies
        api org.example:dep:1.0
          org.example.Bar
    """.trimIndent()

    private val libFixtureArtifact = "facade-lib-fixture"
    private val libFixtureClassName = "FacadeLibFixture"
    private val libFixtureGroup = "com.example"
    private val libCacheDir get() = File(System.getProperty("user.home"), ".kanton/cache/$libFixtureArtifact")
    private val libKtFile get() = File(libCacheDir, "src/main/kotlin/${libFixtureGroup.replace('.', '/')}/$libFixtureClassName.kt")

    private var libFixtureFile: File? = null
    private var lastException: Exception? = null
    private var lastSyncResult: SyncBackLibResult? = null

    @After
    fun cleanup() {
        libFixtureFile?.delete()
        libCacheDir.deleteRecursively()
        libFixtureFile = null
        lastException = null
        lastSyncResult = null
    }

    // --- Given ---

    @Given("a valid .kt.md library fixture file")
    fun aValidLibFixtureFile() {
        libFixtureFile = File.createTempFile("facade-lib-fixture", ".kt.md")
            .also { it.writeText(libFixtureSource) }
    }

    @Given("no library source file exists")
    fun noLibSourceFileExists() {
        libFixtureFile = File(System.getProperty("java.io.tmpdir"), "no-such-lib-facade-test-99991.kt.md")
    }

    @Given("a library scaffold exists")
    fun aLibraryScaffoldExists() {
        libCacheDir.mkdirs()
    }

    @Given("no library scaffold exists")
    fun noLibraryScaffoldExists() {
        libCacheDir.deleteRecursively()
        assertFalse(libCacheDir.exists(), "Pre-condition: cache dir should not exist")
    }

    @And("no library scaffold exists for the fixture")
    fun noLibraryScaffoldForFixture() {
        libCacheDir.deleteRecursively()
        assertFalse(libCacheDir.exists(), "Pre-condition: cache dir should not exist")
    }

    @And("the library fixture has been scaffolded")
    fun theLibraryFixtureHasBeenScaffolded() {
        ScaffoldLib.run(libFixtureFile!!)
    }

    @And("the library body has been edited to add {string}")
    fun theLibraryBodyHasBeenEdited(marker: String) {
        libKtFile.writeText(
            libKtFile.readText().replace(
                "fun greet(): String = \"hello\"",
                "fun greet(): String = \"hello $marker\""
            )
        )
    }

    // --- When ---

    @When("I scaffold the library")
    fun iScaffoldTheLibrary() {
        runCatching { ScaffoldLib.run(libFixtureFile!!) }
            .onFailure { lastException = it as? Exception }
    }

    @When("I delete the library scaffold by artifact")
    fun iDeleteTheLibraryScaffoldByArtifact() {
        runCatching { DeleteScaffoldLib.run(libFixtureArtifact) }
            .onFailure { lastException = it as? Exception }
    }

    @When("I delete the library scaffold by source file")
    fun iDeleteTheLibraryScaffoldBySourceFile() {
        runCatching { DeleteScaffoldLib.run(libFixtureFile!!) }
            .onFailure { lastException = it as? Exception }
    }

    @When("I sync the library back")
    fun iSyncTheLibraryBack() {
        lastSyncResult = SyncBackLib.run(libFixtureFile!!)
    }

    // --- Then ---

    @Then("the library scaffold should fail")
    fun theLibraryScaffoldShouldFail() {
        assertNotNull(lastException, "Expected the operation to fail but no exception was thrown")
    }

    @And("the library error message should indicate the source file was not found")
    fun libraryErrorIndicatesSourceFileNotFound() {
        assertTrue(
            "not found" in lastException!!.message!!.lowercase(),
            "message: ${lastException!!.message}"
        )
    }

    @And("the library error message should indicate the project was not found")
    fun libraryErrorIndicatesProjectNotFound() {
        assertTrue(
            "not found" in lastException!!.message!!.lowercase(),
            "message: ${lastException!!.message}"
        )
    }

    @Then("an editable library project is created")
    fun editableLibraryProjectCreated() {
        assertTrue(libCacheDir.isDirectory, "Expected project directory at ${libCacheDir.absolutePath}")
    }

    @And("the library project contains a build file")
    fun libraryProjectContainsBuildFile() {
        assertTrue(File(libCacheDir, "build.gradle.kts").exists(), "build.gradle.kts missing")
    }

    @And("the library project contains a library source file")
    fun libraryProjectContainsLibrarySourceFile() {
        assertTrue(libKtFile.exists(), "library source file missing: ${libKtFile.absolutePath}")
    }

    @Then("the library source file contains a class named after the artifact")
    fun librarySourceContainsClassNamedAfterArtifact() {
        val libKt = libKtFile.readText()
        assertTrue("class FacadeLibFixture" in libKt, "Expected class named after artifact in library source")
    }

    @Then("the library sync should report a failure")
    fun theLibrarySyncShouldReportFailure() {
        assertIs<SyncBackLibResult.Failed>(lastSyncResult, "Expected Failed but got: $lastSyncResult")
    }

    @And("the library failure reason should indicate the source file was not found")
    fun libraryFailureReasonIndicatesSourceFileNotFound() {
        val failed = assertIs<SyncBackLibResult.Failed>(lastSyncResult)
        assertTrue("not found" in failed.reason.lowercase(), "reason: ${failed.reason}")
    }

    @And("the library failure reason should indicate the project needs to be scaffolded first")
    fun libraryFailureReasonIndicatesScaffoldNeeded() {
        val failed = assertIs<SyncBackLibResult.Failed>(lastSyncResult)
        val reason = failed.reason.lowercase()
        assertTrue("scaffold" in reason || "no exploded" in reason, "reason: ${failed.reason}")
    }

    @Then("no library changes are written to the source file")
    fun noLibraryChangesWritten() {
        assertIs<SyncBackLibResult.NoChanges>(lastSyncResult, "Expected NoChanges but got: $lastSyncResult")
    }

    @Then("the library source file is updated with the edits")
    fun librarySourceFileUpdatedWithEdits() {
        assertIs<SyncBackLibResult.Synced>(lastSyncResult, "Expected Synced but got: $lastSyncResult")
    }

    @And("the library source file contains {string}")
    fun librarySourceFileContains(text: String) {
        assertTrue(text in libFixtureFile!!.readText(), "Expected '$text' in library source file")
    }

    @Then("the library project directory no longer exists")
    fun libraryProjectDirectoryGone() {
        assertFalse(libCacheDir.exists(), "Expected cache dir to be gone: ${libCacheDir.absolutePath}")
    }
}
