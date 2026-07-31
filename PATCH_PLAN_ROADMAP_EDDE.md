# EDDE+ ROADMAP NAPRAWCZA: Persona Colony (Od Prototypu do Produkcji)

Poniższy plan naprawczy adresuje wszystkie krytyczne luki (P0-P8) wykryte w audycie (2026-07-26). Skupiamy się na weryfikowalnym działaniu (Evidence-Based Execution), usunięciu fałszywych sukcesów (Fake-Success) oraz zabezpieczeniu cyklu życia i danych aplikacji.

---

## 🛠️ FAZA 1: INFRASTRUKTURA, BUILD I BEZPIECZEŃSTWO DANYCH (P0)

### KROK 1.1: Naprawa Gradle Wrappera (P0-2)
* **Zadanie:** Usunięcie uszkodzonego pliku `gradle-wrapper.jar` i regeneracja.
* **Akcja:** Wykonanie komendy `gradle wrapper --gradle-version 9.3.1` w czystym środowisku (lub ręczna aktualizacja plików). 
* **Weryfikacja:** `./gradlew clean` kończy się sukcesem bez błędów "Invalid or corrupt jarfile".

### KROK 1.2: Konfiguracja podpisywania Release (P0-3)
* **Zadanie:** Usunięcie fallbacku do `debug` dla wariantu `release` w `app/build.gradle.kts`.
* **Akcja:** Wymuszenie błędu budowania, jeśli brakuje prawidłowego pliku Keystore dla Release.
* **Weryfikacja:** Próba zbudowania `./gradlew assembleRelease` bez odpowiednich zmiennych środowiskowych powinna zakończyć się błędem, a nie sukcesem z certyfikatem "Android Debug".

### KROK 1.3: Szyfrowanie Kluczy API (P0-4)
* **Zadanie:** Usunięcie kluczy API Gemini/OpenRouter ze zwykłego `DataStore`.
* **Akcja:** Zmodyfikowanie `AgentPreferencesRepository.kt`, aby używał `LocalEncryptedVault` (Android Keystore + AES-GCM) do zapisu i odczytu wrażliwych tokenów. Jawne wykluczenie tych danych z reguł `android:allowBackup`.
* **Weryfikacja:** Odczytanie pliku XML preferencji na zrootowanym emulatorze musi zwracać losowy ciąg bajtów, a nie plain-text API keys.

### KROK 1.4: Ochrona Bazy Danych (P0-7)
* **Zadanie:** Usunięcie destrukcyjnego fallbacku migracji w Room.
* **Akcja:** W pliku `AppDatabase.kt` usunąć `fallbackToDestructiveMigration()`.
* **Weryfikacja:** Zmiana numeru wersji bazy bez podania migracji rzuca `IllegalStateException` podczas uruchomienia zamiast kasować dane użytkownika.

---

## ⚡ FAZA 2: INTEGRALNOŚĆ WYKONANIA I KONTROLA STANU (P0/P1)

### KROK 2.1: Likwidacja Fake-Success w ExecutionEngine (P0-1)
* **Zadanie:** Zamiana udawanej egzekucji na rzeczywistą.
* **Akcja (Kalendarz):** W `ExecutionEngine` dla `SYNC_CALENDAR` użyć rzeczywistego `CalendarContract` do dodania/odczytania wydarzeń i zapisać to w `ExecutionEvidence`.
* **Akcja (WorkManager):** Dla `RULE_EVALUATION` wywołać `WorkManager.getInstance().enqueue(request)`, pobrać `workRequestId` i dopiero po sprawdzeniu statusu workera nadać status `EXECUTED`.
* **Weryfikacja:** Baza danych zgłasza błąd `FAILED`, jeśli usługa sprzętowa/kalendarza rzuci wyjątek, zamiast zawsze raportować `EXECUTED`.

### KROK 2.2: Rzeczywisty Panic Mode / Kill Switch (P0-6)
* **Zadanie:** Zatrzymanie procesów w tle, a nie tylko zmiana statusu w bazie.
* **Akcja:** `PanicTileService` wywołuje globalnego EventBusa lub specjalną metodę w repozytorium, która: 
  - robi `WorkManager.cancelAllWork()`,
  - anuluje `CoroutineScope` silnika wykonawczego,
  - robi `server.stop()` dla Ktor ApiGateway.
* **Weryfikacja:** Uruchomienie "Panic" przerywa trwające odliczania i blokuje nasłuch sieciowy HTTP w ułamku sekundy.

### KROK 2.3: Modernizacja Zadań w Tle (P0-8)
* **Zadanie:** Usunięcie nieskończonego Foreground Service dla synchronizacji.
* **Akcja:** Zamiana `AgentRestSchedulerService` na `PeriodicWorkRequestBuilder` w WorkManagerze z odpowiednimi `Constraints` (np. wymaga sieci).
* **Weryfikacja:** Brak notyfikacji na pasku udającej nieskończoną aktywność; system usypia proces, a zadania wyzwalane są co np. 15 minut.

---

## 🔒 FAZA 3: POLITYKI BEZPIECZEŃSTWA I ARCHITEKTURA (P0/P1)

### KROK 3.1: Centralny Policy Enforcement Point (P0-5)
* **Zadanie:** Wymuszenie respektowania przez agentów progów autonomii i ustawień prywatności z UI.
* **Akcja:** Implementacja `PolicyGuard.kt` - każda operacja I/O (np. dostęp do kalendarza lub wywołanie LLM API) musi asynchronicznie odpytać `PolicyGuard`. Jeśli użytkownik wyłączył `allowDataAccess`, system ma zwrócić wyjście `BLOCKED`.
* **Weryfikacja:** Wyłączenie autoryzacji w "Settings" fizycznie blokuje żądania (Network Profiler rejestruje brak wysyłanych requestów HTTP).

### KROK 3.2: Redukcja Uprawnień Manifestu (P1)
* **Zadanie:** Usunięcie "na wszelki wypadek" nadmiarowych pozwoleń.
* **Akcja:** Usunięcie `QUERY_ALL_PACKAGES` i nieuzasadnionych uprawnień rejestru połączeń (jeśli nie implementujemy asystenta głosowego pełniącego funkcję dialera).
* **Weryfikacja:** Inspekcja złączonego `AndroidManifest.xml` nie wykazuje oflagowanych "Dangerous Permissions", których aplikacja de facto nie używa.

### KROK 3.3: Refaktoryzacja "God Objects" (P2)
* **Zadanie:** Rozbicie `DashboardScreen.kt` (2700 linii) i `ColonyViewModel.kt` (1900 linii).
* **Akcja:** Modularne wydzielenie mniejszych komponentów (np. `AgentListWidget`, `ExecutionStatusWidget`) oraz podział `ColonyViewModel` na oddzielne domeny (np. `AgentsViewModel`, `MissionsViewModel`, `SettingsViewModel`).

---

## 🎭 FAZA 4: TRANSPARENTNOŚĆ I MOCK MATRIX (P1/P2)

### KROK 4.1: Jawne Oznakowanie Symulacji
* **Zadanie:** Przestrzeganie prawdy systemowej na UI.
* **Akcja:** W `LocalLLMRunner`, `SmartHomeManager` i `SandboxSimulationEnvironment` dodać wizualny overlay lub wyraźne badge (chip): **[SIMULATION MODE]**. 
* **Weryfikacja:** Kod nie ma prawa przedstawiać fałszywych logów jako dowodów skuteczności systemu ML, a logi "Evolution" oznaczane są jako "Generowane Losowo".

### KROK 4.2: Bezpieczny Gateway (P2)
* **Zadanie:** Zabezpieczenie lokalnego serwera Ktor.
* **Akcja:** Ustawienie bindowania w Ktorze wyłącznie do lokalnego interfejsu (localhost / 127.0.0.1) jeśli to możliwe, wdrożenie walidacji JSON i ograniczenia na rozmiar body żądania, obsługa konfliktu zajętości portu (BindException).

---

## 🚀 KROK 5: KRYTERIA WYDANIA (CLOSURE GATE)

Zgodnie ze standardem **EDDE+**, wersja produkcyjna zostanie zadeklarowana **TYLKO I WYŁĄCZNIE, GDY**:
1. Błąd Fake-Success w `ExecutionEngine` zostanie wykorzeniony.
2. Zbudowany APK będzie posiadał zminimalizowany kod (R8) i klucz Release.
3. Testy automatyczne przestaną maskować rzeczywistość (Test WorkManagera będzie faktycznie aspirował do WorkManagera, nie tylko modelu tekstowego).
4. Przejdzie pomyślnie pełen `lint` i `connectedAndroidTest`.
