package com.triline.game

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Three practical AI levels.
 *
 * EASY   = tactical win/block + randomness
 * NORMAL = depth-limited alpha-beta
 * HARD   = deeper alpha-beta with transposition cache
 *
 * The board is tiny, so a deeper search is inexpensive.
 */
class AiPlayer(private val difficulty: AiDifficulty) {

    private val maxDepth = when (difficulty) {
        AiDifficulty.EASY -> 1
        AiDifficulty.NORMAL -> 4
        AiDifficulty.HARD -> 8
    }

    private val cache = HashMap<String, ScoredMove>()

    fun chooseMove(state: GameState): Pair<Int, Int>? {
        val moves = GameEngine.legalMoves(state.board, state.currentPlayer)
        if (moves.isEmpty()) return null

        return when (difficulty) {
            AiDifficulty.EASY -> chooseEasy(state, moves)
            AiDifficulty.NORMAL, AiDifficulty.HARD ->
                chooseSearch(state, moves)
        }
    }

    private fun chooseEasy(
        state: GameState,
        moves: List<Pair<Int, Int>>
    ): Pair<Int, Int> {
        val immediateWin = moves.firstOrNull { isWinningMove(state, it) }
        if (immediateWin != null) return immediateWin

        val opponent = opponent(state.currentPlayer)
        val block = moves.firstOrNull { move ->
            val after = GameEngine.applyMove(state, move.first, move.second) ?: return@firstOrNull false
            GameEngine.legalMoves(after.board, opponent).any { reply ->
                val replyState = GameEngine.applyMove(after, reply.first, reply.second)
                replyState?.winner == opponent
            }
        }

        // Easy deliberately does not always find the tactical block.
        if (block != null && Random.nextBoolean()) return block

        return moves.random()
    }

    private fun chooseSearch(
        state: GameState,
        moves: List<Pair<Int, Int>>
    ): Pair<Int, Int> {
        cache.clear()

        val maximizingPlayer = state.currentPlayer
        var bestScore = Int.MIN_VALUE
        var bestMove = moves.first()

        for (move in orderedMoves(state, moves)) {
            val next = GameEngine.applyMove(state, move.first, move.second) ?: continue

            val score = if (next.winner == maximizingPlayer) {
                WIN_SCORE
            } else {
                minimax(
                    next,
                    maxDepth - 1,
                    maximizingPlayer,
                    alpha = Int.MIN_VALUE + 1,
                    beta = Int.MAX_VALUE
                )
            }

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
    }

    private fun minimax(
        state: GameState,
        depth: Int,
        maximizingPlayer: Player,
        alpha: Int,
        beta: Int
    ): Int {
        if (state.winner != null) {
            return if (state.winner == maximizingPlayer) {
                WIN_SCORE + depth
            } else {
                -WIN_SCORE - depth
            }
        }

        if (state.isDraw) return 0
        if (depth <= 0) return evaluate(state, maximizingPlayer)

        val key = "${GameState.createPositionKey(state.board, state.currentPlayer)}:$depth:$maximizingPlayer"
        cache[key]?.let { return it.score }

        val moves = GameEngine.legalMoves(state.board, state.currentPlayer)
        if (moves.isEmpty()) return evaluate(state, maximizingPlayer)

        var localAlpha = alpha
        var localBeta = beta

        val maximizing = state.currentPlayer == maximizingPlayer
        var best = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE

        for (move in orderedMoves(state, moves)) {
            val next = GameEngine.applyMove(state, move.first, move.second) ?: continue
            val value = minimax(next, depth - 1, maximizingPlayer, localAlpha, localBeta)

            if (maximizing) {
                best = max(best, value)
                localAlpha = max(localAlpha, best)
            } else {
                best = min(best, value)
                localBeta = min(localBeta, best)
            }

            if (localBeta <= localAlpha) break
        }

        cache[key] = ScoredMove(best)
        return best
    }

    private fun evaluate(state: GameState, player: Player): Int {
        val opponent = opponent(player)

        var score = 0
        val myMoves = GameEngine.legalMoves(state.board, player).size
        val opponentMoves = GameEngine.legalMoves(state.board, opponent).size

        score += (myMoves - opponentMoves) * 15

        if (state.board[4] == player) score += 40
        if (state.board[4] == opponent) score -= 40

        score += immediateThreats(state.board, player) * 120
        score -= immediateThreats(state.board, opponent) * 140

        return score
    }

    private fun immediateThreats(
        board: Map<Int, Player>,
        player: Player
    ): Int {
        var threats = 0
        for (move in GameEngine.legalMoves(board, player)) {
            val tempState = GameState(
                board = board,
                currentPlayer = player,
                positionHistory = emptyList()
            )
            if (isWinningMove(tempState, move)) threats++
        }
        return threats
    }

    private fun isWinningMove(
        state: GameState,
        move: Pair<Int, Int>
    ): Boolean {
        val next = GameEngine.applyMove(state, move.first, move.second)
        return next?.winner == state.currentPlayer
    }

    private fun orderedMoves(
        state: GameState,
        moves: List<Pair<Int, Int>>
    ): List<Pair<Int, Int>> {
        return moves.sortedByDescending { move ->
            var score = 0
            if (move.second == 4) score += 100
            if (isWinningMove(state, move)) score += 10000
            score
        }
    }

    private fun opponent(player: Player): Player =
        if (player == Player.RED) Player.BLUE else Player.RED

    private data class ScoredMove(val score: Int)

    companion object {
        private const val WIN_SCORE = 100_000
    }
}
