package org.littlegit.core.util



fun List<String>.joinWithNewLines(): String {
    return this.joinToString(System.lineSeparator())
}

fun List<String>.joinWithSpace(): String {
    return this.joinToString(" ")
}

object ListUtils {
    fun <T>findAllIndexesWhere(list: List<T>, predicate: (T) -> Boolean): List<Int> {
        val indexes = mutableListOf<Int>()

        list.forEachIndexed { index, item ->
            if (predicate(item)) {
                indexes.add(index)
            }
        }

        return indexes
    }

    fun <T>firstOccurrenceAfterIndex(list: List<T>, index: Int, predicate: (T) -> Boolean): Int {
        for (i in index until list.size)
            if (predicate(list[i]))
                return i

        return -1;
    }
}