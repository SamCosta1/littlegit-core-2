package org.littlegit.core.model

data class UnstagedChanges(val trackedFilesDiff: Diff,
                           val unTrackedFiles: List<LittleGitFile>) {

    val hasTrackedChanges: Boolean; get() = !trackedFilesDiff.fileDiffs.isEmpty()
}