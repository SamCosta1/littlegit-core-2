package org.littlegit.core.parser

import org.littlegit.core.exception.MalformedDiffException
import org.littlegit.core.model.*
import org.littlegit.core.util.ListUtils
import org.littlegit.core.util.joinWithSpace
import org.littlegit.core.util.Path
import org.littlegit.core.util.Paths

object DiffParser {

    fun parse(lines: List<String>, repoPath: Path): Diff {
        val diffStartIndexes = ListUtils.findAllIndexesWhere(lines) { it.startsWith("diff --git") || it.startsWith("diff --cc") }
        val fileDiffs  = mutableListOf<FileDiff>()

        try {
            diffStartIndexes.forEachIndexed { indexOfIndex, _ ->
                val startIndex = diffStartIndexes[indexOfIndex] // The index of the diff start index in the diffStartIndexes list
                val endIndex = if (indexOfIndex == diffStartIndexes.lastIndex) {
                    lines.size
                } else {
                    diffStartIndexes[indexOfIndex + 1]
                }

                fileDiffs.add(parseFileDiff(lines, startIndex, endIndex, repoPath))
            }
        } catch (e: Exception) {
            throw MalformedDiffException(exception = e)
        }

        return Diff(fileDiffs)
    }

    private fun parseFileDiff(lines: List<String>, startIndex: Int, endIndex: Int, repoPath: Path): FileDiff {
        /*

         Before the actual diff are the lines for the form
         --- a/src/main/kotlin/org/littlegit/core/parser/GitResultParser.kt
         +++ b/src/main/kotlin/org/littlegit/core/parser/GitResultParser.kt

         */
        val aFilePathIndex = ListUtils.firstOccurrenceAfterIndex(lines, startIndex) { it.startsWith("---") }
        val bFilePathIndex = aFilePathIndex + 1

        // The file committed must have been empty and doesn't contain the normal structure
        if (aFilePathIndex < 0 || aFilePathIndex > endIndex) {
            return parseEmptyFile(lines, startIndex, repoPath)
        }

        val hunks = mutableListOf<Hunk>()
        var currentHunk: Hunk? = null

        // These initial values shouldn't ever be used, will always be initialised at the start of each hunk
        var fromLineNumber: Int = -1
        var toLineNumber: Int = -1
        var currentDiffLines = mutableListOf<DiffLine>()
        for (index in (aFilePathIndex + 2) until endIndex) {
            val line = lines[index]

            // Hunk header line is in the form:   @@ from-fileDiffs-range to-fileDiffs-range @@ [header]
            if (line.startsWith("@@ ")) {
                if (currentHunk != null) hunks.add(currentHunk)

                val splitted = line.split(" ")
                val header = splitted.subList(4, splitted.size).joinWithSpace().trim()
                val fromFileRange = splitted[1].removePrefix("-").split(",").map { it.trim().toInt() }.toMutableList()
                val toFileRange = splitted[2].removePrefix("+").split(",").map { it.trim().toInt() }.toMutableList()

                if (fromFileRange.size < 2) fromFileRange.add(0)
                if (toFileRange.size < 2) toFileRange.add(0)

                currentDiffLines = mutableListOf()
                currentHunk = Hunk(fromFileRange.first(), fromFileRange.last(), toFileRange.first(), toFileRange.last(), header, currentDiffLines)
                fromLineNumber = currentHunk.fromStartLine
                toLineNumber = currentHunk.toStartLine
            } else {
                val diffLine: DiffLine = when {
                    line.startsWith("+") -> {
                        DiffLine(DiffLineType.Addition, toLineNum = toLineNumber++, line = line.removePrefix("+"))
                    }
                    line.startsWith("-") -> {
                        DiffLine(DiffLineType.Deletion, fromLineNum = fromLineNumber++, line = line.removePrefix("-"))
                    }
                    line.startsWith("\\ No newline at end of file") -> {
                        DiffLine(DiffLineType.NoNewLineAtEndOfFile, line = line)
                    }
                    else -> {
                        DiffLine(DiffLineType.Unchanged, fromLineNum = fromLineNumber++, toLineNum = toLineNumber++, line = line.removePrefix(" "))
                    }
                }

                currentDiffLines.add(diffLine)
            }
        }

        currentHunk?.let { hunks.add(it) }

        val aFilePathStr = lines[aFilePathIndex].removePrefix("--- ")
        val bFilePathStr = lines[bFilePathIndex].removePrefix("+++ ")

        val aFilePath = Paths.get(repoPath.toString(), stripQuotesIfNeeded(aFilePathStr).removePrefix("a/"))
        val bFilePath = Paths.get(repoPath.toString(), stripQuotesIfNeeded(bFilePathStr).removePrefix("b/"))

        return when {
            aFilePathStr == "/dev/null" -> FileDiff.NewFile(bFilePath, hunks)
            bFilePathStr == "/dev/null" -> FileDiff.DeletedFile(aFilePath, hunks)
            aFilePath != bFilePath -> FileDiff.RenamedFile(aFilePath, bFilePath, hunks)
            else -> FileDiff.ChangedFile(aFilePath, hunks)
        }
    }

    private fun stripQuotesIfNeeded(filePath: String): String {
        // Sometimes the path is wrapped in quotes sometimes it isn't, account for both cases
        if (filePath.startsWith('"')) {
            return filePath.removePrefix("\"").removeSuffix("\"")
        }

        return filePath;
    }

    private fun parseEmptyFile(lines: List<String>, startIndex: Int, repoPath: Path): FileDiff {
        val deletedFile = lines[startIndex + 1].startsWith("deleted file mode")
        val newFile = lines[startIndex + 1].startsWith("new file mode")
        val renamedFile = !deletedFile && !newFile

        // For renames can't take the file name from the diff line since the names by definition could be different lengths and
        // There's no reliable way of pulling them out
        // Luckily git provides the following two lines which we'll use
        //rename from FROM_NAME
        //rename to TO_NAME
        if (renamedFile) {
            val fromLineIndex = ListUtils.firstOccurrenceAfterIndex(lines, startIndex) { it.startsWith("rename from") }
            val toLineIndex = ListUtils.firstOccurrenceAfterIndex(lines, startIndex) { it.startsWith("rename to") }

            val fromPath = lines[fromLineIndex].removePrefix("rename from ")
            val toPath = lines[toLineIndex].removePrefix("rename to ")
            return FileDiff.RenamedFile(Paths.get(repoPath.toString(), fromPath), Paths.get(repoPath.toString(), toPath), emptyList())
        } else {
            // In this case we strip it out of the diff header line: diff --git a/FILE_PATH b/FILE_PATH

            // Strip out the dif --git
            val filePaths = lines[startIndex].removePrefix("diff --git ")

            if (filePaths.isBlank()) {
                throw IllegalStateException("File paths are blank")
            }
            // The file name will be the same twice, the only reliable way of extracting it is to use the character count
            val pathLength = (filePaths.length - 1) / 2
            var filePath = filePaths.substring(0, pathLength)

            filePath = stripQuotesIfNeeded(filePath)

            val path = filePath.removePrefix("a/")

            return if (newFile) {
                FileDiff.NewFile(Paths.get(repoPath.toString(), path), emptyList())
            } else {
                FileDiff.DeletedFile(Paths.get(repoPath.toString(), path), emptyList())
            }
        }
    }
}