package org.littlegit.core.model
import org.littlegit.core.util.joinWithNewLines
import java.time.OffsetDateTime

class FullCommit(hash: String,
                 refs: List<String>,
                 parentHashes: List<String>,
                 date: OffsetDateTime,
                 committerEmail: String,
                 commitSubject: String,
                 isHead: Boolean,
                 val diff: Diff = Diff(emptyList()),
                 val commitBody: List<String>)
    : RawCommit(hash,
        refs,
        parentHashes,
        date,
        committerEmail,
        commitSubject,
        isHead) {


    companion object {
        fun from(raw: RawCommit, diff: Diff, commitBody: List<String>): FullCommit {
            return FullCommit(raw.hash, raw.refs, raw.parentHashes, raw.date, raw.committerEmail, raw.commitSubject, raw.isHead, diff, commitBody)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FullCommit

        return commitBody == other.commitBody && diff == other.diff
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + commitBody.hashCode()
        return result
    }

    override fun toString(): String = "FullCommit(${super.toString()} diff=$diff, commitBody='$commitBody')"

}
