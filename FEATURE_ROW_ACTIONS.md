# Feature: Row Actions

Row actions are buttons or menu items displayed in a dedicated actions column, created and managed by `EasyGrid` on the wrapped grid.

## API

### `EasyGrid<T>` methods

```java
// Label only
EasyRowAction<T> addRowAction(String label, SerializableConsumer<T> handler);

// Label + static icon (Icon instance or VaadinIcon via IconFactory)
EasyRowAction<T> addRowAction(String label, Icon icon, SerializableConsumer<T> handler);
EasyRowAction<T> addRowAction(String label, IconFactory iconFactory, SerializableConsumer<T> handler);

// Icon only (no label)
EasyRowAction<T> addRowAction(Icon icon, SerializableConsumer<T> handler);
EasyRowAction<T> addRowAction(IconFactory iconFactory, SerializableConsumer<T> handler);

// Per-row dynamic icon via ValueProvider
<ICON extends AbstractIcon<ICON>>
EasyRowAction<T> addRowAction(ValueProvider<T, ICON> iconProvider, SerializableConsumer<T> handler);

<ICON extends AbstractIcon<ICON>>
EasyRowAction<T> addRowAction(String label, ValueProvider<T, ICON> iconProvider, SerializableConsumer<T> handler);

// Render all actions as a context menu (overflow menu) instead of inline buttons
void setRowActionsAsMenu(boolean asMenu);

// Access the underlying Grid.Column for header, width, freezing, etc.
Grid.Column<T> getActionsColumn();
```

### `EasyRowAction<T>`

```java
public class EasyRowAction<T> {
    // Conditional visibility
    EasyRowAction<T> visibleWhen(SerializablePredicate<T> predicate);

    // Conditional enablement
    EasyRowAction<T> enabledWhen(SerializablePredicate<T> predicate);

    // Tooltip
    EasyRowAction<T> tooltip(String tooltip);
    EasyRowAction<T> tooltip(ValueProvider<T, String> tooltipProvider);

    // Confirmation dialog before executing the action
    EasyRowAction<T> withConfirmation(String message);
    EasyRowAction<T> withConfirmation(String title, String message);
}
```

## Usage

```java
// Label + VaadinIcon (VaadinIcon implements IconFactory)
easyGrid.addRowAction("Edit", VaadinIcon.EDIT, person -> {
    editPerson(person);
});

easyGrid.addRowAction("Delete", VaadinIcon.TRASH, person -> {
    personService.delete(person);
    easyGrid.getDataProvider().refreshAll();
}).withConfirmation("Are you sure you want to delete this person?");

// Label only
easyGrid.addRowAction("Details", person -> showDetails(person));

// Per-row dynamic icon
easyGrid.addRowAction(
    person -> person.isActive() ? VaadinIcon.CHECK.create() : VaadinIcon.CLOSE.create(),
    person -> toggleActive(person)
);

// Actions as a context menu (overflow menu) instead of inline buttons
easyGrid.setRowActionsAsMenu(true);

// Conditional visibility
easyGrid.addRowAction("Activate", VaadinIcon.CHECK, person -> {
    personService.activate(person);
}).visibleWhen(person -> !person.isActive());

easyGrid.addRowAction("Deactivate", VaadinIcon.CLOSE, person -> {
    personService.deactivate(person);
}).visibleWhen(person -> person.isActive());

// Configure the actions column via the underlying Grid.Column
easyGrid.getActionsColumn()
    .setHeader("Actions")
    .setWidth("150px")
    .setFrozenToEnd(true);
```
