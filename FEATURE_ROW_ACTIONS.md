# Feature: Row Actions

Row actions are buttons or menu items displayed in a dedicated actions column, created and managed by `EasyGrid` on the wrapped grid.

## API

### `EasyGrid<T>` methods

```java
// Add an action button (label + icon)
EasyRowAction<T> addRowAction(String label, VaadinIcon icon, SerializableConsumer<T> handler);

// Add an action button with a theme variant
EasyRowAction<T> addRowAction(String label, VaadinIcon icon, ButtonVariant variant, SerializableConsumer<T> handler);

// Render all actions as a context menu (overflow menu) instead of inline buttons
void setRowActionsAsMenu(boolean asMenu);

// Access the underlying Grid.Column for header, width, freezing, etc.
Grid.Column<T> getActionsColumn();
```

### `EasyRowAction<T>`

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

## Usage

```java
// Inline action buttons
easyGrid.addRowAction("Edit", VaadinIcon.EDIT, person -> {
    editPerson(person);
});

easyGrid.addRowAction("Delete", VaadinIcon.TRASH, ButtonVariant.LUMO_ERROR, person -> {
    personService.delete(person);
    easyGrid.getDataProvider().refreshAll();
}).withConfirmation("Are you sure you want to delete this person?");

// Actions as a context menu (overflow menu) instead of inline buttons
easyGrid.setRowActionsAsMenu(true);

// Conditional visibility
easyGrid.addRowAction("Activate", VaadinIcon.CHECK, person -> {
    personService.activate(person);
}).withVisibleWhen(person -> !person.isActive());

easyGrid.addRowAction("Deactivate", VaadinIcon.CLOSE, person -> {
    personService.deactivate(person);
}).withVisibleWhen(person -> person.isActive());

// Configure the actions column via the underlying Grid.Column
easyGrid.getActionsColumn()
    .setHeader("Actions")
    .setWidth("150px")
    .setFrozenToEnd(true);
```
