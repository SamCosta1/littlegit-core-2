package org.littlegit.core.unit.parser

import org.littlegit.core.model.GitError
import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.LocalResourceFile
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.shell.ShellResult
import kotlin.test.Test

class ResultParserTests {

    private val localChangesErrorV1 = LocalResourceFile("err/err-local-changes_v1.txt")
    private val localChangesErrorV2 = LocalResourceFile("err/err-local-changes_v2.txt")
    private val notARepoError = LocalResourceFile("err/err-not-a-repo.txt")
    private val nothingToCommit = LocalResourceFile("err/err-nothing-to-commit.txt")
    private val noRemoteError = LocalResourceFile("err/err-no-remote.txt")
    private val noUpstreamError = LocalResourceFile("err/err-no-upstream.txt")
    private val cannotReadRemoteError = LocalResourceFile("err/err-cannot-read-remote.txt")
    private val cannotReadRemoteHttpError = LocalResourceFile("err/err-cannot-read-remote-http.txt")
    private val invalidRemoteName = LocalResourceFile("err/err-invalid-remote-windows.txt")
    private val lockedCommit = LocalResourceFile("err/err-head-locked.txt")
    private val corruptPatchError = LocalResourceFile("err/err-corrupt-patch.txt")

    @Test
    fun testLockedCommit() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(lockedCommit.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.CannotLockRef)
    }
    
    @Test fun testInvalidRemoteNameWindowsResult() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(invalidRemoteName.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.InvalidRemoteInfo)
    }

    @Test fun testLocalChangesWouldBeOverwritten_V1() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(localChangesErrorV1.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.LocalChangesWouldBeOverwritten)
    }

    @Test fun testLocalChangesWouldBeOverwritten_V2() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(localChangesErrorV2.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.LocalChangesWouldBeOverwritten)
    }

    @Test fun testNotARepo() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(notARepoError.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.NotARepo)
    }

    @Test fun testNothingToCommit() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Success(nothingToCommit.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.NothingToCommit)
    }

    @Test fun testNoRemoteBranch() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(noRemoteError.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.NoRemote)
    }

    @Test fun testNoUpstreamBranch() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(noUpstreamError.content))
        println(parsedResult)
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.NoUpstreamBranch)
    }

    @Test fun testCannotReadRemote() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(cannotReadRemoteError.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.CannotReadRemote)
    }

    @Test fun testCannotReadRemoteHttp() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(cannotReadRemoteHttpError.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.CannotReadRemote)
    }

    @Test fun testCorruptPatch() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(corruptPatchError.content))
        assertTrue(parsedResult is GitResult.Error && parsedResult.err is GitError.CorruptPatch)
    }

    @Test
    fun testEmptyError() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(emptyList()))
        assertTrue("Result result is error",  parsedResult is GitResult.Error)

        val error = parsedResult as GitResult.Error
        assertTrue("Result result is error",  error.err is GitError.Unknown)
        assertTrue("lines are empty", error.err.error.isEmpty())
    }

    @Test
    fun testEmptySuccess() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Success(emptyList()))
        assertTrue("Result result is success",  parsedResult is GitResult.Success)

        val success = parsedResult as GitResult.Success
        assertTrue("Result lines are empty",  success.lines.isEmpty())
    }

    @Test
    fun testBlankLinesError() {
        val parsedResult = GitResultParser.parseShellResult(ShellResult.Error(listOf("   ", "", " ")))
        assertTrue("Result result is error",  parsedResult is GitResult.Error)

        val error = parsedResult as GitResult.Error
        assertTrue("Result result is error",  error.err is GitError.Unknown)
        assertTrue("lines are empty", error.err.error.isEmpty())
    }

}