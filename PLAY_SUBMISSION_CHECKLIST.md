# GoNull — Google Play Submission Checklist

All declaration answers in one place. Copy-paste the quoted text directly into
Play Console. Track: **Internal testing** first.

- **Package:** `app.gonull`
- **Version:** code auto-derived from git commit count (currently 35), name `1.0.5`
- **AAB:** `app/build/outputs/bundle/release/app-release.aab` (rebuild with `./gradlew :app:bundleRelease` after committing)
- **Privacy policy (LIVE):** https://gonull.app/privacy
- **Disclosure video:** https://www.youtube.com/watch?v=wOHIq2baar0 (set to Unlisted)

---

## 1. Build & release
- [ ] Commit any changes first (version code only advances on commit).
- [ ] `./gradlew :app:bundleRelease`
- [ ] Upload `app-release.aab` to **Internal testing → Create release**.
- [ ] Add release notes (tester-facing).

---

## 2. AccessibilityServices declaration (Policy → App content)

GoNull is **NOT** an accessibility tool — `isAccessibilityTool` is intentionally
unset. Do **not** set it to true. This means the prominent-disclosure path applies.

- **Why does your app need the AccessibilityServices API?**
  - [x] **App functionality** (this one ONLY)
  - [ ] Analytics / Developer communications / Advertising / Fraud prevention / Personalization / Account management → all unchecked
- **Do you collect/share personal or sensitive data using the API?** → **No**
- **Video link:** `https://www.youtube.com/watch?v=wOHIq2baar0`

> Description (if asked): "GoNull uses the Accessibility Service solely to detect
> when a user opens an app they've chosen to block, so the app can display its
> blocking screen. This is core app functionality with no other purpose. It reads
> only the foreground app's package name, on-device, and collects/shares nothing."

---

## 3. Data safety

**Overview**
- [ ] Collects or shares required user data types? → **Yes**
- [ ] All collected data encrypted in transit? → **Yes**
- [ ] Provide a way to request data deletion? → **Yes** (uninstall deletes all on-device data; users can email `hello@gonull.app` to request crash-data deletion)

**Data types to declare — Firebase Crashlytics only.** For every item below:
Collected **Yes** · Shared **No** · Ephemeral **No** · Required **Yes** · Encrypted in transit **Yes**.

| Category → Data type | Purpose(s) |
|---|---|
| App info and performance → **Crash logs** | App functionality, Analytics |
| App info and performance → **Diagnostics** | Analytics |
| **Device or other IDs** | Analytics |

**Do NOT declare these** (stored only on-device, never transmitted, so not "collected"):
- Blocked apps list, usage stats, streaks, focus-mode history
- Journal entries, commitments, reflections, trigger/craving logs
- Accountability-partner name/phone (logged locally only)
- → Health/fitness, Messages, Contacts, Photos, Location, Financial = all **No**

> Firebase Analytics SDK was removed — do not declare App activity / interactions.

---

## 4. App content (other sections)

- **Ads:** Does your app contain ads? → **No**
- **App access:** **All functionality available without special access** (no login).
  Add to release **Testing instructions**:
  > "On first launch, complete onboarding and grant: Accessibility (enables
  > blocking), Display over other apps (shows the block screen), and Usage access.
  > No login required. To test: add an app to the block list, then open it — the
  > block screen appears."
- **Content rating (IARC questionnaire):**
  - Category: **Utility / Productivity**
  - Violence, sexual content, profanity, drugs, gambling → **No** to all
  - User-generated content shared with others → **No**
  - Expected result: Everyone / PEGI 3
- **Target audience and content:**
  - Age groups: **13–15, 16–17, 18+** (leave under-13 unchecked)
  - Appeal to children? → **No**
- **Health apps:** provide health features? → **No** (digital wellbeing, not health)
- **Financial features** → No
- **News app** → No
- **Government app** → No
- **COVID-19 contact tracing / status** → No

---

## 5. Permission declarations

- **`QUERY_ALL_PACKAGES`:**
  > "GoNull is a device-utility app blocker. It needs the list of installed apps so
  > users can choose which apps to block from the App Selection screen. App names
  > and icons are read on-device via PackageManager and never transmitted."
- **`FOREGROUND_SERVICE_SPECIAL_USE`** (if prompted):
  > "Special-use foreground services run the unlock countdown timer, monitor active
  > access sessions for the expiry warning, and (optionally) run the usage-stats
  > polling fallback blocker — all user-facing, ongoing tasks that must continue
  > while the user is in other apps."
- **AccessibilityServices:** see section 2.

---

## 6. Store listing reminders
- [ ] Screenshots, icon, feature graphic, and description must **not** appeal to
      children (keep adult-framed — already are).
- [ ] Assets available: `play-store-assets/feature-graphic-1024x500.png`, `icon-512.png`.

---

## 7. Pre-submit verification
- [x] `gonull.app/privacy` returns 200 with correct content (the GoNull team, hello@gonull.app, Crashlytics only).
- [ ] Disclosure video set to **Unlisted** and opens from an incognito window.
- [ ] AAB version code is higher than any previously uploaded (auto-handled; currently 35).
- [ ] Manifest `isAccessibilityTool` remains unset.

---

## 8. Submit
- [ ] Save all declarations.
- [ ] **Review release → Start rollout to Internal testing.**
- [ ] Share the tester opt-in link.

> Note for new personal developer accounts: Google may require ~14 days of closed
> testing with 12+ testers before Production is unlocked.
