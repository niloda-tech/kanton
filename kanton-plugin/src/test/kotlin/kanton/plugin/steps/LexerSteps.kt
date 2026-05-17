package kanton.plugin.steps

import kanton.plugin.lexer.KantonMdTokenTypes
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*

class LexerSteps {

    private val ctx get() = PluginScenarioContext

    @When("the editor parses the file")
    fun editorParsesTheFile() {
        ctx.lexException = null
        ctx.tokens = try {
            ctx.tokenize(ctx.input)
        } catch (e: Exception) {
            ctx.lexException = e
            emptyList()
        }
    }

    @Then("the entire content is treated as descriptive text")
    fun entireContentIsText() {
        assertTrue("Expected all tokens to be TEXT", ctx.tokens.all { it.first == KantonMdTokenTypes.TEXT })
    }

    @And("no code block is detected")
    fun noCodeBlockDetected() {
        assertFalse("Expected no CODE_FENCE_START token", ctx.tokens.any { it.first == KantonMdTokenTypes.CODE_FENCE_START })
    }

    @Then("a fenced code block is detected")
    fun fencedCodeBlockDetected() {
        assertNull("Expected no parse error", ctx.lexException)
        assertTrue("Expected at least one CODE_FENCE_START token", ctx.tokens.any { it.first == KantonMdTokenTypes.CODE_FENCE_START })
    }

    @Then("the block is identified as a cli block")
    fun blockIsCli() {
        val lang = ctx.tokens.first { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }.second.trim()
        assertEquals("cli", lang)
    }

    @Then("the fenced block is identified as a cli block")
    fun fencedBlockIsCli() = blockIsCli()

    @Then("the block is identified as a lib block")
    fun blockIsLib() {
        val lang = ctx.tokens.first { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }.second.trim()
        assertEquals("lib", lang)
    }

    @Then("the fenced block is identified as a lib block")
    fun fencedBlockIsLib() = blockIsLib()

    @Then("the block is identified as a kotlin block")
    fun blockIsKotlin() {
        val lang = ctx.tokens.first { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }.second.trim()
        assertEquals("kotlin", lang)
    }

    @Then("the block is identified as a dependencies block")
    fun blockIsDependencies() {
        val lang = ctx.tokens.first { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }.second.trim()
        assertEquals("dependencies", lang)
    }

    @Then("the block is identified as a deps block")
    fun blockIsDeps() {
        val lang = ctx.tokens.first { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }.second.trim()
        assertEquals("deps", lang)
    }

    @Then("the text regions before and after the block are treated as descriptive text")
    fun textRegionsPreserved() {
        val textTokens = ctx.tokens.filter { it.first == KantonMdTokenTypes.TEXT }
        assertTrue("Expected text tokens both before and after the code block", textTokens.size >= 2)
    }

    @Then("two code blocks are detected")
    fun twoCodeBlocksDetected() {
        val count = ctx.tokens.count { it.first == KantonMdTokenTypes.CODE_FENCE_START }
        assertEquals("Expected 2 CODE_FENCE_START tokens", 2, count)
    }

    @And("the first block is identified as a dependencies block")
    fun firstBlockIsDependencies() {
        val langTokens = ctx.tokens.filter { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }
        assertEquals("dependencies", langTokens[0].second.trim())
    }

    @And("the second block is identified as a cli block")
    fun secondBlockIsCli() {
        val langTokens = ctx.tokens.filter { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }
        assertEquals("cli", langTokens[1].second.trim())
    }

    @And("the first block is identified as a deps block")
    fun firstBlockIsDeps() {
        val langTokens = ctx.tokens.filter { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }
        assertEquals("deps", langTokens[0].second.trim())
    }

    @And("the second block is identified as a lib block")
    fun secondBlockIsLib() {
        val langTokens = ctx.tokens.filter { it.first == KantonMdTokenTypes.CODE_FENCE_LANG }
        assertEquals("lib", langTokens[1].second.trim())
    }

    @And("the block has no code content")
    fun blockHasNoCodeContent() {
        assertFalse("Expected no CODE_CONTENT token for empty block", ctx.tokens.any { it.first == KantonMdTokenTypes.CODE_CONTENT })
    }

    @Then("the open block is treated as having code content up to the end of the file")
    fun unclosedBlockHasCodeContent() {
        assertTrue("Expected CODE_CONTENT token for unclosed block", ctx.tokens.any { it.first == KantonMdTokenTypes.CODE_CONTENT })
        val content = ctx.tokens.first { it.first == KantonMdTokenTypes.CODE_CONTENT }.second
        assertTrue("Expected code content to be non-empty", content.isNotEmpty())
    }

    @And("no error prevents the file from being displayed")
    fun noErrorPreventingDisplay() {
        assertNull("Expected no exception during lexing", ctx.lexException)
    }
}
