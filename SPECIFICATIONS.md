# Easy Grid Add-on — Specification

## 1. Overview

The Easy Grid Add-on is a configuration helper that wraps an externally instantiated Vaadin `Grid<T>`. It uses reflection to discover bean properties, maps them to appropriately typed and formatted columns, and provides a clean Java API for controlling column ordering and type-specific rendering.

`EasyGrid<T>` is **not** a component — it configures an existing `Grid<T>` that was created and managed by the caller. Data binding, selection, events, and all other standard `Grid` features are accessed directly on the wrapped `Grid` instance.

## 2. Core Concepts

### 2.1 Automatic Column Discovery

Given a POJO class `T`, `EasyGrid<T>` introspects its properties (via getter/setter conventions) and creates a `Grid.Column` for each one on the wrapped grid, with appropriate renderers and sorting behavior.

### 2.2 Type-to-Renderer Mapping

Each Java type maps to a default column renderer and configuration:

| Java Type | Renderer | Alignment | Sorting |
|-----------|----------|-----------|---------|
| `String` | `TextRenderer` | Start | Alphabetical |
| `Integer`, `int` | `NumberRenderer` | End | Numeric |
| `Long`, `long` | `NumberRenderer` | End | Numeric |
| `Double`, `double`, `Float`, `float` | `NumberRenderer` | End | Numeric |
| `BigDecimal` | `NumberRenderer` | End | Numeric |
| `Boolean`, `boolean` | `TextRenderer` ("true"/"false") | Center | — |
| `LocalDate` | `LocalDateRenderer` | Start | Chronological |
| `LocalDateTime` | `LocalDateTimeRenderer` | Start | Chronological |
| `LocalTime` | `TextRenderer` (formatted) | Start | Chronological |
| `Enum<?>` | `TextRenderer` (`toString()`) | Start | Alphabetical |

Custom type mappings can be registered globally or per-grid instance.

**Boolean rendering:** `Boolean` columns render as the string literals `"true"` or `"false"` by default. Any other representation ("Yes"/"No", "On"/"Off", localised strings, etc.) requires a custom formatter — typically one that delegates to an application i18n provider.

**Enum rendering:** Enum columns render using `Enum.toString()`, not `Enum.name()`. Per the Java documentation for `Enum.name()`: *"Most programmers should use the toString method in preference to this one, as the toString method may return a more user-friendly name."* Enums that need human-readable labels should override `toString()` accordingly.

### 2.3 Sorting

All columns backed by `Comparable` property types are sortable by default. Multi-column sorting is supported.

## 3. API Design

### 3.1 Construction

`EasyGrid<T>` takes an existing `Grid<T>` instance and a bean class. The caller retains full ownership of the grid.

```java
// Basic: all discovered properties become columns
Grid<Person> grid = new Grid<>();
EasyGrid<Person> easyGrid = new EasyGrid<>(grid, Person.class);
grid.setItems(personService.findAll());
add(grid);

// Selective: only specified properties become columns, in order
Grid<Person> grid = new Grid<>();
EasyGrid<Person> easyGrid = new EasyGrid<>(grid, Person.class, "firstName", "lastName", "email", "age");
grid.setItems(personService.findAll());
add(grid);

// Manual: suppress automatic column creation; add columns explicitly
Grid<Person> grid = new Grid<>();
EasyGrid<Person> easyGrid = new EasyGrid<>(grid, Person.class, false);
easyGrid.addColumn("firstName");
easyGrid.addColumn("lastName");
easyGrid.addColumn(String.class, person -> person.getAddress().getCity());
grid.setItems(personService.findAll());
add(grid);
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
| **Instance** | `EasyGrid.forType(Class)` | All columns of that type in one grid |
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

**Instance level** — `EasyGrid.forType(Class)` returns the instance-level `ColumnConfiguration` for a type. Changes apply to every column of that type on this grid:

```java
easyGrid.forType(BigDecimal.class)
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
easyGrid.forType(Object.class).setNullRepresentation("–");

// Only the "email" column shows "(none)" for null
easyGrid.addColumn("email").setNullRepresentation("(none)");
```

Formatters that receive a `ColumnConfiguration` parameter can call `getNullRepresentation()` to produce consistent output.

#### Type Hierarchy Support

Inside each scope level, `EasyGridConfigurationClassMap` walks the Java class hierarchy. When a configuration is requested for `Foo` and none exists, it is created with `Foo`'s superclass configuration as its parent, continuing up to `Object`. Primitive types are mapped to their boxed counterparts before hierarchy walking (`int` → `Integer`, `boolean` → `Boolean`, etc.).

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
EasyGrid<Person> easyGrid = new EasyGrid<>(grid, Person.class,
    "firstName", "lastName", "address.city", "address.postalCode");

easyGrid.getColumn("address.city")
    .setHeader("City")
    .setSortable(true);
```

## 6. Serialization

`EasyGrid<T>` must be fully serializable for Vaadin session persistence. All internal state, column configurations, and renderers must be serializable.

## 7. Usage Example — Complete

```java
Grid<Person> grid = new Grid<>();
EasyGrid<Person> easyGrid = new EasyGrid<>(grid, Person.class);

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

// Data binding and adding to layout — done on the Grid directly
grid.setItems(personService.findAll());
add(grid);
```

## 8. Dependencies

- Vaadin Flow (24.x)
- Lombok (per Flowing Code convention for new add-ons)

## 9. Non-Goals (Out of Scope)

- Data binding, selection, events, filtering, sorting configuration — use `Grid` or GridHelper directly
- Inline cell editing (use EasyForm for editing)
- Server-side data persistence
- Tree grid / hierarchical data
