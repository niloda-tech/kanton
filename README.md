# kanton

Write CLI tools as Markdown. Kanton compiles `.kt.md` files — Markdown documents with a single fenced code block — into self-executing Kotlin CLI programs.

## Quick Example

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

Run it directly:

```bash
$ ./greet.kt.md --name Alice
Hello, Alice!
```

## How It Works

1. Write a `.kt.md` file with a `` ```cli `` fence containing your command definition and Kotlin code
2. The compiler scaffolds a full Gradle project, generates a Clikt command class, and builds a shadow JAR
3. The result is a self-executing binary — no JVM flags, no classpath wrangling

Options are declared as `--long, -short, Help text = "default"` and are automatically available as variables in the run block. Dependencies go in a `# dependencies` Markdown section with Maven coordinates.

## Building

Requires JDK 21+ and Gradle.

```bash
./gradlew :kanton-core:build          # Build + run tests
./gradlew :kanton-core:buildExecutor  # Build the executor binary → ~/.kanton/bin/
./gradlew :kanton-core:bootstrap      # Compile all scripts → ~/.kanton/bin/
./gradlew :kanton-cli:build           # Build the management CLI
./gradlew :kanton-cli:installCli      # Install kanton binary → ~/.kanton/bin/kanton
```

## Project Structure

| Path | Purpose |
|---|---|
| `kanton-core/` | Parser, compiler, scaffold, and compile pipeline |
| `kanton-cli/` | Management CLI (`kanton` command with subcommands) |
| `kanton-plugin/` | Gradle plugin |
| `kanton-core/scripts/` | Built-in `.kt.md` scripts |
| `docs/FORMAT_SPEC.md` | Full `.kt.md` format specification |

## License

See [LICENSE](LICENSE) for details.
