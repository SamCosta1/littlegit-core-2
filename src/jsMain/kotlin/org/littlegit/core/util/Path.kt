package org.littlegit.core.util

actual class Path {


    actual fun toFile(): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun relativize(other: Path): Path {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun normalize(): Path {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual object Paths {
    actual fun get(canonicalPath: String, vararg key: String): Path {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual object System {
    actual fun lineSeparator(): CharSequence {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun nanoTime(): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual class File {
    actual fun toPath(): Path {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual val canonicalPath: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun delete() {
    }

    actual val absolutePath: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}

actual object Files {
    actual fun exists(path: Path): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}