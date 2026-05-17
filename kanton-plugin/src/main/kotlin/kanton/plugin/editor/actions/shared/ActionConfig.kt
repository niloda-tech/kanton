package kanton.plugin.editor.actions.shared

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

data class ActionConfig(
    val title: String,
    val subcommand: String,
    val indicatorTextWithBinary: (binary: File) -> String,
    val extractBinaryError: (result: BinaryResult) -> String? = { result ->
        result.stderr.lines().firstOrNull { it.isNotBlank() }
    },
    val onBinarySuccess: (project: Project, file: VirtualFile, sourceFile: File, result: BinaryResult, binary: File) -> Unit,
    val onFallback: (project: Project, file: VirtualFile, sourceFile: File, source: String, indicator: ProgressIndicator) -> Unit,
    val buildBinaryArgs: (sourceFile: File) -> List<String> = { listOf(it.absolutePath) },
)

fun executeAction(project: Project, file: VirtualFile, config: ActionConfig) {
    val source = readSource(file)
    val sourceFile = File(file.path)

    ProgressManager.getInstance().run(object : Task.Backgroundable(project, config.title, false) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = true

            val binary = findKantonBinary(sourceFile)
            if (binary != null) {
                indicator.text = config.indicatorTextWithBinary(binary)
                val args = listOf(config.subcommand) + config.buildBinaryArgs(sourceFile)
                val result = runBinary(binary, *args.toTypedArray())
                if (result.exitCode != 0) {
                    val msg = config.extractBinaryError(result) ?: "unknown error"
                    notifyError(project, "kanton ${config.subcommand} failed: $msg")
                    return
                }
                config.onBinarySuccess(project, file, sourceFile, result, binary)
                return
            }

            config.onFallback(project, file, sourceFile, source, indicator)
        }
    })
}
