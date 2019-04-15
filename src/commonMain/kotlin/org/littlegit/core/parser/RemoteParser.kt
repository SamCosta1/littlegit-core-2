package org.littlegit.core.parser

data class Remote(val remoteName: String, var pushUrl: String = "", var fetchUrl: String = "")

class InvalidRemote(override var message: String = "Remote is malformed", raw: String): Exception("$message: $raw")

object RemoteParser {

    fun parse(lines: List<String>): List<Remote> {
        val remotes = HashMap<String, Remote>()

        lines.forEach { line ->
            if (line.isNotBlank()) {

                val fields = line.replace('\t', ' ').split(" ")

                if (fields.size != 3) {
                    throw InvalidRemote(raw = line)
                }

                val name = fields[0]
                val url = fields[1]
                val isPush = fields[2].trim() == "(push)"

                val remote = remotes.getOrDefault(name, Remote(name))

                if (isPush) {
                    remote.pushUrl = url
                } else {
                    remote.fetchUrl = url
                }

                remotes[name] = remote
            }

        }
        return remotes.values.toList()
    }
}