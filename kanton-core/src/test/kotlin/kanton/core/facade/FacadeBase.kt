package kanton.core.facade

import java.io.File

abstract class FacadeBase {

    protected val fixtureSource = """
        # facade-test-fixture - acceptance test script

        ```cli
        facade-test-fixture:A minimal script for facade acceptance tests
        --name, -n, Name to greet = "World"

        echo("Hello, ${'$'}name!")
        ```

        # dependencies
        com.github.ajalt.clikt:clikt:5.1.0
          kanton.Script
    """.trimIndent()

    protected val fixtureName = "facade-test-fixture"
    protected val cacheDir get() = File(System.getProperty("user.home"), ".kanton/cache/$fixtureName")

    protected fun fixtureFile(): File =
        File.createTempFile("facade-test-fixture", ".kt.md").also { it.writeText(fixtureSource) }
}
