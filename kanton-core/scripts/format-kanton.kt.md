#!/usr/bin/env bash
exec ~/.kanton/bin/kanton-executor "format-kanton.kt.md" "$@"

# format-kanton - deterministic .kt.md formatter from structured JSON

Reads a JSON spec (from `--json` file or stdin) and an optional run block
(from `--run` file) and assembles a correctly formatted `.kt.md` file.

If `--run` is omitted, the `runBody` field in the JSON is used.

JSON schema:

```json
{
  "scriptName": "my-script",
  "shortDescription": "one-line summary",
  "helpText": "text shown in --help",
  "proseDescription": "longer description paragraph",
  "arguments": ["NAME, Help text = \"default\""],
  "options": ["--flag, -f, Help text = \"default\""],
  "runBody": "echo(\"hello\")",
  "dependencies": [{"coord": "group:artifact:version", "imports": ["fq.Name"]}]
}
```

# actions
[Open exploded Kotlin ↗](kanton://scaffold)
[← Sync back](kanton://sync-back)
[Compile binary ⚡](kanton://compile)
[Delete scaffold](kanton://delete-scaffold)

```cli
format-kanton:Format a .kt.md file from structured JSON input
--json, -j, Path to JSON spec file (reads stdin if omitted) = ""
--run, -r, Path to run-block file (uses runBody from JSON if omitted) = ""
--output, -o, Output file path (prints to stdout if omitted) = ""

val jsonText = if (!json.isNullOrBlank()) {
    java.io.File(json!!).readText()
} else {
    stdinText()
}

val spec = JSONObject(jsonText)
val scriptName = spec.getString("scriptName")
val shortDesc = spec.getString("shortDescription")
val helpText = spec.optString("helpText", shortDesc)
val proseDesc = spec.optString("proseDescription", "")

val runBody = if (!run.isNullOrBlank()) {
    java.io.File(run!!).readText().trimEnd()
} else {
    spec.optString("runBody", "").trimEnd()
}

val argLines = spec.optJSONArray("arguments")?.let { arr ->
    (0 until arr.length()).map { arr.getString(it) }
} ?: emptyList()

val optLines = spec.optJSONArray("options")?.let { arr ->
    (0 until arr.length()).map { arr.getString(it) }
} ?: emptyList()

val fence = "`".repeat(3)

val headerLines = (argLines + optLines).joinToString("\n")
val cliFence = buildString {
    appendLine("${fence}cli")
    appendLine("$scriptName:$helpText")
    if (headerLines.isNotEmpty()) appendLine(headerLines)
    appendLine()
    appendLine(runBody)
    append(fence)
}

val depsSection = buildString {
    appendLine("com.github.ajalt.clikt:clikt:5.1.0")
    appendLine("  kanton.Script")
    if (argLines.isNotEmpty()) {
        appendLine("  com.github.ajalt.clikt.parameters.arguments.argument")
        appendLine("  com.github.ajalt.clikt.parameters.arguments.default")
    }
    if (optLines.isNotEmpty()) {
        appendLine("  com.github.ajalt.clikt.parameters.options.option")
        appendLine("  com.github.ajalt.clikt.parameters.options.default")
    }
    val aiDeps = spec.optJSONArray("dependencies")
    if (aiDeps != null) {
        for (i in 0 until aiDeps.length()) {
            val dep = aiDeps.getJSONObject(i)
            val coord = dep.getString("coord")
            if (coord.startsWith("com.github.ajalt.clikt:clikt")) continue
            appendLine(coord)
            val imports = dep.optJSONArray("imports")
            if (imports != null) {
                for (j in 0 until imports.length()) appendLine("  ${imports.getString(j)}")
            }
        }
    }
}.trimEnd()

val result = buildString {
    appendLine("#!/usr/bin/env bash")
    appendLine("exec ~/.kanton/bin/kanton-executor \"$scriptName.kt.md\" \"\$@\"")
    appendLine()
    appendLine("# $scriptName - $shortDesc")
    appendLine()
    if (proseDesc.isNotBlank()) {
        appendLine(proseDesc)
        appendLine()
    }
    appendLine("# actions")
    appendLine("[Open exploded Kotlin ↗](kanton://scaffold)")
    appendLine("[← Sync back](kanton://sync-back)")
    appendLine("[Compile binary ⚡](kanton://compile)")
    appendLine("[Delete scaffold](kanton://delete-scaffold)")
    appendLine()
    appendLine(cliFence)
    appendLine()
    appendLine("# dependencies")
    appendLine(depsSection)
}

if (!output.isNullOrBlank()) {
    val outFile = java.io.File(output!!)
    outFile.parentFile?.mkdirs()
    outFile.writeText(result)
    echo("Created: ${outFile.absolutePath}")
} else {
    echo(result.trimEnd())
}
```

# dependencies
com.github.ajalt.clikt:clikt:5.1.0
  kanton.Script
  kanton.stdinText
  com.github.ajalt.clikt.parameters.options.option
  com.github.ajalt.clikt.parameters.options.default
org.json:json:20240303
  org.json.JSONObject
  org.json.JSONArray
