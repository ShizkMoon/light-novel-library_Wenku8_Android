# MainActivity Kotlin Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the last production Java file from the active Android app by migrating `MainActivity` to Kotlin while preserving launcher identity and behavior.

**Architecture:** Keep this slice conservative: preserve the existing Activity responsibilities and public API surface, then leave deeper startup/save-migration extraction for a later coroutine/lifecycle refactor. The verification signal is a Kotlin ownership contract plus the full alpha debug build and unit test suite.

**Tech Stack:** AndroidX Activity/AppCompat, Kotlin, Material Components, existing Gradle Android project.

---

### Task 1: Add Kotlin Ownership Contract

**Files:**
- Modify: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/activity/ActivityMigrationContractTest.kt`

- [x] Add `assertKotlinClass(MainActivity::class.java)` to the existing activity ownership test.
- [x] Run `.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.activity.ActivityMigrationContractTest" --console=plain --stacktrace --no-parallel`.
- [x] Expected RED: the test fails because `MainActivity` is still compiled from Java.

### Task 2: Migrate MainActivity

**Files:**
- Delete: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/MainActivity.java`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/MainActivity.kt`

- [x] Convert the class to Kotlin with the same package, class name, manifest identity, enum names, getter/setter names, launcher behavior, permission request codes, save migration flow, toolbar search action, fragment replacement, and double-back-exit behavior.
- [x] Keep static Java-callable behavior through `companion object` where needed.
- [x] Run the activity ownership contract again and fix compile errors only inside the migration slice.

### Task 3: Refresh Ledger And Verify

**Files:**
- Modify: `docs/kotlin-md3-module-migration-ledger.md`

- [x] Update active production inventory to `app/src/main/java java=0 kt=92`.
- [x] Update activity package inventory to `activity java=0 kt=14`.
- [x] Run `.\gradlew.bat assembleAlphaDebug testAlphaDebugUnitTest --console=plain --stacktrace --no-parallel`.
- [x] Run `git diff --check`.
