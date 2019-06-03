package org.littlegit.core.shell

import org.littlegit.core.util.Path

actual class ShellRunnerRemote actual constructor(
    user: String,
    host: String,
    repoPath: Path
) : ShellRunner {
    actual override fun runCommand(command: List<String>): ShellResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}