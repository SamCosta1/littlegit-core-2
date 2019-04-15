package org.littlegit.core.integration

import org.littlegit.core.LittleGitCore
import org.littlegit.core.helper.TempFolder
import org.littlegit.core.util.File
import kotlin.test.BeforeTest


expect open class BaseIntegrationTest() {

    val testFolder: TempFolder
    val littleGit: LittleGitCore

    open fun setup()


    fun fileInTestFolder(name: String): File
}

