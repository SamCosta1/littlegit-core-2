package org.littlegit.core.parser

import org.littlegit.core.commandrunner.GitCommand
import org.littlegit.core.model.Branch
import org.littlegit.core.model.RemoteBranch

object BranchesParser {

    fun parse(raw: List<String>, remotes: List<Remote>?): List<Branch> {

        // RefName -> RemoteBranch object
        val remoteBranches = mutableMapOf<String, RemoteBranch>()

        // Sorting ensures the remote branches are parsed first and can be referenced by the local branches
        val sortedRaw = raw.sortedDescending()

        val branches: List<Branch?> = sortedRaw.map {

            val split = it.split(GitCommand.ForEachBranchRef.deliminator)
            val refName = split[0]
            val isHead = split[1].isNotBlank() && split[1].trim() == "*"
            val upstream = split[2]
            val objectName = split[3]
            val objectType = split[4]

            val branch = Branch.createFrom(refName, isHead, objectName, objectType, remoteBranches[upstream], remotes)

            if (branch is RemoteBranch) {
                remoteBranches[branch.fullRefName] = branch
            }

            branch
        }

        return branches.filterNotNull()
    }
}