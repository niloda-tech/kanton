#!/usr/bin/env bash
exec ~/.kanton/bin/kanton-executor "kanton-delete-scaffold.kt.md" "$@"

# kanton-delete-scaffold - Remove a scaffolded project from the cache

```cli
kanton-delete-scaffold:Remove a scaffolded project from the cache
NAME, Name of the script (or path to .kt.md file)

val target = java.io.File(name)
val deleted = if (target.exists() && target.isFile) {
    kanton.core.cli.DeleteScaffold.run(target)
} else {
    kanton.core.cli.DeleteScaffold.run(name)
}
echo("Deleted: ${deleted.absolutePath}")
```

# dependencies
kanton:kanton-core:0.1.0-SNAPSHOT
  kanton.core.cli.DeleteScaffold
