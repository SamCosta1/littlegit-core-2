package org.littlegit.core.integration

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertEquals
import org.littlegit.core.helper.assertNotNull
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.model.GitError
import org.littlegit.core.parser.Remote
import kotlin.test.Test

class RemoteTests: BaseIntegrationTest() {

    override fun setup() {
        super.setup()
        TestCommandHelper(testFolder)
                .init()
                .initConfig()
    }

    @Test
    fun validAddRemoteTest() {
        val remoteName = "origin"
        val remoteUrl = "www.remote.com"
        val addResult = littleGit.repoModifier.addRemote("origin", remoteUrl)
        assertTrue("Adding remote was successful", addResult.result is GitResult.Success)

        val getResult = littleGit.repoReader.getRemotes()
        val remotes = getResult.data
        assertTrue("Getting remotes was successful", getResult.result is GitResult.Success)
        assertTrue("One remote found", remotes?.size == 1)
        assertEquals("name correct", remotes?.get(0)?.remoteName, remoteName)
        assertEquals("push url correct", remotes?.get(0)?.pushUrl, remoteUrl)
        assertEquals("fetch url correct", remotes?.get(0)?.fetchUrl, remoteUrl)
    }

    @Test
    fun missingRemoteNameTest() {
        val result = littleGit.repoModifier.addRemote("", "www.remote.com").result
        assertTrue("Adding remote was unsuccessful", result is GitResult.Error)
        assertTrue("error should be invalid Remote name", result is GitResult.Error && result.err is GitError.InvalidRemoteInfo)
    }

    @Test
    fun duplicateRemoteTest() {
        val remoteName = "origin"

        TestCommandHelper(testFolder).addRemote(remoteName)

        val result = littleGit.repoModifier.addRemote(remoteName, "www.bla.com").result
        assertTrue("Adding remote was unsuccessful", result is GitResult.Error)
        assertTrue("error should be remote already exists", result is GitResult.Error && result.err is GitError.RemoteAlreadyExists)
    }

    @Test
    fun testReadRemotes() {
        val remote1 = "remote1"
        val remote2 = "remote2"
        val remote1Url = "www.remote1.com"
        val remote2Url = "www.remote2.com"
        TestCommandHelper(testFolder).init()
                .addRemote(remote1, remote1Url)
                .addRemote(remote2, remote2Url)

        val gitResult = littleGit.repoReader.getRemotes()
        val remotes = gitResult.data
        val result = gitResult.result

        assertTrue("Result is successful", result is GitResult.Success)
        assertNotNull("Remotes non null", remotes)
        assertEquals(2, remotes!!.size)

        assertTrue(remotes.contains(Remote(remote1, remote1Url, remote1Url)))
        assertTrue(remotes.contains(Remote(remote2, remote2Url, remote2Url)))
    }

    @Test
    fun testReadNoRemotes() {
        TestCommandHelper(testFolder).init()

        val gitResult = littleGit.repoReader.getRemotes()
        val result = gitResult.result
        val remotes = gitResult.data

        assertTrue("Result is successful", result is GitResult.Success)
        assertNotNull("Remotes non null", remotes)
        assertEquals(0, remotes!!.size)
    }
}