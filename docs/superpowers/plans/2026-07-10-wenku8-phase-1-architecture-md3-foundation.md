# Wenku8 Phase 1 Architecture and Material 3 Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish the acyclic Kotlin module graph, explicit composition root, typed contracts, adaptive single-activity Compose Material 3 shell, route rollout/trampoline foundation, and crash-safe DataStore settings migration while the legacy application remains the release default.

**Architecture:** Pure Kotlin/JVM modules own models, provider/domain/session/network-policy contracts; Android library modules own storage, repository implementations, design primitives, and test fakes. `MyApp` owns a constructor-injected `AppContainer`, a new launcher trampoline selects legacy or Compose shell from local DataStore flags, and every existing settings writer is intercepted behind a durable journal/canonical-then-legacy projection protocol.

**Tech Stack:** Groovy Gradle 9.1.0, AGP 9.0.1 built-in Kotlin, Kotlin/Compose 2.2.10, Kotlin Coroutines 1.10.2, Compose BOM 2026.06.00, Material 3 Adaptive 1.2.0, Material 3 adaptive navigation suite 1.4.0, Navigation Compose 2.9.6, Lifecycle 2.9.4, WindowManager 1.5.1, DataStore 1.2.0, Room 2.8.3, KSP 2.2.10-2.0.2, ArchUnit 1.4.1, JUnit 4, AndroidX Test.

---

## Scope Boundary

This plan implements only Phase 1 of `docs/superpowers/specs/2026-07-10-wenku8-modernization-program-design.md`.

It does **not** create an OkHttp client/interceptor, Jsoup parser, GBK transport, CookieJar, encrypted SessionStore, captcha/login flow, public source implementation, repository remote/cache implementation, live-site request, feature ViewModel, or complete Discover/Bookshelf/Search/Settings page. `:api-public` receives identity only; every source remains capability-disabled and zero-egress until Phase 2.

All commands run from:

```powershell
Set-Location D:\Projects\10_Active\light-novel-library_Wenku8_Android\studio-android\LightNovelLibrary
```

## Preconditions and Stop Conditions

- Execute in an isolated worktree from the independently reviewed Phase 0 completion commit. `git status --short` must be empty.
- Phase 0 public-stub, compatibility inventory, old-signed/minified Intent/Serializable, privacy/egress, license/SBOM, backup, supply-chain, and warning/coverage gates must pass. A denied authorization scope may not be bypassed; Phase 1 itself performs no live/network work.
- Preserve `applicationId "org.mewx.wenku8"`, launcher `org.mewx.wenku8.activity.MainActivity`, every Activity class name/Intent default, `VolumeList`/`ChapterInfo` serialization identity, and logical `:api` ABI.
- The legacy experience remains the default: `RouteRolloutFlags.shellEnabled=false` in every release flavor until Phase 4 exit evidence changes it. Phase 1 shell is reachable only by a debug/developer switch or an explicit instrumentation fixture.
- Do not convert a legacy feature page in this phase. Existing XML/Fragment pages remain compatibility destinations and are launched through typed bridges.
- Do not add a DI framework. New code obtains dependencies from constructor parameters or a route factory; composables never read `MyApp`/`AppContainer` directly.
- Do not add Android imports to `:core:model`, `:core:domain`, `:core:session-contract`, `:core:network`, `:api-contract`, `:api-public`, or `:api-contract-tests`.
- Do not add product flavors to library modules. `alpha`, `baidu`, and `playstore` remain `:app` concerns; provider selection remains a Gradle property/logical `:api` concern.
- If any planned version fails strict verification from its approved repository, stop, update the reviewed version catalog/locks/verification metadata, and rerun Phase 0 supply-chain gates. Never use a dynamic version or silent mirror.
- Every setting writer listed in the Phase 0 preference inventory must be behind `SettingsWriteBarrier` before the coordinator may enter `Snapshotting`. A missed writer is a hard stop.
- The canonical settings write must be durable before the compatible legacy projection. `runBlocking` on the main thread, fire-and-forget writes without a durable journal, and cross-store “atomic transaction” claims are forbidden.
- `migration-transient.db` is a separate Room database at `context.getDatabasePath("migration-transient.db")`, excluded in its entirety (including sidecars) from backup. Canonical settings stay in `files/datastore/app_settings.preferences_pb` and contain sufficient version/value state to rebuild a lost/stale legacy projection without the transient DB.
- A task is complete only after RED fails for the intended reason, PASS succeeds, `git diff --check` succeeds, and its focused commit exists.

## Phase Exit Conditions

- All eleven foundational modules below compile with the exact acyclic dependency graph; public `:api-contract`, `:api-public`, and `:api-contract-tests` are always included.
- `AppContainer`, injected dispatchers/clock, provider/session bindings, route factories, immutable StateFlow conventions, and test replacements are compiled and covered.
- The shell renders compact, medium, expanded, compact-height, separating-hinge, light, dark, dynamic-color-off/on states without duplicate Sheet/pane presentation or inaccessible navigation.
- `MainActivity` remains the external launcher/trampoline; default release routing opens `LegacyMainActivity`. Typed route flags can select `Wenku8ShellActivity` in deterministic tests.
- No feature package imports forbidden legacy/network/storage implementations; no composable performs storage/network/file work or obtains the container.
- Every existing settings writer is intercepted. Migration/import/dual-write/reconcile is idempotent across every checkpoint/crash boundary and concurrent writer test.
- A canonical commit followed by process death before legacy projection returns/reports pending synchronization, then reconciliation produces exact legacy bytes. It never claims both stores synchronized early.
- Whole-store backup/restore, including capture after canonical commit and before projection with transient DB absent, regenerates the compatible legacy projection.
- An old signed rollback build reads the projected settings after new-build writes; no non-secret legacy setting artifact is deleted.
- Public debug/unit/instrumentation/lint/coverage/architecture gates and Phase 0 gates remain green; private claims require a fresh protected attestation.

## Exact Module and Namespace Map

| Gradle project | Directory | Type | Namespace/package root |
| --- | --- | --- | --- |
| `:app` | `app/` | Android application | `org.mewx.wenku8` |
| logical `:api` | `api-stub/` public; protected `api/` private | Android library | frozen legacy ABI |
| `:api-contract` | `api-contract/` | Kotlin/JVM java-library | `org.mewx.wenku8.api.contract` |
| `:api-public` | `api-public/` | Kotlin/JVM java-library | `org.mewx.wenku8.api.publicprovider` |
| `:api-contract-tests` | `api-contract-tests/` | Kotlin/JVM java-test-fixtures | `org.mewx.wenku8.api.contract.testing` |
| `:api-private-adapter` | `api-private-adapter/` | Android library, no flavor; private graph only | `org.mewx.wenku8.api.privateadapter` |
| `:core:model` | `core/model/` | Kotlin/JVM java-library | `org.mewx.wenku8.core.model` |
| `:core:domain` | `core/domain/` | Kotlin/JVM java-library | `org.mewx.wenku8.core.domain` |
| `:core:session-contract` | `core/session-contract/` | Kotlin/JVM java-library | `org.mewx.wenku8.core.session` |
| `:core:network` | `core/network/` | Kotlin/JVM java-library | `org.mewx.wenku8.core.network` |
| `:core:storage` | `core/storage/` | Android library, no flavor | `org.mewx.wenku8.core.storage` |
| `:core:data` | `core/data/` | Android library, no flavor | `org.mewx.wenku8.core.data` |
| `:core:designsystem` | `core/designsystem/` | Android Compose library, no flavor | `org.mewx.wenku8.core.designsystem` |
| `:core:testing` | `core/testing/` | Android test-support library, no flavor | `org.mewx.wenku8.core.testing` |

`verification-tools` remains the Phase 0 JVM gate module and is not a product dependency.

## Exact Dependency Graph

```text
core:model
├── api-contract
│   ├── api-public
│   ├── api-contract-tests
│   └── core:domain
└── core:session-contract
    └── core:network

core:storage -> core:model + core:session-contract
core:data -> core:model + core:domain + api-contract + core:network + core:storage
core:designsystem -> Compose Material 3/Adaptive only
core:testing -> core:model + core:domain + core:session-contract + Android/coroutines test libraries
app -> api-contract + api-public + core:model/domain/session/network/storage/data/designsystem + logical :api
api-private-adapter -> api-contract + core:model + logical :api (private graph only)
```

No edge points from a core/API JVM module to `:app`, Android UI, Room, DataStore, OkHttp, or Jsoup. `:core:network` defines policy values only in this phase.

## File Structure

### Gradle

- Modify `studio-android/LightNovelLibrary/settings.gradle`: exact project descriptors and public/private graph; no duplicate project directory/build directory.
- Extend `studio-android/LightNovelLibrary/gradle/libs.versions.toml`: Phase 1 single version source.
- Create one focused `build.gradle` in each new module directory.
- Modify `studio-android/LightNovelLibrary/app/build.gradle`: module dependencies, Compose/adaptive/navigation/lifecycle/splash dependencies, KSP/Room/DataStore tests.

### Contracts and Runtime

- `core/model/src/main/kotlin/org/mewx/wenku8/core/model/{identity,catalog,account,community,settings}/`: immutable platform-neutral values.
- `api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/`: result/failure/capability/request/source/provider contracts.
- `api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderIdentity.kt`: identity only.
- `api-contract-tests/src/testFixtures/kotlin/org/mewx/wenku8/api/contract/testing/ProviderContractSuite.kt`: reusable disabled-capability contract.
- `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/{catalog,account,community,settings,runtime}/`: repository/use-case/runtime interfaces.
- `core/session-contract/src/main/kotlin/org/mewx/wenku8/core/session/`: redacted session values/store interface.
- `core/network/src/main/kotlin/org/mewx/wenku8/core/network/policy/`: pure HostPolicy types.
- `core/data/src/main/java/org/mewx/wenku8/core/data/cache/`: typed cache/single-flight contracts only.
- `app/src/main/java/org/mewx/wenku8/di/`: `AppContainer`, owner, default/test bindings and disabled provider.

### Material 3 Shell

- `core/designsystem/src/main/java/org/mewx/wenku8/core/designsystem/theme/{Color,Type,Shape,Spacing,Wenku8Theme}.kt`.
- `core/designsystem/src/main/java/org/mewx/wenku8/core/designsystem/component/{Wenku8Scaffold,StateContent}.kt`.
- `app/src/main/java/org/mewx/wenku8/navigation/{AppDestination,AdaptiveLayoutInfo,ShellUiState,ShellEffect,ShellViewModel,Wenku8App,Wenku8NavHost,LegacyDestinationLauncher}.kt`.
- `app/src/main/java/org/mewx/wenku8/activity/{MainActivity,LegacyMainActivity,Wenku8ShellActivity}.kt`.
- `app/src/debug/java/org/mewx/wenku8/debug/DeveloperRouteSwitchActivity.kt`.
- Compose UI/instrumentation/golden tests under `app/src/androidTest/java/org/mewx/wenku8/navigation/` and `docs/verification/ui-golden-manifest.yaml`.

### Settings Storage and Migration

- `core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/{AppSettings,LanguageSelection,ReaderPreferences,RouteRolloutFlags,VersionedSettings}.kt`.
- `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/settings/{SettingsRepository,RouteFlagRepository,SettingsMutation,SettingsWriteResult}.kt`.
- `core/storage/src/main/java/org/mewx/wenku8/core/storage/settings/{SettingsPreferencesDataSource,LegacySettingsCodec,LegacySettingsAdapter,SettingsWriteBarrier,SettingsSnapshotCodec}.kt`.
- `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/{MigrationTransientDatabase,SettingsMigrationDao,SettingsMigrationRecord,SettingsCheckpointRecord,SettingsJournalRecord,MigrationState,JournalState}.kt`.
- `core/data/src/main/java/org/mewx/wenku8/core/data/settings/{DataStoreSettingsRepository,SettingsMigrationCoordinator,SettingsReconciler}.kt`.
- Golden fixtures under `core/storage/src/test/resources/legacy/settings/`; no copyrighted content or secrets.
- Tests under `core/storage/src/test/java/org/mewx/wenku8/core/storage/settings/`, `core/data/src/test/java/org/mewx/wenku8/core/data/settings/`, and `app/src/androidTest/java/org/mewx/wenku8/migration/`.
- Modify Phase 0 backup XML/manifest and create `docs/verification/settings-migration-manifest.yaml`.

## Task 1: Create the Groovy Gradle Module Graph and Version Catalog

**Files:**

- Modify: `studio-android/LightNovelLibrary/settings.gradle`
- Modify: `studio-android/LightNovelLibrary/build.gradle`
- Modify: `studio-android/LightNovelLibrary/gradle/libs.versions.toml`
- Create: `studio-android/LightNovelLibrary/gradle/powershell.gradle`
- Create: `studio-android/LightNovelLibrary/api-contract/build.gradle`
- Create: `studio-android/LightNovelLibrary/api-public/build.gradle`
- Create: `studio-android/LightNovelLibrary/api-contract-tests/build.gradle`
- Create: `studio-android/LightNovelLibrary/api-private-adapter/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/model/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/domain/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/network/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/session-contract/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/storage/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/data/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/designsystem/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/testing/build.gradle`
- Create: `studio-android/LightNovelLibrary/feature/library/build.gradle`
- Create: `studio-android/LightNovelLibrary/feature/novel/build.gradle`
- Create: `studio-android/LightNovelLibrary/feature/reader/build.gradle`
- Create: `studio-android/LightNovelLibrary/feature/account/build.gradle`
- Create: `studio-android/LightNovelLibrary/feature/settings/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/architecture/GradleGraphTest.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/build/PowerShellResolverTest.kt`

- [ ] **Step 1: Write a RED graph test against the exact map above**

Parse `settings.gradle` project descriptors plus Gradle's project/dependency report. Require unique canonical `projectDir` and `buildDir`, always-present public contract/public/test modules, private adapter only for the private graph, and no undeclared edge. Require no product flavor block outside `:app`/logical `:api`.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "*.GradleGraphTest" -Pwenku8Provider=public
```

Expected: FAIL because projects do not exist.

- [ ] **Step 2: Extend the checked-in version catalog**

Add exact entries; do not duplicate literals in module files:

```toml
[versions]
coroutines = "1.10.2"
lifecycle = "2.9.4"
navigation = "2.9.6"
datastore = "1.2.0"
room = "2.8.3"
ksp = "2.2.10-2.0.2"
window = "1.5.1"
material3-adaptive = "1.2.0"
material3-navigation-suite = "1.4.0"
archunit = "1.4.1"
splashscreen = "1.0.1"
compose-bom = "2026.06.00"
androidx-test-runner = "1.7.0"
androidx-test-ext-junit = "1.3.0"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

[libraries]
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-window = { module = "androidx.window:window", version.ref = "window" }
androidx-material3-adaptive = { module = "androidx.compose.material3.adaptive:adaptive", version.ref = "material3-adaptive" }
androidx-material3-adaptive-navigation = { module = "androidx.compose.material3:material3-adaptive-navigation-suite", version.ref = "material3-navigation-suite" }
androidx-splashscreen = { module = "androidx.core:core-splashscreen", version.ref = "splashscreen" }
archunit-junit4 = { module = "com.tngtech.archunit:archunit-junit4", version.ref = "archunit" }
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidx-test-runner" }
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version.ref = "androidx-test-ext-junit" }
```

If any alias differs from the version verified in Phase 0, update locks/verification metadata and record the resolution; do not introduce a second version source.

- [ ] **Step 3: Include exact project descriptors without changing logical `:api` behavior**

Use explicit descriptor mapping:

```groovy
def includeAt = { String path, String dir ->
    include path
    project(path).projectDir = file(dir)
}

includeAt(':api-contract', 'api-contract')
includeAt(':api-public', 'api-public')
includeAt(':api-contract-tests', 'api-contract-tests')
includeAt(':core:model', 'core/model')
includeAt(':core:domain', 'core/domain')
includeAt(':core:session-contract', 'core/session-contract')
includeAt(':core:network', 'core/network')
includeAt(':core:storage', 'core/storage')
includeAt(':core:data', 'core/data')
includeAt(':core:designsystem', 'core/designsystem')
includeAt(':core:testing', 'core/testing')
includeAt(':feature:library', 'feature/library')
includeAt(':feature:novel', 'feature/novel')
includeAt(':feature:reader', 'feature/reader')
includeAt(':feature:account', 'feature/account')
includeAt(':feature:settings', 'feature/settings')

if (provider == Wenku8ProviderSelection.PRIVATE) {
    includeAt(':api-private-adapter', 'api-private-adapter')
}
```

Keep Phase 0's single logical `:api`: public maps only to `api-stub/`; private maps only to protected `api/`. `:api-public` is present in both graphs.

Apply one shared, tested PowerShell resolver from the root build:

```groovy
// build.gradle
apply from: "${rootDir}/gradle/powershell.gradle"
```

```groovy
// gradle/powershell.gradle
import java.nio.file.Files
import java.util.Locale
import java.util.regex.Pattern

ext.resolvePowerShellFrom = { String osName, String pathValue ->
    boolean windows = osName.toLowerCase(Locale.ROOT).contains('windows')
    List<String> candidates = windows
        ? ['pwsh.exe', 'powershell.exe']
        : ['pwsh']
    List<File> pathDirectories = pathValue
        .split(Pattern.quote(File.pathSeparator), -1)
        .findAll { !it.isBlank() }
        .collect { String entry ->
            String unquoted = entry.trim()
            if (unquoted.length() >= 2 && unquoted.startsWith('"') && unquoted.endsWith('"')) {
                unquoted = unquoted.substring(1, unquoted.length() - 1)
            }
            new File(unquoted)
        }

    for (String candidate : candidates) {
        for (File directory : pathDirectories) {
            File executable = new File(directory, candidate)
            if (executable.isFile() && (windows || Files.isExecutable(executable.toPath()))) {
                return executable.canonicalPath
            }
        }
    }
    throw new GradleException('WENKU8-POWERSHELL-E001: PowerShell executable unavailable')
}

ext.resolvePowerShell = {
    rootProject.ext.resolvePowerShellFrom(
        System.getProperty('os.name', ''),
        System.getenv('PATH') ?: ''
    )
}
```

`PowerShellResolverTest` applies the script to a TestKit project and creates executable fixture files in separate PATH directories. It proves Linux accepts only `pwsh`; Windows selects `pwsh.exe` before `powershell.exe` even when the latter appears in an earlier PATH directory; Windows falls back to `powershell.exe`; empty/missing candidates fail with only `WENKU8-POWERSHELL-E001`; and `resolvePowerShell()` finds the real host executable used by CI. No test or error prints PATH content. Every Gradle `Exec` task in Phases 1 and 4-8 calls `rootProject.ext.resolvePowerShell()` and uses portable `-NoProfile -NonInteractive -File` arguments.

- [ ] **Step 4: Add reusable JVM and Android module build files**

Every pure module uses this shape, with only its listed dependencies:

```groovy
plugins {
    alias(libs.plugins.kotlin.jvm)
    id 'java-library'
}
java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
kotlin { jvmToolchain(17) }
tasks.withType(Test).configureEach { useJUnit() }
```

Every Android library uses the same shape. For example, `core/storage/build.gradle` uses:

```groovy
plugins { alias(libs.plugins.android.library) }
android {
    namespace 'org.mewx.wenku8.core.storage'
    compileSdkVersion rootProject.compileSdkVersion
    defaultConfig {
        minSdkVersion rootProject.minSdkVersion
        testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```

The other Android module build files use their exact namespace from the table: `org.mewx.wenku8.core.data`, `org.mewx.wenku8.core.designsystem`, `org.mewx.wenku8.core.testing`, and private-only `org.mewx.wenku8.api.privateadapter`. `:core:designsystem` also applies the Compose plugin/build feature. `:core:storage` applies KSP and Room schema export. No library declares `productFlavors`.

`api-contract-tests/build.gradle` additionally applies `java-test-fixtures` and declares `testFixturesApi project(':api-contract')` plus JUnit/coroutines-test fixture dependencies. `api-private-adapter/build.gradle` is the Android-library shape above and is evaluated only in the protected private graph because logical `:api` is an Android AAR.

- [ ] **Step 5: Verify both Gradle graphs and locks**

```powershell
.\gradlew.bat projects :api-contract:test :api-public:test :core:model:test :core:domain:test :core:session-contract:test :core:network:test :core:storage:testDebugUnitTest :core:data:testDebugUnitTest :core:designsystem:testDebugUnitTest :core:testing:testDebugUnitTest -Pwenku8Provider=public
.\gradlew.bat help -Pwenku8Provider=private
```

Expected: public graph/tasks PASS. Private-without-source FAILs redacted. In protected CI the private graph includes exactly one `:api-private-adapter` and PASSes graph verification.

- [ ] **Step 6: Commit the module skeleton**

```powershell
git add settings.gradle build.gradle gradle app\build.gradle api-contract api-public api-contract-tests api-private-adapter core feature verification-tools\src\test\kotlin\org\mewx\wenku8\verification\build\PowerShellResolverTest.kt
git diff --check
git commit -m "build: establish modernization module graph"
```

## Task 2: Define Stable Core Models and Settings Values

**Files:**

- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/identity/Keys.kt`
- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/catalog/{CatalogModels,ChapterModels}.kt`
- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/account/AccountModels.kt`
- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/community/CommunityModels.kt`
- Create: `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/{AppSettings,LanguageSelection,ReaderPreferences,RouteRolloutFlags,VersionedSettings}.kt`
- Test: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/identity/KeysTest.kt`
- Test: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/catalog/CatalogModelsTest.kt`
- Test: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/account/AccountModelsTest.kt`
- Test: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/community/CommunityModelsTest.kt`
- Test: `studio-android/LightNovelLibrary/core/model/src/test/kotlin/org/mewx/wenku8/core/model/settings/SettingsModelsTest.kt`

- [ ] **Step 1: Write invariant tests before models**

Test non-blank source/remote IDs, positive page numbers, unique bookshelf move keys/source-target mismatch, chapter block ordering, valid language pairs, reader bounds, route flags default false, and monotonic non-negative settings versions.

Run:

```powershell
.\gradlew.bat :core:model:test -Pwenku8Provider=public
```

Expected: FAIL because models are absent.

- [ ] **Step 2: Implement identity and content models as immutable values**

Use these exact identities:

```kotlin
@JvmInline value class SourceId(val value: String) { init { require(value.isNotBlank()) } }
data class NovelKey(val sourceId: SourceId, val remoteId: String) { init { require(remoteId.isNotBlank()) } }
data class ChapterKey(val novel: NovelKey, val remoteId: String) { init { require(remoteId.isNotBlank()) } }
data class ReviewKey(val novel: NovelKey, val remoteId: String) { init { require(remoteId.isNotBlank()) } }
@JvmInline value class ReviewPostKey(val value: String)
@JvmInline value class LoginAttemptId(val value: String)
@JvmInline value class BookshelfEntryKey(val value: String)
```

Implement the catalog/account/community types with these signatures (split across the focused files named above):

```kotlin
data class NovelSummary(val key: NovelKey, val title: String, val author: String?, val cover: BinaryRequest?)
data class NovelDetail(val key: NovelKey, val title: String, val author: String?, val status: String?, val tags: List<String>, val introduction: ControlledRichText, val cover: BinaryRequest?)
data class HomeSection(val id: String, val title: String, val novels: List<NovelSummary>)
data class TagGroup(val id: String, val label: String)
data class TagSummary(val id: String, val groupId: String?, val label: String, val count: Int?)
data class Volume(val remoteId: String, val title: String, val chapters: List<ChapterSummary>)
data class ChapterSummary(val key: ChapterKey, val title: String)
data class ChapterDocument(val key: ChapterKey, val title: String?, val blocks: List<ChapterBlock>)
sealed interface ChapterBlock {
    data class Text(val text: String) : ChapterBlock
    data class Image(val resource: BinaryRequest, val description: String?) : ChapterBlock
    data object SemanticBreak : ChapterBlock
}
data class ControlledRichText(val paragraphs: List<String>)
data class BinaryRequest(val canonicalHttpsUrl: String, val refererOperation: String)
data class BinaryResource(val bytes: ByteArray, val mediaType: String, val canonicalHttpsUrl: String)
data class ExternalLink(val canonicalHttpsUrl: String)
data class CaptchaChallenge(val attemptId: LoginAttemptId, val image: BinaryResource, val expiresAtEpochMillis: Long)
data class SessionState(val authenticated: Boolean, val accountId: String?, val expiresAtEpochMillis: Long?)
data class UserProfile(val accountId: String, val username: String, val nickname: String?, val score: String?, val experience: String?, val rank: String?, val avatar: BinaryRequest?)
data class BookshelfEntry(val key: BookshelfEntryKey, val novel: NovelSummary, val groupId: String)
data class BookshelfGroup(val id: String, val title: String, val items: List<BookshelfEntry>)
data class CheckInResult(val accepted: Boolean, val message: String?)
data class RecommendationResult(val accepted: Boolean, val message: String?)
data class ReviewSummary(val key: ReviewKey, val title: String, val author: String?, val replyCount: Int?)
data class ReviewPost(val key: ReviewPostKey, val author: String?, val body: ControlledRichText, val postedAt: String?)
```

They contain no Android, Cookie, DOM, ContentValues, or executable HTML type.

- [ ] **Step 3: Implement complete settings values and atomic language pairing**

```kotlin
enum class AppLocale { ZH_CN, ZH_TW, ZH_HK }
enum class ContentLanguage { SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE }
enum class AppThemeMode { SYSTEM, LIGHT, DARK }

data class LanguageSelection(val appLocale: AppLocale, val contentLanguage: ContentLanguage) {
    init {
        require(appLocale != AppLocale.ZH_CN || contentLanguage == ContentLanguage.SIMPLIFIED_CHINESE)
        require(appLocale == AppLocale.ZH_CN || contentLanguage == ContentLanguage.TRADITIONAL_CHINESE)
    }
    companion object {
        val SIMPLIFIED = LanguageSelection(AppLocale.ZH_CN, ContentLanguage.SIMPLIFIED_CHINESE)
        val TRADITIONAL = LanguageSelection(AppLocale.ZH_TW, ContentLanguage.TRADITIONAL_CHINESE)
    }
}

data class RouteRolloutFlags(
    val shellEnabled: Boolean = false,
    val discoverEnabled: Boolean = false,
    val bookshelfEnabled: Boolean = false,
    val searchEnabled: Boolean = false,
    val settingsEnabled: Boolean = false,
)
enum class RouteFlag { SHELL_ENABLED, DISCOVER_ENABLED, BOOKSHELF_ENABLED, SEARCH_ENABLED, SETTINGS_ENABLED }
fun RouteRolloutFlags.with(flag: RouteFlag, enabled: Boolean): RouteRolloutFlags = when (flag) {
    RouteFlag.SHELL_ENABLED -> copy(shellEnabled = enabled)
    RouteFlag.DISCOVER_ENABLED -> copy(discoverEnabled = enabled)
    RouteFlag.BOOKSHELF_ENABLED -> copy(bookshelfEnabled = enabled)
    RouteFlag.SEARCH_ENABLED -> copy(searchEnabled = enabled)
    RouteFlag.SETTINGS_ENABLED -> copy(settingsEnabled = enabled)
}

data class ReaderPreferences(
    val fontSizeSp: Int = 20,
    val lineHeightSp: Int = 32,
    val paragraphSpacingSp: Int = 18,
    val pageMarginDp: Int = 16,
    val customFontPath: String? = null,
    val customBackgroundPath: String? = null,
    val nightMode: Boolean = false,
    val einkMode: Boolean = false,
) {
    fun normalized(): ReaderPreferences {
        val font = fontSizeSp.coerceIn(16, 30)
        return copy(
            fontSizeSp = font,
            lineHeightSp = lineHeightSp.coerceIn(24, 52).coerceAtLeast(font + 8),
            paragraphSpacingSp = paragraphSpacingSp.coerceIn(8, 36),
            pageMarginDp = pageMarginDp.coerceIn(8, 48),
        )
    }
}
data class AppSettings(
    val language: LanguageSelection = LanguageSelection.SIMPLIFIED,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val menuBackgroundId: String = "0",
    val menuBackgroundPath: String? = null,
    val reader: ReaderPreferences = ReaderPreferences(),
    val routeFlags: RouteRolloutFlags = RouteRolloutFlags(),
    val unknownLegacyValues: Map<String, String> = emptyMap(),
)
data class VersionedSettings(val value: AppSettings, val mutationVersion: Long, val legacyProjectionVersion: Long) {
    init { require(mutationVersion >= 0); require(legacyProjectionVersion in 0..mutationVersion) }
}
```

`ReaderPreferences` owns existing font size/path, line height, paragraph spacing, page margins, background path, modern night mode and e-ink fields with explicit bounds. `AppSettings` owns language, app theme, dynamic-color opt-in, menu background ID/path, reader values, route flags, and sorted `unknownLegacyValues`. `VersionedSettings` contains `value`, `mutationVersion`, and `legacyProjectionVersion`; require `0 <= legacyProjectionVersion <= mutationVersion`.

- [ ] **Step 4: Verify standard-library-only bytecode and commit**

```powershell
.\gradlew.bat :core:model:test :core:model:dependencies -Pwenku8Provider=public
```

Expected: PASS; runtime dependency report contains Kotlin standard library only and no Android/Compose/coroutines/API module.

```powershell
git add core\model
git diff --check
git commit -m "feat: define immutable core models"
```

## Task 3: Implement Provider Result, Capability, Request, and Source Contracts

**Files:**

- Create: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/{ApiResult,ApiFailure,OperationCode,ProviderCapability,Requests,Wenku8Sources,LoginRequest,ProviderBinding}.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderIdentity.kt`
- Create: `studio-android/LightNovelLibrary/api-contract-tests/src/testFixtures/kotlin/org/mewx/wenku8/api/contract/testing/ProviderContractSuite.kt`
- Test: `studio-android/LightNovelLibrary/api-contract/src/test/kotlin/org/mewx/wenku8/api/contract/LoginRequestTest.kt`
- Test: `studio-android/LightNovelLibrary/api-contract/src/test/kotlin/org/mewx/wenku8/api/contract/ContractInvariantTest.kt`

- [ ] **Step 1: Write RED tests for capability/failure/secret contracts**

Test that every capability-gated operation maps to one `ProviderCapability`; missing capability returns `ApiFailure.Unsupported(theCapability)` with zero request counter; enabled operations may not return Unsupported; source facets expose one immutable provider ID/enabled set/input policy. Test `ApiFailure.Parse` accepts only the closed `OperationCode` enum, exposes the property as `operationCode`, and cannot be constructed from a URL, hostname, private endpoint label, or arbitrary operation string. Test caller arrays are zeroed after `LoginRequest` success, typed failure, exception, timeout, cancellation, double consume, and close.

Add this contract test before implementation:

```kotlin
@Test fun parseFailureAcceptsOnlyClosedOperationCodes() {
    val failure = ApiFailure.Parse(OperationCode.NOVEL_DETAIL, contractRevision = 4)
    assertEquals(OperationCode.NOVEL_DETAIL, failure.operationCode)
    assertNull(OperationCode.fromWireId("https://private.invalid/secret"))
    assertNull(OperationCode.fromWireId("private-endpoint"))
    assertFalse(failure.toString().contains("http", ignoreCase = true))
    assertFalse(failure.toString().contains("private", ignoreCase = true))
}
```

Run:

```powershell
.\gradlew.bat :api-contract:test :api-contract-tests:test -Pwenku8Provider=public
```

Expected: FAIL because contracts are absent.

- [ ] **Step 2: Implement result, failure, capability, and request types**

Use the approved result/failure shapes, including cancellation propagation:

```kotlin
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T, val metadata: ResponseMetadata) : ApiResult<T>
    data class Failure(val error: ApiFailure, val traceId: String) : ApiResult<Nothing>
}

data class ResponseMetadata(
    val sourceId: String,
    val fetchedAtEpochMillis: Long,
    val freshness: Freshness,
)
enum class Freshness { FRESH, STALE }

enum class OperationCode(val wireId: String) {
    HOME("home"),
    BROWSE_LATEST("browse-latest"),
    BROWSE_COMPLETED("browse-completed"),
    BROWSE_CATEGORY("browse-category"),
    BROWSE_RANKING("browse-ranking"),
    TAG_GROUPS("tag-groups"),
    TAGS("tags"),
    NOVELS_BY_TAG("novels-by-tag"),
    SEARCH("search"),
    NOVEL_DETAIL("novel-detail"),
    CATALOG("catalog"),
    CHAPTER("chapter"),
    BINARY("binary"),
    REGISTRATION("registration"),
    LOGIN_PREWARM_ROOT("login-prewarm-root"),
    LOGIN_PREWARM_FORM("login-prewarm-form"),
    CAPTCHA("captcha"),
    LOGIN_SUBMIT("login-submit"),
    VALIDATE_SESSION("validate-session"),
    PROFILE("profile"),
    AVATAR("avatar"),
    BOOKSHELF_READ("bookshelf-read"),
    BOOKSHELF_ADD("bookshelf-add"),
    BOOKSHELF_REMOVE("bookshelf-remove"),
    BOOKSHELF_MOVE("bookshelf-move"),
    RECOMMEND("recommend"),
    REVIEWS("reviews"),
    REVIEW_THREAD("review-thread"),
    REVIEW_CREATE("review-create"),
    REVIEW_REPLY("review-reply"),
    LOGOUT("logout"),
    DAILY_CHECK_IN("daily-check-in");

    companion object {
        private val byWireId = entries.associateBy(OperationCode::wireId)
        fun fromWireId(value: String): OperationCode? = byWireId[value]
    }
}

sealed interface ApiFailure {
    sealed interface Network : ApiFailure {
        data object Offline : Network; data object Dns : Network; data object Connect : Network
        data object Tls : Network; data object Timeout : Network
    }
    data class Http(val status: Int, val retryAfterSeconds: Long?) : ApiFailure
    data object ChallengeBlocked : ApiFailure
    sealed interface Auth : ApiFailure {
        data object CaptchaRequired : Auth; data object InvalidCaptcha : Auth
        data object InvalidCredentials : Auth; data object SessionExpired : Auth
    }
    data class Decode(val charset: String) : ApiFailure
    data class Parse(val operationCode: OperationCode, val contractRevision: Int) : ApiFailure
    data object NotFound : ApiFailure
    data class RateLimited(val retryAfterSeconds: Long?) : ApiFailure
    data class Storage(val operation: String) : ApiFailure
    data class Unsupported(val capability: ProviderCapability) : ApiFailure
    data class ProtocolViolation(val rule: String) : ApiFailure
}
```

Add all twelve capabilities and the complete request surface:

```kotlin
enum class ProviderCapability { ANONYMOUS_CATALOG, BINARY_DOWNLOAD, REGISTRATION_LINK, CAPTCHA_LOGIN, PROFILE, BOOKSHELF_READ, BOOKSHELF_MUTATE, DAILY_CHECK_IN, RECOMMEND_NOVEL, REVIEWS_READ, REVIEWS_CREATE, REVIEWS_REPLY }
data class ProviderCapabilities(val providerId: SourceId, val enabled: Set<ProviderCapability>, val inputPolicy: ProviderInputPolicy)
data class ProviderInputPolicy(val searchMaxEncodedBytes: Int, val reviewTitleMaxCodePoints: Int, val reviewBodyMaxCodePoints: Int, val replyMaxCodePoints: Int)
enum class SearchScope { TITLE, AUTHOR }
enum class BrowseKind { LATEST, COMPLETED, CATEGORY, RANKING }
enum class RankingPeriod { ALL_TIME, MONTH, WEEK, DAY }
data class Page<T>(val items: List<T>, val currentPage: Int, val nextPage: Int?) { init { require(currentPage > 0); require(nextPage == null || nextPage > 0) } }
data class BrowseRequest(val kind: BrowseKind, val page: Int, val language: ContentLanguage, val categoryId: String? = null, val rankingPeriod: RankingPeriod? = null) { init { require(page > 0) } }
data class TagDiscoveryRequest(val groupId: String?, val language: ContentLanguage)
data class TagBrowseRequest(val tagId: String, val page: Int, val language: ContentLanguage) { init { require(tagId.isNotBlank() && page > 0) } }
data class SearchQuery(val text: String, val scope: SearchScope, val page: Int, val language: ContentLanguage) { init { require(page > 0) } }
sealed interface BookshelfCommand {
    data class Add(val novel: NovelKey, val targetGroupId: String?) : BookshelfCommand
    data class Remove(val entryKey: BookshelfEntryKey, val sourceGroupId: String) : BookshelfCommand
    data class Move(val entryKeys: List<BookshelfEntryKey>, val sourceGroupId: String, val targetGroupId: String) : BookshelfCommand {
        init { require(entryKeys.isNotEmpty() && entryKeys.distinct().size == entryKeys.size); require(sourceGroupId != targetGroupId) }
    }
}
data class CreateReviewCommand(val novel: NovelKey, val title: String, val body: String)
data class ReplyCommand(val review: ReviewKey, val body: String)
```

- [ ] **Step 3: Implement single-use secret ownership**

Use the exact `LoginRequest` semantics from the spec; do not copy input arrays:

```kotlin
class LoginRequest(
    val attemptId: LoginAttemptId,
    val username: String,
    password: CharArray,
    captcha: CharArray,
) : AutoCloseable {
    private val consumed = java.util.concurrent.atomic.AtomicBoolean(false)
    private val ownedPassword = password
    private val ownedCaptcha = captcha

    suspend fun <T> consumeSecrets(block: suspend (CharArray, CharArray) -> T): T {
        check(consumed.compareAndSet(false, true))
        return try { block(ownedPassword, ownedCaptcha) } finally { close() }
    }
    override fun close() {
        consumed.set(true)
        ownedPassword.fill('\u0000')
        ownedCaptcha.fill('\u0000')
    }
}
```

Do not add `toString`, copy, component, Parcelable, Serializable, SavedState, or WorkManager support.

- [ ] **Step 4: Define all four source facets and binding**

Create the source facets with these exact methods:

```kotlin
interface Wenku8CatalogSource {
    fun capabilities(): ProviderCapabilities
    suspend fun home(): ApiResult<List<HomeSection>>
    suspend fun browse(request: BrowseRequest): ApiResult<Page<NovelSummary>>
    suspend fun tagGroups(language: ContentLanguage): ApiResult<List<TagGroup>>
    suspend fun tags(request: TagDiscoveryRequest): ApiResult<List<TagSummary>>
    suspend fun novelsByTag(request: TagBrowseRequest): ApiResult<Page<NovelSummary>>
    suspend fun search(query: SearchQuery): ApiResult<Page<NovelSummary>>
    suspend fun novel(key: NovelKey): ApiResult<NovelDetail>
    suspend fun catalog(key: NovelKey): ApiResult<List<Volume>>
    suspend fun chapter(key: ChapterKey): ApiResult<ChapterDocument>
}
interface Wenku8BinarySource {
    fun capabilities(): ProviderCapabilities
    suspend fun fetch(request: BinaryRequest): ApiResult<BinaryResource>
}
interface Wenku8AccountSource {
    fun capabilities(): ProviderCapabilities
    suspend fun registrationPage(): ApiResult<ExternalLink>
    suspend fun beginLogin(): ApiResult<CaptchaChallenge>
    suspend fun login(request: LoginRequest): ApiResult<SessionState>
    suspend fun validateSession(): ApiResult<SessionState>
    suspend fun profile(): ApiResult<UserProfile>
    suspend fun avatar(): ApiResult<BinaryResource>
    suspend fun dailyCheckIn(): ApiResult<CheckInResult>
    suspend fun bookshelf(): ApiResult<List<BookshelfGroup>>
    suspend fun updateBookshelf(command: BookshelfCommand): ApiResult<Unit>
    suspend fun recommendNovel(key: NovelKey): ApiResult<RecommendationResult>
    suspend fun logout(): ApiResult<Unit>
}
interface Wenku8CommunitySource {
    fun capabilities(): ProviderCapabilities
    suspend fun reviews(key: NovelKey, page: Int): ApiResult<Page<ReviewSummary>>
    suspend fun reviewThread(key: ReviewKey, page: Int): ApiResult<Page<ReviewPost>>
    suspend fun createReview(command: CreateReviewCommand): ApiResult<ReviewKey>
    suspend fun reply(command: ReplyCommand): ApiResult<ReviewPostKey>
}
```

Add:

```kotlin
interface ProviderBinding {
    val providerId: SourceId
    val catalog: Wenku8CatalogSource
    val binary: Wenku8BinarySource
    val account: Wenku8AccountSource
    val community: Wenku8CommunitySource
}
```

`PublicProviderIdentity` contains only `val ID = SourceId("public")`. Do not create a transport/source implementation in `:api-public`.

- [ ] **Step 5: Publish a reusable capability contract fixture and verify**

`ProviderContractSuite` takes a `ProviderBinding` and `RequestCounter`, calls every absent-capability operation, asserts the exact Unsupported capability, and asserts the counter remains zero. It also asserts immutable identical capabilities across facets. Phase 2 reuses this without changing its semantics.

```powershell
.\gradlew.bat :api-contract:test :api-contract-tests:test :api-public:test -Pwenku8Provider=public
```

Expected: PASS; `rg -n "okhttp|jsoup|java.net|android\." api-contract api-public` returns no production match.

- [ ] **Step 6: Commit provider contracts**

```powershell
git add api-contract api-public api-contract-tests
git diff --check
git commit -m "feat: define typed provider contracts"
```

## Task 4: Define Domain, Runtime, Session, Host-Policy, and Cache Contracts

**Files:**

- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/catalog/CatalogRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/account/AccountRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/community/CommunityRepository.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/settings/{SettingsRepository,RouteFlagRepository,SettingsMutation,SettingsWriteResult}.kt`
- Create: `studio-android/LightNovelLibrary/core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/runtime/{AppDispatchers,AppClock}.kt`
- Create: `core/session-contract/src/main/kotlin/org/mewx/wenku8/core/session/{SessionRecord,SessionStore}.kt`
- Create: `core/network/src/main/kotlin/org/mewx/wenku8/core/network/policy/{HostPolicy,DenyAllHostPolicy}.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/cache/{DomainCache,ReadSingleFlight}.kt`
- Test: `studio-android/LightNovelLibrary/core/domain/src/test/kotlin/org/mewx/wenku8/core/domain/DomainBoundaryTest.kt`
- Test: `studio-android/LightNovelLibrary/core/session-contract/src/test/kotlin/org/mewx/wenku8/core/session/SessionStoreContractTest.kt`
- Test: `studio-android/LightNovelLibrary/core/network/src/test/kotlin/org/mewx/wenku8/core/network/policy/HostPolicyContractTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/cache/ReadSingleFlightContractTest.kt`

- [ ] **Step 1: Write RED contract tests for deterministic runtime and redaction**

Test fake dispatchers/clock, session Cookie redacted `toString`, signed-out store epoch monotonicity, HostPolicy denial before any request counter increment, typed cache keys/entries, single-flight cancellation propagation, settings state immutability, and no implementation dependency in repository interfaces.

Run:

```powershell
.\gradlew.bat :core:domain:test :core:session-contract:test :core:network:test :core:data:testDebugUnitTest -Pwenku8Provider=public
```

Expected: FAIL because contracts are absent.

- [ ] **Step 2: Define injected runtime and settings repository contracts**

```kotlin
data class AppDispatchers(
    val main: kotlinx.coroutines.CoroutineDispatcher,
    val io: kotlinx.coroutines.CoroutineDispatcher,
    val default: kotlinx.coroutines.CoroutineDispatcher,
)
fun interface AppClock { fun nowEpochMillis(): Long }

interface SettingsRepository {
    val settings: kotlinx.coroutines.flow.StateFlow<VersionedSettings>
    val readiness: kotlinx.coroutines.flow.StateFlow<SettingsReadiness>
    suspend fun update(mutation: SettingsMutation): SettingsWriteResult
    suspend fun reconcile(): SettingsReconciliationResult
}

interface RouteFlagRepository {
    val flags: kotlinx.coroutines.flow.Flow<RouteRolloutFlags>
    suspend fun set(flag: RouteFlag, enabled: Boolean): SettingsWriteResult
}

enum class SettingsReadiness { STARTING, READY, FAILED_LEGACY_READABLE }
sealed interface SettingsWriteResult {
    data class Synchronized(val version: Long) : SettingsWriteResult
    data class PendingSynchronization(val version: Long, val mutationId: Long) : SettingsWriteResult
    data class Failure(val operation: String) : SettingsWriteResult
}
sealed interface SettingsReconciliationResult {
    data object Clean : SettingsReconciliationResult
    data class Repaired(val count: Int) : SettingsReconciliationResult
    data class Pending(val count: Int) : SettingsReconciliationResult
    data class Failure(val operation: String) : SettingsReconciliationResult
}
sealed interface SettingsMutation {
    data class Language(val value: LanguageSelection) : SettingsMutation
    data class Theme(val value: AppThemeMode) : SettingsMutation
    data class DynamicColor(val enabled: Boolean) : SettingsMutation
    data class MenuBackground(val id: String, val path: String?) : SettingsMutation
    data class ReaderFontSize(val sp: Int) : SettingsMutation
    data class ReaderFontPath(val path: String?) : SettingsMutation
    data class ReaderLineHeight(val sp: Int) : SettingsMutation
    data class ReaderParagraphSpacing(val sp: Int) : SettingsMutation
    data class ReaderPageMargin(val dp: Int) : SettingsMutation
    data class ReaderBackgroundPath(val path: String?) : SettingsMutation
    data class ReaderNightMode(val enabled: Boolean) : SettingsMutation
    data class EinkMode(val enabled: Boolean) : SettingsMutation
    data class RouteEnabled(val flag: RouteFlag, val enabled: Boolean) : SettingsMutation
}

fun SettingsMutation.applyTo(settings: AppSettings): AppSettings = when (this) {
    is SettingsMutation.Language -> settings.copy(language = value)
    is SettingsMutation.Theme -> settings.copy(themeMode = value)
    is SettingsMutation.DynamicColor -> settings.copy(dynamicColor = enabled)
    is SettingsMutation.MenuBackground -> settings.copy(menuBackgroundId = id, menuBackgroundPath = path)
    is SettingsMutation.ReaderFontSize -> settings.copy(reader = settings.reader.copy(fontSizeSp = sp).normalized())
    is SettingsMutation.ReaderFontPath -> settings.copy(reader = settings.reader.copy(customFontPath = path))
    is SettingsMutation.ReaderLineHeight -> settings.copy(reader = settings.reader.copy(lineHeightSp = sp).normalized())
    is SettingsMutation.ReaderParagraphSpacing -> settings.copy(reader = settings.reader.copy(paragraphSpacingSp = sp).normalized())
    is SettingsMutation.ReaderPageMargin -> settings.copy(reader = settings.reader.copy(pageMarginDp = dp).normalized())
    is SettingsMutation.ReaderBackgroundPath -> settings.copy(reader = settings.reader.copy(customBackgroundPath = path))
    is SettingsMutation.ReaderNightMode -> settings.copy(reader = settings.reader.copy(nightMode = enabled))
    is SettingsMutation.EinkMode -> settings.copy(reader = settings.reader.copy(einkMode = enabled))
    is SettingsMutation.RouteEnabled -> settings.copy(routeFlags = settings.routeFlags.with(flag, enabled))
}
```

There is no arbitrary key/value mutation. Repository interfaces for catalog/account/community expose domain values/flows and do not mention transport, DOM, Room, DataStore, Android, or legacy ABI.

- [ ] **Step 3: Define a redacted platform-neutral SessionStore**

```kotlin
class SessionCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean,
    val persistent: Boolean,
    val expiresAtEpochMillis: Long?,
) {
    override fun toString(): String = "SessionCookie(name=<redacted>, domain=$domain, path=$path)"
}

data class SessionRecord(
    val providerId: SourceId,
    val accountId: String,
    val epoch: Long,
    val cookies: List<SessionCookie>,
    val createdAtEpochMillis: Long,
    val expiresAtEpochMillis: Long?,
)

interface SessionStore {
    suspend fun load(providerId: SourceId): SessionRecord?
    suspend fun replace(record: SessionRecord)
    suspend fun purge(providerId: SourceId)
    suspend fun incrementEpoch(providerId: SourceId): Long
}
```

Phase 1 supplies only `SignedOutSessionStore`: `load` returns null, `incrementEpoch` is monotonic in memory, `purge` clears it, and `replace` throws `IllegalStateException("session persistence disabled in phase one")` before retaining any value. It never persists a Cookie; Phase 2 owns encryption.

- [ ] **Step 4: Define the pure HostPolicy decision boundary**

```kotlin
enum class NetworkMethod { GET, HEAD, POST }
enum class SensitiveHeader { COOKIE, AUTHORIZATION, REFERER, OTHER_CREDENTIAL }
data class NetworkOrigin(val scheme: String, val host: String, val port: Int)
data class NetworkTarget(val origin: NetworkOrigin, val encodedPath: String, val hasUserInfo: Boolean, val literalIp: Boolean)
data class OutboundRequest(
    val operationCode: String,
    val target: NetworkTarget,
    val method: NetworkMethod,
    val sourceOrigin: NetworkOrigin?,
    val authenticated: Boolean,
    val redirectHop: Int,
    val sensitiveHeaders: Set<SensitiveHeader>,
)
sealed interface HostDecision {
    data class Allow(val canonicalTarget: NetworkTarget, val stripHeaders: Set<SensitiveHeader>) : HostDecision
    data class Deny(val rule: String) : HostDecision
}
fun interface HostPolicy { fun evaluate(request: OutboundRequest): HostDecision }
```

`DenyAllHostPolicy` always returns `Deny("phase-1-network-disabled")`; it constructs no network client.

- [ ] **Step 5: Define typed cache and single-flight boundaries without implementations**

```kotlin
data class CacheEntry<V : Any>(
    val value: V,
    val fetchedAtEpochMillis: Long,
    val parserRevision: Int,
    val accountId: String?,
    val sessionEpoch: Long?,
)
interface DomainCache<K : Any, V : Any> {
    suspend fun read(key: K): CacheEntry<V>?
    suspend fun write(key: K, entry: CacheEntry<V>)
    suspend fun remove(key: K)
}
interface ReadSingleFlight {
    suspend fun <K : Any, V : Any> run(key: K, block: suspend () -> V): V
}
```

Only fakes exist in Phase 1. Phase 2/3 implement provider/repository/storage behavior.

- [ ] **Step 6: Verify platform boundaries and commit**

```powershell
.\gradlew.bat :core:domain:test :core:session-contract:test :core:network:test :core:data:testDebugUnitTest -Pwenku8Provider=public
rg -n "android\.|androidx\.|okhttp|jsoup|ContentValues|Wenku8API|GlobalConfig" core\domain core\session-contract core\network -g '*.kt'
```

Expected: tests PASS and search exits 1.

```powershell
git add core\domain core\session-contract core\network core\data
git diff --check
git commit -m "feat: define runtime and repository boundaries"
```

## Task 5: Define the AppContainer Boundary and Deterministic Test Runtime

**Files:**

- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/{AppContainer,AppContainerOwner,DisabledProviderBinding,RouteViewModelFactory}.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/ProviderBindingFactory.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/testing/src/main/java/org/mewx/wenku8/core/testing/{FakeAppClock,TestAppDispatchers,MainDispatcherRule}.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/di/{AppContainerContractTest,DisabledProviderBindingTest,RouteViewModelFactoryTest}.kt`

- [ ] **Step 1: Write RED tests for constructor-only access and disabled provider behavior**

Reflect production composables/ViewModels and reject a constructor/global read of `MyApp`, `AppContainerOwner`, `GlobalConfig`, or a concrete repository. Run the reusable provider contract fixture against `DisabledProviderBinding`; every operation must return the matching `Unsupported(capability)` and zero network activity. Verify fake clock/dispatchers make state tests deterministic.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.di.*" :core:testing:testDebugUnitTest -Pwenku8Provider=public
```

Expected: FAIL because boundaries/fakes are absent.

- [ ] **Step 2: Define the exact container and owner interfaces**

```kotlin
interface AppContainer {
    val dispatchers: AppDispatchers
    val clock: AppClock
    val settingsRepository: SettingsRepository
    val routeFlags: RouteFlagRepository
    val sessionStore: SessionStore
    val providerBinding: ProviderBinding
}

interface AppContainerOwner {
    val appContainer: AppContainer
}

fun android.content.Context.appContainer(): AppContainer =
    (applicationContext as AppContainerOwner).appContainer
```

The extension is for Activity/route factory composition only. Composables and ViewModels may not call it.

- [ ] **Step 3: Add a capability-disabled, zero-egress binding**

Create one immutable capabilities value using the selected provider ID, empty enabled set, and conservative positive input limits. Implement each facet by returning:

```kotlin
private fun unsupported(capability: ProviderCapability): ApiResult.Failure =
    ApiResult.Failure(ApiFailure.Unsupported(capability), traceId = "phase1-disabled-provider")
```

Every method maps to the exhaustive capability table in Section 8.2; no method throws `UnsupportedOperationException`, and no file in `DisabledProviderBinding.kt` imports a network package.

Add `buildConfigField 'String', 'WENKU8_PROVIDER_ID', '"public"'` or `"private"` from the already validated `wenku8Provider` Gradle property. `ProviderBindingFactory.create(id)` accepts only those two IDs and returns `DisabledProviderBinding(PublicProviderIdentity.ID)` or `DisabledProviderBinding(SourceId("private"))`. Tests assert public/private debug and minified-release BuildConfig identity; the binding remains capability-disabled until Phase 2 supplies the selected implementation.

- [ ] **Step 4: Add route factories and test clocks/dispatchers**

```kotlin
class RouteViewModelFactory<VM : androidx.lifecycle.ViewModel>(
    private val create: (androidx.lifecycle.viewmodel.CreationExtras) -> VM,
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(
        modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras,
    ): T = modelClass.cast(create(extras))
}
```

`FakeAppClock` advances only when a test calls `advanceBy`; `TestAppDispatchers` uses one `StandardTestDispatcher` for main/io/default unless a test supplies separate schedulers. `MainDispatcherRule` installs/resets Dispatchers.Main in `starting/finished`.

- [ ] **Step 5: Verify and commit the composition boundary**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.di.*" :core:testing:testDebugUnitTest -Pwenku8Provider=public
```

Expected: PASS; disabled provider shared contract reports zero dispatched requests.

```powershell
git add app\src\main\java\org\mewx\wenku8\di app\src\test\java\org\mewx\wenku8\di core\testing
git diff --check
git commit -m "feat: define explicit application container"
```

## Task 6: Build the Compose Material 3 Design System

**Files:**

- Create: `studio-android/LightNovelLibrary/core/designsystem/src/main/java/org/mewx/wenku8/core/designsystem/theme/{Color,Type,Shape,Spacing,Wenku8Theme}.kt`
- Create: `studio-android/LightNovelLibrary/core/designsystem/src/main/java/org/mewx/wenku8/core/designsystem/component/{Wenku8Scaffold,StateContent}.kt`
- Create: `studio-android/LightNovelLibrary/core/designsystem/src/main/res/values/strings.xml`
- Create: `studio-android/LightNovelLibrary/core/designsystem/src/main/res/values-zh-rTW/strings.xml`
- Create: `studio-android/LightNovelLibrary/core/designsystem/src/main/res/values-zh-rHK/strings.xml`
- Test: `studio-android/LightNovelLibrary/core/designsystem/src/androidTest/java/org/mewx/wenku8/core/designsystem/{Wenku8ThemeTest,StateContentTest}.kt`

- [ ] **Step 1: Write RED theme, semantics, and sizing tests**

Assert complete light/dark schemes, optional dynamic color only on API 31+, independent semantic error/warning/success roles, no hard-coded page colors, typography at Material roles with letter spacing `0.sp`, shape maximum 8dp for cards, central safe-drawing/IME insets, at least 48dp icon controls, and localized loading/empty/error/retry semantics.

Run:

```powershell
.\gradlew.bat :core:designsystem:connectedDebugAndroidTest -Pwenku8Provider=public
```

Expected: FAIL because design system is absent.

- [ ] **Step 2: Define restrained multi-role light and dark schemes**

Use these stable fallback colors; do not add gradients/orbs/card piles:

```kotlin
internal val LightScheme = lightColorScheme(
    primary = Color(0xFF35644F), onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F0D2), onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF52635A), onSecondary = Color.White,
    tertiary = Color(0xFF755A2C), onTertiary = Color.White,
    error = Color(0xFFBA1A1A), onError = Color.White,
    background = Color(0xFFF8FAF7), onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF8FAF7), onSurface = Color(0xFF191C1A),
)
internal val DarkScheme = darkColorScheme(
    primary = Color(0xFF9CD4B7), onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF17513A), onPrimaryContainer = Color(0xFFB8F0D2),
    secondary = Color(0xFFB9CCC0), onSecondary = Color(0xFF24352D),
    tertiary = Color(0xFFE5C18D), onTertiary = Color(0xFF402D04),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    background = Color(0xFF101411), onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF101411), onSurface = Color(0xFFE1E3DF),
)
```

- [ ] **Step 3: Implement theme, typography, shapes, and spacing tokens**

`Wenku8Theme(themeMode, dynamicColor, content)` selects dynamic light/dark only when requested and supported, otherwise the fallback scheme. `Wenku8Typography` starts from Material 3 defaults and sets every explicit `letterSpacing=0.sp`; do not scale fonts with viewport width. Define 4/8/12/16/24/32dp spacing, 16dp compact gutter, 24dp expanded gutter, 48dp minimum touch target, and 4/8dp shape scale.

- [ ] **Step 4: Implement unframed shared state components**

`Wenku8Scaffold` delegates to Material 3 `Scaffold` with `WindowInsets.safeDrawing` and a real `SnackbarHost`. `StateContent` exposes focused composables for `InitialLoading`, `Empty`, `RecoverableError`, `Offline` and `AuthRequired`; they take resource-backed title/action and callbacks. Do not nest cards or render a fake Text-as-button.

- [ ] **Step 5: Verify light/dark/dynamic and accessibility, then commit**

```powershell
.\gradlew.bat :core:designsystem:testDebugUnitTest :core:designsystem:connectedDebugAndroidTest -Pwenku8Provider=public
```

Expected: PASS at font scale 1.0/2.0, light/dark, Simplified/Traditional/Hong Kong resources; no clipped action or missing semantics.

```powershell
git add core\designsystem
git diff --check
git commit -m "feat: add Material 3 design system"
```

## Task 7: Build the Adaptive Navigation Compose Shell as Pure UI

**Files:**

- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/{AppDestination,AdaptiveLayoutInfo,ShellUiState,ShellEffect,ShellViewModel,LegacyDestinationLauncher,Wenku8App,Wenku8NavHost}.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/res/values/navigation_strings.xml`
- Create: `studio-android/LightNovelLibrary/app/src/main/res/values-zh-rTW/navigation_strings.xml`
- Create: `studio-android/LightNovelLibrary/app/src/main/res/values-zh-rHK/navigation_strings.xml`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/navigation/AdaptiveLayoutInfoTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/Wenku8AppTest.kt`
- Create: `studio-android/LightNovelLibrary/core/testing/src/main/java/org/mewx/wenku8/core/testing/screenshot/{UiGoldenCapture,UiGoldenCaseMetadata}.kt`
- Create: `studio-android/LightNovelLibrary/tools/verification/run-androidx-ui-goldens.ps1`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/ui/{UiGoldenManifestVerifier,UiGoldenApprover}.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/{build.gradle,src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt}`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `docs/verification/ui-golden-manifest.yaml`

- [ ] **Step 1: Write RED adaptive mapping and UI tests**

Test 599/600/839/840dp width boundaries; compact-height `<480dp`; bar/rail/expanded rail selection; separating and occluding hinge hard boundaries; start-pane-before-detail traversal; Sheet-to-pane exclusivity; destination/selection preservation on resize/process recreation; bounded one-time effects; focus/back semantics. UI tests assert Material `NavigationBar`, `NavigationRail` or expanded navigation, never two navigation modes at once.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.Wenku8AppTest -Pwenku8Provider=public
```

Expected: FAIL because shell code is absent.

- [ ] **Step 2: Define typed destinations and adaptive values**

```kotlin
enum class AppDestination(val route: String, @StringRes val label: Int) {
    DISCOVER("discover", R.string.nav_discover),
    BOOKSHELF("bookshelf", R.string.nav_bookshelf),
    SEARCH("search", R.string.nav_search),
    SETTINGS("settings", R.string.nav_settings),
}

enum class NavigationPresentation { BOTTOM_BAR, RAIL, EXPANDED_RAIL }
data class HingeBoundary(val bounds: android.graphics.Rect, val occluding: Boolean)
data class AdaptiveLayoutInfo(
    val presentation: NavigationPresentation,
    val compactHeight: Boolean,
    val hinge: HingeBoundary?,
    val contentRegions: List<android.graphics.Rect>,
)
```

Keep `AdaptiveLayoutInfo` calculation in a pure mapper that accepts dp width/height and plain hinge facts. AndroidX `WindowAdaptiveInfo`/`FoldingFeature` conversion stays in the eventual Activity adapter.

- [ ] **Step 3: Implement one Material 3 navigation suite and NavHost**

`Wenku8App` takes immutable `ShellUiState`, `AdaptiveLayoutInfo`, and callbacks. Use `NavigationSuiteScaffold`/Material 3 icons with localized labels; compact height uses rail when width allows. Use `NavHost` with the four typed routes. Phase 1 route content calls a supplied `LegacyDestinationLauncher` through a one-shot effect and shows only a bounded progress state while the compatibility Activity owns the screen; tests provide a recording launcher, so no Activity starts during screenshot/semantics assertions.

```kotlin
fun interface LegacyDestinationLauncher {
    fun launch(destination: AppDestination)
}

data class ShellUiState(
    val selected: AppDestination = AppDestination.DISCOVER,
    val launchingLegacy: Boolean = false,
)
```

State/effects are ViewModel-owned:

```kotlin
sealed interface ShellEffect { data class LaunchLegacy(val destination: AppDestination) : ShellEffect }

class ShellViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val initial = savedStateHandle.get<String>("destination")
        ?.let { route -> AppDestination.entries.singleOrNull { it.route == route } }
        ?: AppDestination.DISCOVER
    private val mutableState = MutableStateFlow(ShellUiState(selected = initial))
    val uiState: StateFlow<ShellUiState> = mutableState.asStateFlow()
    private val mutableEffects = Channel<ShellEffect>(capacity = 8)
    val effects: Flow<ShellEffect> = mutableEffects.receiveAsFlow()

    fun select(destination: AppDestination) {
        savedStateHandle["destination"] = destination.route
        mutableState.value = ShellUiState(selected = destination, launchingLegacy = true)
        viewModelScope.launch { mutableEffects.send(ShellEffect.LaunchLegacy(destination)) }
    }
    fun legacyLaunchReturned() { mutableState.update { it.copy(launchingLegacy = false) } }
}
```

The Activity collects effects with lifecycle and calls `LegacyDestinationLauncher`; the composable receives only state/callbacks. No composable imports the container, repository, Intent, storage, network, parser, file, executor, or `GlobalConfig`.

- [ ] **Step 4: Add Phase 1 golden cases to the authoritative manifest**

Add stable IDs for 360x640 compact, 700x900 medium, 1280x800 expanded, 915x412 compact-height, separating hinge, occluding hinge, light/dark, zh-CN/zh-TW/zh-HK fallback, and font scale 2.0. Each row includes fixture/baseline hash and zero dynamic masks except status/navigation bars.

Register and dispatch the sole golden pipeline before any screenshot command:

```groovy
// verification-tools/build.gradle
tasks.register('verifyUiGoldenManifest', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyUiGoldenManifest', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath,
        providers.gradleProperty('phase').orElse('all').get()
}
tasks.register('approveUiGoldens', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    doFirst {
        if (!providers.gradleProperty('phase').isPresent() ||
            !providers.gradleProperty('uiGoldenReviewer').isPresent() ||
            !providers.gradleProperty('uiGoldenSourceCommit').isPresent()) {
            throw new GradleException('phase, uiGoldenReviewer, and uiGoldenSourceCommit are required')
        }
    }
    args 'approveUiGoldens', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath,
        providers.gradleProperty('phase').orNull,
        providers.gradleProperty('uiGoldenReviewer').orNull,
        providers.gradleProperty('uiGoldenSourceCommit').orNull
}

// app/build.gradle
def registerUiGoldenTask = { String taskName, String mode, int phase, String testClass ->
    tasks.register(taskName, Exec) {
        group = 'verification'
        dependsOn 'assembleAlphaDebug', 'assembleAlphaDebugAndroidTest',
            ':verification-tools:classes'
        doFirst {
            def serial = providers.gradleProperty('uiGoldenSerial')
                .orElse(providers.environmentVariable('WENKU8_UI_GOLDEN_SERIAL'))
                .orNull
            if (serial == null || serial.isBlank()) {
                throw new GradleException('WENKU8-UI-GOLDEN-E001: uiGoldenSerial is required')
            }
            commandLine rootProject.ext.resolvePowerShell(), '-NoProfile', '-NonInteractive',
                '-File', new File(rootProject.projectDir, 'tools/verification/run-androidx-ui-goldens.ps1').absolutePath,
                '-Mode', mode, '-Phase', phase.toString(), '-TestClass', testClass,
                '-Serial', serial, '-Provider', 'public'
        }
    }
}
registerUiGoldenTask('recordPhase1UiGoldens', 'record', 1,
    'org.mewx.wenku8.navigation.Wenku8AppGoldenTest')
registerUiGoldenTask('verifyPhase1UiGoldens', 'verify', 1,
    'org.mewx.wenku8.navigation.Wenku8AppGoldenTest')
```

After the common parser assigns `command = args[0]`, `projectRoot = Path.of(args[1])`, `docsRoot = Path.of(args[2])`, and `commandArgs = args.drop(3)`, add exact dispatcher branches:

```kotlin
if (command == "verifyUiGoldenManifest") {
    require(commandArgs.size == 1) { "UI-GOLDEN-E001: expected exactly one phase selector" }
    val phaseSelector = commandArgs.single()
    val parsedPhase = phaseSelector.toIntOrNull()
    require(phaseSelector == "all" || (parsedPhase != null && parsedPhase in 1..8)) {
        "UI-GOLDEN-E002: invalid phase selector"
    }
    UiGoldenManifestVerifier.verify(projectRoot, docsRoot, phaseSelector)
    return
}
if (command == "approveUiGoldens") {
    require(commandArgs.size == 3) { "UI-GOLDEN-E003: expected phase, reviewer, and source commit" }
    val phase = commandArgs[0].toIntOrNull()
        ?.takeIf { it in 1..8 }
        ?: error("UI-GOLDEN-E004: invalid approval phase")
    val reviewer = commandArgs[1]
    val sourceCommit = commandArgs[2]
    UiGoldenApprover.approve(projectRoot, docsRoot, phase, reviewer, sourceCommit)
    return
}
```

`run-androidx-ui-goldens.ps1` implements the index contract exactly: one emulator serial, AGP `output-metadata.json` APK resolution, direct `adb shell am instrument -e class ... -e wenku8ScreenshotMode ...`, external-files extraction, canonical `cases.json`, cleanup in `finally`, and record/verify output separation. It invokes the already-built verifier main class directly after extraction; it never starts a nested Gradle build. `UiGoldenApprover` is the only writer of approved baselines/manifest hashes and is never a dependency of a verify/CI task.

Record candidates, inspect original pixels, then approve with a different reviewer identity before the verification step:

```powershell
.\gradlew.bat :app:recordPhase1UiGoldens -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public
$sourceCommit = (git rev-parse HEAD).Trim()
.\gradlew.bat :verification-tools:approveUiGoldens -Pphase=1 "-PuiGoldenReviewer=$env:WENKU8_UI_REVIEWER" "-PuiGoldenSourceCommit=$sourceCommit"
```

- [ ] **Step 5: Verify shell state/semantics/goldens and commit**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.navigation.*" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.Wenku8AppTest -Pwenku8Provider=public
.\gradlew.bat :app:verifyPhase1UiGoldens -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public
.\gradlew.bat :verification-tools:verifyUiGoldenManifest -Pphase=1
```

Expected: PASS; resizing preserves selected destination and no control/text crosses a hinge or clips at font 2.0.

```powershell
git add app\src\main\java\org\mewx\wenku8\navigation app\src\main\res app\src\test\java\org\mewx\wenku8\navigation app\src\androidTest\java\org\mewx\wenku8\navigation ..\..\docs\verification\ui-golden-manifest.yaml
git diff --check
git commit -m "feat: add adaptive Material 3 shell"
```

## Task 8: Lock Legacy Settings Semantics with Pure Codecs and Golden Fixtures

**Files:**

- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/settings/{LegacySettingKey,LegacySettingsSnapshot,LegacySettingsCodec,LegacySettingsAdapter,SettingsSnapshotCodec,SettingsWriteBarrier}.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/test/resources/legacy/settings/{empty,settings-v1,unknown-key,malformed-record,duplicate-key}.wk8`
- Create: `studio-android/LightNovelLibrary/core/storage/src/test/resources/legacy/settings/modern-reader-settings.json`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/settings/{LegacySettingsCodecTest,LegacySettingsAdapterTest,SettingsSnapshotCodecTest,SettingsWriteBarrierTest}.kt`
- Modify: `docs/verification/preference-manifest.yaml`

- [ ] **Step 1: Check in synthetic golden bytes and write RED semantic/byte tests**

Use only synthetic values. The complete known legacy key set is:

```text
version
language
menu_bg_id
menu_bg_path
reader_font_path
reader_font_size
reader_line_distance
reader_paragraph_distance
reader_paragraph_edge_distance
reader_background_path
eink_mode
```

`settings-v1.wk8` covers all keys using the exact `key::::value||||key::::value` UTF-8 format. Other fixtures cover empty, unknown, malformed, and duplicate records. Modern preferences cover `font_size_sp`, `line_height_sp`, `paragraph_spacing_sp`, and `night_mode`. Tests require primary-before-backup lookup, internal/external fallback, no destructive read, known mapping, unknown/malformed preservation, deterministic projection, and SHA-256 recorded in the preference manifest.

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.settings.*" -Pwenku8Provider=public
```

Expected: FAIL because codecs/adapters do not exist.

- [ ] **Step 2: Implement a lossless pure legacy codec**

```kotlin
data class LegacyRawRecord(val raw: String, val key: String?, val value: String?, val valid: Boolean)
data class LegacySettingsSnapshot(
    val known: Map<LegacySettingKey, String>,
    val unknown: Map<String, String>,
    val rawRecords: List<LegacyRawRecord>,
    val source: LegacySettingsSource,
)

object LegacySettingsCodec {
    fun decode(bytes: ByteArray, source: LegacySettingsSource): LegacySettingsSnapshot {
        val text = bytes.toString(Charsets.UTF_8)
        val records = text.split("||||").map { parseRecord(it) }
        return classify(records, source)
    }
    fun encode(snapshot: LegacySettingsSnapshot): ByteArray =
        orderedRecords(snapshot).joinToString("||||") { "${it.key}::::${it.value}" }.toByteArray(Charsets.UTF_8)
}
```

Do not discard invalid/duplicate/unknown raw records during import. Diagnostics store only an enum/count bucket and fixture hash, never raw value/path.

- [ ] **Step 3: Implement injected path/preference adapters**

Define `LegacySettingsFiles` and `LegacyModernReaderPreferences` interfaces so tests use in-memory bytes/maps. Production `LegacySettingsAdapter.snapshot()` reads `settings.wk8` in the exact Phase 0 manifest order and modern SharedPreferences without writing. `writeProjection()` uses `AtomicFile`: write temp, flush/fsync, finishWrite; on failure call `failWrite` and preserve the known-good file. It never truncates the only copy first.

- [ ] **Step 4: Map legacy snapshots to complete canonical values**

`SettingsSnapshotCodec.import` maps `SC -> LanguageSelection.SIMPLIFIED`, `TC -> TRADITIONAL`, numeric settings through declared bounds, `eink_mode 1/0`, custom paths as nullable strings, and modern reader preference values. Missing/invalid known values use documented defaults while their raw record remains preserved. Unknown valid pairs enter sorted `AppSettings.unknownLegacyValues` so projection/rollback does not erase them.

- [ ] **Step 5: Add the writer barrier before any coordinator uses it**

```kotlin
class SettingsWriteBarrier {
    private val mutex = kotlinx.coroutines.sync.Mutex()
    suspend fun <T> exclusive(block: suspend () -> T): T = mutex.withLock { block() }
}
```

Tests launch snapshot and concurrent writes on a test dispatcher and prove no write enters between snapshot begin/end; cancellation releases the barrier and propagates.

- [ ] **Step 6: Verify goldens and commit**

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.settings.*" -Pwenku8Provider=public
.\gradlew.bat :verification-tools:verifyInventories -Pwenku8Provider=public
```

Expected: PASS and preference inventory hashes match.

```powershell
git add core\storage ..\..\docs\verification\preference-manifest.yaml
git diff --check
git commit -m "test: lock legacy settings semantics"
```

## Task 9: Create the Canonical DataStore and Excluded Migration Journal Database

**Files:**

- Modify: `studio-android/LightNovelLibrary/core/storage/build.gradle`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/settings/{SettingsPreferencesDataSource,SettingsPreferencesCodec}.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/{MigrationState,JournalState,SettingsMigrationRecord,SettingsCheckpointRecord,SettingsJournalRecord,SettingsMigrationDao,MigrationTransientDatabase}.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/schemas/org.mewx.wenku8.core.storage.migration.MigrationTransientDatabase/1.json`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/{settings,migration}/**`
- Test: `studio-android/LightNovelLibrary/core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/MigrationTransientDatabaseTest.kt`

- [ ] **Step 1: Write RED physical-boundary, codec, DAO, and upgrade tests**

Require canonical file `files/datastore/app_settings.preferences_pb`; transient file `databases/migration-transient.db`; no canonical setting columns in transient schema; full DB/sidecars excluded by both backup XMLs. Test canonical round trip, version invariants, transactionally reserved unique mutation IDs, checkpoint idempotency, journal state transitions, pending ordering, process reopen, and schema export hash.

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest :core:storage:connectedDebugAndroidTest -Pwenku8Provider=public
```

Expected: FAIL because stores/schema do not exist.

- [ ] **Step 2: Configure DataStore, Room, KSP, and schema export**

Add only catalog aliases:

```groovy
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}
dependencies {
    implementation project(':core:model')
    implementation project(':core:session-contract')
    implementation libs.androidx.datastore.preferences
    implementation libs.androidx.room.runtime
    implementation libs.androidx.room.ktx
    ksp libs.androidx.room.compiler
    testImplementation libs.junit4
    testImplementation libs.kotlinx.coroutines.test
}
ksp { arg('room.schemaLocation', "$projectDir/schemas") }
```

Do not add a product flavor.

- [ ] **Step 3: Implement one canonical DataStore instance and exact keys**

Create via `PreferenceDataStoreFactory.create(scope, produceFile = { context.dataStoreFile(FILE_NAME) })` with `FILE_NAME="app_settings"`; never instantiate it twice. `SettingsPreferencesCodec` owns typed keys for every `AppSettings` field plus `mutation_version` and `legacy_projection_version`. Encode unknown legacy pairs as deterministic sorted JSON; reject a password/Cookie/captcha key by name/property test.

```kotlin
class SettingsPreferencesDataSource(private val dataStore: DataStore<Preferences>) {
    val records: Flow<VersionedSettings> = dataStore.data.map(SettingsPreferencesCodec::decode)
    suspend fun current(): VersionedSettings = records.first()
    suspend fun update(transform: (VersionedSettings) -> VersionedSettings): VersionedSettings =
        SettingsPreferencesCodec.decode(dataStore.updateData { current ->
            SettingsPreferencesCodec.encode(transform(SettingsPreferencesCodec.decode(current)))
        })
    suspend fun replace(record: VersionedSettings): VersionedSettings = update { record }
    suspend fun markProjectionVersion(version: Long): VersionedSettings = update { current ->
        require(current.mutationVersion == version)
        current.copy(legacyProjectionVersion = version)
    }
}
```

- [ ] **Step 4: Implement a separate transient Room schema**

```kotlin
enum class MigrationState { NOT_STARTED, SNAPSHOTTING, IMPORTING, DUAL_WRITE, RECONCILING, VERIFIED, LEGACY_READ_ONLY, COMPLETE }
enum class JournalState { PENDING, CANONICAL_APPLIED, LEGACY_PROJECTED }

@Entity(tableName = "settings_migration")
data class SettingsMigrationRecord(
    @PrimaryKey val domain: String = "settings",
    val state: MigrationState,
    val snapshotSha256: String?,
    val nextMutationId: Long,
)

@Entity(tableName = "settings_checkpoint", primaryKeys = ["domain", "recordKey"])
data class SettingsCheckpointRecord(val domain: String, val recordKey: String, val sourceHash: String, val importedVersion: Long)

@Entity(tableName = "settings_journal")
data class SettingsJournalRecord(
    @PrimaryKey val mutationId: Long,
    val canonicalVersion: Long,
    val payloadJson: String,
    val state: JournalState,
)
```

DAO transaction `reserveMutation(payloadJson, canonicalVersion)` increments `nextMutationId` and inserts PENDING atomically. State updates are compare-and-set (`WHERE state=:expected`) so replay cannot move backward. Pending reads sort by mutation ID.

- [ ] **Step 5: Verify files, schema, backup exclusions, and commit**

```powershell
.\gradlew.bat :core:storage:kspDebugKotlin :core:storage:testDebugUnitTest :core:storage:connectedDebugAndroidTest :verification-tools:verifyInventories -Pwenku8Provider=public
```

Expected: PASS; schema hash checked in; instrumentation reports exact physical paths; backup verifier confirms full transient DB/sidecar exclusion and canonical whole-store inclusion policy.

```powershell
git add core\storage app\src\main\res\xml ..\..\docs\verification\backup-manifest.yaml
git diff --check
git commit -m "feat: add versioned settings stores"
```

## Task 10: Implement Idempotent Settings Import, Journal Replay, and Reconciliation

**Files:**

- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/settings/{DataStoreSettingsRepository,SettingsMigrationCoordinator,SettingsReconciler,SettingsMutationCodec,MigrationCrashInjector}.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/settings/{SettingsMigrationCoordinatorTest,SettingsMutationTest,SettingsReconcilerTest,SettingsConcurrentWriterTest}.kt`

- [ ] **Step 1: Enumerate crash points and write the RED interruption matrix**

```kotlin
enum class MigrationCrashPoint {
    AFTER_SNAPSHOT_STATE,
    AFTER_RECORD_CHECKPOINT,
    AFTER_JOURNAL_INSERT,
    AFTER_CANONICAL_COMMIT,
    AFTER_JOURNAL_CANONICAL_MARK,
    AFTER_LEGACY_ATOMIC_REPLACE,
    AFTER_CANONICAL_PROJECTION_VERSION,
    AFTER_JOURNAL_PROJECTED_MARK,
}
fun interface MigrationCrashInjector { fun hit(point: MigrationCrashPoint) }
```

For every point: inject process-style exception, close stores, create a new coordinator, resume, and assert one canonical version increment, exact canonical values, exact compatible legacy projection, zero pending journal rows, no lost unknown/malformed raw record, and state `VERIFIED`. Add cancellation and 100-concurrent-writer cases.

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.*" -Pwenku8Provider=public
```

Expected: FAIL because coordinator/reconciler are absent.

- [ ] **Step 2: Implement startup state transitions and per-record import checkpoints**

Inside `SettingsWriteBarrier.exclusive`:

1. `NOT_STARTED -> SNAPSHOTTING`; read stable legacy file and modern preferences; store only sanitized source hash.
2. `SNAPSHOTTING -> IMPORTING`; import known keys one at a time in stable key order and upsert a checkpoint keyed by the legacy setting name/source hash.
3. Re-running a checkpoint with the same source hash is a no-op; a changed hash causes a new reviewed import version, never deletion of old bytes.
4. Commit one complete canonical `VersionedSettings` only after all records checkpoint.
5. Enter `DUAL_WRITE`, run reconciliation, then `VERIFIED` only when journals are empty and record-by-record equality is proven.

The old `.migration_completed` sentinel is read only for legacy external-copy behavior and never becomes this state.

- [ ] **Step 3: Implement journal-first, canonical-then-projection writes**

```kotlin
suspend fun update(mutation: SettingsMutation): SettingsWriteResult = barrier.exclusive {
    val before = canonical.current()
    val target = mutation.applyTo(before.value)
    val version = before.mutationVersion + 1
    val mutationId = journal.reserve(SettingsMutationCodec.encode(mutation), version)
    crash.hit(MigrationCrashPoint.AFTER_JOURNAL_INSERT)
    canonical.replace(VersionedSettings(target, version, before.legacyProjectionVersion))
    crash.hit(MigrationCrashPoint.AFTER_CANONICAL_COMMIT)
    journal.markCanonicalApplied(mutationId)
    return@exclusive try {
        legacy.writeProjection(SettingsSnapshotCodec.project(target))
        crash.hit(MigrationCrashPoint.AFTER_LEGACY_ATOMIC_REPLACE)
        canonical.markProjectionVersion(version)
        journal.markLegacyProjected(mutationId)
        journal.deleteProjected(mutationId)
        SettingsWriteResult.Synchronized(version)
    } catch (cancellation: kotlinx.coroutines.CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        SettingsWriteResult.PendingSynchronization(version, mutationId)
    }
}
```

The methods `current`, `replace`, and `markProjectionVersion` are the named `SettingsPreferencesDataSource` operations defined in Task 9; `reserve`, `markCanonicalApplied`, `markLegacyProjected`, and `deleteProjected` are the named DAO operations from that task. Before canonical commit, only transient journal state changes and both user stores remain unchanged. After canonical commit, failure is pending synchronization and never reported as fully synchronized.

- [ ] **Step 4: Reconcile with and without a transient journal**

`SettingsReconciler` replays pending rows in mutation order. It compares canonical `mutationVersion`, journal target, legacy semantic snapshot, and `legacyProjectionVersion` before each action. If the transient DB is absent after restore but canonical `legacyProjectionVersion < mutationVersion` or the legacy projection is missing/different, it deterministically projects the complete canonical value, then marks projection version. It never trusts a restored completion flag.

`DataStoreSettingsRepository.startMigration()` is guarded by an `AtomicBoolean`, invokes `SettingsMigrationCoordinator.start()`, publishes `SettingsReadiness.READY` only after initial reconciliation, and publishes a typed failed readiness without losing the legacy-readable fallback on error. Calling it twice is a no-op after the first launch.

- [ ] **Step 5: Verify idempotency, equality, and concurrent writers**

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.settings.*" -Pwenku8Provider=public
```

Expected: PASS at all eight crash points; 100 writes produce versions 1..100 with no duplicate/lost mutation; cancellation propagates; final legacy bytes and canonical semantics match.

- [ ] **Step 6: Commit the migration protocol**

```powershell
git add core\data
git diff --check
git commit -m "feat: add recoverable settings migration"
```

## Task 11: Intercept Every Settings Writer and Wire the Default AppContainer

**Files:**

- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/DefaultAppContainer.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/settings/{SettingsMutationLauncher,LegacySettingsProjectionObserver}.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/MyApp.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/global/GlobalConfig.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/{MainActivity,MenuBackgroundSelectorActivity}.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/fragment/ConfigFragment.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/setting/WenkuReaderSettingV1.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/activity/Wenku8ReaderActivityV1.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/settings/ModernReaderDisplaySettings.kt`
- Delete: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/settings/SharedPreferencesModernReaderDisplaySettingsStore.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/modern/activity/ModernReaderActivity.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/architecture/SettingsWriterVerifier.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/{build.gradle,src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt}`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/di/DefaultAppContainerTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/settings/SettingsMutationLauncherTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/settings/LegacySettingsProjectionObserverTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/settings/SettingsWriterIntegrationTest.kt`

- [ ] **Step 1: Add a RED exhaustive writer test from the Phase 0 preference inventory**

The verifier scans compiled/source writes to `settings.wk8`, `modern_reader_display_settings`, DataStore settings keys, and the eleven legacy setting identities. The only allowed physical writers are `SettingsPreferencesDataSource`, `LegacySettingsAdapter.writeProjection`, Room journal DAO, and test fakes. Every UI/legacy writer row must map to a typed `SettingsMutation` call.

Register and dispatch the command before the RED invocation:

```groovy
// verification-tools/build.gradle
tasks.register('verifySettingsWriters', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifySettingsWriters', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}
```

```kotlin
if (command == "verifySettingsWriters") {
    SettingsWriterVerifier.verify(projectRoot, docsRoot)
    return
}
```

Run:

```powershell
.\gradlew.bat :verification-tools:verifySettingsWriters -Pwenku8Provider=public
```

Expected: FAIL and name `GlobalConfig.setToAllSetting`, `setCurrentLang`, reader setters, menu background, ConfigFragment, and modern SharedPreferences writer call sites.

- [ ] **Step 2: Construct all long-lived bindings in `DefaultAppContainer`**

```kotlin
class DefaultAppContainer(context: android.content.Context) : AppContainer {
    private val applicationContext = context.applicationContext
    override val dispatchers = AppDispatchers(Dispatchers.Main, Dispatchers.IO, Dispatchers.Default)
    override val clock = AppClock { System.currentTimeMillis() }
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)
    private val writeBarrier = SettingsWriteBarrier()
    private val transientDb = MigrationTransientDatabase.create(applicationContext)
    private val canonical = SettingsPreferencesDataSource.create(applicationContext, scope)
    private val legacy = LegacySettingsAdapter.android(applicationContext)
    private val concreteSettings = DataStoreSettingsRepository(
        canonical, legacy, transientDb.settingsMigrationDao(), writeBarrier, clock, dispatchers.io,
    )
    override val settingsRepository: SettingsRepository = concreteSettings
    override val routeFlags: RouteFlagRepository = settingsRepository.asRouteFlagRepository()
    override val sessionStore: SessionStore = SignedOutSessionStore()
    override val providerBinding: ProviderBinding = ProviderBindingFactory.create(BuildConfig.WENKU8_PROVIDER_ID)

    fun start() {
        scope.launch { concreteSettings.startMigration() }
        scope.launch { LegacySettingsProjectionObserver(settingsRepository).collect() }
    }
}
```

`DataStoreSettingsRepository.startMigration()` delegates once to the Task 10 coordinator and drives `readiness`; it is the only startup entry. Use these constructor names consistently, never a global lookup or alternate store. The scope is owned/cancelled by the application process and is not exposed to composables.

- [ ] **Step 3: Make `MyApp` the owner without creating a service locator**

```kotlin
class MyApp : Application(), AppContainerOwner {
    private lateinit var defaultContainer: DefaultAppContainer
    override val appContainer: AppContainer get() = defaultContainer

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        defaultContainer = DefaultAppContainer(this)
        defaultContainer.start()
        Wenku8API.AppVer = BuildConfig.VERSION_NAME
    }
    // Retain getContext() only for verified legacy callers during the compatibility window.
}
```

Do not add a static `getAppContainer()`.

- [ ] **Step 4: Convert each UI writer to a typed lifecycle coroutine**

Use one helper:

```kotlin
fun LifecycleOwner.launchSettingsMutation(
    repository: SettingsRepository,
    mutation: SettingsMutation,
    onResult: (SettingsWriteResult) -> Unit = {},
) = lifecycleScope.launch { onResult(repository.update(mutation)) }
```

Map exact writers:

- Config language -> `SettingsMutation.Language`; e-ink -> `SettingsMutation.EinkMode`.
- Menu background ID/path -> one `SettingsMutation.MenuBackground` so paired values cannot tear.
- V1 reader font size/path, line height, paragraph spacing, edge margin, background -> typed reader mutations.
- Modern reader font/line/paragraph/night -> the same canonical reader mutations.

Activities use `lifecycleScope`; Fragment uses `viewLifecycleOwner.lifecycleScope`. Preserve in-memory control response, but show a recoverable Snackbar/error if result is `Failure` or `PendingSynchronization`. Never use `runBlocking` or an executor.

- [ ] **Step 5: Turn GlobalConfig into a compatibility read projection only**

Remove its direct settings file read/write methods and mutable `ContentValues` ownership. `LegacySettingsProjectionObserver` atomically installs the latest immutable `AppSettings` into a volatile compatibility snapshot used by retained getters (`getCurrentLang`, `isEinkModeEnabled`, reader getter methods). Remove `setToAllSetting`, `saveAllSetting`, and implicit write-on-read defaults after all call sites compile against repository updates. Keep legacy file access only inside `LegacySettingsAdapter`.

- [ ] **Step 6: Remove the direct modern SharedPreferences writer**

Migrate existing `modern_reader_display_settings` values during snapshot, then make `ModernReaderDisplaySettingsController` consume `StateFlow<VersionedSettings>` and expose suspend typed mutations. Delete `SharedPreferencesModernReaderDisplaySettingsStore` only after its inventory row maps to the importer and tests prove old preferences import exactly once.

- [ ] **Step 7: Verify interception and commit**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest :core:data:testDebugUnitTest :verification-tools:verifySettingsWriters -Pwenku8Provider=public
rg -n "setToAllSetting|saveAllSetting|getSharedPreferences\(.*modern_reader_display_settings|settings\.wk8" app\src\main\java -g '*.kt'
```

Expected: tests/verifier PASS; search returns no direct writer, with any retained string occurring only in a documented import constant.

```powershell
git add app core\data verification-tools ..\..\docs\verification\preference-manifest.yaml
git diff --check
git commit -m "refactor: intercept legacy settings writers"
```

## Task 12: Install Route Flags, the Launcher Trampoline, and the Real Shell Activity

**Files:**

- Move/Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/MainActivity.kt` -> `LegacyMainActivity.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/MainActivity.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/Wenku8ShellActivity.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/navigation/{MainRouteSelector,AndroidLegacyDestinationLauncher,WindowAdaptiveInfoAdapter}.kt`
- Create: `studio-android/LightNovelLibrary/app/src/debug/java/org/mewx/wenku8/debug/DeveloperRouteSwitchActivity.kt`
- Create: `studio-android/LightNovelLibrary/app/src/debug/AndroidManifest.xml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`
- Modify: retained Fragment/Activity references that currently cast/import `MainActivity`.
- Test: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/{MainTrampolineTest,ShellActivityTest,LegacyDestinationLauncherTest}.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/navigation/{Phase1RouteProcessDeathSeedTest,Phase1RouteProcessDeathVerifyTest}.kt`
- Create: `studio-android/LightNovelLibrary/tools/verification/run-ui-process-death.ps1`

- [ ] **Step 1: Write RED launcher identity, route default, and back-stack tests**

Assert merged manifest keeps exported launcher `org.mewx.wenku8.activity.MainActivity`; no flag/default data opens `LegacyMainActivity`; shell flag opens `Wenku8ShellActivity`; launcher finishes after one target; rotation/recreation does not start a duplicate target; old explicit MainActivity Intent remains accepted; debug switch is absent from release; top-level shell Back finishes, subordinate NavHost Back pops, and API 36 predictive-back start/cancel/commit preserves state. Separately define seed/verify cases for selected destination, drawer/Sheet logical visibility, and focus target; a one-process recreation assertion is not labeled process death.

Run:

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.MainTrampolineTest,org.mewx.wenku8.navigation.ShellActivityTest -Pwenku8Provider=public
```

Expected: FAIL because trampoline/shell Activity do not exist.

- [ ] **Step 2: Preserve old implementation as non-exported `LegacyMainActivity`**

Move the current class body and rename the class only. Update verified casts/imports. Add typed `EXTRA_LEGACY_SECTION` handling with enum values `LATEST`, `RANKINGS`, `BOOKSHELF`, `SETTINGS`; missing/malformed retains today's Latest default. Do not change its Fragment behavior in this task.

- [ ] **Step 3: Implement a non-blocking launcher trampoline with splash retention**

```kotlin
class MainActivity : ComponentActivity() {
    private var launched = false
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { !launched }
        if (savedInstanceState?.getBoolean(KEY_LAUNCHED) == true) return
        val container = applicationContext.appContainer()
        lifecycleScope.launch {
            val target = MainRouteSelector(container.settingsRepository, container.routeFlags).select()
            startActivity(Intent(this@MainActivity, target.activityClass))
            launched = true
            finish()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_LAUNCHED, launched)
        super.onSaveInstanceState(outState)
    }
}
```

`MainRouteSelector` waits for `SettingsReadiness.READY`, then reads the first route flags. It never blocks main and defaults to legacy on typed storage failure.

- [ ] **Step 4: Host Compose, Navigation, WindowManager, and theme in one shell Activity**

`Wenku8ShellActivity` obtains dependencies once, calls edge-to-edge centrally, collects app settings with lifecycle, maps `currentWindowAdaptiveInfo` and `WindowInfoTracker.windowLayoutInfo` to `AdaptiveLayoutInfo`, and invokes `Wenku8Theme { Wenku8App(appContainer = appContainer, adaptiveLayoutInfo = adaptiveLayoutInfo) }`. It owns the one `NavHostController`; no destination creates another Activity except the temporary `AndroidLegacyDestinationLauncher` compatibility bridge.

`AndroidLegacyDestinationLauncher` maps Discover/Bookshelf/Settings to `LegacyMainActivity` typed section extras and Search to `SearchActivity`. It is the only shell code that constructs those compatibility Intents.

- [ ] **Step 5: Add a debug-only local route switch**

`DeveloperRouteSwitchActivity` displays a compact Material 3 segmented/toggle surface only in debug, updates `RouteFlag.SHELL_ENABLED`, then relaunches `MainActivity` with `FLAG_ACTIVITY_NEW_TASK|CLEAR_TASK`. It does not expose an exported receiver, network kill switch, or release component. Route flags stay false in all release default resources.

- [ ] **Step 6: Update the manifest without changing application identity**

```xml
<activity
    android:name="org.mewx.wenku8.activity.MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity android:name="org.mewx.wenku8.activity.LegacyMainActivity" android:exported="false" />
<activity android:name="org.mewx.wenku8.activity.Wenku8ShellActivity" android:exported="false" />
```

Retain every other current component/exported policy unchanged.

- [ ] **Step 7: Verify legacy default and adaptive shell, then commit**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest :app:connectedAlphaDebugAndroidTest :app:processAlphaReleaseMainManifest -Pwenku8Provider=public
.\tools\verification\run-ui-process-death.ps1 -Phase 1 -Api 36 -SeedClass org.mewx.wenku8.navigation.Phase1RouteProcessDeathSeedTest -VerifyClass org.mewx.wenku8.navigation.Phase1RouteProcessDeathVerifyTest -Provider public
```

`run-ui-process-death.ps1` implements the plan-index contract and records pre/post PID plus source/app/test-APK/state hashes. Expected: PASS; release launcher opens legacy; instrumented flag opens shell; old explicit intent/default behavior passes; shell compact/medium/expanded tests pass; the verify invocation runs in a new PID and restores the selected destination/logical overlay intent without a duplicate launch effect.

```powershell
git add app
git diff --check
git commit -m "feat: add route-gated Compose launcher shell"
```

## Task 13: Prove Process-Death, Backup/Restore, and Signed Rollback Settings Safety

**Files:**

- Create: `docs/verification/settings-migration-manifest.yaml`
- Modify: `docs/verification/modernization-matrix.yaml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/res/xml/{data_extraction_rules,backup_rules}.xml`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/migration/{SettingsMigrationSeedTest,SettingsMigrationVerifyTest,SettingsBackupRestoreTest}.kt`
- Create: `studio-android/LightNovelLibrary/tools/verification/run-settings-migration-harness.ps1`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/compliance/SettingsMigrationEvidenceVerifier.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/{build.gradle,src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt}`

- [ ] **Step 1: Write RED evidence checks for every required boundary**

The manifest must enumerate each state transition, eight crash points, concurrent legacy writer, process death, canonical-to-legacy repair, transient-journal absence, whole-store backup/restore, post-canonical/pre-projection capture, and old signed rollback read. Each row requires fixture hash, API, build/signer hash, pre/post canonical/legacy/journal hashes, expected result, report path, and commit.

Register and dispatch the verifier before its first invocation:

```groovy
tasks.register('verifySettingsMigrationEvidence', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifySettingsMigrationEvidence', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}
```

```kotlin
if (command == "verifySettingsMigrationEvidence") {
    SettingsMigrationEvidenceVerifier.verify(projectRoot, docsRoot)
    return
}
```

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "*.SettingsMigrationEvidenceVerifier*"
```

Expected: FAIL because evidence is absent.

- [ ] **Step 2: Assert the exact physical backup policy after DataStore exists**

Retain full exclusions for `migration-transient.db`, WAL/SHM/journal, session-store, and `saves/cert.wk8`. Record `files/datastore/app_settings.preferences_pb` as one included canonical store; do not attempt row selection. The canonical file contains full settings values, mutation version, and legacy projection version.

- [ ] **Step 3: Build a host-side process-death harness**

The PowerShell harness validates target paths, installs a release-like artifact, then for each crash point:

```powershell
adb shell am instrument -w -e stage seed -e crashPoint AFTER_CANONICAL_COMMIT org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
adb shell am force-stop org.mewx.wenku8
adb shell monkey -p org.mewx.wenku8 1
adb shell am instrument -w -e stage verify -e crashPoint AFTER_CANONICAL_COMMIT org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
```

The actual script loops all eight enum values and captures `dumpsys package`, app/test APK hashes, and test reports. Expected: one canonical mutation, no duplicate mutation ID, exact legacy projection, zero pending row after resume.

- [ ] **Step 4: Capture backup after canonical commit but before projection**

The seed test pauses after the canonical DataStore fsync and before projection, records hashes, and terminates. Run backup, clear/reinstall/restore without copying the excluded transient DB, then launch verifier:

```powershell
adb shell bmgr backupnow org.mewx.wenku8
adb shell pm clear org.mewx.wenku8
$restoreLine = adb shell bmgr list sets | Select-String -Pattern '^[0-9a-fA-F]+\s*:' | Select-Object -First 1
if ($null -eq $restoreLine) { throw 'No backup restore set is available' }
$restoreToken = ($restoreLine.Line -split ':')[0].Trim()
adb shell bmgr restore $restoreToken org.mewx.wenku8
adb shell am instrument -w -e stage verifyRestore org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
```

Expected: canonical version/value restores, app is signed out, coordinator detects missing/stale projection from canonical versions alone, and recreates exact `settings.wk8`; transient journal remains absent.

- [ ] **Step 5: Prove an old signed/minified rollback build reads the projection**

In the external signing environment, install/upgrade to the new externally signed candidate, execute every legacy-compatible setting mutation, verify synchronization, then install the prior signed APK using the same certificate and downgrade allowance:

```powershell
adb install -r build\signed-candidate\app-alpha-release.apk
adb shell am instrument -w -e stage writeAllSettings org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
adb install -r -d $env:WENKU8_OLD_SIGNED_APK
adb shell am instrument -w -e stage verifyLegacySettings org.mewx.wenku8.compat.test/androidx.test.runner.AndroidJUnitRunner
```

Expected: old build/probe reads language, e-ink, menu background, V1/modern reader values and unknown legacy values with exact semantics. No non-secret legacy file is deleted. Store only hashes/redacted reports.

- [ ] **Step 6: Run API 23/33 evidence verification and commit**

```powershell
.\tools\verification\run-settings-migration-harness.ps1 -ApiLevels 23,33 -Provider public
.\gradlew.bat :verification-tools:verifySettingsMigrationEvidence -Pwenku8Provider=public
```

Expected: PASS for every row and no report contains free text, identifiers, paths, raw settings values, or secrets.

```powershell
git add app\src\main\res\xml app\src\androidTest tools\verification verification-tools ..\..\docs\verification\settings-migration-manifest.yaml ..\..\docs\verification\modernization-matrix.yaml
git diff --check
git commit -m "test: prove settings migration recovery"
```

## Task 14: Enforce Architecture Rules and Bind the Phase 1 Exit Gate

**Files:**

- Modify: `studio-android/LightNovelLibrary/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Create: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/architecture/{ModuleDependencyTest,ForbiddenImportsTest,ComposeBoundaryTest,ViewModelConventionTest}.kt`
- Modify: `docs/verification/modernization-matrix.yaml`
- Modify: `.github/workflows/android-ci.yml`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/compliance/Phase1Gate.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/compliance/Phase1GateTest.kt`

- [ ] **Step 1: Write RED ArchUnit/source dependency rules**

Enforce:

- `feature..` cannot import `Wenku8API`, `LightNetwork`, `LightUserSession`, `GlobalConfig`, DAO implementations, OkHttp, or Jsoup.
- `..ui..`/composables cannot import storage/network/parser/file/executor packages or `AppContainerOwner`.
- ViewModels depend on domain/use-case interfaces and expose immutable `StateFlow`; no implementation/global constructor.
- API/core JVM modules have no Android dependency.
- `core:network` depends on session contract, never storage; `core:storage` supplies session later and never network.
- No Gradle cycle, duplicate project directory, or cross-feature ViewModel import.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.architecture.*" -Pwenku8Provider=public
```

Expected: FAIL until all intended package/module boundaries are visible to the tests and any accidental import is removed.

- [ ] **Step 2: Add the Phase 1 evidence rows and aggregate verifier**

Map every Phase 1 deliverable/exit condition to exact task/test/provider/variant/API/fixture/golden/report/hash/commit. `Phase1Gate` verifies current files and report hashes; `NOT_RUN`, stale commit, indirect evidence, or one narrow test standing in for a broad claim fails.

Register the canonical verifier, architecture aggregate, and the retained Phase 3 compatibility alias with these exact paths:

```groovy
// verification-tools/build.gradle
tasks.register('phase1Gate', JavaExec) {
    group = 'verification'
    description = 'Verifies the complete Phase 1 evidence matrix.'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'phase1Gate', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
    dependsOn ':phase0Gate', ':app:testAlphaDebugUnitTest',
        ':core:model:test', ':core:domain:test', ':core:network:test',
        ':core:storage:testDebugUnitTest', ':core:data:testDebugUnitTest',
        'verifyInventories', 'verifySettingsWriters', 'verifySettingsMigrationEvidence',
        'verifyUiGoldenManifest', 'verifyOutboundManifest', 'verifyPlannedGradleTasks',
        'verifyXmlSurfaceLedger', ':app:verifyPhase1UiGoldens'
}

// root build.gradle
tasks.register('verifyArchitecture') {
    group = 'verification'
    description = 'Runs Phase 1 architecture rules and evidence verification.'
    dependsOn ':app:testAlphaDebugUnitTest', ':verification-tools:phase1Gate'
}

// app/build.gradle; retained only for the already-written Phase 3 command.
tasks.register('phase1ExitGate') {
    group = 'verification'
    description = 'Compatibility alias for the canonical Phase 1 architecture gate.'
    dependsOn ':verifyArchitecture'
}
```

Add the exact dispatcher branch to `VerificationMain.kt`:

```kotlin
if (command == "phase1Gate") {
    org.mewx.wenku8.verification.compliance.Phase1Gate.verify(projectRoot, docsRoot)
    return
}
```

New plans call `:verification-tools:phase1Gate` or root `verifyArchitecture`; `:app:phase1ExitGate` contains no verification logic and may be removed only after every retained Phase 3 command/evidence row is migrated.

- [ ] **Step 3: Run the complete public module and app suite**

```powershell
.\gradlew.bat clean :api-contract:test :api-public:test :api-contract-tests:test :core:model:test :core:domain:test :core:session-contract:test :core:network:test :core:storage:testDebugUnitTest :core:data:testDebugUnitTest :core:designsystem:testDebugUnitTest :core:testing:testDebugUnitTest :app:testAlphaDebugUnitTest :app:lintAlphaDebug :app:assembleAlphaDebug :app:assembleBaiduDebug :app:assemblePlaystoreDebug -Pwenku8Provider=public
```

Expected: PASS. `:api-public` contains identity only; no transport/network/parser source.

- [ ] **Step 4: Run instrumentation, adaptive goldens, migration, and release-like R8 compatibility**

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest :core:storage:connectedDebugAndroidTest :core:designsystem:connectedDebugAndroidTest :app:assembleAlphaRelease :verification-tools:verifyUiGoldenManifest :verification-tools:verifySettingsMigrationEvidence -Pwenku8Provider=public
```

Expected: PASS only under the Phase 0 authorization/license gates. API 23/33 settings and API 36 shell predictive-back/inset smokes are retained in the matrix. Old-signed/minified contracts still pass.

- [ ] **Step 5: Re-run Phase 0, coverage, warnings, inventory, secrets, and graph gates**

```powershell
.\gradlew.bat phase0Gate verifyPhase0Coverage :verification-tools:verifyWarnings :verification-tools:verifyInventories :verification-tools:verifySettingsWriters :verification-tools:verifyOutboundManifest -Pwenku8Provider=public
rg -n "okhttp|jsoup|CookieJar|HttpURLConnection" api-public core\network -g '*.kt'
git diff --check
```

Expected: all gates PASS; search exits 1; no credential literal or protected endpoint appears in source/report/artifact.

- [ ] **Step 6: Run and commit the Phase 1 aggregate gate**

```powershell
.\gradlew.bat :verification-tools:phase1Gate -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public
git add app api-contract api-public api-contract-tests core settings.gradle build.gradle gradle verification-tools ..\..\docs\verification ..\..\.github\workflows\android-ci.yml
git diff --check
git commit -m "test: bind phase one architecture evidence"
```

Expected: PASS with all Phase 1 evidence current and no Critical/Important open finding.

## Phase 1 Handoff

Hand independent reviewers:

- the fourteen focused commits and exact public/protected module graphs;
- architecture/import/StateFlow/container reports;
- compact/medium/expanded/hinge/light/dark/font-scale shell goldens and semantics results;
- route-default/trampoline/Intent/predictive-back evidence;
- every settings crash-point, concurrent-writer, process-death, backup/restore, and signed rollback report;
- Phase 0 regression gate and fresh protected-private attestation where private claims are made;
- an explicit statement that Phase 1 made zero live calls and `:api-public` has no provider/transport/parser implementation.

Run independent architecture/API, Material 3/navigation/accessibility, and migration/testing/compatibility reviews. Resolve every Critical and Important finding before executing the Phase 2 public-provider plan.
