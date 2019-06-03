package org.littlegit.core.helper

import org.littlegit.core.commandrunner.CommitHash
import org.littlegit.core.model.ResetType
import org.littlegit.core.util.File
import org.littlegit.core.util.Paths


class TestCommandHelper(private val file: TempFolder) {

    companion object {
        const val DEFAULT_EMAIL =  "frodo.baggins@shire.com"
        const val DEFAULT_NAME = "Frodo Baggins"
    }

    fun initConfig(name: String = DEFAULT_NAME, email: String = DEFAULT_EMAIL): TestCommandHelper {
        execute("git config user.name $name")
        execute("git config user.email $email")

        return this;
    }

    fun init(): TestCommandHelper {
        execute("git init")
        return this
    }

    fun commit(message: String = "test-message"): TestCommandHelper {
        execute("git commit -m $message")
        return this
    }

    fun addAll(): TestCommandHelper {
        execute("git add .")
        return this
    }

    fun addRemote(remote: String, url: String = "www.remote.com"): TestCommandHelper {
        execute("git remote add $remote $url")
        return this
    }

    fun createRemoteBranch(branchName: String, remote: String): TestCommandHelper {
        execute(("git update-ref refs/remotes/$remote/$branchName HEAD"))
        return this
    }

    fun getLastCommitMessage(): String {
        return execute("git log -1 --pretty=%B").first()
    }

    fun getLastCommitHash(): String {
        return execute("git log -1 --pretty=%H").first()
    }

    fun getLastCommitTimeStamp(): String {
        return execute("git log -1 --date=iso --pretty=%ct").first()
    }

    fun run(command: String): List<String> {
        return execute(command)
    }

    fun reset(toHash: CommitHash, mode: ResetType): TestCommandHelper {
        execute("git reset --${mode.raw} $toHash")
        return this
    }

    fun writeToFile(file: String, content: String): TestCommandHelper {
        writeToFileAndReturnIt(file, content)
        return this
    }

    fun writeToFile(file: String, content: List<String>): TestCommandHelper {
        writeToFileAndReturnIt(file, content)
        return this
    }

    fun writeToFileAndReturnIt(file: String, content: String): File  = writeToFileAndReturnIt(file, listOf(content))

    fun writeToFileAndReturnIt(file: String, content: List<String>): File {

        val path = Paths.get(this.file.absolutePath, file)

        TestExecuteHelper.writeToFile(path, content)
        return path.toFile()
    }

    fun isStaged(file: File): Boolean {
        val stagedFiles = execute("git diff --cached --name-only")

        return stagedFiles.any { file.absolutePath.endsWith(it) }
    }

    private fun execute(command: String): List<String> = TestExecuteHelper.execute(file.root, command)


    fun branchAndCheckout(branch: String): TestCommandHelper {
        execute("git checkout -b $branch")
        return this
    }

    fun deleteBranch(branchName: String) {
        execute("git branch -d $branchName")
    }

    fun checkout(branch: String): TestCommandHelper {
        execute("git checkout $branch")
        return this
    }

    fun setupRemoteTracking(remote: String, branchName: String): TestCommandHelper {
        execute("git branch -u $remote/$branchName")
        return this
    }
}
