Run the project tests and analyze results.

1. Run `./gradlew test` for unit tests
2. If a connected device is available, optionally run `./gradlew connectedAndroidTest` for instrumentation tests
3. Parse the test output and report:
   - Total tests run, passed, failed, skipped
   - Details on any failures with the failing assertion and relevant code
   - Suggestions for fixing failures

If $ARGUMENTS is provided, run only tests matching that filter: `./gradlew test --tests "*$ARGUMENTS*"`