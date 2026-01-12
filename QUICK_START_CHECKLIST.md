# GoNull - Quick Start Checklist

Use this checklist to track your progress through the build and submission process.

---

## ☑️ Part 1: Build the App (30-45 minutes)

### Setup
- [ ] Android Studio installed (Hedgehog 2023.1.1+)
- [ ] JDK 17 installed
- [ ] Project opened in Android Studio
- [ ] Gradle sync completed successfully

### Resources
- [ ] Launcher icons created (right-click res → New → Image Asset)
- [ ] No build errors after sync

### Build
- [ ] Debug APK built successfully (`Build` → `Build APK`)
- [ ] No red errors in Build output

**Common Issues Fixed**:
- [ ] SDK location found (check `local.properties`)
- [ ] Flow.first() import fixed if needed
- [ ] R class resolved

---

## ☑️ Part 2: Test the App (45-60 minutes)

### Device Setup
- [ ] **Physical device**: Developer options enabled (tap Build Number 7x)
- [ ] **Physical device**: USB debugging enabled
- [ ] **OR Emulator**: Pixel 6 API 34 created
- [ ] Device connected and recognized by Android Studio

### App Installation
- [ ] App installed on device/emulator (green ▶️ Run button)
- [ ] App launched successfully
- [ ] No immediate crashes

### Functional Testing

**Onboarding**:
- [ ] Welcome screen appears
- [ ] Accessibility disclosure page shows detailed explanation
- [ ] Permissions page displays two required permissions

**Permissions**:
- [ ] Accessibility Service granted (Settings → Accessibility → GoNull → ON)
- [ ] Display Over Other Apps granted (Settings → Apps → GoNull → Allow)
- [ ] Both permissions show green checkmarks

**Blocking**:
- [ ] Added at least one test app to block list (+ button → select app → Save)
- [ ] Opened blocked app from launcher
- [ ] Blocking screen appeared with "Ø" symbol
- [ ] "Request Access" button visible

**Emergency Unlock**:
- [ ] Tapped "Emergency Access" button
- [ ] Tapped "TAP HERE" button 50 times
- [ ] Counter incremented (1/50, 2/50, etc.)
- [ ] Successfully unlocked after 50 taps
- [ ] Granted access to blocked app

**Lock Mode** (Optional):
- [ ] Enabled Lock Mode in Settings
- [ ] Granted Device Admin permission
- [ ] Verified cannot uninstall app (expected behavior)
- [ ] Know how to disable: Settings → Security → Device Admins

### Stability Check
- [ ] No crashes during 10 minutes of testing
- [ ] Logcat shows no critical RED errors
- [ ] App survives phone sleep/wake cycle

---

## ☑️ Part 3: Create Play Listing (60-90 minutes)

### Google Account Setup
- [ ] Google Play Developer account created ($25 paid)
- [ ] Account approved (usually instant)
- [ ] Developer Distribution Agreement accepted

### Signed Release Build
- [ ] Keystore created at `/Users/chai/Documents/gonull-release.keystore`
- [ ] ⚠️ **CRITICAL**: Passwords saved in secure location
- [ ] Release AAB generated: `app/release/app-release.aab`
- [ ] AAB file located and ready for upload

### App Creation
- [ ] New app created in Play Console
- [ ] App name: "GoNull"
- [ ] Language: English (US)
- [ ] Free app selected

### Store Listing

**Text Content**:
- [ ] Short description (80 chars) written
- [ ] Full description (4000 chars) pasted (see guide)
- [ ] Description emphasizes ADHD/neurodivergent users
- [ ] Description explains Accessibility Service usage clearly

**Visual Assets**:
- [ ] App icon uploaded (512x512 PNG)
- [ ] Feature graphic uploaded (1024x500 PNG)
- [ ] At least 2 phone screenshots uploaded:
  - [ ] Onboarding/Accessibility disclosure
  - [ ] Home screen with blocked apps
  - [ ] (Optional) Blocking screen
  - [ ] (Optional) Emergency unlock dialog

**Categories & Metadata**:
- [ ] App category: Productivity
- [ ] Tags: Focus, Digital Wellbeing, ADHD
- [ ] Contact email entered
- [ ] Privacy policy URL entered

### Compliance Forms

**Store Settings**:
- [ ] App access: "Some functionality restricted" (permissions required)
- [ ] Ads declaration: "No ads"
- [ ] Content rating: Questionnaire completed → "EVERYONE"
- [ ] Target audience: "18 and over"
- [ ] News app: "Not a news app"

**Privacy Policy**:
- [ ] Privacy policy created (see template in guide)
- [ ] Hosted on GitHub Gist, Google Sites, or own domain
- [ ] URL added to Store Listing

---

## ☑️ Part 4: Submit to Play Store (30-45 minutes)

### Upload
- [ ] Navigated to Production (or Testing track)
- [ ] Created new release
- [ ] Uploaded `app-release.aab`
- [ ] Upload successful (green checkmark)

### Release Details
- [ ] Release name: "1.0.0 (Phase 0 - Policy Validation)"
- [ ] Release notes written (see guide template)

### Accessibility Justification
- [ ] Accessibility features section found
- [ ] **CRITICAL**: Detailed justification written explaining:
  - [ ] How it helps ADHD users
  - [ ] How it helps autistic users
  - [ ] How it helps impulse control disorders
  - [ ] What we detect vs. don't collect
  - [ ] Why Accessibility Service is essential (vs alternatives)
  - [ ] Privacy commitment
  - [ ] User consent process
- [ ] Justification saved

### Final Submission
- [ ] All sections have green checkmarks
- [ ] Release reviewed (clicked "Review release")
- [ ] Submitted (clicked "Start rollout to Production" or testing track)
- [ ] Confirmation email received

---

## 🎯 Post-Submission

### Monitoring
- [ ] Bookmark Play Console dashboard
- [ ] Check email for review updates (daily)
- [ ] Monitor review status in Play Console

### Expected Timeline
- [ ] Internal/Closed testing: 1-24 hours
- [ ] Production: 1-7 days

### While Waiting
- [ ] Continue testing on device
- [ ] Document any bugs found
- [ ] Plan Phase 1 features
- [ ] Research accountability partner implementation
- [ ] Prepare social media announcement (if approved)

---

## 📊 Success Metrics (Phase 0)

**Minimum Success** (Must Have):
- [ ] App submitted to Play Store
- [ ] No immediate rejection
- [ ] Accessibility justification accepted OR constructive feedback received

**Target Success** (Should Have):
- [ ] App approved within 7 days
- [ ] No policy violations
- [ ] Ready to onboard first beta users

**Stretch Success** (Nice to Have):
- [ ] Approved on first submission
- [ ] Positive early user feedback
- [ ] Featured in Play Store "New Apps"

---

## ⚠️ If Rejected

### Immediate Actions
- [ ] Read rejection email carefully
- [ ] Identify specific policy violation cited
- [ ] Review Google Play policy documentation
- [ ] Document rejection reason

### Response Strategy

**If Accessibility Service Issue**:
- [ ] Reply to Google with additional clarification
- [ ] Provide clinical research references (ADHD studies)
- [ ] Offer to enhance in-app disclosure
- [ ] If still rejected → switch to UsageStatsPollingService

**If Other Policy Issue**:
- [ ] Make required changes
- [ ] Update store listing
- [ ] Resubmit with explanation of changes

### Fallback Plan
- [ ] Code changes needed: Switch blocking strategy
- [ ] Store listing updates: Emphasize 2-second delay as feature
- [ ] Resubmission timeline: 24-48 hours

---

## 📞 Help & Resources

### Quick Links
- [ ] Play Console: https://play.google.com/console
- [ ] Build guide: `BUILD_AND_SUBMIT_GUIDE.md`
- [ ] Implementation strategy: `IMPLEMENTATION_STRATEGY.md`
- [ ] Phase 0 changes: `PHASE_0_CHANGES.md`

### Getting Help
- [ ] Check `BUILD_AND_SUBMIT_GUIDE.md` for detailed troubleshooting
- [ ] Search Stack Overflow (tag: android, google-play)
- [ ] r/androiddev subreddit for technical issues
- [ ] Play Console → Help → Contact Support for policy questions

---

## ✅ Complete!

When all checkboxes are checked:
- [ ] **Phase 0 is complete**
- [ ] **App is submitted and pending review**
- [ ] **You're ready for Phase 1 development**

**Estimated Total Time**: 4-6 hours for first-time submission

**Next Steps**: Wait for Google's review, monitor email, plan Phase 1 features.

**Congratulations on reaching this milestone!** 🎉
