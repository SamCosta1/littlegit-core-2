package org.littlegit.core.integration

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.model.GitError
import kotlin.test.Test
import kotlin.test.assertFailsWith

class PushTests: BaseIntegrationTest() {

    @Test
    fun testNoRemote() {
        TestCommandHelper(testFolder).init()

        val result = littleGit.repoModifier.push().result
        assertTrue("Result is error", result is GitResult.Error)
        assertTrue("Result error is no remote", (result as GitResult.Error).err is GitError.NoRemote)
    }

    @Test
    fun testInvalidArgument() {
        assertFailsWith(IllegalArgumentException::class) {
            TestCommandHelper(testFolder).init()

            littleGit.repoModifier.push(null, "", true)
        }
    }
}