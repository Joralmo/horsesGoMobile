package com.horsesgo.game

internal class GameRules {
    fun open(level: Difficulty): MatchState = MatchState(level, Board.opening(level), Side.Black)

    fun legalDestinations(board: Board, side: Side): List<Position> =
        candidates(board, side).filter { board[it] == Board.FREE }

    fun hasMove(board: Board, side: Side): Boolean =
        candidates(board, side).any { board[it] == Board.FREE }

    fun mobility(board: Board, side: Side): Int =
        candidates(board, side).count { board[it] == Board.FREE }

    fun legalMoves(match: MatchState): List<Move> {
        val from = match.board.positionOf(match.turn)
        return legalDestinations(match.board, match.turn).map { Move(from, it) }
    }

    fun winner(match: MatchState): Side? =
        if (hasMove(match.board, match.turn)) null else match.turn.opponent()

    fun play(match: MatchState, to: Position): MatchState? =
        play(match, Move(match.board.positionOf(match.turn), to))

    fun play(match: MatchState, move: Move): MatchState? {
        val board = match.board
        if (move.from != board.positionOf(match.turn) || move.to !in board ||
            board[move.to] != Board.FREE || move.to !in candidates(board, match.turn)
        ) return null
        return match.copy(board = board.afterLegalMove(match.turn, move), turn = match.turn.opponent())
    }

    private fun candidates(board: Board, side: Side): List<Position> {
        val (row, column) = board.positionOf(side)
        return jumpTables.getValue(board.size)[row * board.size + column]
    }

    private companion object {
        val offsets = listOf(
            Position(-2, -1), Position(-2, 1), Position(-1, -2), Position(-1, 2),
            Position(1, -2), Position(1, 2), Position(2, -1), Position(2, 1),
        )
        val jumpTables = Difficulty.entries.associate { level ->
            val size = level.size
            size to List(size * size) { index ->
                val row = index / size
                val column = index % size
                offsets.map { Position(row + it.row, column + it.column) }
                    .filter { it.row in 0 until size && it.column in 0 until size }
            }
        }
    }
}
