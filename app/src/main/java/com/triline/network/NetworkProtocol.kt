package com.triline.network

import com.triline.game.MatchType
import com.triline.game.Player

/**
 * Transport-independent multiplayer protocol.
 *
 * The same protocol can be carried over:
 * - Bluetooth RFCOMM sockets
 * - Local Wi-Fi / Nearby Connections
 * - Internet WebSocket
 *
 * The Internet server must validate every Move.
 */
sealed interface NetworkMessage {

    data class Hello(
        val protocolVersion: Int = 1,
        val playerId: String
    ) : NetworkMessage

    data class RoomCreated(
        val roomCode: String,
        val assignedColor: Player
    ) : NetworkMessage

    data class PlayerJoined(
        val playerId: String
    ) : NetworkMessage

    data class Move(
        val from: Int,
        val to: Int,
        val sequence: Long
    ) : NetworkMessage

    data class StateSnapshot(
        val board: List<String>,
        val currentPlayer: Player,
        val sequence: Long
    ) : NetworkMessage

    data class TournamentStart(
        val matchType: MatchType
    ) : NetworkMessage

    data class GameResult(
        val winner: Player?,
        val gameNumber: Int
    ) : NetworkMessage

    data class Rematch(
        val gameNumber: Int
    ) : NetworkMessage

    data class Resign(
        val player: Player
    ) : NetworkMessage

    data object Ping : NetworkMessage
    data object Pong : NetworkMessage
}

interface GameTransport {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun send(message: NetworkMessage)
    fun setListener(listener: (NetworkMessage) -> Unit)
}
