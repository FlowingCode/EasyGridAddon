# Implementation Analysis: EasyGrid vs. SPECIFICATIONS.md

Updated: 2026-06-03

> Supersedes the 2026-05-26 row-actions-only analysis. This revision lists only the **outstanding**
> discrepancies; items resolved since then have been removed.

Scope: the whole component under `src/main/java/com/flowingcode/vaadin/addons/easygrid/`, audited
against `SPECIFICATIONS.md`. Row actions are specified separately in `FEATURE_ROW_ACTIONS.md`
(linked from spec §3.4).

**Bottom line:** the core component (discovery, construction, ordering, headers, nested properties,
the `EasyColumn` API, the configuration tree, renderers, serialization) conforms to the spec. The
only true spec-vs-code *contradictions* are in the §2.2 Sorting column (Boolean and Enum); the rest
are documentation/spec-wording fixes and one deferred row-actions gap.

---

## 1. Core component — conformant

### 1.1 Construction / discovery / ordering / headers / nested (§2.1, 3.1, 3.2, 4, 5) — IMPLEMENTED

- All four constructors exist and behave as specified: `EasyGrid(Class)`, `EasyGrid(Class, String...)`
  (selective, order preserved), `EasyGrid(Class, boolean)` (suppress auto columns), and
  `EasyGridWrapper(GRID, Class, String...)` (`EasyGrid.java:42-67`, `EasyGridWrapper.java:72-108`).
- `addColumn(String)` and `addColumn(Class<V>, ValueProvider<T,? extends V>)` both return `EasyColumn`
  (`EasyGridWrapper.java:139-141, 200-204`); the typed overload sets no key/header, as specified.
- `setColumnOrder(String...)` / `hideColumns(String...)` (`EasyGridWrapper.java:150-176`).
- Auto-header via `SharedUtil.propertyIdToHumanFriendly` (`EasyGridWrapper.java:240`) — reproduces
  all five spec examples, including `isActive` → "Is Active".
- Nested dot-notation resolves through Vaadin's `BeanPropertySet`; the full dotted name is used as the
  column key, so `getColumn("address.city")` round-trips.
- `getWrappedGrid()` (`EasyGridComposite.java:79-81`) and `@Delegate` of the standard `Grid` subset
  satisfy §1 and §9 (out-of-scope features are left to the wrapped grid, not reimplemented).

Behavioral notes (spec is silent — not contradictions):
- Auto-header for a nested property uses only the **leaf** segment: `address.city` → "City". The spec
  example sets the header manually, so this is reasonable but undocumented.
- Acronyms are not space-split (consecutive capitals stay together; `id` → "Id").
- `setColumnOrder` de-duplicates names and silently ignores unknown keys; `hideColumns` ignores
  unknown keys.

### 1.2 `EasyColumn` API (§3.3) — IMPLEMENTED, exact

Every method in the spec's `EasyColumn` table exists with the listed signature and `EasyColumn<T,V>`
return type (`EasyColumn.java`, delegated setters in `IEasyGridColumn.java`). No missing methods, no
signature mismatches. Additive public setters exist beyond the spec (`setComparator`,
`setEditorComponent`, `setTooltipGenerator`, part-name generators, etc.) — allowed by the spec's
"…and other `Grid.Column` setters".

### 1.3 Configuration tree / null rep / renderers / serialization (§3.5, 6) — IMPLEMENTED

- Resolution order `Column → Instance(hierarchy) → Global(hierarchy) → built-in default`, scope-first,
  first-non-null wins (`ColumnConfigurationLink`, `ColumnConfigurationImpl`, `InstanceEasyGridConfiguration`).
- Built-in default registers `""` on `Object.class` (`GlobalEasyGridConfiguration.java:62`).
- Primitive→boxed hierarchy mapping (`EasyGridConfigurationClassMap.java:49-78`).
- `typeConfiguration(Class)` (instance) and `GlobalEasyGridConfiguration.forType(Class)` + `freeze()`.
- Renderer utility classes are `@UtilityClass` returning serializable `RendererFactory`; the full
  graph (column, configs, factories) is `Serializable` (§6 satisfied).

Open doc/spec flags:
- **`freeze()` is weaker than documented.** It blocks new `forType(...)` *registrations*
  (`GlobalEasyGridConfiguration.java:90-92`), but a `ColumnConfiguration` reference obtained *before*
  freeze still has live setters that mutate the shared node. "Prevent further modifications" overstates
  the guarantee. *(doc fix)*
- **"Three utility classes" is now four.** §3.5 says three; the impl ships a fourth, `LocalTimeRenderers`,
  wired as the `LocalTime` default. *(spec update)*
- **Column-level layer absent from the `resolve(...)` javadoc.** `createNewLayer()` adds a real
  column-level node, and `CONFIGURATION_RESOLUTION.md`'s implementation chain shows it, but
  `InstanceEasyGridConfiguration.resolve(...)`'s javadoc still lists only Instance + Global. *(doc fix)*
- **Deserialization detaches the global node.** A serialized column/instance config pulls in a *copy*
  of the linked global node, not the live static singleton, so it won't see global re-registrations made
  after serialization. Behavior is preserved; worth documenting. *(behavioral note)*

---

## 2. Type-to-renderer mapping & sorting (§2.2, 2.3) — two real contradictions

Defaults live in `GlobalEasyGridConfiguration`'s static block (`:61-69`); the no-factory fallback is
`ColumnConfigurationTextRenderer` (a `TextRenderer` using `toString()`). Every type row's renderer,
format, and alignment is implemented as specified. The contradictions are in the **Sorting** column:

- **Boolean sorting — CONTRADICTION.** The §2.2 table marks Boolean sorting `—`, but `Boolean` is
  `Comparable`, so the auto-comparator is set (`EasyGridWrapper.java:255`) and Boolean columns *are*
  sortable. This also contradicts §2.3 ("all `Comparable` types sortable by default"). **Fix the §2.2
  table** — the code behavior is the sensible one.
- **Enum sorting — DIVERGENCE.** §2.2 says Enum sorting is "Alphabetical", but the auto-comparator uses
  the enum's *natural* order (`ordinal()`/declaration order), which only coincides with alphabetical by
  accident. **Fix:** reword the spec to "natural order", or install a `toString()`-based comparator for
  enums. (Enum *rendering* correctly uses `toString()`, not `name()` — matches spec.) Tracked in
  [#13](https://github.com/FlowingCode/EasyGridAddon/issues/13).

Lesser notes:
- String/Boolean/Enum render via `ColumnConfigurationTextRenderer` (a `TextRenderer` subclass) — functionally
  equivalent to the spec's "TextRenderer".
- `LocalTime` default pattern is `HH:mm:ss` (spec leaves the format blank — unspecified, not a divergence).

Sorting wiring (§2.3) is otherwise faithful: in-memory comparator set when the value type is
`Comparable`/primitive; backend sort property set to the bean property name for `addColumn(String)` and
*not* for `addColumn(Class, ValueProvider)`; key and sort property independent after creation (delegated
to Vaadin). Multi-column sort is inherited from the wrapped `Grid` — EasyGrid neither enables it by
default nor exposes a toggle (use `getWrappedGrid().setMultiSort(...)`).

---

## 3. Row Actions (§3.4 → FEATURE_ROW_ACTIONS.md)

`FEATURE_ROW_ACTIONS.md` now tracks the implementation. The items below are the remaining open notes.

- **`RowActionsRenderer` SPI residual.** The interface is `public` (`RowActionsRenderer.java:38`) with
  public-only method signatures, so consumers can supply a custom renderer via `setRowActionsRenderer(...)`.
  The built-in implementations (`LitRowActionsRenderer`, `ContextMenuRowActionsRenderer`) remain
  package-private, so they can't be reused or subclassed as a base. *(minor)*
- **`withConfirmation` is English-only.** Hardcoded `"Ok"`/`"Cancel"`, no per-row/dynamic message, no i18n
  hook. *(deferred — recorded in TODO.txt)*
- **`@Uses(ConfirmDialog.class)` absent on `HasRowActions`** (only `@Uses(Button.class)`); the dialog is
  created lazily, so bundle impact should be confirmed. *(ROW-ACTIONS-TODO #3)*

---

## Summary (outstanding only)

| # | Area | Finding | Type | Action |
|---|---|---|---|---|
| 1 | §2.2 | Boolean marked non-sortable but is sortable (Comparable) | contradiction | fix spec table |
| 2 | §2.2 | Enum sorting is natural/ordinal order, not "Alphabetical" ([#13](https://github.com/FlowingCode/EasyGridAddon/issues/13)) | divergence | fix spec wording or add comparator |
| 3 | §3.5 | `freeze()` blocks new registrations only; existing nodes stay mutable | doc | tighten javadoc/spec wording |
| 4 | §3.5 | "Three utility classes" — impl ships four (adds `LocalTimeRenderers`) | spec update | update spec |
| 5 | §3.5 | Column-level layer absent from `InstanceEasyGridConfiguration.resolve(...)` javadoc | doc (minor) | expand javadoc |
| 6 | §3.4 | `withConfirmation` English-only; `@Uses(ConfirmDialog)` absent | impl gap / deferred | see TODO.txt |

Core component: conformant. The two genuine code-vs-spec contradictions (#1, #2) are in Enum/Boolean
sorting; #3–#5 are documentation/spec-wording fixes, and #6 is a deferred row-actions gap (TODO.txt).
