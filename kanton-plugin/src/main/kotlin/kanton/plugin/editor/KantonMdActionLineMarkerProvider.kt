package kanton.plugin.editor

import kanton.plugin.lexer.KantonMdTokenTypes
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class KantonMdActionLineMarkerProvider : LineMarkerProvider {

    private val actionSectionRegex = Regex("^# actions\\s*$", RegexOption.IGNORE_CASE)
    private val headingRegex = Regex("^# .*")
    private val linkRegex = Regex("""\[([^\]]+)\]\(kanton://([^)]+)\)""")

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(
        elements: List<PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        for (element in elements) {
            if (element.node.elementType != KantonMdTokenTypes.TEXT) continue
            val vFile = element.containingFile?.virtualFile ?: continue
            val project = element.project
            val text = element.text
            val elementStart = element.textRange.startOffset

            var inActions = false
            var pos = 0

            while (pos < text.length) {
                val nlIdx = text.indexOf('\n', pos)
                val lineEnd = if (nlIdx == -1) text.length else nlIdx
                val line = text.substring(pos, lineEnd).trimEnd('\r')

                when {
                    actionSectionRegex.matches(line) -> inActions = true
                    inActions && headingRegex.matches(line) -> inActions = false
                    inActions -> {
                        val match = linkRegex.find(line)
                        if (match != null) {
                            val label = match.groupValues[1]
                            val action = match.groupValues[2]
                            val absStart = elementStart + pos
                            val absEnd = (absStart + line.length).coerceAtLeast(absStart + 1)
                            val range = TextRange(absStart, absEnd)

                            result.add(LineMarkerInfo(
                                element,
                                range,
                                AllIcons.Actions.Execute,
                                { label },
                                { _, _ -> handleAction(action, project, vFile) },
                                GutterIconRenderer.Alignment.LEFT,
                                { label }
                            ))
                        }
                    }
                }

                if (nlIdx == -1) break
                pos = nlIdx + 1
            }
        }
    }
}
