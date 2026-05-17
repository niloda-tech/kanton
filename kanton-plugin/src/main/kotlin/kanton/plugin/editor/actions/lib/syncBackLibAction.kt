package kanton.plugin.editor.actions.lib

import kanton.plugin.editor.actions.shared.ActionConfig
import kanton.plugin.editor.actions.shared.executeAction
import kanton.plugin.editor.actions.shared.notifyError
import kanton.plugin.editor.actions.shared.notifyInfo
import kanton.core.lib.SyncBackLib
import kanton.core.lib.SyncBackLibResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

fun syncBackLibAction(project: Project, file: VirtualFile) = executeAction(project, file, ActionConfig(
    title = "Syncing back library…",
    subcommand = "sync-back",
    indicatorTextWithBinary = { binary -> "Syncing back via binary: ${binary.name}…" },
    onBinarySuccess = { _, _, sourceFile, _, binary ->
        ApplicationManager.getApplication().invokeLater {
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(sourceFile.toPath())
            file.refresh(false, false)
            notifyInfo(project, "Library synced back using binary (${binary.name})")
        }
    },
    onFallback = { _, _, sourceFile, _, indicator ->
        indicator.text = "Syncing back via built-in…"
        when (val result = SyncBackLib.run(File(sourceFile.absolutePath))) {
            is SyncBackLibResult.Synced -> {
                val document = FileDocumentManager.getInstance().getDocument(file)
                if (document != null) {
                    val updated = result.file.readText()
                    ApplicationManager.getApplication().invokeLater {
                        WriteCommandAction.runWriteCommandAction(project) { document.setText(updated) }
                        notifyInfo(project, "Synced back ${result.artifact}")
                    }
                }
            }
            is SyncBackLibResult.NoChanges -> notifyInfo(project, "No changes to sync back")
            is SyncBackLibResult.Failed -> notifyError(project, "Sync back failed: ${result.reason}")
        }
    },
))
