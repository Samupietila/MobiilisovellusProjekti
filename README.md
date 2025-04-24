# 🎨 Guess My Doodle

**Guess My Doodle** is an Android app where one player draws a given word, and the other tries to guess what it is. The app is built using **Kotlin** and **Jetpack Compose** in Android Studio and connects players via **Bluetooth**. The goal is to blend creativity and collaboration in a fun mobile experience.

---

## 📱 Features

- Draw using the touchscreen
- Random word generation
- Bluetooth connection between two devices
- Word guessing logic
- Real-time interaction
- Clean and responsive UI with Jetpack Compose


## 🛠️ Technologies & Tools

- **Kotlin** – Primary language
- **Jetpack Compose** – Modern UI toolkit
- **Android Studio** – Development environment
- **Kotlin-BLE** – For peer-to-peer communication


## ⚙️ Installation

1. Make sure you have Android Studio (Bumblebee or newer) installed.
2. Clone the project:
   ```bash
   git clone https://github.com/Samupietila/MobiilisovellusProjekti.git
3. Open the project in Android Studio.
4. Run the app on two physical Android devices to test Bluetooth features.
   

## 📸 Screenshots

- Coming soon...


## 🏗️ Architecture


## ✅ Testing

- Instructions or strategy on how the app is tested (unit tests, UI tests, etc.) coming soon.


## 📂 Folder Structure
app/ ├── manifests/ │ └── AndroidManifest.xml ├── kotlin+java/ │ └── com.example.mobiilisovellusprojekti/ │ ├── data/ │ │ ├── Word.kt │ │ ├── WordDao.kt │ │ ├── WordDatabase.kt │ │ └── WordRepository.kt │ ├── screens/ │ │ ├── navigation/ │ │ └── screens/ │ ├── ui.theme/ │ │ ├── ButtonStyles.kt │ │ ├── Color.kt │ │ ├── Theme.kt │ │ └── Type.kt │ ├── ViewModels/ │ │ ├── BluetoothViewModel.kt │ │ ├── DrawingViewModel.kt │ │ └── WordViewModel.kt │ └── MainActivity.kt ├── test/ │ ├── WordDaoTest.kt │ ├── DrawingViewModelTest.kt │ └── ExampleUnitTest.kt ├── androidTest/ │ ├── ExampleInstrumentedTest.kt │ └── WordDaoTest.kt ├── res/ │ ├── drawable/ │ │ ├── eraser.png │ │ ├── logo.png │ │ ├── ic_launcher_background.xml │ │ └── ic_launcher_foreground.xml │ ├── mipmap/ │ ├── values/ │ └── xml/ ├── build.gradle.kts ├── proguard-rules.pro


## 🚧 Project Status
- MVP Completed
- Polish & Improvements in Progress

## 🛣️ Roadmap

## 👨‍💻 Developers

| [@jukkiss](https://github.com/jukkiss) | [@samupietila](https://github.com/samupietila) | [@mikagronroos2](https://github.com/mikagronroos2) | [@annikannisto](https://github.com/annikannisto) |
|:--:|:--:|:--:|:--:|
| <img src="https://github.com/jukkiss.png" width="70" height="70"> | <img src="https://github.com/samupietila.png" width="70" height="70"> | <img src="https://github.com/mikagronroos2.png" width="70" height="70"> | <img src="https://github.com/annikannisto.png" width="70" height="70"> |


## 📄 License



