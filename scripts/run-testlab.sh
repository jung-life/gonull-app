#!/usr/bin/env bash
#
# Run GoNull's instrumentation tests on Firebase Test Lab across a device matrix.
# This exercises the test suite on real-device-equivalent configs spanning the
# app's supported API range (minSdk 26 .. targetSdk 35) in one shot.
#
# Prerequisites (one-time):
#   brew install --cask google-cloud-sdk        # if gcloud is not installed
#   gcloud auth login
#   gcloud config set project gonull-bbe08       # the app's Firebase project
#
# Usage:
#   scripts/run-testlab.sh                 # instrumentation matrix (default)
#   scripts/run-testlab.sh robo            # automated crawler, no test APK
#
set -euo pipefail
cd "$(dirname "$0")/.."

APP_APK=app/build/outputs/apk/debug/app-debug.apk
TEST_APK=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
MODE="${1:-instrumentation}"

echo "==> Building debug + androidTest APKs"
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest

# Virtual devices spanning the supported API range. Add physical models for
# OEM coverage, e.g. --device model=oriole,version=33 (Pixel 6).
# List what's currently available: gcloud firebase test android models list
DEVICES=(
  --device model=MediumPhone.arm,version=26
  --device model=MediumPhone.arm,version=30
  --device model=MediumPhone.arm,version=33
  --device model=MediumPhone.arm,version=34
)

if [[ "$MODE" == "robo" ]]; then
  echo "==> Running Robo crawler on Test Lab"
  gcloud firebase test android run \
    --type robo \
    --app "$APP_APK" \
    "${DEVICES[@]}" \
    --timeout 5m
else
  echo "==> Running instrumentation tests on Test Lab matrix"
  gcloud firebase test android run \
    --type instrumentation \
    --app "$APP_APK" \
    --test "$TEST_APK" \
    "${DEVICES[@]}" \
    --timeout 10m
fi
