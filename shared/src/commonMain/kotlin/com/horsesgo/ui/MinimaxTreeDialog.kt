package com.horsesgo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.horsesgo.ai.MinimaxNode
import com.horsesgo.ai.MinimaxTree
import com.horsesgo.game.Board
import com.horsesgo.game.Position
import com.horsesgo.game.Side

@Composable
internal fun MinimaxTreeDialog(board: Board, root: MinimaxNode, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight * 0.9f),
                shape = RoundedCornerShape(22.dp),
                color = GamePanel,
                contentColor = GameInk,
            ) {
                Column {
                    Column(
                        Modifier.padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            "Árbol MiniMax",
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            "Hasta ${MinimaxTree.MAX_DEPTH} movimientos",
                            color = GameMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    HorizontalDivider(color = GameLine)
                    Column(
                        Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PositionSummary(board, root.score)
                        Text(
                            "Nivel 1 · Jugador (MAX)",
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleSmall,
                        )
                        for (branch in root.children) {
                            PlayerBranch(branch)
                        }
                    }
                    HorizontalDivider(color = GameLine)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Volver al tablero") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PositionSummary(board: Board, score: Int) {
    val black = board.positionOf(Side.Black).notation()
    val white = board.positionOf(Side.White).notation()
    val destroyed = board.cells().flatMapIndexed { row, cells ->
        cells.mapIndexedNotNull { column, cell ->
            if (cell == Board.DESTROYED) Position(row, column).notation() else null
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Nivel 0 · Posición actual",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleSmall,
        )
        Text("Negro $black · Blanco $white", style = MaterialTheme.typography.bodyMedium)
        Text(
            if (destroyed.isEmpty()) "Las demás casillas están libres" else "Destruidas · ${destroyed.joinToString()}",
            color = GameMuted,
            style = MaterialTheme.typography.bodySmall,
        )
        Text("Valor de la posición · $score", color = GameAccent, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PlayerBranch(branch: MinimaxNode) {
    val move = checkNotNull(branch.move)
    Surface(shape = RoundedCornerShape(12.dp), color = GameBackground, contentColor = GameInk) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${move.from.notation()} → ${move.to.notation()} · Valor ${branch.score}",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (branch.winner != null) {
                Text(winnerLabel(branch.winner), color = GameAccent, style = MaterialTheme.typography.bodySmall)
            } else {
                Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nivel 2 · Máquina (MIN)", color = GameMuted, style = MaterialTheme.typography.labelMedium)
                    for (reply in branch.children) {
                        val replyMove = checkNotNull(reply.move)
                        val value = when {
                            reply.winner != null -> winnerLabel(reply.winner)
                            reply.score == 1 -> "1 salto legal del jugador"
                            else -> "${reply.score} saltos legales del jugador"
                        }
                        Text(
                            "${replyMove.from.notation()} → ${replyMove.to.notation()} · $value",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun winnerLabel(side: Side): String = when (side) {
    Side.Black -> "Gana el jugador"
    Side.White -> "Gana la máquina"
}
