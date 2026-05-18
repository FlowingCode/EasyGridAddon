# Implementation Analysis: Row Actions vs. FEATURE_ROW_ACTIONS.md

Generated: 2026-05-26

---

## 1. Method name divergence (`with` prefix)

The spec names the fluent setters with a `with` prefix; the implementation dropped it.

| Spec | Implementation |
|---|---|
| `withVisibleWhen(predicate)` | `visibleWhen(predicate)` |
| `withEnabledWhen(predicate)` | `enabledWhen(predicate)` |
| `withTooltip(String)` | `tooltip(String)` |
| `withTooltip(Function)` | `tooltip(ValueProvider<T,String>)` |

Decision needed: align the spec to the implementation or rename the methods.

---

## 2. `addRowAction` with `ButtonVariant` — not implemented

The spec declares:

```java
EasyRowAction<T> addRowAction(String label, VaadinIcon icon,
    ButtonVariant variant, SerializableConsumer<T> handler);
```

No such overload exists. Users must call `action.addThemeVariants(variant)` as a separate
step after `addRowAction`. Whether the convenience overload is worth adding is a design
question; for now the feature doc is ahead of the code.

---

## 3. Icon type: `VaadinIcon` vs. broader hierarchy

The spec shows only `VaadinIcon`. The implementation is considerably wider:

| Overload in impl | Spec |
|---|---|
| `addRowAction(String, Icon, handler)` | closest match (uses `Icon` not `VaadinIcon`) |
| `addRowAction(Icon, handler)` — icon-only | absent |
| `addRowAction(String, handler)` — label-only | absent |
| `addRowAction(String, IconFactory, handler)` | absent |
| `addRowAction(IconFactory, handler)` | absent |
| `addRowAction(ValueProvider<T,ICON>, handler)` — per-row icon | absent |
| `addRowAction(String, ValueProvider<T,ICON>, handler)` — per-row icon | absent |

The implementation supersedes the spec here: accepting `AbstractIcon<ICON>` and
`ValueProvider<T, ICON>` is more general and more idiomatic in Vaadin. The spec should
be updated to reflect the actual overload set.

---

## 4. Tooltip type: `SerializableFunction` vs. `ValueProvider`

The spec uses `SerializableFunction<T, String>` for the dynamic tooltip; the
implementation uses `ValueProvider<T, String>`. Both extend `Function<T,R>` and
`Serializable`. `ValueProvider` is the idiomatic Vaadin type here. The spec should
be updated to `ValueProvider`.

---

## 5. Menu mode: accepted but not rendered

`setRowActionsAsMenu(boolean)` is present in both spec and implementation, but the
`asMenu` field in `RowActionsManager` is set and never read — `updateRenderer()` always
produces inline `<vaadin-button>` elements regardless of its value.

This means the spec feature is silently non-functional. Either the rendering branch must
be implemented or the method should throw `UnsupportedOperationException` until it is.

---

## 6. `removeRowAction` / `EasyRowAction.remove()` — not in spec

The implementation added:

- `HasRowActions.removeRowAction(EasyRowAction<T>)`
- `EasyRowAction.remove()`

These are not mentioned in the spec. They are useful and should be added to
`FEATURE_ROW_ACTIONS.md`.

---

## 7. `refreshRowActions()` — not in spec

`HasRowActions.refreshRowActions()` (and `RowActionsManager.refresh()`) are not in the
spec. They are the current escape hatch for mutation-after-registration (see
ROW-ACTIONS-TODO.md #7). If auto-detection (option a in #7) is implemented, this method
becomes internal and may not need to be part of the public API doc.

---

## 8. Open items already tracked in ROW-ACTIONS-TODO.md

The following gaps are already tracked and are listed here only for completeness:

- **#3** — `@Uses(ConfirmDialog.class)` missing on `HasRowActions`
- **#4** — `withConfirmation`: no per-row dynamic messages, no localizable button labels
- **#7** — Mutation after registration requires manual `refreshRowActions()` call
- **#8** — `withConfirmation` has no test coverage

---

## Summary

| # | Gap | Severity |
|---|---|---|
| 1 | `with` prefix mismatch (spec vs. impl) | spec update needed |
| 2 | `addRowAction(..., ButtonVariant, ...)` absent | impl gap |
| 3 | Spec lists only `VaadinIcon`; impl is broader | spec update needed |
| 4 | `SerializableFunction` vs. `ValueProvider` in tooltip | spec update needed |
| 5 | Menu mode silently no-ops | impl gap (high risk) |
| 6 | `removeRowAction` / `remove()` not in spec | spec update needed |
| 7 | `refreshRowActions()` not in spec | spec update needed |
