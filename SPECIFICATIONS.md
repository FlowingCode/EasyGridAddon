# Easy Grid Add-on — Specification

## 1. Overview

The Easy Grid Add-on provides `EasyGrid<T>`, a Vaadin `Composite` component that wraps an internally created `Grid<T>`. It uses reflection to discover bean properties, maps them to appropriately typed and formatted columns, and provides a clean Java API for controlling column ordering and type-specific rendering.

`EasyGrid<T>` is a component — it is added to the layout directly. Data binding and all other standard `Grid` features are accessed directly on the `EasyGrid` instance, which delegates them to the wrapped `Grid`. The wrapped `Grid` is accessible via `getWrappedGrid()` for any configuration not covered by the delegating API.

For advanced cases where a custom `Grid` subclass must be supplied (e.g. `TreeGrid`), use `EasyGridWrapper<T, GRID>` instead — it accepts a caller-provided `GRID extends Grid<T>` and otherwise provides the same API.

## 2. Core Concepts

### 2.1 Automatic Column Discovery

Given a POJO class `T`, `EasyGrid<T>` introspects its properties (via getter/setter conventions) and creates a `Grid.Column` for each one on the wrapped grid, with appropriate renderers and sorting behavior.

### 2.2 Type-to-Renderer Mapping

Each Java type maps to a default column renderer and configuration:

| Java Type | Renderer | Default Format | Alignment | Sorting |
|-----------|----------|----------------|-----------|---------|
| `String` | `TextRenderer` | — | Start | Alphabetical |
| `Integer`, `int` | `NumberRenderer` | — | End | Numeric |
| `Long`, `long` | `NumberRenderer` | — | End | Numeric |
| `Double`, `double`, `Float`, `float` | `NumberRenderer` | — | End | Numeric |
| `BigDecimal` | `NumberRenderer` | — | End | Numeric |
| `Boolean`, `boolean` | `TextRenderer` | "true"/"false" | Center | — |
| `LocalDate` | `LocalDateRenderer` | `yyyy-MM-dd` | Start | Chronological |
| `LocalDateTime` | `LocalDateTimeRenderer` | `yyyy-MM-dd HH:mm:ss` | Start | Chronological |
| `LocalTime` | `TextRenderer` (formatted) | — | Start | Chronological |
| `Enum<?>` | `TextRenderer` | `toString()` | Start | Alphabetical |

Custom type mappings can be registered globally or per-grid instance.

**Boolean rendering:** `Boolean` columns render as the string literals `"true"` or `"false"` by default. Any other representation ("Yes"/"No", "On"/"Off", localised strings, etc.) requires a custom formatter — typically one that delegates to an application i18n provider.

**Enum rendering:** Enum columns render using `Enum.toString()`, not `Enum.name()`. Per the Java documentation for `Enum.name()`: *"Most programmers should use the toString method in preference to this one, as the toString method may return a more user-friendly name."* Enums that need human-readable labels should override `toString()` accordingly.

### 2.3 Sorting

All columns backed by `Comparable` property types are sortable by default. Multi-column sorting is supported.

For each sortable column, two distinct things are wired up automatically:

- **In-memory comparator** — set from the column's `ValueProvider` whenever the column's value type implements `Comparable` or is a primitive. Used by `ListDataProvider`-backed grids.
- **Backend sort property** — set to the bean property name on columns added via `addColumn(String)` (or any of the constructors that resolve named properties). Used by `BackEndDataProvider`-backed grids to map a column sort request to a backend sort key. Columns added via `addColumn(Class, ValueProvider)` have no backend sort property and must have one set explicitly with `EasyColumn.setSortProperty(...)` if backend sorting is needed.

The backend sort property defaults to the same string used as the column key, but the two are independent after creation: calling `setSortProperty(...)` or `setKey(...)` later changes only the one called.

## 3. API Design

### 3.1 Construction

`EasyGrid<T>` creates its own `Grid<T>` internally. The caller adds the `EasyGrid` instance to the layout and calls `setItems()` on it.

```java
// Basic: all discovered properties become columns
EasyGrid<Person> easyGrid = new EasyGrid<>(Person.class);
easyGrid.setItems(personService.findAll());
add(easyGrid);

// Selective: only specified properties become columns, in order
EasyGrid<Person> easyGrid = new EasyGrid<>(Person.class, "firstName", "lastName", "email", "age");
easyGrid.setItems(personService.findAll());
add(easyGrid);

// Manual: suppress automatic column creation; add columns explicitly
EasyGrid<Person> easyGrid = new EasyGrid<>(Person.class, false);
easyGrid.addColumn("firstName");
easyGrid.addColumn("lastName");
easyGrid.addColumn(String.class, person -> person.getAddress().getCity());
easyGrid.setItems(personService.findAll());
add(easyGrid);
```

For cases where a custom `Grid` subclass must be supplied, use `EasyGridWrapper`:

```java
TreeGrid<Person> treeGrid = new TreeGrid<>();
EasyGridWrapper<Person, TreeGrid<Person>> wrapper =
    new EasyGridWrapper<>(treeGrid, Person.class, "firstName", "lastName");
wrapper.setItems(personService.findAll());
add(wrapper);
// access the wrapped grid for TreeGrid-specific configuration
wrapper.getWrappedGrid().setItemHierarchyData(...);
```

`EasyGrid` also provides a typed overload for columns not backed by a named bean property:

```java
// Adds a typed column that participates in the configuration tree by type,
// but has no auto-generated key or header
EasyColumn<T, V> addColumn(Class<V> type, ValueProvider<T, ? extends V> getter);
```

### 3.2 Column Ordering and Visibility

```java
// Set column order (only listed columns shown, in this order)
easyGrid.setColumnOrder("firstName", "lastName", "email", "birthDate", "age");

// Hide columns
easyGrid.hideColumns("id", "createdAt", "updatedAt");
```

### 3.3 Column Configuration — `EasyColumn<T, V>`

`getColumn(String propertyName)` returns an `EasyColumn<T, V>` that provides both type-specific formatting and direct access to all standard `Grid.Column` setters for fluent configuration. The underlying `Grid.Column<T>` is also accessible via `EasyColumn.getColumn()` for any configuration not covered by the delegating API.

```java
public class EasyColumn<T, V> {

    // Type-specific formatting (EasyGrid-managed, applied to the column renderer)
    EasyColumn<T, V> setNullRepresentation(String nullRepresentation);
    EasyColumn<T, V> setFormatter(SerializableFunction<V, String> formatter);
    EasyColumn<T, V> setRendererFactory(RendererFactory<T, V> rendererFactory);
    EasyColumn<T, V> setTextAlign(ColumnTextAlign textAlign);

    // Cast-checked type narrowing — succeeds when the column's value type is a subtype of S
    <S> EasyColumn<T, S> as(Class<S> type);

    // The column value type
    Class<V> getType();

    // Standard Grid.Column configuration — delegated for fluent chaining
    EasyColumn<T, V> setHeader(String headerText);
    EasyColumn<T, V> setHeader(Component headerComponent);
    EasyColumn<T, V> setFooter(String footerText);
    EasyColumn<T, V> setFooter(Component footerComponent);
    EasyColumn<T, V> setWidth(String width);
    EasyColumn<T, V> setFlexGrow(int flexGrow);
    EasyColumn<T, V> setAutoWidth(boolean autoWidth);
    EasyColumn<T, V> setResizable(boolean resizable);
    EasyColumn<T, V> setSortable(boolean sortable);
    EasyColumn<T, V> setSortProperty(String... properties);
    EasyColumn<T, V> setFrozen(boolean frozen);
    EasyColumn<T, V> setFrozenToEnd(boolean frozenToEnd);
    EasyColumn<T, V> setVisible(boolean visible);
    EasyColumn<T, V> setKey(String key);
    // ... and other Grid.Column setters

    // Access the underlying Vaadin column for configuration not covered above
    Grid.Column<T> getColumn();
}
```

Usage example:

```java
// Format dates using a renderer factory from the renderer utility classes
easyGrid.getColumn("birthDate")
    .as(LocalDate.class)
    .setRendererFactory(LocalDateRenderers.of("dd/MM/yyyy"));

// Format booleans with a custom formatter
easyGrid.getColumn("subscriber")
    .as(Boolean.class)
    .setFormatter(b -> b ? "Active" : "Inactive");

// Standard Grid.Column configuration available directly on EasyColumn
easyGrid.getColumn("firstName")
    .setHeader("First Name")
    .setFrozen(true)
    .setWidth("200px");
```

### 3.4 Row Actions

See [FEATURE_ROW_ACTIONS.md](FEATURE_ROW_ACTIONS.md).

### 3.5 Type Configuration Tree

Column display configuration is resolved through a three-level tree, from most to least specific:

| Level | API | Scope |
|---|---|---|
| **Column** | `EasyColumn` setters | One specific column |
| **Instance** | `EasyGrid.typeConfiguration(Class)` | All columns of that type in one grid |
| **Global** | `GlobalEasyGridConfiguration.forType(Class)` | All grids in the application |

Within each level the class hierarchy is walked before the tree falls through to the next level (scope-first). The full resolution order for a `Foo extends Entity` column is:

```
Column·Foo
  → Instance·Foo → Instance·Entity → Instance·Object
    → Global·Foo → Global·Entity → Global·Object
      → Built-in default
```

The first non-`null` value found wins. See [CONFIGURATION_RESOLUTION.md](CONFIGURATION_RESOLUTION.md) for the rationale behind scope-first ordering.

**Column level** — `EasyGrid.addColumn(…)` returns an `EasyColumn` whose setters write into an isolated configuration node at the top of the chain for that column only:

```java
easyGrid.addColumn("active").setNullRepresentation("—");
easyGrid.addColumn("salary").setTextAlign(ColumnTextAlign.END);
```

**Instance level** — `EasyGrid.typeConfiguration(Class)` returns the instance-level `ColumnConfiguration` for a type. Changes apply to every column of that type on this grid:

```java
easyGrid.typeConfiguration(BigDecimal.class)
    .setRendererFactory(NumberRenderers.of("%,.2f", Locale.US));
```

**Global level** — `GlobalEasyGridConfiguration.forType(Class)` returns the application-wide `ColumnConfiguration`. Call `GlobalEasyGridConfiguration.freeze()` after startup to prevent further modifications:

```java
GlobalEasyGridConfiguration.forType(LocalDate.class)
    .setRendererFactory(LocalDateRenderers.of("dd/MM/yyyy"));
GlobalEasyGridConfiguration.freeze();
```

#### Null Representation

The `nullRepresentation` property controls what is displayed when a column value is `null`. The built-in global default registers `""` (empty string) on `Object.class`, so every column starts with an empty cell for `null` values. Override it at any level:

```java
// All columns in this grid show "–" for null
easyGrid.typeConfiguration(Object.class).setNullRepresentation("–");

// Only the "email" column shows "(none)" for null
easyGrid.addColumn("email").setNullRepresentation("(none)");
```

#### Type Hierarchy Support

Inside each scope level, configuration walks the Java class hierarchy. When a configuration is requested for `Foo` and none exists, it is created with `Foo`'s superclass configuration as its parent, continuing up to `Object`. Primitive types are mapped to their boxed counterparts before hierarchy walking (`int` → `Integer`, `boolean` → `Boolean`, etc.).

A global `Number.class` renderer factory is therefore automatically inherited by `Integer`, `Long`, `BigDecimal`, and every other `Number` subtype unless a more specific configuration overrides it.

#### Renderer Utility Classes

Three `@UtilityClass` types in `com.flowingcode.vaadin.addons.easygrid.renderers` produce `RendererFactory` instances for common value types:

- **`LocalDateRenderers`** — wraps `LocalDateRenderer`; overloads accept a format pattern, locale, null representation, or a `DateTimeFormatter` supplier.
- **`LocalDateTimeRenderers`** — wraps `LocalDateTimeRenderer`; same overloads as `LocalDateRenderers`.
- **`NumberRenderers`** — wraps `NumberRenderer`; overloads accept a `NumberFormat`, a `Locale`, or a `Formatter` pattern string with optional locale and null representation.

```java
GlobalEasyGridConfiguration.forType(LocalDate.class)
    .setRendererFactory(LocalDateRenderers.of("dd/MM/yyyy", Locale.UK));

GlobalEasyGridConfiguration.forType(Number.class)
    .setRendererFactory(NumberRenderers.of(NumberFormat.getInstance()));
```

## 4. Default Header Generation

When a column is added by `EasyGrid` and no explicit header has been set on it, the header is auto-generated from the property name using camelCase-to-title-case conversion:

| Property Name | Generated Header |
|--------------|-----------------|
| `firstName` | `First Name` |
| `lastName` | `Last Name` |
| `dateOfBirth` | `Date Of Birth` |
| `email` | `Email` |
| `isActive` | `Is Active` |

## 5. Nested Property Support

Nested properties can be referenced using dot notation:

```java
EasyGrid<Person> easyGrid = new EasyGrid<>(Person.class,
    "firstName", "lastName", "address.city", "address.postalCode");

easyGrid.getColumn("address.city")
    .setHeader("City")
    .setSortable(true);
```

## 6. Serialization

`EasyGrid<T>` must be fully serializable for Vaadin session persistence. All internal state, column configurations, and renderers must be serializable.

## 7. Usage Example — Complete

```java
EasyGrid<Person> easyGrid = new EasyGrid<>(Person.class);

// Column ordering and visibility
easyGrid.setColumnOrder("firstName", "lastName", "email", "birthDate", "age", "subscriber");
easyGrid.hideColumns("id", "createdAt", "updatedAt");

// Type-specific formatting
easyGrid.getColumn("birthDate").as(LocalDate.class)
    .setRendererFactory(LocalDateRenderers.of("dd/MM/yyyy"));
easyGrid.getColumn("subscriber").as(Boolean.class)
    .setFormatter(b -> b ? "Yes" : "No");

// Standard Grid.Column configuration via EasyColumn directly
easyGrid.getColumn("firstName")
    .setHeader("Name")
    .setFrozen(true);
easyGrid.getColumn("age")
    .setTextAlign(ColumnTextAlign.END);

// Data binding and adding to layout
easyGrid.setItems(personService.findAll());
add(easyGrid);
```

## 8. Dependencies

- Vaadin Flow (24.x)
- Lombok (per Flowing Code convention for new add-ons)

## 9. Non-Goals (Out of Scope)

- Data binding, selection, events, filtering, sorting configuration — use `Grid` or GridHelper directly
- Inline cell editing (use EasyForm for editing)
- Server-side data persistence
- Tree grid / hierarchical data
