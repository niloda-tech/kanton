# `.kt.md` Format Specification

This is the canonical specification for `.kt.md` — the unified source format for CLI scripts and libraries in the kanton ecosystem. The kanton compiler transforms `.kt.md` files into executable Kotlin programs (CLI scripts) or publishable libraries.

---

## 1. File Extension & Conventions

- Source files use the `.kt.md` extension (e.g. `hello.kt.md`, `my-lib.kt.md`)
- A single file is either a **CLI script** (contains a `` ```cli `` fence) or a **library** (contains a `` ```lib `` fence)
- Output is a full JVM Gradle project scaffolded to `~/.kanton/cache/<name>/`
- CLI scripts compile to self-executing JVM binaries (shadow JAR)
- Libraries publish to Maven Local via `publishToMavenLocal`
- Source files are human-readable Markdown with one special fenced block

---

## 2. CLI Scripts

### 2.1 Execution Preamble

Begin every directly executable CLI `.kt.md` file with the following two lines:

```bash
#!/usr/bin/env bash
exec ~/.kanton/bin/kanton-executor "<name>.kt.md" "$@"
```

Replace `<name>.kt.md` with the filename of your script.

### 2.2 Top-Level Structure

```
#!/usr/bin/env bash
exec ~/.kanton/bin/kanton-executor "<name>.kt.md" "$@"

# Script Title

Optional prose description of what the script does.

# actions
[Label for action](kanton://action-name)

```cli
<cli header>
<blank line>
<run block>
```

# dependencies
<dep entries>
```

| Part | Required | Description |
|---|---|---|
| Execution preamble | Yes (for direct execution) | Shell stub that invokes the executor |
| `` ```cli `` fence | Yes | CLI definition + run body |
| `# actions` section | No | IDE gutter buttons (must appear before the fence) |
| `# dependencies` section | No | Maven coords + imports |
| Markdown prose | No | Human documentation only, ignored by compiler |

### 2.3 The `cli` Fence

The `` ```cli `` fence contains everything the compiler needs to generate a Kotlin class.

#### CLI Header

The **first line** of the fence defines the script name and help text:

```
name:Help text shown in --help output
```

- `name` — the command name (lowercase, hyphen-ok, no spaces)
- `:` — literal colon separator (no spaces around it)
- `Help text` — shown when the user runs `<script> --help`

#### Option Lines

Each subsequent line (before the blank separator) that **starts with `--`** defines one CLI option:

```
--long-name, -s, Help text = "default"
--long-name, -s, Help text : int() = 42
```

**Token order (comma-separated):**

1. One or more flag names — each must start with `-`
2. Help text — shown in `--help`
3. _(Optional)_ Type function — prefixed with ` : `, e.g. `: int()`, `: double()`, `: choice()`
4. _(Optional)_ Default value — prefixed with ` = `

**Examples:**

```
--name, -n, Name to greet = "World"
```
→ `private val name by option("--name", "-n", help = "Name to greet").default("World")`

```
--repeat, -r, Repeat count : int() = 1
```
→ `private val repeat by option("--repeat", "-r", help = "Repeat count").int().default(1)`

```
--verbose, -v, Enable verbose output
```
→ `private val verbose by option("--verbose", "-v", help = "Enable verbose output")`

#### Argument Lines (Positional Arguments)

Lines that **do not** start with `-` define positional arguments (Clikt `argument()`). They are passed by position, not by flag name.

```
NAME, Help text
NAME, Help text = "default"
NAME, Help text : int() = 1
```

- Argument name — conventionally UPPERCASE; used as the metavar in `--help` output
- Variable name — lowercased and camelCased: `FILE` → `file`, `OUTPUT-DIR` → `outputDir`

**Examples:**

```
FILE, The file to process
```
→ `private val file by argument("FILE", help = "The file to process")`

```
COUNT, Number of items : int() = 1
```
→ `private val count by argument("COUNT", help = "Number of items").int().default(1)`

Options and arguments can be interleaved in any order in the header.

#### Blank Line Separator

A single blank line separates the CLI header from the run body. This is required if a run body is present.

#### Run Block

Everything after the blank separator is injected verbatim into `override fun run()`:

```kotlin
override fun run() {
    // your code here
}
```

Indentation from the source is preserved.

---

### 2.4 The `# dependencies` Section

Appears after the closing `` ``` `` of the cli fence. It is a Markdown heading (matched case-insensitively) containing one or more dependency entries.

#### Format

```
maven.group:artifact-id:version
  fully.qualified.ClassName
  another.fully.qualified.Name
```

- Dependency coordinate: bare line, no leading spaces
- Imports: each indented with **exactly 2 spaces**
- Blank lines are ignored
- A new non-indented line starts the next dependency entry

#### What gets generated

For each entry:
- `implementation("maven.group:artifact-id:version")` in `build.gradle.kts`
- `import fully.qualified.ClassName` in the generated `Main.kt`

#### Special Imports

| Import | Purpose |
|---|---|
| `kanton.Script` | Marker: identifies this as a kanton CLI script. Filtered from generated imports. |
| `kanton.stdinText` | Provides `stdinText()` function in the scaffolded project |
| `kanton.runScript` | Provides `runScript()` function in the scaffolded project |

These are provided by `kanton/Stubs.kt` in the scaffolded project, not by a Maven artifact.

#### Example

```
com.github.ajalt.clikt:clikt:5.1.0
  kanton.Script
  com.github.ajalt.clikt.parameters.options.option
  com.github.ajalt.clikt.parameters.options.default
org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1
  kotlinx.coroutines.runBlocking
```

---

### 2.5 The `# actions` Section

Appears **before** the `` ```cli `` fence. It is a Markdown heading containing Markdown links using the `kanton://` scheme.

#### Format

```markdown
# actions
[Label](kanton://action-name)
```

- Each line is a standard Markdown link: `[Human-readable label](kanton://action-name)`
- The scheme is always `kanton://`; the path is the action identifier
- Not processed by the compiler — read by the IDE plugin for gutter icons

#### Built-in Actions

| Action | URL | What it does |
|---|---|---|
| Scaffold | `kanton://scaffold` | Explodes the script to a Gradle project at `~/.kanton/cache/<name>/` |
| Sync back | `kanton://sync-back` | Reads edits from the scaffold back into the `.kt.md` |
| Compile | `kanton://compile` | Compiles the scaffold to an executable binary |
| Delete scaffold | `kanton://delete-scaffold` | Deletes `~/.kanton/cache/<name>/` |

---

### 2.6 Lexical Scope

#### Available Without Import

| Symbol | From | Description |
|---|---|---|
| `echo(String)` | Clikt | Print to stdout — always available in `run()` |

#### Requires Import (under clikt dependency)

| Symbol | Import |
|---|---|
| `kanton.Script` | `kanton.Script` |
| `stdinText()` | `kanton.stdinText` |
| `option(...)` | `com.github.ajalt.clikt.parameters.options.option` |
| `.default(value)` | `com.github.ajalt.clikt.parameters.options.default` |
| `.int()` | `com.github.ajalt.clikt.parameters.types.int` |
| `.double()` | `com.github.ajalt.clikt.parameters.types.double` |
| `.choice(...)` | `com.github.ajalt.clikt.parameters.types.choice` |
| `argument(...)` | `com.github.ajalt.clikt.parameters.arguments.argument` |

#### Do NOT Write

- `import com.github.ajalt.clikt.core.CliktCommand` — use `kanton.Script`
- `fun main(args: Array<String>)` — the runner provides this
- `@file:DependsOn(...)` — list deps in `# dependencies` only
- A class declaration or `override fun run()` — generated by the compiler

---

### 2.7 Compilation Pipeline

```
.kt.md
    │
    ▼
kanton-core
    │  parseCliMd()     — extracts cli, run, deps sections
    │  parseOptionLine() — parses each option into OptionConfig
    │  parseDeps()       — parses dependency coords + imports
    │  buildTransform()  — composes .int().default(...) chains
    ▼
Gradle project scaffold
    │  settings.gradle.kts
    │  build.gradle.kts
    │  src/main/kotlin/Main.kt
    │  src/main/kotlin/kanton/Stubs.kt
    ▼
Executable binary  (via shadowJar)
```

#### Scaffold Output

```
~/.kanton/cache/<name>/
    settings.gradle.kts
    build.gradle.kts
    src/main/kotlin/
        Main.kt             ← Kotlin class + fun main()
        kanton/Stubs.kt     ← stdinText(), runScript()
```

- The exploded output is a plain `.kt` source file, not a `.kts` script
- The class extends `CliktCommand` directly
- Edits made in the IDE are synced back via `kanton://sync-back`

---

### 2.8 CLI Minimal Example

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
  com.github.ajalt.clikt.parameters.options.option
  com.github.ajalt.clikt.parameters.options.default
```

---

## 3. Libraries

### 3.1 Top-Level Structure

```
```lib
group:artifact:version:Help text

<kotlin body>
```

# dependencies
<dep entries>
```

| Part | Required | Description |
|---|---|---|
| `` ```lib `` fence | Yes | Maven coordinates + Kotlin source body |
| `# dependencies` section | No | Dependencies with Gradle config prefixes |
| Markdown prose | No | Human documentation only |

### 3.2 The `lib` Fence

#### Coordinate Header

The **first line** of the fence defines the library's Maven coordinates:

```
group:artifact:version:Help text
```

- `group` — Maven group ID (e.g. `com.example`)
- `artifact` — Maven artifact ID (e.g. `my-lib`)
- `version` — semver version (e.g. `1.0.0`)
- `Help text` — optional description (everything after the third colon)

Colons in the help text are preserved (only the first three colons are structural).

#### Blank Line Separator

A blank line separates the coordinate header from the Kotlin body.

#### Kotlin Body

Everything after the blank line is top-level Kotlin source — no class wrapper, no `main()`:

```kotlin
fun greet(name: String) = "Hello, $name!"

data class Config(val host: String, val port: Int)
```

### 3.3 Library Dependencies

The `# dependencies` section for libraries supports Gradle configuration prefixes:

```
api org.example:lib:1.0
  org.example.Foo
implementation org.example:other:2.0
testImplementation org.example:test-lib:3.0
```

Supported configs: `api`, `implementation`, `compileOnly`, `runtimeOnly`, `testImplementation`, `testCompileOnly`. Default (no prefix) is `implementation`.

### 3.4 Library Compilation

```
.kt.md (lib)
    │
    ▼
kanton-core
    │  parseLibMd()     — extracts lib, body, deps sections
    │  parseLibCoords() — parses group:artifact:version:help
    │  parseLibDeps()   — parses deps with config prefixes
    ▼
Gradle library project
    │  settings.gradle.kts
    │  build.gradle.kts   ← java-library + maven-publish
    │  src/main/kotlin/<ClassName>.kt
    ▼
Published artifact  (via publishToMavenLocal)
```

#### Built-in Actions (Library)

| Action | URL | What it does |
|---|---|---|
| Scaffold | `kanton://scaffold` | Explodes to `~/.kanton/cache/<artifact>/` |
| Sync back | `kanton://sync-back` | Reads edits back into the `.kt.md` |
| Publish | `kanton://publish` | Runs `publishToMavenLocal` |
| Delete scaffold | `kanton://delete-scaffold` | Deletes `~/.kanton/cache/<artifact>/` |

### 3.5 Library Minimal Example

```markdown
```lib
com.example:greeter:1.0.0:A friendly greeting library

fun greet(name: String) = "Hello, $name!"

fun farewell(name: String) = "Goodbye, $name!"
```

# dependencies
api org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1
  kotlinx.coroutines.runBlocking
```

---

## 4. Naming Conventions

| Context | Rule | Example |
|---|---|---|
| Script name → class name | PascalCase from kebab-case | `my-script` → `MyScript` |
| Option `--long-name` → variable | camelCase from kebab-case | `--dry-run` → `dryRun` |
| Argument `NAME` → variable | lowercase + camelCase | `OUTPUT-DIR` → `outputDir` |
| Library artifact → class name | PascalCase from kebab-case | `my-lib` → `MyLib` |

---

## 5. Common Mistakes

| Mistake | Symptom | Fix |
|---|---|---|
| Missing blank line between header and run block | Run code treated as option lines | Add a blank line after the last option/argument |
| Space around `:` in the name:help line | Name parsed incorrectly | Use `name:help` with no spaces around the colon |
| Writing `fun main(...)` in the run block | Duplicate `main` at compile time | Remove it — the scaffold provides `main` |
| Using `@file:DependsOn` in source | Not recognized | List deps in `# dependencies` only |
| 4-space indent on imports | Import not recognized | Use exactly 2-space indent |
| Missing `kanton.Script` import | Compile error | Add it under the clikt dependency |
| Missing `option`/`default` imports | `Unresolved reference` at build | Add the clikt extension function imports |
| Starting an argument with `-` | Treated as an option | Argument names must not start with `-` |
| Using `` ```ktcli `` or `` ```ktlib `` fences | Not recognized | Use `` ```cli `` or `` ```lib `` |

---

## 6. Paths & Directories

| Path | Purpose |
|---|---|
| `~/.kanton/bin/` | Compiled binaries and the executor |
| `~/.kanton/cache/<name>/` | Scaffolded Gradle projects (CLI scripts keyed by script name, libraries by artifact) |
| `~/.kanton/repo/` | The kanton source repository |
