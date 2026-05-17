#!/usr/bin/env bash
exec ~/.kanton/bin/kanton-executor "kanton-compile.kt.md" "$@"

# kanton-compile - Compile a .kt.md file into a self-executing binary

```cli
kanton-compile:Compile a .kt.md file into a self-executing binary
FILE, Path to the .kt.md source file
--output, -o, Output directory for the binary

val srcFile = java.io.File(file)
if (!srcFile.exists()) { echo("Error: File not found: ${srcFile.absolutePath}"); throw kotlin.system.exitProcess(1) }
val outDir = output?.let { java.io.File(it) } ?: java.io.File(System.getProperty("user.home"), ".kanton/bin")
outDir.mkdirs()
val result = kanton.core.cli.compile.Compile.run(srcFile, outDir)
echo("Binary: ${result.absolutePath}")
```

# dependencies
kanton:kanton-core:0.1.0-SNAPSHOT
  kanton.core.cli.compile.Compile
