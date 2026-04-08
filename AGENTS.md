# AGENTS.md

This document provides technical instructions, architectural guidelines, and lessons learned for AI agents working on the Solinda codebase.

## Coding Conventions

This project follows the official Kotlin and Android coding conventions. Please adhere to these style guides when writing and modifying code:

*   **Kotlin Style Guide:** [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
*   **Android Kotlin Style Guide:** [https://developer.android.com/kotlin/style-guide](https://developer.android.com/kotlin/style-guide)

## Testing

When adding new methods, especially to ViewModel or game logic classes, please add corresponding unit tests to ensure they can be invoked and behave as expected. This helps prevent regressions and build failures.

## Architectural Guidelines

### State Management & Persistence
*   **MVVM:** The application follows a strict MVVM architecture. UI logic resides in ViewModels, which interact with a Repository for data persistence.
*   **Reactive UI:** Use Compose observable state (`mutableStateOf`, `mutableStateListOf`, `mutableIntStateOf`) to ensure the UI reacts to model changes.
*   **Serialization:** Game state is persisted using **Gson**. Ensure all new data models are serializable and properly integrated into the top-level `GameState`.
*   **Separation of Concerns:** State models are organized into separate files (e.g., `SolitaireData.kt`, `JewelindaData.kt`, `CalculatorData.kt`, `CommonSettings.kt`, `PileState.kt`, `CardState.kt`) to avoid large, monolithic files and improve maintainability.

### Centralized Constants
*   Core game values such as dimensions, delays, thresholds, margins, and reveal factors are centralized in `Constants.kt`.
*   Standard card animation duration is set to `ANIMATION_DURATION_MS = 150ms` for a smooth, responsive feel.

### UI & Layout
*   **Jetpack Compose:** The entire application is built using Jetpack Compose.
*   **Edge-to-Edge:** Use `enableEdgeToEdge()` in `MainActivity` and apply `Modifier.safeDrawingPadding()` to handle system insets correctly.
*   **Adaptive Layouts:** Orientation detection is handled using `LocalConfiguration.current.orientation`. Always ensure screens function correctly in both Portrait and Landscape modes.
    *   **Solitaire:** Card heights vary by orientation (1.2x width in landscape, 1.8x in portrait).
    *   **Calculator:** Landscape mode uses a two-column layout with specific button weights and order.
    *   **Compass:** Buttons are positioned at the top-right with a 10% vertical offset for consistency.
*   **Precise Alignment:** In `SolitaireScreen.kt`, use `Box` with explicit `Modifier.offset` for top-area elements (Stock, Waste, FreeCells, Foundations) to ensure alignment with tableau columns.

### Haptic Feedback
*   Haptic feedback is controlled by `viewModel.isHapticsEnabled`.
*   **Jewelinda:** `VIRTUAL_KEY` for swaps/matches, `LONG_PRESS` for explosions.
*   **Solitaire/FreeCell:** `VIRTUAL_KEY` for drops, `CLOCK_TICK` for hover.
*   **Calculator:** `KEYBOARD_TAP` on all button clicks.
*   **Compass:** `KEYBOARD_TAP` when azimuth hits 0° (constrained by a 1-second cooldown).

## Game-Specific Logic & Lessons Learned

### Solitaire (Klondike & FreeCell)
*   **Animations:** Use `Animatable`. To prevent flickering, defer model updates until the animation completes or use a `skipModelUpdate` flag in the ViewModel.
*   **Foundation Alignment:** Klondike foundations align with columns 4-7, and FreeCell foundations align with columns 5-8.
*   **Auto-Complete:** Handled recursively in the UI layer (`SolitaireScreen.kt`) via `handleAutoComplete` to manage chain reaction animation timing.
*   **Scrolling:** Tableau uses `Modifier.verticalScroll`. Hit detection (`getPileRect`) must account for the current `scrollState.value`.
*   **Dimming:** Tableau cards are visually dimmed using a light gray background and a gray tint (`BlendMode.Multiply`) if they are immovable. This only applies to face-up cards.

### Jewelinda
*   **Gem Animations:** Uses nested `graphicsLayer` modifiers. The outer layer applies squash-and-stretch (bottom-center anchor: `0.5f, 1f`), and the inner layer handles rotation/pulse (true center: `0.5f, 0.5f`).
*   **Screen Shake:** Capped at 600ms. Individual explosions trigger a 300ms shake at 6dp intensity.
*   **Hypergems:** Clear all gems of a target color directly. This is a non-explosive activation to ensure clarity and performance.
*   **Particle Engine:** Uses a fixed pool of 75 particles for performance optimization. Bursts are capped at 6 particles per event.

### Calculator
*   **Input Limits:** Strictly enforces a 10-character limit for both input and results.
*   **Sequential Logic:** Resets `storedValue` if a number is input when `pendingOperator` is null and `isNewInput` is true.
*   **Visual Highlights:** Active operators are highlighted with a pink background (`0xFFFFC0CB`).

### Compass
*   **Needle Animation:** Uses `Animatable` with manual delta calculation to ensure the needle takes the shortest path across the 0°/360° point.

## Safety Constraints
*   Always enter a "deep planning mode" before modifying code: interact via `request_user_input` to fully understand goals.
*   Never perform a find-and-replace across the entire project without careful manual review.
*   Before submitting changes, always run all relevant tests and verify that the application builds and runs as expected.
