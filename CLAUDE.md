# GoNull - Project Guide

## Overview
GoNull is an Android app blocker that uses commitment contracts to help users break social media addiction. It creates meaningful friction through time-delayed unlocks rather than relying on willpower.

## Tech Stack

### Android App (`app/`)
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM (ViewModel + Compose screens)
- **Database**: Room (entities in `data/local/entity/`, DAOs in `data/local/dao/`)
- **Navigation**: Jetpack Navigation Compose (`ui/navigation/NavGraph.kt`)
- **Background**: AccessibilityService, Foreground Services, WorkManager
- **Min SDK**: 26 | **Target SDK**: 34 | **Kotlin**: 1.9.22 | **Compose Compiler**: 1.5.8

### Video Project (`gonull-video/`)
- **Framework**: Remotion 4.x (React-based video generation)
- **Language**: TypeScript with React 19
- **Output**: 1080x1920 vertical video at 30fps

## Build Commands
- **Debug APK**: `./gradlew assembleDebug`
- **Release APK**: `./gradlew assembleRelease`
- **Run tests**: `./gradlew test`
- **Android lint**: `./gradlew lint`
- **Remotion dev**: `cd gonull-video && npm run dev`
- **Render video**: `cd gonull-video && npm run build` (renders directly into `app/src/main/res/raw/onboarding.mp4`, the asset the app actually plays — no manual copy needed; rebuild the APK afterward). Use `npm run render-preview` to render a scratch copy to `gonull-video/out/` instead.

## Key Architecture Patterns
- Room entities use `@Entity` with table names (e.g., `blocked_apps`, `focus_modes`)
- DAOs use `@Dao` with suspend functions for async operations
- ViewModels expose state via `StateFlow` and are consumed in Compose screens
- Services: `AppBlockerService` (AccessibilityService), `UsageStatsPollingService` (fallback)
- Database singleton at `AppDatabase.getDatabase(context)` with manual migrations
- Blocking strategies: `BlockingStrategy` pattern for hybrid accessibility/usage-stats approach

## File Conventions
- Screens: `ui/screens/{feature}/{Feature}Screen.kt` + `{Feature}ViewModel.kt`
- Components: `ui/components/{ComponentName}.kt`
- Entities: `data/local/entity/{Name}Entity.kt`
- DAOs: `data/local/dao/{Name}Dao.kt`
- Services: `service/{Name}Service.kt` or `service/{Name}Manager.kt`

## Git Workflow
- Always push to remote automatically after every commit (`git push`)

## ADB (if Android SDK is available)
- ADB path: `~/Library/Android/sdk/platform-tools/adb`
- Package name: `app.gonull`
- Main activity: `app.gonull/.ui.MainActivity`
