package kanton.plugin.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class KantonMdLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null
    private var state = TEXT_STATE

    private var pendingType: IElementType? = null
    private var pendingStart = 0
    private var pendingEnd = 0

    companion object {
        const val TEXT_STATE = 0
        const val CODE_STATE = 1

        private val FENCE_OPEN = Regex("^```(\\w+)\\s*$")
        private val FENCE_CLOSE = Regex("^```\\s*$")
    }

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.state = initialState
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        this.tokenType = null
        this.pendingType = null
        advance()
    }

    override fun getState(): Int = state
    override fun getTokenType(): IElementType? = tokenType
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = endOffset

    override fun advance() {
        if (pendingType != null) {
            tokenType = pendingType
            tokenStart = pendingStart
            tokenEnd = pendingEnd
            pendingType = null
            return
        }

        if (tokenEnd >= endOffset) {
            tokenType = null
            return
        }

        tokenStart = tokenEnd

        when (state) {
            TEXT_STATE -> advanceText()
            CODE_STATE -> advanceCode()
        }
    }

    private fun advanceText() {
        var pos = tokenStart
        while (pos < endOffset) {
            val lineEnd = findLineEnd(pos)
            val line = buffer.subSequence(pos, lineEnd).toString()
            val match = FENCE_OPEN.matchEntire(line)
            if (match != null) {
                if (pos > tokenStart) {
                    tokenEnd = pos
                    tokenType = KantonMdTokenTypes.TEXT
                    return
                }
                val tickEnd = pos + 3
                tokenEnd = tickEnd
                tokenType = KantonMdTokenTypes.CODE_FENCE_START

                pendingType = KantonMdTokenTypes.CODE_FENCE_LANG
                pendingStart = tickEnd
                pendingEnd = consumeNewline(lineEnd)

                state = CODE_STATE
                return
            }
            pos = consumeNewline(lineEnd)
        }
        tokenEnd = endOffset
        tokenType = KantonMdTokenTypes.TEXT
    }

    private fun advanceCode() {
        var pos = tokenStart
        while (pos < endOffset) {
            val lineEnd = findLineEnd(pos)
            val line = buffer.subSequence(pos, lineEnd).toString()
            if (FENCE_CLOSE.matches(line)) {
                if (pos > tokenStart) {
                    tokenEnd = pos
                    tokenType = KantonMdTokenTypes.CODE_CONTENT
                    return
                }
                tokenEnd = consumeNewline(lineEnd)
                tokenType = KantonMdTokenTypes.CODE_FENCE_END
                state = TEXT_STATE
                return
            }
            pos = consumeNewline(lineEnd)
        }
        tokenEnd = endOffset
        tokenType = KantonMdTokenTypes.CODE_CONTENT
    }

    private fun findLineEnd(from: Int): Int {
        var i = from
        while (i < endOffset && buffer[i] != '\n' && buffer[i] != '\r') i++
        return i
    }

    private fun consumeNewline(lineEnd: Int): Int {
        var i = lineEnd
        if (i < endOffset && buffer[i] == '\r') i++
        if (i < endOffset && buffer[i] == '\n') i++
        return i
    }
}
