#!/usr/bin/env bash
exec ~/.kanton/bin/kanton-executor "kanton-scaffold.kt.md" "$@"

# kanton-scaffold - Scaffold a Gradle project from a .kt.md file

```cli
kanton-scaffold:Scaffold a Gradle project from a .kt.md file
FILE, Path to the .kt.md source file
--native, -n, Enable GraalVM native-image support

val srcFile = java.io.File(file)
if (!srcFile.exists()) { echo("Error: File not found: ${srcFile.absolutePath}"); throw kotlin.system.exitProcess(1) }
val result = kanton.core.cli.Scaffold.run(srcFile)
echo("Scaffolded: ${result.projectDir.absolutePath}")
echo("Open in IDE: open ${result.projectDir.absolutePath}")
```

# dependencies
kanton:kanton-core:0.1.0-SNAPSHOT
  kanton.core.cli.Scaffold
