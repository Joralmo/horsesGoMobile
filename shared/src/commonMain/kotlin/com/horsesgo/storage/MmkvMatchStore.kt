package com.horsesgo.storage

import com.horsesgo.game.MatchState
import com.tencent.mmkv.kmp.MMKV

internal class MmkvMatchStore : MatchStore {
    private val storage by lazy { MMKV.mmkvWithID("horses-go") }

    override fun load(): MatchRead = try {
        val encoded = storage.decodeString(ACTIVE_MATCH)
        when {
            encoded != null -> MatchSnapshot.decode(encoded)?.let(MatchRead::Present) ?: MatchRead.Invalid
            storage.containsKey(ACTIVE_MATCH) -> MatchRead.Invalid
            else -> MatchRead.Absent
        }
    } catch (_: Exception) {
        MatchRead.Unavailable
    }

    override fun save(match: MatchState): Boolean = try {
        val accepted = storage.encodeString(ACTIVE_MATCH, MatchSnapshot.encode(match))
        if (accepted) storage.sync()
        accepted
    } catch (_: Exception) {
        false
    }

    private companion object {
        const val ACTIVE_MATCH = "active-match"
    }
}
