package com.horsesgo.storage

import com.horsesgo.game.Difficulty
import com.horsesgo.game.GameRules
import com.horsesgo.game.Position
import com.horsesgo.game.Side
import com.horsesgo.game.testOpening
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MatchSnapshotTest {
    private val rules = GameRules()

    @Test
    fun roundTripPreservesTheDestroyedOriginAndWhiteTurn() {
        val opening = testOpening()
        val match = assertNotNull(rules.play(opening, Position(3, 0)))
        val recovered = assertNotNull(MatchSnapshot.decode(MatchSnapshot.encode(match)))
        assertEquals(Difficulty.Beginner, recovered.difficulty)
        assertEquals(Side.White, recovered.turn)
        assertEquals(match.board.cells(), recovered.board.cells())
    }

    @Test
    fun rejectsBadVersionShapeAndParity() {
        val opening = testOpening()
        val snapshot = MatchSnapshot(1, "beginner", opening.board.cells(), "black")
        assertNull(snapshot.copy(schemaVersion = 7).restore())
        assertNull(snapshot.copy(cells = listOf(listOf(2))).restore())
        assertNull(snapshot.copy(nextTurn = "white").restore())
        assertNull(snapshot.copy(difficulty = "unknown").restore())
        for (cell in listOf(2, 3, 9)) {
            val invalid = snapshot.cells.map { it.toMutableList() }
            invalid[0][0] = cell
            assertNull(snapshot.copy(cells = invalid).restore())
        }
        assertNull(MatchSnapshot.decode("invalid"))
    }

    @Test
    fun preservesSavedKeysDimensionsAndTurnForEveryLevel() {
        val storedSizes = mapOf("beginner" to 4, "intermediate-5x5" to 5, "intermediate" to 6, "advanced" to 8)
        for ((key, size) in storedSizes) {
            val level = Difficulty.entries.first { it.key == key }
            assertEquals(size, level.size)
            val opening = testOpening(level)
            val afterBlack = assertNotNull(rules.play(opening, rules.legalMoves(opening).first()))
            for (match in listOf(opening, afterBlack)) {
                val saved = MatchSnapshot(1, key, match.board.cells(), match.turn.key)
                for (result in listOf(saved.restore(), MatchSnapshot.decode(MatchSnapshot.encode(match)))) {
                    val restored = assertNotNull(result)
                    assertEquals(match.difficulty, restored.difficulty)
                    assertEquals(match.board.cells(), restored.board.cells())
                    assertEquals(match.turn, restored.turn)
                }
            }
        }
    }
}
