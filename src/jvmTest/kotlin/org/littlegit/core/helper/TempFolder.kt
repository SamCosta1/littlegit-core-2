package org.littlegit.core.helper

import org.junit.rules.TemporaryFolder
import org.littlegit.core.util.File

actual class TempFolder(private val folder: TemporaryFolder) {
    actual fun newFile(filename: String) = File(folder.newFile(filename))
    actual val absolutePath: String
        get() = folder.root.absolutePath
    actual val root: File
        get() = File(folder.root)
    actual val canonicalPath: String
        get() = folder.root.canonicalPath
}