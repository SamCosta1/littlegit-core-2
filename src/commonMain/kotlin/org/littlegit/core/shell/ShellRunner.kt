package org.littlegit.core.shell

sealed class ShellResult {
    abstract val lines: List<String>

    data class Success(override val lines: List<String>): ShellResult()
    data class Error(override val lines: List<String>): ShellResult()
}

interface ShellRunner {
    fun runCommand(command: List<String>): ShellResult
}