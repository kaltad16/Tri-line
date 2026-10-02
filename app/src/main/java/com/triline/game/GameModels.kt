package com.triline.game

enum class Player {
    RED, BLUE, NONE
}

enum class GameMode {
    AI,
    BLUETOOTH,
    LOCAL_WIFI,
    INTERNET
}

enum class AiDifficulty {
    EASY,
    NORMAL,
    HARD
}

enum class MatchType {
    SINGLE_GAME,
    BEST_OF_3,
    BEST_OF_5,
    BEST_OF_7
}

enum class ConnectionState {
    DISCONNECTED,
    SEARCHING,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class TournamentState(
    val matchType: MatchType = MatchType.BEST_OF_3,
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val draws: Int = 0,
    val gameNumber: Int = 1,
    val tournamentWinner: Int? = null
) {
    fun requiredWins(): Int = when (matchType) {
        MatchType.SINGLE_GAME -> 1
        MatchType.BEST_OF_3 -> 2
        MatchType.BEST_OF_5 -> 3
        MatchType.BEST_OF_7 -> 4
    }
}

data class GameState(
    val board: Map<Int, Player> = initialBoard(),
    val currentPlayer: Player = Player.RED,
    val selectedJunction: Int? = null,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val positionHistory: List<String> =
        listOf(createPositionKey(initialBoard(), Player.RED)),
    val gameMode: GameMode = GameMode.AI,
    val myColor: Player = Player.RED,
    val localPlayer: Player = Player.RED,
    val aiDifficulty: AiDifficulty = AiDifficulty.NORMAL,
    val connectionState: ConnectionState =
        ConnectionState.DISCONNECTED,
    val matchType: MatchType = MatchType.SINGLE_GAME,
    val tournament: TournamentState? = null
) {
    companion object {
        fun initialBoard(): Map<Int, Player> = mapOf(
            0 to Player.RED,
            1 to Player.RED,
            2 to Player.RED,
            3 to Player.NONE,
            4 to Player.NONE,
            5 to Player.NONE,
            6 to Player.BLUE,
            7 to Player.BLUE,
            8 to Player.BLUE
        )

        fun createPositionKey(
            board: Map<Int, Player>,
            playerToMove: Player
        ): String {
            val boardKey = (0..8).joinToString("") {
                when (board[it]) {
                    Player.RED -> "R"
                    Player.BLUE -> "B"
                    Player.NONE, null -> "N"
                }
            }

            val turnKey = when (playerToMove) {
                Player.RED -> "R"
                Player.BLUE -> "B"
                Player.NONE -> "N"
            }

            return "$boardKey$turnKey"
        }
    }
}
