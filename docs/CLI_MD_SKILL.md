# `.kt.md` Skill Reference — JSON Spec Workflow

Instead of writing `.kt.md` directly (error-prone for models), produce a **JSON spec** and let the `format-kanton` formatter assemble the file deterministically.

---

## JSON Schema

```json
{
  "scriptName": "my-script",
  "shortDescription": "one-line summary under 60 chars",
  "helpText": "text shown in --help output",
  "proseDescription": "longer description paragraph (optional)",
  "arguments": [
    "NAME, Help text = \"default\"",
    "COUNT, Number of items : int() = 1"
  ],
  "options": [
    "--flag, -f, Help text = \"default\"",
    "--count, -c, Repeat count : int() = 0"
  ],
  "runBody": "echo(\"Hello, $name!\")",
  "dependencies": [
    {"coord": "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1", "imports": ["kotlinx.coroutines.runBlocking"]}
  ]
}
```

**Field reference:**

| Field | Required | Description |
|---|---|---|
| `scriptName` | Yes | Kebab-case name (e.g. `word-count`). Used for filename, class name, and preamble. |
| `shortDescription` | Yes | One-line summary, used as `# title` suffix |
| `helpText` | Yes | Shown in `--help` output. Becomes the `name:help` header line. |
| `proseDescription` | No | Markdown prose paragraph below the title |
| `arguments` | No | Array of positional argument lines. Each string follows the format: `NAME, Help text = "default"`. Use `int()`/`double()` for typed args. |
| `options` | No | Array of option lines. Each string follows the format: `--long, -s, Help text = "default"`. Use `int()`/`double()` for typed options. |
| `runBody` | Yes | Raw Kotlin injected into `override fun run()`. No class wrapper, no `main()`, no imports. |
| `dependencies` | No | Array of `{coord, imports}` objects. **Do NOT include clikt** — it is auto-added with all required imports. |

---

## What the Formatter Handles Automatically

The model does NOT need to worry about:

- Execution preamble (`#!/usr/bin/env bash` + `exec` line)
- Actions section (always the standard 4 actions)
- Fence delimiters (`` ```cli `` / `` ``` ``)
- Colon spacing in `name:help` (assembled without spaces)
- Blank-line separator between options and run block
- `# dependencies` header
- 2-space indent on imports
- Base clikt imports (`kanton.Script`, `option`, `default`, `argument`)

---

## Invoking the Formatter

The formatter is a `.kt.md` script at `kanton-core/scripts/format-kanton.kt.md`.

**From JSON file + separate run block:**
```bash
~/.kanton/bin/kanton-executor kanton-core/scripts/format-kanton.kt.md \
  --json /tmp/spec.json --run /tmp/run.kt --output path/to/script.kt.md
```

**From JSON on stdin (with `runBody` field inline):**
```bash
cat /tmp/spec.json | ~/.kanton/bin/kanton-executor kanton-core/scripts/format-kanton.kt.md \
  --output path/to/script.kt.md
```

---

## Preferred Workflow for Claude Code

When generating a `.kt.md` script via slash command:

1. **Design** the script conceptually — decide name, options, arguments, deps, and code
2. **Write JSON spec** to a temp file (without `runBody`)
3. **Write run block** to a separate temp file (raw Kotlin, no escaping needed)
4. **Run the formatter** to produce the `.kt.md` file
5. **Clean up** temp files

This eliminates format errors (colon spacing, blank separators, import indentation) because the formatter handles all structural formatting deterministically.

---

## Dependency Rules

- **Do NOT include clikt** in the `dependencies` array — it is auto-added
- **stdinText**: Add `{"coord": "com.github.ajalt.clikt:clikt:5.1.0", "imports": ["kanton.stdinText"]}` only for the extra import
- **Coroutines**: `{"coord": "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1", "imports": ["kotlinx.coroutines.runBlocking"]}`
- **Serialization**: `{"coord": "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1", "imports": ["kotlinx.serialization.Serializable", "kotlinx.serialization.json.Json"]}`

---

## Example: JSON Spec for word-count

**spec.json:**
```json
{
  "scriptName": "word-count",
  "shortDescription": "count words and lines in stdin",
  "helpText": "Count words, lines, and characters from stdin",
  "proseDescription": "Reads piped text from stdin and reports word count, line count, and character count.",
  "options": [
    "--verbose, -v, Show per-line breakdown = \"false\""
  ],
  "dependencies": [
    {"coord": "com.github.ajalt.clikt:clikt:5.1.0", "imports": ["kanton.stdinText"]}
  ]
}
```

**run.kt:**
```kotlin
val input = stdinText()
val lines = input.lines().filter { it.isNotBlank() }
val words = input.split(Regex("\\s+")).filter { it.isNotBlank() }
val chars = input.length

if (verbose == "true") {
    lines.forEachIndexed { i, line ->
        echo("Line ${i + 1} (${line.split(Regex("\\s+")).filter { it.isNotBlank() }.size} words): $line")
    }
    echo("")
}
echo("Lines: ${lines.size}")
echo("Words: ${words.size}")
echo("Chars: $chars")
```

**Formatter command:**
```bash
~/.kanton/bin/kanton-executor kanton-core/scripts/format-kanton.kt.md \
  --json spec.json --run run.kt --output word-count.kt.md
```
