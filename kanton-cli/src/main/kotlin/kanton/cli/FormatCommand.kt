package kanton.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kanton.core.cli.format.CliSpec
import kanton.core.cli.format.formatCliMd
import kanton.core.cli.models.ArgumentConfig
import kanton.core.cli.models.DepEntry
import kanton.core.cli.models.OptionConfig
import org.json.JSONObject
import java.io.File

class FormatCommand : CliktCommand(name = "format") {
    override fun help(context: Context) = "Format a .kt.md file from structured JSON input"

    private val json by option("-j", "--json", help = "Path to JSON spec file (reads stdin if omitted)").default("")
    private val runFile by option("-r", "--run", help = "Path to run-block file (uses runBody from JSON if omitted)").default("")
    private val output by option("-o", "--output", help = "Output file path (prints to stdout if omitted)").default("")

    override fun run() {
        val jsonText = if (json.isNotBlank()) {
            File(json).readText()
        } else {
            System.`in`.bufferedReader().readText()
        }

        val (spec, defaultRunBody) = parseJsonSpec(jsonText)

        val runBody = if (runFile.isNotBlank()) {
            File(runFile).readText().trimEnd()
        } else {
            defaultRunBody.trimEnd()
        }

        val result = formatCliMd(spec, runBody)

        if (output.isNotBlank()) {
            val outFile = File(output)
            outFile.parentFile?.mkdirs()
            outFile.writeText(result)
            echo("Created: ${outFile.absolutePath}")
        } else {
            echo(result.trimEnd())
        }
    }
}

private fun parseJsonSpec(jsonText: String): Pair<CliSpec, String> {
    val obj = JSONObject(jsonText)
    val name = obj.getString("scriptName")
    val shortDesc = obj.getString("shortDescription")
    val helpText = obj.optString("helpText", shortDesc)
    val proseDesc = obj.optString("proseDescription", "")
    val runBody = obj.optString("runBody", "")

    val arguments = obj.optJSONArray("arguments")?.let { arr ->
        (0 until arr.length()).map { i ->
            val line = arr.getString(i)
            val parts = line.split(",").map { it.trim() }
            ArgumentConfig(
                name = parts[0],
                help = parts.getOrElse(1) { "" },
                typeFunc = null,
                default = null
            )
        }
    } ?: emptyList()

    val options = obj.optJSONArray("options")?.let { arr ->
        (0 until arr.length()).map { i ->
            val line = arr.getString(i)
            val parts = line.split(",").map { it.trim() }
            val names = parts.takeWhile { it.startsWith("-") }
            val rest = parts.drop(names.size)
            OptionConfig(
                names = names,
                help = rest.firstOrNull() ?: "",
                typeFunc = null,
                default = rest.getOrNull(1)?.takeIf { it.contains("=") }?.substringAfter("=")?.trim()
            )
        }
    } ?: emptyList()

    val deps = obj.optJSONArray("dependencies")?.let { arr ->
        (0 until arr.length()).map { i ->
            val dep = arr.getJSONObject(i)
            val coord = dep.getString("coord")
            val imports = dep.optJSONArray("imports")?.let { imps ->
                (0 until imps.length()).map { j -> imps.getString(j) }
            } ?: emptyList()
            DepEntry(coord, imports)
        }
    } ?: emptyList()

    val spec = CliSpec(
        name = name,
        help = helpText,
        title = "$name - $shortDesc",
        description = proseDesc.takeIf { it.isNotBlank() },
        options = options,
        arguments = arguments,
        dependencies = deps,
    )
    return spec to runBody
}
