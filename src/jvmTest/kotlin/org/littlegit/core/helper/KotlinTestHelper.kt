package org.littlegit.core.helper

fun assertTrue(msg: String = "", condition: Boolean) {
    kotlin.test.assertTrue(condition, msg)
}

fun assertFalse(msg: String = "", condition: Boolean) {
    kotlin.test.assertFalse(condition, msg)
}