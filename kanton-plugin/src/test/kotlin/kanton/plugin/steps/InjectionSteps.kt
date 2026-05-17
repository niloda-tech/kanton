package kanton.plugin.steps

import kanton.plugin.injection.KantonMdMultiHostInjector
import kanton.plugin.injection.CliScriptContext
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*
import java.lang.reflect.Method

class InjectionSteps {

    private val ctx get() = PluginScenarioContext

    private val injector: KantonMdMultiHostInjector by lazy { KantonMdMultiHostInjector() }

    private val findRunSectionStartMethod: Method by lazy {
        KantonMdMultiHostInjector::class.java
            .getDeclaredMethod("findRunSectionStart", String::class.java)
            .also { it.isAccessible = true }
    }

    private fun invokeRunSectionStart(content: String): Int =
        findRunSectionStartMethod.invoke(injector, content) as Int

    @When("the IDE analyses the block for Kotlin assistance")
    fun ideAnalysesBlockForKotlinAssistance() {
        ctx.runSectionStart = invokeRunSectionStart(ctx.blockContent)
    }

    @When("the IDE prepares Kotlin assistance for the block")
    fun idePrepareKotlinAssistance() {
        // injectionLang was set in the Given step; assertions happen in Then steps
    }

    @When("the IDE prepares assistance for the block")
    fun idePrepareAssistance() {
        // injectionLang was set in the Given step; assertions happen in Then steps
    }

    @Then("the run body is identified as the region to assist with")
    fun runBodyIsIdentified() {
        assertTrue("Expected run section start > 0, got ${ctx.runSectionStart}", ctx.runSectionStart > 0)
    }

    @And("the region starts after the blank separator line")
    fun regionStartsAfterBlankLine() {
        assertTrue(
            "Expected runSectionStart within block, got ${ctx.runSectionStart} vs length ${ctx.blockContent.length}",
            ctx.runSectionStart in 1..ctx.blockContent.length
        )
        val precedingChar = ctx.blockContent[ctx.runSectionStart - 1]
        assertEquals("Expected blank separator line immediately before run body", '\n', precedingChar)
    }

    @Then("no run body is identified")
    fun noRunBodyIdentified() {
        assertEquals("Expected findRunSectionStart to return -1", -1, ctx.runSectionStart)
    }

    @Then("the run body is identified")
    fun runBodyIsIdentifiedSimple() {
        assertTrue("Expected run section start > 0, got ${ctx.runSectionStart}", ctx.runSectionStart > 0)
    }

    @And("the identified region stays within the block boundary")
    fun identifiedRegionWithinBoundary() {
        assertTrue(
            "Expected runSectionStart <= blockContent.length (${ctx.runSectionStart} <= ${ctx.blockContent.length})",
            ctx.runSectionStart <= ctx.blockContent.length
        )
    }

    @Then("Kotlin language features are active inside the block")
    fun kotlinFeaturesActive() {
        assertTrue(
            "Expected KOTLIN_PREFIX to be non-empty (Kotlin injection is configured)",
            CliScriptContext.KOTLIN_PREFIX.isNotBlank()
        )
    }

    @And("standard Clikt command APIs are available without explicit imports")
    fun cliktApisAvailableWithoutImports() {
        assertTrue(
            "Expected KOTLIN_PREFIX to contain CliktCommand import",
            "CliktCommand" in CliScriptContext.KOTLIN_PREFIX
        )
    }

    @Then("the dependencies block does not receive the Clikt command scaffold")
    fun dependenciesBlockHasNoCliktScaffold() {
        assertTrue(
            "Expected DEPENDENCIES_PREFIX to be empty (no Clikt scaffold for deps blocks)",
            CliScriptContext.DEPENDENCIES_PREFIX.isEmpty()
        )
        assertTrue(
            "Expected DEPENDENCIES_SUFFIX to be empty (no Clikt scaffold for deps blocks)",
            CliScriptContext.DEPENDENCIES_SUFFIX.isEmpty()
        )
    }
}
