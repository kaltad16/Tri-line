# Architecture

## Game layer

`GameEngine` is the authoritative local rules engine.

It owns:

- legal movement
- winning lines
- home-row exclusion
- five-fold repetition

`GameViewModel` owns UI state and user actions.

## AI

`AiPlayer` is isolated from the game engine.

Difficulty levels:

- EASY: immediate wins + random legal play
- NORMAL: tactical one-ply evaluation
- HARD: reserved for deeper minimax / alpha-beta implementation

## Multiplayer

All transports should deliver the same `NetworkMessage` types.

Recommended implementations:

- Bluetooth: Bluetooth sockets
- Local Wi-Fi: Wi-Fi Direct / Nearby Connections
- Internet: WebSocket client + authoritative server

The Internet server must validate moves rather than trusting the client.

## Tournament

`MatchController` handles:

- Single game
- Best of 3
- Best of 5
- Best of 7
- player scores
- game number
- tournament winner

Players should alternate colours between tournament games while Red remains the first mover.
