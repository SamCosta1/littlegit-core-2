package org.littlegit.core.model

import org.littlegit.core.util.Path

data class MergeResult(val conflictFiles: List<ConflictFile>) {
    val hasConflicts: Boolean; get() = !conflictFiles.isEmpty()
}

data class ConflictFile(val filePath: Path, val oursBlobHash: String, val theirsBlobHash: String, val baseBlobHash: String)

enum class ConflictFileType {
    Theirs,
    Ours,
    Base;

    fun getHash(conflictFile: ConflictFile): String {
        return when(this) {
            Theirs -> conflictFile.theirsBlobHash
            Ours -> conflictFile.oursBlobHash
            Base -> conflictFile.baseBlobHash
        }
    }
}