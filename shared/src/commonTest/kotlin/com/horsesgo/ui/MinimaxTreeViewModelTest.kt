package com.horsesgo.ui

import com.horsesgo.ai.MoveSearch
import com.horsesgo.game.Difficulty
import com.horsesgo.game.GameRules
import com.horsesgo.game.MatchState
import com.horsesgo.game.Position
import com.horsesgo.game.Side
import com.horsesgo.game.testBoard
import com.horsesgo.game.testOpening
import com.horsesgo.storage.MatchRead
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MinimaxTreeViewModelTest {
    private val level = Difficulty.Intermediate5x5
    private val opening = testOpening(level, black = Position(1, 1), white = Position(2, 3))

    @Test
    fun consultationDoesNotPlayOrSaveAndNextTreeUsesTheNewTurn() = withGame(MatchRead.Present(opening)) { vm, store ->
        val before = vm.uiState.value
        val destination = Position(3, 2)
        assertTrue(before.canViewMinimaxTree)
        assertTrue(destination in before.playerDestinations)

        vm.openMinimaxTree()
        val firstTree = assertNotNull(vm.uiState.value.minimaxTree)
        assertEquals(Side.Black, firstTree.turn)
        assertFalse(vm.uiState.value.canPlay)
        vm.openMinimaxTree()
        vm.play(destination)
        vm.openSelector()
        runCurrent()
        assertEquals(Screen.Board, vm.uiState.value.screen)
        assertEquals(opening, vm.uiState.value.match)
        assertEquals(firstTree, vm.uiState.value.minimaxTree)
        assertTrue(store.saved.isEmpty())

        vm.closeMinimaxTree()
        assertEquals(before, vm.uiState.value)
        vm.play(destination)
        runCurrent()
        assertEquals(listOf(1, 2), store.saved.map { it.board.moveNumber })
        assertTrue(vm.uiState.value.canViewMinimaxTree)

        vm.openMinimaxTree()
        val secondTree = assertNotNull(vm.uiState.value.minimaxTree)
        assertTrue(secondTree.children.isNotEmpty())
        assertTrue(secondTree.children.all { it.move?.from == destination })
        assertEquals(2, store.saved.size)
    }

    @Test
    fun onlyAnActiveBlackTurnOnThe5x5BoardCanOpenTheTree() {
        val finished = MatchState(level, testBoard(level = level), Side.Black)
        val rules = GameRules()
        val whiteTurn = assertNotNull(rules.play(opening, Position(3, 2)))
        val allowWhite = CompletableDeferred<Unit>()
        val search = MoveSearch { match ->
            allowWhite.await()
            rules.legalMoves(match).first()
        }
        for (match in listOf(MatchRead.Absent, MatchRead.Present(finished), MatchRead.Present(testOpening()))) {
            withGame(match) { vm, _ ->
                assertFalse(vm.uiState.value.canViewMinimaxTree)
                vm.openMinimaxTree()
                assertNull(vm.uiState.value.minimaxTree)
            }
        }
        withGame(MatchRead.Present(whiteTurn), search) { vm, _ ->
            assertEquals(Side.White, vm.uiState.value.match?.turn)
            assertFalse(vm.uiState.value.canViewMinimaxTree)
            vm.openMinimaxTree()
            assertNull(vm.uiState.value.minimaxTree)
            allowWhite.complete(Unit)
            runCurrent()
            assertTrue(vm.uiState.value.canViewMinimaxTree)

            vm.openSelector()
            assertFalse(vm.uiState.value.canViewMinimaxTree)
            vm.openMinimaxTree()
            assertNull(vm.uiState.value.minimaxTree)
        }
    }

    @Test
    fun treeIsUnavailableWhileSavingAndAfterAFailedSave() = withGame(MatchRead.Present(opening)) { vm, store ->
        store.accepted = false
        vm.play(Position(3, 2))
        assertTrue(vm.uiState.value.saving)
        assertFalse(vm.uiState.value.canViewMinimaxTree)
        vm.openMinimaxTree()
        assertNull(vm.uiState.value.minimaxTree)

        runCurrent()
        assertEquals(GameMessage.SaveFailed, vm.uiState.value.message)
        assertFalse(vm.uiState.value.canViewMinimaxTree)
        vm.openMinimaxTree()
        assertNull(vm.uiState.value.minimaxTree)
        assertEquals(opening, vm.uiState.value.match)
    }
}
