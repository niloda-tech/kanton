package kanton.plugin.steps

import kanton.plugin.highlighting.KantonMdSyntaxHighlighter
import kanton.plugin.lexer.KantonMdTokenTypes
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*

class HighlightingSteps {

    private val ctx get() = PluginScenarioContext
    private val highlighter = KantonMdSyntaxHighlighter()

    @When("the editor applies syntax highlighting")
    fun editorAppliesSyntaxHighlighting() {
        ctx.tokens = ctx.tokenize(ctx.input)
    }

    @Then("the fence markers are highlighted as keywords")
    fun fenceMarkersHighlightedAsKeywords() {
        val fenceTokens = ctx.tokens.filter {
            it.first == KantonMdTokenTypes.CODE_FENCE_START || it.first == KantonMdTokenTypes.CODE_FENCE_END
        }
        assertTrue("Expected at least one fence marker token", fenceTokens.isNotEmpty())
        for (token in fenceTokens) {
            val keys = highlighter.getTokenHighlights(token.first)
            assertEquals(1, keys.size)
            assertEquals(KantonMdSyntaxHighlighter.FENCE_MARKER, keys[0])
        }
    }

    @Then("the language tag is highlighted as metadata")
    fun languageTagHighlightedAsMetadata() {
        val langTokens = ctx.tokens.filter { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }
        assertTrue("Expected at least one language tag token", langTokens.isNotEmpty())
        for (token in langTokens) {
            val keys = highlighter.getTokenHighlights(token.first)
            assertEquals(1, keys.size)
            assertEquals(KantonMdSyntaxHighlighter.FENCE_LANG, keys[0])
        }
    }

    @Then("the code content is highlighted as template language")
    fun codeContentHighlightedAsTemplateLang() {
        val contentTokens = ctx.tokens.filter { it.first == KantonMdTokenTypes.CODE_CONTENT }
        assertTrue("Expected at least one code content token", contentTokens.isNotEmpty())
        for (token in contentTokens) {
            val keys = highlighter.getTokenHighlights(token.first)
            assertEquals(1, keys.size)
            assertEquals(KantonMdSyntaxHighlighter.CODE_CONTENT, keys[0])
        }
    }

    @Then("the plain text is highlighted as documentation")
    fun plainTextHighlightedAsDocumentation() {
        val textTokens = ctx.tokens.filter { it.first == KantonMdTokenTypes.TEXT }
        assertTrue("Expected at least one text token", textTokens.isNotEmpty())
        for (token in textTokens) {
            val keys = highlighter.getTokenHighlights(token.first)
            assertEquals(1, keys.size)
            assertEquals(KantonMdSyntaxHighlighter.TEXT, keys[0])
        }
    }
}
