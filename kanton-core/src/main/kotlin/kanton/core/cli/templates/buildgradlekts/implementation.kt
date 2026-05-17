package kanton.core.cli.templates.buildgradlekts

import kanton.core.shared.Template
import kanton.core.shared.bindAll
import kanton.core.shared.template

fun implementation(mavenCoords: List<String>): List<Template> =
    """implementation("{{coord}}")"""
        .template
        .bindAll("coord", mavenCoords)
