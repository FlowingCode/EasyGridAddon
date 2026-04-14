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
| `Boolean`, `boolean` | `TextRenderer` ("Yes"/"No") | Center | — |
| `LocalDate` | `LocalDateRenderer` | Start | Chronological |
| `LocalDateTime` | `LocalDateTimeRenderer` | Start | Chronological |
| `LocalTime` | `TextRenderer` (formatted) | Start | Chronological |
| `Enum<?>` | `TextRenderer` (name) | Start | Alphabetical |

Custom type mappings can be registered globally or per-grid instance.

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
```

### 3.2 Column Ordering and Visibility

```java
// Set column order (only listed columns shown, in this order)
easyGrid.setColumnOrder("firstName", "lastName", "email", "birthDate", "age");

// Hide columns
easyGrid.hideColumns("id", "createdAt", "updatedAt");
```

### 3.3 Column Configuration — `EasyColumn<T, V>`

`getColumnConfig(String propertyName)` returns a wrapper that provides type-specific formatting. For all other column properties (header, footer, width, alignment, sorting, freezing, visibility, renderer), use `getColumn()` to access the underlying `Grid.Column<T>` directly.

```java
public class EasyColumn<T, V> {

    // Type-specific formatting (EasyGrid-managed, applied to the column renderer)
    EasyColumn<T, V> withValueFormatter(SerializableFunction<V, String> formatter);
    EasyColumn<T, V> withDateFormat(String pattern);
    EasyColumn<T, V> withDateTimeFormat(String pattern);
    EasyColumn<T, V> withNumberFormat(String pattern);
    EasyColumn<T, V> withBooleanLabels(String trueLabel, String falseLabel);

    // Access underlying Vaadin column for all other configuration
    Grid.Column<T> getColumn();
}
```

Usage example:

```java
easyGrid.getColumnConfig("birthDate")
    .withDateFormat("dd/MM/yyyy");

easyGrid.getColumnConfig("subscriber")
    .withBooleanLabels("Active", "Inactive");

// Use getColumn() for standard Grid.Column configuration
easyGrid.getColumnConfig("firstName").getColumn()
    .setHeader("First Name")
    .setFrozen(true)
    .setWidth("200px");
```

### 3.4 Row Actions

See [FEATURE_ROW_ACTIONS.md](FEATURE_ROW_ACTIONS.md).

### 3.5 Global Type Configuration

Register custom column configurations that apply to all `EasyGrid` instances:

```java
// Register a global formatter for a custom type
EasyGrid.registerTypeConfig(Money.class, config -> {
    config.withValueFormatter(money -> money.getCurrency() + " " + money.getAmount());
});

// Register a global renderer for nested types
EasyGrid.registerTypeConfig(Address.class, config -> {
    config.withValueFormatter(address ->
        address.getStreet() + ", " + address.getCity()
    );
});
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

easyGrid.getColumnConfig("address.city").getColumn()
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
easyGrid.getColumnConfig("birthDate").withDateFormat("dd/MM/yyyy");
easyGrid.getColumnConfig("subscriber").withBooleanLabels("Yes", "No");

// Standard Grid.Column configuration via getColumn()
easyGrid.getColumnConfig("firstName").getColumn()
    .setHeader("Name")
    .setFrozen(true);
easyGrid.getColumnConfig("age").getColumn()
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
