# Tri-Line

An Android implementation of the Tri-Line board game.

## Features

- 3×3 junction board
- Red and Blue tablets
- Red always moves first
- Adjacent movement only
- Starting home row does not count as a win
- Five-fold repetition draw
- One-player mode with Easy / Normal / Hard AI
- Two-player Quick Game
- Two-player tournaments: Best of 3 / 5 / 7
- Player colour selection
- Bluetooth, Local Wi-Fi and Internet multiplayer architecture

## Project structure

```text
app/src/main/java/com/triline/
├── MainActivity.kt
├── game/
│   ├── GameModels.kt
│   ├── GameEngine.kt
│   ├── GameViewModel.kt
│   ├── AiPlayer.kt
│   └── MatchController.kt
├── network/
│   └── NetworkProtocol.kt
└── ui/
    └── TriLineScreen.kt
```

## Rules

1. Red always makes the first move.
2. A tablet moves only to a connected empty junction.
3. Jumping is not permitted.
4. Three tablets in a valid line wins.
5. A player's original home row does not count as a winning line.
6. Repeating the same board position with the same player to move five times results in a draw.
7. In tournaments, players alternate colours between games. Red still moves first.

## Multiplayer

`network/NetworkProtocol.kt` defines the transport-independent messages. Bluetooth, Local Wi-Fi and Internet transports can be implemented behind the same interface.

For Internet play, the server should be authoritative and validate every move.
