# Wenku8 Phase 8 Legacy Removal and Release Audit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove every retired legacy runtime path, prove the complete modernization Definition of Done, and produce externally signed, minified, reproducible release evidence only for currently authorized channels.

**Architecture:** Phase 8 is a proof-driven subtraction phase. A generated retirement manifest links every deletion to zero-reachability, compatibility-window, migration, provider, UI, and rollback evidence before source is removed; public builds drop the frozen legacy API bridge, while a protected private graph may retain its isolated private adapter only inside attested CI. The frozen launcher identity `org.mewx.wenku8.activity.MainActivity` becomes the sole Activity and directly owns the one Navigation Compose host; all pages are Compose Material 3 over typed provider/domain boundaries, canonical stores, and bounded background work.

**Tech Stack:** Kotlin, Groovy Gradle, Android Gradle Plugin, Compose Material 3, Navigation Compose, Room, DataStore, WorkManager/JobScheduler, R8, Baseline Profiles, JUnit4, AndroidX Test, screenshot testing, Gradle dependency verification/locking, CycloneDX, apksigner, bundletool, PowerShell, ADB.

---

## Scope And Stop Conditions

Run Gradle commands from `studio-android/LightNovelLibrary/` and repository scripts from the repository root. Phase 0-7 exit gates must be current and bound to the same source ancestry.

Stop immediately when any deletion lacks a reviewed retirement row; an old upgrade/rollback fixture cannot be read; a migration journal is unreconciled; a public/private graph leaks the other implementation; a production license is unknown/incompatible; a site/content/channel approval is unknown/expired/rejected; a private attestation is stale/replayed/unbound; signing material enters the repository; or a credential, Cookie, captcha, private endpoint, or raw user/content body enters source, commands, logs, screenshots, reports, or artifacts.

The public provider may be built for deterministic internal verification while a distribution scope is blocked, but no blocked artifact is signed or described as releasable. Daily check-in remains absent unless the accepted HTTPS contract required by Phase 2 and Phase 7 exists.

## File Structure

- Create `docs/verification/legacy-retirement-manifest.yaml`, `phase-8-release-matrix.yaml`, `definition-of-done-matrix.yaml`, `final-independent-audit.md`, and `emergency-rollback-runbook.md`.
- Create `docs/verification/phase-8-retirement-audit.md` with per-domain compatibility-window approvals.
- Create `studio-android/LightNovelLibrary/verification/verify-phase8-retirement.ps1`, `verify-final-architecture.ps1`, `verify-release-evidence.ps1`, and `reproducible-release.ps1`.
- Create `studio-android/LightNovelLibrary/verification/stage-retired-paths.ps1`; it stages only exact accepted file rows from the retirement manifest.
- Create `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/retirement/{ReachabilityGraph,RetirementManifestVerifier,LegacySymbolScanner}.kt`.
- Create `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/release/{DefinitionOfDoneVerifier,ReleaseEvidenceVerifier,ReproducibilityVerifier}.kt`.
- Modify `studio-android/LightNovelLibrary/settings.gradle`, root/app Gradle files, `app/src/main/AndroidManifest.xml`, app navigation/composition files, backup rules, ProGuard rules, baseline-profile configuration, CI workflows, public SBOM/provenance/locks/notices, and root README.
- Delete only paths named by accepted retirement rows; Tasks 2-7 name the production families and keep required golden fixtures/evidence.

## Task Dependency Graph

`1 -> 2 -> 3 -> 4`; `1 -> 5 -> 6`; `3,4,6 -> 7 -> 8`; `8 -> 9,10,11`; `9,10,11 -> 12 -> 13 -> 14`.

### Task 1: Freeze The Exact Retirement And Definition-Of-Done Manifests

**Files:**
- Create: `docs/verification/legacy-retirement-manifest.yaml`
- Create: `docs/verification/definition-of-done-matrix.yaml`
- Create: `docs/verification/phase-8-retirement-audit.md`
- Create: `studio-android/LightNovelLibrary/verification/stage-retired-paths.ps1`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/retirement/RetirementManifestVerifier.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/retirement/RetirementManifestVerifierTest.kt`

- [ ] **Step 1: Write RED schema and completeness tests**

Require each row to contain stable ID, one exact file path/symbol (directories and globs are forbidden), migration domain, owner phase, last reachable route, zero-reachability report hash, replacement test IDs, old-signed/minified compatibility result, migration/rollback result, compatibility-window decision, window start/end signed release IDs and dates, reviewer, action (`DELETE`, `KEEP_EVIDENCE`, or `PROTECTED_PRIVATE_ONLY`), and source commit. The referenced per-domain audit must prove at least two successive signed production releases and 30 calendar days (whichever is longer), zero confirmed data-loss/security incidents, no unresolved P1 migration defect, 100% reconciliation in release qualification, controlled-pilot size/procedure/limits, and no persistent pending-journal bucket above threshold. Reject directories, wildcards, missing hashes, stale commits, self-approved rows, absent explicit-export evidence, or `UNKNOWN` decisions.

Run: `.\gradlew.bat :verification-tools:test --tests "*.RetirementManifestVerifierTest" -Pwenku8Provider=public --console=plain`

Expected: FAIL because the manifests and verifier are absent.

- [ ] **Step 2: Add deterministic YAML schemas and initial rows**

```yaml
schemaVersion: 1
fixtureOnly: true
sourceCommit: "0000000000000000000000000000000000000000"
entries:
  - id: legacy-main-fragments
    exactFilePath: studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/fragment/LatestFragment.kt
    migrationDomain: library-shell
    lastReachableRoute: legacy-shell-rollback
    action: DELETE
    compatibilityWindowDecision: ACCEPTED
    windowStartRelease: signed-release-a
    windowEndRelease: signed-release-b
    windowStartDate: 2026-01-01
    windowEndDate: 2026-01-31
    retirementAudit: docs/verification/phase-8-retirement-audit.md#library-shell
    evidence:
      reachabilitySha256: "0000000000000000000000000000000000000000000000000000000000000000"
      replacementTestIds: [P4-DISCOVER, P4-SEARCH, P4-BOOKSHELF]
      oldSignedResult: PASS
      migrationRollbackResult: PASS
```

The verifier rejects `fixtureOnly: true`, the all-zero commit/hash, the illustrative dates/releases, and any `exactFilePath` that is not one repository-relative file. During execution the generator writes a non-fixture row only from measured commit/hashes, signed release metadata, current migration diagnostics, explicit-export pilot evidence, and reviewed per-domain decisions; never invent values.

- [ ] **Step 3: Generate candidate rows from Phase 0 UI/ABI/path inventories**

`RetirementManifestVerifier` joins the Phase 0 inventory, Phase 4-7 route ledgers, merged manifests, R8 usage output, source import graph, runtime navigation traces, signed release history, incident/P1 review, reconciliation fixtures, and the per-domain retirement audit. Every active inventory row must map to a final route/component or one exact-file retirement entry. `stage-retired-paths.ps1` parses accepted rows, resolves each path beneath the repository, rejects directories/symlinks/globs, and invokes `git add -A --` with only those file paths.

Register the verifier in `verification-tools/build.gradle`:

```groovy
tasks.register('verifyRetirementManifest', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyRetirementManifest', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}
```

Add the matching `VerificationMain.kt` branch after argument parsing:

```kotlin
if (command == "verifyRetirementManifest") {
    org.mewx.wenku8.verification.retirement.RetirementManifestVerifier.verify(projectRoot, docsRoot)
    return
}
```

- [ ] **Step 4: Run and commit the frozen manifest**

Run: `.\gradlew.bat :verification-tools:verifyRetirementManifest -Pwenku8Provider=public --console=plain`

Expected: PASS with no unclassified row or sentinel.

```powershell
git add ..\..\docs\verification\legacy-retirement-manifest.yaml ..\..\docs\verification\definition-of-done-matrix.yaml verification-tools
git diff --check --cached
git commit -m "test(retirement): freeze legacy removal evidence"
```

### Task 2: Prove Zero Product Reachability Before Deleting Legacy Pages

**Files:**
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/retirement/{ReachabilityGraph,LegacySymbolScanner,FinalUiTechnologyVerifier}.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Create: `studio-android/LightNovelLibrary/verification/verify-phase8-retirement.ps1`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/retirement/ReachabilityGraphTest.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/retirement/FinalUiTechnologyVerifierTest.kt`

- [ ] **Step 1: Write RED graph fixtures**

Inject a legacy Activity manifest entry, explicit Intent, Fragment transaction, XML inflation, notification PendingIntent, deep link, reflection string, ServiceLoader entry, and Compose callback. Require a shortest source-to-target path for each; unreachable evidence without scanning all roots fails.

- [ ] **Step 2: Implement the final runtime-root scanner**

Scan launcher, exported/non-exported manifest components, Navigation Compose routes, deep links, notifications, workers/jobs/services/receivers, explicit/implicit Intents, reflection/class-name resources, XML custom views, provider loaders, and test-excluded production bytecode. Output deterministic JSON and SHA-256.

Register the exact task in `verification-tools/build.gradle`:

```groovy
tasks.register('verifyFinalReachability', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyFinalReachability', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}
tasks.register('verifyFinalUiTechnology', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyFinalUiTechnology', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}
```

Add this exact dispatcher branch:

```kotlin
if (command == "verifyFinalReachability") {
    org.mewx.wenku8.verification.retirement.ReachabilityGraph.verifyFinal(projectRoot, docsRoot)
    return
}
if (command == "verifyFinalUiTechnology") {
    org.mewx.wenku8.verification.retirement.FinalUiTechnologyVerifier.verify(projectRoot, docsRoot)
    return
}
```

- [ ] **Step 3: Assert final active source bans**

```powershell
$roots = @('app/src/main','feature','core','api-public/src/main')
$patterns = 'AsyncTask|findViewById|androidx.fragment.app.Fragment|AppCompatActivity|androidx.cardview|GlobalConfig\.|LightNetwork|LightUserSession|Wenku8API|com\.google\.android\.material|AndroidView(Binding)?|ViewBinding|DataBindingUtil|LayoutInflater|setContentView'
$hits = rg -n $patterns $roots -g '*.kt' -g '*.xml' 2>$null
if ($hits) { throw $hits }
```

Allow only exact compatibility/evidence rows still marked `KEEP_EVIDENCE`; no reachable file may match. The regex is a fast diagnostic, not the authoritative proof.

`FinalUiTechnologyVerifier` parses reachable Kotlin with the Kotlin 2.2.10 compiler PSI already pinned to `verification-tools` and resolves imports/call aliases. It rejects:

- `androidx.compose.material.*` component imports/calls except the exact `androidx.compose.material.icons.*` namespace and `material-icons-extended` coordinate;
- `com.google.android.material.*`, AppCompat widget/page classes, ViewBinding/DataBinding, `AndroidView`, `AndroidViewBinding`, layout inflation, and XML-owned page/view bridges;
- release dependencies on Compose Material 2 components, Material Views, AppCompat UI, CardView, or view-binding/data-binding after reachable users are gone;
- a Material 3 `Card`, `ElevatedCard`, or `OutlinedCard` call lexically/semantically inside the content lambda of another such card, including aliased imports and wrapper functions annotated by the verifier fixture;
- any reachable XML reference without its exact X01-X34 row or any X row lacking replacement/retirement evidence.

Tests inject each forbidden form plus allowed Material 3 surfaces and Material icon imports. Regex-only matching, screenshots, and dependency absence alone cannot satisfy this gate.

- [ ] **Step 4: Run and commit reachability evidence**

Run: `.\gradlew.bat :verification-tools:verifyFinalReachability :verification-tools:verifyFinalUiTechnology :verification-tools:verifyXmlSurfaceLedger :app:processAlphaReleaseMainManifest :app:assembleAlphaRelease -Pwenku8Provider=public --console=plain`

Expected: PASS and a zero-path report for every `DELETE` row.

```powershell
git add verification-tools verification ..\..\docs\verification\legacy-retirement-manifest.yaml
git diff --check --cached
git commit -m "test(retirement): prove zero legacy reachability"
```

### Task 3: Delete Retired Activity, Fragment, XML, And Custom Navigation Families

**Files:**
- Delete accepted rows under `app/src/main/java/org/mewx/wenku8/activity/legacy/`
- Delete accepted rows under `app/src/main/java/org/mewx/wenku8/fragment/`
- Delete accepted legacy adapters/components under `app/src/main/java/org/mewx/wenku8/{adapter,component}/`
- Delete accepted page layouts under `app/src/main/res/layout/`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`, `values-zh-rTW/strings.xml`, and `values-zh-rHK/strings.xml`
- Test: `app/src/androidTest/java/org/mewx/wenku8/navigation/FinalRouteReachabilityTest.kt`

- [ ] **Step 1: Add a RED exact-count test**

Assert one Activity at the frozen class name `org.mewx.wenku8.activity.MainActivity`, zero `Wenku8ShellActivity`, zero Fragment page classes, zero reachable page-layout XML, zero AppCompat Toolbar/old CardView, every A01-A15/F01-F06 ledger ID mapped to Compose or retirement evidence, and every X01-X34 row retained with exact replacement-test and deletion/report hashes. The test fails if the resource directory and XML ledger differ in either direction.

- [ ] **Step 2: Delete only accepted manifest/source/resource rows**

Process exact accepted file rows in dependency order: original-name trampolines other than `MainActivity`, rollback-only Activities, Fragments, adapters/components, page layouts, then exact unreferenced resource files. Preserve `MainActivity`, generated notices, backup rules, icons, localized strings still referenced by Compose, golden fixtures, Serializable compatibility fixtures, and migration evidence. No directory-wide deletion command is allowed.

- [ ] **Step 3: Verify resource shrinker and navigation**

Run: `.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.FinalRouteReachabilityTest :app:assembleAlphaRelease -Pwenku8Provider=public --console=plain`

Expected: PASS; release merged manifest has one Activity and no deleted class/resource reference.

- [ ] **Step 4: Commit deletion as one reviewed set**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\stage-retired-paths.ps1 -Manifest ..\..\docs\verification\legacy-retirement-manifest.yaml -TaskId 3
git diff --check --cached
git commit -m "refactor(ui): remove retired legacy pages"
```

### Task 4: Remove Legacy Concurrency, Global State, And Transition Facades

**Files:**
- Delete accepted `app/src/main/java/org/mewx/wenku8/async/` rows
- Delete accepted `app/src/main/java/org/mewx/wenku8/global/GlobalConfig.kt`
- Delete accepted transition launchers/adapters under `app/src/main/java/org/mewx/wenku8/{compat,navigation,settings}/`
- Modify: `app/src/main/java/org/mewx/wenku8/MyApp.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/di/{AppContainer,DefaultAppContainer}.kt`
- Test: `verification-tools/src/test/kotlin/org/mewx/wenku8/verification/architecture/FinalArchitectureTest.kt`

- [ ] **Step 1: Write RED source and bytecode rules**

Reject AsyncTask, raw Thread/Executor ownership in reachable UI, Handler business orchestration, `findViewById`, direct GlobalConfig, static mutable Context/container access, Activity/composable provider/network/parser/cache/file calls, and transition interfaces with no caller.

- [ ] **Step 2: Move remaining ownership to reviewed boundaries**

The application creates `DefaultAppContainer` once; ViewModels receive domain interfaces and dispatchers; workers/jobs receive factories; feature UI receives immutable StateFlow and callbacks. Delete each transition facade only after its production caller count is zero.

- [ ] **Step 3: Run architecture and lifecycle suites**

Run: `.\gradlew.bat verifyArchitecture :app:testAlphaDebugUnitTest :feature:library:testDebugUnitTest :feature:novel:testDebugUnitTest :feature:reader:testDebugUnitTest :feature:account:testDebugUnitTest :feature:settings:testDebugUnitTest -Pwenku8Provider=public --console=plain`

Expected: PASS with zero banned symbol.

- [ ] **Step 4: Commit**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\stage-retired-paths.ps1 -Manifest ..\..\docs\verification\legacy-retirement-manifest.yaml -TaskId 4
git add app/src/main/java/org/mewx/wenku8/MyApp.kt app/src/main/java/org/mewx/wenku8/di/AppContainer.kt app/src/main/java/org/mewx/wenku8/di/DefaultAppContainer.kt verification-tools
git diff --check --cached
git commit -m "refactor(architecture): remove transition ownership"
```

### Task 5: Retire Route Flags And Compatibility Trampolines

**Files:**
- Modify: `core/model/src/main/kotlin/org/mewx/wenku8/core/model/settings/{RouteRolloutFlags,AppSettings}.kt`
- Modify: `core/domain/src/main/kotlin/org/mewx/wenku8/core/domain/settings/RouteFlagRepository.kt`
- Modify: `core/storage/src/main/java/org/mewx/wenku8/core/storage/settings/{SettingsPreferencesDataSource,LegacySettingsAdapter}.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/navigation/{AppRoute,Wenku8NavHost}.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/activity/MainActivity.kt`
- Delete: `app/src/main/java/org/mewx/wenku8/activity/Wenku8ShellActivity.kt`
- Delete: `app/src/debug/java/org/mewx/wenku8/debug/DeveloperRouteSwitchActivity.kt`
- Test: `app/src/androidTest/java/org/mewx/wenku8/navigation/FinalLauncherBackStackTest.kt`

- [ ] **Step 1: Write RED launcher/default/process tests**

Assert launcher opens the shell directly, all internal/deep-link routes use one NavHost, process recreation does not duplicate destinations, top-level Back exits, subordinate Back pops, API 36 predictive cancel/commit is stable, and no route flag or legacy target exists in release bytecode.

- [ ] **Step 2: Remove route-flag reads and debug switch**

Move the reviewed edge-to-edge theme, adaptive-window mapping, and one NavHost body from transitional `Wenku8ShellActivity` into the frozen `MainActivity` class, then delete `Wenku8ShellActivity`. `MainActivity` becomes the unconditional shell launcher without starting another Activity. Remove route flag fields/mutations/preferences only after settings migration imports and rollback release evidence are accepted; preserve unknown legacy setting pairs without reintroducing a switch.

- [ ] **Step 3: Run launcher, deep-link, settings-upgrade, and minified tests**

Run: `.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.FinalLauncherBackStackTest :core:storage:testDebugUnitTest --tests "*Settings*" :app:assembleAlphaRelease -Pwenku8Provider=public --console=plain`

- [ ] **Step 4: Commit**

```powershell
git add -A app core/model core/domain core/storage
git diff --check --cached
git commit -m "refactor(navigation): retire rollout trampolines"
```

### Task 6: Remove The Public Legacy API Bridge And Prove Provider Isolation

**Files:**
- Delete: `studio-android/LightNovelLibrary/api-legacy-bridge/`
- Modify: `studio-android/LightNovelLibrary/settings.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/api-private-adapter/build.gradle`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/architecture/FinalProviderGraphTest.kt`

- [ ] **Step 1: Write RED graph/import/ABI tests**

Public mode must include `api-contract`, `api-public`, and no logical `:api`/bridge/private adapter. Protected private mode includes its logical private `:api` and `api-private-adapter` only through the protected overlay. App/features import only core domain/model; `core:data` owns the typed provider binding.

- [ ] **Step 2: Remove all bridge imports before its directory**

Run `rg -n "org\.mewx\.wenku8\.(api\.Wenku8API|network\.Light(Network|UserSession))" app core feature -g '*.kt'`; replace no code here unless a reviewed typed core boundary exists. The search must be empty before deleting the bridge.

- [ ] **Step 3: Configure final public/private graphs**

```groovy
include ':api-contract', ':api-public', ':api-contract-tests'
if (provider == 'private') {
    include ':api'
    project(':api').projectDir = file('api')
    include ':api-private-adapter'
}
```

The actual settings file keeps the index's explicit `includeAt` helper and redacted missing-private failure; the snippet states the final membership, not permission to duplicate descriptors.

- [ ] **Step 4: Run both graphs and commit**

Run public: `.\gradlew.bat projects :api-contract:test :api-public:test :api-contract-tests:test :app:assembleAlphaRelease -Pwenku8Provider=public --console=plain`

Run protected CI only: `.\gradlew.bat projects :api-private-adapter:test :app:assembleAlphaRelease -Pwenku8Provider=private --console=plain`

Expected: public PASS without bridge/private symbols; private PASS only with fresh protected source and attestation.

```powershell
git add -A settings.gradle app/build.gradle api-legacy-bridge api-private-adapter verification-tools
git diff --check --cached
git commit -m "refactor(api): remove frozen legacy bridge"
```

### Task 7: End Legacy Dual-Write Only After The Compatibility Window

**Files:**
- Modify: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/`
- Modify: `core/storage/src/main/java/org/mewx/wenku8/core/storage/settings/LegacySettingsAdapter.kt`
- Modify: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/`
- Modify: `app/src/main/res/xml/backup_rules.xml` and `data_extraction_rules.xml`
- Create: `docs/verification/legacy-write-retirement-report.yaml`
- Create: `docs/verification/legacy-read-retirement-report.yaml`
- Test: `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/LegacyWriteRetirementTest.kt`

- [ ] **Step 1: Write RED upgrade/import/rollback tests**

Seed every Phase 0 settings/search/bookshelf/pixel/V1/catalog/download fixture and an interrupted journal. Prove import/reconciliation, canonical values, download bytes, process/reboot resume, backup/restore, and the last approved old-version rollback reader before disabling projection.

- [ ] **Step 2: Freeze a final legacy snapshot and switch writers off**

In one canonical transaction mark each domain `LEGACY_READ_ONLY` only when `mutationVersion == legacyProjectionVersion`, no pending journal/checkpoint/lease exists, and the reviewed write-retirement compatibility-window decision matches the running source. Keep bounded import codecs and golden fixtures; remove projection writers and writer barriers with no caller. This step does not stop legacy reads or mark the domain complete.

- [ ] **Step 3: Require a separate read-stop approval and archive malformed originals**

After at least one additional signed release qualification with writes disabled, require a second per-domain approval proving canonical reads, old-install upgrade, backup/restore, offline content, zero pending reconciliation, and no incident/P1 defect. Atomically copy every malformed/unmapped original record plus its SHA-256 and source-relative identity to `files/legacy-retirement-archive/<domain>/`, verify the copy, include that archive in backup rules, and retain import codecs/golden fixtures as evidence. If explicit-export pilot evidence is insufficient, remain `LEGACY_READ_ONLY` and keep legacy readers.

- [ ] **Step 4: Stop legacy reads and transition the domain to `COMPLETE`**

Only after the read-stop report is accepted, remove the production legacy reader registration and transactionally change the domain from `LEGACY_READ_ONLY` to `COMPLETE` with archive/report hashes. `RestoreReconciler` must treat a restored old file beside a `COMPLETE` domain as an import candidate into a new higher generation, never delete or silently ignore it.

- [ ] **Step 5: Prove old data upgrades and new data remains canonical**

Run: `.\gradlew.bat :core:storage:testDebugUnitTest :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.migration.LegacyWriteRetirementTest :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.backup.Phase3BackupBoundaryTest -Pwenku8Provider=public --console=plain`

- [ ] **Step 6: Commit**

```powershell
git add core/storage app/src/main/res/xml ..\..\docs\verification\legacy-write-retirement-report.yaml ..\..\docs\verification\legacy-read-retirement-report.yaml
git diff --check --cached
git commit -m "refactor(storage): retire legacy projections"
```

### Task 8: Prove The Final Compose Material 3 Product Surface

**Files:**
- Create: `app/src/androidTest/java/org/mewx/wenku8/accessibility/FinalProductJourneyTest.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/navigation/FinalAdaptiveRouteMatrixTest.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/navigation/{FinalRouteProcessDeathSeedTest,FinalRouteProcessDeathVerifyTest}.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/screenshot/FinalProductGoldenTest.kt`
- Modify: `app/build.gradle` and `verification-tools/build.gradle`
- Modify: `docs/verification/ui-golden-manifest.yaml`
- Modify: `docs/verification/manual-assistive-technology-manifest.yaml`
- Create: `docs/verification/manual-a11y-final.md`
- Create: `docs/verification/final-visual-review.md`

- [ ] **Step 1: Add RED route/state/owner assertions**

Require every A01-A15/F01-F06 and X01-X34 row, every final route, and loading/content/empty/error/offline/auth state where applicable. Assert compact/medium/expanded/compact-height, separating/occluding hinge, resize, light/dark/e-ink, zh-CN/zh-TW/zh-HK, font scale 2.0, IME, insets, in-process recreation, and API 36 predictive Back. Separate final seed/verify classes cover every top-level/subordinate route and representative overlay/scroll/input state across a changed PID after `MainActivity` becomes the direct host.

- [ ] **Step 2: Register final golden/manual-evidence tasks and run automated semantics**

Automated tests prove semantics plus keyboard/DPAD behavior; they do not claim an enabled TalkBack or Switch Access service. Register exact tasks:

```groovy
// app/build.gradle
registerUiGoldenTask('recordFinalUiGoldens', 'record', 8,
    'org.mewx.wenku8.screenshot.FinalProductGoldenTest')
registerUiGoldenTask('verifyFinalUiGoldens', 'verify', 8,
    'org.mewx.wenku8.screenshot.FinalProductGoldenTest')

// verification-tools/build.gradle
tasks.register('verifyFinalAssistiveEvidence', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyFinalAssistiveEvidence', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}
```

Add dispatcher `if (command == "verifyFinalAssistiveEvidence") { ManualAssistiveEvidenceVerifier.verifyFinal(projectRoot, docsRoot); return }`. Then rerun TalkBack and Switch Access on discover, search, detail/catalog, bookshelf/download, paginated/continuous reader, account/login, settings, community, wallpaper, About, and image viewer after the final host move. `manual-a11y-final.md` and the structured manifest bind every PASS to current source/app/test-APK/service/device/configuration/report hashes, tester, and a different reviewer. Any Phase 4-7 row not rerun against final source remains historical evidence and cannot satisfy this task.

- [ ] **Step 3: Record/approve/verify deterministic screenshots and staged process death**

Run automated classes without a cross-module instrumentation-property collision:

```powershell
.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.navigation.FinalAdaptiveRouteMatrixTest,org.mewx.wenku8.accessibility.FinalProductJourneyTest -Pwenku8Provider=public --console=plain
.\tools\verification\run-ui-process-death.ps1 -Phase 8 -Api 36 -SeedClass org.mewx.wenku8.navigation.FinalRouteProcessDeathSeedTest -VerifyClass org.mewx.wenku8.navigation.FinalRouteProcessDeathVerifyTest -Provider public
.\gradlew.bat :app:recordFinalUiGoldens -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain
$sourceCommit = (git rev-parse HEAD).Trim()
.\gradlew.bat :verification-tools:approveUiGoldens -Pphase=8 "-PuiGoldenReviewer=$env:WENKU8_UI_REVIEWER" "-PuiGoldenSourceCommit=$sourceCommit"
.\gradlew.bat :app:verifyFinalUiGoldens :verification-tools:verifyUiGoldenManifest :verification-tools:verifyFinalAssistiveEvidence -Pphase=8 -PuiGoldenSerial=$env:WENKU8_UI_GOLDEN_SERIAL -Pwenku8Provider=public --console=plain
```

Inspect for clipping, overlap, CJK orphaning, horizontal overflow, double scroll, hinge crossing, wrong state, IME occlusion, tiny targets, and incoherent focus. No unexplained tolerance/mask change passes.

- [ ] **Step 4: Commit reviewed UI evidence**

```powershell
git add app/src/androidTest ..\..\docs\verification\ui-goldens ..\..\docs\verification\ui-golden-manifest.yaml ..\..\docs\verification\manual-assistive-technology-manifest.yaml ..\..\docs\verification\manual-a11y-final.md ..\..\docs\verification\final-visual-review.md
git diff --check --cached
git commit -m "test(ui): prove final material product surface"
```

### Task 9: Close Lint, Coverage, Architecture, And Device Gates

**Files:**
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/release/DefinitionOfDoneVerifier.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Modify: `docs/verification/definition-of-done-matrix.yaml`
- Modify: `.github/workflows/android-ci.yml`

- [ ] **Step 1: Make current unexplained warnings and missing coverage RED**

The verifier reads Lint XML and coverage XML, rejects every active-source error/unexplained warning, and enforces parser/storage/migration 90/80, repository/use-case/ViewModel 80/70, overall production logic 70/60 line/branch thresholds.

Register `:verification-tools:verifyDefinitionOfDone` explicitly:

```groovy
tasks.register('verifyDefinitionOfDone', JavaExec) {
    group = 'verification'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'verifyDefinitionOfDone', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
}
```

Dispatch it explicitly:

```kotlin
if (command == "verifyDefinitionOfDone") {
    org.mewx.wenku8.verification.release.DefinitionOfDoneVerifier.verify(projectRoot, docsRoot)
    return
}
```

- [ ] **Step 2: Fix owning code or add narrow reviewed suppressions**

Every suppression records exact issue ID, file/symbol, reason, owner, expiry/recheck date, and test. No wildcard/baseline regeneration or threshold reduction passes.

- [ ] **Step 3: Run API 23/29/30/31/32/33/34/35/36 matrix**

CI covers install/launch, migration/backup, downloads and scheduler stop/reboot, navigation/process death, reader, permissions, accessibility smoke, and provider identity. API 23 is never replaced by an API below minSdk.

- [ ] **Step 4: Commit quality evidence**

Run: `.\gradlew.bat verifyPhase0Coverage :app:lintAlphaDebug verifyArchitecture :verification-tools:verifyDefinitionOfDone -Pwenku8Provider=public --console=plain`

Expected: PASS with current reports.

```powershell
git add verification-tools ..\..\docs\verification\definition-of-done-matrix.yaml ..\..\.github\workflows\android-ci.yml
git diff --check --cached
git commit -m "test(quality): close final quality gates"
```

### Task 10: Verify Licenses, Privacy, Egress, And Supply Chain

**Files:**
- Modify: `docs/licenses/source-asset-provenance.yaml`, `NOTICE`, and `SOURCE_OFFER.md`
- Modify: `docs/verification/outbound-network-manifest.yaml`
- Modify: `studio-android/LightNovelLibrary/gradle/verification-metadata.xml` and lockfiles
- Modify: public SBOM/provenance outputs and packaged `app/src/main/res/raw/notice.txt`

- [ ] **Step 1: Add RED packaged-diff checks**

Reject unknown/incompatible/missing source, dependency, plugin, font, image, fixture, or generated notice rows; unlocked/unverified/wrong-origin artifacts; analytics/ad/crash/AD_ID SDKs; unaudited origins; cleartext; sensitive logs/payloads; and APK/AAB entries absent from the ledger.

- [ ] **Step 2: Regenerate only from approved inputs**

Normalize SBOM timestamps/serials, update hashes for deleted files, generate notice/source offer, and keep public/private overlays separate. No Wild source/fixture or private endpoint enters public evidence.

- [ ] **Step 3: Run clean-cache online then verified offline build**

Run: `.\gradlew.bat :verification-tools:generateSbom :verification-tools:generateNotices :verification-tools:verifyPackagedLicenses :verification-tools:verifyOutboundManifest :verification-tools:verifySensitiveSource --write-locks -Pwenku8Provider=public --console=plain`

Then run with `--offline` and compare dependency graph and packaged-input hashes.

- [ ] **Step 4: Commit**

```powershell
git add build.gradle settings.gradle gradle app verification-tools ..\..\docs\licenses ..\..\docs\verification ..\..\NOTICE ..\..\SOURCE_OFFER.md
git diff --check --cached
git commit -m "compliance: close release provenance and privacy"
```

### Task 11: Add Baseline Profiles, R8, And External Signing Verification

**Files:**
- Create: `studio-android/LightNovelLibrary/baselineprofile/build.gradle`
- Create: `studio-android/LightNovelLibrary/baselineprofile/src/main/java/org/mewx/wenku8/baselineprofile/BaselineProfileGenerator.kt`
- Modify: `studio-android/LightNovelLibrary/settings.gradle`, `gradle/libs.versions.toml`, `app/build.gradle`, `app/proguard-rules.pro`
- Modify: `docs/verification/gradle-task-contract.yaml`
- Create: `studio-android/LightNovelLibrary/verification/verify-release-evidence.ps1`

- [ ] **Step 1: Write RED profile/R8/signature checks**

Require startup plus discover/detail/reader/account/settings critical paths, no legacy/provider-private class in public mapping/seeds, installable minified artifacts, deterministic provider ID, external signing certificate allowlist, `apksigner verify`, and bundletool universal APK launch. A Gradle TestKit fixture initially asserts that `:app:generateAlphaReleaseBaselineProfile` is missing, and that no hand-written task or `:baselineprofile:generateBaselineProfile` alias is allowed to satisfy the check.

- [ ] **Step 2: Pin and configure the consumer and producer plugins**

Pin the official Google Maven plugin marker and its aligned benchmark runtime, plus the current stable UI Automator/ProfileInstaller coordinates, in the existing catalog:

```toml
[versions]
baselineprofile = "1.5.0-alpha07"
uiautomator = "2.4.0"
profileinstaller = "1.4.1"

[plugins]
android-test = { id = "com.android.test", version.ref = "agp" }
androidx-baselineprofile = { id = "androidx.baselineprofile", version.ref = "baselineprofile" }

[libraries]
androidx-benchmark-macro-junit4 = { module = "androidx.benchmark:benchmark-macro-junit4", version.ref = "baselineprofile" }
androidx-test-uiautomator = { module = "androidx.test.uiautomator:uiautomator", version.ref = "uiautomator" }
androidx-profileinstaller = { module = "androidx.profileinstaller:profileinstaller", version.ref = "profileinstaller" }
```

Add one exact project descriptor:

```groovy
// settings.gradle
include ':baselineprofile'
project(':baselineprofile').projectDir = file('baselineprofile')
```

Extend the app's single leading `plugins` block established by the Phase 0/1 plugin-catalog migration; do not append a second `plugins` block after statements and do not restore legacy `apply plugin` declarations. Apply the consumer plugin to `:app` and scope generation to the public alpha release path. The reviewed profile is merged into `src/main/generated/baselineProfiles` so the identical code profile is packaged by all channels; generation never runs implicitly during a release build:

```groovy
// app/build.gradle
plugins {
    alias(libs.plugins.androidx.baselineprofile)
}

baselineProfile {
    variants {
        alphaRelease {
            from(project(':baselineprofile'))
            saveInSrc = true
            mergeIntoMain = true
            automaticGenerationDuringBuild = false
        }
    }
}

dependencies {
    implementation libs.androidx.profileinstaller
}
```

Create the producer as an AGP 9 built-in-Kotlin Android test module; do not apply `org.jetbrains.kotlin.android` or add `kotlinOptions`:

```groovy
// baselineprofile/build.gradle
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace 'org.mewx.wenku8.baselineprofile'
    compileSdkVersion rootProject.compileSdkVersion
    targetProjectPath = ':app'

    defaultConfig {
        minSdkVersion 28
        targetSdkVersion rootProject.targetSdkVersion
        testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'
    }
    flavorDimensions 'default'
    productFlavors {
        alpha { dimension 'default' }
    }
    testOptions.managedDevices.devices {
        pixel6Api33(com.android.build.api.dsl.ManagedVirtualDevice) {
            device = 'Pixel 6'
            apiLevel = 33
            systemImageSource = 'aosp'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

baselineProfile {
    managedDevices = ['pixel6Api33']
    useConnectedDevices = false
}

dependencies {
    implementation libs.androidx.benchmark.macro.junit4
    implementation libs.androidx.test.uiautomator
    implementation libs.androidx.test.ext.junit
}
```

`gradle-task-contract.yaml` allowlists exactly `:app:generateAlphaReleaseBaselineProfile` as generated by plugin ID `androidx.baselineprofile`, marker `androidx.baselineprofile:androidx.baselineprofile.gradle.plugin:1.5.0-alpha07`, on the consumer project `:app`, with producer `:baselineprofile`, AGP `9.0.1`, managed device `pixel6Api33`, and output `app/src/main/generated/baselineProfiles`. The producer module does not generate a task named `:baselineprofile:generateBaselineProfile`; adding such an alias fails `verifyPlannedGradleTasks`.

- [ ] **Step 3: Generate and merge the reviewed profile**

Use `BaselineProfileRule` plus UI Automator only for deterministic fixture-mode startup, discover, detail, reader, account-shell, and settings journeys. The generator waits for each stable route marker, never signs in or embeds user/content values, and resets app data between independent journeys. Check in generated profile text only; no account value, content value, URL, endpoint, screenshot, device file, or report payload appears.

Run the plugin-generated consumer task and then prove the exact task provenance:

```powershell
.\gradlew.bat :app:generateAlphaReleaseBaselineProfile :verification-tools:verifyPlannedGradleTasks -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Expected: the managed API 33 device runs the `alphaNonMinifiedRelease` producer test, the consumer writes one reviewed main profile, and the task-contract verifier confirms plugin ownership/version. If the current alpha distribution authorization is not `ACCEPTED`, the run is correctly blocked and no profile is claimed current.

- [ ] **Step 4: Verify unsigned then externally signed artifacts**

The repository creates unsigned release candidates. The external pipeline injects keys outside the workspace, signs once, and returns artifact hash, certificate digest, mapping hash, provider ID, channel, approval revision, and verification report. The script accepts paths through parameters and never accepts key/password values.

- [ ] **Step 5: Run and commit public build configuration**

Run: `.\gradlew.bat :app:generateAlphaReleaseBaselineProfile :app:assembleAlphaRelease :app:bundlePlaystoreRelease :verification-tools:verifyPlannedGradleTasks -Pwenku8Provider=public --console=plain --stacktrace --no-parallel`

```powershell
git add settings.gradle gradle/libs.versions.toml app/build.gradle app/proguard-rules.pro app/src/main/generated/baselineProfiles baselineprofile verification/verify-release-evidence.ps1 ../../docs/verification/gradle-task-contract.yaml
git diff --check --cached
git commit -m "build(release): add profiled minified artifacts"
```

### Task 12: Prove Reproducibility And Public/Private/Channel Gates

**Files:**
- Create: `studio-android/LightNovelLibrary/verification/reproducible-release.ps1`
- Create: `docs/verification/phase-8-release-matrix.yaml`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/release/{ReleaseEvidenceVerifier,ReproducibilityVerifier}.kt`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`

- [ ] **Step 1: Write RED two-root reproducibility tests**

Build from two clean differently named roots with pinned JDK/SDK/NDK/Gradle, normalized environment/time/locale, verified offline dependencies, and identical source. Compare unsigned APK/AAB contents, packaged inputs, mapping, baseline profile, SBOM, and provenance using documented normalized fields.

- [ ] **Step 2: Encode fail-closed channel/provider rows**

Every alpha/Baidu/Playstore and public/private row records authorization decision/recheck/expiry, source commit, provider ID, artifact/certificate/mapping/SBOM hashes, device/API suite, egress/privacy/license results, and private attestation binding when applicable. `UNKNOWN`, `EXPIRED`, `REJECTED`, or missing evidence is `BLOCKED`, never `PASS`.

Register both tasks explicitly in `verification-tools/build.gradle`:

```groovy
['verifyReleaseEvidence', 'verifyReproducibility'].each { command ->
    tasks.register(command, JavaExec) {
        group = 'verification'
        classpath = sourceSets.main.runtimeClasspath
        mainClass = application.mainClass
        args command, rootProject.projectDir.absolutePath,
            new File(rootProject.projectDir, '../../docs').canonicalPath
    }
}
```

Add both dispatcher branches:

```kotlin
if (command == "verifyReleaseEvidence") {
    org.mewx.wenku8.verification.release.ReleaseEvidenceVerifier.verify(projectRoot, docsRoot)
    return
}
if (command == "verifyReproducibility") {
    org.mewx.wenku8.verification.release.ReproducibilityVerifier.verify(projectRoot, docsRoot)
    return
}
```

- [ ] **Step 3: Run public and protected matrices**

Run public: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\reproducible-release.ps1 -Provider public`

Then run `.\gradlew.bat :verification-tools:verifyReproducibility :verification-tools:verifyReleaseEvidence -Pwenku8Provider=public --console=plain`; expected PASS only for matrix rows with current artifact and authorization evidence.

Run private only in protected CI with fresh nonce/trusted key and no public report leakage. Live tests use environment-only credentials and remain opt-in; mutation tests require their separate confirmation gate.

- [ ] **Step 4: Commit only public/redacted evidence**

```powershell
git add verification verification-tools ..\..\docs\verification\phase-8-release-matrix.yaml
git diff --check --cached
git commit -m "test(release): prove provider and channel gates"
```

### Task 13: Verify Emergency Rollback Without Restoring Legacy Source

**Files:**
- Create: `docs/verification/emergency-rollback-runbook.md`
- Create: `studio-android/LightNovelLibrary/verification/emergency-rollback-drill.ps1`
- Test: `app/src/androidTest/java/org/mewx/wenku8/rollback/EmergencyRollbackDrillTest.kt`

- [ ] **Step 1: Write RED forward-fix and previous-release drills**

Cover provider capability disable, route/action disable through server-independent packaged policy only where already designed, session purge, work cancellation, database forward fix, installation of an approved emergency artifact rebuilt from the recorded pre-retirement commit with a higher version code during the supported window, and installation of the next current forward fix without data loss. Never rely on Android package downgrade.

- [ ] **Step 2: Define decision authority and immutable evidence**

The runbook names trigger, owner, approvers, artifact allowlist, data expectations, communication/channel constraints, and recovery verification. It never instructs enabling cleartext, bypassing HostPolicy/signature/authorization, restoring deleted legacy source at runtime, or downgrading Room destructively.

- [ ] **Step 3: Run controlled emulator/device drill**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\emergency-rollback-drill.ps1 -Provider public`

Expected: PASS with old data readable where supported, current upgrade successful, session signed out when keys cannot restore, and no canonical/download loss.

- [ ] **Step 4: Commit**

```powershell
git add verification/emergency-rollback-drill.ps1 app/src/androidTest/java/org/mewx/wenku8/rollback ..\..\docs\verification\emergency-rollback-runbook.md
git diff --check --cached
git commit -m "docs(release): verify emergency rollback"
```

### Task 14: Run The Complete Definition Of Done And Independent Final Audit

**Files:**
- Modify: `docs/verification/definition-of-done-matrix.yaml`
- Create: `docs/verification/final-independent-audit.md`
- Create: `studio-android/LightNovelLibrary/verification/verify-final-architecture.ps1`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Modify: `README.md`

- [ ] **Step 1: Make every Section 16 row machine-verifiable**

Each row links exact source, task, test, provider, variant, API/device, fixture/baseline, report, artifact hash, source commit, reviewer, and current status. The verifier rejects missing files, stale commits/hashes, duplicate evidence standing in for unrelated claims, blocked rows marked pass, and any open Critical/Important finding.

Register the sole Phase 8 aggregate path in `verification-tools/build.gradle`:

```groovy
tasks.register('phase8Gate', JavaExec) {
    group = 'verification'
    description = 'Canonical Phase 8 definition-of-done gate.'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'phase8Gate', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
    dependsOn 'verifyRetirementManifest', 'verifyFinalReachability',
        'verifyFinalUiTechnology', 'verifyFinalAssistiveEvidence',
        'verifyUiGoldenManifest', 'verifyXmlSurfaceLedger',
        'verifyPlannedGradleTasks', ':app:verifyFinalUiGoldens',
        'verifyDefinitionOfDone', 'verifyReleaseEvidence', 'verifyReproducibility'
}
```

Add the dispatcher branch:

```kotlin
if (command == "phase8Gate") {
    org.mewx.wenku8.verification.release.DefinitionOfDoneVerifier.verifyPhase8(projectRoot, docsRoot)
    return
}
```

Do not create a root `phase8Gate` or `:app:phase8Gate` alias; all plan/workflow/evidence rows call `:verification-tools:phase8Gate`.

- [ ] **Step 2: Run the complete deterministic program gate**

```powershell
.\gradlew.bat clean :phase0Gate :verification-tools:phase1Gate :verification-tools:phase2Gate :verification-tools:phase3Gate :app:phase4LibraryGate :app:phase5NovelGate :app:phase6ReaderGate :app:phase7AccountSecondaryGate :api-contract:test :api-public:test :api-contract-tests:test :core:model:test :core:domain:test :core:network:test :core:storage:testDebugUnitTest :core:data:testDebugUnitTest :feature:library:testDebugUnitTest :feature:novel:testDebugUnitTest :feature:reader:testDebugUnitTest :feature:account:testDebugUnitTest :feature:settings:testDebugUnitTest :app:testAlphaDebugUnitTest :app:lintAlphaDebug verifyPhase0Coverage verifyArchitecture :verification-tools:verifyOutboundManifest :verification-tools:verifySensitiveSource :app:assembleAlphaDebug :app:assembleAlphaRelease :verification-tools:phase8Gate -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
```

Run the controlled instrumentation/screenshot/API 23-36/provider/private/live/channel gates only when their prerequisites apply. A blocked distribution row does not invalidate internal deterministic implementation, but it prevents a release/completion claim for that scope.

- [ ] **Step 3: Commission four independent reviews**

Architecture/API/clean-room; migration/storage/background/compatibility; Compose MD3/navigation/accessibility/visual; and release/licensing/supply-chain/privacy reviewers inspect current source and evidence independently. Resolve every Critical/Important finding with a new RED/GREEN commit and re-review.

- [ ] **Step 4: Write the final audit and update README truthfully**

Report measured Kotlin/source ownership, test/coverage/lint, final Compose MD3 reachability, architecture graph, provider operation/capability status, migration compatibility, release/channel blocks, artifact hashes, and residual risks. Do not describe blocked live/distribution evidence as passing.

- [ ] **Step 5: Commit the final evidence set**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\verify-final-architecture.ps1`

Expected: `PHASE8-DEFINITION-OF-DONE-PASS` only when every applicable row passes and every blocked row is accurately scoped.

```powershell
git add README.md verification verification-tools/build.gradle verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt ..\..\docs\verification\definition-of-done-matrix.yaml ..\..\docs\verification\final-independent-audit.md
git diff --check --cached
git commit -m "docs(audit): prove modernization definition of done"
```

## Phase 8 Completion Checklist

- [ ] One Activity and one Navigation Compose host own every reachable route.
- [ ] Reachable UI has zero Fragment page, page XML, AsyncTask, raw executor/thread owner, `findViewById`, direct GlobalConfig, and legacy API ABI import.
- [ ] Public provider and protected private provider pass the same typed contract without graph leakage.
- [ ] Old installs, interrupted migration, backup/restore, offline reading, and the approved rollback window lose no bookshelf, progress, settings, chapter, or image data.
- [ ] Legacy dual-write ended only from accepted compatibility-window evidence.
- [ ] Every reachable page/action is native Compose Material 3 with adaptive, state, localization, screenshot, and assistive-technology evidence.
- [ ] Lint, coverage, architecture, API 23-36, provider, privacy/egress, license/SBOM/provenance, dependency, R8/profile, reproducibility, and signing gates are current.
- [ ] Unknown/expired/rejected site, content, channel, license, signing, or private-attestation scope remains fail-closed and is not claimed complete.
- [ ] No Critical or Important independent finding remains open.
- [ ] `docs/verification/definition-of-done-matrix.yaml` and `final-independent-audit.md` describe the current source commit and artifacts exactly.
