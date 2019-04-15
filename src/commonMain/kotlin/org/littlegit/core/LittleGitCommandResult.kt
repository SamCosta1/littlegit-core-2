package org.littlegit.core

import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.model.GitError

data class LittleGitCommandResult<out T>(val data: T?, val result: GitResult) {
    companion object {
        fun buildError(err: GitError) = LittleGitCommandResult(null, GitResult.Error(err))
    }

    val isError: Boolean; get() = result.isError
}