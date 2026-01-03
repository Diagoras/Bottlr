<a name="readme-top"></a>

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <img src="https://raw.githubusercontent.com/Diagoras/Bottlr/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp">
<h1 align="center">Bottlr</h1>
  <p align="center">
    A simple, lightweight tool for managing your liquor inventory.
    <br />
    <a href="mailto:bottlrdev@gmail.com" target="Bottlr Bug Report">Report Bug</a>
    ·
    <a href="mailto:bottlrdev@gmail.com" target="Bottlr Feature Request">Request Feature</a>
  </p>
</div>

---

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#architecture">Architecture</a></li>
    <li><a href="#getting-started">Getting Started</a></li>
    <li><a href="#testing">Testing</a></li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

---

<!-- ABOUT THE PROJECT -->
## About The Project

Bottlr is a simple, lightweight tool for managing your liquor inventory. Whether you're a personal collector, a bartender, or a manager, Bottlr has the cataloguing and inventory tools you'll need to manage your collection, regardless of size.

### Features

- **Bottle Cataloguing**: Easily add, edit, and manage your liquor bottles with images and detailed information
- **Cocktail Recipes**: Create and manage your cocktail recipes
- **Cloud Storage**: Remotely store your inventory data with Firebase and Google SSO
- **Social Media Sharing**: Share your favorite bottles on social media platforms
- **Google Shopping Integration**: One-click search to find listings for your bottles
- **Inventory Search**: Quickly find specific bottles or categories within your inventory

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Built With

- **Kotlin** - Primary language
- **Android SDK 35** - Target API level
- **Jetpack Components**:
  - Room - Local database
  - Navigation Component - Fragment navigation
  - ViewModel & LiveData - UI state management
  - Hilt - Dependency injection
- **Firebase** - Authentication, Firestore, Cloud Storage
- **Glide** - Image loading
- **Espresso** - UI testing
- **MockK** - Unit testing

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Architecture

Bottlr uses **MVVM (Model-View-ViewModel)** architecture with the following structure:

```
app/src/main/kotlin/com/bottlr/app/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   └── entities/     # Room entities
│   └── repository/       # Data repositories
├── di/                   # Hilt dependency injection modules
└── ui/
    ├── details/          # Bottle details screen
    ├── editor/           # Add/edit bottle screen
    ├── gallery/          # Bottle gallery screen
    ├── home/             # Home screen
    ├── search/           # Search functionality
    └── settings/         # Settings & Firebase sync
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- Android SDK 35
- JDK 17
- Android device or emulator running Android 10.0+ (API 29+)
- Docker (for running Firebase integration tests)

### Installation

1. Clone the repo
   ```sh
   git clone https://github.com/Diagoras/Bottlr.git
   ```

2. Open in Android Studio

3. Replace `app/google-services.json` with your Firebase configuration file

4. Build and run
   ```sh
   ./gradlew assembleDebug
   ```

### Building a Release

```sh
# Create signing.properties with your keystore details
# Then build the release AAB for Play Store:
./gradlew bundleRelease
```

The signed AAB will be at `app/build/outputs/bundle/release/app-release.aab`

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Testing

Bottlr has comprehensive test coverage:

### Unit Tests (64 tests)
```sh
./gradlew test
```

Tests for ViewModels, Repositories, and business logic.

### Instrumented Tests (69 tests)
```sh
./gradlew connectedDebugAndroidTest
```

Includes:
- **DAO Tests**: Room database operations
- **UI Flow Tests**: Add, view, edit bottle workflows
- **Photo Flow Tests**: Camera and gallery integration
- **Navigation Tests**: Screen navigation
- **Firebase Sync Tests**: Cloud sync and data erasure
- **Smoke Tests**: App startup verification

### Firebase Integration Tests

Firebase integration tests run against local emulators via Docker. **Docker must be installed** for these tests to work.

The emulators start automatically when running instrumented tests and stop when tests complete:

```sh
# Docker will be started/stopped automatically
./gradlew connectedDebugAndroidTest

# Or manually manage emulators:
./gradlew startFirebaseEmulators   # Start Docker container
./gradlew stopFirebaseEmulators    # Stop Docker container
```

Firebase Emulator UI is available at http://localhost:4000 while tests run.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Usage

### Basic Functionality

| Action | How To |
|--------|--------|
| Add a Bottle | Tap the "+" FAB from the gallery |
| Edit a Bottle | Open bottle details, tap the edit button |
| Search | Tap the search icon in the gallery |
| Share | Tap share icon from bottle details |
| Buy | Tap shopping icon to search Google |
| Cloud Sync | Go to Settings, sign in with Google |

### Bottle Fields

| Field | Description |
|-------|-------------|
| Name | Specific name of your bottle (required) |
| Distillery | Manufacturer of the bottle |
| Region | Where the spirit is from |
| Type | Spirit type (Tequila, Scotch, Vodka, etc.) |
| Age | How long the spirit was aged |
| ABV | Alcohol By Volume percentage |
| Rating | Your rating from 1-10 |
| Notes | Personal tasting notes |
| Keywords | Comma-separated tags for search |

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Roadmap

- [ ] NFC Tagging - Scan and save bottle data to NFC tags
- [ ] Location Tracking - Save where you found a bottle
- [ ] Cocktail Book - Track what you can make with your inventory
- [ ] Personalized Suggestions - Recommendations based on your collection
- [ ] Database Lookup - Search pre-populated bottle database
- [ ] Cost-Per-Pour Calculator - Pricing for cocktails and pours

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Contact

bottlrdev@gmail.com

Project Link: [https://github.com/Diagoras/Bottlr](https://github.com/Diagoras/Bottlr)

<p align="right">(<a href="#readme-top">back to top</a>)</p>
