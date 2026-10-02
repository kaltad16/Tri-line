package com.triline.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import com.triline.game.GameEngine
import com.triline.game.GameViewModel
import com.triline.game.Player

@Composable
fun TriLineScreen(
    viewModel: GameViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                state.winner == Player.RED -> "RED WINS!"
                state.winner == Player.BLUE -> "BLUE WINS!"
                state.isDraw -> "DRAW"
                state.currentPlayer == Player.RED -> "RED'S TURN"
                else -> "BLUE'S TURN"
            },
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(
                    Color(0xFFD5A879),
                    androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                )
                .padding(28.dp)
        ) {
            BoardCanvas(
                board = state.board,
                winner = state.winner
            )

            BoardPieces(
                board = state.board,
                selected = state.selectedJunction,
                enabled = state.winner == null && !state.isDraw,
                onClick = viewModel::onJunctionClicked
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = viewModel::resetGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("NEW GAME")
        }
    }
}

@Composable
private fun BoardCanvas(
    board: Map<Int, Player>,
    winner: Player?
) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val p = mapOf(
            0 to Offset(0f, 0f),
            1 to Offset(cx, 0f),
            2 to Offset(w, 0f),
            3 to Offset(0f, cy),
            4 to Offset(cx, cy),
            5 to Offset(w, cy),
            6 to Offset(0f, h),
            7 to Offset(cx, h),
            8 to Offset(w, h)
        )

        val connections = listOf(
            0 to 1, 1 to 2,
            0 to 3, 3 to 6,
            2 to 5, 5 to 8,
            6 to 7, 7 to 8,
            3 to 4, 4 to 5,
            1 to 4, 4 to 7,
            0 to 4, 4 to 8,
            2 to 4, 4 to 6
        )

        connections.forEach { (a, b) ->
            drawLine(
                color = Color(0xFF3E281D),
                start = p[a]!!,
                end = p[b]!!,
                strokeWidth = 7f,
                cap = StrokeCap.Round
            )
        }

        if (winner != null) {
            GameEngine.winningLine(board, winner)?.let { line ->
                val glow =
                    if (winner == Player.RED)
                        Color(0xFFFF665C)
                    else
                        Color(0xFF55A8FF)

                drawLine(
                    color = glow.copy(alpha = .25f),
                    start = p[line.first()]!!,
                    end = p[line.last()]!!,
                    strokeWidth = 26f,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = glow,
                    start = p[line.first()]!!,
                    end = p[line.last()]!!,
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun BoardPieces(
    board: Map<Int, Player>,
    selected: Int?,
    enabled: Boolean,
    onClick: (Int) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        repeat(9) { index ->
            val row = index / 3
            val col = index % 3

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = if (col == 0) 0.dp else 0.dp
                        )
                ) {
                    val x = when (col) {
                        0 -> Alignment.Start
                        1 -> Alignment.CenterHorizontally
                        else -> Alignment.End
                    }

                    val y = when (row) {
                        0 -> Alignment.Top
                        1 -> Alignment.CenterVertically
                        else -> Alignment.Bottom
                    }

                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = when {
                            row == 0 && col == 0 -> Alignment.TopStart
                            row == 0 && col == 1 -> Alignment.TopCenter
                            row == 0 && col == 2 -> Alignment.TopEnd
                            row == 1 && col == 0 -> Alignment.CenterStart
                            row == 1 && col == 1 -> Alignment.Center
                            row == 1 && col == 2 -> Alignment.CenterEnd
                            row == 2 && col == 0 -> Alignment.BottomStart
                            row == 2 && col == 1 -> Alignment.BottomCenter
                            else -> Alignment.BottomEnd
                        }
                    ) {
                        val player = board[index] ?: Player.NONE

                        Box(
                            Modifier
                                .size(62.dp)
                                .clickable(
                                    enabled = enabled,
                                    onClick = { onClick(index) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (player == Player.NONE) {
                                Box(
                                    Modifier
                                        .size(14.dp)
                                        .background(
                                            Color(0xFF3E281D),
                                            CircleShape
                                        )
                                )
                            } else {
                                Tablet(
                                    player = player,
                                    selected = selected == index
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tablet(
    player: Player,
    selected: Boolean
) {
    val color =
        if (player == Player.RED)
            Color(0xFFE53935)
        else
            Color(0xFF1976D2)

    Box(
        Modifier
            .size(if (selected) 52.dp else 46.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (player == Player.RED) "R" else "B",
            color = Color.White
        )
    }
}
