# Wenku8 Phase 3 Storage and Compatibility Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the versioned Room stores, complete the remaining legacy-data migration protocol, and deliver cancellable API-aware durable migration/download execution without losing old bookshelf, progress, settings, search, catalog, download, or session behavior.

**Architecture:** Keep Phase 1's settings DataStore and migration-transient Room database and Phase 2's encrypted SessionStore; Phase 3 extends rather than replaces them. Backed-up canonical user data, excluded catalog/cache data, and excluded migration journals/checkpoints remain separate physical stores. Every migrated domain uses a writer barrier, stable snapshot, idempotent mutation ID, exactly-once canonical transaction, at-least-once legacy projection, version comparison, and reconciliation; background work runs in bounded chunks through WorkManager on API 23-33 and a user-initiated JobScheduler adapter on API 34-36 for eligible user-started bulk transfers.

**Tech Stack:** Kotlin 2.2.10, Groovy Gradle with version catalog aliases, Android Gradle Plugin 9.0.1, Room 2.8.3, DataStore 1.2.0, WorkManager, Kotlin coroutines 1.10.2, Android JobScheduler/JobService, OkHttp, JUnit4, AndroidX Test, Robolectric, MockWebServer, PowerShell, Gradle, ADB, API 23/29/30/31/32/33/34/35/36 emulators.

---

## Authoritative Inputs

- Approved specification: `docs/superpowers/specs/2026-07-10-wenku8-modernization-program-design.md`, especially Sections 6.2-6.4, 7, 9, 11 Phase 3, 12, 13, 14, and 16.
- Program index: `docs/superpowers/plans/2026-07-10-wenku8-modernization-plan-index.md`.
- Phase 0 evidence manifests and golden artifacts.
- Phase 1 architecture/settings implementation and Phase 2 SessionStore implementation named below.
- Active project root for every Gradle command: `studio-android/LightNovelLibrary/`.
- Repository root for Git, host harness, and verification-manifest commands: repository root.

This phase performs no live Wenku8 request and needs no account credential. All network tests use MockWebServer or a fake transfer source. Never place the authorized test username, password, Cookie, captcha, private endpoint, or raw authenticated response in a command, source file, fixture, log, screenshot, report, WorkManager data, JobScheduler extras, or commit.

## File Structure

The paths below are fixed. Keep each file focused; do not collapse databases, migration protocol, Android schedulers, or host verification into one file.

### Build And Verification

- Modify `studio-android/LightNovelLibrary/gradle/libs.versions.toml`: add the pinned WorkManager and Robolectric coordinates used by this phase; retain Phase 1 Room/KSP aliases.
- Modify `studio-android/LightNovelLibrary/core/storage/build.gradle`: add Room schema export/test assets and WorkManager test dependencies needed by storage workers.
- Modify `studio-android/LightNovelLibrary/core/network/build.gradle`: add MockWebServer test support for Range semantics.
- Modify `studio-android/LightNovelLibrary/core/data/build.gradle`: add WorkManager runtime/testing and dependencies on storage/network/domain.
- Modify `studio-android/LightNovelLibrary/app/build.gradle`: add the release-like `phase3Harness` build type and instrumented-test dependencies.
- Create `docs/verification/phase-3-storage-contract.yaml`: authoritative store, schema, migration-domain, scheduler, retry, backup, and device-gate IDs.
- Modify `docs/verification/modernization-matrix.yaml`: map each Phase 3 requirement to a task, test ID, API, fixture hash, and retained report path.
- Create `.github/workflows/phase-3-device-matrix.yml`: controlled API 23-36 verification jobs; destructive reboot/force-stop scenarios run nightly and on signed release candidates.

### Canonical And Cache Room Stores

- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryEntities.kt`: bookshelf, search, progress, download, domain-version, and applied-mutation rows with canonical/projection versions.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryDaos.kt`: domain DAOs, monotonic mutation-version reservation, version-guarded delete/clear/projection acknowledgement, and exactly-once terminal-commit queries.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryDatabase.kt`: backed-up canonical Room database.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/catalog/CatalogEntities.kt`: normalized novel, volume, chapter, and cache-metadata rows.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/catalog/CatalogDaos.kt`: metadata/catalog/cache DAOs.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/catalog/CatalogCacheDatabase.kt`: excluded rebuildable catalog/cache Room database.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/DatabasePaths.kt`: the three physical database names and exclusion assertions.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/db/StorageDatabaseFactory.kt`: opens user, catalog/cache, and the existing Phase 1 transient database without aliasing paths.
- Create `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/db/EntityContractTest.kt`: keys, forbidden-field, account-partition, and version contract tests.
- Create `studio-android/LightNovelLibrary/core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/UserLibraryDatabaseTest.kt`: transactional DAO behavior.
- Create `studio-android/LightNovelLibrary/core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/CatalogCacheDatabaseTest.kt`: ordering, parser-revision, TTL, and cache-partition behavior.
- Create `studio-android/LightNovelLibrary/core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/DatabaseUpgradeTest.kt`: upgrade tests from every checked-in schema.
- Check in generated schemas under `studio-android/LightNovelLibrary/core/storage/schemas/org.mewx.wenku8.core.storage.db.user.UserLibraryDatabase/` and `studio-android/LightNovelLibrary/core/storage/schemas/org.mewx.wenku8.core.storage.db.catalog.CatalogCacheDatabase/`.

### Legacy Compatibility And Migration Protocol

- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyFileSystem.kt`: injected read/list/atomic-replace/copy interface.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/AndroidLegacyFileSystem.kt`: file/SAF implementation with temp-write and recoverable prior copy.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyPathPolicy.kt`: Phase 0 artifact-manifest-driven primary/backup/sentinel lookup.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacySaveCodec.kt`: facade over focused search/bookshelf/progress/catalog XML codecs.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacySearchHistoryCodec.kt`.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyBookshelfCodec.kt`.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyReaderProgressCodec.kt`.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyCatalogXmlCodec.kt`.
- Create `studio-android/LightNovelLibrary/core/storage/src/test/resources/legacy/search_history/`, `legacy/bookshelf/`, `legacy/progress/`, and `legacy/catalog/`: copied only from approved Phase 0 byte fixtures, including malformed-but-preserved records.
- Create focused tests under `studio-android/LightNovelLibrary/core/storage/src/test/java/org/mewx/wenku8/core/storage/legacy/` for byte/semantic round trips and path precedence.
- Modify the Phase 1 `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationTransientDatabase.kt`: add generic state, snapshot, checkpoint, journal/outbox, and transfer-lease tables while retaining Phase 1 settings tables.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationRecords.kt`: generic domain, immutable-snapshot entry, import checkpoint, legacy-write reservation/delta, canonical-first journal/outbox, and transfer-lease entities/enums.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationDao.kt`: compare-and-set transitions, pending journals, checkpoints, and leases.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationSnapshotStore.kt`: verified immutable snapshot-handle/read/delete contract.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/AndroidMigrationSnapshotStore.kt`: atomic excluded snapshots under `noBackupFilesDir/wenku8-migration/snapshots/`.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/DomainWriteBarrier.kt`: per-domain/scope structured writer serialization.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationParticipant.kt`: settings/session/remaining-domain integration boundary.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/JournaledMutationRunner.kt`: append, canonical commit, projection, and reconciliation protocol.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationCoordinator.kt`: state-machine orchestration and bounded chunk selection.
- Create `studio-android/LightNovelLibrary/core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/RestoreReconciler.kt`: journal-independent repair from canonical/projection versions after restore.
- Create storage-owned participants under `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/`: `SessionMigrationParticipant.kt`, `SearchMigrationParticipant.kt`, `BookshelfMigrationParticipant.kt`, `ReaderProgressMigrationParticipant.kt`, and `DownloadCatalogMigrationParticipant.kt`.
- Create `core/data/src/main/java/org/mewx/wenku8/core/data/migration/participants/SettingsMigrationParticipant.kt`: adapts the Phase 1 core-data settings coordinator without introducing a storage-to-data dependency cycle.
- Create focused protocol tests under `core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/` and device process/restore tests under `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/`.
- Create `studio-android/LightNovelLibrary/app/src/main/java/org/mewx/wenku8/compat/storage/LegacyStorageGateway.kt`: the only retained legacy-route write entry; it delegates to migration participants and never writes a file directly.
- Create `docs/verification/phase-3-writer-routing.yaml`: exact one-to-one mapping from every Phase 0 artifact-writer ID to a typed gateway operation and physical projection owner.
- Modify every Phase 0 writer-ledger target, including `GlobalConfig.kt`, reader progress stores, search, bookshelf, settings, intro/catalog, and download call sites, until the writer-verifier proves no bypass.
- Modify `app/src/main/java/org/mewx/wenku8/compat/LegacyCompatibilityRegistry.kt`: register the existing Phase 1 `LegacyIntentCodec` and `LegacySettingsAdapter`, the Phase 2 credential/session adapter, and Phase 3 path/progress/bookshelf/catalog adapters.

### Durable Work And Transfers

- Create `core/data/src/main/java/org/mewx/wenku8/core/data/work/ChunkBudget.kt`: item/time bounds with injectable monotonic clock.
- Create `core/data/src/main/java/org/mewx/wenku8/core/data/work/MigrationChunkWorker.kt` and `ReconciliationChunkWorker.kt`: stable unique work, checkpoint after each item, and reschedule before 7 minutes 30 seconds.
- Create `core/network/src/main/kotlin/org/mewx/wenku8/core/network/transfer/RangeTransferClient.kt`: cancellable allowlisted GET/Range response validation.
- Create `core/storage/src/main/java/org/mewx/wenku8/core/storage/transfer/PartialFileStore.kt`: `.partial` writes, fsync, hash verification, and atomic replacement.
- Create `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/TransferModels.kt`, `TransferChunkRunner.kt`, `TransferScheduler.kt`, and `DefaultTransferScheduler.kt`: safe at-least-once transfer and API policy.
- Create `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/ForegroundTransferWorker.kt`: API 23-33 user-started foreground path and regular/expedited fallback.
- Create `app/src/main/java/org/mewx/wenku8/work/TransferNotificationFactory.kt`: bounded progress/cancel notification with no account/content text.
- Create `app/src/main/java/org/mewx/wenku8/work/UidtTransferJobService.kt`: API 34-36 JobService lifecycle, stop reason, cancellation, lease, retry, and exactly one `jobFinished` call.
- Create `app/src/main/java/org/mewx/wenku8/work/AndroidTransferScheduler.kt`: JobInfo/WorkRequest construction and eligibility fallback.
- Modify `app/src/main/AndroidManifest.xml`: exact WorkManager foreground and UIDT declarations/permissions.
- Create the exact scheduler, worker, and transfer test files named in Tasks 11 and 13-19 under the `core/data`, `core/network`, `core/storage`, and `app` test roots.

### Backup, Restore, And Host Evidence

- Modify `app/src/main/res/xml/data_extraction_rules.xml` and `app/src/main/res/xml/backup_rules.xml`: include whole canonical user/DataStore physical stores and exclude whole session, credential, catalog/cache, parser-cache, and migration-transient stores.
- Create `app/src/androidTest/java/org/mewx/wenku8/backup/Phase3BackupBoundaryTest.kt`: physical rule/path and post-canonical/pre-projection assertions.
- Create `app/src/phase3Harness/java/org/mewx/wenku8/harness/Phase3HarnessBindings.kt`: named pause/fault bindings compiled only in the release-like harness variant.
- Create `app/src/androidTest/java/org/mewx/wenku8/harness/Phase3StageATest.kt` and `Phase3StageBTest.kt`: seed and independently verify durable state.
- Create `tools/phase3-device-harness.ps1`: emulator-only multi-stage Gradle/ADB driver for jobscheduler stop/timeout/run, process kill, force-stop, and reboot.
- Create `tools/verify-phase3-writers.ps1` plus `verification-tools/src/main/kotlin/org/mewx/wenku8/verification/phase3/Phase3WriterRoutingVerifier.kt` and its matching test: structurally parse both writer manifests, fail when any legacy route bypasses `LegacyStorageGateway`, and reject forbidden non-secret deletion.
- Create `studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/phase3/Phase3MatrixVerifier.kt` and its matching test: structured matrix/command/test/evidence verification used by `tools/verify-phase3-matrix.ps1`.

## Preconditions And Stop Conditions

### Required Before Task 1

- [ ] `git rev-parse --verify 3c8fa142^{commit}` succeeds, or the executor records the reviewed successor commit containing the same or stricter specification.
- [ ] Phase 0 exit evidence is green, including the complete artifact/path/writer ledgers and old-signed/minified compatibility fixtures.
- [ ] Phase 1 exit evidence is green and these files exist: `SettingsPreferencesDataSource.kt`, `LegacySettingsCodec.kt`, `LegacySettingsAdapter.kt`, `SettingsWriteBarrier.kt`, `SettingsMigrationCoordinator.kt`, `SettingsReconciler.kt`, `MigrationTransientDatabase.kt`, `AppContainer.kt`, and `DefaultAppContainer.kt`.
- [ ] Phase 1 physically stores `app_settings.preferences_pb` at `files/datastore/app_settings.preferences_pb`; it stores `migration-transient.db` as its own Room database and excludes that complete database plus `-wal`, `-shm`, and `-journal` files from backup.
- [ ] Phase 2 exit evidence is green. `EncryptedSessionStore` uses `noBackupFilesDir/wenku8-session/records/{provider}.session` and `epochs/{provider}.epoch`; credential migration uses `noBackupFilesDir/wenku8-migration/credential/`, exposes `LegacyCredentialAdapter.inspect()/rejectLegacyWrite()/scrub(mutationId)`, and never imports a plaintext password.
- [ ] The checked-in Phase 0 legacy artifact manifest has one owner row for every production read/write of `.wk8`, SharedPreferences, `saves/intro`, `saves/novel`, `saves/imgs`, custom assets, cache roots, `.nomedia`, and `.migration_completed`.
- [ ] API 23/29/30/31/32/33/34/35/36 emulator system images used by the controlled harness are pinned in Phase 0 tooling evidence.

Run from `studio-android/LightNovelLibrary`:

```powershell
.\gradlew.bat -Pwenku8Provider=public phase0Gate verifyPhase0Coverage :verification-tools:phase1Gate :verification-tools:phase2Gate --console=plain --stacktrace --no-parallel
```

Expected: `BUILD SUCCESSFUL`, with Phase 1 settings migration and Phase 2 SessionStore gates listed as executed, not `UP-TO-DATE` from an untrusted cache.

### Stop Immediately When

- Any prerequisite file/type differs from the reviewed Phase 1 or Phase 2 plan. Reconcile the plans and obtain review instead of creating a duplicate settings or session store.
- A Room database, DataStore file, session file, or journal is proposed to contain a password, captcha, Cookie outside SessionStore, arbitrary HTML, or an unbounded response body.
- A backup rule relies on excluding selected Room rows. Split the physical store instead.
- A canonical write can occur without the same Room transaction inserting a unique applied mutation ID.
- A legacy writer can mutate a Phase 3 domain after snapshotting without the domain writer barrier.
- A crash point loses the only known-good legacy file, truncates it before durable replacement, or claims synchronized state before the projection version catches up.
- A restore path trusts a restored `Verified`/`Complete` flag instead of comparing canonical and projection versions.
- Background retry can repeat a mutation, an unvalidated Range request, or a partial-file overwrite. Only idempotent GET, validated Range resume, or safe whole-item restart is allowed.
- A foreground/user-initiated path is selected for automatic migration or for a transfer that was not explicitly started from visible UI.
- A foreground/UIDT notification, WorkManager Data, JobInfo extras, tag, progress row, or diagnostic includes an account ID, title, query, URL, response, Cookie, or credential.
- A device harness is pointed at a physical device or a device containing user data. The script must require `ro.kernel.qemu=1` and a matching disposable AVD marker.
- Any non-secret legacy artifact is deleted. The already-reviewed Phase 2 password-only `cert.wk8` scrub remains the sole early deletion/rewrite exception.
- `git diff --check`, a Phase 3 focused test, or an independent Critical/Important review fails.

## Task Dependency Graph

| Task | Depends on | Produces |
| ---: | --- | --- |
| 1 | Preconditions | Frozen Phase 3 verification contract and build wiring |
| 2 | 1 | Canonical user Room schema/DAOs |
| 3 | 1 | Excluded catalog/cache Room schema/DAOs |
| 4 | 2, 3 | Physical database factory and schema-upgrade gates |
| 5 | 1 | Legacy path/filesystem/byte codecs |
| 6 | 2, 4, 5 | Generic transient state/delta/journal/checkpoint/lease schema and excluded immutable snapshot store |
| 7 | 5, 6 | Phase-aware legacy-first/canonical-first routing, writer barrier, delta replay, and journal protocol |
| 8 | 2, 3, 7 | Search/bookshelf/progress/download-catalog participants |
| 9 | 7 | Phase 1 settings and Phase 2 session integration registry |
| 10 | 8, 9 | All legacy writer interception and rollback-compatible projections |
| 11 | 8, 10 | Bounded migration/reconciliation WorkManager workers |
| 12 | 2, 3, 6-10 | Physical backup rules and restore reconciliation |
| 13 | 2, 3 | Cancellable GET/Range client and partial-file store |
| 14 | 6, 13 | Transfer chunk runner, lease, and exactly-one terminal commit |
| 15 | 11, 14 | API-neutral scheduler selection and fallback policy |
| 16 | 15 | API 23-33 foreground/expedited/regular WorkManager path |
| 17 | 15 | API 34-36 UIDT JobInfo/JobService lifecycle |
| 18 | 16, 17 | Manifest, AppContainer, notification, and cancellation integration |
| 19 | 6-18 | Multi-stage instrumentation plus PowerShell ADB harness |
| 20 | 12, 19 | API 23-36 CI/matrix/retained evidence and Phase 3 exit gate |

Do not reorder dependent tasks. Each task ends in a focused commit and two independent reviews before the next dependent task starts.

## Command Convention

Every Android command below runs from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android\studio-android\LightNovelLibrary'
```

Repository-level Git, YAML, and host-script commands run from:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android'
```

Never omit `--console=plain --stacktrace --no-parallel` from verification commands unless the command is an ADB or Git command. Expected RED text is the exact missing contract introduced by that task; an unrelated compilation, dependency, daemon, emulator, or authorization failure is not an acceptable RED.

Task-local Git commands that stage paths beginning with `app/`, `core/`, or `gradle/` run from `studio-android/LightNovelLibrary`; commands that stage `docs/`, `tools/`, or `.github/` run from the repository root. Each task states the root when it is not evident from the staged paths.

### Task 1: Freeze The Phase 3 Storage And Scheduler Contract

**Depends on:** all preconditions.

**Files:**
- Create: `tools/verify-phase3-contract.ps1`
- Create: `docs/verification/phase-3-storage-contract.yaml`
- Modify: `studio-android/LightNovelLibrary/gradle/libs.versions.toml`
- Modify: `studio-android/LightNovelLibrary/core/storage/build.gradle`
- Modify: `studio-android/LightNovelLibrary/core/network/build.gradle`
- Modify: `studio-android/LightNovelLibrary/core/data/build.gradle`
- Modify: `studio-android/LightNovelLibrary/app/build.gradle`
- Test: `tools/verify-phase3-contract.ps1`

- [ ] **Step 1: Add the failing contract verifier**

Create the complete script:

```powershell
# tools/verify-phase3-contract.ps1
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$manifest = Join-Path $repo 'docs\verification\phase-3-storage-contract.yaml'
$catalog = Join-Path $repo 'studio-android\LightNovelLibrary\gradle\libs.versions.toml'

if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) {
    throw 'PHASE3-CONTRACT-MISSING: docs/verification/phase-3-storage-contract.yaml'
}

$text = Get-Content -Raw -LiteralPath $manifest
$required = @(
    'schema: wenku8-phase3-storage-contract/v1',
    'user_database: wenku8-user.db',
    'catalog_cache_database: wenku8-catalog-cache.db',
    'migration_transient_database: migration-transient.db',
    'settings_datastore: files/datastore/app_settings.preferences_pb',
    'session_store_owner: phase-2-encrypted-session-store',
    'post_canonical_pre_projection',
    'safe_idempotent_get',
    'validated_range_resume',
    'safe_whole_item_restart',
    'api: 23', 'api: 29', 'api: 30', 'api: 31', 'api: 32',
    'api: 33', 'api: 34', 'api: 35', 'api: 36'
)
foreach ($needle in $required) {
    if (-not $text.Contains($needle)) {
        throw "PHASE3-CONTRACT-INCOMPLETE: $needle"
    }
}

$forbidden = @('T' + 'BD', 'T' + 'ODO', 'fill' + ' later', 'exactly-once HTTP')
foreach ($needle in $forbidden) {
    if ($text.Contains($needle)) {
        throw "PHASE3-CONTRACT-FORBIDDEN: $needle"
    }
}

$catalogText = Get-Content -Raw -LiteralPath $catalog
foreach ($needle in @('work = "2.11.0"', 'robolectric = "4.16.1"', 'androidx-work-runtime-ktx', 'androidx-work-testing')) {
    if (-not $catalogText.Contains($needle)) {
        throw "PHASE3-VERSION-CATALOG-INCOMPLETE: $needle"
    }
}

Write-Host 'PHASE3-CONTRACT-PASS'
```

- [ ] **Step 2: Run the verifier and observe the intended RED**

Run from the repository root:

```powershell
& .\tools\verify-phase3-contract.ps1
```

Expected: terminating error `PHASE3-CONTRACT-MISSING: docs/verification/phase-3-storage-contract.yaml`. Do not continue if the script fails for PowerShell syntax or repository path resolution.

- [ ] **Step 3: Add the complete contract manifest**

Create exactly:

```yaml
# docs/verification/phase-3-storage-contract.yaml
schema: wenku8-phase3-storage-contract/v1
phase: 3
stores:
  canonical_user:
    user_database: wenku8-user.db
    backup: whole_store_included
    contains: [bookshelf, search_history, reader_progress, download_state, domain_versions, applied_mutations]
  rebuildable_catalog_cache:
    catalog_cache_database: wenku8-catalog-cache.db
    backup: whole_store_excluded
    contains: [novel_metadata, volume_catalog, chapter_catalog, cache_metadata]
  migration_transient:
    migration_transient_database: migration-transient.db
    backup: whole_store_excluded
    contains: [domain_state, snapshot, checkpoint, journal_outbox, transfer_lease]
  settings:
    settings_datastore: files/datastore/app_settings.preferences_pb
    owner: phase-1-settings-migration
    backup: whole_store_included
  session:
    session_store_owner: phase-2-encrypted-session-store
    backup: whole_store_excluded
migration_domains:
  - settings
  - session_credentials
  - search_history
  - bookshelf_by_source_and_account
  - reader_progress_by_source
  - downloads_and_catalog
states:
  - NotStarted
  - Snapshotting
  - Importing
  - DualWrite
  - Reconciling
  - Verified
  - LegacyReadOnly
  - Complete
crash_points:
  - before_journal_append
  - after_journal_append
  - post_canonical_pre_projection
  - after_projection_pre_version_ack
  - after_version_ack
network_retry_classes:
  - safe_idempotent_get
  - validated_range_resume
  - safe_whole_item_restart
  - forbidden_non_idempotent_mutation
scheduler_paths:
  ordinary_migration: bounded_workmanager
  api_23_33_user_bulk: foreground_coroutine_worker
  api_31_33_denial: visible_queued_regular_work
  api_34_36_user_bulk: user_initiated_data_transfer_job
  api_34_36_ineligible: visible_queued_regular_work
device_gates:
  - api: 23
    owns: [minimum_sdk, legacy_files, foreground_worker]
  - api: 29
    owns: [legacy_external_storage]
  - api: 30
    owns: [scoped_storage_boundary]
  - api: 31
    owns: [foreground_start_denial, expedited_fallback]
  - api: 32
    owns: [legacy_storage_permission, foreground_start_denial]
  - api: 33
    owns: [notification_denial, foreground_start_denial]
  - api: 34
    owns: [uidt_eligibility, uidt_job_service]
  - api: 35
    owns: [uidt_stop_reason, data_sync_limit]
  - api: 36
    owns: [current_uidt, current_notification_rules]
retained_evidence_root: build/reports/phase3
```

- [ ] **Step 4: Pin the exact WorkManager/test coordinates**

Add these entries without changing the Phase 1 pins:

```toml
# gradle/libs.versions.toml additions
[versions]
work = "2.11.0"
robolectric = "4.16.1"
androidx-test-core = "1.6.1"
androidx-test-ext = "1.2.1"
androidx-test-runner = "1.6.2"

[libraries]
androidx-work-runtime-ktx = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }
androidx-work-testing = { module = "androidx.work:work-testing", version.ref = "work" }
androidx-room-testing = { module = "androidx.room:room-testing", version.ref = "room" }
robolectric = { module = "org.robolectric:robolectric", version.ref = "robolectric" }
androidx-test-core-ktx = { module = "androidx.test:core-ktx", version.ref = "androidx-test-core" }
androidx-test-ext-junit-ktx = { module = "androidx.test.ext:junit-ktx", version.ref = "androidx-test-ext" }
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidx-test-runner" }
```

If the tables already exist, insert only the rows inside the existing tables; duplicate TOML tables are invalid.

- [ ] **Step 5: Wire module dependencies and Room schema export**

Add the following exact dependency/configuration statements to the existing Phase 1 module files:

```groovy
// core/storage/build.gradle: verify this Phase 1 block remains exactly once
ksp {
    arg('room.schemaLocation', file('schemas').path)
}

// core/storage/build.gradle addition inside android
sourceSets {
    androidTest.assets.srcDir file('schemas')
}

// core/storage/build.gradle additions inside dependencies
implementation libs.androidx.work.runtime.ktx
testImplementation libs.androidx.work.testing
testImplementation libs.robolectric
androidTestImplementation libs.androidx.room.testing
androidTestImplementation libs.androidx.work.testing
androidTestImplementation libs.androidx.test.core.ktx
androidTestImplementation libs.androidx.test.ext.junit.ktx
androidTestImplementation libs.androidx.test.runner
androidTestImplementation libs.kotlinx.coroutines.test

// core/network/build.gradle additions inside dependencies
testImplementation libs.okhttp.mockwebserver

// core/data/build.gradle additions inside dependencies
implementation project(':core:domain')
implementation project(':core:network')
implementation project(':core:storage')
implementation libs.androidx.work.runtime.ktx
testImplementation libs.androidx.work.testing
testImplementation libs.robolectric

// app/build.gradle additions inside dependencies
implementation libs.androidx.work.runtime.ktx
androidTestImplementation libs.androidx.work.testing
androidTestImplementation libs.kotlinx.coroutines.test
```

Keep a single occurrence of each dependency and the existing Phase 1 KSP block; Phase 3 adds only the missing `sourceSets`/WorkManager/test rows. Phase 2 already supplies MockWebServer when present. Do not add WorkManager to `:core:model`, `:core:domain`, or any feature module.

- [ ] **Step 6: Re-run the focused verifier**

Run:

```powershell
& .\tools\verify-phase3-contract.ps1
```

Expected: `PHASE3-CONTRACT-PASS`.

- [ ] **Step 7: Resolve dependencies and compile the affected test graphs**

Run from `studio-android/LightNovelLibrary`:

```powershell
.\gradlew.bat -Pwenku8Provider=public :core:storage:compileDebugUnitTestKotlin :core:network:compileTestKotlin :core:data:compileDebugUnitTestKotlin :app:compileAlphaDebugAndroidTestKotlin --console=plain --stacktrace --no-parallel
```

Expected: `BUILD SUCCESSFUL`; dependency verification/locking remains green and no repository outside the Phase 0 allowlist is used.

- [ ] **Step 8: Commit the frozen Phase 3 contract**

Run from the repository root:

```powershell
git add tools/verify-phase3-contract.ps1 docs/verification/phase-3-storage-contract.yaml studio-android/LightNovelLibrary/gradle/libs.versions.toml studio-android/LightNovelLibrary/core/storage/build.gradle studio-android/LightNovelLibrary/core/network/build.gradle studio-android/LightNovelLibrary/core/data/build.gradle studio-android/LightNovelLibrary/app/build.gradle
git diff --check --cached
git commit -m "build: freeze phase 3 storage contract"
```

Expected: one commit containing only Task 1 files.

### Task 2: Add The Backed-Up Canonical User Room Store

**Depends on:** Task 1.

**Files:**
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryEntities.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryDaos.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/CanonicalMutationStore.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user/UserLibraryDatabase.kt`
- Create: `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/UserLibraryDatabaseTest.kt`

- [ ] **Step 1: Write the failing in-memory DAO test**

Create the test with these complete cases:

```kotlin
package org.mewx.wenku8.core.storage.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mewx.wenku8.core.storage.db.user.AppliedMutationEntity
import org.mewx.wenku8.core.storage.db.user.BookshelfMembershipEntity
import org.mewx.wenku8.core.storage.db.user.DownloadEntity
import org.mewx.wenku8.core.storage.db.user.DownloadState
import org.mewx.wenku8.core.storage.db.user.UserLibraryDatabase
import org.mewx.wenku8.core.storage.db.user.VersionedWriteResult

@RunWith(AndroidJUnit4::class)
class UserLibraryDatabaseTest {
    private lateinit var db: UserLibraryDatabase

    @Before
    fun open() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            UserLibraryDatabase::class.java,
        ).build()
    }

    @After
    fun close() = db.close()

    @Test
    fun accountPartitionsDoNotOverwriteLocalMembership() = runTest {
        val local = membership("local", 1L)
        val account = membership("account:opaque-a", 2L)
        assertEquals(VersionedWriteResult.INSERTED, db.bookshelfDao().applyVersionedPayload(local))
        assertEquals(VersionedWriteResult.INSERTED, db.bookshelfDao().applyVersionedPayload(account))

        assertEquals(listOf(local), db.bookshelfDao().byPartition("local"))
        assertEquals(listOf(account), db.bookshelfDao().byPartition("account:opaque-a"))
    }

    @Test
    fun mutationVersionsAreMonotonicAndSnapshotInsertCannotReplaceUserWrite() = runTest {
        assertEquals(1L, db.domainVersionDao().reserveMutationVersion("bookshelf:local"))
        assertEquals(2L, db.domainVersionDao().reserveMutationVersion("bookshelf:local"))
        val userWrite = membership("local", 2L)
        db.bookshelfDao().applyVersionedPayload(userWrite)
        val staleSnapshot = membership("local", 1L).copy(originSnapshotId = "snapshot-1")
        assertEquals(-1L, db.bookshelfDao().insertSnapshotIfAbsent(staleSnapshot))
        assertEquals(userWrite, db.bookshelfDao().byPartition("local").single())
        assertEquals(0, db.bookshelfDao().deleteAtMostVersion("local", "wenku8-public", "1", 1L))
        assertEquals(1, db.bookshelfDao().acknowledgeProjection("local", "wenku8-public", "1", 2L))
    }

    @Test
    fun appliedMutationIsUniqueAndTerminalCommitOccursOnce() = runTest {
        val mutation = AppliedMutationEntity("m-1", "downloads", 1L, null, 10L)
        assertTrue(db.appliedMutationDao().insertOnce(mutation))
        assertFalse(db.appliedMutationDao().insertOnce(mutation))

        db.downloadDao().applyVersionedPayload(
            DownloadEntity(
                workKey = "download:w8:1:2",
                sourceId = "wenku8-public",
                novelRemoteId = "1",
                chapterRemoteId = "2",
                destinationRelativePath = "saves/novel/2.xml",
                state = DownloadState.RUNNING,
                expectedBytes = 12L,
                transferredBytes = 12L,
                locatorSchema = 1,
                canonicalHttpsUrl = "https://www.wenku8.net/modules/article/packshow.php?aid=1&cid=2",
                destinationRootId = "legacy-chapter-root",
                locatorSha256 = "3199ee77ff16867aef48bd2390f6fec36be66e709fce1c4a3755016f71fb9437",
                inputRevision = 1L,
                strongEtag = "\"etag-a\"",
                representationLength = 12L,
                sha256 = null,
                retryCount = 0,
                stopReason = null,
                mutationVersion = 1L,
                legacyProjectionVersion = 0L,
                originSnapshotId = null,
                activeLeaseOwnerId = null,
                activeLeaseGeneration = 0L,
                promotionGeneration = null,
                terminalCommitCount = 0,
            )
        )
        assertEquals(1, db.downloadDao().fenceNextLease("download:w8:1:2", "owner-a", 0L, 1L))
        assertEquals(1, db.downloadDao().authorizePromotion("download:w8:1:2", "owner-a", 1L))
        assertEquals(1, db.downloadDao().commitTerminalOnce("download:w8:1:2", "owner-a", 1L, DownloadState.SUCCEEDED, "abc"))
        assertEquals(0, db.downloadDao().commitTerminalOnce("download:w8:1:2", "owner-a", 1L, DownloadState.SUCCEEDED, "abc"))
        assertEquals(1, db.downloadDao().get("download:w8:1:2")!!.terminalCommitCount)
    }

    private fun membership(partition: String, version: Long) = BookshelfMembershipEntity(
        partitionKey = partition,
        sourceId = "wenku8-public",
        novelRemoteId = "1",
        legacyAid = 1,
        serverEntryId = null,
        groupId = "local",
        sortIndex = 0,
        mutationVersion = version,
        legacyProjectionVersion = 0L,
        originSnapshotId = null,
        updatedAtEpochMillis = 100L,
    )
}
```

- [ ] **Step 2: Run the test and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.UserLibraryDatabaseTest --console=plain --stacktrace --no-parallel
```

Expected: compilation fails because `UserLibraryDatabase` and its entities do not exist. A missing emulator is not the expected RED.

- [ ] **Step 3: Add the complete canonical entities**

Create:

```kotlin
package org.mewx.wenku8.core.storage.db.user

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "bookshelf_membership",
    primaryKeys = ["partitionKey", "sourceId", "novelRemoteId"],
    indices = [Index("serverEntryId"), Index("mutationVersion")],
)
data class BookshelfMembershipEntity(
    val partitionKey: String,
    val sourceId: String,
    val novelRemoteId: String,
    val legacyAid: Int?,
    val serverEntryId: String?,
    val groupId: String,
    val sortIndex: Int,
    val mutationVersion: Long,
    val legacyProjectionVersion: Long,
    val originSnapshotId: String?,
    val updatedAtEpochMillis: Long,
)

@Entity(tableName = "search_history", primaryKeys = ["entryId"], indices = [Index("sortIndex")])
data class SearchHistoryEntity(
    val entryId: String,
    val normalizedQuery: String,
    val displayQuery: String,
    val scope: String,
    val contentLanguage: String,
    val sortIndex: Int,
    val mutationVersion: Long,
    val legacyProjectionVersion: Long,
    val originSnapshotId: String?,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "reader_progress",
    primaryKeys = ["sourceId", "novelRemoteId"],
    indices = [Index("chapterRemoteId"), Index("updatedAtEpochMillis")],
)
data class ReaderProgressEntity(
    val sourceId: String,
    val novelRemoteId: String,
    val legacyAid: Int?,
    val volumeRemoteId: String?,
    val chapterRemoteId: String,
    val legacyCid: Int?,
    val blockIndex: Int,
    val charIndex: Int,
    val legacyPixelOffset: Int?,
    val legacyViewportHeight: Int?,
    val mutationVersion: Long,
    val legacyProjectionVersion: Long,
    val originSnapshotId: String?,
    val updatedAtEpochMillis: Long,
)

enum class DownloadState { QUEUED, RUNNING, PAUSED, CANCELLED, FAILED, SUCCEEDED }

@Entity(tableName = "download", primaryKeys = ["workKey"], indices = [Index("state"), Index("chapterRemoteId")])
data class DownloadEntity(
    val workKey: String,
    val sourceId: String,
    val novelRemoteId: String,
    val chapterRemoteId: String,
    val destinationRelativePath: String,
    val state: DownloadState,
    val expectedBytes: Long?,
    val transferredBytes: Long,
    val locatorSchema: Int,
    val canonicalHttpsUrl: String,
    val destinationRootId: String,
    val locatorSha256: String,
    val inputRevision: Long,
    val strongEtag: String?,
    val representationLength: Long?,
    val sha256: String?,
    val retryCount: Int,
    val stopReason: Int?,
    val mutationVersion: Long,
    val legacyProjectionVersion: Long,
    val originSnapshotId: String?,
    val activeLeaseOwnerId: String?,
    val activeLeaseGeneration: Long,
    val promotionGeneration: Long?,
    val terminalCommitCount: Int,
)

@Entity(tableName = "canonical_domain_version", primaryKeys = ["domainKey"])
data class CanonicalDomainVersionEntity(
    val domainKey: String,
    val mutationVersion: Long,
    val legacyProjectionVersion: Long,
    val nextMutationVersion: Long,
)

@Entity(tableName = "applied_mutation", primaryKeys = ["mutationId"], indices = [Index("domainKey")])
data class AppliedMutationEntity(
    val mutationId: String,
    val domainKey: String,
    val canonicalVersion: Long,
    val originSnapshotId: String?,
    val committedAtEpochMillis: Long,
)
```

- [ ] **Step 4: Add the complete DAOs**

Create:

```kotlin
package org.mewx.wenku8.core.storage.db.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

enum class VersionedWriteResult { INSERTED, UPDATED, STALE_OR_DUPLICATE }

@Dao
abstract class BookshelfDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(value: BookshelfMembershipEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertSnapshotIfAbsent(value: BookshelfMembershipEntity): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun updateAfterVersionGuard(value: BookshelfMembershipEntity): Int

    @Query("SELECT * FROM bookshelf_membership WHERE partitionKey = :partition AND sourceId = :sourceId AND novelRemoteId = :novelId")
    protected abstract suspend fun get(partition: String, sourceId: String, novelId: String): BookshelfMembershipEntity?

    @Transaction
    open suspend fun applyVersionedPayload(value: BookshelfMembershipEntity): VersionedWriteResult {
        if (insertIfAbsent(value) != -1L) return VersionedWriteResult.INSERTED
        val current = requireNotNull(get(value.partitionKey, value.sourceId, value.novelRemoteId))
        if (current.mutationVersion >= value.mutationVersion) return VersionedWriteResult.STALE_OR_DUPLICATE
        check(updateAfterVersionGuard(value) == 1)
        return VersionedWriteResult.UPDATED
    }

    @Transaction
    open suspend fun applyVersionedPayload(values: List<BookshelfMembershipEntity>): List<VersionedWriteResult> =
        values.map { applyVersionedPayload(it) }

    @Query("SELECT * FROM bookshelf_membership WHERE partitionKey = :partition ORDER BY sortIndex, novelRemoteId")
    suspend fun byPartition(partition: String): List<BookshelfMembershipEntity>

    @Query("SELECT * FROM bookshelf_membership WHERE mutationVersion > legacyProjectionVersion ORDER BY mutationVersion LIMIT :limit")
    suspend fun pendingProjection(limit: Int): List<BookshelfMembershipEntity>

    @Query("UPDATE bookshelf_membership SET legacyProjectionVersion = :version WHERE partitionKey = :partition AND sourceId = :sourceId AND novelRemoteId = :novelId AND mutationVersion = :version")
    suspend fun acknowledgeProjection(partition: String, sourceId: String, novelId: String, version: Long): Int

    @Query("DELETE FROM bookshelf_membership WHERE partitionKey = :partition AND sourceId = :sourceId AND novelRemoteId = :novelId AND mutationVersion <= :maxVersion")
    suspend fun deleteAtMostVersion(partition: String, sourceId: String, novelId: String, maxVersion: Long): Int

    @Query("DELETE FROM bookshelf_membership WHERE partitionKey = :partition AND mutationVersion <= :maxVersion")
    suspend fun clearPartitionAtMostVersion(partition: String, maxVersion: Long): Int

    @Query("DELETE FROM bookshelf_membership WHERE originSnapshotId = :snapshotId")
    suspend fun deleteSnapshotRows(snapshotId: String): Int
}

@Dao
abstract class SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(value: SearchHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertSnapshotIfAbsent(value: SearchHistoryEntity): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun updateAfterVersionGuard(value: SearchHistoryEntity): Int

    @Query("SELECT * FROM search_history WHERE entryId = :entryId")
    protected abstract suspend fun get(entryId: String): SearchHistoryEntity?

    @Transaction
    open suspend fun applyVersionedPayload(value: SearchHistoryEntity): VersionedWriteResult {
        if (insertIfAbsent(value) != -1L) return VersionedWriteResult.INSERTED
        val current = requireNotNull(get(value.entryId))
        if (current.mutationVersion >= value.mutationVersion) return VersionedWriteResult.STALE_OR_DUPLICATE
        check(updateAfterVersionGuard(value) == 1)
        return VersionedWriteResult.UPDATED
    }

    @Transaction
    open suspend fun applyVersionedPayload(values: List<SearchHistoryEntity>): List<VersionedWriteResult> =
        values.map { applyVersionedPayload(it) }

    @Query("SELECT * FROM search_history ORDER BY sortIndex, entryId")
    suspend fun all(): List<SearchHistoryEntity>

    @Query("SELECT * FROM search_history WHERE mutationVersion > legacyProjectionVersion ORDER BY mutationVersion LIMIT :limit")
    suspend fun pendingProjection(limit: Int): List<SearchHistoryEntity>

    @Query("UPDATE search_history SET legacyProjectionVersion = :version WHERE entryId = :entryId AND mutationVersion = :version")
    suspend fun acknowledgeProjection(entryId: String, version: Long): Int

    @Query("DELETE FROM search_history WHERE entryId = :entryId AND mutationVersion <= :maxVersion")
    suspend fun deleteAtMostVersion(entryId: String, maxVersion: Long): Int

    @Query("DELETE FROM search_history WHERE mutationVersion <= :maxVersion")
    suspend fun clearAtMostVersion(maxVersion: Long): Int

    @Query("DELETE FROM search_history WHERE originSnapshotId = :snapshotId")
    suspend fun deleteSnapshotRows(snapshotId: String): Int
}

@Dao
abstract class ReaderProgressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(value: ReaderProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertSnapshotIfAbsent(value: ReaderProgressEntity): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun updateAfterVersionGuard(value: ReaderProgressEntity): Int

    @Transaction
    open suspend fun applyVersionedPayload(value: ReaderProgressEntity): VersionedWriteResult {
        if (insertIfAbsent(value) != -1L) return VersionedWriteResult.INSERTED
        val current = requireNotNull(get(value.sourceId, value.novelRemoteId))
        if (current.mutationVersion >= value.mutationVersion) return VersionedWriteResult.STALE_OR_DUPLICATE
        check(updateAfterVersionGuard(value) == 1)
        return VersionedWriteResult.UPDATED
    }

    @Query("SELECT * FROM reader_progress WHERE sourceId = :sourceId AND novelRemoteId = :novelId")
    suspend fun get(sourceId: String, novelId: String): ReaderProgressEntity?

    @Query("SELECT * FROM reader_progress WHERE mutationVersion > legacyProjectionVersion ORDER BY mutationVersion LIMIT :limit")
    suspend fun pendingProjection(limit: Int): List<ReaderProgressEntity>

    @Query("UPDATE reader_progress SET legacyProjectionVersion = :version WHERE sourceId = :sourceId AND novelRemoteId = :novelId AND mutationVersion = :version")
    suspend fun acknowledgeProjection(sourceId: String, novelId: String, version: Long): Int

    @Query("DELETE FROM reader_progress WHERE sourceId = :sourceId AND novelRemoteId = :novelId AND mutationVersion <= :maxVersion")
    suspend fun deleteAtMostVersion(sourceId: String, novelId: String, maxVersion: Long): Int

    @Query("DELETE FROM reader_progress WHERE sourceId = :sourceId AND mutationVersion <= :maxVersion")
    suspend fun clearSourceAtMostVersion(sourceId: String, maxVersion: Long): Int

    @Query("DELETE FROM reader_progress WHERE originSnapshotId = :snapshotId")
    suspend fun deleteSnapshotRows(snapshotId: String): Int
}

@Dao
abstract class DownloadDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(value: DownloadEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertSnapshotIfAbsent(value: DownloadEntity): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun updateAfterVersionGuard(value: DownloadEntity): Int

    @Transaction
    open suspend fun applyVersionedPayload(value: DownloadEntity): VersionedWriteResult {
        if (insertIfAbsent(value) != -1L) return VersionedWriteResult.INSERTED
        val current = requireNotNull(get(value.workKey))
        if (current.mutationVersion >= value.mutationVersion) return VersionedWriteResult.STALE_OR_DUPLICATE
        check(updateAfterVersionGuard(value) == 1)
        return VersionedWriteResult.UPDATED
    }

    @Query("SELECT * FROM download WHERE workKey = :workKey")
    suspend fun get(workKey: String): DownloadEntity?

    @Query("SELECT * FROM download WHERE state IN ('QUEUED','RUNNING','PAUSED') ORDER BY workKey")
    fun active(): Flow<List<DownloadEntity>>

    @Query("UPDATE download SET state = :state, sha256 = :sha256, terminalCommitCount = 1 WHERE workKey = :workKey AND activeLeaseOwnerId = :ownerId AND activeLeaseGeneration = :generation AND promotionGeneration = :generation AND terminalCommitCount = 0")
    abstract suspend fun commitTerminalOnce(workKey: String, ownerId: String, generation: Long, state: DownloadState, sha256: String?): Int

    @Query("UPDATE download SET transferredBytes = :bytes, strongEtag = :strongEtag, representationLength = :representationLength, retryCount = :retryCount, stopReason = :stopReason WHERE workKey = :workKey AND activeLeaseOwnerId = :ownerId AND activeLeaseGeneration = :generation AND terminalCommitCount = 0")
    abstract suspend fun checkpoint(
        workKey: String,
        ownerId: String,
        generation: Long,
        bytes: Long,
        strongEtag: String?,
        representationLength: Long?,
        retryCount: Int,
        stopReason: Int?,
    ): Int

    @Query("UPDATE download SET activeLeaseOwnerId = :ownerId, activeLeaseGeneration = :nextGeneration, promotionGeneration = NULL WHERE workKey = :workKey AND activeLeaseGeneration = :expectedGeneration AND terminalCommitCount = 0")
    abstract suspend fun fenceNextLease(workKey: String, ownerId: String, expectedGeneration: Long, nextGeneration: Long): Int

    @Query("UPDATE download SET promotionGeneration = :generation WHERE workKey = :workKey AND activeLeaseOwnerId = :ownerId AND activeLeaseGeneration = :generation AND terminalCommitCount = 0")
    abstract suspend fun authorizePromotion(workKey: String, ownerId: String, generation: Long): Int

    @Query("UPDATE download SET legacyProjectionVersion = :version WHERE workKey = :workKey AND mutationVersion = :version")
    suspend fun acknowledgeProjection(workKey: String, version: Long): Int

    @Query("DELETE FROM download WHERE workKey = :workKey AND mutationVersion <= :maxVersion AND terminalCommitCount = 0")
    suspend fun deleteAtMostVersion(workKey: String, maxVersion: Long): Int

    @Query("DELETE FROM download WHERE originSnapshotId = :snapshotId AND terminalCommitCount = 0")
    suspend fun deleteSnapshotRows(snapshotId: String): Int
}

@Dao
abstract class DomainVersionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(value: CanonicalDomainVersionEntity): Long

    @Query("SELECT * FROM canonical_domain_version WHERE domainKey = :domainKey")
    abstract suspend fun get(domainKey: String): CanonicalDomainVersionEntity?

    @Query("UPDATE canonical_domain_version SET nextMutationVersion = nextMutationVersion + 1 WHERE domainKey = :domainKey")
    protected abstract suspend fun incrementNext(domainKey: String): Int

    @androidx.room.Transaction
    open suspend fun reserveMutationVersion(domainKey: String): Long {
        insertIfAbsent(CanonicalDomainVersionEntity(domainKey, 0L, 0L, 1L))
        val reserved = requireNotNull(get(domainKey)).nextMutationVersion
        check(incrementNext(domainKey) == 1)
        return reserved
    }

    @Query("UPDATE canonical_domain_version SET mutationVersion = :version WHERE domainKey = :domainKey AND mutationVersion < :version AND nextMutationVersion > :version")
    abstract suspend fun advanceCanonicalAtLeast(domainKey: String, version: Long): Int

    @Query("UPDATE canonical_domain_version SET legacyProjectionVersion = :version WHERE domainKey = :domainKey AND mutationVersion >= :version AND legacyProjectionVersion < :version")
    abstract suspend fun acknowledgeLegacyProjection(domainKey: String, version: Long): Int

    @Query("SELECT * FROM canonical_domain_version WHERE mutationVersion > legacyProjectionVersion ORDER BY domainKey")
    abstract suspend fun pendingProjection(): List<CanonicalDomainVersionEntity>
}

@Dao
abstract class AppliedMutationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insert(value: AppliedMutationEntity): Long

    suspend fun insertOnce(value: AppliedMutationEntity): Boolean = insert(value) != -1L

    @Query("SELECT EXISTS(SELECT 1 FROM applied_mutation WHERE mutationId = :mutationId)")
    abstract suspend fun contains(mutationId: String): Boolean

    @Query("DELETE FROM applied_mutation WHERE originSnapshotId = :snapshotId")
    abstract suspend fun deleteSnapshotMutations(snapshotId: String): Int
}
```

Every canonical user payload write goes through `applyVersionedPayload`; direct `@Insert(REPLACE)`, unguarded `@Update`, and domain-wide clear are forbidden. `@Update` above is protected and is reachable only after the primary-key row and strictly lower stored `mutationVersion` have been checked inside the same Room transaction. Snapshot insertion remains insert-if-absent and may return `STALE_OR_DUPLICATE`; rebuildable Task 3 cache rows are the only scoped replacement exception.

- [ ] **Step 5: Add the atomic canonical mutation transaction**

Create `CanonicalMutationStore.kt` with this complete transaction boundary:

```kotlin
package org.mewx.wenku8.core.storage.db.user

import androidx.room.withTransaction

data class CanonicalMutationCommand(
    val mutationId: String,
    val domainKey: String,
    val mutationVersion: Long,
    val originSnapshotId: String?,
    val committedAtEpochMillis: Long,
)

enum class CanonicalMutationResult { COMMITTED, ALREADY_COMMITTED, STALE_SNAPSHOT_IGNORED }

fun interface VersionedPayloadTransaction {
    suspend fun apply(): VersionedWriteResult
}

class CanonicalMutationStore(private val db: UserLibraryDatabase) {
    suspend fun applyOnce(
        command: CanonicalMutationCommand,
        payload: VersionedPayloadTransaction,
    ): CanonicalMutationResult = db.withTransaction {
        require(command.mutationVersion > 0L)
        val inserted = db.appliedMutationDao().insertOnce(
            AppliedMutationEntity(
                mutationId = command.mutationId,
                domainKey = command.domainKey,
                canonicalVersion = command.mutationVersion,
                originSnapshotId = command.originSnapshotId,
                committedAtEpochMillis = command.committedAtEpochMillis,
            )
        )
        if (!inserted) return@withTransaction CanonicalMutationResult.ALREADY_COMMITTED
        val write = payload.apply()
        check(write != VersionedWriteResult.STALE_OR_DUPLICATE || command.originSnapshotId != null) {
            "A live mutation may not lose a version race"
        }
        val advanced = db.domainVersionDao().advanceCanonicalAtLeast(command.domainKey, command.mutationVersion)
        val current = requireNotNull(db.domainVersionDao().get(command.domainKey))
        check(advanced == 1 || current.mutationVersion >= command.mutationVersion)
        if (write == VersionedWriteResult.STALE_OR_DUPLICATE) {
            CanonicalMutationResult.STALE_SNAPSHOT_IGNORED
        } else {
            CanonicalMutationResult.COMMITTED
        }
    }
}
```

The payload mutation, unique applied-mutation insert, and monotonic domain-version advance are one `UserLibraryDatabase.withTransaction`. Any exception rolls all three back. Participants in Tasks 7-8 may only adapt a typed payload to `VersionedPayloadTransaction`; they may not reproduce this transaction or call a protected DAO update.

- [ ] **Step 6: Add the canonical database**

Create:

```kotlin
package org.mewx.wenku8.core.storage.db.user

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class UserLibraryConverters {
    @TypeConverter fun downloadState(value: String): DownloadState = DownloadState.valueOf(value)
    @TypeConverter fun downloadState(value: DownloadState): String = value.name
}

@Database(
    entities = [
        BookshelfMembershipEntity::class,
        SearchHistoryEntity::class,
        ReaderProgressEntity::class,
        DownloadEntity::class,
        CanonicalDomainVersionEntity::class,
        AppliedMutationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(UserLibraryConverters::class)
abstract class UserLibraryDatabase : RoomDatabase() {
    abstract fun bookshelfDao(): BookshelfDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun readerProgressDao(): ReaderProgressDao
    abstract fun downloadDao(): DownloadDao
    abstract fun domainVersionDao(): DomainVersionDao
    abstract fun appliedMutationDao(): AppliedMutationDao

    companion object {
        const val FILE_NAME = "wenku8-user.db"
    }
}
```

- [ ] **Step 7: Run the focused DAO test and schema export**

Run:

```powershell
.\gradlew.bat :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.UserLibraryDatabaseTest :core:storage:kspDebugKotlin --console=plain --stacktrace --no-parallel
```

Expected: all tests pass, including a fault injected between payload/applied-mutation/domain-version operations that leaves none of the three committed, a repeated mutation that changes none of them, a lower-version live payload rejection, a lower-version snapshot no-op, and generation-fenced checkpoint/promotion/terminal writes; `BUILD SUCCESSFUL`; Room exports `1.json` for `UserLibraryDatabase`.

- [ ] **Step 8: Commit the canonical user store**

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/db/user core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/UserLibraryDatabaseTest.kt core/storage/schemas/org.mewx.wenku8.core.storage.db.user.UserLibraryDatabase/1.json
git diff --check --cached
git commit -m "feat(storage): add canonical user database"
```

Expected: Task 2 files only.

### Task 3: Add The Excluded Catalog And Cache Room Store

**Depends on:** Task 1. It may execute in parallel with Task 2 only in an isolated worktree; merge before Task 4.

**Files:**
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/catalog/CatalogEntities.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/catalog/CatalogDaos.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/catalog/CacheIdentity.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/catalog/CatalogCacheDatabase.kt`
- Create: `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/CatalogCacheDatabaseTest.kt`

- [ ] **Step 1: Write the failing catalog ordering and cache-partition tests**

Create this complete test; it inserts out of order and proves catalog order plus account/session-epoch partitioning:

```kotlin
package org.mewx.wenku8.core.storage.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mewx.wenku8.core.storage.db.catalog.CacheMetadataEntity
import org.mewx.wenku8.core.storage.db.catalog.CacheIdentity
import org.mewx.wenku8.core.storage.db.catalog.CatalogCacheDatabase
import org.mewx.wenku8.core.storage.db.catalog.ChapterEntity
import org.mewx.wenku8.core.storage.db.catalog.VolumeEntity

@RunWith(AndroidJUnit4::class)
class CatalogCacheDatabaseTest {
    private lateinit var db: CatalogCacheDatabase

    @Before
    fun open() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            CatalogCacheDatabase::class.java,
        ).build()
    }

    @After
    fun close() = db.close()

    @Test
    fun catalogOrderAndAccountEpochPartitionAreStable() = runTest {
        db.catalogDao().upsertVolumes(listOf(volume("v2", 1), volume("v1", 0)))
        db.catalogDao().upsertChapters(listOf(chapter("c3", "v2", 0), chapter("c2", "v1", 1), chapter("c1", "v1", 0)))
        assertEquals(listOf("v1", "v2"), db.catalogDao().volumes("wenku8-public", "SIMPLIFIED_CHINESE", 1, "1").map { it.remoteId })
        assertEquals(listOf("c1", "c2"), db.catalogDao().chapters("wenku8-public", "SIMPLIFIED_CHINESE", 1, "1", "v1").map { it.remoteId })

        db.cacheMetadataDao().put(cache("anonymous", 0L, "anon-key"))
        db.cacheMetadataDao().put(cache("account:opaque-a", 7L, "private-key"))
        assertEquals(CacheIdentity.key("wenku8-public", "SIMPLIFIED_CHINESE", 1, "anonymous", 0L, "detail", "p"), db.cacheMetadataDao().fresh("wenku8-public", "SIMPLIFIED_CHINESE", 1, "anonymous", 0L, "detail", "p", 100L)?.cacheKey)
        assertEquals(CacheIdentity.key("wenku8-public", "SIMPLIFIED_CHINESE", 1, "account:opaque-a", 7L, "detail", "p"), db.cacheMetadataDao().fresh("wenku8-public", "SIMPLIFIED_CHINESE", 1, "account:opaque-a", 7L, "detail", "p", 100L)?.cacheKey)
        assertEquals(null, db.cacheMetadataDao().fresh("wenku8-public", "SIMPLIFIED_CHINESE", 1, "account:opaque-a", 8L, "detail", "p", 100L))
        assertEquals(null, db.cacheMetadataDao().fresh("other-source", "SIMPLIFIED_CHINESE", 1, "anonymous", 0L, "detail", "p", 100L))
        assertEquals(null, db.cacheMetadataDao().fresh("wenku8-public", "TRADITIONAL_CHINESE", 1, "anonymous", 0L, "detail", "p", 100L))
        assertEquals(null, db.cacheMetadataDao().fresh("wenku8-public", "SIMPLIFIED_CHINESE", 2, "anonymous", 0L, "detail", "p", 100L))
    }

    private fun volume(id: String, index: Int) = VolumeEntity(
        sourceId = "wenku8-public", novelRemoteId = "1", remoteId = id,
        title = id, contentLanguage = "SIMPLIFIED_CHINESE", sortIndex = index, parserRevision = 1, originSnapshotId = null,
    )

    private fun chapter(id: String, volumeId: String, index: Int) = ChapterEntity(
        sourceId = "wenku8-public", novelRemoteId = "1", volumeRemoteId = volumeId,
        remoteId = id, legacyCid = id.removePrefix("c").toInt(), title = id, contentLanguage = "SIMPLIFIED_CHINESE",
        sortIndex = index, durableRelativePath = "saves/novel/${id.removePrefix("c")}.xml", parserRevision = 1,
        originSnapshotId = null,
    )

    private fun cache(partition: String, epoch: Long, key: String) = CacheMetadataEntity(
        cacheKey = CacheIdentity.key("wenku8-public", "SIMPLIFIED_CHINESE", 1, partition, epoch, "detail", "p"), sourceId = "wenku8-public", partitionKey = partition,
        sessionEpoch = epoch, contentLanguage = "SIMPLIFIED_CHINESE", operation = "detail",
        canonicalParametersHash = "p", payloadKind = "novel", payloadKey = "1",
        parserRevision = 1, fetchedAtEpochMillis = 1L, expiresAtEpochMillis = 200L,
    )
}
```

- [ ] **Step 2: Run the test and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.CatalogCacheDatabaseTest --console=plain --stacktrace --no-parallel
```

Expected: compilation fails on unresolved `CatalogCacheDatabase`.

- [ ] **Step 3: Add the complete catalog/cache entities**

Create:

```kotlin
package org.mewx.wenku8.core.storage.db.catalog

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "novel", primaryKeys = ["sourceId", "remoteId", "contentLanguage"])
data class NovelEntity(
    val sourceId: String,
    val remoteId: String,
    val contentLanguage: String,
    val legacyAid: Int?,
    val title: String,
    val author: String?,
    val status: String?,
    val introductionParagraphsJson: String,
    val coverHttpsUrl: String?,
    val parserRevision: Int,
    val fetchedAtEpochMillis: Long,
    val originSnapshotId: String?,
)

@Entity(
    tableName = "volume",
    primaryKeys = ["sourceId", "contentLanguage", "parserRevision", "novelRemoteId", "remoteId"],
    indices = [Index("sourceId", "contentLanguage", "parserRevision", "novelRemoteId", "sortIndex")],
)
data class VolumeEntity(
    val sourceId: String,
    val novelRemoteId: String,
    val remoteId: String,
    val title: String,
    val contentLanguage: String,
    val sortIndex: Int,
    val parserRevision: Int,
    val originSnapshotId: String?,
)

@Entity(
    tableName = "chapter",
    primaryKeys = ["sourceId", "contentLanguage", "parserRevision", "novelRemoteId", "remoteId"],
    indices = [Index("sourceId", "contentLanguage", "parserRevision", "novelRemoteId", "volumeRemoteId", "sortIndex")],
)
data class ChapterEntity(
    val sourceId: String,
    val novelRemoteId: String,
    val volumeRemoteId: String,
    val remoteId: String,
    val legacyCid: Int?,
    val title: String,
    val contentLanguage: String,
    val sortIndex: Int,
    val durableRelativePath: String?,
    val parserRevision: Int,
    val originSnapshotId: String?,
)

@Entity(
    tableName = "cache_metadata",
    primaryKeys = ["sourceId", "contentLanguage", "parserRevision", "partitionKey", "sessionEpoch", "operation", "canonicalParametersHash"],
    indices = [Index("cacheKey", unique = true), Index("partitionKey", "sessionEpoch")],
)
data class CacheMetadataEntity(
    val cacheKey: String,
    val sourceId: String,
    val partitionKey: String,
    val sessionEpoch: Long,
    val contentLanguage: String,
    val operation: String,
    val canonicalParametersHash: String,
    val payloadKind: String,
    val payloadKey: String,
    val parserRevision: Int,
    val fetchedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
)
```

- [ ] **Step 4: Add the complete catalog/cache DAOs and database**

Create:

```kotlin
package org.mewx.wenku8.core.storage.db.catalog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertNovel(value: NovelEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertVolumes(values: List<VolumeEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertChapters(values: List<ChapterEntity>)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertSnapshotNovel(value: NovelEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertSnapshotVolume(value: VolumeEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertSnapshotChapter(value: ChapterEntity): Long

    @Query("SELECT * FROM novel WHERE sourceId = :sourceId AND remoteId = :novelId AND contentLanguage = :language AND parserRevision = :parserRevision")
    suspend fun novel(sourceId: String, novelId: String, language: String, parserRevision: Int): NovelEntity?

    @Query("SELECT * FROM volume WHERE sourceId = :sourceId AND contentLanguage = :language AND parserRevision = :parserRevision AND novelRemoteId = :novelId ORDER BY sortIndex, remoteId")
    suspend fun volumes(sourceId: String, language: String, parserRevision: Int, novelId: String): List<VolumeEntity>

    @Query("SELECT * FROM chapter WHERE sourceId = :sourceId AND contentLanguage = :language AND parserRevision = :parserRevision AND novelRemoteId = :novelId AND volumeRemoteId = :volumeId ORDER BY sortIndex, remoteId")
    suspend fun chapters(sourceId: String, language: String, parserRevision: Int, novelId: String, volumeId: String): List<ChapterEntity>

    @Query("DELETE FROM chapter WHERE originSnapshotId = :snapshotId")
    suspend fun deleteSnapshotChapters(snapshotId: String): Int

    @Query("DELETE FROM volume WHERE originSnapshotId = :snapshotId")
    suspend fun deleteSnapshotVolumes(snapshotId: String): Int

    @Query("DELETE FROM novel WHERE originSnapshotId = :snapshotId")
    suspend fun deleteSnapshotNovels(snapshotId: String): Int
}

@Dao
abstract class CacheMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun replaceRebuildableEntry(value: CacheMetadataEntity)

    @androidx.room.Transaction
    open suspend fun put(value: CacheMetadataEntity) {
        require(value.parserRevision > 0)
        require(value.expiresAtEpochMillis > value.fetchedAtEpochMillis)
        require(value.cacheKey == CacheIdentity.key(value.sourceId, value.contentLanguage, value.parserRevision, value.partitionKey, value.sessionEpoch, value.operation, value.canonicalParametersHash))
        replaceRebuildableEntry(value)
    }

    @Query("SELECT * FROM cache_metadata WHERE sourceId = :sourceId AND contentLanguage = :language AND parserRevision = :parserRevision AND partitionKey = :partition AND sessionEpoch = :epoch AND operation = :operation AND canonicalParametersHash = :parametersHash AND fetchedAtEpochMillis <= :now AND expiresAtEpochMillis > :now LIMIT 1")
    suspend fun fresh(sourceId: String, language: String, parserRevision: Int, partition: String, epoch: Long, operation: String, parametersHash: String, now: Long): CacheMetadataEntity?

    @Query("DELETE FROM cache_metadata WHERE partitionKey = :partition")
    suspend fun invalidatePartition(partition: String): Int

    @Query("DELETE FROM cache_metadata WHERE parserRevision < :minimumRevision")
    suspend fun invalidateOlderParserRevisions(minimumRevision: Int): Int

    @Query("DELETE FROM cache_metadata WHERE sourceId = :sourceId AND partitionKey = :partition")
    suspend fun clearPartition(sourceId: String, partition: String): Int
}
```

Create the cache identity implementation; no caller may supply an arbitrary cache key:

```kotlin
package org.mewx.wenku8.core.storage.db.catalog

import java.security.MessageDigest

object CacheIdentity {
    fun key(
        sourceId: String,
        contentLanguage: String,
        parserRevision: Int,
        partitionKey: String,
        sessionEpoch: Long,
        operation: String,
        canonicalParametersHash: String,
    ): String {
        require(sourceId.isNotBlank() && contentLanguage.isNotBlank() && parserRevision > 0)
        require(partitionKey.isNotBlank() && sessionEpoch >= 0L && operation.isNotBlank())
        require(canonicalParametersHash.matches(Regex("[a-zA-Z0-9_-]{1,128}")))
        val canonical = listOf(sourceId, contentLanguage, parserRevision.toString(), partitionKey, sessionEpoch.toString(), operation, canonicalParametersHash).joinToString("\u0000")
        return MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
```

Freshness is exact identity equality over source, content language, parser revision, partition/session epoch, operation, and canonical parameters plus `fetchedAt <= now < expiresAt`. A parser revision or language change is a cache miss even if an arbitrary payload key collides. The scoped `replaceRebuildableEntry` is permitted only because this entire physical database is excluded and rebuildable.

```kotlin
package org.mewx.wenku8.core.storage.db.catalog

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NovelEntity::class, VolumeEntity::class, ChapterEntity::class, CacheMetadataEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CatalogCacheDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun cacheMetadataDao(): CacheMetadataDao

    companion object {
        const val FILE_NAME = "wenku8-catalog-cache.db"
    }
}
```

- [ ] **Step 5: Complete and run the focused test**

In the test helpers, use fixed values: `sourceId="wenku8-public"`, `novelRemoteId="1"`, `contentLanguage="SIMPLIFIED_CHINESE"`, parser revision `1`, fetched time `1`, expiry `200`, operation `detail`, parameters hash `p`. Run:

```powershell
.\gradlew.bat :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.CatalogCacheDatabaseTest :core:storage:kspDebugKotlin --console=plain --stacktrace --no-parallel
```

Expected: test passes and Room exports `CatalogCacheDatabase/1.json`.

- [ ] **Step 6: Commit the excluded catalog/cache store**

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/db/catalog core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/CatalogCacheDatabaseTest.kt core/storage/schemas/org.mewx.wenku8.core.storage.db.catalog.CatalogCacheDatabase/1.json
git diff --check --cached
git commit -m "feat(storage): add catalog cache database"
```

Expected: Task 3 files only.

### Task 4: Enforce Physical Store Isolation And Schema Continuity

**Depends on:** Tasks 2 and 3.

**Files:**
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/DatabasePaths.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/db/StorageDatabaseFactory.kt`
- Create: `core/storage/src/test/java/org/mewx/wenku8/core/storage/db/EntityContractTest.kt`
- Create: `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/DatabasePhysicalBoundaryTest.kt`
- Create: `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db/DatabaseUpgradeTest.kt`

- [ ] **Step 1: Write failing entity and physical-boundary tests**

The pure JVM test must reflect over every entity from Tasks 2-3 and reject field names matching `password|captcha|cookie|rawHtml|responseBody`. It must also assert that every backed-up canonical entity has both `mutationVersion` and `legacyProjectionVersion`, except `AppliedMutationEntity` and `CanonicalDomainVersionEntity`, whose own fields encode the protocol.

The device test must assert these exact paths:

```kotlin
assertEquals(context.getDatabasePath("wenku8-user.db"), paths.userDatabase)
assertEquals(File(context.noBackupFilesDir, "database/wenku8-catalog-cache.db"), paths.catalogCacheDatabase)
assertEquals(context.getDatabasePath("migration-transient.db"), paths.migrationTransientDatabase)
assertEquals(3, setOf(paths.userDatabase, paths.catalogCacheDatabase, paths.migrationTransientDatabase).size)
```

It must open all three databases, query `PRAGMA database_list`, and assert each main path equals the path above. Use `ApplicationProvider.getApplicationContext()` and close every database in `finally`.

- [ ] **Step 2: Run both tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.db.EntityContractTest" :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.DatabasePhysicalBoundaryTest --console=plain --stacktrace --no-parallel
```

Expected: unresolved `DatabasePaths`/`StorageDatabaseFactory`; the entity test may already pass once compiled.

- [ ] **Step 3: Add the exact physical path policy**

Create:

```kotlin
package org.mewx.wenku8.core.storage.db

import android.content.Context
import java.io.File

data class DatabasePaths(
    val userDatabase: File,
    val catalogCacheDatabase: File,
    val migrationTransientDatabase: File,
) {
    init {
        require(setOf(userDatabase.canonicalFile, catalogCacheDatabase.canonicalFile, migrationTransientDatabase.canonicalFile).size == 3) {
            "Storage databases must use distinct physical files"
        }
    }

    companion object {
        const val USER_DATABASE = "wenku8-user.db"
        const val CATALOG_CACHE_DATABASE = "wenku8-catalog-cache.db"
        const val MIGRATION_TRANSIENT_DATABASE = "migration-transient.db"

        fun from(context: Context): DatabasePaths = DatabasePaths(
            userDatabase = context.getDatabasePath(USER_DATABASE),
            catalogCacheDatabase = File(context.noBackupFilesDir, "database/$CATALOG_CACHE_DATABASE"),
            migrationTransientDatabase = context.getDatabasePath(MIGRATION_TRANSIENT_DATABASE),
        )
    }
}
```

- [ ] **Step 4: Add the database factory without destructive fallback**

Create:

```kotlin
package org.mewx.wenku8.core.storage.db

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import org.mewx.wenku8.core.storage.db.catalog.CatalogCacheDatabase
import org.mewx.wenku8.core.storage.db.user.UserLibraryDatabase
import org.mewx.wenku8.core.storage.migration.MigrationTransientDatabase

class StorageDatabaseFactory(private val applicationContext: Context) {
    val paths: DatabasePaths = DatabasePaths.from(applicationContext)

    fun openUserDatabase(): UserLibraryDatabase = Room.databaseBuilder(
        applicationContext,
        UserLibraryDatabase::class.java,
        DatabasePaths.USER_DATABASE,
    ).build()

    fun openCatalogCacheDatabase(): CatalogCacheDatabase {
        paths.catalogCacheDatabase.parentFile?.mkdirs()
        val context = object : ContextWrapper(applicationContext) {
            override fun getDatabasePath(name: String) = paths.catalogCacheDatabase
        }
        return Room.databaseBuilder(
            context,
            CatalogCacheDatabase::class.java,
            DatabasePaths.CATALOG_CACHE_DATABASE,
        ).build()
    }

    fun openMigrationTransientDatabase(): MigrationTransientDatabase =
        MigrationTransientDatabase.create(applicationContext)
}
```

Do not call `fallbackToDestructiveMigration`, `deleteDatabase`, or `clearAllTables` in this factory.

- [ ] **Step 5: Add the schema-continuity instrumentation test**

Use `MigrationTestHelper` with both exported schema directories. For version 1, call `createDatabase(FILE_NAME, 1).close()` then open through `StorageDatabaseFactory` and assert all expected table names. Add a parameter list `listOf(1)` named `SHIPPED_USER_SCHEMAS` and `SHIPPED_CACHE_SCHEMAS`; every future Room-version commit must add its previous version and migration before this test can pass. The test must scan each schema directory and assert the checked-in JSON versions exactly equal the corresponding list.

- [ ] **Step 6: Run the database boundary and continuity suite**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.db.EntityContractTest" :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.db.DatabasePhysicalBoundaryTest,org.mewx.wenku8.core.storage.db.DatabaseUpgradeTest --console=plain --stacktrace --no-parallel
```

Expected: all tests pass; the test report shows three distinct paths and no destructive migration.

- [ ] **Step 7: Commit physical isolation and schema gates**

Run from `studio-android/LightNovelLibrary`:

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/db core/storage/src/test/java/org/mewx/wenku8/core/storage/db core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/db
git diff --check --cached
git commit -m "feat(storage): isolate room stores physically"
```

Expected: Task 4 files only.

### Task 5: Lock Legacy Paths And Byte/Semantic Codecs

**Depends on:** Task 1 and the Phase 0 artifact/path golden manifest.

**Files:**
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyFileSystem.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/AndroidLegacyFileSystem.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyPathPolicy.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacySaveCodec.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacySearchHistoryCodec.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyBookshelfCodec.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyReaderProgressCodec.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyCatalogXmlCodec.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy/LegacyChapterDocumentCodec.kt`
- Test: `core/storage/src/test/java/org/mewx/wenku8/core/storage/legacy/LegacyCodecGoldenTest.kt`
- Test: `core/storage/src/test/java/org/mewx/wenku8/core/storage/legacy/LegacyPathPolicyTest.kt`
- Fixtures: `core/storage/src/test/resources/legacy/{search_history,bookshelf,progress,catalog}/`

- [ ] **Step 1: Copy only the approved Phase 0 golden fixture bytes and write failing hashes**

For each Phase 0 fixture ID owned by `search_history.wk8`, `bookshelf_local.wk8`, `read_saves.wk8`, `read_saves_v1.wk8`, `saves/intro`, and `saves/novel/{cid}.xml`, copy its already-approved byte fixture into the matching directory. Add `fixtures.sha256` containing `<lowercase-sha256><two spaces><relative-name>` for every file. Do not synthesize replacement bytes when a shipped fixture is missing; stop and repair Phase 0 evidence.

Write tests that:

1. recompute every hash;
2. decode valid records to the exact Phase 0 semantic JSON;
3. re-encode valid records to identical bytes where the old writer was deterministic;
4. retain `InvalidLegacyRecord(raw, reason)` entries without deletion;
5. assert primary/backup lookup and `.migration_completed` semantics from the Phase 0 path manifest.

- [ ] **Step 2: Run the golden suite and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.legacy.*" --console=plain --stacktrace --no-parallel
```

Expected: unresolved codec/path types; fixture hashes themselves pass.

- [ ] **Step 3: Add the shared codec result and filesystem contract**

Create these complete declarations:

```kotlin
package org.mewx.wenku8.core.storage.legacy

import java.io.InputStream

data class InvalidLegacyRecord(val raw: String, val reason: String)
data class LegacyDecodeResult<T>(val records: List<T>, val invalid: List<InvalidLegacyRecord>)

interface LegacyFileSystem {
    suspend fun exists(path: LegacyPath): Boolean
    suspend fun read(path: LegacyPath): ByteArray?
    suspend fun open(path: LegacyPath): InputStream?
    suspend fun list(path: LegacyPath): List<LegacyPath>
    suspend fun atomicReplace(path: LegacyPath, bytes: ByteArray, priorSuffix: String = ".prior"): Boolean
}

@JvmInline
value class LegacyPath(val value: String) {
    init {
        require(value.isNotBlank())
        require(!value.contains(".."))
    }
}
```

`AndroidLegacyFileSystem.atomicReplace` must implement this exact order: create sibling `.tmp-<UUID>`, write all bytes, flush, `FileDescriptor.sync`, rename existing target to `.prior`, atomically move temp to target with `ATOMIC_MOVE` when supported, fall back to same-directory replace, verify target hash/length, retain `.prior` until verification, and restore `.prior` if replacement fails. SAF writes use a sibling temporary document plus provider rename when supported; when the provider cannot atomically rename, write a separately named verified replacement and retain the original as the known-good prior version. It must never truncate the target in place.

- [ ] **Step 4: Add the exact path policy**

Create a policy whose public contract is:

```kotlin
package org.mewx.wenku8.core.storage.legacy

data class LegacyRoots(
    val internalRoot: LegacyPath,
    val externalRoot: LegacyPath?,
    val safRoot: LegacyPath?,
    val externalCopyCompleted: Boolean,
)

class LegacyPathPolicy(private val roots: LegacyRoots) {
    fun readCandidates(relative: String): List<LegacyPath> {
        val clean = relative.replace('\\', '/').trimStart('/')
        require(clean.isNotBlank() && !clean.split('/').contains(".."))
        val internal = LegacyPath("${roots.internalRoot.value}/$clean")
        val external = (roots.safRoot ?: roots.externalRoot)?.let { LegacyPath("${it.value}/$clean") }
        return if (roots.externalCopyCompleted) listOfNotNull(internal, external) else listOfNotNull(external, internal)
    }

    fun projectionTarget(relative: String): LegacyPath = readCandidates(relative).first()
    fun migrationSentinel(): LegacyPath = LegacyPath("${roots.internalRoot.value}/.migration_completed")
}
```

The Android root factory must derive paths from `filesDir`, the historical `/wenku8/` root, the persisted SAF tree, and the legacy sentinel. It must not reinterpret `.migration_completed` as a Phase 3 completion flag.

- [ ] **Step 5: Add complete search, bookshelf, and progress codecs**

Use these data contracts and exact delimiters:

```kotlin
package org.mewx.wenku8.core.storage.legacy

data class LegacyPixelProgress(val cid: Int, val position: Int, val viewportHeight: Int)
data class LegacyReaderProgress(val aid: Int, val volumeId: Int, val cid: Int, val lineId: Int, val wordId: Int)

object LegacySearchHistoryCodec {
    fun decode(bytes: ByteArray): LegacyDecodeResult<String> {
        val text = bytes.toString(Charsets.UTF_8)
        val records = mutableListOf<String>()
        val invalid = mutableListOf<InvalidLegacyRecord>()
        var offset = 0
        while (offset < text.length) {
            val start = text.indexOf('[', offset)
            if (start < 0) {
                if (text.substring(offset).isNotBlank()) invalid += InvalidLegacyRecord(text.substring(offset), "missing-open-bracket")
                break
            }
            if (start > offset && text.substring(offset, start).isNotBlank()) {
                invalid += InvalidLegacyRecord(text.substring(offset, start), "bytes-between-records")
            }
            val end = text.indexOf(']', start + 1)
            if (end < 0) {
                invalid += InvalidLegacyRecord(text.substring(start), "missing-close-bracket")
                break
            }
            records += text.substring(start + 1, end)
            offset = end + 1
        }
        return LegacyDecodeResult(records, invalid)
    }

    fun encode(records: List<String>): ByteArray = records.joinToString("") { "[$it]" }.toByteArray(Charsets.UTF_8)
}

object LegacyBookshelfCodec {
    fun decode(bytes: ByteArray): LegacyDecodeResult<Int> {
        val valid = mutableListOf<Int>()
        val invalid = mutableListOf<InvalidLegacyRecord>()
        bytes.toString(Charsets.UTF_8).split("||").filter(String::isNotEmpty).forEach { raw ->
            raw.toIntOrNull()?.let(valid::add) ?: invalid.add(InvalidLegacyRecord(raw, "invalid-aid"))
        }
        return LegacyDecodeResult(valid, invalid)
    }

    fun encode(records: List<Int>): ByteArray = records.joinToString("||").toByteArray(Charsets.UTF_8)
}

object LegacyReaderProgressCodec {
    fun decodePixel(bytes: ByteArray): LegacyDecodeResult<LegacyPixelProgress> = decode(bytes, "||", ",,", 3) { parts ->
        LegacyPixelProgress(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }

    fun decodeReader(bytes: ByteArray): LegacyDecodeResult<LegacyReaderProgress> = decode(bytes, "||", ":", 5) { parts ->
        LegacyReaderProgress(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), parts[4].toInt())
    }

    fun encodePixel(records: List<LegacyPixelProgress>): ByteArray = records.joinToString("||") {
        "${it.cid},,${it.position},,${it.viewportHeight}"
    }.toByteArray(Charsets.UTF_8)

    fun encodeReader(records: List<LegacyReaderProgress>): ByteArray = records.joinToString("||") {
        "${it.aid}:${it.volumeId}:${it.cid}:${it.lineId}:${it.wordId}"
    }.toByteArray(Charsets.UTF_8)

    private fun <T> decode(
        bytes: ByteArray,
        recordDelimiter: String,
        fieldDelimiter: String,
        expectedFields: Int,
        convert: (List<String>) -> T,
    ): LegacyDecodeResult<T> {
        val records = mutableListOf<T>()
        val invalid = mutableListOf<InvalidLegacyRecord>()
        bytes.toString(Charsets.UTF_8).split(recordDelimiter).filter(String::isNotEmpty).forEach { raw ->
            val parts = raw.split(fieldDelimiter)
            if (parts.size != expectedFields) {
                invalid += InvalidLegacyRecord(raw, "invalid-field-count")
                return@forEach
            }
            try {
                records += convert(parts)
            } catch (_: RuntimeException) {
                invalid += InvalidLegacyRecord(raw, "invalid-progress-record")
            }
        }
        return LegacyDecodeResult(records, invalid)
    }
}
```

- [ ] **Step 6: Add the complete catalog XML, chapter-document, and facade codecs**

`LegacyCatalogXmlCodec` uses a namespace-aware, external-entity-disabled pull parser. It returns ordered `LegacyVolumeRecord`/`LegacyChapterRecord` values plus invalid records, preserves the original bytes for compatibility reads, and never evaluates DTD/entity declarations. `LegacyChapterDocumentCodec` separately handles the historically named `{cid}.xml` files, which are CRLF-delimited text with paired `<!--image-->` markers rather than XML.

The XML parser configuration must include these exact protections before parsing:

```kotlin
factory.isNamespaceAware = true
factory.setFeature("http://xmlpull.org/v1/doc/features.html#process-docdecl", false)
parser.setInput(ByteArrayInputStream(bytes), "UTF-8")
```

If the Android pull-parser implementation cannot guarantee declaration rejection in a JVM test, reject any input containing `<!DOCTYPE` or `<!ENTITY` before constructing the parser and return `InvalidLegacyRecord(rawInput, "forbidden-xml-declaration")`.

Implement these exact chapter semantics:

```kotlin
package org.mewx.wenku8.core.storage.legacy

sealed interface LegacyChapterBlock {
    data class Text(val value: String) : LegacyChapterBlock
    data class Image(val value: String) : LegacyChapterBlock
    data object CrLf : LegacyChapterBlock
}

object LegacyChapterDocumentCodec {
    private const val IMAGE = "<!--image-->"

    fun decode(bytes: ByteArray): LegacyDecodeResult<LegacyChapterBlock> {
        val records = mutableListOf<LegacyChapterBlock>()
        val invalid = mutableListOf<InvalidLegacyRecord>()
        val document = bytes.toString(Charsets.UTF_8)
        var lineStart = 0
        while (lineStart <= document.length) {
            val crlf = document.indexOf("\r\n", lineStart)
            val lineEnd = if (crlf >= 0) crlf else document.length
            val line = document.substring(lineStart, lineEnd)
            var cursor = 0
            while (cursor < line.length) {
                val start = line.indexOf(IMAGE, cursor)
                if (start < 0) {
                    records += LegacyChapterBlock.Text(line.substring(cursor))
                    cursor = line.length
                    break
                }
                if (start > cursor) records += LegacyChapterBlock.Text(line.substring(cursor, start))
                val end = line.indexOf(IMAGE, start + IMAGE.length)
                if (end < 0) {
                    invalid += InvalidLegacyRecord(line, "unpaired-image-marker")
                    records += LegacyChapterBlock.Text(line.substring(start))
                    cursor = line.length
                    break
                }
                records += LegacyChapterBlock.Image(line.substring(start + IMAGE.length, end))
                cursor = end + IMAGE.length
            }
            if (line.isEmpty()) records += LegacyChapterBlock.Text("")
            if (crlf < 0) break
            records += LegacyChapterBlock.CrLf
            lineStart = crlf + 2
        }
        return LegacyDecodeResult(records, invalid)
    }

    fun encode(records: List<LegacyChapterBlock>): ByteArray = buildString {
        records.forEach { block ->
            when (block) {
                is LegacyChapterBlock.Text -> append(block.value)
                is LegacyChapterBlock.Image -> append(IMAGE).append(block.value).append(IMAGE)
                LegacyChapterBlock.CrLf -> append("\r\n")
            }
        }
    }.toByteArray(Charsets.UTF_8)
}
```

The RED fixture must include `前<!--image-->a.jpg<!--image-->中<!--image-->b.jpg<!--image-->后\r\n下一行` and assert the exact ordered block sequence `Text("前"), Image("a.jpg"), Text("中"), Image("b.jpg"), Text("后"), CrLf, Text("下一行")` plus byte-identical encode. Text before, between, and after image pairs is never trimmed or discarded; an unpaired marker is retained as text and reported invalid.

Implement the catalog parser completely as follows; it recognizes `volume[vid]`, direct volume title text, ordered `chapter[cid]` leaf text, and intro `data[name]` entries used by approved fixtures. Missing/noninteger IDs become invalid records and never silently become `0`:

```kotlin
package org.mewx.wenku8.core.storage.legacy

import java.io.ByteArrayInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

data class LegacyChapterRecord(val cid: Int, val title: String, val sortIndex: Int)
data class LegacyVolumeRecord(val vid: Int, val title: String, val sortIndex: Int, val chapters: List<LegacyChapterRecord>)
data class LegacyCatalogDocument(
    val metadata: Map<String, String>,
    val volumes: List<LegacyVolumeRecord>,
    val originalBytes: ByteArray,
    val invalid: List<InvalidLegacyRecord>,
)

class LegacyCatalogXmlCodec {
    fun decode(bytes: ByteArray): LegacyCatalogDocument {
        val rawInput = bytes.toString(Charsets.UTF_8)
        if (rawInput.contains("<!DOCTYPE", ignoreCase = true) || rawInput.contains("<!ENTITY", ignoreCase = true)) {
            return LegacyCatalogDocument(emptyMap(), emptyList(), bytes.copyOf(), listOf(InvalidLegacyRecord("xml-declaration", "forbidden-xml-declaration")))
        }
        val metadata = linkedMapOf<String, String>()
        val volumes = mutableListOf<LegacyVolumeRecord>()
        val invalid = mutableListOf<InvalidLegacyRecord>()
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            factory.setFeature("http://xmlpull.org/v1/doc/features.html#process-docdecl", false)
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(bytes), "UTF-8")
            var currentVid: Int? = null
            var currentTitle = ""
            var currentChapters = mutableListOf<LegacyChapterRecord>()
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "data" -> {
                            val name = parser.attribute("name") ?: parser.attributeAt(0)
                            val attributeValue = parser.attribute("value")
                            val value = attributeValue ?: parser.nextText()
                            if (name.isNullOrBlank()) invalid += InvalidLegacyRecord("data", "missing-data-name")
                            else metadata[name] = value
                        }
                        "volume" -> {
                            currentVid = (parser.attribute("vid") ?: parser.attributeAt(0))?.toIntOrNull()
                            currentTitle = ""
                            currentChapters = mutableListOf()
                            if (currentVid == null) invalid += InvalidLegacyRecord("volume", "invalid-vid")
                        }
                        "chapter" -> {
                            val rawCid = parser.attribute("cid") ?: parser.attributeAt(0)
                            val cid = rawCid?.toIntOrNull()
                            val title = parser.nextText()
                            if (cid == null || currentVid == null) invalid += InvalidLegacyRecord(rawCid.orEmpty(), "invalid-cid-or-parent")
                            else currentChapters += LegacyChapterRecord(cid, title, currentChapters.size)
                        }
                    }
                    XmlPullParser.TEXT -> if (currentVid != null && parser.text.trim().isNotEmpty()) {
                        currentTitle = parser.text.trim()
                    }
                    XmlPullParser.END_TAG -> if (parser.name == "volume") {
                        currentVid?.let { volumes += LegacyVolumeRecord(it, currentTitle, volumes.size, currentChapters.toList()) }
                        currentVid = null
                        currentTitle = ""
                        currentChapters = mutableListOf()
                    }
                }
                event = parser.next()
            }
            LegacyCatalogDocument(metadata.toMap(), volumes.toList(), bytes.copyOf(), invalid.toList())
        } catch (failure: Exception) {
            LegacyCatalogDocument(metadata.toMap(), volumes.toList(), bytes.copyOf(), invalid + InvalidLegacyRecord(failure.javaClass.simpleName, "malformed-catalog-xml"))
        }
    }

    private fun XmlPullParser.attribute(name: String): String? = getAttributeValue(null, name)?.takeIf(String::isNotEmpty)
    private fun XmlPullParser.attributeAt(index: Int): String? =
        if (attributeCount > index) getAttributeValue(index)?.takeIf(String::isNotEmpty) else null
}
```

Preserve `originalBytes` so rollback reads can use the exact source even after a partial semantic import. Its implementation and tests are independent of Wild.

Create the facade without Android globals:

```kotlin
package org.mewx.wenku8.core.storage.legacy

class LegacySaveCodec(
    val searchHistory: LegacySearchHistoryCodec = LegacySearchHistoryCodec,
    val bookshelf: LegacyBookshelfCodec = LegacyBookshelfCodec,
    val readerProgress: LegacyReaderProgressCodec = LegacyReaderProgressCodec,
    val catalogXml: LegacyCatalogXmlCodec = LegacyCatalogXmlCodec(),
    val chapterDocument: LegacyChapterDocumentCodec = LegacyChapterDocumentCodec,
)
```

Fixtures cover intro metadata, volume/chapter order, CRLF, blank-space lines, paired/unpaired image markers, ordered text/images, malformed catalog XML, and entity-expansion rejection.

- [ ] **Step 7: Run golden, malformed, and path-precedence tests**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.legacy.*" --console=plain --stacktrace --no-parallel
```

Expected: all approved hashes, semantic assertions, deterministic byte round trips, malformed preservation cases, and path/sentinel precedence cases pass.

- [ ] **Step 8: Commit the legacy compatibility foundation**

Run from `studio-android/LightNovelLibrary`:

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/legacy core/storage/src/test/java/org/mewx/wenku8/core/storage/legacy core/storage/src/test/resources/legacy
git diff --check --cached
git commit -m "feat(storage): lock legacy save compatibility"
```

Expected: no source copied from Wild and no fixture outside the approved Phase 0 artifact set.

### Task 6: Extend Migration-Transient State And Add Immutable Snapshot Storage

**Depends on:** Tasks 2, 4, and 5. Preserve every Phase 1 settings table and row.

**Files:**
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationRecords.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationDao.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationSnapshotStore.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/AndroidMigrationSnapshotStore.kt`
- Modify: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationTransientDatabase.kt`
- Create: `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/MigrationTransientUpgradeTest.kt`
- Create: `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/MigrationDaoTest.kt`
- Create: `core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/AndroidMigrationSnapshotStoreTest.kt`
- Modify: `core/storage/schemas/org.mewx.wenku8.core.storage.migration.MigrationTransientDatabase/`

- [ ] **Step 1: Write failing upgrade, transition, checkpoint, and lease tests**

The upgrade test creates the Phase 1 schema at version 1, inserts one `SettingsMigrationRecord` and one `SettingsJournalRecord`, closes it, upgrades to version 2, and asserts those rows remain byte/field identical while the generic domain/snapshot/checkpoint/legacy-reservation/legacy-delta/canonical-journal/lease tables exist.

The DAO test asserts:

- `NotStarted -> Snapshotting` succeeds once and a second compare-and-set returns `0`;
- checkpoint `(domainKey,itemKey)` is replaced only when the incoming source hash/revision changes;
- duplicate `mutationId` insertion is ignored;
- pending journals are returned in `mutationVersion, createdAtEpochMillis` order;
- legacy deltas are returned strictly by reserved mutation version and cannot be replaced;
- a reservation can advance only `RESERVED -> LEGACY_WRITTEN -> DELTA_APPENDED`, and process reconstruction completes the missing transition from recorded before/after hashes;
- a non-expired lease blocks another owner;
- the same owner/generation/boot can renew only before expiry; renewal never changes generation; an expired lease or a lease from a different Android boot-session ID can be claimed only at the exact generation already fenced in canonical Room;
- release succeeds only for the current owner/generation.

The snapshot-store test copies two mutable legacy sources while holding a fake barrier, modifies the live source after sealing, and proves every later chunk/process reads the original immutable snapshot bytes. It also covers rejection of uppercase/short/path-like snapshot IDs; a temp-dir crash; orphan cleanup; an injected failure after entry insertion that rolls back both entries and seal; an injected failure before seal insertion that leaves no partial DB set; missing/extra entry; length/hash mismatch; pre-import re-snapshot; and post-checkpoint rollback of only `originSnapshotId` rows before re-snapshot.

- [ ] **Step 2: Run the tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.migration.MigrationTransientUpgradeTest,org.mewx.wenku8.core.storage.migration.MigrationDaoTest,org.mewx.wenku8.core.storage.migration.AndroidMigrationSnapshotStoreTest --console=plain --stacktrace --no-parallel
```

Expected: unresolved generic migration entities/DAO or missing version-2 schema.

- [ ] **Step 3: Add the complete transient records**

Create:

```kotlin
package org.mewx.wenku8.core.storage.migration

import androidx.room.Entity
import androidx.room.Index

enum class MigrationPhase {
    NotStarted, Snapshotting, Importing, DualWrite, Reconciling, Verified, LegacyReadOnly, Complete
}

enum class JournalStatus { PENDING_CANONICAL, CANONICAL_COMMITTED, PROJECTION_APPLIED, FAILED_RECOVERABLE }
enum class SnapshotStatus { COPYING, SEALED }
enum class LegacyReservationStatus { RESERVED, LEGACY_WRITTEN, DELTA_APPENDED }
enum class LegacyDeltaStatus { PENDING_REPLAY, CANONICAL_REPLAYED }

@Entity(tableName = "migration_domain", primaryKeys = ["domainKey"])
data class MigrationDomainRecord(
    val domainKey: String,
    val phase: String,
    val inputRevision: Long,
    val snapshotId: String?,
    val snapshotSha256: String?,
    val updatedAtEpochMillis: Long,
)

@Entity(tableName = "migration_snapshot_seal", primaryKeys = ["snapshotId"], indices = [Index("domainKey", unique = true)])
data class MigrationSnapshotSealRecord(
    val snapshotId: String,
    val domainKey: String,
    val snapshotVersionFloor: Long,
    val directorySha256: String,
    val entryCount: Int,
    val status: String,
    val createdAtEpochMillis: Long,
)

@Entity(tableName = "migration_snapshot_entry", primaryKeys = ["snapshotId", "itemKey"], indices = [Index("domainKey")])
data class MigrationSnapshotRecord(
    val snapshotId: String,
    val domainKey: String,
    val itemKey: String,
    val ordinal: Int,
    val sourceRelativePath: String,
    val snapshotRelativePath: String,
    val sha256: String,
    val byteLength: Long,
)

@Entity(tableName = "migration_checkpoint", primaryKeys = ["domainKey", "snapshotId", "itemKey"], indices = [Index("mutationId", unique = true)])
data class MigrationCheckpointRecord(
    val domainKey: String,
    val snapshotId: String,
    val itemKey: String,
    val sourceSha256: String,
    val sourceRevision: Long,
    val mutationId: String,
    val importedCanonicalVersion: Long?,
    val status: String,
)

@Entity(tableName = "legacy_write_reservation", primaryKeys = ["mutationId"], indices = [Index("domainKey", "mutationVersion", unique = true)])
data class LegacyWriteReservationRecord(
    val mutationId: String,
    val domainKey: String,
    val mutationVersion: Long,
    val payload: ByteArray,
    val payloadSha256: String,
    val legacyBeforeSha256: String?,
    val expectedLegacyAfterSha256: String,
    val observedLegacyAfterSha256: String?,
    val status: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(tableName = "legacy_delta", primaryKeys = ["domainKey", "mutationVersion"], indices = [Index("mutationId", unique = true), Index("status")])
data class LegacyDeltaRecord(
    val domainKey: String,
    val mutationVersion: Long,
    val mutationId: String,
    val payload: ByteArray,
    val payloadSha256: String,
    val legacyAfterSha256: String,
    val status: String,
    val createdAtEpochMillis: Long,
)

@Entity(tableName = "migration_journal", primaryKeys = ["mutationId"], indices = [Index("domainKey", "status"), Index("mutationVersion")])
data class MigrationJournalRecord(
    val mutationId: String,
    val domainKey: String,
    val payload: ByteArray,
    val mutationVersion: Long,
    val status: String,
    val attemptCount: Int,
    val lastFailureCode: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(tableName = "transfer_lease", primaryKeys = ["workKey"], indices = [Index("expiresAtElapsedRealtime")])
data class TransferLeaseRecord(
    val workKey: String,
    val ownerId: String,
    val generation: Long,
    val bootSessionId: Long,
    val expiresAtElapsedRealtime: Long,
)
```

`payload` is local excluded transient data, but participant codecs must still reject password, captcha, Cookie, raw authenticated response, and unbounded body fields before creating it.

- [ ] **Step 4: Add compare-and-set DAO operations**

Create this complete DAO. Lease acquisition is one Room transaction; it inserts once, renews the same owner, or changes owner only after expiry:

```kotlin
package org.mewx.wenku8.core.storage.migration

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class MigrationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertDomain(value: MigrationDomainRecord): Long

    @Query("SELECT * FROM migration_domain WHERE domainKey = :domainKey")
    abstract suspend fun domain(domainKey: String): MigrationDomainRecord?

    @Query("UPDATE migration_domain SET phase = :next, updatedAtEpochMillis = :now WHERE domainKey = :domainKey AND phase = :expected")
    abstract suspend fun compareAndSetPhase(domainKey: String, expected: String, next: String, now: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertSnapshotSeal(value: MigrationSnapshotSealRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertSnapshotEntries(values: List<MigrationSnapshotRecord>): List<Long>

    @Transaction
    open suspend fun insertSealedSnapshot(
        id: SnapshotId,
        seal: MigrationSnapshotSealRecord,
        entries: List<MigrationSnapshotRecord>,
    ) {
        require(seal.snapshotId == id.value && seal.status == "SEALED")
        require(seal.entryCount == entries.size)
        require(entries.map { it.itemKey }.toSet().size == entries.size)
        require(entries.map { it.ordinal } == entries.indices.toList())
        require(entries.all {
            it.snapshotId == id.value && it.domainKey == seal.domainKey &&
                it.sha256.matches(Regex("[0-9a-f]{64}")) && it.byteLength >= 0L
        })
        check(insertSnapshotEntries(entries).none { it == -1L })
        check(insertSnapshotSeal(seal) != -1L)
    }

    @Query("SELECT * FROM migration_snapshot_seal WHERE snapshotId = :snapshotId")
    abstract suspend fun snapshotSeal(snapshotId: String): MigrationSnapshotSealRecord?

    @Query("SELECT * FROM migration_snapshot_entry WHERE snapshotId = :snapshotId ORDER BY ordinal, itemKey")
    abstract suspend fun snapshotEntries(snapshotId: String): List<MigrationSnapshotRecord>

    @Query("DELETE FROM migration_snapshot_entry WHERE snapshotId = :snapshotId")
    abstract suspend fun deleteSnapshotEntries(snapshotId: String): Int

    @Query("DELETE FROM migration_snapshot_seal WHERE snapshotId = :snapshotId")
    abstract suspend fun deleteSnapshotSeal(snapshotId: String): Int

    @Query("SELECT * FROM migration_snapshot_entry WHERE domainKey = :domainKey ORDER BY ordinal, itemKey")
    abstract suspend fun snapshots(domainKey: String): List<MigrationSnapshotRecord>

    @Query("SELECT * FROM migration_checkpoint WHERE domainKey = :domainKey AND snapshotId = :snapshotId AND itemKey = :itemKey")
    protected abstract suspend fun checkpoint(domainKey: String, snapshotId: String, itemKey: String): MigrationCheckpointRecord?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertCheckpoint(value: MigrationCheckpointRecord): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun updateCheckpointAfterGuard(value: MigrationCheckpointRecord): Int

    @Transaction
    open suspend fun upsertCheckpointIfChanged(value: MigrationCheckpointRecord): Boolean {
        val current = checkpoint(value.domainKey, value.snapshotId, value.itemKey)
        if (current?.sourceSha256 == value.sourceSha256 && current.sourceRevision == value.sourceRevision) return false
        if (current == null) {
            check(insertCheckpoint(value) != -1L)
        } else {
            check(updateCheckpointAfterGuard(value) == 1)
        }
        return true
    }

    @Query("SELECT COUNT(*) FROM migration_checkpoint WHERE snapshotId = :snapshotId")
    abstract suspend fun checkpointCount(snapshotId: String): Int

    @Query("DELETE FROM migration_checkpoint WHERE snapshotId = :snapshotId")
    abstract suspend fun clearSnapshotCheckpoints(snapshotId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertLegacyReservation(value: LegacyWriteReservationRecord): Long

    @Query("SELECT * FROM legacy_write_reservation WHERE mutationId = :mutationId")
    abstract suspend fun legacyReservation(mutationId: String): LegacyWriteReservationRecord?

    @Query("SELECT * FROM legacy_write_reservation WHERE domainKey = :domainKey AND status != 'DELTA_APPENDED' ORDER BY mutationVersion LIMIT :limit")
    abstract suspend fun incompleteLegacyReservations(domainKey: String, limit: Int): List<LegacyWriteReservationRecord>

    @Query("UPDATE legacy_write_reservation SET status = :next, observedLegacyAfterSha256 = :afterSha256, updatedAtEpochMillis = :now WHERE mutationId = :mutationId AND status = :expected")
    abstract suspend fun compareAndSetLegacyReservation(
        mutationId: String,
        expected: String,
        next: String,
        afterSha256: String?,
        now: Long,
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertLegacyDelta(value: LegacyDeltaRecord): Long

    @Transaction
    open suspend fun appendLegacyDelta(value: LegacyDeltaRecord, now: Long): Boolean {
        if (insertLegacyDelta(value) == -1L) return false
        check(compareAndSetLegacyReservation(
            value.mutationId,
            LegacyReservationStatus.LEGACY_WRITTEN.name,
            LegacyReservationStatus.DELTA_APPENDED.name,
            value.legacyAfterSha256,
            now,
        ) == 1)
        return true
    }

    @Query("SELECT * FROM legacy_delta WHERE domainKey = :domainKey AND mutationVersion > :afterVersion AND status = 'PENDING_REPLAY' ORDER BY mutationVersion LIMIT :limit")
    abstract suspend fun pendingLegacyDeltas(domainKey: String, afterVersion: Long, limit: Int): List<LegacyDeltaRecord>

    @Query("UPDATE legacy_delta SET status = 'CANONICAL_REPLAYED' WHERE domainKey = :domainKey AND mutationVersion = :version AND status = 'PENDING_REPLAY'")
    abstract suspend fun markLegacyDeltaReplayed(domainKey: String, version: Long): Int

    @Query("DELETE FROM legacy_delta WHERE domainKey = :domainKey AND mutationVersion <= :throughVersion AND status = 'CANONICAL_REPLAYED'")
    abstract suspend fun deleteReplayedLegacyDeltasThrough(domainKey: String, throughVersion: Long): Int

    @Query("DELETE FROM legacy_write_reservation WHERE domainKey = :domainKey AND mutationVersion <= :throughVersion AND status = 'DELTA_APPENDED'")
    abstract suspend fun deleteCompletedLegacyReservationsThrough(domainKey: String, throughVersion: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertJournal(value: MigrationJournalRecord): Long

    @Query("SELECT * FROM migration_journal WHERE mutationId = :mutationId")
    abstract suspend fun journal(mutationId: String): MigrationJournalRecord?

    @Query("SELECT * FROM migration_journal WHERE domainKey = :domainKey AND status != 'PROJECTION_APPLIED' ORDER BY mutationVersion, createdAtEpochMillis LIMIT :limit")
    abstract suspend fun pendingJournals(domainKey: String, limit: Int): List<MigrationJournalRecord>

    @Query("UPDATE migration_journal SET status = :next, attemptCount = attemptCount + :attemptDelta, lastFailureCode = :failureCode, updatedAtEpochMillis = :now WHERE mutationId = :mutationId AND status = :expected")
    abstract suspend fun compareAndSetJournalStatus(
        mutationId: String,
        expected: String,
        next: String,
        attemptDelta: Int,
        failureCode: String?,
        now: Long,
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertLease(value: TransferLeaseRecord): Long

    @Query("SELECT * FROM transfer_lease WHERE workKey = :workKey")
    abstract suspend fun lease(workKey: String): TransferLeaseRecord?

    @Query("UPDATE transfer_lease SET ownerId = :ownerId, generation = :nextGeneration, bootSessionId = :bootSessionId, expiresAtElapsedRealtime = :expires WHERE workKey = :workKey AND generation = :expectedGeneration AND (bootSessionId != :bootSessionId OR expiresAtElapsedRealtime <= :now)")
    protected abstract suspend fun claimExpiredLease(
        workKey: String,
        ownerId: String,
        expectedGeneration: Long,
        nextGeneration: Long,
        bootSessionId: Long,
        now: Long,
        expires: Long,
    ): Int

    @Transaction
    open suspend fun claimLeaseGeneration(
        workKey: String,
        ownerId: String,
        expectedGeneration: Long,
        nextGeneration: Long,
        bootSessionId: Long,
        now: Long,
        expires: Long,
    ): TransferLeaseRecord? {
        require(nextGeneration > expectedGeneration && expires > now)
        val first = TransferLeaseRecord(workKey, ownerId, nextGeneration, bootSessionId, expires)
        if (insertLease(first) != -1L) return first
        val updated = claimExpiredLease(workKey, ownerId, expectedGeneration, nextGeneration, bootSessionId, now, expires)
        return if (updated == 1) lease(workKey) else null
    }

    @Query("UPDATE transfer_lease SET expiresAtElapsedRealtime = :expires WHERE workKey = :workKey AND ownerId = :ownerId AND generation = :generation AND bootSessionId = :bootSessionId AND expiresAtElapsedRealtime > :now")
    protected abstract suspend fun renewLeaseCas(
        workKey: String,
        ownerId: String,
        generation: Long,
        bootSessionId: Long,
        now: Long,
        expires: Long,
    ): Int

    @Transaction
    open suspend fun renewLease(workKey: String, ownerId: String, generation: Long, bootSessionId: Long, now: Long, expires: Long): Boolean {
        require(expires > now)
        return renewLeaseCas(workKey, ownerId, generation, bootSessionId, now, expires) == 1
    }

    @Query("DELETE FROM transfer_lease WHERE workKey = :workKey AND ownerId = :ownerId AND generation = :generation")
    abstract suspend fun releaseLease(workKey: String, ownerId: String, generation: Long): Int
}
```

The journal insert uses `OnConflictStrategy.IGNORE`; no replacement of an existing mutation payload is allowed.

- [ ] **Step 5: Add the excluded immutable snapshot store**

Create the exact storage contract:

```kotlin
package org.mewx.wenku8.core.storage.migration

import java.io.InputStream

data class SnapshotSource(
    val itemKey: String,
    val sourceRelativePath: String,
    val open: suspend () -> InputStream,
)

@JvmInline
value class SnapshotId private constructor(val value: String) {
    companion object {
        fun parse(raw: String): SnapshotId {
            require(raw.matches(Regex("[0-9a-f]{64}"))) { "snapshotId must be lowercase SHA-256" }
            return SnapshotId(raw)
        }
    }
}

data class SnapshotHandle(
    val snapshotId: SnapshotId,
    val domain: MigrationDomainKey,
    val snapshotVersionFloor: Long,
    val directorySha256: String,
    val entries: List<MigrationSnapshotRecord>,
)

sealed interface SnapshotVerification {
    data object Valid : SnapshotVerification
    data class Invalid(val code: String, val itemKey: String?) : SnapshotVerification
}

interface MigrationSnapshotStore {
    suspend fun createSealed(
        domain: MigrationDomainKey,
        snapshotId: SnapshotId,
        snapshotVersionFloor: Long,
        sources: List<SnapshotSource>,
    ): SnapshotHandle

    suspend fun loadSealed(snapshotId: SnapshotId): SnapshotHandle?
    suspend fun verify(handle: SnapshotHandle): SnapshotVerification
    suspend fun <T> readVerified(handle: SnapshotHandle, itemKey: String, block: suspend (InputStream) -> T): T
    suspend fun discard(handle: SnapshotHandle)
    suspend fun cleanOrphans()
}
```

`AndroidMigrationSnapshotStore` uses only `context.noBackupFilesDir/wenku8-migration/snapshots/`. The coordinator derives the ID as lowercase SHA-256 of `domainKey + NUL + inputRevision + NUL + randomNonce`; every API accepts only `SnapshotId.parse`, never a raw path segment. The implementation resolves both `<snapshotId>.tmp` and `<snapshotId>` under the canonical snapshot root and rejects traversal, separators, symlinks, and an existing temp/final path. While `DomainWriteBarrier` is held, `createSealed` copies each live legacy source to a generated safe item filename, flushes and `FileDescriptor.sync()`s every file, records byte length and lowercase SHA-256, writes and fsyncs a sorted manifest, computes the directory hash from ordered `itemKey + NUL + length + NUL + sha256`, fsyncs the temp directory, and atomically renames it to `<snapshotId>` in the same parent. It then calls only `MigrationDao.insertSealedSnapshot(id, seal, entries)`, whose single Room transaction validates identity/count/ordinal/hash and inserts all entries plus the seal or rolls everything back. Existing snapshot IDs, entry rows, final directories, seals, and partial entry sets are never overwritten.

`loadSealed` and every `readVerified` call re-run `SnapshotId.parse` and compare the DB entry set, manifest, file length, file hash, directory hash, domain, and version floor before returning bytes. A seal without exactly its declared entry set, or entries without one seal, is invalid and never importable. Participants receive only `SnapshotHandle`/`readVerified`; their import code has no live `LegacyFileSystem.read/open` dependency.

Recovery rules are exact:

1. A `.tmp` directory or final directory without a sealed DB row is orphaned and removed from the excluded snapshot root.
2. A sealed row with no import checkpoint whose directory/entry is missing or mismatched is discarded and re-created under the domain barrier from a new stable live snapshot.
3. A sealed row with one or more checkpoints that later fails verification enters `SnapshotRecoveryRequired`; in one canonical Room transaction the participant calls only the Task 2/3 `deleteSnapshotRows(snapshotId)` and `deleteSnapshotMutations(snapshotId)` APIs, never a general clear/REPLACE. It then clears that snapshot's transient checkpoints/seal/entries, retains every legacy delta/reservation and live legacy file, and re-snapshots under the barrier.
4. A valid sealed snapshot remains immutable across chunks, worker retries, and process death; import always resumes from its DB checkpoint against the same handle.

- [ ] **Step 6: Upgrade the existing database additively**

Modify the Phase 1 database annotation to retain `SettingsMigrationRecord`, `SettingsCheckpointRecord`, and `SettingsJournalRecord`; add `MigrationDomainRecord`, `MigrationSnapshotSealRecord`, `MigrationSnapshotRecord`, `MigrationCheckpointRecord`, `LegacyWriteReservationRecord`, `LegacyDeltaRecord`, `MigrationJournalRecord`, and `TransferLeaseRecord`; set `version = 2`; and use an exported additive Room auto-migration from 1 to 2. Keep `FILE_NAME = "migration-transient.db"` and its existing `create(context)` path unchanged. Add `abstract fun migrationDao(): MigrationDao`.

Do not create a second transient database, move settings rows, or rename Phase 1 tables.

- [ ] **Step 7: Run upgrade, DAO, and snapshot-store tests**

Run:

```powershell
.\gradlew.bat :core:storage:kspDebugKotlin :core:storage:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.core.storage.migration.MigrationTransientUpgradeTest,org.mewx.wenku8.core.storage.migration.MigrationDaoTest,org.mewx.wenku8.core.storage.migration.AndroidMigrationSnapshotStoreTest --console=plain --stacktrace --no-parallel
```

Expected: settings rows survive; all generic tables exist; reservation/delta ordering, transition, duplicate, checkpoint, validated snapshot ID, atomic seal-plus-entry insertion, immutable snapshot/recovery, and lease tests pass; and schema `2.json` is exported.

- [ ] **Step 8: Commit transient state and immutable snapshot storage**

Run from `studio-android/LightNovelLibrary`:

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/migration core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/MigrationTransientUpgradeTest.kt core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/MigrationDaoTest.kt core/storage/src/androidTest/java/org/mewx/wenku8/core/storage/migration/AndroidMigrationSnapshotStoreTest.kt core/storage/schemas/org.mewx.wenku8.core.storage.migration.MigrationTransientDatabase
git diff --check --cached
git commit -m "feat(storage): seal durable migration snapshots"
```

Expected: no Phase 1 settings schema row is deleted or rewritten by migration.

### Task 7: Implement The Per-Domain State Machine And Journal Protocol

**Depends on:** Tasks 5 and 6.

**Files:**
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/DomainWriteBarrier.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationParticipant.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/JournaledMutationRunner.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/MigrationCoordinator.kt`
- Test: `core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/MigrationStateMachineTest.kt`
- Test: `core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/JournaledMutationRunnerTest.kt`

- [ ] **Step 1: Write the failing state-transition table test**

Use this exact legal transition set:

```kotlin
val legal = setOf(
    NotStarted to Snapshotting,
    Snapshotting to Importing,
    Importing to DualWrite,
    DualWrite to Reconciling,
    Reconciling to Verified,
    Verified to LegacyReadOnly,
    LegacyReadOnly to Complete,
    Reconciling to DualWrite,
)
```

Assert every other pair is rejected, `Complete` is terminal for state transitions (not for ordinary application writes), and `Verified` requires zero canonical-first journals plus participant equality. `Importing -> DualWrite` additionally requires a valid sealed snapshot, all snapshot entries checkpointed, every incomplete legacy reservation recovered, and every legacy delta through the barrier-locked high-water mutation version replayed into canonical storage in strict order. The phase compare-and-set occurs before releasing that same barrier, so no legacy-first writer can slip between the zero-delta check and `DualWrite`. The `Reconciling -> DualWrite` edge is the only recovery edge and occurs when equality fails after all replayable journals have been processed.

- [ ] **Step 2: Write failing crash-boundary and concurrent-writer tests**

Parameterize legacy-authoritative phases (`NotStarted`, `Snapshotting`, `Importing`) over `AFTER_VERSION_RESERVED`, `AFTER_RESERVATION`, `AFTER_LEGACY_WRITE`, and `AFTER_DELTA_APPEND`; parameterize compatibility-projection phases (`DualWrite`, `Reconciling`, `Verified`) over `AFTER_JOURNAL`, `AFTER_CANONICAL`, `AFTER_PROJECTION`, and `AFTER_ACK`; parameterize canonical-only phases (`LegacyReadOnly`, `Complete`) over `AFTER_VERSION_RESERVED` and `AFTER_CANONICAL`. For each point, construct a fresh runner after the injected process death, call recovery/reconciliation where applicable, and assert:

- the applied-mutation table has exactly one row;
- the canonical record has exactly one version increment;
- the legacy projection equals canonical after reconciliation;
- the early-phase reservation becomes `DELTA_APPENDED`, its typed delta replays after snapshot import without replacing a newer row, and the later-phase journal becomes `PROJECTION_APPLIED`;
- a writer admitted during `Snapshotting` or `Importing` changes live legacy bytes first, never the sealed snapshot; its reserved version is greater than the snapshot floor and is replayed after all snapshot rows;
- an import `insertSnapshotIfAbsent` cannot replace a concurrent/user row and no production import uses `REPLACE`;
- two concurrent writers for the same domain/scope serialize; different scopes may proceed concurrently;
- cancellation propagates and leaves a replayable journal rather than a false success.
- `LegacyReadOnly` and `Complete` commit through the same atomic canonical mutation store but create no legacy reservation, delta, projection, or projection journal and never touch a legacy path.

- [ ] **Step 3: Run both tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.MigrationStateMachineTest" --tests "org.mewx.wenku8.core.storage.migration.JournaledMutationRunnerTest" --console=plain --stacktrace --no-parallel
```

Expected: unresolved state machine, barrier, participant, and runner types.

- [ ] **Step 4: Add the complete barrier and participant boundary**

Create:

```kotlin
package org.mewx.wenku8.core.storage.migration

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mewx.wenku8.core.storage.legacy.LegacyPath

@JvmInline value class MigrationDomainKey(val value: String)
@JvmInline value class MutationId(val value: String)

class DomainWriteBarrier {
    private val locks = ConcurrentHashMap<String, Mutex>()

    suspend fun <T> withWrite(domain: MigrationDomainKey, block: suspend () -> T): T =
        locks.computeIfAbsent(domain.value) { Mutex() }.withLock { block() }
}

data class MutationPayload(
    val domain: MigrationDomainKey,
    val operation: String,
    val payload: ByteArray,
    val payloadSha256: String,
)

data class PendingMutation(
    val id: MutationId,
    val domain: MigrationDomainKey,
    val mutationVersion: Long,
    val payload: MutationPayload,
)

data class LegacyWritePlan(
    val beforeSha256: String?,
    val expectedAfterSha256: String,
    val projectedBytes: List<Pair<LegacyPath, ByteArray>>,
)

data class LegacyWriteResult(val observedAfterSha256: String)

interface MigrationParticipant {
    val domain: MigrationDomainKey
    suspend fun snapshotSources(): List<SnapshotSource>
    suspend fun importChunk(snapshot: SnapshotHandle, limit: Int): ImportChunkResult
    suspend fun prepareLegacyWrite(payload: MutationPayload): LegacyWritePlan
    suspend fun applyPreparedLegacy(plan: LegacyWritePlan): LegacyWriteResult
    suspend fun currentLegacySha256(): String?
    suspend fun applyCanonicalOnce(mutation: PendingMutation): CanonicalCommitResult
    suspend fun canonicalContains(mutationId: MutationId): Boolean
    suspend fun projectLegacy(mutation: PendingMutation): ProjectionResult
    suspend fun acknowledgeProjection(mutation: PendingMutation)
    suspend fun equality(): EqualityResult
    suspend fun restorePendingProjections(limit: Int): List<PendingMutation>
}

data class ImportChunkResult(val processed: Int, val remaining: Boolean)
enum class CanonicalCommitResult { COMMITTED, ALREADY_COMMITTED }
sealed interface ProjectionResult {
    data object Applied : ProjectionResult
    data class RecoverableFailure(val code: String) : ProjectionResult
}
data class EqualityResult(val equal: Boolean, val pendingJournalCount: Int, val mismatchedItemCount: Int)
```

No participant exposes a password/Cookie or logs payload bytes.

- [ ] **Step 5: Add the journal runner with explicit fault points**

Define the supporting types and implement this exact sequence in `JournaledMutationRunner.run`:

```kotlin
package org.mewx.wenku8.core.storage.migration

enum class FaultPoint {
    AFTER_VERSION_RESERVED,
    AFTER_RESERVATION,
    AFTER_LEGACY_WRITE,
    AFTER_DELTA_APPEND,
    AFTER_JOURNAL,
    AFTER_CANONICAL,
    AFTER_PROJECTION,
    AFTER_ACK,
}

fun interface FaultInjector {
    suspend fun reached(point: FaultPoint)
    companion object { val None = FaultInjector { } }
}

sealed interface MutationOutcome {
    data object Synchronized : MutationOutcome
    data object PendingSynchronization : MutationOutcome
    data object LegacyCommittedPendingImport : MutationOutcome
    data object CanonicalOnlyCommitted : MutationOutcome
}

fun interface MutationVersionAllocator {
    suspend fun reserve(domain: MigrationDomainKey): Long
}

interface PhaseAwareMutationStore {
    suspend fun phase(domain: MigrationDomainKey): MigrationPhase
    suspend fun insertLegacyReservation(value: LegacyWriteReservationRecord): Boolean
    suspend fun markLegacyWritten(id: MutationId, observedAfterSha256: String)
    suspend fun appendLegacyDelta(value: LegacyDeltaRecord)
    suspend fun incompleteLegacyReservations(domain: MigrationDomainKey, limit: Int): List<LegacyWriteReservationRecord>
}

interface JournalStore {
    suspend fun insertPending(mutation: PendingMutation)
    suspend fun markCanonicalCommitted(id: MutationId)
    suspend fun markRecoverable(id: MutationId, code: String)
    suspend fun markProjectionApplied(id: MutationId)
    suspend fun pending(domain: MigrationDomainKey, limit: Int): List<PendingMutation>
}

class JournaledMutationRunner(
    private val barrier: DomainWriteBarrier,
    private val versions: MutationVersionAllocator,
    private val phaseStore: PhaseAwareMutationStore,
    private val journal: JournalStore,
    private val participantFor: (MigrationDomainKey) -> MigrationParticipant,
    private val now: () -> Long,
    private val faultInjector: FaultInjector = FaultInjector.None,
) {
    suspend fun run(payload: MutationPayload): MutationOutcome = barrier.withWrite(payload.domain) {
        val participant = participantFor(payload.domain)
        require(participant.domain == payload.domain)
        val version = versions.reserve(payload.domain)
        faultInjector.reached(FaultPoint.AFTER_VERSION_RESERVED)
        val mutation = PendingMutation(
            id = MutationId(stableMutationId(payload.domain, version, payload.payloadSha256)),
            domain = payload.domain,
            mutationVersion = version,
            payload = payload,
        )
        when (phaseStore.phase(payload.domain)) {
            MigrationPhase.NotStarted,
            MigrationPhase.Snapshotting,
            MigrationPhase.Importing -> runLegacyFirst(participant, mutation)
            MigrationPhase.DualWrite,
            MigrationPhase.Reconciling,
            MigrationPhase.Verified -> runCanonicalFirst(participant, mutation)
            MigrationPhase.LegacyReadOnly,
            MigrationPhase.Complete -> runCanonicalOnly(participant, mutation)
        }
    }

    private suspend fun runLegacyFirst(
        participant: MigrationParticipant,
        mutation: PendingMutation,
    ): MutationOutcome {
        val plan = participant.prepareLegacyWrite(mutation.payload)
        val reservation = LegacyWriteReservationRecord(
            mutationId = mutation.id.value,
            domainKey = mutation.domain.value,
            mutationVersion = mutation.mutationVersion,
            payload = mutation.payload.payload,
            payloadSha256 = mutation.payload.payloadSha256,
            legacyBeforeSha256 = plan.beforeSha256,
            expectedLegacyAfterSha256 = plan.expectedAfterSha256,
            observedLegacyAfterSha256 = null,
            status = LegacyReservationStatus.RESERVED.name,
            createdAtEpochMillis = now(),
            updatedAtEpochMillis = now(),
        )
        check(phaseStore.insertLegacyReservation(reservation))
        faultInjector.reached(FaultPoint.AFTER_RESERVATION)
        val written = participant.applyPreparedLegacy(plan)
        check(written.observedAfterSha256 == plan.expectedAfterSha256)
        phaseStore.markLegacyWritten(mutation.id, written.observedAfterSha256)
        faultInjector.reached(FaultPoint.AFTER_LEGACY_WRITE)
        phaseStore.appendLegacyDelta(
            LegacyDeltaRecord(
                domainKey = mutation.domain.value,
                mutationVersion = mutation.mutationVersion,
                mutationId = mutation.id.value,
                payload = mutation.payload.payload,
                payloadSha256 = mutation.payload.payloadSha256,
                legacyAfterSha256 = written.observedAfterSha256,
                status = LegacyDeltaStatus.PENDING_REPLAY.name,
                createdAtEpochMillis = now(),
            )
        )
        faultInjector.reached(FaultPoint.AFTER_DELTA_APPEND)
        return MutationOutcome.LegacyCommittedPendingImport
    }

    private suspend fun runCanonicalFirst(
        participant: MigrationParticipant,
        mutation: PendingMutation,
    ): MutationOutcome {
        journal.insertPending(mutation)
        faultInjector.reached(FaultPoint.AFTER_JOURNAL)
        participant.applyCanonicalOnce(mutation)
        journal.markCanonicalCommitted(mutation.id)
        faultInjector.reached(FaultPoint.AFTER_CANONICAL)
        when (val projection = participant.projectLegacy(mutation)) {
            ProjectionResult.Applied -> Unit
            is ProjectionResult.RecoverableFailure -> {
                journal.markRecoverable(mutation.id, projection.code)
                return MutationOutcome.PendingSynchronization
            }
        }
        faultInjector.reached(FaultPoint.AFTER_PROJECTION)
        participant.acknowledgeProjection(mutation)
        faultInjector.reached(FaultPoint.AFTER_ACK)
        journal.markProjectionApplied(mutation.id)
        return MutationOutcome.Synchronized
    }

    private suspend fun runCanonicalOnly(
        participant: MigrationParticipant,
        mutation: PendingMutation,
    ): MutationOutcome {
        participant.applyCanonicalOnce(mutation)
        faultInjector.reached(FaultPoint.AFTER_CANONICAL)
        return MutationOutcome.CanonicalOnlyCommitted
    }

    private fun stableMutationId(domain: MigrationDomainKey, version: Long, payloadSha256: String): String =
        sha256("${domain.value}\u0000$version\u0000$payloadSha256")
}
```

`sha256` is the shared lowercase digest helper. `MutationVersionAllocator` delegates to Task 2 `DomainVersionDao.reserveMutationVersion`, whose Room `@Transaction` is the only allocator; `LegacyMutationFactory` never supplies an ID/version. `MigrationParticipant.applyCanonicalOnce` delegates to the Task 2 `CanonicalMutationStore`, so payload, applied-mutation row, and domain version cannot split. Add DAO adapters for the phase/reservation/delta/journal interfaces. `CancellationException` is never caught.

Early-phase recovery runs under the same barrier. For each `RESERVED` row it compares live legacy hash with `legacyBeforeSha256` and `expectedLegacyAfterSha256`: a before match deterministically re-applies the prepared bytes, an expected-after match marks `LEGACY_WRITTEN`, and any third hash stops into snapshot recovery without overwriting live user data. A `LEGACY_WRITTEN` row appends its typed delta transactionally. Later-phase `reconcile` retains the prior canonical-first behavior. Neither path allocates a second version for the same reservation.

- [ ] **Step 6: Add state-machine orchestration**

`MigrationCoordinator.advance(domain, maxItems)` performs one legal transition or one bounded import/reconciliation chunk. Before `Snapshotting`, it verifies the writer registry reports no bypass. It then acquires `DomainWriteBarrier`, reserves/reads the canonical mutation-version high-water mark, and calls `MigrationSnapshotStore.createSealed` over `participant.snapshotSources()` before changing `Snapshotting -> Importing`. The barrier is released only after the immutable snapshot seal/entry rows are durable.

Every `Importing` chunk loads the same sealed handle, verifies it, and passes that handle to `participant.importChunk`; participants may read only through `readVerified`. Snapshot records use `insertSnapshotIfAbsent` and `originSnapshotId`; unconditional `REPLACE` is forbidden. After the last snapshot checkpoint, the coordinator reacquires the barrier, recovers incomplete legacy-write reservations, captures the current reserved-version high-water mark, replays every `PENDING_REPLAY` legacy delta in strict mutation-version order through `applyCanonicalOnce` (with projection version equal because legacy already contains the write), and repeats until no delta exists through the captured high-water mark. In that same barrier critical section it proves snapshot checkpoints complete, deltas/reservations complete, and canonical/live legacy equality, then compare-and-sets `Importing -> DualWrite`. Only subsequent writers take the canonical-first path.

`Verified` is written only after `equality.equal`, zero pending canonical-first journals/deltas/reservations, and zero mismatched items. It never advances to `LegacyReadOnly` or `Complete` automatically; those transitions require the Phase 8 compatibility-window approval ID. Once approved, both late phases are canonical-write-only: no legacy projection or journal is created, while normal user writes remain available.

- [ ] **Step 7: Run protocol tests including cancellation**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.*" --console=plain --stacktrace --no-parallel
```

Expected: all legal/illegal transitions, all phase-specific crash points, sealed-snapshot process reconstruction, concurrent early-phase writer/delta replay, no-import-overwrite, atomic canonical mutation, canonical-first reconciliation, canonical-only `LegacyReadOnly`/`Complete` writes, equality, and cancellation tests pass.

- [ ] **Step 8: Commit the protocol**

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/migration core/storage/src/test/java/org/mewx/wenku8/core/storage/migration
git diff --check --cached
git commit -m "feat(storage): add journaled migration protocol"
```

Expected: the diff contains no main-thread blocking bridge and no cross-store atomicity claim.

### Task 8: Implement Remaining Domain Participants

**Depends on:** Tasks 2, 3, 5, and 7.

**Files:**
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/SearchMigrationParticipant.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/BookshelfMigrationParticipant.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/ReaderProgressMigrationParticipant.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/DownloadCatalogMigrationParticipant.kt`
- Create focused tests under `core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/participants/`

- [ ] **Step 1: Write failing per-domain import/projection tests**

For each participant, use a fake live `LegacyFileSystem`, Task 6 `MigrationSnapshotStore`, Task 5 codec bytes, and in-memory Room stores. Assert:

- snapshot hashes/bytes remain stable after the barrier releases and live files change;
- every import chunk/process reads only the sealed `SnapshotHandle`; a test fake throws if import touches live `LegacyFileSystem`;
- writes admitted in `Snapshotting`/`Importing` update live legacy first, append ordered deltas, and replay only after base snapshot import;
- a repeated import with the same item/source hash returns `ALREADY_COMMITTED`, does not increment canonical version, and `insertSnapshotIfAbsent` cannot replace a row carrying a newer user/delta version;
- search order and maximum 20 entries remain stable;
- local bookshelf order and legacy `aid` survive; account/source partitions never enter `bookshelf_local.wk8`;
- both pixel and V1 reader progress import; the V1 record wins when both identify the same novel and has a newer fixture timestamp/revision;
- intro/catalog metadata imports to the cache database while chapter/image files remain authoritative at their existing paths;
- invalid records remain in snapshot diagnostics and original legacy bytes;
- projection writes temp/prior/verified replacement, then acknowledges the exact canonical version;
- equality is record-by-record, not row-count-only.

- [ ] **Step 2: Run participant tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.participants.*" --console=plain --stacktrace --no-parallel
```

Expected: unresolved participant implementations.

- [ ] **Step 3: Implement search and bookshelf participants**

Use domain keys `search-history` and `bookshelf:<sourceId>:<partitionKey>`. Snapshot-import mutation IDs are lowercase SHA-256 of `domainKey + NUL + snapshotId + NUL + itemKey + NUL + sourceSha256`; typed user payloads contain no mutation ID/version. The phase-aware runner reserves the monotonic version transactionally and derives the user mutation ID from domain/version/payload hash. Process recovery completes the durable reservation without allocating a replacement; a later user gesture is a distinct mutation and receives a new version.

Search canonical rows use normalized SHA-256 entry IDs and preserve display text/order. Bookshelf canonical rows preserve `legacyAid`, `serverEntryId`, group, source, account partition, and order; only `partitionKey == "local"` is projected into `bookshelf_local.wk8`.

Snapshot imports call only `insertSnapshotIfAbsent`; live/delta canonical application delegates the one transaction owned by Task 2:

```kotlin
when (canonicalMutationStore.applyOnce(
    CanonicalMutationCommand(
        mutationId = mutation.id.value,
        domainKey = mutation.domain.value,
        mutationVersion = mutation.mutationVersion,
        originSnapshotId = null,
        committedAtEpochMillis = clock.now(),
    ),
    VersionedPayloadTransaction { domainDao.applyVersionedPayload(canonicalRow) },
)) {
    CanonicalMutationResult.COMMITTED -> CanonicalCommitResult.COMMITTED
    CanonicalMutationResult.ALREADY_COMMITTED -> CanonicalCommitResult.ALREADY_COMMITTED
    CanonicalMutationResult.STALE_SNAPSHOT_IGNORED -> error("live mutation cannot be a stale snapshot")
}
```

`applyVersionedPayload` implements typed add/update/delete/clear using the Task 2 exact DAO APIs and refuses an incoming version lower than the existing row. Multi-row clear/reorder payloads execute all guarded row operations inside the same `CanonicalMutationStore` transaction. No participant inserts `AppliedMutationEntity` or advances `CanonicalDomainVersionEntity` directly. An import/resnapshot rollback calls only `deleteSnapshotRows(snapshotId)`/`deleteSnapshotMutations(snapshotId)`, never a domain-wide clear.

- [ ] **Step 4: Implement progress and download/catalog participants**

Use domain keys `reader-progress:<sourceId>` and `downloads-catalog:<sourceId>`. Progress projection regenerates both `read_saves.wk8` and `read_saves_v1.wk8` from complete canonical records where legacy IDs exist; it never drops a canonical-only record to claim equality. Download/catalog import records existing intro/catalog/chapter/image paths and hashes but does not rewrite durable chapter XML or image bytes in this task.

Every canonical user row receives the runner-reserved mutation version and retains its previous `legacyProjectionVersion` until verified projection. A replayed early-phase delta sets both versions to its already-applied legacy version. Catalog-cache rows are rebuildable, use snapshot-origin insert-if-absent/delete APIs, and never become the sole evidence needed to project user bookshelf/progress/settings.

- [ ] **Step 5: Run the full participant matrix twice**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.participants.*" --console=plain --stacktrace --no-parallel
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.participants.*" --console=plain --stacktrace --no-parallel
```

Expected: both runs pass with identical canonical/projection hashes and zero additional applied-mutation rows on the second run.

- [ ] **Step 6: Commit domain participants**

```powershell
git add core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/participants
git diff --check --cached
git commit -m "feat(storage): migrate legacy library domains"
```

Expected: no SessionStore or settings DataStore reimplementation.

### Task 9: Integrate Phase 1 Settings, Phase 2 Session, Intent, And Compatibility Adapters

**Depends on:** Task 7 and completed Phase 1/2 implementations.

**Files:**
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/migration/participants/SettingsMigrationParticipant.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/SessionMigrationParticipant.kt`
- Create: `app/src/main/java/org/mewx/wenku8/compat/LegacyCompatibilityRegistry.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/di/DefaultAppContainer.kt`
- Test: `app/src/test/java/org/mewx/wenku8/compat/LegacyCompatibilityRegistryTest.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/migration/participants/SettingsParticipantIntegrationTest.kt`
- Test: `core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/participants/SessionParticipantIntegrationTest.kt`

- [ ] **Step 1: Write failing ownership and identity tests**

Assert there is exactly one `app_settings.preferences_pb`, one `migration-transient.db`, and one Phase 2 `EncryptedSessionStore`; the generic coordinator registers six domain families but delegates settings to `SettingsMigrationCoordinator`/`SettingsReconciler` and credentials to `LegacyCredentialAdapter`/Phase 2 credential reconciliation. Assert `LegacyIntentCodec` remains the only raw Intent-extra decoder and the registry exposes path, settings, progress, bookshelf, and credential adapters.

- [ ] **Step 2: Run tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.migration.participants.SettingsParticipantIntegrationTest" :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.participants.SessionParticipantIntegrationTest" :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.compat.LegacyCompatibilityRegistryTest" --console=plain --stacktrace --no-parallel
```

Expected: missing participant wrappers and compatibility registry bindings.

- [ ] **Step 3: Add delegate-only participant wrappers**

`SettingsMigrationParticipant` lives in `:core:data`, adapts the existing Phase 1 `SettingsMigrationCoordinator`/`SettingsReconciler` to the storage-owned `MigrationParticipant` interface, and does not decode settings again. `SessionMigrationParticipant` consumes `core/session-contract/src/main/kotlin/org/mewx/wenku8/core/session/SessionStore.kt` and the Phase 2 `core/storage/src/main/java/org/mewx/wenku8/core/storage/session/migration/LegacyCredentialAdapter.kt`; it delegates the reviewed `inspect()`, `rejectLegacyWrite()`, and `scrub(mutationId)` calls and preserves the checkpoint sequence `REAUTHENTICATION_REQUIRED -> SESSION_COMMITTED -> LEGACY_SCRUBBED -> COMPLETE`. It exposes only non-secret phase/checkpoint status. Its snapshot payload contains file presence/hash and scrub state, never legacy credential bytes. On missing/invalid Keystore or restore, it calls `SessionStore.purge(providerId)` and returns signed out.

- [ ] **Step 4: Add the complete compatibility registry shape**

Create:

```kotlin
package org.mewx.wenku8.compat

import org.mewx.wenku8.core.storage.legacy.LegacyPathPolicy
import org.mewx.wenku8.core.storage.migration.MigrationParticipant
import org.mewx.wenku8.core.storage.settings.LegacySettingsAdapter
import org.mewx.wenku8.core.storage.session.migration.LegacyCredentialAdapter

data class LegacyCompatibilityRegistry(
    val intentCodec: LegacyIntentCodec,
    val pathPolicy: LegacyPathPolicy,
    val settingsAdapter: LegacySettingsAdapter,
    val credentialAdapter: LegacyCredentialAdapter,
    val participants: Map<String, MigrationParticipant>,
) {
    init {
        require(participants.keys == participants.values.map { it.domain.value }.toSet())
        require(participants.keys.any { it == "settings" })
        require(participants.keys.any { it == "session-credentials" })
        require(participants.keys.any { it == "search-history" })
        require(participants.keys.any { it.startsWith("bookshelf:") })
        require(participants.keys.any { it.startsWith("reader-progress:") })
        require(participants.keys.any { it.startsWith("downloads-catalog:") })
    }
}
```

Phase 0 owns `app/src/main/java/org/mewx/wenku8/compat/LegacyIntentCodec.kt`; the registry is in the same package and therefore uses that exact type without a second import or decoder.

- [ ] **Step 5: Wire existing instances in DefaultAppContainer**

Construct one `StorageDatabaseFactory`, one existing settings DataStore, one existing encrypted SessionStore, and one transient database. Register participant instances over those same objects. Do not call `Room.databaseBuilder` or `DataStoreFactory.create` anywhere else in app composition.

- [ ] **Step 6: Run ownership, signed-out, and registry tests**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.migration.participants.SettingsParticipantIntegrationTest" :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.participants.SessionParticipantIntegrationTest" :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.compat.LegacyCompatibilityRegistryTest" --console=plain --stacktrace --no-parallel
```

Expected: tests pass; a source scan reports one settings DataStore factory, one encrypted SessionStore binding, and one transient database factory.

- [ ] **Step 7: Commit existing-component integration**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/migration/participants/SettingsMigrationParticipant.kt core/data/src/test/java/org/mewx/wenku8/core/data/migration/participants/SettingsParticipantIntegrationTest.kt core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/participants/SessionMigrationParticipant.kt core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/participants/SessionParticipantIntegrationTest.kt app/src/main/java/org/mewx/wenku8/compat/LegacyCompatibilityRegistry.kt app/src/main/java/org/mewx/wenku8/di/DefaultAppContainer.kt app/src/test/java/org/mewx/wenku8/compat/LegacyCompatibilityRegistryTest.kt
git diff --check --cached
git commit -m "feat(storage): integrate settings and session migration"
```

Expected: no password literal, no credential payload, and no duplicate store.

### Task 10: Intercept Every Legacy Writer And Preserve Rollback Projections

**Depends on:** Tasks 8 and 9.

**Files:**
- Create: `app/src/main/java/org/mewx/wenku8/compat/storage/LegacyStorageGateway.kt`
- Create: `docs/verification/phase-3-writer-routing.yaml`
- Create: `verification-tools/src/main/kotlin/org/mewx/wenku8/verification/phase3/Phase3WriterRoutingVerifier.kt`
- Create: `verification-tools/src/test/kotlin/org/mewx/wenku8/verification/phase3/Phase3WriterRoutingVerifierTest.kt`
- Modify: `verification-tools/build.gradle`
- Create: `tools/verify-phase3-writers.ps1`
- Modify: every Phase 0 writer-ledger target in `app/src/main/java/`
- Test: `app/src/test/java/org/mewx/wenku8/compat/storage/LegacyStorageGatewayTest.kt`

- [ ] **Step 1: Add the failing static writer verifier**

Write `Phase3WriterRoutingVerifierTest` first. Use the verification module's pinned SnakeYAML parser and the Phase 0 Kotlin/Java lexical scanner; never parse YAML or source with line-oriented regular expressions. Fixtures must prove all of these failures independently:

- one Phase 0 artifact writer ID has no routing row;
- two routing rows claim the same writer ID;
- a routing row invents an ID absent from the Phase 0 manifest;
- a row's `source_path`/`source_symbol` differs from the Phase 0 writer ledger;
- a legacy writer body remains, including any `GlobalConfig` writer body;
- a call site invokes `LightCache.saveFile/deleteFile` or a `GlobalConfig` write/add/remove/move/set/save helper instead of its declared typed route;
- a chapter XML, chapter image, novel cover, or other approved `saves/imgs`/cover-root writer lacks a gateway/factory operation;
- migration code performs a non-secret legacy delete/general clear/destructive Room fallback;
- a route points to an unapproved projection owner or allows `cert.wk8` into Phase 3.

The positive fixture has exact set equality between every writer ID recursively parsed from `docs/verification/artifact-manifest.yaml` and every `writer_id` in `phase-3-writer-routing.yaml`. It also proves one and only one route per writer ID and one current source call site per route. No artifact family, path, owner, or `GlobalConfig` file exemption is allowed.

- [ ] **Step 2: Add the structured routing manifest and verifier**

Create `phase-3-writer-routing.yaml` with this closed schema and no optional wildcard rows:

```yaml
schema: wenku8-phase3-writer-routing/v1
routes:
  - writer_id: exact-id-from-phase-0-artifact-manifest
    source_path: exact/repository/relative/source/path.kt
    source_symbol: exact.qualified.Type.method
    route_kind: LEGACY_STORAGE_GATEWAY
    gateway_operation: addSearch
    projection_owner: org.mewx.wenku8.core.storage.migration.participants.SearchMigrationParticipant
    physical_artifact_id: exact-phase-0-artifact-id
```

Populate one concrete row for every Phase 0 writer ID; the example values above describe schema, not a row that may remain in the checked-in file. `route_kind` is one of `LEGACY_STORAGE_GATEWAY`, `PHASE1_SETTINGS_GATEWAY`, or the exact Phase 2 password-scrub policy route. The latter can appear only for the already-audited `cert.wk8` writer ID. All other rows identify a typed operation and exact projection owner. Chapter XML, intro/catalog metadata, chapter images, novel covers, cache invalidation, search, bookshelf, both progress formats, and every approved cover/image root receive separate operations when their physical policies differ.

`Phase3WriterRoutingVerifier` parses both YAML documents into typed data classes, validates their schemas and exact writer-ID set equality, then lexically indexes production Kotlin/Java call expressions. An old writer method must be deleted or its entire executable body must be one direct typed-gateway delegation; a retained read helper does not permit a writer body in the same file. Exact projection implementation paths come from routing rows and are validated against a closed package allowlist. Snapshot cleanup may call only `deleteSnapshotRows(snapshotId)`, `deleteSnapshotMutations(snapshotId)`, `clearSnapshotCheckpoints(snapshotId)`, `deleteSnapshotEntries(snapshotId)`, `deleteSnapshotSeal(snapshotId)`, and excluded snapshot-directory cleanup. User mutations may call only the version-guarded Task 2 delete/clear/acknowledgement APIs. `clearAllTables`, destructive migration fallback, and deletion of a non-secret legacy artifact always fail.

Register `:verification-tools:verifyPhase3WriterRouting` as a `JavaExec` entry point. The PowerShell file is a thin fail-fast wrapper; it contains no YAML parser:

```powershell
# tools/verify-phase3-writers.ps1
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$android = Join-Path $repo 'studio-android\LightNovelLibrary'
Push-Location $android
try {
    & .\gradlew.bat :verification-tools:verifyPhase3WriterRouting -Pwenku8Provider=public --console=plain --stacktrace --no-parallel
    if ($LASTEXITCODE -ne 0) { throw 'PHASE3-WRITER: structured verifier failed' }
} finally {
    Pop-Location
}
Write-Host 'PHASE3-WRITER-ROUTING-PASS'
```

- [ ] **Step 3: Run the verifier and observe the intended RED inventory**

Run from the repository root:

```powershell
& .\tools\verify-phase3-writers.ps1
```

Expected: structured failures naming every currently unrouted `GlobalConfig`, search, bookshelf, reader progress, settings, intro/catalog, chapter XML, chapter-image, novel-cover, and approved image/cover-root writer ID. Save the exact list in the Task 10 review record; do not add an exemption.

- [ ] **Step 4: Add the suspend-only gateway**

Create:

```kotlin
package org.mewx.wenku8.compat.storage

import org.mewx.wenku8.core.storage.migration.JournaledMutationRunner
import org.mewx.wenku8.core.storage.migration.MutationPayload
import org.mewx.wenku8.core.storage.migration.MutationOutcome

class LegacyStorageGateway(
    private val runner: JournaledMutationRunner,
    private val mutations: LegacyMutationFactory,
) {
    suspend fun addSearch(query: String): MutationOutcome = runner.run(mutations.addSearch(query))
    suspend fun removeSearch(query: String): MutationOutcome = runner.run(mutations.removeSearch(query))
    suspend fun clearSearch(): MutationOutcome = runner.run(mutations.clearSearch())
    suspend fun addLocalBookshelf(aid: Int): MutationOutcome = runner.run(mutations.addLocalBookshelf(aid))
    suspend fun removeLocalBookshelf(aid: Int): MutationOutcome = runner.run(mutations.removeLocalBookshelf(aid))
    suspend fun moveLocalBookshelfFirst(aid: Int): MutationOutcome = runner.run(mutations.moveLocalBookshelfFirst(aid))
    suspend fun savePixelProgress(cid: Int, position: Int, height: Int): MutationOutcome =
        runner.run(mutations.savePixelProgress(cid, position, height))
    suspend fun saveReaderProgress(aid: Int, volumeId: Int, cid: Int, lineId: Int, wordId: Int): MutationOutcome =
        runner.run(mutations.saveReaderProgress(aid, volumeId, cid, lineId, wordId))
    suspend fun clearReaderProgress(aid: Int): MutationOutcome = runner.run(mutations.clearReaderProgress(aid))
    suspend fun writeCatalogArtifact(relativePath: String, bytes: ByteArray): MutationOutcome =
        runner.run(mutations.writeCatalogArtifact(relativePath, bytes))
    suspend fun writeChapterArtifact(relativePath: String, bytes: ByteArray): MutationOutcome =
        runner.run(mutations.writeChapterArtifact(relativePath, bytes))
    suspend fun writeChapterImage(relativePath: String, bytes: ByteArray): MutationOutcome =
        runner.run(mutations.writeChapterImage(relativePath, bytes))
    suspend fun writeNovelCover(relativePath: String, bytes: ByteArray): MutationOutcome =
        runner.run(mutations.writeNovelCover(relativePath, bytes))
}

interface LegacyMutationFactory {
    fun addSearch(query: String): MutationPayload
    fun removeSearch(query: String): MutationPayload
    fun clearSearch(): MutationPayload
    fun addLocalBookshelf(aid: Int): MutationPayload
    fun removeLocalBookshelf(aid: Int): MutationPayload
    fun moveLocalBookshelfFirst(aid: Int): MutationPayload
    fun savePixelProgress(cid: Int, position: Int, height: Int): MutationPayload
    fun saveReaderProgress(aid: Int, volumeId: Int, cid: Int, lineId: Int, wordId: Int): MutationPayload
    fun clearReaderProgress(aid: Int): MutationPayload
    fun writeCatalogArtifact(relativePath: String, bytes: ByteArray): MutationPayload
    fun writeChapterArtifact(relativePath: String, bytes: ByteArray): MutationPayload
    fun writeChapterImage(relativePath: String, bytes: ByteArray): MutationPayload
    fun writeNovelCover(relativePath: String, bytes: ByteArray): MutationPayload
}
```

The factory validates IDs/ranges, bounds artifact size, and asks the Task 5 manifest-driven `LegacyPathPolicy` to canonicalize the relative path under the operation-specific `saves/intro`, `saves/novel`, `saves/imgs`, or approved cover root. It returns only a `MutationPayload`; the Task 7 runner transactionally reserves the monotonic version and derives the mutation ID. The factory never allocates either field, accepts an arbitrary root, or accepts `cert.wk8`.

- [ ] **Step 5: Replace every writer-ledger call site with structured coroutine calls**

Activities/Fragments use `lifecycleScope.launch`; Modern Reader uses its existing injected scope/store; background loaders call the suspend gateway from their owning structured scope. No call site uses `runBlocking`, `GlobalScope`, raw `Thread`, `Executor`, or fire-and-forget application scope. Each visible command handles `PendingSynchronization` with retained local success plus a visible synchronization state, not a Toast-only blank result. Phase 1 settings writer IDs route directly to its existing typed settings write gateway/barrier, not through a new settings store; the routing verifier treats those exact operations as first-class routes. The Phase 2 password-only scrub remains its exact policy route and cannot be called by general UI/storage code.

Keep `GlobalConfig` legacy read compatibility during this phase, but remove its writer bodies. A temporarily retained binary/source-compatible writer symbol must be a one-line direct delegation to the typed gateway and carry its exact writer-ledger ID; it cannot remain an allowed filesystem/projection implementation. Physical writes live only in the exact projection owners declared by the routing manifest.

- [ ] **Step 6: Add gateway behavior and cancellation tests**

Test every method delegates once with the expected domain/payload, including chapter XML, chapter images, novel covers, and each approved image/cover root. Prove factories return no ID/version, the runner allocates distinct monotonic versions/IDs for distinct UI gestures, same-domain calls serialize behind the barrier, and cancellation propagates. Test that a post-canonical projection failure returns `PendingSynchronization` and that a later gateway read observes canonical state.

- [ ] **Step 7: Run writer verifier and affected app tests**

Run:

```powershell
& .\tools\verify-phase3-writers.ps1
Set-Location 'studio-android\LightNovelLibrary'
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.compat.storage.LegacyStorageGatewayTest" :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.global.GlobalConfigContractTest" --console=plain --stacktrace --no-parallel
```

Expected: `PHASE3-WRITER-ROUTING-PASS`, both app tests pass, and the verifier reports exact one-to-one coverage of all Phase 0 artifact-manifest writer IDs with no `GlobalConfig` exemption.

- [ ] **Step 8: Commit writer interception**

Run from the repository root:

```powershell
git add docs/verification/phase-3-writer-routing.yaml tools/verify-phase3-writers.ps1 studio-android/LightNovelLibrary/verification-tools/build.gradle studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/phase3/Phase3WriterRoutingVerifier.kt studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/phase3/Phase3WriterRoutingVerifierTest.kt studio-android/LightNovelLibrary/app/src/main/java studio-android/LightNovelLibrary/app/src/test/java/org/mewx/wenku8/compat/storage
git diff --check --cached
git commit -m "refactor(storage): intercept legacy writers"
```

Expected: no unrelated UI redesign and no legacy file deletion caused by migration.

### Task 11: Run Migration And Reconciliation In Bounded WorkManager Chunks

**Depends on:** Tasks 8-10.

**Files:**
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/work/ChunkBudget.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/work/MigrationChunkWorker.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/work/ReconciliationChunkWorker.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/work/MigrationWorkScheduler.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/work/ChunkBudgetTest.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/work/MigrationWorkersTest.kt`

- [ ] **Step 1: Write failing time/item-budget and worker-resume tests**

Use an injected monotonic clock. Assert a chunk stops at 100 committed items or 7 minutes 30 seconds, whichever occurs first; checkpoints after each item; returns success only on terminal domain state; schedules retry with retained stable work key/revision when more remains; maps cancellation to persisted `CANCELLED_BY_USER` only when the canonical cancellation flag is set; propagates framework cancellation otherwise. Recreate the worker with the same in-memory databases and assert it resumes item 51 without repeating items 1-50.

- [ ] **Step 2: Run the focused tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.work.*" --console=plain --stacktrace --no-parallel
```

Expected: unresolved chunk budget/workers/scheduler.

- [ ] **Step 3: Add the complete chunk budget**

Create:

```kotlin
package org.mewx.wenku8.core.data.work

fun interface MonotonicClock { fun elapsedRealtimeMillis(): Long }

class ChunkBudget(
    private val clock: MonotonicClock,
    private val maxItems: Int = 100,
    private val maxDurationMillis: Long = 450_000L,
) {
    private val started = clock.elapsedRealtimeMillis()
    private var committed = 0

    fun recordCommittedItem() { committed += 1 }
    fun canStartAnother(): Boolean = committed < maxItems && clock.elapsedRealtimeMillis() - started < maxDurationMillis
    fun committedItems(): Int = committed
}
```

No production worker raises either bound without a new measured worst-case report and spec review.

- [ ] **Step 4: Add worker input and result contracts**

WorkManager input contains only `domainKey`, `inputRevision`, and opaque `workKey`; each value has a 256-character bound. Define `MigrationWorkDependencies` with coordinator, clock, cancellation store, and scheduler interfaces. `MigrationChunkWorker.doWork()` loops while the budget permits, calls `coordinator.advance(domain, 1)`, and observes checkpoint persistence before `recordCommittedItem`. If work remains, it returns `Result.retry()` with exponential backoff configured by the scheduler; typed permanent corruption returns `Result.failure` with only an enum code. `ReconciliationChunkWorker` follows the same bounds over ordered pending journal rows.

- [ ] **Step 5: Add stable unique-work scheduling**

Use unique names `migration:<domainKey>:<inputRevision>` and `reconcile:<domainKey>:<inputRevision>` with `ExistingWorkPolicy.KEEP`. Apply `Constraints.Builder().setRequiresStorageNotLow(true)`; add network only for a chunk that explicitly reads remote bytes. Backoff is exponential starting at 30 seconds and capped by WorkManager. Automatic migration is never expedited, foreground, or UIDT.

- [ ] **Step 6: Run bounded/resume/cancellation tests**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.work.*" --console=plain --stacktrace --no-parallel
```

Expected: item/time limits, per-item checkpoint, process reconstruction, stable unique work, retry/backoff, and cancellation cases pass.

- [ ] **Step 7: Commit bounded migration work**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/work core/data/src/test/java/org/mewx/wenku8/core/data/work
git diff --check --cached
git commit -m "feat(data): run migration in bounded chunks"
```

Expected: no worker carries a secret or content body in Data/progress/tags.

### Task 12: Enforce Whole-Store Backup Boundaries And Restore Reconciliation

**Depends on:** Tasks 4 and 8-10.

**Files:**
- Modify: `app/src/main/res/xml/data_extraction_rules.xml`
- Modify: `app/src/main/res/xml/backup_rules.xml`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/RestoreReconciler.kt`
- Create: `core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/RestoreReconcilerTest.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/backup/Phase3BackupBoundaryTest.kt`

- [ ] **Step 1: Write failing XML/path and journal-free restore tests**

Parse both XML resources as XML, not raw substring checks. Assert whole-file inclusion of the complete `wenku8-user.db` SQLite family (main, WAL, SHM, and rollback journal when present) and `files/datastore/app_settings.preferences_pb`; assert whole-file exclusion of catalog cache, migration transient DB plus sidecars, encrypted session files, Phase 2 credential migration directory, `cert.wk8`, Cookies, and parser/authenticated caches. Resolve `AndroidMigrationSnapshotStore` and prove its complete `noBackupFilesDir/wenku8-migration/snapshots/` tree is outside every included backup domain on both rule generations. Assert no row/table syntax exists.

Create canonical settings/progress/bookshelf fixtures where `mutationVersion=9` and `legacyProjectionVersion=8`, omit the transient DB entirely, restore a stale/missing legacy file, invoke `RestoreReconciler`, and assert deterministic projection plus version `9/9`. Repeat with a restored legacy `.migration_completed` sentinel and generic state absent; the result must be identical.

- [ ] **Step 2: Run tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.RestoreReconcilerTest" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.backup.Phase3BackupBoundaryTest --console=plain --stacktrace --no-parallel
```

Expected: missing RestoreReconciler and missing/incomplete physical backup rules.

- [ ] **Step 3: Replace data-extraction rules with complete physical rules**

Use this structure, preserving any stricter Phase 1/2 exclusions:

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup disableIfNoEncryptionCapabilities="true">
        <include domain="database" path="wenku8-user.db" />
        <include domain="database" path="wenku8-user.db-wal" />
        <include domain="database" path="wenku8-user.db-shm" />
        <include domain="database" path="wenku8-user.db-journal" />
        <include domain="file" path="datastore/app_settings.preferences_pb" />
        <include domain="file" path="custom/" />
        <exclude domain="database" path="wenku8-catalog-cache.db" />
        <exclude domain="database" path="migration-transient.db" />
        <exclude domain="database" path="migration-transient.db-wal" />
        <exclude domain="database" path="migration-transient.db-shm" />
        <exclude domain="database" path="migration-transient.db-journal" />
        <exclude domain="file" path="wenku8-session/" />
        <exclude domain="file" path="wenku8-migration/credential/" />
        <exclude domain="file" path="saves/cert.wk8" />
        <exclude domain="file" path="cache/" />
        <exclude domain="root" path="saves/cert.wk8" />
        <exclude domain="external" path="saves/cert.wk8" />
    </cloud-backup>
    <device-transfer>
        <include domain="database" path="wenku8-user.db" />
        <include domain="database" path="wenku8-user.db-wal" />
        <include domain="database" path="wenku8-user.db-shm" />
        <include domain="database" path="wenku8-user.db-journal" />
        <include domain="file" path="datastore/app_settings.preferences_pb" />
        <include domain="file" path="custom/" />
        <exclude domain="database" path="wenku8-catalog-cache.db" />
        <exclude domain="database" path="migration-transient.db" />
        <exclude domain="database" path="migration-transient.db-wal" />
        <exclude domain="database" path="migration-transient.db-shm" />
        <exclude domain="database" path="migration-transient.db-journal" />
        <exclude domain="file" path="wenku8-session/" />
        <exclude domain="file" path="wenku8-migration/credential/" />
        <exclude domain="file" path="saves/cert.wk8" />
        <exclude domain="file" path="cache/" />
        <exclude domain="root" path="saves/cert.wk8" />
        <exclude domain="external" path="saves/cert.wk8" />
    </device-transfer>
</data-extraction-rules>
```

Because catalog cache and the immutable migration snapshot root live under `noBackupFilesDir`, XML exclusions are defense-in-depth; the device test must create marker files in both actual roots and prove neither appears in the transport file set or restored process.

- [ ] **Step 4: Replace legacy full-backup rules with equivalent complete boundaries**

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="database" path="wenku8-user.db" requireFlags="clientSideEncryption|deviceToDeviceTransfer" />
    <include domain="database" path="wenku8-user.db-wal" requireFlags="clientSideEncryption|deviceToDeviceTransfer" />
    <include domain="database" path="wenku8-user.db-shm" requireFlags="clientSideEncryption|deviceToDeviceTransfer" />
    <include domain="database" path="wenku8-user.db-journal" requireFlags="clientSideEncryption|deviceToDeviceTransfer" />
    <include domain="file" path="datastore/app_settings.preferences_pb" requireFlags="clientSideEncryption|deviceToDeviceTransfer" />
    <include domain="file" path="custom/" requireFlags="clientSideEncryption|deviceToDeviceTransfer" />
    <exclude domain="database" path="wenku8-catalog-cache.db" />
    <exclude domain="database" path="migration-transient.db" />
    <exclude domain="database" path="migration-transient.db-wal" />
    <exclude domain="database" path="migration-transient.db-shm" />
    <exclude domain="database" path="migration-transient.db-journal" />
    <exclude domain="file" path="wenku8-session/" />
    <exclude domain="file" path="wenku8-migration/credential/" />
    <exclude domain="file" path="saves/cert.wk8" />
    <exclude domain="file" path="cache/" />
    <exclude domain="root" path="saves/cert.wk8" />
    <exclude domain="external" path="saves/cert.wk8" />
</full-backup-content>
```

- [ ] **Step 5: Add journal-independent restore reconciliation**

`RestoreReconciler.run(limit)` loads every participant's `restorePendingProjections(limit)` from backed-up canonical versions. It does not read or trust generic `Verified`, `LegacyReadOnly`, `Complete`, `.migration_completed`, or a journal row. For each pending canonical record it deterministically regenerates the legacy projection, then acknowledges that exact version. It also invokes the existing settings restore reconciler and Phase 2 signed-out/session purge path. It returns only bounded enum counts for local diagnostics.

- [ ] **Step 6: Run the post-canonical/pre-projection restore tests**

Run:

```powershell
.\gradlew.bat :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.migration.RestoreReconcilerTest" :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.backup.Phase3BackupBoundaryTest --console=plain --stacktrace --no-parallel
```

Expected: physical boundary assertions, excluded immutable-snapshot marker assertions, and all fresh/partial/old-schema/sentinel/post-canonical restore cases pass; restored SessionStore is signed out.

- [ ] **Step 7: Commit backup and restore behavior**

```powershell
git add app/src/main/res/xml/data_extraction_rules.xml app/src/main/res/xml/backup_rules.xml app/src/androidTest/java/org/mewx/wenku8/backup/Phase3BackupBoundaryTest.kt core/storage/src/main/java/org/mewx/wenku8/core/storage/migration/RestoreReconciler.kt core/storage/src/test/java/org/mewx/wenku8/core/storage/migration/RestoreReconcilerTest.kt
git diff --check --cached
git commit -m "feat(storage): reconcile whole-store restores"
```

Expected: no selected-row backup rule and no session/credential backup.

### Task 13: Implement Cancellable Safe GET/Range And Atomic Partial Files

**Depends on:** Tasks 2 and 3 plus the Phase 2 audited HTTP/HostPolicy.

**Files:**
- Create: `core/network/src/main/kotlin/org/mewx/wenku8/core/network/transfer/RangeTransferClient.kt`
- Create: `core/storage/src/main/java/org/mewx/wenku8/core/storage/transfer/PartialFileStore.kt`
- Test: `core/network/src/test/kotlin/org/mewx/wenku8/core/network/transfer/RangeTransferClientTest.kt`
- Test: `core/storage/src/test/java/org/mewx/wenku8/core/storage/transfer/PartialFileStoreTest.kt`

- [ ] **Step 1: Write failing MockWebServer and filesystem tests**

Cover: initial GET with a strong quoted ETag; initial 200 with neither ETag nor Content-Length that reaches EOF and commits as one non-resumable representation; an interrupted unknown-length 200 that discards its partial and restarts from zero; rejection of `W/` weak ETag; refusal to send Range when validator or stored total length is absent; valid `Range: bytes=4-` plus the exact strong `If-Range`; mismatched Content-Range start/end/total; mismatched Content-Length; changed/missing ETag; server returns 200 to a Range request; exact 416 completion; 416 with changed ETag/length; HTTPS-to-HTTP/unknown-host rejection inherited from HostPolicy; cancellation calls `Call.cancel`; partial metadata fsync/checkpoint; hash mismatch retains known-good final; successful hash atomically replaces final while retaining/restoring prior on injected rename failure. Add a two-representation server fixture and prove mixed bytes can never commit.

- [ ] **Step 2: Run tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:network:test --tests "org.mewx.wenku8.core.network.transfer.RangeTransferClientTest" :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.transfer.PartialFileStoreTest" --console=plain --stacktrace --no-parallel
```

Expected: missing RangeTransferClient and PartialFileStore.

- [ ] **Step 3: Add the complete transfer response contract**

```kotlin
package org.mewx.wenku8.core.network.transfer

data class RangeRequest(
    val canonicalHttpsUrl: String,
    val offset: Long,
    val expectedStrongEtag: String?,
    val expectedRepresentationLength: Long?,
)

sealed interface RangeDisposition {
    data class Stream(
        val start: Long,
        val endInclusive: Long?,
        val total: Long?,
        val strongEtag: String?,
        val resumed: Boolean,
    ) : RangeDisposition
    data class RestartWholeItem(val reasonCode: String) : RangeDisposition
    data class AlreadyComplete(val total: Long, val strongEtag: String) : RangeDisposition
}

class RangeProtocolException(val rule: String) : Exception(rule)
```

`RangeTransferClient.consume(request, block)` validates the URL through Phase 2 `HostPolicy`, awaits OkHttp with `suspendCancellableCoroutine`, registers `invokeOnCancellation { call.cancel() }`, validates status/headers, and executes `block(disposition, responseBody.byteStream())` inside `response.use`. It never returns a response body/stream beyond that scope. A positive offset is invalid unless both `expectedStrongEtag` and `expectedRepresentationLength` are present, the ETag is a quoted strong validator, and the offset is no greater than that length; only then may it emit `Range` and exact `If-Range` headers.

- [ ] **Step 4: Implement exact Range validation**

- Offset zero accepts only 200. A present ETag must be a quoted strong ETag; `W/` is rejected. The response records exact nonnegative `Content-Length` when supplied. If absent, `Stream.total` is `null`; it is a non-resumable whole stream, no intermediate resumable checkpoint is published, and its representation length becomes the counted byte length only after a clean response-body EOF.
- Positive offset accepts 206 only when the response repeats the exact strong ETag, `Content-Range` is `bytes <offset>-<end>/<total>` with no wildcard total, total exactly equals the stored representation length, end is at least start and below total, and `Content-Length` exactly equals `end - start + 1`.
- Positive offset receiving 200, a missing/changed validator, weak validator, or changed/unknown total returns `RestartWholeItem` before exposing any body byte. The caller deletes only the incomplete partial and metadata, then starts a new whole GET in a later bounded chunk.
- 416 returns `AlreadyComplete` only when the exact strong ETag matches, `Content-Range` is `bytes */<total>`, total exactly equals the stored representation length, and durable partial length equals total. Every other 416 throws `RangeProtocolException("range-not-satisfiable-mismatch")` and cannot commit.
- Any other status maps through the Phase 2 typed HTTP failure. Cancellation is rethrown.

- [ ] **Step 5: Add atomic partial-file behavior**

`PartialFileStore` owns generation-scoped `<final>.partial.<generation>`, `<final>.partial.<generation>.json`, `<final>.prior`, and final. Its typed `PartialMetadata(schema, locatorSha256, ownerId, leaseGeneration, strongEtag, representationLength, partialLength, prefixSha256, completeEof)` is written by temp/fsync/atomic-replace after the partial bytes are fsynced. `strongEtag` and `representationLength` may both be null only for an offset-zero, non-resumable whole GET; such a partial is never appended and is discarded on interruption/retry. After a clean EOF, the store fsyncs bytes, records the counted length as `representationLength`, sets `completeEof=true`, and may commit that one unmixed representation even when the server omitted Content-Length. `append` opens in append mode only after metadata, actual length, exact locator hash, owner/generation, validator/total, and client-proved offset agree; it copies in 64 KiB blocks and persists a prefix hash/length checkpoint. `restart` deletes only that generation's incomplete partial/metadata, never final/prior. `commit` requires `completeEof`, computed full hash, exact counted/declared length when declared, and either the supplied expected hash or the computed hash of the single complete representation. Promotion also requires the Task 14 generation-fence critical section. It moves final to prior, atomically moves partial to final, re-verifies final length/hash, then removes prior; any failure restores prior. All paths are resolved beneath the approved durable content root and reject traversal.

- [ ] **Step 6: Run safe-at-least-once network/file tests**

Run:

```powershell
.\gradlew.bat :core:network:test --tests "org.mewx.wenku8.core.network.transfer.RangeTransferClientTest" :core:storage:testDebugUnitTest --tests "org.mewx.wenku8.core.storage.transfer.PartialFileStoreTest" --console=plain --stacktrace --no-parallel
```

Expected: all strong-validator, unknown-length initial-200 EOF/interruption, no-validator whole restart, exact 206/416, mixed-representation rejection, cancel/hash/atomic-recovery cases pass and MockWebServer records only GET requests.

- [ ] **Step 7: Commit safe transfer primitives**

```powershell
git add core/network/src/main/kotlin/org/mewx/wenku8/core/network/transfer core/network/src/test/kotlin/org/mewx/wenku8/core/network/transfer core/storage/src/main/java/org/mewx/wenku8/core/storage/transfer core/storage/src/test/java/org/mewx/wenku8/core/storage/transfer
git diff --check --cached
git commit -m "feat(storage): add safe resumable transfer primitives"
```

Expected: no POST/account/community mutation enters the transfer client.

### Task 14: Add The Leased Transfer Chunk Runner And Exactly-One Canonical Terminal Commit

**Depends on:** Tasks 6 and 13.

**Files:**
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/DurableTransferLocator.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/TransferModels.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/TransferChunkRunner.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/TransferLeaseCoordinator.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/transfer/DurableTransferLocatorTest.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/transfer/TransferChunkRunnerTest.kt`

- [ ] **Step 1: Write failing lease/race/retry/terminal tests**

First persist a locator, close/reopen Room, and load it using only `(workKey,inputRevision)`. Assert exact survival and reject a changed revision, locator hash, URL, host, method, destination root/path, expected length/hash, or oversized field before any network/file access. Run two runners with the same work key and different owners; exactly one gets the lease and dispatches HTTP. Use a server that stalls longer than one heartbeat interval to prove the independent heartbeat renews without byte callbacks; separately fire the injected call/read-idle timeout and prove heartbeat cancellation is not mistaken for timeout. Force takeover after expiry and make the stale owner attempt each checkpoint, metadata publication, final-file promotion, and terminal commit; every attempt must return `STALE_GENERATION` without mutating bytes/Room. Inject death after bytes/metadata fsync but before DB checkpoint, after DB checkpoint, and after final file replacement but before terminal commit. Recreate runner and assert strong-ETag Range only when validator/total/partial length all agree, otherwise a safe whole restart, final hash, one terminal DB commit, no concurrent duplicate request, retry reason/offset recorded, and cancellation closes call/partial stream without marking success. Switch the server representation between attempts and prove mixed bytes never commit.

- [ ] **Step 2: Run the test and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.transfer.TransferChunkRunnerTest" --console=plain --stacktrace --no-parallel
```

Expected: missing durable locator, lease coordinator, and transfer runner.

- [ ] **Step 3: Add bounded transfer models**

```kotlin
package org.mewx.wenku8.core.data.transfer

data class DurableTransferLocator(
    val schema: Int,
    val workKey: String,
    val inputRevision: Long,
    val sourceId: String,
    val operationCode: String,
    val resourceId: String,
    val canonicalHttpsUrl: String,
    val destinationRootId: String,
    val destinationRelativePath: String,
    val expectedBytes: Long?,
    val expectedSha256: String?,
    val userInitiated: Boolean,
    val locatorSha256: String,
)

enum class RetryClass { IDEMPOTENT_GET, VALIDATED_RANGE, SAFE_WHOLE_RESTART }
data class RetryTrace(val retryClass: RetryClass, val offset: Long, val reasonCode: String)

sealed interface TransferChunkResult {
    data class Progress(val bytes: Long, val total: Long?) : TransferChunkResult
    data class Retry(val trace: RetryTrace) : TransferChunkResult
    data class Completed(val bytes: Long, val sha256: String) : TransferChunkResult
    data object LeaseBusy : TransferChunkResult
    data object Cancelled : TransferChunkResult
}
```

`DurableTransferLocator.create` accepts all fields except `locatorSha256`, requires schema `1`, bounded opaque IDs, operation `PUBLIC_CONTENT_GET`, nonnegative revision/length, lowercase optional SHA-256, and computes `locatorSha256` from every preceding field in the displayed order separated by NUL. An injected `TransferUrlValidator` delegates to the Phase 2 `HostPolicy` and returns the canonical allowlisted HTTPS URL; an injected `TransferDestinationValidator` delegates to Task 5 path policy and returns a canonical relative path under a closed root ID. `loadValidated(workKey,inputRevision)` reads only the canonical `DownloadEntity`, reconstructs the value, recomputes the hash, re-runs both validators, and requires exact work key/revision. Scheduler/worker/JobService input contains only these two opaque values. RetryTrace is local evidence and contains no full URL/title/account/body.

- [ ] **Step 4: Implement the runner lifecycle**

The runner and `TransferLeaseCoordinator` use this exact lifecycle:

1. loads and fully validates the durable locator from only `(workKey,inputRevision)` before acquiring a lease or opening a path;
2. acquires a cross-process exclusive lock file for that work key, observes an absent/expired/different-boot Task 6 lease, atomically calls canonical `downloadDao.fenceNextLease(workKey,owner,expectedGeneration,nextGeneration)`, then inserts/CASes the transient lease to that exact generation before releasing the lock; a competing owner cannot dispatch without winning both fences;
3. starts a child heartbeat coroutine on an independent monotonic timer: every 20 seconds it CAS-renews the 90-second transient expiry for the exact owner/generation even when zero bytes arrive. A failed/late renewal cancels the active OkHttp call and runner with `STALE_GENERATION`. The OkHttp connect timeout, 45-second read-idle timeout, and bounded chunk call timeout are configured independently and never renew the lease;
4. loads actual partial length and compares canonical checkpoint with Task 13 generation-scoped partial metadata; it sends Range only with the exact stored locator hash, owner/generation, strong ETag, total representation length, and prefix length, otherwise records safe whole restart;
5. after every 1 MiB and before yielding, acquires the same cross-process work lock, revalidates both transient owner/generation/unexpired lease and canonical owner/generation, fsyncs bytes/metadata, then calls the generation-guarded canonical checkpoint. Losing either check writes nothing and cancels the call;
6. on whole restart, removes only the current generation's partial, records `SAFE_WHOLE_RESTART`, and yields so the next bounded chunk starts at zero;
7. after EOF/hash validation, reacquires the cross-process lock, revalidates both fences, calls `authorizePromotion(workKey,owner,generation)`, promotes only the matching generation-scoped file, revalidates final hash, and calls `commitTerminalOnce(workKey,owner,generation,SUCCEEDED,hash)` before releasing the lock. Return `0` is accepted only when the existing terminal row has the same success/hash;
8. releases the transient lease in `finally` using exact owner/generation and stops/joins the heartbeat; cancellation first cancels the call, then attempts one generation-fenced nonterminal checkpoint, and is rethrown.

The cross-process lock is held only for short fence/checkpoint/promotion/terminal sections, never during HTTP. Lease acquisition uses the same lock, so takeover cannot interleave between a stale-generation validation and a file promotion. A crash after promotion is recovered by validating the final file; the new generation re-authorizes that same hash and performs the one guarded terminal commit. Every checkpoint/file promotion/terminal path requires a non-forgeable `LeaseFence(ownerId,generation,bootSessionId)` returned by the coordinator; raw owner/generation parameters are not public runner inputs.

It never claims exactly-once HTTP delivery; every retry is one of the three allowed classes.

- [ ] **Step 5: Run crash/race/retry tests**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.transfer.TransferChunkRunnerTest" --console=plain --stacktrace --no-parallel
```

Expected: durable locator reopen/tamper tests, independent heartbeat versus call-timeout tests, all crash points, lease race/takeover, stale-generation rejection at every mutation/promotion boundary, strong-ETag Range validation, validator/length whole restart, mixed-representation rejection, cancellation, and terminal commit tests pass. Assert `terminalCommitCount == 1`.

- [ ] **Step 6: Commit the transfer runner**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/transfer core/data/src/test/java/org/mewx/wenku8/core/data/transfer
git diff --check --cached
git commit -m "feat(data): checkpoint leased transfer chunks"
```

Expected: retry traces contain enum/offset/reason only.

### Task 15: Select Scheduler Paths Deterministically By API And Eligibility

**Depends on:** Tasks 11 and 14.

**Files:**
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/TransferScheduler.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/DefaultTransferScheduler.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/transfer/DefaultTransferSchedulerTest.kt`

- [ ] **Step 1: Write the failing exhaustive API/policy table test**

Create one case for each API 23, 29, 30, 31, 32, 33, 34, 35, and 36, crossed with automatic/user-started, single/bulk, eligible/ineligible, and quota available/unavailable where applicable. Assert automatic migration/reconciliation always selects regular bounded WorkManager. Assert only explicit visible user bulk work can select foreground or UIDT. Notification denial changes disclosure state but never silently drops/cancels work.

- [ ] **Step 2: Run the policy test and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.transfer.DefaultTransferSchedulerTest" --console=plain --stacktrace --no-parallel
```

Expected: unresolved scheduler types.

- [ ] **Step 3: Add complete scheduler request/decision types**

```kotlin
package org.mewx.wenku8.core.data.transfer

data class TransferScheduleRequest(
    val workKey: String,
    val apiLevel: Int,
    val explicitlyUserStarted: Boolean,
    val bulk: Boolean,
    val uidtEligible: Boolean,
    val immediateForegroundEligible: Boolean,
    val notificationPermissionGranted: Boolean,
    val estimatedDownloadBytes: Long?,
    val minimumChunkBytes: Long,
)

enum class ExecutionPath { REGULAR_WORK, FOREGROUND_WORK, EXPEDITED_OR_REGULAR_WORK, UIDT_JOB }
enum class DisclosurePath { SYSTEM_NOTIFICATION_AND_IN_APP, SYSTEM_TASK_DISCLOSURE_AND_IN_APP, IN_APP_QUEUED }

data class ScheduleDecision(
    val execution: ExecutionPath,
    val disclosure: DisclosurePath,
    val reasonCode: String,
)

interface TransferScheduler {
    fun decide(request: TransferScheduleRequest): ScheduleDecision
    suspend fun schedule(request: TransferScheduleRequest): ScheduleDecision
    suspend fun cancel(workKey: String)
}
```

Validate `workKey` is opaque/bounded, estimated bytes are nonnegative/unknown, and minimum chunk bytes are positive.

- [ ] **Step 4: Implement the exact selection table**

`DefaultTransferScheduler.decide` uses:

```kotlin
return when {
    !request.explicitlyUserStarted || !request.bulk -> regular("bounded-regular-work")
    request.apiLevel >= 34 && request.uidtEligible -> uidt("eligible-user-initiated-transfer")
    request.apiLevel >= 34 -> regular("uidt-ineligible-or-quota-unavailable")
    request.apiLevel >= 31 && request.immediateForegroundEligible -> foreground("eligible-visible-foreground-start")
    request.apiLevel >= 31 -> expeditedFallback("foreground-start-ineligible")
    request.apiLevel >= 23 -> foreground("legacy-visible-foreground-start")
    else -> error("minSdk contract violated")
}
```

For foreground/UIDT, `notificationPermissionGranted=false` maps to `SYSTEM_TASK_DISCLOSURE_AND_IN_APP`; regular queued maps to `IN_APP_QUEUED`. `schedule` delegates to injected WorkManager/UIDT adapters and if UIDT scheduling returns quota/ineligible at the scheduling boundary, persists queued state and schedules regular bounded work exactly once.

- [ ] **Step 5: Run the exhaustive selection test**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.transfer.DefaultTransferSchedulerTest" --console=plain --stacktrace --no-parallel
```

Expected: every matrix row passes; no API 34-36 branch selects a background foreground-service start; no automatic branch selects UIDT.

- [ ] **Step 6: Commit scheduler policy**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/transfer/TransferScheduler.kt core/data/src/main/java/org/mewx/wenku8/core/data/transfer/DefaultTransferScheduler.kt core/data/src/test/java/org/mewx/wenku8/core/data/transfer/DefaultTransferSchedulerTest.kt
git diff --check --cached
git commit -m "feat(data): select api-aware transfer scheduling"
```

Expected: policy is pure and device-independent.

### Task 16: Implement API 23-33 Foreground Work And Denial Fallback

**Depends on:** Task 15.

**Files:**
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/ForegroundTransferWorker.kt`
- Create: `core/data/src/main/java/org/mewx/wenku8/core/data/transfer/WorkManagerTransferAdapter.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/transfer/ForegroundTransferWorkerTest.kt`
- Test: `core/data/src/test/java/org/mewx/wenku8/core/data/transfer/WorkManagerTransferAdapterTest.kt`

- [ ] **Step 1: Write failing foreground-before-transfer, denial, quota, and cancellation tests**

With injected `ForegroundPromoter`, `TransferChunkRunner`, and fallback scheduler, assert `setForeground` occurs before the first byte; API 31-33 `ForegroundServiceStartNotAllowedException` persists visible queued state and enqueues one regular request; expedited quota uses `RUN_AS_NON_EXPEDITED_WORK_REQUEST`; notification denial retains in-app progress/cancel state; worker cancellation cancels runner/OkHttp and persists checkpoint; repeated fallback does not create duplicate unique work.

- [ ] **Step 2: Run tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.transfer.ForegroundTransferWorkerTest" --tests "org.mewx.wenku8.core.data.transfer.WorkManagerTransferAdapterTest" --console=plain --stacktrace --no-parallel
```

Expected: missing worker/adapter contracts.

- [ ] **Step 3: Add the foreground worker**

`ForegroundTransferWorker` accepts only `workKey` and `inputRevision` from WorkManager Data, loads all URL/path/bytes from canonical Room, calls injected `ForegroundInfoFactory.create(workKey)` and `setForeground` before runner invocation, and maps chunk results to bounded success/retry/failure. Catch `ForegroundServiceStartNotAllowedException` only on API 31-33; persist `FOREGROUND_START_DENIED`, call `enqueueRegularUnique(workKey)`, and return success for the denied request so it does not loop.

Its `getForegroundInfo()` returns the same stable notification ID/channel and a cancel PendingIntent scoped to opaque work key. Notification title/body are generic localized strings such as “Download in progress” and byte percentage; never novel/chapter/account text.

- [ ] **Step 4: Add exact WorkRequest construction**

For eligible visible starts on API 23-33, enqueue unique `foreground-transfer:<workKey>` with network/storage constraints and exponential backoff. On API 31-33, the optional first request uses:

```kotlin
OneTimeWorkRequestBuilder<ForegroundTransferWorker>()
    .setInputData(workDataOf("workKey" to workKey, "inputRevision" to revision))
    .setConstraints(constraints)
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
    .addTag("transfer")
    .build()
```

Use `ExistingWorkPolicy.KEEP`; tags contain no source/account/content ID.

- [ ] **Step 5: Run deterministic worker/fallback tests**

Run:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest --tests "org.mewx.wenku8.core.data.transfer.ForegroundTransferWorkerTest" --tests "org.mewx.wenku8.core.data.transfer.WorkManagerTransferAdapterTest" --console=plain --stacktrace --no-parallel
```

Expected: setForeground ordering, API31-33 denial, quota fallback, notification denial, unique enqueue, checkpoint, and cancellation cases pass.

- [ ] **Step 6: Commit the API 23-33 path**

```powershell
git add core/data/src/main/java/org/mewx/wenku8/core/data/transfer/ForegroundTransferWorker.kt core/data/src/main/java/org/mewx/wenku8/core/data/transfer/WorkManagerTransferAdapter.kt core/data/src/test/java/org/mewx/wenku8/core/data/transfer/ForegroundTransferWorkerTest.kt core/data/src/test/java/org/mewx/wenku8/core/data/transfer/WorkManagerTransferAdapterTest.kt
git diff --check --cached
git commit -m "feat(data): add foreground transfer fallback"
```

Expected: no illegal foreground retry loop.

### Task 17: Implement API 34-36 User-Initiated Data Transfer Lifecycle

**Depends on:** Task 15 and Task 14 lease/runner.

**Files:**
- Create: `app/src/main/java/org/mewx/wenku8/work/UidtJobSpecFactory.kt`
- Create: `app/src/main/java/org/mewx/wenku8/work/UidtTransferJobService.kt`
- Test: `app/src/test/java/org/mewx/wenku8/work/UidtJobSpecFactoryTest.kt`
- Test: `app/src/test/java/org/mewx/wenku8/work/UidtTransferJobServiceTest.kt`

- [ ] **Step 1: Write failing JobInfo and service-race tests**

Using Robolectric/API fakes, assert JobInfo has `setUserInitiated(true)`, validated Internet NetworkRequest, estimated download/minimum chunk bytes, storage constraint, persistence, and only opaque work key/revision extras. Assert the Android API tuple is exactly `(downloadBytes, uploadBytes)`: estimated bytes are `(remainingBytes, NETWORK_BYTES_UNKNOWN)`, transferred progress is `(bytes, 0L)`, and refreshed estimates are `(remaining, 0L)`. Service tests cover immediate `setNotification`, progress bytes, one terminal `jobFinished`, coroutine/call cancellation in `onStopJob`, recoverable/permanent/user-cancel stop decisions, remaining-byte estimate refresh, reschedule, lease contention with WorkManager, and completion-vs-stop race.

- [ ] **Step 2: Run tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.work.UidtJobSpecFactoryTest" --tests "org.mewx.wenku8.work.UidtTransferJobServiceTest" --console=plain --stacktrace --no-parallel
```

Expected: missing UIDT spec factory/service.

- [ ] **Step 3: Add the complete JobInfo factory**

```kotlin
package org.mewx.wenku8.work

import android.app.job.JobInfo
import android.content.ComponentName
import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.PersistableBundle
import androidx.annotation.RequiresApi

@RequiresApi(34)
class UidtJobSpecFactory(private val context: Context) {
    fun create(jobId: Int, workKey: String, revision: Long, remainingBytes: Long?, minimumChunkBytes: Long): JobInfo {
        require(workKey.length in 1..256)
        require(revision >= 0L)
        require(minimumChunkBytes > 0L)
        val network = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        val extras = PersistableBundle().apply {
            putString("workKey", workKey)
            putLong("inputRevision", revision)
        }
        return JobInfo.Builder(jobId, ComponentName(context, UidtTransferJobService::class.java))
            .setUserInitiated(true)
            .setRequiredNetwork(network)
            .setEstimatedNetworkBytes(
                remainingBytes ?: JobInfo.NETWORK_BYTES_UNKNOWN,
                JobInfo.NETWORK_BYTES_UNKNOWN,
            )
            .setMinimumNetworkChunkBytes(minimumChunkBytes)
            .setRequiresStorageNotLow(true)
            .setPersisted(true)
            .setExtras(extras)
            .build()
    }
}
```

If metered transfer is disabled by canonical user settings, add `NET_CAPABILITY_NOT_METERED`; do not hard-code it for users who explicitly allow metered downloads.

- [ ] **Step 4: Add one-terminal-path service state**

Define `RunningJob(params, job, callCanceller, finished: AtomicBoolean)` in a `ConcurrentHashMap<Int, RunningJob>`. `onStartJob` validates API/extras, calls `setNotification(params, notificationId, notification, JOB_END_NOTIFICATION_POLICY_REMOVE)` before launching, returns `true`, acquires the shared Task 6 work-key lease, and runs one bounded chunk in the injected service scope. Progress invokes `updateTransferredNetworkBytes(params, bytes, 0L)` and updates the generic notification. Before reschedule, invoke `updateEstimatedNetworkBytes(params, remaining, 0L)`. The tests capture both arguments and fail if download/upload order is reversed.

Completion calls a helper:

```kotlin
private fun finish(running: RunningJob, reschedule: Boolean) {
    if (running.finished.compareAndSet(false, true)) {
        runningJobs.remove(running.params.jobId, running)
        jobFinished(running.params, reschedule)
    }
}
```

No other code calls `jobFinished`.

- [ ] **Step 5: Implement stop/cancel/retry semantics**

`onStopJob` first wins `running.finished.compareAndSet(false, true)` and removes the matching map entry, then cancels its coroutine and active OkHttp call, durably records `params.stopReason` with checkpoint, and decides:

- return `false` for `STOP_REASON_CANCELLED_BY_APP`, `STOP_REASON_USER`, persisted user cancellation, permanent protocol/hash failure, or already terminal state;
- return `true` for connectivity/storage/battery constraints, quota, preempt, timeout, system processing, background restriction, device state, or undefined recoverable interruption;
- completion/stop races use the same AtomicBoolean; when `onStopJob` wins, the coroutine cannot later call `jobFinished`, and the stopped job does not call `jobFinished` because JobScheduler already owns the stop path.

The next start acquires the same lease and resumes only validated Range or safe whole-item restart.

- [ ] **Step 6: Run UIDT lifecycle tests on API 34, 35, and 36 configurations**

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.work.UidtJobSpecFactoryTest" --tests "org.mewx.wenku8.work.UidtTransferJobServiceTest" --console=plain --stacktrace --no-parallel
```

Expected: JobInfo fields, immediate notification, byte progress, one finish, all stop reasons, cancellation, race, lease contention, and reschedule tests pass.

- [ ] **Step 7: Commit UIDT implementation**

```powershell
git add app/src/main/java/org/mewx/wenku8/work/UidtJobSpecFactory.kt app/src/main/java/org/mewx/wenku8/work/UidtTransferJobService.kt app/src/test/java/org/mewx/wenku8/work/UidtJobSpecFactoryTest.kt app/src/test/java/org/mewx/wenku8/work/UidtTransferJobServiceTest.kt
git diff --check --cached
git commit -m "feat(app): add user initiated transfer jobs"
```

Expected: no raw URL/account/content/secret in JobInfo or notification.

### Task 18: Integrate Manifest, Notifications, Worker Factory, And Scheduler

**Depends on:** Tasks 16 and 17.

**Files:**
- Create: `app/src/main/java/org/mewx/wenku8/work/TransferNotificationFactory.kt`
- Create: `app/src/main/java/org/mewx/wenku8/work/TransferCancelReceiver.kt`
- Create: `app/src/main/java/org/mewx/wenku8/work/AndroidTransferScheduler.kt`
- Create: `app/src/main/java/org/mewx/wenku8/work/Wenku8WorkerFactory.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/di/AppContainer.kt`
- Modify: `app/src/main/java/org/mewx/wenku8/MyApp.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/org/mewx/wenku8/work/AndroidTransferSchedulerTest.kt`
- Test: `app/src/androidTest/java/org/mewx/wenku8/work/Phase3MergedManifestTest.kt`

- [ ] **Step 1: Write failing merged-manifest and integration tests**

Assert every flavor's merged manifest has only the selected declarations: foreground/dataSync/notification permissions, UIDT and boot permission, WorkManager's wake/network/system job/foreground/reschedule components, and one non-exported BIND_JOB_SERVICE-protected UIDT service. Assert no duplicate service, wrong process, exported service, missing foreground type, or secret-bearing metadata. Test scheduler cancellation cancels both unique WorkManager work and UIDT job derived from the stable work-key job ID.

- [ ] **Step 2: Run tests and observe the intended RED**

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.work.AndroidTransferSchedulerTest" :app:processAlphaDebugMainManifest :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.work.Phase3MergedManifestTest --console=plain --stacktrace --no-parallel
```

Expected: missing scheduler/factory plus manifest contract failures.

- [ ] **Step 3: Add exact manifest declarations**

Merge these into the existing manifest and retain Phase 0 privacy removals:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RUN_USER_INITIATED_JOBS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<application
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules">
    <service
        android:name="androidx.work.impl.foreground.SystemForegroundService"
        android:foregroundServiceType="dataSync"
        tools:node="merge" />
    <service
        android:name="org.mewx.wenku8.work.UidtTransferJobService"
        android:exported="false"
        android:permission="android.permission.BIND_JOB_SERVICE" />
    <receiver
        android:name="org.mewx.wenku8.work.TransferCancelReceiver"
        android:exported="false" />
</application>
```

Do not add a custom boot receiver when WorkManager/JobScheduler persistence already supplies it; inventory the merged AndroidX receivers instead.

- [ ] **Step 4: Add generic notification and cancellation surface**

Create channel `durable_transfers`, stable notification IDs from a collision-tested hash registry, generic localized title/status, and bounded integer progress. The cancel PendingIntent targets the non-exported `TransferCancelReceiver` and carries only opaque `workKey`; the receiver validates the key, uses `goAsync()`, invokes the AppContainer's structured application scope to call `TransferScheduler.cancel(workKey)`, and always calls `PendingResult.finish()` in `finally`. `POST_NOTIFICATIONS` denial leaves canonical DB Flow available to the in-app progress/cancel surface and relies on platform task disclosure; it does not suppress required foreground/UIDT calls.

- [ ] **Step 5: Bind workers/schedulers through AppContainer**

`MyApp` implements `Configuration.Provider`, returns a WorkManager `Configuration` with `Wenku8WorkerFactory`, and obtains dependencies from `DefaultAppContainer`. The worker factory constructs only known worker classes and returns null for unknown classes. `AndroidTransferScheduler` uses Task 15 decision, Task 16 adapter on API 23-33, Task 17 JobScheduler on API 34-36, persists queued state before fallback, and uses one stable job ID mapping per work key.

- [ ] **Step 6: Test API 31 denial, API 33 notification denial, and API 34 UIDT integration**

Run:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.work.*" :app:processAlphaDebugMainManifest :app:processBaiduDebugMainManifest :app:processPlaystoreDebugMainManifest :app:connectedAlphaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.mewx.wenku8.work.Phase3MergedManifestTest --console=plain --stacktrace --no-parallel
```

Expected: scheduler integration and all three merged manifests pass; denial branches retain in-app cancel state.

- [ ] **Step 7: Commit Android integration**

```powershell
git add app/src/main/java/org/mewx/wenku8/work app/src/main/java/org/mewx/wenku8/di/AppContainer.kt app/src/main/java/org/mewx/wenku8/MyApp.kt app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml app/src/test/java/org/mewx/wenku8/work app/src/androidTest/java/org/mewx/wenku8/work
git diff --check --cached
git commit -m "feat(app): integrate durable transfer scheduling"
```

Expected: no analytics/ad/crash initializer reappears and each flavor uses the same fail-closed declarations.

### Task 19: Prove Stop, Kill, Force-Stop, And Reboot Recovery With A Host Harness

**Depends on:** Tasks 6-18.

**Files:**
- Modify: `app/build.gradle`
- Create: `app/src/phase3Harness/java/org/mewx/wenku8/harness/Phase3HarnessBindings.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/harness/Phase3StageATest.kt`
- Create: `app/src/androidTest/java/org/mewx/wenku8/harness/Phase3StageBTest.kt`
- Create: `tools/phase3-device-harness.ps1`
- Test output: `build/reports/phase3/<api>/<scenario>/`

- [ ] **Step 1: Write failing Stage A/B tests against the absent harness binding**

Stage A accepts instrumentation args `scenario` and `checkpoint`, seeds only synthetic fixtures, enqueues the matching migration/transfer, waits until the named production checkpoint is durable, and atomically writes/fsyncs `files/phase3-harness/pre.json`. The production operation remains paused by a harness-only cancellable latch, but Stage A itself returns normally immediately after the durable report; it never waits for the host action. The host must wait for the Stage A instrumentation process to exit successfully and prove the target app process/job still exists before issuing a release/stop/kill action. Stage B is a distinct instrumentation invocation; it loads canonical DB/checkpoint/journal/lease/final file/notification state and writes `post.json` only after asserting:

- terminal canonical commit count is exactly one;
- no overlapping lease owner or duplicate active request exists;
- stop reason equals the host action;
- cancellation is terminal or interruption is safely rescheduled;
- every retry trace is GET, validated Range, or safe whole restart;
- final bytes/hash match fixture when terminal success is expected;
- legacy projection catches up after the post-canonical/pre-projection checkpoint.
- the retained legacy read adapters, configured with canonical Room/DataStore access disabled, read the projected bookshelf/progress/settings/catalog bytes and match the Phase 0 old-release golden semantics on that API.

Before seeding, Stage A runs the API-owned boundary assertion from `phase-3-storage-contract.yaml`: API 23 minimum/legacy-file behavior; API 29 last legacy external-storage behavior; API 30 scoped-storage boundary; API 31 foreground denial/expedited fallback; API 32 final legacy storage permission; API 33 notification denial; API 34 UIDT eligibility; API 35 timeout/stop reason; API 36 current UIDT/notification behavior. It records the stable assertion ID and result in `pre.json`; an API row cannot pass only because generic transfer bytes succeeded.

Guard both tests with `check(BuildConfig.PHASE3_HARNESS)` so they cannot mutate a normal debug run.

- [ ] **Step 2: Run the harness compilation and observe the intended RED**

Run:

```powershell
.\gradlew.bat -Pwenku8Provider=public -Pwenku8TestBuildType=phase3Harness :app:compileAlphaPhase3HarnessKotlin :app:compileAlphaPhase3HarnessAndroidTestKotlin --console=plain --stacktrace --no-parallel
```

Expected: missing `phase3Harness` build type/bindings.

- [ ] **Step 3: Add the release-like harness build type without changing normal debug tests**

Add to `app/build.gradle`:

```groovy
android {
    testBuildType providers.gradleProperty('wenku8TestBuildType').getOrElse('debug')

    buildTypes {
        debug {
            buildConfigField 'boolean', 'PHASE3_HARNESS', 'false'
        }
        release {
            buildConfigField 'boolean', 'PHASE3_HARNESS', 'false'
        }
        phase3Harness {
            initWith release
            debuggable true
            minifyEnabled true
            signingConfig signingConfigs.debug
            matchingFallbacks = ['release', 'debug']
            buildConfigField 'boolean', 'PHASE3_HARNESS', 'true'
        }
    }
}
```

The harness keeps the real application ID and therefore may run only after the host proves a disposable emulator and uninstalls any pre-existing package. It is minified/release-like but never a distributable signed artifact.

- [ ] **Step 4: Add named checkpoint bindings only in the harness source set**

`Phase3HarnessBindings` implements the injected fault/checkpoint interface with named points `AFTER_JOURNAL`, `POST_CANONICAL_PRE_PROJECTION`, `AFTER_FILE_FSYNC`, `AFTER_DB_CHECKPOINT`, and `AFTER_FINAL_REPLACE`. It atomically persists a harness-only latch containing a random 128-bit lowercase `releaseToken`, fsyncs it, and suspends the production coroutine cancellably through `FileObserver`/`CompletableDeferred`; no busy loop. Only the `scheduler-run` and backup-capture protocols create the exact `files/phase3-harness/release-<token>` file via `run-as`; stop/timeout/process-kill/force-stop/reboot release only through real cancellation/process lifecycle. A token is one-use, bounded, validated, deleted after consumption, and cannot select a path. Normal debug/release binds `FaultInjector.None` and contains no host-controlled pause path.

The source-set binding rejects any fixture whose source URL is not the local MockWebServer loopback address and rejects non-synthetic destination roots.

- [ ] **Step 5: Add complete Stage A/B report contracts**

Use `org.json.JSONObject` with fixed keys:

```json
{
  "schema": "wenku8-phase3-harness/v1",
  "stage": "A",
  "api": 34,
  "scenario": "scheduler-stop",
  "checkpoint": "POST_CANONICAL_PRE_PROJECTION",
  "workKey": "phase3-synthetic-001",
  "jobId": 730001,
  "appPid": 2468,
  "processName": "org.mewx.wenku8",
  "releaseToken": "0123456789abcdef0123456789abcdef",
  "canonicalCommitCount": 1,
  "legacyProjectionVersion": 0,
  "mutationVersion": 1,
  "activeLeaseCount": 1,
  "requestCount": 1,
  "fixtureSha256": "59f659b686b6479ed64eecf7347cb1f49bd42ee6143285db723788ea43e06ca8"
}
```

The synthetic transfer body is exactly the UTF-8 bytes of `phase3-synthetic-fixture-v1`; the SHA-256 shown above is its computed hash. `appPid` comes from `Process.myPid()` and `processName` from `Application.getProcessName()`; Stage A fails unless the process name is exactly the application ID (not the test package or a colon process). Stage B uses the same schema and fixed additional keys `appPid`, `stopReason`, `retryClasses`, `finalSha256`, `terminalCommitCount`, `activeLeaseCount`, `duplicateRequestCount`, and `projectionCaughtUp`. Generated reports are retained evidence and are not committed as fixtures.

- [ ] **Step 6: Add the complete emulator-only PowerShell harness**

Create this script and keep every destructive action after the emulator guards:

```powershell
# tools/phase3-device-harness.ps1
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet(23, 29, 30, 31, 32, 33, 34, 35, 36)]
    [int]$Api,

    [Parameter(Mandatory = $true)]
    [ValidateSet('scheduler-run', 'scheduler-stop', 'scheduler-timeout', 'process-kill', 'force-stop', 'reboot')]
    [string]$Scenario,

    [string]$Serial = $env:ANDROID_SERIAL
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$android = Join-Path $repo 'studio-android\LightNovelLibrary'
$sdkRoot = if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_SDK_ROOT)) { $env:ANDROID_SDK_ROOT } else { $env:ANDROID_HOME }
if ([string]::IsNullOrWhiteSpace($sdkRoot)) { throw 'PHASE3-HARNESS: Android SDK environment is not set' }
$adbName = if ([System.IO.Path]::DirectorySeparatorChar -eq '\') { 'adb.exe' } else { 'adb' }
$adb = Join-Path $sdkRoot "platform-tools/$adbName"
if (-not (Test-Path -LiteralPath $adb)) { throw 'PHASE3-HARNESS: adb executable not found' }

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)
    $prefix = @()
    if (-not [string]::IsNullOrWhiteSpace($Serial)) { $prefix = @('-s', $Serial) }
    $output = & $adb @prefix @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "PHASE3-HARNESS adb failed: $($Arguments -join ' ')`n$output" }
    return ($output -join "`n").Trim()
}

function Start-StageAInstrumentation {
    param(
        [string[]]$InstrumentationArguments,
        [string]$StdoutPath,
        [string]$StderrPath
    )
    $arguments = @()
    if (-not [string]::IsNullOrWhiteSpace($Serial)) { $arguments += @('-s', $Serial) }
    $arguments += @('shell', 'am', 'instrument', '-w', '-r')
    $arguments += $InstrumentationArguments
    $start = @{
        FilePath = $adb
        ArgumentList = $arguments
        RedirectStandardOutput = $StdoutPath
        RedirectStandardError = $StderrPath
        PassThru = $true
    }
    if ([System.IO.Path]::DirectorySeparatorChar -eq '\') { $start.WindowStyle = 'Hidden' }
    return Start-Process @start
}

function Read-HarnessJson {
    param([string]$RelativePath)
    $prefix = @()
    if (-not [string]::IsNullOrWhiteSpace($Serial)) { $prefix = @('-s', $Serial) }
    $output = & $adb @prefix exec-out run-as $package cat $RelativePath 2>$null
    if ($LASTEXITCODE -ne 0) { return $null }
    $text = ($output -join "`n").Trim()
    if ([string]::IsNullOrWhiteSpace($text)) { return $null }
    try { return [pscustomobject]@{ Text = $text; Json = ($text | ConvertFrom-Json) } } catch { return $null }
}

function Get-Sha256([string]$Path) {
    return (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
}

function Get-ReportMerkle([string]$Directory) {
    $root = (Resolve-Path -LiteralPath $Directory).Path
    $selfManifest = [IO.Path]::GetFullPath((Join-Path $root 'evidence-manifest.json'))
    $lines = foreach ($file in (Get-ChildItem -LiteralPath $root -Recurse -File | Where-Object { [IO.Path]::GetFullPath($_.FullName) -ne $selfManifest } | Sort-Object FullName)) {
        if ($file.Attributes.HasFlag([IO.FileAttributes]::ReparsePoint)) { throw "PHASE3-HARNESS: report symlink forbidden $($file.FullName)" }
        $relative = [IO.Path]::GetRelativePath($root, $file.FullName).Replace('\', '/')
        "$relative`0$(Get-Sha256 $file.FullName)"
    }
    if (@($lines).Count -eq 0) { throw 'PHASE3-HARNESS: empty evidence report' }
    $bytes = [Text.Encoding]::UTF8.GetBytes((@($lines) -join "`n") + "`n")
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return (($sha.ComputeHash($bytes) | ForEach-Object { $_.ToString('x2') }) -join '') } finally { $sha.Dispose() }
}

if ([string]::IsNullOrWhiteSpace($Serial)) {
    $devices = @(& $adb devices | Select-String '\tdevice$' | ForEach-Object { ($_.Line -split '\s+')[0] })
    if ($devices.Count -ne 1) { throw "PHASE3-HARNESS: expected one disposable emulator, found $($devices.Count)" }
    $Serial = $devices[0]
}

$qemu = Invoke-Adb shell getprop ro.kernel.qemu
$avd = Invoke-Adb shell getprop ro.boot.qemu.avd_name
$actualApi = [int](Invoke-Adb shell getprop ro.build.version.sdk)
if ($qemu -ne '1' -or [string]::IsNullOrWhiteSpace($avd)) {
    throw 'PHASE3-HARNESS: destructive scenarios require a named disposable emulator'
}
if ($actualApi -ne $Api) { throw "PHASE3-HARNESS: requested API $Api but device is API $actualApi" }

$package = 'org.mewx.wenku8'
$testPackage = 'org.mewx.wenku8.test'
$runner = "$testPackage/androidx.test.runner.AndroidJUnitRunner"
$checkpoint = if ($Scenario -eq 'scheduler-run') { 'AFTER_DB_CHECKPOINT' } else { 'POST_CANONICAL_PRE_PROJECTION' }
$rowId = switch ($Scenario) {
    'scheduler-run' { 'P3-API-MATRIX' }
    'scheduler-stop' { 'P3-HOST-STOP' }
    'scheduler-timeout' { 'P3-HOST-STOP' }
    'process-kill' { 'P3-HOST-KILL' }
    'force-stop' { 'P3-HOST-FORCE-STOP' }
    'reboot' { 'P3-HOST-REBOOT' }
}
$reportBase = [IO.Path]::GetFullPath((Join-Path $repo 'build\reports\phase3'))
$reportRoot = [IO.Path]::GetFullPath((Join-Path $reportBase "$Api\$Scenario"))
if (-not $reportRoot.StartsWith($reportBase + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'PHASE3-HARNESS: report path escaped root' }
if (Test-Path -LiteralPath $reportRoot) { Remove-Item -LiteralPath $reportRoot -Recurse -Force }
New-Item -ItemType Directory -Force -Path $reportRoot | Out-Null

Push-Location $android
try {
    & .\gradlew.bat -Pwenku8Provider=public -Pwenku8TestBuildType=phase3Harness :app:assembleAlphaPhase3Harness :app:assembleAlphaPhase3HarnessAndroidTest --console=plain --stacktrace --no-parallel
    if ($LASTEXITCODE -ne 0) { throw 'PHASE3-HARNESS: Gradle build failed' }
} finally {
    Pop-Location
}

$appApk = Join-Path $android 'app\build\outputs\apk\alpha\phase3Harness\app-alpha-phase3Harness.apk'
$testApk = Join-Path $android 'app\build\outputs\apk\androidTest\alpha\phase3Harness\app-alpha-phase3Harness-androidTest.apk'
if (-not (Test-Path -LiteralPath $appApk)) { throw "PHASE3-HARNESS: missing $appApk" }
if (-not (Test-Path -LiteralPath $testApk)) { throw "PHASE3-HARNESS: missing $testApk" }

$stageAProcess = $null
$stageAStdout = Join-Path $reportRoot 'stage-a-stdout.txt'
$stageAStderr = Join-Path $reportRoot 'stage-a-stderr.txt'
$stageAWasKilledByCleanup = $false
try {
    try { Invoke-Adb uninstall $package | Out-Null } catch { }
    Invoke-Adb install $appApk | Out-Null
    Invoke-Adb install $testApk | Out-Null

    $stageAArguments = @(
        '-e', 'class', 'org.mewx.wenku8.harness.Phase3StageATest#seedAndPause',
        '-e', 'scenario', $Scenario,
        '-e', 'checkpoint', $checkpoint,
        $runner
    )
    $stageAProcess = Start-StageAInstrumentation $stageAArguments $stageAStdout $stageAStderr
    $preDeadline = [DateTimeOffset]::UtcNow.AddMinutes(3)
    $preCapture = $null
    do {
        $preCapture = Read-HarnessJson 'files/phase3-harness/pre.json'
        if ($null -ne $preCapture) { break }
        if ($stageAProcess.HasExited) { throw 'PHASE3-HARNESS: Stage A exited before durable pre.json' }
        if ([DateTimeOffset]::UtcNow -gt $preDeadline) { throw 'PHASE3-HARNESS: Stage A checkpoint timeout' }
        Start-Sleep -Milliseconds 500
    } while ($true)

    $prePath = Join-Path $reportRoot 'pre.json'
    $preCapture.Text | Set-Content -LiteralPath $prePath -Encoding utf8NoBOM
    $pre = $preCapture.Json
    if ($pre.api -ne $Api -or $pre.scenario -ne $Scenario -or $pre.checkpoint -ne $checkpoint) {
        throw 'PHASE3-HARNESS: Stage A report identity mismatch'
    }

    $jobId = [string]$pre.jobId
    switch ($Scenario) {
        'scheduler-run' { Invoke-Adb shell cmd jobscheduler run -f $package $jobId | Out-Null }
        'scheduler-stop' { Invoke-Adb shell cmd jobscheduler stop $package $jobId | Out-Null }
        'scheduler-timeout' { Invoke-Adb shell cmd jobscheduler timeout $package $jobId | Out-Null }
        'process-kill' {
            $pid = Invoke-Adb shell pidof $package
            if ([string]::IsNullOrWhiteSpace($pid)) { throw 'PHASE3-HARNESS: app process not running before kill' }
            Invoke-Adb shell kill -9 $pid | Out-Null
        }
        'force-stop' { Invoke-Adb shell am force-stop $package | Out-Null }
        'reboot' {
            Invoke-Adb reboot | Out-Null
            & $adb -s $Serial wait-for-device | Out-Null
            $deadline = [DateTimeOffset]::UtcNow.AddMinutes(4)
            while ((Invoke-Adb shell getprop sys.boot_completed) -ne '1') {
                if ([DateTimeOffset]::UtcNow -gt $deadline) { throw 'PHASE3-HARNESS: boot timeout' }
                Start-Sleep -Seconds 2
            }
        }
    }

    if (-not $stageAProcess.WaitForExit(45000)) {
        throw 'PHASE3-HARNESS: Stage A did not exit after the host action'
    }
    if ($Scenario -in @('force-stop', 'reboot')) {
        Invoke-Adb shell am start -W -n "$package/org.mewx.wenku8.activity.MainActivity" | Out-Null
    }

    $readyDeadline = [DateTimeOffset]::UtcNow.AddMinutes(3)
    do {
        Start-Sleep -Seconds 1
        $state = Invoke-Adb shell dumpsys jobscheduler
        $ready = $state.Contains($package)
    } until ($ready -or [DateTimeOffset]::UtcNow -gt $readyDeadline)
    $state | Set-Content -LiteralPath (Join-Path $reportRoot 'jobscheduler.txt') -Encoding utf8

    $stageB = Invoke-Adb shell am instrument -w -r -e class 'org.mewx.wenku8.harness.Phase3StageBTest#verifyAfterHostAction' -e scenario $Scenario -e checkpoint $checkpoint $runner
    $stageB | Set-Content -LiteralPath (Join-Path $reportRoot 'stage-b-instrumentation.txt') -Encoding utf8
    if (-not $stageB.Contains('OK (1 test)')) { throw "PHASE3-HARNESS: Stage B failed`n$stageB" }

    $postCapture = Read-HarnessJson 'files/phase3-harness/post.json'
    if ($null -eq $postCapture) { throw 'PHASE3-HARNESS: Stage B post.json missing' }
    $postPath = Join-Path $reportRoot 'post.json'
    $postCapture.Text | Set-Content -LiteralPath $postPath -Encoding utf8NoBOM
    $post = $postCapture.Json
    if ($post.terminalCommitCount -ne 1) { throw 'PHASE3-HARNESS: terminal canonical commit count is not one' }
    if ($post.activeLeaseCount -ne 0) { throw 'PHASE3-HARNESS: lease remained active' }
    if ($post.duplicateRequestCount -ne 0) { throw 'PHASE3-HARNESS: concurrent duplicate request observed' }
    if (-not $post.projectionCaughtUp) { throw 'PHASE3-HARNESS: legacy projection did not reconcile' }
    $allowedRetry = @('IDEMPOTENT_GET', 'VALIDATED_RANGE', 'SAFE_WHOLE_RESTART')
    foreach ($retry in @($post.retryClasses)) {
        if ($retry -notin $allowedRetry) { throw "PHASE3-HARNESS: forbidden retry class $retry" }
    }
    if ($post.finalSha256 -and $post.finalSha256 -ne $post.fixtureSha256) { throw 'PHASE3-HARNESS: final fixture hash mismatch' }

    $stageAMetaPath = Join-Path $reportRoot 'stage-a-process.json'
    [ordered]@{
        pid = $stageAProcess.Id
        exitCode = $stageAProcess.ExitCode
        killedByCleanup = $false
    } | ConvertTo-Json | Set-Content -LiteralPath $stageAMetaPath -Encoding utf8NoBOM
    $hashInputs = @($prePath, $postPath, (Join-Path $reportRoot 'jobscheduler.txt'), $stageAStdout, $stageAStderr, $stageAMetaPath)
    $hashes = foreach ($path in ($hashInputs | Sort-Object)) {
        $hash = Get-FileHash -Algorithm SHA256 -LiteralPath $path
        [ordered]@{ path = [IO.Path]::GetRelativePath($reportRoot, $path).Replace('\', '/'); sha256 = $hash.Hash.ToLowerInvariant() }
    }
    $hashes | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $reportRoot 'hashes.json') -Encoding utf8NoBOM
    $sourceCommit = (& git -C $repo rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or $sourceCommit -notmatch '^[0-9a-f]{40}$') { throw 'PHASE3-HARNESS: source commit unavailable' }
    $fixture = Join-Path $repo 'docs\verification\phase-3-storage-contract.yaml'
    $fixtureRelative = [IO.Path]::GetRelativePath($repo, $fixture).Replace('\', '/')
    $artifactRelative = [IO.Path]::GetRelativePath($repo, $appApk).Replace('\', '/')
    $reportRelative = [IO.Path]::GetRelativePath($repo, $reportRoot).Replace('\', '/')
    $reportHash = Get-ReportMerkle $reportRoot
    [ordered]@{
        schema = 'wenku8-phase3-evidence/v1'
        id = $rowId
        command = "pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api $Api -Scenario $Scenario"
        test_id = 'org.mewx.wenku8.harness.Phase3StageBTest#verifyAfterHostAction'
        source_commit = $sourceCommit
        commit_sha = $sourceCommit
        fixture_path = $fixtureRelative
        fixture_sha256 = Get-Sha256 $fixture
        artifact_path = $artifactRelative
        artifact_sha256 = Get-Sha256 $appApk
        report_path = $reportRelative
        report_sha256 = $reportHash
        report_hash_excludes = @('evidence-manifest.json')
        api = $Api
        scenario = $Scenario
    } | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $reportRoot 'evidence-manifest.json') -Encoding utf8NoBOM
    if ((Get-ReportMerkle $reportRoot) -ne $reportHash) { throw 'PHASE3-HARNESS: report Merkle changed after manifest write' }
    Write-Host "PHASE3-HARNESS-PASS api=$Api scenario=$Scenario report=$reportRoot"
} finally {
    if ($null -ne $stageAProcess) {
        if (-not $stageAProcess.HasExited) {
            Stop-Process -Id $stageAProcess.Id -Force -ErrorAction SilentlyContinue
            $stageAWasKilledByCleanup = $true
        }
        $stageAProcess.WaitForExit()
        $stageAMetaPath = Join-Path $reportRoot 'stage-a-process.json'
        if (-not (Test-Path -LiteralPath $stageAMetaPath)) {
            [ordered]@{
                pid = $stageAProcess.Id
                exitCode = $stageAProcess.ExitCode
                killedByCleanup = $stageAWasKilledByCleanup
            } | ConvertTo-Json | Set-Content -LiteralPath $stageAMetaPath -Encoding utf8NoBOM
        }
    }
    try { Invoke-Adb shell am force-stop $package | Out-Null } catch { }
}
```

On non-Windows CI, use `pwsh` and the platform `adb` executable; keep the same guards and argument-array invocation. Do not replace the emulator checks with a command-line opt-out.

- [ ] **Step 7: Compile and run the non-destructive scheduler-run smoke**

Run from the repository root against a disposable emulator whose API matches the parameter:

```powershell
pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api 34 -Scenario scheduler-run
```

Expected: `PHASE3-HARNESS-PASS api=34 scenario=scheduler-run`; pre/post/jobscheduler/process/hash files and the source-bound `evidence-manifest.json` exist.

- [ ] **Step 8: Run each destructive scenario on its owned API**

Run:

```powershell
pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api 31 -Scenario scheduler-stop
pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api 35 -Scenario scheduler-timeout
pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api 29 -Scenario process-kill
pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api 33 -Scenario force-stop
pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api 36 -Scenario reboot
```

Expected for each: PASS, one canonical terminal commit, no concurrent duplicate request, recorded stop reason, projection caught up, only allowed retry classes, and a freshly recomputed evidence manifest. Run commands only after switching to the matching disposable emulator; an API mismatch must fail before uninstall/action.

- [ ] **Step 9: Commit harness source, not generated reports**

```powershell
git add studio-android/LightNovelLibrary/app/build.gradle studio-android/LightNovelLibrary/app/src/phase3Harness studio-android/LightNovelLibrary/app/src/androidTest/java/org/mewx/wenku8/harness tools/phase3-device-harness.ps1
git diff --check --cached
git commit -m "test: add phase 3 process recovery harness"
```

Expected: `build/reports/phase3` remains untracked and contains no account/content data.

### Task 20: Run Backup Restore And The API 23-36 Phase Exit Matrix

**Depends on:** Tasks 12 and 19.

**Files:**
- Create: `tools/phase3-backup-harness.ps1`
- Create: `tools/verify-phase3-matrix.ps1`
- Create: `verification-tools/src/main/kotlin/org/mewx/wenku8/verification/phase3/Phase3MatrixVerifier.kt`
- Create: `verification-tools/src/test/kotlin/org/mewx/wenku8/verification/phase3/Phase3MatrixVerifierTest.kt`
- Modify: `verification-tools/build.gradle`
- Create: `.github/workflows/phase-3-device-matrix.yml`
- Modify: `docs/verification/modernization-matrix.yaml`
- Modify: `docs/verification/phase-3-storage-contract.yaml`
- Test/evidence: `build/reports/phase3/` and CI retained artifacts.

- [ ] **Step 1: Write failing structured matrix/evidence tests**

`Phase3MatrixVerifierTest` uses real YAML/JSON parsers and fixed temporary Git repositories. It independently rejects: a missing required ID/field; duplicate ID; non-concrete path; hash with correct shape but wrong content; nonexistent Gradle task; wildcard/nonexistent test ID; missing JUnit/source test evidence; missing commit; report/artifact claiming another commit; tracked or untracked production/manifest/resource/Gradle input changed after the claimed commit; a post-source commit outside the evidence/workflow allowlist; missing report file; extra/unhashed report file; path traversal/symlink escape; stale report; and fabricated evidence-manifest values. Its positive fixture contains APIs exactly `23,29,30,31,32,33,34,35,36` and the exact retry/terminal thresholds.

Run:

```powershell
.\gradlew.bat :verification-tools:test --tests "org.mewx.wenku8.verification.phase3.Phase3MatrixVerifierTest" --console=plain --stacktrace --no-parallel
```

Expected: FAIL because `Phase3MatrixVerifier` does not exist.

- [ ] **Step 2: Implement structured command, test, commit, and evidence validation**

`Phase3MatrixVerifier` parses `modernization-matrix.yaml`, `phase-3-storage-contract.yaml`, Gradle `tasks --all` output, test source index, optional JUnit XML, and every report's `evidence-manifest.json` into typed models. It emits normalized JSON for the PowerShell hash layer. Every Phase 3 row requires exactly these concrete scalar fields: `id`, `provider`, `variant`, `command`, `test_id`, `fixture_path`, `fixture_sha256`, `artifact_path`, `artifact_sha256`, `report_path`, `report_sha256`, and `commit_sha`; device rows additionally require exact API/scenario/threshold fields. It rejects aliases, globs, explanatory/zero hashes, unknown fields, duplicate paths/IDs, and paths outside the repository.

Gradle commands are tokenized, not substring-matched. Every task token must equal a task name captured from fresh `.\gradlew.bat tasks --all`; PowerShell/script commands must equal a command declared by stable ID in `phase-3-storage-contract.yaml`, and their script path must exist. Every `test_id` is a concrete `fully.qualified.Class#method`, discovered either by the Phase 0 lexical source index or a JUnit XML `<testcase classname/name>`; class wildcards and command-only evidence fail. The evidence manifest must repeat the row ID, exact command/test ID, `commit_sha`, relative artifact/report paths, and hashes.

Verify `commit_sha` exists, is the evidence manifest's source/artifact commit, and is an ancestor of `HEAD`. Diff production source, manifests, Gradle inputs, and resources against that commit and reject any change. Commits and staged/unstaged files after it are allowed only under `docs/verification/modernization-matrix.yaml`, `docs/verification/phase-3-storage-contract.yaml`, and `.github/workflows/phase-3-device-matrix.yml`; verification tooling/harness changes must already be in `commit_sha`. This makes stale reports and hashes copied from another implementation commit fail.

Register these exact tasks in `verification-tools/build.gradle`:

```groovy
tasks.register('verifyPhase3MatrixStructure', JavaExec) {
    dependsOn test
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'org.mewx.wenku8.verification.phase3.Phase3MatrixVerifierKt'
    args '--matrix', file('../../docs/verification/modernization-matrix.yaml'),
         '--contract', file('../../docs/verification/phase-3-storage-contract.yaml')
}

tasks.register('phase3Gate') {
    dependsOn 'verifyPhase3WriterRouting', 'verifyPhase3MatrixStructure'
    dependsOn ':core:storage:testDebugUnitTest', ':core:network:test',
              ':core:data:testDebugUnitTest', ':app:testAlphaDebugUnitTest',
              ':app:lintAlphaDebug', ':app:assembleAlphaDebug'
}
```

The registered aggregate name is exactly `:verification-tools:phase3Gate`; do not create an app-owned Phase 3 gate alias.

- [ ] **Step 3: Add the recomputing PowerShell evidence verifier**

Create:

```powershell
# tools/verify-phase3-matrix.ps1
[CmdletBinding()]
param([switch]$RequireReports)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$matrixPath = Join-Path $repo 'docs\verification\modernization-matrix.yaml'
if (-not (Test-Path -LiteralPath $matrixPath -PathType Leaf)) { throw 'PHASE3-MATRIX-MISSING: modernization-matrix.yaml' }
$android = Join-Path $repo 'studio-android\LightNovelLibrary'
$work = Join-Path $repo 'build\reports\phase3\matrix-verifier'
New-Item -ItemType Directory -Force -Path $work | Out-Null
$taskIndex = Join-Path $work 'gradle-tasks.txt'
$normalizedPath = Join-Path $work 'normalized-matrix.json'

function Get-Sha256([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { throw "PHASE3-MATRIX-FILE-MISSING: $Path" }
    return (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
}

function Get-ReportMerkle([string]$Directory) {
    if (-not (Test-Path -LiteralPath $Directory -PathType Container)) { throw "PHASE3-MATRIX-REPORT-MISSING: $Directory" }
    $root = (Resolve-Path -LiteralPath $Directory).Path
    $selfManifest = [IO.Path]::GetFullPath((Join-Path $root 'evidence-manifest.json'))
    $lines = foreach ($file in (Get-ChildItem -LiteralPath $root -Recurse -File | Where-Object { [IO.Path]::GetFullPath($_.FullName) -ne $selfManifest } | Sort-Object FullName)) {
        $relative = [IO.Path]::GetRelativePath($root, $file.FullName).Replace('\', '/')
        if ($relative.StartsWith('../') -or $file.Attributes.HasFlag([IO.FileAttributes]::ReparsePoint)) { throw "PHASE3-MATRIX-REPORT-ESCAPE: $relative" }
        "$relative`0$(Get-Sha256 $file.FullName)"
    }
    if (@($lines).Count -eq 0) { throw "PHASE3-MATRIX-REPORT-EMPTY: $Directory" }
    $bytes = [Text.Encoding]::UTF8.GetBytes((@($lines) -join "`n") + "`n")
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return ([Convert]::ToHexString($sha.ComputeHash($bytes))).ToLowerInvariant() } finally { $sha.Dispose() }
}

Push-Location $android
try {
    $tasksOutput = & .\gradlew.bat tasks --all --console=plain --stacktrace --no-parallel 2>&1
    if ($LASTEXITCODE -ne 0) { throw "PHASE3-MATRIX: Gradle task inventory failed`n$tasksOutput" }
    $tasksOutput | Set-Content -LiteralPath $taskIndex -Encoding utf8NoBOM
    & .\gradlew.bat :verification-tools:verifyPhase3MatrixStructure -Pphase3TaskIndex=$taskIndex -Pphase3NormalizedOutput=$normalizedPath -Pphase3RequireReports=$RequireReports --console=plain --stacktrace --no-parallel
    if ($LASTEXITCODE -ne 0) { throw 'PHASE3-MATRIX: structured verifier failed' }
} finally {
    Pop-Location
}

$rows = @(Get-Content -Raw -LiteralPath $normalizedPath | ConvertFrom-Json)
foreach ($row in $rows) {
    $fixture = Join-Path $repo $row.fixture_path
    $artifact = Join-Path $repo $row.artifact_path
    $report = Join-Path $repo $row.report_path
    if ((Get-Sha256 $fixture) -ne $row.fixture_sha256) { throw "PHASE3-MATRIX-FIXTURE-HASH-MISMATCH: $($row.id)" }
    $artifactHash = Get-Sha256 $artifact
    if ($artifactHash -ne $row.artifact_sha256) { throw "PHASE3-MATRIX-ARTIFACT-HASH-MISMATCH: $($row.id)" }
    if ($RequireReports) {
        $reportHash = Get-ReportMerkle $report
        if ($reportHash -ne $row.report_sha256) { throw "PHASE3-MATRIX-REPORT-HASH-MISMATCH: $($row.id)" }
        $evidence = Get-Content -Raw -LiteralPath (Join-Path $report 'evidence-manifest.json') | ConvertFrom-Json
        $reportHashExcludes = @($evidence.report_hash_excludes)
        if ($reportHashExcludes.Count -ne 1 -or $reportHashExcludes[0] -ne 'evidence-manifest.json') { throw "PHASE3-MATRIX-REPORT-HASH-POLICY-MISMATCH: $($row.id)" }
        if ($evidence.id -ne $row.id -or $evidence.source_commit -ne $row.commit_sha -or $evidence.commit_sha -ne $row.commit_sha -or $evidence.command -ne $row.command -or $evidence.test_id -ne $row.test_id -or $evidence.fixture_path -ne $row.fixture_path -or $evidence.fixture_sha256 -ne $row.fixture_sha256 -or $evidence.artifact_path -ne $row.artifact_path -or $evidence.artifact_sha256 -ne $artifactHash -or $evidence.report_path -ne $row.report_path -or $evidence.report_sha256 -ne $reportHash) {
            throw "PHASE3-MATRIX-EVIDENCE-BINDING-MISMATCH: $($row.id)"
        }
    }
    $commitObject = $row.commit_sha + '^{commit}'
    & git -C $repo cat-file -e $commitObject
    if ($LASTEXITCODE -ne 0) { throw "PHASE3-MATRIX-COMMIT-MISSING: $($row.id)" }
    & git -C $repo merge-base --is-ancestor $row.commit_sha HEAD
    if ($LASTEXITCODE -ne 0) { throw "PHASE3-MATRIX-COMMIT-NOT-ANCESTOR: $($row.id)" }
}

$productionPaths = @(
    'studio-android/LightNovelLibrary',
    'studio-android/LightNovelLibrary/app/src/main',
    'studio-android/LightNovelLibrary/core',
    'studio-android/LightNovelLibrary/api-stub/src/main',
    'studio-android/LightNovelLibrary/gradle',
    'studio-android/LightNovelLibrary/build.gradle',
    'studio-android/LightNovelLibrary/settings.gradle',
    'studio-android/LightNovelLibrary/gradle.properties',
    'studio-android/LightNovelLibrary/app/build.gradle'
)
$sourceCommit = @($rows.commit_sha | Sort-Object -Unique)
if ($sourceCommit.Count -ne 1) { throw 'PHASE3-MATRIX-SOURCE-COMMIT-MISMATCH' }
& git -C $repo diff --quiet $($sourceCommit[0]) -- @productionPaths
if ($LASTEXITCODE -ne 0) { throw 'PHASE3-MATRIX-PRODUCTION-CHANGED-SINCE-EVIDENCE' }
$untrackedProtected = @(& git -C $repo ls-files --others --exclude-standard -- @productionPaths)
if ($LASTEXITCODE -ne 0) { throw 'PHASE3-MATRIX-UNTRACKED-SCAN-FAILED' }
if ($untrackedProtected.Count -gt 0) { throw "PHASE3-MATRIX-UNTRACKED-PROTECTED-SOURCE: $($untrackedProtected -join ',')" }
$allowedAfterSource = @('docs/verification/modernization-matrix.yaml', 'docs/verification/phase-3-storage-contract.yaml', '.github/workflows/phase-3-device-matrix.yml')
$changed = @(& git -C $repo diff --name-only $($sourceCommit[0]) --; & git -C $repo diff --cached --name-only; & git -C $repo diff --name-only) | Sort-Object -Unique
$forbidden = @($changed | Where-Object { $_ -and $_ -notin $allowedAfterSource })
if ($forbidden.Count -gt 0) { throw "PHASE3-MATRIX-POST-SOURCE-CHANGE: $($forbidden -join ',')" }
Write-Host 'PHASE3-MATRIX-PASS'
```

Run before adding rows:

```powershell
& .\tools\verify-phase3-matrix.ps1 -RequireReports
```

Expected: the structured verifier names the missing Phase 3 IDs before any hash layer can report PASS.

- [ ] **Step 4: Add the emulator backup/restore driver**

Create an emulator-only script with the same QEMU, named-AVD, API, clean-install, build, and instrumentation guards as Task 19. It performs this exact sequence on API 33 and API 36:

1. build/install `alphaPhase3Harness` and its test APK;
2. invoke Stage A with checkpoint `POST_CANONICAL_PRE_PROJECTION` and write/pull `pre.json`;
3. select an emulator-local backup transport returned by `adb shell bmgr list transports`; fail if none contains `LocalTransport`;
4. run `bmgr enable true`, `bmgr transport <exact-returned-component>`, and `bmgr backupnow org.mewx.wenku8`;
5. save backup command output and `bmgr list sets`; require a nonzero restore token;
6. run `pm clear org.mewx.wenku8` and prove canonical/transient/session files are absent;
7. run `bmgr restore <token> org.mewx.wenku8`, wait for completion, and launch the app;
8. invoke Stage B backup verification in a separate instrumentation process;
9. assert user DB/DataStore restored, transient/catalog/session/credential stores did not restore, SessionStore is signed out, canonical/projection `9/8` was repaired to `9/9`, and no non-secret legacy source was deleted;
10. retain pre/post/bmgr/physical-path reports and SHA-256 values under `build/reports/phase3/<api>/backup-restore/`.

Use argument arrays for every ADB invocation and never interpolate shell fragments. The script rejects a physical device before `uninstall`, `pm clear`, `bmgr`, or restore.

Implement the script completely as follows:

```powershell
# tools/phase3-backup-harness.ps1
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet(33, 36)]
    [int]$Api,
    [string]$Serial = $env:ANDROID_SERIAL
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$android = Join-Path $repo 'studio-android\LightNovelLibrary'
$sdkRoot = if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_SDK_ROOT)) { $env:ANDROID_SDK_ROOT } else { $env:ANDROID_HOME }
if ([string]::IsNullOrWhiteSpace($sdkRoot)) { throw 'PHASE3-BACKUP: Android SDK environment is not set' }
$adbName = if ([System.IO.Path]::DirectorySeparatorChar -eq '\') { 'adb.exe' } else { 'adb' }
$adb = Join-Path $sdkRoot "platform-tools/$adbName"
if (-not (Test-Path -LiteralPath $adb)) { throw 'PHASE3-BACKUP: adb executable not found' }

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)
    $prefix = @()
    if (-not [string]::IsNullOrWhiteSpace($Serial)) { $prefix = @('-s', $Serial) }
    $output = & $adb @prefix @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "PHASE3-BACKUP adb failed: $($Arguments -join ' ')`n$output" }
    return ($output -join "`n").Trim()
}

function Get-Sha256([string]$Path) {
    return (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
}

function Get-ReportMerkle([string]$Directory) {
    $root = (Resolve-Path -LiteralPath $Directory).Path
    $selfManifest = [IO.Path]::GetFullPath((Join-Path $root 'evidence-manifest.json'))
    $lines = foreach ($file in (Get-ChildItem -LiteralPath $root -Recurse -File | Where-Object { [IO.Path]::GetFullPath($_.FullName) -ne $selfManifest } | Sort-Object FullName)) {
        if ($file.Attributes.HasFlag([IO.FileAttributes]::ReparsePoint)) { throw "PHASE3-BACKUP: report symlink forbidden $($file.FullName)" }
        $relative = [IO.Path]::GetRelativePath($root, $file.FullName).Replace('\', '/')
        "$relative`0$(Get-Sha256 $file.FullName)"
    }
    if (@($lines).Count -eq 0) { throw 'PHASE3-BACKUP: empty evidence report' }
    $bytes = [Text.Encoding]::UTF8.GetBytes((@($lines) -join "`n") + "`n")
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return (($sha.ComputeHash($bytes) | ForEach-Object { $_.ToString('x2') }) -join '') } finally { $sha.Dispose() }
}

if ([string]::IsNullOrWhiteSpace($Serial)) {
    $devices = @(& $adb devices | Select-String '\tdevice$' | ForEach-Object { ($_.Line -split '\s+')[0] })
    if ($devices.Count -ne 1) { throw "PHASE3-BACKUP: expected one disposable emulator, found $($devices.Count)" }
    $Serial = $devices[0]
}

$qemu = Invoke-Adb shell getprop ro.kernel.qemu
$avd = Invoke-Adb shell getprop ro.boot.qemu.avd_name
$actualApi = [int](Invoke-Adb shell getprop ro.build.version.sdk)
if ($qemu -ne '1' -or [string]::IsNullOrWhiteSpace($avd)) { throw 'PHASE3-BACKUP: named disposable emulator required' }
if ($actualApi -ne $Api) { throw "PHASE3-BACKUP: requested API $Api but device is API $actualApi" }

$package = 'org.mewx.wenku8'
$runner = 'org.mewx.wenku8.test/androidx.test.runner.AndroidJUnitRunner'
$reportBase = [IO.Path]::GetFullPath((Join-Path $repo 'build\reports\phase3'))
$reportRoot = [IO.Path]::GetFullPath((Join-Path $reportBase "$Api\backup-restore"))
if (-not $reportRoot.StartsWith($reportBase + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'PHASE3-BACKUP: report path escaped root' }
if (Test-Path -LiteralPath $reportRoot) { Remove-Item -LiteralPath $reportRoot -Recurse -Force }
New-Item -ItemType Directory -Force -Path $reportRoot | Out-Null

Push-Location $android
try {
    & .\gradlew.bat -Pwenku8Provider=public -Pwenku8TestBuildType=phase3Harness :app:assembleAlphaPhase3Harness :app:assembleAlphaPhase3HarnessAndroidTest --console=plain --stacktrace --no-parallel
    if ($LASTEXITCODE -ne 0) { throw 'PHASE3-BACKUP: Gradle build failed' }
} finally {
    Pop-Location
}

$appApk = Join-Path $android 'app\build\outputs\apk\alpha\phase3Harness\app-alpha-phase3Harness.apk'
$testApk = Join-Path $android 'app\build\outputs\apk\androidTest\alpha\phase3Harness\app-alpha-phase3Harness-androidTest.apk'
if (-not (Test-Path -LiteralPath $appApk) -or -not (Test-Path -LiteralPath $testApk)) { throw 'PHASE3-BACKUP: harness APK missing' }

$stageAProcess = $null
$stageAStdout = Join-Path $reportRoot 'stage-a-stdout.txt'
$stageAStderr = Join-Path $reportRoot 'stage-a-stderr.txt'
$stageAWasKilledByCleanup = $false
try {
    try { Invoke-Adb uninstall $package | Out-Null } catch { }
    Invoke-Adb install $appApk | Out-Null
    Invoke-Adb install $testApk | Out-Null

    $stageAArguments = @('-s', $Serial, 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class', 'org.mewx.wenku8.harness.Phase3StageATest#seedPostCanonicalForBackup', '-e', 'scenario', 'backup-restore', '-e', 'checkpoint', 'POST_CANONICAL_PRE_PROJECTION', $runner)
    $start = @{
        FilePath = $adb
        ArgumentList = $stageAArguments
        RedirectStandardOutput = $stageAStdout
        RedirectStandardError = $stageAStderr
        PassThru = $true
    }
    if ([System.IO.Path]::DirectorySeparatorChar -eq '\') { $start.WindowStyle = 'Hidden' }
    $stageAProcess = Start-Process @start

    $preDeadline = [DateTimeOffset]::UtcNow.AddMinutes(3)
    $preText = $null
    $pre = $null
    do {
        try {
            $candidate = Invoke-Adb exec-out run-as $package cat files/phase3-harness/pre.json
            $candidateJson = $candidate | ConvertFrom-Json
            $preText = $candidate
            $pre = $candidateJson
        } catch { }
        if ($null -ne $pre) { break }
        if ($stageAProcess.HasExited) { throw 'PHASE3-BACKUP: Stage A exited before durable pre.json' }
        if ([DateTimeOffset]::UtcNow -gt $preDeadline) { throw 'PHASE3-BACKUP: Stage A checkpoint timeout' }
        Start-Sleep -Milliseconds 500
    } while ($true)

    $prePath = Join-Path $reportRoot 'pre.json'
    $preText | Set-Content -LiteralPath $prePath -Encoding utf8NoBOM
    if ($pre.scenario -ne 'backup-restore' -or $pre.checkpoint -ne 'POST_CANONICAL_PRE_PROJECTION' -or $pre.mutationVersion -ne 9 -or $pre.legacyProjectionVersion -ne 8) {
        throw 'PHASE3-BACKUP: wrong capture boundary'
    }

    $transportOutput = Invoke-Adb shell bmgr list transports
    $transportOutput | Set-Content -LiteralPath (Join-Path $reportRoot 'bmgr-transports.txt') -Encoding utf8
    $transportLine = @($transportOutput -split "`n" | Where-Object { $_ -match 'LocalTransport' } | Select-Object -First 1)
    if ($transportLine.Count -ne 1) { throw 'PHASE3-BACKUP: emulator LocalTransport unavailable' }
    $transport = ($transportLine[0] -replace '^\s*\*?\s*', '').Trim()

    Invoke-Adb shell bmgr enable true | Out-Null
    $selected = Invoke-Adb shell bmgr transport $transport
    $selected | Set-Content -LiteralPath (Join-Path $reportRoot 'bmgr-selected-transport.txt') -Encoding utf8
    $backup = Invoke-Adb shell bmgr backupnow $package
    $backup | Set-Content -LiteralPath (Join-Path $reportRoot 'bmgr-backup.txt') -Encoding utf8
    if ($backup -notmatch '(?i)success') { throw "PHASE3-BACKUP: backup did not report success`n$backup" }

    $sets = Invoke-Adb shell bmgr list sets
    $sets | Set-Content -LiteralPath (Join-Path $reportRoot 'bmgr-sets.txt') -Encoding utf8
    $tokenMatch = [regex]::Match($sets, '(?m)^\s*([0-9a-fA-F]+)\s*:')
    if (-not $tokenMatch.Success) { throw 'PHASE3-BACKUP: restore token unavailable' }
    $token = $tokenMatch.Groups[1].Value

    Invoke-Adb shell pm clear $package | Out-Null
    if (-not $stageAProcess.WaitForExit(45000)) { throw 'PHASE3-BACKUP: Stage A did not exit after pm clear' }
    $cleared = Invoke-Adb shell run-as $package ls -R .
    $cleared | Set-Content -LiteralPath (Join-Path $reportRoot 'post-clear-paths.txt') -Encoding utf8
    if ($cleared -match 'wenku8-user|migration-transient|app_settings|session') { throw 'PHASE3-BACKUP: app data survived pm clear' }

    $restore = Invoke-Adb shell bmgr restore $token $package
    $restore | Set-Content -LiteralPath (Join-Path $reportRoot 'bmgr-restore.txt') -Encoding utf8
    if ($restore -notmatch '(?i)(finished|success|restoreStarting)') { throw "PHASE3-BACKUP: restore did not start successfully`n$restore" }
    Invoke-Adb shell am start -W -n "$package/org.mewx.wenku8.activity.MainActivity" | Out-Null

    $stageB = Invoke-Adb shell am instrument -w -r -e class 'org.mewx.wenku8.harness.Phase3StageBTest#verifyAfterBackupRestore' -e scenario backup-restore $runner
    $stageB | Set-Content -LiteralPath (Join-Path $reportRoot 'stage-b-instrumentation.txt') -Encoding utf8
    if (-not $stageB.Contains('OK (1 test)')) { throw "PHASE3-BACKUP: Stage B failed`n$stageB" }
    $postText = Invoke-Adb exec-out run-as $package cat files/phase3-harness/post.json
    $postPath = Join-Path $reportRoot 'post.json'
    $postText | Set-Content -LiteralPath $postPath -Encoding utf8NoBOM
    $post = Get-Content -Raw -LiteralPath $postPath | ConvertFrom-Json
    if ($post.mutationVersion -ne 9 -or $post.legacyProjectionVersion -ne 9) { throw 'PHASE3-BACKUP: projection version did not reconcile' }
    if (-not $post.sessionSignedOut) { throw 'PHASE3-BACKUP: restored process is not signed out' }
    if ($post.transientStoreRestored -or $post.catalogCacheRestored -or $post.credentialStoreRestored -or $post.snapshotStoreRestored) { throw 'PHASE3-BACKUP: excluded store restored' }
    if (-not $post.nonSecretLegacyPreserved) { throw 'PHASE3-BACKUP: non-secret legacy data was not preserved' }

    $stageAMetaPath = Join-Path $reportRoot 'stage-a-process.json'
    [ordered]@{
        pid = $stageAProcess.Id
        exitCode = $stageAProcess.ExitCode
        killedByCleanup = $false
    } | ConvertTo-Json | Set-Content -LiteralPath $stageAMetaPath -Encoding utf8NoBOM
    $evidenceFiles = Get-ChildItem -LiteralPath $reportRoot -File | Sort-Object Name
    $hashes = foreach ($file in $evidenceFiles) {
        $hash = Get-FileHash -Algorithm SHA256 -LiteralPath $file.FullName
        [ordered]@{ path = $file.Name; sha256 = $hash.Hash.ToLowerInvariant() }
    }
    $hashes | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $reportRoot 'hashes.json') -Encoding utf8NoBOM
    $sourceCommit = (& git -C $repo rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or $sourceCommit -notmatch '^[0-9a-f]{40}$') { throw 'PHASE3-BACKUP: source commit unavailable' }
    $fixture = Join-Path $repo 'docs\verification\phase-3-storage-contract.yaml'
    $fixtureRelative = [IO.Path]::GetRelativePath($repo, $fixture).Replace('\', '/')
    $artifactRelative = [IO.Path]::GetRelativePath($repo, $appApk).Replace('\', '/')
    $reportRelative = [IO.Path]::GetRelativePath($repo, $reportRoot).Replace('\', '/')
    $reportHash = Get-ReportMerkle $reportRoot
    [ordered]@{
        schema = 'wenku8-phase3-evidence/v1'
        id = 'P3-BACKUP-RESTORE'
        command = "pwsh -NoProfile -File tools/phase3-backup-harness.ps1 -Api $Api"
        test_id = 'org.mewx.wenku8.harness.Phase3StageBTest#verifyAfterBackupRestore'
        source_commit = $sourceCommit
        commit_sha = $sourceCommit
        fixture_path = $fixtureRelative
        fixture_sha256 = Get-Sha256 $fixture
        artifact_path = $artifactRelative
        artifact_sha256 = Get-Sha256 $appApk
        report_path = $reportRelative
        report_sha256 = $reportHash
        report_hash_excludes = @('evidence-manifest.json')
        api = $Api
        scenario = 'backup-restore'
    } | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $reportRoot 'evidence-manifest.json') -Encoding utf8NoBOM
    if ((Get-ReportMerkle $reportRoot) -ne $reportHash) { throw 'PHASE3-BACKUP: report Merkle changed after manifest write' }
    Write-Host "PHASE3-BACKUP-PASS api=$Api report=$reportRoot"
} finally {
    if ($null -ne $stageAProcess) {
        if (-not $stageAProcess.HasExited) {
            Stop-Process -Id $stageAProcess.Id -Force -ErrorAction SilentlyContinue
            $stageAWasKilledByCleanup = $true
        }
        $stageAProcess.WaitForExit()
        $stageAMetaPath = Join-Path $reportRoot 'stage-a-process.json'
        if (-not (Test-Path -LiteralPath $stageAMetaPath)) {
            [ordered]@{
                pid = $stageAProcess.Id
                exitCode = $stageAProcess.ExitCode
                killedByCleanup = $stageAWasKilledByCleanup
            } | ConvertTo-Json | Set-Content -LiteralPath $stageAMetaPath -Encoding utf8NoBOM
        }
    }
    try { Invoke-Adb shell am force-stop $package | Out-Null } catch { }
}
```

- [ ] **Step 5: Add the complete pinned CI workflow**

Create:

```yaml
name: Phase 3 storage device matrix

on:
  pull_request:
    branches: [master]
  schedule:
    - cron: '17 18 * * *'
  workflow_dispatch:

permissions:
  contents: read

concurrency:
  group: phase3-${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  pull-request-smoke:
    if: github.event_name == 'pull_request'
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        api: [31, 34]
    steps:
      - uses: actions/checkout@34e114876b0b11c390a56381ad16ebd13914f8d5 # v4
      - uses: actions/setup-java@c1e323688fd81a25caa38c78aa6df2d33d3e20d9 # v4
        with:
          distribution: temurin
          java-version: '21'
          cache: gradle
      - name: API ${{ matrix.api }} scheduler smoke
        uses: ReactiveCircus/android-emulator-runner@4c44018e59b437e86cdfc41da381398f93ed8808 # v2
        with:
          api-level: ${{ matrix.api }}
          arch: x86_64
          profile: pixel_2
          emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim
          disable-animations: true
          script: pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api ${{ matrix.api }} -Scenario scheduler-run
      - name: Retain smoke evidence
        if: always()
        uses: actions/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02 # v4
        with:
          name: phase3-pr-api-${{ matrix.api }}
          path: build/reports/phase3/${{ matrix.api }}/scheduler-run/
          if-no-files-found: error

  nightly-device-matrix:
    if: github.event_name != 'pull_request'
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        include:
          - { api: 23, scenario: scheduler-run }
          - { api: 29, scenario: process-kill }
          - { api: 30, scenario: scheduler-run }
          - { api: 31, scenario: scheduler-stop }
          - { api: 32, scenario: scheduler-run }
          - { api: 33, scenario: force-stop }
          - { api: 34, scenario: scheduler-run }
          - { api: 35, scenario: scheduler-timeout }
          - { api: 36, scenario: reboot }
    steps:
      - uses: actions/checkout@34e114876b0b11c390a56381ad16ebd13914f8d5 # v4
      - uses: actions/setup-java@c1e323688fd81a25caa38c78aa6df2d33d3e20d9 # v4
        with:
          distribution: temurin
          java-version: '21'
          cache: gradle
      - name: API ${{ matrix.api }} ${{ matrix.scenario }}
        uses: ReactiveCircus/android-emulator-runner@4c44018e59b437e86cdfc41da381398f93ed8808 # v2
        with:
          api-level: ${{ matrix.api }}
          arch: x86_64
          profile: pixel_2
          emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim
          disable-animations: true
          script: pwsh -NoProfile -File tools/phase3-device-harness.ps1 -Api ${{ matrix.api }} -Scenario ${{ matrix.scenario }}
      - name: Retain device evidence
        if: always()
        uses: actions/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02 # v4
        with:
          name: phase3-api-${{ matrix.api }}-${{ matrix.scenario }}
          path: build/reports/phase3/${{ matrix.api }}/${{ matrix.scenario }}/
          if-no-files-found: error
          retention-days: 30

  backup-restore:
    if: github.event_name != 'pull_request'
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        api: [33, 36]
    steps:
      - uses: actions/checkout@34e114876b0b11c390a56381ad16ebd13914f8d5 # v4
      - uses: actions/setup-java@c1e323688fd81a25caa38c78aa6df2d33d3e20d9 # v4
        with:
          distribution: temurin
          java-version: '21'
          cache: gradle
      - name: API ${{ matrix.api }} backup restore
        uses: ReactiveCircus/android-emulator-runner@4c44018e59b437e86cdfc41da381398f93ed8808 # v2
        with:
          api-level: ${{ matrix.api }}
          arch: x86_64
          profile: pixel_2
          emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim
          disable-animations: true
          script: pwsh -NoProfile -File tools/phase3-backup-harness.ps1 -Api ${{ matrix.api }}
      - name: Retain backup evidence
        if: always()
        uses: actions/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02 # v4
        with:
          name: phase3-backup-api-${{ matrix.api }}
          path: build/reports/phase3/${{ matrix.api }}/backup-restore/
          if-no-files-found: error
          retention-days: 30
```

Phase 0 dependency/action provenance must approve these exact action commits before merge; a tag-only replacement is rejected.

- [ ] **Step 6: Verify and commit the complete evidence tooling before generating evidence**

Run the writer/matrix verifier tests, parse every embedded PowerShell script, and run the non-destructive API 34 smoke. Then commit the Task 20 verifier, backup harness, workflow, and contract schema while the production tree is clean. Record this resulting commit as `$sourceCommit`; every later matrix row and `evidence-manifest.json` must bind to exactly it.

```powershell
git add tools/phase3-backup-harness.ps1 tools/verify-phase3-matrix.ps1 .github/workflows/phase-3-device-matrix.yml studio-android/LightNovelLibrary/verification-tools/build.gradle studio-android/LightNovelLibrary/verification-tools/src/main/kotlin/org/mewx/wenku8/verification/phase3/Phase3MatrixVerifier.kt studio-android/LightNovelLibrary/verification-tools/src/test/kotlin/org/mewx/wenku8/verification/phase3/Phase3MatrixVerifierTest.kt docs/verification/phase-3-storage-contract.yaml
git diff --check --cached
git commit -m "test: add phase 3 evidence verifier"
$sourceCommit = git rev-parse HEAD
```

Expected: the commit succeeds only after structured negative tests pass; `git status --short` contains no production/tooling change, and `$sourceCommit` is one lowercase 40-hex commit.

- [ ] **Step 7: Generate backup/device evidence at the clean source commit**

Run against matching disposable emulators:

```powershell
pwsh -NoProfile -File tools/phase3-backup-harness.ps1 -Api 33
pwsh -NoProfile -File tools/phase3-backup-harness.ps1 -Api 36
```

Expected: `PHASE3-BACKUP-PASS` twice; reports prove the post-canonical/pre-projection capture point and signed-out restore. Each harness writes `evidence-manifest.json` with the exact `$sourceCommit`, command, concrete test ID, artifact relative path/hash, and report Merkle hash; it never uses the later evidence-manifest commit.

- [ ] **Step 8: Add exact Phase 3 modernization-matrix rows**

Add stable IDs `P3-STORE-USER`, `P3-STORE-CACHE`, `P3-STORE-TRANSIENT`, `P3-LEGACY-GOLDEN`, `P3-JOURNAL-CRASH`, `P3-WRITER-BARRIER`, `P3-SETTINGS-INTEGRATION`, `P3-SESSION-INTEGRATION`, `P3-BACKUP-RESTORE`, `P3-BOUNDED-WORK`, `P3-FOREGROUND-23-33`, `P3-DENIAL-31-33`, `P3-UIDT-34-36`, `P3-NOTIFICATION-DENIAL`, `P3-SAFE-RETRY`, `P3-HOST-STOP`, `P3-HOST-KILL`, `P3-HOST-FORCE-STOP`, `P3-HOST-REBOOT`, `P3-API-MATRIX`, and `P3-NON-DELETION`.

Each row records the exact Gradle/test or PowerShell command from Tasks 1-20, a concrete `Class#method` test ID, provider `public`, flavor/build type, API(s), `fixture_path` plus actual computed `fixture_sha256`, `artifact_path` plus actual computed `artifact_sha256`, threshold (`terminalCommitCount=1`, `duplicateRequestCount=0`, allowed retry enum set), concrete `report_path` plus deterministic directory-Merkle `report_sha256`, and the exact `$sourceCommit` as `commit_sha`. Copy values only from the freshly recomputed normalized verifier output/evidence manifest; never enter an explanatory, zero, guessed, or manually reused hash.

- [ ] **Step 9: Run the complete deterministic local Phase 3 gate**

Run from `studio-android/LightNovelLibrary`:

```powershell
.\gradlew.bat -Pwenku8Provider=public :core:storage:testDebugUnitTest :core:network:test :core:data:testDebugUnitTest :app:testAlphaDebugUnitTest :app:testBaiduDebugUnitTest :app:testPlaystoreDebugUnitTest :app:lintAlphaDebug :app:assembleAlphaDebug :app:assembleAlphaRelease --console=plain --stacktrace --no-parallel
```

Expected: `BUILD SUCCESSFUL`; storage codec/migration coverage is at least 90% line/80% branch; no module hides below its threshold.

- [ ] **Step 10: Run exact public variant/manifest and compatibility gates**

Run:

```powershell
.\gradlew.bat -Pwenku8Provider=public :app:processAlphaDebugMainManifest :app:processBaiduDebugMainManifest :app:processPlaystoreDebugMainManifest phase0Gate verifyPhase0Coverage :verification-tools:phase1Gate :verification-tools:phase2Gate :verification-tools:phase3Gate --console=plain --stacktrace --no-parallel
```

Expected: Phase 0-3 gates pass; old-signed/minified Intent/Serializable, settings, signed-out SessionStore, merged manifests, legacy golden bytes, backup rules, and non-deletion remain green.

- [ ] **Step 11: Run every API/device journey and retain reports**

Run the Task 19 harness for all nine matrix rows, the five destructive scenarios, and Task 20 backup restore. Then run:

```powershell
Set-Location 'D:\Projects\10_Active\light-novel-library_Wenku8_Android'
& .\tools\verify-phase3-contract.ps1
& .\tools\verify-phase3-writers.ps1
& .\tools\verify-phase3-matrix.ps1 -RequireReports
git diff --check
```

Expected: all three verifiers print PASS, all API values 23/29/30/31/32/33/34/35/36 are present, and `git diff --check` is silent.

- [ ] **Step 12: Perform credential/privacy/non-deletion scans**

Run the Phase 0 secret/redaction/egress verifier over source, Gradle outputs, APKs, screenshots, reports, WorkManager DB dump, JobScheduler dump, and notifications. Run the writer verifier with migration simulation. Expected: no credential/Cookie/captcha/raw body/private endpoint, no unauthorized host, and no migration-caused deletion of any non-secret legacy artifact. The Phase 2 auditable password-only `cert.wk8` scrub remains the sole exception and leaves rollback signed out.

- [ ] **Step 13: Commit only the authoritative evidence mappings**

Run from the repository root:

```powershell
git add docs/verification/modernization-matrix.yaml docs/verification/phase-3-storage-contract.yaml
git diff --check --cached
git commit -m "test: gate phase 3 storage migration"
```

Expected: only evidence manifests are committed after `$sourceCommit`; generated device reports remain retained CI/local evidence, not source fixtures. Re-run the matrix verifier after the commit and require it to accept the evidence-only descendant.

- [ ] **Step 14: Run independent Phase 3 reviews before declaring the exit gate**

Dispatch separate reviewers for: (1) Room/backup/migration safety, (2) WorkManager/foreground/UIDT lifecycle and Android API contracts, and (3) compatibility/testing/evidence completeness. Resolve every Critical and Important finding, rerun the affected narrow and full gates, and append the reviewed commit SHA to the matrix. Phase 4 may not begin until all three reviews pass.

## Phase 3 Completion Checklist

The reviewed coverage map is: Sections 6.2-6.4 -> Tasks 5, 9, 10, 12; Sections 7.3-7.4 -> Tasks 9, 11, 18; Sections 9.1-9.2 -> Tasks 2-4; Section 9.3 -> Tasks 5, 8-10; Sections 9.4-9.5 -> Tasks 6-10; Section 9.6 -> Tasks 4, 12, 20; Section 9.7 -> Tasks 11, 13-20; Phase 3 deliverables/exit gate -> Tasks 1 and 20; Sections 12.1-12.2 -> focused tests in every task plus Task 20 coverage; Sections 13.1-13.2 -> Tasks 18-20; Sections 14.2/14.4 -> Tasks 8, 10, 12, 19; Section 16.2/16.3 durable-data rows -> Task 20 evidence. Phase 3 does not claim later-phase Compose route or legacy-retirement completion.

- [ ] Canonical Room covers bookshelf, search history, reader progress, downloads, versions, and applied mutations.
- [ ] Excluded Room covers normalized novel metadata, ordered volumes/chapters, parser revision, TTL, language, account partition, and session epoch cache metadata.
- [ ] The existing Phase 1 DataStore/settings migration and Phase 2 encrypted SessionStore/credential migration are reused exactly once.
- [ ] `LegacySaveCodec`, `LegacyPathPolicy`, `LegacyIntentCodec`, settings, progress, bookshelf, and credential adapters are registered and golden-tested.
- [ ] Every migration chunk and reconstructed process imports only hash/length-verified bytes from the same sealed immutable `noBackupFilesDir` snapshot; live legacy changes cannot alter it, and snapshot import uses insert-if-absent rather than `REPLACE`.
- [ ] `NotStarted`/`Snapshotting`/`Importing` writes are legacy-first with durable reservations and typed ordered deltas; every delta through the barrier-locked high-water mark replays before the compare-and-set to `DualWrite`, after which writes are canonical-first.
- [ ] Mutation versions are allocated only by transactional canonical Room `reserveMutationVersion`; version-guarded delete/clear/projection acknowledgement and snapshot-only `deleteSnapshotRows`/`deleteSnapshotMutations` APIs prevent newer or non-snapshot rows from being removed.
- [ ] The one-to-one Phase 0 writer routing manifest intercepts chapter XML, chapter images, novel covers, and every approved `saves/imgs`/cover-root writer with no `GlobalConfig` writer exemption.
- [ ] Range resume requires the exact quoted strong ETag, stored total representation length, offset, Content-Range, and Content-Length; absent/weak/changed validators force whole restart and mixed representations never commit.
- [ ] UIDT tests assert Android's exact `(downloadBytes, uploadBytes)` order for initial estimates, transferred bytes, and refreshed estimates on API 34-36.
- [ ] Both host harnesses launch Stage A asynchronously, poll durable `pre.json` before the host action, capture stdout/stderr/exit, run Stage B separately, and clean host/device test state in `finally`.
- [ ] Every device/backup `evidence-manifest.json` binds row ID, exact command, concrete test ID, source commit, fixture/artifact/report paths, recomputed content hashes, and a report Merkle that excludes only the root evidence manifest itself.
- [ ] The matrix gate rejects tracked changes and `git ls-files --others --exclude-standard` results in protected production, manifest, resource, and Gradle roots after the evidence source commit.
- [ ] The aggregate storage migration exit task is exactly `:verification-tools:phase3Gate`, with root `phase0Gate`/`verifyPhase0Coverage`, `:verification-tools:phase1Gate`, and `:verification-tools:phase2Gate` as the only preceding phase gate names.
- [ ] Every domain follows NotStarted through Complete with the exact writer barrier/snapshot/import/checkpoint/journal/canonical/projection/reconcile/version protocol.
- [ ] Canonical writes are exactly once by mutation ID; HTTP delivery is described only as safe at least once.
- [ ] Migration/checkpoint/journal and cache/session stores are physically excluded; canonical user/DataStore stores are whole-file backup units.
- [ ] Post-canonical/pre-projection backup restore repairs from canonical versions without a transient journal and starts signed out.
- [ ] WorkManager chunks are bounded, checkpointed, idempotent, cancellable, and resumable after process death.
- [ ] API 23-33 foreground work, API 31-33 denial/expedited fallback, and API 34-36 UIDT JobService lifecycle pass deterministic tests.
- [ ] Notification denial retains an in-app progress/cancel surface and platform-required disclosure.
- [ ] Host Stage A/B evidence covers jobscheduler stop/timeout/run, process kill, force-stop, reboot, and backup restore.
- [ ] API 23/29/30/31/32/33/34/35/36 gates and modernization-matrix hashes/reports pass.
- [ ] No migration deletes a non-secret legacy artifact; password-only `cert.wk8` scrub remains the audited Phase 2 exception.
- [ ] Coverage reaches 90% line/80% branch for storage codecs/migration and all higher-level Phase 3 thresholds.
- [ ] No Critical or Important independent finding remains.

## Execution Handoff

Plan complete at `docs/superpowers/plans/2026-07-10-wenku8-phase-3-storage-migration.md`. Execute only after Phase 0-2 gates and the preconditions above pass. Use `superpowers:subagent-driven-development`; assign a fresh implementation worker per task, then run specification review and code-quality review before accepting each commit. Keep device/backup harnesses on disposable emulators only.
