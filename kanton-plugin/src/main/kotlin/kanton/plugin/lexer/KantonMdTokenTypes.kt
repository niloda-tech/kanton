package kanton.plugin.lexer

import kanton.plugin.KantonMdLanguage
import com.intellij.psi.tree.IElementType

object KantonMdTokenTypes {
    @JvmField val TEXT = IElementType("TEXT", KantonMdLanguage)
    @JvmField val CODE_FENCE_START = IElementType("CODE_FENCE_START", KantonMdLanguage)
    @JvmField val CODE_FENCE_LANG = IElementType("CODE_FENCE_LANG", KantonMdLanguage)
    @JvmField val CODE_CONTENT = IElementType("CODE_CONTENT", KantonMdLanguage)
    @JvmField val CODE_FENCE_END = IElementType("CODE_FENCE_END", KantonMdLanguage)
}
