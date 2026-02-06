Create a new Compose screen for: $ARGUMENTS

Follow the project's existing patterns:
1. Create `ui/screens/{feature}/{Feature}Screen.kt` with a `@Composable` function
2. Create `ui/screens/{feature}/{Feature}ViewModel.kt` extending `ViewModel` with `StateFlow` for UI state
3. Use Material 3 components consistent with existing screens
4. Add the route to `ui/navigation/NavGraph.kt`

Reference `HomeScreen.kt` and `HomeViewModel.kt` as the pattern to follow. Use the project's existing theme (Color.kt, Type.kt, Theme.kt).