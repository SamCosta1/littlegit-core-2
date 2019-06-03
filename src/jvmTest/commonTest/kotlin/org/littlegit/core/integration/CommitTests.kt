package org.littlegit.core.integration

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.model.FileDiff
import org.littlegit.core.model.GitError
import org.littlegit.core.model.Hunk
import kotlin.test.Test
import kotlin.test.assertEquals

class CommitTests: BaseIntegrationTest() {


    @Test
    fun testValidCommit() {
        val commitMessage = "test message"

        testFolder.newFile("testFile")
        val commandHelper = TestCommandHelper(testFolder)
                .init()
                .initConfig()
                .addAll()

        val result = littleGit.repoModifier.commit(commitMessage).result

        assertTrue("Result was a success", result is GitResult.Success)
        assertTrue("RawCommit message is as expected", commandHelper.getLastCommitMessage() == commitMessage)
    }

    @Test fun testCommitBeforeInit() {
        val result = littleGit.repoModifier.commit("msg").result
        assertTrue("RawCommit rejected", result is GitResult.Error && result.err is GitError.NotARepo)
    }

    @Test fun testCommitAfterInitBeforeAdd() {
        TestCommandHelper(testFolder).init().initConfig()

        val result = littleGit.repoModifier.commit("msg").result
        assertTrue("RawCommit rejected", result is GitResult.Error && result.err is GitError.NothingToCommit)
    }

    @Test fun testMultiLineCommitMessage() {
        val fileName = "testFile.txt"
        val commitSubject = "This is a subject line"
        val commitMessage = listOf(
                "First body line",
                "Second body line"
        )

        testFolder.newFile(fileName)
        val commandHelper = TestCommandHelper(testFolder)
        commandHelper.init().initConfig().addAll()

        val joinedMessage = mutableListOf(commitSubject, "")
        joinedMessage.addAll(commitMessage)
        littleGit.repoModifier.commit(joinedMessage)

        val result = littleGit.repoReader.getFullCommit(commandHelper.getLastCommitHash())
        val fullCommit = result.data

        assertEquals(commitMessage, fullCommit?.commitBody)

        val fileDiffs = fullCommit?.diff?.fileDiffs
        assertEquals(1, fileDiffs?.size)
        assertTrue(condition = fileDiffs?.get(0) is FileDiff.NewFile)

        val newFile = fileDiffs?.get(0) as FileDiff.NewFile
        assertEquals(fileName, newFile.filePath.fileName.toString())
        assertEquals(emptyList<Hunk>(), newFile.hunks)
        assertEquals(commitSubject, fullCommit.commitSubject)

    }

    // Lines with hashes are usually treated as comments and not included
    // in commit messages, littlegit should ensure this doesn't happen and that the
    // Lines beginning in hashes are present
    @Test fun testMultiLineCommitMessageWithHashes() {
        val fileName = "testFile.txt"
        val commitSubject = "This is a subject line"
        val commitMessage = listOf(
                "#First body line",
                "#Second body line"
        )

        testFolder.newFile(fileName)
        val commandHelper = TestCommandHelper(testFolder)
        commandHelper.init().initConfig().addAll()

        val joinedMessage = mutableListOf(commitSubject, "")
        joinedMessage.addAll(commitMessage)
        littleGit.repoModifier.commit(joinedMessage)

        val fullCommit = littleGit.repoReader.getFullCommit(commandHelper.getLastCommitHash()).data
        assertEquals(commitMessage, fullCommit?.commitBody)

        val fileDiffs = fullCommit?.diff?.fileDiffs
        assertEquals(1, fileDiffs?.size)
        assertTrue(condition = fileDiffs?.get(0) is FileDiff.NewFile)

        val newFile = fileDiffs?.get(0) as FileDiff.NewFile
        assertEquals(fileName, newFile.filePath.fileName.toString())
        assertEquals(emptyList(), newFile.hunks)
        assertEquals(commitSubject, fullCommit.commitSubject)
    }
}