package org.littlegit.core.integration

import org.littlegit.core.LittleGitCore
import org.littlegit.core.util.File
import kotlin.test.BeforeTest


expect open class BaseIntegrationTest {

    val testFolder: File
    val littleGit: LittleGitCore

    fun setup()


    fun fileInTestFolder(name: String): File
}

