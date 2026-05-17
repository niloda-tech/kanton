package kanton.core.cli.scaffold

import kanton.core.shared.Template
import kanton.core.shared.bind
import kanton.core.shared.template

fun testTemplate(className: String, scriptName: String): Template =
    TEST_TEMPLATE.bind(
        "className" to className,
        "scriptName" to scriptName,
    )

private val TEST_TEMPLATE = """
import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals

class {{className}}Test {
    @Test
    fun `runs with defaults`() {
        val result = {{className}}().test()
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `shows help`() {
        val result = {{className}}().test("--help")
        assertEquals(0, result.statusCode)
        assert(result.output.contains("{{scriptName}}"))
    }
}
""".trimIndent().template
