package org.littlegit.core.util

expect class CPDate: Comparable<CPDate> {

    companion object {
        fun fromEpochMilis(milis: Long): CPDate
    }
}