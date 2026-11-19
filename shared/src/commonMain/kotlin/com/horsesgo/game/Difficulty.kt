package com.horsesgo.game

internal enum class Difficulty(
    val key: String,
    val label: String,
    val size: Int,
    val searchDepth: Int,
) {
    Beginner("beginner", "Principiante", 4, 4),
    Intermediate5x5("intermediate-5x5", "Intermedio", 5, 6),
    Intermediate("intermediate", "Intermedio", 6, 6),
    Advanced("advanced", "Avanzado", 8, 8),
}
