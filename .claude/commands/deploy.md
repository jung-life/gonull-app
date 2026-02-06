Build and deploy the app to a connected Android device.

1. Run `./gradlew assembleDebug`
2. If build succeeds, check for connected devices with `~/Library/Android/sdk/platform-tools/adb devices`
3. Install the APK with `~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk`
4. Launch the app with `~/Library/Android/sdk/platform-tools/adb shell am start -n app.gonull/.ui.MainActivity`

Report each step's result. If any step fails, diagnose the issue.