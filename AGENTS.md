# AGENTS.md

This document provides instructions for AI agents working on the Solinda codebase.

## Coding Conventions

This project follows the official Kotlin and Android coding conventions. Please adhere to these style guides when writing and modifying code:

*   **Kotlin Style Guide:** [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
*   **Android Kotlin Style Guide:** [https://developer.android.com/kotlin/style-guide](https://developer.android.com/kotlin/style-guide)

## Testing

When adding new methods, especially to ViewModel or game logic classes, please add corresponding unit tests to ensure they can be invoked and behave as expected. This helps prevent regressions and build failures.


# Agent Instructions: Project "Gemini Jewel" Mode

## Context & Scope
I am adding a "Match-3" (Bejeweled clone) game mode to an existing Android application. 
- **Core Principle:** DO NOT modify or delete existing application code unless explicitly requested for integration (e.g., adding a button to the main menu).
- **Namespace:** All new code must reside in the package `com.example.solinda.jewelinda`.
- **UI Framework:** Jetpack Compose.

## Architectural Guidelines
1. **Modularity:** Keep Game Logic (Engine) strictly separate from the UI (Compose).
2. **State Management:** Use a `ViewModel` with `StateFlow` to represent the 8x8 grid.
3. **Performance:** Use `Modifier.offset` and `animateIntOffsetAsState` for gem movement to ensure 60fps animations.
4. **Resources:** Use placeholders or Material Design Icons for gems initially; keep assets organized in `res/drawable/game`.

## Phase 1: Infrastructure & Data Model
- Create `Gem.kt` data class: `id` (UUID), `type` (Enum), `posX` (Int), `posY` (Int).
- Create `GameBoard.kt`: Logic for an 8x8 array.
- Implement `initBoard()`: Fill the board with random gems ensuring no 3-in-a-row matches exist on startup.

## Phase 2: UI Components
- **GemComponent.kt:** A composable that renders a single gem based on its `type`. Must support absolute positioning via `Modifier.offset`.
- **GameGrid.kt:** A `BoxWithConstraints` container that iterates through the board and renders `GemComponent`s.
- **Input Handling:** Implement a swipe gesture detector (detecting North, South, East, West) to trigger gem swap attempts.

## Phase 3: Game Engine Logic
- **Swap Logic:** `swapGems(posA, posB)` with rollback if no match is formed.
- **Match Detection:** A function to scan the grid for horizontal and vertical lines of 3+.
- **Gravity & Refill:** Logic to shift gems down to fill null spaces and spawn new gems at $y < 0$.

## Safety Constraints
- Never perform a `find-and-replace` across the entire project.
- Always check `MainActivity` navigation before adding new Intent filters.

