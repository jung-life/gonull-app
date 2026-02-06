Run Android lint checks on the project.

1. Run `./gradlew lint`
2. Read the lint report at `app/build/reports/lint-results-debug.html` or the XML variant
3. Summarize findings by severity (error, warning, info)
4. For each error/warning, show the file, line, and a brief fix suggestion
5. Prioritize issues that would block Play Store submission