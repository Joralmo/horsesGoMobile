package com.horsesgo.storage

import com.horsesgo.game.Board
import com.horsesgo.game.Difficulty
import com.horsesgo.game.MatchState
import com.horsesgo.game.Side
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
internal data class MatchSnapshot(
    val schemaVersion: Int,
    val difficulty: String,
    val cells: List<List<Int>>,
    val nextTurn: String,
) {
    fun restore(): MatchState? {
        if (schemaVersion != VERSION) return null
        val level = Difficulty.entries.firstOrNull { it.key == difficulty } ?: return null
        val board = Board.fromCells(level.size, cells) ?: return null
        val turn = if (board.moveNumber % 2 == 0) Side.Black else Side.White
        if (turn.key != nextTurn) return null
        return MatchState(level, board, turn)
    }

    companion object {
        private const val VERSION = 1

        fun encode(match: MatchState): String = Json.encodeToString(
            MatchSnapshot(VERSION, match.difficulty.key, match.board.cells(), match.turn.key),
        )

        fun decode(data: String): MatchState? = try {
            Json.decodeFromString<MatchSnapshot>(data).restore()
        } catch (_: SerializationException) {
            null
        }
    }
}
