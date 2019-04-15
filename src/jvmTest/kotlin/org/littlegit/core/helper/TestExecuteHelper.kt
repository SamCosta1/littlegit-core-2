package org.littlegit.core.helper

import org.littlegit.core.util.File
import org.littlegit.core.util.Path
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files

actual object TestExecuteHelper {
    actual fun execute(file: File, command: String): List<String> {
        val output = mutableListOf<String>()

        val p: Process
        try {
            p = Runtime.getRuntime().exec(command, emptyArray<String>(), file.javaFile())
            p.waitFor()
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val error = BufferedReader(InputStreamReader(p.errorStream))

            var line: String?
            do {
                line = error.readLine()
                if (line != null && line.isNotBlank()) {
                    println(line)
                }
            } while (line != null)

            do {
                line = reader.readLine()

                if (line != null && line.isNotBlank()) {
                    output.add(line)
                }

            } while (line != null)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return output
    }

    actual fun writeToFile(path: Path, content: List<String>) {
        Files.write(path.javaPath(), content, Charset.forName("UTF-8"))
    }
}