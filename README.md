# PersonaMesh — Personal Colony of Android Agents

PersonaMesh to zaawansowana, produkcyjna platforma wieloagentowa na system Android, zbudowana w języku **Kotlin** z interfejsem **Jetpack Compose** i architekturą **Material Design 3**. Platforma umożliwia lokalne tworzenie, orkiestrację i audyt zautomatyzowanej "kolonii agentów" działających wspólnie w celu zarzadzania zasobami cyfrowymi użytkownika.

---

## 🌟 Główne Funkcjonalności & Architektura

1. **14-Fazowy Pętlowy System EDDE+ (Cognitive-Operational Loop)**
   - Pełna implementacja faz cyklu: `Perceive` -> `Extract Essence` -> `Map & Challenge` -> `Select Direction` -> `Synthesize Model` -> `Forecast` -> `Generate Options` -> `Decide` -> `Plan & Execute` -> `Observe` -> `Evaluate & Verify` -> `Reflect` -> `Persist` -> `Evolve`.
   - **Verification Before Closure (📊 Evaluate & Verify)**: System rygorystycznie wymusza prawdziwy dowód wykonania (brak udawanych/mockowanych sukcesów) i weryfikuje stany przed domknięciem zadania.

2. **Policy Enforcement & Capability Guard**
   - **PolicyEnforcementPoint**: Centralny punkt sprawdzania progu autonomii (`Manual`, `Semi-Autonomous`, `High`). W trybie `Semi-Autonomous` operacje o randze `High` oraz `Critical` bezwzględnie wymagają zgody użytkownika.
   - **AgentCapabilityGuard**: Dynamiczna kontrola uprawnień per agent (np. READ_CALENDAR, SEND_NOTIFICATIONS, CAMERA, NETWORK) z automatycznym audytem w `PrivacyAuditor`.

3. **Lokalna Baza Danych Room (Migracja Schema v24)**
   - W pełni wygenerowana i przetestowana baza danych `AppDatabase` wspierająca relacje, logi audytowe, historię zadań, wskaźniki ewolucyjne oraz stan agentów.

4. **Multi-Agent MultiStep Dialog & Adaptive UI**
   - Rygorystycznie uporządkowane ekrany bez duplikatów komponentów (usunięto przestarzałe oraz martwe definicje `CreateAgentDialog` i nieużywane komponenty).
   - Wsparcie dla widoków tabletowych / ChromeOS / foldable oraz adaptacyjnych ulepszeń układu.

5. **Bezpieczeństwo & Wyłącznik Awaryjny (Kill-Switch)**
   - Atomowy wyłącznik awaryjny zatrzymujący wszystkie aktywne zadania, zadania WorkManager i połączenia sieciowe w trybie natychmiastowym.

---

## 🛠️ Warianty Budowania (Product Flavors)

Aplikacja wspiera dwa warianty kompilacyjne (Flavor Dimension `mode`):
- **`demo`**: Wariant demonstracyjny ze wstępnie skonfigurowaną reprezentacją agentów i symulacyjnymi scenariuszami testowymi (`com.aistudio.personamesh.jshkpq.demo`).
- **`production`**: Wariant produkcyjny przeznaczony do rzeczywistego użycia na urządzeniach z pełną integracją API i lokalną bazą Room.

---

## 🚀 Uruchomienie & Kompilacja

### Wymagania:
- **Android Studio** (Ladybug lub nowszy)
- **JDK 17** lub **JDK 21**
- **Android SDK 36**

### Polecenia Gradle:
```bash
# Budowanie wariantu produkcyjnego
./gradlew assembleProductionDebug

# Uruchomienie testów jednostkowych i integracyjnych
./gradlew testDebugUnitTest

# Przeprowadzenie pełnego testu kompilacyjnego i weryfikacyjnego
./gradlew testDebugUnitTest assembleDebug
```

---

## 📄 Weryfikacja i Jakość Kodu

Wszystkie moduły i klasy zostały zweryfikowane pod kątem braku martwego kodu, braku brakujących klas oraz braku zastępczych deklaracji TODO/mockupów. Kod źródłowy w całości realizuje docelowe funkcje i pomyślnie przechodzi automatyczną kompilację oraz testy.
