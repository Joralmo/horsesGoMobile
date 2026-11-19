package com.horsesgo.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameRulesTest {
    private val rules = GameRules()

    @Test
    fun legalJumpDestroysOnlyTheOriginAndChangesTurn() {
        val opening = rules.open(Difficulty.Beginner)
        val from = opening.difficulty.blackStart
        val destination = Position(3, 0)

        val next = assertNotNull(rules.play(opening, destination))

        assertEquals(Side.White, next.turn)
        assertEquals(1, next.board.moveNumber)
        assertEquals(Board.DESTROYED, next.board[from])
        assertEquals(Board.BLACK, next.board[destination])
        assertEquals(Board.WHITE, next.board[opening.difficulty.whiteStart])
        assertEquals(Board.BLACK, opening.board[from])
    }

    @Test
    fun invalidTargetsAreRejectedAndDestroyedSquaresCannotBeReused() {
        val opening = rules.open(Difficulty.Beginner)
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

internal fun testBoard(
    free: List<Position> = emptyList(),
    black: Position = Difficulty.Beginner.blackStart,
    white: Position = Difficulty.Beginner.whiteStart,
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
