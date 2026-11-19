package com.horsesgo.game

internal enum class Side(val key: String, val cell: Int) {
    Black("black", Board.BLACK),
    White("white", Board.WHITE);

    fun opponent(): Side = if (this == Black) White else Black
}

internal data class MatchState(
    val difficulty: Difficulty,
    val board: Board,
    val turn: Side,
)
