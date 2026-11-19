package com.horsesgo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
