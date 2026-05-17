package kanton.plugin.steps

import kanton.plugin.injection.CliScriptContext
import kanton.plugin.injection.LibScriptContext
import kanton.core.lib.models.LibDepEntry
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*

class ScriptContextSteps {

    // ── CLI context ─────────────────────────────────────────────────────────

    private var kotlinPrefix: String = ""
    private var kotlinSuffix: String = ""
    private var depsPrefix: String = ""
    private var depsSuffix: String = ""
    private var runPrefix: String = ""
    private var depsContext: String = ""
    private var optionDeclarations: String = ""

    @When("the IDE prepares the injection context for a cli block")
    fun prepareInjectionContextForCli() {
        kotlinPrefix = CliScriptContext.KOTLIN_PREFIX
        kotlinSuffix = CliScriptContext.KOTLIN_SUFFIX
    }

    @When("the IDE prepares the injection context for a dependencies block")
    fun prepareInjectionContextForDependencies() {
        depsPrefix = CliScriptContext.DEPENDENCIES_PREFIX
        depsSuffix = CliScriptContext.DEPENDENCIES_SUFFIX
    }

    @Then("the injected code is wrapped in a CliktCommand class")
    fun injectedCodeWrappedInCliktCommand() {
        assertTrue(
            "Expected prefix to contain CliktCommand class wrapper",
            kotlinPrefix.contains("CliktCommand")
        )
        assertTrue(
            "Expected prefix to contain object declaration",
            kotlinPrefix.contains("object __script")
        )
    }

    @And("the standard Clikt imports are included")
    fun standardCliktImportsIncluded() {
        assertTrue(
            "Expected prefix to contain CliktCommand import",
            kotlinPrefix.contains("import com.github.ajalt.clikt.core.CliktCommand")
        )
        assertTrue(
            "Expected prefix to contain options import",
            kotlinPrefix.contains("import com.github.ajalt.clikt.parameters.options.*")
        )
    }

    @Then("the injected code has no prefix or suffix wrapping")
    fun injectedCodeHasNoWrapping() {
        assertTrue("Expected dependencies prefix to be empty", depsPrefix.isEmpty())
        assertTrue("Expected dependencies suffix to be empty", depsSuffix.isEmpty())
    }

    @Given("I have a run block with dependency functions")
    fun runBlockWithDependencyFunctions() {
        depsContext = "    private fun stdinText(): String = readlnOrNull() ?: \"\"\n"
        optionDeclarations = ""
    }

    @Given("I have a run block with option declarations")
    fun runBlockWithOptionDeclarations() {
        depsContext = ""
        optionDeclarations = "    private val name by option(\"--name\", help = \"Name\")\n"
    }

    @Given("I have a run block with no dependencies or options")
    fun runBlockWithNoDepsOrOptions() {
        depsContext = ""
        optionDeclarations = ""
    }

    @When("the IDE builds the run block prefix")
    fun ideBuildRunBlockPrefix() {
        runPrefix = CliScriptContext.buildRunPrefix(depsContext, optionDeclarations)
    }

    @Then("the prefix includes the dependency function delegates")
    fun prefixIncludesDependencyFunctions() {
        assertTrue(
            "Expected run prefix to contain dependency function delegates",
            runPrefix.contains("private fun stdinText()")
        )
    }

    @Then("the prefix includes the option declarations")
    fun prefixIncludesOptionDeclarations() {
        assertTrue(
            "Expected run prefix to contain option declarations",
            runPrefix.contains("private val name by option")
        )
    }

    @And("the prefix opens a run method")
    fun prefixOpensRunMethod() {
        assertTrue(
            "Expected run prefix to contain override fun run()",
            runPrefix.contains("override fun run()")
        )
    }

    @Then("the prefix opens a CliktCommand class")
    fun prefixOpensCliktCommandClass() {
        assertTrue(
            "Expected run prefix to contain CliktCommand",
            runPrefix.contains("CliktCommand")
        )
        assertTrue(
            "Expected run prefix to contain object __script",
            runPrefix.contains("object __script")
        )
    }

    // ── Lib context ─────────────────────────────────────────────────────────

    private var builtPrefix: String = ""
    private var deps: List<LibDepEntry> = emptyList()

    @Given("I have a lib block with dependencies declared")
    fun libBlockWithDependencies() {
        deps = listOf(
            LibDepEntry("com.example:lib:1.0", "api", emptyList()),
            LibDepEntry("org.other:util:2.0", "implementation", emptyList())
        )
    }

    @Given("I have a lib block with no dependencies")
    fun libBlockWithNoDependencies() {
        deps = emptyList()
    }

    @When("the IDE builds the dependency injection prefix")
    fun ideBuildsDependencyPrefix() {
        builtPrefix = LibScriptContext.buildDepsPrefix(deps)
    }

    @Then("the prefix includes DependsOn annotations for each dependency")
    fun prefixIncludesDependsOnAnnotations() {
        for (dep in deps) {
            assertTrue(
                "Expected prefix to contain @file:DependsOn for ${dep.coord}",
                builtPrefix.contains("""@file:DependsOn("${dep.coord}")""")
            )
        }
    }

    @Then("the prefix is empty")
    fun prefixIsEmpty() {
        assertTrue("Expected prefix to be empty", builtPrefix.isEmpty())
    }

    @And("each annotation is on its own line")
    fun eachAnnotationOnOwnLine() {
        val lines = builtPrefix.lines().filter { it.isNotBlank() }
        assertEquals(deps.size, lines.size)
    }
}
