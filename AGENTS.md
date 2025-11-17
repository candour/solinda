# AGENTS.md

This document provides instructions for AI agents working on the Solinda codebase.

## Coding Conventions

This project follows the official Kotlin and Android coding conventions. Please adhere to these style guides when writing and modifying code:

*   **Kotlin Style Guide:** [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
*   **Android Kotlin Style Guide:** [https://developer.android.com/kotlin/style-guide](https://developer.android.com/kotlin/style-guide)

## Building the Project

The primary build command for this project is:

```bash
./gradlew assembleDebug
```

This command builds the debug version of the application and places the APK in `app/build/outputs/apk/debug/app-debug.apk`.

## Testing

To run the unit tests for this project, use the following command:

```bash
./gradlew test
```

## Static Analysis

To run the static analysis checks, use the following command:

```bash
./gradlew check
```

This command will run a series of checks, including linting and code style verification, to ensure the code quality is maintained.
