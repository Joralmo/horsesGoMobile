package com.horsesgo.ui

import com.horsesgo.ai.MoveSearch
import com.horsesgo.game.Difficulty
import com.horsesgo.game.GameRules
import com.horsesgo.game.Position
import com.horsesgo.game.Side
import com.horsesgo.storage.MatchRead
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MapReplacementTest {
    @Test
    fun staleSearchCannotReplaceNewMap() {
        val allowReturn = CompletableDeferred<Unit>()
        val started = CompletableDeferred<Unit>()
        val search = MoveSearch { match ->
            withContext(NonCancellable) {
                started.complete(Unit)
                allowReturn.await()
                GameRules().legalMoves(match).firstOrNull()
            }
        }
        withGame(search = search) { vm, store ->
            try {
                vm.selectLevel(Difficulty.Beginner)
                vm.loadSelectedMap()
                runCurrent()
                vm.play(vm.uiState.value.playerDestinations.first())
                runCurrent()
                assertTrue(started.isCompleted)
                vm.play(Position(0, 0))
                assertEquals(1, vm.uiState.value.match?.board?.moveNumber)
                vm.openSelector()
                vm.selectLevel(Difficulty.Advanced)
                vm.loadSelectedMap()
                runCurrent()
                allowReturn.complete(Unit)
                runCurrent()

                val state = vm.uiState.value.match
                assertEquals(Difficulty.Advanced, state?.difficulty)
                assertEquals(Side.Black, state?.turn)
                assertEquals(0, state?.board?.moveNumber)
                assertEquals(state, (store.stored as MatchRead.Present).match)
                assertEquals(listOf(0, 1, 0), store.saved.map { it.board.moveNumber })
            } finally {
                allowReturn.complete(Unit)
            }
        }
    }
}
