# Solinda

Solinda is a classic Solitaire game for Android, perfect for a quick break or a relaxing challenge.

## Building the Project

This project is automatically built using GitHub Actions when changes are pushed to the `main` branch. The resulting APK is available as a build artifact.

## Pre-commit Hooks

This repository includes a pre-commit hook that runs `./gradlew test` to ensure that all tests pass before a commit is made. To install the hook, run the following command from the root of the repository:

```bash
./install-hooks.sh
```
