# GoNull - App Blocker for Android

GoNull is an Android app that uses commitment contracts to help users break social media addiction. Unlike traditional app blockers that rely on willpower, GoNull creates meaningful friction through time-delayed unlocks.

## Features (Phase 1 - MVP)

- **App Selection** - Select which apps to block from your installed apps
- **Blocking Screen** - Intercepts blocked app launches and shows a blocking overlay
- **Time-Delayed Unlock** - Request access with a 30-minute delay before unlocking
- **Unlock Timer** - Countdown notification showing time remaining until unlock
- **Basic Stats** - Track daily blocked attempts

## Project Structure

```
app/src/main/java/app/gonull/
├── GoNullApplication.kt
├── data/
│   └── local/
│       ├── AppDatabase.kt
│       ├── dao/
│       │   ├── BlockedAppDao.kt
│       │   ├── UnlockRequestDao.kt
│       │   └── UsageLogDao.kt
│       └── entity/
│           ├── BlockedAppEntity.kt
│           ├── UnlockRequestEntity.kt
│           └── UsageLogEntity.kt
├── service/
│   ├── AppBlockerService.kt (AccessibilityService)
│   └── UnlockTimerService.kt (Foreground Service)
├── receiver/
│   ├── BootReceiver.kt
│   └── AlarmReceiver.kt
├── ui/
│   ├── MainActivity.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── navigation/
│   │   └── NavGraph.kt
│   └── screens/
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   └── HomeViewModel.kt
│       ├── appselection/
│       │   ├── AppSelectionScreen.kt
│       │   └── AppSelectionViewModel.kt
│       ├── blocking/
│       │   └── BlockingOverlayActivity.kt
│       └── settings/
│           └── SettingsScreen.kt
└── util/
    ├── Constants.kt
    └── PermissionHelper.kt
```

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0+)
- **Target SDK**: 34 (Android 14)
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM with Clean Architecture principles
- **Database**: Room
- **Async**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose

## Dependencies

- Jetpack Compose
- Room Database
- Navigation Compose
- Coroutines
- Material3
- Coil (for image loading)
- Accompanist Permissions

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with API level 34

### Building the Project

1. Open the project in Android Studio

2. Sync Gradle files (Android Studio will prompt you)

3. Build the project:
```bash
./gradlew build
```

4. Run on emulator or device:
```bash
./gradlew installDebug
```

Or use the Run button in Android Studio.

## Required Permissions

The app requires the following permissions to function:

1. **Accessibility Service** (Required)
   - Detects when blocked apps are opened
   - User must manually enable in Settings → Accessibility

2. **Display Over Other Apps** (Required)
   - Shows the blocking screen overlay
   - User must manually grant in Settings → Special app access

3. **Usage Stats Access** (Optional)
   - For detailed app usage tracking
   - User can optionally grant in Settings → Special app access

## How It Works

1. **Setup**: User grants required permissions and selects apps to block
2. **Blocking**: When a blocked app is launched, AccessibilityService detects it and shows the blocking overlay
3. **Request**: User can request temporary access, which starts a 30-minute countdown timer
4. **Unlock**: After the timer completes, the app is accessible for 15 minutes
5. **Re-block**: After the access window expires, the app is blocked again

## Architecture

### Data Layer
- **Room Database**: Persists blocked apps, unlock requests, and usage logs
- **DAOs**: Data access objects for database operations
- **Entities**: Database table definitions

### Domain Layer
- Simple use cases embedded in ViewModels for Phase 1

### Presentation Layer
- **Jetpack Compose**: Modern declarative UI
- **ViewModels**: Manage UI state and business logic
- **Navigation**: Compose Navigation for screen transitions

### Services
- **AppBlockerService**: AccessibilityService that monitors app launches
- **UnlockTimerService**: Foreground service for countdown timer with notification

## Next Steps (Phase 2)

- [ ] Persistent timer across app restarts
- [ ] Handle phone reboots and restore timers
- [ ] Access window enforcement (auto re-block after 15 min)
- [ ] Usage tracking during unlocked periods
- [ ] Stats screen with charts
- [ ] Configurable delay times per app
- [ ] Onboarding flow for first-time users

## Future Features (Post-MVP)

- Accountability partner system (requires backend)
- Financial penalties via Stripe
- Device Owner mode (prevent uninstall)
- Cloud sync
- Schedules (different rules for different times)

## Development

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Keep composables small and focused
- Use state hoisting for reusable components

### Testing
- Unit tests for ViewModels
- Integration tests for database operations
- UI tests for critical user flows

## Known Issues

- Custom fonts (JetBrains Mono, Inter) are currently using system fallbacks
  - Download and add font files to `app/src/main/res/font/` to enable custom fonts
- Notification icons are using system defaults
  - Add custom drawable icons for timer notifications

## License

[Add your license here]

## Contributing

[Add contributing guidelines here]

## Contact

[Add contact information here]
