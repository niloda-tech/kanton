package kanton.plugin.editor

import kanton.plugin.editor.actions.cli.compileBinaryAction
import kanton.plugin.editor.actions.cli.deleteScaffoldAction
import kanton.plugin.editor.actions.cli.scaffoldAction
import kanton.plugin.editor.actions.cli.syncBackAction
import kanton.plugin.editor.actions.lib.deleteScaffoldLibAction
import kanton.plugin.editor.actions.lib.publishAction
import kanton.plugin.editor.actions.lib.scaffoldLibAction
import kanton.plugin.editor.actions.lib.syncBackLibAction
import kanton.plugin.editor.actions.shared.notifyError
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

fun handleAction(action: String, project: Project, file: VirtualFile) {
    when (action) {
        "scaffold" -> scaffoldAction(project, file)
        "sync-back" -> syncBackAction(project, file)
        "compile" -> compileBinaryAction(project, file)
        "delete-scaffold" -> deleteScaffoldAction(project, file)
        "scaffold-lib" -> scaffoldLibAction(project, file)
        "sync-back-lib" -> syncBackLibAction(project, file)
        "publish" -> publishAction(project, file)
        "delete-scaffold-lib" -> deleteScaffoldLibAction(project, file)
        else -> notifyError(project, "Unknown kanton action: $action")
    }
}
