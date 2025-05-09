#### 1. Johdanto

* Sovelluksen tarkoitus
* Kohderyhmä
* Lyhyt kuvaus päätoiminnoista

#### 2. Arkkitehtuuri ja rakenne

* Käytetty arkkitehtuuri (esim. MVVM, MVC)
* Kuvaus siitä, miten eri osat keskustelevat (esim. ViewModel ⇄ Repository ⇄ Room)
* Käytetyt kielet ja työkalut (esim. Kotlin, Jetpack Compose, Room)

#### 3. Tiedostorakenne

* Lyhyt selitys tärkeimmistä hakemistoista ja tiedostoista

  ```
  /data
    └── entity/Word.kt          // Tietokannan sanaolio
    └── dao/WordDao.kt          // Rajapinta tietokantakyselyille
    └── repository/WordRepository.kt
  /ui
    └── screens/WordScreen.kt   // Näyttää listan sanoista
  /viewmodels
    └── WordViewModel.kt
  ```

#### 4. Tietokanta

* Käytetty tietokantateknologia (esim. Room)
* Tietokannan rakenne (taulut, sarakkeet)
* Mahdollinen skeemakaavio

#### 5. Päätoiminnot (features)

* Mitä käyttäjä voi tehdä?

  * Esim. "Käyttäjä voi lisätä uuden sanan painamalla 'Lisää' -painiketta"
* Mitä tapahtuu taustalla?

  * View kutsuu ViewModelia → ViewModel päivittää Repositorya → Repository tekee työn DAO\:n kautta

#### 6. Näyttöjen toiminta

* Lyhyet kuvaukset eri näytöistä

  * Mikä komponentti vastaa mistäkin?
  * Mitä muuttujia käytetään? (StateFlow, LiveData?)
  * Miten navigointi tapahtuu?

#### 7. Käytetyt kirjastot

* Lista ulkopuolisista kirjastoista

  * Esim. Room, kotlinx.coroutines, Material3

#### 8. Testaus

* Onko sovelluksella yksikkötestejä? Miten testaus on järjestetty?
* Esimerkki yhdestä testitapauksesta

#### 9. Tunnetut rajoitteet tai jatkokehitysideoita

* Mitä ominaisuuksia jäi puuttumaan tai voisi kehittää myöhemmin?



