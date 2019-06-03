package org.littlegit.core.unit.patches

import org.littlegit.core.helper.LocalResourceFile
import org.littlegit.core.helper.assertEquals
import org.littlegit.core.model.FileDiff
import org.littlegit.core.unit.parser.DiffParser
import org.littlegit.core.util.Paths
import kotlin.test.Test

@Suppress("MemberVisibilityCanBePrivate")
class GeneratePatchTests {
    private val repoPath = Paths.get("/test")
    val multipleFilesMultipleHunks = LocalResourceFile("diffCommits/diff-commit-multiple-files-multiple-hunks.txt")

    @Test
    fun testHunkPatch() {

        val diff = DiffParser.parse(multipleFilesMultipleHunks.content, repoPath)
        val firstFile = diff.fileDiffs.first() as FileDiff.ChangedFile

        val patch = firstFile.hunks[1].generatePatch(firstFile, repoPath)
        val expectedPatch = """
        diff --git a/src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt b/src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt
        --- a/src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt
        +++ b/src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt
        @@ -55,4 +59,6 @@
        ${" "}
                 override val command: List<String> get() = listOf("git", "log", "--all", "--decorate=full", "--format=${"$"}format")
             }
        +
        +
         }
        """.trimIndent().split("\n").toMutableList()
        assertEquals(expectedPatch, patch)
    }


    @Test
    fun testReverseHunkPatch() {

        val diff = DiffParser.parse(multipleFilesMultipleHunks.content, repoPath)
        val firstFile = diff.fileDiffs.first() as FileDiff.ChangedFile

        val patch = firstFile.hunks[1].generateInversePatch(firstFile, repoPath)
        val expectedPatch = """
        diff --git a/src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt b/src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt
        --- a/src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt
        +++ b/src/main/kotlin/org/littlegit/core/commandrunner/GitCommand.kt
        @@ -59,6 +55,4 @@
        ${" "}
                 override val command: List<String> get() = listOf("git", "log", "--all", "--decorate=full", "--format=${"$"}format")
             }
        -
        -
         }
        """.trimIndent().split("\n").toMutableList()
        assertEquals(expectedPatch, patch)
    }


}

