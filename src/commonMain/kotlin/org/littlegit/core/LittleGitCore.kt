package org.littlegit.core

import org.littlegit.core.modifier.ConfigModifier
import org.littlegit.core.modifier.RepoModifier
import org.littlegit.core.reader.RepoReader
import org.littlegit.core.commandrunner.GitCommandRunner
import org.littlegit.core.shell.ShellRunner
import org.littlegit.core.shell.ShellRunnerLocal
import org.littlegit.core.shell.ShellRunnerRemote
import org.littlegit.core.util.Path

class LittleGitCore private constructor(shellRunner: ShellRunner, repoPath: Path) {

     class Builder {
        private var user: String? = null
        private var host: String? = null
        private var repoPath: Path? = null

        fun setRemoteUser(usr: String): Builder {
            user = usr
            return this
        }

        fun setRemoteHost(hst: String): Builder {
            host = hst
            return this
        }

        fun setRepoDirectoryPath(path: Path): Builder {
            repoPath = path
            return this
        }

        fun build(): LittleGitCore {
            if (host.isNullOrBlank() && !user.isNullOrBlank() ||
                    !host.isNullOrBlank() && user.isNullOrBlank()) {
                throw IllegalStateException("To manipulate a remote repo, supply a user and host")
            }

            if (repoPath == null) throw IllegalStateException("You must specify a directory for the repo")

            return if (!host.isNullOrBlank()) {
                LittleGitCore(ShellRunnerRemote(user!!, host!!, repoPath!!), repoPath!!)
            } else {
                LittleGitCore(ShellRunnerLocal(repoPath!!), repoPath!!)
            }
        }
    }

    private val commandRunner = GitCommandRunner(shellRunner, repoPath)

    val repoReader = RepoReader(commandRunner, repoPath)
    val repoModifier = RepoModifier(commandRunner, repoReader, repoPath)
    val configModifier = ConfigModifier(commandRunner)
}