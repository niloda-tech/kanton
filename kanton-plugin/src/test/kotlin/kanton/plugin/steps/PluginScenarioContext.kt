package kanton.plugin.steps

import kanton.plugin.editor.actions.shared.BinaryResult
import kanton.plugin.lexer.KantonMdLexer
import com.intellij.psi.tree.IElementType
import java.io.File

object PluginScenarioContext {

    sealed class Element
    object TextElement : Element()
    data class CodeElement(val lang: String, val hasContent: Boolean) : Element()

    var input: String = ""
    var blockContent: String = ""
    var injectionLang: String? = null
    var tokens: List<Pair<IElementType, String>> = emptyList()
    var lexException: Exception? = null
    var elements: List<Element> = emptyList()
    var runSectionStart: Int = -2
    var binaryResult: BinaryResult? = null
    var discoveredBinary: File? = null

    fun reset() {
        input = ""
        blockContent = ""
        injectionLang = null
        tokens = emptyList()
        lexException = null
        elements = emptyList()
        runSectionStart = -2
        binaryResult = null
        discoveredBinary = null
    }

    fun tokenize(src: String): List<Pair<IElementType, String>> {
        val lexer = KantonMdLexer()
        lexer.start(src)
        val result = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            result.add(lexer.tokenType!! to lexer.bufferSequence.subSequence(lexer.tokenStart, lexer.tokenEnd).toString())
            lexer.advance()
        }
        return result
    }
}
