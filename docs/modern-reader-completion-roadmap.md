# Modern Reader Completion Roadmap

Last verified: 2026-07-10

This roadmap expands the reader-first direction from `docs/kotlin-md3-rewrite-direction.md`. It keeps the next implementation work focused on completing the Kotlin + native MD3 reader experience before broad app-wide polish.

## Current Position

The modern reader already has a meaningful Kotlin foundation:

- Compose Material 3 reader surface and sheets exist under `reader/modern/ui`.
- Pagination, layout, launch, image, progress, and chapter orchestration are represented by Kotlin components. Modern progress saving now preserves the old V1 behavior of clearing the saved record when the current chapter is already on its final page.
- Reader failure states now have a structured UI model for load failures and missing launch arguments, including explicit retry/close actions. Missing chapter/book arguments stop before background loading starts.
- `ModernReaderActivity` has started moving decisions into coordinators, and chapter loading now rejects stale outcomes from older load generations before they can overwrite newer reader state. The Activity still owns lifecycle wiring, executor state, layout creation, image detail launch, and session construction.
- The legacy V1 and vertical readers still exist and should remain available until the modern reader is proven as the primary path.

The next work should not be another mechanical conversion pass. It should close product-level gaps that stop the modern reader from replacing the old reader.

## Completion Target

The modern reader is ready to become the default reader only when these user-visible capabilities are covered:

1. The reader opens from the existing novel detail/chapter flow using the same public intent contract.
2. Local cached content and public/private API-backed content load through the existing module boundary.
3. Pagination, page movement, chapter edge movement, and progress save/restore are stable.
4. Reader settings persist and trigger predictable reflow.
5. Chapter images resolve to cache when available, show a stable loading/unavailable state otherwise, and open in the existing image viewer when cached.
6. The table of contents is a first-class entry in the reader chrome.
7. Empty, loading, network-failure, missing-argument, and missing-catalog states are visible and understandable.
8. Runtime screenshots or emulator/device checks prove the main reader loop renders correctly before old defaults are retired.
9. The modern reader is promoted only after the public checkout passes build/tests/lint plus cached/local, failure-state, image, catalog, and settings runtime checks.

## Milestone 1: Reader Navigation Spine

Goal: make chapter and page movement feel like one coherent reader instead of a collection of isolated flows.

Scope:

- keep the existing launch extras stable;
- keep current page, chapter, and catalog state in one reader-level state model;
- make previous/next page and previous/next chapter behavior explicit at page and chapter edges;
- preserve progress save behavior when the user moves pages, switches chapters, or leaves the reader.

Acceptance:

- focused reader unit tests cover edge chapter switching and progress-save decisions;
- old reader entry points remain available;
- no private API behavior is added to the public checkout.

## Milestone 2: First-Class Catalog Entry

Goal: make the table of contents visible, predictable, and useful inside the reader.

Scope:

- add or refine a visible catalog button in reader chrome;
- show a Material 3 modal bottom sheet or side sheet with volume grouping;
- highlight the current chapter;
- scroll or position the list so the current chapter is easy to find;
- route chapter taps through the same chapter-switch coordinator as edge navigation;
- show a graceful state when catalog data is unavailable.

Acceptance:

- unit coverage proves catalog row selection maps to the intended chapter-switch decision;
- Compose UI renders grouped catalog content without overlapping text at common phone widths;
- screenshots are captured before making the modern reader the default.

## Milestone 3: Reading Settings And Reflow

Goal: make reading controls feel native and durable.

Scope:

- keep night mode, font size, line spacing, paragraph spacing, and reader width/edge choices in a single display settings model;
- persist settings through the existing settings store;
- rebuild reader sessions through one session factory boundary;
- keep reflow deterministic and avoid silent cursor jumps when settings change.

Acceptance:

- unit tests cover settings clamping, persistence decisions, and reflow cursor preservation;
- manual or emulator verification confirms the settings sheet updates visible typography and spacing.

## Milestone 4: Image Handling And Detail Viewing

Goal: make mixed text/image chapters reliable without exposing private API assumptions.

Scope:

- keep cached image lookup isolated from UI rendering;
- show stable image-line states for cached, loading, unavailable, and failed images;
- route cached image taps to the existing `ViewImageDetailActivity` `"path"` contract;
- avoid retry storms or duplicate cache work.

Acceptance:

- unit tests cover cached path resolution, duplicate request gating, failed refresh, and detail-launch decision;
- runtime verification confirms cached images open in the existing viewer.

## Milestone 5: Runtime QA And Default Reader Decision

Goal: prove the modern reader can safely become the main path.

Scope:

- run a cached local chapter flow;
- run a public checkout failure/empty API flow;
- run a chapter with images;
- run settings changes and process leave/return progress behavior;
- capture screenshots for day mode, night mode, catalog, settings, temporary image state, cached image state, and error state.

Acceptance:

- `assembleAlphaDebug testAlphaDebugUnitTest lintAlphaDebug` passes for the public checkout;
- reader-focused unit tests pass;
- screenshots show no blank Compose screen, text overlap, broken sheet layout, or inaccessible controls;
- only after this milestone should the modern reader be promoted ahead of the legacy reader.

## Work Not To Prioritize Yet

These are real tasks, but they should not distract from reader completion:

- app-wide Compose conversion;
- historical `eclipse-android` rewrites;
- private endpoint reconstruction in the public repo;
- replacing every XML screen before the reader is complete;
- deleting legacy readers before runtime QA confirms the modern reader.

## Next Implementation Slice

The next code slice should be Milestone 2: first-class catalog entry. It is the strongest bridge between the user-visible reader direction and the current coordinator-heavy Kotlin architecture:

- it directly addresses the requested dedicated directory entry;
- it exercises MD3 reader chrome and sheet design;
- it reuses existing chapter-switch work instead of inventing another navigation path;
- it gives a clear screenshot target for visual QA.

The slice should stay reader-scoped, test-first, and public-checkout safe.
