package org.littlegit.core.shell


import org.littlegit.core.util.Path

expect class ShellRunnerRemote(user: String, host: String, repoPath: Path) : ShellRunner {

    override fun runCommand(command: List<String>): ShellResult

}