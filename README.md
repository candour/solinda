# Solinda

Solinda is a comprehensive Android gaming suite that combines classic card games with a modern, high-energy Match-3 puzzle experience. Designed for smooth performance and high-quality visuals, Solinda offers a variety of ways to play and challenge yourself.

## Game Modes

### 🃏 Solitaire Classics
Enjoy the quintessential card games with a clean, intuitive interface optimized for mobile play.
*   **Klondike Solitaire:** The standard game of patience. Build your foundations from Ace to King. Supports both **Deal 1** and **Deal 3** modes.
*   **FreeCell Solitaire:** A highly strategic variant where nearly every deal is a puzzle waiting to be solved. Use your four free cells wisely to clear the board.

### 💎 Jewelinda
Dive into a vibrant Match-3 adventure featuring deep mechanics and explosive gameplay.
*   **Innovative Objectives:**
    *   **Color Collection:** Match and collect specific gems to reach your target.
    *   **Frost Clearance:** Break through layers of "Cracked Ice" by matching gems on frosted tiles.
    *   **Hybrid Levels:** Experience levels that combine multiple objectives for ultimate challenge.
*   **Powerful Special Gems:**
    *   **Bombs:** Form a 4-gem match to create a Bomb. Detonate it to clear a 3x3 area!
    *   **Hypergems:** Create a match of 5 or more (or T/L shapes) to spawn a Hypergem. Swapping a Hypergem with any color triggers a mass explosion of all gems of that color on the board.
*   **Dynamic Visuals:** Satisfying physics-based animations, "squash-and-stretch" effects upon impact, and a custom-built particle engine for spectacular match sequences.
*   **Tactile Feedback:** Feel every match and explosion with carefully tuned haptic feedback.

## Key Features
*   **Adaptive Design:** Seamlessly switches between **Portrait** and **Landscape** layouts, ensuring a great experience on any device.
*   **Responsive UI:** Built with Material 3, featuring a clean look that respects your system's light and dark mode settings.
*   **Persistence:** Your game state is automatically saved. Pick up right where you left off, whether you're mid-shuffle or halfway through a level.
*   **Configurable Experience:** Adjust margins, card reveal factors, and haptic settings via the comprehensive Options menu.

## Getting Started
Solinda is currently in active development. You can try out the latest features by downloading the APK from our CI/CD pipeline:

1.  Go to the **Actions** tab in this GitHub repository.
2.  Select the most recent successful run of the "Android CI" workflow.
3.  Scroll down to the **Artifacts** section and download the `app-debug` file.
4.  Install the APK on your Android device (requires enabling installation from unknown sources).

## Technology Stack
Solinda leverages the latest in Android development to provide a modern and performant experience:
*   **Language:** Pure **Kotlin** for robust and expressive logic.
*   **UI Framework:** **Jetpack Compose** powers the Jewelinda experience, while the Solitaire modes utilize high-performance custom View-based rendering.
*   **Architecture:** **MVVM** (Model-View-ViewModel) for a clean, testable codebase.
*   **Graphics:** A custom-built **Particle Engine** for real-time visual effects and C++ integration via **NDK** for specialized performance needs.
*   **Data Handling:** **Gson** for efficient state serialization and persistence.

## License
Solinda is open-source software licensed under the **GNU AGPLv3**.
