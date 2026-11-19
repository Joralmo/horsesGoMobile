package com.horsesgo.storage

import com.horsesgo.game.Difficulty
import com.horsesgo.game.GameRules
import com.horsesgo.game.Position
import com.horsesgo.game.Side
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MatchSnapshotTest {
    private val rules = GameRules()

    @Test
    fun roundTripPreservesTheDestroyedOriginAndWhiteTurn() {
        val opening = rules.open(Difficulty.Beginner)
        val match = assertNotNull(rules.play(opening, Position(3, 0)))
        val recovered = assertNotNull(MatchSnapshot.decode(MatchSnapshot.encode(match)))
        assertEquals(Difficulty.Beginner, recovered.difficulty)
        assertEquals(Side.White, recovered.turn)
        assertEquals(match.board.cells(), recovered.board.cells())
    }

    @Test
    fun rejectsBadVersionShapeAndParity() {
        val opening = rules.open(Difficulty.Beginner)
        val snapshot = MatchSnapshot(1, "beginner", opening.board.cells(), "black")
        assertNull(snapshot.copy(schemaVersion = 7).restore())
        assertNull(snapshot.copy(cells = listOf(listOf(2))).restore())
        assertNull(snapshot.copy(nextTurn = "white").restore())
        assertNull(MatchSnapshot.decode("invalid"))
    }
}
