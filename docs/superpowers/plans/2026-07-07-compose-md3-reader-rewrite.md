# Compose MD3 Reader Rewrite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the legacy reader with a Kotlin-owned reader whose UI shell is native Compose Material 3 and whose pagination/rendering core is testable Kotlin.

**Architecture:** The rewrite is split into a pure Kotlin paging core, a compatibility data layer for Wenku8 XML and old read saves, and a Compose Material 3 activity shell. The first milestone keeps the old Java readers available while adding a new modern reader option that can load the same intent extras and save formats.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, AndroidX Activity Compose, JUnit4, existing Wenku8 XML parser and save-file APIs.

---

## File Structure

- Create `app/src/main/java/org/mewx/wenku8/reader/modern/model/ReaderModels.kt`: immutable document, block, cursor, line, page, and layout model types.
- Create `app/src/main/java/org/mewx/wenku8/reader/modern/paging/ModernReaderPaginator.kt`: deterministic pure Kotlin pagination engine.
- Create `app/src/main/java/org/mewx/wenku8/reader/modern/data/LegacyReaderContentMapper.kt`: mapper from `OldNovelContentParser.NovelContent` into modern reader blocks.
- Create `app/src/main/java/org/mewx/wenku8/reader/modern/activity/ModernReaderActivity.kt`: Compose activity entrypoint that accepts the old reader extras.
- Create `app/src/main/java/org/mewx/wenku8/reader/modern/ui/ModernReaderScreen.kt`: Compose Material 3 reader surface, overlay controls, and bottom settings sheet.
- Modify `app/build.gradle`: enable Compose and add Compose/Activity dependencies through the official Compose BOM.
- Modify `AndroidManifest.xml`: register `ModernReaderActivity`.
- Modify `NovelInfoActivity.java` and reader option strings: add a "new MD3 reader" launch option while preserving V1 and vertical readers.
- Create `app/src/test/java/org/mewx/wenku8/reader/modern/paging/ModernReaderPaginatorTest.kt`: red/green unit coverage for pagination.

## Task 1: Pure Kotlin Pagination Core

**Files:**
- Create: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/reader/modern/paging/ModernReaderPaginatorTest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/model/ReaderModels.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/paging/ModernReaderPaginator.kt`

- [ ] **Step 1: Write failing pagination tests**

Add tests for CJK indentation/wrapping, backward page lookup, and image page placeholders. Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.reader.modern.paging.ModernReaderPaginatorTest" --console=plain --stacktrace
```

Expected: compilation fails because the modern reader model and paginator do not exist yet.

- [ ] **Step 2: Implement immutable reader model types**

Add sealed blocks for text and image content, cursor/range types, and `ReaderLayoutSpec` with pixel dimensions and spacing values.

- [ ] **Step 3: Implement minimal forward pagination**

Implement `ModernReaderPaginator.pageFrom(cursor, layout)` with injected `ReaderTextMeasurer`, full-width two-space paragraph indentation, line wrapping, image placeholders, and stable end cursors.

- [ ] **Step 4: Implement backward lookup**

Implement `ModernReaderPaginator.pageBefore(cursor, layout)` by replaying pages from the document start until the page ending at or before the requested cursor is found. This is deliberately simple for milestone one and can be indexed later.

- [ ] **Step 5: Verify tests pass**

Run the same focused test command. Expected: all tests in `ModernReaderPaginatorTest` pass.

## Task 2: Compose Dependency Bootstrap

**Files:**
- Modify: `studio-android/LightNovelLibrary/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`

- [ ] **Step 1: Install SDK platform if required**

If using the latest Compose stable release requires Android SDK 37, install `platforms;android-37.0` and `build-tools;37.0.0` with `sdkmanager`.

- [ ] **Step 2: Enable Compose**

Set `buildFeatures { compose true }` in the Android app module and add Compose dependencies through the Compose BOM plus `androidx.activity:activity-compose`.

- [ ] **Step 3: Compile empty Compose usage**

Run `.\gradlew.bat :app:compileAlphaDebugKotlin --console=plain --stacktrace`. Expected: Kotlin compilation succeeds.

## Task 3: Compose MD3 Reader Shell

**Files:**
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/activity/ModernReaderActivity.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/ui/ModernReaderScreen.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Add Activity shell**

Create a `ComponentActivity` that reads `aid`, `cid`, `volume`, `from`, and `forcejump` extras and passes a loading state into Compose.

- [ ] **Step 2: Add MD3 screen**

Create a full-screen reader surface with edge-to-edge padding, center tap overlay toggle, top metadata, bottom progress/actions row, and a modal settings sheet scaffold.

- [ ] **Step 3: Compile Activity**

Run `.\gradlew.bat :app:compileAlphaDebugKotlin --console=plain --stacktrace`. Expected: Compose activity compiles.

## Task 4: Legacy Content Compatibility

**Files:**
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/data/LegacyReaderContentMapper.kt`
- Modify: `ModernReaderActivity.kt`

- [ ] **Step 1: Map old parser output**

Convert `OldNovelContentParser.NovelContentType.TEXT` to `ReaderBlock.Paragraph` and `IMAGE` to `ReaderBlock.Image`.

- [ ] **Step 2: Load local/cloud content with executor**

Use the existing local save path and `Wenku8API.getNovelContent()` behavior, but replace `AsyncTask` with a lifecycle-aware executor and main-thread state update.

- [ ] **Step 3: Display first page**

Use `ModernReaderPaginator` to render the first page in Compose with plain text and image placeholders.

## Task 5: Entry Point And Rollout

**Files:**
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/NovelInfoActivity.java`
- Modify: `studio-android/LightNovelLibrary/app/src/main/res/values/strings.xml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/res/values-zh-rHK/strings.xml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/res/values-zh-rTW/strings.xml`

- [ ] **Step 1: Add modern reader option**

Add "MD3 阅读器（新版）" as the first chapter option while keeping V1 and old vertical entries.

- [ ] **Step 2: Launch modern Activity**

Route option index 0 to `ModernReaderActivity`, index 1 to `Wenku8ReaderActivityV1`, and index 2 to `VerticalReaderActivity`.

- [ ] **Step 3: Verify old readers still launch**

Compile and inspect the dialog option mapping to confirm old readers are preserved.

## Task 6: Verification

**Files:**
- All modified app files.

- [ ] **Step 1: Run focused unit tests**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.reader.modern.paging.ModernReaderPaginatorTest" --console=plain --stacktrace
```

- [ ] **Step 2: Run full debug build and unit tests**

```powershell
.\gradlew.bat assembleAlphaDebug testAlphaDebugUnitTest --console=plain --stacktrace
```

- [ ] **Step 3: Run whitespace check**

```powershell
git diff --check
```

- [ ] **Step 4: Runtime preview**

Install the APK on `wenku8_md3_preview`, open a chapter through the chapter option dialog, and capture a screenshot of the modern reader shell.

## Self-Review

- Spec coverage: Kotlin core, Compose MD3 shell, legacy compatibility, rollout, and verification are covered.
- Placeholder scan: no TBD/TODO placeholders are present in this plan.
- Scope check: this is still a large rewrite, so the first milestone intentionally ships the modern reader beside the old one before replacing defaults.
