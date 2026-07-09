# Kotlin MD3 Settings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the Settings screen into the first Kotlin-owned, MD3-styled app surface while preserving its existing behavior.

**Architecture:** Keep the existing XML Fragment stack, but replace `ConfigFragment.java` with `ConfigFragment.kt`. Enable AGP 9 built-in Kotlin support, use Material 3 XML widgets, and replace settings cache cleanup `AsyncTask` classes with fragment-owned executor work that reports back on the main thread.

**Tech Stack:** Android Gradle Plugin 9.0.1 built-in Kotlin, Java 17, Kotlin, Material Components 1.13.0, XML layouts, AndroidX Fragment/AppCompat.

---

### Task 1: Enable Kotlin Compilation

**Files:**
- Modify: `studio-android/LightNovelLibrary/gradle.properties`

- [ ] **Step 1: Enable AGP built-in Kotlin**

Change:

```properties
android.builtInKotlin=false
```

To:

```properties
android.builtInKotlin=true
```

- [ ] **Step 2: Verify the setting is present**

Run: `rg -n "android.builtInKotlin" studio-android/LightNovelLibrary/gradle.properties`

Expected: one line with `android.builtInKotlin=true`.

### Task 2: Rewrite Settings Layout With MD3 Components

**Files:**
- Modify: `studio-android/LightNovelLibrary/app/src/main/res/layout/fragment_config.xml`

- [ ] **Step 1: Replace the old RelativeLayout list**

Use a `ScrollView` containing a vertical `LinearLayout`, a notice `MaterialCardView`, and one grouped `MaterialCardView` for settings rows. Keep the existing IDs: `notice_layout`, `notice`, `btn_choose_language`, `btn_clear_cache`, `btn_navigation_drawer_wallpaper`, `eink_mode_config`, `switch_eink_mode`, `btn_check_update`, and `btn_about`.

- [ ] **Step 2: Preserve click targets**

Each row must be at least `56dp` high, clickable, focusable, and use `?attr/selectableItemBackground`.

- [ ] **Step 3: Preserve switch semantics**

The E-ink row must contain `com.google.android.material.materialswitch.MaterialSwitch` with ID `@+id/switch_eink_mode`.

### Task 3: Rewrite ConfigFragment In Kotlin

**Files:**
- Delete: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/fragment/ConfigFragment.java`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/fragment/ConfigFragment.kt`

- [ ] **Step 1: Create Kotlin fragment shell**

Use `Fragment(R.layout.fragment_config)` and bind views in `onViewCreated`.

- [ ] **Step 2: Port notice rendering**

Use `Html.fromHtml`, trim trailing whitespace, replace `URLSpan` with a `ClickableSpan`, and open `GlobalConfig.blogPageUrl`.

- [ ] **Step 3: Port settings actions**

Language, cache cleanup, wallpaper, E-ink, update check, and About actions must keep their current destinations and dialogs.

- [ ] **Step 4: Replace AsyncTask**

Use `Executors.newSingleThreadExecutor()`, `Handler(Looper.getMainLooper())`, `Future`, and `AtomicBoolean` cancellation. Always dismiss `ProgressDialogHelper` on completion.

### Task 4: Static Verification

**Files:**
- Inspect all modified files.

- [ ] **Step 1: Check no old Java fragment remains**

Run: `Test-Path studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/fragment/ConfigFragment.java`

Expected: `False`.

- [ ] **Step 2: Check no AsyncTask remains in Settings fragment**

Run: `rg -n "AsyncTask|onActivityCreated" studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/fragment/ConfigFragment.kt`

Expected: no output.

- [ ] **Step 3: Check XML IDs**

Run: `rg -n "notice_layout|notice|btn_choose_language|btn_clear_cache|btn_navigation_drawer_wallpaper|eink_mode_config|switch_eink_mode|btn_check_update|btn_about" studio-android/LightNovelLibrary/app/src/main/res/layout/fragment_config.xml`

Expected: all IDs are present.

### Task 5: Build Verification

**Files:**
- No source edits.

- [ ] **Step 1: Run Gradle verification**

Run from `studio-android/LightNovelLibrary`:

```powershell
./gradlew.bat assembleAlphaDebug testAlphaDebugUnitTest --console=plain
```

Expected: build and unit tests pass. If wrapper download fails because `services.gradle.org` is unreachable, record the exact error and run `git diff --check` as the fallback static verification.
