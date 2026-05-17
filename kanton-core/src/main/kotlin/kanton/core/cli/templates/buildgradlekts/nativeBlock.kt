package kanton.core.cli.templates.buildgradlekts

import kanton.core.shared.Template
import kanton.core.shared.bind
import kanton.core.shared.template

fun nativeBlock(scriptName: String): Template =
    """
graalvmNative {
    binaries {
        named("main") {
            imageName.set("{{scriptName}}")
            mainClass.set("MainKt")
            buildArgs.add("--no-fallback")
            buildArgs.add("--initialize-at-build-time=kotlin,kotlinx")
            buildArgs.add("--initialize-at-run-time=kotlin.uuid.SecureRandomHolder")
        }
    }
}
""".template.bind("scriptName" to scriptName)
