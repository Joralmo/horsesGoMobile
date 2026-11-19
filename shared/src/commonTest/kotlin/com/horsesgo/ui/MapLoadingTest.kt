package com.horsesgo.ui

import com.horsesgo.game.Difficulty
import com.horsesgo.game.GameRules
import com.horsesgo.game.MatchState
import com.horsesgo.game.Position
import com.horsesgo.game.Side
import com.horsesgo.game.testBoard
import com.horsesgo.storage.MatchRead
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MapLoadingTest {
    @Test
    fun selectionDoesNotStartAMatchAndAllOpeningsUseTheCorrectSize() = withGame { vm, store ->
        val positions = listOf("B2" to "C3", "C3" to "D4", "D4" to "E5")
        Difficulty.entries.forEachIndexed { i, level ->
            if (i > 0) vm.openSelector()
            val before = vm.uiState.value.match
            vm.selectLevel(level)
            assertEquals(before, vm.uiState.value.match)
            assertEquals(i, store.saved.size)

            vm.loadSelectedMap()
            runCurrent()
            val match = assertNotNull(vm.uiState.value.match)
            assertEquals(level.size, match.board.size)
            assertEquals(0, match.board.moveNumber)
            assertEquals(Side.Black, match.turn)
            assertEquals(positions[i].first, match.board.positionOf(Side.Black).notation())
            assertEquals(positions[i].second, match.board.positionOf(Side.White).notation())
        }
    }

    @Test
    fun rejectedReplacementKeepsSavedMatchAndAllowsReturn() {
        val initial = GameRules().open(Difficulty.Beginner)
        withGame(MatchRead.Present(initial)) { vm, store ->
            store.accepted = false
            vm.openSelector()
            vm.selectLevel(Difficulty.Advanced)
            vm.loadSelectedMap()
            runCurrent()
            assertEquals(GameMessage.SaveFailed, vm.uiState.value.message)
            assertEquals(MatchRead.Present(initial), store.stored)
            vm.returnToMatch()
            assertEquals(Screen.Board, vm.uiState.value.screen)
            assertEquals(initial, vm.uiState.value.match)
            assertNull(vm.uiState.value.message)
        }
    }

    @Test
    fun completedMatchCanBeReplaced() {
        val final = MatchState(Difficulty.Beginner, testBoard(free = listOf(Position(0, 0))), Side.White)
        withGame(MatchRead.Present(final)) { vm, _ ->
            assertEquals(Side.Black, vm.uiState.value.winner)
            vm.openSelector()
            vm.selectLevel(Difficulty.Intermediate)
            vm.loadSelectedMap()
            runCurrent()
            assertNull(vm.uiState.value.winner)
            assertEquals(Difficulty.Intermediate, vm.uiState.value.match?.difficulty)
        }
    }
}
