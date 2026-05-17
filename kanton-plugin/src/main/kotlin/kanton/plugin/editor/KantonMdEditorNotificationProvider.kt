package kanton.plugin.editor

import kanton.plugin.editor.actions.cli.compileBinaryAction
import kanton.plugin.editor.actions.cli.deleteScaffoldAction
import kanton.plugin.editor.actions.cli.scaffoldAction
import kanton.plugin.editor.actions.cli.syncBackAction
import kanton.plugin.editor.actions.lib.deleteScaffoldLibAction
import kanton.plugin.editor.actions.lib.publishAction
import kanton.plugin.editor.actions.lib.scaffoldLibAction
import kanton.plugin.editor.actions.lib.syncBackLibAction
import kanton.plugin.editor.actions.shared.readSource
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import kanton.core.shared.parsing.parseActions
import java.util.function.Function
import javax.swing.JComponent

class KantonMdEditorNotificationProvider : EditorNotificationProvider {

    private val cliFencePattern = Regex("^```cli\\s*$", RegexOption.MULTILINE)
    private val libFencePattern = Regex("^```lib\\s*$", RegexOption.MULTILINE)

    override fun collectNotificationData(
        project: Project,
        file: VirtualFile
    ): Function<in FileEditor, out JComponent?>? {
        if (!file.name.endsWith(".kt.md")) return null
        return Function { fileEditor ->
            val source = readSource(file)
            val actions = parseActions(source).filter { it.scheme == "kanton" }
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)

            val hasCli = cliFencePattern.containsMatchIn(source)
            val hasLib = libFencePattern.containsMatchIn(source)

            if (actions.isNotEmpty()) {
                panel.text = if (hasLib) "Kanton library" else "Kanton script"
                for (link in actions) {
                    panel.createActionLabel(link.label) {
                        handleAction(link.action, project, file)
                    }
                }
            } else if (hasLib) {
                panel.text = "Kanton library"
                panel.createActionLabel("Scaffold library ↗") { scaffoldLibAction(project, file) }
                panel.createActionLabel("← Sync back") { syncBackLibAction(project, file) }
                panel.createActionLabel("Publish to mavenLocal ⚡") { publishAction(project, file) }
                panel.createActionLabel("Delete scaffold") { deleteScaffoldLibAction(project, file) }
            } else if (hasCli) {
                panel.text = "Kanton script"
                panel.createActionLabel("Open exploded Kotlin ↗") { scaffoldAction(project, file) }
                panel.createActionLabel("← Sync back") { syncBackAction(project, file) }
                panel.createActionLabel("Compile binary ⚡") { compileBinaryAction(project, file) }
                panel.createActionLabel("Delete scaffold") { deleteScaffoldAction(project, file) }
            } else {
                return@Function null
            }

            panel
        }
    }
}
