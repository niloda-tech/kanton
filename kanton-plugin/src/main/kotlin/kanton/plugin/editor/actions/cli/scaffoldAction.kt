package kanton.plugin.editor.actions.cli

import kanton.plugin.editor.actions.shared.ActionConfig
import kanton.plugin.editor.actions.shared.executeAction
import kanton.plugin.editor.actions.shared.notifyError
import kanton.plugin.editor.actions.shared.notifyInfo
import kanton.core.cli.Scaffold
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

fun scaffoldAction(project: Project, file: VirtualFile) = executeAction(project, file, ActionConfig(
    title = "Scaffolding project…",
    subcommand = "scaffold",
    indicatorTextWithBinary = { binary -> "Scaffolding via binary: ${binary.name}…" },
    onBinarySuccess = { _, _, sourceFile, result, binary ->
        val path = result.stdout.lines()
            .lastOrNull { it.startsWith("Scaffolded: ") }
            ?.removePrefix("Scaffolded: ")
            ?.trim()
        if (path == null) {
            val stderrMsg = result.stderr.lines().firstOrNull { it.isNotBlank() }
            if (stderrMsg != null) {
                notifyError(project, stderrMsg)
                return@ActionConfig
            }
            try {
                val scaffoldResult = Scaffold.run(sourceFile)
                openOrFocusProject(project, scaffoldResult.projectDir, "built-in")
            } catch (e: Exception) {
                notifyError(project, "Scaffold failed: ${e.message}")
            }
            return@ActionConfig
        }
        val projectDir = File(path)
        val symlink = File(projectDir, sourceFile.name).toPath()
        if (!Files.exists(symlink, LinkOption.NOFOLLOW_LINKS)) {
            Files.createSymbolicLink(symlink, sourceFile.canonicalFile.toPath())
        }
        openOrFocusProject(project, projectDir, "binary (${binary.name})")
    },
    onFallback = { _, _, sourceFile, _, indicator ->
        indicator.text = "Scaffolding via built-in…"
        try {
            val result = Scaffold.run(sourceFile)
            openOrFocusProject(project, result.projectDir, "built-in")
        } catch (e: Exception) {
            notifyError(project, "Scaffold failed: ${e.message}")
        }
    },
))

private fun openOrFocusProject(project: Project, projectDir: File, method: String) {
    val existing = ProjectManager.getInstance().openProjects
        .firstOrNull { it.basePath?.let { p -> File(p).canonicalPath } == projectDir.canonicalPath }

    if (existing != null) {
        LocalFileSystem.getInstance().refreshAndFindFileByNioFile(projectDir.toPath())?.refresh(false, true)
        ApplicationManager.getApplication().invokeLater {
            WindowManager.getInstance().getFrame(existing)?.toFront()
        }
        return
    }

    ApplicationManager.getApplication().invokeLater {
        notifyInfo(project, "Scaffold opened using $method")
        @Suppress("UnstableApiUsage")
        ProjectManagerEx.getInstanceEx().openProject(
            Path.of(projectDir.absolutePath),
            OpenProjectTask { forceOpenInNewFrame = true }
        )
    }
}
