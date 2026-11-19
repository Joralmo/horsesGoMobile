package com.horsesgo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.horsesgo.game.Board
import com.horsesgo.game.Position
import com.horsesgo.game.Side

@Composable
internal fun GameScreen(
    state: GameUiState,
    onPlay: (Position) -> Unit,
    onChangeMap: () -> Unit,
) {
    val match = state.match ?: return
    val board = match.board
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(match.difficulty.label, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        val moves = board.moveNumber
        val moveLabel = if (moves == 1) "1 jugada" else "$moves jugadas"
        Text("${board.size} × ${board.size} · $moveLabel", color = GameMuted)
    }

    val status = when {
        state.winner == Side.Black -> "Ganaste"
        state.winner == Side.White -> "Ganó la máquina"
        state.saving -> "Guardando…"
        match.turn == Side.Black -> "Tu turno"
        else -> "Turno de la máquina"
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (state.winner != null) GameAccent else GamePanel,
        contentColor = if (state.winner != null) GameBackground else GameInk,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(status, Modifier.padding(14.dp), fontWeight = FontWeight.SemiBold)
    }

    GameBoard(board, state.canPlay, onPlay)
    TextButton(onClick = onChangeMap, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) {
        Text("Cambiar mapa")
    }
}

@Composable
private fun GameBoard(board: Board, canPlay: Boolean, onPlay: (Position) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val boardSize = maxWidth - 20.dp
            Row {
                Column(Modifier.width(20.dp).height(boardSize)) {
                    for (row in board.size - 1 downTo 0) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Text((row + 1).toString(), color = GameMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Column(
                    Modifier.width(boardSize).height(boardSize).clip(RoundedCornerShape(12.dp)),
                ) {
                    for (row in board.size - 1 downTo 0) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (column in 0 until board.size) {
                                val position = Position(row, column)
                                BoardCell(board[position], position, canPlay, onPlay, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(20.dp))
            repeat(board.size) { column ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(('A' + column).toString(), color = GameMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    cell: Int,
    position: Position,
    canPlay: Boolean,
    onPlay: (Position) -> Unit,
    modifier: Modifier,
) {
    val light = (position.row + position.column) % 2 == 1
    val terrain = when {
        cell == Board.DESTROYED -> Color(0xFF304541)
        light -> Color(0xFFE3D0A6)
        else -> Color(0xFF72958A)
    }
    val label = when (cell) {
        Board.DESTROYED -> "destruida"
        Board.BLACK -> "caballo negro"
        Board.WHITE -> "caballo blanco"
        else -> "libre"
    }
    val semantics = Modifier.semantics { contentDescription = "${position.notation()}, $label" }
    val tap = if (canPlay && cell == Board.FREE) {
        Modifier.clickable(role = Role.Button) { onPlay(position) }
    } else Modifier

    Box(
        modifier = modifier.fillMaxHeight()
            .background(terrain).then(semantics).then(tap),
        contentAlignment = Alignment.Center,
    ) {
        when (cell) {
            Board.BLACK, Board.WHITE -> Knight(Modifier.fillMaxWidth(0.76f).aspectRatio(1f), dark = cell == Board.BLACK)
            Board.DESTROYED -> Canvas(Modifier.fillMaxWidth(0.62f).aspectRatio(1f)) {
                val middle = Offset(size.width * 0.48f, size.height * 0.47f)
                val crack = Path().apply {
                    moveTo(size.width * 0.10f, size.height * 0.22f)
                    lineTo(middle.x, middle.y)
                    lineTo(size.width * 0.33f, size.height * 0.70f)
                    lineTo(size.width * 0.60f, size.height * 0.94f)
                    moveTo(middle.x, middle.y)
                    lineTo(size.width * 0.77f, size.height * 0.29f)
                    lineTo(size.width * 0.87f, size.height * 0.40f)
                }
                drawPath(crack, Color(0xFF9FAF9B), style = Stroke(1.6.dp.toPx()))
            }
        }
    }
}

@Composable
internal fun Knight(modifier: Modifier, dark: Boolean) {
    Canvas(modifier) {
        scale(size.width / 100f, size.height / 100f, pivot = Offset.Zero) {
            val body = Path().apply {
                moveTo(21f, 83f)
                lineTo(24f, 72f)
                cubicTo(27f, 60f, 41f, 53f, 45f, 43f)
                lineTo(33f, 49f)
                lineTo(23f, 46f)
                lineTo(17f, 37f)
                lineTo(33f, 21f)
                lineTo(36f, 11f)
                lineTo(46f, 18f)
                cubicTo(62f, 15f, 73f, 29f, 77f, 44f)
                lineTo(82f, 83f)
                close()
            }
            val fill = if (dark) GameBackground else GameInk
            val outline = if (dark) Color(0xFF0C1817) else Color(0xFF3A4B43)
            drawPath(body, fill)
            drawPath(body, outline, style = Stroke(3.5f))
            drawCircle(if (dark) GameAccent else outline, 2.8f, Offset(43f, 29f))
            drawLine(outline, Offset(18f, 85f), Offset(85f, 85f), strokeWidth = 4f)
        }
    }
}

@Composable
internal fun GameNotice(message: GameMessage, onRetry: () -> Unit) {
    Surface(color = GamePanel, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
            val text = when (message) {
                GameMessage.InvalidSave -> "No se pudo recuperar la partida guardada."
                GameMessage.ReadFailed -> "No se pudo leer el guardado."
                GameMessage.SaveFailed -> "No se pudo guardar el cambio."
            }
            Text(text, color = MaterialTheme.colorScheme.error)
            if (message == GameMessage.SaveFailed) {
                TextButton(onClick = onRetry) { Text("Reintentar guardado") }
            }
        }
    }
}
