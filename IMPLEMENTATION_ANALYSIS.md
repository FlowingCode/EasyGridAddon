# Implementation Analysis: EasyGrid vs. SPECIFICATIONS.md / FEATURE_ROW_ACTIONS.md

Updated: 2026-06-11 (third revision — removal-API decision recorded, advanced doc sections restored)

> Supersedes the earlier 2026-06-11 revisions. This revision lists only the **outstanding**
> discrepancies; items resolved since then have been removed (see "Resolved since previous
> revisions" at the end).

Scope: the whole component under `src/main/java/com/flowingcode/vaadin/addons/easygrid/`, audited
against `SPECIFICATIONS.md` and `FEATURE_ROW_ACTIONS.md` (linked from spec §3.4).

**Bottom line:** the core component (discovery, construction, ordering, headers, nested properties,
the `EasyColumn` API, the configuration tree, renderers, serialization) conforms to the spec, and the
row-actions implementation covers the documented API with IT coverage. The remaining true
contradictions are the §2.2 Sorting column (Boolean and Enum) and the menu-mode rendering
description ("a single button per row" vs. the implemented right-click context menu); the rest are
documentation/spec wording fixes and deferred row-actions gaps.

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
- Dependencies (§8): the pom now targets Vaadin `24.10.6`, matching the spec's "Vaadin Flow (24.x)"
  (it previously pointed at 25.0.0).

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
  `InstanceEasyGridConfiguration.resolve(...)`'s javadoc now documents all three levels, including the
  column-level layer.
- Built-in default registers `""` on `Object.class` (`GlobalEasyGridConfiguration.java:62`).
- Primitive→boxed hierarchy mapping (`EasyGridConfigurationClassMap.java:49-78`).
- `typeConfiguration(Class)` (instance) and `GlobalEasyGridConfiguration.forType(Class)` + `freeze()`.
  The `freeze()` javadoc is now accurately scoped ("preventing further calls to `forType(Class)`"),
  and the class javadoc explains the startup/thread-safety contract.
- Renderer utility classes are `@UtilityClass` returning serializable `RendererFactory`; the full
  graph (column, configs, factories) is `Serializable` (§6 satisfied).

Open doc/spec flags:
- **Spec §3.5 still overstates `freeze()`.** "Call `GlobalEasyGridConfiguration.freeze()` after startup
  to prevent further modifications" — `freeze()` blocks new `forType(...)` *registrations*
  (`GlobalEasyGridConfiguration.java:89-94`), but a `ColumnConfiguration` reference obtained *before*
  freeze still has live setters that mutate the shared node. The javadoc has been fixed; the spec
  wording has not. *(spec wording fix)*
- **"Three utility classes" is now four.** §3.5 says three; the impl ships a fourth, `LocalTimeRenderers`,
  wired as the `LocalTime` default (`GlobalEasyGridConfiguration.java:68`). *(spec update)*
- **Deserialization detaches the global node.** A serialized column/instance config pulls in a *copy*
  of the linked global node, not the live static singleton, so it won't see global re-registrations made
  after serialization. Behavior is preserved; worth documenting. *(behavioral note)*
- **Post-freeze resolution skips node creation.** After `freeze()`, internal resolution
  (`GlobalEasyGridConfiguration.resolve`, `:104-106`) returns the nearest *registered* ancestor config
  (or `null`) instead of materializing new nodes. Net behavior is equivalent; undocumented. *(behavioral
  note)*

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

The feature doc covers the `addRowAction` overloads, `setRowActionsAsMenu`, `getActionsColumn`,
`setDefaultRowActionVariants`, `setRowActionsRenderer`, `refreshRowActions`, and the fluent
`EasyRowAction` API (`visibleWhen`, `enabledWhen`, `tooltip`, `withConfirmation`, styling/theme
variants, `remove()`). Removal is exclusively `action.remove()` — `RowActionsManager.removeRowAction`
is package-private **by design** (2026-06-11 decision); the doc no longer declares a grid-level
`removeRowAction`. Against that contract:

- All seven `addRowAction` overloads exist (`HasRowActions.java:60-154`) and return a fluent
  `EasyRowAction<T>`; `visibleWhen`, `enabledWhen`, `tooltip` (static + per-row), and
  `withConfirmation` behave as documented, with IT coverage (`EasyRowActionIT`: invocation,
  visibility, enablement, confirmation, menu mode, removal, refresh, theme variants, column
  visibility).
- `setRowActionsAsMenu(boolean)` swaps between the inline `LitRowActionsRenderer` and
  `ContextMenuRowActionsRenderer` (`RowActionsManager.java:136-140`). The doc's custom-renderer
  note accurately describes the interplay (only `setRowActionsAsMenu(true)` replaces a custom
  renderer; `false` does not).
- `setDefaultRowActionVariants(...)` matches the doc: built-in default `LUMO_TERTIARY_INLINE`,
  applied only to actions created after the call, cleared by no-args/`null`
  (`RowActionsManager.java:52, 89-91, 126-128`).
- `refreshRowActions()` and the auto-refresh contract of the fluent setters match the doc;
  styling/theme changes require the explicit refresh, as documented.
- `EasyRowAction` implements `HasStyle`/`HasThemeVariant` and forwards them onto the rendered
  button, as documented.
- `EasyRowAction.remove()` is idempotent and a no-op when already removed; the action is dead
  afterwards — exactly as documented.

### Gaps and contradictions

- **Menu-mode trigger — CONTRADICTION.** The doc says menu mode presents actions "in an overflow
  (context) menu **triggered by a single button per row**". The implementation uses
  `GridContextMenu` (`ContextMenuRowActionsRenderer.java:50-77`): the menu opens on
  right-click/long-press anywhere on the row, and **no per-row trigger button (and no actions
  column) exists**. The doc's intro ("buttons or menu items displayed in a dedicated actions
  column") has the same problem. Fix the doc to describe the context-menu behavior, or implement a
  per-row overflow-button trigger. *(decision needed)*
- **`getActionsColumn()` is null in menu mode — doc fix.** The implementation returns `null` when
  the active renderer has no column (`RowActionsManager.java:192-197`,
  `ContextMenuRowActionsRenderer.java:80-82`); the `HasRowActions` javadoc documents this, but the
  feature doc does not — and its Usage section calls `setRowActionsAsMenu(true)` and later
  `getActionsColumn().setHeader(...)`, which would NPE if executed as one block.
- **Menu mode ignores `tooltip(...)`, styling, and theme variants — divergence.** The context-menu
  renderer honors label, icon, `visibleWhen`, `enabledWhen`, and confirmation (via `execute`), but
  does not apply tooltips, `HasStyle` classes/styles, theme variants, or the default variants to
  menu items. The doc presents these as unconditional `EasyRowAction` features. Either scope them
  to inline rendering in the doc, or forward them onto menu items. *(doc scope note or impl gap)*

Behavioral notes (doc is silent — not contradictions):
- `tooltip(...)` renders as the native `title` attribute (`EasyRowAction.java:253`), not Vaadin's
  `Tooltip` component.
- `remove()` schedules the rebuild for the next `beforeClientResponse` cycle rather than re-rendering
  synchronously; the change is still visible without a data refresh, which is what the doc's
  "immediate re-render" effectively promises.
- A per-row *label* provider exists internally (`RowActionsManager.addRowAction` takes
  `ValueProvider<T,String>`) but is not exposed through `HasRowActions`; the doc does not promise it.
- Static-icon overloads snapshot the icon's attributes/properties into the Lit template
  (`LitRendererBuilder.java:377-384`); in menu mode the same `Icon` instance is reattached on each
  menu open (`ContextMenuRowActionsRenderer.java:64-71`). The doc no longer makes any claim about
  icon cloning, so this is informational only.

### Deferred / open items

- **`withConfirmation` is English-only.** Hardcoded `"Ok"`/`"Cancel"` (`EasyRowAction.java:163`), no
  per-row/dynamic message, no i18n hook. *(deferred — recorded in TODO.txt and WIP.txt #4)*
- **`@Uses(ConfirmDialog.class)` absent on `HasRowActions`** (only `@Uses(Button.class)`,
  `HasRowActions.java:35-37`); the dialog is created lazily, so the production bundle may omit it
  unless another view component pulls it in. *(WIP.txt #3)*
- **Built-in renderers not reusable.** `RowActionsRenderer` is a public SPI, but the built-in
  implementations (`LitRowActionsRenderer`, `ContextMenuRowActionsRenderer`) are package-private
  `final`, so they can't be subclassed as a base for custom renderers. *(minor)*

---

## Summary (outstanding only)

| # | Area | Finding | Type | Action |
|---|---|---|---|---|
| 1 | §2.2 | Boolean marked non-sortable but is sortable (Comparable) | contradiction | fix spec table |
| 2 | §2.2 | Enum sorting is natural/ordinal order, not "Alphabetical" ([#13](https://github.com/FlowingCode/EasyGridAddon/issues/13)) | divergence | fix spec wording or add comparator |
| 3 | §3.5 | Spec still says `freeze()` "prevents further modifications"; it only blocks new registrations (javadoc already fixed) | spec wording | fix spec wording |
| 4 | §3.5 | "Three utility classes" — impl ships four (adds `LocalTimeRenderers`) | spec update | update spec |
| 5 | §3.4 | Menu mode is a right-click `GridContextMenu`, not "a single button per row" | contradiction | fix doc or implement trigger button |
| 6 | §3.4 | `getActionsColumn()` returns `null` in menu mode; doc omits it and its usage example would NPE | doc | document null / split example |
| 7 | §3.4 | Menu mode ignores tooltip, styling, and theme variants | divergence | scope doc or apply to menu items |
| 8 | §3.4 | `withConfirmation` English-only; `@Uses(ConfirmDialog)` absent | impl gap / deferred | see TODO.txt / WIP.txt |

Core component: conformant. Spec-side fixes: #1–#4, #6. Decisions: #5, #7. #8 remains deferred
(TODO.txt / WIP.txt).

---

## Resolved since previous revisions

By code changes (since 2026-06-03):
- `setRowActionsAsMenu` is no longer a no-op — menu mode is implemented via
  `ContextMenuRowActionsRenderer` (trigger-mechanism caveat remains, see #6).
- Fluent `EasyRowAction` setters auto-schedule a renderer rebuild; `EasyRowAction.remove()`
  implemented (idempotent).
- `InstanceEasyGridConfiguration.resolve(...)` and `GlobalEasyGridConfiguration.freeze()` javadocs
  corrected.
- `withConfirmation`, menu mode, removal, refresh, and theme variants gained IT coverage
  (`EasyRowActionIT`).
- The pom moved from Vaadin 25.0.0 to 24.10.6, matching spec §8.

By the 2026-06-11 doc edits to `FEATURE_ROW_ACTIONS.md`:
- Stale "not yet implemented" roadmap banner and duplicate H1 removed.
- "Icon is cloned for each row / original instance is not reused" claim removed (the menu-mode
  icon-instance reuse is no longer a divergence).
- Grid-level `removeRowAction(EasyRowAction<T>)` dropped from the doc — removal is exclusively
  `action.remove()`; `RowActionsManager.removeRowAction` stays package-private by design.
- The advanced sections (default theme variants, custom renderer, refresh, styling/theme variants)
  were restored, so the doc again covers the full public API; the restored custom-renderer note now
  accurately states that only `setRowActionsAsMenu(true)` replaces a custom renderer (the former
  wording claimed any call did, contradicting the guard in `RowActionsManager.java:137-139`).
