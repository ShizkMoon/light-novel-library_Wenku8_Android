# Wenku8 Modernization Requirement-To-Plan Coverage Matrix

**Specification:** `docs/superpowers/specs/2026-07-10-wenku8-modernization-program-design.md`

**Purpose:** Prove that every approved requirement has an owning executable task before implementation. This is a planning cross-reference, not completion evidence; runtime status remains in `docs/verification/modernization-matrix.yaml` created during execution.

| Specification requirement | Owning plan tasks | Planned proof |
| --- | --- | --- |
| 4.1 complete public product journeys | P2 T10-24; P4 T3-16; P5 T3-17; P6 T15-20; P7 T3-18 | Provider contracts, route journeys, screenshots, device tests |
| 4.2 Kotlin modular architecture | P1 T1-5, T14; P4 T1-4; P5 T1-5; P8 T4, T6, T9 | Gradle graph/import rules, architecture tests, final symbol scan |
| 5 non-goals and clean-room boundary | P0 T6, T8, T12; P2 T5-9, T26, T28; P8 T10, T12 | Authorization, provenance, role separation, attestation and egress gates |
| 6.1 application ID, launcher and Android boundaries | P0 T4; P1 T12-14; P4 T11-15; P8 T3, T5 | Merged-manifest, launcher, old-signed, R8 and reachability tests |
| 6.2 frozen Intent contracts | P0 T3-4; P4 T11-12; P5 T13-14; P6 T16, T19; P7 T13-14; P8 T1-3 | Inventory, `LegacyIntentCodec`, API 23/32/33 and minified fixtures |
| 6.3 Serializable boundary types | P0 T4; P5 T13; P6 T16, T19; P8 T1-3 | `serialVersionUID`/field/order probes and old-release payload tests |
| 6.4 legacy files and paths | P0 T3-4, T10; P1 T8-13; P3 T1-12; P8 T7, T13 | Golden bytes, path policy, backup/restore and rollback drills |
| 6.5 provider selection and legacy ABI | P0 T2, T4; P1 T1, T3-5; P2 T1, T25-27; P8 T6 | Unique Gradle graph, ABI probe, selected-provider identity, bridge retirement |
| 7.1 target modules | P1 T1; P2 T1; P4 T1; P5 T1; P8 T6 | Exact project descriptors, unique directories and module compile tasks |
| 7.2 extraction strategy | P1 T12; P4 T11-15; P5 T12-15; P6 T15-19; P7 T13-18; P8 T1-5 | Vertical route flags/trampolines, parity gates, proof-driven deletion |
| 7.3 dependency rules | P1 T4-5, T14; P4 T1-4, T10; P5 T1-4, T12; P7 T1-3, T13; P8 T4, T6 | ArchUnit/import/Gradle graph tests |
| 7.4 AppContainer composition | P1 T5, T11; P2 T21; P4 T10; P5 T12; P6 T15; P7 T13; P8 T4 | Single construction root and test factories |
| 7.5 StateFlow and one-shot effects | P1 T7; P4 T5-9; P5 T6-11; P6 T9-14; P7 T4-12 | ViewModel transition/cancellation/process tests |
| 7.6 single-Activity Navigation Compose | P1 T7, T12; P4 T11-15; P5 T12-14; P6 T15-16; P7 T13-14; P8 T3, T5, T8 | One NavHost, typed routes, back/deep-link/process/adaptive tests |
| 8.1 Wild behavior-only clean room | P0 T6, T8; P2 T5, T9-13, T23-24, T30; P8 T10, T14 | Operation ledgers, synthetic fixtures, hashes and independent attestation |
| 8.1.1 site/content/distribution authorization | P0 T6; P2 T5, T28, T30; P4 T16; P5 T17; P7 T18; P8 T10-12, T14 | Current scoped decisions; blocked scopes cannot become PASS |
| 8.1.2 licensing and supply chain | P0 T8, T11-12; P8 T10-12 | SBOM/provenance/NOTICE/source offer/locks/origins/reproducibility |
| 8.2 provider facets and capability mapping | P1 T3; P2 T2-4, T13, T17-21, T23 | Shared immutable capability set and zero-work guard suite |
| 8.3 stable contract types | P1 T2-4; P2 T2-4 | `core:model` single ownership; API requests/results/facets import models |
| 8.4 complete public behavior surface | P2 T10-13, T16-20, T24, T28-30 | Anonymous/account/bookshelf/recommendation/community journeys |
| 8.5 HTTPS and encoding | P0 T9; P2 T6-9 | HostPolicy, redirect validation, GBK byte/query tests, no cleartext |
| 8.6 parsing policy | P2 T5, T9-13 | Bounded independent parsers and malformed/masquerade fixtures |
| 8.7 authentication/session | P2 T14-19, T21, T29 | Captcha isolation, encrypted store, epoch, unconditional local logout |
| 8.8 environment-only test credentials | P0 T6-7; P2 T28-30; P7 T18; P8 T12, T14 | Environment presence checks and secret scans; no literals or output |
| 8.9 typed failures | P1 T3; P2 T2, T4, T8-24; P4 T3 | Exhaustive failure mapping and cancellation propagation |
| 8.10 cache/single-flight | P1 T4; P2 T22; P3 T2-4; P4 T3-4 | TTL/stale/epoch keys, one-flight, targeted invalidation and Room stores |
| 8.11 provider tests | P2 T5-30 | Contract, MockWebServer, parser, session, journey, live and private matrices |
| 8.12 privacy/logging/outbound network | P0 T7, T9; P2 T6, T8, T28-30; P7 T9-12; P8 T10, T12 | SDK/AD_ID removal, redaction, outbound manifest and runtime egress |
| 9.1-9.3 sources of truth and stores | P1 T8-11; P2 T14-15, T22; P3 T1-6 | DataStore, three physical Room stores, encrypted session and adapters |
| 9.4 state/journal protocol | P1 T9-10; P2 T15; P3 T6-10 | Per-domain state, snapshot, checkpoint, journal and reconciliation tests |
| 9.5 write safety | P1 T10-11; P3 T7-10; P4 T4; P8 T7 | Writer barriers, exactly-once canonical commit, at-least-once projection |
| 9.6 backup/restore | P0 T10; P1 T9, T13; P2 T29; P3 T12, T20; P8 T7, T13 | Physical exclusions, restore reconciliation and signed rollback drills |
| 9.7 durable background work | P3 T11, T13-20; P5 T5, T8; P7 T9; P8 T9, T13 | WorkManager/UIDT selection, bounded chunks, stop/reboot/cancel evidence |
| 10.1 final Compose MD3 rule | P1 T6-7; P4 T5-15; P5 T6-16; P6 T11-19; P7 T4-18; P8 T2-3, T8 | Reachability plus Kotlin-PSI/dependency scan proves no page XML/Fragment/AppCompat/Material2/Material-View bridge/AndroidView/nested Card |
| 10.2 top-level information architecture | P4 T5-15; P5 T12; P7 T13 | Typed destinations and library/novel/account/settings entry points |
| 10.3 adaptive navigation | P1 T7; P4 T6-15; P5 T7, T10-12, T16; P6 T14, T17; P7 T6, T15-17 | Compact/medium/expanded/compact-height/hinge/resize matrices |
| 10.4 UI-owner and XML-surface ledgers | P0 T3; P4 T1, T15-16; P5 T1, T14-17; P6 T19-20; P7 T1, T14, T18; P8 T1-3, T8 | Every A/F ID and X01-X34 resource maps to Compose or exact reviewed retirement evidence |
| 10.5 standard components | P1 T6; P4 T5-9; P5 T7-11; P6 T11-13; P7 T6, T8-12 | Material components, stable dimensions and no nested page cards |
| 10.6 shared UI state contracts | P4 T5; P5 T6, T9, T11; P6 T9-10; P7 T4-5, T8-12 | Loading/content/empty/error/offline/auth/refresh/page states |
| 10.7 complete reader | P5 T13; P6 T1-20 | Paginated/continuous engine, settings/images/input/progress/parity/retirement |
| 10.8 theme and tokens | P1 T6; P6 T6, T11-14; P7 T8 | MD3 schemes/type/spacing/shapes, dynamic/e-ink/reader assets |
| 10.9 accessibility/system integration | P1 T6-7; P4 T13-14; P5 T16; P6 T13-18; P7 T15-17; P8 T8 | Semantics, 48dp, insets, IME, predictive Back, plus current hash-bound manual TalkBack/Switch Access evidence |
| 10.10 localization | P1 T6-7; P4 T6-14; P5 T7-16; P6 T17; P7 T15-17; P8 T8 | zh-CN/zh-TW/zh-HK, font 2.0, screenshot/overflow checks |
| 11 Phase 0-8 deliverables and exit gates | P0 T13; P1 T14; P2 T30; P3 T20; P4 T16; P5 T17; P6 T20; P7 T18; P8 T14 | Per-phase evidence gate cannot borrow later work |
| 12.1 test layers | P0 T1, T3-5; P1 T7, T12-14; P2 T5-30; P3 T1-20; P4 T5-16; P5 T6-17; P6 T7-20; P7 T4-18; P8 T8-14 | Unit/integration/instrumentation, separate-process seed/kill/verify, registered screenshot/device/live/release layers |
| 12.2 coverage thresholds | P0 T5, T13; P1 T14; P2 T30; P3 T20; P4 T16; P5 T17; P6 T20; P7 T18; P8 T9, T14 | 90/80 parser-storage-migration, 80/70 repo/VM, 70/60 overall |
| 12.3 UI state coverage | P4 T5-14; P5 T6-16; P6 T9-18; P7 T4-17; P8 T8 | Every applicable route state family |
| 12.4 screenshot matrix | P1 T7; P4 T14; P5 T16; P6 T17; P7 T17; P8 T8 | Pinned AndroidX app-instrumentation capture, host extraction, immutable approved baselines, fresh verify tasks, original-pixel review |
| 12.5 primary runtime journeys | P2 T24; P4 T16; P5 T17; P6 T18; P7 T18; P8 T8, T14 | Search-to-reader, account, offline, process, migration and release journeys |
| 13 CI and quality gates | P0 T1, T5-13; P1 T14; P2 T27-30; P3 T19-20; P4 T13-16; P5 T16-17; P6 T17-20; P7 T15-18; P8 T9-12, T14 | PR/device/provider/live/release matrices and stale-evidence rejection |
| 14 rollout and rollback | P1 T12-13; P4 T12, T15; P5 T14-15; P6 T16, T19; P7 T14, T18; P8 T1-7, T13 | Route/data/provider rollback and evidence-gated retirement |
| 15 risks and mitigations | P0 T6-12; P2 T5-9, T14-15, T22, T26-30; P3 T4-20; P4 T11-16; P5 T13-17; P6 T15-20; P7 T13-18; P8 T1-14 | Clean room, migration, scheduler, UI, license, AVD and provider controls |
| 16.1 product/provider DoD | P2 T30; P4 T16; P5 T17; P6 T20; P7 T18; P8 T12, T14 | Current operation/journey/channel/provider evidence |
| 16.2 compatibility DoD | P0 T4; P1 T13; P2 T25, T29; P3 T20; P4 T11-12; P5 T13-15; P6 T16, T19; P7 T13-14; P8 T1-7, T13 | Upgrade/rollback/Intent/Serializable/path/save/provider tests |
| 16.3 architecture DoD | P1 T14; P4 T1, T10-16; P5 T1, T12-17; P6 T1, T15-20; P7 T1, T13-18; P8 T2-6, T9 | Final module/reachability/symbol and lifecycle scans |
| 16.4 UI/accessibility DoD | P0 T3; P4 T13-16; P5 T16-17; P6 T17-20; P7 T15-18; P8 T2-3, T8 | Zero legacy/Material2/bridge/nested-card reachability, X01-X34 closure, and current adaptive/manual-assistive evidence |
| Executable custom Gradle task registry | P0 T3, T13; P1 T7, T11, T13-14; P4 T14, T16; P5 T16-17; P6 T17, T20; P7 T17-18; P8 T2, T8, T14 | `gradle-task-contract.yaml` and `verifyPlannedGradleTasks` prove every custom reference has one exact registration/dispatcher or pinned plugin owner |
| 16.5 quality/release DoD | P0 T5-13; P8 T9-14 | Lint/coverage/signing/auth/license/privacy/egress/reproducibility/final audit |
| 17 independent review protocol | P0 T13; P1 T14; P2 T30; P3 T20; P4 T16; P5 T17; P6 T20; P7 T18; P8 T14 | Spec review, task spec review, code-quality review and final four-domain audit |

## Review Rule

Any specification row without an owning task, or any task without an exact test/report/artifact target, is an Important finding. Implementation may not mark a phase complete until its runtime evidence replaces this planning cross-reference in the authoritative modernization matrix.
