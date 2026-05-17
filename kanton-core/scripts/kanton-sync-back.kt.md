#!/usr/bin/env bash
exec ~/.kanton/bin/kanton-executor "kanton-sync-back.kt.md" "$@"

# kanton-sync-back - Sync changes from exploded project back to .kt.md source

```cli
kanton-sync-back:Sync changes from exploded project back to .kt.md source
FILE, Path to the .kt.md source file

val srcFile = java.io.File(file)
if (!srcFile.exists()) { echo("Error: File not found: ${srcFile.absolutePath}"); throw kotlin.system.exitProcess(1) }
val result = kanton.core.cli.SyncBack.run(srcFile)
when (result) {
    is kanton.core.cli.SyncBack.Result.NoChanges -> echo("No changes to sync.")
    is kanton.core.cli.SyncBack.Result.Synced -> echo("Synced: ${result.file.absolutePath}")
}
```

# dependencies
kanton:kanton-core:0.1.0-SNAPSHOT
  kanton.core.cli.SyncBack
