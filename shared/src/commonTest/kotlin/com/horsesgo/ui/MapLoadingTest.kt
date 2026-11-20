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
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MapLoadingTest {
    @Test
    fun selectionDoesNotStartAMatchAndAllOpeningsUseTheCorrectSize() {
        val random = CountingRandom()
        withGame(rules = GameRules(random)) { vm, store ->
            assertEquals(listOf(4, 5, 6, 8), Difficulty.entries.map { it.size })
            Difficulty.entries.forEachIndexed { i, level ->
                if (i > 0) vm.openSelector()
                val before = vm.uiState.value.match
                vm.selectLevel(level)
                assertEquals(before, vm.uiState.value.match)
                assertEquals(i, store.saved.size)
                assertEquals(i * 2, random.draws)

                vm.loadSelectedMap()
                runCurrent()
                val match = assertNotNull(vm.uiState.value.match)
                assertEquals(level.size, match.board.size)
                assertEquals(0, match.board.moveNumber)
                assertEquals(Side.Black, match.turn)
                assertNotEquals(match.board.positionOf(Side.Black), match.board.positionOf(Side.White))
                assertEquals((i + 1) * 2, random.draws)
            }
        }
    }

    @Test
    fun retryReusesTheRejectedOpeningAndAnotherLoadDrawsAgain() {
        val random = CountingRandom()
        withGame(rules = GameRules(random)) { vm, store ->
            vm.selectLevel(Difficulty.Beginner)
            vm.loadSelectedMap()
            runCurrent()
            val previous = assertNotNull(vm.uiState.value.match)
            assertEquals(2, random.draws)

            val attempts = mutableListOf<MatchState>()
            store.onSave = { attempts += it }
            store.accepted = false
            vm.openSelector()
            vm.loadSelectedMap()
            runCurrent()
            assertEquals(GameMessage.SaveFailed, vm.uiState.value.message)
            assertEquals(4, random.draws)
            assertEquals(previous, vm.uiState.value.match)
            assertEquals(MatchRead.Present(previous), store.stored)

            store.accepted = true
            vm.retrySave()
            runCurrent()
            val loaded = assertNotNull(vm.uiState.value.match)
            assertEquals(listOf(loaded, loaded), attempts)
            assertEquals(listOf(previous, loaded), store.saved)
            assertEquals(4, random.draws)
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

private class CountingRandom : Random() {
    var draws = 0
        private set
    private val source = Random(19)

    override fun nextInt(until: Int): Int {
        draws++
        return source.nextInt(until)
    }

    override fun nextBits(bitCount: Int): Int = error("Expected a bounded opening draw")
}
