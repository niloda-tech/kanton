package kanton.plugin.parser

import kanton.plugin.lexer.KantonMdTokenTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class KantonMdParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        while (!builder.eof()) {
            when (builder.tokenType) {
                KantonMdTokenTypes.TEXT -> parseTextBlock(builder)
                KantonMdTokenTypes.CODE_FENCE_START -> parseCodeBlock(builder)
                else -> builder.advanceLexer()
            }
        }
        rootMarker.done(root)
        return builder.treeBuilt
    }

    private fun parseTextBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer()
        marker.done(KantonMdElementTypes.TEXT_BLOCK)
    }

    private fun parseCodeBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // CODE_FENCE_START
        if (builder.tokenType == KantonMdTokenTypes.CODE_FENCE_LANG) {
            builder.advanceLexer()
        }
        if (builder.tokenType == KantonMdTokenTypes.CODE_CONTENT) {
            builder.advanceLexer()
        }
        if (builder.tokenType == KantonMdTokenTypes.CODE_FENCE_END) {
            builder.advanceLexer()
        }
        marker.done(KantonMdElementTypes.CODE_BLOCK)
    }
}
