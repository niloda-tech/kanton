package kanton.plugin.editor.actions.cli

import kanton.plugin.editor.actions.shared.ActionConfig
import kanton.plugin.editor.actions.shared.executeAction
import kanton.plugin.editor.actions.shared.notifyInfo
import kanton.core.cli.SyncBack
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

fun syncBackAction(project: Project, file: VirtualFile) = executeAction(project, file, ActionConfig(
    title = "Syncing back…",
    subcommand = "sync-back",
    indicatorTextWithBinary = { binary -> "Syncing back via binary: ${binary.name}…" },
    onBinarySuccess = { _, _, sourceFile, _, binary ->
        ApplicationManager.getApplication().invokeLater {
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(sourceFile.toPath())
            file.refresh(false, false)
            notifyInfo(project, "Synced back using binary (${binary.name})")
        }
    },
    onFallback = { _, _, sourceFile, _, indicator ->
        indicator.text = "Syncing back via built-in…"
        when (val result = SyncBack.run(sourceFile)) {
            is SyncBack.Result.NoChanges -> notifyInfo(project, "No changes to sync back")
            is SyncBack.Result.Synced -> {
                ApplicationManager.getApplication().invokeLater {
                    val document = FileDocumentManager.getInstance().getDocument(file)
                    if (document != null) {
                        WriteCommandAction.runWriteCommandAction(project) {
                            document.setText(result.file.readText())
                        }
                    }
                    notifyInfo(project, "Synced back: ${result.file.name}")
                }
            }
        }
    },
))
