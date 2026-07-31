Kotlin# PersonaMesh / Persona Colony — Full Review Cycle

**Data audytu:** 2026-07-26  
**Zakres:**
- `persona-colony-v4.zip` — gotowy APK
- `persona-android-system (4).zip` — projekt Android/Kotlin

## Werdykt

**Stan:** `BLOCKED — funkcjonalny prototyp, nie production-ready`

Projekt ma rozbudowany i w dużej części realny interfejs Compose, trwałą bazę Room, rzeczywiste wywołania Gemini/OpenRouter, WorkManager, SpeechRecognizer, ML Kit i lokalny serwer Ktor. Jednocześnie kluczowa warstwa wykonawcza zawiera **fake-success**, część funkcji jest jawnie symulowana, ustawienia autonomii i bezpieczeństwa nie są egzekwowane, dostarczony Gradle wrapper jest uszkodzony, a APK jest podpisany certyfikatem Android Debug.

Nie należy przedstawiać tej wersji jako:
- gotowego wydania produkcyjnego,
- rzeczywistego autonomicznego systemu wieloagentowego,
- systemu z prawdziwym lokalnym modelem Gemma,
- systemu z wiarygodnym evidence-based execution,
- wersji zweryfikowanej pełnym buildem i testami z dostarczonego ZIP-a.

## Ocena syntetyczna

| Obszar | Ocena | Werdykt |
|---|---:|---|
| UI / zakres produktu | 7/10 | Rozbudowany, realny prototyp |
| Warstwa danych Room | 6/10 | Realna, ale ryzyko utraty danych |
| Integracje AI | 5/10 | Realne API, słabe przechowywanie kluczy |
| Integralność wykonania | 2/10 | Krytyczny fake-success |
| Multi-agentowość | 3/10 | Głównie persony i sekwencyjne prompty |
| Bezpieczeństwo i prywatność | 3/10 | Nadmiar uprawnień i dekoracyjne polityki |
| Testy i reprodukowalność | 2/10 | Wrapper uszkodzony, testy za słabe |
| Gotowość produkcyjna | 2/10 | Blokery P0 |

**Ocena całościowa:** **3,5/10 jako system produkcyjny**  
**Ocena jako demonstrator UI / proof of concept:** **6,5/10**

---

# 1. Inwentaryzacja artefaktów

## `persona-colony-v4.zip`

Zawartość:
- jeden plik `persona-colony-v4.apk`,
- rozmiar APK około 92,5 MB.

APK zawiera klasy i komunikaty zgodne z projektem źródłowym, między innymi:
- `ExecutionEngine`,
- `AILlmClient`,
- `LocalLLMRunner`,
- `ApiGateway`,
- `ColonyViewModel`,
- `SYNC_CALENDAR`,
- `WORKER_ENQUEUED`,
- `Simulated Task Response`,
- `Sandbox: Loading Gemma 2B weights (Simulated)...`.

To oznacza, że wykryte ścieżki symulowane i fake-success trafiły do dostarczonego APK; nie są wyłącznie martwymi pozostałościami w źródłach.

## `persona-android-system (4).zip`

Najważniejsze cechy:
- projekt Android/Kotlin + Jetpack Compose,
- około 139 plików,
- około 79 plików Kotlin w `app/src/main/java`,
- około 26,4 tys. linii Kotlin w warstwie głównej,
- 24 encje Room,
- baza w wersji 21,
- 10 plików testowych,
- compile/target SDK 36,
- AGP 9.1.1,
- Kotlin 2.2.10,
- deklarowany Gradle 9.3.1.

Największe pliki:
- `DashboardScreen.kt` — około 2762 linii,
- `ColonyViewModel.kt` — około 1960 linii,
- `SettingsScreen.kt` — około 1434 linii,
- `ActiveAgentsScreen.kt` — około 1160 linii,
- `PersonaColonyScreen.kt` — około 976 linii.

To wskazuje na rozrost klas typu **god object / god screen** i utrudnia testowanie oraz kontrolę odpowiedzialności.

---

# 2. Krytyczne blokery P0

## P0-1 — Fake-success w `ExecutionEngine`

**Plik:**  
`app/src/main/java/com/example/engine/ExecutionEngine.kt:51-84`

### `CALENDAR_SYNC`

Kod:
- tworzy obiekt `ExecutionEvidence`,
- generuje losowy `requestId`,
- wpisuje `effectId = "SYNC_CALENDAR"`,
- tworzy tekstowy hash `CAL_<timestamp>`,
- zwraca `ExecutionOutcome.Executed`,
- ustawia status `EXECUTED`.

Nie ma tam:
- odczytu `CalendarContract`,
- zapisu do CalendarProvider,
- porównania stanu przed/po,
- potwierdzenia liczby zsynchronizowanych rekordów,
- identyfikatora rzeczywistego efektu.

### `RULE_EVALUATION`

Kod:
- deklaruje `toolProvider = "WorkManager (RuleEvaluatorWorker)"`,
- deklaruje `effectId = "WORKER_ENQUEUED"`,
- zwraca `EXECUTED`.

Nie ma tam:
- utworzenia `WorkRequest`,
- wywołania `WorkManager.enqueue`,
- odczytu `WorkInfo`,
- potwierdzenia ukończenia workera.

### Wniosek

To jest dokładny przypadek **fake-success**: evidence jest generowane bez wykonania opisywanej operacji. Dodatkowo lokalne `AGENTS.md` zabrania takiego zachowania przez regułę „No Fake Execution”.

### Poprawka

- `CALENDAR_SYNC`: wykonać realny odczyt/zapis, zebrać URI/ID rekordów, liczbę zmian i wynik weryfikacji.
- `RULE_EVALUATION`: realnie enqueue’ować unikalny `WorkRequest`, zapisać `workRequest.id`, a `EXECUTED` nadać dopiero po sprawdzeniu `WorkInfo.State.SUCCEEDED`.
- przed zakończeniem: rozdzielić stany `ACCEPTED`, `ENQUEUED`, `RUNNING`, `EXECUTED`, `VERIFIED`, `FAILED`.

## P0-2 — Dostarczony projekt nie ma działającego Gradle wrappera

Próba uruchomienia:
```text
./gradlew
```

kończy się:
```text
Invalid or corrupt jarfile .../gradle/wrapper/gradle-wrapper.jar
```

Sam ZIP przechodzi kontrolę struktury, ale `gradle-wrapper.jar` zawiera uszkodzone bajty, w tym sekwencje UTF-8 replacement character `EF BF BD`. Plik został uszkodzony przed lub podczas wcześniejszego eksportu.

### Skutek

Nie można z dostarczonej paczki:
- wykonać reprodukowalnego buildu,
- uruchomić lint,
- uruchomić testów,
- potwierdzić raportowanego `Build succeeded`.

### Poprawka

Regeneracja wrappera z zaufanego Gradle:
```bash
gradle wrapper --gradle-version 9.3.1
./gradlew --version
./gradlew clean lint testDebugUnitTest assembleDebug
```

Do CI dodać kontrolę integralności wrappera i zakazać transformowania plików binarnych przez edytor tekstowy/AI canvas.

## P0-3 — APK jest podpisany kluczem debug

Certyfikat APK:
- subject/issuer: `C=US, O=Android, CN=Android Debug`,
- ważność od 2026-07-26,
- SHA-256 certyfikatu:  
  `e04a68b5ee8fe3a139bc0e4f9c04a141c9768a3c59c1d3aba5ab957949baa5c4`,
- SHA-256 APK:  
  `767f4f56a6c320d80f07555adc69a31db38a117f59c28b1af810ccb24ba353e5`.

**Plik:** `app/build.gradle.kts`

Konfiguracja pozwala podpisać wariant `release` kluczem debug, gdy brakuje release keystore.

### Skutek

- APK jest artefaktem testowym.
- Nie jest bezpiecznym wydaniem produkcyjnym.
- Pipeline może wygenerować „release” o mylącej nazwie i debugowym zaufaniu.

### Poprawka

- usunąć fallback do `signingConfigs.debug`,
- przerwać build release, gdy brakuje wymaganych sekretów,
- generować podpisany AAB,
- użyć oddzielnego upload key / Play App Signing,
- publikować fingerprint i provenance artefaktu.

## P0-4 — Klucze API przechowywane jako zwykłe wartości DataStore

**Plik:**  
`app/src/main/java/com/example/data/AgentPreferencesRepository.kt:42-45, 86-106`

Klucze:
- `openrouter_api_key`,
- `gemini_api_key`

są zapisywane jako zwykłe string preferences.

Projekt ma `LocalEncryptedVault`, korzystający z Android Keystore i AES-GCM, ale nie jest on używany do przechowywania tych kluczy.

Dodatkowo:
- `android:allowBackup="true"`,
- reguły backupu są praktycznie puste,
- nie wykluczają preferencji i bazy z kopii zapasowych.

### Poprawka

- przenieść sekrety do Keystore-backed encrypted storage,
- nie umieszczać klucza Gemini w `BuildConfig` wydania klienckiego,
- rozważyć backend proxy z krótkotrwałymi tokenami,
- jawnie wykluczyć sekrety i dane prywatne z cloud backup / device transfer albo zastosować odpowiednie szyfrowanie end-to-end.

## P0-5 — Polityki bezpieczeństwa są dekoracyjne

Ustawienia:
- `globalAutonomyThreshold`,
- `allowBackgroundExecution`,
- `allowDataAccess`,
- `maxActiveTasksPerPersona`,
- `strictManualOverride`

są używane wyłącznie w:
- repozytorium preferencji,
- ekranie ustawień.

Nie znaleziono ich egzekwowania w:
- `ExecutionEngine`,
- WorkManager,
- `AILlmClient`,
- dostępie do kalendarza/kontaktów/call log,
- schedulerze,
- gatewayu.

### Skutek

Użytkownik może wyłączyć dostęp lub wymusić tryb manualny w UI, lecz logika wykonawcza nie traktuje tego jako wiążącej polityki.

### Poprawka

Wprowadzić centralny `PolicyEnforcementPoint`, który jest obowiązkowy dla każdej operacji:
```text
Request -> Policy Check -> Permission Check -> Approval -> Execute -> Verify -> Persist Evidence
```

## P0-6 — „Panic/lockdown” nie zatrzymuje systemu wykonawczego

**Pliki:**
- `PanicTileService.kt:16-37`,
- `ColonyRepository.kt:36-38`,
- `ColonyDao.kt:86-100`,
- `ColonyViewModel.kt:832-837`.

Funkcja:
- zmienia statusy agentów, misji i subtasków w bazie na `Halted`,
- dodaje komunikat.

Nie:
- anuluje WorkManagera,
- zatrzymuje `AgentRestSchedulerService`,
- przerywa Retrofit/OkHttp,
- zatrzymuje Ktor gateway,
- unieważnia aktywnych coroutine jobs,
- blokuje kolejne wykonania.

### Wniosek

To jest **database status switch**, nie emergency kill switch.

---

# 3. Macierz realności

| Funkcja | Stan | Ocena |
|---|---|---|
| Compose UI i nawigacja | REAL | Rozbudowane i podłączone |
| Room / DAO / trwały zapis | REAL | Działa architektonicznie |
| Gemini API | REAL | Bezpośrednie wywołanie klienta |
| OpenRouter API | REAL | Bezpośrednie wywołanie klienta |
| PII sanitization | PARTIAL | Regexy pomagają, ale nie gwarantują pełnej anonimizacji |
| SpeechRecognizer | REAL | Systemowe API Androida |
| ML Kit Translator | REAL + FALLBACK | Integracja realna, fallback słownikowy |
| RuleEvaluatorWorker | REAL | Worker istnieje i ma logikę |
| ExecutionEngine: worker enqueue | FAKE-SUCCESS | Nie enqueue’uje workera |
| Calendar sync | FAKE-SUCCESS | Brak rzeczywistej synchronizacji |
| `getRealCalendarEvents()` | STUB | Zwraca pustą listę |
| Lokalny Gemma 2B | SIMULATED | Delay + stały tekst |
| Smart Home | MOCK | Stałe urządzenia, zawsze sukces |
| Webhook API | PARTIAL/MISLEADING | Serwer realny, webhook ignoruje body |
| Sandbox | SIMULATION | Brak izolacji procesu/VM |
| Council chat | PERSONA SIMULATION | Jeden model symuluje wielu agentów |
| Agent discussion | PARTIAL | Sekwencyjne wywołania LLM, wspólny klient |
| Agent negotiation | SIMULATION | Losowe wybory/statusy |
| Mesh telemetry | SIMULATION | Losowy latency/CPU/RAM |
| Knowledge graph synthesis | SIMULATION | Losowe połączenia |
| Evolution heuristics | SIMULATION | Losowa confidence/success count |
| Panic mode | PARTIAL | Statusy DB, brak zatrzymania runtime |
| Autonomy/security settings | UI-ONLY | Brak centralnego enforcementu |

---

# 4. Multi-agentowość — uczciwa klasyfikacja

Projekt nie jest jeszcze pełnym autonomicznym multi-agent systemem.

## Co jest

- wiele rekordów/person agentów,
- osobne role i prompty,
- delegowanie opisów subtasków,
- sekwencyjne wywołania modelu,
- wspólna historia dyskusji,
- baza misji, decyzji, pamięci i komunikatów.

## Czego brakuje

- niezależnych instancji wykonawczych,
- osobnych kolejek i lifecycle agentów,
- oddzielnych kontekstów pamięci i narzędzi,
- rzeczywistego protokołu komunikacji agent-agent,
- arbitra/managera opartego na dowodach,
- wzajemnej walidacji rezultatów,
- izolacji błędów i budżetów,
- trwałego stanu wykonania i resume,
- realnego capability-based tool routing,
- obserwowalnego execution trace.

Najuczciwsza obecna nazwa:
**„Persona Council / Multi-Persona AI Prototype”**

Nazwa **„autonomous multi-agent system”** będzie obroniona dopiero po dodaniu niezależnych runtime’ów i rzeczywistego wykonywania narzędzi.

---

# 5. Testy i jakość dowodów

## Test `ExecutionEngineTest`

**Plik:**  
`app/src/test/java/com/example/engine/ExecutionEngineTest.kt:47-69`

Nazwa testu mówi:
`Rule Evaluation Worker Enqueues WorkManager`

Test nie sprawdza WorkManagera. Sprawdza wyłącznie:
- status `EXECUTED`,
- klasę `ExecutionOutcome.Executed`,
- tekstowe pola evidence.

W praktyce test utrwala fake-success zamiast go wykrywać.

Test LLM:
```kotlin
assertTrue(result.status == "BLOCKED" ||
           result.status == "SIMULATED" ||
           result.status == "FAILED")
```
akceptuje trzy bardzo różne rezultaty i nie weryfikuje konkretnego kontraktu.

## Test migracji

Test korzysta głównie z bieżącej bazy in-memory; nie odtwarza kompletnej starej bazy i nie przeprowadza pełnego ciągu migracji produkcyjnych.

Eksportowane schematy istnieją tylko dla:
- 10,
- 13,
- 14,
- 15,
- 17,
- 18.

Brakuje:
- 11,
- 12,
- 16,
- 19,
- 20,
- 21.

## Screenshot test

Test renderuje uproszczony tekst zamiast rzeczywistych krytycznych ekranów aplikacji. Nie daje wiarygodnej regresji UI.

## Raport wewnętrzny

`app/AUDIT_MOCK_REALITY_REPORT.md:77` twierdzi:
- zależności są poprawnie rozstrzygane,
- build kończy się sukcesem.

Dostarczona paczka temu przeczy, bo wrapper jest uszkodzony. Raportu Markdown nie można traktować jako aktualnego źródła prawdy.

---

# 6. Dane i migracje

## Dobre elementy

- realny Room,
- wiele encji i DAO,
- jawne migracje 9→21,
- część schematów eksportowana,
- dane są faktycznie utrwalane.

## Ryzyka

**Plik:**  
`app/src/main/java/com/example/data/AppDatabase.kt:251-274`

Włączono:
```kotlin
fallbackToDestructiveMigration(dropAllTables = true)
fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
```

Jeżeli brakuje ścieżki migracji lub wystąpi downgrade, aplikacja może skasować wszystkie tabele.

### Poprawka

- usunąć destructive fallback z produkcji,
- zachować go wyłącznie w osobnym build type/dev flavor,
- wygenerować schematy każdej wersji,
- wykonać testy migracji każdej wspieranej wersji do 21,
- dodać test zachowania danych użytkownika.

---

# 7. Usługi działające w tle

`AgentRestSchedulerService`:
- startuje przy każdym uruchomieniu `MainActivity`,
- działa jako foreground service typu `dataSync`,
- ma `START_STICKY`,
- odpytuje bazę co 15 sekund,
- nie sprawdza `allowBackgroundExecution`,
- nie implementuje `onTimeout`,
- nie zatrzymuje się po wykonaniu skończonej pracy.

Dla bieżących wersji Androida taki model jest niezgodny z przeznaczeniem długotrwałego `dataSync` foreground service i naraża aplikację na timeout/ANR.

### Poprawka

- okresowe zadania przenieść do WorkManagera,
- foreground service uruchamiać tylko dla widocznego, ograniczonego działania użytkownika,
- wprowadzić `stopSelf`,
- obsłużyć timeout,
- powiązać uruchamianie z rzeczywistą preferencją użytkownika.

---

# 8. Uprawnienia i prywatność

Manifest deklaruje m.in.:
- `READ_CALENDAR`,
- `READ_CONTACTS`,
- `READ_CALL_LOG`,
- `RECORD_AUDIO`,
- `QUERY_ALL_PACKAGES`,
- `PACKAGE_USAGE_STATS`,
- `FOREGROUND_SERVICE_DATA_SYNC`.

Ryzyka:
- `QUERY_ALL_PACKAGES` jest bardzo szerokim uprawnieniem i powinno być używane tylko w rzadkich, dobrze uzasadnionych przypadkach.
- `READ_CALL_LOG` oraz usage stats wymagają bardzo konkretnej wartości produktu i transparentnego UX.
- w `MainActivity` tworzona jest lista runtime permissions, lecz kod ich tam nie żąda;
- lista zawiera `ACTIVITY_RECOGNITION`, którego manifest nie deklaruje;
- dostęp do danych powinien być kontekstowy, minimalny i egzekwowany przez politykę.

### Poprawka

- usunąć każde uprawnienie bez potwierdzonej ścieżki produktu,
- zastąpić `QUERY_ALL_PACKAGES` precyzyjnym `<queries>`,
- przed żądaniem wyświetlać uzasadnienie,
- testować odmowę, częściową zgodę i cofnięcie zgody,
- nie generować sztucznych danych kontaktów, gdy uprawnienia są odrzucone.

---

# 9. Gateway i sieć

Lokalny Ktor/Netty jest realny, ale:
- `/webhook` nie przetwarza rzeczywistego body,
- zapisuje stały tekst `Webhook received`,
- brak uwierzytelnienia,
- brak rate limit,
- CORS dopuszcza dowolny host,
- brak limitu payloadu i jasnej polityki bind address.

### Poprawka

- domyślnie bind do localhost,
- losowy token sesyjny lub mTLS dla dostępu z LAN,
- walidacja JSON schema,
- limit rozmiaru i timeout,
- allowlist origins,
- audit log z request ID,
- prawdziwy status `RECEIVED`, a nie `EXECUTED`.

---

# 10. Rozmiar APK

APK zawiera:
- około 86,1 MB nieskompresowanych DEX,
- około 60,7 MB bibliotek natywnych,
- biblioteki dla `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`,
- duże biblioteki ML Kit translation.

### Poprawka

- publikować Android App Bundle,
- użyć ABI splits,
- włączyć R8/minify dla release,
- sprawdzić nieużywane moduły ML Kit i zależności,
- oddzielić funkcje eksperymentalne do dynamic feature modules.

---

# 11. Plan napraw P0–P3

## P0 — przed jakimkolwiek release

1. Usunąć fake-success z `ExecutionEngine`.
2. Naprawić i zweryfikować Gradle wrapper.
3. Zablokować release podpisywany debug key.
4. Przenieść klucze API do bezpiecznej architektury.
5. Wprowadzić centralny policy enforcement.
6. Zbudować rzeczywisty panic/kill switch.
7. Usunąć destructive migration fallback z produkcji.
8. Zastąpić nieskończony dataSync service właściwym WorkManagerem.

## P1 — integralność systemu

1. Dodać precyzyjny state machine wykonania.
2. Dodać weryfikowalne evidence z realnych adapterów.
3. Oznaczać każde dane jako `REAL`, `SIMULATED`, `USER_ENTERED` lub `DERIVED`.
4. Usunąć losowe „telemetry/evolution” albo oznaczyć jako demo.
5. Zabezpieczyć gateway.
6. Zredukować uprawnienia.
7. Napisać pełne testy migracji.
8. Rozbić `ColonyViewModel` i największe ekrany.

## P2 — prawdziwa multi-agentowość

1. Agent runtime z osobnym ID, kolejką, stanem i budżetem.
2. Niezależne pamięci i tool scopes.
3. Orchestrator/manager z jawnie zapisanym routingiem.
4. Worker, critic i verifier jako oddzielne wykonania.
5. Trwały event log i resume.
6. Izolacja błędów i retry policy.
7. Approval gates.
8. Realne testy end-to-end.

## P3 — produkt i optymalizacja

1. AAB + ABI splits + R8.
2. Pełna obserwowalność.
3. Dostępność UI i testy screenshot rzeczywistych ekranów.
4. Usunięcie martwych/demo modułów.
5. Dokumentacja modelu zagrożeń i Data Safety.
6. Performance profiling i battery benchmarks.

---

# 12. Minimalna bramka zamknięcia

Projekt można oznaczyć jako release candidate dopiero, gdy:

- `./gradlew clean lint testDebugUnitTest connectedDebugAndroidTest bundleRelease` przechodzi z czystego checkoutu;
- wrapper ma zweryfikowaną sumę;
- release nie używa debug certificate;
- każdy `EXECUTED` ma dowód rzeczywistego efektu;
- test WorkManager sprawdza rzeczywisty `WorkInfo`;
- calendar sync wykonuje i weryfikuje realną operację;
- polityki z UI są egzekwowane w runtime;
- panic anuluje pracę i blokuje kolejne wykonania;
- klucze nie są przechowywane jako plaintext preferences;
- backup rules chronią dane wrażliwe;
- migracje zachowują dane;
- symulacje są jawnie oznaczone;
- uprawnienia są zminimalizowane;
- foreground work jest zgodny z aktualnym modelem Androida.

## Ostateczna klasyfikacja

**To nie jest pusty mockup.**  
Jest tam dużo prawdziwego kodu, realny Android UI, baza i sieć.

**To nie jest też jeszcze uczciwie domknięty autonomiczny system agentowy.**  
Warstwa prezentacji jest znacznie dojrzalsza niż warstwa wykonawcza, a część „dowodów wykonania”, telemetryki, ewolucji i integracji to symulacje lub losowe dane.

**Werdykt końcowy:**  
`BLOCKED — zachować jako prototyp rozwojowy; nie publikować jako produkcyjny release przed wykonaniem P0.`

---

# 13. Provenance artefaktów

- APK SHA-256: `767f4f56a6c320d80f07555adc69a31db38a117f59c28b1af810ccb24ba353e5`
- Certyfikat APK SHA-256: `e04a68b5ee8fe3a139bc0e4f9c04a141c9768a3c59c1d3aba5ab957949baa5c4`
- Certyfikat: `Android Debug`
- Dostarczony wrapper: uszkodzony
- Testy/build z dostarczonego źródła: **nieuruchamialne**
- Zgodność źródło↔APK: **silny fingerprint match na podstawie klas i unikalnych stringów**
