# PersonaMesh - 25-Step Next Patch Plan

## Wprowadzenie
Plan wdrażania 25 MUST-HAVE funkcji dla systemu PersonaMesh, z podziałem na 5 faz priorytetowych. Plan jest ustrukturyzowany z myślą o architekturze Android (Kotlin, Jetpack Compose, Room, Coroutines).

---

## 🔴 FAZA 1: CRITICAL (Fundament Techniczny)

### 1. 🔒 Local-First Encrypted Vault
*   **Cel:** Bezpieczny, szyfrowany lokalnie magazyn na wrażliwe dane.
*   **Implementacja (Android):** Użycie `EncryptedSharedPreferences` dla kluczy/tokenów oraz SQLCipher dla bazy Room (`AppDatabase`).
*   **Kroki:** Dodanie zależności do `androidx.security:security-crypto` i `net.zetetic:android-database-sqlcipher`. Migracja istniejącej bazy do wersji szyfrowanej.

### 2. 📡 Real-Time Permission Scanner
*   **Cel:** Skanowanie uprawnień aplikacji na urządzeniu.
*   **Implementacja (Android):** Użycie `PackageManager` do pobrania listy zainstalowanych aplikacji i ich uprawnień (`GET_PERMISSIONS`).
*   **Kroki:** Stworzenie `PermissionScannerAgent`. UI: Ekran z listą aplikacji posortowaną według "Risk Score" (np. dostęp do mikrofonu, kamery, kontaktów).

### 3. 🌐 Browser Bridge Extension
*   **Cel:** Integracja z przeglądarką / z zewnątrz.
*   **Implementacja (Android):** Implementacja `Intent.ACTION_SEND` (Share Target) w `AndroidManifest.xml` dla `MainActivity`.
*   **Kroki:** Odbieranie udostępnionych tekstów i linków z innych aplikacji (np. Chrome) i przekazywanie ich bezpośrednio do silnika EDDE do analizy.

### 4. ⚡ One-Tap Panic Lockdown
*   **Cel:** Natychmiastowe blokowanie systemu.
*   **Implementacja (Android):** Globalny `Quick Settings Tile` (Kafel w szybkich ustawieniach) lub stałe powiadomienie (Foreground Service).
*   **Kroki:** Utworzenie `PanicTileService`. Akcja: natychmiastowe czyszczenie cache, zamknięcie aktywnych agentów (anulowanie Coroutines), wylogowanie z UI i zapis logu.

### 5. 📋 EDDE Engine CLI + GUI
*   **Cel:** Widoczny cykl EDDE z możliwością eksportu.
*   **Implementacja (Android):** Nowy ekran `EddeConsoleScreen` w Compose, symulujący terminal.
*   **Kroki:** Utworzenie generatora PDF (np. za pomocą `PdfDocument` z Android Canvas) do eksportu logów sesji EDDE dla egzekutora.

---0

## 🟠 FAZA 2: HIGH (Główna Wartość Dodana)

### 6. 💰 Smart Finance Agent
*   **Implementacja:** Moduł czytający pliki CSV (np. z użyciem biblioteki `kotlin-csv`). Analiza wydatków lokalnie.
*   **UI:** Wykresy (np. Vico/Compose-Charts) wydatków, budżety.

### 7. 📅 Calendar Intelligence Engine
*   **Implementacja:** Integracja z `CalendarProvider` w Androidzie (`READ_CALENDAR`).
*   **Kroki:** Agent analizujący wydarzenia, szukający konfliktów i wolnych bloków na "deep work".

### 8. 📝 Auto-Report Generator
*   **Implementacja:** Rozbudowa modułu z kroku 5. Szablony dla Word (Apache POI) lub Markdown. Zapisywanie raportów w `MediaStore.Downloads`.

### 9. 🔍 Web Content Analyzer
*   **Implementacja:** Pobieranie treści HTML z URL (np. via `Jsoup`), ekstrakcja tekstu i przepuszczenie przez `ColonyViewModel` / LLM do analizy i streszczenia.

### 10. 🧠 Personal Knowledge Graph
*   **Implementacja:** Baza grafowa lub relacyjna (Room) mapująca powiązania między notatkami i wnioskami. UI: Wizualizacja tagów i połączeń (Canvas w Compose).

---

## 🟡 FAZA 3: MEDIUM (Inteligentne Rozszerzenia)

### 11. 🤖 Autonomous Agent Builder
*   **Implementacja:** Ekran `AgentBuilderScreen`, gdzie użytkownik definiuje parametry (Prompt, Temperatura, Dostęp do narzędzi). Zapis definicji w bazie Room.

### 12. 📊 Cross-Agent Dashboard
*   **Implementacja:** Główny widok `DashboardScreen` zbierający metryki od wszystkich agentów (Health, Finance, Security). Kafelkowy interfejs (Material 3 Cards).

### 13. 🛡️ Subscription Audit & Kill Switch
*   **Implementacja:** Moduł do Finance Agenta. Regexowe wykrywanie powtarzających się płatności z historii CSV.

### 14. 😴 Sleep & Recovery Optimizer
*   **Implementacja:** Odczyt danych z Google Fit/Health Connect API. Analiza i wyświetlanie rekomendacji w UI.

### 15. 📚 Study Spaced Repetition Engine
*   **Implementacja:** Algorytm SuperMemo-2 (SM-2) zaimplementowany w Kotlinie. Tabela w Room dla "Flashcards" z polami: `interval`, `repetition`, `easinessFactor`.

---

## 🟢 FAZA 4: NICE-TO-HAVE (Efekt WOW)

### 16. 🎨 Adaptive Theme Engine
*   **Implementacja:** Dynamiczna zmiana `MaterialTheme` (kolory, typografia) bazująca na `AgentMood` lub porze dnia w `AppTheme` kompozycie.

### 17. 🎙️ Voice Command Surface
*   **Implementacja:** Użycie `SpeechRecognizer` API w Androidzie do nasłuchiwania komend głosowych po wciśnięciu przycisku.

### 18. 🌍 Local-first Translation Layer
*   **Implementacja:** Użycie ML Kit (On-Device Translation API) od Google. Działa 100% offline.

### 19. 🛁 Focus Mode Deep Work Suite
*   **Implementacja:** Integracja z `NotificationManager` (tryb DND - Do Not Disturb). Zmiana stanu interfejsu na minimalizm.

### 20. 👥 Relationship Nudge Engine
*   **Implementacja:** Analiza metadanych z `CallLog` i `ContactsContract` (wymaga zgody użytkownika). Przypomnienia generowane na podstawie częstotliwości kontaktów.

---

## 🔵 FAZA 5: EVOLUTION (Wizja v2.0)

### 21. 🚀 On-Device LLM Runner
*   **Implementacja:** Integracja z MediaPipe LLM Inference API lub llama.cpp dla Androida (JNI/NDK) do uruchamiania np. modelu Gemma 2B lokalnie.

### 22. 🔗 API Gateway & Webhook System
*   **Implementacja:** Lokalny serwer HTTP w Androidzie (np. Ktor Server) przyjmujący żądania od innych aplikacji w tle.

### 23. 🧬 Auto-Evolution Engine (v2)
*   **Implementacja:** Rozbudowa systemu refleksji EDDE. Automatyczne generowanie i zapisywanie nowych heurystyk w Room na podstawie logów systemowych (zaakceptowanych przez użytkownika).

### 24. 🏠 Smart Home Bridge Agent
*   **Implementacja:** Moduł Matter / Thread do komunikacji z IoT (Google Home SDK dla Androida).

### 25. 🧪 Sandbox Simulation Environment
*   **Implementacja:** Wirtualna maszyna stanu (State Machine) w Kotlinie. System "kopii zapasowej" stanu przed symulacją, wykonanie logiki w środowisku testowym i przewidzenie wyników w UI bez mutowania prawdziwej bazy danych.

---
**Status planu:** ZATWIERDZONY
**Autor:** EDDE System / AI Studio
