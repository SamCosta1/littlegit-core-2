package org.littlegit.core.helper

import org.littlegit.core.util.System

class LocalResourceFile(localPath: String): ResourceFile("${System.getUserDir()}/src/commonTest/testFiles/$localPath")