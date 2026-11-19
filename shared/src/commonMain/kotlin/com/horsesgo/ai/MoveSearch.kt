package com.horsesgo.ai

import com.horsesgo.game.MatchState
import com.horsesgo.game.Move

internal fun interface MoveSearch {
    suspend fun chooseMove(match: MatchState): Move?
}
