package org.littlegit.core.integration

import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertEquals
import org.littlegit.core.helper.assertTrue
import kotlin.test.Test

class LogBetweenTests: BaseIntegrationTest() {
    private lateinit var commandHelper: TestCommandHelper

    override fun setup() {
        super.setup()
        commandHelper = TestCommandHelper(testFolder)
                .init()
                .initConfig()
    }

    @Test
    fun testNoCommitsBetween() {
        val branchName = "myBranch"

        commandHelper.writeToFile("file.txt", "Content")
                .addAll()
                .commit("Commit1")
        commandHelper.branchAndCheckout(branchName)

        val branch1 = littleGit.repoReader.getBranch("refs/heads/master").data!!
        val branch2 = littleGit.repoReader.getBranch("refs/heads/$branchName").data!!

        val logBetween = littleGit.repoReader.getLogBetween(branch1, branch2)

        assertEquals(0, logBetween.data?.size)
    }

    @Test
    fun testCommitsBetween() {
        val branchName = "myBranch"

        val commit1Message = "Commit1"
        val commit2Message = "Commit2"
        val commit3Message = "Commit3"
        commandHelper.writeToFile("file.txt", "Content")
                .addAll()
                .commit(commit1Message)
        commandHelper.branchAndCheckout(branchName)
        commandHelper.writeToFile("file.txt", "Content1")
                .addAll()
                .commit(commit2Message)
        commandHelper.writeToFile("file.txt", "Content3")
                .addAll()
                .commit(commit3Message)

        val branch1 = littleGit.repoReader.getBranch("refs/heads/master").data!!
        val branch2 = littleGit.repoReader.getBranch("refs/heads/$branchName").data!!

        val logBetween = littleGit.repoReader.getLogBetween(branch1, branch2)

        assertEquals(2, logBetween.data?.size)
        assertTrue(logBetween.data?.find { it.commitSubject == commit2Message } != null)
        assertTrue(logBetween.data?.find { it.commitSubject == commit3Message } != null)

    }
}