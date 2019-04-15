package org.littlegit.core.util

object FileUtils {
    fun writeToTempFile(fileNamePrefix: String, fileNameSuffix: String, content: List<String>): File? {

        return try {
            val tempFile = createTempFile(fileNamePrefix, fileNameSuffix)
            writeToFile(content, tempFile)

            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun createTempFile(fileNamePrefix: String, fileNameSuffix: String): File? = LangSpecificFileUtils.createTempFile(fileNamePrefix, fileNameSuffix)

    fun writeToFile(message: List<String>, tempFile: File?) = LangSpecificFileUtils.writeToFile(message, tempFile)


    fun readFromPath(path: Path) = LangSpecificFileUtils.readFromPath(path)
}

expect object LangSpecificFileUtils {
    fun createTempFile(fileNamePrefix: String, fileNameSuffix: String): File?

    fun writeToFile(message: List<String>, tempFile: File?)

    fun readFromPath(path: Path): List<String>
}