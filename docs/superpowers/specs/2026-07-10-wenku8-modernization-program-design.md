# Wenku8 Android Modernization Program Design

**Status:** Draft for independent review; product direction approved by the user on 2026-07-10

**Repository baseline:** `4da90cec8d1fb975c54ab50455c8886f34f44bd1`

**Active product:** `studio-android/LightNovelLibrary`

**Behavioral reference:** `niuhuan/wild@ead92b718ef6707aa3e2519f05b103aa2761185e`

## 1. Executive Summary

This program replaces the active Android application's legacy-centered architecture with a modular, coroutine-based, offline-capable Kotlin architecture and migrates every reachable user interface to native Jetpack Compose Material 3.

The migration uses a vertical-slice strangler strategy. New routes, repositories, public API sources, and storage adapters coexist with legacy code until each user journey reaches behavioral parity and passes compatibility, runtime, accessibility, and screenshot gates. A route becomes the default only after its replacement is proven. Legacy data is never destructively rewritten as the first migration action.

The public checkout must become a functional application rather than a build-only stub. It will provide independently authored implementations for anonymous browsing and reading, captcha login, session validation, account information, bookshelf operations, recommendations, review browsing, review creation, and replies. Daily check-in is capability-gated because the only currently verified Wild behavior sends it to a cleartext host with no valid HTTPS equivalent; the public UI must hide it until an independently verified HTTPS contract exists. Wild is a behavioral reference only. No Wild source, selector set, test, comment, error message, or distinctive parsing flow may be copied, translated, or mechanically rewritten.

The optional private `:api` provider remains supported. Public and private providers must implement the same typed contract and pass the same provider contract suite.

## 2. Approved Decisions

1. Use a vertical-slice strangler migration rather than a big-bang rewrite.
2. The final active UI runtime is Compose Material 3 for every reachable page.
3. XML Activities and Fragments may exist only as temporary compatibility entries during migration.
4. Use a single-activity Navigation Compose shell as the final navigation owner.
5. Preserve application identity, old Intent extras, Serializable boundary types, save semantics, cache directories, and optional private API compatibility.
6. Replace the public exception-throwing stub with a complete clean-room public provider.
7. Use ViewModel, immutable StateFlow state, structured coroutines, Room, DataStore, OkHttp, Jsoup, and WorkManager.
8. Start dependency injection with constructor injection and an explicit `AppContainer`; do not add a DI framework during the compatibility-heavy foundation phase.
9. Keep Modern Reader as the only final reader. Retire the V1 and vertical readers only after parity evidence exists.
10. Do not treat build success, a Material 3 theme parent, or Kotlin file counts as proof of architecture or UI completion.
11. This specification supersedes older direction documents wherever they permit retained XML product pages or a build-only public stub as a final state. Those documents remain useful historical records until phase plans update or archive them.
12. Treat current site authorization, content/distribution rights, repository licensing, third-party provenance, and privacy egress as fail-closed release prerequisites rather than assumptions inferred from public reachability or historical source publication.
13. Remove third-party advertising, analytics, automatic crash collection, and raw user/content logging in Phase 0; they are not part of the modernized product.

## 3. Current-State Baseline

The following audit facts define the starting point and must be refreshed at the beginning and end of the program:

| Area | Baseline |
| --- | --- |
| Active production source | 121 Kotlin files, 0 Java files, 16,909 physical lines |
| Active app shape | 15 manifest Activities, 6 Fragments, 34 XML layouts |
| Compose usage | One Compose Activity and five Material 3 UI files, concentrated in Modern Reader |
| Explicit Material XML usage | 7 of 33 referenced layouts contain a Material widget |
| Legacy UI elements | 12 old CardView elements, 6 AppCompat Toolbar elements, 0 XML MaterialToolbar elements |
| Legacy concurrency | 24 AsyncTask task definitions plus raw executors, threads, handlers, and timers |
| UI binding/global coupling | 197 `findViewById` mentions and 268 `GlobalConfig` mentions |
| Modern architecture primitives | No ViewModel, StateFlow, lifecycleScope, coroutine use, Room, DataStore, or DI |
| Largest hotspots | NovelInfoActivity 1,222 lines; GlobalConfig 725; V1 reader Activity 907 |
| Unit tests | 222 passing, 133 focused on Modern Reader |
| Instrumentation | 50 tests with two stale MyApp failures |
| Lint | 0 errors, 420 warnings, 1 hint |
| Public provider | `:api` maps to `api-stub`; normal reachable calls can throw `UnsupportedOperationException("stub")` |
| CI mismatch | minSdk 23, connected-test emulator API 21 |

The historical `eclipse-android` and `eclipse-android-old` projects are archives. They may be consulted to understand behavior but are never migration targets.

## 4. Goals

### 4.1 Product Goals

- Make the public checkout capable of the complete visible Wenku8 workflow.
- Let a user discover, search, inspect, save, download, and read a novel without a private provider.
- Support captcha-based login, account state, bookshelf synchronization, recommendations, review browsing, posting, and replies through the public provider.
- Expose daily check-in only when the selected provider advertises a verified HTTPS implementation; never send session material to a cleartext endpoint.
- Preserve offline reading, existing downloads, reading progress, settings, search history, and local bookshelf state across upgrades.
- Deliver one coherent, adaptive Material 3 application on phones, tablets, landscape windows, and resizable/foldable devices.
- Make loading, empty, offline, authentication, parsing failure, and recovery states first-class UI states.

### 4.2 Engineering Goals

- Establish one-way dependencies and testable interfaces.
- Remove network, parsing, file, and cache work from Activities, Fragments, adapters, and composables.
- Replace unstructured concurrency with structured coroutines and injected dispatchers.
- Replace global mutable configuration access with repositories and immutable observable state.
- Use typed failures instead of null, integer sentinels, or exception-only normal control flow.
- Provide deterministic parser, network, repository, storage, migration, ViewModel, Compose UI, and screenshot tests.
- Keep public and optional private providers behaviorally substitutable at the contract boundary.

## 5. Non-Goals

- Rewriting the archived Eclipse projects.
- Copying Wild code or relicensing this repository as part of the implementation.
- Adding hidden WebViews, Cloudflare bypasses, JavaScript injection, or private endpoints.
- Using cleartext HTTP for account or content operations.
- Persisting a user password.
- Automating captcha circumvention.
- Redesigning Wenku8 server behavior or inventing unsupported server features.
- Treating public web accessibility, the historical application, or a user account as permission to publish an API contract, automate the site, redistribute content, or ship through an application store.
- Deleting non-secret legacy save files during initial migration. Verified password scrubbing from `cert.wk8` after successful new authentication is the explicit exception defined in Sections 6.4 and 9.
- Preserving every implementation detail of the old UI when it conflicts with accessibility, recovery, or adaptive layout requirements.

## 6. Compatibility Invariants

### 6.1 Application and Android Boundaries

- Keep `applicationId` equal to `org.mewx.wenku8`.
- Keep launcher behavior and existing exported-component policy unless a security correction is separately documented and tested.
- Preserve old Activity class names as compatibility trampolines while an internal or external caller can still target them.
- Preserve back-stack semantics for existing user journeys during route rollout.
- Preserve current supported language identities and Simplified/Traditional Chinese selection behavior.

### 6.2 Intent Contracts

`LegacyIntentCodec` owns decoding, validation, defaulting, API-33 typed Serializable access, and conversion into typed route arguments. No new route reads raw extras directly.

| Legacy receiver/flow | Key | Runtime type accepted | Legacy missing/malformed behavior | Typed-route behavior |
| --- | --- | --- | --- | --- |
| Novel detail | `aid` | Int | Defaults to 1 | Compatibility trampoline preserves 1; internal route requires positive ID |
| Novel detail | `from` | String? | Null allowed; `fav`, `latest`, and `list` are known senders | Normalize to source enum with unknown legacy value preserved for diagnostics |
| Novel detail | `title` | String or CharSequence? | Null allowed | Normalize to nullable String |
| Search result | `key` | String? | Empty string | Trim for UI display; provider validation rejects empty request |
| Review list/create | `aid` | Int | Defaults to 1 | Trampoline preserves 1; internal route requires positive ID |
| Review thread | `rid` | Int | Defaults to 1 | Trampoline preserves 1; internal route requires positive ID |
| Review thread | `title` | String? | Null allowed | Nullable display title |
| Reader V1/vertical | `aid`, `cid` | Int | Each defaults to 1 | Old class trampoline preserves 1; new Reader route treats missing/non-positive IDs as a visible argument error |
| Reader | `from` | String? | Null/cloud; exactly `fav` means local/cache-first | Normalize to typed source mode without changing `fav` meaning |
| Reader | `forcejump` | String? | Empty becomes `no`; exactly `yes` enables saved-position jump | Normalize to Boolean |
| Reader | `volume` | Serializable `VolumeList` | V1 creates empty object; vertical permits null | API-33 typed decode; retain receiver-specific trampoline default |
| Reader | `volumes` | Serializable `ArrayList<VolumeList>` | Optional; modern falls back to `volume` | Filter only valid VolumeList entries and preserve order |
| Image viewer | `path` | String? | Old Activity crashes when missing | Trampoline accepts old key; new route shows recoverable missing-argument error |

Phase 0 generates a checked-in sender/receiver manifest from all `putExtra`/`get*Extra` sites. Every entry records key, package/class sender and receiver, runtime type, null/malformed/default behavior, allowed sentinel values, API-33 decoding, and retirement owner. A legacy class cannot be removed until its manifest rows pass old-sender-to-new-trampoline tests.

### 6.3 Serializable Boundary Types

Keep the package name, class name, Serializable identity, and public field compatibility of boundary DTOs such as `VolumeList` and `ChapterInfo` until all senders and receivers have migrated and round-trip tests prove retirement is safe.

- Current compiled baseline candidates are `ChapterInfo=471719262165600067L` and `VolumeList=9025397832339598968L`.
- Phase 0 extracts the actual UID, class name, fields, and R8 name from the last signed shipped artifacts. A mismatch with the candidates stops the migration until the shipped value is adopted.
- Declare the verified UIDs explicitly and add R8 keep-name/field rules for the compatibility window.
- Tests deserialize golden payloads written by the old signed/minified release into the new minified release, not merely new-to-new round trips.
- Run pre-API-33 untyped Bundle and API-33+ typed Bundle tests.
- Generated release mapping and compatibility fixture hashes are retained as audit artifacts.

### 6.4 Legacy Files and Paths

The compatibility layer must preserve the semantics of:

| Legacy artifact | Purpose |
| --- | --- |
| `search_history.wk8` | Search history |
| `read_saves.wk8` | Legacy reader progress |
| `read_saves_v1.wk8` | V1/modern compatibility progress |
| `bookshelf_local.wk8` | Local bookshelf |
| `settings.wk8` | Application and reader settings |
| `cert.wk8` | Legacy account/session material |
| `notice.wk8` | Cached notice text |
| `avatar.jpg` | Cached account avatar |
| `saves/intro` | Novel metadata and introductions |
| `saves/novel/{cid}.xml` | Cached chapter XML |
| `saves/imgs` | Covers and chapter images |
| `custom/menu_bg` | Custom drawer/background image |
| `custom/reader_font` | Imported reader font |
| `custom/reader_background` | Imported reader background |
| root `cache` and app `cacheDir/imgs` | Legacy image/network caches |
| `.nomedia` markers | Media scanner suppression |
| `modern_reader_display_settings` SharedPreferences | Modern font size, line height, paragraph spacing, night mode |
| `/wenku8/` | Legacy external root |
| `filesDir/` | Current internal root |
| `.migration_completed` | Existing migration sentinel |

Primary/backup lookup order, internal/external fallback behavior, and migration sentinel behavior must be locked with golden tests before storage code changes.

Phase 0 generates a checked-in artifact manifest from every production file/SharedPreferences read and write. It records path policy, format, encoding, writer, reader, backup eligibility, account/source partition, migration owner, and deletion policy. The list above is the minimum, not permission to omit a discovered artifact.

`cert.wk8` receives a dedicated schema/security audit. Plaintext passwords are never imported into the new SessionStore or backed up. If a legacy password is present, the new account flow starts signed out and requires reauthentication; after successful new authentication, the legacy password-bearing file is atomically removed or rewritten without the password. Rollback may require login again, but bookshelf/progress/content data is not affected.

### 6.5 Provider Selection and Legacy ABI

Provider selection is explicit and fail-closed. Directory presence never changes behavior by itself.

- `:api-contract`, `:api-public`, and `:api-contract-tests` are always included under unique project paths.
- The default `-Pwenku8Provider` value is `public`.
- `-Pwenku8Provider=private` requires a valid private `api/build.gradle`; configuration fails with a redacted message when it is unavailable.
- In a private job, the existing private source remains the transitional logical `:api` project and `:api-private-adapter` wraps it into the typed contract.
- In a public job, `settings.gradle` includes the single project path `:api` and maps only that descriptor to `api-legacy-bridge/`; it never includes a second `:legacy-api-bridge` project identity. The bridge's build file depends on `:api-public` and the contract/core modules needed for adaptation. Remaining legacy callers depend on logical `:api`, while new routes receive `:api-public` through `AppContainer`.
- In a private job, `settings.gradle` includes the single project path `:api` at the checkout's private `api/` directory plus the uniquely named `:api-private-adapter`; the adapter depends on logical `:api`. No two Gradle project descriptors ever share a project directory or build directory in either graph.
- The public provider is still compiled and contract-tested in a private checkout; it never disappears from that Gradle graph.
- The public logical `:api` bridge exports the frozen `Wenku8API`, `Wenku8Error`, `LightNetwork`, and `LightUserSession` source ABI needed by remaining legacy routes. It delegates safe operations to the public provider or returns existing non-throwing failure/null semantics; it never throws for a normal visible action.
- The bridge implements synchronous URL/enum/request-parameter helpers directly and preserves the legacy asynchronous call sites. It must not call suspend providers through main-thread `runBlocking`. Where a legacy wire shape cannot be safely adapted, its visible old route is disabled in the public variant until the corresponding new route is enabled.
- The throwing `api-stub` implementation is removed in Phase 2. The non-throwing bridge remains until an inventory proves that zero production caller imports the legacy ABI.
- Public and private debug/minified-release jobs assert the selected provider identity and run provider-specific smoke tests.

## 7. Target Architecture

### 7.1 Target Modules

| Module | Responsibility | Allowed dependencies |
| --- | --- | --- |
| `:app` | Manifest, application composition root, Navigation Compose host, external compatibility trampolines | Feature entry APIs, core design system, data/storage/provider implementations needed for composition |
| `:api-contract` | Provider interfaces, wire-independent request/response models, typed failures | `:core:model` only |
| `:api-public` | Independently authored public HTTPS transport and parsers | contract, model, network, session contract |
| `:api-contract-tests` | Provider-neutral contract test suite published as reusable test fixtures/suites | contract, model, testing libraries |
| transitional logical `:api` | Public legacy bridge or selected private legacy ABI | Legacy callers only |
| `:api-private-adapter` | Optional typed adapter over the private legacy provider | contract, model, private logical `:api` |
| `:core:model` | Stable domain identifiers and immutable models | Kotlin/Java standard library only |
| `:core:domain` | Repository interfaces and application use cases shared by features | `:core:model` and `:api-contract` where provider capability types are required |
| `:core:session-contract` | Platform-neutral session records, session epoch, load/save/purge interface | `:core:model` |
| `:core:network` | OkHttp clients, GBK codec, CookieJar, request throttling, redaction | model, session contract |
| `:core:storage` | Room, DataStore, encrypted session storage, legacy codecs and path policy | model, session contract |
| `:core:data` | Repository implementations, offline policy, cache coordination, provider adapters | domain, contract, network, storage, model |
| `:core:designsystem` | Material 3 theme, tokens, shared composables, adaptive primitives | Compose Material 3 |
| `:core:testing` | Fixtures, fakes, test dispatchers, screenshot configuration | model and test libraries |
| `:feature:library` | Discovery, latest, ranking, categories, search, bookshelf, history | model, repository interfaces, design system |
| `:feature:novel` | Detail, catalog, collection, downloads, reviews | model, repository interfaces, design system |
| `:feature:reader` | Modern Reader, pagination, catalog, images, settings, progress | model, repository interfaces, design system |
| `:feature:account` | Captcha login, session, profile, and capability-gated check-in | model, repository interfaces, design system |
| `:feature:settings` | Language, theme, reader settings, cache, migration UI, wallpaper, about | model, repository interfaces, design system |

### 7.2 Extraction Strategy

Do not move cyclic packages directly into Gradle modules.

1. Establish the target interfaces and packages inside the current app module.
2. Move direct legacy calls behind adapters.
3. Make dependency direction acyclic and enforce it with tests/static checks.
4. Extract the now-isolated package into its target physical module.
5. Run public/private provider and compatibility gates after every extraction.

This avoids turning current package cycles into Gradle dependency cycles.

### 7.3 Dependency Rules

- A feature module cannot import `Wenku8API`, `LightNetwork`, `LightUserSession`, `GlobalConfig`, Room DAO implementations, OkHttp, or Jsoup.
- A composable cannot read storage, make a network request, parse HTML/XML, or own an executor.
- A ViewModel depends on use cases or repository interfaces, not implementations.
- Repository implementations are the only layer that combines remote, database, DataStore, session, and legacy file sources.
- Provider code cannot import Android UI classes.
- Feature modules communicate through typed navigation arguments and stable domain models, never by importing another feature's ViewModel.
- Provider code owns remote transport and parsing only. `:core:data` owns domain caching, stale policy, invalidation, and read single-flight. `:core:storage` owns persistence.
- `:core:network` consumes `SessionStore` through `:core:session-contract`; `:core:storage` supplies the encrypted implementation and `:app` injects it. Network and storage do not depend on one another.

### 7.4 Composition and Injection

`MyApp` creates one explicit `AppContainer` that owns long-lived clients, databases, repositories, clocks, dispatchers, and provider bindings. Route-level factories construct ViewModels with constructor dependencies.

The container is not a global service locator:

- Composables receive state and callbacks.
- ViewModels receive dependencies through constructors.
- Workers receive dependencies through explicit factories.
- Tests replace bindings with fakes through test containers.

A DI framework may be reconsidered after legacy compatibility code is removed, but it is not required by this program.

### 7.5 State and Effects

Each route has one ViewModel exposing immutable `StateFlow<UiState>`.

- Persistent UI state belongs in `UiState`.
- Navigation, Snackbar requests, permission requests, and one-time external launches use a bounded effect stream.
- `SavedStateHandle` stores route arguments and small restoration state.
- Long-running downloads and migrations use WorkManager and database-backed progress.
- Compose collects state with lifecycle-aware APIs.
- Work is launched in `viewModelScope` or an injected application/worker scope.
- Dispatchers, clock, and retry policy are injectable for deterministic tests.

### 7.6 Navigation

The final app has one Navigation Compose host.

- Compact width uses a bottom NavigationBar.
- Medium width uses a NavigationRail.
- Expanded width uses a rail or permanent drawer plus list-detail panes.
- External legacy Activities decode their old Intent with `LegacyIntentCodec`, forward to the new route, and finish when compatibility policy permits.
- Back handling closes Dialog/Sheet, reader chrome, and drawer before popping the route.
- Predictive back is supported on current Android versions.

## 8. Public API Clean-Room Design

### 8.1 Licensing Boundary

Wild is GPLv3. This repository's exact contribution-wide GPL version status is not assumed from a license template. To avoid accidental code mixing:

- Behavioral facts such as public URLs, parameters, encodings, status codes, and session behavior may inform this specification.
- Wild implementation code, CSS selector combinations, tests, fixtures, comments, log messages, and distinctive control flow may not be copied, translated, or mechanically rewritten.
- Target fixtures must be newly authored minimal HTML/GBK documents.
- Public provider implementers work from this behavior specification and independently observed site responses.
- Any proposal to copy Wild code requires a separate license decision and is outside this program.

The boundary is auditable:

- A reference analyst may inspect Wild and public site behavior, but may not author provider implementation or parser fixtures.
- An implementation agent receives only the accepted behavior-evidence ledger, public responses captured independently for the project, and synthetic fixtures. It must not access the Wild checkout, source, tests, or generated artifacts.
- `docs/api-evidence/operations/` contains one provenance record per operation: observation date, public URL class, sanitized request shape, status/redirect behavior, charset, required/optional fields, failure signatures, fixture hash, and observer identity.
- Raw authenticated responses and secrets are never committed. Sanitized observations remove account-specific and copyrighted body content not needed to state the contract.
- Each parser records why its independently authored selectors are sufficient for the sanitized fixture structure.
- A clean-room reviewer attests that implementation and fixtures are traceable to the project evidence ledger rather than Wild expression before merge.

#### 8.1.1 Site, Content, and Distribution Authorization

The repository history records that the Wenku8 operator previously prohibited publication of its API, and it separately warns that application stores may require proof of rights for copyright-sensitive content. This specification does not infer current authorization from public HTML, an existing account, the historical app, or the ability to make a request. The term "public provider" identifies the repository variant; it is not a claim of endorsement or permission.

Phase 0 creates `docs/compliance/site-content-distribution-approval.yaml` with a maintainer owner, decision date, expiry/recheck date, jurisdictions/channels, and evidence hashes for each independently approved scope:

- automated client access and rate limits;
- publication of endpoint/request contracts and provider source;
- captcha login, account reads, and each mutation class;
- offline caching/downloading and treatment of copyrighted text/images;
- alpha, direct-download/baidu, and Play Store distribution;
- privacy notice, account-data handling, and any age/region/channel obligations.

Sensitive correspondence or legal material stays outside the repository; the checked-in record contains only redacted conclusions, scope, owner, dates, and hashes. A qualified maintainer/legal review must mark a scope `ACCEPTED`; `UNKNOWN`, `EXPIRED`, or `REJECTED` is fail-closed. Until the relevant scope is accepted, deterministic synthetic-fixture implementation may proceed inside the access-controlled workspace, but automated live observation, live account tests, pushing/publishing new exact operation ledgers or provider endpoints outside that workspace, and any distributable build with that provider capability enabled are prohibited. Phase 2 and public-provider release gates cannot pass on fixture success alone. A policy, ownership, host, or channel change invalidates the affected approval and disables release/live gates pending re-review.

#### 8.1.2 Repository and Third-Party Licensing

The root text declares GPLv2-or-later, but a license file alone does not prove provenance or compatibility for every copied source, binary, font, image, fixture, or dependency. The active reader contains a vendored SlidingLayout family whose own in-app notice says `Unknown License`; retaining it until Phase 6 cannot authorize redistribution.

- Phase 0 generates a checked-in source/dependency/asset SBOM and provenance ledger, including vendored source directories, transitive Maven artifacts, build plugins, fonts, images, fixtures, and generated/distributed notices.
- Each entry records origin, version/commit or content hash, copyright owner, SPDX expression, modification status, required source/notice offer, allowed build/distribution scopes, and resolution owner.
- `UNKNOWN`, missing, or incompatible licensing in any production source or packaged artifact blocks every signed/distributable variant. SlidingLayout must obtain verifiable permission or be replaced/removed from all such variants before Phase 0 exits; delaying UI retirement is not a waiver.
- `NOTICE`, about-screen license text, corresponding-source delivery, and store metadata are generated from the approved ledger and diff-tested against the packaged APK/AAB and SBOM.
- Gradle uses centralized repository declarations with content filters and an approved-origin policy; project repositories, dynamic/changing versions, silent mirror substitution, and unverified artifacts fail configuration. A mirror may be used only for explicitly scoped coordinates whose bytes pass the same pinned verification metadata as the approved upstream.
- Check in strict Gradle dependency verification metadata and dependency locks for every resolvable public production/test/plugin configuration; protected private configurations supply the equivalent overlay below. The wrapper pins `distributionSha256Sum`; CI validates the wrapper JAR and records Gradle/AGP/JDK/toolchain hashes.
- A clean-cache online resolution followed by a verified offline rebuild must produce identical dependency graphs and packaged input hashes. CI retains the lock state, verification report, resolved coordinate/origin/checksum inventory, SBOM, and build provenance with each signed artifact.
- The public checkout contains the complete public SBOM/provenance/locks/verification base. Protected private CI supplies a separately access-controlled overlay for private coordinates, origins, endpoint configuration, approvals, licenses, locks, and checksums; a deterministic merge may add private rows but cannot replace/weaken a public row. The merged files exist only in the protected job's ephemeral workspace.
- Public CI/evidence receives only a signed redacted attestation binding schema/tool version, provider ID, variant, source commit, public-base hash, artifact hash, protected policy revision, current authorization decision/recheck/expiry, issue time, `notAfter`, unique CI run ID/nonce, signing key ID, result, and an opaque protected attestation ID or encrypted-report digest. It never contains a guessable raw overlay digest, private coordinate, or endpoint. `notAfter` cannot exceed the earliest bound approval/key validity.
- `docs/compliance/private-attestation-trust.yaml` pins accepted verification public keys/certificate chains, key IDs, validity windows, rotation successors, and revocations. Initial trust and every rotation/revocation require two-maintainer review plus an out-of-band fingerprint record; a same-change self-signed replacement is rejected. The offline public verifier rejects a signature with a mismatched provider/variant/commit/base/artifact/policy revision, expired or future time, reused run ID/nonce, unknown/revoked key, stale approval, or schema downgrade. Protected CI re-runs all gates and issues a fresh attestation for every release candidate; an older `PASS` is never reusable for a new run or artifact.
- Wild remains behavior-only under the clean-room rules above; its GPLv3 license is not used to cure unrelated repository provenance gaps.

### 8.2 Provider Contracts

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

The public provider implements every operation required by an advertised reachable product route. UI routes/actions are derived from `ProviderCapabilities`; a disabled capability is hidden or replaced by a clear unavailable explanation before invocation. Any provider call whose required capability is absent at call time legally returns `Unsupported(capability)` and performs zero network activity. This covers non-UI callers and capability changes after rendering without weakening the UI rule: reachable UI checks capabilities before rendering or invoking an action. A normal visible action cannot terminate in `UnsupportedOperationException`.

The capability mapping is exhaustive: catalog reads, tag discovery, and novels-by-tag require `ANONYMOUS_CATALOG`; binary fetch requires `BINARY_DOWNLOAD`; registration requires `REGISTRATION_LINK`; begin/login/session-validation/logout require `CAPTCHA_LOGIN`; profile/avatar require `PROFILE`; bookshelf read and mutation require their respective bookshelf capabilities; check-in and recommendation require their named capabilities; and community read/create/reply require their respective review capabilities. All source facets return the same immutable enabled set for the selected provider instance. An enabled operation must implement its contract and cannot return `Unsupported`.

Daily check-in is `false` for the public provider until its evidence ledger contains a valid HTTPS scheme/host/path, request contract, and success/failure semantics. The currently observed Wild cleartext app host is explicitly rejected. Private providers may advertise check-in only when their transport passes the same HTTPS policy.

### 8.3 Stable Contract Types

The contract module must compile independently. The following definitions establish ownership and required fields; implementation plans may split them into focused files without changing their semantics.

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

@JvmInline value class SourceId(val value: String)
data class NovelKey(val sourceId: SourceId, val remoteId: String)
data class ChapterKey(val novel: NovelKey, val remoteId: String)
data class ReviewKey(val novel: NovelKey, val remoteId: String)
@JvmInline value class ReviewPostKey(val value: String)
@JvmInline value class LoginAttemptId(val value: String)
@JvmInline value class BookshelfEntryKey(val value: String)

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

data class ProviderCapabilities(
    val providerId: SourceId,
    val enabled: Set<ProviderCapability>,
    val inputPolicy: ProviderInputPolicy,
)

data class ProviderInputPolicy(
    val searchMaxEncodedBytes: Int,
    val reviewTitleMaxCodePoints: Int,
    val reviewBodyMaxCodePoints: Int,
    val replyMaxCodePoints: Int,
)

enum class ContentLanguage { SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE }
enum class SearchScope { TITLE, AUTHOR }
enum class BrowseKind { LATEST, COMPLETED, CATEGORY, RANKING }
enum class RankingPeriod { ALL_TIME, MONTH, WEEK, DAY }

data class Page<T>(val items: List<T>, val currentPage: Int, val nextPage: Int?)
data class BrowseRequest(
    val kind: BrowseKind,
    val page: Int,
    val language: ContentLanguage,
    val categoryId: String? = null,
    val rankingPeriod: RankingPeriod? = null,
)
data class TagGroup(val id: String, val label: String)
data class TagDiscoveryRequest(val groupId: String?, val language: ContentLanguage)
data class TagBrowseRequest(val tagId: String, val page: Int, val language: ContentLanguage)
data class SearchQuery(
    val text: String,
    val scope: SearchScope,
    val page: Int,
    val language: ContentLanguage,
)

data class NovelSummary(val key: NovelKey, val title: String, val author: String?, val cover: BinaryRequest?)
data class NovelDetail(
    val key: NovelKey,
    val title: String,
    val author: String?,
    val status: String?,
    val tags: List<String>,
    val introduction: ControlledRichText,
    val cover: BinaryRequest?,
)
data class HomeSection(val id: String, val title: String, val novels: List<NovelSummary>)
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

data class CaptchaChallenge(
    val attemptId: LoginAttemptId,
    val image: BinaryResource,
    val expiresAtEpochMillis: Long,
)
class LoginRequest(
    val attemptId: LoginAttemptId,
    val username: String,
    password: CharArray,
    captcha: CharArray,
) : AutoCloseable {
    private val consumedOrClosed = java.util.concurrent.atomic.AtomicBoolean(false)
    private val ownedPassword: CharArray = password
    private val ownedCaptcha: CharArray = captcha
    suspend fun <T> consumeSecrets(
        block: suspend (password: CharArray, captcha: CharArray) -> T,
    ): T {
        check(consumedOrClosed.compareAndSet(false, true))
        return try {
            block(ownedPassword, ownedCaptcha)
        } finally {
            close()
        }
    }
    override fun close() {
        consumedOrClosed.set(true)
        ownedPassword.fill('\u0000')
        ownedCaptcha.fill('\u0000')
    }
}
data class SessionState(val authenticated: Boolean, val accountId: String?, val expiresAtEpochMillis: Long?)
data class UserProfile(
    val accountId: String,
    val username: String,
    val nickname: String?,
    val score: String?,
    val experience: String?,
    val rank: String?,
    val avatar: BinaryRequest?,
)

data class BookshelfEntry(
    val key: BookshelfEntryKey,
    val novel: NovelSummary,
    val groupId: String,
)
data class BookshelfGroup(val id: String, val title: String, val items: List<BookshelfEntry>)
sealed interface BookshelfCommand {
    data class Add(val novel: NovelKey, val targetGroupId: String?) : BookshelfCommand
    data class Remove(val entryKey: BookshelfEntryKey, val sourceGroupId: String) : BookshelfCommand
    data class Move(
        val entryKeys: List<BookshelfEntryKey>,
        val sourceGroupId: String,
        val targetGroupId: String,
    ) : BookshelfCommand
}
data class CheckInResult(val accepted: Boolean, val message: String?)
data class RecommendationResult(val accepted: Boolean, val message: String?)

data class ReviewSummary(val key: ReviewKey, val title: String, val author: String?, val replyCount: Int?)
data class ReviewPost(val key: ReviewPostKey, val author: String?, val body: ControlledRichText, val postedAt: String?)
data class CreateReviewCommand(val novel: NovelKey, val title: String, val body: String)
data class ReplyCommand(val review: ReviewKey, val body: String)
```

Contract rules:

- Page numbers are positive and `nextPage` is null when no next page is proven.
- `TagDiscoveryRequest` discovers tag metadata only; selecting a tag always calls `novelsByTag` and returns paged `NovelSummary` values.
- `BookshelfEntryKey` preserves the server membership ID independently of `NovelKey`. Move commands contain one or more unique entry keys, a verified source group, and a different target group.
- Input is normalized once, then validated against the selected provider's accepted evidence-backed `ProviderInputPolicy` before a request is made.
- The `LoginRequest` constructor consumes ownership of the supplied password and captcha arrays without copying them. The caller immediately drops all aliases and must never read or mutate them again; request close therefore clears the exact caller-supplied arrays.
- `consumeSecrets` is single-use, rejects an already closed request, and is suspend-aware. The provider constructs the encoded login body, awaits the cancellable OkHttp call, parses the terminal login result, and clears any provider-owned temporary encoded byte arrays entirely inside this scope; its `finally` clears both owned arrays on success, failure, or coroutine cancellation.
- Provider implementations do not retain either array, a derived login body, or an alias outside `consumeSecrets`. Tests assert the caller-supplied arrays are zeroed after success, typed failure, exception, timeout, and cancellation.
- `ChapterDocument` preserves block order.
- `ControlledRichText` contains non-executable normalized content.
- Provider models never expose Cookies, DOM nodes, ContentValues, Android UI types, or arbitrary HTML.

### 8.4 Public Behavior Surface

| Operation | Method/path class | Auth | Idempotency/cache | Normative result |
| --- | --- | --- | --- | --- |
| Capabilities | Local provider metadata | No | Pure/no cache | Evidence-backed enabled set and input limits |
| Home | `GET /index.php?charset=gbk` | No | Read; 10-minute domain cache | Ordered home sections |
| Tag discovery | Accepted HTTPS tag-group/tag operation | No | Read; list cache | Tag groups and tag summaries |
| Novels by tag | Accepted HTTPS paged tag-result operation | No | Read; list cache | Paged novel summaries and nullable next page |
| Novel detail | `GET /modules/article/articleinfo.php?id={aid}&charset=gbk` | No | Read; 1-hour domain cache | Normalized detail or typed not-found/parse failure |
| Catalog | `GET /modules/article/reader.php?aid={aid}&charset=gbk` | No | Read; 1-hour domain cache | Ordered volumes and chapters |
| Chapter | `GET /novel/{aid/1000}/{aid}/{cid}.htm` | No | Read; durable offline content | Ordered text/image document |
| Rankings/completed | HTTPS `toplist.php` / `articlelist.php` operations | No | Read; list cache | Paged novel summaries |
| Search | HTTPS `search.php`; GBK query bytes percent-encoded | No | Read; short cache | Paged novel summaries |
| Binary | Validated HTTPS URL emitted by a parsed model | Depends on resource | Read; image/content cache | Bounded media bytes and verified media type |
| Registration | Independently verified HTTPS registration page | No | Read/no domain cache | Validated external HTTPS link |
| Login prewarm/captcha | HTTPS `/`, `/login.php`, `/checkcode.php` | Attempt Cookie only | Never domain-cached | Opaque attempt and bounded captcha image |
| Login submit | HTTPS `/login.php` form | Attempt Cookie only | Mutation; no automatic retry | Authenticated session or typed auth failure |
| Session/profile/avatar | Evidence-ledger HTTPS forms/pages | Yes | Reads; account/epoch partition | Session/profile/binary result |
| Bookshelf read/group/move/add/remove | Evidence-ledger HTTPS forms/pages | Yes | Read or non-retried mutation | Exact redirect/body success and typed failure |
| Recommendation | Evidence-ledger HTTPS form | Yes | Non-retried mutation | Accepted/rejected result and targeted invalidation |
| Review list/thread | HTTPS review pages recorded in evidence ledger | Read may be anonymous | Read; short cache | Paged summaries/posts |
| Review create/reply | Evidence-ledger HTTPS form | Yes | Non-retried mutation | Created key or typed validation/auth failure |
| Logout | Evidence-ledger remote action plus unconditional local purge | Yes | Mutation; no retry required for local purge | Typed remote result after local state is cleared |
| Daily check-in | No accepted public wire contract yet | Yes | Mutation | Capability disabled until a valid HTTPS evidence record exists |

All production endpoints are HTTPS and restricted to an explicit host allowlist. Cleartext fallback is prohibited.

For every evidence-ledger operation, the accepted record is normative and contains the exact scheme/host/port, method, path template, form/query fields, charset, authentication precondition, redirect locations, success parser, failure parser, idempotency classification, input limits, cache effect, and redacted fixture hash. An operation remains capability-disabled until that record exists and its MockWebServer contract passes.

### 8.5 HTTP and Encoding

- Use OkHttp with separate anonymous and authenticated clients that share safe connection resources but not mutable test state.
- The initial content/account origin is exactly `https://www.wenku8.net:443`. Additional HTTPS image origins enter the generated HostPolicy only through an accepted evidence-ledger record; `app.wenku8.com` and all cleartext origins are absent.
- Decode Wenku8 pages using the declared/known GBK contract with explicit decode failures.
- Encode Chinese search input as GBK bytes before percent encoding.
- Use Jsoup DOM parsing; do not use a whole-page regular expression parser.
- Normalize relative URLs against the validated HTTPS base URL.
- Attach Referer only to approved Wenku8 image hosts.
- Set conservative connect/read/call timeouts and map them to distinct network failures.
- Limit concurrency and apply request throttling to avoid abusive behavior.
- Use conditional requests where the site supports them.
- Validate every redirect hop before issuing it and cap a chain at five hops.
- Reject userinfo, literal IP hosts, non-default ports, unknown hosts, and HTTPS-to-HTTP network requests.
- A safe GET/HEAD `http://www.wenku8.net/...` Location may be canonicalized to the independently verified equivalent `https://www.wenku8.net/...` without sending a cleartext request. If the HTTPS form is not verified, fail with `ProtocolViolation`.
- Strip Cookie, Authorization, Referer, and other sensitive headers before any permitted origin change. Authenticated flows cannot change origin.
- Do not follow mutation redirects. Validate the status and exact relative/same-origin Location pattern recorded by that operation's evidence ledger; an arbitrary 3xx is not success.

### 8.6 Parsing Policy

Each endpoint has a pure parser with a named `contractRevision`.

- Required fields fail with `Parse(endpoint, contractRevision)`.
- Missing optional fields produce null/default domain values and parser diagnostics.
- Selector fallbacks must be independently authored and tested against minimal fixtures.
- A page that looks like a login, challenge, block, or error page is rejected before domain parsing.
- Chapter parsing preserves text/image order, `<br>` boundaries, non-breaking spaces, relative images, and meaningful paragraph separation.
- Watermarks and known navigation elements are removed by independently documented semantic rules.
- Parser failures never cache the invalid response as valid domain data.

### 8.7 Authentication and Session

1. Create an isolated, in-memory, per-attempt CookieJar and an attempt-scoped client before any prewarm request.
2. Prewarm `/` and `/login.php` through that same attempt client.
3. Request `/checkcode.php` through that same attempt client.
4. Return only an opaque, cryptographically random, single-use `LoginAttemptId`, bounded captcha image, and expiry. Cookies and server context never leave provider internals.
5. Bind the submitted `LoginRequest` to that attempt ID. Reject unknown, expired, cancelled, already-used, or replayed attempts.
6. Collect captcha text from the user. The application never automates captcha solving.
7. Submit username, password, captcha, cookie duration, and action through the same isolated attempt client.
8. On success, promote only the validated session Cookies into the account SessionStore and increment the session epoch.
9. Destroy attempt Cookies and clear password/captcha arrays on success, failure, cancellation, timeout, or process death. Process death always requires a new captcha.
10. Validate login through response semantics and a valid, unexpired session Cookie.
11. Validate session before mutations after process restoration or a stale interval.
12. Treat a 200 login page, block page, expired Cookie, or challenge page as authentication/session failure rather than business content.
13. Logout increments the session epoch, cancels old-epoch calls, and clears local Cookies/account caches in `finally`, even when the remote logout action fails.

Cookie persistence must honor domain, path, secure, expiry, and logout semantics. MockWebServer tests prove that Cookies set by each prewarm/captcha stage reach the next request, login uses the same attempt jar, and no Cookie crosses into a concurrent or subsequent attempt. Passwords and captcha text are never persisted.

### 8.8 Test Credential Handling

The user has authorized a dedicated test account for implementation verification. Its literal credentials are not part of this document or repository.

- Local live tests read `WENKU8_LIVE_USERNAME` and `WENKU8_LIVE_PASSWORD` from the process environment.
- Credentials must not appear in Gradle properties, local.properties, source, resources, fixtures, CI output, screenshots, crash reports, command history emitted by the agent, or committed files.
- Password and captcha values remain local to a non-saveable login controller and the short-lived `LoginRequest`. They never enter UiState, SavedStateHandle, rememberSaveable, WorkManager input/output, retry objects, analytics, tracing attributes, or crash metadata.
- Captcha text is entered interactively or supplied to a one-run local harness after the image is displayed. It is never bypassed.
- Default live account smoke tests are read-only: login, validate session, read profile, read bookshelf, and logout.
- Reversible bookshelf add/remove tests require `WENKU8_LIVE_REVERSIBLE_MUTATIONS=true`, use a dedicated fixture novel, record initial state, and restore it in `finally`.
- Check-in, recommendation, review creation, and reply tests can have persistent server effects. MockWebServer tests cover them by default. A live mutation requires a separate `WENKU8_LIVE_PERSISTENT_MUTATIONS=true` gate and explicit user confirmation for that run.
- Redaction tests assert that username, password, Cookie values, captcha text, and raw response bodies are absent from logs.

### 8.9 Typed Failures

```kotlin
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
    data class Parse(val endpoint: String, val contractRevision: Int) : ApiFailure
    data object NotFound : ApiFailure
    data class RateLimited(val retryAfterSeconds: Long?) : ApiFailure
    data class Storage(val operation: String) : ApiFailure
    data class Unsupported(val capability: ProviderCapability) : ApiFailure
    data class ProtocolViolation(val rule: String) : ApiFailure
}
```

Failures carry only redacted operation names and trace IDs. They do not retain passwords, Cookies, captcha text, complete User-Agent values, or full HTML.

Coroutine `CancellationException` always propagates and is never converted to `ApiFailure`. Explicit user cancellation of a durable WorkManager job is represented by that job's domain state, not a provider failure.

Provider contract tests call every capability-gated operation with the corresponding capability absent and assert `Unsupported(theCapability)` plus zero dispatched HTTP requests. Separate UI tests assert that unavailable actions are not reachable before invocation.

### 8.10 Caching and Request Coalescing

Anonymous cache keys contain source, host, schema version, language, operation, and canonical parameters. Authenticated cache and in-flight keys additionally contain the non-secret account ID and a monotonically increasing session epoch.

| Data | Initial policy |
| --- | --- |
| Home | 10 minutes |
| Lists, tags, rankings, search | 15 to 60 minutes depending on operation |
| Novel detail and catalog | 1 hour |
| Reviews | Short-lived, refreshable |
| Chapters and images | Offline-first, durable subject to user cache policy |
| Account/profile/bookshelf | Private cache, invalidated by session and mutations |

Rules:

- Do not cache blank bodies, login pages, challenge pages, block pages, parse failures, or server error pages as valid data.
- Merge concurrent identical idempotent reads with repository-owned single-flight coordination.
- Never coalesce or automatically retry login, logout, bookshelf mutations, recommendation, check-in, review creation, or replies unless a future evidence ledger proves a server-supported idempotency key.
- Logout or account switch increments the session epoch, cancels old-epoch calls, and rejects a completion whose captured epoch no longer matches before any cache/database write.
- A repository may return explicitly marked stale data after a network failure.
- UI state must disclose offline/stale content.
- Mutations invalidate only affected cache keys.
- Parser contract revision changes isolate or invalidate incompatible cached parser results.
- Authenticated cache entries are partitioned by non-secret account identity and cleared on logout.
- A checked-in mutation invalidation matrix names the exact domain keys affected by every successful mutation.

### 8.11 Public Provider Tests

The provider test suite includes:

- Minimal synthetic normal pages.
- Reordered fields and missing optional fields.
- Relative and absolute URLs.
- Single-page, paged, and missing-pagination responses.
- Tag-group/tag discovery separated from paged novels-by-tag results.
- GBK Chinese search encoding and invalid encoding.
- Empty body and incorrect Content-Type.
- Captcha image, invalid captcha, invalid credentials, expired session, 403 challenge, 200 challenge, and 200 login-page masquerade.
- Bookshelf redirect success, body success, and body failure.
- Bookshelf membership-ID retention across reload plus single remove and multi-entry move request shapes.
- Chapter `<br>`, `&nbsp;`, watermark, relative image, absolute image, and text/image ordering.
- Cookie domain, path, secure, expiry, replacement, and logout.
- Fake-clock TTL, single-flight concurrency, stale fallback, and targeted invalidation.
- Log redaction for all credential/session material.
- Shared contract tests executed against the public provider and optional private adapter.

Real-site smoke tests are opt-in and never part of deterministic default CI.

### 8.12 Privacy, Logging, and Outbound Network

Phase 0 applies to retained legacy routes as well as new code. The current application initializes Firebase/AdMob, requests the advertising ID, sends search terms and novel identifiers/titles to analytics, and logs review drafts and raw server/account/bookshelf/review bodies. No account or live-provider phase may begin while those paths remain reachable.

- Remove Firebase Analytics, Crashlytics automatic collection, AdMob, advertising-ID permission/metadata, their Gradle plugins/dependencies, and their auto-initializers from every production variant. Reintroducing any telemetry, advertising, or remote crash backend requires a separate user-approved privacy design and is outside this program.
- Remove raw request/response, HTML/XML, search term, title/author/content ID, review/reply draft, bookshelf, avatar/profile, username, Cookie, and credential logging from every retained legacy and new route. Release logging is an allowlist of bounded operation codes, redacted failure classes, and random trace IDs.
- Generate the public base `docs/verification/outbound-network-manifest.yaml` from source, dependencies, merged manifests, network-security configuration, and packaged bytecode/resources. Every possible public-variant origin/SDK auto-initializer records purpose, data classes, authentication, owning feature/flavor, consent/legal basis where applicable, and retirement owner. Protected private CI merges the access-controlled compliance overlay defined in Section 8.1.2 and exports only its redacted pass/fail/hash attestation.
- Production networking is created only by audited application factories. The public runtime host policy permits accepted Wenku8 HTTPS operations/image hosts and flavor-approved update checks; the private policy is generated only inside protected CI from its approved overlay. Both deny unknown origins and never include analytics/ad endpoints. Legacy network helpers are wrapped by the selected audited policy before they remain reachable.
- Static checks fail on direct unaudited OkHttp/URLConnection/WebView/socket construction, known telemetry/ad SDKs, advertising-ID declarations, or raw sensitive logging. A hermetic instrumentation egress harness launches and exercises every retained/new route with DNS/proxy capture and fails on any destination or payload field absent from the manifest.
- Phase 0 tests prove a release-like build produces zero third-party analytics/ad/crash requests at cold start, navigation, search, detail, reader, migration, login, profile, bookshelf, review compose/reply, error, and process-restart boundaries.
- `MigrationDiagnostics` is the only program-defined operational signal. It accepts compile-time enum outcomes and coarse pending-count buckets, stores them locally, and has no device/account/content/query/novel/chapter identifiers, timestamps finer than a day, free text, stack trace, or automatic network sink. Users or controlled pilot testers may explicitly export a previewed redacted report through the system share sheet; cancellation sends nothing. Contract/property tests reject extra keys, high-cardinality values, identifier correlation, and any sink other than the local store or user-initiated export.

## 9. Data and Migration Design

### 9.1 Sources of Truth

- Room becomes the source of truth for normalized novel metadata, catalog metadata, bookshelf membership, download state, search history, and reader progress.
- DataStore becomes the source of truth for language, theme, reader display settings, rollout flags, and non-sensitive preferences.
- An encrypted `SessionStore` backed by Android Keystore protects session Cookies. It does not store passwords.
- Durable chapter XML/image files remain the source of truth for downloaded content until a separately approved content-store migration exists.
- Legacy `.wk8` files remain readable throughout the compatibility window.

### 9.2 Proposed Room Responsibilities

The exact schema is implementation-plan work, but ownership is fixed:

- Source and novel identity.
- Novel summary/detail cache metadata.
- Volume/chapter catalog ordering.
- Bookshelf groups and membership.
- Search history.
- Download requests and per-item progress.
- Reader progress and last-opened history.
- Cache metadata, parser revision, freshness, and account partition.

Raw passwords, captcha text, arbitrary HTML, and unbounded response bodies are never database fields.

Session records include provider ID, non-secret account ID, session epoch, Cookie attributes, creation time, and expiry. A missing/invalidated Keystore key, authentication-tag failure, malformed ciphertext, or partial record causes atomic deletion of the complete session record and authenticated caches, then returns the signed-out state. The app never attempts to salvage individual Cookie bytes.

### 9.3 Legacy Compatibility Components

- `LegacySaveCodec`: exact `.wk8` and XML parsing/writing semantics.
- `LegacyPathPolicy`: internal/external roots, primary/backup order, SAF and sentinel behavior.
- `LegacyIntentCodec`: old Intent and Serializable boundaries.
- `LegacyReaderProgressAdapter`: old and V1 progress conversion and dual-write.
- `LegacyBookshelfAdapter`: local bookshelf import/export.
- `LegacySettingsAdapter`: old setting identity and reader-setting conversion.

Legacy codecs are pure or filesystem-injected and covered by byte/semantic golden fixtures.

### 9.4 Per-Domain Migration State and Journal

Migration state is tracked independently for settings, search history, bookshelf per account/source, reader progress per source, downloads/catalog metadata, and session credentials. The old `.migration_completed` file remains only the legacy external-copy signal and is never reused as modernization state.

Each domain uses this state machine:

1. `NotStarted`: only the legacy representation is authoritative.
2. `Snapshotting`: all writers for that domain pass through a write barrier while a stable legacy snapshot is recorded.
3. `Importing`: records import with durable per-record checkpoints and mutation IDs.
4. `DualWrite`: new routes write a durable mutation journal/outbox, apply the canonical new-store change, then replay the legacy projection.
5. `Reconciling`: journal replay and record-by-record comparison repair an interrupted second write.
6. `Verified`: zero pending journal entries and canonical record-by-record equality are proven for that domain/account/source.
7. `LegacyReadOnly`: old files remain a fallback but no longer receive writes.
8. `Complete`: the old read path can retire only after the approved compatibility window.

Every existing legacy writer must be intercepted behind the domain adapter before `Snapshotting` begins. A legacy route cannot bypass the barrier and mutate a file during import. Migration is idempotent; a crash resumes from a checkpoint and replays journal entries by mutation ID. An invalid legacy record is reported and preserved rather than discarded.

### 9.5 Write Safety

- Use write-to-temp plus atomic replace when supported.
- Never truncate the only known-good legacy file before a new write is durable.
- Preserve a recoverable prior version during migration.
- Room, DataStore, and files are not one atomic transaction. A durable journal defines the recoverable commit protocol and reconciliation behavior at every crash point.
- Before the canonical commit, failure leaves both stores unchanged. After canonical commit but before legacy projection, the operation returns a recoverable `pending synchronization` state and journal replay completes or compensates it; it never claims both stores are synchronized.
- Database migrations have upgrade tests from every shipped schema version.
- Rollback builds continue to read the legacy files during the rollout window.

### 9.6 Backup and Restore

- Add explicit `data-extraction-rules` and legacy `fullBackupContent` rules.
- Backup rules operate on physical files/databases/preferences, never selected Room rows. Encrypted session records, legacy `cert.wk8`, Cookies, authenticated response caches, and transient parser caches live in separately excluded storage.
- Mutation journals and migration checkpoints live in a dedicated `migration-transient` database under an excluded storage boundary; they never share a Room database or DataStore file with backed-up canonical records.
- Every backed-up canonical settings/progress/bookshelf record includes a monotonic mutation version and its last applied legacy-projection version. Canonical data contains all values required to deterministically regenerate the compatible legacy projection after restore, so an excluded transient journal is not the sole evidence of an incomplete projection.
- Include user-created compatible settings/progress/bookshelf data only as whole physical stores after restore tests prove their versioned canonical records can reconcile any restored or missing legacy companion.
- Restore onto a device without the original Keystore key always becomes signed out and purges unusable ciphertext.
- Restore tests cover fresh install, partial/absent optional stores, old schema to current schema, a restored legacy sentinel without modernization state, and a backup captured after canonical commit but before legacy projection.
- The restore coordinator re-runs per-domain reconciliation instead of trusting a restored completion flag.

### 9.7 Durable Background Execution

WorkManager is a durable scheduler, not an unlimited process lifetime. Every migration, reconciliation, and download is a sequence of bounded, idempotent chunks with a stable work key, input revision, per-item checkpoint, retry count/backoff, cancellation state, and terminal domain result in canonical storage. A process may die between any two items without repeating a committed mutation or losing the user's queue.

Execution selection is explicit:

- Ordinary `CoroutineWorker` runs migration/reconciliation and small download chunks whose measured worst case stays below eight minutes. It checkpoints after each item and yields/reschedules before the platform time budget rather than relying on a single long worker.
- On API 23-33, an explicitly user-started bulk download requiring immediate continuous transfer may use a foreground `CoroutineWorker`. It calls `setForeground` before transfer, declares the `dataSync` foreground-service type, shows an ongoing progress/cancel notification, and still checkpoints/reschedules bounded chunks. On API 31-33 it is scheduled only from an eligible user-visible start. `ForegroundServiceStartNotAllowedException` or expedited quota denial persists a visible queued state and falls back to a bounded regular request; an expedited first chunk may use `RUN_AS_NON_EXPEDITED_WORK_REQUEST`, never a retry loop that repeatedly attempts an illegal foreground start.
- On API 34-36, eligible explicitly user-started bulk transfers use the platform user-initiated data-transfer job path through a `TransferScheduler` adapter. The `JobInfo` sets `setUserInitiated(true)`, an evidence-backed required `NetworkRequest`, estimated upload/download and minimum chunk bytes, storage/battery constraints where applicable, and `setPersisted(true)` for reboot recovery. If eligibility or quota is absent, work becomes a visible queued bounded WorkManager request; the app never attempts an illegal background foreground-service start. Automatic migration never uses the user-initiated path.
- The UIDT `JobService.onStartJob` calls `setNotification` immediately with progress/cancel UI and an explicit end-notification policy, then returns `true` while a service-owned structured coroutine performs the chunk. Actual transferred bytes are checkpointed and drive bounded notification progress; a rescheduled job refreshes its remaining-byte estimate through supported `JobInfo`/scheduler APIs. Exactly one terminal path calls `jobFinished(params, wantsReschedule)`. `onStopJob` cancels the coroutine and active OkHttp call, durably records the platform stop reason/checkpoint, and returns `true` only for a recoverable retry; a persisted user-cancel/permanent failure returns `false`. A work-key lease prevents WorkManager and JobScheduler from executing the same chunk concurrently.
- No job contains a password, Cookie, captcha, raw response, review draft, or account profile in WorkManager/JobScheduler input, output, tags, progress, notification text, or diagnostics.

Phase 3 owns the merged-manifest contract: only permissions/services actually selected by the scheduler are declared. The fallback path accounts for `android.permission.FOREGROUND_SERVICE`, `android.permission.FOREGROUND_SERVICE_DATA_SYNC`, `android.permission.POST_NOTIFICATIONS`, and the WorkManager foreground service merged with `foregroundServiceType="dataSync"`; the API 34+ path accounts for `android.permission.RUN_USER_INITIATED_JOBS`, `android.permission.RECEIVE_BOOT_COMPLETED`, and a non-exported job service protected by `android.permission.BIND_JOB_SERVICE`. The merged WorkManager declarations for `WAKE_LOCK`, `ACCESS_NETWORK_STATE`, `SystemJobService`, foreground service, and rescheduling/boot receivers are inventoried rather than assumed. Service types, exported flags, process, and permissions are asserted from every flavor's merged manifest. Notification denial still leaves an in-app progress/cancel surface and follows platform foreground-task disclosure rules.

App-process instrumentation covers notification states, foreground-start denial/fallback, network loss, low storage, notification and in-app cancellation, retry/backoff, time-limit yield, and idempotent checkpoint resume with exactly one canonical terminal commit. UIDT/foreground quota and eligibility branches are deterministic unit/integration tests against an injected `TransferScheduler`; device tests are platform contract smoke, not attempts to exhaust real production quota.

Reboot, force-stop, and scheduler-stop evidence comes from a host-side multi-stage Gradle/ADB harness because the target instrumentation process cannot survive those actions. Stage A installs a release-like artifact, seeds fixtures, enqueues work, pauses at a named checkpoint, and exports pre-action state. The host then invokes recorded `cmd jobscheduler` stop/timeout/run operations, `am force-stop`, process kill, or emulator reboot and waits for boot/unlock/scheduler readiness. Stage B relaunches a separate verifier, pulls canonical/checkpoint/notification state, and asserts one canonical terminal commit, no concurrently duplicated request under the work-key lease, correct stop reason, and safe reschedule or persisted cancellation. It does not assert exactly-once HTTP delivery: every retry records its reason and byte/range contract, and is permitted only for an idempotent GET, a server-verified Range resume, or a safe whole-item restart whose final bytes/hash replace no known-good file until complete. Non-idempotent account/community mutations never run through this background scheduler. `modernization-matrix.yaml` records exact host commands, API/system image, pre/post report hashes, emulator snapshot IDs, scheduler dump, request/range/retry trace, final file hash, and retained artifact paths. Cancellation propagates to the active OkHttp call and closes partial files; a later retry resumes only from a verified server/range contract or restarts that item safely.

## 10. Compose Material 3 Product Design

### 10.1 Final UI Rule

Every reachable product page uses Jetpack Compose Material 3. XML layouts may remain temporarily only for compatibility entry shells. Final completion requires zero active page layouts, zero Fragment-owned pages, zero AppCompat Toolbar, zero old CardView, zero Material 2 component, zero Material View widget/bridge, and zero `AndroidView`/ViewBinding/DataBinding page bridge in reachable UI. Platform splash, manifest, theme, and non-page resource XML may remain when required by Android. Kotlin-PSI verification rejects `Card`, `ElevatedCard`, or `OutlinedCard` nested in another Material 3 card content lambda; screenshots are not accepted as proof of structural non-nesting.

### 10.2 Top-Level Information Architecture

- Discover: latest, rankings, categories, tags, and completed lists.
- Bookshelf: local and account bookshelf, synchronization, downloads, and reading history.
- Search: active search, search history, results, filters, and pagination.
- Settings: language, app theme, reader preferences, cache, account, migration, wallpaper, and about.
- Novel detail, community, account, image viewer, and reader are subordinate routes.

### 10.3 Adaptive Navigation

- Use Material 3 adaptive `WindowAdaptiveInfo`/window size classes and AndroidX WindowManager `FoldingFeature` posture/occlusion data.
- Compact width (<600dp): Material 3 NavigationBar and one content pane.
- Medium width (600-839dp): NavigationRail; list-detail uses one pane or supporting pane when both meet minimum readable widths.
- Expanded width (>=840dp): permanent drawer or rail with list-detail panes.
- Compact height (<480dp) reduces app-bar/chrome height and prefers rail over a bottom bar when width permits; it never removes the only visible route action.
- Novel detail uses detail/catalog panes when width allows.
- Reader uses a bounded readable text width and a persistent catalog pane when width allows.
- A separating or occluding hinge is a hard content boundary. Text, list rows, controls, dialogs, and reader pages never span it; panes are assigned to physical regions with start-pane-before-detail-pane reading order.
- Window resizing preserves destination, list position, filters, selected novel/chapter, reader cursor, and logical visibility intent. Presentation is remapped: a compact catalog Sheet becomes one expanded catalog pane, never both.
- On compact width, Back closes the catalog Sheet before route navigation. An always-present expanded pane is not closed by Back; Back pops the subordinate route. Focus moves to the selected/current item when a pane appears and returns to the invoking control when a Sheet closes.
- No fixed 300dp/320dp side panels define adaptive behavior.

### 10.4 Auditable UI-Owner Migration Ledger

The baseline has 15 manifest Activities and 6 Fragment UI owners. Shared drawer/list Fragments are not counted as separate product destinations, but their actions and states remain explicit ledger rows.

| ID | Current owner | Final route/component | Disposition |
| --- | --- | --- | --- |
| A01 | MainActivity | AppShell | Replace with single-activity Navigation Compose host |
| A02 | SearchActivity | Search | Replace |
| A03 | SearchResultActivity | Search/results | Replace |
| A04 | NovelInfoActivity | Novel/detail | Replace and split data/download responsibilities |
| A05 | NovelReviewListActivity | Novel/reviews | Replace |
| A06 | NovelReviewNewPostActivity | Novel/reviews/create | Replace and preserve draft/retry state |
| A07 | NovelReviewReplyListActivity | Novel/reviews/thread | Replace |
| A08 | VerticalReaderActivity | Reader/continuous mode compatibility | Implement continuous mode in Modern Reader, then retire Activity |
| A09 | Wenku8ReaderActivityV1 | Reader/paginated mode compatibility | Reach parity, then retire Activity |
| A10 | ModernReaderActivity | Reader | Retain trampoline identity temporarily; move behavior into feature/ViewModel |
| A11 | ViewImageDetailActivity | Image viewer | Replace; retain old `path` trampoline and media controls |
| A12 | UserLoginActivity | Account/login | Replace with captcha-aware flow and registration link |
| A13 | UserInfoActivity | Account/profile | Replace; profile/avatar/logout/check-in capability state |
| A14 | MenuBackgroundSelectorActivity | Settings/wallpaper | Replace |
| A15 | AboutActivity | Settings/about | Replace |
| F01 | LatestFragment | Discover/latest | Replace |
| F02 | RKListFragment | Discover/rankings | Replace custom tabs/ViewPager |
| F03 | NovelItemListFragment | Shared discover/search result list | Absorb all paging/error/empty actions into target routes |
| F04 | NavigationDrawerFragment | AppShell actions | Map navigation, search, account/login, theme, license/about, wallpaper, and offline-default actions individually |
| F05 | FavFragment | Bookshelf | Replace and unify local/account/offline/sync states |
| F06 | ConfigFragment | Settings | Retain language, cache, e-ink, wallpaper, update, notice, and about behavior in Compose |

Phase 0 checks in a machine-readable UI-owner/action ledger derived from Manifest, Fragment creation, click/menu handlers, and layout/menu resources. Every owner, visible action, state, Intent, and retirement decision has a stable ID, target route/component, test ID, and owner. The generated user-route count is reported from this ledger; a hand-maintained number such as 19 is not a completion gate.

The baseline XML surface is independently stable-ID'd; Activity/Fragment ownership is not treated as sufficient classification for a layout. `docs/verification/xml-surface-ledger.yaml` contains exactly these 34 current layout rows and is regenerated from structured resource/source references. A row records reachability roots, kind (`PAGE`, `PAGE_COMPONENT`, `OVERLAY`, `ROW`, or `UNREACHABLE`), final route/component, replacement test ID, and retirement owner. Source drift, an additional layout, or a reachable row without all fields fails Phase 0 and Phase 8.

| ID | Current XML resource | Current owner/kind | Final route/component | Replacement proof | Retirement owner |
| --- | --- | --- | --- | --- | --- |
| X01 | `ad_unified.xml` | Unreferenced ad layout / `UNREACHABLE` | None; advertising UI absent | `UI-X01-NO-AD` | Phase 0 Task 7 |
| X02 | `dialog_progress.xml` | `ProgressDialogHelper` / `OVERLAY` | Material 3 progress/Dialog state | `UI-X02-PROGRESS` | Phase 8 Task 3 |
| X03 | `fragment_config.xml` | F06 / `PAGE` | `settings/root` and subordinate settings | `UI-X03-SETTINGS` | Phase 7 Tasks 8, 14 |
| X04 | `fragment_fav.xml` | F05 / `PAGE` | `library/bookshelf` | `UI-X04-BOOKSHELF` | Phase 4 Tasks 8, 15 |
| X05 | `fragment_latest.xml` | F01 / `PAGE` | `library/discover/latest` | `UI-X05-LATEST` | Phase 4 Tasks 6, 15 |
| X06 | `fragment_novel_item_list.xml` | F03 / `PAGE_COMPONENT` | Shared Material 3 `NovelList` | `UI-X06-NOVEL-LIST` | Phase 4 Tasks 5-7 |
| X07 | `fragment_rklist.xml` | F02 / `PAGE` | `library/discover/ranking` | `UI-X07-RANKING` | Phase 4 Tasks 6, 15 |
| X08 | `layout_about.xml` | A15 / `PAGE` | `settings/about` | `UI-X08-ABOUT` | Phase 7 Tasks 11, 14 |
| X09 | `layout_account_info.xml` | A13 / `PAGE` | `account/profile` | `UI-X09-PROFILE` | Phase 7 Tasks 6, 14 |
| X10 | `layout_main_menu.xml` | F04 / `PAGE_COMPONENT` | AppShell navigation/actions | `UI-X10-SHELL-ACTIONS` | Phase 8 Task 3 after Phase 7 |
| X11 | `layout_main.xml` | A01 / `PAGE` | AppShell/NavHost | `UI-X11-SHELL` | Phase 8 Tasks 3, 5 |
| X12 | `layout_menu_background_selector.xml` | A14 / `PAGE` | `settings/wallpaper` | `UI-X12-WALLPAPER` | Phase 7 Tasks 10, 14 |
| X13 | `layout_novel_chapter_sidesheet.xml` | A04 / `PAGE_COMPONENT` | `novel/catalog` Sheet/pane | `UI-X13-CATALOG` | Phase 5 Tasks 7, 15 |
| X14 | `layout_novel_info.xml` | A04 / `PAGE` | `novel/detail` | `UI-X14-DETAIL` | Phase 5 Tasks 7, 14-15 |
| X15 | `layout_novel_review_list.xml` | A05 / `PAGE` | `novel/reviews` | `UI-X15-REVIEWS` | Phase 5 Tasks 10, 14 |
| X16 | `layout_novel_review_new_post.xml` | A06 / `PAGE` | `novel/reviews/create` | `UI-X16-REVIEW-CREATE` | Phase 5 Tasks 11, 14 |
| X17 | `layout_novel_review_reply_list.xml` | A07 / `PAGE` | `novel/reviews/thread` and reply | `UI-X17-REVIEW-THREAD` | Phase 5 Tasks 10-11, 14 |
| X18 | `layout_reader_swipe_page.xml` | A09 / `PAGE_COMPONENT` | Reader paginated page | `UI-X18-PAGINATED-PAGE` | Phase 6 Task 19 |
| X19 | `layout_reader_swipe_temp.xml` | A09 / `PAGE` | Reader paginated mode | `UI-X19-PAGINATED` | Phase 6 Task 19 |
| X20 | `layout_search_result.xml` | A03 / `PAGE` | `library/search/results` | `UI-X20-SEARCH-RESULTS` | Phase 4 Task 12 |
| X21 | `layout_search.xml` | A02 / `PAGE` | `library/search` | `UI-X21-SEARCH` | Phase 4 Task 12 |
| X22 | `layout_user_login.xml` | A12 / `PAGE` | `account/login` | `UI-X22-LOGIN` | Phase 7 Tasks 6, 14 |
| X23 | `layout_vertical_reader_temp.xml` | A08 / `PAGE` | Reader continuous mode | `UI-X23-CONTINUOUS` | Phase 6 Task 19 |
| X24 | `layout_view_image_detail.xml` | A11 / `PAGE` | `image/viewer` | `UI-X24-IMAGE` | Phase 7 Tasks 12, 14 |
| X25 | `toolbar_main.xml` | A01 / `PAGE_COMPONENT` | AppShell Material 3 app bar | `UI-X25-SHELL-BAR` | Phase 8 Task 3 |
| X26 | `toolbar_pure.xml` | Shared Activities / `PAGE_COMPONENT` | Route-owned Material 3 app bars | `UI-X26-ROUTE-BARS` | Phase 8 Task 3 |
| X27 | `toolbar_search_result.xml` | A03 / `PAGE_COMPONENT` | Search results Material 3 app bar | `UI-X27-RESULT-BAR` | Phase 4 Task 12 |
| X28 | `toolbar_search.xml` | A02 / `PAGE_COMPONENT` | Material 3 SearchBar/DockedSearchBar | `UI-X28-SEARCH-BAR` | Phase 4 Task 12 |
| X29 | `view_novel_chapter_item.xml` | A04 / `ROW` | Catalog `ListItem` | `UI-X29-CHAPTER-ROW` | Phase 5 Task 7 |
| X30 | `view_novel_item.xml` | F03/A04 / `ROW` | Shared Material 3 novel row | `UI-X30-NOVEL-ROW` | Phase 5 Task 15 |
| X31 | `view_review_post_item.xml` | A05 / `ROW` | Review `ListItem` | `UI-X31-REVIEW-ROW` | Phase 5 Task 10 |
| X32 | `view_review_reply_item.xml` | A07 / `ROW` | Reply `ListItem` | `UI-X32-REPLY-ROW` | Phase 5 Task 10 |
| X33 | `view_search_history_item.xml` | A02 / `ROW` | Search history chip/row | `UI-X33-HISTORY-ROW` | Phase 4 Task 7 |
| X34 | `view_tab.xml` | F02 / `PAGE_COMPONENT` | Material 3 `PrimaryTabRow` | `UI-X34-TABS` | Phase 4 Task 6 |

Every X row remains in the ledger after deletion as retirement evidence. A layout may be deleted earlier than its listed final retirement task only when its exact row already has zero-reachability and replacement-test hashes and no rollback implementation references it.

### 10.5 Standard Components

- Scaffold, TopAppBar, SnackbarHost.
- NavigationBar, NavigationRail, modal/permanent drawer.
- LazyColumn, ListItem, and cards only for genuinely grouped/repeated items. Cards are never nested, and page sections are never wrapped in decorative/floating cards.
- Material 3 SearchBar or DockedSearchBar.
- PrimaryTabRow/SecondaryTabRow for ranking/category peer views, FilterChip/InputChip for filters, DropdownMenu for option sets, and Tooltip for unfamiliar icon actions; each has selected, focus, disabled, and overflow semantics.
- Material 3 pull-to-refresh.
- OutlinedTextField, password visibility control, and inline field error.
- Switch, Checkbox, RadioButton, Slider, and SegmentedButton according to semantics.
- AlertDialog, ModalBottomSheet, and Snackbar.
- Circular/LinearProgressIndicator with visible cancellation for long work.
- Standard icons with localized content descriptions.

TextView-like composables do not impersonate buttons. Icon-only controls expose accessible labels. The UI avoids marketing heroes, decorative gradients, glass panels, and card piles.

### 10.6 Shared State Contracts

Lists support:

- InitialLoading
- Content
- Empty
- Refreshing while preserving content
- AppendLoading
- RecoverableError while preserving context
- End

Forms support:

- Idle
- Invalid with field-local errors
- Submitting with duplicate submission disabled
- Success
- Failure with input preserved

Data pages support:

- Loading
- Content
- Empty
- OfflineContent
- Error with retry/recovery
- AuthRequired with a login action

A network, parser, authentication, or storage failure cannot be represented only by a Toast followed by a blank screen.

### 10.7 Reader Requirements

The reader is full-bleed and unframed. Reading palettes may differ from the app color scheme, but controls, sheets, dialogs, typography roles, focus behavior, and semantics remain Material 3.

Required reader behavior:

- Validate missing/invalid launch arguments.
- Load local/cache/remote content with structured failures.
- Preserve ordered text and images.
- Deterministic pagination and reflow after display setting changes.
- Previous/next page and chapter with first/last boundary feedback.
- Catalog grouped by volume, current chapter highlighted and visible.
- Image pending/cached/broken states and image viewer navigation.
- Progress restore, save, process restoration, and old-format compatibility.
- Day/night reading palette, font size, line spacing, paragraph spacing, and margins.
- Visible, discoverable controls; gestures/volume keys are never the only access path.

Reader retirement uses this behavior-level parity matrix:

| Behavior | Final decision |
| --- | --- |
| Horizontal paginated reading | Retain as Modern Reader paginated mode |
| Continuous vertical reading | Retain as Modern Reader continuous mode, not retire as non-parity |
| Direct page/progress seek | Retain with accessible Slider, value text, and jump confirmation where needed |
| Chapter/volume jump and catalog | Retain and improve with current-chapter focus |
| Find text in chapter | Retain with standard SearchBar and result navigation |
| Font size, line spacing, paragraph spacing, page margins | Retain with reader-specific accessible typography tokens |
| Custom font import/reset | Retain; validate file type, copy safely, expose system fallback and failure state |
| Custom reading background import/reset | Retain; maintain contrast scrim/control legibility and reset action |
| Day/night and e-ink/grayscale | Retain as independent reader appearance controls |
| Volume-key paging | Retain as an opt-in setting; visible controls remain available |
| Tap zones/gestures | Retain as optional shortcuts with visible and accessibility alternatives |
| Page transition style currently selected by legacy behavior | Inventory and reproduce the active selected style; any unused style retirement requires a named behavior test and approval |
| Chapter images | Preserve ordered rendering, loading/broken states, and accessible descriptions when meaningful |
| Image viewer zoom, rotate, and save | Retain with standard icon controls, permission/storage recovery, and keyboard/semantics actions |

Material 3 typography roles apply to reader chrome, sheets, dialogs, and metadata. Book content uses a separate `ReaderTypography` scale so user-selected font, line height, paragraph spacing, and margins remain functional while respecting accessibility minimums.

### 10.8 Theme and Tokens

- Complete light and dark schemes.
- Optional dynamic color on Android 12+ with a stable brand fallback.
- Separate semantic error, warning, and success roles.
- Reader night mode is independent from application dark mode.
- Material 3 typography roles, no viewport-scaled fonts.
- 4dp/8dp spacing rhythm, 16dp compact gutters, 24dp expanded gutters.
- Restricted shape scale suitable for an operational reading application.
- No page-level hard-coded brand/error colors after migration.

### 10.9 Accessibility and System Integration

- Touch targets are at least 48x48dp with at least 8dp separation where practical.
- Icon-only controls have localized labels.
- Selected, checked, expanded, disabled, progress, heading, and error semantics are explicit.
- TalkBack, keyboard, DPAD, and Switch Access can complete primary workflows.
- Focus returns predictably after Dialog, Sheet, navigation, and errors.
- Font scale 2.0 does not clip, overlap, or hide actions.
- Light/dark contrast meets WCAG AA for normal text and controls.
- Edge-to-edge and system/IME insets are centrally handled.
- Back handling order is Dialog/Sheet, reader chrome, drawer, route.
- Predictive back is supported.
- Animation respects system animator duration and reduced-motion behavior.

Executable accessibility coverage includes:

- Automated semantics assertions for every route/state, including role, label, heading, state description, collection information, error, live region, and traversal order.
- Manual TalkBack, keyboard, DPAD, and Switch Access journeys for discover/search, detail/catalog, bookshelf/download, reader/catalog/settings/image, account/captcha, application settings, and review create/reply.
- Reader content reading order followed by a discoverable accessibility action to show chrome. Hidden pointer controls never make catalog/settings/page/chapter navigation unreachable.
- Page and chapter changes announce concise position/title changes without repeatedly reading the full page.
- Focus returns to the invoking element after Dialog/Sheet and to the relevant field after validation failure.
- Decorative images are cleared from semantics; meaningful cover/chapter images have localized descriptions or contextual labels.
- IME action, keyboard dismissal, and focus-next order are tested for login, search, review, reply, and settings forms.

Automated semantics, keyboard, and DPAD tests do not stand in for an enabled assistive service. TalkBack and Switch Access journeys are executed on a controlled device and recorded in `docs/verification/manual-assistive-technology-manifest.yaml`. Each row binds the exact journey to assistive service package/version, device/API/build fingerprint, locale/theme/font/navigation/posture configuration, source/app/test-APK SHA-256, tester, independent reviewer, UTC time, report path/hash, and PASS. Phase-specific gates reject missing/stale rows, and Phase 8 reruns the complete matrix after the final `MainActivity` host move; user/account/captcha/content values are absent from evidence.

Predictive-back and inset coverage includes API 36 gesture start/cancel/commit tests for Dialog, Sheet, reader chrome, drawer, subordinate route, top-level destination, and legacy trampoline. Each form and reader-chrome state is tested with IME open, display cutout, gesture navigation, three-button navigation, and compact-height landscape insets.

### 10.10 Localization

- All product strings are resources, not hard-coded Compose text.
- `AppLocale` and `ContentLanguage` are distinct persisted values. The compatibility setting remains an atomic pair: Simplified selects `zh-CN` plus simplified provider content; Traditional selects `zh-TW` plus traditional provider content. A device `zh-HK` locale uses Hong Kong resources with traditional provider content and explicit fallback to `zh-TW`, then default resources.
- Provider/domain cache keys include ContentLanguage; changing UI locale alone does not relabel cached provider content.
- Locale change recreates the activity/host as needed while preserving route, query, selection, and reader position through typed saved state.
- Simplified, Taiwan Traditional, and Hong Kong fallback behavior are tested on every primary route.
- App-bundle language split behavior must not break in-app language switching.
- Long text and font scale are tested together.
- E-ink remains a retained Settings/Reader preference. Manual update checking remains for alpha/baidu sideload flavors; playstore exposes the store-managed update path instead of an in-app package updater.

## 11. Program Phases

Each phase receives its own implementation plan after this specification is approved.

### Phase 0: Baseline Stability and Contract Freeze

Deliverables:

- Guard or remove public-stub reachable crashes.
- Fix CI API level and stale instrumentation tests.
- Add exhaustive Manifest, sender/receiver Intent, Serializable/R8, API signature, artifact/path, backup, and preference manifests plus golden contracts.
- Add deterministic source/UI inventory tasks.
- Enable line and branch coverage reporting.
- Add secret-handling documentation and redaction tests.
- Add the scoped site/content/channel authorization record and a fail-closed build/live-test capability gate.
- Add the source/dependency/asset SBOM, provenance and license ledger, generated notices/source-offer evidence, strict dependency verification/locking, wrapper checksum, repository-origin policy, and reproducible clean-cache/offline build evidence.
- Add the deterministic public-base/protected-private compliance overlay schema, protected merge/egress jobs, redacted signed attestation contract, pinned trust/rotation/revocation manifest, freshness/replay verifier, and negative fixtures.
- Resolve or remove every production `UNKNOWN`/missing/incompatible license, including the active SlidingLayout family.
- Remove Firebase Analytics/Crashlytics, AdMob, advertising-ID declarations, raw sensitive logs, and unaudited legacy egress; add the generated outbound-network manifest and hermetic route egress harness.

Exit gate:

- Public debug build, all unit tests, instrumentation tests, and Lint complete without errors.
- CI uses a device at or above minSdk.
- Compatibility fixtures fail when a frozen contract is intentionally changed.
- Old-signed/minified-to-new-minified Serializable and Intent fixtures pass, including pre-API-33 untyped and API-33+ typed Bundle access; a debug-only round trip cannot satisfy this gate.
- Every scope needed by Phase 2 live observation/account testing and intended release channels is currently `ACCEPTED` in the authorization record; an unknown/expired/rejected scope blocks the dependent plan and artifact.
- Public packaged-source/dependency hashes match public locks, strict verification metadata, SBOM, provenance, licenses, notices, source offer, wrapper/toolchain record, and the verified offline rebuild. Protected private CI proves the equivalent merged overlay gate and publishes only the redacted signed attestation. Zero packaged production entry in either graph has unknown or incompatible licensing.
- The public release-like merged manifest/bytecode contains no analytics/ad/crash SDK or advertising-ID declaration, raw sensitive-log scan passes across retained/new routes, and the full egress harness observes only public-manifest-allowlisted hosts/data classes. Protected private CI injects the audited HostPolicy, runs equivalent static/runtime egress checks against the merged overlay, and exposes only the redacted attestation.
- Private-attestation verification passes binding/freshness/signature checks and fails fixtures for wrong artifact/commit/variant/policy, expired or future time, reused nonce/run ID, stale approval, unknown/revoked key, and schema downgrade.

### Phase 1: Architecture Foundation and Material 3 Shell

Deliverables:

- Foundational contract/model/network/storage/data/design/testing modules.
- AppContainer, dispatchers, ViewModel/StateFlow conventions.
- Material 3 design system and adaptive Navigation Compose shell.
- DataStore-backed preferences/route rollout flags and compatibility trampolines, including migration from existing SharedPreferences/settings files.
- The settings migration safety subset: `LegacySettingsAdapter`, interception of every verified legacy settings writer before snapshot, a writer barrier, durable per-record checkpoint/mutation IDs in excluded transient storage, canonical mutation/projection versions, journal replay, and reconciliation.
- Session-contract interface and provider selection wiring, without account secrets yet.
- Architecture dependency checks.

Exit gate:

- New shell renders compact/medium/expanded and light/dark states.
- Legacy default route remains available.
- Feature code cannot import forbidden legacy infrastructure.
- Settings migration is idempotent and passes interruption at every commit boundary, concurrent legacy-writer, process-death resume, canonical-to-legacy reconciliation, and rollback-build read tests.
- Settings whole-store backup/restore proves that canonical mutation/projection versions rebuild a missing or stale legacy projection without the excluded transient journal.

### Phase 2: Complete Public Provider

Deliverables:

- Anonymous catalog, search, detail, catalog, chapter, image, and review reads.
- Captcha login, encrypted SessionStore, session epoch, profile, bookshelf, recommendation, review creation, and reply operations.
- The credential/session migration safety subset: a legacy credential adapter intercepts all verified `cert.wk8` writers before snapshot, records a non-secret scrub checkpoint/mutation ID in excluded transient storage, reconciles interrupted new-session persistence and legacy secret scrubbing, and never imports a plaintext password.
- Daily check-in remains hidden unless an accepted HTTPS operation evidence record enables that capability.
- GBK codec, parsers, CookieJar, typed failures, cache, request coalescing, and redaction.
- Public/private shared provider contract suite.
- Replace the throwing public api-stub with the non-throwing legacy API bridge; retain the bridge ABI for old routes.

Exit gate:

- Public checkout completes search -> detail -> catalog -> reader against deterministic server fixtures.
- Opt-in live read-only smoke verifies login/profile/bookshelf/logout with runtime credentials.
- No normal visible public action throws `UnsupportedOperationException`.
- Public provider and bridge are selected explicitly; public/private debug and minified-release selection assertions pass.
- Credential migration passes interruption/process-death tests before and after new-session persistence and legacy scrubbing. Missing/invalid Keystore and rollback-build tests prove a deterministic signed-out state with no password recovery attempt.
- Verified removal or password-only rewrite of a password-bearing `cert.wk8` after successful new authentication is the sole secret-scrubbing exception to the general legacy-artifact no-deletion rule.
- Backup/device-transfer tests prove SessionStore, Cookies, `cert.wk8`, and credential-migration transient state are excluded and the restored application starts signed out.

### Phase 3: Storage and Compatibility Migration

Deliverables:

- Room stores and remaining domain migration stores; Phase 1 preferences and Phase 2 SessionStore are integrated rather than recreated.
- Legacy codecs, path policy, importer, dual-write, checkpoint, and recovery.
- The Section 9.7 bounded-work scheduler, WorkManager/foreground/user-initiated transfer policy, merged-manifest declarations, durable progress, and cancellation/recovery behavior.

Exit gate:

- Golden semantic/byte compatibility passes.
- Upgrade, interruption, retry, process death, rollback, scheduler selection, quota/stop, and cancellation tests pass on API 23/29/30/31/32/33/34/35/36.
- The host-side multi-stage ADB harness passes scheduler stop, process kill, force-stop, and reboot scenarios with retained pre/post hashes, exactly one canonical terminal commit, no concurrent duplicate request, and only recorded idempotent/range/safe-whole-item retry requests.
- The backup/restore harness passes the post-canonical/pre-legacy-projection capture point and records its backup boundary, fixture hash, restore result, and report path in `docs/verification/modernization-matrix.yaml`.
- No non-secret legacy artifact is deleted. The Phase 2 verified `cert.wk8` password-scrubbing exception remains permitted and auditable.

### Phase 4: Discovery, Search, Bookshelf, and Shell Default

Deliverables:

- Discover/latest/rankings/categories/tags.
- Search/history/results/filters.
- Local/account bookshelf, synchronization, downloads, and reading history.
- Adaptive shell becomes default after parity.

Exit gate:

- Loading/content/empty/offline/error/paging states are tested.
- Compact and expanded screenshot matrices pass.
- Public provider completes the real anonymous discovery journey.

### Phase 5: Novel Detail, Downloads, and Community

Deliverables:

- Detail/catalog/favorite/download routes.
- Reviews, thread, create, and reply routes.
- NovelInfoActivity network/parser/cache/download responsibilities removed.

Exit gate:

- Detail/catalog dual-pane behavior passes.
- Download cancellation/retry/process-death behavior passes.
- Failed review submission retains input and permits retry.

### Phase 6: Reader Consolidation

Deliverables:

- Reader ViewModel owns state and structured concurrency.
- Complete catalog, images, navigation, settings, progress, and restoration behavior.
- V1 and vertical reader user entries retired after parity.

Exit gate:

- Reader journey passes local, remote, offline, missing argument, first/last chapter, image, reflow, rotation, and process death cases.
- Legacy progress and settings compatibility pass.
- Phone/tablet, light/night, and font-scale screenshots pass.

### Phase 7: Account and Secondary Surfaces

Deliverables:

- Login/captcha/profile/logout and capability-gated check-in state.
- Settings/theme/language/cache/migration/wallpaper/about.
- Image viewer.

Exit gate:

- Auth/session expiry and recovery pass.
- Permission denial and storage failures are recoverable.
- All remaining current surfaces map to Compose routes.

### Phase 8: Legacy Removal and Release Audit

Deliverables:

- Delete unreachable Activity/Fragment/XML/custom navigation/legacy reader UI.
- Delete the legacy API bridge only after zero production imports of its ABI; the throwing api-stub was already removed in Phase 2.
- Remove AsyncTask, findViewById, new-business GlobalConfig access, and transition facades.
- Complete physical module extraction and dependency audit.
- Decide the end of legacy dual-write only after compatibility-window evidence.
- Produce and verify signed minified release artifacts through the external signing pipeline.

Exit gate:

- The Definition of Done in Section 16 is proven item by item.

## 12. Test Strategy

### 12.1 Test Layers

- Pure model, policy, parser, codec, paginator, mapper, and use-case unit tests.
- MockWebServer request/response, redirect, header, encoding, Cookie, and redaction tests.
- Room/DataStore/legacy filesystem repository integration tests.
- Migration golden, interruption, retry, upgrade, and rollback tests.
- Background scheduler-selection, chunk/checkpoint, quota/stop-reason, foreground/user-initiated transfer, notification, cancellation, and process/reboot recovery tests.
- Host-side multi-stage Gradle/ADB scheduler-stop, process-kill, force-stop, and emulator-reboot journeys with separate seed/verify processes and retained pre/post evidence.
- UI route process-death journeys use separate `seed` and `verify` instrumentation invocations around a host-issued `adb shell am kill`; they prove a changed PID and restored route/selection/scroll/input-safe state. Activity recreation in one instrumentation process is not process-death evidence.
- ViewModel state/effect tests with fake dispatchers and clocks.
- Compose UI tests for every route and state family.
- Deterministic screenshot tests for layout, typography, theme, and adaptive behavior.
- API 23/29/30/31/32/33/34/35/36 device journeys.
- Opt-in read-only live-site smoke tests.
- Explicitly gated reversible and persistent live mutations.
- License/SBOM/provenance/dependency-verification, authorization freshness, packaged notice/source-offer, sensitive-log, merged-manifest, and hermetic outbound-egress gates.

### 12.2 Coverage Gates

| Layer | Line | Branch |
| --- | ---: | ---: |
| Parser, storage codec, migration | 90% | 80% |
| Repository, use case, ViewModel | 80% | 70% |
| Overall production logic | 70% | 60% |

Screenshot counts do not substitute for logic assertions.

Coverage includes production Kotlin/Java classes in public/private provider adapters, core model/domain/network/session/storage/data, and feature ViewModels/use cases/pure UI logic. It excludes only generated R/BuildConfig/Compose compiler classes and generated Room/serialization glue identified by exact class patterns in the checked-in verification manifest. It does not exclude parsers, models with behavior, adapters, legacy codecs, or error branches. Reports aggregate per module and for the whole production graph; a module cannot hide below its layer threshold behind another module's tests.

### 12.3 UI State Coverage

Every route has at least:

- One successful-content test.
- One loading test.
- One empty or not-applicable-state test.
- One recoverable-error test.
- Auth/offline tests where the route supports those states.
- Back/recovery verification for Dialogs and Sheets.
- Automated semantics assertions and the manual assistive-technology journey IDs assigned by the UI-owner ledger.

### 12.4 Screenshot Matrix

Minimum viewports:

- 360x640 compact phone.
- 412x915 common phone.
- 915x412 landscape.
- 800x1280 tablet.
- 1280x800 expanded/resizable.

Configurations:

- Simplified and Traditional Chinese.
- Light and dark.
- Font scale 1.0, 1.3, and 2.0.
- Large display size.
- Gesture and three-button navigation.
- Animations enabled and disabled.
- E-ink/grayscale setting where retained.

`docs/verification/ui-golden-manifest.yaml` is the authoritative coverage gate. Each case records stable case ID, UI-owner/route ID, state, overlay, viewport, window posture/occlusion, locale, content language, theme, font scale, display scale, navigation mode, reader mode, fixture hash, baseline image hash, pixel/structural tolerance, permitted dynamic masks, and approval commit.

All screenshot cases use the single AndroidX Compose UI instrumentation pipeline defined by the plan index. Recording produces host-side candidates only; a separate reviewed approval command creates baselines, and verification always captures fresh pixels without modifying approved files. Baselines live at `docs/verification/ui-goldens/<case-id>.png`; candidate/actual files live only under `app/build/reports/ui-goldens/`. The manifest verifier decodes every PNG, rejects blank/flat images, recomputes hashes, and rejects any task/report that did not pass through the registered host extraction pipeline.

Mandatory combinations:

- Every route: successful content at compact `zh-CN`, light, font 1.0.
- Every applicable route: loading, empty, recoverable error, offline, and auth-required at compact baseline.
- Every route: expanded successful-content smoke.
- Every form: IME-open compact and compact-height landscape.
- Every top-level route: medium navigation and API-36 gesture navigation.
- Every route: dark plus font 2.0, including at least one Traditional/Hong Kong fallback case.
- Reader: paginated and continuous modes crossed with light/night/e-ink, font 1.0/2.0, catalog Sheet/pane, image pending/cached/broken, first/last chapter, and hinge posture cases.
- Image viewer: zoom, 90-degree rotation, save success, permission/storage failure.
- Foldable/list-detail routes: separating hinge, occluding hinge, resize Sheet-to-pane, and pane focus/back cases.

Remaining non-mandatory configuration combinations use a checked-in pairwise set generated deterministically from the manifest. A raw screenshot count cannot pass the gate. Baselines require human approval; unexplained masking, tolerance increases, or baseline replacement fails review.

### 12.5 Primary Runtime Journeys

1. Launch -> discover -> search -> detail -> catalog -> read -> progress restore.
2. Offline launch -> bookshelf -> cached detail -> cached chapter -> image.
3. Captcha login -> profile -> bookshelf read -> logout.
4. Reversible bookshelf add/remove with initial-state restoration when explicitly enabled.
5. Download selection -> progress -> cancellation -> retry -> offline read.
6. Review list -> auth-required -> login -> compose failure -> retry, using deterministic server fixtures by default.
7. Old installation data -> interrupted migration -> resume -> new route -> rollback-compatible old read.

## 13. CI and Quality Gates

### 13.1 Pull Request Gates

- Public provider build and tests.
- Unit and integration suites.
- Lint with zero errors and a ratcheting warning baseline.
- Coverage thresholds.
- Architecture dependency checks.
- Secret/redaction checks.
- Compose UI and selected screenshot tests.
- Authorization-scope freshness, SBOM/provenance/license/notice/source-offer consistency, dependency locks/origins/checksums, wrapper/toolchain verification, and clean-cache-to-offline reproducibility.
- Full source/merged-manifest/packaged-bytecode sensitive-log and outbound-network inventory plus hermetic egress tests.
- When private source is available, protected overlay merge, audited HostPolicy injection/equivalent egress enforcement, full private compliance gates, and redacted signed-attestation schema/non-disclosure/binding/freshness/replay/key-rotation tests.
- API 31 and 34 scheduler/manifest smokes run on pull requests; destructive multi-stage force-stop/reboot host journeys run on the controlled nightly and every signed release candidate and remain Phase 3/release gates.
- `git diff --check`.
- Source/UI inventory checks.

`docs/verification/modernization-matrix.yaml` maps every Definition-of-Done requirement to exact Gradle task/test ID, provider, flavor, build type, API/device/configuration, fixture or baseline hash, threshold, retained report path, and commit SHA. A green task not listed for the requirement is not completion evidence.

### 13.2 Device Matrix

- API 23: minimum SDK and legacy permission/file behavior.
- API 29: final legacy external-storage behavior.
- API 30: scoped-storage migration boundary.
- API 31: foreground-service start restrictions and expedited-work fallback.
- API 32: final READ_EXTERNAL_STORAGE behavior and pre-Tiramisu Serializable/permission boundary.
- API 33: typed Serializable Bundle access and Tiramisu permission boundary.
- API 34: foreground-service type/start restrictions and user-initiated data-transfer eligibility.
- API 35: data-sync foreground limits, quota/stop-reason recovery, and edge-to-edge enforcement boundary.
- API 36: target behavior, current background-work/notification rules, predictive back, and current permission model.

### 13.3 Provider Matrix

- Public provider always builds and runs contract tests.
- A protected private CI environment runs the same contract suite against the private adapter.
- Private CI injects its complete compliance/dependency/outbound overlay and an audited HostPolicy (or proves an equivalent fail-closed egress boundary), runs the same-strength static/runtime and supply-chain gates, and returns a fresh release-candidate-bound redacted signed attestation plus approved test summaries. Public CI verifies it offline against the pinned trust/revocation manifest and retained nonce/run registry.
- Private CI results must not expose endpoint/configuration secrets, private coordinates, credentials, response bodies, or protected compliance rows.
- Public completion cannot be inferred from private-provider success.

Required build variants:

- Public `alphaDebug`: full deterministic suite on every pull request.
- Public `baiduDebug` and `playstoreDebug`: compile, resource, manifest, and route-capability smoke.
- Public minified `alphaRelease`, `baiduRelease`, and `playstoreRelease`: only scopes currently accepted for that channel may enable the public provider; then assemble, R8 compatibility, provider identity, legacy Serializable/Intent, and install/launch smoke after external signing. A missing/expired approval fails configuration rather than silently substituting a fixture or disabled provider.
- Private `alphaDebug` and minified `alphaRelease`: protected build plus shared provider/legacy ABI contract suite when private source is available.
- `-Pwenku8Provider=private` without private source and any unknown provider value must fail configuration.

Public release evidence retains APK/AAB SHA-256, mapping file hash, signing certificate digest, `apksigner verify` output, install/upgrade journey result, test report, screenshot manifest result, authorization-record hash, SBOM/provenance/license/notice/source-offer hashes, resolved dependency origin/checksum graph, wrapper/toolchain hashes, outbound manifest/egress result, and source commit SHA. Equivalent private details remain protected; shared evidence retains only the freshly verified bound attestation, trust/key ID and revocation-manifest hash, nonce/run-registry result, protected encrypted-report digest, artifact hash, and approved non-sensitive test summary.

### 13.4 Live Tests

- Not part of deterministic PR CI.
- Disabled unless the precise live-observation/account/mutation scope is currently accepted by the site/content authorization gate.
- Read-only smoke runs manually or on a controlled schedule.
- Rate-limited and single-threaded unless an endpoint requires otherwise.
- Persistent mutations require explicit per-run authorization.
- A live-site change creates a parser fixture and contract revision before implementation repair.

## 14. Rollout and Rollback

### 14.1 Route Rollout

- Each replacement route has a local rollout flag.
- A debug/developer route switch permits old/new comparison during migration.
- Release defaults are build-time reviewed configuration and change only after the phase exit gate.
- Old code remains reachable only for rollback until the retirement gate.
- Local flags are not presented as a production remote kill switch and cannot be used to claim post-release rollback.

### 14.2 Data Rollback

- Initial migrations copy/import; they do not destroy the sole old data source.
- Dual-write maintains rollback compatibility during the declared window.
- A failed new-store write reports an error and does not corrupt the old store.
- Non-secret old-file deletion is a separately approved operation after end-to-end evidence. The only earlier exception is the verified, auditable `cert.wk8` password removal/rewrite after successful new authentication; rollback then starts signed out.

### 14.3 Provider Rollback

- Public parser revisions are versioned and the immediately previous accepted parser remains selectable for the compatibility window.
- Bad parser results never overwrite known-good durable chapter content.
- A route can use stale valid cache with a visible offline/stale indicator.
- Provider rollout does not change private provider selection semantics.

### 14.4 Emergency Production Rollback

- The operational rollback is a higher-versionCode, signed forward-fix release using the same application ID and signing certificate; Android binary downgrade is not assumed.
- Release preparation retains the previous route implementation, provider parser revision, and legacy read projection until the compatibility window closes.
- A release manifest records which previous route/parser revision the emergency build will select.
- The pipeline tests previous signed APK -> signed release candidate -> higher-version rollback build on populated legacy data.
- Any confirmed data loss/corruption, inability to open previously downloaded content, authenticated cross-account data exposure, or migration journal that cannot reconcile is an immediate rollout abort and blocks legacy retirement.

### 14.5 Compatibility Window and Retirement Approval

- A migrated domain remains in dual-write/legacy-readable operation for at least two successive signed production releases and 30 calendar days, whichever is longer.
- The local-only `MigrationDiagnostics` contract from Section 8.12 records migration started/completed/failure enum, reconciliation enum, and pending-journal bucket. A controlled pilot user may preview and manually export that redacted report; there is no automatic collection, background upload, advertising identifier, stable installation identifier, or third-party sink.
- Retirement requires zero confirmed data-loss/security incidents, no unresolved P1 migration defect, a 100% reconciliation result in release qualification fixtures, and no exported controlled-pilot report with a persistent pending-journal bucket above the documented transient threshold. The audit records pilot size, release coverage, opt-in/export procedure, and the limits of that evidence without attempting cross-report identity correlation.
- `docs/verification/phase-8-retirement-audit.md` names the maintainer owner, window start/end releases and dates, evidence links, incident review, and explicit approval for each domain.
- If sufficient explicit-export production evidence is unavailable, legacy reads/writes are retained; absence of reports is not proof of safety.

## 15. Risks and Mitigations

| Risk | Mitigation |
| --- | --- |
| Wenku8 HTML changes | Pure versioned parsers, minimal fixtures, typed parse failures, opt-in smoke tests |
| Site/API/content/channel authorization is absent or changes | Fail-closed scoped approval record; fixture-only work while unknown; live/publication/release gates revalidate dates and scope |
| Captcha/session complexity | User-entered captcha, standards-compliant CookieJar, explicit session validation |
| Wild GPLv3 contamination | Behavioral-spec-only boundary and independently authored implementation/tests |
| Unknown or incompatible repository/asset license | Source/asset provenance ledger, packaged SBOM/notices/source offer, zero-unknown Phase 0 and release gates |
| Dependency mirror/upstream compromise or drift | Central origin/content filters, strict verification and locks, wrapper checksum, clean-cache/offline reproducibility and provenance |
| Legacy telemetry/logging leaks user or content data | Remove analytics/ads/crash SDKs and AD_ID, redact all routes, generated outbound manifest, hermetic egress tests |
| Legacy save corruption | Golden fixtures, per-domain idempotent import, atomic single-file replacement, durable mutation journal/reconciliation, no initial deletion |
| Private provider unavailable publicly | Shared contract and protected private CI; no unsupported compatibility claims |
| Large migration scope | Vertical slices, route flags, per-phase plans, phase exit gates |
| UI style without behavior parity | State contracts, Compose UI tests, runtime journeys, screenshot matrix |
| Modern Reader regressions | Existing pure tests plus parity matrix before old-reader retirement |
| Live tests mutate user data | Read-only default, reversible/persistent mutation gates, explicit confirmation |
| Credentials leak | Environment-only secrets, no password persistence, redaction and secret checks |
| Background limits stop migration/download work | Bounded idempotent chunks, durable checkpoints, API-aware ordinary/foreground/user-initiated scheduling, cancellation/quota/stop recovery tests |
| Over-modularization before decoupling | Package boundaries first, physical extraction only after acyclic dependencies |
| AVD instability hides visual defects | Reproducible emulator harness, retained screenshots, real-device fallback evidence |

## 16. Definition of Done

The modernization program is complete only when current evidence proves every item below.

### 16.1 Product and Provider

- Public checkout completes all reachable anonymous content journeys.
- Public provider implements captcha login, session, profile, bookshelf, recommendation, review creation, and reply contracts.
- Live/provider-publication/distribution claims are made only for scopes and channels currently accepted by the site/content/distribution approval record; fixture-only behavior is not reported as live completion.
- Daily check-in is implemented only for a provider with an accepted HTTPS contract and is otherwise absent from reachable UI.
- Normal visible public actions never use `UnsupportedOperationException` as a result.
- Public and private providers pass the shared provider contract suite.
- Offline cached reading works after process restart.

### 16.2 Compatibility

- Application ID, launcher, Intent, Serializable, path, save, cache, language, and provider-selection contracts pass.
- Old installations upgrade without losing bookshelf, progress, settings, downloaded chapters, or images.
- Interrupted migration resumes safely.
- Rollback during the compatibility window can still read old data.
- Legacy writes are stopped only after approved compatibility-window evidence.

### 16.3 Architecture

- Target modules exist with an acyclic dependency graph.
- Feature modules do not import forbidden legacy/network/storage implementations.
- Reachable UI contains zero AsyncTask and zero raw executor/thread ownership.
- Reachable UI contains zero findViewById.
- New business paths contain zero direct GlobalConfig access.
- Activities and composables contain no network, parser, cache, or file orchestration.
- Route state is ViewModel/StateFlow-owned and process restoration is tested.
- Durable migration/download work follows the bounded scheduler contract, survives process/reboot/quota stops with exactly one canonical terminal commit, permits only recorded safe at-least-once network retries, and remains cancellable on API 23-36.

### 16.4 UI and Accessibility

- Every stable Activity/Fragment/action ID in the checked-in UI-owner ledger maps to a Compose Material 3 route/component or documented retirement evidence.
- Every X01-X34 XML-surface row maps to a tested Compose Material 3 replacement or exact-file retirement evidence.
- Reachable page XML, Fragment pages, AppCompat Toolbar, old CardView, Material 2 component, Material View/`AndroidView` bridge, and nested Material 3 card count are zero.
- Compact, medium, and expanded navigation work.
- Light/dark, Simplified/Traditional Chinese, and font scale 2.0 pass without clipping or overlap.
- TalkBack, keyboard, DPAD, and Switch Access complete the mandated discover, detail, bookshelf/download, reader, account, settings, and community journeys; final TalkBack/Switch Access rows are current, independently reviewed, and hash-bound to the final source and artifacts.
- Every route has loading/content/empty/error/offline/auth states where applicable.
- Reader parity and screenshot gates pass before old reader entries disappear.

### 16.5 Quality and Release

- Public build, unit, integration, parser, migration, provider, Compose UI, screenshot, and device journeys pass.
- CI device APIs are compatible with minSdk.
- Coverage meets Section 12.2.
- Lint has zero errors and zero unexplained warnings in active source. Any intentional toolchain/version warning has a narrow documented suppression and owner.
- A release pipeline injects signing material outside the repository and verifies the signed artifact; no signing key is committed.
- Every enabled live operation and distribution channel has a current accepted authorization scope; expiry or policy drift fails closed.
- Public packaged source/dependencies/assets have zero unknown or incompatible licenses and match the approved public SBOM, provenance, dependency locks/verification/origins/checksums, notices, source offer, wrapper/toolchain hashes, and reproducible offline rebuild. Protected private CI proves the complete merged equivalent and supplies only its signed redacted attestation.
- The public packaged outbound manifest and hermetic egress report prove zero third-party analytics/ad/crash SDK traffic, advertising-ID access, unaudited destination, or sensitive payload/log field across retained and new routes. Protected private CI proves equivalent HostPolicy/static/runtime enforcement without disclosing its overlay.
- Every private completion/release claim uses a fresh non-replayed attestation bound to the exact provider, variant, source commit, artifact, policy revision, current approval window, CI run/nonce, and trusted non-revoked signing key.
- No credential value, Cookie value, captcha value, or raw user/content body appears in any source, artifact, log, screenshot, or report. Private endpoints never appear in the public checkout, public artifacts, logs, screenshots, or shared evidence; protected private source/artifacts may contain only their audited configuration and remain inside protected CI.
- No Critical or Important independent review finding remains open.
- The final completion audit links every requirement to authoritative test, source, report, screenshot, or runtime evidence.

## 17. Specification and Plan Review Protocol

Before implementation planning:

1. Self-review this specification for placeholders, contradictions, ambiguous terms, and unsupported scope claims.
2. Commit the self-reviewed specification as the stable review baseline.
3. Run an independent architecture/API/licensing review.
4. Run an independent Material 3/navigation/accessibility review.
5. Run an independent migration/testing/compatibility review.
6. Resolve every Critical and Important finding in this specification.
7. Commit review-driven amendments.
8. Ask the user to review the committed specification.

After user approval, write one implementation plan per phase:

- Phase 0 baseline and contracts.
- Phase 1 architecture and design-system foundation.
- Phase 2 complete public provider.
- Phase 3 storage and compatibility migration.
- Phase 4 discovery/search/bookshelf and shell.
- Phase 5 detail/download/community.
- Phase 6 reader consolidation.
- Phase 7 account and secondary surfaces.
- Phase 8 legacy removal and release audit.

Plans use TDD steps, exact files, exact commands, expected failures/passes, frequent commits, and subagent-driven execution with review between tasks.
