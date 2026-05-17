package kanton.core.facade

import java.io.File

abstract class LibFacadeBase {

    protected val libFixtureSource = """
        # facade-lib-fixture - acceptance test library

        ```lib
        com.example:facade-lib-fixture:0.1.0:A minimal library for facade acceptance tests

        class FacadeLibFixture {
            fun greet(): String = "hello"
        }
        ```

        # dependencies
        api org.example:dep:1.0
          org.example.Bar
    """.trimIndent()

    protected val libFixtureArtifact = "facade-lib-fixture"
    protected val libFixtureClassName = "FacadeLibFixture"
    protected val libFixtureGroup = "com.example"

    protected val libCacheDir get() = File(System.getProperty("user.home"), ".kanton/cache/$libFixtureArtifact")
    protected val libFixtureKtPath get() = "src/main/kotlin/${libFixtureGroup.replace('.', '/')}/$libFixtureClassName.kt"

    protected fun libFixtureFile(): File =
        File.createTempFile("facade-lib-fixture", ".kt.md").also { it.writeText(libFixtureSource) }
}
