package org.littlegit.core.helper

import org.littlegit.core.util.File


fun assertTrue(msg: String = "", condition: Boolean) {
    kotlin.test.assertTrue(condition, msg)
}

fun assertFalse(msg: String = "", condition: Boolean) {
    kotlin.test.assertFalse(condition, msg)
}

fun  <T> assertEquals(expected: T, actual: T) {
    return kotlin.test.assertEquals(expected, actual)
}

expect class TempFolder {
    val absolutePath: String
    val canonicalPath: String
    val root: File
    fun newFile(filename: String): File

}