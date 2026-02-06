Review the code for the file or feature specified: $ARGUMENTS

Perform a thorough code review focusing on:
1. **Android best practices** - lifecycle awareness, memory leaks, context usage
2. **Compose best practices** - recomposition stability, state hoisting, side effects
3. **Room patterns** - proper suspend usage, migration correctness, query efficiency
4. **Security** - no hardcoded secrets, proper permission handling
5. **Performance** - unnecessary allocations, blocking main thread, efficient coroutine usage
6. **Google Play compliance** - accessibility service justification, battery impact, privacy

If no file/feature is specified, review recent git changes with `git diff HEAD~1`.

Output findings as a list with severity (critical/warning/suggestion) and specific line references.