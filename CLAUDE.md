# kanton — Project Guide

## Script Format: `.kt.md`

The default format for CLI scripts in this repo is `.kt.md`. Do not write raw `.kt` or `.kts` files — use `.kt.md` instead. The compiler compiles `.kt.md` files directly into executable Kotlin programs.

Full grammar: `docs/FORMAT_SPEC.md`
JSON workflow: `docs/CLI_MD_SKILL.md`

### Minimal Example

```markdown
#!/usr/bin/env bash
exec ~/.kanton/bin/kanton-executor "greet.kt.md" "$@"

# greet - simple greeter

```cli
greet:Say hello to someone
--name, -n, Name to greet = "World"

echo("Hello, $name!")
```

# dependencies
com.github.ajalt.clikt:clikt:5.1.0
  kanton.Script
```

### Format Rules (critical subset)

1. **Execution preamble** — `#!/usr/bin/env bash` + `exec ~/.kanton/bin/kanton-executor "<name>.kt.md" "$@"` as the first two lines
2. **Single ````cli` fence** — one per file, contains everything the compiler needs
3. **First line** — `name:Help text` with no spaces around the colon
4. **Option lines** — `--long, -s, Help text = "default"` or `--long, -s, Help text : int() = 0`
5. **Blank line** — separates option lines from run code
6. **Run block** — raw Kotlin injected into `override fun run()`; no class wrapper, no `main()`
7. **`# dependencies`** — Markdown section after the fence; Maven coord on its own line, imports indented 2 spaces

### DSL Surface Area (what's in scope inside `run()`)

| Symbol | Notes |
|---|---|
| `echo(String)` | Print to stdout — from Clikt, always available |
| `option(...)` | Declare options — already declared in the header; don't redeclare in `run()` |
| `stdinText()` | Read all stdin — requires `kanton.stdinText` import |
| `runBlocking { }` | Run coroutines — requires `kotlinx.coroutines.runBlocking` import |

Do **not** write `import com.github.ajalt.clikt.core.CliktCommand` — use `kanton.Script` as the base class (it extends `CliktCommand`).

Do **not** write `@file:DependsOn(...)` in the source — list all deps in the `# dependencies` section.

### Build & Run

```bash
./gradlew :kanton-core:build          # Build everything + run tests
./gradlew :kanton-core:buildExecutor   # Build kanton-executor binary
./gradlew :kanton-core:bootstrap       # Compile all scripts into ~/.kanton/bin/
```

---

## Project Structure

| Path | Purpose |
|---|---|
| `kanton-core/` | Parser, compiler, scaffold, sync-back, compile pipeline |
| `kanton-core/scripts/` | `.kt.md` CLI scripts (kanton's own tools) |
| `docs/FORMAT_SPEC.md` | Full `.kt.md` grammar |
| `docs/CLI_MD_SKILL.md` | JSON spec workflow for programmatic script generation |

---

## Build System

Gradle single-module (expanding to multi-module for plugins). Key tasks:

```bash
./gradlew :kanton-core:build           # Build + test
./gradlew :kanton-core:test            # Run all tests
./gradlew :kanton-core:buildExecutor   # Build executor binary → ~/.kanton/bin/
./gradlew :kanton-core:bootstrap       # Compile all scripts → ~/.kanton/bin/
```

---

## Key Conventions

- All scripts use `.kt.md` extension with `` ```cli `` fence (CLI) or `` ```lib `` fence (libraries)
- The compiler derives the output filename automatically from the script name
- Class names are derived from script name: `my-script` → `class MyScript`
- Option variable names come from `--long-name`: `--repeat` → `val repeat`
- Base class import: `kanton.Script` (not `cli.CliScript`)
- Cache directory: `~/.kanton/cache/`
- Binary output: `~/.kanton/bin/`
- Use the JSON spec workflow (docs/CLI_MD_SKILL.md) for programmatic script generation
