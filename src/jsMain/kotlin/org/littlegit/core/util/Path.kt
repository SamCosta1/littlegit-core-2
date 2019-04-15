package org.littlegit.core.util

actual class Path {
    actual fun relativize(toPath: Path): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun toFile(): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual object Paths {
    actual fun get(canonicalPath: Any, key: String): Path {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual object System {
    actual fun lineSeparator(): CharSequence {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual class File {
    actual fun toPath(): Path {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual val canonicalPath: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}