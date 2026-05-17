package kanton.plugin.steps

import kanton.plugin.editor.actions.shared.parseArtifactName
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*

class ArtifactNameSteps {

    private val ctx get() = PluginScenarioContext
    private var extractedName: String? = null

    @Given("I have a .kt.md file with a lib header line {string}")
    fun fileWithLibHeaderLine(headerLine: String) {
        ctx.input = "```lib\n$headerLine\n\nfun main() {}\n```\n"
    }

    @Given("I have a .kt.md file with no lib block")
    fun fileWithNoLibBlock() {
        ctx.input = "Just some plain text.\nNo code blocks here.\n"
    }

    @When("the IDE extracts the artifact name")
    fun ideExtractsArtifactName() {
        extractedName = parseArtifactName(ctx.input)
    }

    @Then("the artifact name is {string}")
    fun artifactNameIs(expected: String) {
        assertEquals(expected, extractedName)
    }

    @Then("no artifact name is found")
    fun noArtifactNameFound() {
        assertNull("Expected no artifact name but got: $extractedName", extractedName)
    }
}
