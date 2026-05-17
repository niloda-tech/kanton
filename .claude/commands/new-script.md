Write a `.kt.md` script that does: $ARGUMENTS

Use the JSON spec workflow (`docs/CLI_MD_SKILL.md`) to produce the script deterministically.

## Steps

1. Design the script: decide name, help text, options/arguments, dependencies, and run block code.

2. Write the JSON spec to `/tmp/kanton-spec.json`:
```json
{
  "scriptName": "kebab-case-name",
  "shortDescription": "one-line summary",
  "helpText": "text for --help output",
  "proseDescription": "optional longer description",
  "arguments": ["NAME, Help text = \"default\""],
  "options": ["--flag, -f, Help text = \"default\""],
  "dependencies": [{"coord": "group:artifact:version", "imports": ["fq.Name"]}]
}
```

3. Write the run block to `/tmp/kanton-run.kt` — raw Kotlin for `override fun run()`. No class wrapper, no `main()`, no imports.

4. Run the formatter:
```bash
~/.kanton/bin/kanton-executor kanton-core/scripts/format-kanton.kt.md \
  --json /tmp/kanton-spec.json --run /tmp/kanton-run.kt \
  --output kanton-core/scripts/<name>.kt.md
```

5. Clean up temp files.
