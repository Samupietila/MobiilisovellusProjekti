# 🎨 Guess My Doodle

![Platform](https://img.shields.io/badge/platform-Android-blue)
![Language](https://img.shields.io/badge/language-Kotlin-orange)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-7950F2?logo=android)
![Bluetooth](https://img.shields.io/badge/Bluetooth-BLE-007BFF?logo=bluetooth)


**Guess My Doodle** is an Android app where one player draws a given word, and the other tries to guess what it is. The app is built using **Kotlin** and **Jetpack Compose** in Android Studio and connects players via **Bluetooth**. The goal is to blend creativity and collaboration in a fun mobile experience.

---

<details>
  <summary>Table of Contents</summary>
  
1. [Features](#features)
2. [Technologies and Tools](#technologies-and-tools)
3. [Installation](#installation)
4. [Screenshots](#screenshots)
5. [Architecture](#architecture)
6. [Project Status](#project-status)
7. [Roadmap](#roadmap)
8. [Developers](#developers)
9. [License](#license)

</details>

---

<div align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="Project Logo" width="250" height="250">
</div>

## Features
- ✏️ Draw using the touchscreen
- 🔄 Random word generation
- 📱 Bluetooth connection between two devices
- 🧩 Word guessing logic
- ⚡ Real-time interaction
- 🎨 Clean and responsive UI with Jetpack Compose, utilizing a custom theme



## Technologies and Tools

- **Kotlin** – Primary language
- **Jetpack Compose** – Modern UI toolkit
- **Android Studio** – Development environment
- **Kotlin-BLE** – For peer-to-peer communication



## Installation

**Requirements:**
- 📱 Two physical Android devices running Android 13 or newer (for Bluetooth testing)
- 💻 Android Studio Bumblebee or newer installed on your computer

1. Clone the repository:
   ```bash
   git clone https://github.com/Samupietila/MobiilisovellusProjekti.git
2. Open the project in Android Studio.
3. Build and run the app on two physical Android devices.
4. Ensure Bluetooth is enabled on both devices for multiplayer functionality.

## Screenshots
- 🌓 The app supports both dark and light themes for an optimal user experience.

<p align="center">
  <strong>🌙 Dark Theme</strong>
</p>
<p align="center">
  <img src="images/home.png" alt="Home screen" width="160">
  <img src="images/find1.png" alt="Find screen" width="160">
  <img src="images/hat.png" alt="Draw screen" width="160">
  <img src="images/guess.png" alt="Guess screen" width="160">
  <img src="images/end.png" alt="End screen" width="160">
</p>

<p align="center">
  <strong>☀️ Light Theme</strong>
</p>
<p align="center">
  <img src="images/light1.png" alt="Light 1" width="160">
  <img src="images/light2.png" alt="Light 2" width="160">
  <img src="images/light3.png" alt="Light 3" width="160">
  <img src="images/light4.png" alt="Light 4" width="160">
  <img src="images/light5.png" alt="Light 5" width="160">
</p>


## Architecture

The app follows the **MVVM (Model-View-ViewModel)** pattern to separate UI and logic.
It uses **Jetpack Compose** for the UI, **Bluetooth (Kotlin-BLE)** for real-time device-to-device communication, and **ViewModel** for state management.
Asynchronous operations are handled using **Kotlin Coroutines**, and **Room** is used for local data storage.


## Project Status
- MVP Completed
- Polish & Improvements in Progress

## Roadmap
- [ ] 📊 Track player scores and game statistics
- [ ] 👥 Add support for multiplayer/group mode (more than 2 players)
- [ ] 🧾 Add game history tracking (view past games and results)
- [ ] 🌍 Explore cross-platform support with Kotlin Multiplatform
- [ ] 📇 Implement contact system for inviting friends

## Developers

| [@jukkiss](https://github.com/jukkiss) | [@samupietila](https://github.com/samupietila) | [@mikagronroos2](https://github.com/mikagronroos2) | [@annikannisto](https://github.com/annikannisto) |
|:--:|:--:|:--:|:--:|
| <img src="https://github.com/jukkiss.png" width="70" height="70"> | <img src="https://github.com/samupietila.png" width="70" height="70"> | <img src="https://github.com/mikagronroos2.png" width="70" height="70"> | <img src="https://github.com/annikannisto.png" width="70" height="70"> |



## License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
You can view the full license [here](https://creativecommons.org/licenses/by-nc/4.0/).
