package kanton.core.facade

import kanton.core.lib.Publish
import kanton.core.lib.PublishResult
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PublishAcceptanceTest : LibFacadeBase() {

    @Test
    fun `run throws when file does not exist`() {
        val ex = assertThrows<IllegalArgumentException> {
            Publish.run(File("/tmp/no-such-lib-facade-test-99995.kt.md"))
        }
        assertTrue("not found" in ex.message!!.lowercase(), "message: ${ex.message}")
    }

    @Test
    fun `run returns Failed when source is not a lib file`() {
        val bogus = File.createTempFile("publish-facade-test", ".kt.md").also {
            it.writeText("not a valid lib source — no fence here")
        }
        try {
            val result = Publish.run(bogus)
            val failed = assertIs<PublishResult.Failed>(result, "expected Failed, got: $result")
            assertTrue(
                "parse" in failed.reason.lowercase() || "cannot" in failed.reason.lowercase(),
                "reason: ${failed.reason}"
            )
        } finally {
            bogus.delete()
        }
    }
}
