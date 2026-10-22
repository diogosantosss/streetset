# StreetSet

**A personal workout tracking app built with Kotlin Multiplatform.**

Plan your training, track your sessions, and monitor your progress — with a shared codebase for Android and iOS.

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin Multiplatform" />
  <img src="https://img.shields.io/badge/Compose-Multiplatform-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform" />
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-34A853?style=flat-square" alt="Android and iOS" />
</p>

---

## Overview

StreetSet is a workout planning and tracking app designed around structured strength training.

Training is organized into a simple hierarchy:

**Programs → Weeks → Sessions → Blocks → Exercises**

Each block defines an exercise, its training protocol, and planned sets, including reps, load, rest time, RPE, and tempo.

Completed sessions are stored as session logs and used to track training history and display statistics.

## Features

- **Training plans** — Organize programs into weeks and sessions.
- **Flexible workouts** — Structure sessions using exercise blocks and training protocols.
- **Set tracking** — Record reps, load, rest, RPE, and tempo.
- **Session history** — Save completed workouts for future reference.
- **Training statistics** — Use recorded sessions to monitor progress.
- **Local persistence** — Keep workout data stored locally.

## Tech Stack

| Technology            | Purpose                          |
|-----------------------|----------------------------------|
| Kotlin Multiplatform  | Shared application logic         |
| Compose Multiplatform | Shared UI across Android and iOS |
| Room                  | Local database and persistence   |
| Koin                  | Dependency injection             |
| kotlinx-datetime      | Date and time handling           |

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
