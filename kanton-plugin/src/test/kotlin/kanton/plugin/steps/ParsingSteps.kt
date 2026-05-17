package kanton.plugin.steps

import kanton.plugin.lexer.KantonMdTokenTypes
import kanton.plugin.steps.PluginScenarioContext.CodeElement
import kanton.plugin.steps.PluginScenarioContext.Element
import kanton.plugin.steps.PluginScenarioContext.TextElement
import com.intellij.psi.tree.IElementType
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*

class ParsingSteps {

    private val ctx get() = PluginScenarioContext

    @When("the IDE builds the document structure")
    fun ideBuildsDocumentStructure() {
        ctx.elements = buildElements(ctx.tokenize(ctx.input))
    }

    private fun buildElements(toks: List<Pair<IElementType, String>>): List<Element> {
        val result = mutableListOf<Element>()
        var i = 0
        while (i < toks.size) {
            val (type, _) = toks[i]
            when (type) {
                KantonMdTokenTypes.TEXT -> {
                    result.add(TextElement)
                    i++
                }
                KantonMdTokenTypes.CODE_FENCE_START -> {
                    val lang = if (i + 1 < toks.size && toks[i + 1].first == KantonMdTokenTypes.CODE_FENCE_LANG)
                        toks[i + 1].second.trim()
                    else ""
                    var hasContent = false
                    var j = i + 2
                    while (j < toks.size && toks[j].first != KantonMdTokenTypes.CODE_FENCE_END) {
                        if (toks[j].first == KantonMdTokenTypes.CODE_CONTENT) hasContent = true
                        j++
                    }
                    result.add(CodeElement(lang, hasContent))
                    i = if (j < toks.size) j + 1 else j
                }
                else -> i++
            }
        }
        return result
    }

    private fun firstCodeElement(): CodeElement =
        ctx.elements.filterIsInstance<CodeElement>().first()

    @Then("the document contains no elements")
    fun documentHasNoElements() {
        assertTrue("Expected empty document", ctx.elements.isEmpty())
    }

    @Then("the document contains one element")
    fun documentHasOneElement() {
        assertEquals("Expected exactly one structural element", 1, ctx.elements.size)
    }

    @Then("the document contains two elements")
    fun documentHasTwoElements() {
        assertEquals("Expected exactly two structural elements", 2, ctx.elements.size)
    }

    @And("that element is a code block")
    fun thatElementIsCodeBlock() {
        assertTrue("Expected first element to be a code block", ctx.elements.first() is CodeElement)
    }

    @And("the code block is identified as a cli block")
    fun codeBlockIsCli() {
        assertEquals("Expected code block language to be cli", "cli", firstCodeElement().lang)
    }

    @And("the code block is identified as a lib block")
    fun codeBlockIsLib() {
        assertEquals("Expected code block language to be lib", "lib", firstCodeElement().lang)
    }

    @And("the code block is identified as a dependencies block")
    fun codeBlockIsDependencies() {
        assertEquals("Expected code block language to be dependencies", "dependencies", firstCodeElement().lang)
    }

    @And("the code block is identified as a deps block")
    fun codeBlockIsDeps() {
        assertEquals("Expected code block language to be deps", "deps", firstCodeElement().lang)
    }

    @And("the first element is a text region")
    fun firstElementIsText() {
        assertTrue("Expected first element to be a text region", ctx.elements[0] is TextElement)
    }

    @And("the second element is a code block")
    fun secondElementIsCodeBlock() {
        assertTrue("Expected second element to be a code block", ctx.elements[1] is CodeElement)
    }

    @And("the first element is a dependencies block")
    fun firstElementIsDependenciesBlock() {
        val elem = ctx.elements[0]
        assertTrue("Expected first element to be a code block", elem is CodeElement)
        assertEquals("Expected first block language to be dependencies", "dependencies", (elem as CodeElement).lang)
    }

    @And("the second element is a cli block")
    fun secondElementIsCliBlock() {
        val elem = ctx.elements[1]
        assertTrue("Expected second element to be a code block", elem is CodeElement)
        assertEquals("Expected second block language to be cli", "cli", (elem as CodeElement).lang)
    }

    @And("the first element is a deps block")
    fun firstElementIsDepsBlock() {
        val elem = ctx.elements[0]
        assertTrue("Expected first element to be a code block", elem is CodeElement)
        assertEquals("Expected first block language to be deps", "deps", (elem as CodeElement).lang)
    }

    @And("the second element is a lib block")
    fun secondElementIsLibBlock() {
        val elem = ctx.elements[1]
        assertTrue("Expected second element to be a code block", elem is CodeElement)
        assertEquals("Expected second block language to be lib", "lib", (elem as CodeElement).lang)
    }

    @Then("the cli block is eligible to host language assistance")
    fun cliBlockEligibleToHost() {
        val codeElements = ctx.elements.filterIsInstance<CodeElement>()
        assertFalse("Expected at least one code block eligible to host language features", codeElements.isEmpty())
    }

    @Then("the lib block is eligible to host language assistance")
    fun libBlockEligibleToHost() {
        val codeElements = ctx.elements.filterIsInstance<CodeElement>()
        assertFalse("Expected at least one code block eligible to host language features", codeElements.isEmpty())
    }
}
