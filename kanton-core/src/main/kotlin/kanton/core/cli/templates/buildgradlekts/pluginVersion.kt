package kanton.core.cli.templates.buildgradlekts

fun pluginVersion(version: String) = if(version.isEmpty()) "" else """version "$version""""
