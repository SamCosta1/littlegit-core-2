package org.littlegit.core.parser

import org.littlegit.core.commandrunner.GitCommand
import org.littlegit.core.commandrunner.InvalidCommitException
import org.littlegit.core.model.RawCommit
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

data class RefsResult(val refs: List<String>, val isHead: Boolean)

object LogParser {
    fun parse(rawLines: List<String>): List<RawCommit> {
        if (rawLines.isEmpty()) {
            return emptyList()
        }

        val commits = mutableListOf<RawCommit>()

        rawLines.forEach {
            val split = it.removePrefix("\"").removeSuffix( "\"").split(GitCommand.Log.deliminator)

            if (split.size < 6) {
                throw InvalidCommitException(raw = it)
            }

            val commitHash = split[0]
            val parentHashes = split[1].split(" ").filter { it.isNotBlank() }
            val message = split.subList(5, split.size).joinToString(GitCommand.Log.deliminator)

            val date = OffsetDateTime.ofInstant(Instant.ofEpochMilli(split[3].toLong() * 1000), ZoneId.systemDefault())
            val refResults = RefsParser.parseRef(split[2])
            val committerEmail = split[4]

            if (commitHash.isBlank() || date == null) {
                throw InvalidCommitException(raw = it)
            }

            commits.add(RawCommit(commitHash, refResults.refs, parentHashes, date, committerEmail, message, refResults.isHead))
        }

        return commits.sortedByDescending { it.date }
    }
}

object RefsParser {
    fun parseRef(rawRef: String): RefsResult {
        // Refs are split by spaces. But the first ref could be in the form HEAD -> [name]

        var split = rawRef.split(" ")
        var isHead = false

        if (split.size >= 2 && split[1] == "->") {
            split = split.subList(2, split.size)
            isHead = true
        }

        val formatted = mutableListOf<String>() // Refs with the trailing commas removed

        split.forEach {
            val ref = it.removeSuffix(",")
            if (ref.isNotBlank()) {
                formatted.add(ref)
            }
        }

        return RefsResult(formatted, isHead)
    }
}