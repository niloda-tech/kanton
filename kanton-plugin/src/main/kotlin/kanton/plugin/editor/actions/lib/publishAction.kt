package kanton.plugin.editor.actions.lib

import kanton.plugin.editor.actions.shared.ActionConfig
import kanton.plugin.editor.actions.shared.executeAction
import kanton.plugin.editor.actions.shared.notifyError
import kanton.plugin.editor.actions.shared.notifyInfo
import kanton.core.lib.Publish
import kanton.core.lib.PublishResult
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

fun publishAction(project: Project, file: VirtualFile) = executeAction(project, file, ActionConfig(
    title = "Publishing to mavenLocal…",
    subcommand = "publish",
    indicatorTextWithBinary = { binary -> "Publishing via binary: ${binary.name}…" },
    onBinarySuccess = { _, _, _, result, _ ->
        val published = result.stdout.lines().any { it.startsWith("Published= ") }
        if (!published) {
            notifyError(project, "kanton publish failed: no confirmation in output")
        } else {
            val artifact = result.stdout.lines()
                .lastOrNull { it.startsWith("Published= ") }
                ?.removePrefix("Published= ")
                ?.trim() ?: "library"
            notifyInfo(project, "Published $artifact to mavenLocal")
        }
    },
    onFallback = { _, _, sourceFile, _, indicator ->
        indicator.text = "Publishing via built-in…"
        when (val outcome = Publish.run(File(sourceFile.absolutePath))) {
            is PublishResult.Published -> notifyInfo(project, "Published ${outcome.artifact} to mavenLocal")
            is PublishResult.Failed -> notifyError(project, "Publish failed: ${outcome.reason}")
        }
    },
))
