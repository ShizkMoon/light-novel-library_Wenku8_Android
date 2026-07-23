# Wenku8 Phase 0 Baseline Stability and Contract Freeze Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Freeze every shipped Android compatibility boundary, remove privacy and supply-chain release blockers, and make public/private Phase 0 builds fail closed with reproducible evidence before architecture or live-provider work begins.

**Architecture:** Add a JVM verification tool beside the existing Groovy Android build, then drive checked-in YAML/JSON manifests, old-release compatibility probes, privacy/egress checks, dependency verification, and signed compliance attestations from Gradle tasks. Production behavior changes are deliberately narrow: public-stub calls become non-throwing, provider selection becomes explicit, telemetry and sensitive logging disappear, and unknown-license code cannot enter a distributable artifact.

**Tech Stack:** Groovy Gradle 9.1.0, AGP 9.0.1 built-in Kotlin, Kotlin 2.2.10, JUnit 4, AndroidX Test, JaCoCo, CycloneDX, SPDX, SnakeYAML Engine, JSON Schema, Ed25519/OpenSSL, GitHub Actions, ADB/emulator API 23/32/33.

---

## Scope Boundary

This plan implements only Phase 0 of `docs/superpowers/specs/2026-07-10-wenku8-modernization-program-design.md`. It does not implement a public Wenku8 transport, parser, login, repository, Room store, DataStore store, Compose application shell, or new feature page. Phase 1 creates architectural modules and the Material 3 shell; Phase 2 implements the public provider.

All commands below run from:

```powershell
Set-Location D:\Projects\10_Active\light-novel-library_Wenku8_Android\studio-android\LightNovelLibrary
```

Do not put the live username, password, Cookie, captcha, private endpoint, signing key, or protected overlay content in a command, source file, Gradle property, fixture, report, screenshot, or commit.

## Preconditions and Stop Conditions

- Start in an isolated worktree created with `superpowers:using-git-worktrees`, based on reviewed specification commit `3c8fa142` or a descendant that changes no approved Phase 0 contract.
- `git status --short` must be empty before Task 1. Preserve unrelated user changes and stop for coordination if the files named by a task are already modified.
- Tasks 1-3, 5-7, and 9-12 use only source and synthetic fixtures. They may proceed while authorization is unknown.
- No live site request, live account test, exact live-operation ledger publication, or public-provider distributable build is allowed until `docs/compliance/site-content-distribution-approval.yaml` has current `ACCEPTED` rows for that precise scope and channel.
- Set `WENKU8_OLD_SIGNED_APK` and `WENKU8_OLD_MAPPING` only in the external signed-artifact job. If the last shipped signed/minified APK, its signing certificate, or mapping cannot be obtained and hashed, stop Task 4; do not manufacture a new-to-new fixture and call it old-release evidence.
- The old-fixture probe and new candidate must be signed by the external pipeline with the same trusted certificate. Signing material stays outside the repository. A debug-only round trip cannot pass Task 4.
- `UNKNOWN`, missing, or incompatible licensing for any packaged production source, asset, font, fixture, plugin, or Maven artifact blocks Task 8 and every signed/distributable variant. The SlidingLayout family is a named blocker; do not waive it because it is legacy code.
- Protected private source, coordinates, endpoints, compliance rows, locks, checksums, and artifacts may exist only in the protected job's ephemeral workspace. Public CI receives only the signed redacted attestation defined in Task 12.
- `-Pwenku8Provider=private` without `api/build.gradle`, and every unknown provider value, must fail during Gradle configuration with a redacted message.
- A task is complete only after its RED assertion failed for the intended reason, the stated PASS command succeeded, `git diff --check` succeeded, and the task's focused commit exists.

## Phase Exit Conditions

Run the aggregate gate at the end of Task 13. Phase 0 cannot be declared complete unless all of these are true:

- Public `alphaDebug`, `baiduDebug`, `playstoreDebug`, and minified release configuration/build gates pass only for currently authorized channels; instrumentation runs on API 23 or newer.
- Existing unit, instrumentation, and lint suites pass; every warning is either removed or recorded with a narrow owner, reason, expiry, and ratcheting count. The eight AGP-10 legacy flags and Jetifier/compile-time-R/large-project warnings are not silently ignored.
- Public-stub visible paths cannot throw `UnsupportedOperationException`.
- Intent, Serializable, R8, manifest, API ABI, artifact/path, backup, preference, and UI-owner inventories are exhaustive and deterministic.
- Old-signed/minified-to-new-minified Intent and Serializable fixtures pass on API 23 and API 32 untyped Bundle access plus API 33 typed Bundle access.
- Coverage reports include all production logic and meet the checked-in Phase 0 ratchet; exclusions are only the generated classes explicitly listed in the coverage manifest.
- The scoped authorization record is current and accepted for every requested live/release scope; otherwise the relevant live/release task demonstrably fails closed.
- Public and protected-private packaged graphs have zero unknown/incompatible licenses, match locks/verifications/SBOM/provenance/notices/source offer, and reproduce from clean-cache online resolution to verified offline build.
- Public packaged source, merged manifest, bytecode, resources, and runtime egress contain no Firebase Analytics, Crashlytics, AdMob, advertising-ID access, raw sensitive logging, unaudited destination, or undeclared payload class.
- Every private attestation is fresh, non-replayed, trusted, non-revoked, and bound to the exact provider, variant, source commit, public-base hash, artifact hash, policy revision, approval window, run ID, and nonce; all specified negative fixtures fail.

## File Structure

### Build and CI

- Modify `.github/workflows/android-ci.yml`: supported emulator matrix, public provider property, Phase 0 gates, retained reports.
- Create `.github/workflows/protected-private-compliance.yml`: protected overlay merge, equivalent private gates, Ed25519 signing, redacted artifact export.
- Create `.github/CODEOWNERS`: two-maintainer ownership boundary for trust/authorization records.
- Modify `studio-android/LightNovelLibrary/settings.gradle`: centralized repositories and explicit provider selection; directory presence never selects behavior.
- Modify `studio-android/LightNovelLibrary/build.gradle`: plugin/version aliases, verification tasks, dependency locking, warning ratchet.
- Modify `studio-android/LightNovelLibrary/gradle/libs.versions.toml`: single version/coordinate source.
- Modify `studio-android/LightNovelLibrary/gradle/wrapper/gradle-wrapper.properties`: pin Gradle distribution SHA-256.
- Create `studio-android/LightNovelLibrary/gradle/verification-metadata.xml`: strict artifact checksums/signatures generated by Gradle.
- Create `studio-android/LightNovelLibrary/**/gradle.lockfile`: one reviewed lock state per participating Gradle project, covering every resolvable public production/test configuration; plugin versions are catalog-pinned and plugin artifacts remain under strict verification metadata.
- Create `studio-android/LightNovelLibrary/gradle/warning-baseline.json`: exact warning ID/count/owner/expiry ratchet.

### Verification Tool

- Create `studio-android/LightNovelLibrary/verification-tools/build.gradle`: pure JVM application/test module (bootstrapped in Task 1, extended in later tasks).
- Create `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`: command dispatcher.
- Create focused packages under `verification-tools/src/main/kotlin/org/mewx/wenku8/verification/{inventory,compat,coverage,compliance,license,network,supplychain,attestation}`: one verifier per concern.
- Create the exact verifier test files named in Tasks 3-13 under `verification-tools/src/test/kotlin/org/mewx/wenku8/verification/` and their explicitly named synthetic resources under `verification-tools/src/test/resources/`.

### Compatibility and Privacy Production Changes

- Modify `studio-android/LightNovelLibrary/api-stub/src/main/java/org/mewx/wenku8/api/Wenku8API.kt` and `PublicApiStubContract.kt`: typed non-throwing public-stub sentinels.
- Modify `studio-android/LightNovelLibrary/api-stub/src/main/java/org/mewx/wenku8/network/{LightNetwork,LightUserSession}.kt`: zero-egress/non-secret stub semantics.
- Create `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt`: one decoder for frozen extras and API-33 Serializable handling.
- Modify `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/global/api/{ChapterInfo,VolumeList}.kt`: explicit shipped serialVersionUID values only after extraction.
- Modify `studio-android/LightNovelLibrary/app/proguard-rules.pro`: compatibility keep names/fields.
- Remove `studio-android/LightNovelLibrary/app/google-services.json` and `GoogleServicesHelper.kt`; modify all call sites listed in Task 9.
- Create `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/diagnostics/{OperationCode,FailureClass,OperationalLogger,MigrationDiagnostics}.kt`: bounded local-only diagnostics.
- Create `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/network/policy/{LegacyOutboundPolicy,LegacyNetworkFactory}.kt`: temporary audited legacy egress boundary, retired by the Phase 1/2 network module.
- Modify `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`: remove ad/analytics declarations and reference explicit backup/network rules.
- Create `studio-android/LightNovelLibrary/app/src/main/res/xml/{network_security_config,data_extraction_rules,backup_rules}.xml`.

### Checked-In Evidence

- Create `docs/verification/{manifest-components,intent-contracts,serializable-contracts,legacy-api-signatures,artifact-manifest,backup-manifest,preference-manifest,ui-owner-action-ledger,xml-surface-ledger,source-inventory,outbound-network-manifest,coverage-manifest,gradle-task-contract,manual-assistive-technology-manifest,modernization-matrix}.yaml`.
- Create `docs/compliance/{site-content-distribution-approval,private-overlay-schema,private-attestation-trust}.yaml`.
- Create `docs/licenses/{source-asset-provenance,license-policy}.yaml`, root `NOTICE`, and root `SOURCE_OFFER.md`.
- Create `studio-android/LightNovelLibrary/app/src/main/res/raw/notice.txt` from the approved provenance ledger.
- Create `docs/verification/private-attestation-nonce-registry.json` and redacted positive/negative attestation fixtures under `verification-tools/src/test/resources/attestation/`.
- Create `docs/verification/compat-fixtures/` containing only sanitized serialized/Intent bytes, mapping/certificate metadata, and SHA-256 manifests produced by the external signed probe.

## Task 1: Repair the Baseline Test Device and Freeze Build Warnings

**Files:**

- Modify: `.github/workflows/android-ci.yml`

Before the first live-scope invocation, add the exact dispatcher `if (command == "verifyAcceptedLiveScope") { AuthorizationVerifier.verifyAcceptedLiveScope(projectRoot, docsRoot); return }` for the already-registered Phase 0 task.
- Modify: `studio-android/LightNovelLibrary/settings.gradle`
- Delete: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/MyAppTest.java`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/MyAppInstrumentedTest.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Create: `studio-android/LightNovelLibrary/gradle/warning-baseline.json`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/supplychain/WarningVerifier.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/supplychain/WarningVerifierTest.kt`

- [ ] **Step 1: Record the current supported task graph and the known invalid CI API**

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest :app:assembleAlphaRelease :app:lintAlphaDebug :app:connectedAlphaDebugAndroidTest --dry-run -Pwenku8Provider=public --warning-mode all 2>&1 | Tee-Object build\phase0-taskgraph.txt
```

Expected: configuration succeeds, output identifies the current public `api-stub`, Crashlytics/Google Services tasks, API-21 CI in the workflow, and the eight AGP-10/Jetifier/R-class/large-project warnings. This is RED because `api-level: 21` is lower than `minSdk 23`, and warning output is not governed by a checked-in baseline.

- [ ] **Step 2: Replace the stale Mockito Application test with an actual process test**

Use this complete test:

```kotlin
package org.mewx.wenku8

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyAppInstrumentedTest {
    @Test
    fun processApplicationPublishesItsOwnApplicationContext() {
        val application = ApplicationProvider.getApplicationContext<MyApp>()
        assertSame(application.applicationContext, MyApp.getContext())
    }
}
```

Delete the Java test; do not retain the invalid expectation that `MyApp.getContext()` may be null after `onCreate()`.

- [ ] **Step 3: Move CI to supported devices and make provider selection explicit**

Replace the emulator portion with this matrix; retain checkout/JDK setup and report uploads:

```yaml
strategy:
  fail-fast: false
  matrix:
    api-level: [23, 33]
steps:
  - uses: actions/checkout@v4
  - uses: actions/setup-java@v4
    with:
      distribution: temurin
      java-version: '21'
      cache: gradle
  - name: Unit, lint, and public build
    run: ./gradlew --stacktrace --warning-mode all -Pwenku8Provider=public :app:testAlphaDebugUnitTest :app:lintAlphaDebug :app:assembleAlphaDebug
  - name: Instrumentation
    uses: reactivecircus/android-emulator-runner@v2
    with:
      api-level: ${{ matrix.api-level }}
      arch: x86_64
      profile: pixel_2
      working-directory: ./studio-android/LightNovelLibrary
      script: ./gradlew --stacktrace -Pwenku8Provider=public :app:connectedAlphaDebugAndroidTest
  - uses: actions/upload-artifact@v4
    if: always()
    with:
      name: phase0-api-${{ matrix.api-level }}
      path: |
        studio-android/LightNovelLibrary/app/build/reports
        studio-android/LightNovelLibrary/app/build/outputs/androidTest-results
```

- [ ] **Step 4: Write a failing warning-ratchet test**

The baseline JSON schema is exact and every entry needs an owner and expiry:

```json
{
  "schemaVersion": 1,
  "warnings": [
    {"id":"gradle.groovy-space-assignment","maxCount":15,"owner":"build","expires":"2026-08-10"},
    {"id":"android.usesSdkInManifest.disallowed","maxCount":1,"owner":"build","expires":"2026-08-10"},
    {"id":"android.sdk.defaultTargetSdkToCompileSdkIfUnset","maxCount":1,"owner":"build","expires":"2026-08-10"},
    {"id":"android.enableAppCompileTimeRClass","maxCount":1,"owner":"build","expires":"2026-08-10"},
    {"id":"android.newDsl","maxCount":1,"owner":"build","expires":"2026-08-10"},
    {"id":"android.r8.optimizedResourceShrinking","maxCount":1,"owner":"build","expires":"2026-08-10"},
    {"id":"android.defaults.buildfeatures.resvalues","maxCount":1,"owner":"build","expires":"2026-08-10"},
    {"id":"android.nonFinalResIds","maxCount":1,"owner":"build","expires":"2026-08-10"},
    {"id":"android.enableJetifier","maxCount":1,"owner":"build","expires":"2026-08-10"},
    {"id":"android.dependency.excludeLibraryComponentsFromConstraints","maxCount":16,"owner":"build","expires":"2026-08-10"}
  ]
}
```

Parse `build/phase0-taskgraph.txt`, normalize each Gradle/AGP warning to the IDs above, require every observed ID in the baseline, reject expired rows, and reject a count above `maxCount`. Recount after the exact baseline command and lower a count if current output is smaller; never raise it without a reviewed owner/reason. The committed baseline contains all eight observed AGP-10 removal flags and no wildcard ID.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "*.WarningVerifierTest"
```

Expected: FAIL because the verifier/module and complete warning baseline do not exist yet.

- [ ] **Step 5: Bootstrap the verification module, implement, and run the warning ratchet**

Add `include ':verification-tools'`. Its initial build applies Kotlin/JVM 2.2.10 plus `application`, uses JUnit 4, and registers `verifyWarnings` as a `JavaExec` command routed through `VerificationMain`. Task 3 extends the same module; it does not create a second verifier project.

The exact initial dispatcher is `if (command == "verifyWarnings") { WarningVerifier.verifyCurrent(projectRoot, docsRoot); return }`; unknown commands fail with constant `VERIFICATION-COMMAND-UNKNOWN` and never return success.

The verifier's public contract is:

```kotlin
data class WarningAllowance(
    val id: String,
    val maxCount: Int,
    val owner: String,
    val expires: java.time.LocalDate,
)

object WarningVerifier {
    fun verify(log: String, allowances: List<WarningAllowance>, today: java.time.LocalDate) {
        val observed = WarningNormalizer.parse(log)
        require(observed.keys == allowances.map { it.id }.toSet()) {
            "warning IDs differ: observed=${observed.keys.sorted()} allowed=${allowances.map { it.id }.sorted()}"
        }
        allowances.forEach { row ->
            require(row.owner.isNotBlank() && !row.expires.isBefore(today)) { "invalid warning owner/expiry: ${row.id}" }
            require(observed.getValue(row.id) <= row.maxCount) { "warning count increased: ${row.id}" }
        }
    }
}
```

Run:

```powershell
.\gradlew.bat :verification-tools:verifyWarnings -PwarningLog=build\phase0-taskgraph.txt
.\gradlew.bat :app:testAlphaDebugUnitTest :app:lintAlphaDebug -Pwenku8Provider=public
```

Expected: PASS; no unowned, expired, or increased warning remains.

- [ ] **Step 6: Commit the baseline repair**

```powershell
git add ..\..\.github\workflows\android-ci.yml settings.gradle app\src\androidTest gradle\warning-baseline.json verification-tools
git diff --check
git commit -m "test: stabilize phase zero Android baseline"
```

## Task 2: Make Provider Selection Explicit and the Public Stub Non-Throwing

**Files:**

- Modify: `studio-android/LightNovelLibrary/settings.gradle`
- Modify: `studio-android/LightNovelLibrary/api-stub/src/main/java/org/mewx/wenku8/api/{Wenku8API,PublicApiStubContract}.kt`
- Modify: `studio-android/LightNovelLibrary/api-stub/src/main/java/org/mewx/wenku8/network/{LightNetwork,LightUserSession}.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/api/PublicApiStubContractTest.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/supplychain/ProviderSelectionTest.kt`

- [ ] **Step 1: Add failing configuration and no-throw assertions**

Extend `PublicApiStubContractTest` with a table that calls every public `Wenku8API` method using benign values and asserts no `UnsupportedOperationException`. Assert URL-returning methods return `PublicApiStubContract.UNAVAILABLE_URL`, `ContentValues` methods return an empty value, enum parsers use documented stable defaults, network methods dispatch zero traffic, and session methods never retain a password. `ProviderSelectionTest` uses Gradle TestKit with unknown values shaped like an HTTPS URL containing userinfo, an absolute Windows path, an assignment containing credential-like fields, and a long control-character-bearing token. It asserts configuration fails with one constant error code and that the raw value or any substring unique to it is absent from stdout, stderr/exception text, stacktrace, daemon log, and retained reports.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.api.PublicApiStubContractTest" -Pwenku8Provider=public
.\gradlew.bat help -Pwenku8Provider=typo
.\gradlew.bat help -Pwenku8Provider=private
```

Expected: the test FAILS on `UnsupportedOperationException`; `typo` incorrectly configures; `private` silently falls back when the directory is absent.

- [ ] **Step 2: Replace directory-presence selection with one explicit descriptor**

Use this Groovy selection block in `settings.gradle`; later tasks add centralized repositories above it:

```groovy
include ':app'
include ':api'

enum Wenku8ProviderSelection { PUBLIC, PRIVATE }

def rawProvider = providers.gradleProperty('wenku8Provider').orNull
def provider = rawProvider == null || rawProvider == 'public'
    ? Wenku8ProviderSelection.PUBLIC
    : rawProvider == 'private'
        ? Wenku8ProviderSelection.PRIVATE
        : null
if (provider == null) {
    throw new GradleException('WENKU8-PROVIDER-E001: unsupported provider selection')
}

switch (provider) {
    case Wenku8ProviderSelection.PUBLIC:
        project(':api').projectDir = file('api-stub')
        break
    case Wenku8ProviderSelection.PRIVATE:
        def privateBuild = file('api/build.gradle')
        if (!privateBuild.isFile()) {
            throw new GradleException('WENKU8-PROVIDER-E002: protected provider source unavailable')
        }
        project(':api').projectDir = file('api')
        break
}

gradle.settingsEvaluated {
    println provider == Wenku8ProviderSelection.PUBLIC
        ? 'GRADLE: selected provider=public'
        : 'GRADLE: selected provider=private'
}
```

The raw property is used only for the two exact enum-token equality checks above and is never interpolated, concatenated, serialized, logged, added to an exception, or passed to another helper. Do not print a private path, coordinate, endpoint, overlay hash, or unrecognized value. The two failure messages above are the only provider-selection configuration failures exposed by public builds.

- [ ] **Step 3: Implement typed non-throwing stub sentinels**

Use constants and type-specific helpers instead of a generic throwing helper:

```kotlin
class PublicApiStubContract private constructor() {
    companion object {
        @JvmField val isPublicStub: Boolean = true
        const val providerId: String = "public-stub"
        const val UNAVAILABLE_URL: String = "https://invalid.local/"
        const val failureMessage: String = "public provider is not implemented in phase zero"
    }
}

private fun emptyValues(): ContentValues = ContentValues()
private fun unavailableString(): String = PublicApiStubContract.UNAVAILABLE_URL
```

Map each existing API method explicitly: `getCoverURL`/`getAvatarURL` return the unavailable HTTPS URL; publication status parsers return `NOT_FINISHED`; sort parsers return `lastUpdate`; status/string helpers return `UNKNOWN`; every request/response helper returns a new empty `ContentValues`; `searchBadWords` returns null. Remove `unavailable<T>()` entirely.

In `LightUserSession`, immediately zero any incoming password representation and retain only the username if current UI compatibility requires it; `getPassword()` must return `""`. Public stub login returns `SYSTEM_4_NOT_LOGGED_IN`. `LightNetwork` keeps encoding helpers but returns null before opening any connection.

- [ ] **Step 4: Verify both provider branches and the crash guard**

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.api.PublicApiStubContractTest" -Pwenku8Provider=public
.\gradlew.bat help -Pwenku8Provider=typo
.\gradlew.bat help -Pwenku8Provider=private
```

Expected: test PASS; the two invalid/private-without-source commands FAIL during configuration with only the redacted messages above. A protected checkout with `api/build.gradle` must separately show `selected provider=private`.

Run `ProviderSelectionTest` with `--stacktrace` and inspect its retained TestKit output. Expected: every adversarial unknown value produces only `WENKU8-PROVIDER-E001`; private-without-source produces only `WENKU8-PROVIDER-E002`; no raw input fragment appears in any captured channel.

- [ ] **Step 5: Commit explicit selection and crash prevention**

```powershell
git add settings.gradle api-stub app\src\test\java\org\mewx\wenku8\api\PublicApiStubContractTest.kt verification-tools
git diff --check
git commit -m "fix: fail closed on provider selection"
```

## Task 3: Generate Exhaustive Compatibility and UI Inventories

**Files:**

- Modify: `studio-android/LightNovelLibrary/settings.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/inventory/{InventoryScanner,ManifestInventory,IntentInventory,ArtifactInventory,PreferenceInventory,UiOwnerInventory,XmlSurfaceInventory,ApiSignatureInventory,YamlWriter}.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/plan/{GradleTaskContractVerifier,XmlSurfaceLedgerVerifier}.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/inventory/CompatibilityInventoryTest.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/inventory/UiOwnerInventoryTest.kt`
- Create: the checked-in `docs/verification/*.yaml` files listed under File Structure, including `gradle-task-contract.yaml` and `xml-surface-ledger.yaml`.

- [ ] **Step 1: Add a failing determinism/exhaustiveness test**

The test copies synthetic Kotlin/XML/filesystem inputs to two differently named temporary roots, runs every scanner, and requires byte-identical LF-normalized YAML. It also injects one unclassified `putExtra`, `getSharedPreferences`, file write, manifest Activity, click listener, and public API method and requires a named `UnclassifiedInventoryItem` failure for each.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.inventory.*"
```

Expected: FAIL because the module/scanners are absent.

- [ ] **Step 2: Add the verification module and fixed command surface**

Add `include ':verification-tools'` and create this Groovy build file:

```groovy
plugins {
    id 'application'
    id 'org.jetbrains.kotlin.jvm' version '2.2.10'
}

repositories { mavenCentral() }

dependencies {
    implementation 'org.snakeyaml:snakeyaml-engine:2.9'
    testImplementation 'junit:junit:4.13.2'
    testImplementation gradleTestKit()
}

application { mainClass = 'org.mewx.wenku8.verification.VerificationMainKt' }

def phase0VerifierCommands = [
    'generateInventories',
    'verifyInventories',
    'verifyWarnings',
    'verifySensitiveSource',
    'generateSbom',
    'generateNotices',
    'verifyPackagedLicenses',
    'verifyOutboundManifest',
    'verifyReproducibleInputs',
    'verifyPrivateAttestationFixtures',
    'verifyAcceptedLiveScope',
    'verifyIntentContracts',
    'verifyAuthorization',
    'verifyPhase0Coverage',
    'resolvedOriginReport',
    'recordPackagedInputs',
    'verifyPrivateAttestation',
    'verifySharedAttestationNonDisclosure',
    'verifyPlannedGradleTasks',
    'verifyXmlSurfaceLedger',
]
phase0VerifierCommands.each { command ->
    tasks.register(command, JavaExec) {
        group = 'verification'
        classpath = sourceSets.main.runtimeClasspath
        mainClass = application.mainClass
        args command, rootProject.projectDir.absolutePath,
            new File(rootProject.projectDir, '../../docs').canonicalPath
    }
}
```

Task 11 centralizes repositories and moves these literal versions into the version catalog; this temporary declaration must not survive Task 11. Each command above has one exact dispatcher branch before its first invocation. Task 3 implements the inventory/task/XML branches; Tasks 7-9, 11, and 12 implement their named branches. Until its owner task lands, invoking a registered future command must fail with constant `PHASE0-VERIFIER-NOT-IMPLEMENTED`, never succeed or fall through to another verifier.

- [ ] **Step 3: Implement structured manifest/resource scanning and explicit source facts**

Use `DocumentBuilderFactory` with external entities disabled for XML. `InventoryScanner` returns stable records, sorts by stable ID/path/line, writes with SnakeYAML, and never writes timestamps or absolute paths:

```kotlin
data class InventoryFinding(
    val stableId: String,
    val sourcePath: String,
    val line: Int,
    val owner: String,
    val attributes: Map<String, String?>,
)

interface InventorySection {
    val outputName: String
    fun scan(projectRoot: java.nio.file.Path): List<InventoryFinding>
    fun verifyClassified(findings: List<InventoryFinding>, accepted: Map<String, AcceptedRow>)
}
```

Use Kotlin-aware lexical scanning that removes comments/strings before matching call expressions. Every matched call retains source path/line and argument text; ambiguous/dynamic keys are inventory rows with `classification: manual-required`, never silently dropped.

Add these exact Task 3 dispatcher branches after common argument validation:

```kotlin
when (command) {
    "generateInventories" -> InventoryScanner.generate(projectRoot, docsRoot)
    "verifyInventories" -> InventoryScanner.verify(projectRoot, docsRoot)
    "verifyIntentContracts" -> IntentInventory.verifyContracts(projectRoot, docsRoot)
    "verifyPlannedGradleTasks" -> GradleTaskContractVerifier.verify(projectRoot, docsRoot)
    "verifyXmlSurfaceLedger" -> XmlSurfaceLedgerVerifier.verify(projectRoot, docsRoot)
    else -> dispatchOwnedCommand(command, projectRoot, docsRoot)
}
```

`GradleTaskContractVerifier` extracts every Gradle task token from Markdown commands, Groovy `dependsOn`, workflows, and evidence YAML. Standard/plugin tasks pass only through exact plugin/version rows; custom tasks require one literal task-path row, registration owner, and JavaExec dispatcher command where applicable. It explicitly understands the constant `phase0VerifierCommands` registry above and rejects arbitrary/dynamic task names.

- [ ] **Step 4: Generate and classify every required inventory**

Run:

```powershell
.\gradlew.bat :verification-tools:generateInventories -Pwenku8Provider=public
```

Expected generated facts:

- `manifest-components.yaml`: all 15 current Activities, launcher/exported/theme/permission/metadata and merged-manifest owner.
- `intent-contracts.yaml`: every sender/receiver/key/type/default/sentinel/API-33 decode/retirement owner, including `aid`, `cid`, `from`, `title`, `key`, `rid`, `forcejump`, `volume`, `volumes`, and `path`.
- `serializable-contracts.yaml`: class/package, computed UID candidate, fields, constructors, R8 name, signed-fixture hash slot.
- `legacy-api-signatures.yaml`: every public/protected ABI symbol in `Wenku8API`, `Wenku8Error`, `LightNetwork`, and `LightUserSession`.
- `artifact-manifest.yaml`: every production file/SharedPreferences read/write, including every Section 6.4 minimum artifact, primary/backup order, format/encoding, account/source partition, backup/delete policy, owner.
- `backup-manifest.yaml`: each physical file/database/preference domain and inclusion/exclusion reason; no selected Room-row rule.
- `preference-manifest.yaml`: all `settings.wk8` keys and `modern_reader_display_settings` keys/defaults/types/writers/readers.
- `ui-owner-action-ledger.yaml`: A01-A15, F01-F06, all visible/menu/drawer/click actions, states, Intents, planned target route ID, test ID, retirement owner.
- `xml-surface-ledger.yaml`: exactly X01-X34 from Specification Section 10.4, with exact resource path, structured reference roots, reachability/kind, A/F/helper owner, target route/component, replacement test ID, and retirement owner.
- `source-inventory.yaml`: Kotlin/Java/XML/layout/Fragment/Activity/AsyncTask/findViewById/GlobalConfig counts, paths, and hashes; every layout row joins one X ID rather than existing only as an aggregate count.
- `gradle-task-contract.yaml`: every plan/workflow/evidence task reference, exact registration owner/path/type or pinned generating plugin/version, JavaExec dispatcher command, consumers, and retirement owner.
- `manual-assistive-technology-manifest.yaml`: an empty schema-valid `entries: []` baseline; phase gates require populated hash-bound rows rather than treating the empty file as evidence.
- `outbound-network-manifest.yaml`: initially generated origins/SDK initializers for classification in Task 10.
- `coverage-manifest.yaml`: generated-class-only exact exclusions and layer ownership.
- `modernization-matrix.yaml`: requirement IDs with empty evidence status `NOT_RUN`, never a false `PASS`.

Inspect every `manual-required` row and add a concrete classification; do not delete the source finding.

Run the two classification gates and expect all 34 XML IDs plus all currently referenced custom tasks to be named:

```powershell
.\gradlew.bat :verification-tools:verifyXmlSurfaceLedger :verification-tools:verifyPlannedGradleTasks -Pwenku8Provider=public
```

- [ ] **Step 5: Verify inventories fail on source drift**

```powershell
.\gradlew.bat :verification-tools:verifyInventories -Pwenku8Provider=public
git diff --exit-code -- ..\..\docs\verification
```

Expected: PASS and no regenerated diff. Temporarily add a synthetic unclassified `putExtra("phase0_probe", 1)` in a test fixture, rerun, and confirm FAIL mentions its exact fixture path and line; remove only the probe.

- [ ] **Step 6: Commit inventories and their generator**

```powershell
git add settings.gradle verification-tools ..\..\docs\verification
git diff --check
git commit -m "test: freeze Android compatibility inventories"
```

## Task 4: Freeze Old-Signed Intent, Serializable, and R8 Contracts

**Files:**

- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/global/api/{ChapterInfo,VolumeList}.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/proguard-rules.pro`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/compat/{OldReleaseFixtureProbe,LegacyIntentCodecInstrumentedTest,OldReleaseSerializableInstrumentedTest}.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/compat/LegacyIntentCodecTest.kt`
- Create: `docs/verification/compat-fixtures/**`

- [ ] **Step 1: Prove current tests are insufficient**

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "*ReaderLaunchArguments*" -Pwenku8Provider=public
```

Expected: current new-to-new tests may PASS, but no test consumes a byte stream or Intent emitted by the last signed/minified release. Record this as RED in the task evidence; passing the existing test is not the gate.

- [ ] **Step 2: Extract immutable old-release identity in the protected signing job**

The job must refuse unset inputs and compare signing certificates before probing:

```powershell
if ([string]::IsNullOrWhiteSpace($env:WENKU8_OLD_SIGNED_APK)) { throw 'WENKU8_OLD_SIGNED_APK is required' }
if ([string]::IsNullOrWhiteSpace($env:WENKU8_OLD_MAPPING)) { throw 'WENKU8_OLD_MAPPING is required' }
$oldApkHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $env:WENKU8_OLD_SIGNED_APK).Hash.ToLowerInvariant()
& $env:ANDROID_HOME\build-tools\37.0.0\apksigner.bat verify --verbose --print-certs $env:WENKU8_OLD_SIGNED_APK | Out-File build\old-apk-cert.txt -Encoding utf8
```

Store only APK/mapping/certificate hashes and redacted tool output in `compat-fixtures/metadata.yaml`; never commit the APK, signing key, or absolute path. If the actual UIDs differ from candidates `ChapterInfo=471719262165600067L` or `VolumeList=9025397832339598968L`, the extracted shipped values win and implementation pauses until the discrepancy is reviewed.

- [ ] **Step 3: Add a supported minified instrumentation probe variant and build the same-certificate test APK**

`release` is non-debuggable and therefore has no AGP AndroidTest component. Do not reference `assembleAlphaReleaseAndroidTest` or a test-only module's nonexistent `assembleReleaseAndroidTest`. Add one non-distributable app build type that inherits the complete release/R8 configuration but is debuggable so AGP creates a supported AndroidTest variant:

```groovy
android {
    buildTypes {
        compatProbe {
            initWith release
            debuggable true
            minifyEnabled true
            signingConfig signingConfigs.debug
            matchingFallbacks = ['release']
        }
    }
}

androidComponents {
    beforeVariants(selector().withBuildType('compatProbe')) { variantBuilder ->
        variantBuilder.enable = variantBuilder.productFlavors.any { it.second == 'alpha' }
    }
}
```

`ProviderSelectionTest` also uses Gradle TestKit to assert `alphaCompatProbe` is minified/debuggable, inherits the release ProGuard files and dependencies, is enabled only for `alpha`, and is never selected by an assemble/bundle distribution task. The external job installs the old signed APK, externally re-signs the `alphaCompatProbe` AndroidTest APK with the same certificate, and runs `OldReleaseFixtureProbe`. The probe reflects the target app's minified boundary classes, sets every public field, serializes them, and emits Base64 plus SHA-256 through instrumentation status bundles. It also launches each frozen receiver with missing, valid, malformed, and sentinel extras and records normalized outcomes.

Run in the external signing environment:

```powershell
adb install -r $env:WENKU8_OLD_SIGNED_APK
.\gradlew.bat :app:assembleAlphaCompatProbeAndroidTest -Pwenku8Provider=public
& $env:WENKU8_SIGN_PROBE_SCRIPT -InputApk app\build\outputs\apk\androidTest\alpha\compatProbe\app-alpha-compatProbe-androidTest.apk -OutputApk build\signed-probe.apk
adb install -r build\signed-probe.apk
adb shell am instrument -w -e class org.mewx.wenku8.compat.OldReleaseFixtureProbe -e outputDir /sdcard/Download/wenku8-fixtures org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
adb pull /sdcard/Download/wenku8-fixtures ..\..\docs\verification\compat-fixtures
```

Expected: PASS only when the probe certificate matches; emitted files contain no account/content secrets.

- [ ] **Step 4: Add explicit UIDs, keep rules, and one Intent decoder**

Declare the verified values:

```kotlin
class ChapterInfo : java.io.Serializable {
    @JvmField var cid: Int = 0
    @JvmField var chapterName: String? = null
    private companion object { private const val serialVersionUID: Long = 471719262165600067L }
}

class VolumeList : java.io.Serializable {
    @JvmField var volumeName: String? = null
    @JvmField var vid: Int = 0
    @JvmField var inLocal: Boolean = false
    @JvmField var chapterList: java.util.ArrayList<ChapterInfo>? = null
    private companion object { private const val serialVersionUID: Long = 9025397832339598968L }
}
```

Replace the numeric literals if Task 4 Step 2 proves different shipped values. Add exact R8 rules:

```proguard
-keepnames class org.mewx.wenku8.global.api.ChapterInfo
-keepclassmembers class org.mewx.wenku8.global.api.ChapterInfo { long serialVersionUID; public int cid; public java.lang.String chapterName; }
-keepnames class org.mewx.wenku8.global.api.VolumeList
-keepclassmembers class org.mewx.wenku8.global.api.VolumeList { long serialVersionUID; public java.lang.String volumeName; public int vid; public boolean inLocal; public java.util.ArrayList chapterList; }
```

`LegacyIntentCodec` must expose typed functions `novelDetail(intent)`, `search(intent)`, `reviewList(intent)`, `reviewThread(intent)`, `reader(intent, receiverKind)`, and `image(intent)` and implement API 33 access exactly:

```kotlin
private fun <T : java.io.Serializable> Intent.serializable(key: String, type: Class<T>): T? =
    if (android.os.Build.VERSION.SDK_INT >= 33) getSerializableExtra(key, type)
    else @Suppress("DEPRECATION") (getSerializableExtra(key) as? T)
```

Preserve every default/sentinel in Section 6.2; filter `volumes` entries by `VolumeList` while retaining order.

- [ ] **Step 5: Consume old fixtures in the supported probe variant and repeat against the real signed release**

```powershell
.\gradlew.bat :app:assembleAlphaCompatProbe :app:assembleAlphaCompatProbeAndroidTest :app:assembleAlphaRelease -Pwenku8Provider=public
adb install -r app\build\outputs\apk\alpha\compatProbe\app-alpha-compatProbe.apk
adb install -r app\build\outputs\apk\androidTest\alpha\compatProbe\app-alpha-compatProbe-androidTest.apk
adb shell am instrument -w -e fixtureDir /sdcard/Download/wenku8-fixtures org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
```

The protected signing job then signs the actual `app-alpha-release.apk` and a clean copy of the `alphaCompatProbe` test APK with the same release certificate, installs those two APKs, and repeats the identical instrumentation command. The `compatProbe` target is only deterministic local evidence; only the separately built, externally signed `alphaRelease` rerun satisfies signed-release compatibility.

Expected on API 23, API 32, and API 33: PASS for every old stream/Intent row, mapping name/hash and certificate digest retained. API 32 proves the final pre-Tiramisu untyped boundary; API 33 proves typed access. An intentionally changed UID, field name, key, default, or keep rule must make the focused test FAIL.

- [ ] **Step 6: Commit only sanitized fixtures and compatibility code**

```powershell
git add app\build.gradle app\src\main\java\org\mewx\wenku8\compat app\src\main\java\org\mewx\wenku8\global\api app\src\test\java\org\mewx\wenku8\compat app\src\androidTest\java\org\mewx\wenku8\compat app\proguard-rules.pro verification-tools ..\..\docs\verification\compat-fixtures
git diff --check
git commit -m "test: freeze shipped Android boundary contracts"
```

## Task 5: Enable Complete Line and Branch Coverage Gates

**Files:**

- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/build.gradle`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/coverage/CoverageVerifier.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/coverage/CoverageVerifierTest.kt`
- Modify: `docs/verification/coverage-manifest.yaml`

- [ ] **Step 1: Write a failing coverage-manifest test**

Require exact class globs and reject broad package/directory exclusions. The only allowed exclusions are generated `R`, `R$*`, `BuildConfig`, Compose compiler-generated classes identified by exact suffix, and Room/serialization glue when those modules arrive. Require line and branch counters for every production Kotlin/Java class in both public and protected-private graphs.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "*.CoverageVerifierTest"
```

Expected: FAIL because reports and the strict manifest do not exist.

- [ ] **Step 2: Enable JaCoCo for unit and instrumentation coverage**

Apply `jacoco` and add this Android configuration:

```groovy
buildTypes {
    debug {
        testCoverageEnabled true
    }
}

jacoco { toolVersion = '0.8.13' }

tasks.withType(Test).configureEach {
    jacoco.includeNoLocationClasses = true
    jacoco.excludes = ['jdk.internal.*']
}
```

Register `phase0CoverageReport` as a `JacocoReport` that merges `testAlphaDebugUnitTest` execution data and `createAlphaDebugCoverageReport` `.ec` data, uses Kotlin/Java class directories after only manifest-approved exact exclusions, and emits XML/HTML. Register `verifyPhase0Coverage` to read the XML and enforce parser/storage/migration 90/80, repository/use-case/ViewModel 80/70, and overall production logic 70/60 whenever such layers exist; for the current monolith, enforce overall 70/60 and report each package independently so aggregation cannot hide a weak package.

Use literal registrations, with the JavaExec command already present in `phase0VerifierCommands`:

```groovy
// root build.gradle
def phase0CoverageReportTask = tasks.register('phase0CoverageReport', JacocoReport) {
    dependsOn ':app:testAlphaDebugUnitTest', ':app:createAlphaDebugCoverageReport'
    executionData.from(
        fileTree(project(':app').layout.buildDirectory) {
            include 'jacoco/testAlphaDebugUnitTest.exec'
            include 'outputs/code_coverage/alphaDebugAndroidTest/connected/**/*.ec'
        }
    )
    sourceDirectories.from(
        project(':app').file('src/main/java'),
        project(':app').file('src/main/kotlin')
    )
    classDirectories.from(
        project(':app').fileTree(project(':app').layout.buildDirectory.dir('tmp/kotlin-classes/alphaDebug')) {
            exclude 'R.class', 'R$*.class', 'BuildConfig.class'
        },
        project(':app').fileTree(project(':app').layout.buildDirectory.dir('intermediates/javac/alphaDebug/classes')) {
            exclude 'R.class', 'R$*.class', 'BuildConfig.class'
        }
    )
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}
tasks.register('verifyPhase0Coverage') {
    group = 'verification'
    dependsOn ':verification-tools:verifyPhase0Coverage'
}

// verification-tools/build.gradle
tasks.named('verifyPhase0Coverage') {
    dependsOn ':phase0CoverageReport'
}
```

The `VerificationMain.kt` dispatcher branch accepts no command-specific arguments and calls `CoverageVerifier.verify(projectRoot.resolve("build/reports/jacoco/phase0CoverageReport/phase0CoverageReport.xml"), docsRoot.resolve("verification/coverage-manifest.yaml"))`; any extra argument fails before parsing the report. `gradle-task-contract.yaml` records `:app:createAlphaDebugCoverageReport` as AGP 9.0.1-generated and the other three paths as literal custom registrations.

- [ ] **Step 3: Add deterministic tests for Phase 0 boundary logic**

Add focused parameterized tests for every branch introduced in provider selection, stub sentinel mapping, `LegacyIntentCodec`, warning normalization, inventory classification, and compatibility hash validation. Use synthetic inputs; never exclude a failing branch from coverage.

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest :app:createAlphaDebugCoverageReport phase0CoverageReport verifyPhase0Coverage -Pwenku8Provider=public
```

Expected: PASS with XML at `build/reports/jacoco/phase0CoverageReport/phase0CoverageReport.xml`; the verifier prints overall and per-package line/branch percentages. If the current monolith is below 70/60, this step remains incomplete: use the XML missed-branch rows to add named parameterized cases to the exact boundary test classes above, not exclusions.

- [ ] **Step 4: Prove the gate catches regressions and commit**

Temporarily change one tested branch to an unexercised branch, rerun `verifyPhase0Coverage`, and confirm FAIL names its package/counter; revert only the probe.

```powershell
git add app\build.gradle build.gradle verification-tools ..\..\docs\verification\coverage-manifest.yaml
git diff --check
git commit -m "test: enforce phase zero coverage gates"
```

## Task 6: Add Scoped Authorization Records and Fail-Closed Live/Release Gates

**Files:**

- Create: `docs/compliance/site-content-distribution-approval.yaml`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/compliance/{AuthorizationRecord,AuthorizationVerifier}.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/compliance/AuthorizationVerifierTest.kt`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `.github/workflows/android-ci.yml`

- [ ] **Step 1: Write negative tests for absent, unknown, expired, rejected, and scope-mismatched approval**

Use fixed clock `2026-07-10` and JSON-compatible YAML fixtures. Each test must identify the exact scope and must not expose evidence contents:

```kotlin
@Test fun unknownLiveObservationFailsClosed() = assertDenied(
    requested = AuthorizationRequest("live-observation", "public", "alpha", "fixture"),
    expectedRule = "authorization-status-unknown",
)

@Test fun acceptedPlayStoreDoesNotAuthorizeBaidu() = assertDenied(
    requested = AuthorizationRequest("distribution", "public", "baidu", "release"),
    expectedRule = "authorization-channel-mismatch",
)
```

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "*.AuthorizationVerifierTest"
```

Expected: FAIL because no record/verifier exists.

- [ ] **Step 2: Check in the complete fail-closed scope record**

The file is valid YAML expressed as JSON so both Gradle `JsonSlurper` and the verifier parse it structurally:

```json
{
  "schemaVersion": 1,
  "owner": "release-maintainers",
  "decisionDate": null,
  "recheckDate": "2026-08-10",
  "scopes": [
    {"id":"automated-client-access","status":"UNKNOWN","channels":["alpha","baidu","playstore"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"endpoint-contract-publication","status":"UNKNOWN","channels":["source"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"live-observation","status":"UNKNOWN","channels":["controlled"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"captcha-login","status":"UNKNOWN","channels":["controlled"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"account-read","status":"UNKNOWN","channels":["controlled"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"bookshelf-mutation","status":"UNKNOWN","channels":["controlled"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"recommendation-mutation","status":"UNKNOWN","channels":["controlled"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"community-mutation","status":"UNKNOWN","channels":["controlled"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"offline-text-cache","status":"UNKNOWN","channels":["alpha","baidu","playstore"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"offline-image-cache","status":"UNKNOWN","channels":["alpha","baidu","playstore"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"distribution-alpha","status":"UNKNOWN","channels":["alpha"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"distribution-baidu","status":"UNKNOWN","channels":["baidu"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"distribution-playstore","status":"UNKNOWN","channels":["playstore"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"privacy-account-data","status":"UNKNOWN","channels":["alpha","baidu","playstore"],"jurisdictions":[],"evidenceHashes":[]},
    {"id":"age-region-obligations","status":"UNKNOWN","channels":["alpha","baidu","playstore"],"jurisdictions":[],"evidenceHashes":[]}
  ]
}
```

Only a qualified maintainer/legal review may change a status to `ACCEPTED`; it must also fill decision/recheck/expiry, jurisdictions, channels, and SHA-256 evidence hashes. Sensitive correspondence remains outside Git.

- [ ] **Step 3: Implement one reusable decision function**

```kotlin
enum class ApprovalStatus { ACCEPTED, UNKNOWN, EXPIRED, REJECTED }

data class AuthorizationRequest(
    val scopeId: String,
    val providerId: String,
    val channel: String,
    val action: String,
)

sealed interface AuthorizationDecision {
    data class Allowed(val scopeId: String, val expires: java.time.LocalDate) : AuthorizationDecision
    data class Denied(val scopeId: String, val rule: String) : AuthorizationDecision
}

class AuthorizationVerifier(private val clock: java.time.Clock) {
    fun decide(record: AuthorizationRecord, request: AuthorizationRequest): AuthorizationDecision {
        val row = record.scopes.singleOrNull { it.id == request.scopeId }
            ?: return AuthorizationDecision.Denied(request.scopeId, "authorization-scope-absent")
        if (row.status != ApprovalStatus.ACCEPTED) {
            return AuthorizationDecision.Denied(row.id, "authorization-status-${row.status.name.lowercase()}")
        }
        if (request.channel !in row.channels) return AuthorizationDecision.Denied(row.id, "authorization-channel-mismatch")
        val expiry = row.expiryDate ?: return AuthorizationDecision.Denied(row.id, "authorization-expiry-absent")
        if (!expiry.isAfter(java.time.LocalDate.now(clock))) return AuthorizationDecision.Denied(row.id, "authorization-expired")
        if (row.owner.isBlank() || row.evidenceHashes.isEmpty()) return AuthorizationDecision.Denied(row.id, "authorization-evidence-incomplete")
        return AuthorizationDecision.Allowed(row.id, expiry)
    }
}
```

- [ ] **Step 4: Bind release and live tasks to exact scopes**

In `app/build.gradle`, parse the JSON-compatible YAML with `groovy.json.JsonSlurper` during configuration. Inspect `gradle.startParameter.taskNames`; if a requested public release variant lacks its `distribution-<flavor>` plus automated-access/content/privacy rows, throw `GradleException("Authorization denied for provider=<id>, variant=<variant>, scope=<scope>")`. Register exact gate tasks `livePublicReadOnlySmoke`, `livePublicReversibleBookshelfSmoke`, and `livePublicPersistentMutationSmoke`; each depends on `:verification-tools:verifyAuthorization` for its exact scopes and then fails with `live implementation unavailable before Phase 2` until Phase 2 supplies the test action. Absence or fixture success never becomes approval.

```groovy
// verification-tools/build.gradle; verifyAuthorization is already registered by phase0VerifierCommands.
tasks.named('verifyAuthorization', JavaExec) {
    doFirst {
        def scope = providers.gradleProperty('scope').orNull
        def provider = providers.gradleProperty('provider').orNull
        def channel = providers.gradleProperty('channel').orNull
        if ([scope, provider, channel].any { it == null || it.isBlank() }) {
            throw new GradleException('WENKU8-AUTH-E001: scope, provider, and channel are required')
        }
        args scope, provider, channel
    }
}

// app/build.gradle
[
    livePublicReadOnlySmoke: 'live-observation',
    livePublicReversibleBookshelfSmoke: 'bookshelf-mutation',
    livePublicPersistentMutationSmoke: 'community-mutation',
].each { taskName, requiredScope ->
    tasks.register(taskName) {
        group = 'verification'
        dependsOn ':verification-tools:verifyAuthorization'
        doFirst {
            if (providers.gradleProperty('scope').orNull != requiredScope) {
                throw new GradleException('WENKU8-AUTH-E002: exact live scope is required')
            }
            throw new GradleException('WENKU8-LIVE-E001: live implementation unavailable before Phase 2')
        }
    }
}
```

Dispatch `verifyAuthorization` only when `commandArgs.size == 3`, mapping them in order to scope, provider, and channel. The dispatcher calls `AuthorizationVerifier.verifyRequestedScope(projectRoot, docsRoot, scope, provider, channel)` and never includes any received value in a failure message.

Run:

```powershell
.\gradlew.bat :app:assembleAlphaRelease -Pwenku8Provider=public
.\gradlew.bat :verification-tools:verifyAuthorization -Pscope=live-observation -Pprovider=public -Pchannel=controlled
.\gradlew.bat :app:assembleAlphaDebug -Pwenku8Provider=public
```

Expected with the checked-in `UNKNOWN` record: the first two commands FAIL closed and name only provider/variant/scope; deterministic debug fixture work PASSes. After authorized rows are legitimately accepted, the exact corresponding command PASSes while every unrelated scope remains denied.

- [ ] **Step 5: Commit the fail-closed gate**

```powershell
git add app\build.gradle verification-tools ..\..\docs\compliance\site-content-distribution-approval.yaml ..\..\.github\workflows\android-ci.yml
git diff --check
git commit -m "build: gate live and release scopes on approval"
```

## Task 7: Inventory and Remove Third-Party Telemetry, Ads, AD_ID, and Sensitive Logs

**Files:**

- Modify: `studio-android/LightNovelLibrary/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`
- Delete: `studio-android/LightNovelLibrary/app/google-services.json`
- Delete: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/util/GoogleServicesHelper.kt`
- Create: `docs/security/secret-handling.md`
- Modify: every Kotlin file returned by `rg -l "Firebase|GoogleServicesHelper|Log\\.|printStackTrace" app/src/main/java -g '*.kt'`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/diagnostics/{OperationCode,FailureClass,OperationalLogger,MigrationDiagnostics}.kt`
- Test: `studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/diagnostics/{OperationalLoggerTest,MigrationDiagnosticsPropertyTest}.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/network/SensitiveSourceVerifier.kt`

Add the exact dispatcher `if (command == "verifySensitiveSource") { SensitiveSourceVerifier.verify(projectRoot, docsRoot); return }` before invoking the registered task.

- [ ] **Step 1: Add a static RED gate before deleting anything**

The verifier must scan source, dependency coordinates, manifests, resources, and decompiled packaged bytecode for:

```text
com.google.firebase
com.google.android.gms.ads
com.google.android.gms.permission.AD_ID
com.google.android.gms.ads.APPLICATION_ID
google-services.json
FirebaseInitProvider
CrashlyticsInitProvider
MobileAdsInitProvider
```

It must also reject `Log.*`, `printStackTrace`, and string interpolation/arguments containing username, password, Cookie, captcha, raw body/XML/HTML, search term, title/author/content ID, review/reply draft, bookshelf, avatar, or profile in production source.

Run:

```powershell
.\gradlew.bat :verification-tools:verifySensitiveSource -Pwenku8Provider=public
```

Expected: FAIL and name current Gradle plugins/dependencies, AD_ID/meta-data, helper/call sites, and raw logs without printing their values.

- [ ] **Step 2: Remove all telemetry/ad build integration**

Delete the Google Services and Crashlytics classpaths/plugins/config blocks, Firebase BOM/analytics/crashlytics and Play Services Ads dependencies, `google-services.json`, AD_ID permission, AdMob application metadata, and auto-initializer merge entries. Remove every `GoogleServicesHelper` initialization/event call and the helper file. `MyApp.onCreate()` must retain only application/context initialization and legacy-safe local setup.

After the edit this search must produce no output:

```powershell
rg -n "Firebase|Crashlytics|GoogleServicesHelper|MobileAds|play-services-ads|AD_ID|APPLICATION_ID" app build.gradle -g '!**/build/**'
```

Expected: exit code 1 because no match exists.

- [ ] **Step 3: Replace raw logging with a closed allowlist**

Use enums only; no free-text production logger:

```kotlin
enum class OperationCode { APP_START, MIGRATION_READ, MIGRATION_WRITE, CACHE_READ, CACHE_WRITE }
enum class FailureClass { NETWORK, PARSE, STORAGE, AUTH, PROTOCOL, UNKNOWN }

fun interface OperationalLogger {
    fun record(operation: OperationCode, failure: FailureClass?, traceId: String)
}

object NoOpOperationalLogger : OperationalLogger {
    override fun record(operation: OperationCode, failure: FailureClass?, traceId: String) = Unit
}
```

Remove verbose reader/layout/parser logs rather than translating content into another sink. Where retained code needs a failure signal, generate a random UUID trace ID and record only the enum tuple. Never pass an exception, stack trace, URL, ID, title, query, username, body, or filesystem path.

- [ ] **Step 4: Implement local-only bounded migration diagnostics**

```kotlin
enum class MigrationOutcome { STARTED, COMPLETED, FAILED }
enum class ReconciliationOutcome { CLEAN, REPAIRED, PENDING, FAILED }
enum class PendingBucket { ZERO, ONE_TO_TEN, ELEVEN_TO_HUNDRED, OVER_HUNDRED }

data class MigrationDiagnostic(
    val day: java.time.LocalDate,
    val migration: MigrationOutcome,
    val reconciliation: ReconciliationOutcome,
    val pending: PendingBucket,
)
```

Persist only these enum names and UTC day in a local file with no network sink. `MigrationDiagnosticsPropertyTest` reflects every serialized key and asserts the exact set `{day,migration,reconciliation,pending}`, rejects arbitrary strings/high-cardinality values, and proves the only export API returns bytes to a caller after preview; it never launches or sends by itself.

- [ ] **Step 5: Document and test the secret boundary**

Create `docs/security/secret-handling.md` with the exact environment-only names `WENKU8_LIVE_USERNAME` and `WENKU8_LIVE_PASSWORD`, the read-only/reversible/persistent live gates, and prohibitions on source, Gradle/local properties, fixtures, logs, screenshots, SavedState, WorkManager, analytics, crash metadata, command output, and commits. Do not include credential examples or values. A documentation test asserts those allowed variable names exist and rejects assignments/value-like examples.

- [ ] **Step 6: Verify merged manifests, bytecode, logs, and no secret retention**

```powershell
.\gradlew.bat :app:processAlphaDebugMainManifest :app:assembleAlphaDebug :app:testAlphaDebugUnitTest :verification-tools:verifySensitiveSource -Pwenku8Provider=public
rg -n "Firebase|Crashlytics|MobileAds|AD_ID|Log\.|printStackTrace" app\build\intermediates\merged_manifests app\build\intermediates\merged_manifest app\src\main -g '*'
```

Expected: Gradle/tests PASS and `rg` exits 1. Unzip/decompile verification must also report zero forbidden class/resource strings.

- [ ] **Step 7: Commit privacy cleanup**

```powershell
git add build.gradle app verification-tools ..\..\docs\security\secret-handling.md
git diff --check
git commit -m "privacy: remove telemetry ads and sensitive logging"
```

## Task 8: Build the SBOM, Provenance, Notice, Source Offer, and SlidingLayout License Blocker

**Files:**

- Modify: `studio-android/LightNovelLibrary/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify/Delete: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/slider/**`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/activity/Wenku8ReaderActivityV1.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/reader/legacydeck/{LegacyReaderPageSource,LegacyReaderPageDeck}.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/reader/legacydeck/LegacyReaderPageDeckTest.kt`
- Create: `docs/licenses/{source-asset-provenance,license-policy}.yaml`
- Create: `NOTICE`, `SOURCE_OFFER.md`, and `studio-android/LightNovelLibrary/app/src/main/res/raw/notice.txt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/global/GlobalConfig.kt`
- Modify: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/activity/AboutActivity.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/license/{ProvenanceScanner,LicensePolicy,PackagedLicenseVerifier,NoticeGenerator}.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/license/LicenseLedgerTest.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/license/PackagedLicenseTest.kt`

Add exact dispatchers for the registered tasks: `generateSbom -> ProvenanceScanner.generateSbom(projectRoot, docsRoot)`, `generateNotices -> NoticeGenerator.generate(projectRoot, docsRoot)`, and `verifyPackagedLicenses -> PackagedLicenseVerifier.verify(projectRoot, docsRoot)`. Each branch returns immediately; a generation branch may update only its declared deterministic output files.

- [ ] **Step 1: Write a packaging test that treats unknown as fatal**

The test supplies source, Maven, font, image, fixture, plugin, and generated-notice entries with `UNKNOWN`, missing, incompatible, or absent hashes. Every case must fail. It also supplies an APK entry absent from the ledger and a ledger entry absent from the APK; both fail. Explicitly assert the current `org/mewx/wenku8/reader/slider/**` family is a production blocker until replaced or backed by verified permission.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.license.*"
```

Expected: FAIL because no complete ledger/verifier exists.

- [ ] **Step 2: Generate source/dependency/asset provenance and CycloneDX/SPDX SBOMs**

Each `source-asset-provenance.yaml` row is encoded from this exact type; the generator supplies real reviewed values and content hashes:

```kotlin
data class ProvenanceRow(
    val path: String,
    val sha256: String,
    val origin: String,
    val version: String,
    val copyrightOwner: String,
    val spdx: String,
    val modified: Boolean,
    val requiredNotice: String,
    val sourceOffer: String,
    val allowedScopes: Set<String>,
    val resolutionOwner: String,
)
```

Use one row per vendored source file/directory, Maven/plugin coordinate, native library, font, image, fixture, and generated notice. POM/license metadata is evidence to review, not automatic approval. Wild source and fixtures must be absent.

Configure CycloneDX to generate public JSON/XML SBOMs with serial numbers/timestamps normalized by the postprocessor and merge them with source/asset rows. The package gate compares APK/AAB entries and resolved coordinates against the approved ledger.

- [ ] **Step 3: Resolve the SlidingLayout blocker by independent replacement, not relabeling**

If verifiable provenance/permission is obtained, store only its external evidence hash and reviewed SPDX conclusion. Otherwise use the mandatory replacement path below; never assign an SPDX ID by guess.

First write behavior tests for current user-visible next/previous/tap/state restoration and first/last boundary callbacks. Then delete `reader/slider/SlidingLayout.kt`, `SlidingAdapter.kt`, and `reader/slider/base/**`. Create a newly authored component from the behavior tests, not a mechanical translation:

```kotlin
interface LegacyReaderPageSource {
    fun currentView(): android.view.View
    fun previewPrevious(): android.view.View?
    fun previewNext(): android.view.View?
    fun commitPrevious()
    fun commitNext()
    fun saveState(): android.os.Parcelable?
    fun restoreState(state: android.os.Parcelable?)
}

class LegacyReaderPageDeck @JvmOverloads constructor(
    context: android.content.Context,
    attrs: android.util.AttributeSet? = null,
) : android.widget.FrameLayout(context, attrs) {
    var onPageChanged: (() -> Unit)? = null
    var onBoundary: ((Boolean) -> Unit)? = null
    private var source: LegacyReaderPageSource? = null

    fun bind(source: LegacyReaderPageSource) {
        this.source = source
        removeAllViews()
        addView(source.currentView(), LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    fun showNext() = move(forward = true)
    fun showPrevious() = move(forward = false)

    private fun move(forward: Boolean) {
        val pages = requireNotNull(source)
        val incoming = if (forward) pages.previewNext() else pages.previewPrevious()
        if (incoming == null) { onBoundary?.invoke(forward); return }
        if (forward) pages.commitNext() else pages.commitPrevious()
        removeAllViews()
        addView(pages.currentView(), LayoutParams(MATCH_PARENT, MATCH_PARENT))
        onPageChanged?.invoke()
    }
}
```

Add an independently authored gesture detector/translation animation in this file, with keyboard/accessibility actions calling `showNext/showPrevious`. Refactor the Activity's existing paginator state into a `LegacyReaderPageSource`; do not copy code from the deleted family. Tests must prove saved paginator state and current page semantics before deleting the old files.

- [ ] **Step 4: Generate notices/source offer and verify the APK**

`NoticeGenerator` sorts by SPDX/origin/path and deterministically writes root `NOTICE`, packaged `res/raw/notice.txt`, and `SOURCE_OFFER.md`. The source offer states repository/version, corresponding-source location/delivery method, validity period, and contact without claiming rights absent from the ledger. Change the existing in-app license reader/About surface to load only the generated `R.raw.notice`; an instrumentation assertion compares displayed normalized text hash with packaged notice/SBOM evidence.

Run:

```powershell
.\gradlew.bat :app:assembleAlphaDebug :verification-tools:generateSbom :verification-tools:generateNotices :verification-tools:verifyPackagedLicenses -Pwenku8Provider=public
```

Expected: PASS only with zero `UNKNOWN`/missing/incompatible rows, no bytecode from the removed SlidingLayout family in the APK, and notice/source-offer hashes matching the SBOM and packaged resource.

- [ ] **Step 5: Commit licensing evidence and replacement**

```powershell
git add build.gradle app verification-tools ..\..\docs\licenses ..\..\NOTICE ..\..\SOURCE_OFFER.md
git diff --check
git commit -m "compliance: establish packaged license provenance"
```

## Task 9: Generate the Public Outbound Manifest and Enforce Audited Egress

**Files:**

- Create: `docs/verification/outbound-network-manifest.yaml`
- Create: `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/network/policy/{LegacyOutboundPolicy,LegacyNetworkFactory}.kt`
- Create: `studio-android/LightNovelLibrary/app/src/main/res/xml/network_security_config.xml`
- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`
- Modify: every retained direct `URLConnection`, OkHttp, WebView, socket, and legacy network factory call site found by inventory.
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/network/{OutboundManifestVerifier,BytecodeEgressScanner}.kt`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/network/EgressHarnessTest.kt`

Add the exact dispatcher `if (command == "verifyOutboundManifest") { OutboundManifestVerifier.verify(projectRoot, docsRoot); return }` before invoking the registered task.

- [ ] **Step 1: Add RED static and runtime egress checks**

The static scanner rejects production construction of `OkHttpClient`, `URL.openConnection`, `HttpURLConnection`, `WebView`, raw sockets, and DNS outside `LegacyNetworkFactory`/the selected logical `:api`. The runtime harness injects a recording DNS/proxy into the factory, launches every A01-A15/F01-F06 owner and every visible action/state from the UI ledger, and rejects any origin or payload class absent from the outbound manifest.

Run:

```powershell
.\gradlew.bat :verification-tools:verifyOutboundManifest :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.network.EgressHarnessTest -Pwenku8Provider=public
```

Expected: FAIL and list current unaudited constructors/origins by source location, never payload values.

- [ ] **Step 2: Define a deny-by-default manifest and policy**

Each JSON-compatible YAML row records:

```json
{"origin":"https://wenku8.mewx.org:443","purpose":"manual-update-alpha-baidu","dataClasses":["app-version"],"authenticated":false,"owner":"legacy-update","flavors":["alpha","baidu"],"legalBasis":"authorization-scope-id","retirementOwner":"feature-settings"}
```

Do not add analytics/ad/crash origins. Do not add `app.wenku8.com` or any cleartext origin. Wenku8 content/account origins stay disabled until Phase 2 has accepted operation evidence and authorization.

`LegacyOutboundPolicy` accepts only `https`, normalized DNS hosts, port 443, an exact allowlisted host/path-purpose/flavor tuple, and no userinfo/literal IP. `LegacyNetworkFactory` is the only legacy constructor and checks policy before opening a connection. A denial returns a typed local error without DNS or socket activity.

- [ ] **Step 3: Enforce network security and route all retained traffic**

Use this base network security policy:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

Set `android:usesCleartextTraffic="false"` and `android:networkSecurityConfig="@xml/network_security_config"`. Route retained manual update traffic through `LegacyNetworkFactory`. Public stub content/account helpers remain zero-egress; protected private CI supplies its own audited policy only in its ephemeral overlay.

- [ ] **Step 4: Exercise every route boundary and prove zero third-party traffic**

```powershell
.\gradlew.bat :verification-tools:verifyOutboundManifest :app:assembleAlphaDebug :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.network.EgressHarnessTest -Pwenku8Provider=public
```

Expected: PASS for cold start, navigation, search, detail, reader, migration, login, profile, bookshelf, review compose/reply, error, and process restart. Captured destinations are a subset of manifest rows; analytics/ad/crash count is zero; public stub content/account network count is zero.

- [ ] **Step 5: Commit the egress boundary**

```powershell
git add app verification-tools ..\..\docs\verification\outbound-network-manifest.yaml
git diff --check
git commit -m "security: enforce declared outbound network policy"
```

## Task 10: Make Backup Boundaries Physical, Explicit, and Testable

**Files:**

- Modify: `studio-android/LightNovelLibrary/app/src/main/AndroidManifest.xml`
- Create: `studio-android/LightNovelLibrary/app/src/main/res/xml/{data_extraction_rules,backup_rules}.xml`
- Create: `studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/backup/BackupBoundaryInstrumentedTest.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/inventory/BackupRulesVerifier.kt`
- Modify: `docs/verification/backup-manifest.yaml`

- [ ] **Step 1: Add a RED rule test for every secret/transient physical store**

Parse both Android rule files structurally and require whole-file/store exclusions for `files/saves/cert.wk8`, cache data, future `session-store`, and `migration-transient.db` plus WAL/SHM/journal sidecars. Reject row-level/table predicates because Android backup rules cannot express them. Require `settings.wk8`, modern-reader preferences, progress, bookshelf, and downloaded content to have an explicit whole-store inclusion decision in the backup manifest.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "*.BackupRulesVerifier*"
```

Expected: FAIL because the current manifest has `allowBackup=true` with no explicit rules.

- [ ] **Step 2: Add valid Android 12+ and legacy backup rules**

Create `data_extraction_rules.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup disableIfNoEncryptionCapabilities="true">
        <exclude domain="file" path="saves/cert.wk8" />
        <exclude domain="file" path="session-store" />
        <exclude domain="database" path="migration-transient.db" />
        <exclude domain="database" path="migration-transient.db-wal" />
        <exclude domain="database" path="migration-transient.db-shm" />
        <exclude domain="database" path="migration-transient.db-journal" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="file" path="saves/cert.wk8" />
        <exclude domain="file" path="session-store" />
        <exclude domain="database" path="migration-transient.db" />
        <exclude domain="database" path="migration-transient.db-wal" />
        <exclude domain="database" path="migration-transient.db-shm" />
        <exclude domain="database" path="migration-transient.db-journal" />
    </device-transfer>
</data-extraction-rules>
```

Create equivalent `backup_rules.xml` with `<full-backup-content>` and the same `file`/`database` excludes. Cache and `noBackupFilesDir` are excluded by platform physical domain and must be asserted by the instrumentation test rather than represented with invalid rule domains.

Set both attributes:

```xml
android:allowBackup="true"
android:fullBackupContent="@xml/backup_rules"
android:dataExtractionRules="@xml/data_extraction_rules"
```

- [ ] **Step 3: Verify merged rules and a real backup/restore boundary**

The instrumentation test writes unique non-secret markers into an includable settings/progress fixture and secret markers into every excluded physical path. A host harness runs `bmgr backupnow`, clears/reinstalls, restores, and asserts included markers return while every excluded marker is absent. It must also assert the restored app is signed out.

Run on API 31+:

```powershell
.\gradlew.bat :app:assembleAlphaDebug :app:assembleAlphaDebugAndroidTest -Pwenku8Provider=public
adb install -r app\build\outputs\apk\alpha\debug\app-alpha-debug.apk
adb shell bmgr enable true
adb shell am instrument -w -e stage seed org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
adb shell bmgr backupnow org.mewx.wenku8
adb shell pm clear org.mewx.wenku8
$restoreLine = adb shell bmgr list sets | Select-String -Pattern '^[0-9a-fA-F]+\s*:' | Select-Object -First 1
if ($null -eq $restoreLine) { throw 'No backup restore set is available' }
$restoreToken = ($restoreLine.Line -split ':')[0].Trim()
adb shell bmgr restore $restoreToken org.mewx.wenku8
adb shell am instrument -w -e stage verify org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner
```

Expected: PASS with retained backup transport/dataset/report IDs; no credential/session/transient marker survives.

- [ ] **Step 4: Commit backup contracts**

```powershell
git add app\src\main\AndroidManifest.xml app\src\main\res\xml app\src\androidTest\java\org\mewx\wenku8\backup verification-tools ..\..\docs\verification\backup-manifest.yaml
git diff --check
git commit -m "security: define physical backup boundaries"
```

## Task 11: Lock Repositories, Dependencies, Wrapper, Origins, and Reproducibility

**Files:**

- Modify: `studio-android/LightNovelLibrary/settings.gradle`
- Modify: `studio-android/LightNovelLibrary/build.gradle`
- Modify: every module `build.gradle`
- Create: `studio-android/LightNovelLibrary/gradle/libs.versions.toml`
- Modify: `studio-android/LightNovelLibrary/gradle/wrapper/gradle-wrapper.properties`
- Create: `studio-android/LightNovelLibrary/gradle/verification-metadata.xml`
- Create: `studio-android/LightNovelLibrary/**/gradle.lockfile`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/supplychain/{RepositoryPolicyVerifier,ResolvedOriginVerifier,ReproducibilityVerifier}.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/supplychain/RepositoryPolicyTest.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/supplychain/DependencyVerificationTest.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/supplychain/ReproducibleArtifactTest.kt`
- Modify: `.github/workflows/android-ci.yml`

The `phase0VerifierCommands` list already registers `resolvedOriginReport`, `recordPackagedInputs`, and `verifyReproducibleInputs` as literal `JavaExec` tasks. Configure the one task-specific argument without creating a second registration:

```groovy
tasks.named('recordPackagedInputs', JavaExec) {
    doFirst {
        def part = providers.gradleProperty('part').orNull
        if (!(part in ['online', 'offline'])) {
            throw new GradleException('WENKU8-REPRO-E001: part must be online or offline')
        }
        args part
    }
}
```

Add exact dispatcher branches after common argument parsing: `resolvedOriginReport` accepts zero command-specific arguments and calls `ReproducibilityVerifier.writeResolvedOriginReport(projectRoot, docsRoot)`; `recordPackagedInputs` accepts exactly one `online|offline` argument and calls `ReproducibilityVerifier.recordPackagedInputs(projectRoot, docsRoot, part)`; `verifyReproducibleInputs` accepts zero and calls `ReproducibilityVerifier.verifyInputs(projectRoot, docsRoot)`. Any other arity/value fails with a constant code. `verifyPlannedGradleTasks` must pass before Task 11 commits.

- [ ] **Step 1: Write failing supply-chain policy tests**

Tests reject project repositories, Aliyun/silent mirrors, `mavenLocal`, dynamic/ranged/changing/SNAPSHOT versions, unpinned plugins, missing verification checksums, unlocked resolvable configurations, wrapper URL/hash mismatch, and a resolved artifact whose repository origin differs from its approved coordinate origin. A mirror fixture is accepted only when its coordinate filter is explicit and bytes match the pinned upstream checksum.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.supplychain.*"
```

Expected: FAIL on current `allprojects.repositories`, mirror declarations, literal dependency versions, absent locks/verification metadata, and absent wrapper checksum.

- [ ] **Step 2: Centralize plugins, versions, and approved repositories**

Start `gradle/libs.versions.toml` with exact versions already in use plus Phase 0 tools:

```toml
[versions]
agp = "9.0.1"
kotlin = "2.2.10"
compose-bom = "2026.06.00"
activity-compose = "1.12.4"
material = "1.13.0"
junit4 = "4.13.2"
jacoco = "0.8.13"
snakeyaml = "2.9"
cyclonedx = "2.4.0"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
cyclonedx = { id = "org.cyclonedx.bom", version.ref = "cyclonedx" }
```

Move every existing coordinate/version to named aliases; retain no duplicate literal version. Use official origins only:

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
        google { content { includeGroupByRegex 'androidx\\..*'; includeGroupByRegex 'com\\.android\\..*'; includeGroupByRegex 'com\\.google\\..*' } }
        mavenCentral()
    }
}
```

If a coordinate cannot resolve from its approved official origin, stop and document the coordinate/license gap; do not silently restore a mirror.

Convert every Groovy property call to assignment syntax, remove the eight obsolete AGP properties after adopting/testing their documented defaults, disable Jetifier after dependency verification proves no support-library transform is needed, and set `android.dependency.excludeLibraryComponentsFromConstraints=true`. Regenerate the warning log; remove resolved rows from `warning-baseline.json` rather than retaining zero-value exemptions.

- [ ] **Step 3: Pin and validate the Gradle wrapper**

Add the official Gradle 9.1.0 all-distribution hash:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.1.0-all.zip
distributionSha256Sum=b84e04fa845fecba48551f425957641074fcc00a88a84d2aae5808743b35fc85
```

Add `gradle/actions/wrapper-validation@v4` before every build. Record the current wrapper JAR SHA-256 `e2b82129ab64751fd40437007bd2f7f2afb3c6e41a9198e628650b22d5824a14` and require the action to validate it against the official wrapper release.

- [ ] **Step 4: Enable locks and strict dependency verification**

Apply to every project/configuration, including buildscript/plugin and test configurations:

```groovy
allprojects {
    dependencyLocking { lockAllConfigurations() }
    configurations.configureEach {
        resolutionStrategy {
            failOnDynamicVersions()
            failOnChangingVersions()
            cacheChangingModulesFor 0, 'seconds'
        }
    }
}
```

Generate, review, and check in locks and strict SHA-256/PGP metadata:

```powershell
.\gradlew.bat help --write-locks -Pwenku8Provider=public
.\gradlew.bat :app:dependencies :verification-tools:dependencies --write-locks -Pwenku8Provider=public
.\gradlew.bat :app:assembleAlphaDebug :app:testAlphaDebugUnitTest --write-verification-metadata sha256,pgp -Pwenku8Provider=public
```

Expected: generated files have no unreviewed `trusted-artifacts` wildcard and every resolvable public production/test/plugin artifact is locked and verified. Protected private CI generates an additive overlay, never edits public rows.

- [ ] **Step 5: Prove clean-cache online to verified offline reproducibility**

Use one disposable Gradle user home for the initial online resolution and reuse it offline after deleting build outputs:

```powershell
$env:GRADLE_USER_HOME = Join-Path $env:TEMP 'wenku8-phase0-repro'
Remove-Item -LiteralPath $env:GRADLE_USER_HOME -Recurse -Force -ErrorAction SilentlyContinue
.\gradlew.bat --no-daemon --refresh-dependencies :app:assembleAlphaDebug :verification-tools:resolvedOriginReport -Pwenku8Provider=public
.\gradlew.bat :verification-tools:recordPackagedInputs -Ppart=online -Pwenku8Provider=public
.\gradlew.bat clean
.\gradlew.bat --no-daemon --offline :app:assembleAlphaDebug :verification-tools:recordPackagedInputs -Ppart=offline -Pwenku8Provider=public
.\gradlew.bat :verification-tools:verifyReproducibleInputs -Pwenku8Provider=public
```

Expected: PASS; dependency graphs, resolved coordinate/origin/checksum inventory, normalized packaged input hashes, toolchain/JDK/AGP/Gradle hashes, SBOM and lock state are identical. Reports are retained under `build/reports/phase0-provenance/`.

- [ ] **Step 6: Commit the locked build**

```powershell
git add settings.gradle build.gradle app\build.gradle api-stub\build.gradle verification-tools\build.gradle gradle gradle.lockfile app\gradle.lockfile api-stub\gradle.lockfile verification-tools\gradle.lockfile ..\..\.github\workflows\android-ci.yml
git diff --check
git commit -m "build: lock verified dependency origins"
```

## Task 12: Add Protected Private Overlay and Fresh Signed Attestation Verification

**Files:**

- Create: `docs/compliance/{private-overlay-schema,private-attestation-trust}.yaml`
- Create: `docs/compliance/keys/private-ci-ed25519-2026.pub`
- Create: `docs/verification/private-attestation-nonce-registry.json`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/attestation/{CanonicalJson,PrivateOverlayMerger,PrivateAttestation,PrivateAttestationVerifier,NonceRegistry}.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/attestation/*Test.kt`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/test/resources/attestation/{valid,wrong-artifact,wrong-commit,wrong-variant,wrong-policy,expired,future,replayed,stale-approval,unknown-key,revoked-key,schema-downgrade}.json`
- Create: `.github/workflows/protected-private-compliance.yml`
- Create: `.github/CODEOWNERS`

Add the exact dispatcher `if (command == "verifyPrivateAttestationFixtures") { PrivateAttestationVerifier.verifyFixtures(projectRoot, docsRoot); return }` before invoking the registered task.

The other two attestation commands are also registered once by `phase0VerifierCommands`. Configure their path arguments and the retained root compatibility entry explicitly:

```groovy
// verification-tools/build.gradle
tasks.named('verifySharedAttestationNonDisclosure', JavaExec) {
    doFirst {
        def attestation = providers.gradleProperty('attestation').orNull
        def signature = providers.gradleProperty('signature').orNull
        if ([attestation, signature].any { it == null || it.isBlank() }) {
            throw new GradleException('WENKU8-ATTEST-E001: attestation and signature paths are required')
        }
        args attestation, signature
    }
}
tasks.named('verifyPrivateAttestation', JavaExec) {
    doFirst {
        def attestation = providers.gradleProperty('privateAttestation').orNull
        def artifact = providers.gradleProperty('privateArtifact').orNull
        if ([attestation, artifact].any { it == null || it.isBlank() }) {
            throw new GradleException('WENKU8-ATTEST-E002: attestation and artifact paths are required')
        }
        args attestation, artifact
    }
}

// root build.gradle; retained for the protected Phase 2 command.
tasks.register('verifyPrivateAttestation') {
    group = 'verification'
    dependsOn ':verification-tools:verifyPrivateAttestation'
}
```

Both dispatcher branches require exactly two command-specific path arguments, resolve them beneath the declared workspace/evidence roots without logging them, and call `PrivateAttestationVerifier.verifySharedNonDisclosure(...)` or `PrivateAttestationVerifier.verifyBoundArtifact(...)` respectively. Unknown fields, extra arguments, or escaped paths fail with constant codes.

- [ ] **Step 1: Write every required negative attestation fixture first**

Tests use an ephemeral Ed25519 key pair and fixed clock. The valid fixture passes exactly once; each named negative fixture fails for its named reason. The shared/public artifact scan rejects raw overlay digests, private coordinates/endpoints, credentials, protected compliance rows, response bodies, and guessable identifiers.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.attestation.*"
```

Expected: FAIL because verifier/trust/fixtures do not exist.

- [ ] **Step 2: Define additive overlay and canonical attestation schemas**

The overlay merge key is the stable public row ID. Private rows may add new IDs but cannot replace, delete, loosen, or alter a public row. The signed canonical JSON contains only:

```json
{
  "schemaVersion": 1,
  "toolVersion": "1",
  "providerId": "private",
  "variant": "alphaRelease",
  "sourceCommit": "40-lowercase-hex",
  "publicBaseHash": "64-lowercase-hex",
  "artifactHash": "64-lowercase-hex",
  "protectedPolicyRevision": "opaque-revision",
  "authorizationDecision": "ACCEPTED",
  "authorizationRecheckDate": "2026-07-20",
  "authorizationExpiry": "2026-08-10",
  "issuedAt": "2026-07-10T00:00:00Z",
  "notAfter": "2026-07-17T00:00:00Z",
  "runId": "github-run-attempt",
  "nonce": "128-bit-base64url",
  "keyId": "private-ci-ed25519-2026",
  "result": "PASS",
  "protectedAttestationId": "opaque-random-id",
  "encryptedReportDigest": "opaque-non-guessable-digest"
}
```

`notAfter` is the earliest approval/key/recheck validity. Canonicalization sorts object keys, preserves array order, emits UTF-8/LF/no whitespace, and forbids unknown fields/schema downgrade.

- [ ] **Step 3: Pin trust, rotation, and revocation independently**

`private-attestation-trust.yaml` records algorithm, key ID, public-key SHA-256, validity, successor, revocation date/reason, and out-of-band fingerprint hash. The verifier loads the checked-in public key, checks its hash, rejects same-change untrusted replacement, and requires a known non-revoked key valid at `issuedAt` and now. Add CODEOWNERS rows for authorization/trust/keys requiring the two release-maintainer team reviews; branch protection enabling two approvals is an external Phase 0 exit prerequisite.

- [ ] **Step 4: Implement offline signature, binding, freshness, and replay verification**

The core sequence is fixed:

```kotlin
fun verify(input: ByteArray, signature: ByteArray, expected: ExpectedBinding): VerifiedAttestation {
    val attestation = CanonicalJson.decodeStrict(input)
    require(attestation.providerId == expected.providerId)
    require(attestation.variant == expected.variant)
    require(attestation.sourceCommit == expected.sourceCommit)
    require(attestation.publicBaseHash == expected.publicBaseHash)
    require(attestation.artifactHash == expected.artifactHash)
    require(attestation.protectedPolicyRevision == expected.policyRevision)
    trust.verifyEd25519(attestation.keyId, input, signature, clock.instant())
    freshness.verify(attestation, clock.instant())
    nonceRegistry.consume(attestation.runId, attestation.nonce)
    require(attestation.result == "PASS")
    return VerifiedAttestation(attestation.protectedAttestationId)
}
```

Persist the consumed `(runId, nonce)` pair in the retained release evidence registry. A prior PASS cannot be reused for a new source commit/artifact/run.

- [ ] **Step 5: Build the protected workflow with a one-way redacted output**

The protected GitHub Environment checks out private source, materializes the overlay in `$RUNNER_TEMP`, verifies the additive merge, injects its audited HostPolicy, runs the same tests/supply-chain/license/outbound gates, builds the exact candidate, canonicalizes the attestation, and signs with an environment secret key:

```powershell
$key = Join-Path $env:RUNNER_TEMP 'private-attestation.key'
$attestation = Join-Path $env:RUNNER_TEMP 'attestation.json'
$signature = Join-Path $env:RUNNER_TEMP 'attestation.sig'
openssl pkeyutl -sign -rawin -inkey $key -in $attestation -out $signature
.\gradlew.bat :verification-tools:verifySharedAttestationNonDisclosure "-Pattestation=$attestation" "-Psignature=$signature"
```

Upload only `attestation.json`, `attestation.sig`, approved non-sensitive test summary, and encrypted report digest. Delete the ephemeral overlay/key/workspace in an `if: always()` cleanup step. Never upload private build logs/artifacts to public CI.

- [ ] **Step 6: Verify positive and negative fixtures and commit**

```powershell
.\gradlew.bat :verification-tools:verifyPrivateAttestationFixtures -Pwenku8Provider=public
```

Expected: valid PASSes once; all eleven negative fixtures fail with their named rule; a second valid verification fails replay. Then:

```powershell
git add verification-tools ..\..\docs\compliance ..\..\docs\verification\private-attestation-nonce-registry.json ..\..\.github\workflows\protected-private-compliance.yml ..\..\.github\CODEOWNERS
git diff --check
git commit -m "security: verify protected private attestations"
```

## Task 13: Bind Every Phase 0 Requirement to an Aggregate Evidence Gate

**Files:**

- Modify: `studio-android/LightNovelLibrary/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/build.gradle`
- Modify: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/VerificationMain.kt`
- Modify: `docs/verification/modernization-matrix.yaml`
- Modify: `.github/workflows/android-ci.yml`
- Create: `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/compliance/Phase0Gate.kt`
- Test: `studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/compliance/Phase0GateTest.kt`

- [ ] **Step 1: Add a RED completeness test for the evidence matrix**

List every Phase 0 deliverable/exit statement from Section 11 plus relevant Section 6/8.1.1/8.1.2/8.12/12.2/13 requirements as stable requirement IDs. For each, require exact Gradle task/test ID, provider, flavor/build type, API/device, fixture hash, threshold, retained report path, and source commit. `NOT_RUN`, missing report, stale commit, or a green task not listed for that requirement fails.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "*.Phase0GateTest"
```

Expected: FAIL until every prior task writes authoritative evidence.

- [ ] **Step 2: Register the aggregate task without weakening constituent gates**

`Phase0Gate` verifies file/report hashes and task outcomes; it does not infer success from absence of findings:

```kotlin
data class RequirementEvidence(
    val requirementId: String,
    val taskId: String,
    val provider: String,
    val variant: String,
    val api: Int?,
    val fixtureHash: String?,
    val reportPath: String,
    val reportHash: String,
    val sourceCommit: String,
    val result: String,
)
```

Register the canonical verifier, root entry point, and retained compatibility alias explicitly:

```groovy
// verification-tools/build.gradle
tasks.register('phase0Gate', JavaExec) {
    group = 'verification'
    description = 'Verifies the complete Phase 0 evidence matrix.'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = application.mainClass
    args 'phase0Gate', rootProject.projectDir.absolutePath,
        new File(rootProject.projectDir, '../../docs').canonicalPath
    dependsOn 'verifyWarnings', 'verifyInventories', 'verifySensitiveSource',
        'generateSbom', 'generateNotices', 'verifyPackagedLicenses',
        'verifyOutboundManifest', 'verifyReproducibleInputs',
        'verifyPrivateAttestationFixtures', 'verifyIntentContracts',
        'verifyPlannedGradleTasks', 'verifyXmlSurfaceLedger'
    dependsOn ':app:testAlphaDebugUnitTest', ':app:lintAlphaDebug',
        ':app:assembleAlphaDebug', ':verifyPhase0Coverage'
}

// root build.gradle
tasks.register('phase0Gate') {
    group = 'verification'
    description = 'Canonical root entry point for Phase 0.'
    dependsOn ':verification-tools:phase0Gate'
}

// app/build.gradle; retained only for the already-frozen Phase 3 command.
tasks.register('phase0CompatibilityGate') {
    group = 'verification'
    description = 'Compatibility alias for the canonical Phase 0 gate.'
    dependsOn ':phase0Gate'
}
```

Add the dispatcher branch in `VerificationMain.kt` after parsing `command`, `projectRoot`, and `docsRoot`:

```kotlin
if (command == "phase0Gate") {
    org.mewx.wenku8.verification.compliance.Phase0Gate.verify(projectRoot, docsRoot)
    return
}
```

The matrix verifier checks inventories, warning ratchet, unit/instrumentation/lint, coverage, old-release compatibility, authorization, license/SBOM/notices, backup, outbound, locks/origins/reproducibility, secret scan, and attestation fixture reports. Device, external-signing, and authorization-scoped work remains fail-closed evidence: the aggregate never converts an unavailable prerequisite into `SKIPPED` success.

- [ ] **Step 3: Run public debug/variant and minified compatibility matrices**

```powershell
.\gradlew.bat clean phase0Gate :app:testAlphaDebugUnitTest :app:lintAlphaDebug :app:assembleAlphaDebug :app:assembleBaiduDebug :app:assemblePlaystoreDebug -Pwenku8Provider=public
.\gradlew.bat :app:assembleAlphaRelease :app:assembleBaiduRelease :app:assemblePlaystoreRelease -Pwenku8Provider=public
```

Expected: the first command PASSes after all deterministic evidence is present. Each release PASSes only when its precise authorization and license gates are accepted; otherwise it FAILs closed and Phase 0 exit remains unclaimed. Protected jobs separately run private `alphaDebug`/minified `alphaRelease` and publish a freshly bound attestation.

- [ ] **Step 4: Run device and old-release gates**

Run API 23, API 32, and API 33 instrumentation plus the externally signed old-to-new probe from Task 4. Expected: all PASS; reports include device/system image, APK/cert/mapping/fixture hashes, typed/untyped Bundle path, and source commit.

- [ ] **Step 5: Perform final source, secret, unresolved-marker, and diff checks**

```powershell
rg -n "UnsupportedOperationException\(\"stub\"\)|phase0-unresolved-marker" ..\..\docs\verification ..\..\docs\compliance verification-tools app api-stub
rg -n "Firebase|Crashlytics|MobileAds|AD_ID|google-services" app build.gradle
git diff --check
git status --short
```

Expected: the first two searches exit 1; diff check PASSes; status shows only the intended aggregate-gate/matrix/workflow files before commit. Run the repository-approved secret scanner without placing any credential literal in the command; expected zero findings.

- [ ] **Step 6: Commit Phase 0 evidence binding**

```powershell
git add build.gradle app\build.gradle verification-tools ..\..\docs\verification\modernization-matrix.yaml ..\..\.github\workflows\android-ci.yml
git diff --check
git commit -m "test: bind phase zero release evidence"
```

## Phase 0 Handoff

Do not begin Phase 1 merely because deterministic fixture work passes. Hand the reviewer:

- the thirteen focused commits;
- `phase0Gate` report and modernization matrix;
- old signed/minified compatibility hashes and external signing evidence;
- authorization decision/fail-closed output;
- public SBOM/provenance/license/NOTICE/source-offer and protected attestation;
- clean-cache/verified-offline provenance;
- public runtime egress capture and secret/log scans;
- an explicit list of any gate still denied.

Dispatch independent architecture/API/licensing and migration/testing reviewers. Resolve every Critical or Important finding in this plan/evidence before the Phase 1 implementation plan is executed.
