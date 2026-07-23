# Wenku8 Phase 4 Library Shell Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the adaptive single-activity Compose Material 3 shell the reviewed default and deliver complete discovery, search, bookshelf, download-status, and reading-history journeys over the Phase 1-3 contracts.

**Architecture:** Add `:feature:library` behind platform-neutral `:core:domain` repositories and immutable `:core:model` values. `:core:data` is the only layer that combines the Phase 2 provider gateway with the Phase 3 Room, migration-journal, and transfer contracts; feature ViewModels own immutable `StateFlow` state and bounded effects, while `:app` owns Navigation Compose, compatibility entries, and route rollout. Legacy search and main-shell owners remain available as rollback implementations until the route parity and compatibility gates pass.

**Tech Stack:** Kotlin, coroutines, ViewModel, SavedStateHandle, StateFlow, Room, WorkManager/JobScheduler transfer contracts, Jetpack Compose Material 3, Navigation Compose, Material 3 adaptive navigation, AndroidX WindowManager, JUnit4, coroutines-test, AndroidX Compose UI test, screenshot verification, PowerShell, Gradle.

---

## Scope Boundary

This phase owns UI-owner ledger rows A01-A03 and F01-F05 plus the library actions from F04. It implements these route IDs:

- `library/discover`
- `library/discover/latest`
- `library/discover/completed`
- `library/discover/ranking`
- `library/discover/category`
- `library/discover/tag`
- `library/search`
- `library/search/results`
- `library/bookshelf`
- `library/bookshelf/downloads`
- `library/bookshelf/history`

The Phase 1/index ownership rule is absolute: `SourceId`, `NovelKey`, catalog/account/community models, and language values live only under `org.mewx.wenku8.core.model.*`. Corrected Phase 2 API-contract requests/results/facets import those types. Phase 4 creates route-specific presentation values only and must not propagate any duplicate model declaration from an earlier draft.

This phase does not implement novel detail/catalog/community, reader consolidation, account UI, application settings, wallpaper, about, or image viewer. A library item emits a typed `OpenNovel` effect; Phase 4 routes that effect through the reviewed `NovelInfoActivity` compatibility entry. Phase 5 replaces that destination. Settings/account/about/wallpaper actions continue through explicit compatibility launchers until Phase 7.

The Downloads route owns observation, progress, cancellation, and retry of Phase 3 durable work. Selecting chapters for a new novel download belongs to Phase 5. The History route reads canonical reader progress but does not alter reader behavior or retirement decisions.

## Working Directory and Command Rules

Run Gradle commands from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android\studio-android\LightNovelLibrary'
```

Run repository verifiers from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android'
```

Never put a username, password, Cookie, captcha, account response, query result body, private endpoint, or signing value in a command, fixture, screenshot, report, source file, or commit. Live anonymous verification contains no account operation and remains disabled unless the exact Phase 0 live-observation and content scopes are current and `ACCEPTED`.

## Prerequisites and Stop Conditions

### Required Before Task 1

- [ ] Root `phase0Gate`, `:verification-tools:phase1Gate`, `:verification-tools:phase2Gate`, and `:verification-tools:phase3Gate` pass from the Android project root.
- [ ] The reviewed Phase 0 `docs/verification/ui-owner-action-ledger.yaml`, `intent-contracts.yaml`, `artifact-manifest.yaml`, and `modernization-matrix.yaml` exist and their generated hashes verify.
- [ ] Phase 1 provides `AppContainer`, `RouteViewModelFactory`, `Wenku8App`, `Wenku8NavHost`, `AdaptiveLayoutInfo`, `RouteFlagRepository`, `Wenku8Theme`, `Wenku8Scaffold`, and `StateContent` at the exact paths named in the plan index.
- [ ] Phase 2 provides the four typed provider facets, `CachedProviderGateway`, typed failures/freshness, provider capability checks, and a non-throwing public logical `:api` bridge.
- [ ] Phase 3 provides `UserLibraryDatabase`, `CatalogCacheDatabase`, `JournaledMutationRunner`, the search/bookshelf/progress migration participants, `DownloadEntity`, `DownloadDao`, `TransferScheduler`, and API-aware scheduler integration.
- [ ] Public and protected-private build graphs each resolve one provider binding and pass their shared contract suite.

Run:

```powershell
.\gradlew.bat -Pwenku8Provider=public phase0Gate :verification-tools:phase1Gate :verification-tools:phase2Gate :verification-tools:phase3Gate --console=plain --stacktrace --no-parallel
```

Expected: `BUILD SUCCESSFUL`; all three gates execute and no prerequisite is substituted by a disabled/stub binding.

### Stop Immediately When

- A feature file would import `Wenku8API`, `LightNetwork`, `LightUserSession`, `GlobalConfig`, `OkHttp`, Jsoup, a Room DAO/entity, a DataStore implementation, or a provider implementation.
- A composable would read a file/database, dispatch a provider call, parse content, construct an Intent, or own an executor/coroutine scope.
- A new route reads raw Intent extras instead of using the single Phase 0 `LegacyIntentCodec`.
- A canonical search/bookshelf/history mutation bypasses the Phase 3 journal/writer barrier or stops rollback projection.
- A download retry can repeat a mutation, starts work without an explicit visible user action, loses its canonical progress row, or turns cancellation into success.
- A stale account bookshelf row is displayed under a different account/session epoch.
- A route loses query, filter, selected tab, list position, or selected item during process recreation/window resize.
- A separating/occluding hinge is crossed by content or both compact Sheet and expanded pane are visible.
- Any route becomes the release default before its behavior, state, compatibility, accessibility, screenshot, public/private, and rollback gates pass.
- A generated baseline is accepted by raising tolerance/masking unexplained content, or a Critical/Important independent review finding remains open.

## Phase Exit Conditions

- A01-A03 and F01-F05 ledger rows map to tested Compose Material 3 routes or retained, tested rollback entries.
- Discover covers home/latest/completed/ranking/category/tag flows with refresh, paging, filter, stale/offline, error, and empty behavior.
- Search covers active input, history add/remove/clear, title/author scope, paging, stale/offline, error, empty, IME, and process restoration.
- Bookshelf unifies local and account partitions, reports auth-required/offline/sync states, and performs journaled reversible local mutations plus exact account refresh.
- Downloads observes durable progress after process restart and offers accessible cancellation/retry without losing terminal-state integrity.
- History is a canonical, ordered, recoverable list that opens the existing reader compatibility entry without changing Phase 6 reader scope.
- Compact, medium, expanded, compact-height, resize, separating-hinge, and occluding-hinge tests pass; font scale 2.0 and Simplified/Taiwan/Hong Kong resources do not clip or overlap.
- SearchActivity/SearchResultActivity old callers reach the new route through `LegacyIntentCodec`, while debug/reviewed rollback still reaches the retained legacy implementations.
- The reviewed release default opens `Wenku8ShellActivity`; the build-time `legacy` forward-fix selection remains tested and no production remote kill-switch claim is made.
- A currently authorized anonymous live smoke completes launch -> discover -> ranking/category -> search without account state and prints only bounded operation labels.

## File Structure

### Module and Verification Wiring

- Modify `studio-android/LightNovelLibrary/settings.gradle`: include `:feature:library` once at `feature/library/`.
- Modify `studio-android/LightNovelLibrary/gradle/libs.versions.toml`: use the already pinned Compose, lifecycle, navigation, adaptive, WindowManager, coroutines-test, and screenshot coordinates; add no alternate version.
- Create `studio-android/LightNovelLibrary/feature/library/build.gradle`: Android Compose library, namespace `org.mewx.wenku8.feature.library`, no product flavor.
- Modify `studio-android/LightNovelLibrary/app/build.gradle`: depend on `:feature:library`; add reviewed `wenku8RouteDefault` validation and Phase 4 verification tasks.
- Create `docs/verification/phase-4-library-contract.yaml` and modify `docs/verification/{ui-golden-manifest,modernization-matrix}.yaml`.
- Create `tools/verify-phase4-library.ps1`: source/dependency/ledger/default-route/credential scan.

### Core Models, Contracts, and Data

- Create `core/model/src/main/kotlin/org/mewx/wenku8/core/model/library/{LibraryModels,LibraryStates}.kt`.
- Create `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/library/{LibraryRepository,BookshelfRepository,SearchHistoryRepository,DownloadRepository,ReadingHistoryRepository}.kt`.
- Create `core/data/src/main/java/org/mewx/wenku8/core/data/library/{ProviderLibraryRepository,LibraryModelMapper,LibraryFailureMapper,RoomSearchHistoryRepository,RoomBookshelfRepository,RoomDownloadRepository,RoomReadingHistoryRepository}.kt`.
- Create `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/UserLibraryMutationGateway.kt`.
- Modify Phase 3 `UserLibraryDaos.kt` with observable, bounded queries and journal-compatible update primitives; no second database/store.
- Add the exact focused tests named in Tasks 2-4 under `core:model/src/test/kotlin/org/mewx/wenku8/core/model/library/`, `core:data/src/test/java/org/mewx/wenku8/core/data/library/`, and `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/`.

### Library Feature

- Create `feature/library/src/main/java/org/mewx/wenku8/feature/library/navigation/{LibraryRoute,LibraryEntryPoints}.kt`.
- Create `feature/library/src/main/java/org/mewx/wenku8/feature/library/model/{LoadableUiState,PagedUiState,LibraryUiModels}.kt`.
- Create `feature/library/src/main/java/org/mewx/wenku8/feature/library/component/{NovelList,LibraryStatePane}.kt`.
- Create route packages `discover/`, `search/`, `bookshelf/`, `downloads/`, and `history/`, each with `*UiState.kt`, `*ViewModel.kt`, and `*Screen.kt`.
- Create localized resources under `feature/library/src/main/res/values/`, `values-zh-rTW/`, and `values-zh-rHK/`.
- Add ViewModel unit tests under `feature/library/src/test/java/` and Compose tests under `feature/library/src/androidTest/java/`.

### App Navigation and Compatibility

- Modify `app/src/main/java/org/mewx/wenku8/di/{AppContainer,DefaultAppContainer}.kt` to expose repository interfaces only.
- Modify `app/src/main/java/org/mewx/wenku8/navigation/{AppDestination,ShellUiState,ShellViewModel,Wenku8App,Wenku8NavHost}.kt`.
- Create `app/src/main/java/org/mewx/wenku8/navigation/{AppRoute,AppDeepLink,LibraryRouteFactory,LibraryCompatibilityLauncher,RouteDefaultPolicy}.kt`.
- Move current search implementations to `LegacySearchActivity.kt` and `LegacySearchResultActivity.kt`; replace the original class names with compatibility trampolines.
- Modify `app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt` only to name the already frozen search return type; raw decoding remains in that file.
- Modify `app/src/main/AndroidManifest.xml` and add `LibraryNavigationTest.kt`, `LibraryPredictiveBackTest.kt`, `LibraryResizeTest.kt`, and `SearchTrampolineTest.kt` at the exact paths named in Tasks 11-12.

## Task Dependency Graph

| Task | Depends on | Produces |
| ---: | --- | --- |
| 1 | Phase 1-3 exit gates | Module graph and Phase 4 contract verifier |
| 2 | 1 | Platform-neutral library models and repositories |
| 3 | 2, Phase 2 gateway | Provider-backed discovery/search repository |
| 4 | 2, Phase 3 journal/stores | Canonical search/bookshelf/download/history repositories |
| 5 | 1, 2 | Shared feature state, semantics, and list components |
| 6 | 3, 5 | Discover ViewModel and route |
| 7 | 3, 4, 5 | Search ViewModel, history, and route |
| 8 | 4, 5 | Bookshelf ViewModel and route |
| 9 | 4, 5 | Downloads and history routes |
| 10 | 6-9 | AppContainer and route factories |
| 11 | 10 | Navigation host, deep links, restoration, adaptive/back behavior |
| 12 | 7, 11 | Search compatibility trampolines and rollback entries |
| 13 | 6-12 | Accessibility semantics and assistive-technology journeys |
| 14 | 6-13 | Deterministic screenshot matrix and visual review |
| 15 | 12-14 | Reviewed default rollout and rollback gate |
| 16 | 15 | Anonymous live smoke, evidence map, and Phase 4 exit gate |

Every task follows RED -> minimal implementation -> focused PASS -> affected suite -> `git diff --check` -> isolated commit. A fresh specification reviewer and code-quality reviewer approve the task before a dependent task starts.

## Command Convention

Unless a step says repository root, commands run from `studio-android/LightNovelLibrary` and include `-Pwenku8Provider=public --console=plain --stacktrace --no-parallel`. Android feature modules use `src/main/java`, `src/test/java`, and `src/androidTest/java` Kotlin roots as fixed by the plan index.

### Task 1: Add the Library Feature Module and Freeze the Phase 4 Contract

**Files:**
- Modify: `studio-android/LightNovelLibrary/settings.gradle`
- Modify: `studio-android/LightNovelLibrary/gradle/libs.versions.toml`
- Create: `studio-android/LightNovelLibrary/feature/library/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Create: `docs/verification/phase-4-library-contract.yaml`
- Create: `tools/verify-phase4-library.ps1`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/architecture/Phase4ModuleGraphTest.kt`

- [ ] **Step 1: Write the failing graph and contract tests**

Create `Phase4ModuleGraphTest` to load `settings.gradle`, `feature/library/build.gradle`, and the contract YAML. Assert exactly one `:feature:library` descriptor, Android-library namespace `org.mewx.wenku8.feature.library`, direct dependencies only on `:core:model`, `:core:domain`, and `:core:designsystem`, and exact route IDs/UI-owner IDs from Scope Boundary. Assert the feature build file does not mention `:api-contract`, `:api-public`, `:core:data`, `:core:storage`, `:core:network`, logical `:api`, Room, OkHttp, Jsoup, or legacy packages.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.architecture.Phase4ModuleGraphTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because the feature module and Phase 4 contract do not exist.

- [ ] **Step 2: Add the exact module and dependencies**

Append one descriptor to `settings.gradle`:

```groovy
include ':feature:library'
project(':feature:library').projectDir = new File(rootDir, 'feature/library')
```

Create `feature/library/build.gradle`:

```groovy
plugins {
    id 'com.android.library'
    id 'org.jetbrains.kotlin.plugin.compose'
}

android {
    namespace 'org.mewx.wenku8.feature.library'
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

AGP 9 built-in Kotlin owns Android Kotlin compilation. Do not apply `org.jetbrains.kotlin.android` and do not add legacy `android.kotlinOptions`; Java 17 comes from `compileOptions`, while any Kotlin compiler flags use the AGP 9 built-in Kotlin DSL already established in Phase 1.

Add `implementation project(':feature:library')` to `app/build.gradle`. Reuse the version-catalog aliases established by Phase 1; if an alias is absent, add that exact coordinate once to `libs.versions.toml` and update the Phase 1 dependency lock before continuing.

- [ ] **Step 3: Check in the executable contract and verifier**

Create `phase-4-library-contract.yaml` with this complete semantic content:

```yaml
schemaVersion: 1
phase: 4
owners: [A01, A02, A03, F01, F02, F03, F04, F05]
routes:
  - library/discover
  - library/discover/latest
  - library/discover/completed
  - library/discover/ranking
  - library/discover/category
  - library/discover/tag
  - library/search
  - library/search/results
  - library/bookshelf
  - library/bookshelf/downloads
  - library/bookshelf/history
stateFamilies: [loading, content, empty, refreshing, appendLoading, recoverableError, offline, authRequired, end]
viewports: [compact-360x640, phone-412x915, landscape-915x412, tablet-800x1280, expanded-1280x800]
legacyEntries:
  SearchActivity: trampoline
  SearchResultActivity: trampoline
  LegacySearchActivity: rollback
  LegacySearchResultActivity: rollback
defaultGate: behavior+state+compatibility+accessibility+adaptive+screenshot+rollback
```

Create `tools/verify-phase4-library.ps1` to load the YAML, compare its route/owner sets with `ui-owner-action-ledger.yaml`, scan feature production Kotlin for forbidden imports/calls, scan source/resources/reports for credential variable values, and verify no manifest component is newly exported. It must print only `PHASE4-LIBRARY-PASS` on success and throw with relative path plus line on failure.

- [ ] **Step 4: Run focused and affected graph checks**

```powershell
.\gradlew.bat :feature:library:assembleDebug :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.architecture.Phase4ModuleGraphTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
& .\tools\verify-phase4-library.ps1
git diff --check
```

Expected: `BUILD SUCCESSFUL` and `PHASE4-LIBRARY-PASS`; the feature dependency report has no provider/storage/network implementation.

- [ ] **Step 5: Commit the module contract**

```powershell
git add studio-android/LightNovelLibrary/settings.gradle studio-android/LightNovelLibrary/gradle/libs.versions.toml studio-android/LightNovelLibrary/feature/library/build.gradle studio-android/LightNovelLibrary/app/build.gradle studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/architecture/Phase4ModuleGraphTest.kt docs/verification/phase-4-library-contract.yaml tools/verify-phase4-library.ps1
git diff --check --cached
git commit -m "build: add phase four library module"
```

### Task 2: Define Platform-Neutral Library Models and Repository Contracts

**Files:**
- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/library/LibraryModels.kt`
- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/library/LibraryStates.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/library/LibraryRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/library/SearchHistoryRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/library/BookshelfRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/library/DownloadRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/library/ReadingHistoryRepository.kt`
- Test: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/library/LibraryModelsTest.kt`
- Test: `studio-android/LightNovelLibrary/core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/library/LibraryContractsTest.kt`

- [ ] **Step 1: Write failing invariant and dependency tests**

Test positive pages, nonblank IDs/titles, ordered unique keys, normalized nonblank search text, 0..1 download progress, terminal download actions, account/local partition separation, immutable copies, `BookshelfCapabilitySnapshot` value semantics, and `LibraryFailure` redaction. Reflect repository signatures and reject Android, API-contract/provider, Room, DataStore, file, URL, Cookie, and legacy types; in particular the public contract must not mention `ProviderCapability` or `ProviderCapabilities`.

Run:

```powershell
.\gradlew.bat :core:model:test :core:domain:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL on unresolved library types.

- [ ] **Step 2: Add immutable model and outcome types**

Create the two model files with these exact public shapes:

```kotlin
package org.mewx.wenku8.core.model.library

import org.mewx.wenku8.core.model.identity.NovelKey

enum class LibraryFreshness { FRESH, STALE }
enum class LibrarySearchScope { TITLE, AUTHOR }
enum class DiscoveryKind { HOME, LATEST, COMPLETED, CATEGORY, RANKING, TAG }
enum class RankingWindow { ALL_TIME, MONTH, WEEK, DAY }

data class LibraryNovel(
    val key: NovelKey,
    val title: String,
    val author: String?,
    val coverKey: String?,
) {
    init { require(title.isNotBlank()) }
}

data class LibrarySection(val id: String, val title: String, val novels: List<LibraryNovel>) {
    init { require(id.isNotBlank() && title.isNotBlank()) }
}

data class LibraryPage<T>(val items: List<T>, val page: Int, val nextPage: Int?) {
    init { require(page > 0); require(nextPage == null || nextPage > page) }
}

data class DiscoveryRequest(
    val kind: DiscoveryKind,
    val page: Int,
    val categoryOrTagId: String? = null,
    val rankingWindow: RankingWindow? = null,
) {
    init {
        require(page > 0)
        require(kind != DiscoveryKind.CATEGORY && kind != DiscoveryKind.TAG || !categoryOrTagId.isNullOrBlank())
        require(kind == DiscoveryKind.RANKING || rankingWindow == null)
    }
}

data class SearchRequest(val text: String, val scope: LibrarySearchScope, val page: Int) {
    init { require(text.trim().isNotEmpty() && page > 0) }
}

data class SearchHistoryItem(val id: String, val displayText: String, val scope: LibrarySearchScope, val sortIndex: Int) {
    init { require(id.isNotBlank() && displayText.isNotBlank() && sortIndex >= 0) }
}

enum class BookshelfPartition { LOCAL, ACCOUNT }
data class BookshelfItem(val novel: LibraryNovel, val groupId: String, val serverEntryId: String?, val sortIndex: Int)
data class BookshelfGroup(val id: String, val title: String, val items: List<BookshelfItem>)
data class BookshelfSnapshot(
    val partition: BookshelfPartition,
    val groups: List<BookshelfGroup>,
    val freshness: LibraryFreshness,
    val pendingProjectionCount: Int,
)

data class BookshelfCapabilitySnapshot(
    val accountReadAvailable: Boolean,
    val accountMutationAvailable: Boolean,
)

enum class DownloadStatus { QUEUED, RUNNING, PAUSED, CANCELLED, FAILED, SUCCEEDED }
data class DownloadItem(
    val workKey: String,
    val novelKey: NovelKey,
    val chapterRemoteId: String,
    val status: DownloadStatus,
    val transferredBytes: Long,
    val expectedBytes: Long?,
    val retryCount: Int,
) {
    init { require(workKey.isNotBlank() && chapterRemoteId.isNotBlank() && transferredBytes >= 0 && (expectedBytes == null || expectedBytes >= transferredBytes)) }
    val fraction: Float? get() = expectedBytes?.takeIf { it > 0 }?.let { transferredBytes.toFloat() / it.toFloat() }
}

data class ReadingHistoryItem(
    val novel: LibraryNovel,
    val chapterRemoteId: String,
    val chapterTitle: String?,
    val updatedAtEpochMillis: Long,
) {
    init { require(chapterRemoteId.isNotBlank() && updatedAtEpochMillis >= 0) }
}
```

```kotlin
package org.mewx.wenku8.core.model.library

sealed interface LibraryFailure {
    data object Offline : LibraryFailure
    data object AuthenticationRequired : LibraryFailure
    data object SessionExpired : LibraryFailure
    data object NotFound : LibraryFailure
    data class RateLimited(val retryAfterSeconds: Long?) : LibraryFailure
    data class Storage(val operationCode: String) : LibraryFailure
    data class Parse(val operationCode: String) : LibraryFailure
    data class Network(val operationCode: String) : LibraryFailure
    data class Unsupported(val operationCode: String) : LibraryFailure
    data class InvalidInput(val field: String) : LibraryFailure
}

sealed interface LibraryResult<out T> {
    data class Data<T>(val value: T, val freshness: LibraryFreshness) : LibraryResult<T>
    data class Failure(val reason: LibraryFailure) : LibraryResult<Nothing>
}

enum class MutationSyncState { SYNCHRONIZED, PENDING_PROJECTION }
sealed interface LibraryMutationResult {
    data class Success(val sync: MutationSyncState) : LibraryMutationResult
    data class Failure(val reason: LibraryFailure) : LibraryMutationResult
}
```

- [ ] **Step 3: Add repository boundaries with complete methods**

```kotlin
package org.mewx.wenku8.core.domain.library

import kotlinx.coroutines.flow.Flow
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.library.*

interface LibraryRepository {
    suspend fun home(): LibraryResult<List<LibrarySection>>
    suspend fun discover(request: DiscoveryRequest): LibraryResult<LibraryPage<LibraryNovel>>
    suspend fun categories(): LibraryResult<List<Pair<String, String>>>
    suspend fun tags(groupId: String?): LibraryResult<List<Pair<String, String>>>
    suspend fun search(request: SearchRequest): LibraryResult<LibraryPage<LibraryNovel>>
}

interface SearchHistoryRepository {
    fun observe(): Flow<List<SearchHistoryItem>>
    suspend fun record(text: String, scope: LibrarySearchScope): LibraryMutationResult
    suspend fun remove(id: String): LibraryMutationResult
    suspend fun clear(): LibraryMutationResult
}

interface BookshelfRepository {
    val capabilities: BookshelfCapabilitySnapshot
    fun observeLocal(): Flow<BookshelfSnapshot>
    suspend fun refreshAccount(): LibraryResult<BookshelfSnapshot>
    suspend fun addLocal(key: NovelKey): LibraryMutationResult
    suspend fun removeLocal(key: NovelKey): LibraryMutationResult
    suspend fun addAccount(key: NovelKey, groupId: String?): LibraryMutationResult
    suspend fun removeAccount(serverEntryId: String, groupId: String): LibraryMutationResult
}

interface DownloadRepository {
    fun observe(): Flow<List<DownloadItem>>
    suspend fun cancel(workKey: String): LibraryMutationResult
    suspend fun retry(workKey: String): LibraryMutationResult
}

interface ReadingHistoryRepository {
    fun observe(limit: Int = 100): Flow<List<ReadingHistoryItem>>
}
```

`BookshelfCapabilitySnapshot` is an immutable domain projection, not an API/provider object. Do not expose `ProviderCapability`, `ProviderCapabilities`, source facets, or transport freshness from `:core:domain`. `LibraryFailure.toString()` contains only enum/type and bounded operation code because none of its fields accept a URL, query, body, account ID, or trace ID.

- [ ] **Step 4: Run invariant, API, and dependency checks**

```powershell
.\gradlew.bat :core:model:test :core:domain:test :core:model:dependencies :core:domain:dependencies -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
rg -n "android\.|androidx\.|okhttp|jsoup|Room|Dao|DataStore|Wenku8API|GlobalConfig|api\.contract" core\model\src\main core\domain\src\main -g '*.kt'
git diff --check
```

Expected: tests PASS; the source scan exits 1; `:core:model` remains standard-library-only and `:core:domain` adds only its already approved coroutine/API dependency graph.

- [ ] **Step 5: Commit the library contracts**

```powershell
git add core/model/src core/domain/src
git diff --check --cached
git commit -m "feat(domain): define library route contracts"
```

### Task 3: Implement Provider-Backed Discovery and Search

**Files:**
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/library/LibraryModelMapper.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/library/LibraryFailureMapper.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/library/ProviderLibraryRepository.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/library/ProviderLibraryRepositoryTest.kt`

- [ ] **Step 1: Write failing mapping, request, cache, and cancellation tests**

Cover home order; latest/completed/category/ranking/tag request mapping; title/author search; next-page propagation; `ContentLanguage` from settings; fresh/stale mapping; every Phase 2 typed failure; empty success; 20 concurrent identical reads dispatch once through `CachedProviderGateway`; changed query/filter produces distinct keys; cancellation rethrows and never becomes `LibraryFailure`.

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.library.ProviderLibraryRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because the repository and mappers are absent.

- [ ] **Step 2: Add exact model and failure mappings**

`LibraryModelMapper` converts the Phase 1-owned core catalog values returned by the corrected Phase 2 facets into library-route presentation values, preserving source ID, remote ID, order, title, author, and a non-sensitive cover cache key derived from source/remote ID. It never creates a second catalog identity and never passes a canonical URL into feature state.

```kotlin
internal fun org.mewx.wenku8.core.model.catalog.NovelSummary.toLibrary() = LibraryNovel(
    key = key,
    title = title,
    author = author,
    coverKey = cover?.let { "${key.sourceId.value}:${key.remoteId}:cover" },
)

internal fun org.mewx.wenku8.api.contract.Freshness.toDomain() = when (this) {
    org.mewx.wenku8.api.contract.Freshness.FRESH -> LibraryFreshness.FRESH
    org.mewx.wenku8.api.contract.Freshness.STALE -> LibraryFreshness.STALE
}
```

`LibraryFailureMapper` exhaustively maps `ApiFailure.Network.Offline` to `Offline`, auth failures to `AuthenticationRequired`/`SessionExpired`, and every remaining subtype to bounded operation-coded domain failures. It rethrows `CancellationException` before mapping.

- [ ] **Step 3: Implement the repository over the existing cached gateway**

```kotlin
class ProviderLibraryRepository(
    private val gateway: org.mewx.wenku8.core.data.provider.CachedProviderGateway,
    private val language: () -> org.mewx.wenku8.core.model.settings.ContentLanguage,
) : LibraryRepository {
    override suspend fun home() = gateway.home().mapResult("home") { sections ->
        sections.map { LibrarySection(it.id, it.title, it.novels.map { novel -> novel.toLibrary() }) }
    }

    override suspend fun discover(request: DiscoveryRequest) = when (request.kind) {
        DiscoveryKind.TAG -> gateway.novelsByTag(request.toTagRequest(language()))
        DiscoveryKind.HOME -> error("HOME uses home()")
        else -> gateway.browse(request.toBrowseRequest(language()))
    }.mapResult("discover-${request.kind.name.lowercase()}") { page ->
        LibraryPage(page.items.map { it.toLibrary() }, page.currentPage, page.nextPage)
    }

    override suspend fun categories() = gateway.tags(
        org.mewx.wenku8.api.contract.TagDiscoveryRequest(null, language())
    ).mapResult("categories") { tags -> tags.map { it.id to it.label } }

    override suspend fun tags(groupId: String?) = gateway.tags(
        org.mewx.wenku8.api.contract.TagDiscoveryRequest(groupId, language())
    ).mapResult("tags") { tags -> tags.map { it.id to it.label } }

    override suspend fun search(request: SearchRequest) = gateway.search(request.toProvider(language()))
        .mapResult("search") { page -> LibraryPage(page.items.map { it.toLibrary() }, page.currentPage, page.nextPage) }
}
```

Add pure request mappers covering all enum branches. API-contract request/envelope types import the Phase 1 core model identities; they never redeclare them. `mapResult` preserves Phase 2 response freshness and never logs trace IDs, queries, URLs, or response values. The existing Phase 2 gateway remains repository-owned cache/single-flight authority; do not add a second cache.

- [ ] **Step 4: Run repository and Phase 2 cache regression suites**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.library.*" --tests "org.mewx.wenku8.core.data.provider.*" :api-contract:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; cancellation remains cancellation, stale values are explicit, and concurrent identical reads dispatch once.

- [ ] **Step 5: Commit provider-backed library reads**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/library core/data/src/test/java/org/mewx/wenku8/core/data/library
git diff --check --cached
git commit -m "feat(data): add discovery and search repository"
```

### Task 4: Implement Canonical Search, Bookshelf, Download, and History Repositories

**Files:**
- Modify: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryDaos.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/UserLibraryMutationGateway.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/library/{RoomSearchHistoryRepository,RoomBookshelfRepository,RoomDownloadRepository,RoomReadingHistoryRepository}.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/LibraryDaoQueriesTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/library/RoomSearchHistoryRepositoryTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/library/RoomBookshelfRepositoryTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/library/RoomDownloadRepositoryTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/library/RoomReadingHistoryRepositoryTest.kt`

- [ ] **Step 1: Write failing observable-store, journal, account-epoch, and transfer-control tests**

Test deterministic ordering; history capped to positive limit; search add/remove/clear delegates through `JournaledMutationRunner`; local bookshelf add/remove writes canonical plus rollback projection; all four Phase 2 `BOOKSHELF_READ`/`BOOKSHELF_MUTATE` combinations map to the exact domain snapshot; unavailable account refresh/add/remove returns `Unsupported("bookshelf-read")` or `Unsupported("bookshelf-mutate")` before the gateway/request counter changes; account refresh replaces only the current opaque account partition and rejects completion after epoch change; missing cached metadata produces a stable fallback label; downloads survive repository recreation; cancel delegates once then observes `CANCELLED`; retry accepts only `FAILED`/`CANCELLED`, sets `QUEUED`, and invokes `TransferScheduler` once; terminal success cannot be retried or overwritten.

Run:

```powershell
.\gradlew.bat :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.LibraryDaoQueriesTest :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.library.Room*RepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because observable queries, mutation gateway, and repositories are absent.

- [ ] **Step 2: Add bounded observable DAO methods without changing frozen columns**

Add these methods to the existing Phase 3 DAOs:

```kotlin
@Query("SELECT * FROM search_history ORDER BY sortIndex, entryId")
fun observeAll(): kotlinx.coroutines.flow.Flow<List<SearchHistoryEntity>>

@Query("SELECT * FROM bookshelf_membership WHERE partitionKey = :partition ORDER BY sortIndex, novelRemoteId")
fun observePartition(partition: String): kotlinx.coroutines.flow.Flow<List<BookshelfMembershipEntity>>

@Query("SELECT * FROM reader_progress ORDER BY updatedAtEpochMillis DESC LIMIT :limit")
fun observeRecent(limit: Int): kotlinx.coroutines.flow.Flow<List<ReaderProgressEntity>>

@Query("SELECT * FROM download ORDER BY CASE state WHEN 'RUNNING' THEN 0 WHEN 'QUEUED' THEN 1 WHEN 'PAUSED' THEN 2 WHEN 'FAILED' THEN 3 WHEN 'CANCELLED' THEN 4 ELSE 5 END, workKey")
fun observeAllDownloads(): kotlinx.coroutines.flow.Flow<List<DownloadEntity>>

@Query("UPDATE download SET state = :target, retryCount = retryCount + 1, stopReason = NULL WHERE workKey = :workKey AND state IN ('FAILED','CANCELLED') AND terminalCommitCount = 0")
suspend fun markRetryQueued(workKey: String, target: DownloadState = DownloadState.QUEUED): Int

@Query("UPDATE download SET state = 'CANCELLED' WHERE workKey = :workKey AND state IN ('QUEUED','RUNNING','PAUSED') AND terminalCommitCount = 0")
suspend fun markCancelled(workKey: String): Int
```

Require `limit in 1..500` in the storage repository before `observeRecent`; do not interpolate SQL.

- [ ] **Step 3: Add one journal-backed mutation gateway**

```kotlin
package org.mewx.wenku8.core.storage.migration

class UserLibraryMutationGateway(
    private val runner: JournaledMutationRunner,
    private val factory: UserLibraryMutationFactory,
) {
    suspend fun recordSearch(text: String, scope: String) = runner.run(factory.recordSearch(text, scope))
    suspend fun removeSearch(id: String) = runner.run(factory.removeSearch(id))
    suspend fun clearSearch() = runner.run(factory.clearSearch())
    suspend fun addLocalBook(sourceId: String, novelId: String, legacyAid: Int?) =
        runner.run(factory.addLocalBook(sourceId, novelId, legacyAid))
    suspend fun removeLocalBook(sourceId: String, novelId: String) =
        runner.run(factory.removeLocalBook(sourceId, novelId))
}

interface UserLibraryMutationFactory {
    fun recordSearch(text: String, scope: String): PendingMutation
    fun removeSearch(id: String): PendingMutation
    fun clearSearch(): PendingMutation
    fun addLocalBook(sourceId: String, novelId: String, legacyAid: Int?): PendingMutation
    fun removeLocalBook(sourceId: String, novelId: String): PendingMutation
}
```

The factory normalizes text, caps retained history to the frozen legacy maximum from Phase 0 evidence, creates a unique mutation ID, uses the existing `search`/`bookshelf` participants, and keeps legacy projection byte semantics. Modify app `LegacyStorageGateway` to delegate its matching methods to this gateway so there remains one journal sequence and one writer barrier.

- [ ] **Step 4: Implement repositories and exact failure behavior**

`RoomSearchHistoryRepository` maps `observeAll()`, and every write maps `MutationOutcome.Synchronized`/`PendingSynchronization` to `MutationSyncState`. `RoomBookshelfRepository` receives the immutable Phase 2 `ProviderCapabilities` only inside `:core:data` and projects it once at construction:

```kotlin
override val capabilities = BookshelfCapabilitySnapshot(
    accountReadAvailable = providerCapabilities.supports(ProviderCapability.BOOKSHELF_READ),
    accountMutationAvailable = providerCapabilities.supports(ProviderCapability.BOOKSHELF_MUTATE),
)
```

The implementation checks the snapshot before every account gateway call; a false value returns the bounded `LibraryFailure.Unsupported` code with zero gateway/network work. It combines local rows with catalog metadata and uses a stable `Novel #<remoteId>` resource-format fallback only when metadata is absent. An enabled account refresh calls the Phase 2 account source through the cached gateway, captures account ID+epoch before dispatch, rechecks epoch before one Room transaction replaces that partition, and never changes local rows.

`RoomDownloadRepository` uses this control sequence:

```kotlin
override suspend fun cancel(workKey: String): LibraryMutationResult {
    scheduler.cancel(workKey)
    return if (downloadDao.markCancelled(workKey) == 1) success() else invalid("download-state")
}

override suspend fun retry(workKey: String): LibraryMutationResult {
    val row = downloadDao.get(workKey) ?: return invalid("work-key")
    if (row.state !in setOf(DownloadState.FAILED, DownloadState.CANCELLED) || row.terminalCommitCount != 0) return invalid("download-state")
    if (downloadDao.markRetryQueued(workKey) != 1) return invalid("download-race")
    scheduler.schedule(scheduleRequestFactory.retry(row))
    return success()
}
```

If scheduler submission fails after `QUEUED`, return `Storage("download-schedule")`; the durable queued row remains visible and Phase 3 reconciliation retries it. `RoomReadingHistoryRepository` combines `ReaderProgressDao.observeRecent()` with catalog metadata and never reads a legacy file directly.

- [ ] **Step 5: Run storage, migration, transfer, and repository suites**

```powershell
.\gradlew.bat :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.LibraryDaoQueriesTest :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.*" :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.library.*" --tests "org.mewx.wenku8.core.data.transfer.*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; old projection remains readable, account epoch races are rejected, and exactly one scheduler call follows an accepted retry.

- [ ] **Step 6: Commit canonical library repositories**

```powershell
git add core/storage/src core/data/src app/src/main/java/org/mewx/wenku8/compat/storage/LegacyStorageGateway.kt
git diff --check --cached
git commit -m "feat(data): expose canonical library repositories"
```

### Task 5: Build Shared Library State and Material 3 Components

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/library/src/main/java/org/mewx/wenku8/feature/library/model/{LoadableUiState,PagedUiState,LibraryUiModels}.kt`
- Create: `studio-android/LightNovelLibrary/feature/library/src/main/java/org/mewx/wenku8/feature/library/component/{NovelList,LibraryStatePane}.kt`
- Create: localized `feature/library/src/main/res/values*/strings.xml`
- Test: `feature/library/src/test/java/org/mewx/wenku8/feature/library/model/PagedUiStateTest.kt`
- Test: `feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/component/LibraryComponentsTest.kt`

- [ ] **Step 1: Write failing state-transition and semantics tests**

Test initial loading, content, empty, refresh-with-content, append-with-content, recoverable error preserving content, explicit stale/offline banner, auth-required action, end state, stable item keys, heading/collection semantics, 48dp actions, localized icon descriptions, and font scale 2.0. Reject nested cards, clickable text without role, and a Toast-only failure.

Run:

```powershell
.\gradlew.bat :feature:library:testDebugUnitTest :feature:library:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.library.component.LibraryComponentsTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because feature state/components are absent.

- [ ] **Step 2: Define exhaustive UI state**

```kotlin
sealed interface LoadableUiState<out T> {
    data object InitialLoading : LoadableUiState<Nothing>
    data class Content<T>(val value: T, val stale: Boolean = false, val refreshing: Boolean = false) : LoadableUiState<T>
    data object Empty : LoadableUiState<Nothing>
    data class RecoverableError<T>(val previous: T?, val message: UiMessage) : LoadableUiState<T>
    data class AuthRequired<T>(val previous: T?) : LoadableUiState<T>
}

data class PagedUiState<T>(
    val items: List<T> = emptyList(),
    val initialLoading: Boolean = false,
    val refreshing: Boolean = false,
    val appendLoading: Boolean = false,
    val nextPage: Int? = null,
    val stale: Boolean = false,
    val error: UiMessage? = null,
) {
    init { require(!(initialLoading && items.isNotEmpty())); require(!(appendLoading && nextPage == null)) }
    val endReached: Boolean get() = items.isNotEmpty() && nextPage == null && !initialLoading
}

@JvmInline value class UiMessage(val resourceName: String)
```

Route-specific state stores selected filters, scroll index/offset, and selected item key; no route stores a provider URL/body/trace ID.

- [ ] **Step 3: Implement shared unframed list and state panes**

`NovelList` uses `LazyColumn` plus Material 3 `ListItem`, stable `NovelKey`, a bounded cover slot that renders repository-provided state or a decorative fallback icon, title/author semantics, and one click callback. `LibraryStatePane` delegates loading/empty/error/offline/auth to Phase 1 `StateContent`, preserves previous content under refresh/error, and uses `SnackbarHost` for one-time mutation acknowledgement only. All visible strings come from the three resource sets.

- [ ] **Step 4: Run component and design-system regressions**

```powershell
.\gradlew.bat :feature:library:testDebugUnitTest :feature:library:connectedDebugAndroidTest :core:designsystem:connectedDebugAndroidTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS at light/dark and font scale 1.0/2.0; no clipped action, missing semantics, nested card, or hard-coded product string.

- [ ] **Step 5: Commit shared feature primitives**

```powershell
git add feature/library/src/main feature/library/src/test feature/library/src/androidTest
git diff --check --cached
git commit -m "feat(ui): add library state components"
```

### Task 6: Implement Discover State, Paging, and Adaptive Material 3 UI

**Files:**
- Create: `feature/library/src/main/java/org/mewx/wenku8/feature/library/discover/{DiscoverUiState,DiscoverViewModel,DiscoverScreen}.kt`
- Test: `feature/library/src/test/java/org/mewx/wenku8/feature/library/discover/DiscoverViewModelTest.kt`
- Test: `feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/discover/DiscoverScreenTest.kt`

- [ ] **Step 1: Write failing ViewModel and screen tests**

Test home load; kind/ranking/category/tag selection; refresh preserving content; append once at a time; duplicate-page rejection; empty; stale/offline; parser/network error and retry; cancellation on filter change; SavedStateHandle restoration of kind/filter/page/scroll; open-novel one-shot effect; tabs/chips selected semantics; compact and expanded layouts; hinge traversal; pull-to-refresh; DPAD focus order.

Run:

```powershell
.\gradlew.bat :feature:library:testDebugUnitTest --tests "org.mewx.wenku8.feature.library.discover.DiscoverViewModelTest" :feature:library:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.library.discover.DiscoverScreenTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because the Discover route is absent.

- [ ] **Step 2: Implement immutable state and bounded effects**

```kotlin
data class DiscoverUiState(
    val kind: DiscoveryKind = DiscoveryKind.HOME,
    val ranking: RankingWindow = RankingWindow.ALL_TIME,
    val categoryOrTagId: String? = null,
    val sections: LoadableUiState<List<LibrarySection>> = LoadableUiState.InitialLoading,
    val novels: PagedUiState<LibraryNovel> = PagedUiState(),
    val choices: List<Pair<String, String>> = emptyList(),
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
)

sealed interface DiscoverEffect { data class OpenNovel(val key: NovelKey, val title: String) : DiscoverEffect }
```

`DiscoverViewModel` receives only `LibraryRepository`, `SavedStateHandle`, and `AppDispatchers`; uses `viewModelScope`; owns one active load Job; updates state before dispatch; writes only bounded enum/ID/page/scroll values to SavedStateHandle; sends effects through `Channel(capacity = 8)`; and rethrows cancellation. A refresh keeps current content and an append failure keeps all loaded items.

- [ ] **Step 3: Implement the Material 3 discover screen**

Use `Wenku8Scaffold`, `TopAppBar`, `PrimaryTabRow` for home/latest/rankings/completed, `FilterChip` for ranking/category/tag choices, Material 3 pull-to-refresh, and `NovelList`. Expanded width places choice navigation and results in hinge-safe regions; compact width uses one pane. No hero, gradient, floating section card, nested card, or fixed-width side panel is added.

- [ ] **Step 4: Verify states, semantics, and restoration**

```powershell
.\gradlew.bat :feature:library:testDebugUnitTest --tests "org.mewx.wenku8.feature.library.discover.*" :feature:library:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.library.discover.DiscoverScreenTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS for every state and adaptive case; filter changes cancel old work and restored state produces no duplicate request.

- [ ] **Step 5: Commit Discover**

```powershell
git add feature/library/src/main/java/org/mewx/wenku8/feature/library/discover feature/library/src/test/java/org/mewx/wenku8/feature/library/discover feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/discover
git diff --check --cached
git commit -m "feat(ui): add adaptive discovery route"
```

### Task 7: Implement Search, History, Filters, and Paging

**Files:**
- Create: `feature/library/src/main/java/org/mewx/wenku8/feature/library/search/{SearchUiState,SearchViewModel,SearchScreen}.kt`
- Test: `feature/library/src/test/java/org/mewx/wenku8/feature/library/search/SearchViewModelTest.kt`
- Test: `feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/search/SearchScreenTest.kt`

- [ ] **Step 1: Write failing query/history/form/paging tests**

Cover blank submit field error with zero repository call; title/author scope; history load/select/remove/clear; provider byte-limit failure surfaced inline; submit records history before/alongside results with pending-projection state; paging/end; stale/offline/error/retry; rapid query cancellation; duplicate-submit prevention; input/results preserved after failure; IME search/dismiss/focus order; process restoration of query/scope/results/page/scroll; legacy deep-link initial query; font 2.0 and compact-height landscape.

Run:

```powershell
.\gradlew.bat :feature:library:testDebugUnitTest --tests "org.mewx.wenku8.feature.library.search.SearchViewModelTest" :feature:library:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.library.search.SearchScreenTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because the Search route is absent.

- [ ] **Step 2: Define form and results state**

```kotlin
data class SearchUiState(
    val query: String = "",
    val scope: LibrarySearchScope = LibrarySearchScope.TITLE,
    val queryError: UiMessage? = null,
    val submitting: Boolean = false,
    val history: List<SearchHistoryItem> = emptyList(),
    val historySyncPending: Boolean = false,
    val results: PagedUiState<LibraryNovel> = PagedUiState(),
    val hasSubmitted: Boolean = false,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
)

sealed interface SearchEffect {
    data class OpenNovel(val key: NovelKey, val title: String) : SearchEffect
    data object FocusQuery : SearchEffect
    data class Announce(val message: UiMessage) : SearchEffect
}
```

`SearchViewModel` observes canonical history in `viewModelScope`, trims display input only at submit, validates before calling repositories, records history using `SearchHistoryRepository`, and owns one search Job. Failure never clears query/results. `SavedStateHandle` stores query, scope, submitted flag, current page, and scroll values; it stores no provider response body.

- [ ] **Step 3: Implement Material 3 search and results UI**

Use Material 3 `SearchBar` on compact width and `DockedSearchBar` where expanded width permits, a two-option `SingleChoiceSegmentedButtonRow` for title/author, `InputChip` history entries with remove icons/tooltips, clear-history command, `NovelList`, explicit paging progress/end, inline query error, and `SnackbarHost`. IME action calls submit once and the Back order is search suggestions/keyboard before route pop.

- [ ] **Step 4: Run search/history and legacy-codec-independent tests**

```powershell
.\gradlew.bat :feature:library:testDebugUnitTest --tests "org.mewx.wenku8.feature.library.search.*" :feature:library:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.library.search.SearchScreenTest :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.library.RoomSearchHistoryRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; history rollback projection remains synchronized/pending explicitly and failed search retains user input.

- [ ] **Step 5: Commit Search**

```powershell
git add feature/library/src/main/java/org/mewx/wenku8/feature/library/search feature/library/src/test/java/org/mewx/wenku8/feature/library/search feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/search
git diff --check --cached
git commit -m "feat(ui): add searchable library route"
```

### Task 8: Implement Unified Local and Account Bookshelf

**Files:**
- Create: `feature/library/src/main/java/org/mewx/wenku8/feature/library/bookshelf/{BookshelfUiState,BookshelfViewModel,BookshelfScreen}.kt`
- Test: `feature/library/src/test/java/org/mewx/wenku8/feature/library/bookshelf/BookshelfViewModelTest.kt`
- Test: `feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/bookshelf/BookshelfScreenTest.kt`

- [ ] **Step 1: Write failing local/account/sync/auth tests**

Cover local content/empty; all four `BookshelfCapabilitySnapshot` combinations on the first rendered frame; account tab absent with read unavailable and zero repository refresh calls; read-only account content with every mutation control absent and zero mutation calls; account auth-required; account content/empty; refresh; stale offline account content; session-expiry recovery; local remove confirmation and projection pending; account add/remove exact entry/group ID; operation disabled while submitting; refresh completion after epoch change rejected; selected tab/list position process restoration; open novel; login effect; TalkBack/keyboard/DPAD/Switch Access; resize and hinge behavior.

Run the focused feature tests. Expected: FAIL because bookshelf state/UI are absent.

- [ ] **Step 2: Implement state and ViewModel**

```kotlin
data class BookshelfUiState(
    val capabilities: BookshelfCapabilitySnapshot,
    val selected: BookshelfPartition = BookshelfPartition.LOCAL,
    val local: LoadableUiState<BookshelfSnapshot> = LoadableUiState.InitialLoading,
    val account: LoadableUiState<BookshelfSnapshot> = LoadableUiState.InitialLoading,
    val mutationInFlight: NovelKey? = null,
    val pendingRemoval: BookshelfItem? = null,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
)

sealed interface BookshelfEffect {
    data class OpenNovel(val key: NovelKey, val title: String, val local: Boolean) : BookshelfEffect
    data object RequestLogin : BookshelfEffect
    data class Notify(val message: UiMessage) : BookshelfEffect
}
```

The ViewModel reads `repository.capabilities` synchronously and places it in the initial `StateFlow` value before the route can compose. When account read is unavailable it coerces restored `ACCOUNT` selection to `LOCAL`, never launches account refresh, and rejects account events before starting a coroutine. When account mutation is unavailable it may render enabled account reads but never exposes or dispatches add/remove events. Enabled flows continue to collect local canonical state, refresh account only on explicit entry/refresh, map auth failures to `AuthRequired`, preserve stale account content offline, and serialize mutation taps. Removing local content changes membership only; it never deletes downloaded chapters/images in Phase 4.

- [ ] **Step 3: Implement adaptive bookshelf UI**

Use `PrimaryTabRow` for Local/Account only when `accountReadAvailable`; otherwise render the Local route without an empty/disabled Account affordance. Pull-to-refresh is present only for supported account reads, and account add/remove menus are present only when `accountMutationAvailable`; this gating comes from the initial state, so unsupported controls never flash for one frame. Use group headings, `ListItem` rows, icon menu/remove actions with tooltips, `AlertDialog` confirmation, explicit stale/pending-projection status, and an auth-required login button. Expanded mode may show groups and selected group in two hinge-safe regions; compact remains one pane. Back closes the confirmation dialog before route navigation.

- [ ] **Step 4: Verify feature, migration, and account-epoch behavior**

```powershell
.\gradlew.bat :feature:library:testDebugUnitTest --tests "org.mewx.wenku8.feature.library.bookshelf.*" :feature:library:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.library.bookshelf.BookshelfScreenTest :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.library.RoomBookshelfRepositoryTest" :core:storage:testDebugUnitTest --tests "*BookshelfMigration*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; local/account partitions never overwrite each other and old rollback reads remain current.

- [ ] **Step 5: Commit Bookshelf**

```powershell
git add feature/library/src/main/java/org/mewx/wenku8/feature/library/bookshelf feature/library/src/test/java/org/mewx/wenku8/feature/library/bookshelf feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/bookshelf
git diff --check --cached
git commit -m "feat(ui): add unified bookshelf route"
```

### Task 9: Implement Durable Downloads and Reading History Routes

**Files:**
- Create: `feature/library/src/main/java/org/mewx/wenku8/feature/library/downloads/{DownloadsUiState,DownloadsViewModel,DownloadsScreen}.kt`
- Create: `feature/library/src/main/java/org/mewx/wenku8/feature/library/history/{HistoryUiState,HistoryViewModel,HistoryScreen}.kt`
- Test: `feature/library/src/test/java/org/mewx/wenku8/feature/library/downloads/DownloadsViewModelTest.kt`
- Test: `feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/downloads/DownloadsScreenTest.kt`
- Test: `feature/library/src/test/java/org/mewx/wenku8/feature/library/history/HistoryViewModelTest.kt`
- Test: `feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/history/HistoryScreenTest.kt`

- [ ] **Step 1: Write failing durable-state and UI tests**

Downloads: loading/content/empty/error, determinate/indeterminate progress, queued/running/paused/failed/cancelled/succeeded labels, confirmation before cancel, retry only failed/cancelled, duplicate-tap lockout, repository recreation/process restart, generic disclosure text, 48dp icon actions, progress semantics/live region. History: loading/content/empty/error, newest-first, bounded rows, missing metadata fallback, open-reader effect, process-restored scroll, offline availability semantics.

Run focused feature and repository tests. Expected: FAIL because routes are absent.

- [ ] **Step 2: Implement ViewModel state and effects**

```kotlin
data class DownloadsUiState(
    val items: LoadableUiState<List<DownloadItem>> = LoadableUiState.InitialLoading,
    val pendingCancel: DownloadItem? = null,
    val commandWorkKey: String? = null,
)
sealed interface DownloadsEffect { data class Notify(val message: UiMessage) : DownloadsEffect }

data class HistoryUiState(
    val items: LoadableUiState<List<ReadingHistoryItem>> = LoadableUiState.InitialLoading,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
)
sealed interface HistoryEffect { data class OpenReader(val item: ReadingHistoryItem) : HistoryEffect }
```

Both ViewModels receive repository interfaces only. Downloads collects durable Room Flow, and command state clears only after repository result. History opens the Phase 6-owned reader compatibility path through an app callback; it does not construct `VolumeList` or Intent.

- [ ] **Step 3: Implement Material 3 routes**

Downloads uses `LazyColumn`, `ListItem`, `LinearProgressIndicator`, localized state descriptions, cancel `X` icon, retry icon, tooltips, and confirmation Dialog. History uses date-group headings and `ListItem`; cached/offline availability is text/semantics, not color alone. Neither screen starts or owns background work.

- [ ] **Step 4: Run process-restart, scheduler, semantics, and feature tests**

```powershell
.\gradlew.bat :feature:library:testDebugUnitTest --tests "org.mewx.wenku8.feature.library.downloads.*" --tests "org.mewx.wenku8.feature.library.history.*" :feature:library:connectedDebugAndroidTest :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.library.RoomDownloadRepositoryTest" --tests "org.mewx.wenku8.core.data.library.RoomReadingHistoryRepositoryTest" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.harness.Phase3StageBTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; restart sees the same progress, cancellation is nonterminal unless worker commits it, and terminal success remains exactly once.

- [ ] **Step 5: Commit Downloads and History**

```powershell
git add feature/library/src/main/java/org/mewx/wenku8/feature/library/downloads feature/library/src/main/java/org/mewx/wenku8/feature/library/history feature/library/src/test feature/library/src/androidTest
git diff --check --cached
git commit -m "feat(ui): add downloads and reading history routes"
```

### Task 10: Bind Library Repositories and Route Factories Through AppContainer

**Files:**
- Modify: `app/src/main/java/org/mewx/wenku8/di/{AppContainer,DefaultAppContainer}.kt`
- Create: `feature/library/src/main/java/org/mewx/wenku8/feature/library/navigation/LibraryEntryPoints.kt`
- Create: `app/src/main/java/org/mewx/wenku8/navigation/LibraryRouteFactory.kt`
- Test: `app/src/test/java/org/mewx/wenku8/navigation/LibraryRouteFactoryTest.kt`

- [ ] **Step 1: Write failing composition-boundary tests**

Assert one instance each of the five repository interfaces; selected provider gateway/language/user stores/transfer scheduler are injected only in `DefaultAppContainer`; route factories construct ViewModels with `SavedStateHandle`; feature production classes never call `Context.appContainer()`; test containers replace every repository with fakes.

Run app DI/navigation tests. Expected: FAIL because bindings and factory are absent.

- [ ] **Step 2: Extend AppContainer with interfaces only**

```kotlin
interface AppContainer {
    val dispatchers: AppDispatchers
    val clock: AppClock
    val settingsRepository: SettingsRepository
    val routeFlags: RouteFlagRepository
    val sessionStore: SessionStore
    val providerBinding: ProviderBinding
    val libraryRepository: LibraryRepository
    val searchHistoryRepository: SearchHistoryRepository
    val bookshelfRepository: BookshelfRepository
    val downloadRepository: DownloadRepository
    val readingHistoryRepository: ReadingHistoryRepository
    val transferScheduler: TransferScheduler
}
```

Do not expose databases, DAOs, provider source facets, migration runner, or Android scheduler implementation.

- [ ] **Step 3: Add feature entry callbacks and route factory**

```kotlin
data class LibraryEntryPoints(
    val openNovel: (NovelKey, String, Boolean) -> Unit,
    val openReaderFromHistory: (ReadingHistoryItem) -> Unit,
    val requestLogin: () -> Unit,
)
```

`LibraryRouteFactory` obtains AppContainer once in Activity/NavHost composition and creates each ViewModel using the Phase 1 `RouteViewModelFactory` plus `createSavedStateHandle()`. Composables receive state, event callbacks, `AdaptiveLayoutInfo`, and entry callbacks only.

- [ ] **Step 4: Run DI, feature architecture, and fake-container tests**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.di.*" --tests "org.mewx.wenku8.navigation.LibraryRouteFactoryTest" :feature:library:testDebugUnitTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
& .\tools\verify-phase4-library.ps1
git diff --check
```

Expected: PASS and `PHASE4-LIBRARY-PASS`.

- [ ] **Step 5: Commit composition wiring**

```powershell
git add studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/LibraryRouteFactory.kt studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/navigation/LibraryRouteFactoryTest.kt studio-android/LightNovelLibrary/feature/library/src/main/java/org/mewx/wenku8/feature/library/navigation
git diff --check --cached
git commit -m "feat(app): bind library route factories"
```

### Task 11: Install Typed Navigation, Deep Links, Restoration, and Back Behavior

**Files:**
- Create: `app/src/main/java/org/mewx/wenku8/navigation/{AppRoute,AppDeepLink,LibraryCompatibilityLauncher}.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/navigation/{AppDestination,ShellUiState,ShellViewModel,Wenku8App,Wenku8NavHost}.kt`
- Test: `app/src/test/java/org/mewx/wenku8/navigation/{AppDeepLinkTest,LibraryRouteCodecTest}.kt`
- Test: `app/src/androidTest/java/org/mewx/wenku8/navigation/{LibraryNavigationTest,LibraryPredictiveBackTest,LibraryResizeTest}.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/navigation/{Phase4RouteProcessDeathSeedTest,Phase4RouteProcessDeathVerifyTest}.kt`

- [ ] **Step 1: Write failing route/deep-link/back/resize tests**

Cover route encode/decode of bounded query/scope/filter/novel IDs; malformed/unknown deep links show recoverable error and no provider call; top-level singleTop state restoration; bottom bar/rail/expanded rail; downloads/history subordinate navigation; in-process recreation; locale recreation; resize retaining destination/filter/query/scroll; Drawer then route Back order; API 36 predictive start/cancel/commit; hinge presentation; account/settings compatibility action returning to prior destination. The separate seed/verify classes cover real process death for every top-level route plus search query/scope/results/scroll, discover filter/scroll, bookshelf tab/scroll, and downloads/history selection with no duplicate request/effect.

Run navigation unit/instrumented tests. Expected: FAIL because routes are not installed.

- [ ] **Step 2: Define typed route values and safe deep links**

```kotlin
sealed interface AppRoute {
    data object Discover : AppRoute
    data class Search(val query: String? = null, val scope: LibrarySearchScope = LibrarySearchScope.TITLE) : AppRoute
    data class Bookshelf(val partition: BookshelfPartition = BookshelfPartition.LOCAL) : AppRoute
    data object Downloads : AppRoute
    data object History : AppRoute
    data class ArgumentError(val code: String) : AppRoute
}

object AppDeepLink {
    const val BASE = "wenku8://app"
    fun encode(route: AppRoute): android.net.Uri
    fun decode(uri: android.net.Uri): AppRoute
}
```

Implement both functions with `Uri.Builder`/query APIs, accepted route/enum allowlists, maximum 256 Unicode code points for a query, and stable error codes. Never concatenate or split raw URI strings. These are internal explicit deep links; do not add an exported browsable intent filter.

- [ ] **Step 3: Replace Phase 1 legacy route content with library destinations**

`Wenku8NavHost` installs Discover/Search/Bookshelf plus subordinate Downloads/History. Use one NavController owned by `Wenku8ShellActivity`; top-level selection uses `launchSingleTop`, `restoreState`, and `popUpTo(startDestination) { saveState = true }`. Settings remains the Phase 7 compatibility launcher. Novel/reader/login callbacks invoke `LibraryCompatibilityLauncher`; only that app class constructs temporary explicit Intents.

- [ ] **Step 4: Implement exact back and adaptive presentation order**

Dialog/Sheet state is closed by its route first; then drawer/search suggestion layer; then subordinate route; then top-level destination/activity. Expanded permanent panes do not close. Predictive cancel mutates no destination/selection/scroll state. On resize, compact modal visibility becomes expanded pane logical visibility, never both; focus moves to selected item/pane and returns to invoker when compact overlay closes.

- [ ] **Step 5: Run navigation, locale, adaptive, and API 36 tests**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.LibraryNavigationTest,org.mewx.wenku8.navigation.LibraryPredictiveBackTest,org.mewx.wenku8.navigation.LibraryResizeTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\tools\verification\run-ui-process-death.ps1 -Phase 4 -Api 36 -SeedClass org.mewx.wenku8.navigation.Phase4RouteProcessDeathSeedTest -VerifyClass org.mewx.wenku8.navigation.Phase4RouteProcessDeathVerifyTest -Provider public
git diff --check
```

Expected: PASS; one NavHost/Activity owns migrated routes, the process-death report proves a changed PID and exact route-state restoration, and no duplicated destination/request appears after restore.

- [ ] **Step 6: Commit Navigation Compose integration**

```powershell
git add app/src/main/java/org/mewx/wenku8/navigation app/src/test/java/org/mewx/wenku8/navigation app/src/androidTest/java/org/mewx/wenku8/navigation
git diff --check --cached
git commit -m "feat(app): navigate library routes in one host"
```

### Task 12: Convert Search Activities to Compatibility Trampolines

**Files:**
- Move/Modify: `app/src/main/java/org/mewx/wenku8/activity/SearchActivity.kt` -> `LegacySearchActivity.kt`
- Move/Modify: `app/src/main/java/org/mewx/wenku8/activity/SearchResultActivity.kt` -> `LegacySearchResultActivity.kt`
- Create: original-path `SearchActivity.kt` and `SearchResultActivity.kt` trampolines
- Modify: `app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Test: `app/src/androidTest/java/org/mewx/wenku8/compat/SearchTrampolineTest.kt`

- [ ] **Step 1: Write failing old-caller/default/minified/back-stack tests**

Use Phase 0 old-signed fixtures on API 23/32/33. Assert explicit old `SearchActivity` opens empty new search; `SearchResultActivity` key missing -> empty string -> visible argument error without provider call; valid key -> results route; CharSequence/malformed rows preserve frozen behavior; trampoline launches once after recreation, forwards current task/back stack, supports predictive Back, and finishes. Debug rollback opens retained legacy classes; release manifest keeps old class names and does not export a new component.

Run focused instrumented and old-release fixture tests. Expected: FAIL because original owners still render XML.

- [ ] **Step 2: Name the frozen codec return type without adding a decoder**

In the existing `LegacyIntentCodec.kt`, retain `search(intent)` and make its result explicit:

```kotlin
data class LegacySearchArguments(val rawKey: String, val wasMissing: Boolean)

fun search(intent: Intent): LegacySearchArguments {
    val value = intent.getStringExtra("key")
    return LegacySearchArguments(value.orEmpty(), value == null)
}
```

The code must match the Phase 0 fixture behavior exactly; if the reviewed fixture records a different accepted runtime type/default, use that verified behavior and update this plan's evidence record before implementation continues.

- [ ] **Step 3: Preserve rollback implementations and install thin trampolines**

Rename only class declarations/references in the moved legacy files. Trampolines extend `ComponentActivity`, retain splash until forwarding, decode via `LegacyIntentCodec`, build an internal `AppDeepLink`, start the non-exported `Wenku8ShellActivity`, and finish. They contain no UI/network/storage/parser code.

```kotlin
class SearchResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.getBoolean("forwarded") == true) { finish(); return }
        val args = (applicationContext as LegacyCompatibilityOwner).legacyCompatibility.intentCodec.search(intent)
        startActivity(Intent(this, Wenku8ShellActivity::class.java).setData(AppDeepLink.encode(AppRoute.Search(args.rawKey))))
        finish()
    }
}
```

`SearchActivity` forwards `AppRoute.Search()` with no raw extra read. Add non-exported retained legacy classes to the manifest. `LibraryCompatibilityLauncher` selects them only when reviewed rollback mode is active.

- [ ] **Step 4: Run compatibility, minified, manifest, and navigation suites**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "*LegacyIntentCodec*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.compat.SearchTrampolineTest :app:assembleAlphaRelease :app:processAlphaReleaseMainManifest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
```

Expected: PASS; old class names/keys/defaults survive R8 and each old caller arrives at exactly one new route.

- [ ] **Step 5: Commit search compatibility entries**

```powershell
git add app/src/main/java/org/mewx/wenku8/activity app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt app/src/main/AndroidManifest.xml app/src/androidTest/java/org/mewx/wenku8/compat/SearchTrampolineTest.kt
git diff --check --cached
git commit -m "refactor(app): route legacy search entries to Compose"
```

### Task 13: Prove Accessibility and Assistive-Technology Journeys

**Files:**
- Create: `app/src/androidTest/java/org/mewx/wenku8/accessibility/Phase4LibraryAccessibilityTest.kt`
- Create: `docs/verification/manual-a11y-phase4.md`
- Modify: `docs/verification/{ui-owner-action-ledger,manual-assistive-technology-manifest}.yaml`
- Create: `verification-tools/src/main/kotlin/org/mewx/wenku8/verification/ui/ManualAssistiveEvidenceVerifier.kt`
- Create: `verification-tools/src/test/kotlin/org/mewx/wenku8/verification/ui/ManualAssistiveEvidenceVerifierTest.kt`
- Modify: `verification-tools/{build.gradle,src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt}`

- [ ] **Step 1: Add failing automated semantics assertions**

For every Phase 4 route/state assert route heading, collection info, selected tab/chip, error/live-region state, progress range, disabled/expanded state, icon label, traversal order, focus return, IME action, keyboard dismissal, and minimum touch target. Add API 36 gesture Back cases and compact/expanded hinge traversal.

Run the focused instrumentation class. Expected: FAIL with named route/state semantic gaps.

- [ ] **Step 2: Complete semantics and focus behavior in route components**

Use `heading()`, `collectionInfo`, `collectionItemInfo`, `selected`, `stateDescription`, `progressBarRangeInfo`, `error`, `liveRegion`, `traversalIndex`, `focusRequester`, and localized content descriptions at the owning control. Decorative cover fallbacks use `contentDescription = null`; meaningful covers use the novel title. Error retry returns focus to the triggering control/field.

- [ ] **Step 3: Execute and record manual journeys**

Run these stable IDs on the Phase 0 pinned device set and record tester/date/device/API/result/failure reference, with no screenshots containing account data:

```text
P4-A11 TalkBack discover -> ranking -> novel compatibility entry
P4-A12 TalkBack search -> history -> results -> retry
P4-A13 TalkBack bookshelf local/account/auth-required
P4-A14 TalkBack downloads progress -> cancel confirmation -> retry
P4-A15 Keyboard/DPAD discover/search/bookshelf/download/history
P4-A16 Switch Access search submit/history remove and download cancel
P4-A17 API36 predictive Back drawer/search/dialog/subordinate/top-level/trampoline
```

Update ledger rows A01-A03/F01-F05 with exact automated/manual test IDs and keep legacy rollback owner rows.

Each TalkBack/Switch Access PASS is also a structured manifest row using the plan-index schema. The report Markdown contains only stable step/result IDs; the YAML row binds its SHA-256, current route-source hash, app/test APK hashes, service package/version, device/API/build fingerprint, configuration, tester, and independent reviewer. Keyboard/DPAD automated key-event tests remain separate rows and cannot satisfy the assistive-service cases.

- [ ] **Step 4: Register, dispatch, and run accessibility evidence verification**

```groovy
// verification-tools/build.gradle
tasks.register('verifyPhase4AssistiveEvidence', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyAssistiveEvidence', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath, '4'
}
```

```kotlin
if (command == "verifyAssistiveEvidence") {
    ManualAssistiveEvidenceVerifier.verify(projectRoot, docsRoot, requiredPhase)
    return
}
```

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.accessibility.Phase4LibraryAccessibilityTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :verification-tools:verifyPhase4AssistiveEvidence -Pwenku8Provider=public --console=plain
Set-Location '..\..'
& .\tools\verify-phase4-library.ps1
git diff --check
```

Expected: PASS; manual record has no unresolved Critical/Important defect.

- [ ] **Step 5: Commit accessibility evidence**

```powershell
git add studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/accessibility docs/verification/manual-a11y-phase4.md docs/verification/ui-owner-action-ledger.yaml studio-android/LightNovelLibrary/feature/library/src/main
git diff --check --cached
git commit -m "test(ui): verify library accessibility journeys"
```

### Task 14: Record and Approve the Deterministic Library Screenshot Matrix

**Files:**
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/screenshot/Phase4LibraryGoldenTest.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `docs/verification/ui-golden-manifest.yaml`
- Create: `docs/verification/phase-4-visual-review.md`

- [ ] **Step 1: Add failing manifest coverage tests before recording images**

Require successful content for every Phase 4 route at compact zh-CN/light/1.0 and expanded smoke; applicable loading/empty/error/offline/auth; dark+font 2.0 including zh-TW/zh-HK fallback; top-level medium/API36 gesture; search IME-open and compact-height landscape; separating/occluding hinge and resize; download progress/cancel dialog; bookshelf tab/auth; no unexplained mask. Run `:verification-tools:verifyUiGoldenManifest -Pphase=4` and expect missing-case failures.

- [ ] **Step 2: Create deterministic fixtures and screenshot tests**

Fixtures use synthetic titles/authors/IDs and fake clocks, repositories, dispatchers, transfer progress, locale, theme, insets, navigation mode, and posture. Each test sets a stable case ID, waits for Compose idle, captures the exact root, and writes the fixture SHA-256 and image SHA-256 generated by the screenshot task. No provider/network/database/live data runs while recording.

Register exact app tasks with the Phase 1 `registerUiGoldenTask` helper; no feature-module screenshot task or alternate engine is permitted:

```groovy
registerUiGoldenTask('recordPhase4UiGoldens', 'record', 4,
    'org.mewx.wenku8.screenshot.Phase4LibraryGoldenTest')
registerUiGoldenTask('verifyPhase4UiGoldens', 'verify', 4,
    'org.mewx.wenku8.screenshot.Phase4LibraryGoldenTest')
```

- [ ] **Step 3: Record exact matrix outputs and run visual QA**

```powershell
.\gradlew.bat :app:recordPhase4UiGoldens -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
$sourceCommit = (git rev-parse HEAD).Trim()
.\gradlew.bat :verification-tools:approveUiGoldens -Pphase=4 "-PuiGoldenReviewer=$env:WENKU8_UI_REVIEWER" "-PuiGoldenSourceCommit=$sourceCommit"
.\gradlew.bat :app:verifyPhase4UiGoldens :verification-tools:verifyUiGoldenManifest -Pphase=4 -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: recording writes real fixture/image hashes into the manifest; verification passes with reviewed tolerances. Inspect every rendered image at original resolution for clipping, overlap, orphaned CJK glyphs, horizontal overflow, duplicate scroll containers, hinge crossing, tiny controls, card piles, wrong state, and focus/IME occlusion.

- [ ] **Step 4: Approve baselines and retain review evidence**

`phase-4-visual-review.md` records case ID, manifest hash, reviewer, approval commit, defects/fixes, and final PASS. A baseline with unresolved visual defect is not approved. Re-run affected screenshots after every fix.

- [ ] **Step 5: Commit deterministic goldens and review**

```powershell
git add app/src/androidTest/java/org/mewx/wenku8/screenshot/Phase4LibraryGoldenTest.kt docs/verification/ui-goldens docs/verification/ui-golden-manifest.yaml docs/verification/phase-4-visual-review.md
git diff --check --cached
git commit -m "test(ui): approve phase four library goldens"
```

### Task 15: Make the Reviewed Shell the Default and Preserve Forward-Fix Rollback

**Files:**
- Create: `app/src/main/java/org/mewx/wenku8/navigation/RouteDefaultPolicy.kt`
- Modify: `app/build.gradle`
- Modify: `app/src/main/java/org/mewx/wenku8/navigation/MainRouteSelector.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/activity/MainActivity.kt`
- Test: `app/src/test/java/org/mewx/wenku8/navigation/RouteDefaultPolicyTest.kt`
- Test: `app/src/androidTest/java/org/mewx/wenku8/navigation/Phase4DefaultRouteTest.kt`

- [ ] **Step 1: Write failing public/private/debug/release/default/rollback tests**

Assert absent property -> `library`; accepted values exactly `library`/`legacy`; unknown fails configuration; public/private alphaDebug and minified alphaRelease identities; default launch -> one shell Activity; `legacy` forward-fix -> retained `LegacyMainActivity`; local developer flags can preview old/new in debug but do not override reviewed release default; process recreation does not duplicate targets; Settings compatibility still works; no remote-config/network kill switch.

Run focused unit/instrumented/configuration tests. Expected: FAIL because Phase 1 still defaults legacy.

- [ ] **Step 2: Add fail-closed build-time default selection**

```groovy
def routeDefault = providers.gradleProperty('wenku8RouteDefault').orElse('library').get()
if (!(routeDefault in ['library', 'legacy'])) {
    throw new GradleException('wenku8RouteDefault must be library or legacy')
}
android.defaultConfig.buildConfigField 'String', 'WENKU8_ROUTE_DEFAULT', "\"${routeDefault}\""
```

`RouteDefaultPolicy` parses only these two values. `MainRouteSelector` waits for settings readiness, uses debug developer overrides only in debuggable builds, and otherwise follows the build-time default. Storage failure in a `library` release opens a visible shell recovery state with a legacy-open action; it does not silently change the reviewed product default.

- [ ] **Step 3: Update launcher selection and rollback manifest evidence**

Keep launcher/application IDs and exported policy unchanged. Retain `LegacyMainActivity`, legacy Fragments, and compatibility launcher for the declared window. Add release-manifest metadata containing only the enum `library`/`legacy` and source commit binding; no URL/account/title/query enters it.

- [ ] **Step 4: Run default and rollback matrices**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.RouteDefaultPolicyTest" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.Phase4DefaultRouteTest :app:assembleAlphaRelease :app:assembleBaiduRelease :app:assemblePlaystoreRelease -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:assembleAlphaRelease -Pwenku8Provider=public -Pwenku8RouteDefault=legacy --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:tasks -Pwenku8Provider=public -Pwenku8RouteDefault=unknown --console=plain
```

Expected: library/default and explicit legacy builds PASS; unknown value fails with the bounded validation message. Repeat the shared selection/contract tasks in protected private CI.

- [ ] **Step 5: Commit reviewed default policy**

```powershell
git add app/build.gradle app/src/main/java/org/mewx/wenku8/navigation app/src/main/java/org/mewx/wenku8/activity/MainActivity.kt app/src/test/java/org/mewx/wenku8/navigation app/src/androidTest/java/org/mewx/wenku8/navigation
git diff --check --cached
git commit -m "feat(app): default to reviewed library shell"
```

### Task 16: Run Authorized Anonymous Journey and Bind the Phase 4 Exit Gate

**Files:**
- Modify: `api-public/build.gradle`
- Create: `api-public/src/liveTest/kotlin/org/mewx/wenku8/api/publicprovider/live/LiveAnonymousLibrarySmoke.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/journey/Phase4LibraryJourneyTest.kt`
- Modify: `docs/verification/modernization-matrix.yaml`
- Modify: `app/build.gradle`

- [ ] **Step 1: Add failing deterministic journey and matrix-completeness tests**

Deterministic journey: launch -> discover -> rankings -> category/tag -> search -> results -> novel compatibility entry; offline launch -> local bookshelf -> downloads/history; in-process recreation at each route plus the Task 11 separate-process report; no account required. Matrix verifier requires every Phase Exit Condition to name task/test/provider/variant/API/configuration/fixture or baseline hash/report path/commit. Run and expect missing journey/evidence failures.

- [ ] **Step 2: Add a bounded anonymous live smoke behind the existing authorization gate**

`LiveAnonymousLibrarySmoke` constructs the Phase 2 single-threaded rate-limited provider, calls `home`, one ranking page, tag groups/tags, and one search term supplied by the accepted non-secret live fixture policy. It prints only:

```text
LIVE PASS anonymous operations=home,browse-ranking,tag-groups,tags,search
```

It never prints titles, authors, IDs, URLs, response bodies, trace IDs, or counts. Register `:api-public:liveAnonymousLibrarySmoke` as `JavaExec` depending on Phase 0 `verifyAcceptedLiveScope`; no credential environment variable is read.

- [ ] **Step 3: Register the exact Phase 4 exit task**

```groovy
def repositoryRoot = rootProject.projectDir.parentFile.parentFile

tasks.register('verifyPhase4LibraryPlan', Exec) {
    group = 'verification'
    workingDir repositoryRoot
    commandLine rootProject.ext.resolvePowerShell(), '-NoProfile', '-NonInteractive',
        '-File', new File(repositoryRoot, 'tools/verify-phase4-library.ps1').absolutePath
}

tasks.register('phase4LibraryGate') {
    group = 'verification'
    description = 'Canonical Phase 4 library aggregate gate.'
    dependsOn ':phase0Gate'
    dependsOn ':verification-tools:phase1Gate'
    dependsOn ':verification-tools:phase2Gate'
    dependsOn ':verification-tools:phase3Gate'
    dependsOn ':verifyArchitecture'
    dependsOn ':feature:library:testDebugUnitTest'
    dependsOn ':feature:library:connectedDebugAndroidTest'
    dependsOn 'testAlphaDebugUnitTest'
    dependsOn 'lintAlphaDebug'
    dependsOn 'assembleAlphaRelease'
    dependsOn 'verifyPhase4UiGoldens'
    dependsOn ':verification-tools:verifyUiGoldenManifest'
    dependsOn ':verification-tools:verifyPhase4AssistiveEvidence'
    dependsOn ':verification-tools:verifyXmlSurfaceLedger'
    dependsOn ':verification-tools:verifyPlannedGradleTasks'
    dependsOn ':verification-tools:verifySensitiveSource'
    dependsOn ':verification-tools:verifyOutboundManifest'
    dependsOn ':verification-tools:verifyPackagedLicenses'
    dependsOn 'verifyPhase4LibraryPlan'
}
```

This is the sole Phase 4 aggregate task; do not add an unregistered `phase4Gate` alias. The dependencies above execute the repository verifier, Phase 0-3 prerequisite gates, architecture rules, Compose instrumentation in controlled CI, old Intent/minified fixtures, compliance checks, and matrix completeness. It does not run live smoke automatically.

- [ ] **Step 4: Run deterministic public/private and authorized live gates**

```powershell
.\gradlew.bat :app:phase4LibraryGate -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.journey.Phase4LibraryJourneyTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :api-public:liveReadOnlySmoke -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
& .\tools\verify-phase4-library.ps1
git diff --check
```

Expected: deterministic gates PASS. Reuse the Phase 2-owned opt-in read-only smoke for live library reads; do not invent a `liveAnonymousLibrarySmoke` task. It runs only with current accepted scope and emits the single bounded PASS line; otherwise it fails closed before dispatch and Phase 4 remains incomplete rather than relabeled fixture-complete. Protected private CI runs the same route/repository contract and supplies its bound redacted attestation.

- [ ] **Step 5: Run independent phase reviews and resolve findings**

Commission independent reviews for architecture/provider separation, migration/storage/transfer compatibility, Compose MD3/navigation/adaptive/accessibility/visual QA, and executable-plan/evidence coverage. Any Critical/Important finding returns to its owning task, reruns focused and affected suites, and updates evidence before exit.

- [ ] **Step 6: Commit the verified Phase 4 gate**

```powershell
git add api-public/build.gradle api-public/src/liveTest app/build.gradle app/src/androidTest/java/org/mewx/wenku8/journey docs/verification/modernization-matrix.yaml
git diff --check --cached
git commit -m "test: bind phase four library exit gate"
```

## Phase 4 Completion Checklist

- [ ] All prerequisites and stop-condition scans pass.
- [ ] Feature modules import only domain/model/design-system APIs.
- [ ] Discover/search/bookshelf/download/history state families and recovery actions pass.
- [ ] Search and local bookshelf writes remain journaled and rollback-readable.
- [ ] Account cache/state is partitioned by account and session epoch.
- [ ] Download cancel/retry/restart keeps one canonical terminal commit.
- [ ] Search legacy Intents pass old-signed/minified API 23/32/33 fixtures.
- [ ] Compact/medium/expanded/compact-height/hinge/resize/back/locale restoration passes.
- [ ] Automated and manual accessibility journeys pass.
- [ ] Reviewed screenshot matrix passes with real hashes and approved baselines.
- [ ] Default and explicit higher-version legacy forward-fix builds pass.
- [ ] Currently authorized anonymous live journey passes without content/account output.
- [ ] Public gate and protected-private bound attestation pass.
- [ ] `git diff --check` passes and every task is an isolated reviewed commit.

## Deliberate Deferrals

- Novel detail/catalog/favorite/download selection/community and A04-A07 move in Phase 5.
- Reader route/ViewModel consolidation and retirement of V1/vertical entries move in Phase 6; History uses the compatibility launcher.
- Account, settings, wallpaper, about, image viewer, and remaining F04/F06 actions move in Phase 7 and remain explicit compatibility launches.
- Legacy Fragment/Activity/XML deletion and dual-write retirement require the Phase 8 compatibility-window audit.

## Execution Handoff

Plan complete at `docs/superpowers/plans/2026-07-10-wenku8-phase-4-library-shell.md`. Execute with `superpowers:subagent-driven-development` task by task, using `superpowers:using-git-worktrees` before implementation and two-stage review after each task.
