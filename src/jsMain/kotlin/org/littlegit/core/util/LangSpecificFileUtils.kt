package org.littlegit.core.util

actual object LangSpecificFileUtils {
    actual fun createTempFile(
        fileNamePrefix: String,
        fileNameSuffix: String
    ): File? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun writeToFile(message: List<String>, tempFile: File?) {
    }

    actual fun readFromPath(path: Path): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}