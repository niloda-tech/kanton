package kanton.plugin.highlighting

import kanton.plugin.lexer.KantonMdLexer
import kanton.plugin.lexer.KantonMdTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class KantonMdSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val FENCE_MARKER = TextAttributesKey.createTextAttributesKey(
            "KANTON_FENCE_MARKER", DefaultLanguageHighlighterColors.KEYWORD
        )
        val FENCE_LANG = TextAttributesKey.createTextAttributesKey(
            "KANTON_FENCE_LANG", DefaultLanguageHighlighterColors.METADATA
        )
        val CODE_CONTENT = TextAttributesKey.createTextAttributesKey(
            "KANTON_CODE_CONTENT", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR
        )
        val TEXT = TextAttributesKey.createTextAttributesKey(
            "KANTON_TEXT", DefaultLanguageHighlighterColors.DOC_COMMENT
        )
    }

    override fun getHighlightingLexer(): Lexer = KantonMdLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return when (tokenType) {
            KantonMdTokenTypes.CODE_FENCE_START, KantonMdTokenTypes.CODE_FENCE_END -> arrayOf(FENCE_MARKER)
            KantonMdTokenTypes.CODE_FENCE_LANG -> arrayOf(FENCE_LANG)
            KantonMdTokenTypes.CODE_CONTENT -> arrayOf(CODE_CONTENT)
            KantonMdTokenTypes.TEXT -> arrayOf(TEXT)
            else -> emptyArray()
        }
    }
}
