# GoNull Privacy Policy

**Last updated:** April 26, 2026

## Summary

GoNull is an on-device app blocker. It does **not** transmit your data over the
network. Everything the app records — your blocked apps list, usage history,
journal entries, focus-mode activity — is stored only on your device, in private
storage owned by the app. You control it; uninstalling the app deletes it.

## Who we are

GoNull is published by the GoNull team ("we", "us"). You can reach us at
`hello@gonull.app` for any privacy question.

## What GoNull accesses on your device

GoNull requests the following Android permissions. Each is used only for the
purpose described, and the data never leaves your device unless you explicitly
opt in to a future cloud feature.

| Permission | Why GoNull uses it | What we do with the data |
|---|---|---|
| **Accessibility Service** | Detect when a blocked app is brought to the foreground so we can show the blocking screen. | We read `TYPE_WINDOW_STATE_CHANGED` events and look at the foreground package name. The package name is held in memory only long enough to decide whether to block. Accessibility event content (text on the screen, what you type, etc.) is **never** read or stored. |
| **Display over other apps (`SYSTEM_ALERT_WINDOW`)** | Render the blocking screen on top of the app you tried to open. | No data is collected via this permission. |
| **Usage access (`PACKAGE_USAGE_STATS`, optional)** | Power the "Usage Mirror" stats card and the polling-based fallback blocker. | Aggregated foreground-time data (per app, per day) is read from the Android system and stored in our local database. |
| **Query all packages (`QUERY_ALL_PACKAGES`)** | Show the list of installed apps on the App Selection screen. | We read app names and icons from `PackageManager`. Nothing about your installed apps is transmitted off the device. |
| **Foreground service** | Run the unlock countdown, session-expiration monitor, and (if you choose) the polling blocker. | No data is collected by these services. |
| **Receive boot completed** | Re-arm an in-progress unlock timer or expiration warning after you reboot the phone. | No data is collected. |
| **Post notifications** | Show the unlock countdown, "session expiring" warning, and similar prompts. | No data is collected. |
| **Device Admin (Lock Mode, optional)** | Prevent you from uninstalling GoNull while a block session is active. You enable this manually. | No data is collected. You can revoke this in **Settings → Security → Device admin apps** at any time. |

## What GoNull stores on your device

The local Room database contains, for example:

- Apps you've chosen to block, your delay/budget settings for each, and any pending unblock timers.
- Unlock requests and emergency-bypass events.
- Daily and per-session usage counts for blocked apps.
- Streak history.
- Focus-mode activity (Gym, Meditation, Analog).
- Journal entries, mood tags, and morning commitments **that you write yourself**.
- Trigger and craving logs, post-session reflections, and implementation intentions **that you write yourself**.
- Accountability-partner contact details (name and phone number) **only if you choose to add a partner**. As of this version, partner notifications are logged locally — nothing is sent to your partner or to any server.

This data lives in private app storage. Other apps cannot read it. Uninstalling
GoNull deletes the entire database.

## What GoNull does **not** do

- **No advertising IDs** are read.
- **No screen content** is captured. The Accessibility Service only inspects the package-name field of window-state-change events.
- **No clipboard, camera, microphone, contacts, location, or SMS** access.
- **No personally identifiable account data** (no email, name, phone, sign-in tokens) leaves your device.

## Crashlytics & Firebase Analytics

GoNull bundles **Firebase Crashlytics** (for stability monitoring) and the
**Firebase Analytics SDK** (a runtime dependency Crashlytics relies on). These
are the only third-party SDKs in the app and the only reason GoNull contacts
the internet at all.

What is sent off-device, and only when a crash or fatal error occurs:

- A stack trace of the crash.
- Device model, OS version, and Android API level.
- The version of GoNull that crashed.
- A randomly-generated installation ID (not tied to your Google account, not
  resettable from inside our app — you can reset it by clearing app data or
  reinstalling).

What is **not** sent:

- The list of apps you've blocked.
- Anything you typed into journals, commitments, intentions, reflections, or
  trigger logs.
- Usage statistics, streaks, focus-mode history, or accountability-partner
  contact info.

Firebase Analytics' general default events (app open, screen view, in-app
purchase if applicable) are subject to Google's standard privacy practices:
<https://firebase.google.com/support/privacy>. We do not log custom events
that contain personal content from inside the app.

If you would prefer GoNull never report crashes, contact `hello@gonull.app`
and we will provide a build with Crashlytics removed, or add an in-app opt-out
in a future release if there is demand.

## Children's privacy

GoNull is not directed at children under 13. We do not knowingly collect data
from children. Because GoNull collects no data off-device, this protection is
inherent to the app's design.

## Your rights

Because all GoNull data is on your device:

- **Access:** open the app — every record is visible in the corresponding screen (Stats, Journal, Settings).
- **Deletion:** uninstall the app, or use the per-feature delete actions inside the app.
- **Portability:** we currently do not provide an export function. If you need one, contact `hello@gonull.app` and we will prioritize it.

If you live in a jurisdiction with GDPR, CCPA, or similar protections, you also
have the right to lodge a complaint with your local data-protection authority.

## Changes to this policy

We will update this document if our data practices change — for example, if we
add cloud sync, push notifications to accountability partners, or Crashlytics.
The "Last updated" date at the top will reflect the most recent change. If a
change materially expands what data we collect, we will surface a notice
in-app before the new version takes effect.

## Contact

The GoNull team
`hello@gonull.app`
`United States`
