package com.horsesgo.ai

import com.horsesgo.game.GameRules
import com.horsesgo.game.Move
import com.horsesgo.game.Difficulty
import com.horsesgo.game.Position
import com.horsesgo.game.testOpening
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MinimaxSearchTest {
    private val rules = GameRules()

    @Test
    fun reachesTheConfiguredDepthWithoutChangingAnyInputBoard() = runTest {
        val search = MinimaxSearch(rules)
        for (state in depthSensitiveMatches) {
            val depth = state.difficulty.searchDepth
            val before = state.board.cells()
            val expected = exhaustiveBestMoves(state, rules, depth)
            val tooShallow = exhaustiveBestMoves(state, rules, depth - 1)

            assertTrue(expected.intersect(tooShallow).isEmpty())
            assertTrue(assertNotNull(search.chooseMove(state)) in expected)
            assertEquals(before, state.board.cells())
        }
    }

    @Test
    fun terminalDefeatAtTheCutoffTakesPrecedenceOverMobility() = runTest {
        val match = matchFromRows(Difficulty.Beginner, "B.##", "W..#", ".#..", "#...")
        val move = MinimaxSearch(rules).chooseMove(match)
        assertEquals(Position(3, 1), move?.to)
    }

    @Test
    fun intermediate5x5ChoosesTheSixPlyMoveIncludingItsFirstJump() = runTest {
        val level = Difficulty.entries.first { it.size == 5 }
        var match = testOpening(level, black = Position(1, 4), white = Position(2, 1))
        for (destination in "C1 C5 A2 A4 B4 B2 D3 D1 E1 E3 C2".split(" ")) {
            val position = Position(destination[1].digitToInt() - 1, destination[0] - 'A')
            match = assertNotNull(rules.play(match, position))
        }
        val before = match.board.cells()
        val chosen = assertNotNull(MinimaxSearch(rules).chooseMove(match))

        assertEquals(listOf(4, 6, 6, 8), Difficulty.entries.map { it.searchDepth })
        assertEquals(setOf("C4"), exhaustiveBestMoves(match, rules, 2).map { it.to.notation() }.toSet())
        assertEquals(setOf("C4"), exhaustiveBestMoves(match, rules, 5).map { it.to.notation() }.toSet())
        assertEquals(setOf(chosen), exhaustiveBestMoves(match, rules, 6))
        assertEquals("D5", chosen.to.notation())
        assertEquals(before, match.board.cells())
    }

    @Test
    fun throwsForAnAlreadyCancelledSearchContext() {
        val job = Job().apply { cancel() }
        val search = MinimaxSearch(rules)
        val work: suspend () -> Move? = { search.chooseMove(depthSensitiveMatches.last()) }
        assertFailsWith<CancellationException> {
            work.startCoroutineUninterceptedOrReturn(Continuation(job) {
                error("Search returned past its cancellation check")
            })
        }
    }
}
