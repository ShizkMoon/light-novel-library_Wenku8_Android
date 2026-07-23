# Wenku8 Phase 5 Novel, Downloads, and Community Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver adaptive Compose Material 3 novel detail, catalog, favorite, durable download selection, review list/thread/create/reply routes and reduce the original NovelInfo/review Activity class names to compatibility trampolines after parity evidence passes.

**Architecture:** Add `:feature:novel` over platform-neutral `:core:model` values and `:core:domain` repositories. `:core:data` maps the Phase 2 provider facets into the single Phase 1-owned core model identities, coordinates the Phase 3 catalog/user stores and bounded work protocol, and owns cache/offline/invalidation behavior; ViewModels own route state and effects. `:app` owns typed navigation plus old Intent/Serializable and reader/image/account compatibility launchers. Retained legacy implementations remain non-default rollback entries for the compatibility window.

**Tech Stack:** Kotlin, coroutines, ViewModel, SavedStateHandle, StateFlow, Room, Phase 3 WorkManager/JobScheduler scheduler and durable journals, Jetpack Compose Material 3, Navigation Compose, adaptive/window posture APIs, JUnit4, MockWebServer/provider fakes, AndroidX Compose UI test, deterministic screenshot verification, PowerShell, Gradle.

---

## Scope Boundary

This phase owns UI-owner ledger rows A04-A07 and the detail/catalog/download/community actions previously orchestrated by `NovelInfoActivity`. It installs these route IDs:

- `novel/detail`
- `novel/catalog`
- `novel/downloads/select`
- `novel/reviews`
- `novel/reviews/create`
- `novel/reviews/thread`
- `novel/reviews/reply`

The single source of stable identities/content models remains `org.mewx.wenku8.core.model.*`, as fixed by the plan index and Phase 1. `:api-contract` contains result/request/facet types that import those models. This plan must not create `SourceId`, `NovelKey`, `NovelDetail`, `Volume`, `ChapterSummary`, `ReviewKey`, `ReviewSummary`, or `ReviewPost` under `org.mewx.wenku8.api.contract` or a feature package.

Phase 5 does not consolidate reader internals, retire Modern/V1/vertical reader entries, implement the final image viewer, or replace account/login UI. It defines stable `ReaderOpenRequest` and `ImageOpenRequest` entry values. `ReaderCompatibilityLauncher` forwards the former through the frozen `aid`/`cid`/`from`/`forcejump`/`volume`/`volumes` Serializable contract until Phase 6 consumes the same type in the final reader route. `ImageCompatibilityLauncher` forwards a materialized local path through the frozen `path` contract until Phase 7.

## Working Directory and Command Rules

Run Gradle commands from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android\studio-android\LightNovelLibrary'
```

Run repository verifiers from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android'
```

Never place credentials, Cookies, captcha values, review drafts, account/profile values, response bodies, query values, private endpoints, or signing material in commands, fixtures, logs, screenshots, reports, WorkManager Data, JobInfo extras, notifications, or commits. Review mutations use deterministic fixtures by default. Any live mutation remains separately gated by the Phase 0/2 authorization and per-run confirmation policy.

## Prerequisites and Stop Conditions

### Required Before Task 1

- [ ] `:app:phase4LibraryGate` passes for public provider, and protected private CI supplies a fresh bound redacted attestation for the same source/variant policy revision.
- [ ] The Phase 4 shell is the reviewed default, its explicit `legacy` forward-fix selection passes, and A01-A03/F01-F05 ledger rows have accepted evidence.
- [ ] Phase 2 provider exposes detail/catalog, binary, recommendation, review list/thread/create/reply, and bookshelf operations through typed facets over `:core:model` values.
- [ ] Phase 3 canonical catalog/user databases, migration journal, `ChunkBudget`, durable transfer lease/checkpoint protocol, scheduler, worker factory, and process/reboot harness pass.
- [ ] Phase 0 old-signed/minified Intent/Serializable fixtures cover `NovelInfoActivity`, all three review Activities, `VolumeList`, and `ChapterInfo` on API 23/32/33.
- [ ] Site/content/live/release authorization and license/egress gates remain current for every operation/channel used by this phase.

Run:

```powershell
.\gradlew.bat phase0Gate :verification-tools:phase1Gate :verification-tools:phase2Gate :verification-tools:phase3Gate :app:phase4LibraryGate -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: `BUILD SUCCESSFUL`; no provider capability needed by a reachable route is silently substituted by a stub.

### Stop Immediately When

- Any feature source imports a provider, legacy API, network, parser, Room DAO/entity, DataStore implementation, file API, `GlobalConfig`, or another feature ViewModel.
- An API-contract duplicate of a Phase 1 core model is introduced or a core identity changes package/class/field compatibility.
- A composable/ViewModel starts an Activity, constructs an Intent, parses HTML/XML, reads/writes a file, or owns a background executor.
- `NovelInfoActivity` or a review compatibility class reads raw extras outside the one `LegacyIntentCodec`.
- A missing/non-positive internal novel ID reaches a repository. Compatibility trampolines alone preserve the frozen default `aid=1`/`rid=1` behavior.
- Detail/catalog stale fallback overwrites known-good durable data after a parse/server failure.
- Favorite state changes local/account partitions incorrectly, deletes downloaded content on membership removal, or bypasses Phase 3 journal/projection.
- Download selection cannot resume after process/reboot, retries a mutation, loses a checkpoint, writes a partial chapter/image over known-good content, or commits terminal success more than once.
- A review form drops input after validation/network/auth/server failure, permits duplicate submission, automatically retries a POST, or logs/persists draft/body outside SavedStateHandle process state.
- Expanded detail/catalog content crosses a hinge, compact Sheet and expanded pane coexist, or Back closes an always-present expanded pane.
- Any old implementation is deleted or made unreachable for rollback before the declared compatibility-window retirement approval.
- A route/default/retirement gate is claimed with an unresolved Critical/Important independent review finding.

## Phase Exit Conditions

- Detail loads local/cache/remote content with explicit loading/content/empty/offline/auth/error states; title, author, status, tags, introduction, favorite, recommendation capability, catalog, reviews, cover action, and latest/selected chapter actions have parity evidence.
- Compact detail uses one pane plus catalog/download Sheets/routes; expanded uses hinge-safe detail/catalog panes and preserves selection/focus through resize/process recreation.
- Favorite changes are journaled and rollback-readable; account membership is capability/auth/session-epoch safe.
- Download selection supports volume/chapter selection, skip-existing/update/force policies, images preference, durable progress, visible cancel/retry, process death, force-stop/reboot recovery, and exactly one terminal commit.
- Review list/thread paging covers loading/content/empty/stale/offline/auth/error/end; create/reply validation and failures retain input and permit explicit retry without automatic POST replay.
- Old `NovelInfoActivity`, `NovelReviewListActivity`, `NovelReviewNewPostActivity`, and `NovelReviewReplyListActivity` names decode through `LegacyIntentCodec`, forward to one typed route, and finish; retained `Legacy*` implementations remain rollback-only.
- Active `NovelInfoActivity` orchestration has zero network/parser/cache/file/download/AsyncTask/thread/handler responsibilities. Active Phase 5 UI owns zero `findViewById`, page XML, AppCompat Toolbar, old CardView, or direct `GlobalConfig` access.
- Intent/Serializable/R8, public/private, architecture, migration/storage/transfer, accessibility, adaptive, screenshot, deterministic runtime journey, and reviewed rollback gates pass.

## File Structure

### Module and Verification Wiring

- Modify `studio-android/LightNovelLibrary/settings.gradle`: include `:feature:novel` once at `feature/novel/`.
- Create `studio-android/LightNovelLibrary/feature/novel/build.gradle`: Android Compose library namespace `org.mewx.wenku8.feature.novel`.
- Modify `studio-android/LightNovelLibrary/app/build.gradle`: depend on `:feature:novel` and register the Phase 5 gate.
- Create `docs/verification/phase-5-novel-contract.yaml`; modify `ui-golden-manifest.yaml`, `ui-owner-action-ledger.yaml`, and `modernization-matrix.yaml`.
- Create `tools/verify-phase5-novel.ps1`: model ownership, active orchestration, route/ledger, Intent, secret/draft, and reachability verifier.

### Core Models, Contracts, and Data

- Create `core/model/src/main/kotlin/org/mewx/wenku8/core/model/novel/{NovelRouteModels,ReaderOpenRequest,ImageOpenRequest,NovelDownloadModels}.kt`.
- Create `core/model/src/main/kotlin/org/mewx/wenku8/core/model/community/CommunityRouteModels.kt`; retain existing Phase 1 `ReviewKey`, `ReviewSummary`, and `ReviewPost` ownership.
- Create `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/novel/{NovelRepository,NovelDownloadRepository}.kt`.
- Create `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/community/CommunityRepository.kt`.
- Create `core/data/src/main/java/org/mewx/wenku8/core/data/novel/{ProviderNovelRepository,DurableNovelDownloadRepository,ChapterDownloadChunkRunner,ChapterDownloadWorker}.kt`.
- Create `core/data/src/main/java/org/mewx/wenku8/core/data/community/ProviderCommunityRepository.kt`.
- Create `core/storage/src/main/java/org/mewx/wenku8/core/storage/novel/{CanonicalChapterCodec,ImageAssetStore}.kt` and schema migration/checkpoint support needed by durable chapter work.

### Novel Feature

- Create `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/navigation/{NovelRoute,NovelEntryPoints}.kt`.
- Create `detail/{NovelDetailUiState,NovelDetailViewModel,NovelDetailScreen}.kt`.
- Create `catalog/{CatalogUiState,CatalogViewModel,CatalogPane}.kt`.
- Create `download/{DownloadSelectionUiState,DownloadSelectionViewModel,DownloadSelectionScreen}.kt`.
- Create `community/{ReviewListUiState,ReviewListViewModel,ReviewListScreen,ReviewThreadUiState,ReviewThreadViewModel,ReviewThreadScreen,ReviewComposerUiState,ReviewComposerViewModel,ReviewComposerScreen}.kt`.
- Add localized strings under `values/`, `values-zh-rTW/`, and `values-zh-rHK/`, ViewModel tests under `src/test/java`, and Compose/screenshot tests under `src/androidTest/java`.

### App Navigation and Compatibility

- Modify `app/src/main/java/org/mewx/wenku8/di/{AppContainer,DefaultAppContainer}.kt` with repository interfaces only.
- Modify `app/src/main/java/org/mewx/wenku8/navigation/{AppRoute,AppDeepLink,Wenku8NavHost}.kt`.
- Create `app/src/main/java/org/mewx/wenku8/navigation/{NovelRouteFactory,ReaderCompatibilityLauncher,ImageCompatibilityLauncher,AccountCompatibilityLauncher}.kt`.
- Move current A04-A07 implementations to `LegacyNovelInfoActivity.kt`, `LegacyNovelReviewListActivity.kt`, `LegacyNovelReviewNewPostActivity.kt`, and `LegacyNovelReviewReplyListActivity.kt`.
- Replace original class-name files with thin compatibility trampolines and modify `LegacyIntentCodec.kt` only to name frozen return types.
- Modify `AndroidManifest.xml`, compatibility registry, R8 rules/evidence only where old fixtures require; never change verified Serializable identity.

## Task Dependency Graph

| Task | Depends on | Produces |
| ---: | --- | --- |
| 1 | Phase 4 gate | Feature module and executable Phase 5 contract |
| 2 | 1 | Core route/download/community models and repository interfaces |
| 3 | 2, Phase 2/3 | Detail/catalog/image repository |
| 4 | 2, Phase 2 | Community repository and mutation rules |
| 5 | 2, Phase 3 | Durable chapter/image download engine |
| 6 | 2-3, Phase 4 bookshelf | Detail/catalog ViewModels |
| 7 | 6 | Adaptive detail/catalog Material 3 UI |
| 8 | 5-7 | Download selection ViewModel/UI |
| 9 | 4 | Review list/thread ViewModels |
| 10 | 9 | Review list/thread Material 3 UI |
| 11 | 4, 9 | Create/reply form state and UI |
| 12 | 6-11 | AppContainer, typed routes, feature entry API |
| 13 | 12 | Reader/image/account compatibility launchers |
| 14 | 12-13 | A04-A07 legacy Intent trampolines and rollback entries |
| 15 | 14 | Active NovelInfo orchestration removal/parity verifier |
| 16 | 7-15 | Accessibility and deterministic screenshot gates |
| 17 | 1-16 | Runtime journey, provider matrices, and Phase 5 exit gate |

Every task follows RED -> minimal implementation -> focused PASS -> affected suite -> `git diff --check` -> isolated commit. Specification and code-quality reviews approve each task before a dependent task starts.

## Command Convention

Unless a step says repository root, commands run from `studio-android/LightNovelLibrary` with `-Pwenku8Provider=public --console=plain --stacktrace --no-parallel`. Feature modules use `src/main/java`, `src/test/java`, and `src/androidTest/java` Kotlin roots.

### Task 1: Add the Novel Feature Module and Freeze Phase 5 Scope

**Files:**
- Modify: `settings.gradle`
- Create: `feature/novel/build.gradle`
- Modify: `app/build.gradle`
- Create: `docs/verification/phase-5-novel-contract.yaml`
- Create: `tools/verify-phase5-novel.ps1`
- Test: `app/src/test/java/org/mewx/wenku8/architecture/Phase5ModuleGraphTest.kt`

- [ ] **Step 1: Write failing graph/model-ownership/route tests**

Assert one `:feature:novel`, namespace and dependency allowlist, seven route IDs, A04-A07 owners, and no feature dependency/import of provider/network/storage/Room/legacy/API implementations. Scan `api-contract/src/main` and fail if it declares any Phase 1-owned stable model name listed in Scope Boundary.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.architecture.Phase5ModuleGraphTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because module/contract/verifier are absent.

- [ ] **Step 2: Add the exact Android Compose feature module**

Append to `settings.gradle`:

```groovy
include ':feature:novel'
project(':feature:novel').projectDir = new File(rootDir, 'feature/novel')
```

Create `feature/novel/build.gradle`:

```groovy
plugins {
    id 'com.android.library'
    id 'org.jetbrains.kotlin.plugin.compose'
}
android {
    namespace 'org.mewx.wenku8.feature.novel'
    compileSdk rootProject.compileSdkVersion
    defaultConfig { minSdk rootProject.minSdkVersion }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    buildFeatures { compose true }
    testOptions { unitTests.returnDefaultValues = true }
}
dependencies {
    implementation project(':core:model')
    implementation project(':core:domain')
    implementation project(':core:designsystem')
    implementation libs.androidx.activity.compose
    implementation libs.androidx.lifecycle.runtime.compose
    implementation libs.androidx.lifecycle.viewmodel.compose
    implementation libs.androidx.compose.foundation
    implementation libs.androidx.compose.material.icons.extended
    implementation libs.androidx.compose.material3
    implementation libs.androidx.compose.ui
    testImplementation libs.junit4
    testImplementation libs.kotlinx.coroutines.test
    androidTestImplementation libs.androidx.test.ext.junit
    androidTestImplementation libs.androidx.compose.ui.test.junit4
    debugImplementation libs.androidx.compose.ui.test.manifest
    debugImplementation libs.androidx.compose.ui.tooling
}
```

AGP 9 built-in Kotlin owns Android Kotlin compilation. Do not apply `org.jetbrains.kotlin.android` or add legacy `android.kotlinOptions`; keep Java 17 `compileOptions` and the Phase 1 built-in Kotlin compiler configuration.

Add `implementation project(':feature:novel')` to app.

- [ ] **Step 3: Check in the complete semantic contract and verifier**

```yaml
schemaVersion: 1
phase: 5
owners: [A04, A05, A06, A07]
routes: [novel/detail, novel/catalog, novel/downloads/select, novel/reviews, novel/reviews/create, novel/reviews/thread, novel/reviews/reply]
legacyEntries:
  NovelInfoActivity: trampoline
  NovelReviewListActivity: trampoline
  NovelReviewNewPostActivity: trampoline
  NovelReviewReplyListActivity: trampoline
readerEntryType: org.mewx.wenku8.core.model.novel.ReaderOpenRequest
imageEntryType: org.mewx.wenku8.core.model.novel.ImageOpenRequest
stableModelOwner: org.mewx.wenku8.core.model
stateFamilies: [loading, content, empty, offline, authRequired, error, refreshing, appendLoading, submitting, success, failure, end]
```

The PowerShell verifier loads this contract/ledger/Intent manifest, scans duplicate stable-model declarations, checks active class files for forbidden orchestration/imports, checks feature dependency direction, checks retained rollback entries are non-exported and reachable only by reviewed policy, scans draft/body values from test output, and prints only `PHASE5-NOVEL-PASS`.

- [ ] **Step 4: Run graph and verifier checks**

```powershell
.\gradlew.bat :feature:novel:assembleDebug :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.architecture.Phase5ModuleGraphTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
& .\tools\verify-phase5-novel.ps1
git diff --check
```

Expected: PASS and `PHASE5-NOVEL-PASS`.

- [ ] **Step 5: Commit module/scope contract**

```powershell
git add studio-android/LightNovelLibrary/settings.gradle studio-android/LightNovelLibrary/feature/novel/build.gradle studio-android/LightNovelLibrary/app/build.gradle studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/architecture/Phase5ModuleGraphTest.kt docs/verification/phase-5-novel-contract.yaml tools/verify-phase5-novel.ps1
git diff --check --cached
git commit -m "build: add phase five novel module"
```

### Task 2: Define Novel Route, Reader/Image Entry, Download, and Community Contracts

**Files:**
- Create: `core/model/src/main/kotlin/org/mewx/wenku8/core/model/novel/{NovelRouteModels,ReaderOpenRequest,ImageOpenRequest,NovelDownloadModels}.kt`
- Create: `core/model/src/main/kotlin/org/mewx/wenku8/core/model/community/CommunityRouteModels.kt`
- Create: `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/novel/{NovelRepository,NovelDownloadRepository}.kt`
- Create: `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/community/CommunityRepository.kt`
- Test: `core/model/src/test/kotlin/org/mewx/wenku8/core/model/novel/NovelRouteModelsTest.kt`
- Test: `core/model/src/test/kotlin/org/mewx/wenku8/core/model/community/CommunityRouteModelsTest.kt`
- Test: `core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/novel/NovelRepositoryContractTest.kt`
- Test: `core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/community/CommunityRepositoryContractTest.kt`

- [ ] **Step 1: Write failing invariant, redaction, and ownership tests**

Test positive/nonblank identities, valid optional legacy aid/cid, chapter belongs to requested novel, unique download chapter keys, nonempty selection, bounded draft code points, nullable legacy review context, reader source behavior, image asset token/path rules, immutable capability snapshots, repository signatures, and no Android/provider/storage/API duplicate type. Reflection/source tests reject `ProviderCapability`, `ProviderCapabilities`, source facets, response metadata, and transport types from every `:core:model` and `:core:domain` public signature.

Run core model/domain tests. Expected: FAIL on unresolved types.

- [ ] **Step 2: Add route aggregate and stable reader/image entry values**

```kotlin
package org.mewx.wenku8.core.model.novel

import org.mewx.wenku8.core.model.catalog.ChapterSummary
import org.mewx.wenku8.core.model.catalog.NovelDetail
import org.mewx.wenku8.core.model.catalog.Volume
import org.mewx.wenku8.core.model.identity.NovelKey

enum class NovelOrigin { DISCOVER, SEARCH, LOCAL_BOOKSHELF, ACCOUNT_BOOKSHELF, LEGACY_LATEST, LEGACY_LIST, LEGACY_UNKNOWN }
data class NovelRouteRequest(val key: NovelKey, val origin: NovelOrigin, val legacyTitle: String? = null, val legacyAid: Int? = null)
data class NovelAggregate(val detail: NovelDetail, val catalog: List<Volume>, val localFavorite: Boolean, val accountFavorite: Boolean?)

enum class ReaderSourceMode { CLOUD, LOCAL_CACHE_FIRST }
data class ReaderOpenRequest(
    val novelKey: NovelKey,
    val volumeRemoteId: String,
    val chapterRemoteId: String,
    val sourceMode: ReaderSourceMode,
    val forceSavedPosition: Boolean,
) {
    init { require(volumeRemoteId.isNotBlank() && chapterRemoteId.isNotBlank()) }
}

data class ImageOpenRequest(
    val assetId: String,
    val compatibilityPath: String?,
    val description: String?,
) {
    init { require(assetId.isNotBlank()); require(compatibilityPath == null || compatibilityPath.isNotBlank()) }
}
```

`ReaderOpenRequest` is the exact cross-phase API Phase 6 consumes. Do not add `VolumeList`, `ChapterInfo`, Intent, Bundle, Parcelable, or Serializable to it. `ImageOpenRequest` is the exact cross-phase API Phase 7 consumes; compatibility path exists only while A11 remains a trampoline.

- [ ] **Step 3: Add download and community route values**

```kotlin
enum class ExistingContentPolicy { SKIP_VALID, UPDATE_STALE, FORCE_REPLACE }
data class NovelDownloadSelection(
    val novelKey: NovelKey,
    val chapterKeys: List<org.mewx.wenku8.core.model.identity.ChapterKey>,
    val contentLanguage: org.mewx.wenku8.core.model.settings.ContentLanguage,
    val includeImages: Boolean,
    val existingPolicy: ExistingContentPolicy,
) {
    init { require(chapterKeys.isNotEmpty() && chapterKeys.distinct().size == chapterKeys.size); require(chapterKeys.all { it.novel == novelKey }) }
}

data class NovelDownloadAvailability(val chapterRemoteId: String, val validLocal: Boolean, val active: Boolean, val failed: Boolean)

data class NovelCapabilitySnapshot(
    val remoteContentAvailable: Boolean,
    val binaryDownloadAvailable: Boolean,
    val recommendationAvailable: Boolean,
)
```

```kotlin
package org.mewx.wenku8.core.model.community

import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.identity.ReviewKey

data class ReviewLocator(val reviewRemoteId: String, val novelKey: NovelKey?) { init { require(reviewRemoteId.isNotBlank()) } }
data class ReviewDraft(val title: String, val body: String)
data class ReplyDraft(val body: String)
data class CommunityLimits(val titleCodePoints: Int, val bodyCodePoints: Int, val replyCodePoints: Int) {
    init { require(titleCodePoints > 0 && bodyCodePoints > 0 && replyCodePoints > 0) }
}
data class CommunityCapabilitySnapshot(
    val readAvailable: Boolean,
    val createAvailable: Boolean,
    val replyAvailable: Boolean,
    val limits: CommunityLimits,
)
```

- [ ] **Step 4: Add exact repository interfaces over core models**

```kotlin
interface NovelRepository {
    val capabilities: NovelCapabilitySnapshot
    suspend fun detail(key: NovelKey): LibraryResult<NovelDetail>
    suspend fun catalog(key: NovelKey): LibraryResult<List<Volume>>
    suspend fun prepareCover(key: NovelKey): LibraryResult<ImageOpenRequest>
    suspend fun recommend(key: NovelKey): LibraryMutationResult
}

interface NovelDownloadRepository {
    fun observeAvailability(key: NovelKey): kotlinx.coroutines.flow.Flow<List<NovelDownloadAvailability>>
    suspend fun enqueue(selection: NovelDownloadSelection): LibraryMutationResult
}
```

```kotlin
interface CommunityRepository {
    val capabilities: CommunityCapabilitySnapshot
    suspend fun reviews(key: NovelKey, page: Int): LibraryResult<LibraryPage<ReviewSummary>>
    suspend fun thread(locator: ReviewLocator, page: Int): LibraryResult<LibraryPage<ReviewPost>>
    suspend fun create(key: NovelKey, draft: ReviewDraft): LibraryResult<ReviewKey>
    suspend fun reply(locator: ReviewLocator, draft: ReplyDraft): LibraryResult<ReviewPostKey>
}
```

These snapshots are domain values and contain only operation availability plus bounded input limits; they never expose provider enums, source IDs, endpoints, freshness, or transport failure types. `ReviewLocator.novelKey=null` exists only for frozen legacy `rid` Intents. The data adapter supplies a bounded internal context key but never sends that synthetic novel ID as a request parameter; tests inspect the operation request to prove only `rid` is used.

- [ ] **Step 5: Verify ownership and commit**

```powershell
.\gradlew.bat :core:model:test :core:domain:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
rg -n "(data class|value class|class) (SourceId|NovelKey|NovelDetail|Volume|ChapterSummary|ReviewKey|ReviewSummary|ReviewPost)\b" api-contract\src\main feature\novel\src\main -g '*.kt'
git diff --check
```

Expected: tests PASS; source scan exits 1 because stable models exist only in core model.

```powershell
git add core/model/src core/domain/src
git diff --check --cached
git commit -m "feat(domain): define novel and community contracts"
```

### Task 3: Implement Detail, Catalog, Recommendation, and Cover Data

**Files:**
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/novel/ProviderNovelRepository.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/novel/ImageAssetStore.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/novel/ProviderNovelRepositoryTest.kt`
- Test: `core/storage/src/test/java/org/mewx/wenku8/core/storage/novel/ImageAssetStoreTest.kt`

- [ ] **Step 1: Write failing cache/offline/order/cover/recommendation tests**

Cover detail and catalog fresh/stale/offline/error; all combinations needed to map `ANONYMOUS_CATALOG`, `BINARY_DOWNLOAD`, and `RECOMMEND_NOVEL` into the exact `NovelCapabilitySnapshot`; an unavailable remote-content, binary, or recommendation operation returns bounded unsupported state before the gateway request counter changes; ordered volumes/chapters; cached catalog process restart; parse/server failure does not overwrite known-good data; language/source key separation; cover existing-local/fetch/hash/atomic replacement/failure; recommendation capability/auth and no automatic retry; cancellation propagation; no feature-visible URL.

Run focused tests. Expected: FAIL because repository/store are absent.

- [ ] **Step 2: Implement provider-to-core repository without duplicate mapping types**

The corrected Phase 2 facets already return Phase 1 core `NovelDetail`, `Volume`, `BinaryResource`, and key types. `ProviderNovelRepository` calls the existing `CachedProviderGateway`, maps only `ApiResult`/freshness/failure envelopes to Phase 4 `LibraryResult`, and persists accepted detail/catalog through Phase 3 catalog DAOs. No second `NovelDetail` mapper exists.

```kotlin
class ProviderNovelRepository(
    private val gateway: CachedProviderGateway,
    private val catalogStore: CatalogStore,
    private val images: ImageAssetStore,
    providerCapabilities: ProviderCapabilities,
) : NovelRepository {
    override val capabilities = NovelCapabilitySnapshot(
        remoteContentAvailable = providerCapabilities.supports(ProviderCapability.ANONYMOUS_CATALOG),
        binaryDownloadAvailable = providerCapabilities.supports(ProviderCapability.BINARY_DOWNLOAD),
        recommendationAvailable = providerCapabilities.supports(ProviderCapability.RECOMMEND_NOVEL),
    )
    override suspend fun detail(key: NovelKey) = gateway.novel(key).toLibrary("novel-detail") { value, freshness ->
        if (freshness == LibraryFreshness.FRESH) catalogStore.storeDetail(value)
        value
    }
    override suspend fun catalog(key: NovelKey) = gateway.catalog(key).toLibrary("catalog") { value, freshness ->
        if (freshness == LibraryFreshness.FRESH) catalogStore.storeCatalog(key, value)
        value
    }
    override suspend fun prepareCover(key: NovelKey): LibraryResult<ImageOpenRequest> =
        gateway.novel(key).flatMapLibrary("cover-detail") { detail -> images.materialize(key, detail.cover, gateway::binary) }
    override suspend fun recommend(key: NovelKey) = gateway.recommendNovel(key).toMutation("recommend")
}
```

The API capability types above remain private constructor/mapping details in `:core:data`. Each method checks the matching domain snapshot before any gateway call; disabled remote detail/catalog may still return a Phase 3 cached value as explicit stale data, but it performs zero provider work. Disabled binary/recommendation returns a bounded unsupported result with zero dispatch. On network failure, the repository queries Phase 3 durable catalog and returns explicit stale data. Parse/protocol/server failures do not replace cache and are not converted to stale unless a reviewed policy permits that exact failure class.

- [ ] **Step 3: Add atomic image materialization**

`ImageAssetStore.materialize` derives an opaque asset ID from source/novel/kind, checks approved `saves/imgs` roots through `LegacyPathPolicy`, fetches via Phase 2 binary facet only when absent/stale, verifies media type/size/hash, writes through Phase 3 `PartialFileStore`/journal projection, and returns `ImageOpenRequest(assetId, resolvedCompatibilityPath, titleDescription)`. It never logs URL/path/content and never returns an incomplete file.

- [ ] **Step 4: Run provider cache, catalog DB, image, and repository suites**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.novel.ProviderNovelRepositoryTest" --tests "org.mewx.wenku8.core.data.provider.*" :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.novel.ImageAssetStoreTest" :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.CatalogCacheDatabaseTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; known-good detail/catalog/cover survives failures and cancellation remains cancellation.

- [ ] **Step 5: Commit novel data repository**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/novel core/data/src/test/java/org/mewx/wenku8/core/data/novel core/storage/src/main/java/org/mewx/wenku8/core/storage/novel/ImageAssetStore.kt core/storage/src/test/java/org/mewx/wenku8/core/storage/novel/ImageAssetStoreTest.kt
git diff --check --cached
git commit -m "feat(data): add detail and catalog repository"
```

### Task 4: Implement Community Reads and Non-Retried Mutations

**Files:**
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/community/ProviderCommunityRepository.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/community/ProviderCommunityRepositoryTest.kt`

- [ ] **Step 1: Write failing paging/auth/validation/retry/invalidation tests**

Cover all eight `REVIEWS_READ`/`REVIEWS_CREATE`/`REVIEWS_REPLY` combinations and exact `CommunityCapabilitySnapshot`/limit mapping; disabled read/create/reply returns bounded unsupported state before validation/gateway/request counters change; list/thread ordering/paging/stale/offline/empty; locator with and without novel context; auth/session expired; code-point rather than UTF-16 validation; blank trimmed title/body/reply; provider limits; create/reply dispatch once; connection failure request count remains one; no coalescing; created key mapping; targeted invalidation; cancellation; draft/body absent from logs/cache keys/diagnostics.

Run focused tests. Expected: FAIL because the repository is absent.

- [ ] **Step 2: Implement exhaustive read and mutation mapping**

```kotlin
class ProviderCommunityRepository(
    private val gateway: CachedProviderGateway,
    providerCapabilities: ProviderCapabilities,
    private val sourceId: SourceId,
) : CommunityRepository {
    override val capabilities = CommunityCapabilitySnapshot(
        readAvailable = providerCapabilities.supports(ProviderCapability.REVIEWS_READ),
        createAvailable = providerCapabilities.supports(ProviderCapability.REVIEWS_CREATE),
        replyAvailable = providerCapabilities.supports(ProviderCapability.REVIEWS_REPLY),
        limits = providerCapabilities.inputPolicy.let {
            CommunityLimits(it.reviewTitleMaxCodePoints, it.reviewBodyMaxCodePoints, it.replyMaxCodePoints)
        },
    )

    override suspend fun reviews(key: NovelKey, page: Int) =
        gateway.reviews(key, page).toLibraryPage("reviews")

    override suspend fun thread(locator: ReviewLocator, page: Int) =
        gateway.reviewThread(locator.providerKey(sourceId), page).toLibraryPage("review-thread")

    override suspend fun create(key: NovelKey, draft: ReviewDraft) =
        validate(draft)?.let { LibraryResult.Failure(it) }
            ?: gateway.createReview(key, draft.title, draft.body).toLibrary("review-create")

    override suspend fun reply(locator: ReviewLocator, draft: ReplyDraft) =
        validate(draft)?.let { LibraryResult.Failure(it) }
            ?: gateway.reply(locator.providerKey(sourceId), draft.body).toLibrary("review-reply")
}
```

The constructor argument is named `providerCapabilities` so it cannot shadow the domain property shown above. Every method checks the matching domain snapshot first; unsupported operations return before validation, key adaptation, cache lookup, or gateway/network work. `providerKey` uses actual novel context when present. For a legacy locator it creates an in-memory core `NovelKey(sourceId, "legacy-review-context")`; Phase 2 request tests assert review-thread/reply transport uses only review remote ID and never the context value. Enabled-operation validation fails before gateway/network. Mutation gateway methods are not cached, coalesced, or automatically retried; success triggers the Phase 2 targeted invalidation matrix.

- [ ] **Step 3: Prove drafts never enter retained output or durable cache**

Inject title/body/reply canaries, run success/failure/cancellation, and scan build reports/log capture/cache metadata/Room DB dumps. Only test-source literals may match; production events contain bounded operation/failure enums.

- [ ] **Step 4: Run community and provider contract suites**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.community.*" --tests "org.mewx.wenku8.core.data.provider.MutationInvalidatorTest" :api-contract-tests:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; every POST dispatch count is at most one per explicit submit and no canary enters retained output.

- [ ] **Step 5: Commit community repository**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/community core/data/src/test/java/org/mewx/wenku8/core/data/community
git diff --check --cached
git commit -m "feat(data): add community repository"
```

### Task 5: Build the Durable Chapter and Image Download Engine

**Files:**
- Modify: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/{UserLibraryEntities,UserLibraryDatabase}.kt`
- Create: next Room schema and explicit migration.
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/novel/CanonicalChapterCodec.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/novel/{DurableNovelDownloadRepository,ChapterDownloadChunkRunner,ChapterDownloadWorker}.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/work/Wenku8WorkerFactory.kt`
- Test: storage/data/worker/process tests.

- [ ] **Step 1: Write failing schema, selection, crash-boundary, and retry tests**

Cover one row per selected chapter; content language/policy/images persisted; duplicate enqueue idempotence; valid existing skip; stale update; force replacement retaining prior; ordered text/image encode; image pending/cached/broken; chunk time/item bounds; death after chapter fetch/encode/fsync/checkpoint/image/final replace/terminal commit; cancellation; force-stop/reboot; Range-safe image retry; account/community POST never enters worker; exactly one terminal commit.

Run focused schema/data/worker tests. Expected: FAIL because descriptor fields/codec/runner/worker are absent.

- [ ] **Step 2: Extend the canonical download row with reviewed defaults and migration**

Add fields to `DownloadEntity`:

```kotlin
val contentLanguage: String = "SIMPLIFIED_CHINESE",
val includeImages: Boolean = true,
val existingPolicy: String = "SKIP_VALID",
val nextBlockIndex: Int = 0,
val completedImageCount: Int = 0,
```

Bump `UserLibraryDatabase` from version 1 to 2 and add an explicit `Migration(1, 2)` with non-null defaults matching existing behavior. Check in the generated `2.json`; run upgrade tests from schema 1. Do not use destructive migration.

- [ ] **Step 3: Implement canonical and legacy-compatible chapter encoding**

`CanonicalChapterCodec` encodes Phase 1 `ChapterDocument` as a bounded versioned canonical binary record preserving ordered text/image/break blocks, title, source/novel/chapter identity, media descriptors, and checksum. Its projection method delegates to the Phase 3 `LegacyCatalogXmlCodec` to atomically write the frozen `saves/novel/{cid}.xml` representation when a verified legacy CID/path exists. It rejects unbounded block/text/image counts before allocation and never executes markup.

- [ ] **Step 4: Implement enqueue and bounded runner lifecycle**

`DurableNovelDownloadRepository.enqueue` validates the selection, resolves Phase 3 catalog chapter rows, inserts/updates nonterminal `DownloadEntity` rows in one transaction, and schedules each opaque work key with `explicitlyUserStarted=true`; it records no URL/title/account/body in WorkManager Data.

`ChapterDownloadChunkRunner` follows this exact sequence:

1. acquire Phase 3 work-key lease and load canonical row/checkpoint;
2. honor `SKIP_VALID`/`UPDATE_STALE`/`FORCE_REPLACE` against verified final content;
3. call the Phase 2 catalog facet for one chapter using the persisted content language/source/key;
4. encode and fsync canonical partial content, checkpoint `nextBlockIndex`;
5. for each ordered image when enabled, fetch through the binary facet, validate/hash, use `PartialFileStore`, and checkpoint count after fsync;
6. atomically project the legacy chapter through the migration journal without deleting prior known-good content;
7. call `commitTerminalOnce(SUCCEEDED, sha256)` and accept zero only for identical existing success/hash;
8. release lease in `finally`; rethrow cancellation after a nonterminal checkpoint.

It yields before Phase 3 `ChunkBudget` expires and returns retry only for idempotent GET/validated binary fetch/safe whole-item restart. No mutation is retried.

- [ ] **Step 5: Bind worker construction and run destructive lifecycle harness**

`ChapterDownloadWorker` receives only opaque `workKey`, constructs the runner via `Wenku8WorkerFactory`, and maps Progress/Retry/Completed/Cancelled with generic Data. Register the known class in the existing worker factory. Run API 23/31/34/35/36 process kill, scheduler stop, force-stop, and reboot stages; retain pre/post hashes and request traces.

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.novel.CanonicalChapterCodecTest" :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.DatabaseUpgradeTest :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.novel.*Download*" :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.work.Wenku8WorkerFactoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: PASS; old schema upgrades, every crash resumes safely, final hash matches, terminal count is one.

- [ ] **Step 6: Commit durable download engine**

```powershell
git add core/storage/src core/data/src/main/java/org/mewx/wenku8/core/data/novel core/data/src/test app/src/main/java/org/mewx/wenku8/work/Wenku8WorkerFactory.kt app/src/test/java/org/mewx/wenku8/work
git diff --check --cached
git commit -m "feat(download): add durable novel downloads"
```

### Task 6: Implement Detail and Catalog ViewModels

**Files:**
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/detail/{NovelDetailUiState,NovelDetailViewModel}.kt`
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/catalog/{CatalogUiState,CatalogViewModel}.kt`
- Test: `feature/novel/src/test/java/org/mewx/wenku8/feature/novel/detail/NovelDetailViewModelTest.kt`
- Test: `feature/novel/src/test/java/org/mewx/wenku8/feature/novel/catalog/CatalogViewModelTest.kt`

- [ ] **Step 1: Write failing state/effect/restoration tests**

Cover invalid internal args; every `NovelCapabilitySnapshot`, Phase 4 `BookshelfCapabilitySnapshot`, and `CommunityCapabilitySnapshot` combination in the initial state before queued coroutines run; disabled remote refresh/cover/download/recommendation/account-favorite/review actions absent with zero matching repository calls; detail/catalog parallel load; local/cache/remote/stale/offline/error; partial detail with catalog retry; local/account favorite; journal pending; recommendation auth/failure/success; author search; tag discovery; reviews; cover materialization; selected volume/chapter; catalog Sheet/pane logical intent; open reader request; first/last; refresh; process death and resize restoration; cancellation; one-shot effects.

Run focused tests. Expected: FAIL because ViewModels are absent.

- [ ] **Step 2: Define complete immutable state/effects**

```kotlin
data class NovelDetailUiState(
    val request: NovelRouteRequest,
    val novelCapabilities: NovelCapabilitySnapshot,
    val bookshelfCapabilities: BookshelfCapabilitySnapshot,
    val communityCapabilities: CommunityCapabilitySnapshot,
    val detail: LoadableUiState<NovelDetail> = LoadableUiState.InitialLoading,
    val catalog: LoadableUiState<List<Volume>> = LoadableUiState.InitialLoading,
    val localFavorite: Boolean = false,
    val accountFavorite: Boolean? = null,
    val favoriteSubmitting: Boolean = false,
    val recommendationSubmitting: Boolean = false,
    val catalogVisible: Boolean = false,
    val selectedVolumeId: String? = null,
    val selectedChapterId: String? = null,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
)

sealed interface NovelDetailEffect {
    data class OpenReader(val request: ReaderOpenRequest) : NovelDetailEffect
    data class OpenImage(val request: ImageOpenRequest) : NovelDetailEffect
    data class OpenAuthorSearch(val author: String) : NovelDetailEffect
    data class OpenReviews(val key: NovelKey, val title: String) : NovelDetailEffect
    data class OpenDownloads(val key: NovelKey) : NovelDetailEffect
    data object RequestLogin : NovelDetailEffect
    data class Notify(val message: UiMessage) : NovelDetailEffect
}
```

`CatalogUiState` derives flattened stable volume/chapter items, cached/download state, current selection, and logical overlay visibility; it does not duplicate repository content.

- [ ] **Step 3: Implement structured concurrent loading and actions**

The detail ViewModel reads the three repository snapshots synchronously and includes them in the initial `StateFlow` value before composition. It validates `NovelRouteRequest`; when remote content is unavailable it asks the repository only for its cache-only result and never launches provider refresh, and every unavailable cover/download/recommendation/account-favorite/review event is rejected before a coroutine or effect is created. Enabled detail/catalog loads use `supervisorScope` so one recoverable failure does not erase the other. The ViewModel observes local bookshelf membership, persists only IDs/origin/visibility/scroll into SavedStateHandle, and serializes mutations. Local favorite delegates to Phase 4 `BookshelfRepository`; removing it does not delete content. Supported account actions map auth to `RequestLogin`. Reader effects construct the exact `ReaderOpenRequest`; local origin selects `LOCAL_CACHE_FIRST`, otherwise `CLOUD`.

- [ ] **Step 4: Run ViewModel/repository/migration tests**

```powershell
.\gradlew.bat :feature:novel:testDebugUnitTest --tests "org.mewx.wenku8.feature.novel.detail.*" --tests "org.mewx.wenku8.feature.novel.catalog.*" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.novel.ProviderNovelRepositoryTest" --tests "org.mewx.wenku8.core.data.library.RoomBookshelfRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; content survives one-side failure and process recreation emits no duplicate effect/request.

- [ ] **Step 5: Commit ViewModels**

```powershell
git add feature/novel/src/main/java/org/mewx/wenku8/feature/novel/detail feature/novel/src/main/java/org/mewx/wenku8/feature/novel/catalog feature/novel/src/test/java/org/mewx/wenku8/feature/novel
git diff --check --cached
git commit -m "feat(ui): add novel detail state"
```

### Task 7: Build Adaptive Detail and Catalog Material 3 UI

**Files:**
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/detail/NovelDetailScreen.kt`
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/catalog/CatalogPane.kt`
- Create: `feature/novel/src/main/res/values/strings.xml`
- Create: `feature/novel/src/main/res/values-zh-rTW/strings.xml`
- Create: `feature/novel/src/main/res/values-zh-rHK/strings.xml`
- Test: `feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/detail/NovelDetailScreenTest.kt`
- Test: `feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/catalog/CatalogPaneTest.kt`

- [ ] **Step 1: Write failing state/adaptive/semantics tests**

Cover every data state; detail metadata/introduction/tags; favorite selected/submitting/error; initial-frame snapshots with capability-disabled account favorite, recommendation, reviews, remote cover, and download actions absent (not briefly enabled); author/reviews/download/cover actions when supported; catalog grouping/current selection/cached state; compact Sheet Back/focus; expanded two panes; resize Sheet-to-pane exclusivity; separating/occluding hinge; 360x640, 915x412, 1280x800; light/dark/font 2.0; TalkBack/keyboard/DPAD traversal.

Run Compose tests. Expected: FAIL because screens are absent.

- [ ] **Step 2: Implement unframed detail content and actions**

Use `Wenku8Scaffold`, small/medium `TopAppBar`, a constrained content column, cover with stable aspect ratio, Material typography, `AssistChip`/`FilterChip` tags, icon favorite button with selected semantics, and icon+text commands for catalog/download/reviews. Render provider-backed commands only from the already-populated domain snapshots: unsupported controls are absent on the first frame and receive no semantics node/callback. Introduction paragraphs are selectable/readable text; metadata uses `ListItem`/rows, not cards within cards. No hero, gradient, floating page card, viewport-scaled font, fixed side width, or text impersonating a button.

- [ ] **Step 3: Implement compact Sheet and expanded pane from one logical state**

`CatalogPane` uses volume headings and chapter `ListItem` rows with current/cached/downloading semantics. Compact presents it in `ModalBottomSheet`; expanded assigns detail/catalog to physical regions from `AdaptiveLayoutInfo`. Sheet and pane share callbacks/state, never render together, and no content crosses hinge bounds. Back closes compact Sheet; expanded Back pops novel route. Focus moves to selected chapter when pane appears and returns to catalog button after Sheet closes.

- [ ] **Step 4: Run UI/design-system/adaptive tests**

```powershell
.\gradlew.bat :feature:novel:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.novel.detail.NovelDetailScreenTest,org.mewx.wenku8.feature.novel.catalog.CatalogPaneTest :core:designsystem:connectedDebugAndroidTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS with no overlap/clipping/dual presentation/hinge crossing.

- [ ] **Step 5: Commit detail/catalog UI**

```powershell
git add feature/novel/src/main feature/novel/src/androidTest
git diff --check --cached
git commit -m "feat(ui): add adaptive novel detail"
```

### Task 8: Implement Download Selection and Durable Progress Handoff

**Files:**
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/download/{DownloadSelectionUiState,DownloadSelectionViewModel,DownloadSelectionScreen}.kt`
- Test: `feature/novel/src/test/java/org/mewx/wenku8/feature/novel/download/DownloadSelectionViewModelTest.kt`
- Test: `feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/download/DownloadSelectionScreenTest.kt`
- Test: `feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/download/DownloadSelectionProcessTest.kt`

- [ ] **Step 1: Write failing selection/policy/enqueue/recovery tests**

Cover select all/none/volume/chapter; tri-state volume checkbox; existing valid/stale/active states; skip/update/force segmented policy; image toggle; zero-selection inline error; enqueue once; pending/failure preserves selection; success navigates to Phase 4 Downloads route; cancel before submit; process recreation; worker process death/reboot progress; compact/expanded/hinge/font 2.0/semantics.

Run focused tests. Expected: FAIL because route is absent.

- [ ] **Step 2: Implement immutable selection state/ViewModel**

```kotlin
data class DownloadSelectionUiState(
    val novelKey: NovelKey,
    val volumes: List<Volume> = emptyList(),
    val availability: Map<String, NovelDownloadAvailability> = emptyMap(),
    val selectedChapterIds: Set<String> = emptySet(),
    val policy: ExistingContentPolicy = ExistingContentPolicy.SKIP_VALID,
    val includeImages: Boolean = true,
    val submitting: Boolean = false,
    val fieldError: UiMessage? = null,
)
sealed interface DownloadSelectionEffect {
    data object OpenDownloads : DownloadSelectionEffect
    data class Notify(val message: UiMessage) : DownloadSelectionEffect
}
```

The ViewModel observes availability, derives eligible chapters, stores only bounded IDs/policy/toggle in SavedStateHandle, builds `NovelDownloadSelection` from core keys/language, disables duplicate submit, and preserves state on failure.

- [ ] **Step 3: Implement Material 3 selection controls**

Use volume headings, Checkbox/tri-state Checkbox, chapter rows, `SingleChoiceSegmentedButtonRow` for skip/update/force, Switch for images, visible selected count, inline error, and one primary enqueue command. Expanded layout uses hinge-safe list/supporting pane; compact uses one scroll owner. Actions remain visible at font 2.0 and compact height.

- [ ] **Step 4: Run feature and durable-worker regressions**

```powershell
.\gradlew.bat :feature:novel:testDebugUnitTest --tests "org.mewx.wenku8.feature.novel.download.*" :feature:novel:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.novel.download.DownloadSelectionScreenTest :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.novel.*Download*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.harness.Phase3StageBTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; accepted enqueue survives restart and Phase 4 Downloads observes it.

- [ ] **Step 5: Commit download selection**

```powershell
git add feature/novel/src/main/java/org/mewx/wenku8/feature/novel/download feature/novel/src/test/java/org/mewx/wenku8/feature/novel/download feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/download
git diff --check --cached
git commit -m "feat(ui): add durable download selection"
```

### Task 9: Implement Review List and Thread ViewModels

**Files:**
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/community/{ReviewListUiState,ReviewListViewModel,ReviewThreadUiState,ReviewThreadViewModel}.kt`
- Test: `feature/novel/src/test/java/org/mewx/wenku8/feature/novel/community/ReviewListViewModelTest.kt`
- Test: `feature/novel/src/test/java/org/mewx/wenku8/feature/novel/community/ReviewThreadViewModelTest.kt`

- [ ] **Step 1: Write failing paging/auth/offline/restoration/effect tests**

Cover every domain capability snapshot on the initial `StateFlow` value; read unavailable produces an unavailable state and zero page jobs/repository calls; create/reply unavailable produces no navigation effect or repository call; initial/refresh/append/end; empty; stale/offline; recoverable error retaining content; auth-required create/reply; session expiry; duplicate page/load suppression; legacy unscoped locator; selected thread/title; create/reply effects; process recreation/query-free bounded state; cancellation; no body in effect logs.

Run focused tests. Expected: FAIL because ViewModels are absent.

- [ ] **Step 2: Define exhaustive states/effects**

```kotlin
data class ReviewListUiState(
    val novelKey: NovelKey,
    val novelTitle: String?,
    val capabilities: CommunityCapabilitySnapshot,
    val reviews: PagedUiState<ReviewSummary> = PagedUiState(),
    val canCreate: Boolean = false,
    val authRequired: Boolean = false,
)
sealed interface ReviewListEffect {
    data class OpenThread(val locator: ReviewLocator, val title: String?) : ReviewListEffect
    data class OpenCreate(val key: NovelKey) : ReviewListEffect
    data object RequestLogin : ReviewListEffect
}

data class ReviewThreadUiState(
    val locator: ReviewLocator,
    val title: String?,
    val capabilities: CommunityCapabilitySnapshot,
    val posts: PagedUiState<ReviewPost> = PagedUiState(),
    val canReply: Boolean = false,
    val authRequired: Boolean = false,
)
sealed interface ReviewThreadEffect {
    data class OpenReply(val locator: ReviewLocator) : ReviewThreadEffect
    data object RequestLogin : ReviewThreadEffect
}
```

- [ ] **Step 3: Implement ViewModels with one active page job**

Each ViewModel reads `repository.capabilities` synchronously into its initial state before composition. If reads are unavailable it creates no page job; if create/reply is unavailable it creates no navigation effect. Enabled paths validate page/locator, load through `CommunityRepository`, retain content on refresh/append failure, mark stale explicitly, suppress duplicate loads, rethrow cancellation, store page/scroll/locator IDs in SavedStateHandle, and emit bounded navigation effects. Capability absence hides mutation action; auth failure on a supported action exposes login rather than a blank page.

- [ ] **Step 4: Run ViewModel/repository/provider tests**

```powershell
.\gradlew.bat :feature:novel:testDebugUnitTest --tests "org.mewx.wenku8.feature.novel.community.Review*ViewModelTest" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.community.*" :api-contract-tests:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS across every state and legacy locator request uses only rid.

- [ ] **Step 5: Commit review state**

```powershell
git add feature/novel/src/main/java/org/mewx/wenku8/feature/novel/community feature/novel/src/test/java/org/mewx/wenku8/feature/novel/community
git diff --check --cached
git commit -m "feat(ui): add community route state"
```

### Task 10: Build Review List and Thread Material 3 UI

**Files:**
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/community/{ReviewListScreen,ReviewThreadScreen}.kt`
- Test: `feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/community/ReviewListScreenTest.kt`
- Test: `feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/community/ReviewThreadScreenTest.kt`

- [ ] **Step 1: Write failing route/state/semantics/adaptive tests**

Cover content/loading/empty/stale/offline/error/auth; read-unavailable rendering with no loading flash; refresh/append/end; author/time/body paragraphs; heading and collection semantics; create/reply controls absent on the first frame when unsupported and present when supported; login/retry; stable keys; focus after retry; compact/expanded/hinge/font 2.0/dark/Traditional; TalkBack/keyboard/DPAD/Switch Access.

Run Compose tests. Expected: FAIL because screens are absent.

- [ ] **Step 2: Implement unframed paged lists**

Use `Wenku8Scaffold`, `TopAppBar`, `LazyColumn`, heading semantics, `ListItem` for review summaries, simple paragraph rows for posts, Material progress/state components, explicit unavailable/stale/auth banners, and a Material 3 FAB only when the initial domain snapshot permits the clear create/reply command. Unsupported controls have no semantics node or callback. Do not nest cards or render review body as executable rich content.

- [ ] **Step 3: Implement adaptive thread presentation and Back/focus**

Expanded detail may show review list/thread in two physical regions only when both meet readable widths; compact navigates. Hinge is a hard boundary. Back closes any dialog before thread -> list -> detail. Focus enters selected thread and returns to selected review when thread closes.

- [ ] **Step 4: Run UI/accessibility and paging tests**

```powershell
.\gradlew.bat :feature:novel:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.novel.community.ReviewListScreenTest,org.mewx.wenku8.feature.novel.community.ReviewThreadScreenTest :feature:novel:testDebugUnitTest --tests "org.mewx.wenku8.feature.novel.community.Review*ViewModelTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS with stable focus/back/paging semantics.

- [ ] **Step 5: Commit community read UI**

```powershell
git add feature/novel/src/main/java/org/mewx/wenku8/feature/novel/community feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/community
git diff --check --cached
git commit -m "feat(ui): add community read routes"
```

### Task 11: Implement Create and Reply Forms with Draft Preservation

**Files:**
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/community/{ReviewComposerUiState,ReviewComposerViewModel,ReviewComposerScreen}.kt`
- Test: `feature/novel/src/test/java/org/mewx/wenku8/feature/novel/community/ReviewComposerViewModelTest.kt`
- Test: `feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/community/ReviewComposerScreenTest.kt`
- Test: `feature/novel/src/test/java/org/mewx/wenku8/feature/novel/community/ReviewDraftPrivacyTest.kt`

- [ ] **Step 1: Write failing form lifecycle tests**

Cover create/reply unavailable before first render with no submit control and zero repository calls; idle; blank/title/body/reply code-point errors; field-local focus; `repository.capabilities.limits`; submitting disables duplicate; success; auth-required; failure preserves exact input; explicit retry dispatches once more; process recreation preserves draft; IME next/submit/dismiss; compact-height; Back with nonempty draft confirmation; no draft in logs/screenshots/cache/WorkManager Data.

Run focused tests. Expected: FAIL because composer is absent.

- [ ] **Step 2: Define state/mode and implement validation/submission**

```kotlin
sealed interface ComposerMode {
    data class Create(val novelKey: NovelKey) : ComposerMode
    data class Reply(val locator: ReviewLocator) : ComposerMode
}
data class ReviewComposerUiState(
    val mode: ComposerMode,
    val capabilities: CommunityCapabilitySnapshot,
    val title: String = "",
    val body: String = "",
    val titleError: UiMessage? = null,
    val bodyError: UiMessage? = null,
    val submitting: Boolean = false,
    val authRequired: Boolean = false,
    val failure: UiMessage? = null,
    val discardConfirmationVisible: Boolean = false,
)
sealed interface ReviewComposerEffect {
    data object Completed : ReviewComposerEffect
    data object RequestLogin : ReviewComposerEffect
    data class FocusField(val field: String) : ReviewComposerEffect
}
```

Initialize the state from `repository.capabilities` synchronously. A direct/deep-link entry for an unavailable create/reply mode renders bounded unavailable state and rejects submit before launching a coroutine. For enabled modes use `capabilities.limits`, `String.codePointCount`, trim for validation/submission only, retain typed text, call exactly one repository mutation per explicit submit, do not catch cancellation, and store title/body in SavedStateHandle only. Clear SavedStateHandle draft on success/discard. Never put it in Room/DataStore/files/logs/effects.

- [ ] **Step 3: Implement accessible Material 3 form**

When the selected mode is unsupported, show the bounded unavailable state without fields or submit callback. Otherwise use `OutlinedTextField` title only for Create, multiline body, counter derived from code points, inline error semantics, IME actions/focus order, progress in submit button, login/retry actions, Snackbar only for acknowledgement, and `AlertDialog` for discard. Layout uses IME/safe insets and remains usable at 915x412/font 2.0.

- [ ] **Step 4: Run form, mutation-count, restoration, and privacy tests**

```powershell
.\gradlew.bat :feature:novel:testDebugUnitTest --tests "org.mewx.wenku8.feature.novel.community.ReviewComposerViewModelTest" :feature:novel:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.novel.community.ReviewComposerScreenTest :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.community.ProviderCommunityRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; injected draft canaries are absent from retained output and failure/retry preserves fields.

- [ ] **Step 5: Commit community forms**

```powershell
git add feature/novel/src/main/java/org/mewx/wenku8/feature/novel/community feature/novel/src/test/java/org/mewx/wenku8/feature/novel/community feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/community
git diff --check --cached
git commit -m "feat(ui): add resilient community forms"
```

### Task 12: Bind Repositories and Register Typed Novel Routes

**Files:**
- Modify: `app/src/main/java/org/mewx/wenku8/di/{AppContainer,DefaultAppContainer}.kt`
- Create: `feature/novel/src/main/java/org/mewx/wenku8/feature/novel/navigation/{NovelRoute,NovelEntryPoints}.kt`
- Create: `app/src/main/java/org/mewx/wenku8/navigation/NovelRouteFactory.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/navigation/{AppRoute,AppDeepLink,Wenku8NavHost}.kt`
- Test: app route/factory/deep-link tests.

- [ ] **Step 1: Write failing DI/route/deep-link/restoration tests**

Assert repository singleton interfaces, no implementation exposed, safe encode/decode for source/remote/origin/title/review IDs, malformed internal routes -> visible argument error/zero provider call, deep-link bounds, process recreation, list-detail state, phase4 open-novel effect reaches detail, community nested Back, account return, and no second NavHost/Activity.

Run app tests. Expected: FAIL because routes/bindings are absent.

- [ ] **Step 2: Extend AppContainer with interfaces only**

```kotlin
val novelRepository: NovelRepository
val novelDownloadRepository: NovelDownloadRepository
val communityRepository: CommunityRepository
```

`DefaultAppContainer` constructs implementations from the existing selected gateway, stores, migration/worker/scheduler bindings. Feature/ViewModel constructors receive only interfaces.

- [ ] **Step 3: Define exact feature entry API and app route cases**

```kotlin
data class NovelEntryPoints(
    val openReader: (ReaderOpenRequest) -> Unit,
    val openImage: (ImageOpenRequest) -> Unit,
    val openAuthorSearch: (String) -> Unit,
    val requestLogin: (String) -> Unit,
    val openDownloads: () -> Unit,
)
```

Add to `AppRoute`:

```kotlin
data class NovelDetail(val sourceId: String, val remoteId: String, val origin: NovelOrigin, val title: String?) : AppRoute
data class NovelReviews(val sourceId: String, val novelRemoteId: String, val title: String?) : AppRoute
data class ReviewCreate(val sourceId: String, val novelRemoteId: String) : AppRoute
data class ReviewThread(val sourceId: String, val novelRemoteId: String?, val reviewRemoteId: String, val title: String?) : AppRoute
data class ReviewReply(val sourceId: String, val novelRemoteId: String?, val reviewRemoteId: String) : AppRoute
```

`AppDeepLink` uses `Uri.Builder`, query APIs, enum/length/ID allowlists, and never concatenates strings. `Wenku8NavHost` registers all seven route IDs under the existing single controller. `NovelRouteFactory` builds ViewModels with `SavedStateHandle` and entry callbacks.

- [ ] **Step 4: Run app/feature architecture and navigation tests**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.*Novel*" --tests "org.mewx.wenku8.di.*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.NovelNavigationTest :feature:novel:testDebugUnitTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
& .\tools\verify-phase5-novel.ps1
git diff --check
```

Expected: PASS and one host preserves nested route/back/restoration state.

- [ ] **Step 5: Commit route composition**

```powershell
git add studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation studio-android/LightNovelLibrary/app/src/test studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation studio-android/LightNovelLibrary/feature/novel/src/main/java/org/mewx/wenku8/feature/novel/navigation
git diff --check --cached
git commit -m "feat(app): register novel routes"
```

### Task 13: Implement Reader, Image, and Account Compatibility Launchers

**Files:**
- Create: `app/src/main/java/org/mewx/wenku8/navigation/{ReaderCompatibilityLauncher,ImageCompatibilityLauncher,AccountCompatibilityLauncher}.kt`
- Test: `app/src/test/java/org/mewx/wenku8/navigation/ReaderCompatibilityLauncherTest.kt`
- Test: `app/src/test/java/org/mewx/wenku8/navigation/ImageCompatibilityLauncherTest.kt`
- Test: `app/src/test/java/org/mewx/wenku8/navigation/AccountCompatibilityLauncherTest.kt`
- Test: `app/src/androidTest/java/org/mewx/wenku8/compat/ReaderSerializableCompatibilityTest.kt`
- Test: `app/src/androidTest/java/org/mewx/wenku8/navigation/CompatibilityLauncherInstrumentedTest.kt`

- [ ] **Step 1: Write failing frozen-contract and launch-plan tests**

Reader: map core catalog to verified `VolumeList`/`ChapterInfo`, preserve order/UIDs/field names, aid/cid defaults, `from=fav` cache-first, forcejump yes/no, volume/volumes, target reader selection, API 33 typed decode, minified old fixture. Image: nonblank materialized path under approved root -> old `path`; missing path -> recoverable error. Account: login explicit entry and return-route token bounded/non-secret. Assert launchers are the only feature path constructing these Intents.

Run focused compatibility tests. Expected: FAIL because launchers are absent.

- [ ] **Step 2: Implement ReaderCompatibilityLauncher with exact signature**

```kotlin
interface ReaderCompatibilityLauncher {
    fun launch(activity: Activity, request: ReaderOpenRequest)
}

class AndroidReaderCompatibilityLauncher(
    private val catalog: NovelRepository,
    private val mapper: LegacyCatalogSerializableMapper,
    private val planner: ReaderLaunchPlanner,
) : ReaderCompatibilityLauncher {
    override fun launch(activity: Activity, request: ReaderOpenRequest) {
        activity.lifecycleScope.launch {
            val volumes = catalog.catalog(request.novelKey).requireDataOrShowRecovery(activity) ?: return@launch
            val legacy = mapper.map(volumes, request)
            val plan = planner.plan(request)
            activity.startActivity(Intent(activity, plan.targetActivityClass).apply {
                putExtra("aid", legacy.aid)
                putExtra("cid", legacy.cid)
                putExtra("from", if (request.sourceMode == ReaderSourceMode.LOCAL_CACHE_FIRST) "fav" else plan.cloudSource)
                putExtra("forcejump", if (request.forceSavedPosition) "yes" else "no")
                putExtra("volume", legacy.selectedVolume)
                putExtra("volumes", ArrayList(legacy.volumes))
            })
        }
    }
}
```

Only the launcher imports legacy Serializable classes. It uses structured Activity lifecycle scope and never `runBlocking`. Phase 6 replaces the launch destination while preserving the interface/type.

- [ ] **Step 3: Implement image/account compatibility launchers**

Image validates `compatibilityPath` through Phase 3 path policy, then starts `ViewImageDetailActivity` with `path`; null/invalid path emits route recovery and no Activity. Account starts the retained `UserLoginActivity` with a bounded app-owned return token, not credentials/account data; on result the owning ViewModel revalidates session and reloads.

- [ ] **Step 4: Run old-signed/minified and architecture tests**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.*CompatibilityLauncherTest" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.compat.ReaderSerializableCompatibilityTest,org.mewx.wenku8.navigation.CompatibilityLauncherInstrumentedTest :app:assembleAlphaRelease -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS on API 23/32/33 and minified old fixtures; feature imports no Intent/legacy type.

- [ ] **Step 5: Commit compatibility launchers**

```powershell
git add app/src/main/java/org/mewx/wenku8/navigation app/src/test/java/org/mewx/wenku8/navigation app/src/androidTest/java/org/mewx/wenku8/navigation
git diff --check --cached
git commit -m "feat(app): bridge novel routes to retained entries"
```

### Task 14: Convert A04-A07 Activities to Legacy Intent Trampolines

**Files:**
- Move/Modify four current Activity implementations to `Legacy*Activity.kt`.
- Create four original-class-name trampoline files.
- Modify `app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt`.
- Modify `app/src/main/AndroidManifest.xml`.
- Test: `app/src/androidTest/java/org/mewx/wenku8/compat/Phase5NovelTrampolineTest.kt`.

- [ ] **Step 1: Write failing old sender/default/malformed/back-stack tests**

Use every Phase 0 intent-contract row and old-signed fixture. Novel: aid missing/malformed -> 1, from nullable/known/unknown diagnostic, title String/CharSequence nullable. Review list/create: aid default 1. Thread: rid default 1, title nullable, no aid accepted. Assert one forward, finish, task/back preservation, recreation, predictive cancel/commit, minified/API 23/32/33, debug legacy rollback, and unchanged exported policy.

Run focused fixture/instrumented tests. Expected: FAIL because old classes still own pages.

- [ ] **Step 2: Name frozen codec return values without a second decoder**

```kotlin
data class LegacyNovelDetailArguments(val aid: Int, val from: String?, val title: String?)
data class LegacyReviewListArguments(val aid: Int)
data class LegacyReviewThreadArguments(val rid: Int, val title: String?)
```

Keep the existing Phase 0 functions `novelDetail(intent)`, `reviewList(intent)`, and `reviewThread(intent)`, API 33 behavior, defaults, and sentinel normalization. All raw `get*Extra` calls remain inside `LegacyIntentCodec.kt`.

- [ ] **Step 3: Preserve rollback implementations and add thin forwarding classes**

Move current class bodies and rename declarations only. Original class names extend `ComponentActivity`, decode once, map selected provider source plus positive numeric remote ID to `AppRoute`, start non-exported `Wenku8ShellActivity`, and finish. Review thread maps missing novel context to nullable route field. No trampoline inflates layout, accesses provider/storage/file, starts background work, or logs arguments.

```kotlin
class NovelInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.getBoolean("forwarded") == true) { finish(); return }
        val registry = (applicationContext as LegacyCompatibilityOwner).legacyCompatibility
        val args = registry.intentCodec.novelDetail(intent)
        val source = applicationContext.appContainer().providerBinding.providerId.value
        val route = AppRoute.NovelDetail(source, args.aid.toString(), args.from.toNovelOrigin(), args.title)
        startActivity(Intent(this, Wenku8ShellActivity::class.java).setData(AppDeepLink.encode(route)))
        finish()
    }
}
```

- [ ] **Step 4: Update manifest/rollback reachability and run compatibility suites**

Original A04-A07 names keep their current exported policy. New `Legacy*` classes are non-exported and reachable only via build-time reviewed rollback launcher. Run old-signed/minified fixtures, manifest tests, and source scan proving raw extras exist only in codec.

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "*LegacyIntentCodec*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.compat.Phase5NovelTrampolineTest,org.mewx.wenku8.compat.OldReleaseSerializableInstrumentedTest :app:assembleAlphaRelease :app:processAlphaReleaseMainManifest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; every old caller reaches one new route and no verified class/key/default/UID changes.

- [ ] **Step 5: Commit compatibility trampolines**

```powershell
git add app/src/main/java/org/mewx/wenku8/activity app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt app/src/main/AndroidManifest.xml app/src/androidTest/java/org/mewx/wenku8/compat
git diff --check --cached
git commit -m "refactor(app): route legacy novel entries to Compose"
```

### Task 15: Remove Active NovelInfo Orchestration After Parity

**Files:**
- Create: `tools/verify-phase5-active-orchestration.ps1`
- Modify/delete active-only references to `NovelInfoActivity` helpers after ledger proof.
- Modify `docs/verification/ui-owner-action-ledger.yaml`.
- Test: architecture/source inventory and behavior parity matrix.

- [ ] **Step 1: Add failing active reachability and parity verifier**

The verifier traces release-default manifest/navigation/Intent targets and fails if any active A04-A07 route reaches XML layouts, old adapters, AsyncTask, Handler/Thread/Executor, provider/network/parser/cache/file APIs, `findViewById`, `GlobalConfig`, AppCompat Toolbar/CardView, or direct legacy helpers. It separately verifies retained `Legacy*` files are reachable only under `wenku8RouteDefault=legacy` and remain listed for rollback.

Run from repository root. Expected: FAIL until all active references route through trampolines/new NavHost.

- [ ] **Step 2: Complete behavior parity matrix before disabling old default path**

Map each old visible action/state to exact new test IDs: title info, author search, status/update/latest, introduction/tags, cover viewer, favorite local/account, recommendation, catalog/volume/chapter, reader launch, download four policies, review list/create/thread/reply, loading/error/retry/cancel, Back/drawer/menu. Missing behavior blocks this task; it is not recorded as implicit retirement.

- [ ] **Step 3: Remove active registrations/imports and retain rollback source**

Delete or detach obsolete helper/adapters/layout references only when zero release-default reachability and no rollback dependency exists. Helpers still used solely by `LegacyNovelInfoActivity` remain in its rollback package and are marked with ledger owner/retirement phase 8. Original `NovelInfoActivity.kt` and review original-name files remain thin trampolines.

- [ ] **Step 4: Run active architecture, inventory, parity, and rollback tests**

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android'
& .\tools\verify-phase5-active-orchestration.ps1
& .\tools\verify-phase4-library.ps1
& .\tools\verify-phase5-novel.ps1
Set-Location 'studio-android\LightNovelLibrary'
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "*Architecture*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.journey.Phase5NovelParityTest :app:assembleAlphaRelease -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:assembleAlphaRelease -Pwenku8Provider=public -Pwenku8RouteDefault=legacy --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: all verifiers PASS; release default has zero active orchestration while reviewed legacy forward-fix still builds/launches.

- [ ] **Step 5: Commit active orchestration removal evidence**

```powershell
git add tools/verify-phase5-active-orchestration.ps1 studio-android/LightNovelLibrary/app docs/verification/ui-owner-action-ledger.yaml
git diff --check --cached
git commit -m "refactor(app): retire active novel orchestration"
```

### Task 16: Prove Accessibility, Adaptive Layout, and Screenshot Coverage

**Files:**
- Create: `app/src/androidTest/java/org/mewx/wenku8/accessibility/Phase5NovelAccessibilityTest.kt`.
- Create: `app/src/androidTest/java/org/mewx/wenku8/screenshot/Phase5NovelGoldenTest.kt`.
- Modify: `app/build.gradle` and `verification-tools/{build.gradle,src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt}`.
- Modify: `docs/verification/{ui-golden-manifest,manual-assistive-technology-manifest}.yaml`.
- Create: `docs/verification/{manual-a11y-phase5,phase-5-visual-review}.md`.

- [ ] **Step 1: Add failing semantics and golden-manifest coverage tests**

Every route gets compact content, expanded smoke, applicable loading/empty/error/offline/auth, dark+font 2.0 with zh-TW/zh-HK, medium/API36 gesture. Detail/catalog/download add compact-height, separating/occluding hinge, Sheet/pane resize/focus/back. Create/reply add IME-open compact/landscape, validation/submitting/failure-preserved/discard dialog. Reviews add paging/end/thread focus. Run manifest verifier and expect named missing cases.

- [ ] **Step 2: Complete automated semantics/focus/inset/back assertions**

Assert headings, collection metadata, meaningful cover description, decorative image clearing, selected favorite/tab/chip/checkbox, expanded Sheet state, progress/live region, error/field focus, code-point counter, disabled submit, 48dp targets, traversal order, IME action/dismiss, dialog/Sheet focus return, and API36 predictive start/cancel/commit. Fix route code at the owning component.

- [ ] **Step 3: Run manual assistive-technology journeys**

Record tester/date/device/API/result for:

```text
P5-A21 TalkBack detail -> catalog Sheet/pane -> chapter reader compatibility
P5-A22 TalkBack favorite -> download selection -> progress/cancel/retry
P5-A23 TalkBack review list -> auth-required -> login return -> create failure -> retry
P5-A24 TalkBack review thread -> reply validation/failure/retry
P5-A25 Keyboard/DPAD detail/catalog/download/reviews/forms
P5-A26 Switch Access favorite/download selection/create/reply
P5-A27 API36 predictive Back dialog/Sheet/thread/list/detail/trampoline
```

No account/review content appears in retained screenshots/manual record.

Write every TalkBack/Switch Access PASS as a current structured row using the Phase 4 `ManualAssistiveEvidenceVerifier` schema, including service version, configuration, source/app/test-APK/report hashes, tester, and independent reviewer. Register the exact phase task:

```groovy
tasks.register('verifyPhase5AssistiveEvidence', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyAssistiveEvidence', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath, '5'
}

// app/build.gradle
registerUiGoldenTask('recordPhase5UiGoldens', 'record', 5,
    'org.mewx.wenku8.screenshot.Phase5NovelGoldenTest')
registerUiGoldenTask('verifyPhase5UiGoldens', 'verify', 5,
    'org.mewx.wenku8.screenshot.Phase5NovelGoldenTest')
```

- [ ] **Step 4: Record deterministic synthetic screenshots and visually inspect originals**

```powershell
.\gradlew.bat :app:recordPhase5UiGoldens -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
$sourceCommit = (git rev-parse HEAD).Trim()
.\gradlew.bat :verification-tools:approveUiGoldens -Pphase=5 "-PuiGoldenReviewer=$env:WENKU8_UI_REVIEWER" "-PuiGoldenSourceCommit=$sourceCommit"
.\gradlew.bat :app:verifyPhase5UiGoldens :verification-tools:verifyUiGoldenManifest -Pphase=5 -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

The recorder writes real fixture/image SHA-256 values. Inspect original pixels for clipping, overlap, CJK line breaks, horizontal overflow, double scroll, hinge crossing, wrong state, IME occlusion, tiny controls, card piles, and focus presentation. Baselines require human approval; no unexplained mask/tolerance change passes.

- [ ] **Step 5: Run accessibility/golden suites and commit evidence**

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.accessibility.Phase5NovelAccessibilityTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :verification-tools:verifyPhase5AssistiveEvidence :app:verifyPhase5UiGoldens :verification-tools:verifyUiGoldenManifest -Pphase=5 -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; no unresolved visual/accessibility Critical/Important issue.

```powershell
git add app/src/androidTest/java/org/mewx/wenku8/accessibility app/src/androidTest/java/org/mewx/wenku8/screenshot/Phase5NovelGoldenTest.kt docs/verification/ui-goldens docs/verification/ui-golden-manifest.yaml docs/verification/manual-assistive-technology-manifest.yaml docs/verification/manual-a11y-phase5.md docs/verification/phase-5-visual-review.md
git diff --check --cached
git commit -m "test(ui): approve phase five novel coverage"
```

### Task 17: Run Runtime Journeys and Bind the Phase 5 Exit Gate

**Files:**
- Create: `app/src/androidTest/java/org/mewx/wenku8/journey/{Phase5NovelJourneyTest,Phase5CommunityJourneyTest,Phase5DownloadRecoveryTest}.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/navigation/{Phase5RouteProcessDeathSeedTest,Phase5RouteProcessDeathVerifyTest}.kt`
- Modify: `docs/verification/modernization-matrix.yaml`
- Modify: `app/build.gradle`

- [ ] **Step 1: Add failing deterministic journey/matrix tests**

Journey 1: launch -> discover -> detail -> catalog -> reader compatibility. Journey 2: offline bookshelf -> cached detail/catalog -> cached chapter. Journey 3: favorite add/remove restoration. Journey 4: select downloads -> progress -> scheduler/process recovery -> cancel/retry -> offline availability. Journey 5: reviews -> auth-required -> login-return fake -> create failure -> retained draft -> retry; thread reply failure/retry. The separate route seed/verify matrix covers detail selection/catalog Sheet intent, review list/thread position, create/reply non-secret draft state, and download selection across a changed PID. Matrix completeness maps every exit condition to exact test/provider/variant/API/config/fixture/baseline/report/commit.

Run focused journeys/matrix verifier. Expected: FAIL until all evidence is linked.

- [ ] **Step 2: Register the complete gate**

```groovy
def repositoryRoot = rootProject.projectDir.parentFile.parentFile

tasks.register('verifyPhase5ActiveOrchestration', Exec) {
    group = 'verification'
    workingDir repositoryRoot
    commandLine rootProject.ext.resolvePowerShell(), '-NoProfile', '-NonInteractive',
        '-File', new File(repositoryRoot, 'tools/verify-phase5-active-orchestration.ps1').absolutePath
}

tasks.register('verifyPhase5NovelPlan', Exec) {
    group = 'verification'
    workingDir repositoryRoot
    commandLine rootProject.ext.resolvePowerShell(), '-NoProfile', '-NonInteractive',
        '-File', new File(repositoryRoot, 'tools/verify-phase5-novel.ps1').absolutePath
}

tasks.register('phase5NovelGate') {
    group = 'verification'
    description = 'Canonical Phase 5 novel/community aggregate gate.'
    dependsOn ':phase0Gate'
    dependsOn ':verification-tools:phase1Gate'
    dependsOn ':verification-tools:phase2Gate'
    dependsOn ':verification-tools:phase3Gate'
    dependsOn ':app:phase4LibraryGate'
    dependsOn ':verifyArchitecture'
    dependsOn ':feature:novel:testDebugUnitTest'
    dependsOn ':feature:novel:connectedDebugAndroidTest'
    dependsOn ':core:data:testDebugUnitTest'
    dependsOn ':core:storage:testDebugUnitTest'
    dependsOn 'testAlphaDebugUnitTest'
    dependsOn 'lintAlphaDebug'
    dependsOn 'assembleAlphaRelease'
    dependsOn 'verifyPhase5UiGoldens'
    dependsOn ':verification-tools:verifyUiGoldenManifest'
    dependsOn ':verification-tools:verifyPhase5AssistiveEvidence'
    dependsOn ':verification-tools:verifyXmlSurfaceLedger'
    dependsOn ':verification-tools:verifyPlannedGradleTasks'
    dependsOn ':verification-tools:verifySensitiveSource'
    dependsOn ':verification-tools:verifyOutboundManifest'
    dependsOn ':verification-tools:verifyPackagedLicenses'
    dependsOn 'verifyPhase5ActiveOrchestration'
    dependsOn 'verifyPhase5NovelPlan'
}
```

This is the sole Phase 5 aggregate task; do not add an unregistered `phase5Gate` alias. Its explicit dependencies execute Phase 0-4 prerequisite gates, API/model ownership/architecture checks, old Intent/Serializable minified tests, active orchestration verifier, writer/transfer verifiers, controlled instrumentation/device journeys, public/private provider contract selection, compliance checks, and modernization-matrix completeness. Live review mutation tests are never automatic.

- [ ] **Step 3: Run public/provider/device/rollback matrices**

```powershell
.\gradlew.bat :app:phase5NovelGate -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.journey.Phase5NovelJourneyTest,org.mewx.wenku8.journey.Phase5CommunityJourneyTest,org.mewx.wenku8.journey.Phase5DownloadRecoveryTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\tools\verification\run-ui-process-death.ps1 -Phase 5 -Api 36 -SeedClass org.mewx.wenku8.navigation.Phase5RouteProcessDeathSeedTest -VerifyClass org.mewx.wenku8.navigation.Phase5RouteProcessDeathVerifyTest -Provider public
.\gradlew.bat :app:assembleAlphaRelease -Pwenku8Provider=public -Pwenku8RouteDefault=legacy --console=plain --stacktrace --no-parallel
Set-Location '..\..'
& .\tools\verify-phase5-active-orchestration.ps1
& .\tools\verify-phase5-novel.ps1
git diff --check
```

Expected: PASS. Controlled API 23/31/34/35/36 download stop/kill/force-stop/reboot evidence records one final hash/terminal commit. Protected private CI runs the same domain/UI/compatibility tests and returns a current bound redacted attestation.

- [ ] **Step 4: Run independently gated live checks only with accepted scope**

Read-only detail/catalog/review browsing may extend the Phase 2 live harness when current scope permits. Reversible favorite requires fixture ID + exact gate + restoration in `finally`. Review create/reply requires persistent mutation authorization and per-run interactive confirmation. No live result is printed beyond operation labels and PASS/typed failure class. Absence of accepted scope blocks the corresponding exit claim.

- [ ] **Step 5: Resolve independent reviews**

Commission architecture/provider/model-ownership, migration/download/rollback, Compose MD3/navigation/accessibility/visual, and executable evidence reviews. Resolve and rerun every Critical/Important finding before completion.

- [ ] **Step 6: Commit verified Phase 5 gate**

```powershell
git add app/build.gradle app/src/androidTest/java/org/mewx/wenku8/journey docs/verification/modernization-matrix.yaml
git diff --check --cached
git commit -m "test: bind phase five novel exit gate"
```

## Phase 5 Completion Checklist

- [ ] Stable model ownership remains exclusively in `:core:model`; no API/feature duplicates.
- [ ] Detail/catalog/favorite/recommend/cover states and actions pass.
- [ ] Compact Sheet and expanded hinge-safe pane behavior passes.
- [ ] Reader/Image entry contracts pass old Intent/Serializable/minified fixtures.
- [ ] Durable download selection/cancel/retry/process/reboot exactly-one gates pass.
- [ ] Review list/thread paging and create/reply draft-preserving retry pass.
- [ ] A04-A07 original class names are thin codec-driven trampolines.
- [ ] Active NovelInfo/review routes own zero network/parser/cache/file/background orchestration.
- [ ] Retained legacy implementations remain tested rollback-only entries.
- [ ] Accessibility/manual journeys and approved deterministic screenshot matrix pass.
- [ ] Public and protected-private provider matrices pass.
- [ ] All authorization/license/privacy/egress gates remain current.
- [ ] No Critical/Important independent finding remains.
- [ ] `git diff --check` passes and every task has an isolated reviewed commit.

## Deliberate Deferrals

- `ReaderOpenRequest` is stable, but Phase 6 moves reader state/concurrency/navigation into `:feature:reader` and retires V1/vertical entries only after full parity.
- `ImageOpenRequest` is stable, but Phase 7 implements final Compose image viewer permissions/zoom/rotate/save and removes its compatibility launcher.
- Login/profile/settings/wallpaper/about remain Phase 7 compatibility entries; community auth routes return through the account launcher.
- Retained legacy A04-A07 source/layout deletion, legacy bridge removal, and dual-write retirement require Phase 8 compatibility-window approval.

## Execution Handoff

Plan complete at `docs/superpowers/plans/2026-07-10-wenku8-phase-5-novel-community.md`. Execute with `superpowers:subagent-driven-development` task by task, using `superpowers:using-git-worktrees` before implementation and two-stage review after each task.
