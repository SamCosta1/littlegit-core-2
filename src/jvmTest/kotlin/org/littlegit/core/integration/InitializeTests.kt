package org.littlegit.core.integration


import kotlin.test.Test

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertFalse
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.util.Files
import org.littlegit.core.util.Paths

class InitializeTests: BaseIntegrationTest() {


    @Test
    fun testInitRepo() {
        val gitResult = littleGit.repoModifier.initializeRepo()
        assertTrue("Repo is initialized", gitResult.result is GitResult.Success)
        assertTrue(".git directory created", Files.exists(Paths.get("${testFolder.canonicalPath}/.git")))
    }

    @Test fun testInitBareRepo() {
        val gitResult = littleGit.repoModifier.initializeRepo(bare = true)

        assertTrue("Repo is initialized", gitResult.result is GitResult.Success)
        assertTrue("repo initialised", Files.exists(Paths.get("${testFolder.canonicalPath}/HEAD")))
    }

    @Test fun testInitBareRepoWithName() {
        val repoName = "testRepoName.git"
        val gitResult = littleGit.repoModifier.initializeRepo(bare = true, name = repoName)

        assertTrue("Repo is initialized", gitResult.result is GitResult.Success)
        assertFalse(".git directory created", Files.exists(Paths.get("${testFolder.canonicalPath}/.git")))
        assertTrue("directory created", Files.exists(Paths.get("${testFolder.canonicalPath}/$repoName")))
        assertTrue("repo initialised", Files.exists(Paths.get("${testFolder.canonicalPath}/$repoName/HEAD")))
    }

    @Test fun testCheckRepoNotInitialized() {
        assertFalse("Directory not initially a git directory", Files.exists(Paths.get("${testFolder.canonicalPath}/.git")))

        val isInitialized = littleGit.repoReader.isInitialized().data
        assertTrue("Repo not initialized", isInitialized == false)

    }

    @Test fun testCheckRepoInitialized() {
        assertFalse("Directory not initially a git directory", Files.exists(Paths.get("${testFolder.canonicalPath}/.git")))

        TestCommandHelper(testFolder).init()

        val isInitialized = littleGit.repoReader.isInitialized().data
        assertTrue("Repo  initialized", isInitialized == true)
    }
}
