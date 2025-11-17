# Solinda

Solinda is a classic Solitaire game for Android, perfect for a quick break or a relaxing challenge.

## Building the Project

To build the project and generate an APK, follow these steps:

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/your-repository.git
    cd your-repository
    ```

2.  **Ensure you have Java Development Kit (JDK) 17 or higher installed.**

3.  **Make the Gradle wrapper executable:**
    ```bash
    chmod +x gradlew
    ```

4.  **Build the debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
    The generated APK will be located in `app/build/outputs/apk/debug/app-debug.apk`. You can install this APK on an Android device or emulator to play the game.
