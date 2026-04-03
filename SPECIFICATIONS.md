# Easy Grid Add-on — Specification

## 1. Overview

The Easy Grid Add-on is a Vaadin Flow component that automatically generates a fully functional, sortable data grid from a Java POJO definition. It uses reflection to discover bean properties, maps them to appropriately typed and formatted columns, and provides a clean Java API for controlling column visibility, ordering, rendering, row actions, and data export via the Grid Exporter Add-on.

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

### 2.4 Data Binding

`EasyGrid<T>` wraps Vaadin's `Grid<T>` and supports all standard data provider mechanisms:

- In-memory lists (`setItems(List<T>)`)
- Lazy data providers (`setDataProvider(DataProvider<T, ?>)`)
- Callback data providers for backend integration

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

### 3.2 Column Configuration — Fluent API

```java
EasyGrid<Person> grid = new EasyGrid<>(Person.class);

// Configure individual columns
grid.getColumnConfig("firstName")
    .withHeader("First Name")
    .withSortable(true)
    .withWidth("200px")
    .withResizable(true);

grid.getColumnConfig("age")
    .withHeader("Age")
    .withTextAlign(ColumnTextAlign.END);

grid.getColumnConfig("birthDate")
    .withHeader("Date of Birth")
    .withDateFormat("dd/MM/yyyy");

grid.getColumnConfig("subscriber")
    .withHeader("Subscribed")
    .withBooleanLabels("Active", "Inactive");

// Hide columns
grid.hideColumns("id", "createdAt", "updatedAt");

// Set column order (only listed columns shown, in this order)
grid.setColumnOrder("firstName", "lastName", "email", "birthDate", "age");

// Freeze columns (always visible when scrolling horizontally)
grid.getColumnConfig("firstName").withFrozen(true);
```

### 3.3 Column Configuration Wrapper — `EasyColumnConfig<T, V>`

The `getColumnConfig(String propertyName)` method returns a fluent wrapper:

```java
public class EasyColumnConfig<T, V> {
    // Header
    EasyColumnConfig<T, V> withHeader(String header);
    EasyColumnConfig<T, V> withHeader(Component headerComponent);

    // Footer
    EasyColumnConfig<T, V> withFooter(String footer);
    EasyColumnConfig<T, V> withFooter(Component footerComponent);

    // Sizing
    EasyColumnConfig<T, V> withWidth(String width);
    EasyColumnConfig<T, V> withFlexGrow(int flexGrow);
    EasyColumnConfig<T, V> withAutoWidth(boolean autoWidth);
    EasyColumnConfig<T, V> withResizable(boolean resizable);

    // Sorting
    EasyColumnConfig<T, V> withSortable(boolean sortable);
    EasyColumnConfig<T, V> withSortProperty(String... properties);

    // Alignment
    EasyColumnConfig<T, V> withTextAlign(ColumnTextAlign align);

    // Freezing
    EasyColumnConfig<T, V> withFrozen(boolean frozen);
    EasyColumnConfig<T, V> withFrozenToEnd(boolean frozen);

    // Visibility
    EasyColumnConfig<T, V> withVisible(boolean visible);

    // Custom rendering
    EasyColumnConfig<T, V> withRenderer(Renderer<T> renderer);
    EasyColumnConfig<T, V> withComponentRenderer(SerializableFunction<T, Component> componentProvider);
    EasyColumnConfig<T, V> withValueFormatter(SerializableFunction<V, String> formatter);

    // Type-specific formatting
    EasyColumnConfig<T, V> withDateFormat(String pattern);
    EasyColumnConfig<T, V> withDateTimeFormat(String pattern);
    EasyColumnConfig<T, V> withNumberFormat(String pattern);
    EasyColumnConfig<T, V> withBooleanLabels(String trueLabel, String falseLabel);

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
grid.getActionsColumnConfig()
    .withHeader("Actions")
    .withWidth("150px")
    .withFrozenToEnd(true);
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
    config.withTextAlign(ColumnTextAlign.END);
    config.withValueFormatter(money -> money.getCurrency() + " " + money.getAmount());
});

// Register a global renderer for nested types
EasyGrid.registerTypeConfig(Address.class, config -> {
    config.withValueFormatter(address ->
        address.getStreet() + ", " + address.getCity()
    );
});
```

### 3.8 Selection

```java
// Single selection (default)
grid.setSelectionMode(Grid.SelectionMode.SINGLE);
grid.addSelectionListener(event -> {
    event.getFirstSelectedItem().ifPresent(this::showDetails);
});

// Multi-selection
grid.setSelectionMode(Grid.SelectionMode.MULTI);
grid.addSelectionListener(event -> {
    Set<Person> selected = event.getAllSelectedItems();
    // bulk action
});
```

### 3.9 Filtering

`EasyGrid<T>` supports in-memory filtering for list-based data providers:

```java
// Add a header filter row (auto-generated filter fields per column)
grid.enableHeaderFilters();

// Programmatic filtering
grid.setFilter(person ->
    person.getAge() >= 18 && person.isActive()
);

// Combined with external filter (for use with EasyCRUD)
grid.setExternalFilter(SerializablePredicate<T> filter);
```

### 3.10 Events

```java
// Row click
grid.addItemClickListener(event -> {
    showDetails(event.getItem());
});

// Row double-click
grid.addItemDoubleClickListener(event -> {
    editPerson(event.getItem());
});

// Sort change
grid.addSortListener(event -> {
    // handle sort change
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

grid.getColumnConfig("address.city")
    .withHeader("City")
    .withSortable(true);
```

## 6. Serialization

`EasyGrid<T>` must be fully serializable for Vaadin session persistence. All internal state, column configurations, action handlers, and renderers must be serializable.

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
grid.getColumnConfig("firstName").withHeader("Name").withFrozen(true);
grid.getColumnConfig("birthDate").withHeader("Born").withDateFormat("dd/MM/yyyy");
grid.getColumnConfig("subscriber").withBooleanLabels("Yes", "No");
grid.getColumnConfig("age").withTextAlign(ColumnTextAlign.END);

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
