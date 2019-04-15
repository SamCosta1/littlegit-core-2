package org.littlegit.core.model

enum class DiffLineType {
    Addition,
    Deletion,
    Unchanged,
    NoNewLineAtEndOfFile;

    val inverse get() = when(this) {
        Addition -> Deletion
        Deletion -> Addition
        else -> this
    }
}

// When type is unchanged, fromLineNum = toLineNum
data class DiffLine(val type: DiffLineType,
                    val fromLineNum: Int? = null,
                    val toLineNum: Int? = null,
                    val line: String)