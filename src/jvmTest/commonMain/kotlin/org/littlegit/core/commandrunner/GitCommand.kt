package org.littlegit.core.commandrunner

import org.littlegit.core.model.Branch
import org.littlegit.core.model.LocalBranch
import org.littlegit.core.model.RemoteBranch
import org.littlegit.core.model.ResetType
import org.littlegit.core.util.File
import org.littlegit.core.util.OSType
import org.littlegit.core.util.OperatingSystemUtils
import org.littlegit.core.util.Path


typealias CommitHash = String
abstract class GitCommand {

    abstract val command: List<String>

    class ShowFile(private val ref: String, private val filePath: String): GitCommand() {
        override val command: List<String> get() = listOf("git", "show", "$ref:$filePath")
    }
    
    class IsInitialized : GitCommand() {
        override val command: List<String> get() = listOf("git", "rev-parse", "--is-inside-work-tree")
    }

    class InitializeRepo(private val bare: Boolean, val name: String? = null) : GitCommand() {
        override val command: List<String> get()  {
            val commands = mutableListOf("git", "init")
            if (bare)         commands.add("--bare")
            if (name != null) commands.add(name)

            return commands
        }
    }

    class Commit(private val commitFile: File) : GitCommand() {
        override val command: List<String> get() = listOf("git", "commit", "-F", commitFile.canonicalPath)
    }

    class SetSshKeyPath(path: Path): GitCommand() {
        override val command: List<String> = listOf("git", "config", "core.sshCommand", "ssh -i '${path.normalize()}'")
    }

    class GetSshKeyPath: GitCommand() {
        override val command: List<String> = listOf("git", "config", "core.sshCommand")
    }

    class SetUserEmail(private val email: String, private val global: Boolean = false) : GitCommand() {
        override val command: List<String> get() = if (global) listOf("git", "config", "--global", "user.email", email) else listOf("git", "config", "user.email", email)
    }

    class SetUserName(val name: String, private val global: Boolean = false) : GitCommand() {
        override val command: List<String> get() = if (global) listOf("git", "config", "--global", "user.name", name) else listOf("git", "config", "user.name", name)
    }

    class GetUserName(private val global: Boolean = false): GitCommand() {
        override val command: List<String> get() = if (global) listOf("git", "config", "--global", "user.name") else listOf("git", "config", "user.name")
    }

    class GetUserEmail(private val global: Boolean = false): GitCommand() {
        override val command: List<String> get() = if (global) listOf("git", "config", "--global", "user.email") else listOf("git", "config", "user.email")
    }

    class Push(val remote: String? = null, val branch: String? = null): GitCommand() {
        override val command: List<String> get() = listOf("git", "push") + if (remote != null && branch != null) listOf(remote, branch) else emptyList()
    }

    class Fetch(quiet: Boolean, all: Boolean): GitCommand() {
        private val base = mutableListOf("git", "fetch")

        init {
            if (quiet) base.add("--quiet")
            if (all) base.add("--all")
        }

        override val command: List<String> = base
    }

    class PushSetUpstream(val remote: String, val branch: String): GitCommand() {
        override val command: List<String> get() = listOf("git", "push", "-u", remote, branch)
    }

    class AddRemote(val name: String = "origin", private val url: String): GitCommand() {
        override val command: List<String> get() = listOf("git", "remote", "add", name, url)
    }

    class ListRemotes : GitCommand() {
        override val command: List<String> = listOf("git", "remote", "-vv")
    }

    class SymbolicRef(symRefName: String = "HEAD", branch: Branch) : GitCommand() {
        override val command: List<String> = listOf("git", "symbolic-ref", symRefName, branch.fullRefName)
    }

    class ReadTreeHead(val branch: Branch): GitCommand() {
        override val command: List<String> = listOf("git", "read-tree", "-um", "HEAD", branch.fullRefName)
    }

    class UpdateRef(val refName: String, val refLocation: String, val enforceNewRefName: Boolean): GitCommand() {
        override val command: List<String>; get() {
            val commands = mutableListOf("git", "update-ref", refName, refLocation)
            if (enforceNewRefName) {

                /*
                Adding the empty string enforces that we're creating a branch not moving one
                Annoyingly windows and unix implementations of git seem to disagree on what the empty string is
                 */
                when (OperatingSystemUtils.osType) {
                    OSType.Windows -> commands.add("\"\"")
                    else -> commands.add("")
                }
            }

            return commands
        }
    }

    class ForEachBranchRef : GitCommand() {
        companion object {
            const val deliminator = ':'
            const val format = "%(refname)$deliminator%(HEAD)$deliminator%(upstream)$deliminator%(objectname)$deliminator%(objecttype)"
        }

        override val command: List<String> = listOf("git", "for-each-ref", "--format=$format", "refs/heads", "refs/remotes")
    }

    class SearchForRef(refName: String) : GitCommand() {
        override val command: List<String> = listOf("git", "for-each-ref", "--format=${ForEachBranchRef.format}", refName)
    }

    class Log : GitCommand() {
        companion object {
            var deliminator = "@|@"
            //     | RawCommit hash | Parent Hashes | Refs |   Timestamp  | committer email | Subject line of message
            var format = "%H$deliminator%P$deliminator%D$deliminator%ct$deliminator%ce$deliminator%s"
            var formatWithBody = "$format%n%b%n%n%n"
        }

        override val command: List<String> get() = listOf("git", "log", "--branches", "--tags", "--remotes", "--decorate=full", "--format=\"$format\"")
    }

    class LogBetween(private val start: Branch, private val end: Branch): GitCommand() {
        override val command: List<String>
            get() = listOf("git", "log", "${start.commitHash}..${end.commitHash}", "--branches", "--tags", "--remotes", "--decorate=full", "--format=\"${Log.format}\"")
    }

    class FullCommit(val commit: CommitHash) : GitCommand() {
        override val command: List<String> get() = listOf("git", "show", commit, "--date=iso", "--decorate=full", "--format=\"${Log.formatWithBody}\"")
    }

    class StageFile(val file: File): GitCommand() {
        override val command: List<String> get() = listOf("git", "add", file.canonicalPath)
    }

    class UnStageFile(val file: File): GitCommand() {
        override val command: List<String> get() = listOf("git", "reset", file.canonicalPath)
    }

    class StagingAreaDiff: GitCommand() {
        override val command: List<String> = listOf("git", "diff", "--cached")
    }

    class GetUnTrackedNonIgnoredFiles: GitCommand() {
        override val command: List<String> = listOf("git", "ls-files", "--exclude-standard", "--others")
    }

    class UnStagedDiff: GitCommand() {
        override val command: List<String> = listOf("git", "diff")
    }

    class ApplyPatch(patchFile: File) : GitCommand() {
        override val command: List<String> = listOf("git", "apply", "--cached", patchFile.canonicalPath)
    }

    class ApplyStashCommit(stashCommitHash: CommitHash): GitCommand() {
        override val command: List<String> = listOf("git", "stash", "apply", stashCommitHash)
    }

    class CreateStash: GitCommand() {
        override val command: List<String> = listOf("git", "stash", "create")
    }

    class Reset(type: ResetType): GitCommand() {
        override val command: List<String> = listOf("git", "reset", "--${type.raw}")
    }

    class SetLocalBranchUpstream(local: LocalBranch, remote: RemoteBranch): GitCommand() {
        override val command: List<String> = listOf("git", "branch", local.branchName, "-u", remote.branchNameWithRemote)
    }

    class Merge(private val other: Branch, private val noFastForward: Boolean): GitCommand() {
        override val command: List<String> = listOf("git", "merge", if (noFastForward) "--no-ff" else "--ff", ref)
        private val ref: String; get() = if (other is RemoteBranch) other.branchNameWithRemote else other.branchName
    }

    class GetConflictFiles(): GitCommand() {
        override val command: List<String>; get() = listOf("git", "ls-files", "--unmerged", "--full-name")
    }

    class GetBlob(blobHash: String): GitCommand() {
        override val command: List<String> = listOf("git", "cat-file", "blob", blobHash)
    }

    class SetUpstreamTo(localBranch: LocalBranch, remoteBranch: RemoteBranch) : GitCommand() {
        override val command: List<String> = listOf("git", "branch", "--set-upstream-to=${remoteBranch.branchNameWithRemote}", localBranch.branchName)
    }
}
