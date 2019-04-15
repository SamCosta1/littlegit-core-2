package org.littlegit.core.helper

import org.littlegit.core.model.FullCommit
import org.littlegit.core.model.RawCommit

object AssertHelper {

    fun assertRawCommit(expected: RawCommit, actual: RawCommit) {
        assertEquals(expected.commitSubject, actual.commitSubject)
        assertEquals(expected.isHead, actual.isHead)
        assertEquals(expected.date, actual.date)
        assertEquals(expected.committerEmail, actual.committerEmail)
        assertEquals(expected.refs, actual.refs)
        assertEquals(expected.parentHashes, actual.parentHashes)
        assertEquals(expected.hash, actual.hash)
    }

    // Full commit parsing and diff parsing are usually separate tests, so allow for ignoring the diff to keep things clean
    fun assertFullCommit(expected: FullCommit, actual: FullCommit, ignoreDiff: Boolean = false) {
        assertEquals(expected.commitBody, actual.commitBody)
        if (!ignoreDiff) {
            assertEquals(expected.diff, actual.diff)
        }
        assertRawCommit(expected, actual)
    }
}