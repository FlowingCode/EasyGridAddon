# Easy Grid Add-on — Specification

## 1. Overview

The Easy Grid Add-on is a Vaadin Flow component that automatically generates a fully functional, sortable data grid from a Java POJO definition. `EasyGrid<T>` extends Vaadin's `Grid<T>`, adding reflection-based column discovery, type-to-renderer mapping, a fluent column configuration API, row actions, and data export via the Grid Exporter Add-on.

## 2. Core Concepts

### 2.1 Automatic Column Discovery

Given a POJO class `T`, `EasyGrid<T>` introspects its properties (via getter/setter conventions) and creates a `Grid.Column` for each one, with appropriate renderers and sorting behavior.

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

All columns backed by `Comparable` property types are sortable by default. Multi-column sorting is supported. Sorting can be disabled per column.

## 3. API Design

### 3.1 Construction

```java
// Basic: all discovered properties become columns
EasyGrid<Person> grid = new EasyGrid<>(Person.class);
grid.setItems(personService.findAll());

// Selective: only specified properties become columns, in order
EasyGrid<Person> grid = new EasyGrid<>(Person.class, "firstName", "lastName", "email", "age");
grid.setItems(personService.findAll());
```

### 3.2 Column Configuration

Standard `Grid.Column` properties (header, width, sorting, alignment, etc.) are configured directly on the column via `getColumnByKey()`. `getColumnConfig()` is used only for EasyGrid-specific configuration.

```java
EasyGrid<Person> grid = new EasyGrid<>(Person.class);

// Standard Grid.Column configuration
grid.getColumnByKey("firstName")
    .setHeader("First Name")
    .setSortable(true)
    .setWidth("200px")
    .setResizable(true);

grid.getColumnByKey("age")
    .setHeader("Age")
    .setTextAlign(ColumnTextAlign.END);

// EasyGrid-specific configuration (type-aware formatting)
grid.getColumnByKey("birthDate").setHeader("Date of Birth");
grid.getColumnConfig("birthDate").withDateFormat("dd/MM/yyyy");

grid.getColumnByKey("subscriber").setHeader("Subscribed");
grid.getColumnConfig("subscriber").withBooleanLabels("Active", "Inactive");

// Hide columns
grid.hideColumns("id", "createdAt", "updatedAt");

// Set column order (only listed columns shown, in this order)
grid.setColumnOrder("firstName", "lastName", "email", "birthDate", "age");

// Freeze columns (always visible when scrolling horizontally)
grid.getColumnByKey("firstName").setFrozen(true);
```

### 3.3 Column Configuration Wrapper — `EasyColumnConfig<T, V>`

`getColumnConfig(String propertyName)` returns a wrapper that exposes only EasyGrid-specific configuration — formatting, rendering conveniences, and export options. All other column properties are set directly on `Grid.Column` via `getColumn()` or `getColumnByKey()`.

```java
public class EasyColumnConfig<T, V> {
    // Custom rendering
    EasyColumnConfig<T, V> withComponentRenderer(SerializableFunction<T, Component> componentProvider);
    EasyColumnConfig<T, V> withValueFormatter(SerializableFunction<V, String> formatter);

    // Type-specific formatting (changes the column renderer)
    EasyColumnConfig<T, V> withDateFormat(String pattern);
    EasyColumnConfig<T, V> withDateTimeFormat(String pattern);
    EasyColumnConfig<T, V> withNumberFormat(String pattern);
    EasyColumnConfig<T, V> withBooleanLabels(String trueLabel, String falseLabel);

    // Export configuration (Grid Exporter integration)
    EasyColumnConfig<T, V> withExportHeader(String header);
    EasyColumnConfig<T, V> withExportable(boolean exportable);

    // Access underlying Vaadin column
    Grid.Column<T> getColumn();
}
```

### 3.4 Row Actions

Row actions are buttons or menu items displayed in an actions column, typically at the end of each row.

```java
// Add action buttons per row
grid.addRowAction("Edit", VaadinIcon.EDIT, person -> {
    editPerson(person);
});

grid.addRowAction("Delete", VaadinIcon.TRASH, ButtonVariant.LUMO_ERROR, person -> {
    personService.delete(person);
    grid.getDataProvider().refreshAll();
});

// Actions as a context menu (overflow menu) instead of inline buttons
grid.setRowActionsAsMenu(true);

// Conditional action visibility
grid.addRowAction("Activate", VaadinIcon.CHECK, person -> {
    personService.activate(person);
}).withVisibleWhen(person -> !person.isActive());

grid.addRowAction("Deactivate", VaadinIcon.CLOSE, person -> {
    personService.deactivate(person);
}).withVisibleWhen(person -> person.isActive());

// Configure the actions column
grid.getActionsColumnConfig().getColumn()
    .setHeader("Actions")
    .setWidth("150px")
    .setFrozenToEnd(true);
```

### 3.5 Row Action Wrapper — `EasyRowAction<T>`

```java
public class EasyRowAction<T> {
    // Conditional visibility
    EasyRowAction<T> withVisibleWhen(SerializablePredicate<T> predicate);

    // Conditional enablement
    EasyRowAction<T> withEnabledWhen(SerializablePredicate<T> predicate);

    // Tooltip
    EasyRowAction<T> withTooltip(String tooltip);
    EasyRowAction<T> withTooltip(SerializableFunction<T, String> tooltipProvider);

    // Confirmation dialog before executing the action
    EasyRowAction<T> withConfirmation(String message);
    EasyRowAction<T> withConfirmation(String title, String message);
}
```

### 3.6 Grid Exporter Integration

The add-on integrates with the Grid Exporter Add-on to provide data export capabilities.

```java
// Enable export with default formats (Excel, CSV, Docx)
grid.enableExport();

// Enable specific export formats
grid.enableExport(ExportFormat.EXCEL, ExportFormat.CSV);

// Configure export
grid.getExportConfig()
    .withFileName("person-report")
    .withSheetName("People")
    .withTitle("Person Report");

// Custom column export configuration (e.g., different header for export)
grid.getColumnConfig("firstName").withExportHeader("Given Name");

// Exclude a column from export (e.g., the actions column)
grid.getColumnConfig("actions").withExportable(false);
```

### 3.7 Global Type Configuration

Register custom column configurations that apply to all `EasyGrid` instances:

```java
// Register a global formatter for a custom type
EasyGrid.registerTypeConfig(Money.class, config -> {
    config.getColumn().setTextAlign(ColumnTextAlign.END);
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

When no explicit header is provided, headers are auto-generated from property names using camelCase-to-title-case conversion:

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
grid.setColumnOrder("firstName", "lastName", "address.city", "address.postalCode");

grid.getColumnByKey("address.city")
    .setHeader("City")
    .setSortable(true);
```

## 6. Serialization

`EasyGrid<T>` inherits Vaadin `Grid<T>`'s serialization support. All EasyGrid-specific state — column configurations, action handlers, and custom renderers — must be serializable for Vaadin session persistence.

## 7. Usage Example — Complete

```java
// Minimal usage
EasyGrid<Person> simpleGrid = new EasyGrid<>(Person.class);
simpleGrid.setItems(personService.findAll());
add(simpleGrid);

// Customized usage
EasyGrid<Person> grid = new EasyGrid<>(Person.class);

// Configure columns
grid.setColumnOrder("firstName", "lastName", "email", "birthDate", "age", "subscriber");
grid.hideColumns("id", "createdAt", "updatedAt");

grid.getColumnByKey("firstName").setHeader("Name").setFrozen(true);
grid.getColumnByKey("birthDate").setHeader("Born");
grid.getColumnConfig("birthDate").withDateFormat("dd/MM/yyyy");
grid.getColumnConfig("subscriber").withBooleanLabels("Yes", "No");
grid.getColumnByKey("age").setTextAlign(ColumnTextAlign.END);

// Row actions
grid.addRowAction("Edit", VaadinIcon.EDIT, this::editPerson);
grid.addRowAction("Delete", VaadinIcon.TRASH, ButtonVariant.LUMO_ERROR, person -> {
    personService.delete(person);
    grid.getDataProvider().refreshAll();
}).withConfirmation("Are you sure you want to delete this person?");

// Enable export
grid.enableExport();
grid.getExportConfig().withFileName("people-export");

// Set data
grid.setItems(personService.findAll());

add(grid);
```

## 8. Dependencies

- Vaadin Flow (24.x)
- Grid Exporter Add-on (for export functionality)
- Lombok (per Flowing Code convention for new add-ons)

## 9. Non-Goals (Out of Scope)

- Inline cell editing (use EasyForm for editing)
- Server-side data persistence
- Tree grid / hierarchical data
- Lazy loading (supported via Vaadin's DataProvider API but not auto-configured)
