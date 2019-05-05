package org.littlegit.core.helper

import org.littlegit.core.util.System

actual open class LocalResourceFile actual constructor(localPath: String): ResourceFile("${System.getUserDir()}/src/commonTest/testFiles/$localPath")