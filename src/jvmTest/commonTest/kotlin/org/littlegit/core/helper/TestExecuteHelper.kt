package org.littlegit.core.helper

import org.littlegit.core.util.File
import org.littlegit.core.util.Path

expect object TestExecuteHelper {

    fun execute(file: File, command: String): List<String>
    fun writeToFile(path: Path, content: List<String>)
}