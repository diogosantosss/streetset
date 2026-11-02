# StreetSet

**A personal workout tracking app built with Kotlin Multiplatform.**

Plan your training on a calendar, follow your sessions step by step, and monitor your progress — with a shared codebase for Android and iOS.

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin Multiplatform" />
  <img src="https://img.shields.io/badge/Compose-Multiplatform-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform" />
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-34A853?style=flat-square" alt="Android and iOS" />
</p>

---

## Overview

StreetSet is a workout planning and tracking app designed around structured strength training.

Training lives on a calendar, with as many sessions per day as you want:

**Calendar → Sessions → Blocks → Exercises**

Each block defines an exercise (or a group of exercises), its training protocol, and its planned sets, including reps, load, rest time, RPE, and tempo or hold duration.

Completed sessions are stored as session logs and used to track training history and display statistics.

## Features

- **Calendar** — Browse months, see which days have sessions (done, missed or planned), and create several sessions per day.
- **Flexible workouts** — Build sessions from blocks using different training protocols: normal, warm-up, top set, back-off, EMOM, hold, metronome, circuit and superset.
- **Guided training mode** — Follow a session set by set, with rest timers, EMOM and hold timers, and the full list of what comes next. Circuits and supersets are done round by round.
- **Set planning** — Plan reps, load, rest, RPE, rounds, BPM and hold time for each block.
- **Spreadsheet import** — Paste rows copied from a spreadsheet to create sessions in bulk. A dash splits the sets of a block (`5-3-1` reps with `BW-10-15` load), and a session with the same name on the same day is replaced.
- **Session history** — Save completed workouts, with duration and notes.
- **Training statistics** — A 12-month activity heatmap, weekly streak, sessions per week, training mix and most trained exercises.
- **Workout in the background** — The running workout shows as an ongoing notification on Android. iOS Live Activity and Dynamic Island support is in progress.
- **Local persistence** — Keep workout data stored locally.

## Tech Stack

| Technology            | Purpose                                  |
|-----------------------|------------------------------------------|
| Kotlin Multiplatform  | Shared application logic                 |
| Compose Multiplatform | Shared UI across Android and iOS         |
| Room                  | Local database and persistence           |
| Koin                  | Dependency injection                     |
| kotlinx-datetime      | Date and time handling                   |
| Haze                  | Blur effects                             |
| Napier                | Logging                                  |

## Project Structure

```text
StreetSet/
├── shared/
│   └── src/
│       ├── commonMain/   # Shared UI, logic and ViewModels
│       ├── androidMain/  # Android-specific implementations
│       └── iosMain/      # iOS-specific implementations
├── androidApp/           # Android application entry point
└── iosApp/               # iOS application and Xcode project
```

## Motivation

StreetSet is a personal learning project focused on exploring Kotlin Multiplatform and Compose Multiplatform.

The goal is to understand how much application logic and UI can be shared between Android and iOS, while learning how to structure a maintainable application with local persistence and dependency injection.

This project is built for personal use and educational purposes. It is not intended to be a production-ready application or a public framework.

---

<p align="center">
  Built by <strong>Diogo Santos</strong>
</p>

<p align="center">
  <sub>Personal project · October 2026</sub>
</p>
