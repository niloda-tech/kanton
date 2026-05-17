package kanton.plugin.editor.actions.cli

import kanton.plugin.editor.actions.shared.ActionConfig
import kanton.plugin.editor.actions.shared.executeAction
import kanton.plugin.editor.actions.shared.notifyError
import kanton.plugin.editor.actions.shared.notifyInfo
import kanton.plugin.editor.actions.shared.parseScriptName
import kanton.core.cli.compile.Compile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

fun compileBinaryAction(project: Project, file: VirtualFile) = executeAction(project, file, ActionConfig(
    title = "Compiling binary…",
    subcommand = "compile",
    indicatorTextWithBinary = { binary -> "Compiling via binary: ${binary.name}…" },
    extractBinaryError = { result ->
        result.stderr.lines().firstOrNull { it.isNotBlank() && !it.startsWith("WARNING:") }
            ?: result.stderr.lines().firstOrNull { it.isNotBlank() }
            ?: result.stdout.lines().firstOrNull { it.isNotBlank() }
    },
    onBinarySuccess = { _, _, _, result, binary ->
        val outputPath = result.stdout.lines()
            .lastOrNull { it.startsWith("Binary: ") }
            ?.removePrefix("Binary: ")
            ?.trim()
        ApplicationManager.getApplication().invokeLater {
            file.parent?.refresh(false, false)
            if (outputPath != null) {
                LocalFileSystem.getInstance().refreshAndFindFileByNioFile(File(outputPath).toPath())
            }
            notifyInfo(project, "Binary compiled using ${binary.name}: ${outputPath ?: "(unknown path)"}")
        }
    },
    onFallback = { _, _, sourceFile, source, indicator ->
        val scriptName = parseScriptName(source) ?: return@ActionConfig
        indicator.text = "Compiling $scriptName binary…"
        val sourceDir = file.parent?.let { File(it.path) } ?: sourceFile.parentFile
        try {
            val outputFile = Compile.run(sourceFile, sourceDir)
            ApplicationManager.getApplication().invokeLater {
                LocalFileSystem.getInstance().refreshAndFindFileByNioFile(outputFile.toPath())
                file.parent?.refresh(false, false)
                notifyInfo(project, "Binary compiled: ${outputFile.absolutePath}")
            }
        } catch (e: Exception) {
            notifyError(project, "Compile failed: ${e.message}")
        }
    },
))
