package com.horsesgo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.horsesgo.game.Board
import com.horsesgo.game.GameRules
import com.horsesgo.game.Position
import com.horsesgo.game.Side
import com.horsesgo.resources.Res
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Serializable
private data class BoardDisplay(val cells: List<List<Int>>, val legal: List<String>, val canPlay: Boolean)

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun GameBoard(board: Board, canPlay: Boolean, onPlay: (Position) -> Unit) {
    val document by produceState<String?>(null) {
        value = Res.readBytes("files/board.html").decodeToString()
    }
    val rules = remember { GameRules() }
    val destinations = remember(board, canPlay) {
        if (canPlay) rules.legalDestinations(board, Side.Black).associateBy { it.notation().lowercase() }
        else emptyMap()
    }
    val script = remember(board, destinations, canPlay) {
        val display = BoardDisplay(board.cells(), destinations.keys.toList(), canPlay)
        "window.renderBoard(${Json.encodeToString(display)})"
    }
    Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(6.dp)).background(GamePanel)) {
        document?.let { html ->
            ChessgroundView(html, script, Modifier.fillMaxSize()) { square ->
                destinations[square]?.let(onPlay)
            }
        }
    }
}

@Composable
internal expect fun ChessgroundView(html: String, script: String, modifier: Modifier, onSquare: (String) -> Unit)
