package org.littlegit.core.commandrunner

class InvalidCommitException(override var message: String = "Commit is malformed", raw: String): Exception("$message: $raw")