# StreetSet

StreetSet is a personal workout tracking app built with Kotlin Multiplatform. Plan sessions on a calendar, follow guided training blocks, and review your history — Android and iOS share the same code.

## Features

- Calendar with multiple sessions per day and done, missed or planned states
- Blocks with normal, warm-up, top set, back-off, EMOM, hold, metronome, circuit and superset protocols
- Guided training mode with rest, EMOM and hold timers, plus round-by-round circuits and supersets
- Set planning for reps, load, rest, RPE, rounds, BPM and hold time
- Spreadsheet paste import and session logs with duration and notes
- Stats with a 12-month heatmap, weekly streak, sessions per week, training mix and most trained exercises
- Local persistence and an ongoing Android workout notification

## Stack

- Kotlin Multiplatform and Compose Multiplatform
- Voyager for tab and screen navigation
- Room 3 with bundled SQLite for local storage
- Koin for dependency injection
- kotlinx-datetime for dates and times
- Lucide icons and Haze for blurred surfaces
- Napier for logging

## Project structure

```text
shared/     shared UI, logic, ViewModels and database
androidApp/ Android entry point
iosApp/     iOS app and Xcode project
```

## Demo

Coming soon.

---

Built by Diogo Santos · November 2026
