package org.littlegit.core.model

import org.littlegit.core.util.Path

data class Diff(val fileDiffs: List<FileDiff>)

interface SingleFilePathDiff: FileDiff {
    val filePath: Path
}

interface FileDiff {
    val hunks: List<Hunk>

    data class NewFile(override val filePath: Path, override val hunks: List<Hunk>) : SingleFilePathDiff
    data class DeletedFile(override val filePath: Path, override val hunks: List<Hunk>) : SingleFilePathDiff
    data class RenamedFile(val originalPath: Path, val newPath: Path, override val hunks: List<Hunk>) : FileDiff
    data class ChangedFile(override val filePath: Path, override val hunks: List<Hunk>) : SingleFilePathDiff
}

