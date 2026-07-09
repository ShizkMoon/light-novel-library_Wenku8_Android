# Kotlin + MD3 Rewrite Direction

Last verified: 2026-07-10

This document sets the repo-level direction for the ongoing Kotlin and Material Design 3 work. It is intentionally higher level than the task plans under `docs/superpowers/plans/`: the goal is to keep future implementation aligned with the product and architecture target instead of drifting into mechanical file-by-file rewrites.

## Decision Summary

The active app is no longer blocked by production Java migration. The public Android Studio checkout currently has:

```text
studio-android/LightNovelLibrary/app/src/main/java      java=0 kt=113
studio-android/LightNovelLibrary/api-stub/src/main/java java=0 kt=5
studio-android/LightNovelLibrary/app/src/main/res/layout xml=34
```

The remaining work is therefore not "convert Java to Kotlin". The remaining work is:

- turn Kotlin-translated screens into Kotlin-designed screens with smaller responsibilities;
- make the modern reader the primary Kotlin + native MD3 experience;
- migrate retained XML surfaces to Material 3 components where Compose is not yet worth the runtime or rewrite cost;
- keep the public API stub explicit and avoid exposing private endpoints.

## Scope Boundaries

`studio-android/LightNovelLibrary` is the main product surface. The historical `eclipse-android` and `eclipse-android-old` trees are reference archives and should not receive rewrite investment unless a future task explicitly targets historical comparison.

The `:api` module has two modes:

- private checkout present: Gradle uses the real implementation from `studio-android/LightNovelLibrary/api`;
- public checkout only: Gradle maps `:api` to `studio-android/LightNovelLibrary/api-stub`.

That split is intentional. README history says the project previously had version-management pressure around API exposure, and the current Gradle module selection keeps public builds from depending on private sources. Kotlin + MD3 work must preserve that boundary.

## Product Direction

The reader is the product core. User-visible progress should prioritize the reading loop before cosmetic parity across every old screen:

1. Open a book and chapter reliably from the existing detail/chapter flow.
2. Read with stable pagination, progress restore/save, night mode, font/spacing controls, image viewing, and clear errors.
3. Move between chapters without losing context.
4. Open the table of contents from a dedicated reader entry instead of hiding it inside a secondary settings path.
5. Preserve local cache/save compatibility so existing users do not lose books or progress.

The older V1 and vertical readers may stay as fallback while the modern reader reaches parity. They should not be the architecture target.

## UI Direction

Use native Material Design 3, but choose the UI runtime by screen type:

- New reader-first surfaces: Compose Material 3.
- Existing operational screens that are still tied to Fragment/XML navigation: Material Components XML with Material 3 theme tokens.
- Dialogs and sheets: Material 3 components, not custom legacy popups unless required for compatibility.

The app is an operational reader, not a marketing site. MD3 migration should be compact, calm, and task-focused:

- 48dp or larger touch targets;
- readable typography under Android font scaling;
- clear active/disabled/loading/error states;
- bottom sheets for reader settings and catalog;
- no decorative hero layouts inside bookshelf, search, settings, or reader flows.

## Reader Architecture Direction

The modern reader should converge on small Kotlin-owned units:

- `launch`: validates old intent extras and chooses reader launch behavior.
- `data`: loads cached or remote chapter content through public/private API boundaries.
- `model`: immutable document, catalog, cursor, page, and message models.
- `paging` and `layout`: deterministic pagination and layout specs.
- `progress`: restore/save decisions and save-file compatibility.
- `image`: cached-image resolution, refresh, viewing, and stable temporary image-line behavior.
- `activity`: Android lifecycle, executor/coroutine ownership, and wiring only.
- `ui`: Compose MD3 reader surface, chrome, catalog sheet, settings sheet, and page content.

`ModernReaderActivity` should continue shrinking, but the reason is product architecture, not tidiness. It should eventually own lifecycle and wiring while reader behavior lives in tested Kotlin components.

## Catalog Entry Direction

The table of contents needs its own first-class reader entry. The target behavior:

- visible catalog button in reader chrome;
- modal or side sheet that groups chapters by volume;
- current chapter highlighted and scrolled into view;
- one-tap chapter switch;
- cache or availability state shown when that state is known;
- graceful empty/error state when catalog metadata is missing.

This should be designed as a normal reading control, not as a hidden advanced setting.

## App Architecture Direction

After the modern reader loop is coherent, the next architecture hotspots are:

- `NovelInfoActivity`: split detail state, chapter/cache planning, image cache planning, and download execution.
- `MainActivity`: keep startup, migration, drawer, and first-screen routing separate.
- `FavFragment`, `LatestFragment`, `NovelItemListFragment`, and search screens: migrate toward consistent MD3 list/card behavior after reader and detail flows are stable.
- `GlobalConfig` and save/cache utilities: preserve compatibility but move policy decisions into focused Kotlin objects when touching them.

Large Activities can remain temporarily if they are stable, but new behavior should not make them larger.

## Roadmap

### Phase 1: Direction And Guardrails

- Keep this document and `docs/kotlin-md3-module-migration-ledger.md` aligned.
- Treat public API stub behavior as a hard boundary.
- Stop measuring progress by production Java count alone.

### Phase 2: Modern Reader Completion

- Finish reader catalog entry as a first-class MD3 surface.
- Close chapter navigation, image viewing, progress save/restore, settings persistence, and error-state gaps.
- Add runtime screenshots for the reader before replacing old defaults.

### Phase 3: Detail And Cache Architecture

- Split `NovelInfoActivity` responsibilities into tested Kotlin planning/execution components.
- Keep cache/save compatibility exact.
- Make failures explainable in UI without leaking private API assumptions.

### Phase 4: App-Wide MD3 Consistency

- Migrate retained XML screens to Material 3 components or Compose where it clearly improves ownership.
- Unify list rows, cards, dialogs, toolbar behavior, loading states, and empty states.
- Keep screens dense and scannable rather than decorative.

### Phase 5: Legacy Retirement

- Make modern reader the default only after functional parity is verified.
- Remove or hide old reader paths only after save compatibility, image behavior, catalog behavior, and chapter edge navigation are covered.
- Keep historical source trees archived, not actively rewritten.

## Verification Gates

Any implementation slice should choose verification proportional to its risk, but broad progress claims require stronger evidence:

```powershell
cd .\studio-android\LightNovelLibrary
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.reader.modern.*" --console=plain --stacktrace --no-parallel
.\gradlew.bat assembleAlphaDebug testAlphaDebugUnitTest lintAlphaDebug --console=plain --stacktrace --no-parallel
cd ..\..
git diff --check
```

For module-level Kotlin progress claims, refresh the inventory:

```powershell
$roots = @(
  '.\studio-android\LightNovelLibrary\app\src\main\java',
  '.\studio-android\LightNovelLibrary\api-stub\src\main\java'
)
foreach ($root in $roots) {
  $java = (Get-ChildItem -Path $root -Recurse -Filter *.java | Measure-Object).Count
  $kt = (Get-ChildItem -Path $root -Recurse -Filter *.kt | Measure-Object).Count
  Write-Output "$root java=$java kt=$kt"
}
```

For UI or reader-default changes, static checks are not enough. Capture screenshots or run an emulator/device flow that proves the screen renders and the primary interaction path works.
