package org.littlegit.core.parser

import org.littlegit.core.commandrunner.InvalidCommitException
import org.littlegit.core.model.FullCommit
import org.littlegit.core.util.CPDate
import org.littlegit.core.util.Path
import org.littlegit.core.util.joinWithNewLines

object FullCommitParser {

    fun parse(lines: List<String>, repoPath: Path): FullCommit {
        if (lines.isEmpty()) {
            throw InvalidCommitException(raw = lines.joinWithNewLines())
        }

        val rawCommitInfo = LogParser.parse(listOf(lines.first())).first()

        val commitBody = mutableListOf<String>()
        var numConsecutiveBlankLines = 0 // Commit message ended when we reach 2 consecutive blank lines

        var commitMessageEndIndex = 1
        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isBlank()) {
                commitBody.add(line)
                numConsecutiveBlankLines++
            } else {
                numConsecutiveBlankLines = 0
                commitBody.add(line)
            }

            if (numConsecutiveBlankLines == 2) {
                commitMessageEndIndex = i
                break
            }
        }

        val diffLines = lines.subList(commitMessageEndIndex, lines.size).dropWhile { it.isBlank() }
        val diff = DiffParser.parse(diffLines, repoPath)
        val formattedCommitBody = commitBody.dropLastWhile { it.isBlank() }

        return FullCommit.from(rawCommitInfo, diff, formattedCommitBody)
    }
}
