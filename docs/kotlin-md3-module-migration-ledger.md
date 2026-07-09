# Kotlin And MD3 Module Migration Ledger

Last verified: 2026-07-09

This ledger keeps the long-running rewrite honest: the target is not only a modern reader slice, but a repo-wide Kotlin rewrite and Material Design 3 migration that still builds the Android app from the public checkout.

## Public Build Modules

| Module | Current role | Kotlin status | MD3 status | Required end state |
| --- | --- | --- | --- | --- |
| `:app` | Android application, UI, legacy reader, parser models, cache/save integration | Kotlin-owned: 0 Java files and 106 Kotlin files under `app/src/main/java` | Mixed: app theme is Material 3 and settings/modern reader have MD3 surfaces, while most legacy XML screens remain pre-MD3 | Compose or MD3 XML for every retained UI surface, with legacy reader and startup responsibilities split out of large Activities |
| `:api` | Private API module when the private checkout is present | Not available in this public workspace | Not a UI module | Must remain compatible as an optional private implementation, with public build not depending on private sources |
| `:api` via `api-stub` | Public fallback API implementation | Kotlin-owned: 0 Java files and 5 Kotlin files | Not a UI module | Keep explicit public-build behavior and no accidental private endpoint exposure |
| Gradle/build scripts | Module selection, Kotlin/Compose enablement, Android packaging | Groovy Gradle scripts remain acceptable unless a later migration chooses Kotlin DSL | Compose and Material dependencies are enabled in `:app` | Public checkout builds the Android app without private API sources |
| Resources | XML layouts, strings, theme, screenshots | XML resources remain normal Android artifacts | Theme is Material 3, but 34 layout XML files need screen-by-screen MD3 review | Every retained XML screen follows MD3 component, spacing, typography, and touch-target rules |

## Current Source Inventory

Generated with PowerShell file counts on 2026-07-09:

```text
app/src/main/java:      java=0 kt=106
api-stub/src/main/java: java=0  kt=5
```

Package-level `:app` production inventory:

```text
activity  java=0  kt=23
adapter   java=0  kt=4
async     java=0  kt=2
component java=0  kt=2
fragment  java=0  kt=6
global    java=0  kt=12
listener  java=0  kt=3
reader    java=0  kt=46
util      java=0  kt=7
```

## Migration Order

1. Keep the public `api-stub` behavior explicit before touching private API integration. The public stub is now Kotlin-owned and covered by `PublicApiStubContractTest`.
2. Continue extracting reader logic into pure Kotlin models with unit coverage, then thin `ModernReaderActivity`.
3. Keep app-side callers compatible with the Kotlin `api-stub`; do not introduce private endpoint behavior into the public fallback.
4. Keep the now Kotlin-owned `global` package covered by parser and pure-state contract tests while migrating callers.
5. Keep the now Kotlin-owned `util` package covered by pure utility and cache persistence contracts while migrating callers.
6. Keep legacy async wrappers small and tested while replacing `AsyncTask` with lifecycle-aware coroutines in later UI slices.
7. Keep the now Kotlin-owned adapters covered by constructor/item-count contracts until their owning screens move to MD3.
8. Continue converting user-facing screens in small MD3 slices; the application class, all Fragments, all shared components, base Material, Main, About, Search, Search Result, menu background selector, login, account info, new review post, review list, review reply list, image detail, and legacy vertical reader screens are now Kotlin-owned.
9. Remove or retire legacy Java reader paths only after the modern Kotlin reader covers local/cloud loading, progress restore/save, catalog, settings, images, chapter edge navigation, and runtime QA screenshots. The legacy reader view foundations, page view, paginator, slider stack, loader abstraction/XML loader, V1 settings, and V1 horizontal reader Activity are now Kotlin-owned while preserving Java record-style accessors, enum names, static reader controls, getter/setter names, direction constants, saved-state behavior, public setting fields, manifest identity, reader menu behavior, and custom font/background pickers for transitional callers. Modern reader cached-image resolution, cached-image display, uncached-image background saving, and duplicate request gating have started; uncached images stay in a stable placeholder while the existing public image cache workflow resolves them.
10. The active production app and public API stub are now Java-free. The next architecture work is not another mechanical conversion pass: split `MainActivity` startup/save-migration responsibilities and `NovelInfoActivity` cache/download/detail responsibilities into lifecycle-aware Kotlin components, then continue MD3 screen rewrites. `MainActivity` save-migration directory selection and startup decision logic have started moving into pure tested Kotlin policy objects, and `NovelInfoActivity` chapter cache availability, volume cache marking, cache progress accounting, selected-volume download progress accounting, image cache path/download planning, image cache execution, chapter XML cache execution, chapter image reference parsing, per-chapter image cache orchestration, and per-chapter cache workflow orchestration have begun moving into pure tested Kotlin components. The modern reader now reuses the existing `ViewImageDetailActivity` `"path"` contract for already cached chapter images without exposing private API behavior.

## MD3 Acceptance Rules

- Use Material 3 components for retained XML surfaces: `MaterialToolbar`, `MaterialCardView`, `MaterialButton`, `MaterialSwitch`, `MaterialAlertDialogBuilder`, and current theme tokens.
- Use Compose Material 3 for new Kotlin-first surfaces unless compatibility with an existing XML screen is the safer migration path.
- Avoid marketing-style hero layouts inside operational app screens; reading, settings, search, bookshelf, and detail pages should stay compact, scannable, and task-focused.
- Preserve 48dp or larger touch targets, clear focus/click states, and readable typography at Android font-scale changes.
- Keep all screen migrations buildable in the public checkout and covered by either unit tests, static checks, or screenshots when UI behavior changes.

## Verification Gates

Run these before claiming a migration slice is safe:

```powershell
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.reader.modern.*" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.global.api.LegacyApiModelContractTest" --tests "org.mewx.wenku8.global.api.custom.NovelListWithInfoParserTest" --tests "org.mewx.wenku8.listener.ListenerContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.global.GlobalConfigContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.util.UtilContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.async.AsyncAndComponentMigrationContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.adapter.AdapterMigrationContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.AppMigrationContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.activity.ActivityMigrationContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.fragment.FragmentMigrationContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.reader.view.ReaderViewMigrationContractTest" --tests "org.mewx.wenku8.reader.view.WenkuReaderPaginatorTest" --tests "org.mewx.wenku8.reader.slider.SliderMigrationContractTest" --console=plain --stacktrace
.\gradlew.bat :app:testAlphaDebugUnitTest --tests "org.mewx.wenku8.reader.activity.ReaderActivityMigrationContractTest" --tests "org.mewx.wenku8.reader.modern.launch.ReaderLaunchPlannerTest" --tests "org.mewx.wenku8.reader.view.ReaderViewMigrationContractTest" --tests "org.mewx.wenku8.reader.view.WenkuReaderPaginatorTest" --tests "org.mewx.wenku8.reader.slider.SliderMigrationContractTest" --tests "org.mewx.wenku8.reader.ReaderCoreMigrationContractTest" --console=plain --stacktrace --no-parallel
.\gradlew.bat assembleAlphaDebug testAlphaDebugUnitTest --console=plain --stacktrace
git diff --check
```

For module-wide progress claims, also refresh the source inventory:

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
