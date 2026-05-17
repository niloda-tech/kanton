Write a `.kt.md` script that implements an agent pipeline for: $ARGUMENTS

Use the JSON spec workflow (`docs/CLI_MD_SKILL.md`) to produce the script deterministically.

The script should follow the standard agent pipeline phases:
1. **Intent** — parse and validate user intent from CLI args/stdin
2. **Architecture** — break the task into steps (use structured prompts)
3. **Fuzzy gate** — evaluate confidence / applicability before acting
4. **Output** — produce final result, echoed or written to file

## Steps

1. Design the agent script: decide name, help text, options/arguments, dependencies, and pipeline code.

2. Write the JSON spec to `/tmp/kanton-spec.json`:
```json
{
  "scriptName": "agent-name",
  "shortDescription": "one-line summary of the agent",
  "helpText": "text for --help output",
  "proseDescription": "description of what the agent does and when to use it",
  "arguments": [],
  "options": [
    "--input, -i, Input text or file path = \"\"",
    "--verbose, -v, Enable verbose output = \"false\""
  ],
  "dependencies": [
    {"coord": "com.niloda:grok-caller:0.1.0", "imports": ["com.niloda.GrokCaller"]},
    {"coord": "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1", "imports": ["kotlinx.coroutines.runBlocking"]},
    {"coord": "com.github.ajalt.clikt:clikt:5.1.0", "imports": ["kanton.stdinText"]}
  ]
}
```

3. Write the run block to `/tmp/kanton-run.kt`:
```kotlin
val inputText = if (!input.isNullOrBlank()) input!! else stdinText()
if (inputText.isBlank()) { echo("Error: no input provided", err = true); return }

runBlocking {
    // Phase 1: Intent — validate input
    // Phase 2: Architecture — build prompt
    val grok = GrokCaller()
    // Phase 3: Fuzzy gate (add membership functions if needed)
    // Phase 4: Output
    val result = grok.call(inputText, systemPrompt = "You are a helpful assistant.")
    echo(result ?: "No response")
}
```

4. Run the formatter:
```bash
~/.kanton/bin/kanton-executor kanton-core/scripts/format-kanton.kt.md \
  --json /tmp/kanton-spec.json --run /tmp/kanton-run.kt \
  --output kanton-core/scripts/<name>.kt.md
```

5. Clean up temp files.

Fill in the script name, help text, and agent logic based on the request.
