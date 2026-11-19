package com.horsesgo.game

internal enum class Difficulty(
    val key: String,
    val label: String,
    val size: Int,
    val searchDepth: Int,
    val blackStart: Position,
    val whiteStart: Position,
) {
    Beginner("beginner", "Principiante", 4, 4, Position(1, 1), Position(2, 2)),
    Intermediate("intermediate", "Intermedio", 6, 6, Position(2, 2), Position(3, 3)),
    Advanced("advanced", "Avanzado", 8, 8, Position(3, 3), Position(4, 4)),
}
