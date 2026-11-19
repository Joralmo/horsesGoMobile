package com.horsesgo.game

internal data class Position(val row: Int, val column: Int) {
    fun notation(): String = "${'A' + column}${row + 1}"
}
