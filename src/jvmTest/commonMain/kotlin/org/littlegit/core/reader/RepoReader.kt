package org.littlegit.core.reader
import org.littlegit.core.LittleGitCommandResult
import org.littlegit.core.commandrunner.*
import org.littlegit.core.model.*
import org.littlegit.core.unit.parser.*
import org.littlegit.core.util.*

class RepoReader(private val commandRunner: GitCommandRunner,
                 private val repoPath: Path) {

    fun getRemotes(): LittleGitCommandResult<List<Remote>> {
        val resultProcessor = { result: GitResult.Success -> RemoteParser.parse(result.lines) }
        return commandRunner.runCommand(command = GitCommand.ListRemotes(), resultProcessor = resultProcessor)
    }

    fun getCommitList(): LittleGitCommandResult<List<RawCommit>> {
        val resultProcessor = { result: GitResult.Success -> LogParser.parse(result.lines) }

        return commandRunner.runCommand(command = GitCommand.Log(), resultProcessor = resultProcessor)
    }

    fun getLogBetween(branch1: Branch, branch2: Branch): LittleGitCommandResult<List<RawCommit>> {
        val resultProcessor = { result: GitResult.Success -> LogParser.parse(result.lines) }

        return commandRunner.runCommand(command = GitCommand.LogBetween(branch1, branch2), resultProcessor = resultProcessor)
    }

    fun isInitialized(): LittleGitCommandResult<Boolean> {
        val resultProcessor = { result: GitResult.Success -> result.lines[0] == "true" }

        val res = commandRunner.runCommand(command = GitCommand.IsInitialized(), resultProcessor = resultProcessor)

        return if (res.result is GitResult.Error && res.result.err is GitError.NotARepo) {
            LittleGitCommandResult(false, res.result)
        } else {
            LittleGitCommandResult(res.data, res.result)
        }
    }

    fun getFile(ref: String = "", file: File): LittleGitCommandResult<LittleGitFile> {
        val resultProcessor = { result: GitResult.Success -> LittleGitFile(result.lines, file) }

        val relativePath = commandRunner.pathRelativeToRepo(file)
        return commandRunner.runCommand(command = GitCommand.ShowFile(ref, relativePath), resultProcessor = resultProcessor)
    }

    fun getFullCommit(commit: RawCommit) = getFullCommit(commit.hash)

    fun getFullCommit(commit: CommitHash): LittleGitCommandResult<FullCommit> {
        val resultProcessor = { result: GitResult.Success ->
            FullCommitParser.parse(result.lines, repoPath)
        }

        return commandRunner.runCommand(command = GitCommand.FullCommit(commit), resultProcessor = resultProcessor)
    }

    fun getStagingAreaDiff(): LittleGitCommandResult<Diff> {
        val resultProcessor = { result: GitResult.Success ->
            DiffParser.parse(result.lines, repoPath)
        }

        return commandRunner.runCommand(command = GitCommand.StagingAreaDiff(), resultProcessor = resultProcessor)
    }

    fun getUnStagedChanges(): LittleGitCommandResult<UnstagedChanges> {
        val unTrackedFilesProcessor = { result: GitResult.Success ->
            // Each line is a path relative to the repo
            result.lines.map {
                val path = Paths.get(repoPath.toString(), it)
                LittleGitFile(FileUtils.readFromPath(path) , path.toFile())
            }
        }

        val diffProcessor = { result: GitResult.Success ->
            DiffParser.parse(result.lines, repoPath)
        }

        val unTrackedFilesResult = commandRunner.runCommand(command = GitCommand.GetUnTrackedNonIgnoredFiles(), resultProcessor = unTrackedFilesProcessor)
        val trackedFilesDiffResult = commandRunner.runCommand(command = GitCommand.UnStagedDiff(), resultProcessor = diffProcessor)

        if (trackedFilesDiffResult.result is GitResult.Error) {
            return LittleGitCommandResult(null, trackedFilesDiffResult.result)
        }

        if (unTrackedFilesResult.result is GitResult.Error) {
            return LittleGitCommandResult(null, unTrackedFilesResult.result)
        }

        val unTrackedFiles = unTrackedFilesResult.data
        return LittleGitCommandResult(UnstagedChanges(trackedFilesDiffResult.data!!, unTrackedFiles!!), unTrackedFilesResult.result)
    }

    fun getBranches(): LittleGitCommandResult<List<Branch>> {
        val remotesResult = getRemotes()

        if (remotesResult.result is GitResult.Error) {
            return LittleGitCommandResult.buildError(remotesResult.result.err)
        }

        val resultProcessor = { result: GitResult.Success ->
            BranchesParser.parse(result.lines, remotesResult.data)
        }

        return commandRunner.runCommand(command = GitCommand.ForEachBranchRef(), resultProcessor = resultProcessor)
    }

    fun getBranch(branch: Branch): LittleGitCommandResult<Branch?> = getBranch(branch.fullRefName)

    fun getBranch(fullRefName: String): LittleGitCommandResult<Branch?> {
        val remotesResult = getRemotes()

        if (remotesResult.result is GitResult.Error) {
            return LittleGitCommandResult.buildError(remotesResult.result.err)
        }

        val resultProcessor = { result: GitResult.Success ->
            BranchesParser.parse(result.lines, remotesResult.data).firstOrNull()
        }

        return commandRunner.runCommand(command = GitCommand.SearchForRef(fullRefName), resultProcessor = resultProcessor)
    }

    fun getConflictFiles(): LittleGitCommandResult<MergeResult> {
        val resultProcessor = { result: GitResult.Success ->
            ConflictFilesParser.parse(repoPath, result.lines)
        }

        return commandRunner.runCommand(command = GitCommand.GetConflictFiles(), resultProcessor = resultProcessor)
    }

    fun getConflictFileContent(conflictFile: ConflictFile, conflictFileType: ConflictFileType): LittleGitCommandResult<LittleGitFile> {
        val resultParser = { result: GitResult.Success ->
            LittleGitFile(result.lines, conflictFile.filePath.toFile())
        }

        return commandRunner.runCommand(command = GitCommand.GetBlob(conflictFileType.getHash(conflictFile)), resultProcessor = resultParser)
    }
}
