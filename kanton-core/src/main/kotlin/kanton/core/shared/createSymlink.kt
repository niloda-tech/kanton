package kanton.core.shared

import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption

fun createSymlink(sourceFile: File?, projectDir: File) {
    if (sourceFile != null && sourceFile.exists()) {
        val symlink = File(projectDir, sourceFile.name).toPath()
        if (!Files.exists(symlink, LinkOption.NOFOLLOW_LINKS)) {
            Files.createSymbolicLink(symlink, sourceFile.canonicalFile.toPath())
        }
    }
}
