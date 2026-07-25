# PersonaMesh — Full Review Cycle

**Data audytu:** 25 lipca 2026  
**Źródło:** `untitled (9).zip`  
**Zakres:** struktura repozytorium, architektura, runtime truth, mocki i fake-success, bezpieczeństwo i prywatność, dane, Android lifecycle, testy, CI, build/reproducibility, dokumentacja  
**Werdykt końcowy:** `FAILED`

> `FAILED` oznacza tutaj dokładnie: wymagany build został uruchomiony i zakończył się błędem, a implementacja przeczy deklaracji „real execution pipelines (not mockups)”.

---

## 1. Executive verdict

PersonaMesh jest **rozbudowanym prototypem Androida z realnymi elementami infrastruktury**, ale nie jest jeszcze wiarygodnym, produkcyjnym systemem agentowym.

Realne są między innymi:

- lokalna baza Room i przepływy `Flow`;
- rzeczywiste wywołania Gemini REST;
- odczyt Calendar Provider;
- częściowy odczyt listy pakietów Androida;
- foreground service schedulera;
- lokalne powiadomienia;
- Glance widget;
- podstawowe CI i kilka testów Room.

Problem nie polega na tym, że aplikacja jest całkowitą atrapą. Problem jest poważniejszy:

1. **realne mechanizmy są zmieszane z symulowanymi rezultatami;**
2. **symulacja bywa zapisywana jako faktycznie ukończone zadanie;**
3. **UI i dokumentacja nie rozróżniają wiarygodnie wykonania od narracji modelu;**
4. **projekt dostarczono z uszkodzonym wrapperem Gradle i 11 uszkodzonymi obrazami;**
5. **warstwa prywatności sama składa nieprawdziwą deklarację o lokalnym audycie, po czym wysyła dane o aplikacjach do Gemini.**

### Ocena dojrzałości

| Obszar | Ocena | Stan |
|---|---:|---|
| Build i odtwarzalność | 0/5 | krytycznie zablokowane |
| Runtime truth | 1/5 | fake-success i martwe ścieżki |
| Bezpieczeństwo klucza/API | 1/5 | klucz klienta w APK |
| Prywatność | 0.5/5 | mylący disclosure i wysyłka danych |
| Architektura | 2/5 | działający prototyp, silny monolit |
| Testy | 1/5 | głównie szablony i DAO |
| UX prawdy systemowej | 1/5 | demo i realny stan są przemieszane |
| **Łącznie** | **~1.1/5** | **UI-rich prototype, nie production agent system** |

---

## 2. Co sprawdzono

### Inspekcje i polecenia

```bash
unzip -t "/mnt/data/untitled (9).zip"
find . -type f
find app/src -name '*.kt'
rg -n -i "mock|simulate|demo|fake|placeholder|stub|fallback|TODO" .
rg -n "RuleEvaluatorWorker|WorkManager|enqueue|PeriodicWorkRequestBuilder" app/src/main/java
rg -n "actionType\s*=|CALENDAR_SYNC|RULE_EVALUATION|LLM_PROMPT" app/src/main/java
rg -n "_isOffline|isOffline" app/src/main/java
rg -n "Firebase|AppCheck|FirebaseAI|GenerativeBackend" app/src/main/java
jar tf gradle/wrapper/gradle-wrapper.jar
identify <obrazy repozytorium>
sha256sum gradle/wrapper/gradle-wrapper.jar
./gradlew --no-daemon clean testDebugUnitTest lintDebug assembleDebug --stacktrace
```

### Wyniki terminalne

- Zewnętrzne archiwum ZIP: **CRC OK**.
- Plików: **88**.
- Plików Kotlin: **43**.
- Kotlin LOC: **12 294**.
- Build: **FAILED przed konfiguracją projektu**.
- Powód: `Invalid or corrupt jarfile gradle/wrapper/gradle-wrapper.jar`.
- Testy, lint i APK: **NOT RUN**, ponieważ wrapper nie uruchamia Gradle.
- Źródła nie zostały naprawiane podczas audytu.
- Sprawdzano kod dostarczony w archiwum, nie deklaracje README jako źródło prawdy.

---

## 3. Macierz weryfikacji

| Kryterium | Status | Dowód |
|---|---|---|
| Archiwum można rozpakować | PASS | `unzip -t` bez błędów |
| Gradle wrapper jest poprawny | FAIL | JAR nie ma poprawnego katalogu ZIP |
| Oficjalny checksum wrappera | FAIL | repo: `a5e751...`; oficjalny Gradle 9.3.1: `b3a875...` |
| Zasoby obrazów są poprawne | FAIL | 10 launcher WEBP + 1 screenshot PNG uszkodzone |
| `lintDebug` | NOT RUN | wrapper blokuje |
| `testDebugUnitTest` | NOT RUN | wrapper blokuje |
| `assembleDebug` | NOT RUN | wrapper blokuje |
| Realne wykonanie tasków | FAIL | symulowany tekst zapisany jako `Completed` |
| Rule engine uruchamiany w runtime | FAIL | worker istnieje, ale nie jest enqueue’owany |
| Granica mock/real | FAIL | statusy i UI mieszają oba rodzaje wyniku |
| Migracje danych | FAIL | destructive migration, brak schematów |
| Klucz Gemini chroniony | FAIL | bezpośredni klucz przez `BuildConfig` i query param |
| App Check aktywny | FAIL | zależność istnieje, brak użycia w kodzie |
| Audyt prywatności jest zgodny z opisem | FAIL | UI mówi „locally”, kod wysyła listę aplikacji do Gemini |
| Testy krytycznych ścieżek | FAIL | brak testów wykonania, workerów, permissions i migracji |
| Model `gemini-3.5-flash` aktualny | PASS | sam identyfikator modelu nie jest blockerem |
| Dokumentacja zgodna z implementacją | FAIL | deklaruje real execution, implementuje simulated execution |

---

# 4. Findings — P0

## P0-01 — Repozytorium nie jest buildowalne z dostarczonych plików

**Dowód**

- `gradle/wrapper/gradle-wrapper.jar` jest uszkodzony.
- `jar tf` zwraca `zip END header not found`.
- `./gradlew ...` zwraca `Invalid or corrupt jarfile`.
- SHA-256 pliku nie odpowiada oficjalnemu wrapperowi Gradle 9.3.1.
- Archiwum zewnętrzne ma poprawne CRC, więc uszkodzony JAR został zapakowany już w tej postaci.

**Wpływ**

- CI z `.github/workflows/android.yml` polegnie na każdym kroku `./gradlew`.
- Nie można potwierdzić kompilacji, lint, testów ani APK.
- Projekt nie spełnia minimalnego kryterium odtwarzalności.

**Naprawa**

1. Wygenerować wrapper ponownie z zaufanej dystrybucji Gradle 9.3.1.
2. Zweryfikować checksum wrappera.
3. Commitować binarny JAR bez przechodzenia przez tekstowy pipeline.
4. Dodać CI sprawdzające checksum wrappera lub przynajmniej `jar tf`.

---

## P0-02 — Uszkodzone zasoby binarne

**Dowód**

Uszkodzone:

- `app/src/test/screenshots/greeting.png`;
- wszystkie 10 plików:
  - `mipmap-mdpi/ic_launcher.webp`;
  - `mipmap-xhdpi/ic_launcher.webp`;
  - `mipmap-hdpi/ic_launcher.webp`;
  - `mipmap-xxhdpi/ic_launcher.webp`;
  - `mipmap-xxxhdpi/ic_launcher.webp`;
  - analogiczne `ic_launcher_round.webp`.

Poprawne są dwa JPG:

- `drawable/img_app_icon.jpg`;
- `drawable/img_mesh_hero.jpg`.

**Wpływ**

- Po naprawie wrappera AAPT/resource merge prawdopodobnie nadal polegnie.
- Screenshot regression nie ma wiarygodnej referencji.
- Ikona aplikacji wskazana w manifeście odwołuje się do uszkodzonych plików.

**Status dowodu**

- Uszkodzenie obrazów: potwierdzone.
- Konkretny błąd AAPT: nieuruchomiony z powodu wcześniejszego blockera wrappera.

---

## P0-03 — Fake-success: symulacja jest oznaczana jako wykonane zadanie

**Dowód**

`app/src/main/java/com/example/viewmodel/ColonyViewModel.kt:429-478`

- funkcja nazywa się `executeSubTaskReal`;
- prompt w `:462` mówi modelowi: `perform a simulated execution`;
- wynik jest zapisany jako `Executed Task`;
- każde powodzenie odpowiedzi LLM kończy się statusem `Completed` w `:477-478`.

**Sprzeczność**

`Agents.md:16` deklaruje:

> `real execution pipelines (not mockups)`

Ten sam dokument zabrania simulated success i fake outputs.

**Wpływ**

System nie potrafi odpowiedzieć na fundamentalne pytanie:

> Czy działanie zostało wykonane w świecie/systemie, czy tylko opisane przez model?

To dyskwalifikuje obecną implementację jako wiarygodny runtime agentowy.

**Naprawa**

Wprowadzić rozłączne typy wyniku:

```text
PLANNED
SIMULATED
EXECUTED
VERIFIED
FAILED
BLOCKED
NEEDS_APPROVAL
```

`Completed` może powstać wyłącznie z potwierdzonego efektu narzędzia/API i artefaktu dowodowego.

---

## P0-04 — Rule engine ogłasza działania, których nie uruchamia

**Dowód**

`ColonyViewModel.kt:453-455` zapisuje:

> `Evaluated rules and triggered automated background tasks.`

Nie ma tam wywołania workera, WorkManagera ani żadnego działania.

`RuleEvaluatorWorker` istnieje, lecz wyszukiwanie repo pokazuje tylko:

- deklarację klasy;
- trzy martwe importy w `MainActivity.kt`.

Brak:

- `WorkManager.enqueue(...)`;
- `PeriodicWorkRequestBuilder<RuleEvaluatorWorker>()`;
- `OneTimeWorkRequestBuilder<RuleEvaluatorWorker>()`.

Dodatkowo `SubTask.actionType` domyślnie ma `LLM_PROMPT`, a repo nie przypisuje nigdzie `RULE_EVALUATION` ani `CALENDAR_SYNC`.

**Wpływ**

- Branch rule engine jest praktycznie nieosiągalny z normalnie tworzonych tasków.
- Gdyby został osiągnięty, nadal tylko zapisze komunikat o sukcesie.
- Worker jest dead code z perspektywy runtime.

**Naprawa**

- Albo realnie podłączyć worker i przechowywać `WorkRequest.id`, status i output.
- Albo usunąć deklaracje automatyzacji.
- Nigdy nie logować „triggered” przed potwierdzeniem enqueue i rezultatu.

---

## P0-05 — Klucz Gemini trafia do aplikacji klienckiej

**Dowód**

- `README.md:19` instruuje zapisanie `GEMINI_API_KEY` w `.env`.
- Secrets Gradle Plugin generuje wartość `BuildConfig.GEMINI_API_KEY`.
- Kod odczytuje ją wielokrotnie.
- Retrofit wysyła ją jako `@Query("key")`:
  `network/RetrofitClient.kt:17-30`.
- Aplikacja komunikuje się bezpośrednio z `generativelanguage.googleapis.com`.
- Zależności Firebase AI/App Check są dodane, ale brak jakiegokolwiek użycia Firebase/App Check w źródłach.

**Wpływ**

- Klucz jest możliwy do odzyskania z APK.
- Brak rzeczywistego App Check na ścieżce Retrofit.
- Ryzyko nadużycia quota i kosztów.
- Sama obecność dependency nie daje ochrony.

**Naprawa**

Preferowana ścieżka:

1. Firebase AI Logic SDK;
2. klucz Gemini po stronie proxy Firebase;
3. App Check;
4. brak `BuildConfig.GEMINI_API_KEY`;
5. rotacja dotychczas używanego klucza;
6. rate limit, quota i telemetry.

---

## P0-06 — Ekran prywatności przekazuje użytkownikowi fałszywy opis działania

**Dowód UI**

`PrivacyDiagnosticScreen.kt:78`:

> `Audits ... locally. This does not audit external Android OS system permissions.`

**Dowód implementacji**

`ColonyViewModel.kt:585-638`:

- używa Android `PackageManager`;
- pobiera zainstalowane pakiety i requested permissions;
- buduje listę nazw pakietów i uprawnień;
- wysyła ją do Gemini;
- zapisuje odpowiedź modelu jako wynik „Privacy Sentinel”.

To dokładne przeciwieństwo opisu „locally” i „does not audit external Android OS system permissions”.

**Dodatkowe problemy**

- brak disclosure przed wysyłką danych do zewnętrznego modelu;
- pobierane jest tylko `.take(10)` niesortowanych aplikacji;
- na Androidzie 11+ lista pakietów jest filtrowana bez odpowiedniej widoczności manifestu;
- wynik modelu jest klasyfikowany przez wyszukiwanie słów:
  `risk`, `suspicious`, `violation`, `anomaly`;
- zdanie „no risk detected” zostanie uznane za naruszenie;
- błąd „Audit failed” może zostać zapisany jako brak naruszenia.

**Wpływ**

To blocker prywatności i prawdy UI.

**Naprawa**

- Prawdziwy ekran zgody z dokładną listą przesyłanych danych.
- Lokalny pre-audyt bez LLM.
- Jawne rozdzielenie `LOCAL_SCAN` i `CLOUD_ASSESSMENT`.
- Strukturalny JSON/schema, nie keyword matching.
- `INCONCLUSIVE/FAILED` jako osobny status, nigdy „clean”.
- Usuń deklarację „locally”, dopóki to nie jest prawda.

---

## P0-07 — Destrukcyjna migracja może skasować cały lokalny stan

**Dowód**

`AppDatabase.kt:8-24`

- DB version `9`;
- `exportSchema = false`;
- `.fallbackToDestructiveMigration(dropAllTables = true)`.

Baza zawiera między innymi:

- rozmowy;
- memories;
- decyzje;
- system prompts agentów;
- taski i misje;
- privacy audit logs;
- reguły;
- kalendarz;
- milestone i uprawnienia.

**Wpływ**

Przy braku migracji aktualizacja schematu może po cichu usunąć wszystkie tabele.

**Naprawa**

- `exportSchema = true`;
- jawne migracje;
- migration tests dla każdej wersji;
- backup/restore policy;
- destructive migration tylko w buildzie developerskim po wyraźnym opt-in.

---

## P0-08 — Uprawnienia agentów są tekstem dekoracyjnym, a nie capability enforcement

**Dowód**

`Entities.kt:8-20`:

```text
permissions: String
autonomyLevel: String
```

`checkAndEnforceAgentPermissions()` rozpoznaje tylko substring:

- `contact`;
- `calendar`.

Nie egzekwuje między innymi:

- `Full System Access`;
- `Usage Stats`;
- `Do Not Disturb`;
- `Files`;
- `Banking`;
- `Notifications`;
- `Sensors`;
- `Network`;
- `Storage`.

`ColonyViewModel.kt:1056-1068` automatycznie dodaje milestone agentowi:

```text
permissions = "Full System Access"
autonomyLevel = "Semi-Autonomous"
```

bez decyzji użytkownika.

Preferencje:

- `allowBackgroundExecution`;
- `allowDataAccess`;
- `strictManualOverride`;
- `maxActiveTasksPerPersona`;

są prezentowane w UI, ale nie są sprawdzane w krytycznych ścieżkach wykonania tasków, audytu ani Gemini.

**Wpływ**

Warstwa governance jest opisowa, nie wykonawcza.

**Naprawa**

- Enum/typed capabilities.
- Centralny `PolicyEngine.authorize(actor, capability, resource, action)`.
- Każde narzędzie musi wymagać capability token.
- Audyt allow/deny z reason code.
- Milestone nie może przyznawać uprawnień.
- User approval dla zwiększenia autonomii.

---

# 5. Findings — P1

## P1-01 — `isOffline` jest na stałe ustawione na `true`

`ColonyViewModel.kt:99-100`.

Brak jakiegokolwiek zapisu do `_isOffline`.

Skutek: dashboard stale pokazuje stan offline niezależnie od rzeczywistej sieci i API.

---

## P1-02 — Wiadomość użytkownika jest wysyłana do Gemini dwukrotnie

`sendMessage()`:

1. zapisuje wiadomość do Room w `:659`;
2. pobiera historię zawierającą już tę wiadomość w `:681-688`;
3. ponownie dodaje `userText` w `:690-697`.

Skutek:

- zduplikowany input;
- wyższy koszt/token usage;
- możliwość nadmiernego ważenia ostatniego polecenia;
- nieczytelna historia modelu.

---

## P1-03 — Wszystkie tryby używają tego samego modelu, a Google Search jest zawsze włączony

`ColonyViewModel.kt:699-727`

- Fast → `gemini-3.5-flash`;
- Deep Think → `gemini-3.5-flash`;
- Search → `gemini-3.5-flash`;
- image → `gemini-3.5-flash`.

Różnica Deep Think to tylko `thinkingLevel = HIGH`.

Google Search tool jest dodawany dla każdego requestu, ponieważ warunek zależy od modelu, nie od trybu `Search`.

Skutek:

- tryb „Search” nie jest rzeczywiście odrębny;
- użytkownik nie ma jasnej kontroli nad groundingiem;
- requesty mogą używać zewnętrznego wyszukiwania także w trybie Fast/Deep Think.

---

## P1-04 — „Consensus confidence” nie mierzy rzeczywistego konsensusu agentów

System:

1. prosi jeden model, by zasymulował dyskusję agentów;
2. wysyła jego pojedynczą odpowiedź do tego samego modelu jako „Consensus Judge”;
3. zapisuje wygenerowany procent jako poziom zgodności.

Nie ma:

- niezależnych agent executions;
- oddzielnych modeli/stanów;
- głosów;
- score aggregation;
- evidence weighting.

To narracyjny confidence, nie pomiar konsensusu.

---

## P1-05 — Seedowane wiadomości przedstawiają fikcyjne zdarzenia jako fakty

`ColonyViewModel.kt:224-253` automatycznie zapisuje między innymi:

- „I've scheduled a 15-minute mental break at 14:00”;
- „Privacy audit complete: 0 unapproved data accesses detected”.

Nie ma dowodu, że przerwa została zaplanowana ani audyt wykonany.

Dane seed/demo powinny być oznaczone `DEMO`, widoczne tylko w demo mode albo usunięte z produkcji.

---

## P1-06 — Seedowane kalendarze mogą wyglądać jak prawdziwe wydarzenia

Przy pustej bazie kod dodaje wydarzenia względem bieżącej godziny:

- Project Deadline Review;
- Deep Sleep Buffer;
- Wellness Recovery Run.

Brak pola `source = DEMO/DEVICE/USER/AI`.

Skutek: UI i predykcje mogą traktować wygenerowane przykłady jako realny kalendarz.

---

## P1-07 — Diagnostyka pakietów jest niepełna na współczesnym Androidzie

Manifest nie zawiera:

- `<queries>`;
- `QUERY_ALL_PACKAGES`.

`PackageManager.getInstalledPackages()` nie daje pełnej listy aplikacji na Androidzie 11+ bez odpowiedniej widoczności.

Nawet po dodaniu widoczności broad package access wymaga uzasadnionego use case i minimalizacji.

---

## P1-08 — Uprawnienia są żądane przed onboardingiem i kontekstem

`MainActivity.kt:51-77` od razu:

- prosi o Calendar;
- Contacts;
- Notifications;
- Activity Recognition;
- otwiera systemowy ekran Usage Access.

Dopiero potem renderuje onboarding.

Skutek:

- słaby permission UX;
- brak purpose limitation;
- większa szansa odmowy;
- brak granularnego opt-in dla funkcji.

---

## P1-09 — Manifest deklaruje niewykorzystywane write permissions

Deklarowane:

- `WRITE_CALENDAR`;
- `WRITE_CONTACTS`;
- `ACTIVITY_RECOGNITION`.

W sprawdzonych źródłach brak realnego użycia write contacts/calendar i activity recognition.

Zasada: usuń permission, dopóki konkretna funkcja go nie potrzebuje.

---

## P1-10 — Foreground service jest uruchamiany bezwarunkowo przy każdym starcie aplikacji

`MainActivity.kt:80-90`.

Scheduler może być wyłączony w prefs, ale service i notification nadal są uruchamiane; dopiero pętla pomija pracę.

Skutek:

- stale działający proces/notification;
- koszt baterii;
- niezgodność z intencją switcha.

---

## P1-11 — Scheduler odpytuje bazę co 5 sekund i używa `dataSync` do lokalnego monitorowania

`AgentRestSchedulerService.kt:43-129`.

- nieskończona pętla;
- polling co 5 sekund;
- `START_STICKY`;
- typ foreground service: `dataSync`;
- rzeczywista praca: bateria + Room + lokalne statusy agentów.

To nie wygląda jak transfer/synchronizacja danych. Typ usługi jest co najmniej podejrzanie dobrany, a współczesny Android nakłada ograniczenia czasowe na `dataSync`.

Lepsza architektura:

- event-driven Room flows;
- WorkManager dla deferrable work;
- AlarmManager tylko dla dokładnych, uzasadnionych alarmów;
- brak permanentnego polling loop.

---

## P1-12 — Harmonogram odpoczynku działa tylko jednorazowo i nie wznawia agentów

`applyRestPeriods()`:

- jest wywołane przy init i po naciśnięciu Save;
- jeśli bieżący czas jest w oknie, zmienia `Active` → `Paused`;
- nie planuje kolejnego sprawdzenia;
- nie przywraca `Paused` → `Active` po końcu okna.

Foreground scheduler obsługuje inny mechanizm `Resting`, więc nie naprawia tego harmonogramu.

---

## P1-13 — Statusy są stringly typed i można ominąć blokady

Przykład:

`toggleAgentStatus()`:

```kotlin
if (agent.status == "Active") "Paused" else "Active"
```

Każdy status inny niż `Active`, także potencjalnie:

- `Blocked`;
- `Halted`;
- `Resting`;
- `Syncing`;

może zostać przełączony na `Active`.

Potrzebna jest jawna maszyna stanów i dozwolone transition rules.

---

## P1-14 — Możliwa pętla zapisu przy egzekwowaniu permissions

Init:

```kotlin
agents.collect {
    checkAndEnforceAgentPermissions()
}
```

Enforcement może wykonać `insertAgent()`, co ponownie emituje `agents`.

Kod ogranicza część zapisów porównaniem statusu, ale architektura nadal jest reaktywną pętlą side-effectów bez `distinctUntilChanged`, centralnej transakcji i ochrony przed konkurencją.

---

## P1-15 — Mission completion używa potencjalnie nieaktualnych snapshotów StateFlow

`updateSubTaskStatus()`:

1. aktualizuje DAO;
2. natychmiast szuka taska w `subTasks.value`;
3. sprawdza `getSubTasksForMission(...).first()`.

Snapshot `subTasks.value` może jeszcze nie zawierać nowego stanu. Completion powinno być transakcyjne i oparte na bezpośrednim zapytaniu DAO.

---

## P1-16 — Encje Room nie mają relacji, foreign keys ani indeksów

Przykłady:

- `SubTask.missionId` bez FK do Mission;
- `assignedAgent` jako nazwa, nie ID;
- RuleConnection bez FK do RuleNode;
- brak indeksów na status, missionId, timestamps.

Skutek:

- orphan records;
- duplikacja/niespójność nazw;
- gorsze query performance;
- trudne migracje.

---

## P1-17 — Room `REPLACE` jest używany jako uniwersalny upsert

DAO stosuje `OnConflictStrategy.REPLACE` prawie wszędzie.

W SQLite `REPLACE` semantycznie usuwa konfliktujący rekord i wstawia nowy, co może mieć skutki dla relacji i przyszłych FK.

Preferowane:

- `@Upsert`;
- celowane `@Update`;
- jawne transakcje.

---

## P1-18 — Backup i FileProvider są zbyt szerokie

Manifest:

```xml
android:allowBackup="true"
```

Backup rules są plikami sample/TODO bez wyłączeń.

`file_paths.xml` udostępnia przez FileProvider:

- cały cache;
- cały external path;
- cały internal files path.

Nawet przy `exported=false`, nadany URI grant może dotyczyć znacznie szerszego obszaru niż potrzebny eksport JSON.

Należy ograniczyć ścieżki do dedykowanego katalogu exportu i wyłączyć wrażliwe DB/preferences z backupu, jeśli produkt nie ma przemyślanej polityki szyfrowania/restore.

---

## P1-19 — Release config zawiera sprzeczną ścieżkę debug signing

Dla standardowego taska release brak keystore powoduje `GradleException`, co jest właściwym zachowaniem.

Jednocześnie `buildTypes.release` zawiera fallback:

```kotlin
signingConfig = signingConfigs.getByName("debug")
```

gdy release config nie istnieje.

W zwykłym `assembleRelease` ten fallback jest najpewniej poprzedzony wyjątkiem, ale pozostaje:

- sprzeczny;
- niepotrzebny;
- ryzykowny dla niestandardowych tasków/konfiguracji.

Usuń fallback całkowicie.

---

## P1-20 — Test instrumentacyjny ma błędne oczekiwanie package name

Test:

```kotlin
assertEquals("com.example", appContext.packageName)
```

Build config:

```kotlin
applicationId = "com.aistudio.personamesh.jshkpq"
```

Na urządzeniu `targetContext.packageName` powinien odpowiadać application ID, więc test jest niespójny z konfiguracją.

---

## P1-21 — Krytyczne ścieżki nie są testowane

Brak testów dla:

- `executeSubTaskReal`;
- fake-success prevention;
- failure/blocked transitions;
- Gemini parsing;
- duplicate user input;
- rule evaluation;
- WorkManager;
- foreground service lifecycle;
- privacy diagnostic;
- permission engine;
- Room migrations;
- backup rules;
- scheduled rest/resume;
- panic/halt;
- milestone permission escalation;
- package visibility;
- release config.

---

## P1-22 — God ViewModel i monolityczne UI

Metryki:

- `DashboardScreen.kt`: ~92 KB / 1755 linii;
- `ColonyViewModel.kt`: ~58 KB / 1183 linii;
- `ActiveAgentsScreen.kt`: ~48 KB;
- `PersonaColonyScreen.kt`: ~43 KB;
- `SettingsScreen.kt`: ~32 KB.

`ColonyViewModel` odpowiada równocześnie za:

- seeding;
- Room;
- Gemini;
- OS package audit;
- calendar;
- tasks/missions;
- policy;
- badge/milestones;
- inter-agent simulation;
- rest scheduler;
- UI state.

Skutek: wysoka podatność na regresje i brak testowalności.

---

# 6. Findings — P2

## P2-01 — Pull-to-refresh jest animacją bez odświeżenia

`DashboardScreen.kt:346-354` wykonuje tylko `delay(1000)`.

UI sugeruje realny refresh, ale nie uruchamia żadnej operacji.

---

## P2-02 — README jest generyczne i częściowo nieaktualne

- opis „everything you need” jest nieprawdziwy przy uszkodzonych binariach;
- instrukcja usunięcia linii `debugConfig` nie odpowiada obecnemu kodowi;
- brak sekcji o Firebase/App Check;
- brak permission rationale;
- brak known limitations i demo mode;
- brak migracji i danych użytkownika.

---

## P2-03 — Zależności Firebase są deklarowane, ale martwe

`firebase-ai` i App Check są w Gradle, lecz kod nadal używa ręcznego Retrofit.

To daje fałszywe poczucie, że ochrona Firebase została wdrożona.

---

## P2-04 — Brak structured output w krytycznych decyzjach

Kod prosi o „raw JSON”, potem wycina substring między `{` i `}` i parsuje `JSONObject`.

Potrzebne:

- schema/response MIME;
- walidacja;
- retry tylko dla parser failure;
- status `INCONCLUSIVE`;
- brak cichego catch.

---

## P2-05 — Brak limitów historii, kosztu i retencji

Cała historia Council Chat jest wysyłana ponownie przy każdym requestcie.

Brak:

- max messages/token budget;
- summary compaction;
- user-controlled delete/export;
- per-request cost estimate;
- rate limiting;
- cancellation;
- retry/backoff policy.

---

## P2-06 — Błędy wewnętrzne trafiają do UI

`e.localizedMessage` jest zapisywane jako wiadomość modelu.

Może to ujawniać wewnętrzne szczegóły requestów i utrudnia odróżnienie błędu systemu od odpowiedzi agenta.

---

## P2-07 — Lokalizacja jest niespójna

UI i kod mieszają:

- angielski;
- polskie słowa w parserach;
- zaszyte teksty zamiast resource strings.

---

## P2-08 — CI jest minimalne

Obecne CI:

- lint;
- unit test;
- assemble debug.

Brakuje:

- instrumentation/emulator tests;
- dependency/security scanning;
- secret scanning;
- wrapper validation;
- binary asset validation;
- migration tests;
- release build;
- baseline/profile;
- artifact upload;
- reproducibility checks.

---

# 7. Co jest faktycznie działającą implementacją

Poniższe elementy są kodem realnym, nie wyłącznie szkicem:

1. **Room persistence** — encje, DAO, repository i Flow.
2. **Gemini REST** — Retrofit naprawdę wysyła requesty.
3. **Calendar Provider read** — przy nadanym permission.
4. **PackageManager read** — realny, ale filtrowany i niepełny.
5. **Foreground rest service** — realny, lecz źle zaprojektowany.
6. **Lokalne notifications** — używane przez service.
7. **Glance widget** — komponent istnieje.
8. **Import/export agentów** — obecne ścieżki UI i FileProvider.
9. **Rule graph persistence** — node i connection zapisują się do Room.
10. **Basic CI definition** — workflow istnieje, choć dziś nie przejdzie.

To dobry fundament prototypu. Nie jest to jednak dowód na realną autonomię agentową.

---

# 8. Co jest symulacją, atrapą albo mylącym sygnałem

| Funkcja | Rzeczywistość |
|---|---|
| `executeSubTaskReal()` | LLM opisuje symulację, potem task = Completed |
| `RULE_EVALUATION` | loguje sukces bez uruchomienia workera |
| Multi-agent council | jeden model symuluje role |
| Consensus percentage | drugi request ocenia tekst pierwszego requestu |
| Privacy audit | częściowa lista pakietów + ocena LLM |
| „local privacy audit” | w rzeczywistości dane idą do chmury Gemini |
| Seeded audit | fikcyjny „0 unapproved accesses” |
| Seeded calendar | demo events bez etykiety source |
| Pull-to-refresh | 1 sekunda delay |
| Offline state | stałe `true` |
| Agent permissions | głównie wolny tekst i prezentacja UI |
| Rule automation | worker niepodłączony do WorkManager |

---

# 9. Ranked patch plan

## Faza P0-A — Przywrócić repo do stanu buildowalnego

1. Wymienić uszkodzony `gradle-wrapper.jar`.
2. Wymienić 10 launcher WEBP i screenshot PNG.
3. Dodać `verifyBinaryAssets` do CI.
4. Uruchomić:
   ```bash
   ./gradlew clean lintDebug testDebugUnitTest assembleDebug
   ```
5. Następnie:
   ```bash
   ./gradlew connectedDebugAndroidTest
   ```

**Gate:** zero corrupt binary, build i testy uruchamialne z czystego checkoutu.

---

## Faza P0-B — Truthful execution kernel

Wprowadzić:

```kotlin
sealed interface ExecutionOutcome {
    data class Executed(val evidence: ExecutionEvidence) : ExecutionOutcome
    data class Simulated(val explanation: String) : ExecutionOutcome
    data class Blocked(val reason: String) : ExecutionOutcome
    data class Failed(val errorCode: String) : ExecutionOutcome
    data class NeedsApproval(val requestId: String) : ExecutionOutcome
}
```

Każdy executor zwraca:

- typ działania;
- tool/provider;
- start/end;
- request ID;
- effect ID;
- verifier;
- evidence hash;
- status.

**Gate:** LLM prose nigdy nie może samodzielnie utworzyć `Completed`.

---

## Faza P0-C — Podłączyć albo usunąć rule runtime

1. Typed rule AST zamiast tekstowego parsera.
2. WorkManager enqueue.
3. Unique work names.
4. Input/output data.
5. Retry/backoff.
6. Worker status zapisany w Room.
7. Powiadomienie jako prawdziwe Android notification.
8. Testy z `WorkManagerTestInitHelper`.

**Gate:** UI pokazuje queued/running/succeeded/failed na podstawie realnego work state.

---

## Faza P0-D — Bezpieczna ścieżka Gemini

1. Usunąć bezpośredni klucz z `BuildConfig`.
2. Przejść na Firebase AI Logic.
3. Włączyć App Check.
4. Zrotować klucz.
5. Ustawić quota/rate limits.
6. Dodać zgodę i data disclosure.
7. Wyłączyć Search tool domyślnie.
8. Dodać privacy-safe logging.

**Gate:** APK nie zawiera Gemini Developer API key.

---

## Faza P0-E — Naprawić Privacy Diagnostics

1. Zmienić copy UI na zgodne z rzeczywistością.
2. Dodać preview payloadu przed wysłaniem.
3. Lokalny scanner i opcjonalna cloud assessment.
4. Nie przesyłać package names, jeżeli wystarczy lokalna klasyfikacja permission sets.
5. Zwracać:
   - SAFE;
   - RISK;
   - INCONCLUSIVE;
   - FAILED.
6. Usunąć keyword detector.
7. Rozwiązać package visibility zgodnie z minimalnym use case.

**Gate:** UI, manifest i request payload opisują dokładnie tę samą operację.

---

## Faza P0-F — Dane i migracje

1. `exportSchema = true`.
2. Migracje 9→10 itd.
3. Migration tests.
4. Foreign keys i indeksy.
5. `@Upsert`.
6. Backup policy.
7. Ograniczony FileProvider.
8. Opcjonalne szyfrowanie szczególnie wrażliwych danych.

**Gate:** aktualizacja aplikacji nie może skasować stanu użytkownika.

---

## Faza P1 — Rozbić architekturę

Docelowe moduły/use cases:

```text
ui/
domain/
data/
ai/
policy/
execution/
rules/
calendar/
privacy/
scheduler/
notifications/
```

Kluczowe interfejsy:

```text
TaskExecutor
ExecutionVerifier
CapabilityAuthorizer
AiGateway
PrivacyScanner
RuleScheduler
CalendarGateway
AuditRepository
```

`ColonyViewModel` powinien zostać cienkim koordynatorem UI.

---

## Faza P1 — Minimalny zestaw testów przed kolejnym werdyktem

1. Build/lint/unit.
2. `executeSubTaskReal`:
   - simulated != completed;
   - API fail -> failed/pending;
   - no agent/key -> blocked.
3. Duplicate prompt regression.
4. Search tool only in Search mode.
5. Rule WorkManager enqueue + result.
6. Permission deny/allow matrix.
7. Privacy payload disclosure.
8. Audit `no risk` nie jest violation.
9. Audit failure = inconclusive.
10. Rest schedule pause i resume.
11. Service off = service stopped.
12. Database migration 9→10.
13. Instrumented package name.
14. Backup/FileProvider security tests.
15. Milestone cannot grant capabilities.

---

# 10. Proponowana architektura prawdy

```text
User Intent
    ↓
Planner
    ↓
Capability Check ── denied ──> Needs Approval / Blocked
    ↓ allowed
Typed Executor
    ↓
External Effect / Local Effect
    ↓
Evidence Collector
    ↓
Independent Verifier
    ↓
VERIFIED_EXECUTED
```

Symulacja powinna być osobnym torem:

```text
User Intent
    ↓
Simulator
    ↓
SIMULATED_RESULT
    ↓
UI badge: "Simulation — no action performed"
```

Tych torów nie wolno scalać wspólnym statusem `Completed`.

---

# 11. Minimum wymagane do zmiany werdyktu

## Z `FAILED` na `PARTIALLY_VERIFIED`

Minimum:

- poprawny wrapper i obrazy;
- build, lint i unit tests uruchomione;
- fake-success usunięty;
- rule branch przestaje kłamać;
- privacy screen przestaje deklarować local-only;
- klucz nie jest w kliencie albo funkcja AI jest jawnie dev-only;
- destructive migration usunięta;
- podstawowe testy nowych ścieżek przechodzą.

## Z `FAILED` na `VERIFIED`

Dodatkowo:

- device/emulator E2E;
- WorkManager runtime proof;
- App Check proof;
- migration proof;
- permission/capability enforcement;
- audit evidence;
- release build podpisany release key;
- test utraty sieci, odmowy permission, timeout, restart procesu i upgrade DB;
- demo/simulation wyraźnie oddzielone od real mode;
- żadnego nieweryfikowanego `Completed`.

---

# 12. Ostateczna ocena

## `FAILED`

Powody zamykające:

1. wymagany build został uruchomiony i nie wystartował z powodu uszkodzonego wrappera;
2. repo zawiera dodatkowo 11 uszkodzonych zasobów graficznych;
3. funkcja nazwana `executeSubTaskReal` wykonuje symulację i oznacza ją jako `Completed`;
4. rule engine deklaruje uruchomienie zadań, których nie enqueue’uje;
5. system prywatności mówi użytkownikowi „locally”, a przesyła dane pakietów do Gemini;
6. klucz Gemini jest projektowany jako sekret w aplikacji klienckiej;
7. migracje mogą destrukcyjnie usunąć bazę;
8. capability governance nie jest technicznie egzekwowana;
9. testy nie pokrywają żadnego z powyższych ryzyk.

### Najuczciwsza etykieta produktu

**PersonaMesh jest atrakcyjnym, szerokim prototypem osobistego panelu AI na Androidzie z kilkoma realnymi integracjami. Nie jest jeszcze wiarygodnym multi-agent execution systemem.**

Największa wartość repozytorium to UI, koncept i lokalny model danych. Największy dług to brak granicy między narracją AI a faktycznie wykonanym i zweryfikowanym działaniem.
