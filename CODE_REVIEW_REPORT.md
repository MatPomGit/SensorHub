# Code Review Report (rzetelny przegląd statyczny)

## Zakres
Przegląd statyczny kluczowych modułów odpowiedzialnych za odczyt sensorów i pracę w tle:
- `app/src/main/java/com/kia/sensorhub/sensors/*`
- `app/src/main/java/com/kia/sensorhub/workers/SensorWorkers.kt`

Dodatkowo wykonano próbę uruchomienia testów jednostkowych.

## Najważniejsze ustalenia

### 1) `callbackFlow` nie kończy się, gdy sensor jest niedostępny (High)
**Wpływ:** Kolektor może czekać bez końca na dane, co blokuje scenariusze biznesowe oczekujące na pierwsze próbki (np. worker zbierający N próbek). To może prowadzić do „wiszących” jobów i nieprzewidywalnego UX.

**Szczegóły:**
- W `AccelerometerManager`, `GyroscopeManager` i `MagnetometerManager` listener jest rejestrowany przez `sensor?.let { ... }`, ale gdy sensor jest `null`, strumień nie jest zamykany błędem ani completion.
- `awaitClose` pozostaje aktywne, więc kolektor nie otrzymuje żadnego sygnału zakończenia.

**Lokalizacje:**
- `app/src/main/java/com/kia/sensorhub/sensors/AccelerometerManager.kt`
- `app/src/main/java/com/kia/sensorhub/sensors/GyroscopeManager.kt`
- `app/src/main/java/com/kia/sensorhub/sensors/MagnetometerManager.kt`

**Rekomendacja:**
- Na początku `callbackFlow` dodawać jawny guard:
  - jeśli sensor niedostępny -> `close(IllegalStateException("..."))` i `return@callbackFlow`.

---

### 2) Ryzyko nieskończonego wykonania `SensorMonitoringWorker` (High)
**Wpływ:** `doWork()` może nie zakończyć się w rozsądnym czasie (lub wcale), gdy nie napływają eventy sensora (brak sensora, brak eventów w tle, ograniczenia producenta urządzenia). To zwiększa zużycie baterii i ryzyko ubicia workera przez system.

**Szczegóły:**
- Worker pobiera dane przez `flow.take(sampleCount).toList()` bez timeoutu.
- Jeśli strumień nie emituje elementów (lub emituje zbyt wolno), `toList()` blokuje zakończenie pracy.

**Lokalizacja:**
- `app/src/main/java/com/kia/sensorhub/workers/SensorWorkers.kt`

**Rekomendacja:**
- Dodać `withTimeout(...)` / `withTimeoutOrNull(...)` podczas zbierania próbek.
- Przy timeoutach dla błędów trwałych używać `Result.failure()` zamiast bezwarunkowego `retry`.

---

### 3) Współdzielony stan pomiarów w `MagnetometerManager` (Medium)
**Wpływ:** Potencjalne mieszanie danych między wieloma kolektorami (`gravity`/`geomagnetic` jako pola klasy), co może dawać niepoprawny azymut i trudne do odtworzenia błędy.

**Szczegóły:**
- `gravity` i `geomagnetic` są polami obiektu managera (`@Singleton` pośrednio przez DI w repozytorium).
- W przypadku wielu jednoczesnych subskrypcji wartości z jednego kolektora mogą wpływać na inny.

**Lokalizacja:**
- `app/src/main/java/com/kia/sensorhub/sensors/MagnetometerManager.kt`

**Rekomendacja:**
- Przenieść `gravity`/`geomagnetic` do lokalnego scope `callbackFlow` (stan per-subskrypcja), ewentualnie zabezpieczyć synchronizacją i świadomie zarządzać wieloma kolektorami.

---

## Dodatkowe obserwacje
- Obsługa wyjątków w workerach (`catch (e: Exception) { Result.retry() }`) jest bardzo ogólna; część błędów jest trwała i nie powinna powodować nieskończonych retry.

## Walidacja
- Próba uruchomienia testów: `./gradlew testDebugUnitTest --no-daemon`
- Wynik: niepowodzenie z powodu braku Android SDK w środowisku (`sdk.dir` wskazuje nieistniejący katalog), więc review oparto o analizę statyczną.
