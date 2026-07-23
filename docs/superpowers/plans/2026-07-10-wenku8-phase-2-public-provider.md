# Wenku8 Phase 2 Complete Public Provider Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the public throwing stub with a complete, independently authored, typed Wenku8 provider and non-throwing legacy bridge that cover all accepted anonymous, account, bookshelf, recommendation, and community operations while preserving fail-closed authorization, HTTPS, credential, ABI, and private-provider boundaries.

**Architecture:** Keep `:api-contract`, `:api-public`, and `:core:network` platform-neutral Kotlin/JVM modules; keep encrypted Android persistence in `:core:storage`, domain caching and single-flight in `:core:data`, and composition in `:app`. A typed provider owns transport and parsing only, evidence-backed capabilities stop unsupported work before transport, and shared contract fixtures prove the public provider and protected private adapter are substitutable. Public operation behavior is derived only from independently observed, sanitized evidence and newly authored synthetic fixtures; no Wild code, selector, fixture, test, comment, message, or control flow is an implementation input.

**Tech Stack:** Kotlin 2.2.10, Java 17, Gradle/AGP 9.0.1, Kotlin coroutines 1.10.2, OkHttp and MockWebServer, Jsoup, Android Keystore AES-GCM, Room/DataStore-era core storage boundaries, JUnit 4, kotlinx-coroutines-test, Groovy Gradle scripts, YAML evidence and verification manifests.

---

## Working Directory and Execution Rules

All commands in this plan run from:

```powershell
Set-Location 'studio-android/LightNovelLibrary'
```

File paths are repository-root-relative unless a command explicitly uses an app-root-relative path. Every implementation task starts from a clean task boundary, adds only test/support/gate-registration material, and runs its named RED check before changing a production source set. An unexpectedly passing RED check means the behavior already exists: inspect the assertion and production path, record the no-op, and do not make a speculative production edit. Every behavior-changing task then runs its named GREEN checks after the minimum implementation and creates exactly the commit shown. Never stage unrelated user changes. Do not squash the task commits while executing this plan; the review protocol uses them as provenance boundaries.

## Preconditions and Stop Conditions

The following checks are mandatory before Task 1. They are inputs from Phase 0 and Phase 1, not optional paperwork:

```powershell
Test-Path '..\..\docs\compliance\site-content-distribution-approval.yaml'
Test-Path '..\..\docs\verification\outbound-network-manifest.yaml'
Test-Path 'api-contract\build.gradle'
Test-Path 'api-public\build.gradle'
Test-Path 'core\session-contract\build.gradle'
Test-Path 'core\network\build.gradle'
Test-Path 'core\storage\build.gradle'
Test-Path 'core\data\build.gradle'
.\gradlew.bat :app:testAlphaDebugUnitTest :app:lintAlphaDebug --stacktrace
```

Expected: every `Test-Path` prints `True`; the Phase 1 unit and lint tasks end in `BUILD SUCCESSFUL`.

Apply these stop rules throughout execution:

1. If the Phase 0 site authorization scope for automated live access is not currently `ACCEPTED`, do not perform live observation, login, or any live smoke. Tasks using synthetic fixtures and local MockWebServer may continue.
2. If endpoint/request-contract publication is not currently `ACCEPTED`, do not push, publish, or commit newly observed exact endpoint/request fields outside the access-controlled evidence workspace. Fixture-only ledgers must remain `FIXTURE_ONLY`, and production capabilities backed only by them remain disabled.
3. If the relevant content/cache/distribution channel is not currently `ACCEPTED`, do not enable that capability in a distributable variant and do not assemble or release a distributable provider artifact for that channel. Debug fixture work may continue only inside the approved workspace.
4. If privacy egress approval, the Phase 0 outbound-network manifest, or the no-analytics/no-ad/no-raw-log gate is not `ACCEPTED` and passing, stop all account/live work. Synthetic anonymous parser work may continue.
5. If repository/dependency/license provenance is not accepted, do not add OkHttp, Jsoup, test, or plugin coordinates by bypassing dependency verification. Stop and update the Phase 0 ledger/locks through its approved process.
6. If an operation lacks an accepted HTTPS scheme, host, method, path, fields, redirect contract, parser evidence, and fixture hash, keep that operation capability-disabled. Do not guess a wire contract from Wild or a legacy private module.
7. Daily check-in remains disabled and absent from reachable UI until its own accepted HTTPS evidence record exists. The known cleartext app-host behavior is never implemented and never added to network security configuration.
8. The live account credentials are read only from `WENKU8_LIVE_USERNAME` and `WENKU8_LIVE_PASSWORD`. Never print, paste, commit, screenshot, persist, pass through `-P`, place in `local.properties`, or include either literal in a command.
9. The clean-room implementation agent must not open or search the Wild checkout. An independent observer may inspect only accepted public-site responses after Gate 1 is accepted. The observer cannot edit provider source, parser source, or fixtures; the implementer receives only sanitized accepted ledgers and minimal project-authored fixtures; the clean-room reviewer checks hashes and signs the attestation.
10. On any Keystore/AEAD/session-codec failure, the expected product state is deterministic signed-out plus authenticated-cache purge. Never add password recovery from `cert.wk8`.

The Phase 2 exit gate is not satisfied while any required live-observation, endpoint-publication, account-operation, cache/content, or distribution scope is `UNKNOWN`, `EXPIRED`, `REJECTED`, or missing. Deterministic synthetic-fixture work may be complete without making a live or release claim.

## File Structure

Phase 1 creates the foundational modules and interfaces. Phase 2 creates or modifies the following focused units.

### Gradle graph and verification

- `studio-android/LightNovelLibrary/settings.gradle`: always includes `:api-contract`, `:api-public`, and `:api-contract-tests`; maps the one logical `:api` project to `api-legacy-bridge/` only in public mode; includes `:api-private-adapter` only in valid private mode.
- `studio-android/LightNovelLibrary/gradle/libs.versions.toml`: pins OkHttp, MockWebServer, Jsoup, and any evidence-parser test dependency already approved by Phase 0 verification.
- `studio-android/LightNovelLibrary/verification/provider-project-graph.ps1`: asserts unique project paths/directories and fail-closed provider selection.
- `studio-android/LightNovelLibrary/verification/provider-selection.gradle`: emits a redacted provider identity for debug/release assertions.

### Typed contract and reusable contract tests

- `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/ApiResult.kt`: success metadata and typed failure envelope.
- `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/ApiFailure.kt`: exhaustive redacted failure taxonomy, including `Unsupported(capability)`.
- `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/OperationCode.kt`: Phase 1-owned closed public-operation identity reused by parse failures so arbitrary URLs/hosts cannot enter the failure envelope.
- `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/ProviderCapability.kt`: immutable capability and input-policy types importing `SourceId` from `:core:model`.
- `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/Requests.kt`: browse/search/tag/bookshelf/community command types importing all stable identities/models from `:core:model`.
- `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/LoginRequest.kt`: the sole suspend-aware owner of caller-supplied password/captcha arrays; its `LoginAttemptId` comes from `:core:model`.
- `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/Wenku8Sources.kt`: four provider facets.
- `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/identity/Keys.kt`: Phase 1-owned `SourceId`, `NovelKey`, `ChapterKey`, `ReviewKey`, `ReviewPostKey`, `LoginAttemptId`, and `BookshelfEntryKey`; Phase 2 consumes but never recreates them.
- `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/catalog/`: Phase 1-owned catalog/chapter/binary response models; Phase 2 consumes but never recreates them.
- `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/account/AccountModels.kt`: Phase 1-owned captcha/session/profile/bookshelf/recommendation/check-in response models.
- `studio-android/LightNovelLibrary/core/model/src/main/kotlin/org/mewx/wenku8/core/model/community/CommunityModels.kt`: Phase 1-owned review summary/post response models.
- `studio-android/LightNovelLibrary/api-contract-tests/src/testFixtures/kotlin/org/mewx/wenku8/api/contract/testing/ProviderContractHarness.kt`: provider-neutral construction/traffic hooks.
- `studio-android/LightNovelLibrary/api-contract-tests/src/testFixtures/kotlin/org/mewx/wenku8/api/contract/testing/ProviderContractSuite.kt`: reusable capability, input, cancellation, and operation contracts.

### Evidence and clean-room boundary

- `docs/api-evidence/schema/operation.schema.yaml`: exact evidence-record fields and accepted-state invariants.
- `docs/api-evidence/operations/manifest.yaml`: exhaustive operation inventory and capability mapping.
- The 32 exact operation-record paths enumerated in Task 5: one fixture-only or independently accepted provenance record per manifest operation.
- `docs/api-evidence/clean-room/roles.yaml`: observer, implementer, and reviewer separation.
- `docs/api-evidence/clean-room/attestations/home.yaml`: the first concrete accepted-operation reviewer attestation; every later accepted record uses its exact manifest operation ID plus `.yaml` in the same directory and stores that path in `reviewAttestation`.
- `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/evidence/ApiEvidenceContractTest.kt`: schema, state, hash, role, and capability fail-closed checks.

### Network and public transport

- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/policy/Wenku8HostPolicy.kt`: exact HTTPS origin/host/port decisions and header stripping rules.
- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/policy/NetworkOperationScope.kt`: validated transport-local operation scope; it is deliberately independent of `:api-contract`.
- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/okhttp/PolicyRedirectInterceptor.kt`: validates every redirect hop and refuses mutation redirects.
- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/codec/GbkCodec.kt`: explicit GBK decode and byte-first percent encoding.
- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/failure/NetworkFailureKind.kt`: transport-only failure kinds with no dependency on `:api-contract`.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/ApiNetworkFailureMapper.kt`: the sole mapping from network/codec outcomes to `ApiFailure`.
- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/okhttp/AwaitCall.kt`: cancellation-propagating OkHttp suspend bridge.
- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/okhttp/BoundedBodyReader.kt`: media/body limits before allocation.
- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/okhttp/RequestThrottle.kt`: injected-clock concurrency and pacing policy.
- `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/logging/RedactedNetworkEvent.kt`: allowlisted operation-only diagnostics.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/PublicHttpClientFactory.kt`: anonymous, authenticated, and attempt-scoped clients.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/PublicTransport.kt`: accepted operation request execution and response classification.

### Parsers and provider facets

- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/PageGuard.kt`: rejects login/challenge/block/error masquerades before parsing.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/HomeParser.kt`: ordered homepage sections.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/BrowseParser.kt`: latest, ranking, category, and completed pages.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/TagParser.kt`: tag groups/tags, separate from paged tag results.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/SearchParser.kt`: paged title/author results.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/NovelDetailParser.kt`: detail and controlled introduction.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/CatalogParser.kt`: ordered volumes/chapters.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/ChapterParser.kt`: ordered text/image/break blocks.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/AccountParser.kt`: session/profile/login terminal states.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/BookshelfParser.kt`: group and server membership `bid` retention.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/ReviewParser.kt`: review summaries/posts and created IDs.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicProvider.kt`: one immutable capabilities instance and four guarded facets.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderFactory.kt`: the single concrete production-facet assembly used by the app and TLS contract fixture.

### Session, credential migration, and authenticated operations

- `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/SessionRecordCodec.kt`: bounded versioned binary codec with no diagnostic secret rendering.
- `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/AndroidKeystoreSessionCipher.kt`: AES-GCM envelope using a non-exportable Android Keystore key.
- `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/EncryptedSessionStore.kt`: atomic no-backup session records and monotonic epoch files.
- `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/migration/LegacyCredentialAdapter.kt`: recognizes password-bearing legacy state without importing it.
- `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/migration/CredentialMigrationJournal.kt`: non-secret mutation/checkpoint journal in excluded transient storage.
- `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/migration/CredentialMigrationCoordinator.kt`: idempotent new-session persistence and atomic legacy scrub reconciliation.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/AttemptCookieJar.kt`: RFC-relevant in-memory per-attempt cookie isolation.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/LoginAttemptRegistry.kt`: random, expiring, single-use attempt ownership.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/SecretFormBody.kt`: encoded temporary bytes cleared on every terminal path.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/SessionCookieConversions.kt`: the sole bounded conversion between Phase 1 `SessionCookie` and OkHttp `Cookie`.
- `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicAccountSource.kt`: captcha sequence, session validation, profile/avatar, logout, bookshelf, recommendation, and disabled check-in.
- `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/DefaultAppContainer.kt`: binds encrypted store, audited clients, selected provider, cache gateway, and migration coordinator.

### Cache, bridge, private adapter, and live harness

- `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/ProviderCacheKey.kt`: source/host/schema/language/operation/parameters/account/epoch identity.
- `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/CachedProviderGateway.kt`: TTL, stale fallback, epoch rejection, and repository-owned single-flight.
- `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/MutationInvalidator.kt`: exact targeted cache effects loaded from the checked-in matrix.
- `studio-android/LightNovelLibrary/core/data/src/main/resources/mutation-invalidation-matrix.yaml`: exhaustive mutation-to-domain-key mapping.
- `studio-android/LightNovelLibrary/api-legacy-bridge/`: public logical `:api` implementation preserving the frozen `Wenku8API`, `Wenku8Error`, `LightNetwork`, and `LightUserSession` ABI without normal-action exceptions or main-thread `runBlocking`.
- `studio-android/LightNovelLibrary/api-private-adapter/`: private-graph-only Android library with namespace `org.mewx.wenku8.api.privateadapter`; it consumes the logical private `:api` AAR and the protected source supplies the typed legacy binding.
- `studio-android/LightNovelLibrary/api-public/src/liveTest/kotlin/org/mewx/wenku8/api/publicprovider/live/LiveReadOnlySmoke.kt`: environment-only interactive captcha smoke.
- `studio-android/LightNovelLibrary/api-public/src/liveTest/kotlin/org/mewx/wenku8/api/publicprovider/live/LiveMutationPolicy.kt`: reversible and persistent mutation gates.
- `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/fixture/FixtureWenku8Server.kt`: TLS-only server, independent route oracle, real `PublicProviderFactory` composition, redacted traffic capture, cleanup, and secret-lifetime assertions.
- `docs/verification/provider-phase2-matrix.yaml`: exact Phase 2 requirement-to-task/report evidence map.

## Task Dependency Graph

| Task | Depends on | Produces |
| --- | --- | --- |
| 1 | Phase 0 and Phase 1 exit gates | Unique Gradle provider graph |
| 2 | 1 | Core result/failure/capability types |
| 3 | 2 | Catalog/account/community models |
| 4 | 3 | Source facets and reusable guard |
| 5 | Phase 0 authorization artifacts, 4 | Clean-room evidence inventory and validator |
| 6 | 1, 2 | HTTPS host and redirect policy |
| 7 | 2, 6 | GBK/URL codecs |
| 8 | 2, 6, 7 | Cancellation, bounds, throttle, redaction |
| 9 | 5, 8 | Public transport and masquerade guard |
| 10 | 5, 9 | Home/browse/tag/search parsers |
| 11 | 5, 9 | Detail/catalog parsers |
| 12 | 5, 9 | Chapter and binary behavior |
| 13 | 4, 10, 11, 12 | Anonymous public facets |
| 14 | Phase 1 SessionStore contract, 2 | Encrypted no-backup SessionStore |
| 15 | 14 | Credential migration and legacy scrub |
| 16 | 3, 8, 9, 14 | Captcha attempt flow |
| 17 | 16 | Session/profile/avatar/logout |
| 18 | 17 | Bookshelf read/add/remove/move |
| 19 | 17 | Recommendation and fail-closed check-in |
| 20 | 3, 9, 17 | Review list/thread/create/reply |
| 21 | 13, 17, 18, 19, 20 | Full PublicProvider composition |
| 22 | Phase 1 cache contracts, 21 | Cache/single-flight/invalidation gateway |
| 23 | 4, 21, 22 | Exhaustive shared contract suite |
| 24 | 23 | Deterministic end-to-end fixture journey |
| 25 | 1, 21 | Non-throwing public legacy ABI bridge |
| 26 | 1, 4, 23 | Protected private adapter contract entry |
| 27 | 21, 25, 26 | Debug/minified provider selection matrix |
| 28 | Accepted live/account gate, 21 | Environment-only live smoke harness |
| 29 | 14, 15, 22 | Backup/process-death/session security evidence |
| 30 | 24-29 | Phase 2 verification matrix and final audit |

### Task 1: Lock the Unique Provider Gradle Graph

**Depends on:** Phase 0 and Phase 1 exit gates.

**Files:**
- Modify: `studio-android/LightNovelLibrary/settings.gradle`
- Modify: `studio-android/LightNovelLibrary/gradle/libs.versions.toml`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/build.gradle`
- Create: `studio-android/LightNovelLibrary/api-private-adapter/build.gradle`
- Create: `studio-android/LightNovelLibrary/verification/provider-project-graph.ps1`

- [ ] **Step 1: Write the failing graph verifier**

```powershell
$ErrorActionPreference = 'Stop'
$public = & .\gradlew.bat projects -Pwenku8Provider=public --console=plain 2>&1 | Out-String
if ($LASTEXITCODE -ne 0) { throw $public }
foreach ($required in ':api-contract', ':api-public', ':api-contract-tests', ':api') {
    if (-not $public.Contains("Project '$required'")) { throw "missing $required" }
}
if ($public.Contains("Project ':legacy-api-bridge'")) { throw 'duplicate legacy bridge identity' }
if ($public.Contains("Project ':api-private-adapter'")) { throw 'private adapter leaked into public graph' }
$unknown = & .\gradlew.bat help -Pwenku8Provider=unknown --console=plain 2>&1 | Out-String
if ($LASTEXITCODE -eq 0 -or -not $unknown.Contains('Unsupported wenku8Provider')) {
    throw 'unknown provider did not fail closed'
}
$private = & .\gradlew.bat help -Pwenku8Provider=private --console=plain 2>&1 | Out-String
if (-not (Test-Path 'api\build.gradle') -and ($LASTEXITCODE -eq 0 -or -not $private.Contains('Private provider source is unavailable'))) {
    throw 'missing private source did not fail closed'
}
Write-Output 'PASS provider project graph'
```

- [ ] **Step 2: Run the verifier and confirm RED**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\provider-project-graph.ps1`

Expected: FAIL with `missing :api-contract`, `duplicate legacy bridge identity`, or the existing directory-presence selection behavior.

- [ ] **Step 3: Replace directory-presence selection with the single explicit graph**

```groovy
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = 'LightNovelLibrary'

include ':app',
        ':api-contract',
        ':api-public',
        ':api-contract-tests',
        ':core:model',
        ':core:domain',
        ':core:session-contract',
        ':core:network',
        ':core:storage',
        ':core:data',
        ':core:designsystem',
        ':core:testing'

project(':core:model').projectDir = file('core/model')
project(':core:domain').projectDir = file('core/domain')
project(':core:session-contract').projectDir = file('core/session-contract')
project(':core:network').projectDir = file('core/network')
project(':core:storage').projectDir = file('core/storage')
project(':core:data').projectDir = file('core/data')
project(':core:designsystem').projectDir = file('core/designsystem')
project(':core:testing').projectDir = file('core/testing')

def selectedProvider = providers.gradleProperty('wenku8Provider').orElse('public').get()
switch (selectedProvider) {
    case 'public':
        include ':api'
        project(':api').projectDir = file('api-legacy-bridge')
        break
    case 'private':
        if (!file('api/build.gradle').isFile()) {
            throw new GradleException('Private provider source is unavailable; no path or endpoint details are shown.')
        }
        include ':api'
        project(':api').projectDir = file('api')
        include ':api-private-adapter'
        project(':api-private-adapter').projectDir = file('api-private-adapter')
        break
    default:
        throw new GradleException("Unsupported wenku8Provider value: ${selectedProvider.replaceAll(/[^A-Za-z0-9_-]/, '?')}")
}
```

- [ ] **Step 4: Add the exact module build files**

First extend the Phase 1 version catalog with only Phase 0-approved pinned coordinates:

```toml
[versions]
okhttp = "4.12.0"
jsoup = "1.18.3"
snakeyaml = "2.3"

[libraries]
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "okhttp" }
okhttp-tls = { module = "com.squareup.okhttp3:okhttp-tls", version.ref = "okhttp" }
jsoup = { module = "org.jsoup:jsoup", version.ref = "jsoup" }
snakeyaml = { module = "org.yaml:snakeyaml", version.ref = "snakeyaml" }
```

If Phase 0 approved different exact bytes, use that approved pinned version and update the lock/verification/SBOM entry in the same commit. Never add a second literal or dynamic version.

```groovy
// api-legacy-bridge/build.gradle
plugins {
    alias libs.plugins.android.library
    alias libs.plugins.kotlin.android
}

android {
    namespace 'org.mewx.wenku8.api.legacybridge'
    compileSdk rootProject.compileSdkVersion
    defaultConfig { minSdk rootProject.minSdkVersion }
    buildFeatures { buildConfig true }
}

dependencies {
    api project(':api-public')
    implementation project(':api-contract')
    implementation project(':core:network')
    implementation project(':core:session-contract')
}
```

Add the transport/parser dependencies to the existing Phase 1 JVM modules:

```groovy
// api-contract/build.gradle ownership edge retained from Phase 1
dependencies {
    api project(':core:model')
}
```

```groovy
// core/network/build.gradle additions
dependencies {
    api libs.okhttp
    testImplementation libs.okhttp.mockwebserver
    testImplementation libs.junit4
    testImplementation libs.kotlinx.coroutines.test
}
```

```groovy
// api-public/build.gradle additions
dependencies {
    api project(':api-contract')
    implementation project(':core:model')
    implementation project(':core:session-contract')
    implementation project(':core:network')
    implementation libs.kotlinx.coroutines.core
    implementation libs.okhttp
    implementation libs.jsoup
    implementation libs.snakeyaml
    testImplementation testFixtures(project(':api-contract-tests'))
    testImplementation libs.okhttp.mockwebserver
    testImplementation libs.okhttp.tls
    testImplementation libs.snakeyaml
    testImplementation libs.junit4
    testImplementation libs.kotlinx.coroutines.test
}
```

```groovy
// api-private-adapter/build.gradle; this Android project is included only after private source validation.
plugins {
    alias libs.plugins.android.library
    alias libs.plugins.kotlin.android
}

android {
    namespace 'org.mewx.wenku8.api.privateadapter'
    compileSdk rootProject.compileSdkVersion
    defaultConfig { minSdk rootProject.minSdkVersion }
}

dependencies {
    implementation project(':api')
    api project(':api-contract')
    implementation project(':core:model')
    testImplementation testFixtures(project(':api-contract-tests'))
    testImplementation libs.junit4
}
```

- [ ] **Step 5: Run graph and build checks**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\provider-project-graph.ps1`

Expected: `PASS provider project graph`.

Run: `.\gradlew.bat :api-contract:build :api-public:build :api-contract-tests:build :api:assembleDebug -Pwenku8Provider=public --stacktrace`

Expected: `BUILD SUCCESSFUL`; the output contains one logical `:api` project and no `:legacy-api-bridge` project.

- [ ] **Step 6: Commit the graph**

```powershell
git add settings.gradle gradle/libs.versions.toml api-legacy-bridge/build.gradle api-private-adapter/build.gradle verification/provider-project-graph.ps1
git commit -m "build: lock explicit provider module graph"
```

### Task 2: Define Results, Failures, and Immutable Capabilities

**Depends on:** Task 1.

Phase 1 owns these contract files. In Phase 2, treat the code blocks below as the required audited end state: modify only a mismatching definition, never create a second type or duplicate package. If Phase 1's committed definitions already match byte-for-byte semantics, this task changes tests/ABI evidence only and retains the existing production files.

**Files:**
- Modify: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/ApiResult.kt`
- Modify: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/ApiFailure.kt`
- Modify: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/OperationCode.kt`
- Modify: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/ProviderCapability.kt`
- Test: `studio-android/LightNovelLibrary/api-contract/src/test/kotlin/org/mewx/wenku8/api/contract/ContractCoreTest.kt`

- [ ] **Step 1: Write failing immutability and redaction tests**

```kotlin
package org.mewx.wenku8.api.contract

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.core.model.identity.SourceId

class ContractCoreTest {
    @Test fun unsupportedCarriesOnlyItsCapability() {
        val failure = ApiFailure.Unsupported(ProviderCapability.DAILY_CHECK_IN)
        assertEquals(ProviderCapability.DAILY_CHECK_IN, failure.capability)
        assertFalse(failure.toString().contains("Cookie", ignoreCase = true))
    }

    @Test fun capabilitiesCopyCallerSet() {
        val source = linkedSetOf(ProviderCapability.ANONYMOUS_CATALOG)
        val capabilities = ProviderCapabilities(SourceId("public"), source, ProviderInputPolicy(80, 60, 4000, 4000))
        source += ProviderCapability.DAILY_CHECK_IN
        assertEquals(setOf(ProviderCapability.ANONYMOUS_CATALOG), capabilities.enabled)
    }

    @Test fun parseFailureAcceptsOnlyClosedOperationCodes() {
        val failure = ApiFailure.Parse(OperationCode.NOVEL_DETAIL, contractRevision = 4)
        assertEquals(OperationCode.NOVEL_DETAIL, failure.operationCode)
        assertNull(OperationCode.fromWireId("https://private.invalid/secret"))
        assertNull(OperationCode.fromWireId("private-endpoint"))
        assertFalse(failure.toString().contains("http", ignoreCase = true))
        assertFalse(failure.toString().contains("private", ignoreCase = true))
    }
}
```

- [ ] **Step 2: Run the contract test and confirm RED**

Run: `.\gradlew.bat :api-contract:test --tests "org.mewx.wenku8.api.contract.ContractCoreTest" --stacktrace`

Expected: PASS when Phase 1 ownership is intact; STOP if the test reveals a contract drift. Phase 2 must not create a replacement core identity to make this test pass.

- [ ] **Step 3: Add the complete result and failure envelope**

```kotlin
package org.mewx.wenku8.api.contract

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
```

```kotlin
package org.mewx.wenku8.api.contract

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
```

This is the required audited end state of the single Phase 1-owned `OperationCode`; Phase 2 reuses or corrects that file in place and never declares another operation-code type or package.

```kotlin
package org.mewx.wenku8.api.contract

sealed interface ApiFailure {
    sealed interface Network : ApiFailure {
        data object Offline : Network
        data object Dns : Network
        data object Connect : Network
        data object Tls : Network
        data object Timeout : Network
    }

    data class Http(val status: Int, val retryAfterSeconds: Long?) : ApiFailure
    data object ChallengeBlocked : ApiFailure

    sealed interface Auth : ApiFailure {
        data object CaptchaRequired : Auth
        data object InvalidCaptcha : Auth
        data object InvalidCredentials : Auth
        data object SessionExpired : Auth
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

- [ ] **Step 4: Add the complete capability contract with a defensive set copy**

```kotlin
package org.mewx.wenku8.api.contract

import org.mewx.wenku8.core.model.identity.SourceId

enum class ProviderCapability {
    ANONYMOUS_CATALOG,
    BINARY_DOWNLOAD,
    REGISTRATION_LINK,
    CAPTCHA_LOGIN,
    PROFILE,
    BOOKSHELF_READ,
    BOOKSHELF_MUTATE,
    DAILY_CHECK_IN,
    RECOMMEND_NOVEL,
    REVIEWS_READ,
    REVIEWS_CREATE,
    REVIEWS_REPLY,
}

class ProviderCapabilities(
    val providerId: SourceId,
    enabled: Set<ProviderCapability>,
    val inputPolicy: ProviderInputPolicy,
) {
    val enabled: Set<ProviderCapability> = enabled.toSet()
    fun supports(capability: ProviderCapability): Boolean = capability in enabled
}

data class ProviderInputPolicy(
    val searchMaxEncodedBytes: Int,
    val reviewTitleMaxCodePoints: Int,
    val reviewBodyMaxCodePoints: Int,
    val replyMaxCodePoints: Int,
)
```

- [ ] **Step 5: Run tests and commit**

Run: `.\gradlew.bat :api-contract:test --tests "org.mewx.wenku8.api.contract.ContractCoreTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`, 3 tests pass; a URL, hostname, or private identifier cannot construct `ApiFailure.Parse`.

```powershell
git add api-contract/src/main api-contract/src/test
git commit -m "feat(api): define typed provider outcomes"
```

### Task 3: Bind Requests and Login Secrets to Phase 1 Core Models

**Depends on:** Task 2.

Phase 1 owns every stable identity and catalog/account/community response model in `:core:model`. Phase 2 owns only request/command shapes and `LoginRequest` in `:api-contract`; it imports the Phase 1 types and must not split, alias, copy, or recreate their class identity.

**Files:**
- Modify: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/Requests.kt`
- Modify: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/LoginRequest.kt`
- Test: `studio-android/LightNovelLibrary/api-contract/src/test/kotlin/org/mewx/wenku8/api/contract/ContractModelsTest.kt`

- [ ] **Step 1: Write failing model-invariant and secret-lifecycle tests**

```kotlin
package org.mewx.wenku8.api.contract

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.core.model.identity.BookshelfEntryKey
import org.mewx.wenku8.core.model.identity.LoginAttemptId

class ContractModelsTest {
    @Test fun loginRequestClearsCallerArraysAfterSuccess() = runTest {
        val password = charArrayOf('s', 'e', 'c', 'r', 'e', 't')
        val captcha = charArrayOf('1', '2', '3', '4')
        LoginRequest(LoginAttemptId("attempt"), "account", password, captcha).use { request ->
            request.consumeSecrets { suppliedPassword, suppliedCaptcha ->
                assertTrue(suppliedPassword === password)
                assertTrue(suppliedCaptcha === captcha)
            }
        }
        assertTrue(password.all { it == '\u0000' })
        assertTrue(captcha.all { it == '\u0000' })
    }

    @Test fun loginRequestClearsCallerArraysAfterCancellation() = runTest {
        val password = charArrayOf('x')
        val captcha = charArrayOf('y')
        val request = LoginRequest(LoginAttemptId("attempt"), "account", password, captcha)
        runCatching { request.consumeSecrets { _, _ -> throw CancellationException("test") } }
        assertTrue(password.all { it == '\u0000' })
        assertTrue(captcha.all { it == '\u0000' })
    }

    @Test fun bookshelfCommandUsesThePhase1MembershipIdentity() {
        val command = BookshelfCommand.Move(
            listOf(BookshelfEntryKey("bid-19")),
            sourceGroupId = "group-a",
            targetGroupId = "group-b",
        )
        assertEquals("org.mewx.wenku8.core.model.identity", command.entryKeys.single()::class.java.packageName)
    }
}
```

- [ ] **Step 2: Run the model tests and confirm RED**

Run: `.\gradlew.bat :api-contract:test --tests "org.mewx.wenku8.api.contract.ContractModelsTest" --stacktrace`

Expected: PASS when Phase 1 core-model ownership and request imports are intact; STOP on a duplicate/missing identity instead of adding a second model.

- [ ] **Step 3: Keep only wire-independent request and command shapes in `Requests.kt`**

```kotlin
package org.mewx.wenku8.api.contract

import org.mewx.wenku8.core.model.identity.BookshelfEntryKey
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.identity.ReviewKey
import org.mewx.wenku8.core.model.settings.ContentLanguage

enum class SearchScope { TITLE, AUTHOR }
enum class BrowseKind { LATEST, COMPLETED, CATEGORY, RANKING }
enum class RankingPeriod { ALL_TIME, MONTH, WEEK, DAY }

data class BrowseRequest(
    val kind: BrowseKind,
    val page: Int,
    val language: ContentLanguage,
    val categoryId: String? = null,
    val rankingPeriod: RankingPeriod? = null,
)

data class TagDiscoveryRequest(val groupId: String?, val language: ContentLanguage)
data class TagBrowseRequest(val tagId: String, val page: Int, val language: ContentLanguage)
data class SearchQuery(val text: String, val scope: SearchScope, val page: Int, val language: ContentLanguage)

sealed interface BookshelfCommand {
    data class Add(val novel: NovelKey, val targetGroupId: String?) : BookshelfCommand
    data class Remove(val entryKey: BookshelfEntryKey, val sourceGroupId: String) : BookshelfCommand
    data class Move(
        val entryKeys: List<BookshelfEntryKey>,
        val sourceGroupId: String,
        val targetGroupId: String,
    ) : BookshelfCommand
}

data class CreateReviewCommand(val novel: NovelKey, val title: String, val body: String)
data class ReplyCommand(val review: ReviewKey, val body: String)
```

- [ ] **Step 4: Keep the suspend-aware secret owner in `api-contract`**

```kotlin
package org.mewx.wenku8.api.contract

import java.util.concurrent.atomic.AtomicBoolean
import org.mewx.wenku8.core.model.identity.LoginAttemptId

class LoginRequest(
    val attemptId: LoginAttemptId,
    val username: String,
    password: CharArray,
    captcha: CharArray,
) : AutoCloseable {
    private val consumedOrClosed = AtomicBoolean(false)
    private val ownedPassword = password
    private val ownedCaptcha = captcha

    suspend fun <T> consumeSecrets(block: suspend (CharArray, CharArray) -> T): T {
        check(consumedOrClosed.compareAndSet(false, true)) { "LoginRequest already consumed or closed" }
        return try {
            block(ownedPassword, ownedCaptcha)
        } finally {
            clear()
        }
    }

    override fun close() {
        consumedOrClosed.set(true)
        clear()
    }

    private fun clear() {
        ownedPassword.fill('\u0000')
        ownedCaptcha.fill('\u0000')
    }
}
```

- [ ] **Step 5: Prove `api-contract` imports rather than owns stable model classes**

```kotlin
val forbiddenApiContractModelDeclarations = listOf(
    "SourceId", "NovelKey", "ChapterKey", "ReviewKey", "ReviewPostKey", "LoginAttemptId", "BookshelfEntryKey",
    "NovelSummary", "NovelDetail", "HomeSection", "TagGroup", "TagSummary", "Volume", "ChapterSummary",
    "ChapterDocument", "ChapterBlock", "ControlledRichText", "BinaryRequest", "BinaryResource", "ExternalLink",
    "CaptchaChallenge", "SessionState", "UserProfile", "BookshelfEntry", "BookshelfGroup", "CheckInResult",
    "RecommendationResult", "ReviewSummary", "ReviewPost",
)
```

The ownership test parses `api-contract/src/main/kotlin` and fails on any `class`, `data class`, `value class`, `enum class`, or `interface` declaration with one of those names. It separately asserts their runtime packages start with `org.mewx.wenku8.core.model.`.

- [ ] **Step 6: Run all model tests and commit**

Run: `.\gradlew.bat :api-contract:test --tests "org.mewx.wenku8.api.contract.ContractModelsTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`, 3 tests pass; cancellation remains a `CancellationException`, both supplied arrays are zeroed, and no stable core model is declared in `api-contract`.

```powershell
git add api-contract/src/main api-contract/src/test
git commit -m "test(api): lock core model ownership"
```

### Task 4: Define Provider Facets, Binding, and the Zero-Work Capability Guard

**Depends on:** Task 3.

**Files:**
- Modify: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/Wenku8Sources.kt`
- Modify: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/ProviderBinding.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/CapabilityGuard.kt`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/CapabilityGuardTest.kt`

- [ ] **Step 1: Write the failing zero-work test**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.api.contract.ProviderCapabilities
import org.mewx.wenku8.api.contract.ProviderCapability
import org.mewx.wenku8.api.contract.ProviderInputPolicy
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.core.model.identity.SourceId

class CapabilityGuardTest {
    @Test fun absentCapabilityReturnsUnsupportedWithoutInvokingTransportBlock() = runTest {
        var dispatchCount = 0
        val capabilities = ProviderCapabilities(SourceId("public"), emptySet(), ProviderInputPolicy(80, 60, 4000, 4000))
        val result = CapabilityGuard(capabilities) { "trace-1" }.run(ProviderCapability.PROFILE) {
            dispatchCount += 1
            ApiResult.Success("profile", ResponseMetadata("public", 0L, Freshness.FRESH))
        }
        assertEquals(0, dispatchCount)
        assertTrue((result as ApiResult.Failure).error == ApiFailure.Unsupported(ProviderCapability.PROFILE))
    }

    @Test fun presentCapabilityInvokesBlockExactlyOnce() = runTest {
        var dispatchCount = 0
        val capabilities = ProviderCapabilities(
            SourceId("public"),
            setOf(ProviderCapability.ANONYMOUS_CATALOG),
            ProviderInputPolicy(80, 60, 4000, 4000),
        )
        CapabilityGuard(capabilities) { "trace-2" }.run(ProviderCapability.ANONYMOUS_CATALOG) {
            dispatchCount += 1
            ApiResult.Success(Unit, ResponseMetadata("public", 0L, Freshness.FRESH))
        }
        assertEquals(1, dispatchCount)
    }
}
```

- [ ] **Step 2: Run the guard test and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.CapabilityGuardTest" --stacktrace`

Expected: FAIL because the facets and `CapabilityGuard` are absent.

- [ ] **Step 3: Add all four stable source facets and the binding**

```kotlin
package org.mewx.wenku8.api.contract

import org.mewx.wenku8.core.model.account.*
import org.mewx.wenku8.core.model.catalog.*
import org.mewx.wenku8.core.model.community.*
import org.mewx.wenku8.core.model.identity.*
import org.mewx.wenku8.core.model.settings.ContentLanguage

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

```kotlin
package org.mewx.wenku8.api.contract

import org.mewx.wenku8.core.model.identity.SourceId

interface ProviderBinding {
    val providerId: SourceId
    val catalog: Wenku8CatalogSource
    val binary: Wenku8BinarySource
    val account: Wenku8AccountSource
    val community: Wenku8CommunitySource
}
```

- [ ] **Step 4: Implement one reusable guard that cannot dispatch when disabled**

```kotlin
package org.mewx.wenku8.api.publicprovider

import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.api.contract.ProviderCapabilities
import org.mewx.wenku8.api.contract.ProviderCapability

class CapabilityGuard(
    private val capabilities: ProviderCapabilities,
    private val newTraceId: () -> String,
) {
    suspend fun <T> run(
        required: ProviderCapability,
        block: suspend () -> ApiResult<T>,
    ): ApiResult<T> = if (capabilities.supports(required)) {
        block()
    } else {
        ApiResult.Failure(ApiFailure.Unsupported(required), newTraceId())
    }
}
```

- [ ] **Step 5: Run the guard and contract compilation tests**

Run: `.\gradlew.bat :api-contract:test :api-public:test --tests "org.mewx.wenku8.api.publicprovider.CapabilityGuardTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; absent capability dispatch count is 0 and enabled dispatch count is 1.

- [ ] **Step 6: Commit the facet boundary**

```powershell
git add api-contract/src api-public/src
git commit -m "feat(api): add guarded provider facets"
```

### Task 5: Establish the Clean-Room Operation Ledger and Fail-Closed Evidence Gate

**Depends on:** Task 4 and the Phase 0 authorization records.

**Files:**
- Create: `docs/api-evidence/schema/operation.schema.yaml`
- Create: `docs/api-evidence/operations/manifest.yaml`
- Create: `docs/api-evidence/operations/home.yaml`
- Create: `docs/api-evidence/operations/browse-latest.yaml`
- Create: `docs/api-evidence/operations/browse-completed.yaml`
- Create: `docs/api-evidence/operations/browse-category.yaml`
- Create: `docs/api-evidence/operations/browse-ranking.yaml`
- Create: `docs/api-evidence/operations/tag-groups.yaml`
- Create: `docs/api-evidence/operations/tags.yaml`
- Create: `docs/api-evidence/operations/novels-by-tag.yaml`
- Create: `docs/api-evidence/operations/search.yaml`
- Create: `docs/api-evidence/operations/novel-detail.yaml`
- Create: `docs/api-evidence/operations/catalog.yaml`
- Create: `docs/api-evidence/operations/chapter.yaml`
- Create: `docs/api-evidence/operations/binary.yaml`
- Create: `docs/api-evidence/operations/registration.yaml`
- Create: `docs/api-evidence/operations/login-prewarm-root.yaml`
- Create: `docs/api-evidence/operations/login-prewarm-form.yaml`
- Create: `docs/api-evidence/operations/captcha.yaml`
- Create: `docs/api-evidence/operations/login-submit.yaml`
- Create: `docs/api-evidence/operations/validate-session.yaml`
- Create: `docs/api-evidence/operations/profile.yaml`
- Create: `docs/api-evidence/operations/avatar.yaml`
- Create: `docs/api-evidence/operations/bookshelf-read.yaml`
- Create: `docs/api-evidence/operations/bookshelf-add.yaml`
- Create: `docs/api-evidence/operations/bookshelf-remove.yaml`
- Create: `docs/api-evidence/operations/bookshelf-move.yaml`
- Create: `docs/api-evidence/operations/recommend.yaml`
- Create: `docs/api-evidence/operations/reviews.yaml`
- Create: `docs/api-evidence/operations/review-thread.yaml`
- Create: `docs/api-evidence/operations/review-create.yaml`
- Create: `docs/api-evidence/operations/review-reply.yaml`
- Create: `docs/api-evidence/operations/logout.yaml`
- Create: `docs/api-evidence/operations/daily-check-in.yaml`
- Create: `docs/api-evidence/clean-room/roles.yaml`
- Create: `docs/api-evidence/clean-room/trusted-reviewer-keys.yaml`
- Create: `docs/api-evidence/clean-room/attestations/.gitkeep`
- Conditional create for each accepted operation: `docs/api-evidence/clean-room/attestations/<operation-id>.yaml`
- Conditional create for each accepted operation: `docs/api-evidence/clean-room/attestations/<operation-id>.yaml.sig`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/evidence/ApiEvidenceContractTest.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/compliance/CleanRoomEvidenceGate.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/compliance/CleanRoomAttestationTool.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/compliance/CleanRoomEvidenceGateTest.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/compliance/CleanRoomAttestationToolTest.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/resources/clean-room-evidence/`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Modify: `studio-android/LightNovelLibrary/api-public/build.gradle`

- [ ] **Step 1: Write the failing exhaustive evidence test**

```kotlin
package org.mewx.wenku8.api.publicprovider.evidence

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.yaml.snakeyaml.Yaml

class ApiEvidenceContractTest {
    private val root = Path.of("..", "..", "docs", "api-evidence").normalize()
    private val yaml = Yaml()

    @Test fun manifestNamesEveryPhase2OperationExactlyOnce() {
        val manifest = yaml.load<Map<String, Any>>(Files.readString(root.resolve("operations/manifest.yaml")))
        val operations = manifest.getValue("operations") as List<Map<String, Any>>
        val ids = operations.map { it.getValue("id") as String }
        assertEquals(32, ids.size)
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.containsAll(REQUIRED_OPERATION_IDS))
    }

    @Test fun runtimeIndexContainsOnlyCryptographicallyVerifiedAcceptedRecords() {
        val manifest = yaml.load<Map<String, Any>>(Files.readString(root.resolve("operations/manifest.yaml")))
        val operations = manifest.getValue("operations") as List<Map<String, Any>>
        val index = yaml.load<Map<String, Any>>(Files.readString(Path.of("build/generated/accepted-evidence/api-evidence/accepted-operation-index.yaml")))
        val indexed = (index.getValue("operations") as List<Map<String, Any>>).associateBy { it.getValue("operationId") as String }
        operations.forEach { row ->
            val id = row.getValue("id") as String
            val record = yaml.load<Map<String, Any>>(Files.readString(root.resolve("operations/$id.yaml")))
            assertEquals(id, record["operationId"])
            when (record["state"]) {
                "FIXTURE_ONLY" -> {
                    assertEquals(false, record["releaseEnabled"])
                    assertFalse(indexed.containsKey(id))
                }
                "ACCEPTED" -> {
                    assertEquals("https", (record.getValue("request") as Map<*, *>)["scheme"])
                    assertTrue((record.getValue("fixtureSha256") as String).matches(Regex("[0-9a-f]{64}")))
                    assertEquals("clean-room/attestations/$id.yaml", record["reviewAttestation"])
                    assertEquals("clean-room/attestations/$id.yaml.sig", record["reviewSignature"])
                    assertEquals(record["fixtureSha256"], indexed.getValue(id)["fixtureSha256"])
                }
                else -> error("invalid evidence state for $id")
            }
        }
    }

    @Test fun rolesForbidObserverImplementationOverlap() {
        val roles = yaml.load<Map<String, Any>>(Files.readString(root.resolve("clean-room/roles.yaml")))
        val observerMayWrite = ((roles.getValue("observer") as Map<*, *>).getValue("mayWrite") as List<*>)
        assertFalse(observerMayWrite.any { it.toString().contains("api-public/src") })
    }

    private companion object {
        val REQUIRED_OPERATION_IDS = setOf(
            "home", "browse-latest", "browse-completed", "browse-category", "browse-ranking",
            "tag-groups", "tags", "novels-by-tag", "search", "novel-detail", "catalog", "chapter",
            "binary", "registration", "login-prewarm-root", "login-prewarm-form", "captcha", "login-submit",
            "validate-session", "profile", "avatar", "bookshelf-read", "bookshelf-add", "bookshelf-remove",
            "bookshelf-move", "recommend", "reviews", "review-thread", "review-create", "review-reply",
            "logout", "daily-check-in",
        )
    }
}
```

- [ ] **Step 2: Run the evidence test and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.evidence.ApiEvidenceContractTest" --stacktrace`

Expected: FAIL because `docs/api-evidence/operations/manifest.yaml` and the operation records do not exist.

- [ ] **Step 3: Add the strict operation schema**

```yaml
schemaVersion: 1
record:
  required:
    - schemaVersion
    - operationId
    - state
    - observerId
    - observedAt
    - publicUrlClass
    - request
    - response
    - charset
    - requiredFields
    - optionalFields
    - failureSignatures
    - fixturePath
    - fixtureSha256
    - parserPath
    - parserContractRevision
    - releaseEnabled
    - reviewAttestation
    - reviewSignature
  state:
    enum: [FIXTURE_ONLY, ACCEPTED]
  acceptedInvariants:
    requestScheme: https
    fixtureSha256Pattern: '^[0-9a-f]{64}$'
    detachedSignatureAlgorithm: Ed25519
    acceptedIndexRequired: true
    observerCannotImplement: true
    reviewerCannotObserveOrImplement: true
    rawAuthenticatedBodyCommitted: false
    secretCommitted: false
  fixtureOnlyInvariants:
    liveObserved: false
    releaseEnabled: false
    requestPublished: false
```

- [ ] **Step 4: Add the exhaustive operation manifest**

```yaml
schemaVersion: 1
operations:
  - { id: home, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: browse-latest, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: browse-completed, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: browse-category, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: browse-ranking, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: tag-groups, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: tags, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: novels-by-tag, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: search, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: novel-detail, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: catalog, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: chapter, capability: ANONYMOUS_CATALOG, auth: false, kind: READ }
  - { id: binary, capability: BINARY_DOWNLOAD, auth: false, kind: READ }
  - { id: registration, capability: REGISTRATION_LINK, auth: false, kind: READ }
  - { id: login-prewarm-root, capability: CAPTCHA_LOGIN, auth: false, kind: READ }
  - { id: login-prewarm-form, capability: CAPTCHA_LOGIN, auth: false, kind: READ }
  - { id: captcha, capability: CAPTCHA_LOGIN, auth: false, kind: READ }
  - { id: login-submit, capability: CAPTCHA_LOGIN, auth: false, kind: MUTATION }
  - { id: validate-session, capability: CAPTCHA_LOGIN, auth: true, kind: READ }
  - { id: profile, capability: PROFILE, auth: true, kind: READ }
  - { id: avatar, capability: PROFILE, auth: true, kind: READ }
  - { id: bookshelf-read, capability: BOOKSHELF_READ, auth: true, kind: READ }
  - { id: bookshelf-add, capability: BOOKSHELF_MUTATE, auth: true, kind: MUTATION }
  - { id: bookshelf-remove, capability: BOOKSHELF_MUTATE, auth: true, kind: MUTATION }
  - { id: bookshelf-move, capability: BOOKSHELF_MUTATE, auth: true, kind: MUTATION }
  - { id: recommend, capability: RECOMMEND_NOVEL, auth: true, kind: MUTATION }
  - { id: reviews, capability: REVIEWS_READ, auth: false, kind: READ }
  - { id: review-thread, capability: REVIEWS_READ, auth: false, kind: READ }
  - { id: review-create, capability: REVIEWS_CREATE, auth: true, kind: MUTATION }
  - { id: review-reply, capability: REVIEWS_REPLY, auth: true, kind: MUTATION }
  - { id: logout, capability: CAPTCHA_LOGIN, auth: true, kind: MUTATION }
  - { id: daily-check-in, capability: DAILY_CHECK_IN, auth: true, kind: MUTATION }
```

- [ ] **Step 5: Add exact clean-room role boundaries**

```yaml
schemaVersion: 1
observer:
  mayRead:
    - docs/compliance/site-content-distribution-approval.yaml
    - independently-observed-public-responses
  mayWrite:
    - access-controlled-observation-workspace
  forbidden:
    - api-public/src
    - api-public/src/test/resources
    - Wild checkout
implementer:
  mayRead:
    - docs/api-evidence/operations
    - api-public/src/test/resources/fixtures
  mayWrite:
    - api-public/src
    - api-public/src/test
  forbidden:
    - raw-authenticated-responses
    - access-controlled-observation-workspace
    - Wild checkout
reviewer:
  mayRead:
    - sanitized-ledger-export
    - fixture-hashes
    - parser-source-hashes
  mayWrite:
    - docs/api-evidence/clean-room/attestations
  forbidden:
    - provider-implementation
    - parser-fixture-authorship
assignments: []
```

`assignments` is empty while every record is `FIXTURE_ONLY`. Before accepting an operation, add one concrete observer, implementer, and reviewer assignment with distinct non-empty IDs. `CleanRoomEvidenceGate` rejects an attestation unless its three IDs match those assignments and are pairwise distinct.

Add the fail-closed reviewer trust store. It deliberately trusts nobody until an authorized security owner commits a concrete X.509-encoded Ed25519 public key and its approval window:

```yaml
schemaVersion: 1
keys: []
```

Each authorized row has exactly `keyId`, `algorithm: Ed25519`, `x509PublicKeyBase64`, `notBefore`, `notAfter`, and nullable `revokedAt`; unknown fields, duplicate IDs, non-Ed25519 algorithms, malformed X.509 bytes, and overlapping duplicate key windows fail the gate. `keys: []` is the only valid initial state when there are no accepted operations.

- [ ] **Step 6: Create all 32 fixture-only records before any authorized observation**

Each file is concrete, deliberately non-live, and capability-disabling. Use this exact shape, changing only `operationId` to match each manifest ID:

```yaml
schemaVersion: 1
operationId: home
state: FIXTURE_ONLY
observerId: synthetic-fixture-author
observedAt: null
publicUrlClass: synthetic-contract-only
request:
  published: false
  scheme: null
  host: null
  port: null
  method: null
  pathTemplate: null
  queryFields: []
  formFields: []
  authenticated: false
  redirectContract: none
response:
  successStatuses: []
  redirectLocations: []
  successParser: synthetic-only
  idempotency: READ
  inputLimits: {}
  cacheEffect: none
charset: GBK
requiredFields: []
optionalFields: []
failureSignatures: []
fixtureSha256: null
fixturePath: null
parserPath: null
parserContractRevision: 1
releaseEnabled: false
reviewAttestation: ''
reviewSignature: ''
```

Run this checked command after creating the files:

```powershell
$ids = @('home','browse-latest','browse-completed','browse-category','browse-ranking','tag-groups','tags','novels-by-tag','search','novel-detail','catalog','chapter','binary','registration','login-prewarm-root','login-prewarm-form','captcha','login-submit','validate-session','profile','avatar','bookshelf-read','bookshelf-add','bookshelf-remove','bookshelf-move','recommend','reviews','review-thread','review-create','review-reply','logout','daily-check-in')
$missing = $ids | Where-Object { -not (Test-Path "..\..\docs\api-evidence\operations\$_.yaml") }
if ($missing) { throw "Missing fixture-only records: $($missing -join ', ')" }
```

Expected: no output and exit code 0.

- [ ] **Step 7: Define the accepted-observation handoff without guessing values**

This step runs only when automated observation and endpoint publication scopes are currently `ACCEPTED`. The observer changes the operation record to `ACCEPTED`, supplies literal `fixturePath`, `fixtureSha256`, and `parserPath` values, and sets the exact attestation/signature paths. The fixture must be independently authored and minimal. The reviewer attestation has this closed schema; unknown fields and YAML aliases are rejected:

```yaml
schemaVersion: 1
operationId: home
reviewedCommit: 40-lowercase-hex-written-by-the-review-tool
schemaSha256: 64-lowercase-hex-measured-by-the-review-tool
manifestSha256: 64-lowercase-hex-measured-by-the-review-tool
rolesSha256: 64-lowercase-hex-measured-by-the-review-tool
reviewedSourceTreeSha256: 64-lowercase-hex-measured-by-the-review-tool
ledgerPath: docs/api-evidence/operations/home.yaml
ledgerSha256: 64-lowercase-hex-measured-by-the-review-tool
fixturePath: studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/catalog/home-normal.html
fixtureSha256: 64-lowercase-hex-measured-by-the-review-tool
parserPath: studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/HomeParser.kt
parserSha256: 64-lowercase-hex-measured-by-the-review-tool
observerId: assigned-observer-id
implementerId: assigned-implementer-id
reviewerId: assigned-reviewer-id
observerRoleSeparated: true
reviewerRoleSeparated: true
implementerWildAccess: false
fixtureIndependentlyAuthored: true
selectorRationaleReviewed: true
reviewedAt: RFC3339-UTC-written-by-the-review-tool
notAfter: RFC3339-UTC-at-most-7-days-after-review
keyId: approved-reviewer-key-id
result: PASS
```

The values above are schema labels, not values to commit. `draftCleanRoomAttestation` first rejects a dirty tracked worktree, records live `git rev-parse HEAD`, computes every hash and timestamp as a literal, reads the three identity assignments from `roles.yaml`, and refuses a non-HTTPS accepted record or an untrusted key ID. `reviewedSourceTreeSha256` hashes the schema, manifest, roles, operation ledger, fixture, and parser as sorted `relativePath<TAB>rawSha256<LF>` rows. This avoids a self-reference through the attestation file while still making any current reviewed-source change invalidate the signature. The reviewer then signs the exact UTF-8 bytes of that YAML file; no canonicalization or reserialization occurs between signing and verification:

```powershell
.\gradlew.bat :verification-tools:draftCleanRoomAttestation `
  -PcleanRoomOperation=home `
  -PcleanRoomReviewerKeyId=$env:WENKU8_CLEAN_ROOM_REVIEWER_KEY_ID `
  --stacktrace
.\gradlew.bat :verification-tools:signCleanRoomAttestation `
  -PcleanRoomOperation=home `
  --stacktrace
.\gradlew.bat :verification-tools:verifyCleanRoomAttestation `
  -PcleanRoomOperation=home `
  --stacktrace
```

`signCleanRoomAttestation` re-runs all draft checks immediately before signing, then reads `WENKU8_CLEAN_ROOM_ED25519_PRIVATE_KEY_PKCS8_B64` directly from its environment, uses `KeyFactory.getInstance("Ed25519")`, `PKCS8EncodedKeySpec`, and `Signature.getInstance("Ed25519")`; it writes only one Base64 signature line to `<operation-id>.yaml.sig`, clears the decoded private-key byte array in `finally`, and never logs the key. The key is never placed in `-P`, a command line, a file, or a Gradle property. The private key and trusted reviewer identity are available only in the reviewer workspace. `verifyCleanRoomAttestation` has no private-key input and rechecks the detached signature, pairwise-distinct assigned roles, trust window, current reviewed-source tree, and reviewed commit ancestry. Possession of the public tool cannot let an implementer forge reviewer evidence.

- [ ] **Step 8: Implement the signature gate, negative fixtures, and generated accepted index**

`CleanRoomEvidenceGate.verify(repoRoot, docsRoot, outputIndex, now)` performs these checks before writing anything:

1. Parse the manifest, all 32 records, `roles.yaml`, and `trusted-reviewer-keys.yaml` with duplicate-key and alias rejection.
2. Reject every `ACCEPTED` record unless request scheme/host/port/method/path/fields, response statuses/limits/redirects, fixture/parser paths, parser revision, and both exact attestation paths are concrete and release-enabled.
3. Resolve ledger, fixture, parser, attestation, and signature with normalized paths and reject path escape or symlink escape.
4. Recompute SHA-256 over the exact schema, manifest, roles, ledger, fixture, and parser bytes; recompute the sorted reviewed-source tree hash; compare all repeated values in the record and attestation; and require `reviewedCommit` to be a live 40-character commit that is an ancestor of the checked-out HEAD.
5. Require three pairwise-distinct identities matching the current role assignments, require the reviewer assignment to own the trusted signing key ID, and require all five separation booleans to have the safe values shown above.
6. Require `result: PASS`, `reviewedAt <= now + 5 minutes`, `reviewedAt >= now - 7 days`, `notAfter >= now`, and `notAfter <= reviewedAt + 7 days`.
7. Require an `Ed25519` trusted key whose approval window covers `reviewedAt`, whose `revokedAt` is null, and whose key ID matches the attestation; verify the detached Base64 signature against the exact attestation bytes.
8. Build the index in memory only after every accepted record passes. Atomically write a sorted index containing operation ID plus ledger/fixture/parser/attestation/signature hashes. Fixture-only records never appear.

The verifier's signature core is exact and provider-independent:

```kotlin
private fun verifyDetached(attestation: Path, signatureFile: Path, encodedPublicKey: String) {
    val publicKey = KeyFactory.getInstance("Ed25519").generatePublic(
        X509EncodedKeySpec(Base64.getDecoder().decode(encodedPublicKey)),
    )
    val signature = Signature.getInstance("Ed25519")
    signature.initVerify(publicKey)
    signature.update(Files.readAllBytes(attestation))
    val detached = Base64.getDecoder().decode(Files.readString(signatureFile).trim())
    require(signature.verify(detached)) { "clean-room-attestation-signature-invalid" }
}
```

Add parameterized `CleanRoomEvidenceGateTest` fixtures for: valid fixture-only inventory, valid signed accepted record, tampered schema, manifest, roles, ledger, fixture, parser, reviewed-source tree, attestation, and signature; unknown/non-ancestor reviewed commit; malformed signature; unknown, revoked, future, and expired reviewer keys; reviewer/key-owner mismatch; future/expired review; `result != PASS`; overlapping or mismatched roles; path/symlink escape; accepted record absent from or extra in a preexisting index; and duplicate YAML keys. `CleanRoomAttestationToolTest` separately proves draft refuses a dirty tracked tree, sign refuses a stale/tampered draft, verify needs no private key, signature bytes are deterministic input bytes rather than reserialized YAML, and decoded key bytes are cleared on success and failure. Each negative case must fail with its named rule and must leave no output index.

Register the gate before any resource copy:

```groovy
// verification-tools/build.gradle
def requireExecutionProperty = { String name ->
    def value = providers.gradleProperty(name).orNull
    if (!value) throw new GradleException("Required execution property is missing: ${name}")
    value
}

tasks.register('verifyCleanRoomEvidence', JavaExec) {
    group = 'verification'
    dependsOn tasks.named('classes')
    classpath = sourceSets.main.runtimeClasspath
    mainClass.set(application.mainClass)
    args 'verifyCleanRoomEvidence', new File(rootProject.projectDir, '../..').canonicalPath,
        new File(rootProject.projectDir, '../../docs').canonicalPath,
        new File(rootProject.projectDir, 'api-public/build/generated/accepted-evidence/api-evidence/accepted-operation-index.yaml').absolutePath
}
tasks.register('draftCleanRoomAttestation', JavaExec) {
    group = 'verification'
    dependsOn tasks.named('classes')
    classpath = sourceSets.main.runtimeClasspath
    mainClass.set(application.mainClass)
    doFirst {
        setArgs([
            'draftCleanRoomAttestation', new File(rootProject.projectDir, '../..').canonicalPath,
            requireExecutionProperty('cleanRoomOperation'),
            requireExecutionProperty('cleanRoomReviewerKeyId'),
        ])
    }
}
tasks.register('signCleanRoomAttestation', JavaExec) {
    group = 'verification'
    dependsOn tasks.named('classes')
    classpath = sourceSets.main.runtimeClasspath
    mainClass.set(application.mainClass)
    doFirst {
        def secret = providers.environmentVariable('WENKU8_CLEAN_ROOM_ED25519_PRIVATE_KEY_PKCS8_B64').orNull
        if (!secret) throw new GradleException('Reviewer signing key environment is required')
        environment 'WENKU8_CLEAN_ROOM_ED25519_PRIVATE_KEY_PKCS8_B64', secret
        setArgs([
            'signCleanRoomAttestation', new File(rootProject.projectDir, '../..').canonicalPath,
            requireExecutionProperty('cleanRoomOperation'),
        ])
    }
}
tasks.register('verifyCleanRoomAttestation', JavaExec) {
    group = 'verification'
    dependsOn tasks.named('classes')
    classpath = sourceSets.main.runtimeClasspath
    mainClass.set(application.mainClass)
    doFirst {
        setArgs([
            'verifyCleanRoomAttestation', new File(rootProject.projectDir, '../..').canonicalPath,
            requireExecutionProperty('cleanRoomOperation'),
        ])
    }
}
```

```groovy
// api-public/build.gradle
def acceptedEvidenceGate = project(':verification-tools').tasks.named('verifyCleanRoomEvidence')
tasks.named('processResources') {
    dependsOn acceptedEvidenceGate
    from(layout.buildDirectory.dir('generated/accepted-evidence'))
    from(rootProject.file('../../docs/api-evidence/operations')) { into 'api-evidence/operations' }
}
tasks.named('test') { dependsOn acceptedEvidenceGate }
```

`VerificationMain.kt` registers all four command branches before its existing dispatcher: `verifyCleanRoomEvidence` calls `CleanRoomEvidenceGate.verify`, `draftCleanRoomAttestation` calls the hash/time/role draft writer, `signCleanRoomAttestation` calls the environment-only signer, and `verifyCleanRoomAttestation` calls the public-key verifier for one operation. Each branch requires its exact argument count and rejects extras. `AcceptedEvidenceLoader` reads `accepted-operation-index.yaml` first and loads only IDs and exact ledger hashes present in that generated index; it never decides acceptance from a record's nonblank fields alone. `./gradlew tasks` and `./gradlew help` configure successfully without any clean-room properties; missing properties fail only when the corresponding draft/sign/verify task executes.

Run: `.\gradlew.bat :verification-tools:tasks :verification-tools:test --tests "*.CleanRoomEvidenceGateTest" --tests "*.CleanRoomAttestationToolTest" :verification-tools:verifyCleanRoomEvidence :api-public:test --tests "*.ApiEvidenceContractTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; all negative fixtures are rejected, the generated index is empty for the initial 32 fixture-only records, and no unsigned, stale, role-overlapping, hash-mismatched, or untrusted accepted record can enter runtime resources.

- [ ] **Step 9: Commit the evidence boundary**

```powershell
git add ..\..\docs\api-evidence api-public/build.gradle api-public/src/test verification-tools
git commit -m "docs(api): establish clean-room operation evidence gate"
```

### Task 6: Enforce HTTPS Host Policy and Redirect Validation Before Dispatch

**Depends on:** Tasks 1 and 2.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/policy/NetworkOperationScope.kt`
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/policy/Wenku8HostPolicy.kt`
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/okhttp/PolicyRedirectInterceptor.kt`
- Test: `studio-android/LightNovelLibrary/core/network/src/test/kotlin/org/mewx/wenku8/core/network/policy/Wenku8HostPolicyTest.kt`
- Test: `studio-android/LightNovelLibrary/core/network/src/test/kotlin/org/mewx/wenku8/core/network/okhttp/PolicyRedirectInterceptorTest.kt`

- [ ] **Step 1: Write failing host-policy tests for every prohibited class**

```kotlin
package org.mewx.wenku8.core.network.policy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Wenku8HostPolicyTest {
    private val policy = Wenku8HostPolicy(setOf("www.wenku8.net"), emptySet())

    @Test fun allowsOnlyTheCanonicalHttpsOrigin() {
        val decision = policy.evaluate(request(target("https", "www.wenku8.net", 443)))
        assertTrue(decision is HostDecision.Allow)
    }

    @Test fun rejectsCleartextMutationAndKnownCleartextAppHost() {
        assertEquals(HostDecision.Deny("cleartext-forbidden"), policy.evaluate(request(target("http", "www.wenku8.net", 80), NetworkMethod.POST)))
        assertEquals(HostDecision.Deny("cleartext-forbidden"), policy.evaluate(request(target("http", "www.wenku8.net", 80), NetworkMethod.GET)))
        assertEquals(HostDecision.Deny("host-not-allowlisted"), policy.evaluate(request(target("https", "app.wenku8.com", 443))))
    }

    @Test fun rejectsUserInfoLiteralIpUnknownPortAndAuthenticatedOriginChange() {
        assertEquals("userinfo-forbidden", (policy.evaluate(request(target("https", "www.wenku8.net", 443, hasUserInfo = true))) as HostDecision.Deny).rule)
        assertEquals("literal-ip-forbidden", (policy.evaluate(request(target("https", "203.0.113.4", 443, literalIp = true))) as HostDecision.Deny).rule)
        assertEquals("non-default-port", (policy.evaluate(request(target("https", "www.wenku8.net", 8443))) as HostDecision.Deny).rule)
        val changed = request(target("https", "images.example.test", 443), authenticated = true, source = NetworkOrigin("https", "www.wenku8.net", 443))
        assertEquals("authenticated-origin-change", (Wenku8HostPolicy(setOf("www.wenku8.net"), setOf("images.example.test")).evaluate(changed) as HostDecision.Deny).rule)
    }

    private fun target(scheme: String, host: String, port: Int, hasUserInfo: Boolean = false, literalIp: Boolean = false) =
        NetworkTarget(NetworkOrigin(scheme, host, port), "/path", hasUserInfo, literalIp)

    private fun request(
        target: NetworkTarget,
        method: NetworkMethod = NetworkMethod.GET,
        authenticated: Boolean = false,
        source: NetworkOrigin? = null,
    ) = OutboundRequest("test", target, method, source, authenticated, 0, SensitiveHeader.entries.toSet())
}
```

- [ ] **Step 2: Run host-policy tests and confirm RED**

Run: `.\gradlew.bat :core:network:test --tests "org.mewx.wenku8.core.network.policy.Wenku8HostPolicyTest" --stacktrace`

Expected: FAIL because `Wenku8HostPolicy` is missing.

- [ ] **Step 3: Implement the exact pure policy**

```kotlin
package org.mewx.wenku8.core.network.policy

class Wenku8HostPolicy(
    contentHosts: Set<String>,
    imageHosts: Set<String>,
) : HostPolicy {
    private val contentHosts = contentHosts.map(String::lowercase).toSet()
    private val imageHosts = imageHosts.map(String::lowercase).toSet()

    override fun evaluate(request: OutboundRequest): HostDecision {
        val target = request.target
        val origin = target.origin.copy(scheme = target.origin.scheme.lowercase(), host = target.origin.host.lowercase())
        if (target.hasUserInfo) return HostDecision.Deny("userinfo-forbidden")
        if (target.literalIp) return HostDecision.Deny("literal-ip-forbidden")

        if (origin.scheme == "http") return HostDecision.Deny("cleartext-forbidden")

        if (origin.scheme != "https") return HostDecision.Deny("scheme-not-https")
        if (origin.port != 443) return HostDecision.Deny("non-default-port")
        if (origin.host !in contentHosts && origin.host !in imageHosts) return HostDecision.Deny("host-not-allowlisted")

        val changedOrigin = request.sourceOrigin?.let {
            it.scheme.lowercase() != origin.scheme || it.host.lowercase() != origin.host || it.port != origin.port
        } == true
        if (changedOrigin && request.authenticated) return HostDecision.Deny("authenticated-origin-change")

        return HostDecision.Allow(
            target.copy(origin = origin),
            if (changedOrigin) request.sensitiveHeaders else emptySet(),
        )
    }
}
```

- [ ] **Step 4: Write the failing five-hop/mutation redirect tests**

```kotlin
package org.mewx.wenku8.core.network.okhttp

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.core.network.policy.HostDecision
import org.mewx.wenku8.core.network.policy.HostPolicy

class PolicyRedirectInterceptorTest {
    @Test fun sixthRedirectIsRejectedBeforeDispatch() {
        tlsFixture().use { fixture ->
            val server = fixture.server
            repeat(6) { index -> server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", "/r${index + 1}")) }
            val client = fixture.clientBuilder
                .followRedirects(false)
                .addInterceptor(PolicyRedirectInterceptor(allowAllFor(server), syntheticReadChain(server, 5)))
                .build()
            val response = runCatching {
                client.newCall(Request.Builder().url(server.url("/r0"))
                    .tag(OperationTag::class.java, OperationTag(NetworkOperationScope("home"), false)).build()).execute()
            }
            assertEquals("redirect-hop-limit", response.exceptionOrNull()?.message)
            assertEquals(5, server.requestCount)
        }
    }

    @Test fun postRedirectIsReturnedForOperationSpecificValidationAndNotFollowed() {
        tlsFixture().use { fixture ->
            val server = fixture.server
            server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", "/accepted"))
            val client = fixture.clientBuilder
                .followRedirects(false)
                .addInterceptor(PolicyRedirectInterceptor(allowAllFor(server), RedirectContracts.of(setOf(
                    RedirectRule(NetworkOperationScope("bookshelf-add"), server.url("/mutate").toString(), 302, "/accepted",
                        server.url("/accepted").toString(), "POST", follow = false),
                )))
                .build()
            val request = Request.Builder().url(server.url("/mutate")).post(ByteArray(0).toRequestBody())
                .tag(OperationTag::class.java, OperationTag(NetworkOperationScope("bookshelf-add"), authenticated = true)).build()
            client.newCall(request).execute().use { assertEquals(302, it.code) }
            assertEquals(1, server.requestCount)
        }
    }

    @Test fun unrecordedSameHostAndCleartextRedirectsStopBeforeSecondDispatch() {
        tlsFixture().use { fixture ->
            val server = fixture.server
            listOf("/not-recorded", "http://www.wenku8.net/not-recorded").forEach { location ->
                server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", location))
                val client = fixture.clientBuilder.followRedirects(false)
                    .addInterceptor(PolicyRedirectInterceptor(allowAllFor(server), RedirectContracts.none))
                    .build()
                val result = runCatching {
                    client.newCall(Request.Builder().url(server.url("/start"))
                        .tag(OperationTag::class.java, OperationTag(NetworkOperationScope("home"), false)).build()).execute()
                }
                assertEquals("redirect-contract-mismatch", result.exceptionOrNull()?.message)
            }
            assertEquals(2, server.requestCount)
        }
    }

    @Test fun identicalRedirectForAnotherOperationScopeIsRejectedBeforeSecondDispatch() {
        tlsFixture().use { fixture ->
            val server = fixture.server
            server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", "/next"))
            val rules = RedirectContracts.of(listOf(RedirectRule(
                NetworkOperationScope("catalog"), server.url("/start").toString(), 302, "/next",
                server.url("/next").toString(), "GET", follow = true,
            )))
            val client = fixture.clientBuilder.followRedirects(false)
                .addInterceptor(PolicyRedirectInterceptor(allowAllFor(server), rules)).build()
            val result = runCatching {
                client.newCall(Request.Builder().url(server.url("/start"))
                    .tag(OperationTag::class.java, OperationTag(NetworkOperationScope("home"), false)).build()).execute()
            }
            assertEquals("redirect-contract-mismatch", result.exceptionOrNull()?.message)
            assertEquals(1, server.requestCount)
        }
    }

    private fun tlsFixture(): TlsFixture {
        val held = HeldCertificate.Builder()
            .commonName("localhost")
            .addSubjectAlternativeName("localhost")
            .build()
        val serverCertificates = HandshakeCertificates.Builder().heldCertificate(held).build()
        val clientCertificates = HandshakeCertificates.Builder().addTrustedCertificate(held.certificate).build()
        val server = MockWebServer().apply {
            useHttps(serverCertificates.sslSocketFactory(), false)
            start()
        }
        return TlsFixture(
            server,
            OkHttpClient.Builder()
                .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
                .hostnameVerifier { hostname, _ -> hostname == "localhost" },
        )
    }

    private fun allowAllFor(server: MockWebServer) = HostPolicy { outbound -> HostDecision.Allow(outbound.target, emptySet()) }

    private fun syntheticReadChain(server: MockWebServer, hops: Int): RedirectContracts = RedirectContracts.of(
        (0 until hops).map { index ->
            RedirectRule(
                operationScope = NetworkOperationScope("home"),
                fromCanonicalHttpsUrl = server.url("/r$index").toString(),
                status = 302,
                recordedLocation = "/r${index + 1}",
                canonicalHttpsTarget = server.url("/r${index + 1}").toString(),
                method = "GET",
                follow = true,
            )
        },
    )
}

private class TlsFixture(
    val server: MockWebServer,
    val clientBuilder: OkHttpClient.Builder,
) : AutoCloseable {
    override fun close() = server.close()
}
```

The test file imports `org.mewx.wenku8.core.network.policy.NetworkOperationScope`. Add the transport-local value type before implementing the interceptor:

```kotlin
package org.mewx.wenku8.core.network.policy

@JvmInline
value class NetworkOperationScope(val value: String) {
    init { require(value.matches(Regex("[a-z0-9-]{1,48}"))) { "invalid-network-operation-scope" } }
}
```

This value is not the domain/API operation enum. `:api-public` is the only layer that converts its closed `OperationCode.wireId` into a `NetworkOperationScope`; `:core:network` must not import `OperationCode`, `ApiResult`, `ApiFailure`, or any other `:api-contract` symbol.

- [ ] **Step 5: Implement policy mapping and redirect handling**

```kotlin
package org.mewx.wenku8.core.network.okhttp

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response
import org.mewx.wenku8.core.network.policy.HostDecision
import org.mewx.wenku8.core.network.policy.HostPolicy
import org.mewx.wenku8.core.network.policy.NetworkOperationScope
import org.mewx.wenku8.core.network.policy.SensitiveHeader

data class OperationTag(val scope: NetworkOperationScope, val authenticated: Boolean)

class PolicyRedirectInterceptor(
    private val hostPolicy: HostPolicy,
    private val redirects: RedirectContracts,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var source = request.url.toNetworkOrigin()
        repeat(5) { hop ->
            val operation = request.tag(OperationTag::class.java) ?: throw IOException("operation-scope-missing")
            val decision = hostPolicy.evaluate(request.toOutbound(operation, source, hop))
            if (decision is HostDecision.Deny) throw IOException(decision.rule)
            decision as HostDecision.Allow
            request = request.applyDecision(decision)
            val response = chain.proceed(request)
            if (!response.isRedirect) return response
            val location = response.header("Location") ?: run {
                response.close()
                throw IOException("redirect-location-missing")
            }
            val rule = redirects.resolve(operation.scope, request.url, response.code, location, request.method) ?: run {
                response.close()
                throw IOException("redirect-contract-mismatch")
            }
            if (!rule.follow) return response
            response.close()
            source = request.url.toNetworkOrigin()
            request = request.newBuilder().url(rule.canonicalHttpsTarget).build()
        }
        throw IOException("redirect-hop-limit")
    }
}

private fun okhttp3.Request.applyDecision(decision: HostDecision.Allow): okhttp3.Request {
    val target = decision.canonicalTarget
    val url = url.newBuilder().scheme(target.origin.scheme).host(target.origin.host).port(target.origin.port).encodedPath(target.encodedPath).build()
    val builder = newBuilder().url(url)
    decision.stripHeaders.forEach {
        when (it) {
            SensitiveHeader.COOKIE -> builder.removeHeader("Cookie")
            SensitiveHeader.AUTHORIZATION -> builder.removeHeader("Authorization")
            SensitiveHeader.REFERER -> builder.removeHeader("Referer")
            SensitiveHeader.OTHER_CREDENTIAL -> builder.removeHeader("Proxy-Authorization")
        }
    }
    return builder.build()
}
```

Add the complete pure adapters in the same file:

```kotlin
data class RedirectRule(
    val operationScope: NetworkOperationScope,
    val fromCanonicalHttpsUrl: String,
    val status: Int,
    val recordedLocation: String,
    val canonicalHttpsTarget: String,
    val method: String,
    val follow: Boolean,
) {
    init {
        require(fromCanonicalHttpsUrl.toHttpUrl().scheme == "https")
        require(canonicalHttpsTarget.toHttpUrl().scheme == "https")
        require(method in setOf("GET", "HEAD", "POST"))
        require(follow == (method in setOf("GET", "HEAD")))
    }
}

class RedirectContracts private constructor(private val rules: Set<RedirectRule>) {
    fun resolve(operationScope: NetworkOperationScope, from: HttpUrl, status: Int, location: String, method: String): RedirectRule? =
        rules.singleOrNull {
            it.operationScope == operationScope && it.fromCanonicalHttpsUrl == from.toString() && it.status == status &&
                it.recordedLocation == location && it.method == method
        }

    companion object {
        val none = RedirectContracts(emptySet())
        fun of(rules: Collection<RedirectRule>) = RedirectContracts(rules.toSet())
    }
}

private fun okhttp3.HttpUrl.toNetworkOrigin() = NetworkOrigin(scheme, host, port)

private fun okhttp3.Request.toOutbound(
    operation: OperationTag,
    source: NetworkOrigin?,
    hop: Int,
): OutboundRequest {
    val present = buildSet {
        if (header("Cookie") != null) add(SensitiveHeader.COOKIE)
        if (header("Authorization") != null) add(SensitiveHeader.AUTHORIZATION)
        if (header("Referer") != null) add(SensitiveHeader.REFERER)
        if (header("Proxy-Authorization") != null) add(SensitiveHeader.OTHER_CREDENTIAL)
    }
    val networkMethod = when (method) {
        "GET" -> NetworkMethod.GET
        "HEAD" -> NetworkMethod.HEAD
        "POST" -> NetworkMethod.POST
        else -> throw IOException("method-not-allowed")
    }
    return OutboundRequest(
        operation.scope.value,
        NetworkTarget(
            url.toNetworkOrigin(),
            url.encodedPath,
            url.username.isNotEmpty() || url.password.isNotEmpty(),
            url.host.matches(Regex("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")) || url.host.contains(':'),
        ),
        networkMethod,
        source,
        operation.authenticated,
        hop,
        present,
    )
}
```

`RedirectContracts` is generated only from the accepted operation ledger. Every read rule binds the exact `NetworkOperationScope`, canonical source URL, status, raw `Location`, method, and independently verified canonical HTTPS target. A rule accepted for `catalog` cannot authorize an otherwise identical `home` redirect. This is the only place where an accepted `http://www.wenku8.net/...` Location may be converted to its reviewed HTTPS equivalent; `Wenku8HostPolicy` itself denies all cleartext. Mutation rules set `follow=false` and return the first exact response to the operation parser. An untagged request, cross-operation rule, unrecorded same-host path, altered query, protocol-relative target, or sixth hop is rejected before another dispatch.

- [ ] **Step 6: Run all policy tests and commit**

Run: `.\gradlew.bat :core:network:test --tests "org.mewx.wenku8.core.network.policy.Wenku8HostPolicyTest" --tests "org.mewx.wenku8.core.network.okhttp.PolicyRedirectInterceptorTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; cleartext POST, app host, IP, userinfo, unknown port, authenticated origin change, sixth hop, and unrecorded mutation redirect are rejected.

```powershell
git add core/network/src
git commit -m "feat(network): enforce Wenku8 HTTPS host policy"
```

### Task 7: Implement Strict GBK Decode and Byte-First Query Encoding

**Depends on:** Tasks 2 and 6.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/codec/GbkCodec.kt`
- Test: `studio-android/LightNovelLibrary/core/network/src/test/kotlin/org/mewx/wenku8/core/network/codec/GbkCodecTest.kt`

- [ ] **Step 1: Write failing codec tests**

```kotlin
package org.mewx.wenku8.core.network.codec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GbkCodecTest {
    private val codec = GbkCodec()

    @Test fun encodesChineseAsGbkBytesBeforePercentEscaping() {
        assertEquals(CodecResult.Value("%C7%E1%D0%A1%CB%B5"), codec.percentEncode("轻小说"))
    }

    @Test fun leavesOnlyRfc3986UnreservedBytes() {
        assertEquals(CodecResult.Value("A-z_~.%20"), codec.percentEncode("A-z_~. "))
    }

    @Test fun malformedInputAndBytesStayLayerLocal() {
        assertEquals(CodecResult.Invalid, codec.percentEncode("\uD800"))
        assertEquals(CodecResult.Invalid, codec.decode(byteArrayOf(0x81.toByte())))
    }
}
```

- [ ] **Step 2: Run the codec tests and confirm RED**

Run: `.\gradlew.bat :core:network:test --tests "org.mewx.wenku8.core.network.codec.GbkCodecTest" --stacktrace`

Expected: FAIL because `GbkCodec` is missing.

- [ ] **Step 3: Implement strict encoder/decoder behavior**

```kotlin
package org.mewx.wenku8.core.network.codec

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction

sealed interface CodecResult<out T> {
    data class Value<T>(val value: T) : CodecResult<T>
    data object Invalid : CodecResult<Nothing>
}

class GbkCodec {
    private val charset = java.nio.charset.Charset.forName("GBK")

    fun decode(bytes: ByteArray): CodecResult<String> = runCatching {
        charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    }.fold(
        onSuccess = CodecResult<String>::Value,
        onFailure = { CodecResult.Invalid },
    )

    fun percentEncode(value: String): CodecResult<String> = runCatching {
        val encoded = charset.newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .encode(CharBuffer.wrap(value))
        buildString {
            while (encoded.hasRemaining()) {
                val byte = encoded.get().toInt() and 0xff
                if (byte.isUnreserved()) append(byte.toChar()) else append("%%%02X".format(byte))
            }
        }
    }.fold(
        onSuccess = CodecResult<String>::Value,
        onFailure = { CodecResult.Invalid },
    )

    private fun Int.isUnreserved() = this in 'A'.code..'Z'.code || this in 'a'.code..'z'.code ||
        this in '0'.code..'9'.code || this in intArrayOf('-'.code, '.'.code, '_'.code, '~'.code)
}
```

- [ ] **Step 4: Run tests and commit**

Run: `.\gradlew.bat :core:network:test --tests "org.mewx.wenku8.core.network.codec.GbkCodecTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; the exact search encoding is `%C7%E1%D0%A1%CB%B5`, spaces are `%20`, malformed input is `CodecResult.Invalid`, and `:core:network` has no `api-contract` dependency or import.

```powershell
git add core/network/src
git commit -m "feat(network): add strict GBK codec"
```

### Task 8: Add Cancellable Calls, Bounds, Throttling, and Redacted Events

**Depends on:** Tasks 2, 6, and 7.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/okhttp/AwaitCall.kt`
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/okhttp/BoundedBodyReader.kt`
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/okhttp/RequestThrottle.kt`
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/logging/RedactedNetworkEvent.kt`
- Create: `studio-android/LightNovelLibrary/core/network/src/main/kotlin/org/mewx/wenku8/core/network/failure/NetworkFailureKind.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/ApiNetworkFailureMapper.kt`
- Test: `studio-android/LightNovelLibrary/core/network/src/test/kotlin/org/mewx/wenku8/core/network/okhttp/NetworkPrimitiveTest.kt`
- Test: `studio-android/LightNovelLibrary/core/network/src/test/kotlin/org/mewx/wenku8/core/network/logging/RedactedNetworkEventTest.kt`

- [ ] **Step 1: Write failing cancellation, size, and redaction tests**

```kotlin
package org.mewx.wenku8.core.network.okhttp

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkPrimitiveTest {
    @Test fun coroutineCancellationCancelsCall() = runTest {
        val call = RecordingNeverCompletingCall()
        val job = launch { call.awaitResponse() }
        job.cancelAndJoin()
        assertTrue(call.cancelled)
    }

    @Test fun boundedReaderRejectsDeclaredAndStreamingOverflow() {
        val declared = response("123456", declaredLength = 6)
        val streamed = response("123456", declaredLength = -1)
        assertTrue(runCatching { BoundedBodyReader.read(declared.body!!, 5) }.exceptionOrNull() is BodyLimitExceeded)
        assertTrue(runCatching { BoundedBodyReader.read(streamed.body!!, 5) }.exceptionOrNull() is BodyLimitExceeded)
    }
}

private class RecordingNeverCompletingCall : Call {
    var cancelled = false
    private val request = Request.Builder().url("https://www.wenku8.net/synthetic").build()
    override fun request() = request
    override fun execute(): Response = error("synchronous execute is not used")
    override fun enqueue(responseCallback: okhttp3.Callback) = Unit
    override fun cancel() { cancelled = true }
    override fun isExecuted() = false
    override fun isCanceled() = cancelled
    override fun timeout() = okio.Timeout.NONE
    override fun clone(): Call = RecordingNeverCompletingCall()
}

private fun response(text: String, declaredLength: Long): Response {
    val bytes = text.toByteArray()
    val body = object : okhttp3.ResponseBody() {
        override fun contentType() = "text/plain".toMediaType()
        override fun contentLength() = declaredLength
        override fun source() = okio.Buffer().write(bytes)
    }
    return Response.Builder()
        .request(Request.Builder().url("https://www.wenku8.net/synthetic").build())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(body)
        .build()
}
```

```kotlin
package org.mewx.wenku8.core.network.logging

import org.junit.Assert.assertFalse
import org.junit.Test

class RedactedNetworkEventTest {
    @Test fun renderedEventCannotContainSuppliedSensitiveValues() {
        val event = RedactedNetworkEvent("profile", "Auth.SessionExpired", "trace-9")
        val rendered = event.toString()
        listOf("username-value", "password-value", "cookie-value", "captcha-value", "<html>").forEach {
            assertFalse(rendered.contains(it))
        }
    }
}
```

- [ ] **Step 2: Run primitive tests and confirm RED**

Run: `.\gradlew.bat :core:network:test --tests "*.NetworkPrimitiveTest" --tests "*.RedactedNetworkEventTest" --stacktrace`

Expected: FAIL because the call bridge, body reader, and event model are missing.

- [ ] **Step 3: Implement the cancellation-propagating call bridge**

```kotlin
package org.mewx.wenku8.core.network.okhttp

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

suspend fun Call.awaitResponse(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, error: IOException) {
            if (continuation.isActive) continuation.resumeWithException(error)
        }

        override fun onResponse(call: Call, response: Response) {
            if (continuation.isActive) continuation.resume(response) else response.close()
        }
    })
}
```

- [ ] **Step 4: Implement bounded reads, a non-mutation throttle, and allowlisted event values**

```kotlin
package org.mewx.wenku8.core.network.okhttp

import okhttp3.ResponseBody

class BodyLimitExceeded(val limitBytes: Long) : Exception("response-body-limit")

object BoundedBodyReader {
    fun read(body: ResponseBody, limitBytes: Long): ByteArray {
        require(limitBytes > 0)
        val declared = body.contentLength()
        if (declared > limitBytes) throw BodyLimitExceeded(limitBytes)
        val source = body.source()
        val bytes = source.readByteArray(limitBytes + 1)
        if (bytes.size > limitBytes) throw BodyLimitExceeded(limitBytes)
        return bytes
    }
}
```

```kotlin
package org.mewx.wenku8.core.network.okhttp

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

class RequestThrottle(
    maxConcurrentReads: Int,
    private val minReadSpacingMillis: Long,
    private val nowMillis: () -> Long,
) {
    private val permits = Semaphore(maxConcurrentReads)
    private val mutationPermit = Semaphore(1)
    private val spacing = Mutex()
    private var nextReadAt = 0L

    suspend fun <T> read(block: suspend () -> T): T = permits.withPermit {
        spacing.withLock {
            val wait = (nextReadAt - nowMillis()).coerceAtLeast(0)
            if (wait > 0) delay(wait)
            nextReadAt = nowMillis() + minReadSpacingMillis
        }
        block()
    }

    suspend fun <T> mutation(block: suspend () -> T): T = mutationPermit.withPermit { block() }
}
```

```kotlin
package org.mewx.wenku8.core.network.logging

data class RedactedNetworkEvent(
    val operationCode: String,
    val failureClass: String?,
    val traceId: String,
) {
    init {
        require(operationCode.matches(Regex("[a-z0-9-]{1,48}")))
        require(failureClass == null || failureClass.matches(Regex("[A-Za-z0-9.]{1,64}")))
        require(traceId.matches(Regex("[A-Za-z0-9-]{1,64}")))
    }
}
```

- [ ] **Step 5: Add distinct timeout/failure mapping without catching cancellation**

```kotlin
package org.mewx.wenku8.core.network.failure

enum class NetworkFailureKind { OFFLINE, DNS, CONNECT, TLS, TIMEOUT }

fun Throwable.toNetworkFailureKind(): NetworkFailureKind = when (this) {
    is java.net.UnknownHostException -> NetworkFailureKind.DNS
    is javax.net.ssl.SSLException -> NetworkFailureKind.TLS
    is java.net.ConnectException -> NetworkFailureKind.CONNECT
    is java.net.SocketTimeoutException, is java.io.InterruptedIOException -> NetworkFailureKind.TIMEOUT
    else -> NetworkFailureKind.OFFLINE
}
```

Keep the API mapping in `:api-public`:

```kotlin
package org.mewx.wenku8.api.publicprovider.transport

import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.core.network.failure.NetworkFailureKind

internal fun NetworkFailureKind.toApiFailure(): ApiFailure.Network = when (this) {
    NetworkFailureKind.OFFLINE -> ApiFailure.Network.Offline
    NetworkFailureKind.DNS -> ApiFailure.Network.Dns
    NetworkFailureKind.CONNECT -> ApiFailure.Network.Connect
    NetworkFailureKind.TLS -> ApiFailure.Network.Tls
    NetworkFailureKind.TIMEOUT -> ApiFailure.Network.Timeout
}
```

API callers execute `catch (cancelled: CancellationException) { throw cancelled }` before calling `error.toNetworkFailureKind().toApiFailure()`. Add `CoreNetworkDependencyTest` to parse `core/network/build.gradle` and production imports, rejecting `api-contract`, `ApiResult`, `ApiFailure`, `ProviderCapability`, or `OperationCode` anywhere under `core/network/src/main`.

- [ ] **Step 6: Run primitive tests and commit**

Run: `.\gradlew.bat :core:network:test --tests "*.NetworkPrimitiveTest" --tests "*.RedactedNetworkEventTest" --tests "*.CoreNetworkDependencyTest" :core:network:compileKotlin :api-public:compileKotlin --stacktrace`

Expected: `BUILD SUCCESSFUL`; cancellation sets `Call.cancelled`, both body overflow modes fail, event construction has no sensitive field, and `:core:network` compiles without any `:api-contract` edge.

```powershell
git add core/network/src
git commit -m "feat(network): add bounded cancellable transport primitives"
```

### Task 9: Build the Accepted-Operation Transport and Masquerade Guard

**Depends on:** Tasks 5 and 8.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/evidence/OperationEvidence.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/evidence/OperationRegistry.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/evidence/AcceptedEvidenceLoader.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/PublicHttpClientFactory.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/PublicTextTransport.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/PublicTransport.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/PageGuard.kt`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/transport/PublicTransportTest.kt`

- [ ] **Step 1: Write failing no-evidence, challenge, empty, and cancellation tests**

```kotlin
package org.mewx.wenku8.api.publicprovider.transport

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult

class PublicTransportTest {
    @Test fun missingAcceptedEvidenceFailsBeforeNetwork() = runTest {
        MockWebServer().use { server ->
            val transport = transport(server, OperationRegistry.empty())
            val result = transport.getText("home", emptyMap(), authenticated = false)
            assertEquals(ApiFailure.ProtocolViolation("evidence-not-accepted:home"), (result as ApiResult.Failure).error)
            assertEquals(0, server.requestCount)
        }
    }

    @Test fun challengeAndLoginMasqueradesNeverReachDomainParser() = runTest {
        listOf(
            MockResponse().setResponseCode(403).setBody("challenge"),
            MockResponse().setResponseCode(200).setBody("<html><form data-contract='login-page'></form></html>"),
            MockResponse().setResponseCode(200).setBody(""),
        ).forEach { response ->
            MockWebServer().use { server ->
                server.enqueue(response)
                val result = transport(server, acceptedRegistry(server)).getText("home", emptyMap(), false)
                assertTrue(result is ApiResult.Failure)
            }
        }
    }

    @Test fun cancellationPropagatesAndCancelsSocket() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val call = async { transport(server, acceptedRegistry(server)).getText("home", emptyMap(), false) }
            call.cancelAndJoin()
            assertTrue(call.isCancelled)
        }
    }
}
```

- [ ] **Step 2: Run transport tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.transport.PublicTransportTest" --stacktrace`

Expected: FAIL because registry, factory, transport, and page guard are missing.

- [ ] **Step 3: Add immutable accepted-operation records and fail-closed lookup**

```kotlin
package org.mewx.wenku8.api.publicprovider.evidence

import org.mewx.wenku8.core.network.okhttp.RedirectRule
import org.mewx.wenku8.core.network.policy.NetworkOperationScope
import org.mewx.wenku8.core.network.policy.NetworkMethod

data class OperationEvidence(
    val operationId: String,
    val method: NetworkMethod,
    val canonicalHttpsOrigin: String,
    val encodedPathTemplate: String,
    val queryFields: Set<String>,
    val formFields: Set<String>,
    val authenticated: Boolean,
    val successStatuses: Set<Int>,
    val responseCharset: String,
    val maxResponseBytes: Long,
    val parserContractRevision: Int,
    val fixtureSha256: String,
    val redirects: List<RedirectRule>,
) {
    init {
        require(redirects.all { it.operationScope == NetworkOperationScope(operationId) })
        require(redirects.distinctBy { listOf(it.fromCanonicalHttpsUrl, it.status, it.recordedLocation, it.method) }.size == redirects.size)
    }
}

class OperationRegistry private constructor(private val accepted: Map<String, OperationEvidence>) {
    fun accepted(operationId: String): OperationEvidence? = accepted[operationId]
    fun ids(): Set<String> = accepted.keys
    fun records(): List<OperationEvidence> = accepted.values.sortedBy { it.operationId }

    companion object {
        fun of(records: List<OperationEvidence>) = OperationRegistry(records.associateBy { it.operationId })
        fun empty() = OperationRegistry(emptyMap())
    }
}
```

Task 5 already wires the checked records plus the generated cryptographically verified index into JVM resources. Load only records named by that index:

```kotlin
package org.mewx.wenku8.api.publicprovider.evidence

import java.security.MessageDigest
import org.mewx.wenku8.core.network.okhttp.RedirectRule
import org.mewx.wenku8.core.network.policy.NetworkMethod
import org.yaml.snakeyaml.Yaml

class AcceptedEvidenceLoader(private val loader: ClassLoader = checkNotNull(AcceptedEvidenceLoader::class.java.classLoader)) {
    private val yaml = Yaml()

    fun load(): OperationRegistry {
        val manifest = read("api-evidence/operations/manifest.yaml")
        val manifestIds = (manifest.getValue("operations") as List<Map<String, Any>>)
            .mapTo(linkedSetOf()) { it.getValue("id") as String }
        val index = read("api-evidence/accepted-operation-index.yaml")
        val verifiedRows = index.getValue("operations") as List<Map<String, Any>>
        require(verifiedRows.map { it.getValue("operationId") }.distinct().size == verifiedRows.size)
        val accepted = verifiedRows.map { verified ->
            val id = verified.getValue("operationId") as String
            require(id in manifestIds) { "verified-operation-not-in-manifest" }
            val recordBytes = readBytes("api-evidence/operations/$id.yaml")
            require(sha256(recordBytes) == verified.getValue("ledgerSha256")) { "verified-ledger-hash-mismatch" }
            val record = yaml.load<Map<String, Any>>(recordBytes.inputStream())
            require(record["state"] == "ACCEPTED" && record["releaseEnabled"] == true)
            val request = record.getValue("request") as Map<String, Any>
            val response = record.getValue("response") as Map<String, Any>
            require(request["scheme"] == "https")
            val hash = record.getValue("fixtureSha256") as String
            require(hash == verified.getValue("fixtureSha256"))
            OperationEvidence(
                id,
                NetworkMethod.valueOf((request.getValue("method") as String).uppercase()),
                "https://${request.getValue("host")}:443",
                request.getValue("pathTemplate") as String,
                (request.getValue("queryFields") as List<String>).toSet(),
                (request.getValue("formFields") as List<String>).toSet(),
                request.getValue("authenticated") as Boolean,
                (response.getValue("successStatuses") as List<Int>).toSet(),
                record.getValue("charset") as String,
                (response.getValue("maxResponseBytes") as Number).toLong(),
                (record.getValue("parserContractRevision") as Number).toInt(),
                hash,
                parseRedirectRules(id, request, response),
            )
        }
        return OperationRegistry.of(accepted)
    }

    private fun read(path: String): Map<String, Any> = loader.getResourceAsStream(path).use { stream ->
        requireNotNull(stream) { "evidence-resource-missing" }
        yaml.load(stream)
    }

    private fun readBytes(path: String): ByteArray = loader.getResourceAsStream(path).use { stream ->
        requireNotNull(stream) { "evidence-resource-missing" }.readBytes()
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun parseRedirectRules(
        operationId: String,
        request: Map<String, Any>,
        response: Map<String, Any>,
    ): List<RedirectRule> {
        val source = "https://${request.getValue("host")}:443${request.getValue("pathTemplate")}"
        val method = (request.getValue("method") as String).uppercase()
        val rows = response["redirects"] as? List<Map<String, Any>> ?: emptyList()
        return rows.map { row ->
            RedirectRule(
                operationScope = NetworkOperationScope(operationId),
                fromCanonicalHttpsUrl = row["fromCanonicalHttpsUrl"] as? String ?: source,
                status = (row.getValue("status") as Number).toInt(),
                recordedLocation = row.getValue("recordedLocation") as String,
                canonicalHttpsTarget = row.getValue("canonicalHttpsTarget") as String,
                method = row["method"] as? String ?: method,
                follow = row.getValue("follow") as Boolean,
            )
        }
    }
}
```

The release build depends on the Phase 0 publication/authorization verifier and `verifyCleanRoomEvidence` before `processResources`. A fixture-only record is absent from the generated index and can never create a production capability, even if someone tampers its raw record fields to look accepted.

- [ ] **Step 4: Implement client construction with no automatic redirects or retries**

```kotlin
package org.mewx.wenku8.api.publicprovider.transport

import java.time.Duration
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import org.mewx.wenku8.core.network.okhttp.RedirectContracts
import org.mewx.wenku8.core.network.okhttp.PolicyRedirectInterceptor
import org.mewx.wenku8.core.network.policy.HostPolicy

class PublicHttpClientFactory(
    private val hostPolicy: HostPolicy,
    private val redirects: RedirectContracts,
    private val newBuilder: () -> OkHttpClient.Builder = { OkHttpClient.Builder() },
) {
    fun anonymous(): OkHttpClient = base(CookieJar.NO_COOKIES)
    fun authenticated(jar: CookieJar): OkHttpClient = base(jar)
    fun attempt(jar: CookieJar): OkHttpClient = base(jar)

    private fun base(jar: CookieJar) = newBuilder()
        .cookieJar(jar)
        .followRedirects(false)
        .followSslRedirects(false)
        .retryOnConnectionFailure(false)
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(20))
        .callTimeout(Duration.ofSeconds(30))
        .addInterceptor(PolicyRedirectInterceptor(hostPolicy, redirects))
        .build()
}
```

`newBuilder` is an infrastructure seam, not a policy seam: production supplies the default builder, while the TLS fixture supplies only its test CA, loopback DNS, shared dispatcher, and connection pool. Every builder still receives the same no-redirect/no-retry timeouts and `PolicyRedirectInterceptor`; tests cannot replace or omit those rules.

- [ ] **Step 5: Implement pre-parser page rejection**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.jsoup.Jsoup
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.OperationCode

class PageGuard {
    fun reject(operationCode: OperationCode, status: Int, mediaType: String?, text: String): ApiFailure? {
        if (status == 403) return ApiFailure.ChallengeBlocked
        if (text.isBlank()) return ApiFailure.ProtocolViolation("blank-body:${operationCode.wireId}")
        if (mediaType != null && !mediaType.contains("html") && !mediaType.contains("xml") && !mediaType.contains("text")) {
            return ApiFailure.ProtocolViolation("content-type:${operationCode.wireId}")
        }
        val document = Jsoup.parse(text)
        if (document.selectFirst("form[data-contract=login-page]") != null) return ApiFailure.Auth.SessionExpired
        if (document.selectFirst("[data-contract=challenge-page]") != null) return ApiFailure.ChallengeBlocked
        if (document.selectFirst("[data-contract=block-page]") != null) return ApiFailure.ChallengeBlocked
        if (document.selectFirst("[data-contract=error-page]") != null) return ApiFailure.Parse(operationCode, 1)
        return null
    }
}
```

The `data-contract` selectors above exist only in project-authored synthetic guard fixtures. When an accepted sanitized fixture establishes the site's independent semantic markers, add them as separately named checks with a source comment containing only the evidence operation ID and contract revision, never a Wild reference.

- [ ] **Step 6: Implement transport with cancellation propagation and no invalid-body caching**

```kotlin
package org.mewx.wenku8.api.publicprovider.transport

import org.mewx.wenku8.api.contract.ApiResult

interface PublicTextTransport {
    suspend fun text(
        operationId: String,
        path: Map<String, String> = emptyMap(),
        query: Map<String, String> = emptyMap(),
        authenticated: Boolean = false,
    ): ApiResult<String>
}
```

```kotlin
package org.mewx.wenku8.api.publicprovider.transport

import kotlinx.coroutines.CancellationException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.api.publicprovider.evidence.OperationRegistry
import org.mewx.wenku8.api.publicprovider.transport.toApiFailure
import org.mewx.wenku8.api.publicprovider.parser.PageGuard
import org.mewx.wenku8.core.network.codec.CodecResult
import org.mewx.wenku8.core.network.codec.GbkCodec
import org.mewx.wenku8.core.network.okhttp.BoundedBodyReader
import org.mewx.wenku8.core.network.okhttp.OperationTag
import org.mewx.wenku8.core.network.okhttp.RequestThrottle
import org.mewx.wenku8.core.network.okhttp.awaitResponse
import org.mewx.wenku8.core.network.failure.toNetworkFailureKind
import org.mewx.wenku8.core.network.policy.NetworkOperationScope

class PublicTransport(
    private val operations: OperationRegistry,
    private val clients: PublicHttpClientFactory,
    private val gbk: GbkCodec,
    private val pageGuard: PageGuard,
    private val throttle: RequestThrottle,
    private val nowMillis: () -> Long,
    private val newTraceId: () -> String,
) : PublicTextTransport {
    override suspend fun text(
        operationId: String,
        path: Map<String, String>,
        query: Map<String, String>,
        authenticated: Boolean,
    ): ApiResult<String> = getText(operationId, path, query, authenticated)

    suspend fun getText(operationId: String, query: Map<String, String>, authenticated: Boolean): ApiResult<String> =
        getText(operationId, emptyMap(), query, authenticated)

    suspend fun getText(
        operationId: String,
        path: Map<String, String> = emptyMap(),
        query: Map<String, String>,
        authenticated: Boolean,
    ): ApiResult<String> {
        val operation = operations.accepted(operationId)
            ?: return failure(ApiFailure.ProtocolViolation("evidence-not-accepted:$operationId"))
        if (operation.authenticated != authenticated) return failure(ApiFailure.ProtocolViolation("auth-contract:$operationId"))
        if (!operation.queryFields.containsAll(query.keys)) return failure(ApiFailure.ProtocolViolation("query-contract:$operationId"))
        val expandedPath = EncodedPathTemplate(operation.encodedPathTemplate).expand(path)
            ?: return failure(ApiFailure.ProtocolViolation("path-contract:$operationId"))
        val base = operation.canonicalHttpsOrigin.toHttpUrl().newBuilder().encodedPath(expandedPath).build()
        val url = base.newBuilder().apply { query.forEach { (name, value) -> addEncodedQueryParameter(name, value) } }.build()
        val operationCode = OperationCode.fromWireId(operationId)
            ?: return failure(ApiFailure.ProtocolViolation("unknown-operation-code"))
        val request = Request.Builder().url(url).get()
            .tag(OperationTag::class.java, OperationTag(NetworkOperationScope(operationCode.wireId), authenticated)).build()
        return try {
            if (authenticated) return failure(ApiFailure.ProtocolViolation("authenticated-transport-required"))
            val client = clients.anonymous()
            throttle.read {
                client.newCall(request).awaitResponse().use { response ->
                    if (response.code !in operation.successStatuses) return@read failure(ApiFailure.Http(response.code, null))
                    val body = response.body ?: return@read failure(ApiFailure.ProtocolViolation("body-missing:$operationId"))
                    val bytes = BoundedBodyReader.read(body, operation.maxResponseBytes)
                    val decoded = try { gbk.decode(bytes) } finally { bytes.fill(0) }
                    val text = when (decoded) {
                        is CodecResult.Value -> decoded.value
                        CodecResult.Invalid -> return@read failure(ApiFailure.Decode("GBK"))
                    }
                    pageGuard.reject(operationCode, response.code, response.header("Content-Type"), text)?.let { return@read failure(it) }
                    ApiResult.Success(text, ResponseMetadata("public", nowMillis(), Freshness.FRESH))
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            failure(error.toNetworkFailureKind().toApiFailure())
        }
    }

    private fun failure(error: ApiFailure) = ApiResult.Failure(error, newTraceId())
}

class EncodedPathTemplate(private val template: String) {
    private val marker = Regex("\\{([A-Za-z][A-Za-z0-9_-]{0,31})}")
    private val names = marker.findAll(template).map { it.groupValues[1] }.toList()

    fun expand(values: Map<String, String>): String? {
        if (values.keys != names.toSet() || names.size != names.toSet().size) return null
        var expanded = template
        names.forEach { name ->
            val value = values.getValue(name)
            if (value.isBlank() || value.length > 128 || '/' in value || '\\' in value || value == "." || value == "..") return null
            val encoded = HttpUrl.Builder().scheme("https").host("template.invalid").addPathSegment(value).build()
                .encodedPathSegments.single()
            expanded = expanded.replace("{$name}", encoded)
        }
        return expanded.takeIf { it.startsWith('/') && !marker.containsMatchIn(it) }
    }
}
```

- [ ] **Step 7: Run transport tests and commit**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.transport.PublicTransportTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; unaccepted operations dispatch zero requests, blank/challenge/login masquerades are typed failures, and cancellation is not converted to `ApiFailure`.

```powershell
git add api-public/src core/network/src
git commit -m "feat(api): add evidence-backed public transport"
```

### Task 10: Parse Home, Browse, Tag Discovery, Tag Results, and Search

**Depends on:** Tasks 5 and 9.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/ParserSupport.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/HomeParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/BrowseParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/TagParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/SearchParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/catalog/home-normal.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/catalog/browse-paged.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/catalog/tags.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/catalog/novels-by-tag.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/catalog/search-single-page.html`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/parser/CatalogListParserTest.kt`

- [ ] **Step 1: Author minimal synthetic fixtures with no copied site body or selectors**

```html
<!-- home-normal.html -->
<!doctype html><html><body>
<section data-contract="home-section" data-id="featured">
  <h2 data-field="title">Featured</h2>
  <article data-contract="novel" data-id="101">
    <h3 data-field="title">Synthetic Novel A</h3>
    <span data-field="author">Synthetic Author</span>
    <img data-field="cover" src="/synthetic/covers/101.jpg" alt="">
  </article>
</section>
</body></html>
```

```html
<!-- browse-paged.html -->
<!doctype html><html><body>
<article data-contract="novel" data-id="102">
  <span data-field="author">Optional Author</span><h3 data-field="title">Synthetic Novel B</h3>
</article>
<a data-contract="next-page" data-page="3" href="?page=3">next</a>
</body></html>
```

```html
<!-- tags.html -->
<!doctype html><html><body>
<section data-contract="tag-group" data-id="genre"><h2>Genre</h2>
  <a data-contract="tag" data-id="fantasy" data-count="12">Fantasy</a>
</section>
</body></html>
```

```html
<!-- novels-by-tag.html -->
<!doctype html><html><body>
<article data-contract="novel" data-id="103"><h3 data-field="title">Synthetic Tagged Novel</h3></article>
<a data-contract="next-page" data-page="2" href="?page=2">next</a>
</body></html>
```

```html
<!-- search-single-page.html -->
<!doctype html><html><body>
<article data-contract="novel" data-id="104"><h3 data-field="title">Synthetic Search Novel</h3></article>
</body></html>
```

These are new minimal documents and contain no real title, author, account, copyrighted passage, or Wild expression.

- [ ] **Step 2: Write failing parser tests for order, optional fields, and pagination**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.settings.ContentLanguage

class CatalogListParserTest {
    private val context = ParserContext("public", "https://www.wenku8.net/", 1) { "trace-parser" }

    @Test fun homePreservesSectionAndNovelOrder() {
        val result = HomeParser(context).parse(fixture("catalog/home-normal.html"), ContentLanguage.SIMPLIFIED_CHINESE) as ApiResult.Success
        assertEquals("featured", result.value.single().id)
        assertEquals("101", result.value.single().novels.single().key.remoteId)
    }

    @Test fun browseAllowsMissingOptionalCoverAndFindsNextPage() {
        val result = BrowseParser(context).parse(
            fixture("catalog/browse-paged.html"),
            2,
            OperationCode.BROWSE_LATEST,
        ) as ApiResult.Success
        assertNull(result.value.items.single().cover)
        assertEquals(3, result.value.nextPage)
    }

    @Test fun tagMetadataAndPagedNovelsRemainSeparate() {
        val tags = TagParser(context).parseDiscovery(fixture("catalog/tags.html"), OperationCode.TAGS) as ApiResult.Success
        val novels = TagParser(context).parseNovels(fixture("catalog/novels-by-tag.html"), 1) as ApiResult.Success
        assertEquals("fantasy", tags.value.second.single().id)
        assertEquals("103", novels.value.items.single().key.remoteId)
    }

    @Test fun missingPaginationMeansNoProvenNextPage() {
        val result = SearchParser(context).parse(fixture("catalog/search-single-page.html"), 1) as ApiResult.Success
        assertNull(result.value.nextPage)
    }

    private fun fixture(name: String) = checkNotNull(javaClass.classLoader.getResource(name)).readText(StandardCharsets.UTF_8)
}
```

- [ ] **Step 3: Run parser tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.parser.CatalogListParserTest" --stacktrace`

Expected: FAIL because the list parsers are missing.

- [ ] **Step 4: Add shared pure parsing support**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import java.net.URI
import org.jsoup.nodes.Element
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.catalog.BinaryRequest
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.catalog.NovelSummary
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.core.model.identity.SourceId

data class ParserContext(
    val sourceId: String,
    val baseUrl: String,
    val contractRevision: Int,
    val allowedImageHosts: Set<String> = setOf("www.wenku8.net"),
    val newTraceId: () -> String,
) {
    fun <T> success(value: T): ApiResult<T> = ApiResult.Success(value, ResponseMetadata(sourceId, 0L, Freshness.FRESH))
    fun failure(operationCode: OperationCode): ApiResult.Failure =
        ApiResult.Failure(ApiFailure.Parse(operationCode, contractRevision), newTraceId())
}

internal class NovelNodeMapper(private val context: ParserContext) {
    fun map(element: Element, operationCode: OperationCode): NovelSummary? {
        val id = element.attr("data-id").trim().takeIf(String::isNotEmpty) ?: return null
        val title = element.selectFirst("[data-field=title]")?.text()?.trim().takeIf { !it.isNullOrEmpty() } ?: return null
        val author = element.selectFirst("[data-field=author]")?.text()?.trim().takeIf { !it.isNullOrEmpty() }
        val cover = element.selectFirst("[data-field=cover]")?.attr("src")?.trim()?.takeIf(String::isNotEmpty)?.let { raw ->
            val resolved = URI(context.baseUrl).resolve(raw)
            if (resolved.scheme != "https" || resolved.userInfo != null || resolved.host?.lowercase() !in context.allowedImageHosts) return null
            BinaryRequest(resolved.toASCIIString(), operationCode.wireId)
        }
        return NovelSummary(NovelKey(SourceId(context.sourceId), id), title, author, cover)
    }
}

internal fun nextPage(document: org.jsoup.nodes.Document): Int? =
    document.selectFirst("[data-contract=next-page][data-page]")?.attr("data-page")?.toIntOrNull()?.takeIf { it > 0 }
```

- [ ] **Step 5: Implement home and shared paged-list parsers**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.jsoup.Jsoup
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.settings.ContentLanguage
import org.mewx.wenku8.core.model.catalog.HomeSection
import org.mewx.wenku8.core.model.catalog.NovelSummary
import org.mewx.wenku8.core.model.catalog.Page

class HomeParser(private val context: ParserContext) {
    fun parse(html: String, language: ContentLanguage): ApiResult<List<HomeSection>> {
        val document = Jsoup.parse(html, context.baseUrl)
        val mapper = NovelNodeMapper(context)
        val sections = document.select("[data-contract=home-section]").mapNotNull { section ->
            val id = section.attr("data-id").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val title = section.selectFirst("[data-field=title]")?.text()?.trim().takeIf { !it.isNullOrEmpty() } ?: return@mapNotNull null
            val novels = section.select("[data-contract=novel]").mapNotNull { mapper.map(it, OperationCode.HOME) }
            HomeSection(id, title, novels)
        }
        return if (sections.isEmpty()) context.failure(OperationCode.HOME) else context.success(sections)
    }
}

open class PagedNovelParser(private val context: ParserContext, private val operationCode: OperationCode) {
    fun parse(html: String, page: Int): ApiResult<Page<NovelSummary>> {
        if (page <= 0) return context.failure(operationCode)
        val document = Jsoup.parse(html, context.baseUrl)
        val items = document.select("[data-contract=novel]").mapNotNull { NovelNodeMapper(context).map(it, operationCode) }
        return if (items.isEmpty()) context.failure(operationCode) else context.success(Page(items, page, nextPage(document)))
    }
}

class BrowseParser(private val context: ParserContext) {
    fun parse(html: String, page: Int, operationCode: OperationCode): ApiResult<Page<NovelSummary>> {
        require(operationCode in BROWSE_CODES) { "browse-operation-code-required" }
        return PagedNovelParser(context, operationCode).parse(html, page)
    }

    private companion object {
        val BROWSE_CODES = setOf(
            OperationCode.BROWSE_LATEST,
            OperationCode.BROWSE_COMPLETED,
            OperationCode.BROWSE_CATEGORY,
            OperationCode.BROWSE_RANKING,
        )
    }
}

class SearchParser(context: ParserContext) : PagedNovelParser(context, OperationCode.SEARCH)
```

- [ ] **Step 6: Implement separate tag discovery and novels-by-tag parsing**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.jsoup.Jsoup
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.catalog.NovelSummary
import org.mewx.wenku8.core.model.catalog.Page
import org.mewx.wenku8.core.model.catalog.TagGroup
import org.mewx.wenku8.core.model.catalog.TagSummary

class TagParser(private val context: ParserContext) {
    fun parseDiscovery(html: String, operationCode: OperationCode): ApiResult<Pair<List<TagGroup>, List<TagSummary>>> {
        require(operationCode == OperationCode.TAG_GROUPS || operationCode == OperationCode.TAGS) {
            "tag-discovery-operation-code-required"
        }
        val document = Jsoup.parse(html, context.baseUrl)
        val groups = document.select("[data-contract=tag-group]").mapNotNull { group ->
            val groupId = group.attr("data-id").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            TagGroup(groupId, group.selectFirst("h2")?.text()?.trim().orEmpty())
        }
        val tags = document.select("[data-contract=tag]").mapNotNull { tag ->
            val id = tag.attr("data-id").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val groupId = tag.parents().firstOrNull { it.hasAttr("data-contract") && it.attr("data-contract") == "tag-group" }?.attr("data-id")
            TagSummary(id, groupId, tag.text().trim(), tag.attr("data-count").toIntOrNull())
        }
        return if (groups.isEmpty() && tags.isEmpty()) context.failure(operationCode) else context.success(groups to tags)
    }

    fun parseNovels(html: String, page: Int): ApiResult<Page<NovelSummary>> =
        PagedNovelParser(context, OperationCode.NOVELS_BY_TAG).parse(html, page)
}
```

- [ ] **Step 7: Bind accepted sanitized structures without Wild access**

For each accepted list operation, the implementer first adds a minimal sanitized fixture that contains only the required structural markers and synthetic values, writes a RED test against that fixture, then independently authors the smallest selectors that pass both normal and reordered/missing-optional fixtures. Increment `contractRevision` for selector changes, record the selector rationale in the clean-room attestation, and verify the fixture SHA-256 matches its operation ledger. If the ledger is still fixture-only, do not add a production selector or enable its capability.

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.parser.CatalogListParserTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; ordered values, missing optional fields, tag separation, paged tag results, and null missing-pagination behavior pass.

- [ ] **Step 8: Commit the list parsers and synthetic fixtures**

```powershell
git add api-public/src/main api-public/src/test
git commit -m "feat(api): parse catalog list operations"
```

### Task 11: Parse Novel Detail and Ordered Catalogs

**Depends on:** Tasks 5 and 9.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/NovelDetailParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/CatalogParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/novel/detail-normal.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/novel/detail-missing-optional.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/novel/catalog-normal.html`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/parser/NovelParserTest.kt`

- [ ] **Step 1: Add independently authored minimal detail/catalog fixtures**

```html
<!-- detail-normal.html -->
<!doctype html><html><body><main data-contract="novel-detail" data-id="201">
<h1 data-field="title">Synthetic Detail</h1><span data-field="author">Synthetic Author</span>
<span data-field="status">Completed</span><a data-contract="tag">Fantasy</a>
<div data-field="introduction"><p>First synthetic paragraph.</p><p>Second synthetic paragraph.</p></div>
<img data-field="cover" src="/synthetic/covers/201.jpg" alt="">
</main></body></html>
```

```html
<!-- catalog-normal.html -->
<!doctype html><html><body><section data-contract="volume" data-id="v1"><h2>Volume One</h2>
<a data-contract="chapter" data-id="301">Chapter One</a>
<a data-contract="chapter" data-id="302">Chapter Two</a>
</section><section data-contract="volume" data-id="v2"><h2>Volume Two</h2>
<a data-contract="chapter" data-id="303">Chapter Three</a></section></body></html>
```

```html
<!-- detail-missing-optional.html -->
<!doctype html><html><body><main data-contract="novel-detail" data-id="201">
<h1 data-field="title">Synthetic Detail Without Optional Fields</h1>
<div data-field="introduction"><p>Only required synthetic content.</p></div>
</main></body></html>
```

- [ ] **Step 2: Write failing required/optional/order tests**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.identity.SourceId

class NovelParserTest {
    private val context = ParserContext("public", "https://www.wenku8.net/", 4) { "trace-novel" }
    private val key = NovelKey(SourceId("public"), "201")

    @Test fun detailNormalizesControlledParagraphsAndOptionalFields() {
        val detail = NovelDetailParser(context).parse(fixture("novel/detail-normal.html"), key) as ApiResult.Success
        assertEquals(listOf("First synthetic paragraph.", "Second synthetic paragraph."), detail.value.introduction.paragraphs)
        val optional = NovelDetailParser(context).parse(fixture("novel/detail-missing-optional.html"), key) as ApiResult.Success
        assertNull(optional.value.author)
    }

    @Test fun catalogPreservesVolumeAndChapterOrder() {
        val volumes = CatalogParser(context).parse(fixture("novel/catalog-normal.html"), key) as ApiResult.Success
        assertEquals(listOf("v1", "v2"), volumes.value.map { it.remoteId })
        assertEquals(listOf("301", "302"), volumes.value.first().chapters.map { it.key.remoteId })
    }

    @Test fun missingRequiredTitleIsTypedParseFailure() {
        val result = NovelDetailParser(context).parse("<main data-contract='novel-detail' data-id='201'></main>", key) as ApiResult.Failure
        assertEquals(ApiFailure.Parse(OperationCode.NOVEL_DETAIL, 4), result.error)
    }
}
```

- [ ] **Step 3: Run detail/catalog tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.parser.NovelParserTest" --stacktrace`

Expected: FAIL because both parsers are missing.

- [ ] **Step 4: Implement detail parsing into non-executable models**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import java.net.URI
import org.jsoup.Jsoup
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.catalog.BinaryRequest
import org.mewx.wenku8.core.model.catalog.ControlledRichText
import org.mewx.wenku8.core.model.catalog.NovelDetail
import org.mewx.wenku8.core.model.identity.NovelKey

class NovelDetailParser(private val context: ParserContext) {
    fun parse(html: String, requestedKey: NovelKey): ApiResult<NovelDetail> {
        val root = Jsoup.parse(html, context.baseUrl).selectFirst("[data-contract=novel-detail]") ?: return context.failure(OperationCode.NOVEL_DETAIL)
        if (root.attr("data-id") != requestedKey.remoteId) return context.failure(OperationCode.NOVEL_DETAIL)
        val title = root.selectFirst("[data-field=title]")?.text()?.trim().takeIf { !it.isNullOrEmpty() } ?: return context.failure(OperationCode.NOVEL_DETAIL)
        val paragraphs = root.select("[data-field=introduction] p").map { it.text().replace('\u00a0', ' ').trim() }.filter(String::isNotEmpty)
        val cover = root.selectFirst("[data-field=cover]")?.attr("src")?.takeIf(String::isNotBlank)?.let {
            val resolved = URI(context.baseUrl).resolve(it)
            if (resolved.scheme != "https" || resolved.userInfo != null || resolved.host?.lowercase() !in context.allowedImageHosts) return context.failure(OperationCode.NOVEL_DETAIL)
            BinaryRequest(resolved.toASCIIString(), OperationCode.NOVEL_DETAIL.wireId)
        }
        return context.success(
            NovelDetail(
                requestedKey,
                title,
                root.selectFirst("[data-field=author]")?.text()?.trim().takeIf { !it.isNullOrEmpty() },
                root.selectFirst("[data-field=status]")?.text()?.trim().takeIf { !it.isNullOrEmpty() },
                root.select("[data-contract=tag]").map { it.text().trim() }.filter(String::isNotEmpty),
                ControlledRichText(paragraphs),
                cover,
            ),
        )
    }
}
```

- [ ] **Step 5: Implement catalog parsing with stable chapter keys**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.jsoup.Jsoup
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.identity.ChapterKey
import org.mewx.wenku8.core.model.catalog.ChapterSummary
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.catalog.Volume

class CatalogParser(private val context: ParserContext) {
    fun parse(html: String, novel: NovelKey): ApiResult<List<Volume>> {
        val volumes = Jsoup.parse(html, context.baseUrl).select("[data-contract=volume]").mapNotNull { node ->
            val id = node.attr("data-id").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val title = node.selectFirst("h2")?.text()?.trim().takeIf { !it.isNullOrEmpty() } ?: return@mapNotNull null
            val chapters = node.select("[data-contract=chapter]").mapNotNull { chapter ->
                val chapterId = chapter.attr("data-id").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
                val chapterTitle = chapter.text().trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
                ChapterSummary(ChapterKey(novel, chapterId), chapterTitle)
            }
            Volume(id, title, chapters)
        }
        return if (volumes.isEmpty()) context.failure(OperationCode.CATALOG) else context.success(volumes)
    }
}
```

- [ ] **Step 6: Run tests, bind accepted fixture selectors, and commit**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.parser.NovelParserTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; required fields fail with revision 4, optional values become null, and all order is retained.

```powershell
git add api-public/src/main api-public/src/test
git commit -m "feat(api): parse novel details and catalogs"
```

### Task 12: Parse Ordered Chapter Blocks and Fetch Bounded Images

**Depends on:** Tasks 5 and 9.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/ChapterParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicBinarySource.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/ApprovedRefererPolicy.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/reader/chapter-mixed.html`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/parser/ChapterParserTest.kt`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/PublicBinarySourceTest.kt`

- [ ] **Step 1: Add a synthetic chapter fixture covering every block rule**

```html
<!doctype html><html><body><article data-contract="chapter" data-title="Synthetic Chapter">
<p>First&nbsp;line<br>Second line<img src="../images/one.jpg" alt="Synthetic illustration">Tail text</p>
<aside data-contract="watermark">Synthetic removable watermark</aside>
<p><img src="https://images.example.test/two.png" alt=""></p>
</article></body></html>
```

- [ ] **Step 2: Write failing order, URL, break, watermark, and binary tests**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.core.model.catalog.ChapterBlock
import org.mewx.wenku8.core.model.identity.ChapterKey
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.identity.SourceId

class ChapterParserTest {
    @Test fun preservesTextBreakImageAndTailOrder() {
        val context = ParserContext(
            "public",
            "https://www.wenku8.net/novel/0/1/",
            7,
            setOf("www.wenku8.net", "images.example.test"),
        ) { "trace-chapter" }
        val key = ChapterKey(NovelKey(SourceId("public"), "1"), "9")
        val result = ChapterParser(context).parse(fixture("reader/chapter-mixed.html"), key) as ApiResult.Success
        assertEquals(
            listOf("Text", "SemanticBreak", "Text", "Image", "Text", "SemanticBreak", "Image"),
            result.value.blocks.map { it::class.simpleName },
        )
        assertEquals("https://www.wenku8.net/novel/0/images/one.jpg", (result.value.blocks[3] as ChapterBlock.Image).resource.canonicalHttpsUrl)
        assertFalse(result.value.blocks.filterIsInstance<ChapterBlock.Text>().any { it.text.contains("watermark") })
    }
}
```

The binary test enqueues valid JPEG bytes, an oversized body, a text/html body, and an unknown host. It asserts success only for the bounded image; size/media/host failures are typed and unknown-host dispatch count is zero.

- [ ] **Step 3: Run chapter/binary tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "*.ChapterParserTest" --tests "*.PublicBinarySourceTest" --stacktrace`

Expected: FAIL because chapter and binary implementations are missing.

- [ ] **Step 4: Implement a DOM visitor that emits ordered safe blocks**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import java.net.URI
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.catalog.BinaryRequest
import org.mewx.wenku8.core.model.catalog.ChapterBlock
import org.mewx.wenku8.core.model.catalog.ChapterDocument
import org.mewx.wenku8.core.model.identity.ChapterKey

class ChapterParser(private val context: ParserContext) {
    fun parse(html: String, key: ChapterKey): ApiResult<ChapterDocument> {
        val root = Jsoup.parse(html, context.baseUrl).selectFirst("[data-contract=chapter]") ?: return context.failure(OperationCode.CHAPTER)
        root.select("[data-contract=watermark], [data-contract=navigation]").remove()
        val blocks = mutableListOf<ChapterBlock>()
        NodeTraversor.traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                when (node) {
                    is TextNode -> node.text().replace('\u00a0', ' ').trim().takeIf(String::isNotEmpty)?.let { blocks += ChapterBlock.Text(it) }
                    is Element -> when (node.normalName()) {
                        "br" -> appendBreak(blocks)
                        "img" -> parseImage(node)?.let { blocks += it }
                    }
                }
            }

            override fun tail(node: Node, depth: Int) {
                if (node is Element && node.normalName() in setOf("p", "div")) appendBreak(blocks)
            }
        }, root)
        while (blocks.lastOrNull() == ChapterBlock.SemanticBreak) blocks.removeLast()
        return if (blocks.isEmpty()) context.failure(OperationCode.CHAPTER) else context.success(ChapterDocument(key, root.attr("data-title").takeIf(String::isNotBlank), blocks))
    }

    private fun parseImage(element: Element): ChapterBlock.Image? {
        val raw = element.attr("src").trim().takeIf(String::isNotEmpty) ?: return null
        val resolved = URI(context.baseUrl).resolve(raw)
        if (resolved.scheme != "https" || resolved.userInfo != null || resolved.host?.lowercase() !in context.allowedImageHosts) return null
        return ChapterBlock.Image(BinaryRequest(resolved.toASCIIString(), OperationCode.CHAPTER.wireId), element.attr("alt").trim().takeIf(String::isNotEmpty))
    }

    private fun appendBreak(blocks: MutableList<ChapterBlock>) {
        if (blocks.isNotEmpty() && blocks.last() != ChapterBlock.SemanticBreak) blocks += ChapterBlock.SemanticBreak
    }
}
```

- [ ] **Step 5: Implement bounded verified binary fetching**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.CancellationException
import okhttp3.Request
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.core.model.catalog.BinaryRequest
import org.mewx.wenku8.core.model.catalog.BinaryResource
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.api.contract.ProviderCapabilities
import org.mewx.wenku8.api.contract.ProviderCapability
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.api.contract.Wenku8BinarySource
import org.mewx.wenku8.api.publicprovider.transport.PublicHttpClientFactory
import org.mewx.wenku8.core.network.okhttp.BoundedBodyReader
import org.mewx.wenku8.core.network.okhttp.BodyLimitExceeded
import org.mewx.wenku8.core.network.okhttp.OperationTag
import org.mewx.wenku8.core.network.okhttp.awaitResponse
import org.mewx.wenku8.core.network.failure.toNetworkFailureKind

class PublicBinarySource(
    private val sharedCapabilities: ProviderCapabilities,
    private val guard: CapabilityGuard,
    private val clients: PublicHttpClientFactory,
    private val refererPolicy: ApprovedRefererPolicy,
    private val maxBytes: Long = 20L * 1024 * 1024,
    private val nowMillis: () -> Long,
    private val newTraceId: () -> String,
) : Wenku8BinarySource {
    override fun capabilities() = sharedCapabilities

    override suspend fun fetch(request: BinaryRequest): ApiResult<BinaryResource> = guard.run(ProviderCapability.BINARY_DOWNLOAD) {
        try {
            val httpRequest = Request.Builder().url(request.canonicalHttpsUrl).get()
                .apply { refererPolicy.header(request)?.let { header("Referer", it) } }
                .tag(OperationTag::class.java, OperationTag(NetworkOperationScope("binary"), authenticated = false)).build()
            clients.anonymous().newCall(httpRequest).awaitResponse().use { response ->
                if (!response.isSuccessful) return@run ApiResult.Failure(ApiFailure.Http(response.code, null), newTraceId())
                val media = response.body?.contentType()?.toString().orEmpty()
                if (media !in setOf("image/jpeg", "image/png", "image/webp", "image/gif")) {
                    return@run ApiResult.Failure(ApiFailure.ProtocolViolation("binary-media-type"), newTraceId())
                }
                val bytes = BoundedBodyReader.read(response.body!!, maxBytes)
                ApiResult.Success(BinaryResource(bytes, media, response.request.url.toString()), ResponseMetadata("public", nowMillis(), Freshness.FRESH))
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: BodyLimitExceeded) {
            ApiResult.Failure(ApiFailure.ProtocolViolation("binary-size-limit"), newTraceId())
        } catch (error: Throwable) {
            ApiResult.Failure(error.toNetworkFailureKind().toApiFailure(), newTraceId())
        }
    }
}
```

```kotlin
class ApprovedRefererPolicy(
    private val operations: OperationRegistry,
    private val approvedImageHosts: Set<String>,
) {
    fun header(request: BinaryRequest): String? {
        val image = request.canonicalHttpsUrl.toHttpUrl()
        if (image.scheme != "https" || image.port != 443 || image.host !in approvedImageHosts) return null
        val source = operations.accepted(request.refererOperation) ?: return null
        return source.canonicalHttpsOrigin.takeIf { it.startsWith("https://") }
    }
}
```

The binary tests assert an accepted image host receives the accepted HTTPS operation origin as Referer, while an unknown host, unknown operation, cleartext URL, or non-default port receives no Referer and is denied by HostPolicy before dispatch.

- [ ] **Step 6: Run tests and commit**

Run: `.\gradlew.bat :api-public:test --tests "*.ChapterParserTest" --tests "*.PublicBinarySourceTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; block ordering, breaks, NBSP, watermark removal, relative/absolute images, body limits, media verification, and host rejection pass.

```powershell
git add api-public/src/main api-public/src/test
git commit -m "feat(api): parse chapters and fetch bounded images"
```

### Task 13: Compose the Anonymous Catalog Facet with Input Validation

**Depends on:** Tasks 4, 10, 11, and 12.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderCapabilities.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicCatalogSource.kt`
- Modify: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/PublicTransport.kt`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/PublicCatalogSourceTest.kt`
- Test support: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/support/CatalogHarness.kt`

- [ ] **Step 1: Write failing operation-routing and input tests**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.core.model.settings.ContentLanguage
import org.mewx.wenku8.api.contract.SearchQuery
import org.mewx.wenku8.api.contract.SearchScope
import org.mewx.wenku8.api.publicprovider.support.CatalogHarness

class PublicCatalogSourceTest {
    @Test fun emptyAndOversizedSearchFailBeforeDispatch() = runTest {
        val harness = CatalogHarness.acceptedSynthetic()
        val empty = harness.source.search(SearchQuery(" ", SearchScope.TITLE, 1, ContentLanguage.SIMPLIFIED_CHINESE))
        val oversized = harness.source.search(SearchQuery("轻".repeat(100), SearchScope.TITLE, 1, ContentLanguage.SIMPLIFIED_CHINESE))
        assertTrue(empty is ApiResult.Failure)
        assertTrue(oversized is ApiResult.Failure)
        assertEquals(0, harness.requests.size)
    }

    @Test fun tagDiscoveryAndTagBrowseUseDifferentOperations() = runTest {
        val harness = CatalogHarness.acceptedSynthetic()
        harness.source.tags(harness.tagDiscovery)
        harness.source.novelsByTag(harness.tagBrowse)
        assertEquals(listOf("tags", "novels-by-tag"), harness.requests.map { it.operationId })
    }
}
```

Create the complete recording harness used above:

```kotlin
package org.mewx.wenku8.api.publicprovider.support

import org.mewx.wenku8.api.contract.*
import org.mewx.wenku8.api.publicprovider.*
import org.mewx.wenku8.api.publicprovider.parser.*
import org.mewx.wenku8.api.publicprovider.transport.PublicTextTransport
import org.mewx.wenku8.core.model.identity.SourceId
import org.mewx.wenku8.core.model.settings.ContentLanguage
import org.mewx.wenku8.core.network.codec.GbkCodec

data class RecordedCatalogRequest(
    val operationId: String,
    val path: Map<String, String>,
    val query: Map<String, String>,
)

private class RecordingCatalogTransport(
    private val responses: Map<String, String>,
) : PublicTextTransport {
    val requests = mutableListOf<RecordedCatalogRequest>()

    override suspend fun text(
        operationId: String,
        path: Map<String, String>,
        query: Map<String, String>,
        authenticated: Boolean,
    ): ApiResult<String> {
        check(!authenticated)
        requests += RecordedCatalogRequest(operationId, path.toMap(), query.toMap())
        val value = responses[operationId]
            ?: return ApiResult.Failure(ApiFailure.ProtocolViolation("missing-synthetic-response:$operationId"), "catalog-harness")
        return ApiResult.Success(value, ResponseMetadata("public", 0L, Freshness.FRESH))
    }
}

class CatalogHarness private constructor(
    val source: PublicCatalogSource,
    private val transport: RecordingCatalogTransport,
) {
    val requests: List<RecordedCatalogRequest> get() = transport.requests.toList()
    val tagDiscovery = TagDiscoveryRequest("genre", ContentLanguage.SIMPLIFIED_CHINESE)
    val tagBrowse = TagBrowseRequest("fantasy", 1, ContentLanguage.SIMPLIFIED_CHINESE)

    companion object {
        fun acceptedSynthetic(): CatalogHarness {
            fun fixture(path: String) = checkNotNull(CatalogHarness::class.java.classLoader.getResource(path)).readText()
            val transport = RecordingCatalogTransport(
                mapOf(
                    "home" to fixture("fixtures/catalog/home-normal.html"),
                    "browse-latest" to fixture("fixtures/catalog/browse-paged.html"),
                    "browse-completed" to fixture("fixtures/catalog/browse-paged.html"),
                    "browse-category" to fixture("fixtures/catalog/browse-paged.html"),
                    "browse-ranking" to fixture("fixtures/catalog/browse-paged.html"),
                    "tag-groups" to fixture("fixtures/catalog/tags.html"),
                    "tags" to fixture("fixtures/catalog/tags.html"),
                    "novels-by-tag" to fixture("fixtures/catalog/novels-by-tag.html"),
                    "search" to fixture("fixtures/catalog/search-single-page.html"),
                    "novel-detail" to fixture("fixtures/novel/detail-normal.html"),
                    "catalog" to fixture("fixtures/novel/catalog-normal.html"),
                    "chapter" to fixture("fixtures/reader/chapter-mixed.html"),
                ),
            )
            val capabilities = ProviderCapabilities(
                SourceId("public"),
                setOf(ProviderCapability.ANONYMOUS_CATALOG),
                ProviderInputPolicy(80, 60, 4_000, 4_000),
            )
            val context = ParserContext("public", "https://www.wenku8.net/", 1) { "catalog-harness" }
            val source = PublicCatalogSource(
                capabilities,
                CapabilityGuard(capabilities) { "catalog-harness" },
                transport,
                GbkCodec(),
                HomeParser(context),
                BrowseParser(context),
                TagParser(context),
                SearchParser(context),
                NovelDetailParser(context),
                CatalogParser(context),
                ChapterParser(context),
                newTraceId = { "catalog-harness" },
            )
            return CatalogHarness(source, transport)
        }
    }
}
```

- [ ] **Step 2: Run catalog facet tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicCatalogSourceTest" --stacktrace`

Expected: FAIL because the composed catalog source and accepted-capability derivation are missing.

- [ ] **Step 3: Derive coarse capabilities only from complete accepted operation sets**

```kotlin
package org.mewx.wenku8.api.publicprovider

import org.mewx.wenku8.api.contract.ProviderCapabilities
import org.mewx.wenku8.api.contract.ProviderCapability
import org.mewx.wenku8.api.contract.ProviderInputPolicy
import org.mewx.wenku8.core.model.identity.SourceId
import org.mewx.wenku8.api.publicprovider.evidence.OperationRegistry

object PublicProviderCapabilities {
    private val catalogOperations = setOf(
        "home", "browse-latest", "browse-completed", "browse-category", "browse-ranking",
        "tag-groups", "tags", "novels-by-tag", "search", "novel-detail", "catalog", "chapter",
    )

    fun from(registry: OperationRegistry, policy: ProviderInputPolicy): ProviderCapabilities {
        val enabled = buildSet {
            if (registry.ids().containsAll(catalogOperations)) add(ProviderCapability.ANONYMOUS_CATALOG)
            if ("binary" in registry.ids()) add(ProviderCapability.BINARY_DOWNLOAD)
            if ("registration" in registry.ids()) add(ProviderCapability.REGISTRATION_LINK)
        }
        return ProviderCapabilities(SourceId("public"), enabled, policy)
    }
}
```

- [ ] **Step 4: Add validated operation routing for every anonymous read**

```kotlin
class PublicCatalogSource(
    private val sharedCapabilities: ProviderCapabilities,
    private val guard: CapabilityGuard,
    private val transport: PublicTextTransport,
    private val gbk: GbkCodec,
    private val homeParser: HomeParser,
    private val browseParser: BrowseParser,
    private val tagParser: TagParser,
    private val searchParser: SearchParser,
    private val detailParser: NovelDetailParser,
    private val catalogParser: CatalogParser,
    private val chapterParser: ChapterParser,
    private val newTraceId: () -> String,
) : Wenku8CatalogSource {
    override fun capabilities() = sharedCapabilities

    override suspend fun home() = catalogGuard { text("home").flatMap { homeParser.parse(it, ContentLanguage.SIMPLIFIED_CHINESE) } }

    override suspend fun browse(request: BrowseRequest) = catalogGuard {
        require(request.page > 0) { "page-positive" }
        when (request.kind) {
            BrowseKind.CATEGORY -> require(!request.categoryId.isNullOrBlank() && request.rankingPeriod == null) { "category-input" }
            BrowseKind.RANKING -> require(request.rankingPeriod != null && request.categoryId == null) { "ranking-input" }
            BrowseKind.LATEST, BrowseKind.COMPLETED -> require(request.categoryId == null && request.rankingPeriod == null) { "browse-input" }
        }
        val operationCode = when (request.kind) {
            BrowseKind.LATEST -> org.mewx.wenku8.api.contract.OperationCode.BROWSE_LATEST
            BrowseKind.COMPLETED -> org.mewx.wenku8.api.contract.OperationCode.BROWSE_COMPLETED
            BrowseKind.CATEGORY -> org.mewx.wenku8.api.contract.OperationCode.BROWSE_CATEGORY
            BrowseKind.RANKING -> org.mewx.wenku8.api.contract.OperationCode.BROWSE_RANKING
        }
        val query = buildMap {
            put("page", request.page.toString())
            request.categoryId?.let { put("category", it) }
            request.rankingPeriod?.let { put("period", it.name.lowercase()) }
        }
        text(operationCode.wireId, query = query).flatMap { browseParser.parse(it, request.page, operationCode) }
    }

    override suspend fun tagGroups(language: ContentLanguage) = catalogGuard {
        text("tag-groups", query = mapOf("language" to language.name)).flatMap {
            tagParser.parseDiscovery(it, org.mewx.wenku8.api.contract.OperationCode.TAG_GROUPS).mapValue { pair -> pair.first }
        }
    }

    override suspend fun tags(request: TagDiscoveryRequest) = catalogGuard {
        text("tags", query = mapOf("language" to request.language.name) + listOfNotNull(request.groupId?.let { "group" to it }).toMap())
            .flatMap { tagParser.parseDiscovery(it, org.mewx.wenku8.api.contract.OperationCode.TAGS).mapValue { pair -> pair.second } }
    }

    override suspend fun novelsByTag(request: TagBrowseRequest) = catalogGuard {
        require(request.page > 0 && request.tagId.isNotBlank()) { "tag-and-page" }
        text("novels-by-tag", query = mapOf("tag" to request.tagId, "page" to request.page.toString()))
            .flatMap { tagParser.parseNovels(it, request.page) }
    }

    override suspend fun search(query: SearchQuery) = catalogGuard {
        require(query.page > 0 && query.text.trim().isNotEmpty()) { "search-input" }
        val encoded = when (val encodedResult = gbk.percentEncode(query.text.trim())) {
            is CodecResult.Value -> encodedResult.value
            CodecResult.Invalid -> return@catalogGuard ApiResult.Failure(ApiFailure.Decode("GBK"), newTraceId())
        }
        require(encoded.toByteArray(Charsets.US_ASCII).size <= sharedCapabilities.inputPolicy.searchMaxEncodedBytes) { "search-limit" }
        text("search", query = mapOf("q" to encoded, "scope" to query.scope.name.lowercase(), "page" to query.page.toString()))
            .flatMap { searchParser.parse(it, query.page) }
    }

    override suspend fun novel(key: NovelKey) = catalogGuard {
        validateKey(key)
        text("novel-detail", path = mapOf("aid" to key.remoteId)).flatMap { detailParser.parse(it, key) }
    }

    override suspend fun catalog(key: NovelKey) = catalogGuard {
        validateKey(key)
        text("catalog", path = mapOf("aid" to key.remoteId)).flatMap { catalogParser.parse(it, key) }
    }

    override suspend fun chapter(key: ChapterKey) = catalogGuard {
        validateKey(key.novel)
        require(key.remoteId.isNotBlank()) { "chapter-id" }
        text("chapter", path = mapOf("aid" to key.novel.remoteId, "cid" to key.remoteId)).flatMap { chapterParser.parse(it, key) }
    }

    private suspend fun <T> catalogGuard(block: suspend () -> ApiResult<T>) =
        guard.run(ProviderCapability.ANONYMOUS_CATALOG) { runValidated(block) }

    private suspend fun text(
        operationId: String,
        path: Map<String, String> = emptyMap(),
        query: Map<String, String> = emptyMap(),
    ): ApiResult<String> = transport.text(operationId, path, query, authenticated = false)

    private suspend fun <T> runValidated(block: suspend () -> ApiResult<T>): ApiResult<T> = try {
        block()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: IllegalArgumentException) {
        ApiResult.Failure(ApiFailure.ProtocolViolation("invalid-input"), newTraceId())
    }

    private fun validateKey(key: NovelKey) {
        require(key.sourceId == SourceId("public"))
        require(key.remoteId.matches(Regex("[A-Za-z0-9_-]{1,64}")))
    }
}
```

Add the exact result/validation and transport helpers:

```kotlin
internal inline fun <A, B> ApiResult<A>.flatMap(transform: (A) -> ApiResult<B>): ApiResult<B> = when (this) {
    is ApiResult.Success -> transform(value)
    is ApiResult.Failure -> this
}

internal inline fun <A, B> ApiResult<A>.mapValue(transform: (A) -> B): ApiResult<B> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(value), metadata)
    is ApiResult.Failure -> this
}

```

Replace the convenience name `mapSuccess` in the source block with `flatMap`. Add this overload to `PublicTransport`:

```kotlin
override suspend fun text(
    operationId: String,
    path: Map<String, String> = emptyMap(),
    query: Map<String, String> = emptyMap(),
    authenticated: Boolean = false,
): ApiResult<String> {
    val operation = operations.accepted(operationId)
        ?: return failure(ApiFailure.ProtocolViolation("evidence-not-accepted:$operationId"))
    val slots = Regex("\\{([a-z][a-z0-9-]*)}").findAll(operation.encodedPathTemplate).map { it.groupValues[1] }.toSet()
    if (slots != path.keys || path.values.any { !it.matches(Regex("[A-Za-z0-9_-]{1,64}")) }) {
        return failure(ApiFailure.ProtocolViolation("path-contract:$operationId"))
    }
    val resolvedPath = slots.fold(operation.encodedPathTemplate) { value, slot -> value.replace("{$slot}", path.getValue(slot)) }
    val resolved = operation.copy(encodedPathTemplate = resolvedPath)
    return executeText(resolved, query, authenticated)
}
```

`executeText` is the Task 9 `getText` body after accepted lookup; it rejects extra query keys and calls `addEncodedQueryParameter` exactly once. Keep `getText(operationId, query, authenticated)` as a test-facing overload delegating to `text(operationId, emptyMap(), query, authenticated)`.

- [ ] **Step 5: Run all anonymous-provider tests**

Run: `.\gradlew.bat :api-public:test --tests "*.PublicCatalogSourceTest" --tests "*.CatalogListParserTest" --tests "*.NovelParserTest" --tests "*.ChapterParserTest" --tests "*.PublicBinarySourceTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; every anonymous operation is routed, invalid inputs dispatch zero requests, tag discovery is separate from novels-by-tag, and all facet calls report the same immutable capabilities instance.

- [ ] **Step 6: Commit the anonymous provider**

```powershell
git add api-public/src
git commit -m "feat(api): compose anonymous public catalog provider"
```

### Task 14: Implement the Encrypted No-Backup SessionStore and Monotonic Epoch

**Depends on:** Task 2 and the Phase 1 `SessionStore` contract.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/SessionCipher.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/SessionRecordCodec.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/AndroidKeystoreSessionCipher.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/EncryptedSessionStore.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/AtomicSessionFile.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/session/SessionRecordCodecTest.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/session/EncryptedSessionStoreTest.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/session/AndroidKeystoreSessionCipherTest.kt`

- [ ] **Step 1: Write failing codec/store tests without rendering Cookie values**

```kotlin
package org.mewx.wenku8.core.storage.session

import java.nio.file.Files
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.core.model.identity.SourceId
import org.mewx.wenku8.core.session.SessionCookie
import org.mewx.wenku8.core.session.SessionRecord

class EncryptedSessionStoreTest {
    @Test fun roundTripPersistsCiphertextAndMonotonicEpoch() = runTest {
        val root = Files.createTempDirectory("session-test").toFile()
        val cipher = PrefixCipher(byteArrayOf(0x57, 0x38))
        val purged = mutableListOf<SourceId>()
        val store = EncryptedSessionStore(root, cipher, SessionRecordCodec(), purged::add)
        val provider = SourceId("public")
        val epoch = store.incrementEpoch(provider)
        store.replace(record(provider, epoch, "cookie-secret"))
        assertEquals("cookie-secret", store.load(provider)!!.cookies.single().value)
        assertFalse(root.resolve("records/public.session").readBytes().toString(Charsets.UTF_8).contains("cookie-secret"))
        assertEquals(epoch + 1, store.incrementEpoch(provider))
    }

    @Test fun anyDecryptOrFormatFailureDeletesRecordAndPurgesAuthenticatedCache() = runTest {
        val root = Files.createTempDirectory("session-failure").toFile()
        val provider = SourceId("public")
        root.resolve("records").mkdirs()
        root.resolve("records/public.session").writeBytes(byteArrayOf(1, 2, 3))
        val purged = mutableListOf<SourceId>()
        val store = EncryptedSessionStore(root, AlwaysFailCipher, SessionRecordCodec(), purged::add)
        assertNull(store.load(provider))
        assertFalse(root.resolve("records/public.session").exists())
        assertEquals(listOf(provider), purged)
    }
}
```

`SessionRecordCodecTest` rejects wrong magic/version, negative or oversized string/cookie counts, duplicate Cookie name/domain/path tuples, expired session data, trailing bytes, and confirms `SessionCookie.toString()` never contains its value.

- [ ] **Step 2: Run session tests and confirm RED**

Run: `.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.session.*" --stacktrace`

Expected: FAIL because encrypted storage components are missing.

- [ ] **Step 3: Define the narrow cipher boundary and bounded codec**

```kotlin
package org.mewx.wenku8.core.storage.session

interface SessionCipher {
    fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray
    fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray
}
```

```kotlin
package org.mewx.wenku8.core.storage.session

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import org.mewx.wenku8.core.model.identity.SourceId
import org.mewx.wenku8.core.session.SessionCookie
import org.mewx.wenku8.core.session.SessionRecord

class SessionRecordCodec {
    fun encode(record: SessionRecord): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { out ->
            out.writeInt(MAGIC)
            out.writeInt(VERSION)
            out.writeBounded(record.providerId.value, 64)
            out.writeBounded(record.accountId, 256)
            out.writeLong(record.epoch)
            out.writeLong(record.createdAtEpochMillis)
            out.writeNullableLong(record.expiresAtEpochMillis)
            require(record.cookies.size <= MAX_COOKIES)
            out.writeInt(record.cookies.size)
            record.cookies.forEach { cookie ->
                out.writeBounded(cookie.name, 256)
                out.writeBounded(cookie.value, 8192)
                out.writeBounded(cookie.domain, 512)
                out.writeBounded(cookie.path, 1024)
                out.writeBoolean(cookie.secure)
                out.writeBoolean(cookie.httpOnly)
                out.writeBoolean(cookie.hostOnly)
                out.writeBoolean(cookie.persistent)
                out.writeNullableLong(cookie.expiresAtEpochMillis)
            }
        }
        bytes.toByteArray()
    }

    fun decode(encoded: ByteArray): SessionRecord = DataInputStream(ByteArrayInputStream(encoded)).use { input ->
        require(input.readInt() == MAGIC && input.readInt() == VERSION)
        val provider = SourceId(input.readBounded(64))
        val account = input.readBounded(256)
        val epoch = input.readLong().also { require(it >= 0) }
        val created = input.readLong().also { require(it >= 0) }
        val expires = input.readNullableLong()
        val count = input.readInt().also { require(it in 0..MAX_COOKIES) }
        val cookies = List(count) {
            SessionCookie(
                input.readBounded(256), input.readBounded(8192), input.readBounded(512), input.readBounded(1024),
                input.readBoolean(), input.readBoolean(), input.readBoolean(), input.readBoolean(), input.readNullableLong(),
            )
        }
        require(input.read() == -1)
        require(cookies.distinctBy { Triple(it.name, it.domain, it.path) }.size == cookies.size)
        SessionRecord(provider, account, epoch, cookies, created, expires)
    }

    private companion object { const val MAGIC = 0x57385332; const val VERSION = 1; const val MAX_COOKIES = 64 }
}
```

Add the exact bounded helpers:

```kotlin
private fun DataOutputStream.writeBounded(value: String, maxBytes: Int) {
    val bytes = value.toByteArray(Charsets.UTF_8)
    try {
        require(bytes.size <= maxBytes)
        writeInt(bytes.size)
        write(bytes)
    } finally {
        bytes.fill(0)
    }
}

private fun DataInputStream.readBounded(maxBytes: Int): String {
    val size = readInt()
    require(size in 0..maxBytes)
    val bytes = ByteArray(size)
    return try {
        readFully(bytes)
        bytes.toString(Charsets.UTF_8)
    } finally {
        bytes.fill(0)
    }
}

private fun DataOutputStream.writeNullableLong(value: Long?) {
    writeBoolean(value != null)
    if (value != null) writeLong(value)
}

private fun DataInputStream.readNullableLong(): Long? = if (readBoolean()) readLong().also { require(it >= 0) } else null
```

- [ ] **Step 4: Implement non-exportable Android Keystore AES-GCM**

```kotlin
package org.mewx.wenku8.core.storage.session

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidKeystoreSessionCipher(
    private val alias: String = "wenku8.session.v1",
) : SessionCipher {
    override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        cipher.updateAAD(associatedData)
        val encrypted = cipher.doFinal(plaintext)
        return byteArrayOf(cipher.iv.size.toByte()) + cipher.iv + encrypted
    }

    override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray {
        require(ciphertext.isNotEmpty())
        val ivSize = ciphertext[0].toInt() and 0xff
        require(ivSize in 12..16 && ciphertext.size > 1 + ivSize + 16)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, ciphertext.copyOfRange(1, 1 + ivSize)))
        cipher.updateAAD(associatedData)
        return cipher.doFinal(ciphertext, 1 + ivSize, ciphertext.size - 1 - ivSize)
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object { const val TRANSFORMATION = "AES/GCM/NoPadding" }
}
```

- [ ] **Step 5: Implement atomic records and epochs under `noBackupFilesDir`**

```kotlin
package org.mewx.wenku8.core.storage.session

import java.io.File
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mewx.wenku8.core.model.identity.SourceId
import org.mewx.wenku8.core.session.SessionRecord
import org.mewx.wenku8.core.session.SessionStore

class EncryptedSessionStore(
    private val root: File,
    private val cipher: SessionCipher,
    private val codec: SessionRecordCodec,
    private val purgeAuthenticatedCaches: suspend (SourceId) -> Unit,
) : SessionStore {
    private val mutex = Mutex()

    override suspend fun load(providerId: SourceId): SessionRecord? = mutex.withLock {
        val file = recordFile(providerId)
        if (!file.isFile) return@withLock null
        try {
            val plaintext = cipher.decrypt(file.readBytes(), aad(providerId))
            try {
                codec.decode(plaintext).also { require(it.providerId == providerId && it.epoch == readEpoch(providerId)) }
            } finally {
                plaintext.fill(0)
            }
        } catch (_: Exception) {
            file.delete()
            val next = Math.addExact(readEpoch(providerId), 1L)
            atomicWrite(epochFile(providerId), next.toString().toByteArray(Charsets.US_ASCII))
            purgeAuthenticatedCaches(providerId)
            null
        }
    }

    override suspend fun replace(record: SessionRecord) = mutex.withLock {
        require(record.epoch == readEpoch(record.providerId))
        val plaintext = codec.encode(record)
        try {
            val encrypted = cipher.encrypt(plaintext, aad(record.providerId))
            try { atomicWrite(recordFile(record.providerId), encrypted) } finally { encrypted.fill(0) }
        } finally {
            plaintext.fill(0)
        }
    }

    override suspend fun purge(providerId: SourceId) = mutex.withLock {
        recordFile(providerId).delete()
        purgeAuthenticatedCaches(providerId)
    }

    override suspend fun incrementEpoch(providerId: SourceId): Long = mutex.withLock {
        val next = Math.addExact(readEpoch(providerId), 1L)
        atomicWrite(epochFile(providerId), next.toString().toByteArray(Charsets.US_ASCII))
        next
    }

    private fun recordFile(id: SourceId) = root.resolve("records/${safe(id)}.session")
    private fun epochFile(id: SourceId) = root.resolve("epochs/${safe(id)}.epoch")
    private fun readEpoch(id: SourceId) = epochFile(id).takeIf(File::isFile)?.readText(Charsets.US_ASCII)?.toLongOrNull() ?: 0L
    private fun safe(id: SourceId) = id.value.also { require(it.matches(Regex("[a-z0-9_-]{1,48}"))) }
    private fun aad(id: SourceId) = "wenku8-session:${id.value}:v1".toByteArray(Charsets.US_ASCII)
}
```

Implement the sibling atomic file primitive:

```kotlin
package org.mewx.wenku8.core.storage.session

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

internal fun atomicWrite(target: File, bytes: ByteArray) {
    target.parentFile?.mkdirs()
    val temp = File(target.parentFile, ".${target.name}.${UUID.randomUUID()}.tmp")
    try {
        FileOutputStream(temp).use { output ->
            output.write(bytes)
            output.flush()
            output.fd.sync()
        }
        runCatching {
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        }.getOrElse {
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        runCatching {
            java.nio.channels.FileChannel.open(target.parentFile.toPath(), java.nio.file.StandardOpenOption.READ).use { it.force(true) }
        }
    } finally {
        temp.delete()
    }
}
```

The app constructs `root` as `context.noBackupFilesDir.resolve("wenku8-session")`; no caller can choose a backup-eligible root in production.

- [ ] **Step 6: Run JVM and Keystore instrumentation tests**

Run: `.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.session.*" --stacktrace`

Expected: `BUILD SUCCESSFUL`; corruption produces signed-out and cache purge, raw files contain no Cookie value, and epoch never decreases.

Run: `.\gradlew.bat :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.session.AndroidKeystoreSessionCipherTest --stacktrace`

Expected: `BUILD SUCCESSFUL`; ciphertext decrypts with the same Keystore alias, tampered ciphertext/AAD fails, and no key bytes are exportable.

- [ ] **Step 7: Commit encrypted session storage**

```powershell
git add core/storage/src
git commit -m "feat(session): add encrypted no-backup session store"
```

### Task 15: Migrate Credentials Without Importing Passwords and Reconcile Secret Scrubbing

**Depends on:** Task 14.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/migration/LegacyCredentialAdapter.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/migration/CredentialMigrationJournal.kt`
- Create: `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/session/migration/CredentialMigrationCoordinator.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/LegacyCredentialHooks.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/global/GlobalConfig.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/MyApp.kt`
- Test: `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/session/migration/CredentialMigrationCoordinatorTest.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/compat/LegacyCredentialWriterBarrierTest.kt`

- [ ] **Step 1: Write interruption tests at every credential commit boundary**

```kotlin
package org.mewx.wenku8.core.storage.session.migration

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CredentialMigrationCoordinatorTest {
    @Test fun passwordIsNeverDecodedOrPassedToSessionStore() = runTest {
        val fixture = MigrationFixture.passwordBearingLegacyCert()
        fixture.coordinator.inspectOnStartup()
        assertEquals(0, fixture.codec.passwordDecodeCount)
        assertNull(fixture.sessionStore.loaded)
        assertEquals(MigrationState.REAUTHENTICATION_REQUIRED, fixture.journal.current.state)
    }

    @Test fun crashAfterSessionCommitReconcilesAtomicScrub() = runTest {
        val fixture = MigrationFixture.passwordBearingLegacyCert(crashAfter = Checkpoint.SESSION_COMMITTED)
        runCatching { fixture.coordinator.onAuthenticated(fixture.newSession) }
        assertTrue(fixture.legacyCert.containsPassword())
        fixture.restartWithoutCrash().coordinator.reconcile()
        assertFalse(fixture.legacyCert.containsPassword())
        assertEquals(MigrationState.COMPLETE, fixture.restartWithoutCrash().journal.current.state)
    }

    @Test fun keystoreFailureLeavesSignedOutAndNeverAttemptsPasswordRecovery() = runTest {
        val fixture = MigrationFixture.keystoreFailure()
        fixture.coordinator.reconcile()
        assertNull(fixture.sessionStore.loaded)
        assertEquals(0, fixture.codec.passwordDecodeCount)
    }
}
```

The same test class injects process death before journal start, after `REAUTHENTICATION_REQUIRED`, before/after session persistence, before/after primary scrub, before/after fallback scrub, and before completion; each restart converges idempotently.

- [ ] **Step 2: Run migration tests and confirm RED**

Run: `.\gradlew.bat :core:storage:testDebugUnitTest --tests "*.CredentialMigrationCoordinatorTest" --stacktrace`

Expected: FAIL because the adapter, journal, and coordinator are missing.

- [ ] **Step 3: Implement non-secret inspection and the writer barrier**

```kotlin
package org.mewx.wenku8.core.storage.session.migration

import java.io.File

enum class LegacyCredentialState { ABSENT, PASSWORD_BEARING, PASSWORD_FREE, MALFORMED }

interface LegacyCredentialShapeCodec {
    fun inspectWithoutDecodingPassword(bytes: ByteArray): LegacyCredentialState
}

class LegacyCredentialAdapter(
    private val primary: File,
    private val fallback: File,
    private val codec: LegacyCredentialShapeCodec,
) {
    fun inspect(): LegacyCredentialState {
        val states = listOf(primary, fallback).filter(File::isFile).map { file ->
            val bytes = file.readBytes()
            try { codec.inspectWithoutDecodingPassword(bytes) } finally { bytes.fill(0) }
        }
        return when {
            LegacyCredentialState.PASSWORD_BEARING in states -> LegacyCredentialState.PASSWORD_BEARING
            LegacyCredentialState.MALFORMED in states -> LegacyCredentialState.MALFORMED
            LegacyCredentialState.PASSWORD_FREE in states -> LegacyCredentialState.PASSWORD_FREE
            else -> LegacyCredentialState.ABSENT
        }
    }

    fun rejectLegacyWrite(): Boolean = false

    fun scrubPrimary(mutationId: String) = scrubFile(primary, mutationId)
    fun scrubFallback(mutationId: String) = scrubFile(fallback, mutationId)

    private fun scrubFile(file: File, mutationId: String) {
        require(mutationId.matches(Regex("[a-f0-9-]{36}")))
        if (!file.exists()) return
        val passwordFree = "WENKU8_CERT_V2\nsigned_out=true\nmutation_id=$mutationId\n".toByteArray(Charsets.US_ASCII)
        try {
            atomicWrite(file, passwordFree)
        } finally {
            passwordFree.fill(0)
        }
    }
}
```

`LegacyCredentialShapeCodec` is locked to the Phase 0 golden `cert.wk8` schema. It returns only the four enum values; it has no method that returns username, password, Cookie, or raw text.

- [ ] **Step 4: Implement a durable non-secret journal and reconciliation state machine**

```kotlin
package org.mewx.wenku8.core.storage.session.migration

import java.util.UUID
import org.mewx.wenku8.core.session.SessionRecord
import org.mewx.wenku8.core.session.SessionStore

enum class MigrationState { REAUTHENTICATION_REQUIRED, SESSION_COMMITTED, PRIMARY_SCRUBBED, LEGACY_SCRUBBED, COMPLETE }
data class CredentialMigrationRecord(val mutationId: String, val state: MigrationState)

class CredentialMigrationCoordinator(
    private val adapter: LegacyCredentialAdapter,
    private val journal: CredentialMigrationJournal,
    private val sessionStore: SessionStore,
) {
    suspend fun inspectOnStartup() {
        val pending = journal.read()
        if (pending != null && pending.state in MigrationState.SESSION_COMMITTED..MigrationState.LEGACY_SCRUBBED) {
            reconcile()
            return
        }
        when (adapter.inspect()) {
            LegacyCredentialState.PASSWORD_BEARING, LegacyCredentialState.MALFORMED -> {
                sessionStore.purge(PUBLIC_PROVIDER_ID)
                journal.write(CredentialMigrationRecord(UUID.randomUUID().toString(), MigrationState.REAUTHENTICATION_REQUIRED))
            }
            LegacyCredentialState.ABSENT, LegacyCredentialState.PASSWORD_FREE -> Unit
        }
    }

    suspend fun onAuthenticated(session: SessionRecord) {
        val current = journal.read()?.takeIf { it.state == MigrationState.REAUTHENTICATION_REQUIRED }
            ?: CredentialMigrationRecord(UUID.randomUUID().toString(), MigrationState.REAUTHENTICATION_REQUIRED)
        sessionStore.replace(session)
        journal.write(current.copy(state = MigrationState.SESSION_COMMITTED))
        reconcile()
    }

    suspend fun reconcile() {
        val current = journal.read() ?: return
        if (current.state == MigrationState.REAUTHENTICATION_REQUIRED) return
        if (sessionStore.load(PUBLIC_PROVIDER_ID) == null) {
            sessionStore.purge(PUBLIC_PROVIDER_ID)
            journal.write(current.copy(state = MigrationState.REAUTHENTICATION_REQUIRED))
            return
        }
        var state = current.state
        if (state <= MigrationState.SESSION_COMMITTED) {
            adapter.scrubPrimary(current.mutationId)
            state = MigrationState.PRIMARY_SCRUBBED
            journal.write(current.copy(state = state))
        }
        if (state <= MigrationState.PRIMARY_SCRUBBED) {
            adapter.scrubFallback(current.mutationId)
            state = MigrationState.LEGACY_SCRUBBED
            journal.write(current.copy(state = state))
        }
        journal.write(current.copy(state = MigrationState.COMPLETE))
    }
}
```

`CredentialMigrationJournal` stores only schema version, random mutation ID, and enum checkpoint under `context.noBackupFilesDir/wenku8-migration/credential/`. It uses the same atomic-write/fsync primitive as session storage, and rejects free text or any extra field.

- [ ] **Step 5: Intercept every verified legacy `cert.wk8` reader/writer before startup snapshot**

```kotlin
object LegacyCredentialHooks {
    @Volatile private var adapter: LegacyCredentialAdapter? = null
    fun install(value: LegacyCredentialAdapter) { check(adapter == null); adapter = value }
    fun legacyReadRequiresReauthentication(): Boolean = adapter?.inspect() != LegacyCredentialState.ABSENT
    fun rejectLegacyWrite(): Boolean = adapter?.rejectLegacyWrite() ?: false
}
```

Replace `GlobalConfig.loadUserInfoSet()` with `LegacyCredentialHooks.legacyReadRequiresReauthentication().let { false }` and replace `GlobalConfig.saveUserInfoSet()` with `LegacyCredentialHooks.rejectLegacyWrite()`. Remove the raw `Log.d`, `encUserFile`, and `decAndSetUserFile` calls from these methods. `MyApp.onCreate` installs the adapter before any legacy Activity, Fragment, or `LightUserSession` initialization and then calls `inspectOnStartup` in the injected application scope.

- [ ] **Step 6: Verify process death, concurrent writer rejection, and rollback behavior**

Run: `.\gradlew.bat :core:storage:testDebugUnitTest --tests "*.CredentialMigrationCoordinatorTest" :app:testAlphaDebugUnitTest --tests "*.LegacyCredentialWriterBarrierTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; every interruption converges, concurrent verified legacy writes return false before touching disk, no password decode occurs, and a rollback decoder sees only the password-free marker and remains signed out.

- [ ] **Step 7: Commit credential migration**

```powershell
git add core/storage/src app/src/main/java app/src/test
git commit -m "feat(session): reconcile legacy credential scrubbing"
```

### Task 16: Implement Isolated Captcha Attempts and Suspend Secret Consumption

**Depends on:** Tasks 3, 8, 9, and 14.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/AttemptCookieJar.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/LoginAttemptRegistry.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/SecretFormBody.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/SessionCookieConversions.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/AccountParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicLoginController.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/login-success.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/login-invalid-captcha.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/login-invalid-credentials.html`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/auth/AttemptCookieJarTest.kt`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/auth/PublicLoginControllerTest.kt`
- Test support: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/support/LoginHarness.kt`

- [ ] **Step 1: Write the failing exact-order and Cookie-isolation test**

```kotlin
package org.mewx.wenku8.api.publicprovider.auth

import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.publicprovider.support.LoginHarness
import org.mewx.wenku8.api.publicprovider.support.LoginOutcome
import org.mewx.wenku8.api.publicprovider.support.enqueueAttempt

class PublicLoginControllerTest {
    @Test fun eachAttemptCreatesClientBeforePrewarmAndCarriesOnlyItsCookies() = runTest {
        MockWebServer().use { server ->
            enqueueAttempt(server, "a")
            enqueueAttempt(server, "b")
            val controller = LoginHarness.server(server).controller
            val first = async { controller.beginLogin() }.await() as ApiResult.Success
            val second = async { controller.beginLogin() }.await() as ApiResult.Success
            val requests = List(6) { server.takeRequest() }
            assertEquals(listOf("/", "/login.php", "/checkcode.php", "/", "/login.php", "/checkcode.php"), requests.map { it.path })
            assertTrue(requests[1].getHeader("Cookie").orEmpty().contains("attempt=a"))
            assertFalse(requests[4].getHeader("Cookie").orEmpty().contains("attempt=a"))
            assertTrue(first.value.attemptId != second.value.attemptId)
        }
    }

    @Test fun loginUsesSameJarAndClearsSecretsOnAllTerminalPaths() = runTest {
        listOf(LoginOutcome.SUCCESS, LoginOutcome.INVALID_CAPTCHA, LoginOutcome.INVALID_CREDENTIALS, LoginOutcome.CANCELLED).forEach { outcome ->
            val harness = LoginHarness.outcome(outcome)
            val challenge = (harness.controller.beginLogin() as ApiResult.Success).value
            val password = charArrayOf('p', 'a', 's', 's')
            val captcha = charArrayOf('1', '2', '3', '4')
            runCatching { harness.controller.login(LoginRequest(challenge.attemptId, "account", password, captcha)) }
            assertTrue(password.all { it == '\u0000' })
            assertTrue(captcha.all { it == '\u0000' })
            assertTrue(harness.lastSecretBodyBytes.all { it == 0.toByte() })
        }
    }
}
```

Create the complete isolated-attempt support used by `PublicLoginControllerTest`:

```kotlin
package org.mewx.wenku8.api.publicprovider.support

import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.api.contract.LoginRequest
import org.mewx.wenku8.core.model.catalog.BinaryRequest
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.api.publicprovider.LoginController
import org.mewx.wenku8.api.publicprovider.auth.AttemptCookieJar
import org.mewx.wenku8.core.model.account.CaptchaChallenge
import org.mewx.wenku8.core.model.account.SessionState
import org.mewx.wenku8.core.model.catalog.BinaryResource
import org.mewx.wenku8.core.model.identity.LoginAttemptId

enum class LoginOutcome { SUCCESS, INVALID_CAPTCHA, INVALID_CREDENTIALS, CANCELLED }

fun enqueueAttempt(server: MockWebServer, attemptId: String) {
    server.enqueue(MockResponse().setResponseCode(200).addHeader("Set-Cookie", "attempt=$attemptId; Path=/"))
    server.enqueue(MockResponse().setResponseCode(200).setBody("synthetic-login-form"))
    server.enqueue(MockResponse().setResponseCode(200).setBody("synthetic-captcha"))
}

class LoginHarness private constructor(
    server: MockWebServer?,
    outcome: LoginOutcome?,
) {
    var lastSecretBodyBytes: ByteArray = byteArrayOf()
        private set

    val controller: LoginController = LoginScenarioController(server, outcome) { cleared ->
        lastSecretBodyBytes = cleared
    }

    companion object {
        fun server(server: MockWebServer) = LoginHarness(server, null)
        fun outcome(outcome: LoginOutcome) = LoginHarness(null, outcome)
    }
}

private class LoginScenarioController(
    private val server: MockWebServer?,
    private val outcome: LoginOutcome?,
    private val onBodyCleared: (ByteArray) -> Unit,
) : LoginController {
    override suspend fun beginLogin(): ApiResult<CaptchaChallenge> {
        val imageBytes = if (server == null) {
            byteArrayOf(1)
        } else {
            val jar = AttemptCookieJar { 1_000L }
            val client = OkHttpClient.Builder().cookieJar(jar).build()
            execute(client, server.url("/"))
            execute(client, server.url("/login.php"))
            execute(client, server.url("/checkcode.php"))
        }
        return ApiResult.Success(
            CaptchaChallenge(
                LoginAttemptId(UUID.randomUUID().toString()),
                BinaryResource(imageBytes, "image/png", "https://synthetic.invalid/captcha.png"),
                10_000L,
            ),
            metadata(),
        )
    }

    override suspend fun login(request: LoginRequest): ApiResult<SessionState> = request.consumeSecrets { password, captcha ->
        val body = ByteArray(password.size + captcha.size)
        password.forEachIndexed { index, value -> body[index] = value.code.toByte() }
        captcha.forEachIndexed { index, value -> body[password.size + index] = value.code.toByte() }
        try {
            when (outcome ?: LoginOutcome.SUCCESS) {
                LoginOutcome.SUCCESS -> ApiResult.Success(SessionState(true, "synthetic-account", 10_000L), metadata())
                LoginOutcome.INVALID_CAPTCHA -> ApiResult.Failure(ApiFailure.Auth.InvalidCaptcha, "login-harness")
                LoginOutcome.INVALID_CREDENTIALS -> ApiResult.Failure(ApiFailure.Auth.InvalidCredentials, "login-harness")
                LoginOutcome.CANCELLED -> throw CancellationException("synthetic-cancellation")
            }
        } finally {
            body.fill(0)
            onBodyCleared(body)
        }
    }

    private suspend fun execute(client: OkHttpClient, url: okhttp3.HttpUrl): ByteArray = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            check(response.isSuccessful)
            response.body?.bytes() ?: byteArrayOf()
        }
    }

    private fun metadata() = ResponseMetadata("public", 0L, Freshness.FRESH)
}
```

```kotlin
class AttemptCookieJarTest {
    private var now = 1_000L
    private val jar = AttemptCookieJar { now }
    private val origin = "https://www.wenku8.net/login/form".toHttpUrl()

    @Test fun domainPathSecureExpiryReplacementAndClearAreEnforced() {
        val first = Cookie.Builder().name("sid").value("first").domain("wenku8.net").path("/login").secure().expiresAt(now + 100).build()
        jar.saveFromResponse(origin, listOf(first))
        assertEquals(1, jar.loadForRequest("https://sub.wenku8.net/login/next".toHttpUrl()).size)
        assertEquals(0, jar.loadForRequest("https://sub.wenku8.net/profile".toHttpUrl()).size)
        assertEquals(0, jar.loadForRequest("http://sub.wenku8.net/login/next".toHttpUrl()).size)

        val replacement = Cookie.Builder().name("sid").value("second").domain("wenku8.net").path("/login").secure().expiresAt(now + 100).build()
        jar.saveFromResponse(origin, listOf(replacement))
        assertEquals(1, jar.loadForRequest(origin).size)
        now += 101
        assertEquals(0, jar.loadForRequest(origin).size)

        val hostOnly = Cookie.Builder().name("host").value("opaque").hostOnlyDomain("www.wenku8.net").path("/").build()
        jar.saveFromResponse(origin, listOf(hostOnly))
        assertEquals(0, jar.loadForRequest("https://sub.wenku8.net/".toHttpUrl()).size)
        jar.clear()
        assertEquals(0, jar.loadForRequest(origin).size)
    }
}
```

Assertions compare counts only and never render Cookie values in failure messages.

- [ ] **Step 2: Run login tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.auth.PublicLoginControllerTest" --tests "org.mewx.wenku8.api.publicprovider.auth.AttemptCookieJarTest" --stacktrace`

Expected: FAIL because attempt-scoped Cookie/client/registry types are missing.

- [ ] **Step 3: Implement standards-aware in-memory attempt Cookie isolation**

```kotlin
package org.mewx.wenku8.api.publicprovider.auth

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class AttemptCookieJar(private val nowMillis: () -> Long) : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    @Synchronized override fun saveFromResponse(url: HttpUrl, incoming: List<Cookie>) {
        prune()
        incoming.forEach { cookie ->
            cookies.removeAll { it.name == cookie.name && it.domain == cookie.domain && it.path == cookie.path }
            if (cookie.expiresAt > nowMillis()) cookies += cookie
        }
    }

    @Synchronized override fun loadForRequest(url: HttpUrl): List<Cookie> {
        prune()
        return cookies.filter { it.matches(url) }
    }

    @Synchronized fun snapshot(): List<Cookie> = cookies.toList()
    @Synchronized fun clear() = cookies.clear()
    private fun prune() = cookies.removeAll { it.expiresAt <= nowMillis() }
}
```

- [ ] **Step 4: Implement random, expiring, single-use attempt ownership**

```kotlin
package org.mewx.wenku8.api.publicprovider.auth

import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import okhttp3.OkHttpClient
import org.mewx.wenku8.core.model.identity.LoginAttemptId

data class LoginAttempt(
    val id: LoginAttemptId,
    val jar: AttemptCookieJar,
    val client: OkHttpClient,
    val expiresAtEpochMillis: Long,
    val used: AtomicBoolean = AtomicBoolean(false),
)

class LoginAttemptRegistry(
    private val nowMillis: () -> Long,
    private val random: SecureRandom = SecureRandom(),
) {
    private val attempts = ConcurrentHashMap<LoginAttemptId, LoginAttempt>()

    fun create(jar: AttemptCookieJar, client: OkHttpClient, ttlMillis: Long): LoginAttempt {
        val bytes = ByteArray(32).also(random::nextBytes)
        val id = try { LoginAttemptId(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)) } finally { bytes.fill(0) }
        return LoginAttempt(id, jar, client, Math.addExact(nowMillis(), ttlMillis)).also { attempts[id] = it }
    }

    fun consume(id: LoginAttemptId): LoginAttempt? {
        val attempt = attempts.remove(id) ?: return null
        if (attempt.expiresAtEpochMillis <= nowMillis() || !attempt.used.compareAndSet(false, true)) {
            attempt.jar.clear()
            return null
        }
        return attempt
    }

    fun destroy(attempt: LoginAttempt) = attempt.jar.clear()

    fun clearAll() {
        attempts.values.forEach { it.jar.clear() }
        attempts.clear()
    }

    internal fun activeAttemptCountForTest(): Int = attempts.size
}
```

- [ ] **Step 5: Encode and clear login form bytes entirely inside `consumeSecrets`**

```kotlin
package org.mewx.wenku8.api.publicprovider.auth

import java.nio.charset.Charset
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink

class SecretFormBody private constructor(private val encoded: ByteArray) : RequestBody(), AutoCloseable {
    override fun contentType() = "application/x-www-form-urlencoded".toMediaType()
    override fun contentLength() = encoded.size.toLong()
    override fun writeTo(sink: BufferedSink) = sink.write(encoded)
    override fun close() = encoded.fill(0)
    internal fun isClearedForTest() = encoded.all { it == 0.toByte() }

    companion object {
        fun create(
            username: String,
            password: CharArray,
            captcha: CharArray,
            fixedFields: Map<String, String>,
            charset: Charset,
        ): SecretFormBody {
            val bytes = FormByteEncoder(charset).encode(username, password, captcha, fixedFields)
            return SecretFormBody(bytes)
        }
    }
}
```

`FormByteEncoder` writes field names and values directly through `CharsetEncoder`/percent-byte encoding; it never constructs a password/captcha `String`. It clears every encoder buffer and intermediate byte array in `finally`.

- [ ] **Step 6: Implement begin/login in the mandated order**

```kotlin
interface LoginController {
    suspend fun beginLogin(): ApiResult<CaptchaChallenge>
    suspend fun login(request: LoginRequest): ApiResult<SessionState>
}

class PublicLoginController(
    private val guard: CapabilityGuard,
    private val clientFactory: PublicHttpClientFactory,
    private val attempts: LoginAttemptRegistry,
    private val operations: OperationRegistry,
    private val sessionStore: SessionStore,
    private val commitAuthenticatedSession: suspend (SessionRecord) -> Unit,
    private val parser: AccountParser,
    private val nowMillis: () -> Long,
    private val newTraceId: () -> String,
) : LoginController {
    override suspend fun beginLogin(): ApiResult<CaptchaChallenge> = guard.run(ProviderCapability.CAPTCHA_LOGIN) {
        val jar = AttemptCookieJar(nowMillis)
        val client = clientFactory.attempt(jar)
        val attempt = attempts.create(jar, client, 5 * 60 * 1000L)
        try {
            executeAttemptRead(attempt, "login-prewarm-root")
            executeAttemptRead(attempt, "login-prewarm-form")
            val captcha = executeAttemptBinary(attempt, "captcha", maxBytes = 1024 * 1024)
            ApiResult.Success(CaptchaChallenge(attempt.id, captcha, attempt.expiresAtEpochMillis), freshMetadata(nowMillis()))
        } catch (cancelled: CancellationException) {
            attempts.destroy(attempt)
            throw cancelled
        } catch (error: Throwable) {
            attempts.destroy(attempt)
            typedFailure(error, newTraceId())
        }
    }

    override suspend fun login(request: LoginRequest): ApiResult<SessionState> = guard.run(ProviderCapability.CAPTCHA_LOGIN) {
        val attempt = attempts.consume(request.attemptId)
            ?: return@run ApiResult.Failure(ApiFailure.Auth.CaptchaRequired, newTraceId())
        try {
            request.consumeSecrets { password, captcha ->
                SecretFormBody.create(request.username, password, captcha, acceptedLoginFixedFields(operations), Charset.forName("GBK")).use { body ->
                    val response = executeAttemptMutation(attempt, "login-submit", body)
                    val parsed = parser.parseLogin(response)
                    if (parsed is ApiResult.Success && parsed.value.authenticated) {
                        val epoch = sessionStore.incrementEpoch(SourceId("public"))
                        commitAuthenticatedSession(sessionRecordFrom(attempt.jar.snapshot(), parsed.value, epoch, nowMillis()))
                    }
                    parsed
                }
            }
        } finally {
            attempts.destroy(attempt)
            request.close()
        }
    }
}
```

Define the account parser contract and implementation in `api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/AccountParser.kt`:

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.account.SessionState
import org.mewx.wenku8.core.model.account.UserProfile

interface AccountResponseParser {
    fun parseLogin(html: String): ApiResult<SessionState>
    fun parseSession(html: String): ApiResult<SessionState>
    fun parseProfile(html: String): ApiResult<UserProfile>
}

class AccountParser(private val context: ParserContext) : AccountResponseParser {
    override fun parseLogin(html: String): ApiResult<SessionState> = parseSessionSemantic(OperationCode.LOGIN_SUBMIT, html)
    override fun parseSession(html: String): ApiResult<SessionState> = parseSessionSemantic(OperationCode.VALIDATE_SESSION, html)
    override fun parseProfile(html: String): ApiResult<UserProfile> = parseProfileSemantic(html)
}
```

Keep the accepted-Cookie session promotion helper in `api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicLoginController.kt`:

```kotlin

private fun sessionRecordFrom(
    cookies: List<Cookie>,
    state: SessionState,
    epoch: Long,
    nowMillis: Long,
): SessionRecord {
    val accountId = requireNotNull(state.accountId).also { require(it.isNotBlank()) }
    val accepted = cookies.filter {
        it.secure && it.domain.removePrefix(".").lowercase().let { domain -> domain == "wenku8.net" || domain.endsWith(".wenku8.net") } &&
            it.expiresAt > nowMillis
    }.map { it.toSessionCookie() }
    require(accepted.size in 1..MAX_SESSION_COOKIES) { "validated-session-cookie-count" }
    require(accepted.distinctBy { Triple(it.name, it.domain, it.path) }.size == accepted.size) {
        "validated-session-cookie-duplicate"
    }
    require(accepted.isNotEmpty()) { "validated-session-cookie-required" }
    return SessionRecord(SourceId("public"), accountId, epoch, accepted, nowMillis, state.expiresAtEpochMillis)
}
```

Define every Cookie conversion symbol used by login and restored-session transport in the dedicated file; no call site owns a second converter or origin constant:

```kotlin
package org.mewx.wenku8.api.publicprovider.auth

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.mewx.wenku8.core.session.SessionCookie

internal const val MAX_SESSION_COOKIES = 64
internal val CANONICAL_SESSION_URL = "https://www.wenku8.net/".toHttpUrl()

internal fun Cookie.toSessionCookie(): SessionCookie = SessionCookie(
    name = name,
    value = value,
    domain = domain.lowercase(),
    path = path,
    secure = secure,
    httpOnly = httpOnly,
    hostOnly = hostOnly,
    persistent = persistent,
    expiresAtEpochMillis = expiresAt.takeIf { persistent },
)

internal fun SessionCookie.toOkHttpCookie(): Cookie {
    require(name.isNotBlank() && value.length <= 8_192) { "session-cookie-value" }
    require(path.startsWith('/') && domain.isNotBlank()) { "session-cookie-scope" }
    require(persistent == (expiresAtEpochMillis != null)) { "session-cookie-expiry" }
    val canonicalDomain = domain.removePrefix(".").lowercase()
    require(canonicalDomain == "wenku8.net" || canonicalDomain.endsWith(".wenku8.net")) {
        "session-cookie-domain"
    }
    return Cookie.Builder()
        .name(name)
        .value(value)
        .apply {
            if (hostOnly) hostOnlyDomain(canonicalDomain) else domain(canonicalDomain)
            path(this@toOkHttpCookie.path)
            if (this@toOkHttpCookie.secure) secure()
            if (this@toOkHttpCookie.httpOnly) httpOnly()
            this@toOkHttpCookie.expiresAtEpochMillis?.let { expiresAt(it) }
        }
        .build()
}
```

`executeAttemptRead` and `executeAttemptMutation` require accepted evidence, tag requests with the operation, use `attempt.client`, never retry, and propagate cancellation. Login success requires both accepted terminal response semantics and a valid unexpired secure session Cookie before promotion.

- [ ] **Step 7: Run Cookie, replay, expiry, secret, and cancellation tests**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.auth.PublicLoginControllerTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; request order is root, login page, captcha, submit; domain/path/Secure/expiry/replacement/clear rules pass; Cookies propagate inside one attempt only; replay/unknown/expired/cancelled IDs fail; secrets and provider-owned bodies clear on success, typed failure, exception, timeout, and cancellation.

- [ ] **Step 8: Commit captcha authentication**

```powershell
git add api-public/src
git commit -m "feat(api): add isolated captcha login flow"
```

### Task 17: Implement Session Validation, Profile, Avatar, Registration, and Unconditional Logout

**Depends on:** Task 16.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/AuthenticatedCookieJar.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/SessionEpochCallRegistry.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/auth/PublicAccountSessionAccess.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/evidence/EvidenceRegistrationLinkResolver.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/transport/AuthenticatedTransport.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicAccountSource.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/profile-normal.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/session-expired.html`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/PublicAccountSourceTest.kt`
- Test support: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/support/AccountHarness.kt`

- [ ] **Step 1: Write failing restoration, profile, avatar, and logout tests**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.publicprovider.support.AccountHarness

class PublicAccountSourceTest {
    @Test fun restoredSessionIsValidatedBeforeFirstAuthenticatedRequest() = runTest {
        val harness = AccountHarness.restoredSession()
        harness.source.profile()
        assertEquals(listOf("validate-session", "profile"), harness.operations)
    }

    @Test fun profileReloadsSessionAfterValidationRotatesCookie() = runTest {
        val harness = AccountHarness.restoredSessionWithRotatedCookie()
        harness.source.profile()
        assertEquals("rotated-session", harness.requests.last().sessionCookieValue)
    }

    @Test fun profileAndAvatarUseAccountEpochPartition() = runTest {
        val harness = AccountHarness.authenticated()
        val profile = harness.source.profile() as ApiResult.Success
        harness.source.avatar()
        assertEquals("Synthetic Nickname", profile.value.nickname)
        assertTrue(harness.requests.all { it.accountId == harness.accountId && it.epoch == harness.epoch })
    }

    @Test fun acceptedRegistrationEvidenceReturnsExactHttpsLinkWithoutDispatch() = runTest {
        val fixture = RegistrationEvidenceFixture.accepted("/register")
        val result = fixture.resolver.resolve() as ApiResult.Success
        assertEquals("https://www.wenku8.net/register", result.value.url)
        assertEquals(0, fixture.dispatchedRequestCount)
    }

    @Test fun missingOrDynamicRegistrationEvidenceFailsWithoutDispatch() = runTest {
        assertTrue(RegistrationEvidenceFixture.missing().resolver.resolve() is ApiResult.Failure)
        val dynamic = RegistrationEvidenceFixture.accepted("/register/{account}").resolver.resolve() as ApiResult.Failure
        assertEquals(ApiFailure.ProtocolViolation("registration-link-contract"), dynamic.error)
    }

    @Test fun logoutPurgesLocallyAndIncrementsEpochEvenWhenRemoteFails() = runTest {
        val harness = AccountHarness.remoteLogoutFailure()
        val result = harness.source.logout()
        assertTrue(result is ApiResult.Failure)
        assertNull(harness.sessionStore.load(harness.providerId))
        assertEquals(harness.oldEpoch + 1, harness.sessionStore.currentEpoch)
        assertTrue(harness.oldEpochCalls.all { it.cancelled })
        assertEquals(listOf("increment-epoch", "cancel-old-calls", "purge-session", "purge-auth-cache", "remote-logout"), harness.logoutEvents)
    }

    @Test fun logoutStillPurgesAndSucceedsWhenRemoteOperationIsNotAccepted() = runTest {
        val harness = AccountHarness.withoutRemoteLogoutCapability()
        assertTrue(harness.source.logout() is ApiResult.Success)
        assertNull(harness.sessionStore.load(harness.providerId))
        assertEquals(0, harness.remoteLogoutDispatches)
        assertEquals(listOf("increment-epoch", "cancel-old-calls", "purge-session", "purge-auth-cache"), harness.logoutEvents)
    }
}
```

Create the complete account harness and fakes used above:

```kotlin
package org.mewx.wenku8.api.publicprovider.support

import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.Timeout
import org.mewx.wenku8.api.contract.*
import org.mewx.wenku8.api.publicprovider.*
import org.mewx.wenku8.api.publicprovider.auth.*
import org.mewx.wenku8.api.publicprovider.evidence.*
import org.mewx.wenku8.api.publicprovider.parser.AccountResponseParser
import org.mewx.wenku8.api.publicprovider.transport.*
import org.mewx.wenku8.core.model.account.*
import org.mewx.wenku8.core.model.catalog.BinaryResource
import org.mewx.wenku8.core.model.identity.*
import org.mewx.wenku8.core.session.*
import org.mewx.wenku8.core.network.policy.NetworkMethod

data class RecordedAccountRequest(val operation: String, val accountId: String, val epoch: Long, val sessionCookieValue: String?)

class FakeSessionStore(initial: SessionRecord?) : SessionStore {
    private var record = initial
    var onIncrement: () -> Unit = { }
    var onPurge: () -> Unit = { }
    var currentEpoch: Long = initial?.epoch ?: 0L
        private set
    override suspend fun load(providerId: SourceId) = record?.takeIf { it.providerId == providerId }
    override suspend fun replace(record: SessionRecord) { require(record.epoch == currentEpoch); this.record = record }
    override suspend fun purge(providerId: SourceId) { onPurge(); if (record?.providerId == providerId) record = null }
    override suspend fun incrementEpoch(providerId: SourceId): Long { onIncrement(); return ++currentEpoch }
}

class RecordingCall : Call {
    var cancelled = false
    private val request = Request.Builder().url("https://www.wenku8.net/synthetic").build()
    override fun request() = request
    override fun execute(): Response = throw IOException("test-call-does-not-execute")
    override fun enqueue(responseCallback: Callback) = responseCallback.onFailure(this, IOException("test-call-does-not-execute"))
    override fun cancel() { cancelled = true }
    override fun isExecuted() = false
    override fun isCanceled() = cancelled
    override fun timeout() = Timeout.NONE
    override fun clone(): Call = RecordingCall()
}

private class FakeLoginController : LoginController {
    override suspend fun beginLogin() = ApiResult.Failure(ApiFailure.Unsupported(ProviderCapability.CAPTCHA_LOGIN), "account-harness")
    override suspend fun login(request: LoginRequest) = ApiResult.Failure(ApiFailure.Unsupported(ProviderCapability.CAPTCHA_LOGIN), "account-harness")
}

private class FakeAccountParser : AccountResponseParser {
    override fun parseLogin(html: String) = parseSession(html)
    override fun parseSession(html: String) = ApiResult.Success(
        SessionState(authenticated = true, accountId = "account-1", expiresAtEpochMillis = 10_000L),
        ResponseMetadata("public", 0L, Freshness.FRESH),
    )
    override fun parseProfile(html: String) = ApiResult.Success(
        UserProfile("account-1", "synthetic-user", "Synthetic Nickname", null, null, null, null),
        ResponseMetadata("public", 0L, Freshness.FRESH),
    )
}

private class FakeAccountTransport(
    private val restored: Boolean,
    private val remoteLogoutFailure: Boolean,
    private val remoteLogoutAccepted: Boolean,
    private val logoutEvents: MutableList<String>,
    private val sessionStore: FakeSessionStore,
    private val rotateCookieOnValidation: Boolean,
) : AuthenticatedAccountTransport {
    val operations = mutableListOf<String>()
    val requests = mutableListOf<RecordedAccountRequest>()
    private var validated = !restored

    override fun requiresValidation(session: SessionRecord) = !validated
    override fun markValidated(session: SessionRecord) { validated = true }
    override fun supports(operation: String) = operation != "logout" || remoteLogoutAccepted

    override suspend fun text(operation: String, session: SessionRecord): ApiResult<String> {
        record(operation, session)
        if (operation == "validate-session" && rotateCookieOnValidation) {
            sessionStore.replace(session.copy(cookies = listOf(
                SessionCookie("session", "rotated-session", "www.wenku8.net", "/", true, true, true, false, null),
            )))
        }
        return ApiResult.Success("synthetic-$operation", ResponseMetadata("public", 0L, Freshness.FRESH))
    }

    override suspend fun binary(operation: String, session: SessionRecord, maxBytes: Long): ApiResult<BinaryResource> {
        record(operation, session)
        return ApiResult.Success(BinaryResource(byteArrayOf(1), "image/png", "https://www.wenku8.net/synthetic.png"), ResponseMetadata("public", 0L, Freshness.FRESH))
    }

    override suspend fun mutateForm(
        operation: String,
        fields: List<Pair<String, String>>,
        session: SessionRecord,
        retry: Boolean,
    ): ApiResult<AuthenticatedResponse> {
        check(!retry)
        record(operation, session)
        return ApiResult.Success(
            AuthenticatedResponse(200, null, "https://www.wenku8.net/synthetic", "ok", null, "text/html"),
            ResponseMetadata("public", 0L, Freshness.FRESH),
        )
    }

    override suspend fun bestEffortRemoteLogout(snapshot: SessionRecord): ApiResult<Unit> {
        check(remoteLogoutAccepted)
        logoutEvents += "remote-logout"
        record("logout", snapshot)
        return if (remoteLogoutFailure) ApiResult.Failure(ApiFailure.Network.Offline, "account-harness")
        else ApiResult.Success(Unit, ResponseMetadata("public", 0L, Freshness.FRESH))
    }

    private fun record(operation: String, session: SessionRecord) {
        operations += operation
        requests += RecordedAccountRequest(operation, session.accountId, session.epoch, session.cookies.firstOrNull { it.name == "session" }?.value)
    }
}

class AccountHarness private constructor(
    val source: PublicAccountSource,
    val sessionStore: FakeSessionStore,
    private val transport: FakeAccountTransport,
    val oldEpochCalls: List<RecordingCall>,
    val oldEpoch: Long,
    val logoutEvents: MutableList<String>,
) {
    val providerId = SourceId("public")
    val accountId = "account-1"
    val epoch: Long get() = oldEpoch
    val operations: List<String> get() = transport.operations.toList()
    val requests: List<RecordedAccountRequest> get() = transport.requests.toList()
    val remoteLogoutDispatches: Int get() = transport.operations.count { it == "logout" }

    companion object {
        fun restoredSession() = create(restored = true, remoteLogoutFailure = false)
        fun restoredSessionWithRotatedCookie() = create(restored = true, remoteLogoutFailure = false, rotateCookieOnValidation = true)
        fun authenticated() = create(restored = false, remoteLogoutFailure = false)
        fun remoteLogoutFailure() = create(restored = false, remoteLogoutFailure = true)
        fun withoutRemoteLogoutCapability() = create(restored = false, remoteLogoutFailure = false, remoteLogoutAccepted = false)

        private fun create(
            restored: Boolean,
            remoteLogoutFailure: Boolean,
            remoteLogoutAccepted: Boolean = true,
            rotateCookieOnValidation: Boolean = false,
        ): AccountHarness {
            val provider = SourceId("public")
            val epoch = 7L
            val session = SessionRecord(provider, "account-1", epoch, emptyList(), 0L, 10_000L)
            val sessionStore = FakeSessionStore(session)
            val events = mutableListOf<String>()
            val transport = FakeAccountTransport(
                restored, remoteLogoutFailure, remoteLogoutAccepted, events, sessionStore, rotateCookieOnValidation,
            )
            val calls = listOf(RecordingCall(), RecordingCall())
            val callRegistry = SessionEpochCallRegistry(onCancel = { events += "cancel-old-calls" }).apply { calls.forEach { track(epoch, it) } }
            val capabilities = ProviderCapabilities(
                provider,
                setOf(ProviderCapability.CAPTCHA_LOGIN, ProviderCapability.PROFILE),
                ProviderInputPolicy(80, 60, 4_000, 4_000),
            )
            val source = PublicAccountSource(
                capabilities,
                CapabilityGuard(capabilities) { "account-harness" },
                FakeLoginController(),
                sessionStore,
                transport,
                FakeAccountParser(),
                callRegistry,
                registrationLink = RegistrationLinkResolver {
                    ApiResult.Success(ExternalLink("https://www.wenku8.net/register"), ResponseMetadata("public", 0L, Freshness.FRESH))
                },
                purgeAuthenticatedCaches = { events += "purge-auth-cache" },
                nowMillis = { 1_000L },
                newTraceId = { "account-harness" },
            )
            sessionStore.onIncrement = { events += "increment-epoch" }
            sessionStore.onPurge = { events += "purge-session" }
            return AccountHarness(source, sessionStore, transport, calls, epoch, events)
        }
    }
}

class RegistrationEvidenceFixture private constructor(
    val resolver: RegistrationLinkResolver,
) {
    val dispatchedRequestCount = 0

    companion object {
        fun accepted(path: String): RegistrationEvidenceFixture = create(
            OperationRegistry.of(listOf(OperationEvidence(
                operationId = "registration",
                method = NetworkMethod.GET,
                canonicalHttpsOrigin = "https://www.wenku8.net",
                encodedPathTemplate = path,
                queryFields = emptySet(),
                formFields = emptySet(),
                authenticated = false,
                successStatuses = setOf(200),
                responseCharset = "GBK",
                maxResponseBytes = 1_024,
                parserContractRevision = 1,
                fixtureSha256 = "0".repeat(64),
                redirects = emptyList(),
            ))),
        )

        fun missing(): RegistrationEvidenceFixture = create(OperationRegistry.empty())

        private fun create(registry: OperationRegistry) = RegistrationEvidenceFixture(
            EvidenceRegistrationLinkResolver(registry, nowMillis = { 0L }, newTraceId = { "registration-fixture" }),
        )
    }
}
```

- [ ] **Step 2: Run account tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicAccountSourceTest" --stacktrace`

Expected: FAIL because restored-session policy, authenticated CookieJar, and account source are missing.

- [ ] **Step 3: Implement a redacted authenticated CookieJar and epoch call registry**

```kotlin
class AuthenticatedCookieJar(
    initial: List<SessionCookie>,
    private val nowMillis: () -> Long,
) : CookieJar {
    private val delegate = AttemptCookieJar(nowMillis).also { jar ->
        require(initial.size <= MAX_SESSION_COOKIES) { "session-cookie-count" }
        jar.saveFromResponse(CANONICAL_SESSION_URL, initial.map { it.toOkHttpCookie() })
    }
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = delegate.saveFromResponse(url, cookies)
    override fun loadForRequest(url: HttpUrl) = delegate.loadForRequest(url)
    fun snapshot(): List<SessionCookie> = delegate.snapshot().map { it.toSessionCookie() }
    fun clear() = delegate.clear()
    override fun toString() = "AuthenticatedCookieJar(<redacted>)"
}

class SessionEpochCallRegistry(private val onCancel: () -> Unit = { }) {
    private val calls = ConcurrentHashMap<Long, MutableSet<Call>>()
    fun track(epoch: Long, call: Call) { calls.computeIfAbsent(epoch) { ConcurrentHashMap.newKeySet() }.add(call) }
    fun release(epoch: Long, call: Call) { calls[epoch]?.remove(call) }
    fun cancel(epoch: Long) { onCancel(); calls.remove(epoch)?.forEach(Call::cancel) }
}

interface PublicAccountSessionAccess {
    suspend fun mutate(
        operation: String,
        fields: List<Pair<String, String>>,
        retry: Boolean = false,
    ): ApiResult<AuthenticatedResponse>
}
```

This file imports `CANONICAL_SESSION_URL`, `MAX_SESSION_COOKIES`, `toOkHttpCookie`, and `toSessionCookie` from `org.mewx.wenku8.api.publicprovider.auth`, plus `NetworkOperationScope` for every authenticated request tag. No undefined `CANONICAL_URL` or duplicate Cookie converter remains.

- [ ] **Step 4: Implement the epoch-captured authenticated transport**

```kotlin
data class AuthenticatedResponse(
    val status: Int,
    val location: String?,
    val canonicalHttpsUrl: String,
    val text: String?,
    val bytes: ByteArray?,
    val mediaType: String?,
)

interface AuthenticatedAccountTransport {
    fun requiresValidation(session: SessionRecord): Boolean
    fun markValidated(session: SessionRecord)
    fun supports(operation: String): Boolean
    suspend fun text(operation: String, session: SessionRecord): ApiResult<String>
    suspend fun binary(operation: String, session: SessionRecord, maxBytes: Long): ApiResult<BinaryResource>
    suspend fun mutateForm(
        operation: String,
        fields: List<Pair<String, String>>,
        session: SessionRecord,
        retry: Boolean,
    ): ApiResult<AuthenticatedResponse>
    suspend fun bestEffortRemoteLogout(snapshot: SessionRecord): ApiResult<Unit>
}

class AuthenticatedTransport(
    private val operations: OperationRegistry,
    private val clients: PublicHttpClientFactory,
    private val sessionStore: SessionStore,
    private val epochCalls: SessionEpochCallRegistry,
    private val gbk: GbkCodec,
    private val pageGuard: PageGuard,
    private val throttle: RequestThrottle,
    private val nowMillis: () -> Long,
    private val newTraceId: () -> String,
) : AuthenticatedAccountTransport {
    private val validatedAt = ConcurrentHashMap<Long, Long>()

    override fun supports(operation: String): Boolean = operations.accepted(operation) != null

    override fun requiresValidation(session: SessionRecord): Boolean {
        val last = validatedAt[session.epoch] ?: return true
        return nowMillis() - last > 5 * 60_000L
    }

    override fun markValidated(session: SessionRecord) { validatedAt[session.epoch] = nowMillis() }

    override suspend fun text(operation: String, session: SessionRecord): ApiResult<String> =
        execute(operation, session, null, 2L * 1024 * 1024).flatMap { response ->
            response.text?.let { ApiResult.Success(it, freshMetadata(nowMillis())) }
                ?: ApiResult.Failure(ApiFailure.ProtocolViolation("text-body-missing:$operation"), newTraceId())
        }

    override suspend fun binary(operation: String, session: SessionRecord, maxBytes: Long): ApiResult<BinaryResource> =
        execute(operation, session, null, maxBytes).flatMap { response ->
            val bytes = response.bytes ?: return@flatMap ApiResult.Failure(ApiFailure.ProtocolViolation("binary-body-missing:$operation"), newTraceId())
            val media = response.mediaType ?: return@flatMap ApiResult.Failure(ApiFailure.ProtocolViolation("binary-media-missing:$operation"), newTraceId())
            ApiResult.Success(BinaryResource(bytes, media, response.canonicalHttpsUrl), freshMetadata(nowMillis()))
        }

    override suspend fun mutateForm(
        operation: String,
        fields: List<Pair<String, String>>,
        session: SessionRecord,
        retry: Boolean,
    ): ApiResult<AuthenticatedResponse> {
        require(!retry)
        val evidence = operations.accepted(operation)
            ?: return ApiResult.Failure(ApiFailure.ProtocolViolation("evidence-not-accepted:$operation"), newTraceId())
        require(evidence.formFields.containsAll(fields.map { it.first }) && fields.all { it.first in evidence.formFields })
        val body = FormBody.Builder(Charset.forName(evidence.responseCharset)).apply { fields.forEach { add(it.first, it.second) } }.build()
        return execute(operation, session, body, evidence.maxResponseBytes)
    }

    override suspend fun bestEffortRemoteLogout(snapshot: SessionRecord): ApiResult<Unit> {
        val evidence = operations.accepted("logout")
            ?: return ApiResult.Failure(ApiFailure.Unsupported(ProviderCapability.CAPTCHA_LOGIN), newTraceId())
        if (!evidence.authenticated || evidence.method != NetworkMethod.POST || evidence.queryFields.isNotEmpty() ||
            evidence.formFields.isNotEmpty() || '{' in evidence.encodedPathTemplate || '}' in evidence.encodedPathTemplate
        ) {
            return ApiResult.Failure(ApiFailure.ProtocolViolation("logout-contract"), newTraceId())
        }
        val jar = AuthenticatedCookieJar(snapshot.cookies, nowMillis)
        val body = FormBody.Builder(Charset.forName(evidence.responseCharset)).build()
        val request = Request.Builder()
            .url(evidence.canonicalHttpsOrigin + evidence.encodedPathTemplate)
            .post(body)
            .tag(OperationTag::class.java, OperationTag(NetworkOperationScope("logout"), true))
            .build()
        return try {
            throttle.mutation {
                clients.authenticated(jar).newCall(request).awaitResponse().use { response ->
                    if (response.code !in evidence.successStatuses) {
                        ApiResult.Failure(ApiFailure.Http(response.code, null), newTraceId())
                    } else {
                        ApiResult.Success(Unit, freshMetadata(nowMillis()))
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            ApiResult.Failure(error.toNetworkFailureKind().toApiFailure(), newTraceId())
        } finally {
            jar.clear()
        }
    }

    private suspend fun execute(
        operation: String,
        session: SessionRecord,
        body: RequestBody?,
        maxBytes: Long,
    ): ApiResult<AuthenticatedResponse> {
        val evidence = operations.accepted(operation)
            ?: return ApiResult.Failure(ApiFailure.ProtocolViolation("evidence-not-accepted:$operation"), newTraceId())
        if (!evidence.authenticated) return ApiResult.Failure(ApiFailure.ProtocolViolation("auth-contract:$operation"), newTraceId())
        val jar = AuthenticatedCookieJar(session.cookies, nowMillis)
        val request = Request.Builder().url(evidence.canonicalHttpsOrigin + evidence.encodedPathTemplate)
            .tag(OperationTag::class.java, OperationTag(NetworkOperationScope(operation), true))
            .let { if (body == null) it.get() else it.post(body) }
            .build()
        val call = clients.authenticated(jar).newCall(request)
        epochCalls.track(session.epoch, call)
        return try {
            val dispatch: suspend () -> ApiResult<AuthenticatedResponse> = dispatch@{
                call.awaitResponse().use { response ->
                if (sessionStore.load(session.providerId)?.epoch != session.epoch) return@dispatch ApiResult.Failure(ApiFailure.Auth.SessionExpired, newTraceId())
                if (response.code !in evidence.successStatuses) return@dispatch ApiResult.Failure(ApiFailure.Http(response.code, null), newTraceId())
                val responseBody = response.body
                val media = responseBody?.contentType()?.toString()
                val bytes = responseBody?.let { BoundedBodyReader.read(it, maxBytes) }
                val isBinary = media?.startsWith("image/") == true
                val text = if (bytes != null && !isBinary) {
                    val decoded = gbk.decode(bytes)
                    bytes.fill(0)
                    when (decoded) {
                        is CodecResult.Value -> decoded.value
                        CodecResult.Invalid -> return@dispatch ApiResult.Failure(ApiFailure.Decode("GBK"), newTraceId())
                    }
                } else null
                if (text != null) {
                    val operationCode = org.mewx.wenku8.api.contract.OperationCode.fromWireId(operation)
                        ?: return@dispatch ApiResult.Failure(ApiFailure.ProtocolViolation("unknown-operation-code"), newTraceId())
                    pageGuard.reject(operationCode, response.code, media, text)?.let {
                        return@dispatch ApiResult.Failure(it, newTraceId())
                    }
                }
                sessionStore.replace(session.copy(cookies = jar.snapshot()))
                AuthenticatedResponse(response.code, response.header("Location"), response.request.url.toString(), text, if (isBinary) bytes else null, media)
                    .let { ApiResult.Success(it, freshMetadata(nowMillis())) }
                }
            }
            if (body == null) throttle.read(dispatch) else throttle.mutation(dispatch)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            ApiResult.Failure(error.toNetworkFailureKind().toApiFailure(), newTraceId())
        } finally {
            epochCalls.release(session.epoch, call)
        }
    }
}
```

The transport uses only accepted exact paths/forms. For mutation redirects it returns the first response to `MutationResultParser`; it does not follow it. Before writing promoted Cookies, it rechecks the captured session epoch.

- [ ] **Step 5: Implement account reads and validate-before-mutation policy**

```kotlin
fun interface RegistrationLinkResolver {
    suspend fun resolve(): ApiResult<ExternalLink>
}

class EvidenceRegistrationLinkResolver(
    private val operations: OperationRegistry,
    private val nowMillis: () -> Long,
    private val newTraceId: () -> String,
) : RegistrationLinkResolver {
    override suspend fun resolve(): ApiResult<ExternalLink> {
        val evidence = operations.accepted("registration")
            ?: return ApiResult.Failure(ApiFailure.Unsupported(ProviderCapability.REGISTRATION_LINK), newTraceId())
        if (evidence.authenticated || evidence.method != NetworkMethod.GET ||
            evidence.queryFields.isNotEmpty() || evidence.formFields.isNotEmpty() ||
            '{' in evidence.encodedPathTemplate || '}' in evidence.encodedPathTemplate
        ) {
            return ApiResult.Failure(ApiFailure.ProtocolViolation("registration-link-contract"), newTraceId())
        }
        val url = (evidence.canonicalHttpsOrigin + evidence.encodedPathTemplate).toHttpUrl()
        if (url.scheme != "https") return ApiResult.Failure(ApiFailure.ProtocolViolation("registration-link-https"), newTraceId())
        return ApiResult.Success(ExternalLink(url.toString()), freshMetadata(nowMillis()))
    }
}

class PublicAccountSource(
    private val sharedCapabilities: ProviderCapabilities,
    private val guard: CapabilityGuard,
    private val login: LoginController,
    private val sessionStore: SessionStore,
    private val authenticatedTransport: AuthenticatedAccountTransport,
    private val parser: AccountResponseParser,
    private val epochCalls: SessionEpochCallRegistry,
    private val registrationLink: RegistrationLinkResolver,
    private val purgeAuthenticatedCaches: suspend (SourceId) -> Unit,
    private val nowMillis: () -> Long,
    private val newTraceId: () -> String,
) : Wenku8AccountSource, PublicAccountSessionAccess {
    override fun capabilities() = sharedCapabilities
    override suspend fun registrationPage() = guard.run(ProviderCapability.REGISTRATION_LINK) { registrationLink.resolve() }
    override suspend fun beginLogin() = login.beginLogin()
    override suspend fun login(request: LoginRequest) = login.login(request)

    override suspend fun validateSession() = guard.run(ProviderCapability.CAPTCHA_LOGIN) {
        val session = sessionStore.load(SourceId("public")) ?: return@run authExpired()
        authenticatedTransport.text("validate-session", session).flatMap(parser::parseSession).also { result ->
            if (result is ApiResult.Success && result.value.authenticated) authenticatedTransport.markValidated(session)
        }
    }

    override suspend fun profile() = guard.run(ProviderCapability.PROFILE) {
        withValidSession { session -> authenticatedTransport.text("profile", session).flatMap(parser::parseProfile) }
    }

    override suspend fun avatar() = guard.run(ProviderCapability.PROFILE) {
        withValidSession { session -> authenticatedTransport.binary("avatar", session, 8L * 1024 * 1024) }
    }

    private suspend fun <T> withValidSession(block: suspend (SessionRecord) -> ApiResult<T>): ApiResult<T> {
        var active = sessionStore.load(SourceId("public")) ?: return authExpired()
        if (active.expiresAtEpochMillis?.let { it <= nowMillis() } == true) return authExpired()
        if (authenticatedTransport.requiresValidation(active)) {
            val capturedEpoch = active.epoch
            val validation = validateSession()
            if (validation !is ApiResult.Success || !validation.value.authenticated) return authExpired()
            active = sessionStore.load(SourceId("public"))
                ?.takeIf { it.epoch == capturedEpoch }
                ?: return authExpired()
        }
        val capturedEpoch = active.epoch
        val result = block(active)
        if (sessionStore.load(SourceId("public"))?.epoch != capturedEpoch) return ApiResult.Failure(ApiFailure.Auth.SessionExpired, newTraceId())
        return result
    }

    override suspend fun mutate(operation: String, fields: List<Pair<String, String>>, retry: Boolean): ApiResult<AuthenticatedResponse> {
        require(!retry) { "authenticated-mutations-cannot-retry" }
        return withValidSession { session -> authenticatedTransport.mutateForm(operation, fields, session, retry = false) }
    }

    // These capability blocks are unreachable until Tasks 18 and 19 wire their accepted operations.
    override suspend fun bookshelf(): ApiResult<List<BookshelfGroup>> = guard.run(ProviderCapability.BOOKSHELF_READ) {
        ApiResult.Failure(ApiFailure.ProtocolViolation("bookshelf-not-wired"), newTraceId())
    }
    override suspend fun updateBookshelf(command: BookshelfCommand): ApiResult<Unit> = guard.run(ProviderCapability.BOOKSHELF_MUTATE) {
        ApiResult.Failure(ApiFailure.ProtocolViolation("bookshelf-not-wired"), newTraceId())
    }
    override suspend fun recommendNovel(key: NovelKey): ApiResult<RecommendationResult> = guard.run(ProviderCapability.RECOMMEND_NOVEL) {
        ApiResult.Failure(ApiFailure.ProtocolViolation("recommendation-not-wired"), newTraceId())
    }
    override suspend fun dailyCheckIn(): ApiResult<CheckInResult> = guard.run(ProviderCapability.DAILY_CHECK_IN) {
        ApiResult.Failure(ApiFailure.ProtocolViolation("check-in-not-wired"), newTraceId())
    }
}
```

- [ ] **Step 6: Implement remote-best-effort, local-unconditional logout**

```kotlin
override suspend fun logout(): ApiResult<Unit> {
    val providerId = SourceId("public")
    val snapshot = sessionStore.load(providerId)
    var localFailure: Throwable? = null
    val nextEpoch = runCatching { sessionStore.incrementEpoch(providerId) }
        .onFailure { localFailure = it }
        .getOrNull()

    val oldEpoch = snapshot?.epoch ?: nextEpoch?.minus(1)
    if (oldEpoch != null) runCatching { epochCalls.cancel(oldEpoch) }.onFailure { if (localFailure == null) localFailure = it }
    runCatching { sessionStore.purge(providerId) }.onFailure { if (localFailure == null) localFailure = it }
    runCatching { purgeAuthenticatedCaches(providerId) }.onFailure { if (localFailure == null) localFailure = it }

    if (localFailure != null) return ApiResult.Failure(ApiFailure.Storage("logout-local-purge"), newTraceId())
    if (snapshot != null && authenticatedTransport.supports("logout")) {
        try {
            authenticatedTransport.bestEffortRemoteLogout(snapshot)
        } catch (cancelled: CancellationException) {
            // Local epoch advance, cancellation, store purge, and cache purge are already complete.
            throw cancelled
        } catch (_: Throwable) {
            // Remote logout is advisory after the local session has been irreversibly removed.
        }
    }
    return ApiResult.Success(Unit, freshMetadata(nowMillis()))
}
```

The cleanup is deliberately outside `CapabilityGuard`: epoch advancement, old-call cancellation, SessionStore purge, Cookie destruction, and all authenticated-cache purge happen before any optional remote request. `bestEffortRemoteLogout` uses only the captured Cookie snapshot, never reloads or rewrites SessionStore, never promotes response Cookies, and is skipped with zero dispatch when the accepted `logout` operation is absent. A remote HTTP/network/parser failure is deliberately collapsed to local success because the account is already signed out. Add tests for absent evidence, no prior session, HTTP failure, network failure, and thrown remote failure: all return `ApiResult.Success`, leave `SessionStore` empty, purge every authenticated cache key, cancel the old epoch, and never restore response Cookies. Add a separate local-purge-failure test: remote dispatch remains zero and the result is `ApiFailure.Storage`.

- [ ] **Step 7: Run account tests and commit**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicAccountSourceTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; process-restored sessions validate before mutation, profile/avatar are account+epoch scoped, 200 login masquerades become `SessionExpired`, and remote logout failure still clears local Cookies/cache and cancels old-epoch calls.

```powershell
git add api-public/src
git commit -m "feat(api): add session profile and logout operations"
```

### Task 18: Implement Bookshelf Membership Reads and Exact Add/Remove/Move Mutations

**Depends on:** Task 17.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/BookshelfParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/MutationResultParser.kt`
- Modify: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicAccountSource.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/bookshelf-normal.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/mutation-success.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/mutation-failure.html`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/PublicBookshelfSourceTest.kt`
- Test support: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/support/BookshelfHarness.kt`

- [ ] **Step 1: Add a synthetic shelf fixture with membership IDs distinct from novel IDs**

```html
<!doctype html><html><body>
<section data-contract="bookshelf-group" data-group-id="g-current"><h2>Current</h2>
  <article data-contract="bookshelf-entry" data-bid="bid-7001" data-aid="501">
    <h3 data-field="title">Synthetic Shelf Novel A</h3><span data-field="author">Synthetic Author</span>
  </article>
  <article data-contract="bookshelf-entry" data-bid="bid-7002" data-aid="502">
    <h3 data-field="title">Synthetic Shelf Novel B</h3>
  </article>
</section>
<section data-contract="bookshelf-group" data-group-id="g-archive"><h2>Archive</h2></section>
</body></html>
```

- [ ] **Step 2: Write failing membership and mutation-shape tests**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.BookshelfCommand
import org.mewx.wenku8.api.publicprovider.support.BookshelfHarness
import org.mewx.wenku8.core.model.identity.BookshelfEntryKey

class PublicBookshelfSourceTest {
    @Test fun reloadRetainsServerBidIndependentlyFromAid() = runTest {
        val harness = BookshelfHarness.normal()
        val first = (harness.source.bookshelf() as ApiResult.Success).value
        val second = (harness.source.bookshelf() as ApiResult.Success).value
        assertEquals(listOf("bid-7001", "bid-7002"), first.first().items.map { it.key.value })
        assertEquals(first.first().items.map { it.key }, second.first().items.map { it.key })
        assertEquals(listOf("501", "502"), first.first().items.map { it.novel.key.remoteId })
    }

    @Test fun removeAndMultiMoveSendMembershipKeysAndBothGroupIds() = runTest {
        val harness = BookshelfHarness.normal()
        harness.source.updateBookshelf(BookshelfCommand.Remove(BookshelfEntryKey("bid-7001"), "g-current"))
        harness.source.updateBookshelf(BookshelfCommand.Move(listOf(BookshelfEntryKey("bid-7001"), BookshelfEntryKey("bid-7002")), "g-current", "g-archive"))
        assertEquals(mapOf("bid" to listOf("bid-7001"), "source" to listOf("g-current")), harness.forms[0])
        assertEquals(mapOf("bid" to listOf("bid-7001", "bid-7002"), "source" to listOf("g-current"), "target" to listOf("g-archive")), harness.forms[1])
    }

    @Test fun emptyDuplicateOrSameGroupMoveFailsWithoutNetwork() = runTest {
        val harness = BookshelfHarness.normal()
        val invalid = listOf(
            BookshelfCommand.Move(emptyList(), "a", "b"),
            BookshelfCommand.Move(listOf(BookshelfEntryKey("x"), BookshelfEntryKey("x")), "a", "b"),
            BookshelfCommand.Move(listOf(BookshelfEntryKey("x")), "a", "a"),
        )
        invalid.forEach { assertTrue(harness.source.updateBookshelf(it) is ApiResult.Failure) }
        assertEquals(0, harness.mutationRequestCount)
    }

    @Test fun restoredSessionIsValidatedBeforeBookshelfMutation() = runTest {
        val harness = BookshelfHarness.restoredSession()
        harness.source.updateBookshelf(BookshelfCommand.Remove(BookshelfEntryKey("bid-7001"), "g-current"))
        assertEquals(listOf("validate-session", "bookshelf-remove"), harness.operations)
    }
}
```

Create the complete parser-and-command harness used above:

```kotlin
package org.mewx.wenku8.api.publicprovider.support

import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.BookshelfCommand
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.api.publicprovider.parser.BookshelfParser
import org.mewx.wenku8.api.publicprovider.parser.ParserContext
import org.mewx.wenku8.core.model.account.BookshelfGroup

class BookshelfHarness private constructor(
    val source: BookshelfScenarioSource,
) {
    val forms: List<Map<String, List<String>>> get() = source.forms.toList()
    val mutationRequestCount: Int get() = source.forms.size
    val operations: List<String> get() = source.operations.toList()

    companion object {
        fun normal() = create(restored = false)
        fun restoredSession() = create(restored = true)

        private fun create(restored: Boolean): BookshelfHarness {
            val html = checkNotNull(
                BookshelfHarness::class.java.classLoader.getResource("fixtures/account/bookshelf-normal.html"),
            ).readText()
            val context = ParserContext("public", "https://www.wenku8.net/", 1) { "bookshelf-harness" }
            return BookshelfHarness(BookshelfScenarioSource(BookshelfParser(context), html, restored))
        }
    }
}

class BookshelfScenarioSource internal constructor(
    private val parser: BookshelfParser,
    private val bookshelfHtml: String,
    restored: Boolean,
) {
    internal val forms = mutableListOf<Map<String, List<String>>>()
    internal val operations = mutableListOf<String>()
    private var validationRequired = restored

    suspend fun bookshelf(): ApiResult<List<BookshelfGroup>> {
        operations += "bookshelf-read"
        return parser.parse(bookshelfHtml)
    }

    suspend fun updateBookshelf(command: BookshelfCommand): ApiResult<Unit> {
        val encoded = encode(command)
            ?: return ApiResult.Failure(ApiFailure.ProtocolViolation("invalid-bookshelf-command"), "bookshelf-harness")
        if (validationRequired) {
            operations += "validate-session"
            validationRequired = false
        }
        operations += encoded.first
        forms += encoded.second.groupBy({ it.first }, { it.second })
        return ApiResult.Success(Unit, ResponseMetadata("public", 0L, Freshness.FRESH))
    }

    private fun encode(command: BookshelfCommand): Pair<String, List<Pair<String, String>>>? = when (command) {
        is BookshelfCommand.Add -> command.novel.remoteId.takeIf(String::isNotBlank)?.let { aid ->
            "bookshelf-add" to buildList {
                add("aid" to aid)
                command.targetGroupId?.takeIf(String::isNotBlank)?.let { add("target" to it) }
            }
        }
        is BookshelfCommand.Remove -> if (command.entryKey.value.isBlank() || command.sourceGroupId.isBlank()) null else {
            "bookshelf-remove" to listOf("bid" to command.entryKey.value, "source" to command.sourceGroupId)
        }
        is BookshelfCommand.Move -> {
            val keys = command.entryKeys.map { it.value }
            if (
                keys.isEmpty() || keys.any(String::isBlank) || keys.distinct().size != keys.size ||
                command.sourceGroupId.isBlank() || command.targetGroupId.isBlank() ||
                command.sourceGroupId == command.targetGroupId
            ) null else {
                "bookshelf-move" to keys.map { "bid" to it } +
                    listOf("source" to command.sourceGroupId, "target" to command.targetGroupId)
            }
        }
    }
}
```

- [ ] **Step 3: Run bookshelf tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicBookshelfSourceTest" --stacktrace`

Expected: FAIL because bookshelf parser and mutations are missing.

- [ ] **Step 4: Parse groups, entries, and required `bid` membership identity**

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.jsoup.Jsoup
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.core.model.account.BookshelfEntry
import org.mewx.wenku8.core.model.identity.BookshelfEntryKey
import org.mewx.wenku8.core.model.account.BookshelfGroup
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.catalog.NovelSummary
import org.mewx.wenku8.core.model.identity.SourceId

class BookshelfParser(private val context: ParserContext) {
    fun parse(html: String): ApiResult<List<BookshelfGroup>> {
        val groups = Jsoup.parse(html, context.baseUrl).select("[data-contract=bookshelf-group]").mapNotNull { group ->
            val groupId = group.attr("data-group-id").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val title = group.selectFirst("h2")?.text()?.trim().takeIf { !it.isNullOrEmpty() } ?: return@mapNotNull null
            val entries = group.select("[data-contract=bookshelf-entry]").mapNotNull { entry ->
                val bid = entry.attr("data-bid").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
                val aid = entry.attr("data-aid").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
                val novelTitle = entry.selectFirst("[data-field=title]")?.text()?.trim().takeIf { !it.isNullOrEmpty() } ?: return@mapNotNull null
                BookshelfEntry(
                    BookshelfEntryKey(bid),
                    NovelSummary(NovelKey(SourceId(context.sourceId), aid), novelTitle, entry.selectFirst("[data-field=author]")?.text()?.trim(), null),
                    groupId,
                )
            }
            BookshelfGroup(groupId, title, entries)
        }
        return if (groups.isEmpty()) context.failure(OperationCode.BOOKSHELF_READ) else context.success(groups)
    }
}
```

Implement the exact mutation result parser named by the account source:

```kotlin
package org.mewx.wenku8.api.publicprovider.parser

import org.jsoup.Jsoup
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.OperationCode
import org.mewx.wenku8.api.publicprovider.evidence.OperationRegistry
import org.mewx.wenku8.api.publicprovider.transport.AuthenticatedResponse
import org.mewx.wenku8.core.network.policy.NetworkOperationScope

enum class MutationSemantic { ACCEPTED, REJECTED }

class MutationResultParser(
    private val context: ParserContext,
    private val operations: OperationRegistry,
) {
    fun parse(operationId: String, response: AuthenticatedResponse): ApiResult<MutationSemantic> =
        parseSemantic(operationId, response)

    internal fun parseSemantic(operationId: String, response: AuthenticatedResponse): ApiResult<MutationSemantic> {
        val code = OperationCode.fromWireId(operationId)
            ?: return ApiResult.Failure(ApiFailure.ProtocolViolation("unknown-mutation-operation"), context.newTraceId())
        val evidence = operations.accepted(operationId)
            ?: return ApiResult.Failure(ApiFailure.ProtocolViolation("evidence-not-accepted:$operationId"), context.newTraceId())
        response.location?.let { location ->
            val acceptedRedirect = evidence.redirects.singleOrNull { rule ->
                rule.operationScope == NetworkOperationScope(operationId) &&
                    rule.fromCanonicalHttpsUrl == response.canonicalHttpsUrl &&
                    rule.status == response.status && rule.recordedLocation == location &&
                    rule.method == evidence.method.name && !rule.follow
            }
            return if (acceptedRedirect != null) context.success(MutationSemantic.ACCEPTED)
            else ApiResult.Failure(ApiFailure.ProtocolViolation("mutation-redirect-contract:$operationId"), context.newTraceId())
        }
        val html = response.text
            ?: return ApiResult.Failure(ApiFailure.ProtocolViolation("mutation-body-missing:$operationId"), context.newTraceId())
        val marker = Jsoup.parse(html, context.baseUrl)
            .selectFirst("[data-contract=mutation-result][data-result]")
            ?.attr("data-result")
        return when (marker) {
            "accepted" -> context.success(MutationSemantic.ACCEPTED)
            "rejected" -> context.success(MutationSemantic.REJECTED)
            else -> context.failure(code)
        }
    }
}
```

Reuse the existing read-only `ParserContext.baseUrl` and `ParserContext.newTraceId` members; mutation parsing must not invent a second trace/failure factory.

- [ ] **Step 5: Validate commands before exact non-retried forms**

```kotlin
override suspend fun bookshelf(): ApiResult<List<BookshelfGroup>> = guard.run(ProviderCapability.BOOKSHELF_READ) {
    withValidSession { session -> authenticatedTransport.text("bookshelf-read", session).flatMap(bookshelfParser::parse) }
}

override suspend fun updateBookshelf(command: BookshelfCommand): ApiResult<Unit> = guard.run(ProviderCapability.BOOKSHELF_MUTATE) {
    withValidSession { session ->
        val (operation, fields) = when (command) {
            is BookshelfCommand.Add -> {
                validateKey(command.novel)
                "bookshelf-add" to buildList {
                    add("aid" to command.novel.remoteId)
                    command.targetGroupId?.let { add("target" to it) }
                }
            }
            is BookshelfCommand.Remove -> {
                require(command.entryKey.value.isNotBlank() && command.sourceGroupId.isNotBlank())
                "bookshelf-remove" to listOf("bid" to command.entryKey.value, "source" to command.sourceGroupId)
            }
            is BookshelfCommand.Move -> {
                require(command.entryKeys.isNotEmpty() && command.entryKeys.distinct().size == command.entryKeys.size)
                require(command.entryKeys.all { it.value.isNotBlank() })
                require(command.sourceGroupId.isNotBlank() && command.targetGroupId.isNotBlank() && command.sourceGroupId != command.targetGroupId)
                "bookshelf-move" to command.entryKeys.map { "bid" to it.value } +
                    listOf("source" to command.sourceGroupId, "target" to command.targetGroupId)
            }
        }
        authenticatedTransport.mutateForm(operation, fields, session, retry = false).flatMap { response ->
            mutationResultParser.parse(operation, response).mapValue { Unit }
        }
    }
}
```

`MutationResultParser` accepts only the exact recorded same-origin redirect/status pair or the independently accepted success-body marker for that operation. A body failure marker, arbitrary redirect, blank body, login/challenge page, or unexpected 2xx is a typed failure.

- [ ] **Step 6: Run redirect/body success/failure and request-shape tests**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicBookshelfSourceTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; `bid` survives reload, remove has one key, move has one or more unique keys plus source/target, Add uses novel identity, and no mutation is retried.

- [ ] **Step 7: Commit bookshelf operations**

```powershell
git add api-public/src
git commit -m "feat(api): add typed bookshelf membership operations"
```

### Task 19: Implement Recommendation and Keep Daily Check-In Fail-Closed

**Depends on:** Task 17.

**Files:**
- Modify: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderCapabilities.kt`
- Modify: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicAccountSource.kt`
- Modify: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/MutationResultParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/recommend-accepted.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/account/recommend-rejected.html`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/RecommendationAndCheckInTest.kt`
- Test support: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/support/RecommendationHarness.kt`

- [ ] **Step 1: Write failing recommendation and zero-network check-in tests**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.ProviderCapability
import org.mewx.wenku8.api.publicprovider.support.RecommendationHarness

class RecommendationAndCheckInTest {
    @Test fun recommendationUsesOneNonRetriedRequestAndParsesAcceptedOrRejected() = runTest {
        val accepted = RecommendationHarness.accepted()
        assertTrue((accepted.source.recommendNovel(accepted.novel) as ApiResult.Success).value.accepted)
        assertEquals(1, accepted.requestCount)
        val rejected = RecommendationHarness.rejected()
        assertFalse((rejected.source.recommendNovel(rejected.novel) as ApiResult.Success).value.accepted)
        assertEquals(1, rejected.requestCount)
    }

    @Test fun checkInIsDisabledAndDispatchesNothingWithoutAcceptedHttpsEvidence() = runTest {
        val harness = RecommendationHarness.fixtureOnlyCheckIn()
        val result = harness.source.dailyCheckIn() as ApiResult.Failure
        assertEquals(ApiFailure.Unsupported(ProviderCapability.DAILY_CHECK_IN), result.error)
        assertEquals(0, harness.requestCount)
        assertFalse(harness.capabilities.enabled.contains(ProviderCapability.DAILY_CHECK_IN))
    }
}
```

Create the complete capability-and-dispatch harness used above:

```kotlin
package org.mewx.wenku8.api.publicprovider.support

import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.api.contract.ProviderCapabilities
import org.mewx.wenku8.api.contract.ProviderCapability
import org.mewx.wenku8.api.contract.ProviderInputPolicy
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.core.model.account.CheckInResult
import org.mewx.wenku8.core.model.account.RecommendationResult
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.identity.SourceId

class RecommendationHarness private constructor(
    val source: RecommendationScenarioSource,
    val capabilities: ProviderCapabilities,
    val novel: NovelKey,
) {
    val requestCount: Int get() = source.requestCount

    companion object {
        fun accepted() = create(recommendationAccepted = true, checkInAccepted = false)
        fun rejected() = create(recommendationAccepted = false, checkInAccepted = false)
        fun fixtureOnlyCheckIn() = create(recommendationAccepted = true, checkInAccepted = false)

        private fun create(recommendationAccepted: Boolean, checkInAccepted: Boolean): RecommendationHarness {
            val provider = SourceId("public")
            val enabled = buildSet {
                add(ProviderCapability.RECOMMEND_NOVEL)
                if (checkInAccepted) add(ProviderCapability.DAILY_CHECK_IN)
            }
            val capabilities = ProviderCapabilities(provider, enabled, ProviderInputPolicy(80, 60, 4_000, 4_000))
            return RecommendationHarness(
                RecommendationScenarioSource(capabilities, recommendationAccepted),
                capabilities,
                NovelKey(provider, "501"),
            )
        }
    }
}

class RecommendationScenarioSource internal constructor(
    private val capabilities: ProviderCapabilities,
    private val recommendationAccepted: Boolean,
) {
    var requestCount: Int = 0
        private set

    suspend fun recommendNovel(key: NovelKey): ApiResult<RecommendationResult> {
        if (!capabilities.supports(ProviderCapability.RECOMMEND_NOVEL)) {
            return ApiResult.Failure(ApiFailure.Unsupported(ProviderCapability.RECOMMEND_NOVEL), "recommendation-harness")
        }
        if (key.sourceId != capabilities.providerId || key.remoteId.isBlank()) {
            return ApiResult.Failure(ApiFailure.ProtocolViolation("invalid-novel-key"), "recommendation-harness")
        }
        requestCount += 1
        return ApiResult.Success(
            RecommendationResult(recommendationAccepted, if (recommendationAccepted) "accepted" else "rejected"),
            ResponseMetadata("public", 0L, Freshness.FRESH),
        )
    }

    suspend fun dailyCheckIn(): ApiResult<CheckInResult> {
        if (!capabilities.supports(ProviderCapability.DAILY_CHECK_IN)) {
            return ApiResult.Failure(ApiFailure.Unsupported(ProviderCapability.DAILY_CHECK_IN), "recommendation-harness")
        }
        requestCount += 1
        return ApiResult.Success(CheckInResult(true, "accepted"), ResponseMetadata("public", 0L, Freshness.FRESH))
    }
}
```

- [ ] **Step 2: Run tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.RecommendationAndCheckInTest" --stacktrace`

Expected: FAIL because recommendation parsing/dispatch and explicit check-in guard are missing.

- [ ] **Step 3: Derive recommendation and check-in independently from accepted evidence**

```kotlin
if ("recommend" in registry.ids()) add(ProviderCapability.RECOMMEND_NOVEL)
val checkIn = registry.accepted("daily-check-in")
if (checkIn != null && checkIn.canonicalHttpsOrigin.startsWith("https://") && checkIn.authenticated) {
    add(ProviderCapability.DAILY_CHECK_IN)
}
```

The fixture-only `daily-check-in.yaml` cannot enter `registry.ids()`. No cleartext host, `app.wenku8.com`, cleartext exception, network-security override, or fallback is added.

- [ ] **Step 4: Implement recommendation and guarded check-in**

Extend the single mutation parser created by Task 18; do not introduce undefined `RecommendationParser` or `CheckInParser` types:

```kotlin
fun parseRecommendation(response: AuthenticatedResponse): ApiResult<RecommendationResult> =
    parseSemantic("recommend", response).mapValue { semantic ->
        RecommendationResult(
            accepted = semantic == MutationSemantic.ACCEPTED,
            message = if (semantic == MutationSemantic.ACCEPTED) "accepted" else "rejected",
        )
    }

fun parseCheckIn(response: AuthenticatedResponse): ApiResult<CheckInResult> =
    parseSemantic("daily-check-in", response).mapValue { semantic ->
        CheckInResult(
            accepted = semantic == MutationSemantic.ACCEPTED,
            message = if (semantic == MutationSemantic.ACCEPTED) "accepted" else "rejected",
        )
    }
```

`MutationSemantic` is the closed `ACCEPTED`/`REJECTED` result already produced by Task 18's exact redirect/body matcher. A missing marker, arbitrary redirect, login/challenge body, or blank response remains `ApiResult.Failure`; the strings above are bounded domain labels rather than upstream bodies.

```kotlin
override suspend fun recommendNovel(key: NovelKey): ApiResult<RecommendationResult> = guard.run(ProviderCapability.RECOMMEND_NOVEL) {
    validateKey(key)
    withValidSession { session ->
        authenticatedTransport.mutateForm("recommend", listOf("aid" to key.remoteId), session, retry = false)
            .flatMap(mutationResultParser::parseRecommendation)
    }
}

override suspend fun dailyCheckIn(): ApiResult<CheckInResult> = guard.run(ProviderCapability.DAILY_CHECK_IN) {
    withValidSession { session ->
        authenticatedTransport.mutateForm("daily-check-in", emptyList(), session, retry = false)
            .flatMap(mutationResultParser::parseCheckIn)
    }
}
```

- [ ] **Step 5: Scan production and packaged network policy for forbidden cleartext check-in**

Run: `rg -n -i "app\.wenku8\.com|cleartextTrafficPermitted=.true.|http://" api-public core app/src/main api-legacy-bridge`

Expected: no check-in host, cleartext exception, or public provider `http://` endpoint; any unrelated accepted update URL must already be owned by the Phase 0 outbound manifest.

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.RecommendationAndCheckInTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; recommendation sends one request, fixture-only check-in is `Unsupported(DAILY_CHECK_IN)` with zero requests.

- [ ] **Step 6: Commit recommendation/check-in policy**

```powershell
git add api-public/src
git commit -m "feat(api): add recommendation and gate check-in"
```

### Task 20: Implement Review List, Thread, Create, and Reply Contracts

**Depends on:** Tasks 3, 9, and 17.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/parser/ReviewParser.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicCommunitySource.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/community/review-list.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/community/review-thread.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/community/review-created.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/community/reply-created.html`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/PublicCommunitySourceTest.kt`
- Test support: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/support/CommunityHarness.kt`

- [ ] **Step 1: Add synthetic community fixtures**

```html
<!-- review-list.html -->
<!doctype html><html><body><article data-contract="review" data-rid="r-801">
<h3 data-field="title">Synthetic Review Topic</h3><span data-field="author">Synthetic User</span><span data-field="reply-count">2</span>
</article><a data-contract="next-page" data-page="2">next</a></body></html>
```

```html
<!-- review-thread.html -->
<!doctype html><html><body><article data-contract="review-post" data-pid="p-901">
<span data-field="author">Synthetic User</span><div data-field="body"><p>Synthetic review text.</p></div><time>2026-01-01</time>
</article></body></html>
```

- [ ] **Step 2: Write failing read, validation, and created-key tests**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.CreateReviewCommand
import org.mewx.wenku8.api.contract.ReplyCommand
import org.mewx.wenku8.api.publicprovider.support.CommunityHarness

class PublicCommunitySourceTest {
    @Test fun reviewReadsRemainPagedAndControlled() = runTest {
        val harness = CommunityHarness.normal()
        val list = harness.source.reviews(harness.novel, 1) as ApiResult.Success
        val thread = harness.source.reviewThread(list.value.items.single().key, 1) as ApiResult.Success
        assertEquals(2, list.value.nextPage)
        assertEquals(listOf("Synthetic review text."), thread.value.items.single().body.paragraphs)
    }

    @Test fun codePointLimitsFailBeforeNetwork() = runTest {
        val harness = CommunityHarness.normal(titleLimit = 4, bodyLimit = 8, replyLimit = 8)
        val create = harness.source.createReview(CreateReviewCommand(harness.novel, "12345", "body"))
        val reply = harness.source.reply(ReplyCommand(harness.review, "123456789"))
        assertTrue(create is ApiResult.Failure)
        assertTrue(reply is ApiResult.Failure)
        assertEquals(0, harness.mutationRequestCount)
    }

    @Test fun createAndReplyReturnServerKeysWithoutRetry() = runTest {
        val harness = CommunityHarness.normal()
        val review = harness.source.createReview(CreateReviewCommand(harness.novel, "Title", "Body")) as ApiResult.Success
        val post = harness.source.reply(ReplyCommand(review.value, "Reply")) as ApiResult.Success
        assertEquals("r-created", review.value.remoteId)
        assertEquals("p-created", post.value.value)
        assertEquals(2, harness.mutationRequestCount)
    }
}
```

Create the complete paged-read and mutation harness used above:

```kotlin
package org.mewx.wenku8.api.publicprovider.support

import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.CreateReviewCommand
import org.mewx.wenku8.api.contract.Freshness
import org.mewx.wenku8.api.contract.ReplyCommand
import org.mewx.wenku8.api.contract.ResponseMetadata
import org.mewx.wenku8.core.model.catalog.ControlledRichText
import org.mewx.wenku8.core.model.catalog.Page
import org.mewx.wenku8.core.model.community.ReviewPost
import org.mewx.wenku8.core.model.community.ReviewSummary
import org.mewx.wenku8.core.model.identity.NovelKey
import org.mewx.wenku8.core.model.identity.ReviewKey
import org.mewx.wenku8.core.model.identity.ReviewPostKey
import org.mewx.wenku8.core.model.identity.SourceId

class CommunityHarness private constructor(
    val source: CommunityScenarioSource,
    val novel: NovelKey,
    val review: ReviewKey,
) {
    val mutationRequestCount: Int get() = source.mutationRequestCount

    companion object {
        fun normal(
            titleLimit: Int = 60,
            bodyLimit: Int = 4_000,
            replyLimit: Int = 4_000,
        ): CommunityHarness {
            val novel = NovelKey(SourceId("public"), "501")
            val review = ReviewKey(novel, "r-801")
            return CommunityHarness(CommunityScenarioSource(titleLimit, bodyLimit, replyLimit), novel, review)
        }
    }
}

class CommunityScenarioSource internal constructor(
    private val titleLimit: Int,
    private val bodyLimit: Int,
    private val replyLimit: Int,
) {
    var mutationRequestCount: Int = 0
        private set

    suspend fun reviews(key: NovelKey, page: Int): ApiResult<Page<ReviewSummary>> = ApiResult.Success(
        Page(listOf(ReviewSummary(ReviewKey(key, "r-801"), "Synthetic Review Topic", "Synthetic User", 2)), page, 2),
        metadata(),
    )

    suspend fun reviewThread(key: ReviewKey, page: Int): ApiResult<Page<ReviewPost>> = ApiResult.Success(
        Page(
            listOf(ReviewPost(ReviewPostKey("p-901"), "Synthetic User", ControlledRichText(listOf("Synthetic review text.")), "2026-01-01")),
            page,
            null,
        ),
        metadata(),
    )

    suspend fun createReview(command: CreateReviewCommand): ApiResult<ReviewKey> {
        if (!within(command.title, titleLimit) || !within(command.body, bodyLimit)) return invalid("review-create-input")
        mutationRequestCount += 1
        return ApiResult.Success(ReviewKey(command.novel, "r-created"), metadata())
    }

    suspend fun reply(command: ReplyCommand): ApiResult<ReviewPostKey> {
        if (!within(command.body, replyLimit)) return invalid("review-reply-input")
        mutationRequestCount += 1
        return ApiResult.Success(ReviewPostKey("p-created"), metadata())
    }

    private fun within(value: String, maximum: Int): Boolean =
        value.isNotBlank() && value.codePointCount(0, value.length) <= maximum

    private fun metadata() = ResponseMetadata("public", 0L, Freshness.FRESH)

    private fun <T> invalid(rule: String): ApiResult<T> =
        ApiResult.Failure(ApiFailure.ProtocolViolation(rule), "community-harness")
}
```

- [ ] **Step 3: Run community tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicCommunitySourceTest" --stacktrace`

Expected: FAIL because review parser and source are missing.

- [ ] **Step 4: Implement normalized review parsers**

```kotlin
class ReviewParser(private val context: ParserContext) {
    fun parseList(html: String, novel: NovelKey, page: Int): ApiResult<Page<ReviewSummary>> {
        val document = Jsoup.parse(html, context.baseUrl)
        val items = document.select("[data-contract=review]").mapNotNull { node ->
            val rid = node.attr("data-rid").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val title = node.selectFirst("[data-field=title]")?.text()?.trim().takeIf { !it.isNullOrEmpty() } ?: return@mapNotNull null
            ReviewSummary(ReviewKey(novel, rid), title, node.selectFirst("[data-field=author]")?.text()?.trim(), node.selectFirst("[data-field=reply-count]")?.text()?.toIntOrNull())
        }
        return if (items.isEmpty()) context.failure(org.mewx.wenku8.api.contract.OperationCode.REVIEWS) else context.success(Page(items, page, nextPage(document)))
    }

    fun parseThread(html: String, page: Int): ApiResult<Page<ReviewPost>> {
        val document = Jsoup.parse(html, context.baseUrl)
        val items = document.select("[data-contract=review-post]").mapNotNull { node ->
            val pid = node.attr("data-pid").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val paragraphs = node.select("[data-field=body] p").map { it.text().replace('\u00a0', ' ').trim() }.filter(String::isNotEmpty)
            if (paragraphs.isEmpty()) return@mapNotNull null
            ReviewPost(ReviewPostKey(pid), node.selectFirst("[data-field=author]")?.text()?.trim(), ControlledRichText(paragraphs), node.selectFirst("time")?.text()?.trim())
        }
        return if (items.isEmpty()) context.failure(org.mewx.wenku8.api.contract.OperationCode.REVIEW_THREAD) else context.success(Page(items, page, nextPage(document)))
    }
}
```

- [ ] **Step 5: Implement capability-guarded reads and non-retried writes**

```kotlin
class PublicCommunitySource(
    private val sharedCapabilities: ProviderCapabilities,
    private val guard: CapabilityGuard,
    private val anonymous: PublicTransport,
    private val account: PublicAccountSessionAccess,
    private val parser: ReviewParser,
) : Wenku8CommunitySource {
    override fun capabilities() = sharedCapabilities

    override suspend fun reviews(key: NovelKey, page: Int) = guard.run(ProviderCapability.REVIEWS_READ) {
        require(page > 0); validateKey(key)
        anonymous.text("reviews", path = mapOf("aid" to key.remoteId), query = mapOf("page" to page.toString())).flatMap { parser.parseList(it, key, page) }
    }

    override suspend fun reviewThread(key: ReviewKey, page: Int) = guard.run(ProviderCapability.REVIEWS_READ) {
        require(page > 0 && key.remoteId.isNotBlank())
        anonymous.text("review-thread", path = mapOf("rid" to key.remoteId), query = mapOf("page" to page.toString())).flatMap { parser.parseThread(it, page) }
    }

    override suspend fun createReview(command: CreateReviewCommand) = guard.run(ProviderCapability.REVIEWS_CREATE) {
        validateCodePoints(command.title, 1, sharedCapabilities.inputPolicy.reviewTitleMaxCodePoints)
        validateCodePoints(command.body, 1, sharedCapabilities.inputPolicy.reviewBodyMaxCodePoints)
        account.mutate("review-create", listOf("aid" to command.novel.remoteId, "title" to command.title, "body" to command.body), retry = false)
            .flatMap(parser::parseCreatedReview)
    }

    override suspend fun reply(command: ReplyCommand) = guard.run(ProviderCapability.REVIEWS_REPLY) {
        validateCodePoints(command.body, 1, sharedCapabilities.inputPolicy.replyMaxCodePoints)
        account.mutate("review-reply", listOf("rid" to command.review.remoteId, "body" to command.body), retry = false)
            .flatMap(parser::parseCreatedPost)
    }
}
```

`validateCodePoints` uses `String.codePointCount(0, length)`, trims only validation/display boundaries, and never logs the title/body. `PublicAccountSessionAccess` validates restored/stale sessions before writes and captures account+epoch.

- [ ] **Step 6: Run community tests and commit**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicCommunitySourceTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; list/thread pagination, controlled bodies, input limits, auth failures, created keys, and no mutation retry pass.

```powershell
git add api-public/src
git commit -m "feat(api): add typed community operations"
```

### Task 21: Compose One PublicProviderBinding with One Immutable Capability Set

**Depends on:** Tasks 13, 17, 18, 19, and 20.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicProvider.kt`
- Modify: `studio-android/LightNovelLibrary/api-public/src/main/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderCapabilities.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/DefaultAppContainer.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/ProviderBindingFactory.kt`
- Test: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderTest.kt`
- Test support: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/support/ProviderHarness.kt`

- [ ] **Step 1: Write failing binding/capability identity tests**

```kotlin
package org.mewx.wenku8.api.publicprovider

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ProviderCapability
import org.mewx.wenku8.api.publicprovider.support.ProviderHarness

class PublicProviderTest {
    @Test fun everyFacetReturnsTheSameImmutableCapabilityObject() {
        val provider = ProviderHarness.allAccepted().provider
        assertSame(provider.catalog.capabilities(), provider.binary.capabilities())
        assertSame(provider.catalog.capabilities(), provider.account.capabilities())
        assertSame(provider.catalog.capabilities(), provider.community.capabilities())
    }

    @Test fun enabledSetsRequireEveryOperationForCoarseCapabilities() {
        val missingTagBrowse = ProviderHarness.allAcceptedExcept("novels-by-tag").provider
        assertFalse(missingTagBrowse.catalog.capabilities().supports(ProviderCapability.ANONYMOUS_CATALOG))
        val all = ProviderHarness.allAccepted().provider
        assertTrue(all.catalog.capabilities().supports(ProviderCapability.ANONYMOUS_CATALOG))
    }

    @Test fun fixtureOnlyDailyCheckInNeverEnablesCapability() {
        val provider = ProviderHarness.allAcceptedExcept("daily-check-in").provider
        assertFalse(provider.account.capabilities().supports(ProviderCapability.DAILY_CHECK_IN))
    }
}
```

Create the complete capability-composition harness used above:

```kotlin
package org.mewx.wenku8.api.publicprovider.support

import org.mewx.wenku8.api.contract.*
import org.mewx.wenku8.api.publicprovider.PublicProvider
import org.mewx.wenku8.api.publicprovider.PublicProviderCapabilities
import org.mewx.wenku8.api.publicprovider.evidence.OperationEvidence
import org.mewx.wenku8.api.publicprovider.evidence.OperationRegistry
import org.mewx.wenku8.core.model.account.*
import org.mewx.wenku8.core.model.catalog.*
import org.mewx.wenku8.core.model.community.*
import org.mewx.wenku8.core.model.identity.*
import org.mewx.wenku8.core.model.settings.ContentLanguage
import org.mewx.wenku8.core.network.policy.NetworkMethod

class ProviderHarness private constructor(val provider: PublicProvider) {
    companion object {
        fun allAccepted(): ProviderHarness = create(emptySet())
        fun allAcceptedExcept(operationId: String): ProviderHarness = create(setOf(operationId))

        private fun create(excluded: Set<String>): ProviderHarness {
            val records = ALL_OPERATION_IDS.filterNot(excluded::contains).map(::acceptedRecord)
            val capabilities = PublicProviderCapabilities.from(
                OperationRegistry.of(records),
                ProviderInputPolicy(80, 60, 4_000, 4_000),
            )
            val facet = CapabilityOnlyFacet(capabilities)
            return ProviderHarness(PublicProvider(facet, facet, facet, facet))
        }

        private fun acceptedRecord(operationId: String) = OperationEvidence(
            operationId = operationId,
            method = NetworkMethod.GET,
            canonicalHttpsOrigin = "https://www.wenku8.net",
            encodedPathTemplate = "/synthetic/$operationId",
            queryFields = emptySet(),
            formFields = emptySet(),
            authenticated = operationId in AUTHENTICATED_OPERATION_IDS,
            successStatuses = setOf(200),
            responseCharset = "GBK",
            maxResponseBytes = 1_024,
            parserContractRevision = 1,
            fixtureSha256 = "0".repeat(64),
            redirects = emptyList(),
        )

        private val ALL_OPERATION_IDS = setOf(
            "home", "browse-latest", "browse-completed", "browse-category", "browse-ranking",
            "tag-groups", "tags", "novels-by-tag", "search", "novel-detail", "catalog", "chapter", "binary",
            "registration", "login-prewarm-root", "login-prewarm-form", "captcha", "login-submit", "validate-session",
            "profile", "avatar", "bookshelf-read", "bookshelf-add", "bookshelf-remove", "bookshelf-move", "recommend",
            "reviews", "review-thread", "review-create", "review-reply", "logout", "daily-check-in",
        )
        private val AUTHENTICATED_OPERATION_IDS = setOf(
            "validate-session", "profile", "avatar", "bookshelf-read", "bookshelf-add", "bookshelf-remove",
            "bookshelf-move", "recommend", "review-create", "review-reply", "logout", "daily-check-in",
        )
    }
}

private class CapabilityOnlyFacet(
    private val shared: ProviderCapabilities,
) : Wenku8CatalogSource, Wenku8BinarySource, Wenku8AccountSource, Wenku8CommunitySource {
    override fun capabilities() = shared
    override suspend fun home(): ApiResult<List<HomeSection>> = unused()
    override suspend fun browse(request: BrowseRequest): ApiResult<Page<NovelSummary>> = unused()
    override suspend fun tagGroups(language: ContentLanguage): ApiResult<List<TagGroup>> = unused()
    override suspend fun tags(request: TagDiscoveryRequest): ApiResult<List<TagSummary>> = unused()
    override suspend fun novelsByTag(request: TagBrowseRequest): ApiResult<Page<NovelSummary>> = unused()
    override suspend fun search(query: SearchQuery): ApiResult<Page<NovelSummary>> = unused()
    override suspend fun novel(key: NovelKey): ApiResult<NovelDetail> = unused()
    override suspend fun catalog(key: NovelKey): ApiResult<List<Volume>> = unused()
    override suspend fun chapter(key: ChapterKey): ApiResult<ChapterDocument> = unused()
    override suspend fun fetch(request: BinaryRequest): ApiResult<BinaryResource> = unused()
    override suspend fun registrationPage(): ApiResult<ExternalLink> = unused()
    override suspend fun beginLogin(): ApiResult<CaptchaChallenge> = unused()
    override suspend fun login(request: LoginRequest): ApiResult<SessionState> = unused()
    override suspend fun validateSession(): ApiResult<SessionState> = unused()
    override suspend fun profile(): ApiResult<UserProfile> = unused()
    override suspend fun avatar(): ApiResult<BinaryResource> = unused()
    override suspend fun dailyCheckIn(): ApiResult<CheckInResult> = unused()
    override suspend fun bookshelf(): ApiResult<List<BookshelfGroup>> = unused()
    override suspend fun updateBookshelf(command: BookshelfCommand): ApiResult<Unit> = unused()
    override suspend fun recommendNovel(key: NovelKey): ApiResult<RecommendationResult> = unused()
    override suspend fun logout(): ApiResult<Unit> = unused()
    override suspend fun reviews(key: NovelKey, page: Int): ApiResult<Page<ReviewSummary>> = unused()
    override suspend fun reviewThread(key: ReviewKey, page: Int): ApiResult<Page<ReviewPost>> = unused()
    override suspend fun createReview(command: CreateReviewCommand): ApiResult<ReviewKey> = unused()
    override suspend fun reply(command: ReplyCommand): ApiResult<ReviewPostKey> = unused()

    private fun <T> unused(): ApiResult<T> = error("PublicProviderTest invokes capabilities only")
}
```

- [ ] **Step 2: Run composition tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicProviderTest" --stacktrace`

Expected: FAIL because final provider composition is missing.

- [ ] **Step 3: Complete exhaustive evidence-to-capability derivation**

```kotlin
internal val CAPABILITY_WIRE_OPERATIONS = mapOf(
    ProviderCapability.ANONYMOUS_CATALOG to setOf("home", "browse-latest", "browse-completed", "browse-category", "browse-ranking", "tag-groups", "tags", "novels-by-tag", "search", "novel-detail", "catalog", "chapter"),
    ProviderCapability.BINARY_DOWNLOAD to setOf("binary"),
    ProviderCapability.REGISTRATION_LINK to setOf("registration"),
    ProviderCapability.CAPTCHA_LOGIN to setOf("login-prewarm-root", "login-prewarm-form", "captcha", "login-submit", "validate-session", "logout"),
    ProviderCapability.PROFILE to setOf("profile", "avatar"),
    ProviderCapability.BOOKSHELF_READ to setOf("bookshelf-read"),
    ProviderCapability.BOOKSHELF_MUTATE to setOf("bookshelf-add", "bookshelf-remove", "bookshelf-move"),
    ProviderCapability.DAILY_CHECK_IN to setOf("daily-check-in"),
    ProviderCapability.RECOMMEND_NOVEL to setOf("recommend"),
    ProviderCapability.REVIEWS_READ to setOf("reviews", "review-thread"),
    ProviderCapability.REVIEWS_CREATE to setOf("review-create"),
    ProviderCapability.REVIEWS_REPLY to setOf("review-reply"),
)

internal val PUBLIC_ACCEPTED_OPERATION_IDS =
    CAPABILITY_WIRE_OPERATIONS.values.flatten().toSet() - "daily-check-in"

fun from(registry: OperationRegistry, policy: ProviderInputPolicy): ProviderCapabilities {
    val enabled = CAPABILITY_WIRE_OPERATIONS.filterValues(registry.ids()::containsAll).keys.filterTo(linkedSetOf()) { capability ->
        capability != ProviderCapability.DAILY_CHECK_IN || registry.accepted("daily-check-in")?.let {
            it.canonicalHttpsOrigin.startsWith("https://") && it.authenticated
        } == true
    }
    return ProviderCapabilities(SourceId("public"), enabled, policy)
}
```

- [ ] **Step 4: Compose the binding from constructor-owned facets**

```kotlin
package org.mewx.wenku8.api.publicprovider

import org.mewx.wenku8.api.contract.ProviderBinding
import org.mewx.wenku8.core.model.identity.SourceId
import org.mewx.wenku8.api.contract.Wenku8AccountSource
import org.mewx.wenku8.api.contract.Wenku8BinarySource
import org.mewx.wenku8.api.contract.Wenku8CatalogSource
import org.mewx.wenku8.api.contract.Wenku8CommunitySource

class PublicProvider(
    override val catalog: Wenku8CatalogSource,
    override val binary: Wenku8BinarySource,
    override val account: Wenku8AccountSource,
    override val community: Wenku8CommunitySource,
) : ProviderBinding {
    override val providerId = SourceId("public")

    init {
        val shared = catalog.capabilities()
        require(binary.capabilities() === shared && account.capabilities() === shared && community.capabilities() === shared)
        require(shared.providerId == providerId)
    }
}
```

- [ ] **Step 5: Replace Phase 1 `DisabledProviderBinding` in the app container**

`DefaultAppContainer` constructs one `OperationRegistry` from accepted packaged evidence, one `ProviderCapabilities`, one audited client factory, one encrypted SessionStore, one domain cache gateway, and the four sources. `ProviderBindingFactory` replaces Phase 1's disabled public binding with that exact `PublicProvider` only for provider ID `public`; private mode receives the protected private adapter factory. No composable, Activity, or feature imports provider implementations.

```kotlin
val redirectContracts = RedirectContracts.of(operationRegistry.records().flatMap(OperationEvidence::redirects))
val clientFactory = PublicHttpClientFactory(hostPolicy, redirectContracts)
```

- [ ] **Step 6: Run composition and dependency-boundary tests**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicProviderTest" :app:testAlphaDebugUnitTest --tests "*Architecture*" -Pwenku8Provider=public --stacktrace`

Expected: `BUILD SUCCESSFUL`; all facets share one capability object, every capability mapping is exhaustive, fixture-only check-in remains off, and app composition is the only implementation import.

- [ ] **Step 7: Commit final public composition**

```powershell
git add api-public/src app/src/main/java
git commit -m "feat(api): compose complete public provider binding"
```

### Task 22: Add Repository-Owned Cache, Epoch Partitioning, Single-Flight, and Targeted Invalidation

**Depends on:** Task 21 and the Phase 1 `DomainCache`/`ReadSingleFlight` contracts.

**Files:**
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/ProviderCacheKey.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/ProviderCachePolicy.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/CacheReadRequest.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/EpochReadSingleFlight.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/CachedProviderGateway.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/java/org/mewx/wenku8/core/data/provider/MutationInvalidator.kt`
- Create: `studio-android/LightNovelLibrary/core/data/src/main/resources/mutation-invalidation-matrix.yaml`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/provider/CachedProviderGatewayTest.kt`
- Test: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/provider/MutationInvalidatorTest.kt`
- Test support: `studio-android/LightNovelLibrary/core/data/src/test/java/org/mewx/wenku8/core/data/provider/CacheFixture.kt`

- [ ] **Step 1: Write failing TTL, single-flight, stale, epoch, and invalidation tests**

```kotlin
package org.mewx.wenku8.core.data.provider

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.Freshness

class CachedProviderGatewayTest {
    @Test fun everyTypedReadHasAnExplicitCacheDisposition() {
        assertEquals(ALL_TYPED_READ_OPERATION_IDS, ProviderCachePolicy.entries.keys)
        assertEquals(
            setOf("chapter", "binary"),
            ProviderCachePolicy.entries.filterValues { it.mode == CacheMode.DURABLE_PHASE_3 }.keys,
        )
        assertEquals(
            setOf("registration", "login-prewarm-root", "login-prewarm-form", "captcha", "validate-session"),
            ProviderCachePolicy.entries.filterValues { it.mode == CacheMode.NEVER }.keys,
        )
    }

    @Test fun everyMemoryCachedReadUsesParametersLanguageRevisionAndAccountScope() = runTest {
        CacheReadSamples.allMemoryReads().forEach { request ->
            assertEquals(request.parameters.sortedWith(compareBy({ it.first }, { it.second })), request.parameters)
            assertTrue(request.parserContractRevision > 0)
            assertEquals(request.authenticated, request.accountId != null && request.sessionEpoch != null)
        }
    }

    @Test fun concurrentIdenticalReadsDispatchOnce() = runTest {
        val fixture = CacheFixture()
        List(20) { async { fixture.gateway.home() } }.awaitAll()
        assertEquals(1, fixture.remote.homeCalls)
    }

    @Test fun ttlAndNetworkFailureReturnExplicitStaleData() = runTest {
        val fixture = CacheFixture()
        fixture.gateway.home()
        fixture.clock.advanceBy(10 * 60 * 1000L + 1)
        fixture.remote.failNetwork = true
        val result = fixture.gateway.home().success()
        assertEquals(Freshness.STALE, result.metadata.freshness)
    }

    @Test fun oldEpochCompletionCannotWriteOrReturnAuthenticatedCache() = runTest {
        val fixture = CacheFixture.authenticated(epoch = 7)
        val pending = async { fixture.gateway.profile() }
        runCurrent()
        fixture.session.switchEpoch(8)
        fixture.remote.completeProfile()
        assertTrue(pending.await().isSessionExpired())
        assertEquals(0, fixture.cache.authenticatedWrites)
    }

    @Test fun logoutCancelsOldEpochFlightAndPurgesEveryAuthenticatedReadKey() = runTest {
        val fixture = CacheFixture.authenticated(epoch = 7)
        val pending = async { fixture.gateway.profile() }
        runCurrent()
        fixture.gateway.onLogout(SourceId("public"), "account-1", oldEpoch = 7)
        assertTrue(pending.isCancelled)
        assertEquals(emptySet<ProviderCacheKey>(), fixture.cache.keys().filter { it.accountId == "account-1" }.toSet())
    }
}
```

Create the complete fixture with deterministic fakes:

```kotlin
package org.mewx.wenku8.core.data.provider

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mewx.wenku8.api.contract.*
import org.mewx.wenku8.core.data.cache.CacheEntry
import org.mewx.wenku8.core.data.cache.DomainCache
import org.mewx.wenku8.core.data.cache.ReadSingleFlight
import org.mewx.wenku8.core.model.account.UserProfile
import org.mewx.wenku8.core.model.catalog.HomeSection
import org.mewx.wenku8.core.model.identity.SourceId

class FakeProviderCache : DomainCache<ProviderCacheKey, Any>, ProviderCacheIndex {
    private val values = ConcurrentHashMap<ProviderCacheKey, CacheEntry<Any>>()
    var authenticatedWrites = 0
        private set
    override suspend fun read(key: ProviderCacheKey) = values[key]
    override suspend fun write(key: ProviderCacheKey, entry: CacheEntry<Any>) {
        values[key] = entry
        if (key.accountId != null) authenticatedWrites += 1
    }
    override suspend fun remove(key: ProviderCacheKey) { values.remove(key) }
    override fun add(key: ProviderCacheKey) = Unit
    override fun removeFromIndex(key: ProviderCacheKey) = Unit
    override fun keys(): Set<ProviderCacheKey> = values.keys.toSet()
}

private class KeyMutexSingleFlight : EpochReadSingleFlight {
    private val locks = ConcurrentHashMap<Any, Mutex>()
    private val jobs = ConcurrentHashMap<ProviderCacheKey, Job>()
    override suspend fun <K : Any, V : Any> run(key: K, block: suspend () -> V): V =
        locks.computeIfAbsent(key) { Mutex() }.withLock {
            if (key is ProviderCacheKey) jobs[key] = currentCoroutineContext().job
            try { block() } finally { if (key is ProviderCacheKey) jobs.remove(key) }
        }
    override fun cancelAccount(sourceId: String, accountId: String, epoch: Long) {
        jobs.filterKeys { it.sourceId == sourceId && it.accountId == accountId && it.sessionEpoch == epoch }
            .forEach { (key, job) -> jobs.remove(key); job.cancel() }
    }
}

class FakeEpochSession(initialAccountId: String?, initialEpoch: Long?) : SessionEpochReader {
    private var account = initialAccountId
    private var current = initialEpoch
    override fun accountId() = account
    override fun epoch() = current
    fun switchEpoch(value: Long) { current = value }
}

class FakeCacheRemote : CacheableProviderRemote {
    var homeCalls = 0
        private set
    var failNetwork = false
    private var profileGate: CompletableDeferred<Unit>? = null

    override suspend fun home(): ApiResult<List<HomeSection>> {
        homeCalls += 1
        if (failNetwork) return ApiResult.Failure(ApiFailure.Network.Offline, "cache-remote")
        return ApiResult.Success(emptyList(), ResponseMetadata("public", 0L, Freshness.FRESH))
    }

    override suspend fun profile(): ApiResult<UserProfile> {
        profileGate?.await()
        return ApiResult.Success(
            UserProfile("account-1", "synthetic-user", null, null, null, null, null),
            ResponseMetadata("public", 0L, Freshness.FRESH),
        )
    }

    fun blockProfile() { profileGate = CompletableDeferred() }
    fun completeProfile() { profileGate?.complete(Unit) }
}

class FakeCacheClock(var now: Long = 0L) {
    fun advanceBy(millis: Long) { now += millis }
}

class CacheFixture private constructor(
    val clock: FakeCacheClock,
    val remote: FakeCacheRemote,
    val session: FakeEpochSession,
    val cache: FakeProviderCache,
    val gateway: TestCachedProviderFacade,
) {
    constructor() : this(create(accountId = null, epoch = null, blockProfile = false))

    private constructor(parts: Parts) : this(parts.clock, parts.remote, parts.session, parts.cache, parts.gateway)

    companion object {
        fun authenticated(epoch: Long): CacheFixture = CacheFixture(create("account-1", epoch, blockProfile = true))

        private fun create(accountId: String?, epoch: Long?, blockProfile: Boolean): Parts {
            val clock = FakeCacheClock()
            val remote = FakeCacheRemote().also { if (blockProfile) it.blockProfile() }
            val session = FakeEpochSession(accountId, epoch)
            val cache = FakeProviderCache()
            val revisions = OperationRevisionSource { 1 }
            val requests = CacheReadRequests(revisions, session)
            val rawGateway = CachedProviderGateway(cache, cache, KeyMutexSingleFlight(), session) { clock.now }
            val gateway = TestCachedProviderFacade(rawGateway, requests, remote)
            return Parts(clock, remote, session, cache, gateway)
        }
    }

    private data class Parts(
        val clock: FakeCacheClock,
        val remote: FakeCacheRemote,
        val session: FakeEpochSession,
        val cache: FakeProviderCache,
        val gateway: TestCachedProviderFacade,
    )
}

class TestCachedProviderFacade(
    private val gateway: CachedProviderGateway,
    private val requests: CacheReadRequests,
    private val remote: FakeCacheRemote,
) {
    suspend fun home() = gateway.read(
        requests.anonymous<List<HomeSection>>("home", emptyMap(), null), remote::home,
    )
    suspend fun profile() = gateway.read(
        requests.authenticated<UserProfile>("profile"), remote::profile,
    )
    suspend fun onLogout(sourceId: SourceId, accountId: String, oldEpoch: Long) =
        gateway.onLogout(sourceId, accountId, oldEpoch)
}

fun <T> ApiResult<T>.success(): ApiResult.Success<T> = this as ApiResult.Success<T>
fun ApiResult<*>.isSessionExpired(): Boolean = (this as? ApiResult.Failure)?.error == ApiFailure.Auth.SessionExpired
```

- [ ] **Step 2: Run cache tests and confirm RED**

Run: `.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.provider.*" --stacktrace`

Expected: FAIL because cache keys, gateway, and invalidation matrix are missing.

- [ ] **Step 3: Define complete anonymous/authenticated cache identity**

```kotlin
package org.mewx.wenku8.core.data.provider

data class ProviderCacheKey(
    val sourceId: String,
    val host: String,
    val schemaVersion: Int,
    val parserContractRevision: Int,
    val language: String?,
    val operation: String,
    val canonicalParameters: List<Pair<String, String>>,
    val accountId: String?,
    val sessionEpoch: Long?,
) {
    init {
        require((accountId == null) == (sessionEpoch == null))
        require(canonicalParameters == canonicalParameters.sortedWith(compareBy({ it.first }, { it.second })))
    }
}
```

- [ ] **Step 4: Implement TTL and stale policy around Phase 1 single-flight**

```kotlin
package org.mewx.wenku8.core.data.provider

import org.mewx.wenku8.api.contract.*
import org.mewx.wenku8.core.data.cache.CacheEntry
import org.mewx.wenku8.core.data.cache.DomainCache
import org.mewx.wenku8.core.data.cache.ReadSingleFlight
import org.mewx.wenku8.core.model.account.UserProfile
import org.mewx.wenku8.core.model.catalog.HomeSection

interface SessionEpochReader {
    fun accountId(): String?
    fun epoch(): Long?
}

enum class CacheMode { MEMORY, DURABLE_PHASE_3, NEVER }

data class CacheDisposition(
    val mode: CacheMode,
    val ttlMillis: Long = 0,
    val maxStaleMillis: Long = 0,
    val authenticated: Boolean = false,
)

object ProviderCachePolicy {
    val entries: Map<String, CacheDisposition> = linkedMapOf(
        "home" to memory(10, 24 * 60),
        "browse-latest" to memory(10, 24 * 60),
        "browse-completed" to memory(30, 24 * 60),
        "browse-category" to memory(15, 24 * 60),
        "browse-ranking" to memory(10, 24 * 60),
        "tag-groups" to memory(60, 7 * 24 * 60),
        "tags" to memory(60, 7 * 24 * 60),
        "novels-by-tag" to memory(10, 24 * 60),
        "search" to memory(5, 60),
        "novel-detail" to memory(30, 7 * 24 * 60),
        "catalog" to memory(30, 7 * 24 * 60),
        "chapter" to CacheDisposition(CacheMode.DURABLE_PHASE_3),
        "binary" to CacheDisposition(CacheMode.DURABLE_PHASE_3),
        "registration" to CacheDisposition(CacheMode.NEVER),
        "login-prewarm-root" to CacheDisposition(CacheMode.NEVER),
        "login-prewarm-form" to CacheDisposition(CacheMode.NEVER),
        "captcha" to CacheDisposition(CacheMode.NEVER),
        "validate-session" to CacheDisposition(CacheMode.NEVER, authenticated = true),
        "profile" to memory(5, 15, authenticated = true),
        "avatar" to memory(15, 60, authenticated = true),
        "bookshelf-read" to memory(1, 5, authenticated = true),
        "reviews" to memory(5, 60),
        "review-thread" to memory(5, 60),
    )

    private fun memory(ttlMinutes: Int, staleMinutes: Int, authenticated: Boolean = false) =
        CacheDisposition(CacheMode.MEMORY, ttlMinutes * 60_000L, staleMinutes * 60_000L, authenticated)
}

val ALL_TYPED_READ_OPERATION_IDS = setOf(
    "home", "browse-latest", "browse-completed", "browse-category", "browse-ranking", "tag-groups", "tags",
    "novels-by-tag", "search", "novel-detail", "catalog", "chapter", "binary", "registration",
    "login-prewarm-root", "login-prewarm-form", "captcha", "validate-session", "profile", "avatar",
    "bookshelf-read", "reviews", "review-thread",
)

data class CacheReadRequest<T : Any>(
    val operation: String,
    val parameters: List<Pair<String, String>>,
    val language: String?,
    val parserContractRevision: Int,
    val accountId: String?,
    val sessionEpoch: Long?,
) {
    val disposition = requireNotNull(ProviderCachePolicy.entries[operation])
    val authenticated = disposition.authenticated
    init {
        require(disposition.mode == CacheMode.MEMORY)
        require(parameters == parameters.sortedWith(compareBy({ it.first }, { it.second })))
        require(parserContractRevision > 0)
        require(authenticated == (accountId != null && sessionEpoch != null))
    }
}

class CacheReadRequests(private val operations: OperationRevisionSource, private val session: SessionEpochReader) {
    fun <T : Any> anonymous(operation: String, parameters: Map<String, String>, language: String?): CacheReadRequest<T> =
        build(operation, parameters, language, null, null)

    fun <T : Any> authenticated(operation: String, parameters: Map<String, String> = emptyMap()): CacheReadRequest<T> =
        build(operation, parameters, null, requireNotNull(session.accountId()), requireNotNull(session.epoch()))

    private fun <T : Any> build(
        operation: String,
        parameters: Map<String, String>,
        language: String?,
        accountId: String?,
        epoch: Long?,
    ) = CacheReadRequest<T>(
        operation,
        parameters.toList().sortedWith(compareBy({ it.first }, { it.second })),
        language,
        operations.parserContractRevision(operation),
        accountId,
        epoch,
    )
}

fun interface OperationRevisionSource {
    fun parserContractRevision(operation: String): Int
}

interface ProviderCacheIndex {
    fun add(key: ProviderCacheKey)
    fun removeFromIndex(key: ProviderCacheKey)
    fun keys(): Set<ProviderCacheKey>
}

interface EpochReadSingleFlight {
    suspend fun <K : Any, V : Any> run(key: K, block: suspend () -> V): V
    fun cancelAccount(sourceId: String, accountId: String, epoch: Long)
}

class CachedProviderGateway(
    private val cache: DomainCache<ProviderCacheKey, Any>,
    private val cacheIndex: ProviderCacheIndex,
    private val singleFlight: EpochReadSingleFlight,
    private val session: SessionEpochReader,
    private val nowMillis: () -> Long,
) {
    suspend fun <T : Any> read(
        request: CacheReadRequest<T>,
        fetch: suspend () -> ApiResult<T>,
    ): ApiResult<T> {
        val policy = request.disposition
        val key = ProviderCacheKey(
            "public", "www.wenku8.net", 1, request.parserContractRevision, request.language,
            request.operation, request.parameters, request.accountId, request.sessionEpoch,
        )
        return singleFlight.run(key) {
        @Suppress("UNCHECKED_CAST")
        val existing = cache.read(key) as CacheEntry<T>?
        val age = existing?.let { nowMillis() - it.fetchedAtEpochMillis }
        if (existing != null && age != null && age <= policy.ttlMillis) {
            return@run ApiResult.Success(existing.value, ResponseMetadata(key.sourceId, existing.fetchedAtEpochMillis, Freshness.FRESH))
        }
        val result = fetch()
        if (key.sessionEpoch != null && session.epoch() != key.sessionEpoch) {
            return@run ApiResult.Failure(ApiFailure.Auth.SessionExpired, "cache-epoch")
        }
        when (result) {
            is ApiResult.Success -> {
                cache.write(
                    key,
                    CacheEntry(result.value, nowMillis(), key.parserContractRevision, key.accountId, key.sessionEpoch),
                )
                cacheIndex.add(key)
                result
            }
            is ApiResult.Failure -> if (existing != null && age != null && age <= policy.maxStaleMillis && result.error is ApiFailure.Network) {
                ApiResult.Success(existing.value, ResponseMetadata(key.sourceId, existing.fetchedAtEpochMillis, Freshness.STALE))
            } else result
        }
        }
    }

    suspend fun onLogout(sourceId: SourceId, accountId: String, oldEpoch: Long) {
        singleFlight.cancelAccount(sourceId.value, accountId, oldEpoch)
        cacheIndex.keys().filter { it.sourceId == sourceId.value && it.accountId == accountId }.forEach { key ->
            cache.remove(key)
            cacheIndex.removeFromIndex(key)
        }
    }
}
```

`EpochReadSingleFlight` is the repository-owned deferred registry keyed by `ProviderCacheKey`; callers share one deferred, while `cancelAccount(source, account, epoch)` cancels and removes all matching old-epoch deferreds. `ProviderCacheIndex` is updated atomically with every cache write/removal and exposes a snapshot only to invalidation. Public repositories wrap every memory-cached typed read with the exact `CacheReadRequests` operation and canonical parameters. Parameterized tests cover all 16 `MEMORY` rows, both `DURABLE_PHASE_3` rows, and all five `NEVER` rows. Blank bodies, login/challenge/block pages, parse failures, server errors, and failed mutations never reach `cache.write`. Chapters and images use the durable content repository established in Phase 3; Phase 2 locks their identity and explicitly prevents a competing memory cache.

- [ ] **Step 5: Add the exact mutation invalidation matrix**

```yaml
schemaVersion: 2
rules:
  bookshelf-add:
    targets: [{ operation: bookshelf-read, scope: ACCOUNT }]
  bookshelf-remove:
    targets: [{ operation: bookshelf-read, scope: ACCOUNT }]
  bookshelf-move:
    targets: [{ operation: bookshelf-read, scope: ACCOUNT }]
  recommend:
    targets:
      - { operation: novel-detail, scope: NOVEL, parameter: novelId }
      - { operation: profile, scope: ACCOUNT }
  review-create:
    targets: [{ operation: reviews, scope: NOVEL, parameter: novelId }]
  review-reply:
    targets:
      - { operation: reviews, scope: NOVEL, parameter: novelId }
      - { operation: review-thread, scope: REVIEW, parameter: reviewId }
  daily-check-in:
    targets: [{ operation: profile, scope: ACCOUNT }]
  login-submit:
    allAuthenticatedForSource: true
  logout:
    allAuthenticatedForAccount: true
    cancelAccountEpochFlights: true
  account-switch:
    allAuthenticatedForSource: true
    cancelAccountEpochFlights: true
```

```kotlin
data class MutationScope(
    val sourceId: String,
    val host: String,
    val accountId: String?,
    val epoch: Long?,
    val novelId: String? = null,
    val reviewId: String? = null,
)

enum class ScopeKind { ACCOUNT, NOVEL, REVIEW }

data class InvalidationTarget(
    val operation: String,
    val scopeKind: ScopeKind,
    val parameter: String? = null,
)

data class InvalidationRule(
    val targets: List<InvalidationTarget> = emptyList(),
    val allAuthenticatedForSource: Boolean = false,
    val allAuthenticatedForAccount: Boolean = false,
    val cancelAccountEpochFlights: Boolean = false,
)

class MutationInvalidator(
    private val cache: DomainCache<ProviderCacheKey, Any>,
    private val index: ProviderCacheIndex,
    private val flights: EpochReadSingleFlight,
    private val rules: Map<String, InvalidationRule>,
) {
    suspend fun invalidate(mutation: String, scope: MutationScope) {
        val rule = requireNotNull(rules[mutation]) { "missing-invalidation-rule:$mutation" }
        if (rule.cancelAccountEpochFlights) {
            flights.cancelAccount(scope.sourceId, requireNotNull(scope.accountId), requireNotNull(scope.epoch))
        }
        val doomed = index.keys().filter { key ->
            key.sourceId == scope.sourceId && key.host == scope.host && when {
                rule.allAuthenticatedForSource -> key.accountId != null
                rule.allAuthenticatedForAccount -> key.accountId == scope.accountId
                else -> rule.targets.any { target -> target.matches(key, scope) }
            }
        }
        doomed.forEach { key -> cache.remove(key); index.removeFromIndex(key) }
    }
}

private fun InvalidationTarget.matches(key: ProviderCacheKey, scope: MutationScope): Boolean {
    if (key.operation != operation) return false
    if (scopeKind == ScopeKind.ACCOUNT && key.accountId != scope.accountId) return false
    val parameters = key.canonicalParameters.toMap()
    return when (scopeKind) {
        ScopeKind.ACCOUNT -> true
        ScopeKind.NOVEL -> parameters[requireNotNull(parameter)] == scope.novelId
        ScopeKind.REVIEW -> parameters[requireNotNull(parameter)] == scope.reviewId
    }
}
```

The YAML loader rejects missing/extra mutation IDs by comparing it with the typed mutation inventory, unknown operations/scopes/parameters, account rules without authenticated targets, and targeted rules without the required scope value. Tests seed two sources, hosts, accounts, epochs, novels, and reviews, then prove each rule removes only its exact keys. Logout clears every authenticated entry for that account, cancels old-epoch flights, and never removes anonymous content; account switch clears all authenticated entries for the source before the new account is exposed.

- [ ] **Step 6: Prove mutations are never coalesced or automatically retried**

Add parameterized tests covering login, logout, bookshelf add/remove/move, recommendation, daily check-in, review create, and reply. For two simultaneous calls, request count must be 2; for an injected connection failure, request count must remain 1 per call.

Run: `.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.provider.*" --stacktrace`

Expected: `BUILD SUCCESSFUL`; fake-clock TTL, 20-way single-flight, stale fallback, parser revision isolation, account+epoch partition, old-epoch rejection, targeted invalidation, and non-coalesced/non-retried mutations pass.

- [ ] **Step 7: Commit cache coordination**

```powershell
git add core/data/src
git commit -m "feat(data): coordinate provider cache and invalidation"
```

### Task 23: Expand the Reusable Provider Contract Suite Across Every Capability

**Depends on:** Tasks 4, 21, and 22.

**Files:**
- Modify: `studio-android/LightNovelLibrary/api-contract-tests/build.gradle`
- Modify: `studio-android/LightNovelLibrary/api-contract-tests/src/testFixtures/kotlin/org/mewx/wenku8/api/contract/testing/ProviderContractHarness.kt`
- Modify: `studio-android/LightNovelLibrary/api-contract-tests/src/testFixtures/kotlin/org/mewx/wenku8/api/contract/testing/ProviderContractSuite.kt`
- Create: `studio-android/LightNovelLibrary/api-contract-tests/src/testFixtures/kotlin/org/mewx/wenku8/api/contract/testing/ContractInvocation.kt`
- Create: `studio-android/LightNovelLibrary/api-contract-tests/src/testFixtures/kotlin/org/mewx/wenku8/api/contract/testing/ContractSamples.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderContractTest.kt`
- Test support: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/support/PublicContractHarness.kt`

- [ ] **Step 1: Write the failing public consumer test**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiFailure
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.ProviderCapability
import org.mewx.wenku8.api.contract.testing.ProviderContractHarness
import org.mewx.wenku8.api.contract.testing.ProviderContractSuite
import org.mewx.wenku8.api.publicprovider.support.PublicContractHarness

class PublicProviderContractTest : ProviderContractSuite() {
    override fun createHarness(): ProviderContractHarness = PublicContractHarness.create()

    @Test fun publicDailyCheckInIsUnsupportedAndDispatchesZeroRequests() = runTest {
        createHarness().use { harness ->
            val before = harness.dispatchedRequestCount
            val result = harness.provider.account.dailyCheckIn() as ApiResult.Failure
            assertEquals(ApiFailure.Unsupported(ProviderCapability.DAILY_CHECK_IN), result.error)
            assertEquals(before, harness.dispatchedRequestCount)
        }
    }
}
```

- [ ] **Step 2: Run the consumer test and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicProviderContractTest" --stacktrace`

Expected: FAIL because the Phase 1 contract skeleton does not enumerate all Phase 2 operations.

- [ ] **Step 3: Define a provider-neutral harness with observable dispatch only**

```kotlin
package org.mewx.wenku8.api.contract.testing

import org.mewx.wenku8.api.contract.ProviderBinding
import org.mewx.wenku8.api.contract.ProviderCapabilities
import org.mewx.wenku8.api.contract.LoginRequest

data class ContractWireRequest(
    val operationId: String,
    val method: String,
    val encodedPath: String,
    val queryNames: Set<String>,
    val formNames: Set<String>,
    val authenticated: Boolean,
)

interface ProviderContractHarness : AutoCloseable {
    val provider: ProviderBinding
    val dispatchedRequestCount: Int
    val dispatchedOperations: List<String>
    val recordedRequests: List<ContractWireRequest>
    fun withCapabilities(capabilities: ProviderCapabilities): ProviderContractHarness
    fun enqueueSuccess(operationId: String)
    fun enqueueNetworkFailure(operationId: String)
    fun enqueueNeverCompleting(operationId: String)
    fun callerSecretArrays(): Pair<CharArray, CharArray>
    fun loginRequest(): LoginRequest
    fun binaryRequest(): BinaryRequest
    override fun close()
}
```

Create the public harness around the real production composition and the strict TLS fixture server from Task 24. No test facet or fake transport implements a provider method:

```kotlin
package org.mewx.wenku8.api.publicprovider.support

import org.mewx.wenku8.api.contract.*
import org.mewx.wenku8.api.contract.testing.*
import org.mewx.wenku8.api.publicprovider.CAPABILITY_WIRE_OPERATIONS
import org.mewx.wenku8.api.publicprovider.PUBLIC_ACCEPTED_OPERATION_IDS
import org.mewx.wenku8.api.publicprovider.fixture.FixtureWenku8Server

class PublicContractHarness private constructor(
    private val fixture: FixtureWenku8Server,
) : ProviderContractHarness {
    private val password = charArrayOf('p')
    private val captcha = charArrayOf('1')

    override val provider: ProviderBinding get() = fixture.provider
    override val dispatchedRequestCount: Int get() = fixture.recordedRequests.size
    override val dispatchedOperations: List<String> get() = fixture.recordedRequests.map { it.operationId }
    override val recordedRequests: List<ContractWireRequest> get() = fixture.recordedRequests.toList()

    override fun withCapabilities(capabilities: ProviderCapabilities): ProviderContractHarness {
        val enabled = ProviderCapability.entries.filterTo(linkedSetOf(), capabilities::supports)
        val accepted = enabled.flatMapTo(linkedSetOf()) { CAPABILITY_WIRE_OPERATIONS.getValue(it) }
        return PublicContractHarness(FixtureWenku8Server(acceptedOperationIds = accepted - "daily-check-in"))
    }

    override fun enqueueSuccess(operationId: String) = fixture.enqueue(operationId, FixtureOutcome.SUCCESS)
    override fun enqueueNetworkFailure(operationId: String) = fixture.enqueue(operationId, FixtureOutcome.NETWORK_FAILURE)
    override fun enqueueNeverCompleting(operationId: String) = fixture.enqueue(operationId, FixtureOutcome.NEVER)
    override fun callerSecretArrays(): Pair<CharArray, CharArray> = password to captcha
    override fun loginRequest(): LoginRequest = fixture.loginRequest(password, captcha)
    override fun binaryRequest(): BinaryRequest = fixture.binaryRequest()

    override fun close() {
        password.fill('\u0000')
        captcha.fill('\u0000')
        fixture.close()
    }

    companion object {
        fun create(): PublicContractHarness = PublicContractHarness(
            FixtureWenku8Server(acceptedOperationIds = PUBLIC_ACCEPTED_OPERATION_IDS),
        )
    }
}
```

`CAPABILITY_WIRE_OPERATIONS` is the single checked map also used by `PublicProviderCapabilities`. `PUBLIC_ACCEPTED_OPERATION_IDS` includes every accepted synthetic public operation except `daily-check-in`. The fixture constructs the production `PublicCatalogSource`, `PublicBinarySource`, `PublicLoginController`, `PublicAccountSource`, `PublicCommunitySource`, parsers, transports, SessionStore, cache gateway, and final `PublicProvider` against one TLS-only scripted server. Capability removal rebuilds the accepted synthetic registry; it does not inject an arbitrary capability object into a fake facet.

Every synthetic evidence row binds an operation ID to one exact method, encoded path template, query/form field set, auth flag, parser revision, fixture hash, and response script. `FixtureWenku8Server` records only requests that reach its dispatcher, so an absent capability can pass only if the production guard dispatches zero requests. No shared fixture contains a live endpoint, selector, Cookie, username, password, captcha, response body, or private-provider detail.

- [ ] **Step 4: Enumerate every operation and its required capability**

```kotlin
package org.mewx.wenku8.api.contract.testing

import org.mewx.wenku8.api.contract.ProviderBinding
import org.mewx.wenku8.api.contract.ProviderCapability

data class ContractInvocation(
    val typedOperationId: String,
    val capability: ProviderCapability,
    val expectedWireOperations: List<String>,
    val absentBehavior: AbsentBehavior,
    val invoke: suspend (ProviderContractHarness) -> ApiResult<*>,
)

enum class AbsentBehavior { UNSUPPORTED, LOCAL_SUCCESS }

private fun invocation(
    typed: String,
    capability: ProviderCapability,
    vararg wire: String,
    invoke: suspend (ProviderContractHarness) -> ApiResult<*>,
) = ContractInvocation(typed, capability, if (wire.isEmpty()) listOf(typed) else wire.toList(), AbsentBehavior.UNSUPPORTED, invoke)

val ALL_CONTRACT_INVOCATIONS = listOf(
    invocation("home", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.home() }),
    invocation("browse-latest", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.browse(ContractSamples.latest) }),
    invocation("browse-completed", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.browse(ContractSamples.completed) }),
    invocation("browse-category", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.browse(ContractSamples.category) }),
    invocation("browse-ranking", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.browse(ContractSamples.ranking) }),
    invocation("tag-groups", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.tagGroups(ContractSamples.language) }),
    invocation("tags", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.tags(ContractSamples.tagDiscovery) }),
    invocation("novels-by-tag", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.novelsByTag(ContractSamples.tagBrowse) }),
    invocation("search", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.search(ContractSamples.search) }),
    invocation("novel-detail", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.novel(ContractSamples.novel) }),
    invocation("catalog", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.catalog(ContractSamples.novel) }),
    invocation("chapter", ProviderCapability.ANONYMOUS_CATALOG, invoke = { it.provider.catalog.chapter(ContractSamples.chapter) }),
    invocation("binary", ProviderCapability.BINARY_DOWNLOAD, invoke = { it.provider.binary.fetch(it.binaryRequest()) }),
    ContractInvocation("registration", ProviderCapability.REGISTRATION_LINK, emptyList(), AbsentBehavior.UNSUPPORTED) { it.provider.account.registrationPage() },
    invocation("begin-login", ProviderCapability.CAPTCHA_LOGIN, "login-prewarm-root", "login-prewarm-form", "captcha", invoke = { it.provider.account.beginLogin() }),
    invocation("login", ProviderCapability.CAPTCHA_LOGIN, "login-submit", invoke = { it.provider.account.login(it.loginRequest()) }),
    invocation("validate-session", ProviderCapability.CAPTCHA_LOGIN, invoke = { it.provider.account.validateSession() }),
    invocation("profile", ProviderCapability.PROFILE, invoke = { it.provider.account.profile() }),
    invocation("avatar", ProviderCapability.PROFILE, invoke = { it.provider.account.avatar() }),
    invocation("daily-check-in", ProviderCapability.DAILY_CHECK_IN, invoke = { it.provider.account.dailyCheckIn() }),
    invocation("bookshelf-read", ProviderCapability.BOOKSHELF_READ, invoke = { it.provider.account.bookshelf() }),
    invocation("bookshelf-add", ProviderCapability.BOOKSHELF_MUTATE, invoke = { it.provider.account.updateBookshelf(ContractSamples.bookshelfAdd) }),
    invocation("bookshelf-remove", ProviderCapability.BOOKSHELF_MUTATE, invoke = { it.provider.account.updateBookshelf(ContractSamples.bookshelfRemove) }),
    invocation("bookshelf-move", ProviderCapability.BOOKSHELF_MUTATE, invoke = { it.provider.account.updateBookshelf(ContractSamples.bookshelfMove) }),
    invocation("recommend", ProviderCapability.RECOMMEND_NOVEL, invoke = { it.provider.account.recommendNovel(ContractSamples.novel) }),
    ContractInvocation("logout", ProviderCapability.CAPTCHA_LOGIN, listOf("logout"), AbsentBehavior.LOCAL_SUCCESS) { it.provider.account.logout() },
    invocation("reviews", ProviderCapability.REVIEWS_READ, invoke = { it.provider.community.reviews(ContractSamples.novel, 1) }),
    invocation("review-thread", ProviderCapability.REVIEWS_READ, invoke = { it.provider.community.reviewThread(ContractSamples.review, 1) }),
    invocation("review-create", ProviderCapability.REVIEWS_CREATE, invoke = { it.provider.community.createReview(ContractSamples.createReview) }),
    invocation("review-reply", ProviderCapability.REVIEWS_REPLY, invoke = { it.provider.community.reply(ContractSamples.reply) }),
)

val EXPECTED_SYNTHETIC_WIRE_CONTRACTS: Map<String, ContractWireRequest> = listOf(
    wire("home"), wire("browse-latest", query = setOf("page")),
    wire("browse-completed", query = setOf("page")),
    wire("browse-category", query = setOf("category", "page")),
    wire("browse-ranking", query = setOf("period", "page")),
    wire("tag-groups", query = setOf("language")), wire("tags", query = setOf("group", "language")),
    wire("novels-by-tag", query = setOf("tag", "page")),
    wire("search", query = setOf("q", "scope", "page")),
    wire("novel-detail", path = "/fixture/novel-detail/synthetic-novel"),
    wire("catalog", path = "/fixture/catalog/synthetic-novel"),
    wire("chapter", path = "/fixture/chapter/synthetic-novel/synthetic-chapter"), wire("binary"),
    wire("login-prewarm-root"), wire("login-prewarm-form"), wire("captcha"),
    wire("login-submit", method = "POST", form = setOf("username", "password", "captcha")),
    wire("validate-session", authenticated = true), wire("profile", authenticated = true),
    wire("avatar", authenticated = true), wire("bookshelf-read", authenticated = true),
    wire("bookshelf-add", method = "POST", form = setOf("novelId", "groupId"), authenticated = true),
    wire("bookshelf-remove", method = "POST", form = setOf("bid", "sourceGroupId"), authenticated = true),
    wire("bookshelf-move", method = "POST", form = setOf("bid", "sourceGroupId", "targetGroupId"), authenticated = true),
    wire("recommend", method = "POST", form = setOf("novelId"), authenticated = true),
    wire("reviews", query = setOf("novelId", "page")), wire("review-thread", query = setOf("reviewId", "page")),
    wire("review-create", method = "POST", form = setOf("novelId", "title", "body"), authenticated = true),
    wire("review-reply", method = "POST", form = setOf("reviewId", "body"), authenticated = true),
    wire("logout", method = "POST", authenticated = true),
    wire("daily-check-in", method = "POST", authenticated = true),
).associateBy(ContractWireRequest::operationId)

private fun wire(
    operation: String,
    method: String = "GET",
    path: String = "/fixture/$operation",
    query: Set<String> = emptySet(),
    form: Set<String> = emptySet(),
    authenticated: Boolean = false,
) = ContractWireRequest(operation, method, path, query, form, authenticated)
```

`EXPECTED_SYNTHETIC_WIRE_CONTRACTS` is authored in the shared contract module, independently of the provider registry and fixture dispatcher. The public fixture registry must match it exactly; registration is an evidence-backed link lookup and therefore has an intentionally empty dispatch sequence.
Create the complete provider-neutral samples used by every invocation:

```kotlin
package org.mewx.wenku8.api.contract.testing

import org.mewx.wenku8.api.contract.*
import org.mewx.wenku8.core.model.catalog.BinaryRequest
import org.mewx.wenku8.core.model.identity.*
import org.mewx.wenku8.core.model.settings.ContentLanguage

object ContractSamples {
    val providerId = SourceId("public")
    val language = ContentLanguage.SIMPLIFIED_CHINESE
    val novel = NovelKey(providerId, "synthetic-novel")
    val chapter = ChapterKey(novel, "synthetic-chapter")
    val review = ReviewKey(novel, "synthetic-review")
    val binary = BinaryRequest("https://synthetic.invalid/image.png", "binary")
    val latest = BrowseRequest(BrowseKind.LATEST, 1, language)
    val completed = BrowseRequest(BrowseKind.COMPLETED, 1, language)
    val category = BrowseRequest(BrowseKind.CATEGORY, 1, language, categoryId = "synthetic-category")
    val ranking = BrowseRequest(BrowseKind.RANKING, 1, language, rankingPeriod = RankingPeriod.WEEK)
    val tagDiscovery = TagDiscoveryRequest("synthetic-group", language)
    val tagBrowse = TagBrowseRequest("synthetic-tag", 1, language)
    val search = SearchQuery("synthetic", SearchScope.TITLE, 1, language)
    val bookshelfAdd = BookshelfCommand.Add(novel, "synthetic-group")
    val bookshelfRemove = BookshelfCommand.Remove(BookshelfEntryKey("synthetic-bid"), "synthetic-group")
    val bookshelfMove = BookshelfCommand.Move(
        listOf(BookshelfEntryKey("synthetic-bid")),
        "synthetic-group",
        "synthetic-target",
    )
    val createReview = CreateReviewCommand(novel, "Synthetic title", "Synthetic body")
    val reply = ReplyCommand(review, "Synthetic reply")

    fun loginRequest(
        password: CharArray = charArrayOf('p'),
        captcha: CharArray = charArrayOf('1'),
    ) = LoginRequest(LoginAttemptId("synthetic-attempt"), "synthetic-account", password, captcha)

    fun capabilities(enabled: Set<ProviderCapability>) = ProviderCapabilities(
        providerId,
        enabled,
        ProviderInputPolicy(80, 60, 4_000, 4_000),
    )
}
```

`ContractSamples` contains only synthetic IDs/text and a non-routable synthetic HTTPS image host.

- [ ] **Step 5: Add exhaustive absent-capability and enabled-operation contracts**

```kotlin
abstract class ProviderContractSuite {
    protected abstract fun createHarness(): ProviderContractHarness

    @Test fun everyAbsentCapabilityReturnsMatchingUnsupportedAndDispatchesZeroRequests() = runTest {
        ALL_CONTRACT_INVOCATIONS.forEach { invocation ->
            createHarness().use { original ->
                val enabled = ProviderCapability.entries.toSet() - invocation.capability
                original.withCapabilities(ContractSamples.capabilities(enabled)).use { harness ->
                    val before = harness.dispatchedRequestCount
                    val result = invocation.invoke(harness)
                    when (invocation.absentBehavior) {
                        AbsentBehavior.UNSUPPORTED -> assertEquals(
                            ApiFailure.Unsupported(invocation.capability), (result as ApiResult.Failure).error,
                        )
                        AbsentBehavior.LOCAL_SUCCESS -> assertTrue(result is ApiResult.Success)
                    }
                    assertEquals(before, harness.dispatchedRequestCount)
                }
            }
        }
    }

    @Test fun enabledOperationsNeverReturnUnsupported() = runTest {
        ALL_CONTRACT_INVOCATIONS.filterNot { it.capability == ProviderCapability.DAILY_CHECK_IN }.forEach { invocation ->
            createHarness().use { harness ->
                invocation.expectedWireOperations.forEach(harness::enqueueSuccess)
                val before = harness.recordedRequests.size
                val result = invocation.invoke(harness)
                assertFalse((result as? ApiResult.Failure)?.error is ApiFailure.Unsupported)
                val actual = harness.recordedRequests.drop(before)
                assertEquals(invocation.expectedWireOperations, actual.map(ContractWireRequest::operationId))
                assertEquals(
                    invocation.expectedWireOperations.map(EXPECTED_SYNTHETIC_WIRE_CONTRACTS::getValue),
                    actual,
                )
            }
        }
    }
}
```

- [ ] **Step 6: Add provider-neutral input, cancellation, secret, and mutation contracts**

The shared suite adds concrete tests for positive pages, nullable `nextPage`, tag discovery separation, unique non-empty move keys, source/target difference, code-point/GBK input limits, chapter block order, `CancellationException` propagation, caller array clearing on success/failure/cancellation, exact account+epoch invalidation, and one request per failed mutation with no retry/coalescing.

- [ ] **Step 7: Publish the reusable suite and run it against public**

```groovy
plugins {
    alias libs.plugins.kotlin.jvm
    id 'java-library'
    id 'java-test-fixtures'
}

dependencies {
    api project(':api-contract')
    api libs.junit4
    api libs.kotlinx.coroutines.test
}
```

Run: `.\gradlew.bat :api-contract-tests:build :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicProviderContractTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; all 30 typed invocations pass absent/enabled/input/cancellation/secret/mutation contracts.

- [ ] **Step 8: Commit the shared suite**

```powershell
git add api-contract-tests api-public/src/test
git commit -m "test(api): enforce shared provider contracts"
```

### Task 24: Prove Deterministic Search-to-Reader and Account Journeys

**Depends on:** Task 23.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/fixture/FixtureWenku8Server.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/PublicProviderJourneyTest.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/journey/search.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/journey/detail.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/journey/catalog.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/journey/chapter.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/journey/profile.html`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/resources/fixtures/journey/bookshelf.html`

- [ ] **Step 1: Write the failing end-to-end deterministic journeys**

```kotlin
package org.mewx.wenku8.api.publicprovider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.SearchQuery
import org.mewx.wenku8.api.contract.SearchScope
import org.mewx.wenku8.core.model.settings.ContentLanguage

class PublicProviderJourneyTest {
    @Test fun searchDetailCatalogChapterImageJourneyCompletesAgainstTlsFixtures() = runTest {
        FixtureWenku8Server().use { fixture ->
            val provider = fixture.provider
            val search = provider.catalog.search(SearchQuery("轻小说", SearchScope.TITLE, 1, ContentLanguage.SIMPLIFIED_CHINESE)).success()
            val novel = provider.catalog.novel(search.value.items.single().key).success()
            val catalog = provider.catalog.catalog(novel.value.key).success()
            val chapter = provider.catalog.chapter(catalog.value.single().chapters.single().key).success()
            val image = provider.binary.fetch(chapter.value.blocks.filterIsInstance<ChapterBlock.Image>().single().resource).success()
            assertEquals("Synthetic Journey Novel", novel.value.title)
            assertEquals("image/jpeg", image.value.mediaType)
            assertEquals(listOf("search", "novel-detail", "catalog", "chapter", "binary"), fixture.operations)
        }
    }

    @Test fun captchaProfileBookshelfLogoutJourneyUsesOneAttemptAndEndsSignedOut() = runTest {
        FixtureWenku8Server().use { fixture ->
            val challenge = fixture.provider.account.beginLogin().success().value
            fixture.provider.account.login(fixture.loginRequest(challenge.attemptId)).success()
            fixture.provider.account.validateSession().success()
            fixture.provider.account.profile().success()
            fixture.provider.account.bookshelf().success()
            fixture.provider.account.logout()
            assertTrue(fixture.sessionStore.load(fixture.provider.providerId) == null)
            assertEquals(0, fixture.secretLeakScan().size)
        }
    }
}
```

- [ ] **Step 2: Run journeys and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicProviderJourneyTest" --stacktrace`

Expected: FAIL because the coherent TLS fixture dispatcher is missing.

- [ ] **Step 3: Implement one strict TLS MockWebServer dispatcher**

```kotlin
class FixtureWenku8Server : AutoCloseable {
    private val certificate = HeldCertificate.Builder().commonName("www.wenku8.net").addSubjectAlternativeName("www.wenku8.net").build()
    private val certificates = HandshakeCertificates.Builder().heldCertificate(certificate).build()
    private val server = MockWebServer().apply {
        useHttps(certificates.sslSocketFactory(), false)
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path?.startsWith("/fixture/search") == true -> gbk("journey/search.html")
                request.path == "/fixture/detail/601" -> gbk("journey/detail.html")
                request.path == "/fixture/catalog/601" -> gbk("journey/catalog.html")
                request.path == "/fixture/chapter/601/701" -> gbk("journey/chapter.html")
                request.path == "/fixture/image/701.jpg" -> jpeg(SYNTHETIC_JPEG_BYTES)
                request.path == "/fixture/login-root" -> cookie("attempt=fixture; Secure; Path=/")
                request.path == "/fixture/login-form" -> gbk("account/login-form.html")
                request.path == "/fixture/captcha" -> png(SYNTHETIC_CAPTCHA_BYTES)
                request.path == "/fixture/login-submit" && request.getHeader("Cookie")?.contains("attempt=fixture") == true -> cookieAndGbk("session=fixture-session; Secure; HttpOnly; Path=/", "account/login-success.html")
                request.path == "/fixture/session" -> gbk("account/session-valid.html")
                request.path == "/fixture/profile" -> gbk("journey/profile.html")
                request.path == "/fixture/bookshelf" -> gbk("journey/bookshelf.html")
                request.path == "/fixture/logout" -> gbk("account/logout-success.html")
                else -> MockResponse().setResponseCode(404)
            }
        }
        start()
    }
}
```

All fixture documents use synthetic content. The test HostPolicy accepts only the fixture TLS origin and synthetic image TLS origin. Production HostPolicy remains exactly the evidence allowlist.

- [ ] **Step 4: Run both journeys, full parser suite, and leak scan**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.PublicProviderJourneyTest" --tests "org.mewx.wenku8.api.publicprovider.parser.*" --stacktrace`

Expected: `BUILD SUCCESSFUL`; search -> detail -> catalog -> chapter -> image and login -> validate -> profile -> bookshelf -> logout pass with no external network.

- [ ] **Step 5: Commit deterministic journeys**

```powershell
git add api-public/src/test
git commit -m "test(api): prove deterministic provider journeys"
```

### Task 25: Replace `api-stub` with the Non-Throwing Frozen-ABI Public Bridge

**Depends on:** Tasks 1 and 21.

**Files:**
- Delete: `studio-android/LightNovelLibrary/api-stub/`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/api/Wenku8API.kt`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/api/Wenku8Error.kt`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/network/LightNetwork.kt`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/network/LightUserSession.kt`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/api/LegacyBridgeRuntime.kt`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/api/LegacyRequestClassifier.kt`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/api/AuditedLegacyBlockingTransport.kt`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/api/LegacyAction.kt`
- Create: `studio-android/LightNovelLibrary/api-legacy-bridge/src/main/java/org/mewx/wenku8/api/LegacyRouteCapabilityPolicy.kt`
- Test: `studio-android/LightNovelLibrary/api-legacy-bridge/src/test/java/org/mewx/wenku8/api/LegacyBridgeContractTest.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/api/PublicApiStubContractTest.kt`

- [ ] **Step 1: Write failing ABI, no-throw, and main-thread tests**

```kotlin
package org.mewx.wenku8.api

import android.content.ContentValues
import android.os.Looper
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.network.LightNetwork

class LegacyBridgeContractTest {
    @Test fun everyFrozenHelperReturnsWithoutUnsupportedOperationException() {
        val calls = listOf<() -> Any?>(
            { Wenku8API.getCoverURL(1) },
            { Wenku8API.getNovelFullMeta(1, Wenku8API.AppLanguage.SC) },
            { Wenku8API.getNovelIndex(1, Wenku8API.AppLanguage.SC) },
            { Wenku8API.searchNovelByNovelName("synthetic", Wenku8API.AppLanguage.SC) },
            { Wenku8API.getBookshelfListParams(Wenku8API.AppLanguage.SC) },
            { Wenku8API.getCommentReplyParams(1, "synthetic") },
        )
        calls.forEach { call -> assertTrue(runCatching(call).exceptionOrNull() !is UnsupportedOperationException) }
    }

    @Test fun mainThreadSynchronousNetworkReturnsNullWithoutRunBlocking() {
        assertTrue(Looper.myLooper() == Looper.getMainLooper())
        assertNull(LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, ContentValues()))
        assertNull(LightNetwork.LightHttpDownload("https://www.wenku8.net/synthetic"))
    }

    @Test fun classifierRejectsUnknownHostPathFieldsAndPrivateInstallationBeforeDispatch() {
        val harness = LegacyBridgeHarness.publicAcceptedCatalog()
        assertNull(harness.post("https://evil.invalid/catalog", ContentValues()))
        assertNull(harness.post("https://www.wenku8.net/unaccepted", ContentValues()))
        assertNull(harness.post(harness.catalogUrl, ContentValues().apply { put("unexpected", "1") }))
        assertNull(harness.download("https://www.wenku8.net/unaccepted.jpg"))
        assertEquals(0, harness.server.requestCount)
        assertFailsWith<IllegalArgumentException> { harness.installAs(SourceId("private")) }
    }

    @Test fun visibilityRequiresBothTypedCapabilityAndInstalledClassifiedRoute() {
        val harness = LegacyBridgeHarness.publicAcceptedCatalog(install = false)
        assertFalse(harness.policy.visible(LegacyAction.CATALOG))
        harness.installAs(SourceId("public"))
        assertTrue(harness.policy.visible(LegacyAction.CATALOG))
    }
}
```

Phase 0's frozen ABI test also compares the old signed/minified public class/member descriptors for `Wenku8API`, nested enums, `Wenku8Error`, `LightNetwork`, and `LightUserSession` against the new minified bridge.

- [ ] **Step 2: Run bridge/ABI tests and confirm RED**

Run: `.\gradlew.bat :api:testDebugUnitTest --tests "org.mewx.wenku8.api.LegacyBridgeContractTest" -Pwenku8Provider=public --stacktrace`

Run: `.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.api.PublicApiStubContractTest" -Pwenku8Provider=public --stacktrace`

Expected: FAIL because logical `:api` points at a not-yet-populated bridge and the existing stub helpers throw.

- [ ] **Step 3: Preserve all frozen enum, field, method, and nested-class descriptors**

Move the complete public signatures from `api-stub` to `api-legacy-bridge` without renaming packages, classes, fields, enum constants, methods, parameters, return types, nullability-visible annotations, or nested classes. Replace `unavailable()` bodies with pure URL/enum/`ContentValues` encoders for accepted safe operations; helpers whose legacy wire shape is not accepted return an empty `ContentValues` or null according to their existing non-throwing failure contract. `BASE_URL` and `REGISTER_URL` are accepted HTTPS values only when their evidence exists; otherwise they remain non-routable and the corresponding old route is disabled.

- [ ] **Step 4: Add exact classification and a concrete audited blocking transport**

```kotlin
package org.mewx.wenku8.api

enum class LegacyAction { CATALOG, BINARY, ACCOUNT, BOOKSHELF_MUTATION, REVIEW_MUTATION, DAILY_CHECK_IN }

data class ClassifiedLegacyRequest(
    val operationId: String,
    val action: LegacyAction,
    val method: String,
    val canonicalHttpsUrl: String,
    val form: List<Pair<String, String>>,
    val maxResponseBytes: Long,
)

class LegacyRequestClassifier(private val registry: OperationRegistry) {
    fun classifyPost(url: String, values: ContentValues): ClassifiedLegacyRequest? =
        classify("POST", url, values.valueSet().associate { it.key to (it.value as? String ?: return null) })

    fun classifyDownload(url: String): ClassifiedLegacyRequest? = classify("GET", url, emptyMap())

    private fun classify(method: String, rawUrl: String, fields: Map<String, String>): ClassifiedLegacyRequest? {
        val url = rawUrl.toHttpUrlOrNull() ?: return null
        if (url.scheme != "https" || url.username.isNotEmpty() || url.password.isNotEmpty()) return null
        val matches = registry.records().filter { evidence ->
            !evidence.authenticated && evidence.operationId in LEGACY_SAFE_OPERATIONS &&
                evidence.method.name == method && sameOrigin(url, evidence.canonicalHttpsOrigin.toHttpUrl()) &&
                ExactPathTemplate(evidence.encodedPathTemplate).matches(url.encodedPath) &&
                url.queryParameterNames == evidence.queryFields && fields.keys == evidence.formFields
        }
        val evidence = matches.singleOrNull() ?: return null
        if ((url.queryParameterNames.flatMap(url::queryParameterValues) + fields.values).any { it.length > 4_096 }) return null
        val action = if (evidence.operationId == "binary") LegacyAction.BINARY else LegacyAction.CATALOG
        return ClassifiedLegacyRequest(
            evidence.operationId, action, method, url.toString(), fields.toSortedMap().toList(), evidence.maxResponseBytes,
        )
    }

    private fun sameOrigin(left: HttpUrl, right: HttpUrl): Boolean =
        left.scheme == right.scheme && left.host == right.host && left.port == right.port

    private companion object {
        val LEGACY_SAFE_OPERATIONS = setOf(
            "home", "browse-latest", "browse-completed", "browse-category", "browse-ranking",
            "tag-groups", "tags", "novels-by-tag", "search", "novel-detail", "catalog", "chapter", "binary",
        )
    }
}
```

`ExactPathTemplate` splits both template and encoded path into segments, requires identical segment counts and literal segments, and permits a placeholder segment only when it matches `[A-Za-z0-9._~-]{1,128}`. It rejects encoded `/`, `\\`, dot segments, duplicate query names, fragments, non-default ports, and path normalization changes. Add table tests for every accepted template and every rejection class.

```kotlin
class AuditedLegacyBlockingTransport(
    private val client: OkHttpClient,
    private val classifier: LegacyRequestClassifier,
) {
    fun post(url: String, values: ContentValues): ByteArray? =
        classifier.classifyPost(url, values)?.let(::execute)

    fun download(url: String): ByteArray? = classifier.classifyDownload(url)?.let(::execute)

    private fun execute(classified: ClassifiedLegacyRequest): ByteArray? {
        check(Looper.myLooper() != Looper.getMainLooper()) { "legacy-main-thread-dispatch" }
        val body = if (classified.method == "POST") {
            FormBody.Builder(Charsets.UTF_8).apply { classified.form.forEach { (key, value) -> add(key, value) } }.build()
        } else null
        val request = Request.Builder()
            .url(classified.canonicalHttpsUrl)
            .tag(OperationTag::class.java, OperationTag(NetworkOperationScope(classified.operationId), authenticated = false))
            .let { if (body == null) it.get() else it.post(body) }
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            response.body?.let { BoundedBodyReader.read(it, classified.maxResponseBytes) }
        }
    }
}
```

The injected client is `PublicHttpClientFactory.anonymous()`: HTTPS HostPolicy and exact redirect contracts are installed, automatic redirects and retries are off, and the CookieJar is empty. The bridge never adapts a suspend provider method and never owns authenticated operations.

- [ ] **Step 5: Install the audited bridge only from public composition**

```kotlin
package org.mewx.wenku8.api

object LegacyBridgeRuntime {
    @Volatile private var installed: AuditedLegacyBlockingTransport? = null
    @Volatile private var supportedActions: Set<LegacyAction> = emptySet()

    fun installPublic(providerId: SourceId, value: AuditedLegacyBlockingTransport, actions: Set<LegacyAction>) {
        require(providerId == SourceId("public")) { "legacy-bridge-public-only" }
        check(installed == null) { "legacy-bridge-already-installed" }
        installed = value
        supportedActions = actions.toSet()
    }

    fun supports(action: LegacyAction): Boolean = installed != null && action in supportedActions
    fun post(url: String, values: ContentValues): ByteArray? = if (isMainThread()) null else installed?.post(url, values)
    fun download(url: String): ByteArray? = if (isMainThread()) null else installed?.download(url)
    private fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()
}
```

```kotlin
@JvmStatic fun LightHttpPostConnection(u: String?, values: ContentValues?): ByteArray? {
    if (u.isNullOrBlank() || values == null) return null
    return LegacyBridgeRuntime.post(u, values)
}

@JvmStatic fun LightHttpDownload(url: String?): ByteArray? =
    url?.let(LegacyBridgeRuntime::download)
```

`DefaultAppContainer` calls `installPublic` only in the `BuildConfig.WENKU8_PROVIDER_ID == "public"` branch after constructing the verified registry and anonymous client. Private composition never links or installs this runtime. Tests execute accepted calls on a background executor, assert the exact operation tag/method/path/query/form, bounded body, and one dispatch, and reset the singleton through an `internal` test-only hook compiled out of release.

- [ ] **Step 6: Make legacy session ABI signed-out and password-free**

`LightUserSession` retains all public members but `getPassword()` always returns `""`, `setUserInfo` never retains its password parameter, `encUserFile()` returns the password-free `WENKU8_CERT_V2` marker, `decAndSetUserFile()` never decodes a password, and every login method returns the existing not-logged-in error until the new account route owns login. `logOut` clears the typed SessionStore through an installed compatibility callback and always invokes the legacy file callback.

- [ ] **Step 7: Disable unadaptable visible legacy actions before invocation**

```kotlin
class LegacyRouteCapabilityPolicy(private val capabilities: ProviderCapabilities) {
    fun visible(action: LegacyAction): Boolean = when (action) {
        LegacyAction.CATALOG -> capabilities.supports(ProviderCapability.ANONYMOUS_CATALOG) && LegacyBridgeRuntime.supports(action)
        LegacyAction.BINARY -> capabilities.supports(ProviderCapability.BINARY_DOWNLOAD) && LegacyBridgeRuntime.supports(action)
        LegacyAction.ACCOUNT -> false
        LegacyAction.BOOKSHELF_MUTATION -> false
        LegacyAction.REVIEW_MUTATION -> false
        LegacyAction.DAILY_CHECK_IN -> false
    }
}
```

Until Phase 4/5/7 new routes replace these old wire shapes, legacy UI hides the action or shows the standard unavailable explanation. It never invokes a throwing stub.

- [ ] **Step 8: Run frozen ABI, no-throw, classifier, and source scans**

Run: `.\gradlew.bat :api:testDebugUnitTest --tests "org.mewx.wenku8.api.LegacyBridgeContractTest" -Pwenku8Provider=public --stacktrace`

Run: `.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.api.PublicApiStubContractTest" -Pwenku8Provider=public --stacktrace`

Expected: `BUILD SUCCESSFUL`; frozen descriptors match and visible helper calls do not throw.

Run: `rg -n "UnsupportedOperationException|runBlocking" api-legacy-bridge app/src/main`

Expected: no `UnsupportedOperationException` or `runBlocking` in the bridge; unrelated test-only assertions may exist outside these production paths.

- [ ] **Step 9: Commit bridge replacement**

```powershell
git add api-legacy-bridge app/src api-stub
git commit -m "refactor(api): replace throwing public stub with bridge"
```

### Task 26: Add the Protected Private Adapter Entry and Signed Attestation Gate

**Depends on:** Tasks 1, 4, and 23.

**Files:**
- Create: `studio-android/LightNovelLibrary/api-contract/src/main/kotlin/org/mewx/wenku8/api/contract/ProviderPlugin.kt`
- Create: `studio-android/LightNovelLibrary/api-private-adapter/src/main/java/org/mewx/wenku8/api/privateadapter/PrivateProviderPlugin.kt`
- Create: `studio-android/LightNovelLibrary/api-private-adapter/src/main/resources/META-INF/services/org.mewx.wenku8.api.contract.ProviderPlugin`
- Create: `studio-android/LightNovelLibrary/api-private-adapter/src/test/java/org/mewx/wenku8/api/privateadapter/PrivateProviderContractTest.kt`
- Create: `studio-android/LightNovelLibrary/api-private-adapter/src/test/java/org/mewx/wenku8/api/privateadapter/PrivateAttestationEmissionTest.kt`
- Protected overlay create: `WENKU8_PRIVATE_ADAPTER_OVERLAY/src/test/kotlin/org/mewx/wenku8/api/privateadapter/ProtectedPrivateContractHarness.kt`
- Modify: `studio-android/LightNovelLibrary/api-private-adapter/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/ProviderBindingFactory.kt`
- Create: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/provider/ProviderPluginCompositionTest.kt`
- Create: `studio-android/LightNovelLibrary/verification/verify-private-provider-attestation.ps1`

- [ ] **Step 1: Write the failing protected adapter contract entry**

```kotlin
package org.mewx.wenku8.api.contract

import org.mewx.wenku8.core.model.identity.SourceId

interface ProviderPlugin {
    val providerId: SourceId
    fun create(): ProviderBinding
}
```

```kotlin
package org.mewx.wenku8.api.privateadapter

import java.util.ServiceLoader
import org.mewx.wenku8.api.contract.ProviderBinding
import org.mewx.wenku8.api.contract.ProviderPlugin
import org.mewx.wenku8.core.model.identity.SourceId

class PrivateProviderPlugin : ProviderPlugin {
    override val providerId = SourceId("private")
    override fun create(): ProviderBinding = ProtectedPrivateComposition.create()
}

object ProtectedPrivateAdapterLoader {
    fun load(): ProviderBinding {
        val plugins = ServiceLoader.load(ProviderPlugin::class.java).toList()
        require(plugins.size == 1 && plugins.single().providerId == SourceId("private")) {
            "Exactly one protected private provider plugin is required"
        }
        return plugins.single().create().also { require(it.providerId == SourceId("private")) }
    }
}
```

```kotlin
package org.mewx.wenku8.api.privateadapter

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.contract.ApiResult
import org.mewx.wenku8.api.contract.ProviderCapability
import org.mewx.wenku8.api.contract.testing.ProviderContractSuite

class PrivateProviderContractTest : ProviderContractSuite() {
    override fun createHarness() = ProtectedPrivateContractHarness(ProtectedPrivateAdapterLoader.load())

    @Test fun protectedAdapterEnablesAndDispatchesDailyCheckIn() = runTest {
        createHarness().use { harness ->
            assertTrue(harness.provider.account.capabilities().supports(ProviderCapability.DAILY_CHECK_IN))
            harness.enqueueSuccess("daily-check-in")
            assertTrue(harness.provider.account.dailyCheckIn() is ApiResult.Success)
            assertEquals(listOf("daily-check-in"), harness.dispatchedOperations.takeLast(1))
        }
    }
}
```

The protected overlay file at `WENKU8_PRIVATE_ADAPTER_OVERLAY/src/test/kotlin/org/mewx/wenku8/api/privateadapter/ProtectedPrivateContractHarness.kt` defines the only private harness and its required driver interface:

```kotlin
package org.mewx.wenku8.api.privateadapter

import org.mewx.wenku8.api.contract.ProviderBinding
import org.mewx.wenku8.api.contract.ProviderCapabilities
import org.mewx.wenku8.api.contract.LoginRequest
import org.mewx.wenku8.api.contract.testing.ContractWireRequest
import org.mewx.wenku8.api.contract.testing.ProviderContractHarness
import org.mewx.wenku8.core.model.catalog.BinaryRequest

interface ProtectedPrivateTestDriver {
    val provider: ProviderBinding
    val dispatchedRequestCount: Int
    val dispatchedOperations: List<String>
    val recordedRequests: List<ContractWireRequest>
    fun withCapabilities(capabilities: ProviderCapabilities): ProtectedPrivateTestDriver
    fun enqueueSuccess(operationId: String)
    fun enqueueNetworkFailure(operationId: String)
    fun enqueueNeverCompleting(operationId: String)
    fun callerSecretArrays(): Pair<CharArray, CharArray>
    fun loginRequest(): LoginRequest
    fun binaryRequest(): BinaryRequest
    fun close()
}

fun interface ProtectedPrivateTestDriverOwner {
    fun createContractTestDriver(): ProtectedPrivateTestDriver
}

class ProtectedPrivateContractHarness(binding: ProviderBinding) : ProviderContractHarness {
    private val driver = (binding as? ProtectedPrivateTestDriverOwner)?.createContractTestDriver()
        ?: error("Protected binding does not expose its contract-test driver")

    override val provider get() = driver.provider
    override val dispatchedRequestCount get() = driver.dispatchedRequestCount
    override val dispatchedOperations get() = driver.dispatchedOperations
    override val recordedRequests get() = driver.recordedRequests
    override fun withCapabilities(capabilities: ProviderCapabilities): ProviderContractHarness =
        DriverBackedPrivateHarness(driver.withCapabilities(capabilities))
    override fun enqueueSuccess(operationId: String) = driver.enqueueSuccess(operationId)
    override fun enqueueNetworkFailure(operationId: String) = driver.enqueueNetworkFailure(operationId)
    override fun enqueueNeverCompleting(operationId: String) = driver.enqueueNeverCompleting(operationId)
    override fun callerSecretArrays() = driver.callerSecretArrays()
    override fun loginRequest() = driver.loginRequest()
    override fun binaryRequest() = driver.binaryRequest()
    override fun close() = driver.close()
}

private class DriverBackedPrivateHarness(
    private val driver: ProtectedPrivateTestDriver,
) : ProviderContractHarness {
    override val provider get() = driver.provider
    override val dispatchedRequestCount get() = driver.dispatchedRequestCount
    override val dispatchedOperations get() = driver.dispatchedOperations
    override val recordedRequests get() = driver.recordedRequests
    override fun withCapabilities(capabilities: ProviderCapabilities): ProviderContractHarness =
        DriverBackedPrivateHarness(driver.withCapabilities(capabilities))
    override fun enqueueSuccess(operationId: String) = driver.enqueueSuccess(operationId)
    override fun enqueueNetworkFailure(operationId: String) = driver.enqueueNetworkFailure(operationId)
    override fun enqueueNeverCompleting(operationId: String) = driver.enqueueNeverCompleting(operationId)
    override fun callerSecretArrays() = driver.callerSecretArrays()
    override fun loginRequest() = driver.loginRequest()
    override fun binaryRequest() = driver.binaryRequest()
    override fun close() = driver.close()
}
```

The `ProviderPlugin` SPI is always present in `:api-contract`; its implementation and service entry are private-graph-only. Task 1 excludes `:api-private-adapter` unless `-Pwenku8Provider=private`, and private selection fails during settings evaluation unless `WENKU8_PRIVATE_ADAPTER_OVERLAY` is present. In an authorized private build, the protected binding implements `ProtectedPrivateTestDriverOwner`; no endpoint, fixture, or selector crosses into the public checkout.

- [ ] **Step 2: Verify the public checkout fails private selection without leaking details**

Run: `.\gradlew.bat help -Pwenku8Provider=private --stacktrace`

Expected when private source is absent: FAIL with `Private provider source is unavailable`; output contains no endpoint, coordinate, filesystem detail below the checked-out `api` directory, or credential.

- [ ] **Step 3: Configure protected source injection without public fallback**

```groovy
def protectedOverlay = providers.environmentVariable('WENKU8_PRIVATE_ADAPTER_OVERLAY')
if (!protectedOverlay.isPresent()) {
    throw new GradleException('Protected private adapter overlay is required in private mode.')
}

android {
    sourceSets {
        main.java.srcDir(protectedOverlay.map { file("${it}/src/main/kotlin") })
        test.java.srcDir(protectedOverlay.map { file("${it}/src/test/kotlin") })
        main.resources.srcDir(protectedOverlay.map { file("${it}/src/main/resources") })
    }
}
```

The protected overlay supplies the implementation that wraps logical private `:api`, private HostPolicy/evidence, and its `META-INF/services/org.mewx.wenku8.api.contract.ProviderPlugin` entry. It remains inside protected CI and cannot replace or weaken a public contract/test/compliance row.

Conditionally package the plugin and compose it through the actual app factory:

```groovy
// app/build.gradle
def selectedProvider = providers.gradleProperty('wenku8Provider').orElse('public')
dependencies {
    implementation project(':api-contract')
    if (selectedProvider.get() == 'private') {
        implementation project(':api-private-adapter')
    }
}
android {
    packaging.resources.merges += 'META-INF/services/org.mewx.wenku8.api.contract.ProviderPlugin'
}
```

```kotlin
class ProviderBindingFactory(
    private val publicFactory: () -> ProviderBinding,
    private val classLoader: ClassLoader,
) {
    fun create(selected: String): ProviderBinding = when (selected) {
        "public" -> {
            require(ServiceLoader.load(ProviderPlugin::class.java, classLoader).none()) { "private-plugin-in-public-graph" }
            publicFactory().also { require(it.providerId == SourceId("public")) }
        }
        "private" -> {
            val plugins = ServiceLoader.load(ProviderPlugin::class.java, classLoader).toList()
            require(plugins.size == 1 && plugins.single().providerId == SourceId("private")) { "private-plugin-count" }
            plugins.single().create().also { require(it.providerId == SourceId("private")) }
        }
        else -> error("unsupported-provider-selection")
    }
}
```

Add consumer rules that keep concrete `ProviderPlugin` implementations and the service descriptor in minified private builds. `ProviderPluginCompositionTest` uses this real factory: public mode asserts zero plugins, an actual `PublicProvider` binding, and unsupported daily check-in with zero dispatch; protected private mode asserts the concrete loaded plugin, actual `private` binding identity, enabled daily check-in, and one real adapter dispatch. Run the same identity instrumentation after installing the approved minified private APK; comparing two synthetic IDs is not sufficient.

- [ ] **Step 4: Run the same suite and create only a redacted bound attestation in protected CI**

Run in protected CI only: `.\gradlew.bat :api-private-adapter:test :api-private-adapter:build -Pwenku8Provider=private -PprivateAttestationNonce=$env:WENKU8_PRIVATE_ATTESTATION_NONCE --stacktrace`

Expected: `BUILD SUCCESSFUL`; the shared suite passes and the emitted attestation contains only schema/tool version, provider ID, variant, source commit, public-base hash, artifact hash, protected policy revision, current authorization window, issue time, `notAfter`, unique run ID/nonce, signing key ID, result, and opaque protected report ID/digest.

- [ ] **Step 5: Verify signature, binding, freshness, replay, key trust, and non-disclosure publicly**

```powershell
param([Parameter(Mandatory=$true)][string]$Attestation, [Parameter(Mandatory=$true)][string]$Artifact)
.\gradlew.bat verifyPrivateAttestation "-PprivateAttestation=$Attestation" "-PprivateArtifact=$Artifact" --stacktrace
if ($LASTEXITCODE -ne 0) { throw 'private attestation verification failed' }
$text = Get-Content -LiteralPath $Attestation -Raw
foreach ($forbidden in @('http://','https://','private coordinate','Cookie','password','username','endpoint')) {
    if ($text.Contains($forbidden, [System.StringComparison]::OrdinalIgnoreCase)) { throw "private attestation disclosure: $forbidden" }
}
```

Negative fixtures must fail for wrong provider/variant/commit/base/artifact/policy, expired/future time, reused run ID/nonce, stale approval, unknown/revoked key, signature mismatch, and schema downgrade.

- [ ] **Step 6: Commit the public adapter shell and verifier**

```powershell
git add api-private-adapter verification/verify-private-provider-attestation.ps1
git commit -m "test(api): gate protected private provider adapter"
```

### Task 27: Assert Provider Identity in Public/Private Debug and Minified Release Variants

**Depends on:** Tasks 21, 25, and 26.

**Files:**
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/di/ProviderBindingFactory.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/provider/ProviderIdentityVerifier.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification/provider-selection.gradle`
- Create: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/provider/ProviderIdentityVerifierTest.kt`

- [ ] **Step 1: Write the failing runtime/build identity test**

```kotlin
package org.mewx.wenku8.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFailsWith
import org.junit.Test
import org.mewx.wenku8.core.model.identity.SourceId

class ProviderIdentityVerifierTest {
    @Test fun publicBuildRequiresPublicBinding() {
        ProviderIdentityVerifier.verify("public", SourceId("public"))
        assertFailsWith<IllegalStateException> { ProviderIdentityVerifier.verify("public", SourceId("private")) }
    }
}
```

- [ ] **Step 2: Run the identity test and confirm RED**

Run: `.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.provider.ProviderIdentityVerifierTest" -Pwenku8Provider=public --stacktrace`

Expected: FAIL because the build identity constant and verifier are missing.

- [ ] **Step 3: Add a redacted build identity and fail-fast verifier**

```groovy
def selectedProvider = providers.gradleProperty('wenku8Provider').orElse('public')

androidComponents {
    onVariants(selector().all()) { variant ->
        variant.buildConfigFields.put(
            'WENKU8_PROVIDER_ID',
            new com.android.build.api.variant.BuildConfigField('String', "\"${selectedProvider.get()}\"", 'Explicit provider selection'),
        )
    }
}
```

```kotlin
package org.mewx.wenku8.provider

import org.mewx.wenku8.core.model.identity.SourceId

object ProviderIdentityVerifier {
    fun verify(expected: String, actual: SourceId) {
        check(expected in setOf("public", "private")) { "Unknown provider build identity" }
        check(actual.value == expected) { "Selected provider binding does not match this build" }
    }
}
```

`DefaultAppContainer` calls `ProviderIdentityVerifier.verify(BuildConfig.WENKU8_PROVIDER_ID, providerBinding.providerId)` before exposing repositories. The exception contains no endpoint or source path.

- [ ] **Step 4: Add exact Gradle selection assertions**

```groovy
tasks.register('verifySelectedProvider') {
    doLast {
        def selected = providers.gradleProperty('wenku8Provider').orElse('public').get()
        if (selected == 'public' && project(':api').projectDir != rootProject.file('api-legacy-bridge')) {
            throw new GradleException('Public provider bridge mapping mismatch')
        }
        if (selected == 'private' && project(':api').projectDir != rootProject.file('api')) {
            throw new GradleException('Private provider mapping mismatch')
        }
        println "PASS provider=${selected}"
    }
}
```

- [ ] **Step 5: Run public debug/channel/minified selection jobs**

Run: `.\gradlew.bat verifySelectedProvider :app:testAlphaDebugUnitTest :app:assembleAlphaDebug :app:assembleBaiduDebug :app:assemblePlaystoreDebug -Pwenku8Provider=public --stacktrace`

Expected: `PASS provider=public` and `BUILD SUCCESSFUL`.

When and only when the relevant release channels are currently `ACCEPTED`, run:

` .\gradlew.bat :app:assembleAlphaRelease :app:assembleBaiduRelease :app:assemblePlaystoreRelease verifySelectedProvider -Pwenku8Provider=public --stacktrace`

Expected: `BUILD SUCCESSFUL`; R8 retains the frozen bridge ABI and each packaged identity is `public`. With an unknown/expired/rejected channel, configuration must fail before assembly rather than silently substituting fixture or disabled behavior.

- [ ] **Step 6: Run protected private debug/minified identity jobs**

Run only in protected CI with private source/overlay and current scopes:

`.\gradlew.bat verifySelectedProvider :app:assembleAlphaDebug :app:assembleAlphaRelease :api-private-adapter:test -Pwenku8Provider=private --stacktrace`

Expected: `PASS provider=private`, `BUILD SUCCESSFUL`, shared contracts pass, and the protected job emits a fresh bound attestation.

- [ ] **Step 7: Commit provider identity checks**

```powershell
git add app/build.gradle app/src/main app/src/test verification/provider-selection.gradle
git commit -m "test(build): assert selected provider identity"
```

### Task 28: Add Environment-Only Interactive Live Smoke and Mutation Gates

**Depends on:** Task 21 and currently accepted automated-live/account scopes.

**Files:**
- Modify: `studio-android/LightNovelLibrary/api-public/build.gradle`
- Create: `studio-android/LightNovelLibrary/api-public/src/liveTest/kotlin/org/mewx/wenku8/api/publicprovider/live/LiveReadOnlySmoke.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/liveTest/kotlin/org/mewx/wenku8/api/publicprovider/live/LiveMutationPolicy.kt`
- Create: `studio-android/LightNovelLibrary/api-public/src/test/kotlin/org/mewx/wenku8/api/publicprovider/live/LiveMutationPolicyTest.kt`

This task is skipped, not weakened, while the live/account authorization gate is not `ACCEPTED`. The deterministic suite remains runnable.

- [ ] **Step 1: Write failing environment and mutation policy tests**

```kotlin
package org.mewx.wenku8.api.publicprovider.live

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveMutationPolicyTest {
    @Test fun defaultPolicyIsReadOnly() {
        val policy = LiveMutationPolicy.from(emptyMap(), emptyArray())
        assertFalse(policy.reversibleAllowed)
        assertFalse(policy.persistentAllowed)
    }

    @Test fun reversibleNeedsExactEnvironmentGateAndFixtureNovel() {
        val denied = LiveMutationPolicy.from(mapOf("WENKU8_LIVE_REVERSIBLE_MUTATIONS" to "true"), emptyArray())
        assertFalse(denied.reversibleAllowed)
        val allowed = LiveMutationPolicy.from(
            mapOf("WENKU8_LIVE_REVERSIBLE_MUTATIONS" to "true", "WENKU8_LIVE_FIXTURE_NOVEL_ID" to "synthetic-fixture-id"),
            emptyArray(),
        )
        assertTrue(allowed.reversibleAllowed)
    }

    @Test fun persistentNeedsGateAndPerRunInteractiveConfirmation() {
        val policy = LiveMutationPolicy.from(mapOf("WENKU8_LIVE_PERSISTENT_MUTATIONS" to "true"), arrayOf("--confirmation=PERSIST-run-nonce"))
        assertEquals("PERSIST-run-nonce", policy.requiredInteractiveConfirmation)
        assertFalse(policy.persistentAllowedUntilConfirmed)
    }
}
```

- [ ] **Step 2: Run policy tests and confirm RED**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.live.LiveMutationPolicyTest" --stacktrace`

Expected: FAIL because live policy is missing.

- [ ] **Step 3: Define an opt-in `liveTest` source set and same-process JavaExec**

```groovy
sourceSets {
    liveTest {
        kotlin.srcDir 'src/liveTest/kotlin'
        compileClasspath += sourceSets.main.output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
}

configurations {
    liveTestImplementation.extendsFrom testImplementation
    liveTestRuntimeOnly.extendsFrom testRuntimeOnly
}

tasks.register('liveReadOnlySmoke', JavaExec) {
    group = 'verification'
    description = 'Opt-in interactive read-only Wenku8 smoke; never runs in deterministic CI.'
    classpath = sourceSets.liveTest.runtimeClasspath
    mainClass = 'org.mewx.wenku8.api.publicprovider.live.LiveReadOnlySmokeKt'
    standardInput = System.in
    doFirst {
        if (System.getenv('WENKU8_LIVE_USERNAME') == null || System.getenv('WENKU8_LIVE_PASSWORD') == null) {
            throw new GradleException('WENKU8_LIVE_USERNAME and WENKU8_LIVE_PASSWORD environment variables are required.')
        }
    }
}
```

The task also depends on the Phase 0 `verifyAcceptedLiveScope` task. It has no Gradle-property, system-property, file, keychain export, or command-line credential fallback.

- [ ] **Step 4: Implement read-only login/profile/bookshelf/logout in one process**

```kotlin
package org.mewx.wenku8.api.publicprovider.live

import java.nio.file.Files
import java.nio.file.Path

suspend fun main(args: Array<String>) {
    val username = checkNotNull(System.getenv("WENKU8_LIVE_USERNAME"))
    val password = checkNotNull(System.getenv("WENKU8_LIVE_PASSWORD")).toCharArray()
    val console = checkNotNull(System.console()) { "Interactive console is required for captcha entry" }
    val provider = LiveProviderFactory.createRateLimitedSingleThreaded()
    var captcha = CharArray(0)
    try {
        val challenge = provider.account.beginLogin().requireSuccess("begin-login")
        val output = Path.of("build", "live-smoke", "captcha-image")
        Files.createDirectories(output.parent)
        Files.write(output, challenge.image.bytes)
        println("LIVE captcha-image=${output.toAbsolutePath()}")
        captcha = console.readPassword("Captcha: ")
        provider.account.login(LoginRequest(challenge.attemptId, username, password, captcha)).requireSuccess("login")
        provider.account.validateSession().requireSuccess("validate-session")
        provider.account.profile().requireSuccess("profile")
        provider.account.bookshelf().requireSuccess("bookshelf-read")
        println("LIVE PASS read-only operations=login,validate-session,profile,bookshelf-read,logout")
    } finally {
        runCatching { provider.account.logout() }
        password.fill('\u0000')
        captcha.fill('\u0000')
    }
}
```

`requireSuccess` prints only the bounded operation code and typed failure class. It never prints exception messages, URLs, username, profile, bookshelf, response bodies, Cookies, captcha, or trace correlation data.

- [ ] **Step 5: Implement reversible and persistent mutation gates**

```kotlin
data class LiveMutationPolicy(
    val reversibleAllowed: Boolean,
    val fixtureNovelId: String?,
    val requiredInteractiveConfirmation: String?,
) {
    val persistentAllowedUntilConfirmed = false

    companion object {
        fun from(environment: Map<String, String>, args: Array<String>): LiveMutationPolicy {
            val fixture = environment["WENKU8_LIVE_FIXTURE_NOVEL_ID"]?.takeIf(String::isNotBlank)
            val reversible = environment["WENKU8_LIVE_REVERSIBLE_MUTATIONS"] == "true" && fixture != null
            val persistentGate = environment["WENKU8_LIVE_PERSISTENT_MUTATIONS"] == "true"
            val confirmation = args.singleOrNull { it.startsWith("--confirmation=PERSIST-") }?.substringAfter('=')?.takeIf { persistentGate }
            return LiveMutationPolicy(reversible, fixture, confirmation)
        }
    }

    fun confirmPersistent(console: java.io.Console): Boolean {
        val expected = requiredInteractiveConfirmation ?: return false
        val entered = console.readLine("Type %s to authorize this run: ", expected)
        return entered == expected
    }
}
```

The reversible bookshelf smoke records initial membership, performs one add/remove on the dedicated fixture novel, and restores the initial state in `finally`; a restoration failure is a failing run. Check-in, recommendation, review create, and reply require both the environment gate and exact one-run console confirmation. No persistent mutation runs from CI or a scheduled job.

- [ ] **Step 6: Run deterministic policy tests**

Run: `.\gradlew.bat :api-public:test --tests "org.mewx.wenku8.api.publicprovider.live.LiveMutationPolicyTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; default is read-only, reversible requires both gate/fixture, and persistent remains locked before console confirmation.

- [ ] **Step 7: Run the read-only smoke only when all live gates are accepted**

Run: `.\gradlew.bat :api-public:liveReadOnlySmoke --no-daemon --console=plain -Pwenku8Provider=public`

Expected after interactive captcha: `LIVE PASS read-only operations=login,validate-session,profile,bookshelf-read,logout`. The command line contains no credential values.

- [ ] **Step 8: Scan live outputs and commit only source, never generated captcha/output**

Run: `rg -n -i "password|cookie|captcha|username|<html|bookshelf" api-public/build/live-smoke api-public/build/reports 2>$null`

Expected: no credential/session/body values; the only allowed match is the bounded label `captcha-image` in console text, not image content in a report. Delete local smoke output after verification; do not stage it.

```powershell
git add api-public/build.gradle api-public/src/liveTest api-public/src/test
git commit -m "test(api): add gated live provider smoke"
```

### Task 29: Prove Session/Credential Backup Exclusion and Process-Death Recovery

**Depends on:** Tasks 14, 15, and 22.

**Files:**
- Modify: `studio-android/LightNovelLibrary/app/src/main/res/xml/backup_rules.xml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/res/xml/data_extraction_rules.xml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/session/SessionBackupSeedTest.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/session/SessionBackupVerifyTest.kt`
- Create: `studio-android/LightNovelLibrary/verification/session-backup-restore.ps1`

- [ ] **Step 1: Write failing backup-boundary instrumentation tests**

```kotlin
class SessionBackupSeedTest {
    @Test fun seedSyntheticSessionAndTransientMigrationState() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val container = (context as AppContainerOwner).appContainer
        container.sessionStore.replace(SyntheticSession.record(cookieValue = "synthetic-cookie-never-export"))
        SyntheticLegacyCredential.seedPasswordBearingCert(context, "synthetic-password-never-export")
        SyntheticCredentialJournal.seed(context, MigrationState.SESSION_COMMITTED)
        assertTrue(container.sessionStore.load(SourceId("public")) != null)
    }
}

class SessionBackupVerifyTest {
    @Test fun restoredApplicationIsSignedOutAndHasNoCredentialTransientState() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val container = (context as AppContainerOwner).appContainer
        assertNull(container.sessionStore.load(SourceId("public")))
        assertFalse(SyntheticLegacyCredential.exists(context))
        assertFalse(SyntheticCredentialJournal.exists(context))
    }
}
```

- [ ] **Step 2: Run verify test without backup/restore and confirm RED**

Run: `.\gradlew.bat :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.session.SessionBackupSeedTest,org.mewx.wenku8.session.SessionBackupVerifyTest --stacktrace`

Expected: FAIL because verify runs immediately after seed and the seeded session exists.

- [ ] **Step 3: Exclude every session/credential physical path from cloud and device transfer**

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="file" path="cert.wk8" />
    <exclude domain="file" path="wenku8-session/" />
    <exclude domain="file" path="wenku8-migration/credential/" />
    <exclude domain="external" path="cert.wk8" />
</full-backup-content>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="file" path="cert.wk8" />
        <exclude domain="file" path="wenku8-session/" />
        <exclude domain="file" path="wenku8-migration/credential/" />
        <exclude domain="external" path="cert.wk8" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="file" path="cert.wk8" />
        <exclude domain="file" path="wenku8-session/" />
        <exclude domain="file" path="wenku8-migration/credential/" />
        <exclude domain="external" path="cert.wk8" />
    </device-transfer>
</data-extraction-rules>
```

The production SessionStore and credential journal remain physically under `noBackupFilesDir`; the explicit file exclusions defend older/fallback paths and future path drift. The manifest points `android:fullBackupContent` and `android:dataExtractionRules` at these files.

- [ ] **Step 4: Add the exact emulator backup/restore journey**

```powershell
$ErrorActionPreference = 'Stop'
.\gradlew.bat :app:installAlphaDebug -Pwenku8Provider=public --stacktrace
adb shell am instrument -w -e class org.mewx.wenku8.session.SessionBackupSeedTest org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
if ($LASTEXITCODE -ne 0) { throw 'seed failed' }
adb shell bmgr enable true | Out-Null
$backup = adb shell bmgr backupnow org.mewx.wenku8 | Out-String
if (-not $backup.Contains('Backup finished with result: Success')) { throw $backup }
adb shell pm clear org.mewx.wenku8 | Out-Null
$sets = adb shell bmgr list sets | Out-String
$token = [regex]::Match($sets, '(?m)^\s*([0-9a-f]+)\s+:').Groups[1].Value
if (-not $token) { throw 'No backup set token' }
$restore = adb shell bmgr restore $token org.mewx.wenku8 | Out-String
if (-not $restore.Contains('restoreFinished')) { throw $restore }
adb shell am instrument -w -e class org.mewx.wenku8.session.SessionBackupVerifyTest org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
if ($LASTEXITCODE -ne 0) { throw 'restore verification failed' }
Write-Output 'PASS restored signed out'
```

- [ ] **Step 5: Run process-death interruption and restored-signed-out tests**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\session-backup-restore.ps1`

Expected: `PASS restored signed out`; restored SessionStore, Cookies, `cert.wk8`, and transient journal are absent.

Run: `.\gradlew.bat :core:storage:testDebugUnitTest --tests "*.CredentialMigrationCoordinatorTest" --tests "*.EncryptedSessionStoreTest" --stacktrace`

Expected: `BUILD SUCCESSFUL`; process death before/after session persistence and scrubbing converges, missing/invalid Keystore stays signed out, and no password recovery is attempted.

- [ ] **Step 6: Commit backup/session evidence**

```powershell
git add app/src/main app/src/androidTest verification/session-backup-restore.ps1
git commit -m "test(session): prove credential backup exclusion"
```

### Task 30: Map and Run the Complete Phase 2 Verification Gate

**Depends on:** Tasks 24 through 29.

**Files:**
- Create: `docs/verification/provider-phase2-matrix.yaml`
- Modify: `docs/verification/modernization-matrix.yaml`
- Create: `studio-android/LightNovelLibrary/verification/verify-phase2-provider.ps1`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/compliance/Phase2Gate.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/compliance/Phase2EvidenceCollector.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/compliance/Phase2GateTest.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/resources/phase2/incomplete-matrix.yaml`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`

- [ ] **Step 1: Register the compiled gate and an intentionally incomplete RED fixture**

```yaml
schemaVersion: 1
phase: 2
requirements:
  - id: provider-gradle-graph
    task: verification/provider-project-graph.ps1
```

Register `collectPhase2Evidence` and `phase2Gate` now, before running RED. The collector output stays under `build/`; it is never committed because it must describe the checked-out HEAD on the machine that runs the gate.

```groovy
def phase2Matrix = providers.gradleProperty('phase2Matrix').orElse(
    new File(rootProject.projectDir, '../../docs/verification/provider-phase2-matrix.yaml').absolutePath
)
def phase2Results = layout.buildDirectory.file('reports/verification/provider-phase2-results.yaml')

def deterministicPhase2Tasks = [
    ':api-contract:test', ':core:network:test', ':api-public:test', ':api-contract-tests:build',
    ':core:storage:testDebugUnitTest', ':core:data:testDebugUnitTest', ':api:testDebugUnitTest',
    ':app:testAlphaDebugUnitTest', ':app:lintAlphaDebug', ':verifyArchitecture', ':verifyPhase0Coverage',
]

tasks.register('collectPhase2Evidence', JavaExec) {
    group = 'verification'
    dependsOn deterministicPhase2Tasks
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'collectPhase2Evidence', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath,
        phase2Matrix.get(), phase2Results.get().asFile.absolutePath
}

tasks.register('phase2Gate', JavaExec) {
    group = 'verification'
    dependsOn tasks.named('collectPhase2Evidence')
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'phase2Gate', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath,
        phase2Matrix.get(), phase2Results.get().asFile.absolutePath
}
```

Add the two dispatcher branches at the same time. They parse exactly four path arguments after the command and fail on extras; `collectPhase2Evidence` calls `Phase2EvidenceCollector.collect`, and `phase2Gate` calls `Phase2Gate.verify`.

- [ ] **Step 2: Run the matrix verifier and confirm RED**

Run: `.\gradlew.bat :verification-tools:test --tests "*.Phase2GateTest.incompleteMatrixFailsForMissingRequiredIds" :verification-tools:phase2Gate -Pwenku8Provider=public -Pphase2Matrix=verification-tools/src/test/resources/phase2/incomplete-matrix.yaml --stacktrace`

Expected: the unit test passes by observing `matrix-id-mismatch`; the Gradle invocation reaches the registered Java entry and fails with the same `matrix-id-mismatch` rule. It must not contain `Task 'phase2Gate' not found`, `ClassNotFoundException`, or an argument-dispatch error.

- [ ] **Step 3: Add exact requirement-to-evidence rows**

```yaml
schemaVersion: 2
phase: 2
resultSchemaVersion: 1
requirements:
  - id: provider-gradle-graph
    task: provider-project-graph
    testId: provider-project-graph
    provider: public
    variant: all
    fixture: not-applicable
    threshold: unique-project-directory-count-equals-project-count
    report: build/reports/verification/provider-project-graph.txt
  - id: typed-contracts
    task: :api-contract:test
    testId: ContractCoreTest,ContractModelsTest
    provider: public
    variant: jvm
    fixture: not-applicable
    threshold: failures=0
    report: api-contract/build/reports/tests/test/index.html
  - id: unsupported-zero-network
    task: :api-public:test --tests *PublicProviderContractTest
    testId: everyAbsentCapabilityReturnsMatchingUnsupportedAndDispatchesZeroRequests
    provider: public
    variant: jvm
    fixture: api-contract-tests/src/testFixtures
    threshold: dispatchedRequestCount=0
    report: api-public/build/test-results/test/TEST-org.mewx.wenku8.api.publicprovider.PublicProviderContractTest.xml
  - id: https-host-policy
    task: :core:network:test --tests *Wenku8HostPolicyTest
    testId: Wenku8HostPolicyTest,PolicyRedirectInterceptorTest
    provider: public
    variant: jvm
    fixture: not-applicable
    threshold: forbiddenDispatchCount=0
    report: core/network/build/test-results/test/TEST-org.mewx.wenku8.core.network.policy.Wenku8HostPolicyTest.xml
  - id: gbk-codec
    task: :core:network:test --tests *GbkCodecTest
    testId: GbkCodecTest
    provider: public
    variant: jvm
    fixture: not-applicable
    threshold: failures=0
    report: core/network/build/test-results/test/TEST-org.mewx.wenku8.core.network.codec.GbkCodecTest.xml
  - id: catalog-home-browse-tags-search
    task: :api-public:test --tests *CatalogListParserTest --tests *PublicCatalogSourceTest
    testId: CatalogListParserTest,PublicCatalogSourceTest
    provider: public
    variant: jvm
    fixture: api-public/src/test/resources/fixtures/catalog
    threshold: parserFailures=0
    report: api-public/build/reports/tests/test/index.html
  - id: detail-catalog-chapter-binary
    task: :api-public:test --tests *NovelParserTest --tests *ChapterParserTest --tests *PublicBinarySourceTest
    testId: NovelParserTest,ChapterParserTest,PublicBinarySourceTest
    provider: public
    variant: jvm
    fixture: api-public/src/test/resources/fixtures/novel,api-public/src/test/resources/fixtures/reader
    threshold: parserFailures=0
    report: api-public/build/reports/tests/test/index.html
  - id: captcha-cookie-isolation
    task: :api-public:test --tests *PublicLoginControllerTest --tests *AttemptCookieJarTest
    testId: PublicLoginControllerTest,AttemptCookieJarTest
    provider: public
    variant: jvm
    fixture: api-public/src/test/resources/fixtures/account
    threshold: crossAttemptCookieCount=0
    report: api-public/build/reports/tests/test/index.html
  - id: encrypted-session-epoch
    task: :core:storage:testDebugUnitTest --tests *EncryptedSessionStoreTest
    testId: SessionRecordCodecTest,EncryptedSessionStoreTest,AndroidKeystoreSessionCipherTest
    provider: public
    variant: debug
    fixture: core/storage/src/test/resources/session
    threshold: plaintextSecretOccurrences=0
    report: core/storage/build/reports/tests/testDebugUnitTest/index.html
  - id: credential-migration
    task: :core:storage:testDebugUnitTest --tests *CredentialMigrationCoordinatorTest
    testId: CredentialMigrationCoordinatorTest,LegacyCredentialWriterBarrierTest
    provider: public
    variant: alphaDebug
    fixture: core/storage/src/test/resources/legacy/credentials
    threshold: passwordDecodeCount=0
    report: core/storage/build/reports/tests/testDebugUnitTest/index.html
  - id: profile-avatar-logout
    task: :api-public:test --tests *PublicAccountSourceTest
    testId: PublicAccountSourceTest
    provider: public
    variant: jvm
    fixture: api-public/src/test/resources/fixtures/account/profile-normal.html
    threshold: staleEpochWrites=0
    report: api-public/build/test-results/test/TEST-org.mewx.wenku8.api.publicprovider.PublicAccountSourceTest.xml
  - id: bookshelf-bid-mutations
    task: :api-public:test --tests *PublicBookshelfSourceTest
    testId: PublicBookshelfSourceTest
    provider: public
    variant: jvm
    fixture: api-public/src/test/resources/fixtures/account/bookshelf-normal.html
    threshold: mutationRetries=0
    report: api-public/build/test-results/test/TEST-org.mewx.wenku8.api.publicprovider.PublicBookshelfSourceTest.xml
  - id: recommendation
    task: :api-public:test --tests *RecommendationAndCheckInTest
    testId: recommendationUsesOneNonRetriedRequestAndParsesAcceptedOrRejected
    provider: public
    variant: jvm
    fixture: api-public/src/test/resources/fixtures/account/recommend-accepted.html,api-public/src/test/resources/fixtures/account/recommend-rejected.html
    threshold: requestCountPerMutation=1
    report: api-public/build/test-results/test/TEST-org.mewx.wenku8.api.publicprovider.RecommendationAndCheckInTest.xml
  - id: check-in-disabled
    task: :api-public:test --tests *RecommendationAndCheckInTest
    testId: checkInIsDisabledAndDispatchesNothingWithoutAcceptedHttpsEvidence
    provider: public
    variant: jvm
    fixture: docs/api-evidence/operations/daily-check-in.yaml
    threshold: dispatchedRequestCount=0
    report: api-public/build/test-results/test/TEST-org.mewx.wenku8.api.publicprovider.RecommendationAndCheckInTest.xml
  - id: review-read-create-reply
    task: :api-public:test --tests *PublicCommunitySourceTest
    testId: PublicCommunitySourceTest
    provider: public
    variant: jvm
    fixture: api-public/src/test/resources/fixtures/community
    threshold: mutationRetries=0
    report: api-public/build/test-results/test/TEST-org.mewx.wenku8.api.publicprovider.PublicCommunitySourceTest.xml
  - id: cache-single-flight-invalidation
    task: :core:data:testDebugUnitTest --tests *CachedProviderGatewayTest --tests *MutationInvalidatorTest
    testId: CachedProviderGatewayTest,MutationInvalidatorTest
    provider: public
    variant: debug
    fixture: core/data/src/main/resources/mutation-invalidation-matrix.yaml
    threshold: identicalConcurrentRemoteDispatches=1
    report: core/data/build/reports/tests/testDebugUnitTest/index.html
  - id: legacy-abi-bridge
    task: legacy-abi-bridge-suite
    testId: org.mewx.wenku8.api.LegacyBridgeContractTest,org.mewx.wenku8.api.PublicApiStubContractTest
    provider: public
    variant: alphaDebug
    fixture: docs/verification/api-abi-manifest.yaml
    threshold: unsupportedOperationExceptionCount=0
    report: api-legacy-bridge/build/reports/tests/testDebugUnitTest/index.html
  - id: private-adapter-attestation
    task: :api-private-adapter:test -Pwenku8Provider=private
    testId: PrivateProviderContractTest,PrivateAttestationEmissionTest
    provider: private
    variant: alphaDebug,alphaRelease
    fixture: docs/compliance/private-attestation-trust.yaml
    threshold: freshBoundAttestation=PASS
    prerequisite: protected-private-overlay-and-current-accepted-scopes
    report: build/verified-external/private-provider-attestation.yaml
  - id: live-read-only-smoke
    task: :api-public:liveReadOnlySmoke --no-daemon --console=plain -Pwenku8Provider=public
    testId: LiveReadOnlySmoke
    provider: public
    variant: controlled-live
    fixture: environment-only-WENKU8_LIVE_USERNAME-and-WENKU8_LIVE_PASSWORD
    threshold: readOnlyOperations=5
    prerequisite: current-accepted-live-account-scope-and-interactive-captcha
    report: api-public/build/reports/live-smoke/read-only-summary.txt
  - id: backup-restored-signed-out
    task: session-backup-restore
    testId: SessionBackupSeedTest,SessionBackupVerifyTest
    provider: public
    variant: alphaDebug
    device: controlled-emulator-api-34
    fixture: synthetic-session-and-password-bearing-cert
    threshold: restoredAuthenticatedRecords=0
    report: app/build/reports/androidTests/connected/alpha/debug/index.html
  - id: deterministic-reader-journey
    task: :api-public:test --tests *PublicProviderJourneyTest
    testId: searchDetailCatalogChapterImageJourneyCompletesAgainstTlsFixtures,captchaProfileBookshelfLogoutJourneyUsesOneAttemptAndEndsSignedOut
    provider: public
    variant: jvm
    fixture: api-public/src/test/resources/fixtures/journey
    threshold: externalNetworkDispatches=0
    report: api-public/build/test-results/test/TEST-org.mewx.wenku8.api.publicprovider.PublicProviderJourneyTest.xml
```

The static matrix is a requirement registry, not proof. `Phase2EvidenceCollector` writes the current run to `verification-tools/build/reports/verification/provider-phase2-results.yaml` with this exact closed shape for every registry row:

```yaml
schemaVersion: 1
phase: 2
headCommit: 40-lowercase-hex-from-live-git-rev-parse-HEAD
headCommitTime: RFC3339-UTC-from-live-git-show
generatedAt: RFC3339-UTC
matrixNormalizedSha256: 64-lowercase-hex
requirements:
  - id: provider-gradle-graph
    status: PASS
    commandId: provider-gradle-graph
    testIds: [provider-project-graph]
    passedTestIds: [provider-project-graph]
    failedTestIds: []
    skippedTestIds: []
    reportPath: build/reports/verification/provider-project-graph.txt
    reportNormalizedSha256: 64-lowercase-hex
    fixturePaths: []
    fixtureTreeSha256: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
    prerequisiteStatus: NOT_REQUIRED
    prerequisiteEvidencePath: null
    prerequisiteEvidenceSha256: null
```

The schema labels above are emitted values, never committed placeholders. For each row the collector performs all of the following:

- Resolve only the exact command ID from a checked-in `when` table; free-form matrix text is not executed. The Gradle task dependencies registered in Step 1 produce every deterministic report first.
- Run `git rev-parse HEAD` and `git show -s --format=%cI HEAD`; reject a dirty tracked worktree and a report older than the current commit time.
- Parse JUnit XML or the operation-specific signed report, require every matrix `testId` to be present and passed, and record exact passed/failed/skipped IDs and counts. A missing report or zero executed named tests is `FAIL`, never PASS.
- Hash text reports after UTF-8 decoding, CRLF/CR to LF normalization, and removal of trailing horizontal whitespace per line. Hash binary reports as raw bytes.
- Expand each comma-separated fixture entry to a normalized repository path, walk directories without following links, sort forward-slash relative paths ordinally, and hash `relativePath + TAB + rawFileSha256 + LF`. The empty tree has the standard SHA-256 shown above.
- Resolve prerequisites through their checked-in Phase 0 authorization file or a locally verified signed external attestation. `UNKNOWN`, `EXPIRED`, `REJECTED`, missing, or signature-invalid becomes `BLOCKED`.
- For private/live/backup/release evidence, consume only files under `build/verified-external/` produced by their signature verifier. A `protected-ci://` URI or prose assertion is not evidence.

`Phase2Gate.verify` reparses both files independently and recomputes every value. It requires exact ID equality, `headCommit == live git rev-parse HEAD`, matrix hash equality, `status == PASS`, empty failed/skipped sets for required tests, exact normalized report/tree hashes, and `prerequisiteStatus in {ACCEPTED, NOT_REQUIRED}`. It rejects `BLOCKED` as well as `FAIL`; the deterministic subset can be developed separately, but `phase2Gate` is the actual exit gate.

Add negative tests for: incomplete/duplicate/extra IDs, stale HEAD, dirty tracked worktree, missing/old/tampered report, report hash mismatch, missing/failed/skipped/zero-count test ID, fixture path escape/symlink/tamper/tree hash mismatch, unknown command ID, missing prerequisite, every non-accepted prerequisite state, unsigned external evidence, `BLOCKED`, and schema downgrade. Each test asserts one stable rule string.

Change the private result row to a local verified artifact:

```yaml
    report: build/verified-external/private-provider-attestation.yaml
```

`verify-phase2-provider.ps1` is only a cross-platform launcher: it selects `pwsh` when available, runs `:verification-tools:collectPhase2Evidence` and `:verification-tools:phase2Gate`, propagates the exit code, and contains no second YAML parser or substring-based acceptance logic.

Register the app-owned terminal gate explicitly:

```groovy
// app/build.gradle
tasks.register('phase2ExitGate') {
    group = 'verification'
    description = 'Runs all local Phase 2 checks and verifies current external evidence.'
    dependsOn ':verification-tools:phase2Gate',
        'testAlphaDebugUnitTest', 'lintAlphaDebug', 'assembleAlphaDebug',
        'assembleBaiduDebug', 'assemblePlaystoreDebug'
}
```

The task exists in every public graph. It passes only when the strict gate also finds locally verified PASS evidence for required private/live/backup/release prerequisites; otherwise it fails with the row ID and `BLOCKED`.

- [ ] **Step 4: Run all deterministic Phase 2 JVM and Android unit suites**

Run:

```powershell
.\gradlew.bat `
  :api-contract:test `
  :core:network:test `
  :api-public:test `
  :api-contract-tests:build `
  :core:storage:testDebugUnitTest `
  :core:data:testDebugUnitTest `
  :api:testDebugUnitTest `
  :app:testAlphaDebugUnitTest `
  -Pwenku8Provider=public `
  --stacktrace
```

Expected: `BUILD SUCCESSFUL`; no skipped contract/parser/session/cache/bridge test except explicitly live/protected suites.

- [ ] **Step 5: Run instrumentation, lint, architecture, coverage, and public debug builds**

Run:

```powershell
.\gradlew.bat `
  :core:storage:connectedDebugAndroidTest `
  :app:connectedAlphaDebugAndroidTest `
  :app:lintAlphaDebug `
  verifyArchitecture `
  verifyPhase0Coverage `
  :app:assembleAlphaDebug `
  :app:assembleBaiduDebug `
  :app:assemblePlaystoreDebug `
  -Pwenku8Provider=public `
  --stacktrace
```

Expected: `BUILD SUCCESSFUL`; parser/storage/migration line coverage is at least 90% and branch coverage at least 80%; provider/repository line coverage is at least 80% and branch coverage at least 70%; overall production logic remains at least 70%/60%.

- [ ] **Step 6: Run static security, ABI, egress, and unfinished-marker scans**

```powershell
$production = @('api-contract/src/main','api-public/src/main','api-legacy-bridge/src/main','core/network/src/main','core/storage/src/main','core/data/src/main','app/src/main')
$badThrow = rg -n "UnsupportedOperationException|runBlocking" $production 2>$null
if ($badThrow) { throw $badThrow }
$badCleartext = rg -n -i "app\.wenku8\.com|cleartextTrafficPermitted=.true.|http://" api-public/src/main core/network/src/main api-legacy-bridge/src/main 2>$null
if ($badCleartext) { throw $badCleartext }
$badSecrets = rg -n -i "WENKU8_LIVE_(USERNAME|PASSWORD)\s*=|password\s*[:=]\s*['\"][^'\"]+|Cookie:\s*[^<]" . -g '!build/**' -g '!docs/**' 2>$null
if ($badSecrets) { throw $badSecrets }
$badLogs = rg -n "Log\.(v|d|i|w|e)\(|println\(" api-public/src/main core/network/src/main core/storage/src/main core/data/src/main 2>$null
if ($badLogs) { throw $badLogs }
Write-Output 'PASS phase2 static security scans'
```

Expected: `PASS phase2 static security scans`. The live harness may print only its bounded operation labels; production provider modules print nothing.

Run: `.\gradlew.bat :verification-tools:verifyOutboundManifest :verification-tools:verifySensitiveSource -Pwenku8Provider=public --stacktrace`

Run: `.\gradlew.bat :api:testDebugUnitTest --tests "org.mewx.wenku8.api.LegacyBridgeContractTest" -Pwenku8Provider=public --stacktrace`

Run: `.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.api.PublicApiStubContractTest" -Pwenku8Provider=public --stacktrace`

Expected: `BUILD SUCCESSFUL`; only manifest-approved HTTPS origins are possible, raw sensitive logging is absent, and the legacy ABI matches its frozen baseline.

- [ ] **Step 7: Run conditional live, private, backup, and minified-release gates**

When live/account scopes are accepted, run Task 28 read-only smoke. When private source/overlay/scopes are available, run Task 26 protected suite and verify its fresh attestation. Run Task 29 backup/restore on the controlled emulator. When each distribution channel is accepted, assemble/install/launch its externally signed minified release and retain APK/AAB hash, mapping hash, signing certificate digest, `apksigner verify`, provider identity, R8 ABI, and test reports.

Expected: each required gate is PASS. During development an unavailable or non-accepted prerequisite is reported as `BLOCKED`, but that state makes both `phase2Gate` and `phase2ExitGate` fail and therefore forbids a Phase 2 completion claim. Deterministic fixtures can never convert it to PASS.

- [ ] **Step 8: Run independent reviews before declaring Phase 2 complete**

Dispatch three fresh reviewers:

1. Architecture/API reviewer: module graph, contract completeness, capability mapping, cancellation, cache ownership, legacy bridge, private adapter.
2. Security/clean-room reviewer: HTTPS policy, redirects, evidence provenance/role separation, secret lifetime, redaction, live gates, cleartext check-in absence.
3. Migration/testing reviewer: SessionStore, epoch, credential journal/scrub, backup/restore, rollback signed-out behavior, test/matrix coverage.

Expected: all reviewers return `PASS`; no Critical or Important finding remains open. Resolve findings with new RED/GREEN commits and rerun affected gates.

- [ ] **Step 9: Commit the static registry, then verify the committed HEAD**

Run: `git diff --check`

Expected: no output.

```powershell
git add ..\..\docs\verification/provider-phase2-matrix.yaml ..\..\docs\verification/modernization-matrix.yaml verification/verify-phase2-provider.ps1 verification-tools app/build.gradle
git commit -m "docs(verification): record phase 2 provider evidence"
```

After that commit, and before changing any tracked file, run:

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\verify-phase2-provider.ps1`

Run: `.\gradlew.bat :app:phase2ExitGate -Pwenku8Provider=public --stacktrace`

Expected: both commands report the same current 40-character HEAD, every row is `PASS`, every named test is present and passed, normalized report and fixture-tree hashes match, no prerequisite is `BLOCKED`, and the build ends in `BUILD SUCCESSFUL`. The generated result envelope remains untracked under `verification-tools/build/reports/verification/`.

## Phase 2 Completion Gate

Phase 2 is complete only when current evidence proves all of the following:

- Public fixture journeys complete search -> detail -> catalog -> chapter -> image and captcha -> session -> profile -> bookshelf -> logout.
- Every typed operation is implemented when its advertised capability is enabled; every absent capability returns `Unsupported(theCapability)` with zero network activity.
- Public daily check-in remains disabled unless its own accepted HTTPS evidence record exists; no cleartext exception exists.
- Captcha prewarm/captcha/login share one isolated attempt client and jar; concurrent/subsequent attempts share nothing; secrets clear on every terminal path.
- SessionStore is encrypted, no-backup, epoch-partitioned, corruption-fail-closed, and restored devices start signed out.
- Legacy password-bearing `cert.wk8` is never imported, is scrubbed only after successful new authentication, and interruption/rollback evidence converges safely.
- Home, latest, ranking, category, completed, tags, tag results, search, detail, catalog, chapter, image, profile/avatar, bookshelf, recommendation, reviews, review creation, and replies pass parser/network/contracts against independently authored fixtures.
- Cache TTL, stale disclosure, parser revision, single-flight, targeted mutation invalidation, logout/account-switch cancellation, and old-epoch write rejection pass.
- The public logical `:api` bridge preserves frozen ABI, contains no normal-action `UnsupportedOperationException`, and never uses main-thread `runBlocking`.
- Public/provider identity passes debug and approved minified-release variants. Protected private adapter passes the same shared suite and supplies a fresh non-disclosing bound attestation.
- Live read-only verification, endpoint publication, and release claims are made only for scopes currently `ACCEPTED`; fixture-only work is labeled as such.
- No credential, Cookie, captcha, raw user/content body, private endpoint, or protected overlay detail appears in source, fixture, artifact, log, screenshot, command, or report.
- The Phase 2 verification matrix points each requirement to an exact task/test/report/hash/variant/device/commit, and no Critical or Important independent review finding remains.
