package kanton.plugin.editor.actions.cli

import kanton.plugin.editor.actions.shared.ActionConfig
import kanton.plugin.editor.actions.shared.executeAction
import kanton.plugin.editor.actions.shared.notifyError
import kanton.plugin.editor.actions.shared.notifyInfo
import kanton.core.cli.DeleteScaffold
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

fun deleteScaffoldAction(project: Project, file: VirtualFile) = executeAction(project, file, ActionConfig(
    title = "Deleting scaffold…",
    subcommand = "delete",
    indicatorTextWithBinary = { binary -> "Deleting scaffold via binary: ${binary.name}…" },
    onBinarySuccess = { _, _, _, _, binary ->
        ApplicationManager.getApplication().invokeLater {
            notifyInfo(project, "Scaffold deleted using binary (${binary.name})")
        }
    },
    onFallback = { _, _, sourceFile, _, indicator ->
        indicator.text = "Deleting scaffold…"
        try {
            val deleted = DeleteScaffold.run(sourceFile)
            notifyInfo(project, "Deleted scaffold: ${deleted.absolutePath}")
        } catch (e: Exception) {
            notifyError(project, e.message ?: "Delete scaffold failed")
        }
    },
))
