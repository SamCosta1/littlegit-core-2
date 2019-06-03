package org.littlegit.core.integration

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertFalse
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.model.FileDiff
import org.littlegit.core.model.GitError
import kotlin.test.Test
import kotlin.test.assertNotNull

class StagingTests: BaseIntegrationTest() {
    private lateinit var commandHelper: TestCommandHelper

    override fun setup() {
        super.setup()
        commandHelper = TestCommandHelper(testFolder)
                            .init()
                            .initConfig()
    }

    @Test
    fun testStageExistingFile() {
        val fileName = "druid.txt"

        val file = commandHelper.writeToFileAndReturnIt(fileName, "Some content")
        val result = littleGit.repoModifier.stageFile(file)

        assertTrue(result.result is GitResult.Success)
        assertTrue(commandHelper.isStaged(file))
    }

    @Test
    fun testStage1FileOf2() {
        val file1Name = "test.txt"
        val file2Name = "test2.txt"

        val file1 = commandHelper.writeToFileAndReturnIt(file1Name, "Some content")
        val file2 = commandHelper.writeToFileAndReturnIt(file2Name, "Some content")

        val result = littleGit.repoModifier.stageFile(file1)

        assertTrue(result.result is GitResult.Success)
        assertTrue(commandHelper.isStaged(file1))
        assertFalse(commandHelper.isStaged(file2))
    }

    @Test
    fun testStageNonExistentFile() {
        val fileName = "wizard.txt"

        val file = fileInTestFolder(fileName)

        val gitResult = littleGit.repoModifier.stageFile(file).result
        assertFalse(file.exists())
        assertTrue(gitResult is GitResult.Error)

        gitResult as GitResult.Error
        assertTrue(gitResult.err is GitError.PathSpecMatchesNoFiles)
    }

    @Test
    fun testUnStageExistingFile() {
        val fileName = "goblin.txt"

        val file = commandHelper.writeToFileAndReturnIt(fileName, "Some content")

        littleGit.repoModifier.stageFile(file)
        assertTrue(commandHelper.isStaged(file))

        val result = littleGit.repoModifier.unStageFile(file)
        assertTrue(result.result is GitResult.Success)
        assertFalse(commandHelper.isStaged(file))
    }

    @Test
    fun testUnStageNonExistentFile() {
        val fileName = "halfling.txt"

        val file = fileInTestFolder(fileName)

        val gitResult = littleGit.repoModifier.stageFile(file).result
        assertFalse(file.exists())
        assertTrue(gitResult is GitResult.Error)

        gitResult as GitResult.Error
        assertTrue(gitResult.err is GitError.PathSpecMatchesNoFiles)
    }

    @Test
    fun testStageUnStageHunk() {
        val fileName = "sorcerer.txt"

        val testContent = """
            Three rings for the Elven-kings under the sky,
            Seven for the Dwarf-lords in their halls of stone,
            Nine for mortal men doomed to die,
            One for the Dark Lord on his dark throne;


            In the Land of Mordor where the shadows lie.

            One ring to rule them all, one ring to find them,
            One ring to bring them all, and in the darkness bind them;

            In the Land of Mordor where the shadows lie.
        """.trimIndent()

        val modifiedContent = testContent.split("\n").toMutableList()
        modifiedContent[2] = "Nine for mortal human beings doooomed to die"
        modifiedContent.add("Great poem that ^")

        // First commit a change to the file since only file modifications create hunks (i.e. new files don't)
        commandHelper.writeToFile(fileName, testContent.split("\n"))
                .addAll()
                .commit("Mordorrr")
                .writeToFile(fileName, modifiedContent)

        val diff = littleGit.repoReader.getUnStagedChanges()

        // This generates two hunks, we'll use the second one because it's smaller
        val fileDiff = diff.data?.trackedFilesDiff?.fileDiffs?.first()
        assertTrue(fileDiff is FileDiff.ChangedFile); fileDiff as FileDiff.ChangedFile
        val hunk = fileDiff.hunks.lastOrNull()
        assertNotNull(hunk)

        // Now stage the hunk
        val result = littleGit.repoModifier.stageHunk(hunk, fileDiff)
        assertTrue(result.result is GitResult.Success)

        // That hunk should now be gone from un-staged changes and appear in staged changes
        var unStagedDiff = littleGit.repoReader.getUnStagedChanges()
        assertFalse(unStagedDiff.data?.trackedFilesDiff?.fileDiffs?.first()?.hunks?.contains(hunk)!!)

        var stagedDiff = littleGit.repoReader.getStagingAreaDiff()
        assertTrue(stagedDiff.data?.fileDiffs?.first()?.hunks?.contains(hunk)!!)

        // Now un-stage it again
        val unStageResult = littleGit.repoModifier.unStageHunk(hunk, fileDiff)
        assertTrue(unStageResult.result is GitResult.Success)

        // That hunk should now be gone from staged changes and appear in un-staged changes
        unStagedDiff = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedDiff.data?.trackedFilesDiff?.fileDiffs?.first()?.hunks?.contains(hunk)!!)

        stagedDiff = littleGit.repoReader.getStagingAreaDiff()
        assertTrue(stagedDiff.data?.fileDiffs?.isEmpty()!!)
    }

    @Test fun testStageUnStageHunk_AddLine_SmallFile() {
        val fileName = "rogue.txt"

        val testContent = """
            Ash nazg durbatulûk, ash nazg gimbatul
        """.trimIndent()

        val modifiedContent = testContent.split("\n").toMutableList()
        modifiedContent.add("ash nazg thrakatulûk agh burzum-ishi krimpatul.")

        // First commit a change to the file since only file modifications create hunks (i.e. new files don't)
        commandHelper.writeToFile(fileName, testContent.split("\n"))
                .addAll()
                .commit("Mordorrr")
                .writeToFile(fileName, modifiedContent)

        val diff = littleGit.repoReader.getUnStagedChanges()

        // This generates one hunk
        val fileDiff = diff.data?.trackedFilesDiff?.fileDiffs?.first()
        assertTrue(fileDiff is FileDiff.ChangedFile); fileDiff as FileDiff.ChangedFile
        val hunk = fileDiff.hunks.lastOrNull()
        assertNotNull(hunk)

        // Now stage the hunk
        val result = littleGit.repoModifier.stageHunk(hunk, fileDiff)
        assertTrue(result.result is GitResult.Success)

        // That hunk should now be gone from un-staged changes and appear in staged changes
        var unStagedDiff = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedDiff.data?.trackedFilesDiff?.fileDiffs?.isEmpty()!!)

        var stagedDiff = littleGit.repoReader.getStagingAreaDiff()
        assertTrue(stagedDiff.data?.fileDiffs?.first()?.hunks?.contains(hunk)!!)

        // Now un-stage it again
        val unStageResult = littleGit.repoModifier.unStageHunk(hunk, fileDiff)
        assertTrue(unStageResult.result is GitResult.Success)

        // That hunk should now be gone from staged changes and appear in un-staged changes
        unStagedDiff = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedDiff.data?.trackedFilesDiff?.fileDiffs?.first()?.hunks?.contains(hunk)!!)

        stagedDiff = littleGit.repoReader.getStagingAreaDiff()
        assertTrue(stagedDiff.data?.fileDiffs?.isEmpty()!!)
    }

    @Test fun testStageUnStageHunk_RemoveLine_SmallFile() {
        val fileName = "rogue.txt"

        val testContent = """
            Ash nazg durbatulûk, ash nazg gimbatul
            ash nazg thrakatulûk agh burzum-ishi krimpatul.
        """.trimIndent()

        val modifiedContent = testContent.split("\n").toMutableList().dropLast(1)

        // First commit a change to the file since only file modifications create hunks (i.e. new files don't)
        commandHelper.writeToFile(fileName, testContent.split("\n"))
                .addAll()
                .commit("Mordorrr")
                .writeToFile(fileName, modifiedContent)

        val diff = littleGit.repoReader.getUnStagedChanges()

        // This generates one hunk
        val fileDiff = diff.data?.trackedFilesDiff?.fileDiffs?.first()
        assertTrue(fileDiff is FileDiff.ChangedFile); fileDiff as FileDiff.ChangedFile
        val hunk = fileDiff.hunks.lastOrNull()
        assertNotNull(hunk)

        // Now stage the hunk
        val result = littleGit.repoModifier.stageHunk(hunk, fileDiff)
        assertTrue(result.result is GitResult.Success)

        // That hunk should now be gone from un-staged changes and appear in staged changes
        var unStagedDiff = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedDiff.data?.trackedFilesDiff?.fileDiffs?.isEmpty()!!)

        var stagedDiff = littleGit.repoReader.getStagingAreaDiff()
        assertTrue(stagedDiff.data?.fileDiffs?.first()?.hunks?.contains(hunk)!!)

        // Now un-stage it again
        val unStageResult = littleGit.repoModifier.unStageHunk(hunk, fileDiff)
        assertTrue(unStageResult.result is GitResult.Success)

        // That hunk should now be gone from staged changes and appear in un-staged changes
        unStagedDiff = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedDiff.data?.trackedFilesDiff?.fileDiffs?.first()?.hunks?.contains(hunk)!!)

        stagedDiff = littleGit.repoReader.getStagingAreaDiff()
        assertTrue(stagedDiff.data?.fileDiffs?.isEmpty()!!)
    }

    @Test
    fun testStageAlreadyStagedHunk() {
        val fileName = "rogue.txt"

        val testContent = """
            Ash nazg durbatulûk, ash nazg gimbatul
            ash nazg thrakatulûk agh burzum-ishi krimpatul.
        """.trimIndent()

        val modifiedContent = testContent.split("\n").toMutableList().dropLast(1)

        // First commit a change to the file since only file modifications create hunks (i.e. new files don't)
        commandHelper.writeToFile(fileName, testContent.split("\n"))
                .addAll()
                .commit("Mordorrr")
                .writeToFile(fileName, modifiedContent)

        val diff = littleGit.repoReader.getUnStagedChanges()

        // This generates one hunk
        val fileDiff = diff.data?.trackedFilesDiff?.fileDiffs?.first()
        assertTrue(fileDiff is FileDiff.ChangedFile); fileDiff as FileDiff.ChangedFile
        val hunk = fileDiff.hunks.lastOrNull()
        assertNotNull(hunk);

        // Stage everything
        commandHelper.addAll()

        val gitResult = littleGit.repoModifier.stageHunk(hunk, fileDiff)
        val result = gitResult.result
        assertTrue(result is GitResult.Error); result as GitResult.Error
        assertTrue(result.err is GitError.PatchDoesNotApply)
    }
}