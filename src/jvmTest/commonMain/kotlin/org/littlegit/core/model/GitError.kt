package org.littlegit.core.model

sealed class GitError(val error: List<String>) {

    val errString: String; get() = error.joinToString("\n")

    data class LocalChangesWouldBeOverwritten(private val err: List<String>): GitError(err)
    data class Unknown(private val err: List<String>): GitError(err)
    data class NotARepo(private val err: List<String>): GitError(err)
    data class NothingToCommit(private val err: List<String>): GitError(err)
    data class NoRemote(private val err: List<String>): GitError(err)
    data class NoUpstreamBranch(private val err: List<String>): GitError(err)
    data class CannotReadRemote(private val err: List<String>): GitError(err)
    data class InvalidRemoteInfo(private val err: List<String>): GitError(err)
    data class RemoteAlreadyExists(private val err: List<String>): GitError(err)
    data class FileNotInIndex(private val err: List<String>, val fileExistsOnDisk: Boolean): GitError(err)
    data class PathSpecMatchesNoFiles(private val err: List<String>): GitError(err)
    data class CannotLockRef(private val err: List<String>) : GitError(err)
    data class PatchDoesNotApply(private val err: List<String>) : GitError(err)
    data class CorruptPatch(private val err: List<String>) : GitError(err)
    data class InvalidHead(private val err: List<String>): GitError(err)
    data class ReferenceAlreadyExists(private val err: List<String>): GitError(err)
    data class InvalidRefName(private val err: List<String>): GitError(err)
    data class BranchNotFound(private val err: List<String>): GitError(err)
         class RemoteDivergedFromLocal: GitError(listOf("Remote has diverged from the local, try checking out the local branch and then pulling"))
}
