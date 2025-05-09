## Technical Documentation – *Guess My Doodle*

### 1. Introduction

* Purpose of the application

  * *Guess My Doodle* is a multiplayer Android app where one player draws a randomly generated word and the other tries to guess it. The goal is to provide a fun and collaborative experience using creative drawing and Bluetooth-based interaction.
* Target audience

  * Android users interested in casual, creative multiplayer games.
* Brief description of the main features

  * Drawing with touch, guessing the word, Bluetooth peer-to-peer connection, and real-time interaction.

### 2. Architecture and Structure

* Architecture used: MVVM (Model-View-ViewModel)
* Components communicate via ViewModel (state management), Bluetooth communication layer, and local Room database.
* Technologies: Kotlin, Jetpack Compose, Room, Bluetooth BLE, Coroutines

### 3. Project Structure

* Overview of key folders and files


```
C:.
├── MainActivity.kt
├── Permissions.kt

├── data
│   ├── DefaultWords.kt
│   ├── Word.kt
│   ├── WordDao.kt
│   ├── WordDatabase.kt
│   └── WordRepository.kt

├── screens
│   ├── navigation
│   │   └── Navigation.kt
│   └── screens
│       ├── BTConnect.kt
│       ├── CanvasControls.kt
│       ├── Contacts.kt
│       ├── DrawingCanvas.kt
│       ├── DrawScreen.kt
│       ├── GameScreen.kt
│       ├── GuessScreen.kt
│       ├── History.kt
│       ├── Home.kt
│       ├── NewProfile.kt
│       ├── Player.kt
│       ├── Statistics.kt
│       ├── Test.kt
│       └── WordScreen.kt

├── ui
│   └── theme
│       ├── ButtonStyles.kt
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt

└── ViewModels
    ├── BluetoothViewModel.kt
    ├── ChatBleServer.kt
    ├── ChatViewModel.kt
    ├── DrawingViewModel.kt
    ├── GameViewModel.kt
    └── WordViewModel.kt
```


### 4. Database

* Room is used for local storage.
* Data includes words to draw and possibly saved games (future roadmap).
* One or more tables used for managing word list and game history.

### 5. Core Features

* What can the user do?

  * Draw using the touchscreen
  * Guess words
  * Connect via Bluetooth
  * Experience real-time multiplayer gameplay
* What happens in the background?

  * View observes ViewModel state
  * ViewModel handles game logic and state
  * BLE layer transmits data between players
  * Room may be used for persisting data (e.g., words)

### 6. Screens Overview

* Home Screen – Choose to host or join a game
* Find Screen – Discover nearby Bluetooth devices
* Draw Screen – One player draws the given word
* Guess Screen – Second player guesses the word
* End Screen – Show results and options to restart

### 7. External Libraries

* Room – Local storage
* Jetpack Compose – UI
* kotlinx.coroutines – Async operations
* Kotlin-BLE – Bluetooth communication

### 8. Testing

* Manual testing with two physical Android devices (required for Bluetooth functionality)
* Basic functional testing to verify game flow (e.g., draw → guess → result)
* No unit tests included in current MVP

### 9. Known Limitations or Future Improvements

* No support yet for multiplayer with more than 2 players
* No score or game history tracking yet
* Roadmap includes support for statistics, multiplayer extensions, and friend invitations
* Limited to Android devices with Bluetooth and Android 13+
