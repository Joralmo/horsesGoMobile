package com.horsesgo.ai

import com.horsesgo.game.GameRules
import com.horsesgo.game.MatchState
import com.horsesgo.game.Move
import com.horsesgo.game.Side

internal data class MinimaxNode(
    val depth: Int,
    val turn: Side,
    val move: Move?,
    val winner: Side?,
    val score: Int,
    val children: List<MinimaxNode>,
)

internal class MinimaxTree(private val rules: GameRules) {
    fun build(match: MatchState): MinimaxNode {
        require(match.turn == Side.Black)
        return buildNode(match, depth = 0, move = null)
    }

    private fun buildNode(match: MatchState, depth: Int, move: Move?): MinimaxNode {
        val winner = rules.winner(match)
        val leafScore = minimaxLeafScore(rules, match, winner, atLimit = depth == MAX_DEPTH)
        if (leafScore != null) {
            return MinimaxNode(depth, match.turn, move, winner, leafScore, emptyList())
        }

        val children = rules.legalMoves(match).map { candidate ->
            val child = checkNotNull(rules.play(match, candidate))
            buildNode(child, depth + 1, candidate)
        }
        val score = if (match.turn == Side.Black) {
            children.maxOf { it.score }
        } else {
            children.minOf { it.score }
        }
        return MinimaxNode(depth, match.turn, move, winner = null, score, children)
    }

    companion object {
        const val MAX_DEPTH = 2
    }
}
