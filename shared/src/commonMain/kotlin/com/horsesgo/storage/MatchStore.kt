package com.horsesgo.storage

import com.horsesgo.game.MatchState

internal interface MatchStore {
    fun load(): MatchRead
    fun save(match: MatchState): Boolean
}

internal sealed interface MatchRead {
    data class Present(val match: MatchState) : MatchRead
    data object Absent : MatchRead
    data object Invalid : MatchRead
    data object Unavailable : MatchRead
}
