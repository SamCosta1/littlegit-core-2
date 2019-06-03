package org.littlegit.core.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

actual class CPDate(private val dateTime: OffsetDateTime): Comparable<CPDate> {

    override fun compareTo(other: CPDate): Int =  dateTime.compareTo(other.dateTime)

    actual companion object {
        actual fun fromEpochMilis(milis: Long): CPDate =
            CPDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(milis), ZoneId.systemDefault()))

    }

    override fun equals(other: Any?): Boolean {
        return other is CPDate && this.dateTime == other.dateTime
    }
}