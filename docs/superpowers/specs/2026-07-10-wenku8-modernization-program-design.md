# Wenku8 Android Modernization Program Design

**Status:** Draft for independent review; product direction approved by the user on 2026-07-10

**Repository baseline:** `4da90cec8d1fb975c54ab50455c8886f34f44bd1`

**Active product:** `studio-android/LightNovelLibrary`

**Behavioral reference:** `niuhuan/wild@ead92b718ef6707aa3e2519f05b103aa2761185e`

## 1. Executive Summary

This program replaces the active Android application's legacy-centered architecture with a modular, coroutine-based, offline-capable Kotlin architecture and migrates every reachable user interface to native Jetpack Compose Material 3.

The migration uses a vertical-slice strangler strategy. New routes, repositories, public API sources, and storage adapters coexist with legacy code until each user journey reaches behavioral parity and passes compatibility, runtime, accessibility, and screenshot gates. A route becomes the default only after its replacement is proven. Legacy data is never destructively rewritten as the first migration action.

The public checkout must become a functional application rather than a build-only stub. It will provide independently authored implementations for anonymous browsing and reading, captcha login, session validation, account information, bookshelf operations, daily check-in, recommendations, review browsing, review creation, and replies. Wild is a behavioral reference only. No Wild source, selector set, test, comment, error message, or distinctive parsing flow may be copied, translated, or mechanically rewritten.

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
- Support captcha-based login, account state, bookshelf synchronization, daily check-in, recommendations, review browsing, posting, and replies through the public provider.
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
- Deleting legacy save files during initial migration.
- Preserving every implementation detail of the old UI when it conflicts with accessibility, recovery, or adaptive layout requirements.

## 6. Compatibility Invariants

### 6.1 Application and Android Boundaries

- Keep `applicationId` equal to `org.mewx.wenku8`.
- Keep launcher behavior and existing exported-component policy unless a security correction is separately documented and tested.
- Preserve old Activity class names as compatibility trampolines while an internal or external caller can still target them.
- Preserve back-stack semantics for existing user journeys during route rollout.
- Preserve current supported language identities and Simplified/Traditional Chinese selection behavior.

### 6.2 Intent Contracts

The compatibility codec must continue to accept these extras and their current meanings:

| Contract | Required values/semantics |
| --- | --- |
| Reader launch | `aid`, `cid`, `from`, `forcejump`, `volume`, `volumes` |
| Local reader source | `from == "fav"` means local/cache-first behavior |
| Novel detail | `aid`, `from`, `title` |
| Image viewer | `path` |
| Search and reviews | Existing query, novel, review, and reply string extras |

`LegacyIntentCodec` owns decoding, validation, defaulting, and conversion into typed route arguments. No new route reads raw extras directly.

### 6.3 Serializable Boundary Types

Keep the package name, class name, Serializable identity, and public field compatibility of boundary DTOs such as `VolumeList` and `ChapterInfo` until all senders and receivers have migrated and round-trip tests prove retirement is safe.

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
| `avatar.jpg` | Cached account avatar |
| `saves/intro` | Novel metadata and introductions |
| `saves/novel/{cid}.xml` | Cached chapter XML |
| `saves/imgs` | Covers and chapter images |
| `/wenku8/` | Legacy external root |
| `filesDir/` | Current internal root |
| `.migration_completed` | Existing migration sentinel |

Primary/backup lookup order, internal/external fallback behavior, and migration sentinel behavior must be locked with golden tests before storage code changes.

### 6.5 Provider Selection

The logical module remains named `:api`.

- If a valid private `api/build.gradle` exists, Gradle maps `:api` to the private provider.
- Otherwise Gradle maps `:api` to the new public provider directory.
- Both providers depend on `:api-contract`.
- The current `api-stub` remains only until the public provider covers all mandatory contract operations, then it is deleted.

## 7. Target Architecture

### 7.1 Target Modules

| Module | Responsibility | Allowed dependencies |
| --- | --- | --- |
| `:app` | Manifest, application composition root, Navigation Compose host, external compatibility trampolines | Feature entry APIs, core design system, data/storage/provider implementations needed for composition |
| `:api-contract` | Provider interfaces, wire-independent request/response models, typed failures | `:core:model` only |
| logical `:api` | Selected public or private provider implementation | `:api-contract`, network/parser implementation support |
| `:core:model` | Stable domain identifiers and immutable models | Kotlin/Java standard library only |
| `:core:domain` | Repository interfaces and application use cases shared by features | `:core:model` and `:api-contract` where provider capability types are required |
| `:core:network` | OkHttp clients, GBK codec, CookieJar, request throttling, redaction | `:core:model` |
| `:core:storage` | Room, DataStore, encrypted session storage, legacy codecs and path policy | `:core:model` |
| `:core:data` | Repository implementations, offline policy, cache coordination, provider adapters | domain, contract, network, storage, model |
| `:core:designsystem` | Material 3 theme, tokens, shared composables, adaptive primitives | Compose Material 3 |
| `:core:testing` | Fixtures, fakes, test dispatchers, screenshot configuration | model and test libraries |
| `:feature:library` | Discovery, latest, ranking, categories, search, bookshelf, history | model, repository interfaces, design system |
| `:feature:novel` | Detail, catalog, collection, downloads, reviews | model, repository interfaces, design system |
| `:feature:reader` | Modern Reader, pagination, catalog, images, settings, progress | model, repository interfaces, design system |
| `:feature:account` | Captcha login, session, profile, check-in | model, repository interfaces, design system |
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

### 8.2 Provider Contracts

```kotlin
interface Wenku8CatalogSource {
    suspend fun home(): ApiResult<List<HomeSection>>
    suspend fun browse(request: BrowseRequest): ApiResult<Page<NovelSummary>>
    suspend fun tags(request: TagRequest): ApiResult<Page<TagSummary>>
    suspend fun search(query: SearchQuery): ApiResult<Page<NovelSummary>>
    suspend fun novel(key: NovelKey): ApiResult<NovelDetail>
    suspend fun catalog(key: NovelKey): ApiResult<List<Volume>>
    suspend fun chapter(key: ChapterKey): ApiResult<ChapterDocument>
}

interface Wenku8AccountSource {
    suspend fun beginLogin(): ApiResult<CaptchaChallenge>
    suspend fun login(request: LoginRequest): ApiResult<SessionState>
    suspend fun validateSession(): ApiResult<SessionState>
    suspend fun profile(): ApiResult<UserProfile>
    suspend fun avatar(): ApiResult<BinaryResource>
    suspend fun dailyCheckIn(): ApiResult<CheckInResult>
    suspend fun bookshelf(): ApiResult<List<BookshelfGroup>>
    suspend fun updateBookshelf(command: BookshelfCommand): ApiResult<Unit>
    suspend fun recommendNovel(key: NovelKey): ApiResult<RecommendationResult>
    suspend fun logout()
}

interface Wenku8CommunitySource {
    suspend fun reviews(key: NovelKey, page: Int): ApiResult<Page<ReviewSummary>>
    suspend fun reviewThread(key: ReviewKey, page: Int): ApiResult<Page<ReviewPost>>
    suspend fun createReview(command: CreateReviewCommand): ApiResult<ReviewKey>
    suspend fun reply(command: ReplyCommand): ApiResult<ReviewPostKey>
}
```

The public provider implements every operation required by a reachable product route. A provider may return `Unsupported(feature)` only for a capability that the product has explicitly hidden for that provider. A normal visible action cannot terminate in `UnsupportedOperationException`.

### 8.3 Stable Models

- `NovelKey` contains `sourceId` and `remoteId: String`.
- `ChapterKey` contains a `NovelKey` and remote chapter ID.
- `Page<T>` contains items, current page, and nullable next page.
- `ChapterDocument` contains ordered text, image, and semantic break blocks.
- `NovelDetail` contains normalized metadata and controlled rich text/plain text, never raw executable HTML.
- `BinaryResource` contains bytes, media type, canonical source, and cache metadata.
- Provider models never expose Cookie, HTML DOM nodes, ContentValues, or Android UI types.

### 8.4 Public Behavior Surface

| Capability | Public behavior |
| --- | --- |
| Home | `GET /index.php?charset=gbk` |
| Tags | `GET /modules/article/tags.php` with paging parameters |
| Novel detail | `GET /modules/article/articleinfo.php?id={aid}&charset=gbk` |
| Catalog | `GET /modules/article/reader.php?aid={aid}&charset=gbk` |
| Chapter | `GET /novel/{aid/1000}/{aid}/{cid}.htm` |
| Rankings/completed | `toplist.php` and `articlelist.php` |
| Search | `search.php` with GBK percent encoding for query text |
| Reviews | `reviews.php` and associated review-thread forms |
| Images | Independent request with an HTTPS Wenku8 Referer |
| Login | Homepage/login prewarm, captcha image, then form POST with cookie session |
| Bookshelf mutations | Authenticated forms; success may be represented by redirect behavior |

All production endpoints are HTTPS and restricted to an explicit host allowlist. Cleartext fallback is prohibited.

### 8.5 HTTP and Encoding

- Use OkHttp with separate anonymous and authenticated clients that share safe connection resources but not mutable test state.
- Decode Wenku8 pages using the declared/known GBK contract with explicit decode failures.
- Encode Chinese search input as GBK bytes before percent encoding.
- Use Jsoup DOM parsing; do not use a whole-page regular expression parser.
- Normalize relative URLs against the validated HTTPS base URL.
- Attach Referer only to approved Wenku8 image hosts.
- Set conservative connect/read/call timeouts and map them to distinct network failures.
- Limit concurrency and apply request throttling to avoid abusive behavior.
- Use conditional requests where the site supports them.
- Follow redirects for normal navigation; use a non-following request when a mutation's success contract requires observing a redirect status.

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

1. Prewarm the home and login pages.
2. Request `/checkcode.php` and return a `CaptchaChallenge` containing image data and challenge/session context.
3. Collect captcha text from the user. The application never automates captcha solving.
4. Submit username, password, captcha, cookie duration, and action through the authenticated client.
5. Validate login through response semantics and a valid, unexpired session Cookie.
6. Validate session before mutations after process restoration or a stale interval.
7. Treat a 200 login page, block page, expired Cookie, or challenge page as authentication/session failure rather than business content.
8. Logout clears in-memory and persisted session Cookies and invalidates authenticated caches.

Cookie persistence must honor domain, path, secure, expiry, and logout semantics. Passwords are never persisted.

### 8.8 Test Credential Handling

The user has authorized a dedicated test account for implementation verification. Its literal credentials are not part of this document or repository.

- Local live tests read `WENKU8_LIVE_USERNAME` and `WENKU8_LIVE_PASSWORD` from the process environment.
- Credentials must not appear in Gradle properties, local.properties, source, resources, fixtures, CI output, screenshots, crash reports, command history emitted by the agent, or committed files.
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
    data class Unsupported(val feature: String) : ApiFailure
    data object Cancelled : ApiFailure
}
```

Failures carry only redacted operation names and trace IDs. They do not retain passwords, Cookies, captcha text, complete User-Agent values, or full HTML.

### 8.10 Caching and Request Coalescing

Cache keys contain source, host, schema version, language, operation, and canonical parameters.

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
- Merge concurrent identical requests with single-flight coordination.
- A repository may return explicitly marked stale data after a network failure.
- UI state must disclose offline/stale content.
- Mutations invalidate only affected cache keys.
- Parser contract revision changes isolate or invalidate incompatible cached parser results.
- Authenticated cache entries are partitioned by non-secret account identity and cleared on logout.

### 8.11 Public Provider Tests

The provider test suite includes:

- Minimal synthetic normal pages.
- Reordered fields and missing optional fields.
- Relative and absolute URLs.
- Single-page, paged, and missing-pagination responses.
- GBK Chinese search encoding and invalid encoding.
- Empty body and incorrect Content-Type.
- Captcha image, invalid captcha, invalid credentials, expired session, 403 challenge, 200 challenge, and 200 login-page masquerade.
- Bookshelf redirect success, body success, and body failure.
- Chapter `<br>`, `&nbsp;`, watermark, relative image, absolute image, and text/image ordering.
- Cookie domain, path, secure, expiry, replacement, and logout.
- Fake-clock TTL, single-flight concurrency, stale fallback, and targeted invalidation.
- Log redaction for all credential/session material.
- Shared contract tests executed against the public provider and optional private adapter.

Real-site smoke tests are opt-in and never part of deterministic default CI.

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

### 9.3 Legacy Compatibility Components

- `LegacySaveCodec`: exact `.wk8` and XML parsing/writing semantics.
- `LegacyPathPolicy`: internal/external roots, primary/backup order, SAF and sentinel behavior.
- `LegacyIntentCodec`: old Intent and Serializable boundaries.
- `LegacyReaderProgressAdapter`: old and V1 progress conversion and dual-write.
- `LegacyBookshelfAdapter`: local bookshelf import/export.
- `LegacySettingsAdapter`: old setting identity and reader-setting conversion.

Legacy codecs are pure or filesystem-injected and covered by byte/semantic golden fixtures.

### 9.4 Migration State Machine

1. `NotStarted`: only old data exists.
2. `Importing`: copy/parse into new stores with a durable checkpoint.
3. `DualWrite`: new stores are primary for new routes; compatible changes also update old formats atomically.
4. `Verified`: semantic checksums and route-level journeys prove consistency.
5. `LegacyReadOnly`: old files remain a fallback but no longer receive writes.
6. `Complete`: legacy read path can be retired only after an approved compatibility window.

Migration is idempotent. A crash or process kill resumes from the last completed unit. An invalid legacy record is reported and preserved rather than discarded.

### 9.5 Write Safety

- Use write-to-temp plus atomic replace when supported.
- Never truncate the only known-good legacy file before a new write is durable.
- Preserve a recoverable prior version during migration.
- A dual-write failure leaves the old state intact and reports a recoverable storage failure.
- Database migrations have upgrade tests from every shipped schema version.
- Rollback builds continue to read the legacy files during the rollout window.

## 10. Compose Material 3 Product Design

### 10.1 Final UI Rule

Every reachable product page uses Jetpack Compose Material 3. XML layouts may remain temporarily only for compatibility entry shells. Final completion requires zero active page layouts, zero Fragment-owned pages, zero AppCompat Toolbar, and zero old CardView in reachable UI. Platform splash, manifest, theme, and non-page resource XML may remain when required by Android.

### 10.2 Top-Level Information Architecture

- Discover: latest, rankings, categories, tags, and completed lists.
- Bookshelf: local and account bookshelf, synchronization, downloads, and reading history.
- Search: active search, search history, results, filters, and pagination.
- Settings: language, app theme, reader preferences, cache, account, migration, wallpaper, and about.
- Novel detail, community, account, image viewer, and reader are subordinate routes.

### 10.3 Adaptive Navigation

- Compact: Material 3 NavigationBar.
- Medium: NavigationRail.
- Expanded: permanent drawer or rail with list-detail panes.
- Novel detail uses detail/catalog panes when width allows.
- Reader uses a bounded readable text width and a persistent catalog pane when width allows.
- Window resizing preserves destination, list position, filters, selected novel/chapter, reader cursor, and overlay state.
- No fixed 300dp/320dp side panels define adaptive behavior.

### 10.4 Current Surface Migration Map

| Current surface | Final route/state | Disposition |
| --- | --- | --- |
| MainActivity shell | AppShell | Replace with single-activity Navigation Compose shell |
| LatestFragment | Discover/latest | Replace |
| RKListFragment and list pages | Discover/rankings | Replace custom tabs/ViewPager |
| FavFragment | Bookshelf | Replace and unify local/account/offline states |
| ConfigFragment | Settings | Replace partial XML MD3 content |
| SearchActivity | Search | Replace |
| SearchResultActivity | Search/results | Replace |
| NovelInfoActivity | Novel/detail | Replace and split data/download responsibilities |
| NovelReviewListActivity | Novel/reviews | Replace |
| NovelReviewNewPostActivity | Novel/reviews/create | Replace and preserve draft/retry state |
| NovelReviewReplyListActivity | Novel/reviews/thread | Replace |
| ModernReaderActivity | Reader | Retain route identity temporarily; move behavior to ViewModel/modules |
| Wenku8ReaderActivityV1 | Reader compatibility | Retire after parity |
| VerticalReaderActivity | Reader compatibility | Retire after parity |
| ViewImageDetailActivity | Image viewer | Replace; keep old `path` trampoline |
| UserLoginActivity | Account/login | Replace with captcha-aware flow |
| UserInfoActivity | Account/profile | Replace |
| MenuBackgroundSelectorActivity | Settings/wallpaper | Replace |
| AboutActivity | Settings/about | Replace |

Every current surface maps to a new route or an explicit retirement outcome.

### 10.5 Standard Components

- Scaffold, TopAppBar, SnackbarHost.
- NavigationBar, NavigationRail, modal/permanent drawer.
- LazyColumn, ListItem, and cards only for genuinely grouped/repeated items.
- Material 3 SearchBar or DockedSearchBar.
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

### 10.10 Localization

- All product strings are resources, not hard-coded Compose text.
- Simplified and Traditional Chinese are tested on every primary route.
- App-bundle language split behavior must not break in-app language switching.
- Long text and font scale are tested together.

## 11. Program Phases

Each phase receives its own implementation plan after this specification is approved.

### Phase 0: Baseline Stability and Contract Freeze

Deliverables:

- Guard or remove public-stub reachable crashes.
- Fix CI API level and stale instrumentation tests.
- Add Manifest, Intent, Serializable, API signature, file, and path golden contracts.
- Add deterministic source/UI inventory tasks.
- Enable line and branch coverage reporting.
- Add secret-handling documentation and redaction tests.

Exit gate:

- Public debug build, all unit tests, instrumentation tests, and Lint complete without errors.
- CI uses a device at or above minSdk.
- Compatibility fixtures fail when a frozen contract is intentionally changed.

### Phase 1: Architecture Foundation and Material 3 Shell

Deliverables:

- Foundational contract/model/network/storage/data/design/testing modules.
- AppContainer, dispatchers, ViewModel/StateFlow conventions.
- Material 3 design system and adaptive Navigation Compose shell.
- Route rollout flags and compatibility trampolines.
- Architecture dependency checks.

Exit gate:

- New shell renders compact/medium/expanded and light/dark states.
- Legacy default route remains available.
- Feature code cannot import forbidden legacy infrastructure.

### Phase 2: Complete Public Provider

Deliverables:

- Anonymous catalog, search, detail, catalog, chapter, image, and review reads.
- Captcha login, session, profile, bookshelf, check-in, recommendation, review creation, and reply operations.
- GBK codec, parsers, CookieJar, typed failures, cache, request coalescing, and redaction.
- Public/private shared provider contract suite.

Exit gate:

- Public checkout completes search -> detail -> catalog -> reader against deterministic server fixtures.
- Opt-in live read-only smoke verifies login/profile/bookshelf/logout with runtime credentials.
- No normal visible public action throws `UnsupportedOperationException`.

### Phase 3: Storage and Compatibility Migration

Deliverables:

- Room/DataStore/session stores.
- Legacy codecs, path policy, importer, dual-write, checkpoint, and recovery.
- WorkManager-backed migration and download progress.

Exit gate:

- Golden semantic/byte compatibility passes.
- Upgrade, interruption, retry, process death, and rollback tests pass on API 23/29/30/36.
- No legacy artifact is deleted.

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

- Login/captcha/profile/check-in/logout.
- Settings/theme/language/cache/migration/wallpaper/about.
- Image viewer.

Exit gate:

- Auth/session expiry and recovery pass.
- Permission denial and storage failures are recoverable.
- All remaining current surfaces map to Compose routes.

### Phase 8: Legacy Removal and Release Audit

Deliverables:

- Delete unreachable Activity/Fragment/XML/custom navigation/legacy reader UI.
- Delete api-stub after public provider replacement.
- Remove AsyncTask, findViewById, new-business GlobalConfig access, and transition facades.
- Complete physical module extraction and dependency audit.
- Decide the end of legacy dual-write only after compatibility-window evidence.

Exit gate:

- The Definition of Done in Section 16 is proven item by item.

## 12. Test Strategy

### 12.1 Test Layers

- Pure model, policy, parser, codec, paginator, mapper, and use-case unit tests.
- MockWebServer request/response, redirect, header, encoding, Cookie, and redaction tests.
- Room/DataStore/legacy filesystem repository integration tests.
- Migration golden, interruption, retry, upgrade, and rollback tests.
- ViewModel state/effect tests with fake dispatchers and clocks.
- Compose UI tests for every route and state family.
- Deterministic screenshot tests for layout, typography, theme, and adaptive behavior.
- API 23/29/30/36 device journeys.
- Opt-in read-only live-site smoke tests.
- Explicitly gated reversible and persistent live mutations.

### 12.2 Coverage Gates

| Layer | Line | Branch |
| --- | ---: | ---: |
| Parser, storage codec, migration | 90% | 80% |
| Repository, use case, ViewModel | 80% | 70% |
| Overall production logic | 70% | 60% |

Screenshot counts do not substitute for logic assertions.

### 12.3 UI State Coverage

Every route has at least:

- One successful-content test.
- One loading test.
- One empty or not-applicable-state test.
- One recoverable-error test.
- Auth/offline tests where the route supports those states.
- Back/recovery verification for Dialogs and Sheets.

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

The compact baseline includes every primary route plus critical Dialog/Sheet/error states, with at least 57 deterministic golden images at program completion. Every route also has expanded smoke coverage.

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
- `git diff --check`.
- Source/UI inventory checks.

### 13.2 Device Matrix

- API 23: minimum SDK and legacy permission/file behavior.
- API 29: final legacy external-storage behavior.
- API 30: scoped-storage migration boundary.
- API 36: target behavior, edge-to-edge, predictive back, current permission model.

### 13.3 Provider Matrix

- Public provider always builds and runs contract tests.
- A protected private CI environment runs the same contract suite against the private adapter.
- Private CI results must not expose endpoint secrets, credentials, or response bodies.
- Public completion cannot be inferred from private-provider success.

### 13.4 Live Tests

- Not part of deterministic PR CI.
- Read-only smoke runs manually or on a controlled schedule.
- Rate-limited and single-threaded unless an endpoint requires otherwise.
- Persistent mutations require explicit per-run authorization.
- A live-site change creates a parser fixture and contract revision before implementation repair.

## 14. Rollout and Rollback

### 14.1 Route Rollout

- Each replacement route has a local rollout flag.
- A debug/developer route switch permits old/new comparison during migration.
- Release defaults change only after the phase exit gate.
- Old code remains reachable only for rollback until the retirement gate.

### 14.2 Data Rollback

- Initial migrations copy/import; they do not destroy the sole old data source.
- Dual-write maintains rollback compatibility during the declared window.
- A failed new-store write reports an error and does not corrupt the old store.
- Old-file deletion is a separately approved operation after end-to-end evidence.

### 14.3 Provider Rollback

- Public parser revisions are versioned.
- Bad parser results never overwrite known-good durable chapter content.
- A route can use stale valid cache with a visible offline/stale indicator.
- Provider rollout does not change private provider selection semantics.

## 15. Risks and Mitigations

| Risk | Mitigation |
| --- | --- |
| Wenku8 HTML changes | Pure versioned parsers, minimal fixtures, typed parse failures, opt-in smoke tests |
| Captcha/session complexity | User-entered captcha, standards-compliant CookieJar, explicit session validation |
| Wild GPLv3 contamination | Behavioral-spec-only boundary and independently authored implementation/tests |
| Legacy save corruption | Golden fixtures, idempotent import, atomic write, dual-write, no initial deletion |
| Private provider unavailable publicly | Shared contract and protected private CI; no unsupported compatibility claims |
| Large migration scope | Vertical slices, route flags, per-phase plans, phase exit gates |
| UI style without behavior parity | State contracts, Compose UI tests, runtime journeys, screenshot matrix |
| Modern Reader regressions | Existing pure tests plus parity matrix before old-reader retirement |
| Live tests mutate user data | Read-only default, reversible/persistent mutation gates, explicit confirmation |
| Credentials leak | Environment-only secrets, no password persistence, redaction and secret checks |
| Over-modularization before decoupling | Package boundaries first, physical extraction only after acyclic dependencies |
| AVD instability hides visual defects | Reproducible emulator harness, retained screenshots, real-device fallback evidence |

## 16. Definition of Done

The modernization program is complete only when current evidence proves every item below.

### 16.1 Product and Provider

- Public checkout completes all reachable anonymous content journeys.
- Public provider implements captcha login, session, profile, bookshelf, check-in, recommendation, review creation, and reply contracts.
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

### 16.4 UI and Accessibility

- All 19 current surfaces map to Compose Material 3 routes or documented retirement evidence.
- Reachable page XML, Fragment pages, AppCompat Toolbar, and old CardView count are zero.
- Compact, medium, and expanded navigation work.
- Light/dark, Simplified/Traditional Chinese, and font scale 2.0 pass without clipping or overlap.
- TalkBack and keyboard complete the primary search/read journey.
- Every route has loading/content/empty/error/offline/auth states where applicable.
- Reader parity and screenshot gates pass before old reader entries disappear.

### 16.5 Quality and Release

- Public build, unit, integration, parser, migration, provider, Compose UI, screenshot, and device journeys pass.
- CI device APIs are compatible with minSdk.
- Coverage meets Section 12.2.
- Lint has zero errors and zero unexplained warnings in active source. Any intentional toolchain/version warning has a narrow documented suppression and owner.
- A release pipeline injects signing material outside the repository and verifies the signed artifact; no signing key is committed.
- No credential, Cookie, captcha, private endpoint, or raw user content appears in source, artifacts, logs, or screenshots.
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
