## Technical Documentation – *Guess My Doodle*

### 1. Introduction

* Purpose of the application

* *Guess My Doodle* is a two-player Android game where one player draws a randomly generated word, and the other tries to guess it. The game is designed to offer a fun and collaborative experience through creative drawing and real-time Bluetooth communication between two nearby devices.
* Target audience

  * Android users interested in casual, creative multiplayer games.
* Brief description of the main features

  * Drawing with touch, guessing the word, Bluetooth peer-to-peer connection, and real-time interaction.

### 2. Architecture and Structure

* Architecture used: MVVM (Model-View-ViewModel)
* Components communicate via ViewModel (state management), Bluetooth communication layer, and local Room database.
* Technologies: Kotlin, Jetpack Compose, Room, Bluetooth BLE, Coroutines
* Flowchart [Flowchart](documentation/README.md)

### 3. Project Structure

* Overview of key folders and files


```plaintext
C:.
├── MainActivity.kt              // Entry point of the app, main activity
├── Permissions.kt              // Handles Bluetooth and other permission requests
├── project_structure.txt       // Printed directory structure for documentation

├── data                        // Data model and Room database components
│   ├── DefaultWords.kt         // List of default words to be drawn
│   ├── Word.kt                 // Word entity used by Room database
│   ├── WordDao.kt              // DAO interface for accessing and storing words
│   ├── WordDatabase.kt         // Defines the Room database
│   └── WordRepository.kt       // Repository layer between database and ViewModel

├── screens                     // UI screens and game logic
│   ├── navigation
│   │   └── Navigation.kt       // Navigation setup between different screens
│   └── screens
│       ├── BTConnect.kt        // Screen for setting up Bluetooth connection
│       ├── CanvasControls.kt   // Controls for the drawing canvas
│       ├── Contacts.kt         // Screen for viewing or adding contacts (future feature)
│       ├── DrawingCanvas.kt    // Actual drawing area (Canvas)
│       ├── DrawScreen.kt       // Screen where the drawer draws the given word
│       ├── GameScreen.kt       // Main game screen during play
│       ├── GuessScreen.kt      // Screen where the guesser tries to guess the word
│       ├── History.kt          // Screen for viewing past games (future feature)
│       ├── Home.kt             // Start or main menu screen
│       ├── NewProfile.kt       // Screen to create a new player profile (future feature)
│       ├── Player.kt           // Displays or manages player data (future feature)
│       ├── Statistics.kt       // Screen showing game stats and success rate (future feature)
│       ├── Test.kt             // Possible development/testing screen
│       └── WordScreen.kt       // Manages or displays the list of words

├── ui
│   └── theme
│       ├── ButtonStyles.kt     // Custom Material3 button color styles (primary, secondary, outlined)
│       ├── Theme.kt            // Main Material3 theme setup, including color scheme and typography   

└── ViewModels                  // ViewModels following MVVM architecture
    ├── BluetoothViewModel.kt   // Manages Bluetooth state and logic
    ├── ChatBleServer.kt        // BLE server handling connections and messaging
    ├── ChatViewModel.kt        // ViewModel for chat and messaging logic
    ├── DrawingViewModel.kt     // Manages state for drawing interactions
    ├── GameViewModel.kt        // Controls game logic and state transitions
    └── WordViewModel.kt        // Handles state related to word management
```



### 4. Database

* Room is used for local storage.
* Data includes words to draw.

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

* Jetpack Compose – Modern declarative UI framework (includes Material 3, Navigation, Tooling)
* Room – Local database storage (Room Runtime, KTX, Compiler)
* Lifecycle & ViewModel – State management and lifecycle support (LiveData, ViewModel Compose, Runtime KTX)
* Kotlinx Coroutines – Asynchronous operations
* JUnit & Espresso – Unit and UI testing
* Kotlin-BLE (Nordic Semiconductor) – Bluetooth Low Energy communication (Client, Server, Scanner, Advertiser)

### 8. Testing

* Manual testing with two physical Android devices (required for Bluetooth functionality)
* Basic functional testing to verify game flow (e.g., draw → guess → result)

### 9. Known Limitations or Future Improvements

* No support yet for multiplayer with more than 2 players
* No score or game history tracking yet
* Roadmap includes support for statistics, multiplayer extensions, and friend invitations
* Limited to Android devices with Bluetooth and Android 13+



