package com.horsesgo.ai

import com.horsesgo.game.Board
import com.horsesgo.game.Difficulty
import com.horsesgo.game.GameRules
import com.horsesgo.game.MatchState
import com.horsesgo.game.Move
import com.horsesgo.game.Position
import com.horsesgo.game.Side
import com.horsesgo.game.testBoard
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MinimaxDecisionTest {
    private val rules = GameRules()
    private val search = MinimaxSearch(rules)

    @Test
    fun takesForcedWinAndReturnsNoMoveFromATerminalPosition() = runTest {
        val match = matchFromRows(Difficulty.Beginner, "..#.", ".#..", ".###", "#W#B")
        val move = assertNotNull(search.chooseMove(match))
        assertEquals(Position(1, 2), move.to)
        assertEquals(Side.White, rules.winner(assertNotNull(rules.play(match, move))))

        val terminal = match.copy(board = testBoard())
        assertNull(search.chooseMove(terminal))
    }

    @Test
    fun findsTheOnlyMove() = runTest {
        val only = Position(0, 1)
        val match = MatchState(Difficulty.Beginner, testBoard(free = listOf(only)), Side.White)
        assertEquals(only, search.chooseMove(match)?.to)
    }

    @Test
    fun agreesWithExhaustiveMinimaxAndAvoidsTheLosingBranch() = runTest {
        val match = depthSensitiveMatches.first()
        val bestMoves = exhaustiveBestMoves(match, rules, 4)
        assertEquals(setOf(Position(0, 0), Position(3, 3)), bestMoves.map { it.to }.toSet())
        val chosen = assertNotNull(search.chooseMove(match))
        assertTrue(chosen in bestMoves)
        assertTrue(chosen.to != Position(3, 1))
    }
}

internal val depthSensitiveMatches = listOf(
    matchFromRows(Difficulty.Beginner, ".###", "..W.", "#.#.", "B..."),
    matchFromRows(Difficulty.Intermediate, ".#.#..", ".#..B#", "#...#.", "#.##.#", "##..##", "##..#W"),
    matchFromRows(
        Difficulty.Advanced,
        "####.###", "..##...#", "#...###B", "#.#..#.#",
        ".###W###", "##..#.##", "#.#.###.", "#.#..###",
    ),
)

internal fun matchFromRows(level: Difficulty, vararg rows: String): MatchState {
    val cells = rows.map { row ->
        row.map {
            when (it) {
                '.' -> Board.FREE
                '#' -> Board.DESTROYED
                'B' -> Board.BLACK
                'W' -> Board.WHITE
                else -> error("Unrecognized test cell")
            }
        }
    }
    return MatchState(level, requireNotNull(Board.fromCells(level.size, cells)), Side.White)
}

internal fun exhaustiveBestMoves(match: MatchState, rules: GameRules, depth: Int): Set<Move> {
    fun score(state: MatchState, remaining: Int): Int {
        val next = rules.legalMoves(state)
        if (next.isEmpty()) return if (state.turn == Side.Black) -1 else 9
        if (remaining == 0) return rules.mobility(state.board, Side.Black)
        val scores = next.map { score(checkNotNull(rules.play(state, it)), remaining - 1) }
        return if (state.turn == Side.Black) scores.max() else scores.min()
    }

    val results = rules.legalMoves(match).associateWith {
        score(checkNotNull(rules.play(match, it)), depth - 1)
    }
    val best = results.values.minOrNull()
    return results.filterValues { it == best }.keys
}
