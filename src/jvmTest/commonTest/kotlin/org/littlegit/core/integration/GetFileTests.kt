package org.littlegit.core.integration

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertEquals
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.model.GitError
import kotlin.test.Test
class GetFileTests: BaseIntegrationTest() {

    private lateinit var commandHelper: TestCommandHelper

    override fun setup() {
        super.setup()
        commandHelper = TestCommandHelper(testFolder)
                .init()
                .initConfig()
    }

    @Test
    fun lastCommitFileTest() {
        val testFileName = "file.txt"
        val content = "Ash nazg durbatulûk, ash nazg gimbatul,\n ash nazg thrakatulûk agh burzum-ishi krimpatul."

        val file = commandHelper.writeToFileAndReturnIt(testFileName, content)

        commandHelper
                .addAll()
                .commit("Commit")

        val gitResult = littleGit.repoReader.getFile(file = file)
            assertTrue("Result was success", gitResult.result is GitResult.Success)
            assertEquals("Result file content is correct", gitResult.data?.content, content.split("\n"))
            assertEquals("Result file name is correct", gitResult.data?.file, file)

    }

    @Test fun testUnTrackedFile() {
        val testFileName = "file.txt"
        val content = "Ash nazg durbatulûk, ash nazg gimbatul,\n ash nazg thrakatulûk agh burzum-ishi krimpatul."

        val file = commandHelper.writeToFileAndReturnIt(testFileName, content)

        val gitResult = littleGit.repoReader.getFile(file = file)
        assertTrue("Result was error", gitResult.result is GitResult.Error)

        val error = gitResult.result as GitResult.Error
        assertTrue("Result was correct error type", error.err is GitError.FileNotInIndex)
        assertTrue("File exists on disk", (error.err as GitError.FileNotInIndex).fileExistsOnDisk)

    }

    @Test fun testNonExistingFile() {
        val file = commandHelper.writeToFileAndReturnIt("somefile.txt", "")
        file.delete()

        val gitResult = littleGit.repoReader.getFile(file = file)
        assertTrue("Result was error", gitResult.result is GitResult.Error)

        val err = (gitResult.result as GitResult.Error).err
        assertTrue("Result was correct error type", err is GitError.FileNotInIndex)

        err as GitError.FileNotInIndex
        assertTrue("File doesn't exists on disk", !err.fileExistsOnDisk)

    }

    @Test fun testOtherBranchVersion() {
        val testFileName = "file.txt"
        val branch = "branch"
        val content1 = "Ash nazg durbatulûk, ash nazg gimbatul,\n ash nazg thrakatulûk agh burzum-ishi krimpatul."
        val content2 = "One ring to rule them all"

        val file = commandHelper.writeToFileAndReturnIt(testFileName, content1)

        commandHelper
                .addAll()
                .commit("Commit1")
                .branchAndCheckout(branch)
                .writeToFile(testFileName, content2)
                .addAll()
                .commit("Commit2")
        
        val branch1Res = littleGit.repoReader.getFile("master", file)
        assertTrue("Result was success", branch1Res.result is GitResult.Success)
        assertEquals("Result file content is correct", branch1Res.data?.content, content1.split("\n"))
        assertEquals("Result file name is correct", branch1Res.data?.file, file)

        val branch2Res = littleGit.repoReader.getFile(branch, file)
        assertTrue("Result was success", branch1Res.result is GitResult.Success)
        assertEquals("Result file content is correct", branch2Res.data?.content, content2.split("\n"))
        assertEquals("Result file name is correct", branch2Res.data?.file, file)
    }
}