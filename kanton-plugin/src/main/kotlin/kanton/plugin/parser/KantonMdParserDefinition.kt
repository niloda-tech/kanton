package kanton.plugin.parser

import kanton.plugin.KantonMdLanguage
import kanton.plugin.lexer.KantonMdLexer
import kanton.plugin.lexer.KantonMdTokenTypes
import kanton.plugin.psi.KantonMdCodeBlockElement
import kanton.plugin.psi.KantonMdFile
import kanton.plugin.psi.KantonMdTextElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class KantonMdParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = KantonMdLexer()
    override fun createParser(project: Project?): PsiParser = KantonMdParser()
    override fun getFileNodeType(): IFileElementType = KantonMdElementTypes.FILE
    override fun getCommentTokens(): TokenSet = TokenSet.EMPTY
    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            KantonMdElementTypes.CODE_BLOCK -> KantonMdCodeBlockElement(node)
            KantonMdElementTypes.TEXT_BLOCK -> KantonMdTextElement(node)
            else -> KantonMdTextElement(node)
        }
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile = KantonMdFile(viewProvider)
}
