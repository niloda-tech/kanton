package kanton.core

import kanton.core.shared.repos.RepoRegistry
import kanton.core.shared.repos.ScriptRef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepoRegistryTest {

    @Test
    fun `parseRef splits namespace and script`() {
        val ref = RepoRegistry.parseRef("utils:greet")
        assertEquals(ScriptRef("utils", "greet"), ref)
    }

    @Test
    fun `parseRef returns null for bare name`() {
        assertNull(RepoRegistry.parseRef("greet"))
    }

    @Test
    fun `parseRef returns null for empty parts`() {
        assertNull(RepoRegistry.parseRef(":greet"))
        assertNull(RepoRegistry.parseRef("utils:"))
        assertNull(RepoRegistry.parseRef(":"))
    }

    @Test
    fun `parseRef handles script names with hyphens`() {
        val ref = RepoRegistry.parseRef("myorg:deploy-app")
        assertEquals(ScriptRef("myorg", "deploy-app"), ref)
    }

    @Test
    fun `ScriptRef toString formats as namespace colon script`() {
        assertEquals("utils:greet", ScriptRef("utils", "greet").toString())
    }
}
