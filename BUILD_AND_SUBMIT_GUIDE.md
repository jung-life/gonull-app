# GoNull - Build and Submit Guide

Complete step-by-step instructions for building, testing, and submitting GoNull to Google Play Store.

---

## Prerequisites Checklist

Before starting, ensure you have:

- [ ] **Android Studio** (Hedgehog 2023.1.1 or newer)
- [ ] **JDK 17** installed
- [ ] **Android SDK** (API 34 installed)
- [ ] **Physical Android device** OR **Android Emulator** (API 26+)
- [ ] **Google Play Developer Account** ($25 one-time fee if you don't have one)
- [ ] **Google Account** for testing

---

## Part 1: Build the App in Android Studio

### Step 1.1: Open the Project

1. **Launch Android Studio**
2. Click **"Open"** (not "New Project")
3. Navigate to: `/Users/chai/Documents/projects/git/jung/gonull-app`
4. Click **"OK"**

**Wait for Gradle Sync** (this may take 2-5 minutes on first open):
- You'll see "Gradle Sync" in the bottom status bar
- Android Studio will download dependencies
- If prompted about Gradle version, click **"Update"**

---

### Step 1.2: Fix Missing Resources

Before building, we need to create some placeholder resources that are referenced but don't exist yet.

**Create Launcher Icons** (Temporary placeholders):

1. Right-click `app/src/main/res` → **New** → **Image Asset**
2. Keep defaults (green Android icon)
3. Click **"Next"** → **"Finish"**

This creates:
- `mipmap/ic_launcher.xml`
- `mipmap/ic_launcher_round.xml`

**Note**: You can replace these with custom icons later.

---

### Step 1.3: Sync and Build

1. **Sync Project with Gradle Files**:
   - Click the **"Sync Project with Gradle Files"** button (🐘 elephant icon in toolbar)
   - OR: `File` → `Sync Project with Gradle Files`

2. **Wait for Sync to Complete**:
   - Watch the "Build" tab at the bottom
   - Should say "BUILD SUCCESSFUL"

3. **Build the Debug APK**:
   - Click `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - Wait for build to complete (1-3 minutes)
   - You'll see a notification: "APK(s) generated successfully"

**Troubleshooting Common Build Errors**:

❌ **Error: "SDK location not found"**
```
Solution:
1. Create file: `gonull-app/local.properties`
2. Add: sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

❌ **Error: "Unresolved reference: first"**
```
Solution: This is a known issue with the Flow import.
In AppBlockerService.kt and UsageStatsPollingService.kt:
Change: .kotlinx.coroutines.flow.first()
To: kotlinx.coroutines.flow.first()
```

❌ **Error: "Cannot resolve symbol 'R'"**
```
Solution:
1. Build → Clean Project
2. Build → Rebuild Project
```

---

## Part 2: Test on Physical Device or Emulator

### Option A: Test on Physical Android Device (Recommended)

#### Step 2A.1: Enable Developer Options on Your Phone

1. **Open Settings** on your Android phone
2. Scroll to **"About phone"**
3. Find **"Build number"**
4. **Tap "Build number" 7 times** rapidly
5. You'll see: "You are now a developer!"

#### Step 2A.2: Enable USB Debugging

1. Go back to main **Settings**
2. Find **"Developer options"** (usually near bottom)
3. Toggle **"USB debugging"** ON
4. Toggle **"Install via USB"** ON (if available)

#### Step 2A.3: Connect Device and Run App

1. **Connect phone to computer via USB cable**
2. **On phone**: Allow USB debugging prompt → Click **"Always allow"** → **"OK"**

3. **In Android Studio**:
   - Click the **"Run"** button (green play ▶️ icon)
   - OR: `Run` → `Run 'app'`

4. **Select Device**:
   - In "Select Deployment Target" dialog
   - Your phone should appear (e.g., "Samsung SM-G991U")
   - Click it
   - Click **"OK"**

5. **Wait for Installation** (30-60 seconds):
   - Android Studio will install the APK
   - App will automatically launch on your phone

---

### Option B: Test on Android Emulator

#### Step 2B.1: Create Virtual Device

1. Click **"Device Manager"** button (phone icon in right sidebar)
2. Click **"Create Device"**
3. Select **"Phone"** → **"Pixel 6"** → **"Next"**
4. Select System Image:
   - **Release Name**: UpsideDownCake (API 34)
   - Click **"Download"** next to it (if not already downloaded)
   - Click **"Next"**
5. Verify Configuration:
   - AVD Name: `Pixel_6_API_34`
   - Click **"Finish"**

#### Step 2B.2: Launch Emulator and Run App

1. In **Device Manager**, click **▶️ Play** next to your emulator
2. Wait for emulator to boot (1-2 minutes on first launch)
3. Click the **"Run"** button (green play ▶️) in Android Studio
4. Select your emulator from the device list
5. Click **"OK"**

---

### Step 2.3: Test Core Functionality

Once the app launches, test these flows:

#### ✅ **Onboarding Flow**
1. App should open to **Welcome screen**
2. Tap **"Get Started"**
3. Read **Accessibility Disclosure** page
4. Tap **"I Understand - Continue"**
5. You're now on **Permissions page**

#### ✅ **Grant Permissions**

**Accessibility Service**:
1. Tap **"Accessibility Service"** card
2. You'll be taken to Android Settings
3. Find **"GoNull"** in the list
4. Toggle it **ON**
5. Accept the scary warning (this is normal)
6. Press **Back** button to return to app

**Display Over Other Apps**:
1. Tap **"Display Over Other Apps"** card
2. In Settings, find **"GoNull"**
3. Toggle **"Allow display over other apps"** ON
4. Press **Back** to return to app

**Success**: Both permission cards should show green with checkmarks

#### ✅ **Block an App**

1. You should now see the **Home screen**
2. Tap the **green + button** (bottom right)
3. Find and select a test app (e.g., Chrome, Gmail)
4. Tap **"Save (1)"** in top right
5. Press **Back**
6. You should see the app in "Blocked Apps" list

#### ✅ **Test Blocking**

1. **Exit GoNull** (press home button)
2. **Open the app you just blocked** (e.g., Chrome)
3. **Expected**: You should immediately see GoNull's blocking screen with:
   - Big "Ø" symbol
   - App name
   - "is blocked"
   - "Request Access" button
   - "Emergency Access" button (red)

#### ✅ **Test Emergency Unlock**

1. Tap **"Emergency Access"**
2. Read the warning
3. Tap the **"TAP HERE"** button **50 times**
4. Counter should increment: 1/50, 2/50, etc.
5. After 50 taps, button turns green
6. Tap **"Continue to App"**
7. You should be taken to the blocked app (unlocked for 15 min)

#### ✅ **Test Lock Mode (Optional)**

1. Open GoNull
2. Tap **Settings** (gear icon)
3. Scroll to **"Lock Mode"** section
4. Toggle it **ON**
5. Read the warning → Tap **"Enable Lock Mode"**
6. Grant Device Admin permission in system dialog
7. **Test**: Try to uninstall GoNull from your phone
   - Expected: You'll get an error "Cannot uninstall Device Admin app"

**To disable Lock Mode**:
- Settings → Security → Device Admins → GoNull → Deactivate

---

### Step 2.4: Check for Crashes

**Open Logcat** (bottom of Android Studio):
1. Click **"Logcat"** tab
2. Filter by package: Enter `app.gonull` in filter box
3. Look for RED lines (errors)

**Common Issues**:

❌ **"Permission denied for PACKAGE_USAGE_STATS"**
- **This is OK** - it's an optional permission
- App will work without it

❌ **"java.lang.IllegalStateException: Flow was not first()"**
- **Fix**: Update imports in service files (see build troubleshooting)

✅ **"No errors in Logcat after 5 minutes of testing"**
- App is stable and ready!

---

## Part 3: Create Google Play Listing

### Step 3.1: Create Google Play Console Account (If Needed)

1. Go to: [https://play.google.com/console](https://play.google.com/console)
2. Click **"Sign up"**
3. Pay **$25 one-time registration fee**
4. Accept Developer Distribution Agreement
5. Wait for account approval (usually instant, can take 48 hours)

---

### Step 3.2: Generate Signed Release APK

Before uploading to Play Store, you need a **signed release build**.

#### Create Keystore (First Time Only)

1. In Android Studio: `Build` → `Generate Signed Bundle / APK`
2. Select **"Android App Bundle"** → Click **"Next"**
3. Click **"Create new..."** (under Key store path)

4. **Fill in Keystore Details**:
   ```
   Key store path: /Users/chai/Documents/gonull-release.keystore
   Password: [CREATE A STRONG PASSWORD - SAVE THIS!]
   Confirm: [SAME PASSWORD]

   Alias: gonull-key
   Password: [CREATE A PASSWORD - SAVE THIS!]
   Confirm: [SAME PASSWORD]
   Validity (years): 25

   Certificate:
   First and Last Name: Your Name
   Organizational Unit: Independent Developer
   Organization: GoNull
   City or Locality: [Your City]
   State or Province: [Your State]
   Country Code: US
   ```

5. Click **"OK"**

6. ⚠️ **CRITICAL**: Save these passwords somewhere secure!
   - **If you lose them, you can NEVER update your app again**
   - Store in password manager or encrypted file

#### Generate Release Build

1. Still in the "Generate Signed Bundle" dialog:
   - Release type: **"release"**
   - Click **"Next"**

2. Select build variant:
   - Select **"release"** (should be pre-selected)
   - Click **"Create"**

3. **Wait for build** (2-5 minutes)

4. When complete, you'll see:
   ```
   locate: app/release/app-release.aab
   analyze: app-release.aab
   ```

5. Click **"locate"** to open Finder with your signed AAB file

**File location**:
```
/Users/chai/Documents/projects/git/jung/gonull-app/app/release/app-release.aab
```

---

### Step 3.3: Create App in Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Click **"Create app"**

**App Details**:
```
App name: GoNull
Default language: English (United States)
App or game: App
Free or paid: Free
```

**Declarations**:
- [x] I confirm this app complies with Google Play policies
- [x] I confirm this is not a copyrighted app

3. Click **"Create app"**

---

### Step 3.4: Complete Store Listing

You'll be taken to the Dashboard. Complete these sections:

#### **Store Presence → Main Store Listing**

**App Details**:
```
App name: GoNull

Short description (80 chars max):
Break free from phone addiction with commitment contracts and time delays

Full description (4000 chars max):
```

**COPY-PASTE THIS** (optimized for accessibility justification):

```
GoNull is an app blocker designed specifically for people with ADHD, executive function challenges, and digital addiction who struggle with impulse control around social media apps.

🧠 BUILT FOR NEURODIVERGENT USERS

Unlike typical app blockers that rely on willpower, GoNull creates external structure through commitment contracts. For users with ADHD or autism, simply "deciding" to avoid apps doesn't work—GoNull provides the friction barrier your brain needs.

⏱️ HOW IT WORKS

1. SELECT APPS: Choose which apps you want to block
2. TIME DELAY: When you try to open a blocked app, you can request access—but you must wait 30 minutes
3. COMMITMENT: During the wait, your impulse passes and you make better choices
4. EMERGENCY ACCESS: For genuine emergencies, unlock with 50 taps (creates deliberate friction)

🔒 FEATURES

• Time-delayed unlock requests (15-1440 minute delays)
• Lock Mode prevents uninstalling during active blocks
• Emergency friction unlock (50-tap requirement)
• Track your blocked attempts and progress
• No ads, no data collection, 100% private

♿ ACCESSIBILITY & PRIVACY

GoNull uses Android's Accessibility Service to detect when you open blocked apps. This is essential for users with impulse control challenges who need automatic intervention.

WE DETECT:
• When you open an app you chose to block
• The app's package name (e.g., "com.instagram.android")

WE DO NOT COLLECT:
• Screen content or passwords
• Browsing history or personal data
• Any information sent to external servers

All blocking happens locally on your device. Your data never leaves your phone.

🎯 WHO IS THIS FOR?

• People with ADHD struggling with phone addiction
• Users with executive dysfunction who need external structure
• Anyone who wants to break compulsive app checking
• People tired of app blockers they can easily bypass

💪 COMMITMENT OVER WILLPOWER

GoNull isn't about shame or restriction—it's about helping your executive function work the way you need it to. By creating friction and time delays, we give your prefrontal cortex time to catch up with your impulses.

🔐 SERIOUS COMMITMENT TOOLS

• Lock Mode: Enable Device Admin to prevent uninstalling during blocks
• Accountability logs: Track every time you use emergency unlock
• Future: Accountability partners and financial stakes for stronger commitment

📱 REQUIREMENTS

• Android 8.0 or higher
• Accessibility Service permission (required for blocking)
• Display over other apps permission (required for blocking screen)

🆓 100% FREE & OPEN

No subscriptions, no premium tiers, no data selling. GoNull is built for people who genuinely want to change their relationship with their phones.

---

If you have ADHD, autism, or impulse control challenges around your phone, GoNull provides the external structure traditional app blockers can't offer.

Download now and take back control.
```

**App Icon**:
1. Click **"Add app icon"**
2. Upload a 512x512 PNG icon
   - **Temporary**: Use any green/black icon with "Ø" symbol
   - You can use Figma, Canva, or [AppIconMaker](https://appiconmaker.co/) to create one

**Feature Graphic** (Required):
1. Click **"Add feature graphic"**
2. Upload 1024x500 PNG
   - Create in Canva with text: "GoNull - Break Free from Phone Addiction"
   - Background: Black (#0A0A0A)
   - Text: Green (#22C55E)

**Phone Screenshots** (Minimum 2 required):
1. Click **"Add phone screenshots"**
2. You need at least 2 screenshots (1080x1920 or similar)

**How to take screenshots**:
- **On physical device**: Take screenshots while testing (Power + Volume Down)
- **On emulator**: Click camera icon in emulator toolbar

**Required screenshots**:
1. **Onboarding/Accessibility Disclosure page** (shows we explain what we do)
2. **Home screen** with blocked apps
3. **Blocking screen** (when app is blocked)
4. **Emergency unlock dialog** (optional but good)

**App Category**:
```
Category: Productivity
Tags: Focus, Digital Wellbeing, ADHD
```

**Contact Details**:
```
Email: [YOUR EMAIL]
Website: [Optional - can leave blank]
Phone: [Optional]
```

**Privacy Policy** (REQUIRED):

You need a privacy policy URL. Quick option:

1. Use a GitHub Gist or simple webpage
2. **Content** (copy-paste this):

```markdown
# GoNull Privacy Policy

Last Updated: [Today's Date]

## Data Collection

GoNull does NOT collect, store, or transmit any personal data.

## Accessibility Service Usage

GoNull uses Android's Accessibility Service to detect when you open blocked apps. This is used solely for the purpose of displaying the blocking screen.

We detect:
- When you open an app you've chosen to block
- The package name of blocked apps

We DO NOT:
- Read screen content
- Collect passwords or personal information
- Track your browsing history
- Send any data to external servers

All app blocking happens locally on your device.

## Device Admin (Lock Mode)

When you enable Lock Mode, GoNull requests Device Admin privileges to prevent app uninstallation during active blocking sessions. This is optional and can be disabled at any time through Android Settings.

## Data Storage

All data (blocked apps list, unlock requests, usage logs) is stored locally on your device using Android's Room database. This data is not backed up to cloud services and is deleted if you uninstall the app.

## Third-Party Services

GoNull does not use any third-party analytics, advertising, or tracking services.

## Children's Privacy

GoNull does not knowingly collect information from children under 13.

## Changes to Privacy Policy

We will notify users of any changes to this privacy policy through app updates.

## Contact

For privacy questions: [YOUR EMAIL]
```

3. Host this on:
   - **GitHub Gist**: https://gist.github.com/ (free, public)
   - **Google Sites**: https://sites.google.com/ (free, easy)
   - **Your own domain** (if you have one)

4. Enter the URL in Privacy Policy field

---

#### **Store Settings → App Access**

```
(•) All or some functionality is restricted

Add instructions:
"GoNull requires Accessibility Service and Display Over Other Apps permissions to function. These are granted during onboarding."
```

---

#### **Store Settings → Ads**

```
( ) Yes, my app contains ads
(•) No, my app does not contain ads
```

---

#### **Store Settings → Content Rating**

1. Click **"Start questionnaire"**
2. Enter email address
3. Select category: **"Utility, Productivity, Communication, or Other"**

**Questionnaire**:
```
Does your app contain any violent content? NO
Does your app contain any sexual content? NO
... (answer NO to all)
```

4. Submit → You'll get **"EVERYONE"** rating

---

#### **Store Settings → Target Audience**

```
Age groups:
[x] 18 and over

Store presence:
[x] Yes, my app is designed for children
( ) No, my app is not designed for children
```

---

#### **Store Settings → News Apps**

```
( ) My app is a news app
(•) My app is not a news app
```

---

## Part 4: Submit to Play Store Early Access

### Step 4.1: Upload Your App Bundle

1. Go to **"Production"** in left sidebar
2. Click **"Create new release"**
3. Click **"Upload"** under "App bundles"
4. Select your file: `app-release.aab`
5. Wait for upload (30-60 seconds)

**Release name**: `1.0.0 (Phase 0 - Policy Validation)`

**Release notes**:
```
Initial release of GoNull - app blocker for ADHD and digital wellbeing.

Features:
• Time-delayed app unlock requests
• Accessibility Service for automatic blocking
• Emergency friction unlock
• Lock Mode (anti-uninstall protection)
• 100% local, privacy-first design
```

---

### Step 4.2: Complete Accessibility Justification

⚠️ **THIS IS THE MOST IMPORTANT PART**

Google will ask why you're using Accessibility Service. You must provide a detailed, compliant answer.

1. Scroll to **"Accessibility"** section
2. Click **"Manage"**
3. Click **"Add accessibility features"**

**Question**: "Why does your app use accessibility services?"

**YOUR ANSWER** (copy-paste this):

```
GoNull uses Accessibility Services specifically to assist users with ADHD, executive dysfunction, autism, and impulse control disorders in managing their phone usage.

HOW IT HELPS USERS WITH DISABILITIES:

1. ADHD & Executive Dysfunction:
   Users with ADHD experience impaired impulse control and executive function. When they see a social media app, they often open it compulsively before conscious decision-making can occur. GoNull's Accessibility Service detects these automatic app launches and intervenes with a blocking screen, giving the user's prefrontal cortex time to engage.

2. Autism & Routine Management:
   Autistic users benefit from external structure and predictable routines. GoNull provides this structure by enforcing pre-committed app usage rules, reducing decision fatigue and anxiety around self-regulation.

3. Impulse Control Disorders:
   For users diagnosed with impulse control disorders, willpower-based app blockers fail because they can be easily disabled in moments of impulsivity. GoNull's Accessibility Service creates automatic intervention that doesn't rely on in-the-moment decision-making.

WHAT WE DETECT:
• Window state changes (when an app is opened)
• Package names of apps the user has chosen to block

WHAT WE DO NOT ACCESS:
• Screen content or text
• User passwords or credentials
• Personal communications or messages
• Browsing history or web content

TECHNICAL IMPLEMENTATION:
We only monitor TYPE_WINDOW_STATE_CHANGED events and only respond when the opened app matches the user's pre-configured block list. All processing is local; no data is transmitted off-device.

ACCESSIBILITY SERVICE IS ESSENTIAL:
Alternative methods (UsageStatsManager polling) introduce 2+ second delays, during which users with ADHD have already engaged with the app and experienced dopamine reinforcement, rendering the intervention ineffective.

PRIVACY COMMITMENT:
All data remains on-device. We do not collect, store, or transmit any user information. Our open-source codebase is available for verification.

USER CONSENT:
During onboarding, we provide a comprehensive disclosure explaining exactly how Accessibility Services are used, what we detect, and what we don't collect. Users must explicitly acknowledge this before granting permission.

EVIDENCE OF NEED:
GoNull is designed based on clinical research on ADHD and impulse control, which shows that external barriers (not internal willpower) are most effective for behavior change in neurodivergent populations.

We are committed to using Accessibility Services exclusively for their intended purpose: assisting users with disabilities in accessing and controlling their device in ways their neurotype makes difficult.
```

4. Click **"Save"**

---

### Step 4.3: Submit for Review

1. Review all sections - they should all have green checkmarks
2. Click **"Review release"**
3. Review the summary
4. Click **"Start rollout to Production"**

**Alternative**: Start with **"Internal Testing"** or **"Closed Testing"** first
- Less risky for initial validation
- Click "Testing" in sidebar instead of "Production"
- Add yourself as a tester
- Upload AAB and test it yourself via Play Store before going public

---

### Step 4.4: What Happens Next

**Review Timeline**:
- **Internal/Closed Testing**: Usually approved in 1-24 hours
- **Production**: Usually reviewed in 1-7 days

**Possible Outcomes**:

#### ✅ **Approved**
- Your app will appear in Google Play Store
- You'll receive an email: "Your app is live"
- Proceed with Phase 1 development

#### ⚠️ **Approved with Warnings**
- App is approved but Google flags concerns
- Read carefully and make adjustments
- Document for future updates

#### ❌ **Rejected**
- Most likely reason: Accessibility Service policy violation
- Google will email specific reasons

**If rejected for Accessibility Service**:

1. **Don't panic** - we have a fallback
2. **Reply to Google**:
   - Provide additional clarification
   - Reference clinical research on ADHD and impulse control
   - Offer to add more disclosure in app

3. **If still rejected**:
   - Switch to `UsageStatsPollingService` in code
   - Update app description: "2-second detection creates intentional friction"
   - Resubmit as different blocking method

---

## Monitoring & Next Steps

### Check Review Status

1. Go to Play Console
2. Dashboard shows: **"In Review"** or **"Approved"**
3. Check email for updates

### If Approved - Next Actions

1. **Share with beta testers**:
   - Friends with ADHD
   - r/ADHD subreddit (ask for feedback)
   - r/nosurf community

2. **Monitor crash reports**:
   - Play Console → Quality → Android vitals
   - Fix any crashes immediately

3. **Collect feedback**:
   - Read user reviews
   - Create feedback form (Google Forms)

4. **Plan Phase 1**:
   - Implement persistent timers
   - Add stats screen
   - Prepare for accountability partner feature

---

## Troubleshooting Common Submission Issues

❌ **"Your app's target API level is too low"**
```
Solution: Already set to API 34 in build.gradle
If error persists, verify targetSdk = 34
```

❌ **"Missing privacy policy"**
```
Solution: Add privacy policy URL in Store Listing
```

❌ **"Content rating required"**
```
Solution: Complete Content Rating questionnaire
```

❌ **"App icon does not meet requirements"**
```
Solution: Must be 512x512 PNG with no transparency
```

❌ **"Feature graphic required"**
```
Solution: Upload 1024x500 PNG
```

❌ **"Accessibility Service not justified"**
```
Solution: Review and expand your accessibility justification
Include more clinical/research references
Emphasize disability assistance, not general productivity
```

---

## Emergency Contacts

**Google Play Support**:
- Play Console → Help → Contact Us
- Usually responds in 1-3 business days

**Community Support**:
- r/androiddev (Reddit)
- Stack Overflow (tag: google-play)

---

## Success Checklist

Phase 0 Complete When:

- [ ] App builds without errors
- [ ] Runs on physical device or emulator
- [ ] All core features tested (blocking, unlock, emergency)
- [ ] Signed release AAB generated
- [ ] Play Console account created
- [ ] Store listing completed
- [ ] Privacy policy hosted
- [ ] Accessibility justification written
- [ ] App submitted for review
- [ ] Monitoring review status

**Estimated Time**: 3-6 hours for first-time submission

---

## What to Do While Waiting for Approval

1. **Test more edge cases** on your device
2. **Prepare Phase 1 features** (persistent timers, stats)
3. **Create marketing materials** (website, social media)
4. **Research accountability partner** implementation (Supabase, Firebase)
5. **Document lessons learned** from Phase 0

---

## Final Notes

**This is Phase 0**: The goal is **policy validation**, not perfection.

- Don't worry about polish
- Focus on getting Google's feedback
- The app WILL evolve significantly in Phase 1-2

**Most Important**: Your **Accessibility Justification** is what determines approval. The technical implementation is solid—it's the policy explanation that matters.

**Good luck!** 🚀

---

**Questions or Issues?**
- Document them in `BUILD_ISSUES.md`
- Check Stack Overflow for build errors
- Ask in r/androiddev for Play Store policy questions
