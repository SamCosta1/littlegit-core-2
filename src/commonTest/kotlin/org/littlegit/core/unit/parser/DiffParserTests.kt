package org.littlegit.core.unit.parser

import org.littlegit.core.helper.LocalResourceFile
import org.littlegit.core.exception.MalformedDiffException
import org.littlegit.core.helper.assertEquals
import org.littlegit.core.model.*
import org.littlegit.core.util.Paths
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DiffParserTests {

    private val repoPath = Paths.get("/test")
    private val singleFileCreated = LocalResourceFile("diffCommits/diff-commit-create-one-file.txt")
    private val singleEmptyFileCreated = LocalResourceFile("diffCommits/diff-commit-create-empty-file.txt")
    private val singleEmptyFileDeleted = LocalResourceFile("diffCommits/diff-commit-remove-empty-file.txt")
    private val singleEmptyFileRenamed = LocalResourceFile("diffCommits/diff-commit-renamed-empty-file.txt")
    private val singleFileModified = LocalResourceFile("diffCommits/diff-commit-modify-one-file.txt")
    private val singleFileRenamed = LocalResourceFile("diffCommits/diff-commit-rename-one-file.txt")
    private val singleFileRemoved = LocalResourceFile("diffCommits/diff-commit-delete-one-file.txt")
    private val multipleFilesMultipleHunks = LocalResourceFile("diffCommits/diff-commit-multiple-files-multiple-hunks.txt")
    private val specialCharacters = LocalResourceFile("diffCommits/diff-commit-special-characters.txt")
    private val malformedDiff = LocalResourceFile("diffCommits/diff-commit-malformed.txt")
    private val noNewLineAtEndOfFile = LocalResourceFile("diffCommits/diff-commit-no-newline-at-end-of-file.txt")
    private val quotesInFileName = LocalResourceFile("diffCommits/diff-commit-quotes-in-filename.txt")

    @Test
    fun testQuotesInFileName() {
        val diff = DiffParser.parse(quotesInFileName.content, repoPath)


        val file1Diff = FileDiff.NewFile(repoPath.resolve("some file . txt"), emptyList())
        val file2Diff = FileDiff.NewFile(repoPath.resolve("some file . txt"), emptyList())

        val file3Content = listOf(
                DiffLine(DiffLineType.Deletion, 1, null, "oeirjgoeij"),
                DiffLine(DiffLineType.NoNewLineAtEndOfFile, null, null, "\\ No newline at end of file"),
                DiffLine(DiffLineType.Addition, null, 1, "+"),
                DiffLine(DiffLineType.Addition, null, 2, "+"),
                DiffLine(DiffLineType.Addition, null, 3, "Stuff"),
                DiffLine(DiffLineType.Addition, null, 4, "+"),
                DiffLine(DiffLineType.Addition, null, 5, "\""),
                DiffLine(DiffLineType.Addition, null, 6, "\""),
                DiffLine(DiffLineType.Addition, null, 7, ")^%\$£@!")
        )
        val file3Diff = FileDiff.ChangedFile(repoPath.resolve("\\\"directory\\\"/bla.txt"), listOf(Hunk(1,0,1,7, "", file3Content)))
        val correctDiff = Diff(listOf(file1Diff, file2Diff, file3Diff))

        assertEquals(correctDiff, diff)
    }

    @Test fun testEmptyFileCreated() {
        val diff = DiffParser.parse(singleEmptyFileDeleted.content, repoPath)

        val fileDiff = FileDiff.DeletedFile(repoPath.resolve("emptyFile.txt"), emptyList())
        val correctDiff = Diff(listOf(fileDiff))

        assertEquals(correctDiff, diff)
    }

    @Test fun testEmptyFileDeleted() {
        val diff = DiffParser.parse(singleEmptyFileCreated.content, repoPath)

        val fileDiff = FileDiff.NewFile(repoPath.resolve("emptyFile.txt"), emptyList())
        val correctDiff = Diff(listOf(fileDiff))

        assertEquals(correctDiff, diff)
    }

    @Test fun testEmptyFileRenamed() {
        val diff = DiffParser.parse(singleEmptyFileRenamed.content, repoPath)

        val fileDiff = FileDiff.RenamedFile(repoPath.resolve("file with spaces.txt"), repoPath.resolve("renamed file.txt"), emptyList())
        val correctDiff = Diff(listOf(fileDiff))

        assertEquals(correctDiff, diff)
    }

    @Test fun testSingleFileCreated() {
        val diff = DiffParser.parse(singleFileCreated.content, repoPath)

        val fileContent = listOf(DiffLine(DiffLineType.Addition, null, 1, "hellow"))
        val fileDiff = FileDiff.NewFile(repoPath.resolve("helloWorld.txt"), listOf(Hunk(0,0,1,0, "", fileContent)))
        val correctDiff = Diff(listOf(fileDiff))

        assertEquals(correctDiff, diff)
    }

    @Test fun testSingleFileModified() {
        val diff = DiffParser.parse(singleFileModified.content, repoPath)

        val fileContent = listOf(
            DiffLine(DiffLineType.Unchanged, 108, 108, ""),
            DiffLine(DiffLineType.Unchanged, 109, 109, ""),
            DiffLine(DiffLineType.Unchanged, 110, 110, "# End of https://www.gitignore.io/api/gradle,kotlin,intellij"),
            DiffLine(DiffLineType.Addition, null, 111, ""),
            DiffLine(DiffLineType.Addition, null, 112, "src/test/kotlin/org/littlegit/core/integration/\\.DS_Store")
        )

        val fileDiff = FileDiff.ChangedFile(repoPath.resolve(".gitignore"), listOf(Hunk(108,3,108,5, "gradle-app.setting", fileContent)))
        val correctDiff = Diff(listOf(fileDiff))

        assertEquals(correctDiff, diff)
    }

    @Test fun testSingleFileRenamed() {
        val diff = DiffParser.parse(singleFileRenamed.content, repoPath)

        val fileContent = listOf(
            DiffLine(DiffLineType.Unchanged, 1, 1, "function helloWorld() {"),
            DiffLine(DiffLineType.Deletion, 2, null, "   var hello = \"Hello world\";"),
            DiffLine(DiffLineType.Addition, null, 2, "   var hello = \"Hello world!!!!\";"),
            DiffLine(DiffLineType.Unchanged, 3, 3, "   console.log(hello);"),
            DiffLine(DiffLineType.Unchanged, 4, 4, "}"),
            DiffLine(DiffLineType.Unchanged, 5, 5, "")
        )

        val fileDiff = FileDiff.RenamedFile(repoPath.resolve("helloworld.txt"), repoPath.resolve("helloworld.js"), listOf(Hunk(1,5,1,5, "", fileContent)))
        val correctDiff = Diff(listOf(fileDiff))

        assertEquals(correctDiff, diff)
    }

    @Test fun testSingleFileRemoved() {
        val diff = DiffParser.parse(singleFileRemoved.content, repoPath)

        val fileContent = listOf(
                DiffLine(DiffLineType.Deletion, 1, null, "function helloWorld() {"),
                DiffLine(DiffLineType.Deletion, 2, null, "   var hello = \"Hello world!!!!\";"),
                DiffLine(DiffLineType.Deletion, 3, null, "   console.log(hello);"),
                DiffLine(DiffLineType.Deletion, 4, null, "}"),
                DiffLine(DiffLineType.Deletion, 5, null, ""),
                DiffLine(DiffLineType.Deletion, 6, null, "helloWorld();")
        )

        val fileDiff = FileDiff.DeletedFile(repoPath.resolve("helloworld.js"), listOf(Hunk(1,6,0,0, "", fileContent)))
        val correctDiff = Diff(listOf(fileDiff))

        assertEquals(correctDiff, diff)
    }

    @Test fun testMultipleFilesMultipleHunks() {
        val diff = DiffParser.parse(multipleFilesMultipleHunks.content, repoPath)

        val file1Hunk1 = listOf(
            DiffLine(DiffLineType.Unchanged, 46, 46, "        override val command: List<String> get() = listOf(\"git\", \"remote\", \"add\", name, url)"),
            DiffLine(DiffLineType.Unchanged, 47, 47, "    }"),
            DiffLine(DiffLineType.Unchanged, 48, 48, ""),
            DiffLine(DiffLineType.Addition, null, 49, "    class ListRemotes : GitCommand() {"),
            DiffLine(DiffLineType.Addition, null, 50, "        override val command: List<String> get() = listOf(\"git\", \"remote\", \"-vv\")"),
            DiffLine(DiffLineType.Addition, null, 51, "    }"),
            DiffLine(DiffLineType.Addition, null, 52, ""),
            DiffLine(DiffLineType.Unchanged, 49, 53, "    class Log : GitCommand() {"),
            DiffLine(DiffLineType.Unchanged, 50, 54, "        companion object {"),
            DiffLine(DiffLineType.Unchanged, 51, 55, "            var deliminator = \"@|@\"")
        )

        val file1Hunk2 = listOf(
            DiffLine(DiffLineType.Unchanged, 55, 59, ""),
            DiffLine(DiffLineType.Unchanged, 56, 60, "        override val command: List<String> get() = listOf(\"git\", \"log\", \"--all\", \"--decorate=full\", \"--format=\$format\")"),
            DiffLine(DiffLineType.Unchanged, 57, 61, "    }"),
            DiffLine(DiffLineType.Addition, null, 62, ""),
            DiffLine(DiffLineType.Addition, null, 63, ""),
            DiffLine(DiffLineType.Unchanged, 58, 64, "}")
        )

        val file1Diff = FileDiff.ChangedFile(repoPath.resolve("src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt"), listOf(
            Hunk(46,6,46,10, "abstract class GitCommand {", file1Hunk1),
            Hunk(55,4,59,6, "abstract class GitCommand {", file1Hunk2)
        ))

        val file2Content = listOf(
            DiffLine(DiffLineType.Addition, null, 1, "package org.littlegit.core.parser"),
            DiffLine(DiffLineType.Addition, null, 2, ""),
            DiffLine(DiffLineType.Addition, null, 3, "data class Remote(val remoteName: String, var pushUrl: String = \"\", var fetchUrl: String = \"\")"),
            DiffLine(DiffLineType.Addition, null, 4, ""),
            DiffLine(DiffLineType.Addition, null, 5, "class InvalidRemote(override var message: String = \"Remote is malformed\", raw: String): Exception(\"\$message: \$raw\")"),
            DiffLine(DiffLineType.Addition, null, 6, ""),
            DiffLine(DiffLineType.Addition, null, 7, "object RemoteParser {"),
            DiffLine(DiffLineType.NoNewLineAtEndOfFile, null, null, "\\ No newline at end of file")
        )

        val file2Diff = FileDiff.NewFile(repoPath.resolve("src/main/kotlin/org/littlegit/core/parser/RemoteParser.kt"), listOf(
            Hunk(0,0,1,7, "", file2Content)
        ))

        val correctDiff = Diff(listOf(file1Diff, file2Diff))
        println(file2Diff.hunks)
        println(diff.fileDiffs.last().hunks)
        assertEquals(correctDiff, diff)
    }

    @Test fun testSpecialCharacters() {
        val diff = DiffParser.parse(specialCharacters.content, repoPath)

        val file1Content = listOf(
                DiffLine(DiffLineType.Addition, null, 1, "+plus"),
                DiffLine(DiffLineType.Addition, null, 2, "-minus"),
                DiffLine(DiffLineType.Addition, null, 3, "@@ats"),
                DiffLine(DiffLineType.Addition, null, 4, "diff --git"),
                DiffLine(DiffLineType.Addition, null, 5, ""),
                DiffLine(DiffLineType.Addition, null, 6, "\$dollar")
        )

        val file2Content = listOf(
                DiffLine(DiffLineType.Unchanged, 2, 2, "-bla"),
                DiffLine(DiffLineType.Unchanged, 3, 3, "+bla"),
                DiffLine(DiffLineType.Unchanged, 4, 4, " bla"),
                DiffLine(DiffLineType.Addition , null, 5, "-hello")
        )

        val file1Diff = FileDiff.NewFile(repoPath.resolve("file1.txt"), listOf(
            Hunk(0, 0,1, 6, "", file1Content)
        ))

        val file2Diff = FileDiff.ChangedFile(repoPath.resolve("file2.txt"), listOf(
            Hunk(2,3,2,4, "", file2Content)
        ))

        val file3Diff = FileDiff.NewFile(repoPath.resolve("a/b/awks file \\\\ name : +_)(*&^%\$\\302\\243@!/:\\\\:\\\\ :b !@\\302\\243\$%^&*()_+eroij    oif .txt"), emptyList())

        val correctDiff = Diff(listOf(file1Diff, file2Diff, file3Diff))
        assertEquals(correctDiff, diff)
    }

    @Test
    fun testMalformedDiff() {
        assertFailsWith(MalformedDiffException::class) {
            DiffParser.parse(malformedDiff.content, repoPath)
        }
    }

    @Test
    fun testNoNewLineAtEndOfFile() {
        val diff = DiffParser.parse(noNewLineAtEndOfFile.content, repoPath)

        val file1Content = listOf(
                DiffLine(DiffLineType.Deletion, 1, null, "name")
        )

        val file2Content = listOf(
                DiffLine(DiffLineType.Addition, null, 1, "Some Name"),
                DiffLine(DiffLineType.NoNewLineAtEndOfFile, null, null, "\\ No newline at end of file")
        )

        val file1Diff = FileDiff.DeletedFile(repoPath.resolve("name"), listOf(Hunk(1,0,0,0, "", file1Content)))
        val file2Diff = FileDiff.NewFile(repoPath.resolve("name.txt"), listOf(Hunk(0,0,1,0, "", file2Content)))
        val correctDiff = Diff(listOf(file1Diff, file2Diff))

        assertEquals(correctDiff, diff)
    }
}