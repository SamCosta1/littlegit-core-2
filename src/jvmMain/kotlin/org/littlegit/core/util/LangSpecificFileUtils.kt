package org.littlegit.core.util

actual object LangSpecificFileUtils {
    actual fun createTempFile(
        fileNamePrefix: String,
        fileNameSuffix: String
    ): File? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun writeToFile(message: List<String>, tempFile: File?) {
        tempFile?.javaFile()?.bufferedWriter().use { out ->
            message.forEach {
                out?.write(it)
                out?.newLine()
            }
        }
    }

    actual fun readFromPath(path: Path) = path.toFile().javaFile().readLines()

}