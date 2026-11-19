package com.horsesgo.game

internal class Board private constructor(
    private val rows: Array<IntArray>,
    private val black: Position,
    private val white: Position,
    val moveNumber: Int,
) {
    val size: Int get() = rows.size

    operator fun contains(position: Position): Boolean =
        position.row in rows.indices && position.column in rows.indices

    operator fun get(position: Position): Int = rows[position.row][position.column]

    fun positionOf(side: Side): Position = if (side == Side.Black) black else white

    fun cells(): List<List<Int>> = rows.map { it.toList() }

    // Only the affected rows are copied. Every other row remains private and immutable.
    fun afterLegalMove(side: Side, move: Move): Board {
        val updated = rows.copyOf()
        updated[move.from.row] = rows[move.from.row].copyOf()
        updated[move.to.row] = rows[move.to.row].copyOf()
        updated[move.from.row][move.from.column] = DESTROYED
        updated[move.to.row][move.to.column] = side.cell
        val nextBlack = if (side == Side.Black) move.to else black
        val nextWhite = if (side == Side.White) move.to else white
        return Board(updated, nextBlack, nextWhite, moveNumber + 1)
    }

    companion object {
        const val FREE = 0
        const val DESTROYED = 1
        const val BLACK = 2
        const val WHITE = 3

        fun opening(level: Difficulty): Board {
            val rows = Array(level.size) { IntArray(level.size) }
            rows[level.blackStart.row][level.blackStart.column] = BLACK
            rows[level.whiteStart.row][level.whiteStart.column] = WHITE
            return Board(rows, level.blackStart, level.whiteStart, 0)
        }

        fun fromCells(size: Int, cells: List<List<Int>>): Board? {
            if (cells.size != size || cells.any { it.size != size }) return null
            var black: Position? = null
            var white: Position? = null
            var destroyed = 0
            cells.forEachIndexed { row, values ->
                values.forEachIndexed { col, cell ->
                    when (cell) {
                        FREE -> Unit
                        DESTROYED -> destroyed++
                        BLACK -> {
                            if (black != null) return null
                            black = Position(row, col)
                        }
                        WHITE -> {
                            if (white != null) return null
                            white = Position(row, col)
                        }
                        else -> return null
                    }
                }
            }
            return Board(
                Array(size) { cells[it].toIntArray() },
                black ?: return null,
                white ?: return null,
                destroyed,
            )
        }
    }
}
