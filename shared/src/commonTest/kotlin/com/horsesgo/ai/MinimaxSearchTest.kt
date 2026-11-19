package com.horsesgo.ai

import com.horsesgo.game.GameRules
import com.horsesgo.game.Move
import com.horsesgo.game.Difficulty
import com.horsesgo.game.Position
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
