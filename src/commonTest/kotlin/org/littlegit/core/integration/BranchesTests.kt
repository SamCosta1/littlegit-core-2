package org.littlegit.core.integration

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.*
import org.littlegit.core.model.GitError
import org.littlegit.core.model.LocalBranch
import org.littlegit.core.model.RemoteBranch
import org.littlegit.core.model.ResetType
import org.littlegit.core.util.FileUtils
import kotlin.test.Test
import kotlin.test.assertNotSame

class BranchesTests: BaseIntegrationTest() {

    private lateinit var commandHelper: TestCommandHelper

    override fun setup() {
        super.setup()
        commandHelper = TestCommandHelper(testFolder)
                .init()
                .initConfig()
    }

    @Test
    fun testGetBranches() {

        val newBranch = "iron-hills-fixes"
        commandHelper
                .writeToFile("durin.txt", "Dwarves rule")
                .addAll()
                .commit()
                .branchAndCheckout(newBranch)

        val result = littleGit.repoReader.getBranches()
        assertTrue("Result was success", result.result is GitResult.Success)

        val branches = result.data

        assertNotNull(branches); branches!!
        assertEquals(2, branches.size)
        assertEquals("master", branches[0].branchName)
        assertEquals(newBranch, branches[1].branchName)
        assertEquals(commandHelper.getLastCommitHash(), branches[0].commitHash)
        assertEquals(commandHelper.getLastCommitHash(), branches[1].commitHash)
        assertFalse(branches[0].isHead)
        assertTrue(branches[1].isHead)
        assertTrue(branches[0] is LocalBranch)
        assertTrue(branches[1] is LocalBranch)
    }

    @Test
    fun testGetBranches_WhenNoneExist() {
        commandHelper.deleteBranch("master")

        val result = littleGit.repoReader.getBranches()
        assertTrue("Result was success", result.result is GitResult.Success)
        assertNotNull(result.data)
        assertEquals(0, result.data!!.size)
    }

    @Test
    fun testCreateBranch() {
        commandHelper
                .writeToFile("map.txt", "Route to gimli")
                .addAll()
                .commit()

        val branchName = "find-gimli"
        val createResult = littleGit.repoModifier.createBranch(branchName)
        assertTrue("Result was success", createResult.result is GitResult.Success)

        val branches = littleGit.repoReader.getBranches()
        val branch = branches.data?.find { it.branchName == branchName }

        assertTrue(branch != null)
        assertTrue(branch is LocalBranch)
    }

    @Test
    fun testCreateBranch_FullRefName() {
        commandHelper
                .writeToFile("map.txt", "Route to gimli")
                .addAll()
                .commit()

        val branchName = "find-gimli"
        val fullRefName = "refs/heads/$branchName"

        val createResult = littleGit.repoModifier.createBranch(fullRefName)
        assertTrue("Result was success", createResult.result is GitResult.Success)

        val branches = littleGit.repoReader.getBranches()
        val branch = branches.data?.find { it.branchName == branchName }

        assertTrue(branch != null)
        assertTrue(branch is LocalBranch)
    }

    @Test
    fun testCreateBranch_AlreadyExists() {
        val branchName = "find-gimli"

        commandHelper
                .writeToFile("map.txt", "Route to gimli")
                .addAll()
                .commit()
                .branchAndCheckout(branchName)

        val result = littleGit.repoModifier.createBranch(branchName).result
        assertTrue("Result was error", result is GitResult.Error); result as GitResult.Error
        assertTrue("Result was error", result.err is GitError.ReferenceAlreadyExists)
    }

    @Test
    fun testCreateBranch_EmptyRepo() {
        val branchName = "find-gimli"
        val result = littleGit.repoModifier.createBranch(branchName).result
        assertTrue("Result was error", result is GitResult.Error); result as GitResult.Error
        assertTrue("Result was error", result.err is GitError.InvalidHead)
    }

    @Test
    fun testCreateBranch_AtSpecifiedCommit() {
        val branchName = "find-gimli"

        val lastCommitHash = commandHelper
                                .writeToFile("map.txt", "Route to gimli")
                                .addAll()
                                .commit()
                                .writeToFile("map2.txt", "Escape route once we've got him")
                                .addAll()
                                .commit()
                                .getLastCommitHash()

        val commits = littleGit.repoReader.getCommitList().data!!

        // Create a branch off he first commit
        val result = littleGit.repoModifier.createBranch(branchName, commit = commits.first()).result
        assertTrue("Result was success", result is GitResult.Success)

        val branches = littleGit.repoReader.getBranches()
        val branch = branches.data?.find { it.branchName == branchName }

        assertTrue(branch != null)
        assertTrue(branch is LocalBranch)
        assertEquals(commits.first().hash, branch?.commitHash)
        assertNotSame(lastCommitHash, commits.first().hash)
    }

    @Test
    fun testCreateBranch_InvalidBranchName() {
        val branchName = "find-gimli:invalid"

        commandHelper
                .writeToFile("map.txt", "Route to gimli")
                .addAll()
                .commit()
                .branchAndCheckout(branchName)

        val result = littleGit.repoModifier.createBranch(branchName).result
        assertTrue("Result was error", result is GitResult.Error); result as GitResult.Error
        assertTrue("Result was error", result.err is GitError.InvalidRefName)
    }

    @Test
    fun testCheckoutBranch_CleanWorkingTree() {
        val branchName = "find-gimli"

        val branchContent = "Rout to gimli via mines of moria"
        val masterContent = "Route to gimli"

        val file = commandHelper.writeToFileAndReturnIt("map.txt", masterContent)

        commandHelper
                .addAll()
                .commit()
                .branchAndCheckout(branchName)
                .writeToFile(file.fileName, branchContent)
                .addAll()
                .commit()
                .checkout("master")

        val branches = littleGit.repoReader.getBranches()

        // Ensure we're currently on master
        assertTrue(branches.data!!.find { it.isHead }?.branchName == "master")

        val branchToCheckout = branches.data?.find { it.branchName == branchName }
        assertNotNull(branchToCheckout); branchToCheckout!!

        val result = littleGit.repoModifier.checkoutBranch(branchToCheckout)

        assertTrue("Result was success", result.result is GitResult.Success)

        val newBranches = littleGit.repoReader.getBranches()
        assertEquals(branchName, newBranches.data?.find { it.isHead }?.branchName)
        assertEquals(branchContent, FileUtils.readFromPath(file.toPath()).first())
    }

    @Test
    fun testCheckoutBranch_NonExistingBranch() {
        val branchName = "find-gimli"

        commandHelper
                .writeToFile("map.txt", "To gimliiii!")
                .addAll()
                .commit()
                .branchAndCheckout(branchName)

        val branches = littleGit.repoReader.getBranches().data!!
        val branch = branches.find { it.branchName == branchName } as LocalBranch

        commandHelper
                .checkout("master")
                .deleteBranch(branchName)

        val result = littleGit.repoModifier.checkoutBranch(branch, false).result

        assertTrue("Result was error", result is GitResult.Error); result as GitResult.Error
        assertTrue("Result was error", result.err is GitError.BranchNotFound)
    }

    @Test
    fun testCheckoutBranch_DirtyWorkingTree_WithoutMovingChanges() {
        val branchName = "find-gimli"

        val branchContent = "Rout to gimli via mines of moria"
        val masterContent = "Route to gimli"
        val dirtyContent = "Route to gimli unfinished"

        val file = commandHelper.writeToFileAndReturnIt("map.txt", masterContent)

        commandHelper
                .addAll()
                .commit()
                .branchAndCheckout(branchName)
                .writeToFile(file.fileName, branchContent)
                .addAll()
                .commit()
                .checkout("master")
                .writeToFile(file.fileName, dirtyContent)

        val branches = littleGit.repoReader.getBranches()

        // Ensure we're currently on master
        assertTrue(branches.data!!.find { it.isHead }?.branchName == "master")

        val branchToCheckout = branches.data?.find { it.branchName == branchName }
        val result = littleGit.repoModifier.checkoutBranch(branchToCheckout as LocalBranch, false).result

        assertTrue("Result was error", result is GitResult.Error); result as GitResult.Error
        assertTrue("Result was error", result.err is GitError.LocalChangesWouldBeOverwritten)

        val newBranches = littleGit.repoReader.getBranches()
        assertEquals("master", newBranches.data?.find { it.isHead }?.branchName)
        assertEquals(dirtyContent, FileUtils.readFromPath(file.toPath()).first())
    }

    @Test
    fun testCheckoutBranch_DirtyWorkingTree_WithMovingChanges() {
        val branchName = "find-gimli"

        val branchContent = "Rout to gimli via mines of moria"
        val masterContent = "Route to gimli"
        val dirtyContent = "Route to gimli unfinished"

        val file = commandHelper.writeToFileAndReturnIt("map.txt", masterContent)
        val dirtyContentAmended = "$dirtyContent amended"

        commandHelper
                .addAll()
                .commit()
                .branchAndCheckout(branchName)
                .writeToFile(file.fileName, branchContent)
                .addAll()
                .commit()
                .checkout("master")

                // Test includes unstaged changes, staged changes and untracked files
                .writeToFile(file.fileName, dirtyContent)
                .addAll()
                .writeToFile(file.fileName, dirtyContentAmended)

        val newFileContent = "New plans to find gimli"
        val newFile = commandHelper.writeToFileAndReturnIt("newFile.txt", newFileContent)

        val branches = littleGit.repoReader.getBranches()

        // Ensure we're currently on master
        assertTrue(branches.data!!.find { it.isHead }?.branchName == "master")

        val branchToCheckout = branches.data?.find { it.branchName == branchName }
        val result = littleGit.repoModifier.checkoutBranch(branchToCheckout as LocalBranch, true)

        assertTrue("Result was success", result.result is GitResult.Success)

        val newBranches = littleGit.repoReader.getBranches()
        assertEquals(branchName, newBranches.data?.find { it.isHead }?.branchName)
        assertEquals("""
        <<<<<<< Updated upstream
        $branchContent
        =======
        $dirtyContentAmended
        >>>>>>> Stashed changes
            """.trimIndent().split("\n"), FileUtils.readFromPath(file.toPath()))

        assertEquals(listOf(newFileContent), FileUtils.readFromPath(newFile.toPath()))
    }

    @Test
    fun testCheckoutBranch_RemoteBranch_WhenLocalExists() {
        val branchName = "find-gimli"
        val fileContent = "Route to gimli"
        commandHelper
                .writeToFile("map.txt", fileContent)
                .addAll()
                .commit()
                .branchAndCheckout(branchName)
                .addRemote("origin")
                .createRemoteBranch(branchName, "origin")
                .setupRemoteTracking("origin", branchName)
                .checkout("master")

        val remoteBranch = littleGit.repoReader.getBranches().data?.find { it is RemoteBranch && it.branchName == branchName }
        assertNotNull(remoteBranch); remoteBranch!!

        val result = littleGit.repoModifier.checkoutBranch(remoteBranch).result

        assertTrue(result is GitResult.Success)

        // Check correct branch checked out (i.e the local equivalent of the remote we specified)
        val currentBranch = littleGit.repoReader.getBranches().data?.find { it.isHead }
        assertNotNull(currentBranch); currentBranch!!

        assertTrue(currentBranch is LocalBranch); currentBranch as LocalBranch
        assertEquals(remoteBranch, currentBranch.upstream)
        assertEquals(branchName, currentBranch.branchName)
    }

    @Test
    fun testCheckoutBranch_RemoteBranch_WhenLocalDoesNotExist() {
        val branchName = "find-gimli"
        val fileContent = "Route to gimli"
        commandHelper
                .writeToFile("map.txt", fileContent)
                .addAll()
                .commit()
                .branchAndCheckout(branchName)
                .addRemote("origin")
                .createRemoteBranch(branchName, "origin")
                .setupRemoteTracking("origin", branchName)
                .checkout("master")
                .deleteBranch(branchName)

        val remoteBranch = littleGit.repoReader.getBranches().data?.find { it is RemoteBranch && it.branchName == branchName }
        assertNotNull(remoteBranch); remoteBranch!!

        val result = littleGit.repoModifier.checkoutBranch(remoteBranch).result

        assertTrue(result is GitResult.Success)

        // Check correct branch checked out (i.e the local equivalent of the remote we specified)
        val currentBranch = littleGit.repoReader.getBranches().data?.find { it.isHead }
        assertNotNull(currentBranch); currentBranch!!

        assertTrue(currentBranch is LocalBranch); currentBranch as LocalBranch
        assertEquals(remoteBranch, currentBranch.upstream)
        assertEquals(branchName, currentBranch.branchName)
    }

    @Test
    fun testCheckoutBranch_RemoteBranch_WhenRemoteAheadOfLocal() {
        val branchName = "find-gimli"
        val fileContent = "Route to gimli"

        val localBranchCommitHash = commandHelper
                .writeToFile("map.txt", fileContent)
                .addAll()
                .commit()
                .branchAndCheckout(branchName)
                .getLastCommitHash()

        val remoteCommitHash = commandHelper
                .writeToFile("map.txt", "$fileContent - 2")
                .addAll()
                .commit()
                .getLastCommitHash()

        commandHelper.addRemote("origin")
                .createRemoteBranch(branchName, "origin")
                .setupRemoteTracking("origin", branchName)
                .reset(localBranchCommitHash, ResetType.Hard)
                .checkout("master")
                .deleteBranch("find-gimli")

        val targetBranch = littleGit.repoReader.getBranches().data?.find { it is RemoteBranch }
        assertNotNull(targetBranch); targetBranch!!

        val checkoutResult = littleGit.repoModifier.checkoutBranch(targetBranch, true)
        assertFalse(checkoutResult.isError)

        val currentBranch = littleGit.repoReader.getBranches().data?.find { it.isHead }

        assertEquals(targetBranch.branchName, currentBranch?.branchName)
        assertEquals(remoteCommitHash, currentBranch?.commitHash)
    }

    @Test
    fun testCheckoutBranch_RemoteBranch_WhenRemoteHasDivergedFromLocal() {
        val branchName = "find-gimli"
        val fileContent = "Route to gimli"

        val localBranchCommitHash = commandHelper
                                        .writeToFile("map.txt", fileContent)
                                        .addAll()
                                        .commit()
                                        .branchAndCheckout(branchName)
                                        .getLastCommitHash()
        commandHelper
            .writeToFile("map.txt", "$fileContent - 2")
            .addAll()
            .commit()
            .addRemote("origin")
            .createRemoteBranch(branchName, "origin")
            .setupRemoteTracking("origin", branchName)
            .reset(localBranchCommitHash, ResetType.Hard)
            .checkout("master")

        val remoteBranch = littleGit.repoReader.getBranches().data?.find { it is RemoteBranch && it.branchName == branchName }
        assertNotNull(remoteBranch); remoteBranch!!

        val result = littleGit.repoModifier.checkoutBranch(remoteBranch).result
        assertTrue(result is GitResult.Error); result as GitResult.Error
        assertTrue(result.err is GitError.RemoteDivergedFromLocal)
    }
}