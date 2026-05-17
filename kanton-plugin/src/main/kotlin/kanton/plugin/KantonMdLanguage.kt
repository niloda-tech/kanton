package kanton.plugin

import com.intellij.lang.Language

object KantonMdLanguage : Language("KantonMd") {
    private fun readResolve(): Any = KantonMdLanguage
}
