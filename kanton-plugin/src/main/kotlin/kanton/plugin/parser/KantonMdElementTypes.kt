package kanton.plugin.parser

import kanton.plugin.KantonMdLanguage
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType

object KantonMdElementTypes {
    val FILE = IFileElementType(KantonMdLanguage)
    val TEXT_BLOCK = IElementType("TEXT_BLOCK", KantonMdLanguage)
    val CODE_BLOCK = IElementType("CODE_BLOCK", KantonMdLanguage)
}
