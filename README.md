# Solinda

Solinda is a classic Solitaire game for Android, perfect for a quick break or a relaxing challenge.

## Building the Project

This project is automatically built using GitHub Actions when changes are pushed to the `main` branch. The resulting APK is available as a build artifact.

## Building from the Command Line

To build the project from the command line, you'll need to set up the Android SDK and its dependencies.

1.  **Download and set up the Android SDK:**

    ```bash
    wget https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -P /tmp
    rm -rf android-sdk && mkdir -p android-sdk/cmdline-tools/latest && unzip /tmp/commandlinetools-linux-13114758_latest.zip -d android-sdk/cmdline-tools/ && mv android-sdk/cmdline-tools/cmdline-tools/* android-sdk/cmdline-tools/latest/
    ```

2.  **Set environment variables:**

    ```bash
    export ANDROID_HOME=$PWD/android-sdk
    export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
    ```

3.  **Install required SDK packages:**

    ```bash
    yes | sdkmanager "platform-tools" "platforms;android-36" "build-tools;34.0.0" "ndk;27.0.12077973"
    ```

4.  **Accept SDK licenses:**

    ```bash
    yes | sdkmanager --licenses
    ```

5.  **Run the build:**

    ```bash
    ./gradlew assembleDebug
    ```
