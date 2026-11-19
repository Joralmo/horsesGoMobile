package com.horsesgo.ai

import com.horsesgo.game.GameRules
import com.horsesgo.game.MatchState
import com.horsesgo.game.Move
import com.horsesgo.game.Side
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.ensureActive

internal class MinimaxSearch(private val rules: GameRules) : MoveSearch {
    override suspend fun chooseMove(match: MatchState): Move? {
        coroutineContext.ensureActive()
        if (match.turn != Side.White) return null

        var bestMove: Move? = null
        var bestScore = Int.MAX_VALUE
        for (move in rules.legalMoves(match)) {
            val child = checkNotNull(rules.play(match, move))
            val score = evaluate(child, match.difficulty.searchDepth - 1, Int.MIN_VALUE, bestScore)
            if (score < bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private suspend fun evaluate(match: MatchState, depth: Int, alpha: Int, beta: Int): Int {
        coroutineContext.ensureActive()
        val winner = rules.winner(match)
        if (winner != null) return if (winner == Side.White) WHITE_WIN else BLACK_WIN
        if (depth == 0) return rules.mobility(match.board, Side.Black)

        val minimizing = match.turn == Side.White
        var lower = alpha
        var upper = beta
        var result = if (minimizing) Int.MAX_VALUE else Int.MIN_VALUE
        for (move in rules.legalMoves(match)) {
            val child = checkNotNull(rules.play(match, move))
            val score = evaluate(child, depth - 1, lower, upper)
            if (minimizing) {
                result = minOf(result, score)
                upper = minOf(upper, result)
            } else {
                result = maxOf(result, score)
                lower = maxOf(lower, result)
            }
            if (lower >= upper) break
        }
        return result
    }

    private companion object {
        // The human mobility heuristic ranges from zero to eight.
        const val WHITE_WIN = -1
        const val BLACK_WIN = 9
    }
}
