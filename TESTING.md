# GoNull — Testing

## Test layers

| Layer | Location | Runs on | What it covers |
|---|---|---|---|
| Unit (JVM) | `app/src/test/` | local JVM, fast | Pure logic: `FrictionRules`, `FocusModeEntity` |
| Instrumentation | `app/src/androidTest/` | device/emulator/Test Lab | Room DAOs, Compose UI (accessibility disclosure) |

## Run locally

```bash
# Unit tests (fast, no device)
./gradlew testReleaseUnitTest

# Instrumentation tests (needs a connected device or emulator)
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lintRelease
```

## Run on Firebase Test Lab (cross-device, "professional" coverage)

Test Lab runs the suite on a matrix of devices/Android versions and reports
crashes, failures, per-device video, and logcat. The app's Firebase project is
**gonull-bbe08**.

### Option A — Firebase Console (no CLI)
1. Build the two APKs:
   ```bash
   ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
   ```
2. Go to **Firebase Console → (gonull-bbe08) → Test Lab → Run a test → Instrumentation test**.
3. Upload:
   - App APK: `app/build/outputs/apk/debug/app-debug.apk`
   - Test APK: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
4. Pick a device matrix (spread across API 26 / 30 / 33 / 34 and a few OEMs) → Run.

### Option B — gcloud CLI
```bash
brew install --cask google-cloud-sdk        # one-time
gcloud auth login
gcloud config set project gonull-bbe08
scripts/run-testlab.sh            # instrumentation matrix
scripts/run-testlab.sh robo       # automated crawler (no test APK)
```
List currently available devices: `gcloud firebase test android models list`.

## Play Pre-launch Report (automatic, free)
Every build uploaded to a testing track is auto-run on real physical devices by
Google. Results: **Play Console → Test and release → Pre-launch report**
(crashes, ANRs, accessibility, security, screenshots). Check it after each upload.

## Known automation limits
The core blocking flow depends on the **AccessibilityService**, **overlay**, and
**usage-access** permissions, which Test Lab / instrumentation cannot grant
programmatically. So automated coverage focuses on:
- Pure logic (unit tests)
- Data layer (DAO tests)
- Individual Compose screens/components in isolation (e.g. the disclosure screen)

End-to-end blocking, OEM battery-killer survival, and post-reboot re-arming still
need **real-device manual testing** (closed testing + Crashlytics in the wild).
