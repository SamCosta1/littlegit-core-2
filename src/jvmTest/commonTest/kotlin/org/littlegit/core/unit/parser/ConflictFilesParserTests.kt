package org.littlegit.core.unit.parser


import org.littlegit.core.helper.LocalResourceFile
import org.littlegit.core.helper.assertEquals
import org.littlegit.core.helper.assertFalse
import org.littlegit.core.model.ConflictFile
import org.littlegit.core.model.MergeResult
import org.littlegit.core.util.Paths
import kotlin.test.Test


@Suppress("MemberVisibilityCanBePrivate")
class ConflictFilesParserTests {

    private val filesInSubDirs = LocalResourceFile("conflictFiles/files-in-subdirs.txt")

    @Test
    fun testWithFilesInSubDirs() {
        val dummyPath = Paths.get("/Users/gandalf/Projects/toTheMountain/testFiles")
        val parsed = ConflictFilesParser.parse(dummyPath, filesInSubDirs.content)

        val expectedFile = ConflictFile(
                Paths.get(dummyPath.toFile().canonicalPath, "subdir/sub.txt"),
                "30844b63e5f1bec58ef2ce01db1311d17699988e",
                "9f34a0f0645fa9afd22f02be37cc5031a079d308",
                "c64c940750ba8809f15c21250f1ed55bd4dcb95d")

        assertEquals(MergeResult(listOf(expectedFile)), parsed)
    }

    @Test
    fun testEmptyRawOutput() {
        val dummyPath = "/Users/gandalf/Projects/toTheMountain/testFiles"

        val parsed = ConflictFilesParser.parse(Paths.get(dummyPath), listOf())
        assertFalse(parsed.hasConflicts)
        assertEquals(0, parsed.conflictFiles.size)
    }

    @Test
    fun testBlankRawOutput() {
        val dummyPath = "/Users/gandalf/Projects/toTheMountain/testFiles"

        val parsed = ConflictFilesParser.parse(Paths.get(dummyPath), listOf("", "   "))
        assertFalse(parsed.hasConflicts)
        assertEquals(0, parsed.conflictFiles.size)
    }
}
