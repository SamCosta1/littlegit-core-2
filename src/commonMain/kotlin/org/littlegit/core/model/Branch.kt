package org.littlegit.core.model

import org.littlegit.core.commandrunner.CommitHash
import org.littlegit.core.exception.RemoteNotFoundException
import org.littlegit.core.parser.Remote

abstract class Branch(
        val fullRefName: String,
        val isHead: Boolean,
        val commitHash: CommitHash
) {
    companion object {
        fun createFrom(refName: String, head: Boolean, objectName: String, objectType: String, upstream: RemoteBranch? = null, remotes: List<Remote>?): Branch? {
            if (refName.isBlank() || objectName.isBlank()) {
                return null
            }

            val commitHash = if (ObjectType.fromRaw(objectType) == ObjectType.Commit) {
                objectName.trim()
            } else {
                ""
            }

            return when {
                refName.startsWith("refs/heads/") -> LocalBranch(refName, head, upstream, commitHash)
                refName.startsWith("refs/remotes/") -> {
                    val remote = remotes?.find { refName.startsWith("refs/remotes/${it.remoteName}") } ?: throw RemoteNotFoundException(refName)
                    RemoteBranch(refName, head, commitHash, remote)
                }
                else -> null
            }
        }
    }


    abstract val branchName: String

    override fun equals(other: Any?): Boolean {
        return other is Branch
                && other.fullRefName == this.fullRefName
                && other.isHead == this.isHead
                && other.commitHash == this.commitHash
    }

    override fun hashCode(): Int {
        var result = fullRefName.hashCode()
        result = 31 * result + (commitHash.hashCode())
        return result
    }
}

class RemoteBranch(refName: String, head: Boolean, commitHash: String, val remote: Remote) : Branch(refName, head, commitHash) {
    override val branchName: String; get() = fullRefName.removePrefix("refs/remotes/${remote.remoteName}/")

    val branchNameWithRemote: String = fullRefName.removePrefix("refs/remotes/")

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is RemoteBranch
    }

}

class LocalBranch(refName: String, head: Boolean, val upstream: RemoteBranch?, commitHash: String) : Branch(refName, head, commitHash) {
    override val branchName: String; get() = fullRefName.removePrefix("refs/heads/")

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is LocalBranch && this.upstream == other.upstream
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (upstream?.hashCode() ?: 0)
        return result
    }
}
