package kanton.plugin.injection

import kanton.plugin.psi.KantonMdCodeBlockElement
import kanton.plugin.psi.KantonMdTextElement
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import kanton.core.cli.parsing.buildTransform
import kanton.core.cli.parsing.parseOptionLine
import kanton.core.cli.syncback.buildInjectionContextFromMd
import kanton.core.cli.syncback.knownCliInlineBody
import kanton.core.cli.syncback.knownCliReturnType
import kanton.core.lib.parsing.parseLibMd
import kanton.core.lib.parsing.parseLibDeps
import org.jetbrains.kotlin.idea.KotlinLanguage.INSTANCE

class KantonMdMultiHostInjector : MultiHostInjector {

    private val LOG = Logger.getInstance(KantonMdMultiHostInjector::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context !is KantonMdCodeBlockElement) return
        val range = context.getCodeContentRange() ?: return
        val lang = context.getFenceLang() ?: return

        when (lang) {
            "cli" -> injectCli(registrar, context, range)
            "lib" -> injectLib(registrar, context)
            "run" -> injectRun(registrar, context, range)
            "kotlin" -> injectKotlin(registrar, context, range)
            "dependencies" -> injectDependencies(registrar, context, range)
            "imports" -> injectImports(registrar, context, range)
        }
    }

    // ── ```cli ───────────────────────────────────────────────────────────
    // Combined cli+run block: header lines above blank line, run code below.
    private fun injectCli(registrar: MultiHostRegistrar, context: KantonMdCodeBlockElement, range: TextRange) {
        val content = context.getCodeContent() ?: run {
            LOG.warn("KantonInjector[cli]: CODE_CONTENT token missing"); return
        }
        val contentRange = context.getCodeContentRange() ?: return
        val rawRunStart = findRunSectionStart(content)
        val runStart = rawRunStart.takeIf { it > 0 && it < content.length } ?: run {
            LOG.warn("KantonInjector[cli]: no blank-line separator found"); return
        }
        val runRange = TextRange(contentRange.startOffset + runStart, contentRange.endOffset)

        val ctx = buildInjectionContextFromMd(context.containingFile?.text ?: "")
        if (ctx != null) {
            registrar.startInjecting(INSTANCE)
            registrar.addPlace(ctx.prefix, ctx.suffix, context as PsiLanguageInjectionHost, runRange)
            registrar.doneInjecting()
            return
        }

        val cliLines = content.substring(0, runStart).lines().filter { it.isNotBlank() }
        val depsContext = buildDepsContextFromMarkdown(context)
        val optionDeclarations = buildOptionDeclarationsFromLines(cliLines.drop(1))
        registrar.startInjecting(INSTANCE)
        registrar.addPlace(
            CliScriptContext.buildRunPrefix(depsContext, optionDeclarations),
            CliScriptContext.RUN_SUFFIX,
            context as PsiLanguageInjectionHost,
            runRange
        )
        registrar.doneInjecting()
    }

    // ── ```lib ───────────────────────────────────────────────────────────
    // Library body: header line above blank line, Kotlin body below.
    private fun injectLib(registrar: MultiHostRegistrar, context: KantonMdCodeBlockElement) {
        val content = context.getCodeContent() ?: run {
            LOG.warn("KantonInjector[lib]: CODE_CONTENT token missing"); return
        }
        val contentRange = context.getCodeContentRange() ?: return

        val lines = content.lines()
        val blankIdx = lines.indexOfFirst { it.isBlank() }
        if (blankIdx < 0 || blankIdx >= lines.size - 1) {
            LOG.warn("KantonInjector[lib]: no blank separator between header and body"); return
        }

        val bodyStart = lines.take(blankIdx + 1).sumOf { it.length + 1 }
        val bodyRange = TextRange(contentRange.startOffset + bodyStart, contentRange.endOffset)

        val source = context.containingFile?.text ?: ""
        val sections = parseLibMd(source)
        val depsSection = sections.firstOrNull { it.tag == "deps" }
        val deps = depsSection?.let { parseLibDeps(it.lines) } ?: emptyList()
        val prefix = LibScriptContext.buildDepsPrefix(deps)

        registrar.startInjecting(INSTANCE)
        registrar.addPlace(prefix, "", context as PsiLanguageInjectionHost, bodyRange)
        registrar.doneInjecting()
    }

    // ── ```run ───────────────────────────────────────────────────────────
    private fun injectRun(registrar: MultiHostRegistrar, context: KantonMdCodeBlockElement, range: TextRange) {
        val depsBlock = findSiblingBlock(context, "deps")
        val cliBlock = findSiblingBlock(context, "cli")
        val depsContext = buildDepsContext(depsBlock)
        val optionDeclarations = buildOptionDeclarations(cliBlock)

        registrar.startInjecting(INSTANCE)
        registrar.addPlace(
            CliScriptContext.buildRunPrefix(depsContext, optionDeclarations),
            CliScriptContext.RUN_SUFFIX,
            context as PsiLanguageInjectionHost,
            range
        )
        registrar.doneInjecting()
    }

    // ── ```kotlin ────────────────────────────────────────────────────────
    private fun injectKotlin(registrar: MultiHostRegistrar, context: KantonMdCodeBlockElement, range: TextRange) {
        val importsBlock = findSiblingBlock(context, "imports")
        val dependenciesBlock = findSiblingBlock(context, "dependencies")
            ?: findSiblingBlock(context, "deps")
        val depAnnotations = buildDependencyAnnotations(dependenciesBlock)

        registrar.startInjecting(INSTANCE)

        val importsRange = importsBlock?.getCodeContentRange()
        if (importsBlock != null && importsRange != null) {
            registrar.addPlace(
                depAnnotations + "\n" + CliScriptContext.STANDARD_IMPORTS + "\n",
                "\n",
                importsBlock as PsiLanguageInjectionHost,
                importsRange
            )
            registrar.addPlace(
                CliScriptContext.CLIKT_CLASS_PREFIX,
                CliScriptContext.CLIKT_CLASS_SUFFIX,
                context as PsiLanguageInjectionHost,
                range
            )
        } else {
            val prefix = if (depAnnotations.isNotEmpty()) {
                depAnnotations + "\n" + CliScriptContext.KOTLIN_PREFIX
            } else {
                CliScriptContext.KOTLIN_PREFIX
            }
            registrar.addPlace(
                prefix,
                CliScriptContext.KOTLIN_SUFFIX,
                context as PsiLanguageInjectionHost,
                range
            )
        }

        registrar.doneInjecting()
    }

    // ── ```dependencies ──────────────────────────────────────────────────
    private fun injectDependencies(registrar: MultiHostRegistrar, context: KantonMdCodeBlockElement, range: TextRange) {
        registrar.startInjecting(INSTANCE)
        registrar.addPlace(
            CliScriptContext.DEPENDENCIES_PREFIX,
            CliScriptContext.DEPENDENCIES_SUFFIX,
            context as PsiLanguageInjectionHost,
            range
        )
        registrar.doneInjecting()
    }

    // ── ```imports ────────────────────────────────────────────────────────
    private fun injectImports(registrar: MultiHostRegistrar, context: KantonMdCodeBlockElement, range: TextRange) {
        val imports = buildImportStatements(context)
        registrar.startInjecting(INSTANCE)
        registrar.addPlace(
            imports,
            CliScriptContext.IMPORTS_SUFFIX,
            context as PsiLanguageInjectionHost,
            range
        )
        registrar.doneInjecting()
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private fun findRunSectionStart(content: String): Int {
        var offset = 0
        for (line in content.lines()) {
            val lineWithNewline = line.length + 1
            if (line.isBlank() && offset < content.length) return offset + lineWithNewline
            offset += lineWithNewline
        }
        return -1
    }

    private fun findSiblingBlock(context: KantonMdCodeBlockElement, lang: String): KantonMdCodeBlockElement? {
        return context.containingFile?.children
            ?.filterIsInstance<KantonMdCodeBlockElement>()
            ?.find { it.getFenceLang() == lang }
    }

    private fun buildDepsContext(depsBlock: KantonMdCodeBlockElement?): String {
        val content = depsBlock?.getCodeContent() ?: return ""
        val imports = mutableListOf<String>()
        for (line in content.lines()) {
            when {
                line.isBlank() -> Unit
                line.startsWith("  ") -> imports.add(line.trim())
            }
        }
        if (imports.isEmpty()) return ""
        return buildString {
            for (imp in imports) {
                val simpleName = imp.substringAfterLast('.')
                val returnType = knownCliReturnType(imp)
                val body = knownCliInlineBody(imp).ifEmpty { "$imp()" }
                appendLine("    private fun $simpleName()$returnType = $body")
            }
        }
    }

    private fun buildOptionDeclarations(cliBlock: KantonMdCodeBlockElement?): String {
        val content = cliBlock?.getCodeContent()?.trim() ?: return ""
        val lines = content.lines().drop(1)
        return buildOptionDeclarationsFromLines(lines)
    }

    private fun buildDepsContextFromMarkdown(context: KantonMdCodeBlockElement): String {
        val siblings = context.containingFile?.children
            ?.filterIsInstance<KantonMdTextElement>() ?: return ""
        val depsLines = siblings.flatMap { extractDepsSectionLines(it.text) }
        return buildDepsContextFromLines(depsLines)
    }

    private fun extractDepsSectionLines(text: String): List<String> {
        val lines = text.lines()
        val result = mutableListOf<String>()
        var inDeps = false
        for (line in lines) {
            when {
                line.matches(Regex("^# dependencies\\s*$", RegexOption.IGNORE_CASE)) -> inDeps = true
                inDeps && line.matches(Regex("^# .*")) -> return result
                inDeps -> result.add(line)
            }
        }
        return result
    }

    private fun buildDepsContextFromLines(lines: List<String>): String {
        val imports = mutableListOf<String>()
        for (line in lines) {
            when {
                line.isBlank() -> Unit
                line.startsWith("  ") -> imports.add(line.trim())
            }
        }
        if (imports.isEmpty()) return ""
        return buildString {
            for (imp in imports) {
                val simpleName = imp.substringAfterLast('.')
                val returnType = knownCliReturnType(imp)
                val body = knownCliInlineBody(imp).ifEmpty { "$imp()" }
                appendLine("    private fun $simpleName()$returnType = $body")
            }
        }
    }

    private fun buildOptionDeclarationsFromLines(lines: List<String>): String {
        return buildString {
            for (line in lines) {
                if (line.isBlank()) continue
                val opt = parseOptionLine(line)
                val longName = opt.names.firstOrNull { it.startsWith("--") } ?: continue
                val varName = longName.removePrefix("--")
                val namesStr = opt.names.joinToString(", ") { "\"$it\"" }
                val transform = buildTransform(opt.typeFunc, opt.default)
                val escapedHelp = opt.help.replace("\"", "\\\"")
                append("    private val $varName by option($namesStr, help = \"$escapedHelp\")")
                if (transform.isNotEmpty()) append(transform)
                appendLine()
            }
        }
    }

    private fun buildImportStatements(importsBlock: KantonMdCodeBlockElement?): String {
        val content = importsBlock?.getCodeContent()?.trim() ?: return ""
        if (content.isBlank()) return ""
        return buildString {
            content.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { line -> appendLine("import ${line.trim('"')}") }
        }
    }

    private fun buildDependencyAnnotations(dependenciesBlock: KantonMdCodeBlockElement?): String {
        val content = dependenciesBlock?.getCodeContent()?.trim() ?: return ""
        if (content.isBlank()) return ""
        return buildDepsContext(dependenciesBlock)
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return listOf(KantonMdCodeBlockElement::class.java)
    }
}
