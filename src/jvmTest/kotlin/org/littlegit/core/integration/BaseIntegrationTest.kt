package org.littlegit.core.integration

import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.littlegit.core.LittleGitCore
import org.littlegit.core.util.File
import org.littlegit.core.util.Path
import org.littlegit.core.util.Paths

actual open class BaseIntegrationTest {

    private var _littleGit: LittleGitCore? = null

    @Rule
    @JvmField
    var _testFolder = TemporaryFolder()

    actual val testFolder: File; get() =  File(_testFolder.root)

    actual val littleGit: LittleGitCore
        get() = _littleGit!!

    @Before
    actual fun setup() {
        _littleGit = LittleGitCore.Builder()
            .setRepoDirectoryPath(Path(_testFolder.root.toPath()))
            .build()
    }

    actual fun fileInTestFolder(name: String): File = Paths.get(_testFolder.root.canonicalPath, name).toFile()
}