package org.littlegit.core.util

typealias JavaPath = java.nio.file.Path

actual class Path(private val _path: JavaPath) {
    actual fun relativize(other: Path): Path = Path(_path.relativize(other._path))

    actual fun toFile(): File = File(_path.toFile())

    actual fun normalize(): Path = Path(_path.normalize())

    fun javaPath(): JavaPath = _path

}

typealias JavaFile = java.io.File;
actual class File(private val _file: JavaFile) {

    actual fun toPath(): Path = Path(_file.toPath())

    actual val absolutePath: String
        get() = _file.absolutePath

    actual val canonicalPath: String
        get() = _file.canonicalPath

    fun javaFile(): java.io.File = java.io.File(canonicalPath)

    actual fun delete() {
        _file.delete();
    }
}

typealias JavaPaths = java.nio.file.Paths
actual object Paths {
    actual fun get(canonicalPath: String, vararg key: String): Path = Path(JavaPaths.get(canonicalPath, *key))

}

typealias JavaSystem = java.lang.System;
actual object System {
    actual fun lineSeparator(): CharSequence = JavaSystem.lineSeparator()

    actual fun nanoTime(): Any = JavaSystem.nanoTime();

}

typealias JavaFiles = java.nio.file.Files
actual object Files {
    actual fun exists(path: Path): Boolean = JavaFiles.exists(path.javaPath())

}