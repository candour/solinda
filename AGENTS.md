# AGENTS.md

This document provides instructions for AI agents working on the Solinda codebase.

## Coding Conventions

This project follows the official Kotlin and Android coding conventions. Please adhere to these style guides when writing and modifying code:

*   **Kotlin Style Guide:** [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
*   **Android Kotlin Style Guide:** [https://developer.android.com/kotlin/style-guide](https://developer.android.com/kotlin/style-guide)

## Pre-commit Hooks

This repository includes a pre-commit hook that runs `./gradlew test` to ensure that all tests pass before a commit is made. As an AI agent, you are not required to install the hook, but you must ensure that all tests pass before submitting your changes. You can run the tests manually with the following command:

```bash
./gradlew test
```
