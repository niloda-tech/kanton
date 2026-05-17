package kanton.plugin.editor.actions.lib

import kanton.plugin.editor.actions.shared.ActionConfig
import kanton.plugin.editor.actions.shared.executeAction
import kanton.plugin.editor.actions.shared.notifyError
import kanton.plugin.editor.actions.shared.notifyInfo
import kanton.core.lib.DeleteScaffoldLib
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

fun deleteScaffoldLibAction(project: Project, file: VirtualFile) = executeAction(project, file, ActionConfig(
    title = "Deleting library scaffold…",
    subcommand = "delete",
    indicatorTextWithBinary = { binary -> "Deleting scaffold via binary: ${binary.name}…" },
    onBinarySuccess = { _, _, _, _, binary ->
        ApplicationManager.getApplication().invokeLater {
            notifyInfo(project, "Library scaffold deleted using binary (${binary.name})")
        }
    },
    onFallback = { _, _, sourceFile, _, indicator ->
        indicator.text = "Deleting library scaffold…"
        try {
            val deleted = DeleteScaffoldLib.run(File(sourceFile.absolutePath))
            notifyInfo(project, "Deleted scaffold: ${deleted.absolutePath}")
        } catch (e: IllegalArgumentException) {
            notifyError(project, e.message ?: "Delete scaffold failed")
        }
    },
))
