package kanton.plugin

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object KantonMdFileType : LanguageFileType(KantonMdLanguage) {
    override fun getName(): String = "KantonMd"
    override fun getDescription(): String = "Kanton script or library (.kt.md)"
    override fun getDefaultExtension(): String = "kt.md"
    override fun getIcon(): Icon = KantonMdIcons.FILE
}
