# Accessibility Services Justification

This document is the written justification GoNull submits to the Google Play
Console under **App content → Accessibility → How does your app use the
Accessibility API?** It mirrors the in-app onboarding disclosure so reviewers
can verify they match.

> Use this as the source text. Paste the relevant section into Play Console; the
> short version is closer to the field's character limit, the long version is
> the authoritative copy and matches the in-app disclosure.

---

## Short version (Play Console field)

GoNull is an app blocker for users with ADHD, executive-function challenges,
and digital-impulse disorders. The Accessibility Service is used **only** to
detect when a user-selected blocked app comes to the foreground (the
`TYPE_WINDOW_STATE_CHANGED` event), so GoNull can immediately render an
on-screen friction barrier asking the user to confirm or wait through a
delay before proceeding.

GoNull does not read the contents of any screen, capture text, monitor
keystrokes, or record any user input. Only the package-name field of the
window-state-change event is inspected, and only against a list of apps the
user has explicitly chosen to block. Nothing is transmitted off the device —
the app does not declare the `INTERNET` permission. Accessibility access is
the only practical way to reliably interrupt an addictive-app launch on
modern Android; the alternative (`UsageStatsManager` polling) introduces a
2-second lag during which the harmful behavior is already occurring,
defeating the user's goal.

## Long version (in-app disclosure & developer-response template)

### Who GoNull is for

GoNull is a digital-wellbeing tool aimed at users who struggle with
self-regulation around specific apps — most commonly social media, video,
news, and gambling apps. Our target users include people with:

- ADHD, where impulsive task-switching to a high-stimulation app derails work or sleep.
- Executive-function challenges (autism spectrum, traumatic brain injury, etc.) where the gap between intention and behavior is large.
- Digital impulse-control disorders, often co-occurring with anxiety or depression.

These users have repeatedly expressed in research (e.g., the work of Adam
Alter, Cal Newport, and clinical literature on internet-use disorder) that
**friction at the moment of impulse** is more effective than retrospective
willpower. That moment-of-impulse intervention is exactly what the
Accessibility Service enables.

### What GoNull does with the Accessibility API

Our `AccessibilityService` (`AppBlockerService.kt`) listens for a single event
type:

- `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED`

When such an event fires, the service:

1. Reads `event.packageName` (the package that just took the foreground).
2. Compares it against an in-memory cache of user-chosen blocked apps.
3. If — and only if — the package is on that list, launches GoNull's blocking activity.

We do not call any of the following:

- `AccessibilityNodeInfo` traversal, `getRootInActiveWindow`, or any
  text-extraction API. **Screen content is never inspected.**
- `performAction` on any node. The service does not interact with other apps;
  it never clicks, scrolls, or fills fields on the user's behalf.
- `flagRequestFilterKeyEvents` or `serviceInfo.flags` related to keystroke
  capture. **Keys are never recorded.**

The service is configured in `accessibility_service_config.xml` to receive
only `typeWindowStateChanged` events, with no node-info or content retrieval
flags set.

### What we collect and where it goes

- **Inspected:** the foreground `packageName` field, in memory only.
- **Logged:** when a user-blocked app is intercepted, GoNull writes a single
  row to its local database recording the package and the timestamp. This is
  used to power the "Today" stats card in-app.
- **Transmitted:** nothing. GoNull does not declare `INTERNET` and contains
  no networking code, analytics SDKs, advertising IDs, or third-party
  trackers.
- **Retention:** the local database is wiped on uninstall. Users can clear
  individual entries from inside the app.

### Why we need the Accessibility API rather than `UsageStatsManager`

We support both approaches and ship them as a hybrid. We default to the
Accessibility approach because it is the only Android API that allows us to
**block at the moment the user opens the app**, which is the entire user
benefit. `UsageStatsManager` polling can only tell us *after the fact* that
the user has been in the foreground app for 2+ seconds — by which point a
single dopamine cycle has already played, and our intervention is too late
to be therapeutic. Users with ADHD or impulse-control disorders are
particularly sensitive to this lag; clinical literature on
exposure-and-response prevention shows that delaying the response to a
trigger is what builds tolerance.

For users who are uncomfortable granting Accessibility Service access —
or whose enterprise/MDM policies prohibit it — GoNull falls back to a
`UsageStatsManager` polling service. The fallback is functional but the
2-second lag is documented in onboarding as a degraded experience, not the
default.

### How the user grants and revokes access

- During onboarding, the user is shown a dedicated screen (`OnboardingScreen.kt`)
  that explains the Accessibility Service in plain language: what is read, what
  is not read, what is stored, and why this is the only practical mechanism.
  See the in-app screen at *Onboarding → "Accessibility Service"*.
- The user must then leave the app and turn the service on in Android's
  Accessibility settings; we never claim it can be enabled silently.
- The user can revoke access at any moment from the same Accessibility
  settings screen, or by uninstalling the app. GoNull also surfaces a
  permission-status card in **Settings → Permissions** so the user can see
  the current state without leaving the app.

### Privacy & compliance commitments

- We do not sell or share any data; there is none to sell or share.
- We do not embed third-party SDKs that have separate data flows. Accompanist
  and Coil ship code only; they make no network calls of their own. ExoPlayer
  is used solely to play a bundled local onboarding video file
  (`res/raw/onboarding.mp4`).
- Our privacy policy is in `legal/PRIVACY_POLICY.md` and will be hosted at
  `[POLICY URL]` before submission.

### If the reviewer believes Accessibility is not justified

If a reviewer concludes our Accessibility usage is unjustified, our
contingency is:

1. Switch the production build's default to `UsageStatsBased` blocking.
2. Demote Accessibility to an opt-in "advanced" mode in Settings, behind a
   second confirmation dialog.
3. Resubmit, with this document updated to reflect the new defaults.

The hybrid architecture (`BlockingStrategy.kt`) was built specifically to
support this fallback, so the change is a configuration toggle, not a
re-engineering effort.

### Contact

For policy-review questions: `[CONTACT EMAIL]`.
For demo access: a sample APK and a 30-second screen-record demonstrating the
Accessibility flow from grant to block-and-unblock is available on request.
