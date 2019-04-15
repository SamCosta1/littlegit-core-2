package org.littlegit.core.model

import java.time.OffsetDateTime

open class RawCommit(
        val hash: String,
        val refs: List<String>,
        val parentHashes: List<String>,
        val date: OffsetDateTime,
        val committerEmail: String,
        val commitSubject: String,
        val isHead: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawCommit

        if (hash != other.hash) return false
        if (refs != other.refs) return false
        if (parentHashes != other.parentHashes) return false
        if (date != other.date) return false
        if (committerEmail != other.committerEmail) return false
        if (commitSubject != other.commitSubject) return false
        if (isHead != other.isHead) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.hashCode()
        result = 31 * result + refs.hashCode()
        result = 31 * result + parentHashes.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + committerEmail.hashCode()
        result = 31 * result + commitSubject.hashCode()
        result = 31 * result + isHead.hashCode()
        return result
    }

    override fun toString(): String {
        return "RawCommit(hash='$hash', refs=$refs, parentHashes=$parentHashes, date=$date, committerEmail='$committerEmail', commitSubject='$commitSubject', isHead=$isHead)"
    }
}