package org.littlegit.core.parser

import org.littlegit.core.model.GitError
import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.shell.ShellResult

object GitResultParser {

    fun parseShellResult(shellResult: ShellResult): GitResult {
        return when (shellResult) {
            is ShellResult.Error -> parseErrorResult(shellResult)
            is ShellResult.Success -> parseSuccessResult(shellResult)
        }
    }

    private fun parseErrorResult(errResult: ShellResult.Error): GitResult.Error {
        val lines = cleanLines(errResult.lines.toMutableList())

        if (lines.isEmpty()) {
            return GitResult.Error(GitError.Unknown(lines))
        }

        if (lines.first().startsWith("fatal: Not a git repository", ignoreCase = true)) {
            return GitResult.Error(GitError.NotARepo(lines))
        }

        if (lines.first().startsWith("error: ") && lines.first().endsWith("not uptodate. Cannot merge.")
                || lines.first().startsWith("error: Your local changes to the following files would be overwritten by checkout", ignoreCase = true)) {
            return GitResult.Error(GitError.LocalChangesWouldBeOverwritten(lines))
        }

        if (lines.first().startsWith("fatal: No remote repository specified")
                || lines.first().startsWith("fatal: No configured push destination")) {
            return GitResult.Error(GitError.NoRemote(lines))
        }

        if (lines.first().startsWith("fatal: The current branch") && lines.first().endsWith("has no upstream branch.")) {
            return GitResult.Error(GitError.NoUpstreamBranch(lines))
        }

        if (lines.first().startsWith("fatal: pathspec") && lines.first().endsWith("did not match any files") ||
            lines.first().startsWith("fatal: ambiguous argument") && lines.first().endsWith("unknown revision or path not in the working tree.")) {
            return GitResult.Error(GitError.PathSpecMatchesNoFiles(lines))
        }

        if (lines.first().startsWith("fatal: unable to access") && lines.first().contains("Could not resolve host")) {
            return GitResult.Error(GitError.CannotReadRemote(lines))
        }

        if (lines.size > 1 && lines[1].startsWith("fatal: Could not read from remote repository.")) {
            return GitResult.Error(GitError.CannotReadRemote(lines))
        }
        
        if (lines.first().startsWith("fatal: Path ") && lines.first().endsWith("exists on disk, but not in the index.")) {
            return GitResult.Error(GitError.FileNotInIndex(lines, true))
        }

        if (lines.first().startsWith("fatal: Path ") && lines.first().endsWith("does not exist (neither on disk nor in the index).")) {
            return GitResult.Error(GitError.FileNotInIndex(lines, false))
        }

        if ((lines.first().startsWith("fatal:") && lines.first().endsWith("is not a valid remote name"))
                || lines.first().startsWith("usage: git remote add [<options>] <name> <url>")) {
            return GitResult.Error(GitError.InvalidRemoteInfo(lines))
        }

        if (lines.first().startsWith("fatal: remote") && lines.first().endsWith("already exists.")) {
            return GitResult.Error(GitError.RemoteAlreadyExists(lines))
        }

        if (lines.first().startsWith("fatal: cannot lock ref")) {
            return GitResult.Error(GitError.CannotLockRef(lines))
        }

        if (lines.last().startsWith("error: ") && lines.last().endsWith("patch does not apply")) {
            return GitResult.Error(GitError.PatchDoesNotApply(lines))
        }

        if (lines.first().startsWith("error: corrupt patch at line")) {
            return GitResult.Error(GitError.CorruptPatch(lines))
        }

        if (lines.first().startsWith("fatal: HEAD: not a valid SHA1")) {
            return GitResult.Error(GitError.InvalidHead(lines))
        }

        if (lines.first().startsWith("fatal: update_ref failed for ref")) {
            if (lines.first().endsWith("reference already exists")) {
                return GitResult.Error(GitError.ReferenceAlreadyExists(lines))
            } else if (lines.first().contains("refusing to update ref with bad name")) {
                return GitResult.Error(GitError.InvalidRefName(lines))
            }
        }

        return GitResult.Error(GitError.Unknown(lines))
    }

    private fun parseSuccessResult(successResult: ShellResult.Success): GitResult {
        val lines = successResult.lines.toMutableList()

        if (lines.isNotEmpty()) {
            val lastLine = lines.last()
            if (lastLine.startsWith("nothing added to commit") || lastLine.startsWith("nothing to commit")) {
                return GitResult.Error(GitError.NothingToCommit(lines))
            }
        }

        return GitResult.Success(lines)
    }

    private fun cleanLines(lines: MutableList<String>): List<String> {
        return lines.dropWhile { it.isBlank() }
    }
}
