package org.littlegit.core.util

expect class Path {
    val fileName: Path

    fun toFile(): File
    fun relativize(other: Path): Path
    fun normalize(): Path
}

expect class File {
    fun toPath(): Path
    fun delete()

    val fileName: String
    val absolutePath: String
    val canonicalPath: String
    fun newFile(s: String)
    fun resolve(other: String): File
    fun exists(): Boolean
}

expect object Paths {
    fun get(canonicalPath: String, vararg key: String): Path
}

expect object System {
    fun lineSeparator(): CharSequence
    fun nanoTime(): Any

}

expect object Files {
    fun exists(path: Path): Boolean

}