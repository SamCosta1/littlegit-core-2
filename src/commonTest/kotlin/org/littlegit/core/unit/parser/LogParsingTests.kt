package org.littlegit.core.unit.parser

import org.littlegit.core.model.RawCommit
import org.littlegit.core.commandrunner.InvalidCommitException
import org.littlegit.core.helper.assertEquals
import org.littlegit.core.helper.assertTrue
import org.littlegit.core.util.CPDate
import kotlin.test.Test
import kotlin.test.assertFailsWith


class LogParsingTests {

    companion object {
        private val EMPTY_EMAIL = listOf("\"03e6c7df90e56aa5d721a14f8e8363397f17cc28@|@@|@@|@1525272249@|@@|@master commit 2\"")
        private val HEAD_COMMIT = listOf("428d62f5b7244454edfbf94fd99dee0972f130e8@|@159a78f59031cb814f83148d6f6aaebd2a186a22 bb7058bb7e9214f2497336938ce2e4a9c43ca96d@|@HEAD -> refs/heads/master@|@1525272325@|@samdc@apadmi.com@|@Merge branch 'feature'")
        private val MULTI_PARENT = listOf("159a78f59031cb814f83148d6f6aaebd2a186a22@|@03e6c7df90e56aa5d721a14f8e8363397f17cc28 bb7058bb7e9214f2497336938ce2e4a9c43ca96d@|@@|@1525272270@|@samdc@apadmi.com@|@master commit 3")
        private val NO_PARENT = listOf("03e6c7df90e56aa5d721a14f8e8363397f17cc28@|@@|@@|@1525272249@|@samdc@apadmi.com@|@master commit 2")
        private val SPECIAL_CHAR_EMAIL = listOf("159a78f59031cb814f83148d6f6aaebd2a186a22@|@03e6c7df90e56aa5d721a14f8e8363397f17cc28@|@@|@1525272270@|@|samdc|@|apadmi.com|@|@master commit 3")
        private val SPECIAL_CHAR_SUBJECT = listOf("bb7058bb7e9214f2497336938ce2e4a9c43ca96d@|@159a78f59031cb814f83148d6f6aaebd2a186a22@|@@|@1525272312@|@test@email.com@|@feature comm!@£\$%^&*()_+)@£\$%^&*|||()(*&^%''\"\$£@|@@|@|it")
        private val EMPTY_SUBJECT = listOf("bb7058bb7e9214f2497336938ce2e4a9c43ca96d@|@159a78f59031cb814f83148d6f6aaebd2a186a22@|@@|@1525272312@|@test@email.com@|@")
        private val MISSING_HASH = listOf("@|@159a78f59031cb814f83148d6f6aaebd2a186a22@|@@|@1525272312@|@test@email.com@|@feature commit")
        private val GARBAGE = listOf("ergegetohjt40t843635^£$%.,ergERgergn[erg  eurhger")
    }

    @Test
    fun missingCommitHashTest() {
        assertFailsWith(InvalidCommitException::class) {
            LogParser.parse(MISSING_HASH)
        }
    }

    @Test
    fun garbageTest() {
        assertFailsWith(InvalidCommitException::class) {
            LogParser.parse(GARBAGE)
        }
    }

    @Test
    fun emptySubjectTest() {
        val parsed = LogParser.parse(EMPTY_SUBJECT)

        val correct = listOf(RawCommit("bb7058bb7e9214f2497336938ce2e4a9c43ca96d",
                emptyList(),
                listOf("159a78f59031cb814f83148d6f6aaebd2a186a22"),
                CPDate.fromEpochMilis(1525272312000),
                "test@email.com",
                "", false))



        parsed.forEachIndexed { index, rawCommit ->
            assertTrue("Commit is as expected", rawCommit == correct[index])
        }
    }

    @Test fun specialCharacterEmailTest() {
        val parsed = LogParser.parse(SPECIAL_CHAR_EMAIL)

        val correct = listOf(RawCommit("159a78f59031cb814f83148d6f6aaebd2a186a22",
                                            emptyList(),
                                            listOf("03e6c7df90e56aa5d721a14f8e8363397f17cc28"),
                                            CPDate.fromEpochMilis(1525272270000),
                                            "|samdc|@|apadmi.com|",
                                            "master commit 3", false))

        parsed.forEachIndexed { index, rawCommit ->
            assertTrue("Commit is as expected", rawCommit == correct[index])
        }
    }

    @Test
    fun specialCharacterCommitSubjectTest() {
        val parsed = LogParser.parse(SPECIAL_CHAR_SUBJECT)

        val correct = listOf(RawCommit("bb7058bb7e9214f2497336938ce2e4a9c43ca96d",
                                                emptyList(),
                                                listOf("159a78f59031cb814f83148d6f6aaebd2a186a22"),
                                                CPDate.fromEpochMilis(1525272312000),
                                                "test@email.com",
                                                "feature comm!@£\$%^&*()_+)@£\$%^&*|||()(*&^%''\"\$£@|@@|@|it", false))



        parsed.forEachIndexed { index, rawCommit ->
            assertTrue("Commit is as expected", rawCommit == correct[index])
        }
    }

    @Test fun noParentTest() {
        val parsed = LogParser.parse(NO_PARENT)

        val correct = listOf(RawCommit("03e6c7df90e56aa5d721a14f8e8363397f17cc28",
                                emptyList(),
                                emptyList(),
                                CPDate.fromEpochMilis(1525272249000),
                                "samdc@apadmi.com",
                                "master commit 2", false)
        )

        parsed.forEachIndexed { index, rawCommit ->
            assertEquals("Commit is as expected", rawCommit, correct[index])
        }
    }

    @Test
    fun headCommitTest() {
        val parsed = LogParser.parse(HEAD_COMMIT)
        val correct = listOf(RawCommit("428d62f5b7244454edfbf94fd99dee0972f130e8",
                                listOf("refs/heads/master"),
                                listOf("159a78f59031cb814f83148d6f6aaebd2a186a22", "bb7058bb7e9214f2497336938ce2e4a9c43ca96d"),
                                CPDate.fromEpochMilis(1525272325000),
                                "samdc@apadmi.com",
                                "Merge branch 'feature'", true))


        parsed.forEachIndexed { index, rawCommit ->
            assertEquals("Commit is as expected", rawCommit, correct[index])
        }
    }

    @Test
    fun multipleParentCommitTest() {
        val parsed = LogParser.parse(MULTI_PARENT)

        val correct = listOf(RawCommit("159a78f59031cb814f83148d6f6aaebd2a186a22",
                                        emptyList(),
                                        listOf("03e6c7df90e56aa5d721a14f8e8363397f17cc28", "bb7058bb7e9214f2497336938ce2e4a9c43ca96d"),
                                        CPDate.fromEpochMilis(1525272270000),
                                        "samdc@apadmi.com",
                                        "master commit 3", false))

        parsed.forEachIndexed { index, rawCommit ->
            assertTrue("Commit is as expected", rawCommit == correct[index])
        }
    }


    @Test
    fun emptyEmailTest() {
        val parsed = LogParser.parse(EMPTY_EMAIL)

        val correctCommits = listOf(
                RawCommit("03e6c7df90e56aa5d721a14f8e8363397f17cc28",
                        emptyList(),
                        emptyList(),
                        CPDate.fromEpochMilis(1525272249000),
                        "",
                        "master commit 2", false)
        )

        parsed.forEachIndexed { index, rawCommit ->
            assertEquals("Commit is as expected", rawCommit, correctCommits[index])
        }
    }

    @Test
    fun testEmptyList() {
        assertTrue(LogParser.parse(emptyList()) == emptyList<RawCommit>())
    }
}
