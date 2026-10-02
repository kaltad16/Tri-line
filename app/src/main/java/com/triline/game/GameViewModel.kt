package com.triline.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var ai: AiPlayer = AiPlayer(AiDifficulty.NORMAL)
    private val matchController = MatchController()

    fun onJunctionClicked(index: Int) {
        val state = _uiState.value

        if (state.winner != null || state.isDraw) return
        if (index !in 0..8) return

        // In AI mode the human can only move their selected colour.
        if (state.gameMode == GameMode.AI &&
            state.currentPlayer != state.myColor
        ) return

        if (state.board[index] == state.currentPlayer) {
            _uiState.update { it.copy(selectedJunction = index) }
            return
        }

        val from = state.selectedJunction ?: return
        makeMove(from, index)
    }

    private fun makeMove(from: Int, to: Int) {
        val state = _uiState.value
        val next = GameEngine.applyMove(state, from, to) ?: return
        _uiState.value = next

        if (next.winner != null || next.isDraw) {
            finishGame(next)
            return
        }

        if (next.gameMode == GameMode.AI &&
            next.currentPlayer != next.myColor
        ) {
            scheduleAiMove()
        }
    }

    private fun scheduleAiMove() {
        viewModelScope.launch {
            delay(250)
            val state = _uiState.value
            if (state.winner != null || state.isDraw) return@launch
            if (state.gameMode != GameMode.AI) return@launch
            if (state.currentPlayer == state.myColor) return@launch

            val move = ai.chooseMove(state) ?: return@launch
            val next = GameEngine.applyMove(state, move.first, move.second) ?: return@launch
            _uiState.value = next

            if (next.winner != null || next.isDraw) {
                finishGame(next)
            }
        }
    }

    private fun finishGame(state: GameState) {
        val tournament = state.tournament ?: return
        if (state.winner == null && state.isDraw) return

        val complete = matchController.recordResult(state.winner)
        if (complete) {
            _uiState.update {
                it.copy(tournament = matchController.tournament)
            }
        } else {
            _uiState.update {
                it.copy(tournament = matchController.tournament)
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedJunction = null) }
    }

    fun chooseColor(color: Player) {
        require(color == Player.RED || color == Player.BLUE)
        _uiState.update { it.copy(myColor = color) }
    }

    fun setAiDifficulty(difficulty: AiDifficulty) {
        ai = AiPlayer(difficulty)
        _uiState.update { it.copy(aiDifficulty = difficulty) }
    }

    fun startNewGame(
        mode: GameMode,
        myColor: Player,
        difficulty: AiDifficulty = AiDifficulty.NORMAL,
        matchType: MatchType = MatchType.SINGLE_GAME
    ) {
        ai = AiPlayer(difficulty)

        val board = GameState.initialBoard()

        if (matchType != MatchType.SINGLE_GAME) {
            matchController.start(matchType)
        }

        _uiState.value = GameState(
            board = board,
            currentPlayer = Player.RED,
            gameMode = mode,
            myColor = myColor,
            aiDifficulty = difficulty,
            matchType = matchType,
            tournament = if (matchType == MatchType.SINGLE_GAME) {
                null
            } else {
                matchController.tournament
            },
            connectionState = if (mode == GameMode.AI) {
                ConnectionState.CONNECTED
            } else {
                ConnectionState.DISCONNECTED
            },
            positionHistory = listOf(
                GameState.createPositionKey(board, Player.RED)
            )
        )

        // If human selected BLUE, AI controls RED and must start.
        if (mode == GameMode.AI && myColor == Player.BLUE) {
            scheduleAiMove()
        }
    }

    fun resetGame() {
        _uiState.value = GameState()
    }
}
