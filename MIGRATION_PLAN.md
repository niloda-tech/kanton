# Kanton Migration Plan

## What Is Kanton

**kanton** — a precision multi-tool for writing, compiling, installing, and managing Kotlin CLI scripts and libraries from a single markdown source format. Named after Swiss cantons (Swiss army knife energy).

Rebranded from `ktcli`. Fresh repo at `~/.kanton/repo`.

## Naming Conventions

| Concept | Old (ktcli) | New (kanton) |
|---------|-------------|--------------|
| File extension | `.ktcli.md` / `.ktlib.md` | `.kt.md` (unified) |
| CLI script fence | ` ```ktcli ` | ` ```cli ` |
| Library fence | ` ```ktlib ` | ` ```lib ` |
| Root package | `ktcli.core` | `kanton.core` |
| Cache dir | `~/.ktcli/cache/` | `~/.kanton/cache/` |
| Bin dir | `~/.ktcli/bin/` | `~/.kanton/bin/` |
| Repo dir | (was in ~/cursor/agents) | `~/.kanton/repo` |
| Module name | `ktcli-core` | `kanton-core` |
| CLI base class import | `cli.CliScript` | `kanton.Script` |

## Current State (completed)

- [x] Fresh git repo at `~/.kanton/repo`
- [x] Gradle 9.0.0 wrapper, Kotlin 2.3.0, JVM toolchain 17
- [x] Root build config (settings.gradle.kts, build.gradle.kts, gradle.properties)
- [x] `kanton-core` module with build.gradle.kts (clikt 5.1.0, cucumber, junit)
- [x] Empty source tree: `kanton/core/{shared,cli,lib}` with all sub-packages
- [x] Build passes (`./gradlew :kanton-core:build` → BUILD SUCCESSFUL)
- [x] Initial commit

## Migration Phases

### Phase 1: Core Parsing (migrate first — no external dependencies beyond clikt)

Source: `~/cursor/agents/ktcli-core/src/main/kotlin/ktcli/core/`
Target: `~/.kanton/repo/kanton-core/src/main/kotlin/kanton/core/`

1. **shared/models/Models.kt** → `kanton.core.shared.models`
   - `Section`, `ActionLink`, `scriptNameToClassName()`, `kebabToCamelCase()`
   - No changes beyond package rename

2. **shared/parsing/** → `kanton.core.shared.parsing`
   - `parseSections.kt` — multi-fence parser
   - `parseActions.kt` — action link parser
   - No changes beyond package rename

3. **cli/models/Models.kt** → `kanton.core.cli.models`
   - `DepEntry`, `OptionConfig`, `InjectionContext`
   - No changes beyond package rename

4. **cli/parsing/Parsing.kt** → `kanton.core.cli.parsing`
   - `parseKtcliMd` → rename to `parseCliMd` or `parseKtMd`
   - `isKtcliMdFormat` → `isCliFormat`
   - `parseDeps`, `parseOptionLine`, `buildTransform` — keep as-is
   - **Key change:** fence tag detection from `"ktcli"` → `"cli"`

5. **cli/parsing/parseScriptName.kt** → `kanton.core.cli.parsing`
   - Update to look for ` ```cli ` fence instead of ` ```ktcli `

6. **lib/models/LibModels.kt** → `kanton.core.lib.models`
   - `LibCoords`, `LibDepEntry`, `LibraryScaffoldResult`
   - No changes beyond package rename

7. **lib/parsing/parseKtlibMd.kt** → `kanton.core.lib.parsing`
   - `parseKtlibMd` → rename to `parseLibMd` or `parseKtMdLib`
   - `isKtlibMdFormat` → `isLibFormat`
   - **Key change:** fence tag detection from `"ktlib"` → `"lib"`
   - `parseLibCoords`, `parseLibDeps`, `parseLibDepLine` — keep as-is

8. **Migrate tests alongside each file:**
   - `ParsingTest` → update fixtures to use ` ```cli ` fence
   - `KtlibParsingTest` → update fixtures to use ` ```lib ` fence
   - Copy test resources, update fence tags

### Phase 2: Code Generation (scaffold/templates)

1. **shared/templates/settingsGradleKts.kt** → as-is
2. **cli/scaffold/buildExplodedKotlin.kt** — generates Main.kt from parsed source
3. **cli/templates/buildGradleKts.kt** — build.gradle.kts template for scaffolded projects
4. **cli/templates/CLI_STUBS.kt** — stdinText(), runScript() stubs
5. **lib/scaffold/buildExplodedLibraryKotlin.kt** — generates top-level Kotlin
6. **lib/templates/buildLibraryGradleKts.kt** — library build template

Tests: `ExplodedTest`, `KtlibExplodedTest`

### Phase 3: Facades (scaffold, compile, sync-back, delete)

1. **cli/Scaffold.kt** — update cache path to `~/.kanton/cache/`
2. **cli/Compile.kt** — update output paths
3. **cli/SyncBack.kt**
4. **cli/DeleteScaffold.kt**
5. **lib/ScaffoldLib.kt** — update cache path
6. **lib/Publish.kt**
7. **lib/SyncBackLib.kt**
8. **lib/DeleteScaffoldLib.kt**

Key constant change: `~/.ktcli/cache/` → `~/.kanton/cache/`

Tests: all `facade/*` acceptance tests

### Phase 4: Sync-back (most complex logic)

1. **cli/syncback/** — DepUpdater, extractRunBodyFromMainKt, replaceIn*Block, etc.
2. **cli/syncback/ContextBuilder.kt** — K2 injection context (IDE support)
3. **lib/syncback/ktlibSyncBack.kt**

Tests: `SyncbackTest`, `KtlibSyncbackTest`

### Phase 5: Compile Pipeline

1. **cli/compile/compileBinary.kt** — shadowJar + shell stub
2. **cli/compile/compileNative.kt** — GraalVM native binary
3. **cli/compile/Bootstrap.kt** — Gradle task that bootstraps bin/ binaries

Update bin output path: `~/.kanton/bin/`

Tests: `BinaryAcceptanceTest`

### Phase 6: IDE Plugins (future modules)

- `kanton-plugin` (was `ktcli-plugin`) — IntelliJ support for `.kt.md` CLI scripts
- `kanton-lib-plugin` (was `ktlib-plugin`) — IntelliJ support for `.kt.md` libraries
- These depend on kanton-core being fully migrated first

### Phase 7: Management CLI (in progress)

- [x] `kanton-cli` module with Clikt subcommand tree
- [x] Subcommands: scaffold, compile, install, sync-back, delete, test, format
- [x] `./gradlew :kanton-cli:installCli` → `~/.kanton/bin/kanton`
- [ ] Namespacing and script repositories
- [ ] Per-script testing support (test command scaffolds + runs `./gradlew test`)

## File Format Reference

### CLI Script (`.kt.md` with ` ```cli ` fence)

````markdown
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
````

### Library (`.kt.md` with ` ```lib ` fence)

````markdown
```lib
com.example:my-lib:1.0:Helpful greeter

fun greet(name: String) = "Hello, $name!"
```

# dependencies
api com.example:other:2.0
  com.example.Other
````

## Key Decisions (Resolved)

- **CLI base class name**: `kanton.Script` (package `kanton`, class `Script` extends `CliktCommand`)
- **Stub imports**: `kanton.Script`, `kanton.stdinText`, `kanton.runScript`
- **Executor binary name**: `kanton-executor` (standalone binary at `~/.kanton/bin/`)
- **Backward compatibility**: No — clean break, no `.ktcli.md` / `` ```ktcli `` support
- **Format spec doc**: Written at `docs/FORMAT_SPEC.md`

## Source Reference

The original code lives at `~/cursor/agents/ktcli-core/`. The `CLAUDE.md` in that module has detailed documentation of every file, function, and test. Use it as the authoritative reference during migration.
