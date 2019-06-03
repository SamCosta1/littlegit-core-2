package org.littlegit.core.integration

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.helper.TestCommandHelper
import org.littlegit.core.helper.assertEquals
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.model.DiffLine
import org.littlegit.core.model.DiffLineType
import org.littlegit.core.model.FileDiff
import kotlin.test.Test

class DiffTests: BaseIntegrationTest() {

    private lateinit var commandHelper: TestCommandHelper

    override fun setup() {
        super.setup()
        commandHelper = TestCommandHelper(testFolder)
                .init()
                .initConfig()
    }

    @Test
    fun testGetStagingArea_NoStagedChanges() {
        commandHelper.writeToFile("testFile.txt", "Some text")

        val result = littleGit.repoReader.getStagingAreaDiff()
        assertTrue(result.result is GitResult.Success)
        assertTrue(result.data?.fileDiffs?.isEmpty() == true)
    }

    @Test
    fun testGetStagingArea() {
        val stagedFileName = "stagedFile.txt"
        val stagedFileContent = "Mount doom"
        commandHelper
                .writeToFile(stagedFileName, stagedFileContent)
                .addAll()
                .writeToFile("unStagedFile.txt", "More text")

        val result = littleGit.repoReader.getStagingAreaDiff()
        assertTrue(result.result is GitResult.Success)
        assertEquals(1, result.data?.fileDiffs?.size)

        val fileDiff = result.data?.fileDiffs?.first()
        assertTrue(fileDiff is FileDiff.NewFile)
        fileDiff as FileDiff.NewFile

        assertEquals(stagedFileName, fileDiff.filePath.fileName.toString())
        assertEquals(1, fileDiff.hunks.size)
        assertEquals(1, fileDiff.hunks.first().lines.size)
        assertEquals(stagedFileContent, fileDiff.hunks.first().lines.first().line)
    }

    @Test
    fun testGetUnStagedChanges() {
        val trackedFileName = "trackedFile.txt"
        val unTrackedFileName = "unTrackedFile.txt"

        // Commit the tracked file
        val originalLine = "Gandalf rocks"
        val newLine = "Gandalf really rocks"
        val unTrackedFileContent = "Saruman sucks"

        commandHelper
                .writeToFile(trackedFileName, originalLine)
                .addAll()
                .commit("Commit")
                .writeToFile(unTrackedFileName, unTrackedFileContent)
                .writeToFile(trackedFileName, newLine)

        val unStagedChangesResult = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedChangesResult.result is GitResult.Success)

        // Assert tracked files correct
        val trackedFiles = unStagedChangesResult.data?.trackedFilesDiff
        assertEquals(1, trackedFiles?.fileDiffs?.size)

        val file = trackedFiles?.fileDiffs?.first() as FileDiff.ChangedFile
        assertEquals(trackedFileName, file.filePath.fileName.toString())

        assertEquals(listOf(
            DiffLine(DiffLineType.Deletion,1, null, originalLine),
            DiffLine(DiffLineType.Addition,null, 1, newLine)
        ), file.hunks.first().lines)

        // Assert un-tracked files correct
        val unTrackedFiles = unStagedChangesResult.data?.unTrackedFiles
        assertEquals(1, unTrackedFiles?.size)
        assertEquals(unTrackedFileName, unTrackedFiles?.first()?.file?.fileName)
        assertEquals(1, unTrackedFiles?.first()?.content?.size)
        assertEquals(unTrackedFileContent, unTrackedFiles?.first()?.content?.first())
    }

    @Test
    fun testGetUnStagedChanges_NoUnTrackedFiles() {
        val trackedFileName = "trackedFile.txt"

        // Commit the tracked file
        val originalLine = "Gandalf rocks"
        val newLine = "Gandalf really rocks"

        commandHelper
                .writeToFile(trackedFileName, originalLine)
                .addAll()
                .commit("Commit")
                .writeToFile(trackedFileName, newLine)

        val unStagedChangesResult = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedChangesResult.result is GitResult.Success)

        // Assert tracked files correct
        val trackedFiles = unStagedChangesResult.data?.trackedFilesDiff
        assertEquals(1, trackedFiles?.fileDiffs?.size)

        val file = trackedFiles?.fileDiffs?.first() as FileDiff.ChangedFile
        assertEquals(trackedFileName, file.filePath.fileName.toString())

        assertEquals(listOf(
                DiffLine(DiffLineType.Deletion,1, null, originalLine),
                DiffLine(DiffLineType.Addition,null, 1, newLine)
        ), file.hunks.first().lines)

        // Assert un-tracked files correct
        val unTrackedFiles = unStagedChangesResult.data?.unTrackedFiles
        assertEquals(0, unTrackedFiles?.size)
    }

    @Test
    fun testGetUnStagedChanges_NoTrackedFiles() {
        val unTrackedFileName = "unTrackedFile.txt"

        val unTrackedFileContent = "Saruman sucks"

        commandHelper
                .writeToFile(unTrackedFileName, unTrackedFileContent)

        val unStagedChangesResult = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedChangesResult.result is GitResult.Success)

        // Assert tracked files correct
        val trackedFiles = unStagedChangesResult.data?.trackedFilesDiff
        assertEquals(0, trackedFiles?.fileDiffs?.size)

        // Assert un-tracked files correct
        val unTrackedFiles = unStagedChangesResult.data?.unTrackedFiles
        assertEquals(1, unTrackedFiles?.size)
        assertEquals(unTrackedFileName, unTrackedFiles?.first()?.file?.fileName)
        assertEquals(1, unTrackedFiles?.first()?.content?.size)
        assertEquals(unTrackedFileContent, unTrackedFiles?.first()?.content?.first())
    }

    @Test
    fun testGetUnStagedChanges_NoChanges() {
        val unStagedChangesResult = littleGit.repoReader.getUnStagedChanges()
        assertTrue(unStagedChangesResult.result is GitResult.Success)
        assertEquals(0, unStagedChangesResult.data?.trackedFilesDiff?.fileDiffs?.size)
        assertEquals(0, unStagedChangesResult.data?.unTrackedFiles?.size)
    }
}