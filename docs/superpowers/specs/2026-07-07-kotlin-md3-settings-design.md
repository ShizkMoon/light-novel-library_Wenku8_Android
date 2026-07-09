# Kotlin MD3 Settings Rewrite Design

## Goal

Partially rewrite the Android Studio app toward Kotlin and Material Design 3 by converting the Settings screen into the first Kotlin-owned, MD3-styled surface while preserving existing user-facing behavior and save-file compatibility.

## Scope

This iteration rewrites `ConfigFragment` and its layout only. It does not rewrite the private `:api` module, reader engine, novel detail screen, bookshelf synchronization, or save formats. The settings screen is the right first target because it is small enough to verify, already user-facing, contains obsolete `AsyncTask` code, and can establish reusable Kotlin and MD3 patterns without touching the most fragile reading and network flows.

## Current State

The app already uses a Material 3 theme parent in `app/src/main/res/values/theme.xml`, but the settings screen still uses an older XML list made from `RelativeLayout`, `CardView`, manual icons, and a Java `Fragment`. `ConfigFragment.java` owns UI binding, notice HTML parsing, language selection, cache wiping, navigation, E-ink mode persistence, update checking, and about-screen routing. The cache cleaning work is implemented with two `AsyncTask` classes.

## Design Direction

The new settings screen remains XML-based for compatibility with the current Java Activity and Fragment stack. It uses Material 3 components from the existing Material dependency:

- `MaterialCardView` for the notice and settings groups.
- `MaterialTextView` for labels and support text.
- `MaterialSwitch` for E-ink mode.
- Standard `MaterialAlertDialogBuilder` dialogs for language and cache actions.

The visual language must be quieter and more app-like than a marketing redesign: a light surface, grouped cards, clear row hierarchy, 56dp minimum tap targets, and existing blue/red brand colors mapped through the current theme. The page must not introduce Compose yet; Compose would add a second UI runtime before the app has Kotlin lifecycle patterns.

## Kotlin Architecture

Create `app/src/main/java/org/mewx/wenku8/fragment/ConfigFragment.kt` and remove the Java `ConfigFragment.java`. The Kotlin fragment must:

- Inflate the existing `R.layout.fragment_config`.
- Bind views in `onViewCreated`, not `onActivityCreated`.
- Use nullable-safe activity/context access.
- Keep intent destinations and dialog choices identical to the Java version.
- Replace `AsyncTask` with a fragment-owned `ExecutorService` plus main-thread `Handler`, so no coroutine dependency is required in the first pass. This still removes the deprecated Android API and keeps the Gradle change smaller.
- Cancel ongoing cache work in `onDestroyView`.

## Behavior Requirements

The rewrite must preserve these behaviors:

- Hide notice card when `Wenku8API.NoticeString` is empty.
- Render notice HTML text and route notice links to `GlobalConfig.blogPageUrl`.
- Language dialog keeps simplified/traditional options and restarts `MainActivity` after changing language.
- Cache dialog keeps fast and slow options.
- Fast cache cleanup removes orphan cover/cache files using the same bookshelf checks.
- Slow cache cleanup additionally scans saved novel XML for referenced images and deletes unreferenced image files.
- E-ink switch reads and writes `GlobalConfig.SettingItems.eink_mode`.
- Navigation drawer wallpaper, update check, and about actions keep their existing destinations.

## Error Handling

Cache cleanup must always dismiss its progress dialog. Success must show the existing "OK" toast. Cancellation must show the error code text as the Java implementation did. File deletion failures remain non-fatal and are logged.

## Testing And Verification

The primary verification is `./gradlew.bat assembleAlphaDebug testAlphaDebugUnitTest --console=plain` from `studio-android/LightNovelLibrary`. If Gradle wrapper download is unavailable, record that blocker and run static checks instead:

- `git diff --check`
- `rg -n "AsyncTask|ConfigFragment.java" app/src/main/java/org/mewx/wenku8/fragment`
- Manual source inspection that all old settings IDs are still bound or intentionally removed.

## Rollout

This is the first migration slice. After it lands, future slices can move pure logic into Kotlin repositories and only then consider Compose for isolated screens such as About or Search. The reader remains last because it has the highest regression risk.
