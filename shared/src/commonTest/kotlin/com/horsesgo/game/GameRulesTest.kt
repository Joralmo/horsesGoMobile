package com.horsesgo.game

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameRulesTest {
    private val rules = GameRules()

    @Test
    fun newOpeningsSampleBothHorsesWithoutReplacement() {
        for (level in Difficulty.entries) {
            val size = level.size
            val last = size * size - 1
            val openings = GameRules(OpeningRandom(0, 0, last, last - 1, 0, 0))
            val first = openings.open(level)
            val second = openings.open(level)
            val repeated = openings.open(level)

            assertEquals(Position(0, 0), first.board.positionOf(Side.Black))
            assertEquals(Position(0, 1), first.board.positionOf(Side.White))
            assertEquals(Position(size - 1, size - 1), second.board.positionOf(Side.Black))
            assertEquals(Position(size - 1, size - 2), second.board.positionOf(Side.White))
            assertEquals(first.board.cells(), repeated.board.cells())
            for (match in listOf(first, second, repeated)) {
                assertEquals(Side.Black, match.turn)
                assertEquals(0, match.board.moveNumber)
                assertEquals(size * size - 2, match.board.cells().flatten().count { it == Board.FREE })
                assertTrue(rules.hasMove(match.board, Side.Black))
                assertTrue(rules.hasMove(match.board, Side.White))
            }
        }
    }

    @Test
    fun legalJumpDestroysOnlyTheOriginAndChangesTurn() {
        val opening = testOpening()
        val from = opening.board.positionOf(Side.Black)
        val destination = Position(3, 0)

        val next = assertNotNull(rules.play(opening, destination))

        assertEquals(Side.White, next.turn)
        assertEquals(1, next.board.moveNumber)
        assertEquals(Board.DESTROYED, next.board[from])
        assertEquals(Board.BLACK, next.board[destination])
        assertEquals(Board.WHITE, next.board[opening.board.positionOf(Side.White)])
        assertEquals(Board.BLACK, opening.board[from])
    }

    @Test
    fun invalidTargetsAreRejectedAndDestroyedSquaresCannotBeReused() {
        val opening = testOpening()
        assertNull(rules.play(opening, Position(-1, 2)))
        assertNull(rules.play(opening, Position(1, 2)))
        val afterBlack = assertNotNull(rules.play(opening, Position(3, 0)))
        assertNull(rules.play(afterBlack, Position(3, 0)))
        val afterWhite = assertNotNull(rules.play(afterBlack, Position(0, 1)))
        assertTrue(Position(1, 1) !in rules.legalDestinations(afterWhite.board, Side.Black))
        assertNull(rules.play(afterWhite, Position(1, 1)))
        assertEquals(Side.Black, afterWhite.turn)
    }

    @Test
    fun terminalWinnerIsTheOtherSideEvenWhenNeitherCanMove() {
        val board = testBoard(free = listOf(Position(0, 1)))
        val whiteTurn = MatchState(Difficulty.Beginner, board, Side.White)
        val afterWhite = assertNotNull(rules.play(whiteTurn, Position(0, 1)))

        assertEquals(Side.White, rules.winner(afterWhite))
        assertFalse(rules.hasMove(afterWhite.board, Side.Black))
        assertFalse(rules.hasMove(afterWhite.board, Side.White))
        assertNull(rules.play(afterWhite, Position(0, 0)))

        val blackTurn = MatchState(
            Difficulty.Beginner,
            testBoard(free = listOf(Position(3, 2), Position(0, 0))),
            Side.Black,
        )
        val afterBlack = assertNotNull(rules.play(blackTurn, Position(3, 2)))
        assertEquals(Side.Black, rules.winner(afterBlack))
    }
}

private class OpeningRandom(vararg draws: Int) : Random() {
    private val indices = draws.iterator()

    override fun nextInt(until: Int): Int = indices.nextInt().also { assertTrue(it in 0 until until) }

    override fun nextBits(bitCount: Int): Int = error("Expected a bounded opening draw")
}

internal fun testOpening(
    level: Difficulty = Difficulty.Beginner,
    black: Position = Position(1, 1),
    white: Position = Position(2, 2),
): MatchState {
    val positions = List(level.size * level.size) { Position(it / level.size, it % level.size) }
    return MatchState(level, testBoard(positions, black, white, level), Side.Black)
}

internal fun testBoard(
    free: List<Position> = emptyList(),
    black: Position = Position(1, 1),
    white: Position = Position(2, 2),
    level: Difficulty = Difficulty.Beginner,
): Board {
    val cells = List(level.size) { row ->
        List(level.size) { column ->
            when (Position(row, column)) {
                black -> Board.BLACK
                white -> Board.WHITE
                in free -> Board.FREE
                else -> Board.DESTROYED
            }
        }
    }
    return requireNotNull(Board.fromCells(level.size, cells))
}
