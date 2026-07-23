# Wenku8 Modernization Program Implementation Plan Index

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Coordinate nine independently executable phase plans that turn the current legacy-centered Android app into the complete clean-room public-provider, modular Kotlin, single-activity Compose Material 3 application defined by the approved specification.

**Architecture:** The program is a vertical-slice strangler migration. Phase plans share one Gradle/module/package map, preserve legacy ABI and data boundaries until their evidence gates pass, and move one set of user journeys at a time behind typed domain contracts. Public and protected-private provider graphs remain explicit, while every reachable final page converges on Compose Material 3.

**Tech Stack:** Kotlin, Groovy Gradle, Android Gradle Plugin, Jetpack Compose Material 3, Navigation Compose, Lifecycle/ViewModel/StateFlow, Kotlin coroutines, Room, DataStore, WorkManager/JobScheduler, OkHttp, Jsoup, JUnit4, AndroidX Test, MockWebServer, screenshot testing, PowerShell/Gradle/ADB verification.

---

## Authoritative Inputs

1. Design specification: `docs/superpowers/specs/2026-07-10-wenku8-modernization-program-design.md` at commit `3c8fa142` or a reviewed successor.
2. Active Android project: `studio-android/LightNovelLibrary/`.
3. Historical direction files under `docs/` are evidence inputs, not authority where they conflict with the design specification.
4. Existing `api/` content is protected/private input. Public plans never require it to be present and never disclose its endpoints, coordinates, configuration, or response data.
5. Wild is behavior-only evidence. Provider implementers must not read, copy, translate, or mechanically reproduce Wild code, selectors, fixtures, comments, tests, messages, or distinctive flow.

## Execution Root And Shell

Run Android/Gradle commands from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android\studio-android\LightNovelLibrary'
```

Run repository checks from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android'
```

Use PowerShell syntax and `.\gradlew.bat`. Never put a username, password, Cookie, captcha, private endpoint, signing secret, or protected overlay value in a command, Gradle property, fixture, screenshot, log, or commit.

## Program Plan Set

| Order | Plan | Working software produced | Hard dependency |
| ---: | --- | --- | --- |
| 0 | `2026-07-10-wenku8-phase-0-baseline-contracts.md` | Deterministic baseline, compatibility manifests, fail-closed compliance/supply-chain/privacy gates | Approved specification |
| 1 | `2026-07-10-wenku8-phase-1-architecture-md3-foundation.md` | Modular typed foundation, AppContainer, settings migration subset, adaptive MD3 shell | Phase 0 exit |
| 2 | `2026-07-10-wenku8-phase-2-public-provider.md` | Complete clean-room public provider and non-throwing legacy bridge | Phase 0 authorization scopes and Phase 1 contracts |
| 3 | `2026-07-10-wenku8-phase-3-storage-migration.md` | Canonical stores, remaining legacy migration, durable background work | Phase 1 storage contracts and Phase 2 session contract |
| 4 | `2026-07-10-wenku8-phase-4-library-shell.md` | Default adaptive shell plus discovery/search/bookshelf/download/history routes | Phases 1-3 |
| 5 | `2026-07-10-wenku8-phase-5-novel-community.md` | Detail/catalog/download/review routes and removal of NovelInfoActivity orchestration | Phase 4 shell and repositories |
| 6 | `2026-07-10-wenku8-phase-6-reader-consolidation.md` | ViewModel-owned paginated/continuous reader parity and retired V1/vertical entries | Phase 5 catalog/download behavior |
| 7 | `2026-07-10-wenku8-phase-7-account-secondary.md` | Account/settings/wallpaper/about/image routes; every remaining page is Compose MD3 | Phases 2, 4, and 6 |
| 8 | `2026-07-10-wenku8-phase-8-legacy-removal-release.md` | Removed unreachable legacy UI/ABI, signed release evidence, retirement audit | All prior exit gates and compatibility window |

No phase may satisfy its gate using a later phase's intended work. A phase may stop at a named external prerequisite without weakening that prerequisite.

## Stable Gradle Project And Directory Map

All new Android/JVM modules use Groovy `build.gradle` files so the migration does not combine architecture work with a Kotlin-DSL conversion.

| Gradle path | Directory | Namespace/package root | Owner |
| --- | --- | --- | --- |
| `:app` | `app/` | `org.mewx.wenku8` | Manifest, composition root, Navigation host, compatibility trampolines |
| `:api-contract` | `api-contract/` | `org.mewx.wenku8.api.contract` | Provider interfaces, request/result/failure types |
| `:api-public` | `api-public/` | `org.mewx.wenku8.api.publicprovider` | Clean-room HTTPS transport and parsers |
| `:api-contract-tests` | `api-contract-tests/` | `org.mewx.wenku8.api.contract.testing` | Reusable provider contract suite |
| logical `:api` in public builds | `api-legacy-bridge/` | existing legacy API packages | Frozen legacy ABI adapter over `:api-public` |
| logical `:api` in private builds | existing protected `api/` | protected | Existing private ABI, never copied into public files |
| `:api-private-adapter` | `api-private-adapter/` | `org.mewx.wenku8.api.privateadapter` | Typed adapter loaded only with protected source |
| `:core:model` | `core/model/` | `org.mewx.wenku8.core.model` | Immutable stable identifiers/models |
| `:core:domain` | `core/domain/` | `org.mewx.wenku8.core.domain` | Repository interfaces and use cases |
| `:core:network` | `core/network/` | `org.mewx.wenku8.core.network` | Audited HTTP factories, host policy, encoding, redaction |
| `:core:session-contract` | `core/session-contract/` | `org.mewx.wenku8.core.session` | Session epoch/store interfaces without provider cycles |
| `:core:storage` | `core/storage/` | `org.mewx.wenku8.core.storage` | Room, DataStore, SessionStore, legacy codecs/migration |
| `:core:data` | `core/data/` | `org.mewx.wenku8.core.data` | Repository implementations/cache/offline policy |
| `:core:designsystem` | `core/designsystem/` | `org.mewx.wenku8.core.designsystem` | MD3 theme/tokens/adaptive/shared composables |
| `:core:testing` | `core/testing/` | `org.mewx.wenku8.core.testing` | Fakes, clocks, dispatchers, fixtures, screenshot setup |
| `:feature:library` | `feature/library/` | `org.mewx.wenku8.feature.library` | Discover/search/bookshelf/download/history routes |
| `:feature:novel` | `feature/novel/` | `org.mewx.wenku8.feature.novel` | Detail/catalog/community/download routes |
| `:feature:reader` | `feature/reader/` | `org.mewx.wenku8.feature.reader` | Final reader route/ViewModel/paging/layout |
| `:feature:account` | `feature/account/` | `org.mewx.wenku8.feature.account` | Login/profile/session/check-in-capability routes |
| `:feature:settings` | `feature/settings/` | `org.mewx.wenku8.feature.settings` | App/reader settings, cache, migration, wallpaper, about, image viewer |

Gradle project identity is unique. Public `settings.gradle` maps only logical `:api` to `api-legacy-bridge/`; it never also includes `:legacy-api-bridge`. Protected private mode maps only logical `:api` to `api/` and adds `:api-private-adapter`.

This table describes the post-Phase-2 steady state. Phase 0 and Phase 1 deliberately retain the existing public `api-stub/` as the sole logical `:api` mapping while contracts, module boundaries, and zero-egress behavior are established. Phase 2 performs the reviewed one-time replacement to `api-legacy-bridge/`; no earlier phase may point the public graph at the bridge or make the stub perform network work.

Phase 8 removes `api-legacy-bridge/` and the public logical `:api` descriptor only after zero production imports and the approved compatibility window. The final public graph uses `:api-contract` and `:api-public` directly through `:core:data`; a protected private graph may still add its isolated logical private `:api` plus `:api-private-adapter` inside attested CI.

Source-root convention is deliberate: pure Kotlin/JVM modules (`api-contract`, `api-public`, `api-contract-tests`, `core/model`, `core/domain`, `core/network`, and `core/session-contract`) use `src/main/kotlin` and `src/test/kotlin`; Android library/feature modules and the existing app use the repository's current `src/main/java` / `src/test/java` / `src/androidTest/java` roots even though their production code is Kotlin. Plans must not move files solely to normalize source-root names.

## Aggregate Gate Task Contract

These are the only cross-phase aggregate task paths. The phase that first owns a task must register it in the exact project shown; compatibility aliases exist only for already-written prerequisite commands and depend on the canonical task rather than duplicating verification logic.

| Phase | Canonical aggregate task | Required compatibility alias |
| ---: | --- | --- |
| 0 | `:verification-tools:phase0Gate` with root `phase0Gate` forwarding to it | `:app:phase0CompatibilityGate` -> `:phase0Gate` |
| 1 | `:verification-tools:phase1Gate` | `:app:phase1ExitGate` -> `:verifyArchitecture` |
| 2 | `:verification-tools:phase2Gate` | `:app:phase2ExitGate` -> `:verification-tools:phase2Gate` |
| 3 | `:verification-tools:phase3Gate` | none |
| 4 | `:app:phase4LibraryGate` | none |
| 5 | `:app:phase5NovelGate` | none |
| 6 | `:app:phase6ReaderGate` | none |
| 7 | `:app:phase7AccountSecondaryGate` | none |
| 8 | `:verification-tools:phase8Gate` | none |

New plan text calls the canonical paths. Phase 0 and Phase 1 register their compatibility aliases explicitly, and Phase 2 owns `:app:phase2ExitGate`. Phase 3 deliberately has no app alias. Removing an alias requires first updating every retained plan, workflow, and evidence row that invokes it.

### Executable Verification Task Registry

Every non-AGP/non-Kotlin Gradle task referenced by a plan, script, workflow, evidence row, or `dependsOn` has one literal registration owner and, for `JavaExec`, one literal `VerificationMain.kt` dispatcher branch. `docs/verification/gradle-task-contract.yaml` is generated from the registrations below and records task path, owning phase/file, task type or generating plugin plus pinned version, dispatcher command, consumers, and retirement owner. `:verification-tools:verifyPlannedGradleTasks` parses all nine phase plans and fails on an unregistered custom task, a wrong project path, a duplicate registration, a missing dispatcher, or a plugin-generated task absent from the exact plugin/version allowlist.

| First owner | Exact task paths registered by that owner |
| --- | --- |
| Phase 0 | `:verification-tools:{generateInventories,verifyInventories,verifyWarnings,verifySensitiveSource,generateSbom,generateNotices,verifyPackagedLicenses,verifyOutboundManifest,verifyReproducibleInputs,verifyPrivateAttestationFixtures,verifyAcceptedLiveScope,verifyIntentContracts,verifyAuthorization,verifyPhase0Coverage,resolvedOriginReport,recordPackagedInputs,verifyPrivateAttestation,verifySharedAttestationNonDisclosure,verifyPlannedGradleTasks,verifyXmlSurfaceLedger,phase0Gate}`; root `:{phase0Gate,phase0CoverageReport,verifyPhase0Coverage,verifyPrivateAttestation}`; and `:app:{phase0CompatibilityGate,livePublicReadOnlySmoke,livePublicReversibleBookshelfSmoke,livePublicPersistentMutationSmoke}` |
| Phase 1 | `:verification-tools:{verifyUiGoldenManifest,approveUiGoldens,verifySettingsWriters,verifySettingsMigrationEvidence,phase1Gate}`, root `:verifyArchitecture`, and `:app:{recordPhase1UiGoldens,verifyPhase1UiGoldens,phase1ExitGate}` |
| Phase 2 | `:verification-tools:{verifyCleanRoomEvidence,draftCleanRoomAttestation,signCleanRoomAttestation,collectPhase2Evidence,phase2Gate}`, root `:verifySelectedProvider`, `:api-public:liveReadOnlySmoke`, and `:app:phase2ExitGate` |
| Phase 3 | `:verification-tools:{verifyPhase3WriterRouting,verifyPhase3MatrixStructure,phase3Gate}` |
| Phase 4 | `:app:{recordPhase4UiGoldens,verifyPhase4UiGoldens,verifyPhase4LibraryPlan,phase4LibraryGate}` and `:verification-tools:verifyPhase4AssistiveEvidence` |
| Phase 5 | `:app:{recordPhase5UiGoldens,verifyPhase5UiGoldens,verifyPhase5ActiveOrchestration,verifyPhase5NovelPlan,phase5NovelGate}` and `:verification-tools:verifyPhase5AssistiveEvidence` |
| Phase 6 | `:app:{recordPhase6UiGoldens,verifyPhase6UiGoldens,verifyPhase6ReaderRetirementPlan,phase6ReaderGate}` and `:verification-tools:{verifyPhase6AssistiveEvidence,verifyPhase6ReaderEvidence}` |
| Phase 7 | `:app:{recordPhase7UiGoldens,verifyPhase7UiGoldens,verifyPhase7AccountSecondaryPlan,phase7AccountSecondaryGate}` and `:verification-tools:{verifyReachablePages,verifyPhase7AssistiveEvidence}` |
| Phase 8 | `:app:{recordFinalUiGoldens,verifyFinalUiGoldens}`, `:verification-tools:{verifyRetirementManifest,verifyFinalReachability,verifyFinalUiTechnology,verifyFinalAssistiveEvidence,verifyDefinitionOfDone,verifyReleaseEvidence,verifyReproducibility,phase8Gate}` |

AGP-generated tasks such as `assembleAlphaDebug`, `assembleAlphaCompatProbe`, `assembleAlphaCompatProbeAndroidTest`, `connectedAlphaDebugAndroidTest`, `processAlphaReleaseMainManifest`, and `lintAlphaDebug` are accepted only because `gradle-task-contract.yaml` binds them to AGP application plugin `9.0.1` and a declared variant. Gradle application-plugin task `:verification-tools:installDist` is bound to Gradle `9.1.0`. Phase 8 binds plugin-generated `:app:generateAlphaReleaseBaselineProfile` to `androidx.baselineprofile:androidx.baselineprofile.gradle.plugin:1.5.0-alpha07`, consumer `:app`, and producer `:baselineprofile`; the producer does not own a `:baselineprofile:generateBaselineProfile` task, and a hand-written alias is forbidden. Names inferred from prose are never accepted.

### AndroidX Instrumentation Golden Pipeline

The sole screenshot implementation is the project-owned AndroidX Compose UI instrumentation pipeline: Compose BOM `2026.06.00`, `androidx.compose.ui:ui-test-junit4` from that BOM, AndroidX Test runner `1.7.0`, and AndroidX Test ext JUnit `1.3.0`, all version-catalogued, dependency-verified, and locked. Do not add Roborazzi, Paparazzi, Dropshots, Shot, or a second baseline format.

- All route goldens run through the `:app` instrumentation APK so they exercise the real `MainActivity`/transitional shell, feature composition, window/inset adapters, and navigation host. Feature composables receive deterministic fakes through the app test composition root.
- `UiGoldenCapture` uses Compose `captureToImage()`, writes lossless ARGB PNG plus canonical JSON to `context.getExternalFilesDir("ui-goldens/<run-id>")`, and rejects a zero-size, single-color/blank, non-idle, duplicate-ID, or unregistered capture.
- `tools/verification/run-androidx-ui-goldens.ps1` accepts only `record` or `verify`, one phase, one exact test class, and one emulator serial supplied as `-PuiGoldenSerial` or `WENKU8_UI_GOLDEN_SERIAL`. It resolves APKs from AGP `output-metadata.json`, verifies the serial is an emulator at the required API/configuration, installs app/test APKs, runs `adb shell am instrument` with `-e class` and `-e wenku8ScreenshotMode`, pulls the exact run directory, validates `cases.json`, and removes device output in `finally`. No Gradle instrumentation property is reused across modules.
- `recordPhase*UiGoldens` writes candidates only under `app/build/reports/ui-goldens/<phase>/candidate/`; it never changes an approved baseline or manifest. After original-pixel review, `:verification-tools:approveUiGoldens -Pphase=<n> -PuiGoldenReviewer=<id> -PuiGoldenSourceCommit=<40-hex>` copies exact candidates to `docs/verification/ui-goldens/<case-id>.png` and writes fixture/image SHA-256, reviewer, source commit, and zero/specified masks. It rejects a dirty source commit, self-approval where independence is required, sentinel hashes, or missing review report.
- `verifyPhase*UiGoldens` captures fresh actuals under `app/build/reports/ui-goldens/<phase>/actual/` and compares decoded pixels/structure against approved baselines. `verifyUiGoldenManifest` separately enforces required cases, hashes, tolerances, masks, source ownership, and approval. Verify tasks never update files.

### Separate-Process UI Restoration Contract

`tools/verification/run-ui-process-death.ps1` is the sole route process-death harness. It validates an emulator/API, installs exact app/test APK hashes, runs a `seed` instrumentation invocation, records the target PID and non-secret expected-state hash, backgrounds the task, executes `adb shell am kill org.mewx.wenku8`, proves the PID disappears, restores the retained task, and runs a new `verify` instrumentation invocation. The verifier requires a different PID, the same route/query/selection/scroll or reader cursor/overlay logical intent as applicable, no duplicate effect/request, and a retained report bound to source/APK/test-APK hashes. `ActivityScenario.recreate`, `StateRestorationTester`, rotation, and a single instrumentation process remain useful component tests but are not process-death evidence.

### Final UI Technology And Assistive Evidence

The final UI gate parses reachable Kotlin with Kotlin PSI and the release dependency graph. It rejects Material 2 component imports/calls (while allowing only `androidx.compose.material.icons.*`), `com.google.android.material.*`, AppCompat/ViewBinding/DataBinding, `AndroidView`/`AndroidViewBinding`, XML inflation/page APIs, and a Material 3 `Card`/`ElevatedCard`/`OutlinedCard` nested in another card content lambda. It joins `xml-surface-ledger.yaml` and requires every `X01`-`X34` row to be replaced or retired before final completion.

TalkBack and Switch Access are not claimed by Compose semantics tests. Each phase retains manual journeys in `docs/verification/manual-assistive-technology-manifest.yaml`; Phase 8 reruns the complete final set after `MainActivity` becomes the direct host. Every PASS row contains route/journey ID, assistive service/package/version, device/API/build fingerprint, locale/theme/font/navigation/posture, source and APK hashes, tester, independent reviewer, UTC execution time, report path/hash, and no user content. The phase-specific verifier and final `verifyFinalAssistiveEvidence` reject missing, stale, self-approved, unhashed, or non-PASS rows.

## Dependency Direction

```text
feature/* -> core/domain + core/model + core/designsystem
core/data -> core/domain + core/storage + api-contract + core/network + core/session-contract
api-public -> api-contract + core/model + core/network + core/session-contract
api-private-adapter -> api-contract + logical :api
core/storage -> core/model + core/session-contract
core/network -> core/model + core/session-contract
app -> feature entry APIs + composition implementations
```

Forbidden directions are enforced with tests: features do not import provider/network/storage implementations; core model/domain do not import Android UI; `api-public` does not import app/features; private source does not enter public artifacts or evidence.

## Cross-Phase Test Rhythm

Every implementation task follows this exact order:

1. Add one focused failing test or verifier fixture.
2. Run the narrow command and record the expected failure reason.
3. Add the smallest production/configuration change that satisfies the test.
4. Re-run the focused command and observe a pass.
5. Run the affected module suite plus `git diff --check`.
6. Commit only that task's files with the message specified by its phase plan.

Do not combine a red test with unrelated refactoring. Do not change a golden baseline to make an unexplained failure green.

## Common Commands

Current baseline contract test (phase plans replace this with their exact focused task):

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.AppMigrationContractTest" --console=plain --stacktrace --no-parallel
```

Public verification baseline:

```powershell
.\gradlew.bat -Pwenku8Provider=public :app:assembleAlphaDebug :app:testAlphaDebugUnitTest lintAlphaDebug --console=plain --stacktrace --no-parallel
```

Minified public selection gate:

```powershell
.\gradlew.bat -Pwenku8Provider=public :app:assembleAlphaRelease --console=plain --stacktrace --no-parallel
```

Unknown/private-without-source configuration gate:

```powershell
.\gradlew.bat -Pwenku8Provider=unknown projects --console=plain --stacktrace
.\gradlew.bat -Pwenku8Provider=private projects --console=plain --stacktrace
```

Expected: both commands fail with redacted provider-selection messages in a public checkout.

Repository integrity:

```powershell
git diff --check
git status --short
```

## Non-Negotiable Stop Conditions

- Phase 2 live observation, account tests, endpoint publication outside the controlled workspace, and distributable public-provider artifacts stop unless the exact site/content/channel authorization scope is current and `ACCEPTED`.
- Daily check-in remains absent from reachable public UI until an independently accepted HTTPS operation contract exists. No cleartext exception is allowed.
- A password, Cookie, captcha, account response, private endpoint, signing material, or protected overlay value stops the task if it enters source, command output, fixtures, logs, screenshots, reports, or commits.
- A production `UNKNOWN`/missing/incompatible license stops signed/distributable work.
- A migration task stops before legacy retirement if any record is lost, a journal cannot reconcile, or old-signed/minified compatibility evidence fails.
- A route does not become default until behavior, state, accessibility, adaptive, screenshot, and rollback gates for that route pass.

## Subagent Review Protocol

Each task is implemented by a fresh worker. Before the task commit is accepted:

1. A specification reviewer checks exact requirement coverage and scope.
2. A code-quality reviewer checks behavior, architecture, tests, security/privacy, and maintainability.
3. The root agent independently reads the diff and runs the task's verification commands.

Phase reviews are independent by domain:

- Architecture/API/clean-room and public/private graph.
- Migration/storage/background work/compatibility.
- Compose Material 3/navigation/adaptive/accessibility/visual QA.
- Build/release/licensing/supply-chain/privacy evidence.

Any Critical or Important finding returns the plan/task to revision. Agent summaries alone are never completion evidence.

## Program Completion Evidence

Phase 8 may claim completion only when `docs/verification/modernization-matrix.yaml` maps every Definition-of-Done row to a current authoritative task/test/report/artifact hash, all nine phase exit gates pass, all reachable pages are Compose Material 3, the public provider completes authorized live journeys, old data upgrades and rollback reads pass, and the signed public/private artifacts satisfy their respective public or protected compliance gates.
