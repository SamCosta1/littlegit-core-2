package org.littlegit.core.parser

import org.littlegit.core.exception.MalformedConflictListException
import org.littlegit.core.model.ConflictFile
import org.littlegit.core.model.MergeResult
import org.littlegit.core.util.Path
import org.littlegit.core.util.Paths

object ConflictFilesParser {

    /**
     * Like an ConflictFile but mutable so that it can be built up
     */
    private class TempConflictFileVersion {
        var oursBlobHash: String? = null
        var theirsBlobHash: String? = null
        var baseBlobHash: String? = null

        val allHashesNonNull: Boolean; get() = oursBlobHash != null && theirsBlobHash != null && baseBlobHash != null
    }

    fun parse(repoPath: Path, lines: List<String>): MergeResult {
        // Map of file name -> conflict file
        val resultMap = HashMap<String, TempConflictFileVersion>()

        try {

            for (line in lines) {
                if (line.isBlank()) {
                    continue
                }
                val spaceSplit = line.split(" ")

                // TODO: Do something with this and actually handle binary files
                val fileMode = spaceSplit[0]
                val hash = spaceSplit[1]

                val tabSplit = spaceSplit[2].split("\t")
                val conflictMode = ConflictMode.from(tabSplit[0])
                val pathRelativeToRepo = tabSplit[1]

                val conflictFile = resultMap.getOrPut(pathRelativeToRepo) { TempConflictFileVersion() }

                when (conflictMode) {
                    ConflictMode.Ours   ->  conflictFile.oursBlobHash = hash
                    ConflictMode.Theirs ->  conflictFile.theirsBlobHash = hash
                    ConflictMode.Base   ->  conflictFile.baseBlobHash = hash
                }
            }

        } catch (e: Exception) {
            throw MalformedConflictListException(exception = e)
        }

        return MergeResult(resultMap.entries.map {

            if (!it.value.allHashesNonNull) {
                throw MalformedConflictListException()
            }

            ConflictFile(Paths.get(repoPath.toFile().canonicalPath, it.key),
                    it.value.oursBlobHash!!,
                    it.value.theirsBlobHash!!,
                    it.value.baseBlobHash!!)

        })

    }

    private enum class ConflictMode {
        Ours,
        Theirs,
        Base;

        companion object {
            fun from(raw: String): ConflictMode = when(raw.trim()) {
                "1" -> Base
                "2" -> Ours
                "3" -> Theirs
                else -> throw MalformedConflictListException()
            }
        }
    }
}

