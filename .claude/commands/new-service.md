Create a new Android service for: $ARGUMENTS

Follow the project's existing patterns:
1. Create `service/{Name}Service.kt` or `service/{Name}Manager.kt` depending on whether it's an Android Service or a utility manager
2. For Android Services: extend `Service` or `LifecycleService`, implement proper `onStartCommand`/`onBind`, create notification channel if foreground
3. For Managers: create a singleton or class that can be instantiated from Application/Activity context
4. Register in `AndroidManifest.xml` if it's an Android Service
5. Use coroutines (`CoroutineScope(Dispatchers.IO)`) for background work

Reference existing services like `AppBlockerService.kt`, `UnlockTimerService.kt`, and managers like `FocusModeManager.kt` for patterns.