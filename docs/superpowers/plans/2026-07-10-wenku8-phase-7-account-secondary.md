# Wenku8 Phase 7 Account And Secondary Surfaces Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace every remaining reachable account, settings, wallpaper, about, and image page with process-restorable native Compose Material 3 routes while preserving frozen compatibility entries and fail-closed provider capabilities.

**Architecture:** `:feature:account` and `:feature:settings` consume only immutable `:core:model` values and `:core:domain` interfaces. `:core:data` adapts the selected provider, session, canonical storage, migration, cache, and local document boundaries; `:app` alone binds implementations, owns Navigation Compose, launches platform contracts, and hosts class-name-compatible trampolines. Account secrets remain ephemeral, check-in cannot appear or dispatch without an independently accepted HTTPS capability, and old Activity identities forward through `LegacyIntentCodec` without owning a page.

**Tech Stack:** Kotlin, coroutines, ViewModel, SavedStateHandle, StateFlow, Room/DataStore-backed repositories, encrypted SessionStore, WorkManager-backed maintenance, Jetpack Compose Material 3, Navigation Compose, Material 3 adaptive layouts, AndroidX WindowManager, Activity Result APIs, MediaStore, JUnit4, coroutines-test, AndroidX Compose UI test, deterministic screenshot verification, PowerShell, Gradle.

---

## Scope Boundary

This phase owns UI-owner ledger rows A11-A15, the remaining account/settings/image actions in F04 and F06, and account actions embedded in the Phase 4 bookshelf and Phase 5 novel detail routes. It installs these route IDs:

- `account/login`
- `account/profile`
- `settings/root`
- `settings/application`
- `settings/reader`
- `settings/storage`
- `settings/migration`
- `settings/wallpaper`
- `settings/about`
- `settings/about/licenses`
- `settings/about/privacy`
- `image/viewer`

The login route includes provider-owned captcha display, user-entered captcha, registration handoff, validation, duplicate-submit prevention, typed invalid-captcha and invalid-credential recovery, offline recovery, session expiry, and process-death secret clearing. Profile includes cached avatar state, refresh, session status, logout confirmation, and daily check-in only when the selected provider exposes a currently accepted HTTPS capability. The account bookshelf actions and novel recommendation action are capability-gated and recoverable.

The Settings routes include the atomic application-locale/content-language pair, application theme/dynamic color, e-ink preference, reader preferences and imported assets, cache cleanup, storage usage, migration status and explicit local-only diagnostic export, menu wallpaper selection/import/reset, channel-correct update behavior, notices, About, generated licenses/source offer, and privacy/outbound summary. Image viewer includes missing argument, load, zoom, pan, 90-degree rotation, chrome visibility, save, permission denial, storage failure, keyboard, semantics, and Back behavior.

Phase 7 does not delete retained legacy source, compatibility class identities, Serializable boundary DTOs, legacy provider ABI, route flags, or dual-write projections. Phase 8 may remove each only after its own reachability, compatibility-window, migration, license, and approval evidence passes.

## Authoritative Roots And Shell

Android and Gradle commands run from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android\studio-android\LightNovelLibrary'
```

Repository verification and Git commands run from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android'
```

Android feature modules keep Kotlin under `src/main/java`, `src/test/java`, and `src/androidTest/java`. Pure `:core:model` and `:core:domain` Kotlin stays under `src/main/kotlin` and `src/test/kotlin`. Commands use `-Pwenku8Provider=public --console=plain --stacktrace --no-parallel` unless a protected-private step says otherwise.

## Prerequisites

- [ ] Phase 0-6 deterministic exit tasks pass at their reviewed commits, and `docs/verification/modernization-matrix.yaml` records those commit and report hashes.
- [ ] Phase 2 provides a selected typed provider, encrypted SessionStore, session epoch, captcha/login/profile/avatar/bookshelf/recommendation/logout contracts, zero-work capability guard, shared provider suite, and fail-closed check-in capability.
- [ ] Phase 3 canonical stores, migration coordinator, cache metadata, transfer scheduler, restore reconciliation, legacy adapters, and local-only `MigrationDiagnostics` pass interruption and rollback tests.
- [ ] Phase 4 provides `AppRoute.kt`, `Wenku8NavHost.kt`, `LibraryRouteFactory.kt`, `BookshelfRepository`, adaptive shell, and the default Compose library route.
- [ ] Phase 5 provides final novel/detail/community routes and a typed novel entry callback for recommendation refresh.
- [ ] Phase 6 provides final reader routes, `ReaderRepository`, `ReaderProgressRepository`, `ReaderAssetRepository`, expanded `ReaderPreferences`, and `ReaderEffect.OpenImage(imageKey: String, legacyPath: String?)`; the three old reader class names are thin trampolines only.
- [ ] The authorization record is revalidated for every live account operation used in an opt-in smoke. Deterministic fixture implementation proceeds while a live scope is denied; no denied live operation dispatches.
- [ ] Public packaged dependencies/assets have zero production `UNKNOWN` or incompatible licenses, and the private graph has a fresh non-replayed bound attestation when private evidence is claimed.

## Stop Conditions

- A username, password, Cookie, captcha value, profile value, account response, raw HTML/XML, private endpoint, signing value, or protected overlay value appears in source, a command, fixture, log, screenshot, report, SavedState, WorkManager input/output/tag/progress, or commit.
- A password or captcha is placed in `SavedStateHandle`, DataStore, Room, SessionStore, a retained state object after submission, or a diagnostics/export payload.
- Login attempts automate or bypass captcha entry.
- Check-in appears in semantics, menus, navigation, screenshots, or dispatch code when `AccountCapability.DAILY_CHECK_IN` is absent, or any check-in request can use cleartext or an unaccepted operation contract.
- An account cache write does not recheck account ID and session epoch immediately before commit.
- Logout does not atomically purge encrypted session state and authenticated cache partitions before reporting signed out.
- A feature imports `api-contract`, `api-public`, `core:data`, `core:storage`, a provider implementation, Room, DataStore, OkHttp, Jsoup, legacy network/file helpers, `GlobalConfig`, or `AppContainerOwner`.
- A route reads raw Intent extras, raw file paths, or raw deep-link strings outside `LegacyIntentCodec` and `AppDeepLink`.
- Cache cleanup deletes downloaded chapters, downloaded images, custom reader assets, canonical bookshelf/progress/settings, a known-good legacy source, or migration evidence.
- Wallpaper/image import trusts a display name, MIME string, path, size, or decoder result without bounded validation and atomic internal copy.
- A route has a Toast-only or blank-screen failure, loses input/selection/scroll/chrome state on recreation, or makes a gesture the only available action.
- Any current product action remains reachable only through an XML page, Fragment-owned page, AppCompat Toolbar, old CardView, AsyncTask, raw executor/thread, `findViewById`, or Activity-owned orchestration.
- A baseline is replaced, tolerance widened, dynamic mask added, or manual accessibility result marked passing without reviewed evidence.

## Exit Gate

- Captcha login, profile/avatar/session validation, expiry recovery, logout, account bookshelf actions, and recommendation pass deterministic public/provider fixtures and the protected-private equivalent contract when available.
- Check-in is absent with zero egress when its HTTPS capability is unavailable; when capability evidence is independently accepted, its visible/submitting/success/failure states pass without changing the zero-cleartext rule.
- Application/reader settings, cache/storage/migration, wallpaper, About/licenses/privacy, and image viewer pass loading/content/empty/error/offline/auth states where applicable.
- All A11-A15/F04/F06 actions and every remaining ledger action map to a tested Compose Material 3 route or a documented non-page retirement row.
- Old class-name Intents and the image `path` extra reach the new routes through typed trampolines on API 23/32/33 and minified builds; legacy implementations remain unreachable product source for the compatibility window.
- Compact, medium, expanded, compact-height, separating/occluding hinge, resize, process death, locale recreation, IME, predictive Back, light/dark/e-ink, zh-CN/zh-TW/zh-HK fallback, and font scale 2.0 pass.
- Automated semantics, TalkBack, keyboard, DPAD, Switch Access, and reviewed screenshot matrices pass for every Phase 7 route and embedded account action.
- A production reachability scan reports zero XML/Fragment/AppCompat Toolbar/old CardView pages and zero legacy page owner reachable from launcher, explicit old Intent, navigation action, notification, or deep link.
- Public/provider/private identity, licensing, sensitive-source, outbound-egress, authorization, coverage, Lint, and `git diff --check` gates pass with no open Critical or Important independent finding.

## File Structure Map

### Platform-neutral account and secondary contracts

- Modify `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/account/AccountModels.kt`: account UI capability/session/status values using the Phase 1 core identities.
- Create `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/SecondarySurfaceModels.kt`: cache, migration, wallpaper, About, update, and local image values.
- Modify `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/account/AccountRepository.kt`: complete account operations and ephemeral secret ownership.
- Create `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/settings/SecondarySurfaceRepositories.kt`: cache, migration, wallpaper, About, update, and image repository boundaries.
- Modify `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/library/BookshelfRepository.kt`: account move operation required by the public provider contract.

### Data adapters

- Create `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/account/{ProviderAccountRepository,AccountModelMapper,AccountFailureMapper}.kt`.
- Create `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/settings/{WorkCacheMaintenanceRepository,StorageMigrationStatusRepository,AtomicWallpaperRepository,PackagedAboutRepository,ChannelUpdateRepository,LocalImageRepository}.kt`.
- Create focused unit/integration tests at the exact `core/data/src/test/java/org/mewx/wenku8/core/data/` paths named in Tasks 3, 7, and 9-12.

### Account feature

- Create `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/navigation/{AccountRoute,AccountEntryPoints}.kt`.
- Create login files `LoginUiState.kt`, `LoginViewModel.kt`, `LoginScreen.kt`, and `CaptchaImage.kt` under `feature/account/src/main/java/org/mewx/wenku8/feature/account/login/`.
- Create profile files `ProfileUiState.kt`, `ProfileViewModel.kt`, and `ProfileScreen.kt` under `feature/account/src/main/java/org/mewx/wenku8/feature/account/profile/`.
- Add the exact unit, Compose UI, accessibility, and screenshot test files named in Tasks 4-6 and 15-17 under `feature/account/src/test/java/`, `feature/account/src/androidTest/java/`, and `app/src/androidTest/java/`.

### Settings and image feature

- Create `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/navigation/{SettingsRoute,SettingsEntryPoints,ImageViewerRouteArgs}.kt`.
- Create application files `ApplicationSettingsUiState.kt`, `ApplicationSettingsViewModel.kt`, and `ApplicationSettingsScreen.kt` under `feature/settings/src/main/java/org/mewx/wenku8/feature/settings/application/`.
- Create reader settings files `ReaderSettingsUiState.kt`, `ReaderSettingsViewModel.kt`, and `ReaderSettingsScreen.kt` under `feature/settings/src/main/java/org/mewx/wenku8/feature/settings/reader/`.
- Create storage files `StorageUiState.kt`, `StorageViewModel.kt`, and `StorageScreen.kt` under `feature/settings/src/main/java/org/mewx/wenku8/feature/settings/storage/`.
- Create migration files `MigrationUiState.kt`, `MigrationViewModel.kt`, and `MigrationScreen.kt` under `feature/settings/src/main/java/org/mewx/wenku8/feature/settings/migration/`.
- Create wallpaper files `WallpaperUiState.kt`, `WallpaperViewModel.kt`, and `WallpaperScreen.kt` under `feature/settings/src/main/java/org/mewx/wenku8/feature/settings/wallpaper/`.
- Create About files `AboutUiState.kt`, `AboutViewModel.kt`, and `AboutScreen.kt` under `feature/settings/src/main/java/org/mewx/wenku8/feature/settings/about/`.
- Create image files `ImageViewerUiState.kt`, `ImageViewerViewModel.kt`, `ImageViewerScreen.kt`, and `ZoomTransform.kt` under `feature/settings/src/main/java/org/mewx/wenku8/feature/settings/image/`.
- Add the exact unit, Compose UI, accessibility, and screenshot test files named in Tasks 8-12 and 15-17 under `feature/settings/src/test/java/`, `feature/settings/src/androidTest/java/`, and `app/src/androidTest/java/`.

### App composition, navigation, and compatibility

- Modify `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/{AppContainer,DefaultAppContainer}.kt`.
- Modify `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/{AppRoute,AppDeepLink,Wenku8NavHost,LibraryRouteFactory}.kt`.
- Create `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/{AccountRouteFactory,SettingsRouteFactory,SecondaryPlatformLauncher}.kt`.
- Move legacy page implementations to `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/legacy/{LegacyUserLoginActivity,LegacyUserInfoActivity,LegacyMenuBackgroundSelectorActivity,LegacyAboutActivity,LegacyViewImageDetailActivity}.kt` without declaring them as product components.
- Replace original A11-A15 class paths with thin route trampolines and keep `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt` as the only raw-extra decoder.
- Modify `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`, route/owner/evidence YAML, and exact verification scripts named by tasks below.

## Task Dependency Graph

| Task | Depends on | Produces |
| ---: | --- | --- |
| 1 | Phase 0-6 exits | Frozen Phase 7 route/module/action contract |
| 2 | 1, Phase 1/2 contracts | Account and secondary domain boundaries |
| 3 | 2, Phase 2/3 | Provider account repository and session safety |
| 4 | 2, 3 | Login/captcha ViewModel and form state |
| 5 | 2, 3 | Profile/session/logout/check-in ViewModel |
| 6 | 4, 5 | Adaptive MD3 account routes |
| 7 | 2, 3, Phase 4/5 | Bookshelf mutations and recommendation integration |
| 8 | 2, Phase 1/6 | Application and reader settings routes |
| 9 | 2, Phase 0/3 | Cache, storage, migration, diagnostics routes |
| 10 | 2, Phase 1/3 | Wallpaper repository and route |
| 11 | 2, Phase 0 | About, licenses, privacy, notices, update policy |
| 12 | 2, Phase 6 | Image repository and image viewer route |
| 13 | 6-12 | AppContainer, typed navigation, platform launchers |
| 14 | 13, frozen Intent manifest | A11-A15 compatibility trampolines |
| 15 | 6-14 | Localization, state, adaptive, Back, process restoration |
| 16 | 15 | Accessibility and assistive-technology evidence |
| 17 | 15, 16 | Deterministic screenshot matrix and visual approval |
| 18 | 14-17 | Reachability audit, runtime journeys, independent reviews, exit gate |

Every task follows focused RED, minimal implementation, focused PASS, affected suite, `git diff --check`, independent specification review, independent code-quality review, and an isolated commit. A dependent task does not start until Critical and Important findings are resolved.

### Task 1: Freeze The Phase 7 Route, Module, And Action Contract

**Files:**
- Modify: `studio-android/LightNovelLibrary/feature/account/build.gradle`
- Modify: `studio-android/LightNovelLibrary/feature/settings/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Create: `docs/verification/phase-7-account-secondary-contract.yaml`
- Create: `tools/verify-phase7-account-secondary.ps1`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/phase7/Phase7ContractVerifier.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/phase7/Phase7ContractVerifierTest.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Create: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/architecture/Phase7ModuleGraphTest.kt`

- [ ] **Step 1: Write the failing graph and contract test**

Create `Phase7ModuleGraphTest` that loads both feature build files and the Phase 7 YAML, asserts the twelve exact route IDs and ledger owners A11-A15/F04/F06, and rejects feature dependencies/imports containing `:api-contract`, `:api-public`, `:core:data`, `:core:storage`, `:core:network`, logical `:api`, Room, DataStore, OkHttp, Jsoup, `GlobalConfig`, `LightCache`, `Wenku8API`, or `AppContainerOwner`.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.architecture.Phase7ModuleGraphTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because the Phase 7 contract and dependencies do not exist.

- [ ] **Step 2: Set the exact feature dependencies**

Both feature build files must retain the Android-library/Compose configuration created in Phase 1 and use this dependency boundary:

```groovy
dependencies {
    implementation project(':core:model')
    implementation project(':core:domain')
    implementation project(':core:designsystem')
    implementation libs.androidx.activity.compose
    implementation libs.androidx.lifecycle.runtime.compose
    implementation libs.androidx.lifecycle.viewmodel.compose
    implementation libs.androidx.compose.foundation
    implementation libs.androidx.compose.material3
    implementation libs.androidx.compose.material.icons.extended
    testImplementation libs.junit4
    testImplementation libs.kotlinx.coroutines.test
    androidTestImplementation libs.androidx.test.ext.junit
    androidTestImplementation libs.androidx.compose.ui.test.junit4
    debugImplementation libs.androidx.compose.ui.test.manifest
}
```

`app/build.gradle` adds `implementation project(':feature:account')` and `implementation project(':feature:settings')` exactly once.

- [ ] **Step 3: Check in the executable contract**

Create YAML with this exact finite structure and values:

```yaml
schema: 1
phase: 7
routes:
  - account/login
  - account/profile
  - settings/root
  - settings/application
  - settings/reader
  - settings/storage
  - settings/migration
  - settings/wallpaper
  - settings/about
  - settings/about/licenses
  - settings/about/privacy
  - image/viewer
owners: [A11, A12, A13, A14, A15, F04, F06]
check_in:
  required_capability: DAILY_CHECK_IN
  absent_behavior: hidden-zero-egress
legacy_entries:
  - org.mewx.wenku8.activity.UserLoginActivity
  - org.mewx.wenku8.activity.UserInfoActivity
  - org.mewx.wenku8.activity.MenuBackgroundSelectorActivity
  - org.mewx.wenku8.activity.AboutActivity
  - org.mewx.wenku8.activity.ViewImageDetailActivity
feature_dependencies: [core:model, core:domain, core:designsystem]
```

- [ ] **Step 4: Implement and run the repository verifier**

`Phase7ContractVerifier` parses YAML with the already pinned SnakeYAML Engine in `verification-tools`, compares exact route/owner/class sets, scans feature production source for forbidden imports, scans account/settings/navigation source and generated reports for credential-value patterns, verifies no cleartext URL or network-security exception, and checks that no new exported component exists. Tests cover malformed YAML, duplicate keys, anchors/aliases, unknown fields, wrong scalar types, and missing rows. Add a `verifyPhase7Contract` `VerificationMain.kt` dispatcher branch before its first host invocation; it accepts only the common `projectRoot`/`docsRoot` arguments and calls `Phase7ContractVerifier.verify(projectRoot, docsRoot)`.

`tools/verify-phase7-account-secondary.ps1` is this thin cross-platform launcher for the already-built JVM application distribution; it contains no `ConvertFrom-Yaml`, module installation, regex YAML parsing, nested Gradle invocation, or second schema implementation:

```powershell
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$android = [IO.Path]::GetFullPath((Join-Path $repo 'studio-android/LightNovelLibrary'))
$docs = [IO.Path]::GetFullPath((Join-Path $repo 'docs'))
$distribution = Join-Path $android 'verification-tools/build/install/verification-tools/bin'
$isWindows = [Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT
$launcher = Join-Path $distribution $(if ($isWindows) { 'verification-tools.bat' } else { 'verification-tools' })

if (-not (Test-Path -LiteralPath $launcher -PathType Leaf)) {
    throw 'PHASE7-CONTRACT-E001: built verifier launcher is missing'
}
& $launcher verifyPhase7Contract $android $docs
if ($LASTEXITCODE -ne 0) { throw 'PHASE7-CONTRACT-E002: structured verifier failed' }
```

```powershell
Push-Location 'studio-android\LightNovelLibrary'
try {
    .\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.phase7.Phase7ContractVerifierTest" :verification-tools:installDist -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
    if ($LASTEXITCODE -ne 0) { throw 'PHASE7-CONTRACT-E003: verifier build/tests failed' }
} finally {
    Pop-Location
}
& .\tools\verify-phase7-account-secondary.ps1
```

Expected: the unit tests PASS by observing each named malformed-input rejection; the host invocation reaches the registered JVM dispatcher and fails semantically on the first missing Phase 7 owner until Tasks 2-14 create every contract owner. `Task not found`, missing dispatcher, nested-build, and shell YAML failures are not acceptable RED evidence. `:verification-tools:installDist` is Gradle 9.1.0 application-plugin-generated and is pinned in `gradle-task-contract.yaml`.

- [ ] **Step 5: Verify and commit the contract**

```powershell
Set-Location 'studio-android\LightNovelLibrary'
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.architecture.Phase7ModuleGraphTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
git diff --check
git add studio-android/LightNovelLibrary/feature/account/build.gradle studio-android/LightNovelLibrary/feature/settings/build.gradle studio-android/LightNovelLibrary/app/build.gradle studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/architecture/Phase7ModuleGraphTest.kt studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/phase7/Phase7ContractVerifier.kt studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/phase7/Phase7ContractVerifierTest.kt docs/verification/phase-7-account-secondary-contract.yaml tools/verify-phase7-account-secondary.ps1
git diff --check --cached
git commit -m "build: freeze phase seven surface contract"
```

Expected: graph test PASS; the verifier still names only later missing owners and reveals no secret value.

### Task 2: Define Account And Secondary Surface Domain Contracts

**Files:**
- Modify: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/account/AccountModels.kt`
- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/SecondarySurfaceModels.kt`
- Modify: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/account/AccountRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/settings/SecondarySurfaceRepositories.kt`
- Modify: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/library/BookshelfRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/settings/SecondarySurfaceModelsTest.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/account/AccountRepositoryContractTest.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/settings/SecondarySurfaceRepositoriesTest.kt`

- [ ] **Step 1: Write failing invariant and boundary tests**

Test nonblank bounded operation codes, monotonic non-negative storage bytes, unique account move IDs, immutable capability sets, valid cache categories, finite migration domains/phases, pending bucket bounds, safe wallpaper IDs, bounded image payload, and redacted failure `toString`. Reflect all repository signatures and reject Android, API/provider, network, Room, DataStore, file, URI, Cookie, captcha text, password, and legacy types.

```powershell
.\gradlew.bat :core:model:test :core:domain:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL on unresolved Phase 7 types.

- [ ] **Step 2: Add complete account state models using core identities**

Append these exact shapes in package `org.mewx.wenku8.core.model.account`; use the Phase 1 `SourceId`, `NovelKey`, `LoginAttemptId`, `CaptchaChallenge`, `SessionState`, `UserProfile`, `CheckInResult`, and `RecommendationResult` rather than defining duplicates:

```kotlin
enum class AccountCapability {
    REGISTRATION_LINK,
    CAPTCHA_LOGIN,
    PROFILE,
    BOOKSHELF_READ,
    BOOKSHELF_MUTATE,
    RECOMMEND_NOVEL,
    DAILY_CHECK_IN,
}

enum class AccountFreshness { FRESH, STALE }

data class AccountSession(
    val authenticated: Boolean,
    val accountId: String?,
    val expiresAtEpochMillis: Long?,
    val epoch: Long,
) {
    init { require(epoch >= 0); require(authenticated == !accountId.isNullOrBlank()) }
}

sealed interface AccountFailure {
    data object Offline : AccountFailure
    data object InvalidCaptcha : AccountFailure
    data object InvalidCredentials : AccountFailure
    data object SessionExpired : AccountFailure
    data object AuthenticationRequired : AccountFailure
    data object ChallengeExpired : AccountFailure
    data class Storage(val operationCode: String) : AccountFailure
    data class Network(val operationCode: String) : AccountFailure
    data class Parse(val operationCode: String) : AccountFailure
    data class RateLimited(val retryAfterSeconds: Long?) : AccountFailure
    data class Unsupported(val capability: AccountCapability) : AccountFailure
}

sealed interface AccountResult<out T> {
    data class Data<T>(val value: T, val freshness: AccountFreshness = AccountFreshness.FRESH) : AccountResult<T>
    data class Failure(val reason: AccountFailure) : AccountResult<Nothing>
}
```

- [ ] **Step 3: Add complete secondary surface values**

Create `SecondarySurfaceModels.kt` with package `org.mewx.wenku8.core.model.settings`:

```kotlin
enum class CacheCategory { REBUILDABLE_METADATA, GENERATED_IMAGES, STALE_CHAPTERS }
data class CacheUsage(val category: CacheCategory, val bytes: Long) { init { require(bytes >= 0) } }
data class CacheCleanupRequest(val categories: Set<CacheCategory>) { init { require(categories.isNotEmpty()) } }
sealed interface MaintenanceState {
    data object Idle : MaintenanceState
    data class Running(val completed: Int, val total: Int) : MaintenanceState {
        init { require(total > 0 && completed in 0..total) }
    }
    data class Succeeded(val reclaimedBytes: Long) : MaintenanceState { init { require(reclaimedBytes >= 0) } }
    data class Failed(val operationCode: String) : MaintenanceState
    data object Cancelled : MaintenanceState
}

enum class MigrationUiPhase { NOT_STARTED, SNAPSHOTTING, IMPORTING, DUAL_WRITE, RECONCILING, VERIFIED, LEGACY_READ_ONLY, COMPLETE }
enum class ReconciliationUiState { CLEAN, REPAIRED, PENDING, FAILED }
enum class PendingCountBucket { ZERO, ONE_TO_TEN, ELEVEN_TO_HUNDRED, OVER_HUNDRED }
data class MigrationStatus(
    val domain: String,
    val phase: MigrationUiPhase,
    val reconciliation: ReconciliationUiState,
    val pending: PendingCountBucket,
) { init { require(domain in setOf("settings", "search-history", "bookshelf", "reader-progress", "downloads-catalog", "session-credentials")) } }

data class WallpaperChoice(val id: String, val previewAsset: String?, val selected: Boolean) {
    init { require(id in setOf("default-1", "default-2", "default-3", "default-4", "default-5", "custom")) }
}
data class ExternalDocumentToken(val value: String) { init { require(value.isNotBlank() && value.length <= 1024) } }
data class LocalImageKey(val value: String) { init { require(value.isNotBlank() && value.length <= 256) } }
data class ImagePayload(val bytes: ByteArray, val mediaType: String, val displayName: String) {
    init { require(bytes.isNotEmpty() && bytes.size <= 32 * 1024 * 1024); require(mediaType in setOf("image/jpeg", "image/png", "image/webp")); require(displayName.length in 1..128) }
}
enum class ImageSaveFailure { PERMISSION_DENIED, STORAGE_UNAVAILABLE, WRITE_FAILED, UNSUPPORTED_FORMAT }
sealed interface ImageSaveResult { data class Saved(val displayName: String) : ImageSaveResult; data class Failed(val reason: ImageSaveFailure) : ImageSaveResult }

enum class DistributionChannel { ALPHA, BAIDU, PLAYSTORE }
enum class UpdateAction { MANUAL_CHECK, STORE_MANAGED }
data class AboutInfo(val versionName: String, val versionCode: Long, val notice: String?, val licenseDocumentKey: String, val privacyDocumentKey: String, val sourceOfferDocumentKey: String)
```

Byte-array equality is never used for identity. Tests clear synthetic payload arrays after use.

- [ ] **Step 4: Define complete repository methods and secret ownership**

`AccountRepository.kt`:

```kotlin
package org.mewx.wenku8.core.domain.account

import kotlinx.coroutines.flow.StateFlow
import org.mewx.wenku8.core.model.account.*
import org.mewx.wenku8.core.model.identity.LoginAttemptId
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.account.CaptchaChallenge
import org.mewx.wenku8.core.model.account.CheckInResult
import org.mewx.wenku8.core.model.account.RecommendationResult
import org.mewx.wenku8.core.model.account.UserProfile

interface AccountRepository {
    val session: StateFlow<AccountSession>
    fun capabilities(): Set<AccountCapability>
    suspend fun registrationLink(): AccountResult<String>
    suspend fun beginLogin(): AccountResult<CaptchaChallenge>
    suspend fun login(attemptId: LoginAttemptId, username: String, password: CharArray, captcha: CharArray): AccountResult<AccountSession>
    suspend fun validateSession(): AccountResult<AccountSession>
    suspend fun profile(forceRefresh: Boolean): AccountResult<UserProfile>
    suspend fun avatar(forceRefresh: Boolean): AccountResult<LocalImageKey>
    suspend fun recommendNovel(key: NovelKey): AccountResult<RecommendationResult>
    suspend fun dailyCheckIn(): AccountResult<CheckInResult>
    suspend fun logout(): AccountResult<Unit>
}
```

The repository implementation must clear both supplied arrays in `finally`, including cancellation. It never accepts a password or captcha as `String`.

`SecondarySurfaceRepositories.kt`:

```kotlin
package org.mewx.wenku8.core.domain.settings

import kotlinx.coroutines.flow.Flow
import org.mewx.wenku8.core.model.settings.*

interface CacheMaintenanceRepository {
    fun observeUsage(): Flow<List<CacheUsage>>
    fun observeMaintenance(): Flow<MaintenanceState>
    suspend fun start(request: CacheCleanupRequest): Boolean
    suspend fun cancel(): Boolean
}
interface MigrationStatusRepository {
    fun observe(): Flow<List<MigrationStatus>>
    suspend fun previewDiagnosticExport(): ByteArray
}
interface WallpaperRepository {
    fun observe(): Flow<List<WallpaperChoice>>
    suspend fun selectBuiltIn(id: String): Boolean
    suspend fun importCustom(document: ExternalDocumentToken): Boolean
    suspend fun reset(): Boolean
}
interface AboutRepository {
    suspend fun load(): AboutInfo
    suspend fun readDocument(key: String): String
}
interface UpdateRepository {
    fun action(channel: DistributionChannel): UpdateAction
    suspend fun checkManually(): Boolean
}
interface LocalImageRepository {
    suspend fun open(key: LocalImageKey, legacyPath: String?): Result<ImagePayload>
    suspend fun save(payload: ImagePayload): ImageSaveResult
}
```

Add `suspend fun moveAccount(serverEntryIds: List<String>, sourceGroupId: String, targetGroupId: String): LibraryMutationResult` to Phase 4 `BookshelfRepository`; its tests require a nonempty unique list and distinct groups.

- [ ] **Step 5: Run architecture checks and commit**

```powershell
.\gradlew.bat :core:model:test :core:domain:test :core:model:dependencies :core:domain:dependencies -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
rg -n "android\.|androidx\.|okhttp|jsoup|Room|Dao|DataStore|Wenku8API|GlobalConfig|api\.contract|Cookie" core\model\src\main core\domain\src\main -g '*.kt'
git diff --check
git add core/model/src core/domain/src
git diff --check --cached
git commit -m "feat(domain): define account and secondary contracts"
```

Expected: tests PASS and the forbidden-source scan exits 1.

### Task 3: Implement The Provider Account Repository And Session Safety

**Files:**
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/account/AccountModelMapper.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/account/AccountFailureMapper.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/account/ProviderAccountRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/account/ProviderAccountRepositoryTest.kt`

- [ ] **Step 1: Write failing capability, secret, epoch, cache, and logout tests**

Use a fake selected provider and SessionStore to prove: capability mapping is immutable/exhaustive; absent operations invoke zero provider calls; password/captcha arrays clear on success/failure/cancellation; begin-login attempt is consumed once; profile/avatar writes recheck account ID and epoch; stale completion cannot enter a new account partition; invalid Keystore/auth-tag/session clears complete authenticated state; logout purges session/avatar/profile/account bookshelf caches before publishing signed out; offline profile returns stale cached data; expired session returns `SessionExpired` and no retry loop.

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.account.ProviderAccountRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because the repository is absent.

- [ ] **Step 2: Map provider values without duplicating core models**

`AccountModelMapper` imports Phase 2 `api-contract` result/facet types only inside `:core:data` and maps to the Phase 1 `org.mewx.wenku8.core.model` identities. It maps every provider capability explicitly:

```kotlin
private val accountCapabilities = mapOf(
    ProviderCapability.REGISTRATION_LINK to AccountCapability.REGISTRATION_LINK,
    ProviderCapability.CAPTCHA_LOGIN to AccountCapability.CAPTCHA_LOGIN,
    ProviderCapability.PROFILE to AccountCapability.PROFILE,
    ProviderCapability.BOOKSHELF_READ to AccountCapability.BOOKSHELF_READ,
    ProviderCapability.BOOKSHELF_MUTATE to AccountCapability.BOOKSHELF_MUTATE,
    ProviderCapability.RECOMMEND_NOVEL to AccountCapability.RECOMMEND_NOVEL,
    ProviderCapability.DAILY_CHECK_IN to AccountCapability.DAILY_CHECK_IN,
)
```

No `SourceId`, `NovelKey`, captcha, session, profile, bookshelf, or recommendation model is redefined in `api-contract` or `:core:data`.

- [ ] **Step 3: Implement one guarded operation path**

Use this complete guard inside `ProviderAccountRepository`:

```kotlin
private suspend fun <T> guarded(
    capability: AccountCapability,
    call: suspend () -> ApiResult<T>,
    map: (T) -> T,
): AccountResult<T> {
    if (capability !in capabilities()) return AccountResult.Failure(AccountFailure.Unsupported(capability))
    return when (val result = call()) {
        is ApiResult.Success -> AccountResult.Data(map(result.value))
        is ApiResult.Failure -> AccountResult.Failure(failureMapper.map(result.error))
    }
}
```

Typed overloads map differing provider/domain result types. `dailyCheckIn()` executes this capability check before constructing a request/call object. Absent capability returns `Unsupported(DAILY_CHECK_IN)` with provider, DNS, cache, logger, and operation counters all zero.

- [ ] **Step 4: Implement ephemeral login and atomic session transitions**

`login` uses:

```kotlin
override suspend fun login(
    attemptId: LoginAttemptId,
    username: String,
    password: CharArray,
    captcha: CharArray,
): AccountResult<AccountSession> = try {
    when (val result = source.login(LoginRequest(attemptId, username.trim(), password, captcha))) {
        is ApiResult.Success -> commitSessionAndPublish(result.value)
        is ApiResult.Failure -> AccountResult.Failure(failureMapper.map(result.error))
    }
} finally {
    password.fill('\u0000')
    captcha.fill('\u0000')
}
```

`commitSessionAndPublish` persists through the reviewed Phase 2 SessionStore/credential reconciler, reads back the resulting epoch, purges any previous authenticated partition, and then updates the `MutableStateFlow`. Profile/avatar completion captures `(providerId, accountId, epoch)` before dispatch and rechecks all three immediately before one cache transaction. Logout calls provider at most once, treats already-signed-out as success, purges SessionStore and authenticated caches, increments epoch, then publishes signed out even if a remote logout response is lost.

- [ ] **Step 5: Run focused, shared provider, process-death, and secret scans**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.account.ProviderAccountRepositoryTest" :api-contract-tests:test -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
rg -n "SavedStateHandle.*(password|captcha)|put.*(password|captcha)|Log\.|printStackTrace|http://" core\data\src\main\java\org\mewx\wenku8\core\data\account app\src\main feature\account\src\main -g '*.kt'
git diff --check
```

Expected: tests PASS, check-in-disabled request count is zero, and scan exits 1.

- [ ] **Step 6: Commit the repository**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/account core/data/src/test/java/org/mewx/wenku8/core/data/account
git diff --check --cached
git commit -m "feat(data): add session-safe account repository"
```

### Task 4: Implement Captcha Login State And ViewModel

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/login/LoginUiState.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/login/LoginViewModel.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/test/java/org/mewx/wenku8/feature/account/login/LoginViewModelTest.kt`

- [ ] **Step 1: Write failing state-machine and process-death tests**

Cover initial challenge loading/content/error/offline, captcha refresh, expired challenge, username/password/captcha local validation, focus field, submitting duplicate suppression, invalid captcha preserving username but clearing secret fields and refreshing challenge, invalid credentials preserving username, retry, registration effect, successful profile navigation, cancellation, and process recreation. Inspect `SavedStateHandle.keys()` and assert only `username`, `challengeAttemptId`, and `challengeExpiry` may exist; no password/captcha value survives recreation.

```powershell
.\gradlew.bat :feature:account:testDebugUnitTest --tests "org.mewx.wenku8.feature.account.login.LoginViewModelTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because login state and ViewModel are absent.

- [ ] **Step 2: Define the exact immutable state/effect contract**

```kotlin
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val captcha: String = "",
    val challenge: CaptchaChallenge? = null,
    val loadingChallenge: Boolean = true,
    val submitting: Boolean = false,
    val usernameError: LoginFieldError? = null,
    val passwordError: LoginFieldError? = null,
    val captchaError: LoginFieldError? = null,
    val pageError: AccountFailure? = null,
)
enum class LoginFieldError { REQUIRED, TOO_LONG, INVALID_CAPTCHA, INVALID_CREDENTIALS }
sealed interface LoginEffect {
    data class OpenRegistration(val acceptedHttpsUrl: String) : LoginEffect
    data object OpenProfile : LoginEffect
    data class Focus(val field: LoginField) : LoginEffect
    data class Announce(val messageId: LoginMessage) : LoginEffect
}
enum class LoginField { USERNAME, PASSWORD, CAPTCHA }
enum class LoginMessage { CHALLENGE_REFRESHED, LOGIN_SUCCEEDED, OFFLINE, TRY_AGAIN }
```

The effect stream uses `Channel<LoginEffect>(capacity = Channel.BUFFERED)` with bounded enum/value data. It never contains password, captcha, provider body, username, or trace ID.

- [ ] **Step 3: Implement validation and one-submit ownership**

`submit()` validates username 1..64 code points and password/captcha nonempty with provider input limits when exposed. It copies current password/captcha to `CharArray`, immediately sets both UI strings to empty, then calls the repository once in `viewModelScope`. Invalid captcha refreshes the challenge and focuses captcha; invalid credentials focuses password; offline retains the challenge if not expired. A `Mutex.tryLock()` plus `submitting` prevents duplicate clicks/IME events. `finally` clears arrays and unlocks.

`SavedStateHandle` stores username and opaque challenge identity/expiry only. After process recreation, the ViewModel starts a new challenge and shows no secret text. It never serializes captcha image bytes.

- [ ] **Step 4: Run tests and commit**

```powershell
.\gradlew.bat :feature:account:testDebugUnitTest --tests "org.mewx.wenku8.feature.account.login.LoginViewModelTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add feature/account/src/main/java/org/mewx/wenku8/feature/account/login/LoginUiState.kt feature/account/src/main/java/org/mewx/wenku8/feature/account/login/LoginViewModel.kt feature/account/src/test/java/org/mewx/wenku8/feature/account/login/LoginViewModelTest.kt
git diff --check --cached
git commit -m "feat(account): add captcha login state machine"
```

Expected: all state, cancellation, duplicate, and process-death tests PASS.

### Task 5: Implement Profile, Session, Logout, And Check-In State

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/profile/ProfileUiState.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/profile/ProfileViewModel.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/test/java/org/mewx/wenku8/feature/account/profile/ProfileViewModelTest.kt`

- [ ] **Step 1: Write failing content/offline/auth/logout/check-in tests**

Cover loading, fresh content, stale avatar/profile, avatar failure without blank profile, offline content, recoverable error, auth required, session expiry during refresh, logout confirmation/cancel/submitting/success/failure, process death with confirmation restored but no account data in SavedState, and Back closing confirmation. Run a parameterized absent-capability test asserting no check-in UI event/effect and zero `dailyCheckIn` calls. When a synthetic accepted capability is present, test submitting/duplicate suppression/success/failure and profile refresh.

```powershell
.\gradlew.bat :feature:account:testDebugUnitTest --tests "org.mewx.wenku8.feature.account.profile.ProfileViewModelTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because profile state is absent.

- [ ] **Step 2: Define the complete profile state**

```kotlin
sealed interface ProfileContent {
    data object Loading : ProfileContent
    data class Content(val profile: UserProfile, val avatar: LocalImageKey?, val stale: Boolean) : ProfileContent
    data object AuthRequired : ProfileContent
    data class Error(val failure: AccountFailure) : ProfileContent
}
data class ProfileUiState(
    val content: ProfileContent = ProfileContent.Loading,
    val refreshing: Boolean = false,
    val logoutDialog: Boolean = false,
    val loggingOut: Boolean = false,
    val checkingIn: Boolean = false,
    val checkInVisible: Boolean = false,
    val checkInResult: CheckInResult? = null,
)
sealed interface ProfileEffect { data object OpenLogin : ProfileEffect; data object CloseAccount : ProfileEffect; data class Announce(val message: ProfileMessage) : ProfileEffect }
enum class ProfileMessage { SESSION_EXPIRED, LOGGED_OUT, CHECK_IN_SUCCEEDED, CHECK_IN_FAILED, OFFLINE }
```

- [ ] **Step 3: Implement fail-closed capability and session handling**

On initialization set `checkInVisible = AccountCapability.DAILY_CHECK_IN in repository.capabilities()`. The `checkIn()` function returns before launching a coroutine when false. Session expiry atomically changes content to `AuthRequired`, emits one `SESSION_EXPIRED`, and exposes login. Logout confirmation is a state-owned AlertDialog; success waits for repository signed-out publication before `CloseAccount`. A remote logout failure after local purge still results in signed out and a bounded recovery message.

Profile and avatar load concurrently under one supervisor scope; profile determines page content and avatar may fail independently. Pull-to-refresh preserves existing content. SavedState stores only `logoutDialog`; canonical/session repositories restore all data.

- [ ] **Step 4: Verify zero egress and commit**

```powershell
.\gradlew.bat :feature:account:testDebugUnitTest --tests "org.mewx.wenku8.feature.account.profile.ProfileViewModelTest" :core:data:testDebugUnitTest --tests "*ProviderAccountRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add feature/account/src/main/java/org/mewx/wenku8/feature/account/profile feature/account/src/test/java/org/mewx/wenku8/feature/account/profile
git diff --check --cached
git commit -m "feat(account): add profile and logout state"
```

Expected: tests PASS; absent check-in capability produces zero repository/provider/network calls.

### Task 6: Build Adaptive Native Compose Material 3 Account Routes

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/navigation/AccountRoute.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/navigation/AccountEntryPoints.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/login/LoginScreen.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/login/CaptchaImage.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/main/java/org/mewx/wenku8/feature/account/profile/ProfileScreen.kt`
- Create: `studio-android/LightNovelLibrary/feature/account/src/androidTest/java/org/mewx/wenku8/feature/account/AccountScreenTest.kt`

- [ ] **Step 1: Write failing Compose state, semantics, IME, and Back tests**

For login assert every state from Task 4, captcha content description, refresh icon tooltip, password visibility state, field errors, focus-next order, Done submits once, IME dismisses on success, registration confirmation/handoff, minimum 48dp targets, and Back with IME/dialog before route. For profile assert content/offline/auth/error, heading/list semantics, avatar description, refresh, hidden check-in, visible check-in states, logout dialog focus return, and compact/expanded layouts.

```powershell
.\gradlew.bat :feature:account:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.account.AccountScreenTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because screens are absent.

- [ ] **Step 2: Implement the login screen with standard Material 3 controls**

Use `Wenku8Scaffold`, `TopAppBar`, `OutlinedTextField`, password visibility `IconButton`, `CaptchaImage`, refresh `IconButton`, `Button`, `TextButton`, `LinearProgressIndicator`, inline `isError`/supporting text, `SnackbarHost`, and `AlertDialog` for external registration confirmation. All display strings and descriptions come from feature resources in default, `values-zh-rTW`, and `values-zh-rHK`.

Use one vertical scroll only when font scale requires it. Expanded width centers a maximum 560dp form without a card. Captcha bytes are decoded off main through an injected decoder callback and cleared after replacement. Decorative background is absent.

- [ ] **Step 3: Implement the profile screen and capability omission**

Use pull-to-refresh, `ListItem` for score/experience/rank, a bounded avatar, stale/offline status, `Button` for login recovery, icon refresh, logout command, and `AlertDialog`. Add the check-in `Button` only inside `if (state.checkInVisible)`; do not create a disabled or invisible semantics node when false. Compact uses one column; expanded uses two hinge-safe regions only when both meet readable minimums. No content spans an occluding/separating hinge.

- [ ] **Step 4: Run UI tests and commit**

```powershell
.\gradlew.bat :feature:account:testDebugUnitTest :feature:account:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.account.AccountScreenTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add feature/account/src/main feature/account/src/androidTest
git diff --check --cached
git commit -m "feat(ui): add material account routes"
```

Expected: account UI states, IME, focus, semantics, and hidden capability tests PASS.

### Task 7: Complete Account Bookshelf Mutations And Novel Recommendation

**Files:**
- Modify: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/library/RoomBookshelfRepository.kt`
- Modify: `studio-android/LightNovelLibrary/feature/library/src/main/java/org/mewx/wenku8/feature/library/bookshelf/BookshelfUiState.kt`
- Modify: `studio-android/LightNovelLibrary/feature/library/src/main/java/org/mewx/wenku8/feature/library/bookshelf/BookshelfViewModel.kt`
- Modify: `studio-android/LightNovelLibrary/feature/library/src/main/java/org/mewx/wenku8/feature/library/bookshelf/BookshelfScreen.kt`
- Modify: `studio-android/LightNovelLibrary/feature/novel/src/main/java/org/mewx/wenku8/feature/novel/detail/NovelDetailUiState.kt`
- Modify: `studio-android/LightNovelLibrary/feature/novel/src/main/java/org/mewx/wenku8/feature/novel/detail/NovelDetailViewModel.kt`
- Modify: `studio-android/LightNovelLibrary/feature/novel/src/main/java/org/mewx/wenku8/feature/novel/detail/NovelDetailScreen.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/library/RoomBookshelfRepositoryTest.kt`
- Test: `studio-android/LightNovelLibrary/feature/library/src/test/java/org/mewx/wenku8/feature/library/bookshelf/BookshelfViewModelTest.kt`
- Test: `studio-android/LightNovelLibrary/feature/library/src/androidTest/java/org/mewx/wenku8/feature/library/bookshelf/BookshelfScreenTest.kt`
- Test: `studio-android/LightNovelLibrary/feature/novel/src/test/java/org/mewx/wenku8/feature/novel/detail/NovelDetailViewModelTest.kt`
- Test: `studio-android/LightNovelLibrary/feature/novel/src/androidTest/java/org/mewx/wenku8/feature/novel/detail/NovelDetailScreenTest.kt`

- [ ] **Step 1: Write failing capability/auth/rollback tests**

Bookshelf tests cover add/remove/move, multiple selected membership IDs, source/target validation, duplicate suppression, session expiry, stale/offline snapshot preservation, pending legacy projection, failure retry, dialog/Sheet Back, process death, and initial-state restoration. Novel tests cover recommendation hidden without capability, auth required, confirmation, submitting, accepted/rejected/failure, retry, and zero dispatch when hidden.

Run focused library/novel tests. Expected: FAIL on account move and recommendation states.

- [ ] **Step 2: Map account move through exact server membership IDs**

`RoomBookshelfRepository.moveAccount` validates unique nonblank IDs and distinct groups, calls `BookshelfCommand.Move` once, captures account/epoch before dispatch, rechecks them before the canonical transaction, and refreshes the account partition. It never derives membership ID from novel ID. Reversible deterministic tests restore the original group in `finally`.

- [ ] **Step 3: Add Material 3 capability-gated actions**

Bookshelf uses selected rows, `DropdownMenu` target group, `AlertDialog`, progress, auth-required login action, and pending-projection status. Recommendation is an icon+tooltip command in novel detail only when `RECOMMEND_NOVEL` is present; confirmation prevents accidental mutation. Neither feature imports `AccountRepository` implementation, provider, session store, or storage. Route factories supply interface callbacks.

- [ ] **Step 4: Verify affected modules and commit**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "*RoomBookshelfRepositoryTest" :feature:library:testDebugUnitTest :feature:novel:testDebugUnitTest :feature:library:connectedDebugAndroidTest :feature:novel:connectedDebugAndroidTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add core/data/src/main/java/org/mewx/wenku8/core/data/library/RoomBookshelfRepository.kt feature/library/src feature/novel/src
git diff --check --cached
git commit -m "feat(ui): complete account bookshelf actions"
```

Expected: tests PASS and absent capabilities dispatch zero mutations.

### Task 8: Build Application And Reader Settings Routes

**Files:**
- Create: `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/application/{ApplicationSettingsUiState,ApplicationSettingsViewModel,ApplicationSettingsScreen}.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/reader/{ReaderSettingsUiState,ReaderSettingsViewModel,ReaderSettingsScreen}.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/test/java/org/mewx/wenku8/feature/settings/application/ApplicationSettingsViewModelTest.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/test/java/org/mewx/wenku8/feature/settings/reader/ReaderSettingsViewModelTest.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/SettingsScreenTest.kt`

- [ ] **Step 1: Write failing settings/migration/recreation tests**

Application tests cover loading/content/failure-legacy-readable, atomic Simplified and Traditional language pairs, zh-HK app locale with traditional content/fallback, system/light/dark, dynamic color support, e-ink, pending legacy projection, retry, locale recreation preserving route and selection, and rapid mutation ordering. Reader tests cover every Phase 6 preference, normalized bounds, paginated/continuous, volume keys, tap zones, transition style, font/background import/reset effects, failure recovery, and live reader update.

```powershell
.\gradlew.bat :feature:settings:testDebugUnitTest --tests "org.mewx.wenku8.feature.settings.application.*" --tests "org.mewx.wenku8.feature.settings.reader.*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because routes are absent.

- [ ] **Step 2: Implement ViewModels over SettingsRepository only**

Both ViewModels collect `SettingsRepository.settings` and `readiness` with `stateIn(WhileSubscribed(5_000), initial)`. Each user action sends one existing typed `SettingsMutation`; it renders `Synchronized`, `PendingSynchronization`, or `Failure` explicitly. Language selection uses only `LanguageSelection.SIMPLIFIED`, `LanguageSelection.TRADITIONAL`, or `LanguageSelection(AppLocale.ZH_HK, ContentLanguage.TRADITIONAL_CHINESE)`. No route writes DataStore, SharedPreferences, `settings.wk8`, or `GlobalConfig`.

Reader imports emit platform effects containing only `ReaderAssetKind.FONT` or `ReaderAssetKind.BACKGROUND`; the app launcher returns `ExternalDocumentToken` to `ReaderAssetRepository`, which validates/copies atomically and returns a canonical opaque asset key. Reset is a separate confirmed action.

- [ ] **Step 3: Implement Material 3 controls with stable dimensions**

Use `ListItem`, `SingleChoiceSegmentedButtonRow` for theme/reader mode, `RadioButton` for language, `Switch` for binary settings, bounded `Slider` with visible value for numeric settings, `DropdownMenu` for transition style, and icon import/reset commands with tooltips. Reader settings show visible accessible alternatives to gestures/volume keys. Each control has a stable row height or responsive constraint and remains usable at font scale 2.0.

- [ ] **Step 4: Run affected suites and commit**

```powershell
.\gradlew.bat :feature:settings:testDebugUnitTest :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.settings.SettingsScreenTest :core:data:testDebugUnitTest --tests "*Settings*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add feature/settings/src/main/java/org/mewx/wenku8/feature/settings/application feature/settings/src/main/java/org/mewx/wenku8/feature/settings/reader feature/settings/src/test/java/org/mewx/wenku8/feature/settings/application feature/settings/src/test/java/org/mewx/wenku8/feature/settings/reader feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/SettingsScreenTest.kt
git diff --check --cached
git commit -m "feat(ui): add application and reader settings"
```

Expected: settings remain canonical/legacy synchronized or visibly pending; all UI tests PASS.

### Task 9: Implement Cache, Storage, Migration, And Diagnostic Export

**Files:**
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/settings/WorkCacheMaintenanceRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/settings/StorageMigrationStatusRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/settings/WorkCacheMaintenanceRepositoryTest.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/settings/StorageMigrationStatusRepositoryTest.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/storage/{StorageUiState,StorageViewModel,StorageScreen}.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/migration/{MigrationUiState,MigrationViewModel,MigrationScreen}.kt`
- Test: `studio-android/LightNovelLibrary/feature/settings/src/test/java/org/mewx/wenku8/feature/settings/storage/StorageViewModelTest.kt`
- Test: `studio-android/LightNovelLibrary/feature/settings/src/test/java/org/mewx/wenku8/feature/settings/migration/MigrationViewModelTest.kt`
- Test: `studio-android/LightNovelLibrary/feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/StorageMigrationScreenTest.kt`

- [ ] **Step 1: Write failing ownership, cancellation, redaction, and recovery tests**

Seed canonical data, downloaded content, custom assets, known-good legacy files, rebuildable cache, generated images, stale non-downloaded chapters, journals, diagnostics, session data, and interrupted cleanup. Prove only selected cache categories are deleted; cleanup checkpoints/cancels/resumes; no canonical/download/custom/legacy/migration/session boundary is touched; storage denial/full I/O is recoverable; status maps all six domains; export keys are exactly `{day,migration,reconciliation,pending}` and contains no identifier/free text/time finer than day; cancelling share sends nothing.

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.WorkCacheMaintenanceRepositoryTest" --tests "org.mewx.wenku8.core.data.settings.StorageMigrationStatusRepositoryTest" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because repositories are absent.

- [ ] **Step 2: Implement bounded cleanup through the Phase 3 scheduler**

Use stable work key `cache-maintenance`, a canonical progress row, bounded chunks of at most 100 metadata rows or files, checkpoint after each item, and the Phase 3 `TransferScheduler` cancellation contract. `CacheCleanupRequest` maps only to catalog-cache metadata/generated cache roots classified by `LegacyPathPolicy`; it never recursively deletes a computed arbitrary path. Every resolved path is checked with `canonicalFile.toPath().startsWith(approvedRoot.canonicalFile.toPath())` before deletion. Success reports reclaimed byte count from deleted entries only.

- [ ] **Step 3: Map migration state and local diagnostic preview**

`StorageMigrationStatusRepository` combines Phase 1 settings state, Phase 2 credential state, and Phase 3 coordinator records. It maps exact enum states, buckets pending counts without exposing exact high-cardinality values, and delegates export bytes to Phase 0 `MigrationDiagnostics`. The repository has no share Intent and no network sink.

- [ ] **Step 4: Build Material 3 storage and migration routes**

Storage uses usage rows, `LinearProgressIndicator`, category `Checkbox` controls, clear confirmation, active progress/cancel, success bytes, and storage failure retry. Migration shows all six domain rows with phase/reconciliation/pending state descriptions, retry/reconcile command only where defined, and explicit preview-before-system-share. Back closes confirmation/preview before route pop. No raw path, journal ID, account ID, file name, or timestamp is displayed.

- [ ] **Step 5: Run focused and affected suites, then commit**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.*" :feature:settings:testDebugUnitTest :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.settings.StorageMigrationScreenTest :core:storage:testDebugUnitTest --tests "*Migration*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add core/data/src/main/java/org/mewx/wenku8/core/data/settings/WorkCacheMaintenanceRepository.kt core/data/src/main/java/org/mewx/wenku8/core/data/settings/StorageMigrationStatusRepository.kt core/data/src/test/java/org/mewx/wenku8/core/data/settings feature/settings/src/main/java/org/mewx/wenku8/feature/settings/storage feature/settings/src/main/java/org/mewx/wenku8/feature/settings/migration feature/settings/src/test feature/settings/src/androidTest
git diff --check --cached
git commit -m "feat(settings): add storage and migration surfaces"
```

Expected: cleanup and UI tests PASS; protected data remains byte-identical.

### Task 10: Implement Safe Menu Wallpaper Selection And Import

**Files:**
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/settings/AtomicWallpaperRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/settings/AtomicWallpaperRepositoryTest.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/wallpaper/{WallpaperUiState,WallpaperViewModel,WallpaperScreen}.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/test/java/org/mewx/wenku8/feature/settings/wallpaper/WallpaperViewModelTest.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/WallpaperScreenTest.kt`

- [ ] **Step 1: Write failing import/security/process tests**

Test five built-ins, custom selected/missing, picker cancel, unreadable document, deceptive MIME, oversized bytes/dimensions, decoder failure, orientation, low storage, process death during copy, atomic replacement, reset, legacy `custom/menu_bg` projection, rollback read, and no arbitrary persisted external path. UI tests cover swatches/previews, selected semantics, import icon tooltip, progress, failure, reset confirmation, compact/expanded/font scale 2.0.

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.AtomicWallpaperRepositoryTest" :feature:settings:testDebugUnitTest --tests "org.mewx.wenku8.feature.settings.wallpaper.*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because wallpaper repository/route are absent.

- [ ] **Step 2: Implement bounded document validation and atomic copy**

The app converts the Activity Result URI to opaque `ExternalDocumentToken`; only `AtomicWallpaperRepository` resolves it through an injected document source. It reads at most 16 MiB, sniffs JPEG/PNG/WebP magic, bounds decoded dimensions to 12,000 by 12,000 and pixel count to 48 million, samples a preview off main, writes `custom/menu_bg.tmp`, fsyncs, atomically replaces `custom/menu_bg`, then applies `SettingsMutation.MenuBackground("0", canonicalOpaqueKey)`. Failure leaves previous bytes/settings unchanged. Built-ins map IDs `default-1` through `default-5` to the reviewed packaged assets and settings legacy values `1` through `5`.

- [ ] **Step 3: Implement the route and app picker effect**

Use a responsive image grid with stable aspect ratio, selected radio semantics, import icon, reset command, progress, inline failure, and confirmation. The feature emits `WallpaperEffect.OpenImageDocument`; `SecondaryPlatformLauncher` uses `ActivityResultContracts.OpenDocument` with `image/*` and returns only a token. No feature imports `ContentResolver` or stores a URI/path.

- [ ] **Step 4: Verify and commit**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.AtomicWallpaperRepositoryTest" :feature:settings:testDebugUnitTest :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.settings.WallpaperScreenTest :core:data:testDebugUnitTest --tests "*Settings*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add core/data/src/main/java/org/mewx/wenku8/core/data/settings/AtomicWallpaperRepository.kt core/data/src/test/java/org/mewx/wenku8/core/data/settings/AtomicWallpaperRepositoryTest.kt feature/settings/src/main/java/org/mewx/wenku8/feature/settings/wallpaper feature/settings/src/test/java/org/mewx/wenku8/feature/settings/wallpaper feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/WallpaperScreenTest.kt
git diff --check --cached
git commit -m "feat(settings): add safe wallpaper picker"
```

Expected: import/reset/rollback and UI tests PASS.

### Task 11: Implement About, Generated Licenses, Privacy, Notices, And Channel Updates

**Files:**
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/settings/PackagedAboutRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/settings/ChannelUpdateRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/settings/PackagedAboutRepositoryTest.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/settings/ChannelUpdateRepositoryTest.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/about/{AboutUiState,AboutViewModel,AboutScreen}.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/test/java/org/mewx/wenku8/feature/settings/about/AboutViewModelTest.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/AboutScreenTest.kt`

- [ ] **Step 1: Write failing packaged-document/channel/offline tests**

Assert version comes from injected build metadata; notices are sanitized controlled text with approved links only; licenses and source offer exactly match Phase 0 generated hashes; privacy describes no telemetry/ads/crash/AD_ID and user-initiated local diagnostic export; documents work offline; missing/hash-mismatched package is a visible error. Alpha/Baidu return `MANUAL_CHECK`; Playstore returns `STORE_MANAGED`; no feature contains an update endpoint, package installer, WebView, or HTML renderer.

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.PackagedAboutRepositoryTest" --tests "org.mewx.wenku8.core.data.settings.ChannelUpdateRepositoryTest" :feature:settings:testDebugUnitTest --tests "org.mewx.wenku8.feature.settings.about.*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because repositories/routes are absent.

- [ ] **Step 2: Implement only generated local evidence documents**

`PackagedAboutRepository` opens generated assets by the fixed keys `third-party-notices`, `source-offer`, and `privacy-summary`; verifies SHA-256 values from Phase 0 packaged evidence before returning text; supplies version metadata; and maps cached notice through the controlled rich-text sanitizer. It cannot read an arbitrary key/path. License source text is not duplicated by hand.

`ChannelUpdateRepository.action` is an exhaustive `when`. Manual check delegates to the audited flavor update repository/HostPolicy operation created by the compliance phase; Playstore returns store-managed and never performs a package request.

- [ ] **Step 3: Build unframed Material 3 document routes**

About uses version, notice, licenses, privacy, source offer, and update `ListItem` rows in one `LazyColumn`; licenses/privacy are subordinate routes with selectable text, headings, and approved external-link confirmation. Use top app bars and standard icons/tooltips. No nested cards, marketing hero, decorative gradient, or raw HTML.

- [ ] **Step 4: Run packaged, outbound, and UI gates; commit**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.*About*" --tests "org.mewx.wenku8.core.data.settings.*Update*" :feature:settings:testDebugUnitTest :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.settings.AboutScreenTest :verification-tools:verifyPackagedLicenses :verification-tools:verifyOutboundManifest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add core/data/src/main/java/org/mewx/wenku8/core/data/settings/PackagedAboutRepository.kt core/data/src/main/java/org/mewx/wenku8/core/data/settings/ChannelUpdateRepository.kt core/data/src/test/java/org/mewx/wenku8/core/data/settings feature/settings/src/main/java/org/mewx/wenku8/feature/settings/about feature/settings/src/test/java/org/mewx/wenku8/feature/settings/about feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/AboutScreenTest.kt
git diff --check --cached
git commit -m "feat(settings): add about and privacy surfaces"
```

Expected: documents, channel behavior, UI, license, and outbound gates PASS.

### Task 12: Implement The Native Compose Material 3 Image Viewer

**Files:**
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/settings/LocalImageRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/settings/LocalImageRepositoryTest.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/navigation/ImageViewerRouteArgs.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/main/java/org/mewx/wenku8/feature/settings/image/{ImageViewerUiState,ImageViewerViewModel,ImageViewerScreen,ZoomTransform}.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/test/java/org/mewx/wenku8/feature/settings/image/{ImageViewerViewModelTest,ZoomTransformTest}.kt`
- Create: `studio-android/LightNovelLibrary/feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/ImageViewerScreenTest.kt`

- [ ] **Step 1: Write failing argument/load/transform/save/back tests**

Cover missing/blank/malformed key and legacy path, local/cached image, bounded payload, unsupported/corrupt image, load retry, process death, pinch/keyboard zoom bounds, pan bounds, double-tap reset, clockwise 90/180/270/0 rotation, chrome hide/show, visible zoom/rotate/save controls, first Back shows chrome then pops, API 36 cancel/commit, MediaStore success, API 23-28 permission denial/grant, full/unmounted storage, partial write cleanup, duplicate display-name handling, font scale 2.0, and semantics actions.

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.LocalImageRepositoryTest" :feature:settings:testDebugUnitTest --tests "org.mewx.wenku8.feature.settings.image.*" -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because image viewer is absent.

- [ ] **Step 2: Define typed arguments and deterministic transform**

```kotlin
data class ImageViewerRouteArgs(val imageKey: String, val legacyPath: String? = null) {
    init { require(imageKey.length in 1..256); require(legacyPath == null || legacyPath.length <= 2048) }
}
data class ZoomTransform(val scale: Float = 1f, val rotationQuarterTurns: Int = 0, val offsetX: Float = 0f, val offsetY: Float = 0f) {
    fun normalized(maxX: Float, maxY: Float) = copy(
        scale = scale.coerceIn(1f, 5f),
        rotationQuarterTurns = Math.floorMod(rotationQuarterTurns, 4),
        offsetX = offsetX.coerceIn(-maxX, maxX),
        offsetY = offsetY.coerceIn(-maxY, maxY),
    )
}
```

SavedState stores opaque `imageKey`, optional frozen legacy path for compatibility, transform scalars, and chrome visibility; never image bytes or a remote URL.

- [ ] **Step 3: Implement bounded local open and MediaStore save**

`LocalImageRepository.open` resolves `LocalImageKey` through Phase 3 asset/cache indices first. A legacy path is accepted only from the trampoline, canonicalized, and required to be under an artifact-manifest-approved image root. It reads a maximum 32 MiB, sniffs media type, and returns a sanitized display name. It cannot open a network URI.

Save uses MediaStore with `IS_PENDING` on API 29+, clears pending only after close/fsync, and deletes the incomplete row on failure. API 23-28 writes only after permission grant to the reviewed DCIM subdirectory, uses atomic temp/rename and MediaScanner. Permission/storage failures map to exact `ImageSaveFailure` values.

- [ ] **Step 4: Implement full-bleed image UI with Material 3 chrome**

Render the bitmap full-bleed with `transformable` and `graphicsLayer`; controls are unframed top/bottom Material 3 bars containing Back, zoom out/reset/in, rotate, and save icons with localized tooltips/content descriptions. Keyboard `+`, `-`, `0`, `R`, `S`, arrows, Enter, and Escape map to the same visible actions. A loading indicator, recoverable error, save progress, permission rationale, denial recovery, and Snackbar are explicit. No gesture is the only path.

- [ ] **Step 5: Run API permission/storage and UI suites; commit**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.LocalImageRepositoryTest" :feature:settings:testDebugUnitTest :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.feature.settings.ImageViewerScreenTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add core/data/src/main/java/org/mewx/wenku8/core/data/settings/LocalImageRepository.kt core/data/src/test/java/org/mewx/wenku8/core/data/settings/LocalImageRepositoryTest.kt feature/settings/src/main/java/org/mewx/wenku8/feature/settings/navigation/ImageViewerRouteArgs.kt feature/settings/src/main/java/org/mewx/wenku8/feature/settings/image feature/settings/src/test/java/org/mewx/wenku8/feature/settings/image feature/settings/src/androidTest/java/org/mewx/wenku8/feature/settings/ImageViewerScreenTest.kt
git diff --check --cached
git commit -m "feat(ui): add native image viewer"
```

Expected: transforms, save recovery, semantics, and Back tests PASS.

### Task 13: Bind Repositories, Typed Routes, And Platform Launchers

**Files:**
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/AppContainer.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/DefaultAppContainer.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/AppRoute.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/AppDeepLink.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/Wenku8NavHost.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/AccountRouteFactory.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/SettingsRouteFactory.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/SecondaryPlatformLauncher.kt`
- Create: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/navigation/Phase7RouteFactoryTest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/Phase7NavigationTest.kt`

- [ ] **Step 1: Write failing DI/route/deep-link/launcher tests**

Assert one repository instance per interface, no implementation exposed by `AppContainer`, ViewModels created with SavedStateHandle, all twelve routes encode/decode using `Uri.Builder`, unknown/malformed inputs become `ArgumentError`, no exported browsable filter, top-level Settings state restoration, account/image subordinate Back, reader image callback, bookshelf login callback, locale recreation, process death, hinge resize, platform picker/share/save flows, and no feature access to Context/AppContainer.

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.Phase7RouteFactoryTest" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.Phase7NavigationTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because bindings/routes are absent.

- [ ] **Step 2: Extend AppContainer with interfaces only**

Add:

```kotlin
val accountRepository: AccountRepository
val cacheMaintenanceRepository: CacheMaintenanceRepository
val migrationStatusRepository: MigrationStatusRepository
val wallpaperRepository: WallpaperRepository
val aboutRepository: AboutRepository
val updateRepository: UpdateRepository
val localImageRepository: LocalImageRepository
```

`DefaultAppContainer` constructs the concrete implementations once using existing provider/session/storage/scheduler/settings bindings. No composable/ViewModel sees those constructors, DAOs, path policies, ContentResolver, provider source, or AppContainer.

- [ ] **Step 3: Add exact typed AppRoute members**

```kotlin
data object Login : AppRoute
data object Profile : AppRoute
data object Settings : AppRoute
data object ApplicationSettings : AppRoute
data object ReaderSettings : AppRoute
data object StorageSettings : AppRoute
data object MigrationSettings : AppRoute
data object WallpaperSettings : AppRoute
data object About : AppRoute
data object Licenses : AppRoute
data object Privacy : AppRoute
data class ImageViewer(val args: ImageViewerRouteArgs) : AppRoute
```

`AppDeepLink` accepts only internal `wenku8://app` route names, bounds keys/legacy path, and uses Android URI query APIs. No route accepts a network URL or account value. `Wenku8NavHost` installs all destinations in the one shell NavController.

- [ ] **Step 4: Implement platform entry callbacks**

```kotlin
data class AccountEntryPoints(
    val openProfile: () -> Unit,
    val openLogin: () -> Unit,
    val openRegistration: (String) -> Unit,
    val closeAccount: () -> Unit,
)
data class SettingsEntryPoints(
    val openRoute: (SettingsRoute) -> Unit,
    val openDocument: (DocumentKind) -> Unit,
    val shareDiagnostic: (ByteArray) -> Unit,
    val requestLegacyImagePermission: () -> Unit,
    val close: () -> Unit,
)
```

`SecondaryPlatformLauncher` validates accepted HTTPS registration/update links, uses `ACTION_VIEW` chooser only after confirmation, Activity Result document contracts for imports, `ACTION_SEND` only after diagnostic preview/confirmation, and runtime storage permission only on API 23-28 image save. It never logs Intent data or persists diagnostic bytes.

- [ ] **Step 5: Run architecture/navigation suites and commit**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.Phase7RouteFactoryTest" --tests "org.mewx.wenku8.di.*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.Phase7NavigationTest :feature:account:testDebugUnitTest :feature:settings:testDebugUnitTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add app/src/main/java/org/mewx/wenku8/di app/src/main/java/org/mewx/wenku8/navigation app/src/test/java/org/mewx/wenku8/navigation/Phase7RouteFactoryTest.kt app/src/androidTest/java/org/mewx/wenku8/navigation/Phase7NavigationTest.kt
git diff --check --cached
git commit -m "feat(app): bind phase seven routes"
```

Expected: DI, navigation, platform-effect, and architecture tests PASS.

### Task 14: Replace A11-A15 With Compatibility Trampolines

**Files:**
- Move/Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/UserLoginActivity.kt` -> `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/legacy/LegacyUserLoginActivity.kt`
- Move/Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/UserInfoActivity.kt` -> `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/legacy/LegacyUserInfoActivity.kt`
- Move/Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/MenuBackgroundSelectorActivity.kt` -> `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/legacy/LegacyMenuBackgroundSelectorActivity.kt`
- Move/Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/AboutActivity.kt` -> `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/legacy/LegacyAboutActivity.kt`
- Move/Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/ViewImageDetailActivity.kt` -> `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/legacy/LegacyViewImageDetailActivity.kt`
- Create: original five Activity paths as thin trampolines.
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/compat/Phase7LegacyEntryTest.kt`
- Modify: `docs/verification/intent-contract.yaml`
- Modify: `docs/verification/ui-owner-action-ledger.yaml`

- [ ] **Step 1: Write failing old-sender/minified/API boundary tests**

Use Phase 0 old-signed fixtures on API 23/32/33. Assert old explicit class names launch once; login/profile/wallpaper/about route correctly; image accepts the exact `path` String/null/malformed cases; missing path becomes visible argument error with zero file/network call; API 33 typed Bundle behavior is preserved; recreation does not duplicate; trampoline predictive Back returns to caller; old back-stack order remains; release manifest has original class identities and no legacy implementation component.

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.compat.Phase7LegacyEntryTest :app:assembleAlphaRelease -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL because old classes still own pages.

- [ ] **Step 2: Preserve source but remove every legacy page from product reachability**

Rename class/package declarations to the five `activity.legacy` names, update only their internal self references, and remove them from the product manifest and all product navigation/actions. Do not delete their source or frozen resource contracts in this phase. No debug menu or product deep link can launch them; a test-only direct class fixture may inspect retained behavior without declaring a product component.

- [ ] **Step 3: Implement one reusable route trampoline base**

Each original class extends a non-page `ComponentActivity`, calls `LegacyIntentCodec` where arguments exist, starts `Wenku8ShellActivity` with `AppDeepLink.encode(route)`, propagates reviewed task flags, sets a saved `launched` Boolean, then finishes. It does not call `setContent`, `setContentView`, `findViewById`, a provider, storage, or network. Image uses only `LegacyIntentCodec.decodeImageViewer(intent)`; other classes read no extras.

- [ ] **Step 4: Update exact manifest/ledger retirement states**

The original five names remain non-exported unless the frozen Phase 0 manifest proves a different exported policy. Ledger rows A11-A15 point to route IDs, trampoline tests, retained rollback source, and Phase 8 retirement owner. Legacy implementation class names do not appear as manifest components. The old `path` row remains until Phase 8 evidence proves all old senders retired.

- [ ] **Step 5: Run frozen compatibility and commit**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.compat.Phase7LegacyEntryTest :app:assembleAlphaRelease :verification-tools:verifyIntentContracts -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
git diff --check
git add app/src/main/java/org/mewx/wenku8/activity app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt app/src/main/AndroidManifest.xml app/src/androidTest/java/org/mewx/wenku8/compat/Phase7LegacyEntryTest.kt ../../docs/verification/intent-contract.yaml ../../docs/verification/ui-owner-action-ledger.yaml
git diff --check --cached
git commit -m "refactor(app): route secondary legacy entries to Compose"
```

Expected: old-signed/minified entry tests PASS and all entries arrive at one Compose route.

### Task 15: Complete State, Restoration, Adaptive, Back, Insets, And Localization Coverage

**Files:**
- Modify: all Phase 7 `*UiState.kt`, `*ViewModel.kt`, and `*Screen.kt` files named in Tasks 4-12.
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/Phase7ProcessDeathTest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/{Phase7RouteProcessDeathSeedTest,Phase7RouteProcessDeathVerifyTest}.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/Phase7AdaptiveBackTest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/localization/Phase7LocalizationTest.kt`
- Modify: `studio-android/LightNovelLibrary/feature/account/src/main/res/values/strings.xml`
- Create/Modify: `studio-android/LightNovelLibrary/feature/account/src/main/res/values-zh-rTW/strings.xml`
- Create/Modify: `studio-android/LightNovelLibrary/feature/account/src/main/res/values-zh-rHK/strings.xml`
- Modify: `studio-android/LightNovelLibrary/feature/settings/src/main/res/values/strings.xml`
- Create/Modify: `studio-android/LightNovelLibrary/feature/settings/src/main/res/values-zh-rTW/strings.xml`
- Create/Modify: `studio-android/LightNovelLibrary/feature/settings/src/main/res/values-zh-rHK/strings.xml`

- [ ] **Step 1: Write failing matrix tests before filling gaps**

Generate tests from the twelve route IDs and require successful content plus every applicable loading/empty/error/offline/auth/submitting/permission/storage state; Dialog/Sheet/chrome Back; API 36 predictive start/cancel/commit; in-process saved-state recreation; locale recreation; IME open; compact-height landscape; compact/medium/expanded; separating/occluding hinge; Sheet-to-pane resize; focus return; and no duplicate request/navigation. The separate seed/verify classes cover all twelve routes across a changed PID, clear password/captcha while restoring permitted username/selection/transform state, and reject duplicate repository requests/effects.

Run the three instrumented classes. Expected: FAIL with exact missing route/state/configuration IDs.

- [ ] **Step 2: Complete restoration and Back order**

Persist only route arguments and small non-secret presentation state. Restore from repositories for content. Global Back order is permission/dialog/preview, compact Sheet, image/reader chrome, drawer, subordinate route, top-level Activity. Expanded permanent panes are not closed. Predictive cancel mutates no form, selection, transform, or navigation state. Image viewer Back first restores visible chrome.

- [ ] **Step 3: Complete adaptive/inset behavior**

Use Phase 1 `AdaptiveLayoutInfo`; compact is one pane, medium uses rail/supporting pane only at readable widths, expanded uses permanent rail/drawer and hinge-safe panes. No fixed 300dp or 320dp side panel. Edge-to-edge/system/IME/cutout/gesture/three-button insets are handled centrally. Font scale 2.0 scrolls without hiding the primary action.

- [ ] **Step 4: Complete resource localization**

Every product string, error, content description, state description, and plural is a resource. Simplified selection pairs zh-CN with simplified content; Traditional pairs zh-TW with traditional content; zh-HK uses Hong Kong resources with zh-TW then default fallback and traditional content. Locale recreation preserves route, username only, settings selection, storage selection, wallpaper selection, document position, and image transform.

- [ ] **Step 5: Run full matrix and commit**

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.Phase7ProcessDeathTest,org.mewx.wenku8.navigation.Phase7AdaptiveBackTest,org.mewx.wenku8.localization.Phase7LocalizationTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :feature:account:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\tools\verification\run-ui-process-death.ps1 -Phase 7 -Api 36 -SeedClass org.mewx.wenku8.navigation.Phase7RouteProcessDeathSeedTest -VerifyClass org.mewx.wenku8.navigation.Phase7RouteProcessDeathVerifyTest -Provider public
git diff --check
git add app/src/androidTest feature/account/src feature/settings/src
git diff --check --cached
git commit -m "test(ui): complete secondary route matrices"
```

Expected: every generated route/state/configuration row PASSes.

### Task 16: Prove Accessibility And Assistive-Technology Journeys

**Files:**
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/accessibility/Phase7AccessibilityTest.kt`
- Create: `docs/verification/manual-a11y-phase7.md`
- Modify: `docs/verification/{ui-owner-action-ledger,manual-assistive-technology-manifest}.yaml`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: all Phase 7 screen files with a failing semantics row.

- [ ] **Step 1: Add failing automated semantics assertions**

For every route/state assert heading, role, label, state description, selected/checked/disabled/expanded, collection info, progress range, error, live region, traversal order, minimum target, icon description, decorative-image semantics removal, meaningful avatar/captcha/image description, validation focus, dialog/Sheet focus return, page-change announcements, and keyboard/IME actions.

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.accessibility.Phase7AccessibilityTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL with stable route/state/semantic IDs.

- [ ] **Step 2: Fix semantics and focus at owning controls**

Use Compose semantics properties and `FocusRequester` at the control that owns each state. Captcha refresh, password visibility, image rotate/save/zoom, wallpaper import/reset, cache cancel, diagnostic share, licenses/privacy navigation, and logout have localized labels/tooltips. Check-in has no node when unsupported. Error retries return focus to field/invoker; Dialog/Sheet close returns focus to invoker.

- [ ] **Step 3: Execute exact manual journeys**

Record tester/date/device/API/result/failure reference for:

```text
P7-A11 TalkBack captcha refresh -> fields -> login failure -> retry -> profile -> logout
P7-A12 TalkBack profile offline/auth-required and account bookshelf move
P7-A13 TalkBack application/reader settings including sliders/import/reset
P7-A14 TalkBack storage cleanup -> cancel -> retry and migration preview -> share cancel
P7-A15 TalkBack wallpaper built-in/import failure/reset and About/licenses/privacy
P7-A16 TalkBack image zoom/rotate/save denial/recovery/back
P7-A17 Keyboard/DPAD all Phase 7 routes and dialogs
P7-A18 Switch Access login/settings/wallpaper/image commands
P7-A19 API36 predictive Back dialog/sheet/chrome/drawer/subordinate/trampoline
```

No account/profile/captcha image or user document is captured in shared evidence; deterministic synthetic fixtures only.

Each TalkBack/Switch Access PASS uses the shared structured schema and binds service/device/configuration/source/APK/report hashes plus tester and independent reviewer. Register the exact phase gate before running evidence verification:

```groovy
tasks.register('verifyPhase7AssistiveEvidence', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyAssistiveEvidence', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath, '7'
}
```

- [ ] **Step 4: Run and commit accessibility evidence**

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.accessibility.Phase7AccessibilityTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :verification-tools:verifyPhase7AssistiveEvidence -Pwenku8Provider=public --console=plain
Set-Location '..\..'
& .\tools\verify-phase7-account-secondary.ps1
git diff --check
git add studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/accessibility studio-android/LightNovelLibrary/feature/account/src/main studio-android/LightNovelLibrary/feature/settings/src/main docs/verification/manual-a11y-phase7.md docs/verification/ui-owner-action-ledger.yaml
git diff --check --cached
git commit -m "test(ui): verify secondary accessibility journeys"
```

Expected: automated/manual rows PASS with no unresolved Critical or Important issue.

### Task 17: Record And Approve The Deterministic Phase 7 Screenshot Matrix

**Files:**
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/screenshot/Phase7AccountSettingsGoldenTest.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `docs/verification/{ui-golden-manifest,manual-assistive-technology-manifest}.yaml`
- Create: `docs/verification/phase-7-visual-review.md`

- [ ] **Step 1: Add failing manifest coverage before recording**

Require every Phase 7 route success at 360x640 zh-CN/light/font1.0 and 1280x800 expanded; every applicable loading/empty/error/offline/auth/permission/storage/submitting state; dark+font2.0 including zh-TW and zh-HK fallback; login IME-open and 915x412 compact-height; medium navigation; API36 gesture; separating/occluding hinge; check-in hidden; accepted-capability synthetic state; image zoom/90-degree rotation/save success/permission failure/storage failure; wallpaper custom failure; cache active/cancel; migration preview.

```powershell
.\gradlew.bat :verification-tools:verifyUiGoldenManifest -Pphase=7 -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: FAIL with exact missing case IDs.

- [ ] **Step 2: Record only deterministic synthetic fixtures**

Tests inject fake repositories/clock/dispatchers, synthetic account/profile/captcha/image/document values, fixed insets/navigation/posture/locale/theme/font/display scale, and no network/database/live account. Captcha/profile values contain no real account data. Each case records fixture SHA-256, image SHA-256, structural/pixel tolerance, and approved dynamic mask set; time/random animations are disabled by harness rather than masked.

Register the exact app tasks with the Phase 1 helper:

```groovy
registerUiGoldenTask('recordPhase7UiGoldens', 'record', 7,
    'org.mewx.wenku8.screenshot.Phase7AccountSettingsGoldenTest')
registerUiGoldenTask('verifyPhase7UiGoldens', 'verify', 7,
    'org.mewx.wenku8.screenshot.Phase7AccountSettingsGoldenTest')
```

- [ ] **Step 3: Record, verify, and visually inspect every image**

```powershell
.\gradlew.bat :app:recordPhase7UiGoldens -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
$sourceCommit = (git rev-parse HEAD).Trim()
.\gradlew.bat :verification-tools:approveUiGoldens -Pphase=7 "-PuiGoldenReviewer=$env:WENKU8_UI_REVIEWER" "-PuiGoldenSourceCommit=$sourceCommit"
.\gradlew.bat :app:verifyPhase7UiGoldens :verification-tools:verifyUiGoldenManifest -Pphase=7 -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Inspect original resolution for clipping, overlap, orphaned CJK glyphs, horizontal overflow, double scroll, IME occlusion, hinge crossing, target size, wrapped commands, wrong state, unreadable wallpaper/image chrome, one-hue palette, decorative card piles, and content leaked into evidence.

- [ ] **Step 4: Approve baselines and commit**

`phase-7-visual-review.md` records case ID, manifest hash, reviewer, approval commit, defects/fixes, and PASS. Any defect is fixed and the focused screenshot rerun before approval.

```powershell
git add app/src/androidTest/java/org/mewx/wenku8/screenshot/Phase7AccountSettingsGoldenTest.kt ../../docs/verification/ui-goldens ../../docs/verification/ui-golden-manifest.yaml ../../docs/verification/phase-7-visual-review.md
git diff --check --cached
git commit -m "test(ui): approve phase seven goldens"
```

### Task 18: Prove Zero Legacy Page Reachability And Bind The Phase 7 Exit Gate

**Files:**
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/journey/Phase7AccountSecondaryJourneyTest.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/ui/ReachablePageVerifier.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/ui/ReachablePageVerifierTest.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `docs/verification/modernization-matrix.yaml`
- Create: `docs/verification/phase-7-exit-review.md`

- [ ] **Step 1: Register and dispatch the reachability verifier before RED**

```groovy
// verification-tools/build.gradle
tasks.register('verifyReachablePages', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyReachablePages', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}
```

Add the dispatcher immediately:

```kotlin
if (command == "verifyReachablePages") {
    org.mewx.wenku8.verification.ui.ReachablePageVerifier.verify(projectRoot, docsRoot)
    return
}
```

- [ ] **Step 2: Add failing semantic reachability fixtures and journey tests**

The verifier builds a graph from launcher/manifest Activities, NavHost routes, menu/click callbacks, notifications, explicit Intents, deep links, old class-name trampolines, and bytecode call sites. It joins every referenced layout to X01-X34 and fails if a reachable node owns `setContentView`, layout inflation, Fragment page, AppCompat Toolbar, CardView, AsyncTask, raw executor/thread, `findViewById`, provider/network/file orchestration, or a legacy page owner; an unclassified/missing X row is independently fatal. `ReachablePageVerifierTest` injects one reachable XML Activity and expects its exact shortest path and X ID, so RED is a verifier assertion rather than a missing Gradle task. The deterministic journey runs captcha login -> profile -> account bookshelf move -> recommendation -> logout, every settings subordinate route, cache cancel/retry, migration preview/share cancel, wallpaper import failure/recovery, About/licenses/privacy, reader image -> viewer transform/save failure/recovery, process restart, and old explicit entries.

```powershell
.\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.ui.ReachablePageVerifierTest" -Pwenku8Provider=public --console=plain
.\gradlew.bat :verification-tools:verifyReachablePages :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.journey.Phase7AccountSecondaryJourneyTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: the unit test first fails on the named synthetic XML path; after the verifier handles it correctly, the registered real-source task fails on actual remaining reachability/matrix rows, never with `Task not found`.

- [ ] **Step 3: Register the exact aggregate gate**

```groovy

// app/build.gradle
def repositoryRoot = rootProject.projectDir.parentFile.parentFile
tasks.register('verifyPhase7AccountSecondaryPlan', Exec) {
    group = 'verification'
    dependsOn ':verification-tools:installDist'
    workingDir repositoryRoot
    commandLine rootProject.ext.resolvePowerShell(), '-NoProfile', '-NonInteractive',
        '-File', new File(repositoryRoot, 'tools/verify-phase7-account-secondary.ps1').absolutePath
}

tasks.register('phase7AccountSecondaryGate') {
    group = 'verification'
    description = 'Canonical Phase 7 account/secondary aggregate gate.'
    dependsOn ':phase0Gate', ':verification-tools:phase1Gate',
        ':verification-tools:phase2Gate', ':verification-tools:phase3Gate',
        ':app:phase4LibraryGate', ':app:phase5NovelGate', ':app:phase6ReaderGate'
    dependsOn ':feature:account:testDebugUnitTest'
    dependsOn ':feature:settings:testDebugUnitTest'
    dependsOn 'verifyPhase7UiGoldens'
    dependsOn ':verification-tools:verifyReachablePages'
    dependsOn ':verification-tools:verifyPhase7AssistiveEvidence'
    dependsOn ':verification-tools:verifyUiGoldenManifest'
    dependsOn ':verification-tools:verifyXmlSurfaceLedger'
    dependsOn ':verification-tools:verifyPlannedGradleTasks'
    dependsOn ':verification-tools:verifySensitiveSource'
    dependsOn ':verification-tools:verifyOutboundManifest'
    dependsOn ':verification-tools:verifyPackagedLicenses'
    dependsOn ':verifyPhase0Coverage'
    dependsOn ':verifyArchitecture'
    dependsOn 'testAlphaDebugUnitTest'
    dependsOn 'lintAlphaDebug'
    dependsOn 'assembleAlphaRelease'
    dependsOn 'verifyPhase7AccountSecondaryPlan'
}
```

`:app:phase7AccountSecondaryGate` is the sole Phase 7 aggregate path; do not invent `phase7Gate`. Its explicit dependencies invoke Phase 0-6 prerequisite gates, the repository script, coverage thresholds, frozen Intent/Serializable/path/provider contracts, authorization/licensing/SBOM/provenance/notices/source-offer checks, and matrix completeness. It never runs a live smoke automatically.

- [ ] **Step 4: Run public channel and protected-private deterministic matrices**

```powershell
.\gradlew.bat :app:phase7AccountSecondaryGate :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.journey.Phase7AccountSecondaryJourneyTest -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
.\gradlew.bat :app:assembleBaiduDebug :app:assemblePlaystoreDebug -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
Set-Location '..\..'
& .\tools\verify-phase7-account-secondary.ps1
git diff --check
```

Expected: public deterministic gates PASS. Protected CI runs equivalent account/route/provider/compliance tests for private alphaDebug and minified alphaRelease and returns a fresh bound redacted attestation; public logs reveal no private detail.

- [ ] **Step 5: Run only currently authorized opt-in live read-only account smoke**

Use the Phase 2 environment-only `:api-public:liveReadOnlySmoke` task. The command contains no values:

```powershell
Set-Location 'studio-android\LightNovelLibrary'
.\gradlew.bat :api-public:liveReadOnlySmoke --no-daemon --console=plain -Pwenku8Provider=public
```

Expected only after interactive captcha and current accepted scope: the bounded Phase 2 read-only PASS line for login/session/profile/bookshelf/logout. If scope is denied/expired/unknown, the task fails before dispatch and Phase 7 records the live row as blocked, not passed. Recommendation, bookshelf mutation, review mutation, and check-in are not exercised without their separate per-run authorization gates.

- [ ] **Step 6: Run four independent final reviews and resolve findings**

Commission independent architecture/provider/secret review, migration/storage/rollback review, Compose MD3/navigation/adaptive/accessibility/visual review, and executable-plan/evidence review. `phase-7-exit-review.md` lists finding ID, severity, owner, resolution commit, focused command, affected-suite command, and final status. Critical or Important findings block exit.

- [ ] **Step 6: Commit the verified exit gate**

```powershell
git add studio-android/LightNovelLibrary/app/build.gradle studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/journey/Phase7AccountSecondaryJourneyTest.kt studio-android/LightNovelLibrary/verification-tools/build.gradle studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/ui/ReachablePageVerifier.kt docs/verification/modernization-matrix.yaml docs/verification/phase-7-exit-review.md
git diff --check --cached
git commit -m "test: bind phase seven exit gate"
```

## Phase 7 Completion Checklist

- [ ] Captcha is user-entered; password/captcha never persist and clear on every terminal/cancellation path.
- [ ] Session/profile/avatar/account caches are account-and-epoch partitioned; expiry/logout recovery passes.
- [ ] Unsupported check-in is absent with zero egress; accepted HTTPS capability behavior remains independently gated.
- [ ] Account bookshelf add/remove/move and recommendation state/recovery/capability tests pass.
- [ ] Application and reader settings use typed SettingsRepository mutations and rollback-compatible projections.
- [ ] Cache cleanup is bounded/cancellable and preserves canonical/download/custom/legacy/migration/session data.
- [ ] Migration diagnostics remain local, bounded, previewed, and explicitly shared only by the user.
- [ ] Wallpaper import and image viewer save recover from permission/storage/decoder/process failures.
- [ ] About/licenses/privacy/update documents match generated compliance evidence and channel policy.
- [ ] A11-A15 old class names and frozen image `path` behavior pass old-signed/minified API tests.
- [ ] Every product-reachable page/action is native Compose Material 3; no feature imports implementation layers.
- [ ] State/adaptive/Back/insets/localization/accessibility/screenshot matrices pass with approved evidence.
- [ ] Public and protected-private gates pass without credential/private disclosure.
- [ ] No Critical/Important independent finding remains; `git diff --check` passes; each task is isolated.

## Deliberate Fail-Closed Deferrals

- Daily check-in remains absent and dispatches zero work unless a separate independently accepted HTTPS operation contract enables `DAILY_CHECK_IN`; the known cleartext behavior is never implemented.
- Live account/mutation evidence remains blocked when the exact current scope is not accepted. Deterministic fixtures do not become live-completion evidence.
- Retained legacy source, class-name trampolines, route flags, Serializable DTOs, provider ABI bridge, and legacy projections remain until Phase 8 compatibility-window and reachability approvals.
- Signed/distributable artifacts remain blocked by unknown/expired authorization, unknown/incompatible license, stale/replayed private attestation, or incomplete compliance evidence.

## Execution Handoff

Plan complete at `docs/superpowers/plans/2026-07-10-wenku8-phase-7-account-secondary.md`. Execute task by task with `superpowers:subagent-driven-development`, first use `superpowers:using-git-worktrees`, and require specification review plus code-quality review after every task.
