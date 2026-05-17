package kanton.core.cli.templates

import kanton.core.cli.templates.buildgradlekts.pluginVersion
import kanton.core.shared.Template
import kanton.core.shared.bind
import kanton.core.shared.template

fun nativePlugin(graalNativeVersion: String): Template =
    nativePlugin.bind("graalNativeVersion" to pluginVersion(graalNativeVersion))

private val nativePlugin = """
id("org.graalvm.buildtools.native") {{graalNativeVersion}}
""".template
