package com.triline.game

/**
 * Tournament score controller.
 *
 * Player 1 and Player 2 are logical tournament identities.
 * Colours alternate between games, while RED always has the first move.
 */
class MatchController {

    var tournament = TournamentState()
        private set

    fun start(matchType: MatchType) {
        tournament = TournamentState(matchType = matchType)
    }

    fun recordResult(winner: Player?): Boolean {
        if (tournament.tournamentWinner != null) return true

        tournament = when (winner) {
            null, Player.NONE -> tournament.copy(
                draws = tournament.draws + 1
            )
            Player.RED -> tournament.copy(
                player1Wins = tournament.player1Wins + 1
            )
            Player.BLUE -> tournament.copy(
                player2Wins = tournament.player2Wins + 1
            )
        }

        val required = tournament.requiredWins()

        val tournamentWinner = when {
            tournament.player1Wins >= required -> 1
            tournament.player2Wins >= required -> 2
            else -> null
        }

        tournament = tournament.copy(
            tournamentWinner = tournamentWinner,
            gameNumber = if (tournamentWinner == null && winner != null && winner != Player.NONE) {
                tournament.gameNumber + 1
            } else {
                tournament.gameNumber
            }
        )

        return tournamentWinner != null
    }

    fun player1ColorForGame(): Player =
        if (tournament.gameNumber % 2 == 1) Player.RED else Player.BLUE

    fun player2ColorForGame(): Player =
        if (player1ColorForGame() == Player.RED) Player.BLUE else Player.RED
}
