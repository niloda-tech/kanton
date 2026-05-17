package kanton.plugin.editor.actions.shared

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile

fun readSource(file: VirtualFile): String =
    FileDocumentManager.getInstance().getDocument(file)?.text
        ?: String(file.contentsToByteArray(), file.charset)
