package org.littlegit.core.util

actual class CPDate: Comparable<CPDate> {
    override fun compareTo(other: CPDate): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual companion object {
        actual fun fromEpochMilis(milis: Long): CPDate {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}