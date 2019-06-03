package org.littlegit.core.shell

import org.littlegit.core.util.Path

expect class ShellRunnerLocal(basePath: Path): ShellRunner {
    override fun runCommand(command: List<String>): ShellResult
}