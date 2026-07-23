# Wenku8 Phase 6 Reader Consolidation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver one ViewModel-owned, single-activity Compose Material 3 reader with complete paginated and continuous behavior, preserve every approved legacy launch/progress/settings boundary, and retire the V1, vertical, temporary Modern Reader UI, and Phase 0 `LegacyReaderPageDeck` only after current parity evidence passes.

**Architecture:** Phase 5's `ReaderOpenRequest` remains the only novel-to-reader feature contract. `:feature:reader` owns immutable route state, the independently authored pagination/continuous engines, reader interaction policy, and Material 3 UI; `:core:domain` owns reader repository interfaces; `:core:data` combines Phase 2 provider reads with Phase 3 catalog, chapter, image, progress, settings, and legacy-projection stores. `:app` owns Navigation Compose registration, system integration, and old class-name/Intent trampolines, while no feature imports provider, Room, files, legacy DTOs, or app composition classes.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, Lifecycle ViewModel and `StateFlow`, structured Kotlin coroutines, Room, DataStore, Phase 3 journaled legacy projections, AndroidX WindowManager, AndroidX Activity predictive back, AndroidX Test, Compose UI Test, JUnit4, deterministic screenshot tooling, PowerShell, Gradle, and ADB.

---

## Scope Boundary

This phase owns the reader route and only the reader-adjacent integration required to enter, operate, restore, and leave it. It implements paginated and continuous reading, catalog selection, chapter images, reader settings, custom font/background import and reset, text search, direct progress seek, progress persistence, reader accessibility, adaptive panes, input shortcuts, system bars, legacy launches, and reader-only retirement.

This phase does not implement Phase 7 account/application-settings/wallpaper/about pages, does not replace the Phase 7 image viewer, and does not perform Phase 8's broad Activity/Fragment/XML/API-bridge removal. The existing image-viewer entry remains a typed callback with the frozen legacy `path` fallback until Phase 7 replaces that destination. The three old reader Activity class names remain non-exported compatibility trampolines; removing their class identities is a Phase 8 compatibility-window decision.

Wild remains behavior-only evidence. No task reads, copies, translates, or mechanically rewrites Wild source, selectors, fixtures, tests, comments, messages, or distinctive control flow. The final pagination, gestures, transition, parser adaptation, and tests are independently authored from approved project behavior evidence and synthetic fixtures.

## Execution Root And Command Rules

Run Android and Gradle commands from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android\studio-android\LightNovelLibrary'
```

Run repository tools and documentation checks from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android'
```

Every Gradle verification command in this plan uses `--console=plain --stacktrace --no-parallel`. A focused RED is valid only when it fails for the missing behavior named by that step; dependency resolution, authorization, emulator, daemon, syntax, or unrelated compilation failures are not acceptable RED evidence. Every task ends with its affected suite, `git diff --check`, and one isolated commit.

Never put a username, password, Cookie, captcha, account response, private endpoint, signing material, protected overlay value, or copyrighted live chapter body in a command, source file, fixture, screenshot, report, or commit. Phase 6 requires no live account input. Deterministic tests use only synthetic fixture content.

## Preconditions And Stop Conditions

- [ ] Execute in an isolated worktree created with `superpowers:using-git-worktrees` from the independently reviewed Phase 5 completion commit. `git status --short` must be empty before Task 1.
- [ ] Root `phase0Gate`, `:verification-tools:phase1Gate`, `:verification-tools:phase2Gate`, `:verification-tools:phase3Gate`, `:app:phase4LibraryGate`, and `:app:phase5NovelGate` pass for `-Pwenku8Provider=public`.
- [ ] Phase 0 old-signed/minified Intent/Serializable fixtures, UI-owner/action ledger, source/asset provenance ledger, packaged-license gate, and independently authored legacy reader behavior deck are present and current.
- [ ] Phase 0 has resolved the original SlidingLayout family: production contains either reviewed licensed provenance or the independently authored `org.mewx.wenku8.reader.legacydeck.LegacyReaderPageDeck`; an `UNKNOWN`, missing, or incompatible packaged license stops every retirement/distributable step.
- [ ] Phase 3 `ReaderProgressEntity`, `ReaderProgressDao`, `LegacyReaderProgressCodec`, `ReaderProgressMigrationParticipant`, journaled writer barrier, dual projection to `read_saves.wk8` and `read_saves_v1.wk8`, durable chapter/image paths, and rollback harness pass before reader code writes progress.
- [ ] Phase 5 owns `org.mewx.wenku8.core.model.novel.ReaderOpenRequest`, `ReaderSourceMode`, `NovelEntryPoints.openReader`, detail/catalog/download behavior, and `org.mewx.wenku8.navigation.ReaderCompatibilityLauncher`. Phase 6 reuses these exact identities and does not add a second feature-to-reader request type.
- [ ] The public/private provider graph remains substitutable at the typed repository boundary. `:feature:reader` imports only `:core:model`, `:core:domain`, and `:core:designsystem`; it never imports `:api-contract`, logical `:api`, `:core:data`, `:core:storage`, or app packages.
- [ ] Deterministic fixture work may proceed without a live scope. Any live observation, live account/content request, public endpoint publication, or distributable public-provider artifact stops unless the exact current site/content/channel authorization rows are `ACCEPTED`.
- [ ] A migration/progress task stops if canonical and legacy projections disagree, an interrupted journal cannot reconcile, a non-secret legacy artifact is deleted, or an old-signed rollback build cannot read the projected result.
- [ ] Reader entry routing does not become the release default until state, behavior, accessibility, adaptive, screenshot, process-death, old-signed, rollback-read, license, and independent-review gates all pass at one source commit.
- [ ] `Wenku8ReaderActivityV1`, `VerticalReaderActivity`, `ModernReaderActivity`, their page helpers/resources, and `LegacyReaderPageDeck` are not deleted or reduced to trampolines before Task 19's retirement preflight passes. A stale report, missing hash, unapproved baseline, or indirect test blocks retirement.

## Phase Exit Conditions

Phase 6 is complete only when all conditions below are true at the same reviewed commit:

- One `ReaderViewModel` owns route state, catalog/chapter/image loading, cancellation, reflow, navigation, settings, search, progress, restoration, and bounded one-time effects through structured coroutines.
- Paginated and continuous modes pass local/cache-first, remote-with-cache, offline, stale, empty, recoverable failure, missing argument, first/last chapter, image pending/cached/broken, settings, reflow, rotation, resize, fold, and process-death journeys.
- Ordered text/images, current-chapter catalog focus, page/progress seek, find-in-chapter, previous/next page or viewport, previous/next chapter, completion clearing, and visible boundary feedback match the approved reader parity contract.
- Day, night, and e-ink are independent reader appearances; custom font/background import/reset, typography, line height, paragraph spacing, margins, optional volume keys, tap zones, gestures, overlap page transition, keyboard, DPAD, TalkBack, and Switch Access all have visible or semantic alternatives.
- Reader chrome, dialogs, sheets, catalog pane, system bars, cutouts, IME, edge-to-edge, predictive back, compact height, physical hinge regions, and Sheet-to-pane resize behavior pass focused tests.
- Phase 5's `ReaderOpenRequest` opens the in-host reader route. All old reader class names decode only through `LegacyIntentCodec`, forward once, preserve receiver-specific defaults, and finish without owning a product page.
- Old-signed/minified-to-new-minified reader Intents and `VolumeList`/`ChapterInfo` payloads pass on API 23/32 untyped and API 33+ typed access. Rollback with canonical access disabled reads exact projected progress/settings bytes.
- `Wenku8ReaderActivityV1` and `VerticalReaderActivity` have no user-selectable entry. The temporary Activity-owned Modern Reader, reader-only XML/menu page resources, Phase 0 `LegacyReaderPageDeck`, and their helper code/bytecode are absent after the retirement gate; the three frozen class-name trampolines remain.
- The authoritative UI manifest contains approved, hash-bound reader cases for every mandatory mode/appearance/font/catalog/image/boundary/hinge combination. Phone, tablet, landscape, expanded, light/night/e-ink, Simplified/Traditional/Hong Kong fallback, and font scale 2.0 pass without clipping, overlap, or hinge crossing.
- Repository/use-case/ViewModel line coverage is at least 80% and branch coverage at least 70%; overall production logic remains at least 70%/60%. Lint has zero errors and no new unexplained warnings.
- Public debug, fixture journeys, architecture, compatibility, license/SBOM/notice, sensitive-log, outbound-egress, and Phase 0-6 aggregate gates pass. Any release variant still fails closed when its current authorization/channel scope is not accepted.
- Independent architecture/data-compatibility, Compose/accessibility/visual, and release/licensing reviewers report no open Critical or Important finding.

## Exact File Structure

### Contract, Evidence, And Verification

- Create `docs/verification/phase-6-reader-contract.yaml`: stable reader behavior, route, mode, progress, appearance, input, adaptive, accessibility, retirement, and evidence IDs.
- Create `docs/verification/phase-6-reader-retirement.yaml` in Task 19 from current machine-generated hashes and commits; never check in sentinel or fabricated evidence.
- Modify `docs/verification/ui-golden-manifest.yaml`: authoritative Phase 6 screenshot rows with real fixture/baseline hashes and approval commit.
- Modify `docs/verification/modernization-matrix.yaml`: requirement-to-test/report/hash bindings for Phase 6.
- Modify `docs/verification/ui-owner-action-ledger.yaml`: A08/A09 retired user entries and A10 compatibility trampoline/new route ownership only after Task 19.
- Modify `docs/licenses/source-asset-provenance.yaml`, root `NOTICE`, `SOURCE_OFFER.md`, and packaged `app/src/main/res/raw/notice.txt` only when reader-only source/assets are removed and deterministic generators change their hashes.
- Create `tools/verify-phase6-reader-contract.ps1`: preflight contract/source/signature verifier.
- Create `tools/run-phase6-reader-journey.ps1`: emulator-only multi-stage local/remote/offline/rotation/process-death/rollback evidence harness.
- Reuse `studio-android/LightNovelLibrary/tools/verification/run-androidx-ui-goldens.ps1`: the sole deterministic device/configuration screenshot and semantics extraction harness.
- Create `tools/verify-phase6-reader-retirement.ps1`: fail-closed preflight and post-removal owner/source/resource/bytecode verifier.
- Create `verification-tools/src/main/kotlin/org/mewx/wenku8/verification/reader/Phase6ReaderEvidenceVerifier.kt` and its focused unit test.

### Core Model, Domain, Storage, And Data

- Modify `core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/ReaderPreferences.kt` and `RouteRolloutFlags.kt`: reader mode/input/transition settings and the reader rollout flag.
- Create `core/model/src/main/kotlin/org/mewx/wenku8/core/model/reader/ReaderModels.kt`: cursor, content freshness, image state, appearance, and completion values.
- Retain `core/model/src/main/kotlin/org/mewx/wenku8/core/model/novel/ReaderOpenRequest.kt` unchanged as the Phase 5 feature entry contract.
- Create `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/reader/ReaderRepository.kt`, `ReaderProgressRepository.kt`, and `ReaderAssetRepository.kt`.
- Modify `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/settings/SettingsMutation.kt`: typed reader mode/input/transition mutations.
- Modify `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryDaos.kt`: observable/flush-safe progress operations without changing the Phase 3 schema identity.
- Modify `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/ReaderProgressMigrationParticipant.kt` and `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyReaderProgressCodec.kt`: reuse exact Phase 3 journal/projection semantics for new reader writes and completion clears.
- Modify `core/storage/src/main/java/org/mewx/wenku8/core/storage/settings/SettingsPreferencesCodec.kt` and `LegacySettingsAdapter.kt`: canonical reader settings plus exact legacy-compatible projections.
- Create `core/storage/src/main/java/org/mewx/wenku8/core/storage/reader/ReaderAssetStore.kt` and `AndroidReaderAssetStore.kt`: bounded validation and atomic copy to retained custom paths.
- Create `core/data/src/main/java/org/mewx/wenku8/core/data/reader/DefaultReaderRepository.kt`, `ReaderCatalogMapper.kt`, `ReaderChapterMapper.kt`, `DefaultReaderProgressRepository.kt`, and `DefaultReaderAssetRepository.kt`.
- Add focused tests under `core/model/src/test/kotlin/org/mewx/wenku8/core/model/reader/`, `core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/reader/`, `core/storage/src/test/java/org/mewx/wenku8/core/storage/reader/`, and `core/data/src/test/java/org/mewx/wenku8/core/data/reader/`.

### Reader Feature

- Modify `feature/reader/build.gradle`: only model/domain/design-system/lifecycle/Compose/test dependencies allowed by the index.
- Create `feature/reader/src/main/java/org/mewx/wenku8/feature/reader/navigation/ReaderEntryPoints.kt`.
- Create `feature/reader/src/main/java/org/mewx/wenku8/feature/reader/model/ReaderUiState.kt`, `ReaderEvent.kt`, `ReaderEffect.kt`, and `ReaderRestorationState.kt`.
- Create `feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderLayoutSpec.kt`, `ReaderTextMeasurer.kt`, `ReaderPaginator.kt`, `ReaderPageTransition.kt`, `ContinuousCursorMapper.kt`, `ReaderProgressScale.kt`, and `ReaderSearchIndex.kt`.
- Create `feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderViewModel.kt`, `ReaderStateReducer.kt`, `ReaderProgressWriter.kt`, and `ReaderViewModelFactory.kt`.
- Create `feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderRoute.kt`, `ReaderScreen.kt`, `PaginatedReader.kt`, `ContinuousReader.kt`, `ReaderContentBlock.kt`, `ReaderChrome.kt`, `ReaderCatalog.kt`, `ReaderSettings.kt`, `ReaderSearch.kt`, `ReaderProgressSeek.kt`, `ReaderAdaptiveLayout.kt`, `ReaderAppearance.kt`, `ReaderInput.kt`, and `ReaderSemantics.kt`.
- Create reader strings in `feature/reader/src/main/res/values/strings.xml`, `values-zh-rTW/strings.xml`, and `values-zh-rHK/strings.xml`.
- Add pure/state tests under `feature/reader/src/test/java/org/mewx/wenku8/feature/reader/` and Compose/device tests under `feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/`.

### App Integration And Compatibility

- Modify `app/src/main/java/org/mewx/wenku8/navigation/AppRoute.kt`, `Wenku8NavHost.kt`, and `ReaderCompatibilityLauncher.kt`: encode/decode Phase 5 `ReaderOpenRequest` primitives and open the in-host reader.
- Create `app/src/main/java/org/mewx/wenku8/navigation/ReaderRouteCodec.kt`, `ReaderRouteFactory.kt`, and `ReaderSystemUiController.kt`.
- Modify `app/src/main/java/org/mewx/wenku8/di/AppContainer.kt`, `DefaultAppContainer.kt`, and `RouteViewModelFactory.kt`: inject the three reader repositories and factory without exposing the container to composables/ViewModels.
- Modify `app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt` and create `LegacyReaderEntryMapper.kt`: receiver-specific old Intent/Serializable decode and conversion into the unchanged `ReaderOpenRequest`.
- Create `app/src/main/java/org/mewx/wenku8/compat/ReaderCompatibilityTrampolineActivity.kt`.
- Replace the product-page bodies of `app/src/main/java/org/mewx/wenku8/activity/VerticalReaderActivity.kt`, `app/src/main/java/org/mewx/wenku8/reader/activity/Wenku8ReaderActivityV1.kt`, and `app/src/main/java/org/mewx/wenku8/reader/modern/activity/ModernReaderActivity.kt` with thin subclasses of the compatibility trampoline only after Task 19 preflight.
- Modify `app/src/main/java/org/mewx/wenku8/activity/Wenku8ShellActivity.kt` and `app/src/main/AndroidManifest.xml`: one host, system UI delegation, key delegation, and retained non-exported trampoline identities.
- Add app compatibility/navigation tests under `app/src/test/java/org/mewx/wenku8/compat/`, `app/src/androidTest/java/org/mewx/wenku8/compat/`, and `app/src/androidTest/java/org/mewx/wenku8/navigation/`.

### Reader-Only Retirement Targets

Task 19 may delete these exact production directories/resources only after its preflight succeeds:

- `app/src/main/java/org/mewx/wenku8/reader/legacydeck/`.
- `app/src/main/java/org/mewx/wenku8/reader/loader/`.
- `app/src/main/java/org/mewx/wenku8/reader/setting/`.
- `app/src/main/java/org/mewx/wenku8/reader/view/`.
- `app/src/main/java/org/mewx/wenku8/reader/modern/catalog/`, `data/`, `image/`, `launch/`, `layout/`, `model/`, `paging/`, `progress/`, `settings/`, and `ui/`.
- The old implementation helpers under `app/src/main/java/org/mewx/wenku8/reader/modern/activity/` except the rewritten `ModernReaderActivity.kt` trampoline.
- `app/src/main/res/layout/layout_reader_swipe_temp.xml`, `layout_reader_swipe_page.xml`, and `layout_vertical_reader_temp.xml`.
- `app/src/main/res/menu/menu_reader_v1.xml`.

Task 19 does not delete shared custom font/background files, chapter/image caches, frozen Serializable DTOs, old save files, or Phase 7's image-viewer compatibility owner.

## Task Dependency Graph

```text
1 -> 2 -> 3
3 -> 4 -> 9
3 -> 5 -> 10
2,3 -> 6 -> 10
2 -> 7 -> 8 -> 9
9 -> 10 -> 11 -> 12 -> 13 -> 14
10,14 -> 15 -> 16
11,12,13,14,15,16 -> 17 -> 18 -> 19 -> 20
```

Tasks 4, 5, 6, and 7 may be implemented in parallel only in separate worktrees after Task 3 is merged. Their commits must be merged before Task 9. No worker edits another active task's files.

## Task 1: Freeze The Phase 6 Reader Contract And Build Boundary

**Depends on:** every precondition above.

**Files:**
- Create: `docs/verification/phase-6-reader-contract.yaml`
- Create: `tools/verify-phase6-reader-contract.ps1`
- Modify: `studio-android/LightNovelLibrary/feature/reader/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/reader/Phase6ContractManifestTest.kt`

- [ ] **Step 1: Add a failing contract verifier**

Create `tools/verify-phase6-reader-contract.ps1` with this complete preflight:

```powershell
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$contract = Join-Path $repo 'docs\verification\phase-6-reader-contract.yaml'
$index = Join-Path $repo 'docs\superpowers\plans\2026-07-10-wenku8-modernization-plan-index.md'

if (-not (Test-Path -LiteralPath $contract -PathType Leaf)) {
    throw 'PHASE6-CONTRACT-MISSING: docs/verification/phase-6-reader-contract.yaml'
}

$text = Get-Content -Raw -LiteralPath $contract
$required = @(
    'schema: wenku8-phase6-reader-contract/v1',
    'entry_contract: org.mewx.wenku8.core.model.novel.ReaderOpenRequest',
    'paginated', 'continuous', 'overlap', 'find_in_chapter',
    'local_cache_first', 'remote_with_cache', 'offline_content',
    'legacy_pixel_progress', 'legacy_v1_progress', 'completion_clear',
    'day', 'night', 'eink', 'custom_font', 'custom_background',
    'tap_zones', 'volume_keys_opt_in', 'keyboard_dpad', 'talkback_switch_access',
    'catalog_sheet', 'catalog_pane', 'separating_hinge', 'occluding_hinge',
    'predictive_back_api36', 'process_death', 'old_signed_minified',
    'legacy_reader_page_deck_retired_after_gate'
)
foreach ($needle in $required) {
    if (-not $text.Contains($needle)) { throw "PHASE6-CONTRACT-INCOMPLETE: $needle" }
}

$forbidden = @('T' + 'BD', 'T' + 'ODO', 'fill' + ' later', 'api-contract.ReaderOpenRequest')
foreach ($needle in $forbidden) {
    if ($text.Contains($needle)) { throw "PHASE6-CONTRACT-FORBIDDEN: $needle" }
}

$indexText = Get-Content -Raw -LiteralPath $index
if (-not $indexText.Contains('| `:feature:reader` | `feature/reader/` |')) {
    throw 'PHASE6-CONTRACT: reader module differs from plan index'
}

Write-Host 'PHASE6-CONTRACT-PASS'
```

- [ ] **Step 2: Run the verifier and observe the intended RED**

Run from the repository root:

```powershell
& .\tools\verify-phase6-reader-contract.ps1
```

Expected: terminating error `PHASE6-CONTRACT-MISSING: docs/verification/phase-6-reader-contract.yaml`. Stop if it fails for PowerShell syntax or path resolution.

- [ ] **Step 3: Add the complete reader behavior contract**

Create the manifest exactly with project-owned stable behavior IDs:

```yaml
schema: wenku8-phase6-reader-contract/v1
phase: 6
entry_contract: org.mewx.wenku8.core.model.novel.ReaderOpenRequest
model_owner: org.mewx.wenku8.core.model
modes: [paginated, continuous]
load_policies: [local_cache_first, remote_with_cache, offline_content, stale_content]
content: [ordered_text, ordered_images, semantic_breaks, empty, recoverable_error]
navigation:
  - previous_page_or_viewport
  - next_page_or_viewport
  - previous_chapter
  - next_chapter
  - first_chapter_boundary
  - last_chapter_boundary
  - catalog_current_focus
  - direct_progress_seek
  - find_in_chapter
progress:
  - canonical_cursor
  - legacy_pixel_progress
  - legacy_v1_progress
  - completion_clear
  - process_death
  - rollback_projection
appearance: [day, night, eink, custom_font, custom_background]
typography: [font_size, line_height, paragraph_spacing, page_margin]
input: [visible_controls, tap_zones, swipe, volume_keys_opt_in, keyboard_dpad, talkback_switch_access]
page_transition: overlap
images: [pending, cached, broken, retry, open_viewer]
adaptive: [catalog_sheet, catalog_pane, separating_hinge, occluding_hinge, resize_remap]
system: [edge_to_edge, display_cutout, gesture_navigation, three_button_navigation, predictive_back_api36]
compatibility: [old_signed_minified, api23_untyped, api32_untyped, api33_typed]
retirement:
  - v1_user_entry
  - vertical_user_entry
  - temporary_modern_activity_page
  - legacy_reader_page_deck_retired_after_gate
deferred: [phase7_image_viewer_page, phase7_application_settings_page, phase8_trampoline_identity_removal]
```

- [ ] **Step 4: Lock the reader module dependency direction**

Update `feature/reader/build.gradle` so its production dependencies are exactly the allowed feature graph plus AndroidX UI/runtime libraries already pinned by Phase 1:

```groovy
dependencies {
    implementation project(':core:model')
    implementation project(':core:domain')
    implementation project(':core:designsystem')
    implementation libs.androidx.lifecycle.runtime.compose
    implementation libs.androidx.lifecycle.viewmodel.compose
    implementation platform(libs.androidx.compose.bom)
    implementation libs.androidx.compose.ui
    implementation libs.androidx.compose.foundation
    implementation libs.androidx.compose.material3
    implementation libs.kotlinx.coroutines.core

    testImplementation libs.junit
    testImplementation libs.kotlinx.coroutines.test
    androidTestImplementation platform(libs.androidx.compose.bom)
    androidTestImplementation libs.androidx.compose.ui.test.junit4
    androidTestImplementation libs.androidx.test.ext.junit.ktx
}
```

Do not add `:api-contract`, logical `:api`, `:api-public`, `:core:data`, `:core:storage`, OkHttp, Jsoup, Room, DataStore, or `:app`. Add `implementation project(':feature:reader')` to `app/build.gradle` once and retain Phase 5's `:feature:novel` dependency.

- [ ] **Step 5: Add and run the manifest/dependency test**

`Phase6ContractManifestTest` parses YAML as structured data, asserts the exact arrays above, inspects `:feature:reader` production dependencies, and rejects duplicate `ReaderOpenRequest` declarations outside `core/model/src/main/kotlin/org/mewx/wenku8/core/model/novel/ReaderOpenRequest.kt`.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.reader.Phase6ContractManifestTest" :feature:reader:dependencies -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: `PHASE6-CONTRACT-PASS` appears in the test report; the reader production graph contains only the allowed projects and libraries.

- [ ] **Step 6: Run the repository verifier and commit**

Run from the repository root:

```powershell
& .\tools\verify-phase6-reader-contract.ps1
git add docs/verification/phase-6-reader-contract.yaml tools/verify-phase6-reader-contract.ps1 studio-android/LightNovelLibrary/feature/reader/build.gradle studio-android/LightNovelLibrary/app/build.gradle studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/reader/Phase6ContractManifestTest.kt
git diff --check --cached
git commit -m "build(reader): freeze phase 6 contract"
```

Expected: verifier prints `PHASE6-CONTRACT-PASS`; one commit contains only Task 1 files.

## Task 2: Define Reader Models, Preferences, And Rollout Values

**Depends on:** Task 1.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/reader/ReaderModels.kt`
- Modify: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/ReaderPreferences.kt`
- Modify: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/RouteRolloutFlags.kt`
- Test: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/reader/ReaderModelsTest.kt`
- Test: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/settings/ReaderPreferencesTest.kt`

- [ ] **Step 1: Write failing value and normalization tests**

Test non-negative cursor fields, cursor ordering, valid image byte bounds, normalized reader settings, independent night/e-ink values, default paginated/overlap behavior, disabled volume keys, enabled tap-zone shortcuts, and `readerEnabled=false` in all default route flags. Reflect the module and prove these types contain no Android, Compose, provider, Room, or legacy DTO field.

Run:

```powershell
.\gradlew.bat :core:model:test --tests "org.mewx.wenku8.core.model.reader.ReaderModelsTest" --tests "org.mewx.wenku8.core.model.settings.ReaderPreferencesTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: compilation fails because reader models and the new preference fields are absent.

- [ ] **Step 2: Add stable reader values in `:core:model`**

Create:

```kotlin
package org.mewx.wenku8.core.model.reader

import org.mewx.wenku8.core.model.catalog.BinaryRequest
import org.mewx.wenku8.core.model.catalog.ChapterDocument
import org.mewx.wenku8.core.model.catalog.Volume
import org.mewx.wenku8.core.model.identity.ChapterKey

enum class ReaderMode { PAGINATED, CONTINUOUS }
enum class ReaderAppearance { DAY, NIGHT, EINK }
enum class ReaderPageTransition { OVERLAP }
enum class ReaderContentFreshness { FRESH, STALE, OFFLINE }

data class ReaderCursor(val blockIndex: Int, val charIndex: Int) : Comparable<ReaderCursor> {
    init { require(blockIndex >= 0); require(charIndex >= 0) }
    override fun compareTo(other: ReaderCursor): Int =
        compareValuesBy(this, other, ReaderCursor::blockIndex, ReaderCursor::charIndex)
    companion object { val START = ReaderCursor(0, 0) }
}

data class ReaderCatalog(
    val volumes: List<Volume>,
    val currentChapter: ChapterKey,
) {
    val chapters = volumes.flatMap(Volume::chapters)
    val currentIndex = chapters.indexOfFirst { it.key == currentChapter }
    val previous = chapters.getOrNull(currentIndex - 1)
    val next = chapters.getOrNull(currentIndex + 1)
    val terminalKnown = chapters.isNotEmpty() && currentIndex >= 0
}

data class ReaderChapter(
    val document: ChapterDocument,
    val freshness: ReaderContentFreshness,
    val loadedFromCache: Boolean,
)

sealed interface ReaderImageState {
    data object Pending : ReaderImageState
    data class Cached(
        val imageKey: String,
        val bytes: ByteArray,
        val mediaType: String,
        val legacyPath: String?,
    ) : ReaderImageState {
        init { require(imageKey.isNotBlank()); require(bytes.isNotEmpty()); require(bytes.size <= 20 * 1024 * 1024) }
    }
    data class Broken(val imageKey: String, val reasonCode: String, val retryable: Boolean) : ReaderImageState
}

data class ReaderProgress(
    val chapter: ChapterKey,
    val volumeRemoteId: String?,
    val cursor: ReaderCursor,
    val legacyAid: Int?,
    val legacyCid: Int?,
    val legacyPixelOffset: Int?,
    val legacyViewportHeight: Int?,
    val updatedAtEpochMillis: Long,
)

sealed interface ReaderProgressCommit {
    data class Saved(val mutationVersion: Long) : ReaderProgressCommit
    data class CompletedAndCleared(val mutationVersion: Long) : ReaderProgressCommit
    data class PendingLegacyProjection(val mutationVersion: Long, val mutationId: String) : ReaderProgressCommit
    data class Failed(val operationCode: String) : ReaderProgressCommit
}

enum class ReaderAssetKind { FONT, BACKGROUND }
data class ReaderAsset(val kind: ReaderAssetKind, val canonicalPath: String, val sha256: String)
data class ReaderImageRequest(val resource: BinaryRequest, val imageKey: String)
```

`ReaderImageState.Cached` overrides neither `equals` nor `hashCode`; tests compare metadata and hashes, not byte-array referential equality. `ReaderOpenRequest` remains in its Phase 5 path and is not redefined or moved.

- [ ] **Step 3: Extend preferences without weakening Phase 1 bounds**

Add the fields to the existing `ReaderPreferences` constructor and retain every Phase 1 field/bound:

```kotlin
val mode: ReaderMode = ReaderMode.PAGINATED,
val volumeKeyPaging: Boolean = false,
val tapZoneShortcuts: Boolean = true,
val pageTransition: ReaderPageTransition = ReaderPageTransition.OVERLAP,
```

Import `ReaderMode` and `ReaderPageTransition` from `core.model.reader`. `normalized()` preserves these enum/Boolean values while keeping font `16..30sp`, line height `24..52sp` and at least font plus 8sp, paragraph spacing `8..36sp`, and margin `8..48dp`. Night mode and e-ink remain separate Boolean fields; do not collapse them into app theme.

- [ ] **Step 4: Add a reader rollout flag with default-off semantics**

Extend the existing values exactly:

```kotlin
data class RouteRolloutFlags(
    val shellEnabled: Boolean = false,
    val discoverEnabled: Boolean = false,
    val bookshelfEnabled: Boolean = false,
    val searchEnabled: Boolean = false,
    val settingsEnabled: Boolean = false,
    val readerEnabled: Boolean = false,
)

enum class RouteFlag {
    SHELL_ENABLED, DISCOVER_ENABLED, BOOKSHELF_ENABLED, SEARCH_ENABLED, SETTINGS_ENABLED, READER_ENABLED,
}
```

Add `RouteFlag.READER_ENABLED -> copy(readerEnabled = enabled)` to the exhaustive `with` function. Task 15 may enable it in debug comparison. Task 19 is the only task allowed to change reviewed release defaults.

- [ ] **Step 5: Run model tests and the duplicate-owner scan**

```powershell
.\gradlew.bat :core:model:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
rg -n "(data class|class) ReaderOpenRequest" . -g '*.kt' -g '!core/model/src/main/kotlin/org/mewx/wenku8/core/model/novel/ReaderOpenRequest.kt'
```

Expected: model tests pass and `rg` exits 1. A duplicate model under `api-contract` or a feature stops the task.

- [ ] **Step 6: Commit stable values**

```powershell
git add core/model/src/main/kotlin/org/mewx/wenku8/core/model/reader/ReaderModels.kt core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/ReaderPreferences.kt core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/RouteRolloutFlags.kt core/model/src/test/kotlin/org/mewx/wenku8/core/model/reader/ReaderModelsTest.kt core/model/src/test/kotlin/org/mewx/wenku8/core/model/settings/ReaderPreferencesTest.kt
git diff --check --cached
git commit -m "feat(reader): define stable reader values"
```

## Task 3: Define Reader Repository And Settings Contracts

**Depends on:** Task 2.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/reader/ReaderRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/reader/ReaderProgressRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/reader/ReaderAssetRepository.kt`
- Modify: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/settings/SettingsMutation.kt`
- Test: `studio-android/LightNovelLibrary/core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/reader/ReaderContractsTest.kt`

- [ ] **Step 1: Write failing boundary and cancellation tests**

Compile fakes for every method, assert load policy and failure exhaustiveness, verify image flow cancellation reaches its producer, verify completion is an explicit progress command, and reflect repository signatures to reject Android `Uri`, Intent, Bundle, Bitmap, provider result, Room entity/DAO, DataStore Preferences, File, or legacy DTO types.

Run:

```powershell
.\gradlew.bat :core:domain:test --tests "org.mewx.wenku8.core.domain.reader.ReaderContractsTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: compilation fails because reader repository contracts do not exist.

- [ ] **Step 2: Define content, catalog, and image results**

Create `ReaderRepository.kt`:

```kotlin
package org.mewx.wenku8.core.domain.reader

import kotlinx.coroutines.flow.Flow
import org.mewx.wenku8.core.model.catalog.BinaryRequest
import org.mewx.wenku8.core.model.identity.ChapterKey
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.reader.ReaderCatalog
import org.mewx.wenku8.core.model.reader.ReaderChapter
import org.mewx.wenku8.core.model.reader.ReaderImageState
import org.mewx.wenku8.core.model.novel.ReaderSourceMode

enum class ReaderLoadPolicy { LOCAL_CACHE_FIRST, REMOTE_WITH_CACHE }

sealed interface ReaderFailure {
    data class MissingArgument(val field: String) : ReaderFailure
    data object OfflineWithoutCache : ReaderFailure
    data object EmptyChapter : ReaderFailure
    data object NotFound : ReaderFailure
    data class Storage(val operationCode: String) : ReaderFailure
    data class Network(val operationCode: String) : ReaderFailure
    data class Parse(val contractRevision: Int) : ReaderFailure
    data class InvalidImage(val reasonCode: String) : ReaderFailure
}

sealed interface ReaderResult<out T> {
    data class Content<T>(val value: T) : ReaderResult<T>
    data class Failure(val error: ReaderFailure, val retryable: Boolean) : ReaderResult<Nothing>
}

interface ReaderRepository {
    suspend fun catalog(novel: NovelKey, current: ChapterKey, volumeHint: String): ReaderResult<ReaderCatalog>
    suspend fun chapter(key: ChapterKey, policy: ReaderLoadPolicy): ReaderResult<ReaderChapter>
    fun image(request: BinaryRequest): Flow<ReaderImageState>
}

fun ReaderSourceMode.toLoadPolicy(): ReaderLoadPolicy = when (this) {
    ReaderSourceMode.CLOUD -> ReaderLoadPolicy.REMOTE_WITH_CACHE
    ReaderSourceMode.LOCAL_CACHE_FIRST -> ReaderLoadPolicy.LOCAL_CACHE_FIRST
}
```

The implementation is responsible for cache/source selection. The feature never sees an `ApiResult`, parser revision entity, relative file path, or DAO.

- [ ] **Step 3: Define progress commands with explicit completion semantics**

Create `ReaderProgressRepository.kt`:

```kotlin
package org.mewx.wenku8.core.domain.reader

import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.reader.ReaderProgress
import org.mewx.wenku8.core.model.reader.ReaderProgressCommit

sealed interface ReaderProgressCommand {
    data class Save(val value: ReaderProgress) : ReaderProgressCommand
    data class Complete(val value: ReaderProgress) : ReaderProgressCommand
}

interface ReaderProgressRepository {
    suspend fun load(novel: NovelKey): ReaderProgress?
    suspend fun commit(command: ReaderProgressCommand): ReaderProgressCommit
    suspend fun reconcile(novel: NovelKey): ReaderProgressCommit
}
```

`Complete` is allowed only when the ViewModel proves catalog terminal identity and document-end cursor. It clears canonical progress and both eligible legacy projections through one Phase 3 journal mutation; it never treats an unknown catalog or a failed load as completion.

- [ ] **Step 4: Define custom asset import/reset without Android types**

Create `ReaderAssetRepository.kt`:

```kotlin
package org.mewx.wenku8.core.domain.reader

import org.mewx.wenku8.core.model.reader.ReaderAsset
import org.mewx.wenku8.core.model.reader.ReaderAssetKind

@JvmInline value class ReaderDocumentToken(val value: String) { init { require(value.isNotBlank()) } }

sealed interface ReaderAssetResult {
    data class Imported(val asset: ReaderAsset) : ReaderAssetResult
    data class Reset(val kind: ReaderAssetKind) : ReaderAssetResult
    data class Rejected(val kind: ReaderAssetKind, val reasonCode: String) : ReaderAssetResult
    data class Failed(val kind: ReaderAssetKind, val operationCode: String) : ReaderAssetResult
}

interface ReaderAssetRepository {
    suspend fun import(kind: ReaderAssetKind, token: ReaderDocumentToken): ReaderAssetResult
    suspend fun reset(kind: ReaderAssetKind): ReaderAssetResult
}
```

The app composition supplies a token resolver backed by the one picker result. The token is opaque, process-local, short-lived, and contains no document URI or user path in logs/evidence.

- [ ] **Step 5: Extend typed settings mutations**

Add these branches to the existing sealed `SettingsMutation` and its exhaustive `applyTo` function:

```kotlin
data class ReaderModeValue(val value: ReaderMode) : SettingsMutation
data class ReaderVolumeKeys(val enabled: Boolean) : SettingsMutation
data class ReaderTapZones(val enabled: Boolean) : SettingsMutation
data class ReaderTransition(val value: ReaderPageTransition) : SettingsMutation
```

Each branch copies only its matching `settings.reader` field. Do not add arbitrary key/value mutation or write settings directly from a feature.

- [ ] **Step 6: Verify platform neutrality and commit**

```powershell
.\gradlew.bat :core:domain:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
rg -n "android\.|androidx\.|okhttp|jsoup|Room|DataStore|Wenku8API|GlobalConfig|VolumeList|ChapterInfo" core/domain/src/main -g '*.kt'
```

Expected: tests pass and the source scan exits 1.

```powershell
git add core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/reader core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/settings/SettingsMutation.kt core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/reader/ReaderContractsTest.kt
git diff --check --cached
git commit -m "feat(reader): define reader repository contracts"
```

## Task 4: Implement Cache-Safe Reader Content And Image Repositories

**Depends on:** Task 3 and Phase 5 catalog/download behavior.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/reader/ReaderContentStore.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/reader/RoomLegacyReaderContentStore.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/reader/ReaderCatalogMapper.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/reader/ReaderChapterMapper.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/reader/DefaultReaderRepository.kt`
- Modify: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/novel/ImageAssetStore.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/reader/DefaultReaderRepositoryTest.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/reader/RoomLegacyReaderContentStoreTest.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/novel/ImageAssetStoreTest.kt`

- [ ] **Step 1: Write failing source-order, stale, corruption, and cancellation tests**

Cover both policies, catalog order/current identity, ordered text/image blocks, cache hit without remote work, cache-first miss, remote success persisted atomically, remote network failure with stale cache, parse/empty response never overwriting good bytes, image pending-to-cached, broken/retry, duplicate image single-flight, and flow cancellation cancelling the provider call.

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.reader.RoomLegacyReaderContentStoreTest" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.reader.DefaultReaderRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: compilation fails because the store and repository are absent.

- [ ] **Step 2: Add the storage boundary over Phase 3 stores and paths**

Create this interface and value shape; its Android implementation uses the existing `CatalogCacheDatabase`, `LegacyPathPolicy`, `LegacyChapterDocumentCodec`, atomic replacement helper, and image cache paths rather than a second database or root:

```kotlin
package org.mewx.wenku8.core.storage.reader

import org.mewx.wenku8.core.model.catalog.ChapterDocument
import org.mewx.wenku8.core.model.identity.ChapterKey
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.catalog.Volume

data class StoredChapter(val document: ChapterDocument, val parserRevision: Int, val verifiedSha256: String)
interface ReaderContentStore {
    suspend fun catalog(novel: NovelKey): List<Volume>?
    suspend fun replaceCatalog(novel: NovelKey, volumes: List<Volume>, parserRevision: Int)
    suspend fun chapter(key: ChapterKey): StoredChapter?
    suspend fun replaceChapterIfValid(value: StoredChapter)
}
```

`replaceChapterIfValid` encodes to a sibling temporary file, fsyncs, decodes and compares key/block order, then atomically replaces `saves/novel/{legacyCid}.xml` when a legacy ID exists or the Phase 5 canonical chapter path otherwise. It preserves the prior verified file on every failure. Do not add image methods or an image root to `ReaderContentStore`; reader images reuse and extend Phase 5's sole `ImageAssetStore` boundary.

- [ ] **Step 3: Implement repository policy without feature/provider leakage**

`DefaultReaderRepository` accepts `Wenku8CatalogSource`, `Wenku8BinarySource`, `ReaderContentStore`, the Phase 5 `ImageAssetStore`, a Phase 2 `ReadSingleFlight`, and mapper functions. Use this exact decision order:

```kotlin
override suspend fun chapter(key: ChapterKey, policy: ReaderLoadPolicy): ReaderResult<ReaderChapter> {
    val cached = store.chapter(key)
    if (policy == ReaderLoadPolicy.LOCAL_CACHE_FIRST) {
        return cached?.let { ReaderResult.Content(ReaderChapter(it.document, ReaderContentFreshness.OFFLINE, true)) }
            ?: ReaderResult.Failure(ReaderFailure.OfflineWithoutCache, retryable = true)
    }
    return singleFlight.run("reader-chapter:${key.novel.sourceId.value}:${key.novel.remoteId}:${key.remoteId}") {
        when (val remote = catalogSource.chapter(key)) {
            is ApiResult.Success -> {
                val mapped = chapterMapper.map(remote.value)
                if (mapped.blocks.isEmpty()) ReaderResult.Failure(ReaderFailure.EmptyChapter, true)
                else {
                    store.replaceChapterIfValid(
                        StoredChapter(mapped, parserRevision, chapterMapper.sha256(mapped)),
                    )
                    ReaderResult.Content(ReaderChapter(mapped, ReaderContentFreshness.FRESH, false))
                }
            }
            is ApiResult.Failure -> cached?.let {
                ReaderResult.Content(ReaderChapter(it.document, ReaderContentFreshness.STALE, true))
            } ?: ReaderResult.Failure(remote.error.toReaderFailure(), remote.error.isRetryable())
        }
    }
}
```

`parserRevision` is a constructor value supplied by `DefaultAppContainer` from the accepted Phase 2 operation-ledger binding; it is not inferred from `ResponseMetadata` or fabricated in the repository. `catalog()` uses stored ordered volumes first for immediate display, refreshes through Phase 5's repository/provider adapter when policy permits, and rejects catalogs whose chapter keys do not belong to the requested novel.

- [ ] **Step 4: Implement bounded image state flow**

Extend the existing Phase 5 `ImageAssetStore` with these reader-neutral operations instead of creating another store:

```kotlin
suspend fun readByKey(imageKey: String): StoredImageAsset?
suspend fun writeVerified(imageKey: String, value: BinaryResource, maximumBytes: Long): StoredImageAsset
suspend fun discardIncomplete(imageKey: String)
```

`image(request)` derives a lowercase SHA-256 key from the canonical HTTPS URL, emits `readByKey` bytes immediately when present, otherwise emits `Pending`, runs one provider fetch through single-flight, validates media type/magic and the 20 MiB bound, calls `writeVerified`, then emits `Cached`. `ImageAssetStore` continues to own the Phase 5 approved `saves/imgs` or `cacheDir/imgs` paths, atomic replacement, and path-policy checks. It emits `Broken(key, reasonCode, retryable)` for typed failures, calls `discardIncomplete` after a failed write, and rethrows `CancellationException`. It never passes a remote URL to an unaudited UI image loader.

- [ ] **Step 5: Run focused and affected suites**

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.reader.*" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.reader.*" :api-contract-tests:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: all source-order, stale, atomicity, block-order, image, and cancellation cases pass; synthetic fixture requests are the only network activity.

- [ ] **Step 6: Commit content repositories**

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/reader core/storage/src/test/java/org/mewx/wenku8/core/storage/reader core/storage/src/main/java/org/mewx/wenku8/core/storage/novel/ImageAssetStore.kt core/storage/src/test/java/org/mewx/wenku8/core/storage/novel/ImageAssetStoreTest.kt core/data/src/main/java/org/mewx/wenku8/core/data/reader/ReaderCatalogMapper.kt core/data/src/main/java/org/mewx/wenku8/core/data/reader/ReaderChapterMapper.kt core/data/src/main/java/org/mewx/wenku8/core/data/reader/DefaultReaderRepository.kt core/data/src/test/java/org/mewx/wenku8/core/data/reader/DefaultReaderRepositoryTest.kt
git diff --check --cached
git commit -m "feat(reader): add cache-safe content repository"
```

## Task 5: Implement Journaled Progress Save, Restore, Completion, And Rollback Projection

**Depends on:** Task 3 and the complete Phase 3 progress migration.

**Files:**
- Modify: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryDaos.kt`
- Modify: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/ReaderProgressMigrationParticipant.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/reader/DefaultReaderProgressRepository.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/participants/ReaderProgressWriteTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/reader/DefaultReaderProgressRepositoryTest.kt`

- [ ] **Step 1: Write failing progress and crash-boundary tests**

Test load by source/novel, save cursor `0/0`, chapter change, repeated mutation, cancellation, canonical failure, legacy projection failure, reconciliation, completion at known terminal document end, refusal to complete unknown/nonterminal content, and every Phase 3 crash point. Decode resulting `read_saves.wk8` and `read_saves_v1.wk8` with the existing golden codec. Run with canonical access disabled and assert the projected old read is identical.

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.participants.ReaderProgressWriteTest" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.reader.DefaultReaderProgressRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: tests fail because the participant has no typed new-reader write API.

- [ ] **Step 2: Add one typed participant mutation path**

Extend the existing participant without changing its domain key or state machine:

```kotlin
sealed interface ReaderProgressMutation {
    data class Save(val value: ReaderProgress) : ReaderProgressMutation
    data class ClearCompleted(val value: ReaderProgress) : ReaderProgressMutation
}

suspend fun ReaderProgressMigrationParticipant.apply(
    mutationId: String,
    mutation: ReaderProgressMutation,
): MutationOutcome = when (mutation) {
    is ReaderProgressMutation.Save -> journaledSave(mutationId, mutation.value)
    is ReaderProgressMutation.ClearCompleted -> journaledClear(mutationId, mutation.value.chapter.novel)
}
```

`journaledSave` maps cursor block/char and legacy IDs into the existing `ReaderProgressEntity`; `journaledClear` removes the canonical row only after the journal exists and projects removal to both old formats where legacy IDs permit it. Both use Phase 3's canonical-version then legacy-projection acknowledgement protocol. No direct DAO/file write is allowed outside the participant.

- [ ] **Step 3: Implement the domain adapter**

`DefaultReaderProgressRepository` maps the DAO row to `ReaderProgress`, creates one random mutation ID before the first journal append, retains it across participant retries, and maps `MutationOutcome` to `Saved`, `CompletedAndCleared`, `PendingLegacyProjection`, or `Failed`. `reconcile` invokes only the participant's existing domain reconciliation.

- [ ] **Step 4: Prove old-format and old-signed read compatibility**

Run the focused tests twice, then invoke the Phase 3 legacy-reader projection fixture on API 23 and API 33. Expected: the second run adds no duplicate applied mutation; pre-API-33 and typed API-33 readers decode the same chapter/cursor or cleared record.

- [ ] **Step 5: Run the complete storage compatibility suite**

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.legacy.*" --tests "org.mewx.wenku8.core.storage.migration.participants.ReaderProgress*" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.reader.DefaultReaderProgressRepositoryTest" :verification-tools:phase3Gate -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: Phase 3 gate remains green; no non-secret artifact is deleted outside an explicit completion projection.

- [ ] **Step 6: Commit progress integration**

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryDaos.kt core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/ReaderProgressMigrationParticipant.kt core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/participants/ReaderProgressWriteTest.kt core/data/src/main/java/org/mewx/wenku8/core/data/reader/DefaultReaderProgressRepository.kt core/data/src/test/java/org/mewx/wenku8/core/data/reader/DefaultReaderProgressRepositoryTest.kt
git diff --check --cached
git commit -m "feat(reader): journal reader progress writes"
```

## Task 6: Persist Reader Preferences And Import Safe Custom Assets

**Depends on:** Tasks 2 and 3 plus Phase 1 settings migration.

**Files:**
- Modify: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/settings/SettingsPreferencesCodec.kt`
- Modify: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/settings/LegacySettingsAdapter.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/reader/ReaderAssetStore.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/reader/AndroidReaderAssetStore.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/reader/DefaultReaderAssetRepository.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/reader/ReaderAssetStoreTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/reader/DefaultReaderAssetRepositoryTest.kt`

- [ ] **Step 1: Write failing settings and hostile-input tests**

Test canonical round trips for mode/volume/tap/transition, legacy projection preservation, valid TTF/OTF and JPEG/PNG/WebP, wrong magic, MIME mismatch, over-size, decompression-bomb dimensions, truncated input, cancellation, path traversal, atomic-copy failure, settings failure rollback, reset without deleting the sole old asset, and no URI/token in logs.

- [ ] **Step 2: Add exact canonical keys and projections**

Add DataStore keys `reader_mode`, `reader_volume_keys`, `reader_tap_zones`, and `reader_transition`; decode unknown enum names to the defaults in Task 2 and preserve the raw value in diagnostics. Existing font/background paths, sizes, line/paragraph/margin, night, and e-ink keep Phase 1 keys. Project only legacy-supported identities through `LegacySettingsAdapter`; new-only values remain canonical and never overwrite an unrelated unknown legacy key.

- [ ] **Step 3: Implement bounded validation and atomic copy**

`ReaderAssetStore.import` accepts an already opened one-shot stream from an injected token resolver, copies at most 10 MiB for fonts or 20 MiB for backgrounds to a sibling pending file, validates magic before rename, validates background dimensions are `1..8192` each with bounds-only decode, fsyncs, then atomically replaces `custom/reader_font` or `custom/reader_background`. Font magic must be one of `00 01 00 00`, `4F 54 54 4F`, or `74 72 75 65`; image magic must match JPEG, PNG, or WebP. It returns only canonical path and SHA-256.

- [ ] **Step 4: Coordinate asset commit with typed settings**

`DefaultReaderAssetRepository.import` resolves the opaque token once, imports to a pending candidate, updates the matching `SettingsMutation.ReaderFontPath` or `ReaderBackgroundPath`, and publishes the candidate only for `Synchronized` or `PendingSynchronization`. On typed settings failure it restores the previous candidate and returns `Failed`. `reset` writes a null path and retains old bytes for rollback/non-deletion.

- [ ] **Step 5: Verify settings migration and asset tests**

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.reader.ReaderAssetStoreTest" --tests "org.mewx.wenku8.core.storage.settings.*" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.reader.DefaultReaderAssetRepositoryTest" :verification-tools:phase1Gate -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: settings and asset cases pass, Phase 1 rollback projection remains green, and no asset content appears in test output.

- [ ] **Step 6: Commit settings/assets**

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/settings core/storage/src/main/java/org/mewx/wenku8/core/storage/reader/ReaderAssetStore.kt core/storage/src/main/java/org/mewx/wenku8/core/storage/reader/AndroidReaderAssetStore.kt core/storage/src/test/java/org/mewx/wenku8/core/storage/reader core/data/src/main/java/org/mewx/wenku8/core/data/reader/DefaultReaderAssetRepository.kt core/data/src/test/java/org/mewx/wenku8/core/data/reader/DefaultReaderAssetRepositoryTest.kt
git diff --check --cached
git commit -m "feat(reader): persist reader appearance assets"
```

## Task 7: Build The Independent Deterministic Pagination Engine

**Depends on:** Task 2.

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderLayoutSpec.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderTextMeasurer.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderPaginator.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderPageTransition.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/test/java/org/mewx/wenku8/feature/reader/paging/ReaderPaginatorTest.kt`

- [ ] **Step 1: Write failing pagination and reflow properties**

Cover empty, one CJK paragraph, Latin words, surrogate pairs, CRLF normalization, semantic breaks, consecutive images, image-first page, exact boundary, forward/backward coverage, no dropped/duplicated characters, cursor monotonicity, deterministic replay, font/margin/size reflow preserving logical cursor, and reduced-motion transition selection.

- [ ] **Step 2: Define pixel layout and output values**

```kotlin
data class ReaderLayoutSpec(
    val contentWidthPx: Int,
    val contentHeightPx: Int,
    val lineHeightPx: Int,
    val paragraphSpacingPx: Int,
    val paragraphIndent: String,
    val imageHeightPx: Int,
) { init { require(contentWidthPx > 0 && contentHeightPx > 0 && lineHeightPx > 0) } }

fun interface ReaderTextMeasurer { fun width(text: String): Float }
data class ReaderLine(val text: String, val imageKey: String?, val start: ReaderCursor, val end: ReaderCursor, val heightPx: Int)
data class ReaderPage(val start: ReaderCursor, val end: ReaderCursor, val lines: List<ReaderLine>, val hasPrevious: Boolean, val hasNext: Boolean)
```

- [ ] **Step 3: Implement one independently authored page traversal**

`ReaderPaginator.pages(document, layout, measurer)` normalizes only CRLF to LF, walks `ChapterBlock` order, advances text at Unicode code-point boundaries, chooses the longest prefix whose measured width is within `contentWidthPx`, and guarantees at least one code point per line. Paragraph indent is visual text only and does not advance the source cursor. An image occupies `min(imageHeightPx, contentHeightPx)` and starts a new page when it cannot fit below existing lines. Semantic breaks consume one paragraph spacing. A page is emitted only when it advances a cursor or contains an image; the terminal page end is the canonical document-end cursor.

Add `pageContaining(cursor)` and `pageBefore(cursor)` over the immutable page list. This implementation must not inspect the deleted SlidingLayout, legacy paginator, or Wild expression.

- [ ] **Step 4: Add overlap motion as a presentation policy**

`ReaderPageTransition.kt` exposes `OVERLAP` and `SNAP_REDUCED_MOTION`. The overlap spec keeps the outgoing page above the incoming page while translating the active page by the signed viewport width; it changes no cursor. When system animations are disabled, selection returns `SNAP_REDUCED_MOTION`.

- [ ] **Step 5: Run property and repeatability tests**

```powershell
.\gradlew.bat :feature:reader:testDebugUnitTest --tests "org.mewx.wenku8.feature.reader.paging.ReaderPaginatorTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: all fixtures and 1,000 seeded randomized documents preserve ordered content and deterministic page boundaries.

- [ ] **Step 6: Commit the pure engine**

```powershell
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderLayoutSpec.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderTextMeasurer.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderPaginator.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderPageTransition.kt feature/reader/src/test/java/org/mewx/wenku8/feature/reader/paging/ReaderPaginatorTest.kt
git diff --check --cached
git commit -m "feat(reader): add deterministic pagination"
```

## Task 8: Add Continuous Cursor Mapping, Progress Seek, And Chapter Search

**Depends on:** Task 7.

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ContinuousCursorMapper.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderProgressScale.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderSearchIndex.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/test/java/org/mewx/wenku8/feature/reader/paging/ContinuousAndSearchTest.kt`

- [ ] **Step 1: Write failing anchor, scale, and search tests**

Test first-visible block/offset mapping, resize restoration, 0/1000 endpoints, cursor-scale round trip, empty documents, repeated matches, overlapping CJK text, case-insensitive Latin text, surrogate pairs, next/previous wrap, a 500-match cap, and query/result restoration.

- [ ] **Step 2: Implement stable continuous anchors**

```kotlin
data class ContinuousAnchor(val cursor: ReaderCursor, val offsetPx: Int)

class ContinuousCursorMapper {
    fun fromVisible(blockIndex: Int, charIndex: Int, offsetPx: Int): ContinuousAnchor =
        ContinuousAnchor(ReaderCursor(blockIndex.coerceAtLeast(0), charIndex.coerceAtLeast(0)), offsetPx)
    fun restore(anchor: ContinuousAnchor): Pair<Int, Int> = anchor.cursor.blockIndex to anchor.offsetPx
}
```

The UI reports the first visible text block and first visible character when available; image/semantic blocks use char `0`. Offset is clamped only by the post-layout `LazyListState` scroll operation.

- [ ] **Step 3: Implement a fixed accessible progress scale**

`ReaderProgressScale` computes cumulative Unicode code points plus one unit per image/semantic block. It maps cursors to integer `0..1000` and maps a slider value back to the nearest valid cursor without entering the middle of a surrogate pair. An empty document maps both directions to start.

- [ ] **Step 4: Implement bounded find-in-chapter**

```kotlin
data class ReaderSearchMatch(val blockIndex: Int, val startChar: Int, val endChar: Int)
data class ReaderSearchResult(val query: String, val matches: List<ReaderSearchMatch>, val selectedIndex: Int?)
```

Trim the query, return no matches for blank input, search only text blocks with locale-independent lowercase comparison, advance by one Unicode code point after each hit, cap at 500, and expose pure `next`/`previous` selection with wrap. Images and descriptions are not searched as book text.

- [ ] **Step 5: Run tests and commit**

```powershell
.\gradlew.bat :feature:reader:testDebugUnitTest --tests "org.mewx.wenku8.feature.reader.paging.ContinuousAndSearchTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ContinuousCursorMapper.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderProgressScale.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/paging/ReaderSearchIndex.kt feature/reader/src/test/java/org/mewx/wenku8/feature/reader/paging/ContinuousAndSearchTest.kt
git diff --check --cached
git commit -m "feat(reader): add continuous search and seek models"
```

## Task 9: Build The Reader State Machine And ViewModel-Owned Loading

**Depends on:** Tasks 4 and 8.

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/model/ReaderUiState.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/model/ReaderEvent.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/model/ReaderEffect.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/model/ReaderRestorationState.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderStateReducer.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderViewModel.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/test/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderViewModelLoadTest.kt`

- [ ] **Step 1: Write failing argument, load, retry, and cancellation tests**

Cover missing/non-positive/foreign chapter identity, loading, fresh content, offline content, empty, typed storage/network/parse failure, retry preserving context, catalog-before-boundary decisions, force-saved-position behavior, local/cache-first behavior, stale content banner, two rapid chapter selections cancelling the first, and a late cancelled result never replacing current state.

- [ ] **Step 2: Define one immutable route state family**

Use these exact roots; split the declarations across the named model files:

```kotlin
sealed interface ReaderContentState {
    data object Loading : ReaderContentState
    data class Content(val chapter: ReaderChapter) : ReaderContentState
    data object Empty : ReaderContentState
    data class Error(val failure: ReaderFailure, val retryable: Boolean) : ReaderContentState
}

sealed interface ReaderOverlay {
    data object None : ReaderOverlay
    data object Chrome : ReaderOverlay
    data object Catalog : ReaderOverlay
    data object Settings : ReaderOverlay
    data object Search : ReaderOverlay
    data class SeekConfirmation(val target: ReaderCursor, val value: Int) : ReaderOverlay
}

data class ReaderUiState(
    val request: ReaderOpenRequest,
    val content: ReaderContentState = ReaderContentState.Loading,
    val catalog: ReaderCatalog? = null,
    val cursor: ReaderCursor = ReaderCursor.START,
    val mode: ReaderMode = ReaderMode.PAGINATED,
    val preferences: ReaderPreferences = ReaderPreferences(),
    val overlay: ReaderOverlay = ReaderOverlay.None,
    val pageIndex: Int = 0,
    val pageCount: Int = 0,
    val continuousOffsetPx: Int = 0,
    val imageStates: Map<String, ReaderImageState> = emptyMap(),
    val search: ReaderSearchResult = ReaderSearchResult("", emptyList(), null),
    val progressWritePending: Boolean = false,
    val boundaryCode: String? = null,
)

sealed interface ReaderEffect {
    data class Snackbar(val messageCode: String) : ReaderEffect
    data class Announce(val messageCode: String, val argument: String?) : ReaderEffect
    data class OpenImage(val request: ImageOpenRequest) : ReaderEffect
    data class PickAsset(val kind: ReaderAssetKind) : ReaderEffect
    data object CloseRoute : ReaderEffect
}
```

Effects carry stable local message codes, not visible hard-coded text. Navigation, pickers, Snackbar, and announcements never live as persistent booleans.

- [ ] **Step 3: Restore only bounded route/cursor/UI intent**

`ReaderRestorationState` stores source/novel/chapter/volume IDs, source mode, force flag, mode, cursor block/char, continuous offset, logical overlay intent, and search query/selection. It stores no chapter body, image bytes/path, provider response, Cookie, custom asset token, or repository failure body. Write through `SavedStateHandle` after each stable cursor/overlay/search change.

- [ ] **Step 4: Implement structured load generation control**

`ReaderViewModel` owns one `loadJob`; each `openChapter` increments a `Long` generation, cancels and joins the prior job, sets loading while retaining catalog, loads catalog and progress on the injected IO dispatcher, then loads chapter with `request.sourceMode.toLoadPolicy()`. It applies a result only when generation and current chapter still match. `CancellationException` is always rethrown. No executor, Handler, callback future, `GlobalScope`, or raw thread is permitted.

On initial load, use restored cursor first; otherwise use stored progress only when `forceSavedPosition=true` and stored chapter equals the request chapter. A missing/invalid internal request becomes `ReaderFailure.MissingArgument` without repository work. The old trampoline's receiver-specific defaults are handled before this boundary.

- [ ] **Step 5: Run ViewModel state/effect tests**

```powershell
.\gradlew.bat :feature:reader:testDebugUnitTest --tests "org.mewx.wenku8.feature.reader.viewmodel.ReaderViewModelLoadTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: every state/effect sequence is deterministic under `StandardTestDispatcher`; cancellation leaves no late state/effect.

- [ ] **Step 6: Commit the loading state machine**

```powershell
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/model feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderStateReducer.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderViewModel.kt feature/reader/src/test/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderViewModelLoadTest.kt
git diff --check --cached
git commit -m "feat(reader): add ViewModel reader state"
```

## Task 10: Complete Navigation, Reflow, Images, Search, Settings, And Progress Effects

**Depends on:** Tasks 5, 6, and 9.

**Files:**
- Modify: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/model/ReaderEvent.kt`
- Modify: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderViewModel.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderProgressWriter.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderViewModelFactory.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/test/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderViewModelInteractionTest.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/test/java/org/mewx/wenku8/feature/reader/viewmodel/ReaderProgressWriterTest.kt`

- [ ] **Step 1: Write failing interaction state/effect tests**

Cover page/viewport previous-next, first/last chapter feedback, current-chapter catalog selection, layout reflow at same cursor, mode switching, slider confirmation/cancel, search next/previous, image pending/cached/broken/retry/open, every typed setting mutation, picker result success/rejection/failure, two-second progress debounce, explicit host-stop flush, completion clear, pending projection indicator, process death, and Dialog/Sheet/chrome back priority.

- [ ] **Step 2: Define one exhaustive event API**

`ReaderEvent` contains `LayoutChanged`, `Previous`, `Next`, `PreviousChapter`, `NextChapter`, `SelectChapter`, `CursorChanged`, `ModeChanged`, `ToggleChrome`, `OpenCatalog`, `OpenSettings`, `OpenSearch`, `SearchQueryChanged`, `SearchNext`, `SearchPrevious`, `SeekPreview`, `ConfirmSeek`, `CancelSeek`, `PreferenceChanged`, `PickAsset`, `AssetPicked`, `RetryImage`, `OpenImage`, `RetryContent`, `Back`, and `HostStopped`. The reducer has an explicit branch for every event; no Boolean flag matrix substitutes for overlay state.

- [ ] **Step 3: Add a conflated progress writer with a mandatory flush**

```kotlin
class ReaderProgressWriter(
    scope: CoroutineScope,
    private val repository: ReaderProgressRepository,
    private val onResult: (ReaderProgressCommit) -> Unit,
) {
    private val pending = AtomicReference<ReaderProgressCommand?>(null)
    private val signal = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val commitMutex = Mutex()
    private val job = scope.launch {
        signal.debounce(2.seconds).collect { commitPending() }
    }
    fun offer(command: ReaderProgressCommand) { pending.set(command); signal.tryEmit(Unit) }
    suspend fun flush() = commitPending()
    fun cancel() { job.cancel() }

    private suspend fun commitPending() = commitMutex.withLock {
        val command = pending.getAndSet(null) ?: return@withLock
        onResult(repository.commit(command))
    }
}
```

The atomic pending slot plus serialized mutex prevents a debounce commit and `flush()` from committing the same command twice while retaining a command offered during an active commit. `HostStopped` flushes before returning. A failed or pending legacy projection leaves visible nonblocking state and calls `reconcile` on the next load.

- [ ] **Step 4: Implement navigation and completion policy**

In paginated mode `Previous/Next` changes page when possible, then moves chapter. Continuous mode scrolls one viewport through a UI command and uses explicit chapter commands at boundaries. First/last boundary emits Snackbar plus concise announcement. Completion sends `Complete` only when catalog terminal is known, current chapter is the final ordered chapter, and cursor equals the paginator/continuous document-end cursor; otherwise it sends `Save`.

- [ ] **Step 5: Implement reflow, search, images, and settings**

`LayoutChanged` rebuilds pages on the injected default dispatcher and selects the page containing the prior logical cursor. Settings collect from `SettingsRepository.settings`; typography/margin/mode changes reflow, appearance changes do not. Image jobs are keyed by canonical image key and cancelled when the chapter changes. Picker effects register no token in saved state; `AssetPicked` calls `ReaderAssetRepository` then relies on canonical settings flow. Every visible error uses a resource-backed message code.

- [ ] **Step 6: Run state, progress, and repository suites**

```powershell
.\gradlew.bat :feature:reader:testDebugUnitTest --tests "org.mewx.wenku8.feature.reader.viewmodel.*" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.reader.*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: all sequences pass; no unconsumed effect, child job, or virtual-time task remains.

- [ ] **Step 7: Commit complete ViewModel behavior**

```powershell
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/model/ReaderEvent.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/viewmodel feature/reader/src/test/java/org/mewx/wenku8/feature/reader/viewmodel
git diff --check --cached
git commit -m "feat(reader): complete reader interactions"
```

## Task 11: Render Full-Bleed Paginated And Continuous Content

**Depends on:** Task 10.

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderRoute.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderScreen.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/PaginatedReader.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ContinuousReader.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderContentBlock.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderAppearance.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/ReaderContentUiTest.kt`

- [ ] **Step 1: Write failing UI tests for every content state and mode**

Assert full-bleed/unframed content, bounded readable width, ordered text/image semantics, loading, empty, retryable error, offline/stale indicator, paginated page stability, continuous anchor callback, image pending/cached/broken/retry, font 2.0, custom background contrast scrim, and no card/page container around the primary scene.

- [ ] **Step 2: Implement one pure-state route boundary**

`ReaderRoute` takes `ReaderViewModel`, `ReaderHostCallbacks`, and adaptive facts. It collects with `collectAsStateWithLifecycle`, collects effects once with `repeatOnLifecycle(STARTED)`, registers `ON_STOP` to send `HostStopped`, and passes only state/events into `ReaderScreen`. Picker/open-image/system-bar/navigation actions go through host callbacks; no composable calls `appContainer()`.

- [ ] **Step 3: Implement paginated content with fixed dimensions**

Use a full-size `Box`, a `BoxWithConstraints` measurement callback, and a stable page slot whose dimensions never change with loading/image labels. Render current `ReaderPage.lines` in one constrained column. The independently authored overlap transition uses `graphicsLayer`/offset with fixed viewport dimensions; reduced motion snaps. Center/edge pointer shortcuts are added later and do not replace visible controls.

- [ ] **Step 4: Implement continuous content with stable keys**

Use `LazyColumn` keyed by chapter key plus block index, `contentPadding` from reader margins/safe physical region, and `derivedStateOf` for the first visible anchor. Text uses reader typography, images preserve aspect ratio within bounded readable width, and semantic breaks are spacing without semantics. Restore with `scrollToItem(blockIndex, -offsetPx)` after layout, once per restoration token.

- [ ] **Step 5: Implement day/night/e-ink and custom backgrounds**

`ReaderAppearance` supplies explicit high-contrast content/control colors: day uses a light neutral reading surface, night uses near-black with light text, and e-ink uses white/black/grayscale with dynamic color disabled. A validated custom background is drawn behind a contrast scrim whose minimum alpha is covered by contrast tests. Material 3 remains in force for chrome/sheets/dialogs; book text uses `ReaderTypography` with letter spacing `0.sp`.

- [ ] **Step 6: Run Compose UI and screenshot smoke**

```powershell
.\gradlew.bat :feature:reader:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.reader.ReaderContentUiTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: both modes and all state/image/appearance cases pass at compact and expanded test roots.

- [ ] **Step 7: Commit reader content UI**

```powershell
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderRoute.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderScreen.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/PaginatedReader.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ContinuousReader.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderContentBlock.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderAppearance.kt feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/ReaderContentUiTest.kt
git diff --check --cached
git commit -m "feat(reader): render both reader modes"
```

## Task 12: Add Material 3 Chrome, Catalog, Search, Seek, And Settings

**Depends on:** Task 11.

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderChrome.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderCatalog.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderSettings.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderSearch.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderProgressSeek.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/res/values/strings.xml`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/res/values-zh-rTW/strings.xml`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/res/values-zh-rHK/strings.xml`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/ReaderChromeUiTest.kt`

- [ ] **Step 1: Write failing chrome/overlay/focus tests**

Test every visible command, icon label/tooltip, 48dp targets, current chapter selected/focused, Sheet dismissal focus return, settings controls and values, independent night/e-ink, segmented mode, volume/tap toggles, import/reset recovery, search result count/navigation, seek value/confirmation, first/last disabled state, font 2.0, IME-open search, and back order.

- [ ] **Step 2: Build visible reader navigation chrome**

Use Material 3 top/bottom app bars over the full-bleed scene. Provide icon buttons for close, catalog, search, settings, previous, next, previous chapter, and next chapter with resource labels. Display chapter title plus `page/current` or continuous percentage. Hidden chrome never removes the semantics action that shows it.

- [ ] **Step 3: Build catalog Sheet/pane content once**

`ReaderCatalogContent` uses volume headings and keyed `ListItem` rows, explicit selected/current semantics, and a `LazyListState` initial index for the current chapter. Compact wraps it in `ModalBottomSheet`; expanded hosts the same content persistently. The two presentations can never coexist.

- [ ] **Step 4: Build settings, search, and seek controls**

Use `SingleChoiceSegmentedButtonRow` for paginated/continuous, Sliders with visible values for font/line/paragraph/margin, Switches for night/e-ink/volume/tap, and icon-plus-text commands for import/reset. `SearchBar` retains query and selected match. Progress uses Slider `0..1000`, percent/value text, and `AlertDialog` confirmation before commit. Controls emit typed events only.

- [ ] **Step 5: Add complete localized strings**

Add every title, action, state, value template, content description, boundary, failure, retry, offline/stale, announcement, and import/reset result in default, Taiwan Traditional, and Hong Kong resources. No visible string or accessibility description remains hard-coded in Kotlin.

- [ ] **Step 6: Run overlay UI and resource checks**

```powershell
.\gradlew.bat :feature:reader:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.reader.ReaderChromeUiTest :feature:reader:lintDebug -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: all overlays, focus/back, localization, sizing, and font-scale cases pass with zero lint errors.

- [ ] **Step 7: Commit reader controls**

```powershell
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderChrome.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderCatalog.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderSettings.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderSearch.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderProgressSeek.kt feature/reader/src/main/res feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/ReaderChromeUiTest.kt
git diff --check --cached
git commit -m "feat(reader): add Material 3 reader controls"
```

## Task 13: Add Pointer, Keyboard, DPAD, Volume, And Accessibility Actions

**Depends on:** Task 12.

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderInput.kt`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderSemantics.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderKeyCommandRegistry.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/Wenku8ShellActivity.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/test/java/org/mewx/wenku8/feature/reader/ui/ReaderInputMapperTest.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/ReaderAccessibilityTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/navigation/ReaderKeyCommandRegistryTest.kt`

- [ ] **Step 1: Write failing input and semantics matrices**

Cover left/center/right tap zones, horizontal swipe threshold/direction/cancel, tap shortcuts disabled, volume opt-in/off, key down/up repeat handling, Left/Right/PageUp/PageDown/Space, DPAD focus, continuous viewport scroll, visible alternatives, TalkBack reading order, custom actions, concise live announcements, Switch Access reachability, and no page body re-announcement.

- [ ] **Step 2: Implement deterministic input mapping**

`ReaderInputMapper` maps pointer/key facts to `Previous`, `Next`, or `ToggleChrome`. Tap zones act only for an unconsumed short tap with movement below touch slop; center toggles chrome; left/right follow layout direction. Swipe requires horizontal displacement greater than the platform threshold and dominance over vertical displacement. All shortcuts are disabled while a Dialog/Sheet/Search field consumes input.

- [ ] **Step 3: Delegate volume keys only while the route is active**

`ReaderKeyCommandRegistry` holds one lifecycle-bound weak command sink and its current opt-in Boolean. `Wenku8ShellActivity.dispatchKeyEvent` delegates only `KEYCODE_VOLUME_UP/DOWN` `ACTION_DOWN` events when the registry accepts them; otherwise it calls `super` so volume behavior is unchanged. Route disposal unregisters the exact sink. No static Activity reference is retained.

- [ ] **Step 4: Add explicit reader semantics actions**

The content root has ordered content semantics followed by custom actions for show controls, previous/next page or viewport, previous/next chapter, catalog, and settings. Selected, checked, disabled, heading, collection, progress, error, and live-region states are explicit. Page/chapter changes announce only position/title, never concatenate page text. Decorative backgrounds are cleared from semantics; meaningful images use provided descriptions or a localized contextual label.

- [ ] **Step 5: Run unit, UI, keyboard, and accessibility tests**

```powershell
.\gradlew.bat :feature:reader:testDebugUnitTest --tests "org.mewx.wenku8.feature.reader.ui.ReaderInputMapperTest" :feature:reader:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.reader.ReaderAccessibilityTest :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.ReaderKeyCommandRegistryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: every input path has the same typed event as its visible alternative and focus never disappears behind hidden chrome.

- [ ] **Step 6: Commit input/accessibility behavior**

```powershell
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderInput.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderSemantics.kt feature/reader/src/test/java/org/mewx/wenku8/feature/reader/ui/ReaderInputMapperTest.kt feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/ReaderAccessibilityTest.kt app/src/main/java/org/mewx/wenku8/navigation/ReaderKeyCommandRegistry.kt app/src/main/java/org/mewx/wenku8/activity/Wenku8ShellActivity.kt app/src/test/java/org/mewx/wenku8/navigation/ReaderKeyCommandRegistryTest.kt
git diff --check --cached
git commit -m "feat(reader): add accessible reader input"
```

## Task 14: Handle Adaptive Regions, System Bars, Insets, And Predictive Back

**Depends on:** Task 13.

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderAdaptiveLayout.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderSystemUiController.kt`
- Modify: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderRoute.kt`
- Modify: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderScreen.kt`
- Test: `studio-android/LightNovelLibrary/feature/reader/src/test/java/org/mewx/wenku8/feature/reader/ui/ReaderAdaptiveLayoutTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/ReaderSystemIntegrationTest.kt`

- [ ] **Step 1: Write failing width/height/hinge/back/system UI tests**

Test 599/600/839/840dp boundaries, compact height, bounded text width, persistent pane eligibility from physical regions, separating/occluding hinges, start-pane-before-detail order, no span across hinge, Sheet-to-pane remap, focus return, cutout, gesture/three-button insets, chrome visible/hidden system bars, and API 36 predictive-back start/cancel/commit for seek dialog, catalog/settings Sheet, chrome, and route.

- [ ] **Step 2: Map only usable physical regions**

`ReaderAdaptiveLayout` consumes Phase 1 `AdaptiveLayoutInfo` converted at the app boundary into plain width/height/region/hinge facts. Select a catalog pane only when two non-occluded physical regions each satisfy the tested minimum readable width; otherwise use one bounded content region and a Sheet. Clamp book text to a reviewed maximum readable width inside its assigned region. No fixed 300/320dp panel alone determines mode.

- [ ] **Step 3: Remap presentation without changing logical state**

On resize/fold, preserve destination, chapter, cursor, mode, list/search state, and `overlay == Catalog`. If a pane becomes eligible, render catalog there and no Sheet; if it ceases to be eligible, preserve logical catalog intent and show the Sheet. Focus current chapter when a pane appears and return focus to the catalog button when a Sheet closes.

- [ ] **Step 4: Centralize edge-to-edge and system-bar style**

`ReaderSystemUiController` is app-owned and calls `enableEdgeToEdge` once in the Activity. It applies transparent bars, light/dark icon appearance from reader palette, and transient bar visibility requested by the route; it restores shell style on route disposal. Content consumes only the assigned safe/hinge region while backgrounds draw edge-to-edge. Gesture handles and cutouts remain usable.

- [ ] **Step 5: Implement back and predictive-back ordering**

The ViewModel consumes Back in this order: seek confirmation, modal Sheet, search/settings/catalog overlay, chrome, then `CloseRoute`. Expanded persistent catalog is not closed by Back. Register predictive back only for the current consumable layer; cancel restores unchanged state, commit dispatches one Back, and no layer is popped twice.

- [ ] **Step 6: Run adaptive/API 36 tests and commit**

```powershell
.\gradlew.bat :feature:reader:testDebugUnitTest --tests "org.mewx.wenku8.feature.reader.ui.ReaderAdaptiveLayoutTest" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.ReaderSystemIntegrationTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderAdaptiveLayout.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderRoute.kt feature/reader/src/main/java/org/mewx/wenku8/feature/reader/ui/ReaderScreen.kt feature/reader/src/test/java/org/mewx/wenku8/feature/reader/ui/ReaderAdaptiveLayoutTest.kt app/src/main/java/org/mewx/wenku8/navigation/ReaderSystemUiController.kt app/src/androidTest/java/org/mewx/wenku8/navigation/ReaderSystemIntegrationTest.kt
git diff --check --cached
git commit -m "feat(reader): adapt reader system integration"
```

## Task 15: Register The In-Host Reader Route And App Composition

**Depends on:** Tasks 10 and 14 plus the reviewed Phase 5 route contracts.

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/reader/src/main/java/org/mewx/wenku8/feature/reader/navigation/ReaderEntryPoints.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/AppRoute.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/AppDeepLink.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/Wenku8NavHost.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderCompatibilityLauncher.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderRouteCodec.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderRouteFactory.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderRoutePolicy.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderDocumentTokenRegistry.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/AppContainer.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/DefaultAppContainer.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/RouteViewModelFactory.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/navigation/ReaderRouteCodecTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/ReaderNavigationTest.kt`

- [ ] **Step 1: Write failing route, graph, and process-restoration tests**

Assert Phase 5 `NovelEntryPoints.openReader` reaches one NavHost destination; all five `ReaderOpenRequest` fields encode/decode through structured URI APIs; malformed/oversized/unknown enum input renders a visible argument error and performs zero repository work; Back returns to detail/catalog; process recreation restores the same route/chapter; reader repositories are singleton interfaces; image effects use Phase 5 `ImageOpenRequest`; and no reader callback starts another Activity except the temporary Phase 7 image compatibility destination.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.ReaderRouteCodecTest" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.ReaderNavigationTest :feature:novel:testDebugUnitTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: tests fail because `AppRoute.Reader` and the reader destination are absent.

- [ ] **Step 2: Add one primitive-only route case and codec**

Add to the existing sealed `AppRoute`:

```kotlin
data class Reader(
    val sourceId: String,
    val novelRemoteId: String,
    val volumeRemoteId: String,
    val chapterRemoteId: String,
    val sourceMode: ReaderSourceMode,
    val forceSavedPosition: Boolean,
) : AppRoute
```

`ReaderRouteCodec.encode(request)` builds the route with `Uri.Builder`, one encoded path segment per ID, and query parameters for enum/Boolean. `decode(uri)` caps each ID at 64 ASCII `[A-Za-z0-9_-]` characters, accepts only `CLOUD`/`LOCAL_CACHE_FIRST` and `true`/`false`, constructs the Phase 5 `ReaderOpenRequest`, and returns a typed `InvalidReaderRoute(fieldCode)` instead of throwing. It never accepts `VolumeList`, `ChapterInfo`, Bundle, serialized bytes, a URL, or a chapter body.

- [ ] **Step 3: Define feature host callbacks with the Phase 5 image type**

Create:

```kotlin
data class ReaderEntryPoints(
    val close: () -> Unit,
    val openImage: (ImageOpenRequest) -> Unit,
    val requestAssetDocument: (ReaderAssetKind) -> Unit,
    val applySystemUi: (ReaderSystemUiRequest) -> Unit,
)

data class ReaderSystemUiRequest(
    val appearance: ReaderAppearance,
    val chromeVisible: Boolean,
)
```

`ReaderRoute` converts `ReaderEffect.OpenImage` directly to `openImage(request)`. Phase 7 consumes the same `ImageOpenRequest`; Phase 6 does not define a competing image route type.

- [ ] **Step 4: Add route-flagged in-host dispatch without changing the release default early**

Keep the exact Phase 5 interface `ReaderCompatibilityLauncher.launch(activity, request)`. Add `RolloutReaderCompatibilityLauncher`, injected with `RouteFlagRepository`, `ReaderRoutePolicy`, a lifecycle-bound `ReaderRouteDispatcher`, and the retained Phase 5 `AndroidReaderCompatibilityLauncher` renamed `LegacyReaderCompatibilityLauncher`. Add fail-closed Gradle property `wenku8ReaderDefault`, accepting only `legacy`/`reader` and defaulting to `legacy` until Task 19, as BuildConfig `WENKU8_READER_DEFAULT`. Inside the supplied Activity's `lifecycleScope`, `ReaderRoutePolicy` honors `readerEnabled` only in debuggable builds and otherwise uses the build-time value: `reader` verifies the dispatcher belongs to that `Wenku8ShellActivity` and navigates with `ReaderRouteCodec.encode(request)`; `legacy` delegates once to the retained launcher. The reader branch constructs no Intent and fetches no catalog. Phase 5 `NovelEntryPoints.openReader` continues using the unchanged `ReaderOpenRequest` callback; tests cover both branches and reject unknown property values.

- [ ] **Step 5: Bind repositories, factory, picker tokens, and one destination**

Add only these interfaces to `AppContainer`:

```kotlin
val readerRepository: ReaderRepository
val readerProgressRepository: ReaderProgressRepository
val readerAssetRepository: ReaderAssetRepository
```

`DefaultAppContainer` constructs Task 4-6 implementations from the existing selected provider, Phase 3 stores/participant, settings repository, and a single `ReaderDocumentTokenRegistry`. The registry maps a cryptographically random opaque token to one `ContentResolver` URI, exposes it only through the Task 6 one-shot input resolver, removes it after open/failure, and clears all entries when the host is destroyed. URI values never enter logs, saved state, screenshots, or reports.

`Wenku8NavHost` registers exactly one reader destination on its existing controller. `ReaderRouteFactory` validates/decode results, constructs `ReaderViewModel` with repositories/dispatchers/clock/settings and `SavedStateHandle`, or renders the shared recoverable argument-error component with a Back action. It does not create a nested NavHost.

- [ ] **Step 6: Run navigation, architecture, and Phase 5 regression suites**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.*Reader*" --tests "org.mewx.wenku8.di.*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.ReaderNavigationTest :feature:reader:testDebugUnitTest :feature:novel:testDebugUnitTest :app:phase5NovelGate -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:tasks -Pwenku8Provider=public -Pwenku8ReaderDefault=unknown --console=plain --stacktrace --no-parallel
```

Expected: the first command passes: enabled detail/catalog -> reader -> Back stays in one shell Activity/NavHost, disabled comparison launches the retained reader once, release default remains legacy, and the Phase 5 gate stays green. The second command fails during configuration with the bounded `wenku8ReaderDefault must be legacy or reader` message.

- [ ] **Step 7: Commit in-host navigation**

```powershell
git add feature/reader/src/main/java/org/mewx/wenku8/feature/reader/navigation/ReaderEntryPoints.kt app/build.gradle app/src/main/java/org/mewx/wenku8/navigation app/src/main/java/org/mewx/wenku8/di app/src/test/java/org/mewx/wenku8/navigation/ReaderRouteCodecTest.kt app/src/androidTest/java/org/mewx/wenku8/navigation/ReaderNavigationTest.kt
git diff --check --cached
git commit -m "feat(app): register in-host reader route"
```

## Task 16: Preserve Old Reader Intent, Serializable, Deep-Link, And Back Contracts

**Depends on:** Task 15 and Phase 0 old-signed/minified evidence.

**Files:**
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/LegacyReaderEntryMapper.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/LegacyReaderCatalogSeed.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/ReaderCompatibilityTrampolineActivity.kt`
- Create: `studio-android/LightNovelLibrary/app/src/debug/java/org/mewx/wenku8/compat/ReaderCompatibilityFixtureActivity.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/debug/AndroidManifest.xml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderRouteFactory.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/compat/LegacyReaderEntryMapperTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/compat/OldReaderIntentCompatibilityTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/compat/OldReleaseReaderSerializableTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/compat/ReaderTrampolineBackTest.kt`

- [ ] **Step 1: Write failing receiver/default/typed-access/deep-link tests**

Use Phase 0's frozen sender/receiver rows and old-release fixture bytes. Cover V1/vertical/modern receiver kinds; missing, malformed, sentinel, and valid `aid`, `cid`, `from`, `forcejump`, `volume`, `volumes`; V1 empty-volume versus nullable receiver defaults; order/filtering; `fav`; API 23/32 untyped and API 33 typed access; minified old payload; invalid non-positive IDs; shell process recreation; a debug-only fixture subclass of the common trampoline; API 36 predictive Back; and caller -> fixture trampoline -> shell -> reader -> Back with no trampoline left on the stack. Task 19 repeats the same assertions against the three real frozen class names after parity-gated body replacement.

- [ ] **Step 2: Name the frozen decode values without changing bytes or defaults**

Add these app-owned compatibility types and retain Phase 0's one `serializable` helper:

```kotlin
enum class LegacyReaderReceiverKind { V1, VERTICAL, MODERN }

data class LegacyReaderIntentArguments(
    val aid: Int,
    val cid: Int,
    val from: String?,
    val forceSavedPosition: Boolean,
    val volume: VolumeList?,
    val volumes: List<VolumeList>,
)
```

`LegacyIntentCodec.reader(intent, kind)` remains the sole raw-extra reader. Missing `aid`/`cid` use `1`; exactly `from == "fav"` maps cache-first; exactly `forcejump == "yes"` maps true; V1 supplies the frozen empty `VolumeList` default; other receiver defaults follow the Phase 0 manifest; `volumes` filters `VolumeList` while preserving order and falls back to `volume` where the frozen receiver did. Do not change serialVersionUID, package/class name, public fields, R8 keep rules, or old fixture hashes.

- [ ] **Step 3: Map only to core-model identities and an optional catalog seed**

`LegacyReaderEntryMapper` uses the selected provider `SourceId`, creates `NovelKey(sourceId, aid.toString())` and `ChapterKey(novel, cid.toString())`, maps supplied `VolumeList`/`ChapterInfo` in order to core-model `Volume`/`ChapterSummary`, and returns:

```kotlin
data class LegacyReaderEntry(
    val rawAid: Int,
    val rawCid: Int,
    val request: ReaderOpenRequest?,
    val argumentErrorField: String?,
    val catalogSeed: ReaderCatalog?,
)
```

Non-positive IDs return `request=null` and a visible error field; they perform no repository call. Missing volume identity uses the nonblank diagnostic hint `legacy-unknown` without inventing a chapter or treating catalog completeness as known. The optional seed is process-restored only by re-decoding the original frozen Bundle in the shell Intent; it is never encoded into a public route or imported into durable catalog until repository validation succeeds.

Add `ReaderRouteCodec.encodeRaw(entry)`: it encodes the selected source and raw numeric ID strings into the same structured route without constructing `ReaderOpenRequest`; `decode` returns `InvalidReaderRoute` for non-positive legacy numeric IDs, so the route factory renders the visible error. A valid entry uses `encode(entry.request)` and never serializes its catalog seed into the URI.

- [ ] **Step 4: Add one forwarding compatibility shell**

Create:

```kotlin
abstract class ReaderCompatibilityTrampolineActivity : ComponentActivity() {
    protected abstract val receiverKind: LegacyReaderReceiverKind

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.getBoolean(KEY_FORWARDED) == true) { finish(); return }
        val owner = applicationContext as LegacyCompatibilityOwner
        val decoded = owner.legacyCompatibility.intentCodec.reader(intent, receiverKind)
        val entry = owner.legacyReaderEntryMapper.map(decoded)
        val target = Intent(this, Wenku8ShellActivity::class.java)
            .setData(owner.readerRouteCodec.encodeRaw(entry))
            .putExtra(EXTRA_READER_RECEIVER_KIND, receiverKind.name)
            .putExtra(EXTRA_READER_LEGACY_BUNDLE, Bundle(intent.extras ?: Bundle()))
        startActivity(target)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_FORWARDED, true)
        super.onSaveInstanceState(outState)
    }
}
```

The constants are app-private names defined in the same file. `ReaderRouteFactory` asks `LegacyIntentCodec` to decode the nested Bundle when present, supplies the validated catalog seed to the ViewModel initial state, and otherwise uses the primitive route. No feature reads the Bundle or legacy DTO.

`ReaderCompatibilityFixtureActivity` is a debug-only subclass selecting `MODERN`; the debug manifest declares it non-exported. It exists only to exercise the common forwarding/back/process implementation before Task 19 and is removed with the debug manifest row in Task 19.

- [ ] **Step 5: Preserve manifest/class/R8 identity during parallel rollout**

Keep the existing three Activity manifest names and exported policy. At this task they still own their old bodies for debug parity; add the common trampoline, debug fixture, codec/mapper/minified payload tests, and expected class-identity manifest assertions without replacing them. Task 19 performs body replacement and then launches the exact old class names in the externally signed/minified candidate.

- [ ] **Step 6: Run API 23/32/33/minified/process/back evidence**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.compat.LegacyReaderEntryMapperTest" :app:assembleAlphaRelease :app:assembleAlphaDebugAndroidTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Run `OldReaderIntentCompatibilityTest` and the debug-fixture `ReaderTrampolineBackTest` on API 23, 32, 33, and 36; run `OldReleaseReaderSerializableTest` against old fixture bytes in the externally signed/minified candidate. Expected: exact typed/untyped/default/order results pass, process restart rehydrates the seed, predictive Back does not duplicate navigation, and no debug-only round trip is accepted as proof that a real old class name forwards. Task 19 owns that final proof.

- [ ] **Step 7: Commit compatibility mapping and tests**

```powershell
git add app/src/main/java/org/mewx/wenku8/compat app/src/main/java/org/mewx/wenku8/navigation/ReaderRouteFactory.kt app/src/main/AndroidManifest.xml app/src/debug/java/org/mewx/wenku8/compat/ReaderCompatibilityFixtureActivity.kt app/src/debug/AndroidManifest.xml app/src/test/java/org/mewx/wenku8/compat/LegacyReaderEntryMapperTest.kt app/src/androidTest/java/org/mewx/wenku8/compat
git diff --check --cached
git commit -m "feat(reader): preserve legacy reader entries"
```

## Task 17: Prove Deterministic Visual, Adaptive, Locale, And Accessibility Coverage

**Depends on:** Tasks 11-16.

**Files:**
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/{build.gradle,src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt}`
- Modify: `docs/verification/{ui-golden-manifest,manual-assistive-technology-manifest}.yaml`
- Create: `docs/verification/manual-a11y-phase6.md`
- Create: `docs/verification/phase-6-visual-review.md`
- Create: `studio-android/LightNovelLibrary/feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/ReaderVisualMatrixTest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/screenshot/Phase6ReaderGoldenTest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/accessibility/Phase6ReaderAccessibilityTest.kt`

- [ ] **Step 1: Add failing manifest-completeness and semantics tests**

Require real rows for paginated and continuous crossed with day/night/e-ink and font 1.0/2.0; catalog Sheet/pane; image pending/cached/broken; first/last chapter; separating/occluding hinge; 360x640, 412x915, 915x412, 800x1280, and 1280x800; Simplified, Taiwan Traditional, and Hong Kong fallback; large display size; gesture/three-button navigation; animations on/off; loading/empty/error/offline/stale; and API 36 predictive states. Each row must contain a synthetic fixture hash, baseline hash, tolerance, allowed dynamic masks, approval commit, route/owner ID, and reader mode.

Run:

```powershell
.\gradlew.bat :verification-tools:verifyUiGoldenManifest -Pphase=6 -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL naming each missing stable Phase 6 case; a raw screenshot count is not a pass.

- [ ] **Step 2: Register deterministic recorder/verifier tasks**

Use only the Phase 1 AndroidX app-instrumentation pipeline:

```groovy
// app/build.gradle
registerUiGoldenTask('recordPhase6UiGoldens', 'record', 6,
    'org.mewx.wenku8.screenshot.Phase6ReaderGoldenTest')
registerUiGoldenTask('verifyPhase6UiGoldens', 'verify', 6,
    'org.mewx.wenku8.screenshot.Phase6ReaderGoldenTest')

// verification-tools/build.gradle
tasks.register('verifyPhase6AssistiveEvidence', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyAssistiveEvidence', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath, '6'
}
```

The recorder uses only synthetic chapter text/images, writes original PNGs plus canonical JSON and lowercase SHA-256 to candidate output, and never edits approved baselines. The verifier rejects zero/sentinel hashes, unapproved candidates, missing originals, unexplained mask/tolerance changes, wrong fixture hash, stale approval commit, and blank/flat images.

The shared `tools/verification/run-androidx-ui-goldens.ps1` reads Phase 6 rows from the structured manifest, rejects a physical device, verifies the pinned API 36 emulator/system-image identity, invokes the exact app test class for each viewport/locale/theme/font/display/navigation/animation/posture row, pulls originals and semantics JSON under `app/build/reports/ui-goldens/6/`, and compares reported hashes. No Phase 6-specific recorder or baseline format is created.

- [ ] **Step 3: Complete automated visual and semantic assertions**

`ReaderVisualMatrixTest` asserts physical bounds, no text/control overlap, no horizontal overflow/double scroll, bounded readable width, hinge exclusion, Sheet/pane exclusivity, font-scale survival, custom-background contrast, image-state dimensions, stable page slot, and correct selected/current/focus state. `Phase6ReaderAccessibilityTest` asserts role/label/heading/state description/collection/progress/error/live region/traversal order, 48dp targets, content-before-show-chrome action, concise page/chapter announcement, keyboard/DPAD behavior, the semantics/action surface required by Switch Access, Sheet/Dialog focus return, and API 36 predictive cancellation/commit. Only the manual rows below prove enabled TalkBack/Switch Access operation.

- [ ] **Step 4: Run and record manual assistive journeys**

Record tester, date, device, API, locale, navigation mode, and result for these IDs:

```text
P6-A31 TalkBack open reader -> content -> show chrome -> catalog -> current chapter -> close
P6-A32 TalkBack page/viewport -> previous/next chapter -> first/last boundary
P6-A33 Keyboard and DPAD search -> next/previous match -> seek confirmation -> settings
P6-A34 Switch Access mode/appearance/font -> image retry/open -> Back recovery
P6-A35 TalkBack expanded/fold catalog pane -> resize to Sheet -> focus return
```

An unrun, failed, stale, self-approved, or unhashed journey blocks Task 19. Every TalkBack/Switch Access result is added to `manual-assistive-technology-manifest.yaml` using the shared service/device/configuration/source/APK/report schema; evidence never contains chapter bodies or user/account values.

- [ ] **Step 5: Capture and inspect the complete matrix**

```powershell
.\gradlew.bat :app:recordPhase6UiGoldens -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
$sourceCommit = (git rev-parse HEAD).Trim()
.\gradlew.bat :verification-tools:approveUiGoldens -Pphase=6 "-PuiGoldenReviewer=$env:WENKU8_UI_REVIEWER" "-PuiGoldenSourceCommit=$sourceCommit"
.\gradlew.bat :app:verifyPhase6UiGoldens :verification-tools:verifyUiGoldenManifest -Pphase=6 -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Inspect original pixels at 100% for CJK line breaks, clipping, overlap, tiny controls, wrong mode/appearance/state, image crop, background contrast, hinge crossing, focus presentation, system-bar collision, and blank rendering. Human approval writes real baseline hashes and commit; no unexplained baseline replacement passes.

- [ ] **Step 6: Run accessibility/visual suites and commit approved evidence**

```powershell
Set-Location 'studio-android\LightNovelLibrary'
.\gradlew.bat :feature:reader:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.reader.ReaderVisualMatrixTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.accessibility.Phase6ReaderAccessibilityTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :verification-tools:verifyPhase6AssistiveEvidence :app:verifyPhase6UiGoldens :verification-tools:verifyUiGoldenManifest -Pphase=6 -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: all automated and approved manual/golden rows pass.

```powershell
git add feature/reader/src/androidTest/java/org/mewx/wenku8/feature/reader/ReaderVisualMatrixTest.kt app/src/androidTest/java/org/mewx/wenku8/accessibility/Phase6ReaderAccessibilityTest.kt app/src/androidTest/java/org/mewx/wenku8/screenshot/Phase6ReaderGoldenTest.kt ../../docs/verification/ui-goldens ../../docs/verification/ui-golden-manifest.yaml ../../docs/verification/manual-assistive-technology-manifest.yaml ../../docs/verification/manual-a11y-phase6.md ../../docs/verification/phase-6-visual-review.md
git diff --check --cached
git commit -m "test(reader): approve visual accessibility matrix"
```

## Task 18: Prove Complete Reader Runtime, Offline, Progress, And Process-Death Journeys

**Depends on:** Task 17 and Phase 2/3 deterministic fixtures.

**Files:**
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/journey/Phase6ReaderJourneyTest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/journey/Phase6ReaderProcessStageATest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/journey/Phase6ReaderProcessStageBTest.kt`
- Create: `studio-android/LightNovelLibrary/core/testing/src/main/java/org/mewx/wenku8/core/testing/reader/SyntheticReaderJourneyFixture.kt`
- Create: `tools/run-phase6-reader-journey.ps1`
- Create: `docs/verification/phase-6-reader-journey-matrix.yaml`

- [ ] **Step 1: Write failing end-to-end journey assertions**

Cover: search/detail/catalog -> reader -> progress restore; local/cache-first read; remote fixture read -> persisted cache -> network disabled -> offline read after process restart; missing/non-positive argument; first/last chapter boundaries; ordered text/image pending/cached/broken/retry/open; paginated/continuous switch; typography/margin reflow; day/night/e-ink/custom asset; rotation/resize/fold; process death with catalog/search/settings overlay intent; saved progress; terminal completion clear; and rollback adapter reading projected progress/settings with canonical access disabled.

- [ ] **Step 2: Add one synthetic fixture graph**

`SyntheticReaderJourneyFixture` defines one novel, two ordered volumes, three chapters, text/image/semantic blocks, one valid image, one delayed image, one broken image, local cached bytes, and typed provider failures. It exposes only synthetic titles/body bytes and stable hashes. It can count provider/store/progress calls and disable network deterministically; it has no live host or account capability.

- [ ] **Step 3: Implement Stage A/B process evidence**

Stage A records the initial local `READER_ENABLED` flag, enables it through `RouteFlagRepository` in the debug harness, launches the reader, selects the second chapter, switches continuous mode, changes typography, opens catalog or search according to test parameter, scrolls to a known logical cursor, waits for canonical progress commit, records only IDs/cursor/settings versions/file hashes under `files/phase6-harness/pre.json`, and exits the instrumentation process. Stage B starts in a new process, asserts route/chapter/cursor/mode/logical overlay restoration, disables network, verifies cached text/image, advances to terminal completion, restores the initial route flag, and records canonical/legacy hashes and completion state in `post.json`.

- [ ] **Step 4: Implement the emulator-only host harness**

`tools/run-phase6-reader-journey.ps1` validates the target is an emulator, accepts only API `23,32,33,36`, installs the matching debug/test APKs, clears only the harness package data before seed, runs Stage A, captures pre-report and hashes, executes `adb shell am force-stop org.mewx.wenku8`, runs Stage B in a fresh process, pulls post-report, and verifies expected IDs/versions/hashes. API 32/33 also run old Serializable access; API 36 runs predictive Back. The script uses argument arrays, rejects physical devices, and writes reports under `build/reports/phase6/<api>/reader-journey/`.

- [ ] **Step 5: Run focused runtime and four-API process journeys**

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.journey.Phase6ReaderJourneyTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

From the repository root run:

```powershell
Set-Location '..\..'
& .\tools\run-phase6-reader-journey.ps1 -Api 23
& .\tools\run-phase6-reader-journey.ps1 -Api 32
& .\tools\run-phase6-reader-journey.ps1 -Api 33
& .\tools\run-phase6-reader-journey.ps1 -Api 36
```

Expected: each retained report proves no external network, ordered content, cache persistence, exact cursor/settings restoration, one canonical progress mutation, caught-up legacy projection, and terminal clear only at the known final cursor.

- [ ] **Step 6: Run old-install/new-reader/rollback read evidence**

In the external signed fixture job, install the old signed/minified release with synthetic old progress/settings and downloaded chapter/image fixtures, upgrade to the signed candidate, read/advance/complete in the new reader, then install the higher-version signed rollback fixture and read projected progress/settings/content with canonical access disabled. Retain APK/certificate/mapping/fixture/report hashes only. A debug signature or new-to-new serialization cannot pass.

- [ ] **Step 7: Verify privacy, egress, and commit journey sources/matrix**

Run Phase 0 secret/log/egress scanners over APKs, screenshots, journey reports, saved-state dump, notification/work databases, and logcat. Expected: no credential/Cookie/captcha/private endpoint/raw chapter body and no unapproved destination.

```powershell
git add studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/journey studio-android/LightNovelLibrary/core/testing/src/main/java/org/mewx/wenku8/core/testing/reader tools/run-phase6-reader-journey.ps1 docs/verification/phase-6-reader-journey-matrix.yaml
git diff --check --cached
git commit -m "test(reader): prove runtime restoration journeys"
```

## Task 19: Retire Old Reader Pages And Legacy Deck Only After Parity Gates

**Depends on:** Tasks 16-18 and current Phase 0 license/compatibility evidence.

**Files:**
- Create: `tools/verify-phase6-reader-retirement.ps1`
- Create: `docs/verification/phase-6-reader-retirement.yaml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/VerticalReaderActivity.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/activity/Wenku8ReaderActivityV1.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/activity/ModernReaderActivity.kt`
- Delete: `studio-android/LightNovelLibrary/app/src/debug/java/org/mewx/wenku8/compat/ReaderCompatibilityFixtureActivity.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/debug/AndroidManifest.xml`
- Delete: reader-only production directories and resources listed under `Reader-Only Retirement Targets`
- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/ReaderRoutePolicy.kt`
- Modify: `docs/verification/ui-owner-action-ledger.yaml`
- Modify: `docs/licenses/source-asset-provenance.yaml`
- Regenerate: `NOTICE`, `SOURCE_OFFER.md`, and `studio-android/LightNovelLibrary/app/src/main/res/raw/notice.txt`
- Test: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/compat/RetiredReaderTrampolineTest.kt`

- [ ] **Step 1: Add a fail-closed preflight/post-removal verifier**

The PowerShell verifier has `-Mode Preflight` and `-Mode PostRemoval`. Preflight requires current-source hashes for Tasks 16-18, approved UI manifest/manual journeys, all runtime API reports, old-signed/minified report, exact rollback read, Phase 0-5 gates, zero open Critical/Important findings, and zero unknown/missing/incompatible packaged license. It fails when a report commit differs from `HEAD`, a hash/report is absent, a baseline is unapproved, or a protected/release scope is claimed without current acceptance.

PostRemoval requires the old three class files to be thin trampoline subclasses; rejects `setContent`, `setContentView`, layout inflation, ViewModel/business work, provider/storage/file imports, executor/thread/Handler, or raw extras; requires all listed reader-only directories/layouts/menu and all Phase 0 `LegacyReaderPageDeck` bytecode/resources absent; rejects any selectable V1/vertical/temporary-reader action; and verifies the three class names/manifest/R8/old Intent fixtures remain.

- [ ] **Step 2: Run preflight before touching old reader source**

```powershell
& .\tools\verify-phase6-reader-retirement.ps1 -Mode Preflight
```

Expected: `PHASE6-READER-RETIREMENT-PREFLIGHT-PASS`. Any denied item stops the task; do not weaken the verifier or record a waiver as parity.

- [ ] **Step 3: Replace page owners with frozen-name trampolines**

Replace each original class body with only its receiver identity:

```kotlin
class Wenku8ReaderActivityV1 : ReaderCompatibilityTrampolineActivity() {
    override val receiverKind = LegacyReaderReceiverKind.V1
}

class VerticalReaderActivity : ReaderCompatibilityTrampolineActivity() {
    override val receiverKind = LegacyReaderReceiverKind.VERTICAL
}

class ModernReaderActivity : ReaderCompatibilityTrampolineActivity() {
    override val receiverKind = LegacyReaderReceiverKind.MODERN
}
```

Keep each existing package/class name. Keep manifest components non-exported according to the Phase 0 policy. These are compatibility entry shells, not reachable product pages; class-identity removal remains Phase 8.

- [ ] **Step 4: Delete only proven reader-page implementation/resources**

Delete every exact retirement target listed near the start of this plan, including Phase 0 `reader/legacydeck/`, after the source/reachability verifier proves zero remaining imports. Delete the old Activity-owned modern helper files but retain the rewritten `ModernReaderActivity.kt`. Delete the Task 16 debug fixture subclass and its debug-manifest row now that the real class-name trampolines own those tests. Remove V1/vertical/temporary choices and the retained `LegacyReaderCompatibilityLauncher` branch from all Phase 5 launch planners, menus, dialogs, and UI-owner actions; after this point `ReaderCompatibilityLauncher` dispatches the in-host route for either flag value while release defaults are explicitly true. Do not delete `VolumeList`, `ChapterInfo`, custom assets, old progress/settings files, chapter/image caches, legacy codecs, projections, or the image viewer.

- [ ] **Step 5: Set the reviewed reader route default and update ledgers**

Change `wenku8ReaderDefault`'s absent-property default from `legacy` to `reader` only after preflight, and make `ReaderRoutePolicy` route both debug flag values to the reader once the legacy branch is removed. Reject explicit `legacy` in the post-retirement source with a bounded configuration error that points to the retained pre-retirement commit; an emergency legacy build comes from that reviewed commit as a higher-version signed forward rollback, not hidden deleted code. Update A08/A09 to retired page owners with class-name trampoline evidence, A10 to in-host Reader plus compatibility trampoline, and map every old visible action to a passing new test ID. Generate `phase-6-reader-retirement.yaml` from actual source/report/deleted-path hashes; the generator rejects sentinel values and records the preflight source commit plus staged retirement diff hash.

- [ ] **Step 6: Regenerate license/SBOM/notices and verify packaged bytecode**

```powershell
Set-Location 'studio-android\LightNovelLibrary'
.\gradlew.bat :app:assembleAlphaDebug :verification-tools:generateSbom :verification-tools:generateNotices :verification-tools:verifyPackagedLicenses -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
```

Expected: zero original SlidingLayout, `LegacyReaderPageDeck`, V1/vertical page/helper, or temporary Modern Reader page bytecode/resources; packaged source/assets have zero unknown/incompatible license and generated notice/source-offer hashes match.

- [ ] **Step 7: Run post-removal compatibility, navigation, and source gates**

```powershell
& .\tools\verify-phase6-reader-retirement.ps1 -Mode PostRemoval
Set-Location 'studio-android\LightNovelLibrary'
.\gradlew.bat :app:testAlphaDebugUnitTest :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.compat.RetiredReaderTrampolineTest,org.mewx.wenku8.compat.OldReaderIntentCompatibilityTest,org.mewx.wenku8.navigation.ReaderNavigationTest :feature:reader:testDebugUnitTest :feature:reader:connectedDebugAndroidTest :app:assembleAlphaRelease :app:assembleAlphaDebugAndroidTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:tasks -Pwenku8Provider=public -Pwenku8ReaderDefault=legacy --console=plain --stacktrace --no-parallel
```

Expected: the first Gradle command passes: old explicit class Intents forward once to the new route, no old page is selectable/reachable, and Back/process behavior remains correct. The explicit post-retirement `legacy` property command fails with the reviewed retirement message. When the exact release scope is accepted, externally sign the minified candidate and repeat Phase 0's old-signed probe against all three real class names on API 23/32/33; without that accepted scope/evidence, retirement remains blocked rather than substituting debug output.

- [ ] **Step 8: Commit parity-gated retirement**

Run from the repository root:

```powershell
git add tools/verify-phase6-reader-retirement.ps1 docs/verification/phase-6-reader-retirement.yaml docs/verification/ui-owner-action-ledger.yaml docs/licenses/source-asset-provenance.yaml NOTICE SOURCE_OFFER.md studio-android/LightNovelLibrary/app
git diff --check --cached
git commit -m "refactor(reader): retire legacy reader pages"
```

## Task 20: Bind The Full Phase 6 Evidence Matrix And Exit Gate

**Depends on:** Tasks 1-19.

**Files:**
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/reader/Phase6ReaderEvidenceVerifier.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/reader/Phase6ReaderEvidenceVerifierTest.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `docs/verification/coverage-manifest.yaml`
- Modify: `docs/verification/modernization-matrix.yaml`
- Modify: `docs/verification/phase-6-reader-retirement.yaml`

- [ ] **Step 1: Write a failing exhaustive evidence verifier**

Require exact IDs for entry contract, model ownership, content policies, both modes, deterministic pagination/continuous mapping/search/seek, images, settings/assets, progress/completion/dual projection, process restoration, accessibility/input, adaptive/hinge/system/back, old Intent/Serializable/minified APIs, visual cases, runtime journeys, rollback read, retirement, license/SBOM/notice, privacy/egress, coverage, public/private graph, and three independent review domains. Every row requires provider, variant, API/configuration, test/command, fixture/baseline/report path, lowercase SHA-256, source commit, and status. Reject `NOT_RUN`, indirect evidence, stale commit, missing report, sentinel hash, or a green task not mapped to the requirement.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.reader.Phase6ReaderEvidenceVerifierTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL naming unmapped Phase 6 requirement IDs.

- [ ] **Step 2: Update coverage ownership and thresholds**

Include all reader repository/use-case/ViewModel/paging/reducer/input logic and retained compatibility mapping. Exclude only exact generated R/BuildConfig/Compose/Room patterns already approved by Phase 0. Enforce at least 80% line/70% branch for repository/use-case/ViewModel packages and 70%/60% overall; a strong paginator cannot hide a weak ViewModel/repository package.

- [ ] **Step 3: Register `phase6ReaderGate` with real dependencies**

Register the verifier dispatcher and canonical app aggregate with these exact task paths:

```groovy
// verification-tools/build.gradle
tasks.register('verifyPhase6ReaderEvidence', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyPhase6ReaderEvidence', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}

// app/build.gradle
def repositoryRoot = rootProject.projectDir.parentFile.parentFile
tasks.register('verifyPhase6ReaderRetirementPlan', Exec) {
    group = 'verification'
    workingDir repositoryRoot
    commandLine rootProject.ext.resolvePowerShell(), '-NoProfile', '-NonInteractive',
        '-File', new File(repositoryRoot, 'tools/verify-phase6-reader-retirement.ps1').absolutePath,
        '-Mode', 'PostRemoval'
}

tasks.register('phase6ReaderGate') {
    group = 'verification'
    description = 'Canonical Phase 6 reader aggregate gate.'
    dependsOn ':phase0Gate', ':verification-tools:phase1Gate',
        ':verification-tools:phase2Gate', ':verification-tools:phase3Gate',
        ':app:phase4LibraryGate', ':app:phase5NovelGate'
    dependsOn ':core:model:test', ':core:domain:test',
        ':core:storage:testDebugUnitTest', ':core:data:testDebugUnitTest'
    dependsOn ':feature:reader:testDebugUnitTest', ':feature:reader:connectedDebugAndroidTest',
        ':app:verifyPhase6UiGoldens',
        ':verification-tools:verifyPhase6AssistiveEvidence'
    dependsOn 'testAlphaDebugUnitTest', 'connectedAlphaDebugAndroidTest',
        'lintAlphaDebug', 'assembleAlphaRelease'
    dependsOn ':verifyPhase0Coverage', ':verifyArchitecture',
        ':verification-tools:verifyUiGoldenManifest',
        ':verification-tools:verifyXmlSurfaceLedger',
        ':verification-tools:verifyPlannedGradleTasks',
        ':verification-tools:verifyInventories',
        ':verification-tools:verifyPackagedLicenses',
        ':verification-tools:verifySensitiveSource',
        ':verification-tools:verifyOutboundManifest',
        ':verification-tools:verifyPhase6ReaderEvidence'
    dependsOn 'verifyPhase6ReaderRetirementPlan'
}
```

Add the dispatcher branch in `VerificationMain.kt`:

```kotlin
if (command == "verifyPhase6ReaderEvidence") {
    org.mewx.wenku8.verification.reader.Phase6ReaderEvidenceVerifier.verify(projectRoot, docsRoot)
    return
}
```

`:app:phase6ReaderGate` is the sole Phase 6 aggregate path; do not invent `phase6Gate`. It must not silently skip unavailable device/external evidence or substitute private-provider success for public completion. The evidence verifier requires old-signed compatibility, four-API journeys, retirement, license/SBOM/notice, and current report hashes even where those prerequisites are generated outside one Gradle invocation.

- [ ] **Step 4: Run the complete deterministic public graph**

```powershell
.\gradlew.bat clean :core:model:test :core:domain:test :core:storage:testDebugUnitTest :core:data:testDebugUnitTest :feature:reader:testDebugUnitTest :feature:reader:connectedDebugAndroidTest :app:verifyPhase6UiGoldens :app:testAlphaDebugUnitTest :app:connectedAlphaDebugAndroidTest :app:lintAlphaDebug :app:assembleAlphaDebug :app:assembleBaiduDebug :app:assemblePlaystoreDebug phase0CoverageReport verifyPhase0Coverage :verification-tools:verifyPhase6AssistiveEvidence :verification-tools:verifyUiGoldenManifest :app:phase6ReaderGate -Pphase=6 -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: deterministic debug/module/UI/coverage/evidence gates pass with zero lint errors and no stale report. Run Task 18 host journeys separately on API 23/32/33/36 and re-run the evidence verifier after reports are retained.

- [ ] **Step 5: Re-run minified/public-private/authorization gates**

Build public minified `alphaRelease`, `baiduRelease`, and `playstoreRelease` only when their exact current authorization/channel rows permit them; otherwise each fails closed and no release-complete claim is made. The protected job separately runs private debug/minified reader/provider/compatibility/egress/license gates and returns only the fresh bound redacted attestation. Run unknown/private-without-source negative configuration tests.

```powershell
.\gradlew.bat :app:assembleAlphaRelease :app:assembleBaiduRelease :app:assemblePlaystoreRelease -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat projects -Pwenku8Provider=unknown --console=plain --stacktrace --no-parallel
.\gradlew.bat projects -Pwenku8Provider=private --console=plain --stacktrace --no-parallel
```

Expected: each authorized public release passes and each unauthorized channel fails its scoped gate; unknown provider and private-without-source fail configuration with bounded redacted messages.

- [ ] **Step 6: Dispatch independent Phase 6 reviews and resolve findings**

Dispatch separate reviewers for:

1. architecture/model ownership/provider-data-feature boundaries and single-NavHost state/effects;
2. progress/settings/assets/migration/old-signed/rollback/non-deletion compatibility;
3. Compose Material 3/adaptive/system UI/accessibility/visual/runtime parity;
4. retirement/reachability/licensing/SBOM/notices/privacy/egress/evidence completeness.

Each reviewer reads source/diffs/reports directly. Resolve every Critical and Important finding, rerun affected focused and aggregate commands, and bind the reviewed commit. Agent summaries alone are not evidence.

- [ ] **Step 7: Verify the final retirement commit and matrix**

Run from the repository root:

```powershell
& .\tools\verify-phase6-reader-retirement.ps1 -Mode PostRemoval
Set-Location 'studio-android\LightNovelLibrary'
.\gradlew.bat :verification-tools:verifyPhase6ReaderEvidence :verification-tools:verifyUiGoldenManifest -Pphase=6 -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
git diff --check
git status --short
```

Expected: the retirement YAML final commit/tree/diff/report hashes reconcile, all matrix paths exist, and only intentional Task 20 files are staged.

- [ ] **Step 8: Commit the aggregate gate and reviewed evidence mappings**

Run from the repository root:

```powershell
git add studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/reader/Phase6ReaderEvidenceVerifier.kt studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/reader/Phase6ReaderEvidenceVerifierTest.kt studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt studio-android/LightNovelLibrary/verification-tools/build.gradle studio-android/LightNovelLibrary/app/build.gradle docs/verification/coverage-manifest.yaml docs/verification/modernization-matrix.yaml docs/verification/phase-6-reader-retirement.yaml
git diff --check --cached
git commit -m "test(reader): bind phase six exit gate"
```

## Phase 6 Completion Checklist

- [ ] Phase 5 `ReaderOpenRequest`, `ReaderSourceMode`, `NovelEntryPoints.openReader`, `ImageOpenRequest`, and `ReaderCompatibilityLauncher` identities remain compatible; stable content identities live only in `core.model.identity`/other `core.model` packages.
- [ ] One Navigation Compose host owns the reader route; feature code imports no provider/storage/app/legacy implementation and ViewModel owns immutable state/effects/structured concurrency.
- [ ] Paginated and continuous modes preserve ordered text/images, logical cursor, overlap/reduced-motion behavior, search, seek, catalog, and first/last boundaries.
- [ ] Local/cache-first, remote-with-cache, stale, offline, empty, error, retry, image states, process death, rotation, resize, and fold journeys pass.
- [ ] Progress save/restore/completion uses Phase 3 canonical/journal/projection semantics; old formats and old-signed rollback read pass without non-secret deletion.
- [ ] Reader settings remain canonical/legacy-compatible; day/night/e-ink are independent; custom font/background validation/import/reset and failures are recoverable.
- [ ] Visible controls, gestures, optional tap zones/volume keys, keyboard, DPAD, TalkBack, and Switch Access all reach page/viewport/chapter/catalog/settings/image workflows.
- [ ] Edge-to-edge, cutouts, system bars, safe/IME insets, compact height, hinge regions, Sheet/pane remap, focus, and API 36 predictive Back pass.
- [ ] Old V1/vertical/temporary Modern Reader product pages and selectable entries are retired; original class names are non-exported codec-driven trampolines pending Phase 8 compatibility-window approval.
- [ ] Phase 0 `LegacyReaderPageDeck`, original SlidingLayout bytecode, reader-only page helpers/XML/menu, and any unknown-license replacement residue are absent from packaged artifacts after current license/SBOM/notice verification.
- [ ] Approved real-hash screenshots/manual accessibility evidence cover all mandatory viewports, modes, appearances, font scales, locales, image/boundary/catalog/hinge states.
- [ ] API 23/32/33/36 runtime, old Intent/Serializable/minified, process-death, offline, completion, and rollback evidence is current and hash-bound.
- [ ] Coverage, lint, architecture, privacy, egress, public/private, authorization, and Phase 0-6 aggregate gates pass or explicitly fail closed where external authorization is absent.
- [ ] No Critical or Important independent review finding remains; `git diff --check` is silent and every task has an isolated reviewed commit.

## Deliberate Deferrals

- Phase 7 owns the final Compose image viewer using the already stable `ImageOpenRequest`; Phase 6 keeps the reviewed `ImageCompatibilityLauncher`/`path` fallback and tests reader-to-viewer navigation only.
- Phase 7 owns account, application settings, cache, migration UI, wallpaper, notice, and about routes. Reader-local settings in this plan do not claim those pages.
- Phase 8 decides when the three reader trampoline class identities, frozen Serializable DTOs/R8 rules, logical legacy API bridge, legacy reads/dual writes, and other unreachable legacy UI may be physically removed after the compatibility window.
- Live content/account testing and distributable release claims remain outside deterministic Phase 6 completion unless their exact current site/content/channel scopes are accepted.

## Execution Handoff

Plan complete at `docs/superpowers/plans/2026-07-10-wenku8-phase-6-reader-consolidation.md`. Execute in an isolated worktree with `superpowers:subagent-driven-development`, a fresh implementation worker per task, specification review followed by code-quality review per task, root diff inspection, and fresh verification before each commit is accepted.
