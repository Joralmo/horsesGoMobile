package com.horsesgo.ai

import com.horsesgo.game.Board
import com.horsesgo.game.Difficulty
import com.horsesgo.game.GameRules
import com.horsesgo.game.MatchState
import com.horsesgo.game.Move
import com.horsesgo.game.Position
import com.horsesgo.game.Side
import com.horsesgo.game.testOpening
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MinimaxTreeTest {
    private val rules = GameRules()

    @Test
    fun buildsAllTenReferenceTreesWithoutChangingTheInput() {
        for (fixture in fixtures) {
            var match = testOpening(
                Difficulty.Intermediate5x5,
                black = Position(1, 1),
                white = Position(2, 3),
            )
            for (destination in fixture.prefix.split(" ")) {
                match = assertNotNull(rules.play(match, destination.position()))
            }
            val beforeCells = match.board.cells()
            val beforeTurn = match.turn
            val beforeMoveNumber = match.board.moveNumber

            val tree = MinimaxTree(rules).build(match)

            assertEquals(0, tree.depth)
            assertEquals(Side.Black, tree.turn)
            assertNull(tree.move)
            assertEquals(fixture.rootScore, tree.score)
            assertNull(tree.winner)
            assertEquals(beforeCells, match.board.cells())
            assertEquals(beforeTurn, match.turn)
            assertEquals(beforeMoveNumber, match.board.moveNumber)
            assertEquals(fixture.branches.size, tree.children.size)

            val expectedRoot = OracleState.from(match)
            assertEquals(
                expectedRoot.legalMoves().map { it.to.notation() }.toSet(),
                tree.children.map { assertNotNull(it.move).to.notation() }.toSet(),
            )
            assertEquals(fixture.branches.keys, tree.children.map { assertNotNull(it.move).to.notation() }.toSet())

            for (branch in tree.children) {
                val branchMove = assertNotNull(branch.move)
                assertEquals(expectedRoot.black, branchMove.from)
                val expected = fixture.branches.getValue(branchMove.to.notation())
                assertEquals(1, branch.depth)
                assertEquals(Side.White, branch.turn)
                assertEquals(expected.score, branch.score)
                assertEquals(expected.replyCount, branch.children.size)

                val branchState = expectedRoot.play(branchMove)
                val branchWinner = branchState.winner()
                assertEquals(branchWinner, branch.winner)
                if (branchWinner != null) {
                    assertEquals(0, branch.children.size)
                    continue
                }

                val expectedReplies = branchState.legalMoves()
                assertEquals(
                    expectedReplies.map { it.to.notation() }.toSet(),
                    branch.children.map { assertNotNull(it.move).to.notation() }.toSet(),
                )
                for (leaf in branch.children) {
                    val leafMove = assertNotNull(leaf.move)
                    assertEquals(branchState.white, leafMove.from)
                    val leafState = branchState.play(leafMove)
                    val leafWinner = leafState.winner()
                    assertEquals(2, leaf.depth)
                    assertEquals(Side.Black, leaf.turn)
                    assertEquals(leafWinner, leaf.winner)
                    assertEquals(0, leaf.children.size)
                    assertEquals(
                        if (leafWinner == Side.White) -1 else if (leafWinner == Side.Black) 9
                        else leafState.mobility(Side.Black),
                        leaf.score,
                    )
                }
            }
        }
    }
}

private data class BranchExpectation(val score: Int, val replyCount: Int)

private data class Fixture(
    val prefix: String,
    val rootScore: Int,
    val branches: Map<String, BranchExpectation>,
)

private val fixtures = listOf(
    Fixture("D1 E1 E3 C2 D5 D4 B4 B5 A2 C3 C1 E2", 9, mapOf("B3" to BranchExpectation(9, 0))),
    Fixture("D1 E5 C3 C4 E2 A5 D4 B3 B5 A1 A3 C2 B1 E3 D2 D5 E4 B4 C5 A2", -1, mapOf("A4" to BranchExpectation(-1, 1))),
    Fixture("C4 C5 A3 A4 B5 C3 D4 D5 C2 E3", 1, mapOf(
        "B4" to BranchExpectation(1, 1), "E1" to BranchExpectation(-1, 1), "A1" to BranchExpectation(1, 1),
    )),
    Fixture("D1 E1 C3 C2 D5 D4 B4 B3 A2 A5 C1 C4", -1, mapOf("E2" to BranchExpectation(-1, 4))),
    Fixture("D1 E1 E3 C2 D5 B4 C3 A2 A4 C1 C5 E2 B3 D4", 3, mapOf(
        "A5" to BranchExpectation(1, 1), "A1" to BranchExpectation(-1, 1), "D2" to BranchExpectation(3, 1),
    )),
    Fixture("C4 B4", 3, mapOf(
        "D2" to BranchExpectation(3, 3), "E5" to BranchExpectation(-1, 3),
        "A5" to BranchExpectation(1, 3), "E3" to BranchExpectation(2, 3), "A3" to BranchExpectation(2, 3),
    )),
    Fixture("C4 E1 D2 C2 B1 E3 A3 D5 B5 C3 D4 D1", 9, mapOf(
        "E2" to BranchExpectation(9, 0), "B3" to BranchExpectation(9, 0),
    )),
    Fixture("D1 E1 C3 C2 B1 D4 D2 B3 E4 C1 C5 E2", 9, mapOf("A4" to BranchExpectation(9, 0))),
    Fixture("C4 E1 A3 C2 B5 B4 C3 D5", 2, mapOf(
        "D1" to BranchExpectation(-1, 1), "B1" to BranchExpectation(1, 1),
        "E4" to BranchExpectation(2, 1), "A4" to BranchExpectation(1, 1),
        "E2" to BranchExpectation(2, 1), "A2" to BranchExpectation(1, 1),
    )),
    Fixture("C4 C1 E3 A2", 4, mapOf(
        "D5" to BranchExpectation(1, 2), "D1" to BranchExpectation(-1, 2), "C2" to BranchExpectation(4, 2),
    )),
)

private data class OracleState(
    val cells: List<List<Int>>,
    val black: Position,
    val white: Position,
    val turn: Side,
) {
    fun legalMoves(): List<Move> = legalDestinations().map { Move(positionOf(turn), it) }

    fun winner(): Side? = if (legalDestinations().isEmpty()) turn.opponent() else null

    fun mobility(side: Side): Int = destinations(side).count { cells[it.row][it.column] == Board.FREE }

    fun play(move: Move): OracleState {
        val updated = cells.map { it.toMutableList() }
        updated[move.from.row][move.from.column] = Board.DESTROYED
        updated[move.to.row][move.to.column] = turn.cell
        return copy(
            cells = updated.map { it.toList() },
            black = if (turn == Side.Black) move.to else black,
            white = if (turn == Side.White) move.to else white,
            turn = turn.opponent(),
        )
    }

    private fun positionOf(side: Side): Position = if (side == Side.Black) black else white

    private fun legalDestinations(): List<Position> = legalDestinations(turn)

    private fun legalDestinations(side: Side): List<Position> =
        destinations(side).filter { cells[it.row][it.column] == Board.FREE }

    private fun destinations(side: Side): List<Position> {
        val from = positionOf(side)
        return knightOffsets.map { Position(from.row + it.row, from.column + it.column) }
            .filter { it.row in cells.indices && it.column in cells.indices }
    }

    companion object {
        private val knightOffsets = listOf(
            Position(-2, -1), Position(-2, 1), Position(-1, -2), Position(-1, 2),
            Position(1, -2), Position(1, 2), Position(2, -1), Position(2, 1),
        )

        fun from(match: MatchState): OracleState = OracleState(
            match.board.cells(),
            match.board.positionOf(Side.Black),
            match.board.positionOf(Side.White),
            match.turn,
        )
    }
}

private fun String.position(): Position = Position(this[1].digitToInt() - 1, this[0] - 'A')
