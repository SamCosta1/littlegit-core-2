package org.littlegit.core.integration

import org.littlegit.core.LittleGitCore
import org.littlegit.core.util.File

actual open class BaseIntegrationTest {
    actual val testFolder: File
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual val littleGit: LittleGitCore
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun setup() {
    }

    actual fun fileInTestFolder(name: String): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}