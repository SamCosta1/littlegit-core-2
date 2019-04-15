package org.littlegit.core.model

enum class ObjectType {
    Blob,
    Tree,
    Commit,
    Unknown,
    Tag;

    companion object {
        fun fromRaw(raw: String): ObjectType {
            when (raw.trim()) {
                "blob"   -> return Blob
                "tree"   -> return Tree
                "commit" -> return Commit
                "tag"    -> return Tag
            }

            return Unknown
        }
    }
}