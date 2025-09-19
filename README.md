# 📱 Device Info

## Table of Contents

- [Features](#features)
- [Tech Stack & Libraries](#tech-stack--libraries)
- [Setup & Installation](#setup--installation)
- [How to Use](#how-to-use)
- [Code Overview](#code-overview)
    - [MainActivity.kt](#mainactivitykt)
    - [UI Components](#ui-components)
- [Contributing](#contributing)
- [License](#license)

## Features

* **Comprehensive Device Information:** Displays a wide range of device details.
* **Organized Tabs:** Information is categorized into logical tabs for easy navigation:
    * **Device:** General device information (Manufacturer, Model, Brand, Android Version, Kernel,
      etc.).
    * **SoC (System on Chip):** Details about the processor and chipset (e.g., CPU architecture,
      cores, clock speed - *Assuming you'll implement this*).
    * **Memory:** Information about RAM and storage (e.g., total RAM, available RAM, internal
      storage, external storage - *Assuming you'll implement this*).
    * **Screen:** Screen specifications (e.g., resolution, density, refresh rate - *Partially
      implemented, you have resolution and density*).
    * **Camera:** Details about front and rear cameras (e.g., resolution, focal length, aperture -
      *Assuming you'll implement this*).
* **Modern UI:** Built with Jetpack Compose for a clean and responsive user interface.
* **App Info Dialog:** Provides version information and a link to this source code repository.

## Tech Stack & Libraries

* **Kotlin:** Primary programming language.
* **Jetpack Compose:** Modern Android UI toolkit.
    * **Material 3:** For UI components and styling.
    * **Compose Navigation (Pager):** For tabbed navigation.
* **Android SDK:** Core Android framework.
* **Coroutines:** For asynchronous operations.

## Setup & Installation

1. **Clone the repository:**
2. **Open in Android Studio:**
    * Open Android Studio (latest stable version recommended).
    * Select "Open an Existing Project".
    * Navigate to the cloned `DeviceInfo` directory and select it.
3. **Build the project:**
    * Android Studio should automatically sync the Gradle files.
    * Once synced, build the project by clicking `Build > Make Project` or by clicking the "Play"
      button to run it on an emulator or a connected physical device.

**Requirements:**

* Android Studio (latest version)
* Android SDK targeting a reasonable `minSdkVersion` (as defined in your `build.gradle`).

## How to Use

1. Launch the app on your Android device or emulator.
2. The main screen will display "Device Info" in the app bar.
3. Navigate through the different categories of information using the tabs: "Device", "SoC", "
   Memory", "Screen", and "Camera".
   4.Tap on the information icon (ℹ️) in the top-right corner of the app bar to view the app's
   version and a link to this GitHub repository.

## Code Overview

The main logic for the UI and data fetching is located in
`app/src/main/java/com/flandolf/deviceinfo/`.

### `MainActivity.kt`

* **`MainActivity`:** The entry point of the application. Sets up the `DeviceInfoTheme` and calls
  the main composable `MainScreen`.
* **`MainScreen` Composable:**
    * Manages the overall scaffold of the app, including the `TopAppBar` and `TabRow`.
    * Uses a `HorizontalPager` to handle swiping between different information tabs.
    * Includes an action item in the `TopAppBar` to display the `AppInfoDialog`.
* **`AppInfoDialog` Composable:**
    * Displays an `AlertDialog` showing the app's name, version, and a "View Source" button linking
      to the GitHub repository.
* **Tab-Specific Composables:**
    * **`DeviceInfoTab`:** Displays general device information fetched using `gatherDeviceInfo()`.
    * `SoCInfoTab`, `MemoryInfoTab`, `ScreenInfoTab`, `CameraInfoTab`: Placeholder composables for
      future implementation of specific information categories. *(You'll need to detail these as you
      build them out).*
* **`gatherDeviceInfo()` Function:**
    * Collects various device properties using `android.os.Build` and
      `Context.resources.displayMetrics`.

### UI Components

* **`PropertyRow` Composable:** (Assuming you have this or will create it based on your
  `DeviceInfoTab` usage) A reusable composable to display a label-value pair, used for showing
  individual pieces of device information.

## Contributing

Contributions are welcome! If you'd like to contribute, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature-name`).
3. Make your changes.
4. Commit your changes (`git commit -m 'Add some feature'`).
5. Push to the branch (`git push origin feature/your-feature-name`).
6. Open a Pull Request.

Please make sure to update tests as appropriate.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for
details.

