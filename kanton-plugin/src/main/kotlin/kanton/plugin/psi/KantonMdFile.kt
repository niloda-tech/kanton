package kanton.plugin.psi

import kanton.plugin.KantonMdFileType
import kanton.plugin.KantonMdLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class KantonMdFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, KantonMdLanguage) {
    override fun getFileType(): FileType = KantonMdFileType
}
