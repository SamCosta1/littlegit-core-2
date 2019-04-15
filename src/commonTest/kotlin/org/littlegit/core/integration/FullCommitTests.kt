package org.littlegit.core.integration


import org.littlegit.core.LittleGitCore
import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.AssertHelper
import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.model.FullCommit
import org.littlegit.core.util.CPDate
import kotlin.test.Test

import kotlin.test.assertNotNull

class FullCommitTests: BaseIntegrationTest() {

    @Test
    fun testValidCommit() {
        val commitMessage = "Message"
        val commandHelper = TestCommandHelper(testFolder)
                        .init()
                        .initConfig()
                        .writeToFile("file.txt", "Text")
                        .addAll()
                        .commit(commitMessage)

        val hash = commandHelper.getLastCommitHash()
        val timestamp = commandHelper.getLastCommitTimeStamp()

        val expectedCommit = FullCommit(
                hash,
                listOf("refs/heads/master"),
                emptyList(),
                CPDate.fromEpochMilis(timestamp.toLong() * 1000),
                TestCommandHelper.DEFAULT_EMAIL,
                commitMessage,
                true,
                commitBody = emptyList()
        )

        val result = littleGit.repoReader.getFullCommit(hash)
        assertTrue("Result was success", result.result is GitResult.Success)

        assertNotNull(result.data)
        AssertHelper.assertFullCommit(expectedCommit, result.data!!, ignoreDiff = true)
    }
}
