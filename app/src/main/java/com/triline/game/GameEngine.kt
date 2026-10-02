package com.triline.game

object GameEngine {

    val adjacencies: Map<Int, List<Int>> = mapOf(
        0 to listOf(1, 3, 4),
        1 to listOf(0, 2, 4),
        2 to listOf(1, 5, 4),
        3 to listOf(0, 4, 6),
        4 to listOf(0, 1, 2, 3, 5, 6, 7, 8),
        5 to listOf(2, 4, 8),
        6 to listOf(3, 4, 7),
        7 to listOf(6, 8, 4),
        8 to listOf(5, 7, 4)
    )

    val winningLines = listOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    fun legalMoves(
        board: Map<Int, Player>,
        player: Player
    ): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()

        for ((from, occupant) in board) {
            if (occupant != player) continue

            for (to in adjacencies[from].orEmpty()) {
                if (board[to] == Player.NONE) {
                    moves += from to to
                }
            }
        }

        return moves
    }

    fun applyMove(
        state: GameState,
        from: Int,
        to: Int
    ): GameState? {
        if (state.winner != null || state.isDraw) return null
        if (state.board[from] != state.currentPlayer) return null
        if (state.board[to] != Player.NONE) return null
        if (to !in adjacencies[from].orEmpty()) return null

        val newBoard = state.board.toMutableMap()
        newBoard[from] = Player.NONE
        newBoard[to] = state.currentPlayer

        val winner = checkWin(newBoard, state.currentPlayer)

        val nextPlayer = if (state.currentPlayer == Player.RED) {
            Player.BLUE
        } else {
            Player.RED
        }

        val key = GameState.createPositionKey(newBoard, nextPlayer)
        val history = state.positionHistory + key
        val draw = !winner && history.count { it == key } >= 5

        return state.copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            selectedJunction = null,
            winner = if (winner) state.currentPlayer else null,
            isDraw = draw,
            positionHistory = history
        )
    }

    fun checkWin(
        board: Map<Int, Player>,
        player: Player
    ): Boolean {
        return winningLines.any { line ->
            if (player == Player.RED && line == listOf(0, 1, 2)) return@any false
            if (player == Player.BLUE && line == listOf(6, 7, 8)) return@any false

            line.all { board[it] == player }
        }
    }

    fun winningLine(
        board: Map<Int, Player>,
        player: Player
    ): List<Int>? {
        return winningLines.firstOrNull { line ->
            if (player == Player.RED && line == listOf(0, 1, 2)) return@firstOrNull false
            if (player == Player.BLUE && line == listOf(6, 7, 8)) return@firstOrNull false

            line.all { board[it] == player }
        }
    }
}
