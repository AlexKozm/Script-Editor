# Script Editor

Simple script editor and runner for Kotlin scripts.
Written with Jetpack Compose Multiplatform.

## What is implemented

1. 2 panes: for editing and for output.
2. You could load and save files.
3. You could edit script while there is a running one.
4. You can see errors if the script couldn’t be interpreted.
5. You could stop running script.
6. You can see exit code of a script.
7. Syntax highlighting for basic Kotlin keywords
8. You could click on location inside error to navigate
   to the exact cursor position in code.
9. App follows system color theme.

**All required functionality is implemented**

## Some details about implementation

- There is no lines numbering. This could be improved.
- All code could be written in one module as it is small.
  But I decided to have at least 2 modules for code presentation purposes.
- There is only one test for cursor positioning.
  You could run it with `./gradlew kotest` on Linux.
  All other backend and UI have no tests.

## Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

## Demonstration

Watch [video](2025-12-29_16-31-52.mp4)