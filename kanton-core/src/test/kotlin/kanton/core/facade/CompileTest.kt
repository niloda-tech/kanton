package kanton.core.facade

import kanton.core.cli.compile.Compile
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class CompileAcceptanceTest : FacadeBase() {

    @Test
    fun `run throws when file does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            Compile.run(File("/tmp/no-such-facade-test-99995.kt.md"))
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }
}
