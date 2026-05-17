package kanton.plugin.psi

import kanton.plugin.lexer.KantonMdTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

class KantonMdCodeBlockElement(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost {

    fun getFenceLang(): String? {
        val langNode = node.findChildByType(KantonMdTokenTypes.CODE_FENCE_LANG) ?: return null
        return langNode.text.trim()
    }

    fun getCodeContentRange(): TextRange? {
        val contentNode = node.findChildByType(KantonMdTokenTypes.CODE_CONTENT) ?: return null
        val startInParent = contentNode.startOffset - node.startOffset
        return TextRange(startInParent, startInParent + contentNode.textLength)
    }

    fun getCodeContent(): String? {
        return node.findChildByType(KantonMdTokenTypes.CODE_CONTENT)?.text
    }

    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost = this

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return LiteralTextEscaper.createSimple(this)
    }
}
