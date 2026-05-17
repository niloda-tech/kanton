package kanton.plugin.steps

import kanton.plugin.editor.actions.shared.parseScriptName
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*

class ScriptNameSteps {

    private val ctx get() = PluginScenarioContext
    private var extractedName: String? = null

    @Given("I have a .kt.md file with a cli header line {string}")
    fun fileWithCliHeaderLine(headerLine: String) {
        ctx.input = "```cli\n$headerLine\n--opt, -o, Option\n```\n"
    }

    @Given("I have a .kt.md file with no cli block")
    fun fileWithNoCliBlock() {
        ctx.input = "Just some plain text.\nNo code blocks here.\n"
    }

    @When("the IDE extracts the script name")
    fun ideExtractsScriptName() {
        extractedName = parseScriptName(ctx.input)
    }

    @Then("the script name is {string}")
    fun scriptNameIs(expected: String) {
        assertEquals(expected, extractedName)
    }

    @Then("no script name is found")
    fun noScriptNameFound() {
        assertNull("Expected no script name but got: $extractedName", extractedName)
    }
}
