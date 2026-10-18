# StreetSet

A workout app built with Kotlin Multiplatform and Compose Multiplatform, running on Android and iOS from a single shared codebase.

## What it does

Plan and track strength training. A **Program** is split into **Weeks**, each week holds **Sessions**, and every session is a list of **Blocks** — each block references an **Exercise**, a protocol, and a set of **Planned Sets** (reps, load, rest, RPE, tempo). Completed sessions are recorded as **Session Logs**, which feed the Stats screen.

## Why this project exists

This is a personal learning project. I built it to get deeper into Kotlin Multiplatform and to see how far one shared codebase can go before platform-specific code becomes unavoidable. Everything is my own work for my own use — it isn't meant as a production app or a public framework.

## Stack

- Kotlin Multiplatform + Compose Multiplatform
- Room (KMP) for local persistence
- Koin for dependency injection
- kotlinx-datetime for dates

## Project layout

```
shared/src/commonMain   shared UI and logic (Compose, screens, ViewModels)
shared/src/androidMain  Android-specific code (Room builder, Koin context)
shared/src/iosMain      iOS-specific code (Room database path)
androidApp              Android entry point
iosApp                  iOS entry point (Xcode project)
```

---

**Author:** Diogo Santos  
**Last updated:** October 19, 2026