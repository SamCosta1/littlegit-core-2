package org.littlegit.core.integration

import org.littlegit.core.LittleGitCore
import org.littlegit.core.helper.TempFolder
import org.littlegit.core.util.File
import org.littlegit.core.util.Path
import org.littlegit.core.util.Paths
import kotlin.test.BeforeTest

actual open class BaseIntegrationTest {

    private var _littleGit: LittleGitCore? = null


    actual val testFolder: TempFolder
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.


    actual val littleGit: LittleGitCore
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    @BeforeTest
    actual open fun setup() {
        _littleGit = LittleGitCore.Builder()
            .setRepoDirectoryPath(Paths.get(testFolder.canonicalPath))
            .build()
    }
    actual fun fileInTestFolder(name: String): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}