# Feature: Row Actions

Row actions are buttons or menu items displayed in a dedicated actions column, created and managed by `EasyGrid` on the wrapped grid.

## API

### `EasyGrid<T>` methods

#### Adding row actions

All `addRowAction` overloads append a new action to the actions column and return an `EasyRowAction<T>` that can be further configured via its fluent API (visibility, enablement, tooltip, confirmation).

```java
EasyRowAction<T> addRowAction(String label, SerializableConsumer<T> handler);
```
Adds a text-only action button. When the user clicks it, `handler` is invoked with the corresponding row item.

---

```java
EasyRowAction<T> addRowAction(String label, Icon icon, SerializableConsumer<T> handler);
EasyRowAction<T> addRowAction(String label, IconFactory iconFactory, SerializableConsumer<T> handler);
```
Adds an action button with both a label and a static icon rendered alongside it. The icon is cloned for each row, so only its configuration (type, style) is captured — the original instance is not reused. `VaadinIcon` implements `IconFactory` and can be passed directly.

---

```java
EasyRowAction<T> addRowAction(Icon icon, SerializableConsumer<T> handler);
EasyRowAction<T> addRowAction(IconFactory iconFactory, SerializableConsumer<T> handler);
```
Adds an icon-only action button with no visible label. The icon is cloned for each row, so only its configuration (type, style) is captured — the original instance is not reused. Useful when space is limited and the icon alone conveys the action.

---

```java
<ICON extends AbstractIcon<ICON>>
EasyRowAction<T> addRowAction(ValueProvider<T, ICON> iconProvider, SerializableConsumer<T> handler);

<ICON extends AbstractIcon<ICON>>
EasyRowAction<T> addRowAction(String label, ValueProvider<T, ICON> iconProvider, SerializableConsumer<T> handler);
```
Adds an action button whose icon is resolved per row by calling `iconProvider` with the row item, so different rows may show a different icon for the same action. The optional `label` is shown alongside the dynamic icon.

---

#### Rendering mode

```java
void setRowActionsAsMenu(boolean asMenu);
```
Controls how row actions are rendered. When `true`, all actions are presented as items in an overflow (context) menu triggered by a single button per row. When `false` (the default), each action is rendered as a separate inline button in the actions column.

---

#### Accessing the actions column

```java
Grid.Column<T> getActionsColumn();
```
Returns the `Grid.Column<T>` backing the actions column, allowing the caller to configure its header text, width, freeze position, or any other `Grid.Column` property. The actions column is created automatically when the first action is added.

---

#### Default theme variants

```java
void setDefaultRowActionVariants(ButtonVariant... variants);
```
Sets the Vaadin `ButtonVariant`s applied by default to every action button created *after* this call; actions added earlier are unaffected. The built-in default is `LUMO_TERTIARY_INLINE`. Pass no arguments (or `null`) to clear the defaults. Individual actions can still add their own variants via `EasyRowAction.addThemeVariants(...)`.

---

#### Custom renderer

```java
void setRowActionsRenderer(RowActionsRenderer<T> renderer);
```
Replaces the strategy that turns the registered actions into UI. The built-in inline-button and context-menu renderers (selected via `setRowActionsAsMenu`) cover the common cases; supply a custom `RowActionsRenderer<T>` to present actions another way. The previous renderer is cleaned up and a rebuild is scheduled. Note that a subsequent `setRowActionsAsMenu(...)` call installs a built-in renderer, replacing a custom one.

---

#### Refreshing after configuration changes

```java
void refreshRowActions();
```
Schedules a rebuild of the actions column on the next server response. The fluent `EasyRowAction` methods (`visibleWhen`, `enabledWhen`, `tooltip`, `withConfirmation`) already trigger this automatically, so an explicit call is only needed after changing an action's styling or theme variants (see below), which are not applied automatically.

---

### `EasyRowAction<T>`

All mutator methods return `this` to support method chaining.

#### Visibility

```java
EasyRowAction<T> visibleWhen(SerializablePredicate<T> predicate);
```
Makes the action conditionally visible on a per-row basis. The predicate is evaluated against each row's item when the row is rendered; the action button is hidden for rows where the predicate returns `false`.

---

#### Enablement

```java
EasyRowAction<T> enabledWhen(SerializablePredicate<T> predicate);
```
Makes the action conditionally enabled on a per-row basis. The action button is shown in a disabled state (visible but not clickable) for rows where the predicate returns `false`.

---

#### Tooltip

```java
EasyRowAction<T> tooltip(String tooltip);
```
Sets a fixed tooltip displayed when the user hovers over the action button.

```java
EasyRowAction<T> tooltip(ValueProvider<T, String> tooltipProvider);
```
Sets a per-row tooltip resolved by calling `tooltipProvider` with the row item, so different rows may show different tooltip text for the same action.

---

#### Confirmation

```java
EasyRowAction<T> withConfirmation(String message);
EasyRowAction<T> withConfirmation(String title, String message);
```
Intercepts button clicks and presents a confirmation dialog before invoking the action handler. The handler is only called if the user confirms. `message` is the confirmation prompt shown to the user; the optional `title` sets the dialog heading.

---

#### Styling and theme variants

`EasyRowAction<T>` implements `HasStyle` and `HasThemeVariant<ButtonVariant>`, so the action's button can be styled and themed directly:

```java
action.addClassName("danger");
action.getStyle().set("font-weight", "bold");
action.addThemeVariants(ButtonVariant.LUMO_ERROR);
```
These are forwarded onto the rendered button. Unlike the fluent mutators above, style and theme-variant changes made after the grid has already rendered are **not** applied automatically — call `easyGrid.refreshRowActions()` afterwards to make them visible.

---

#### Removal

```java
void remove();
```
Removes this action from the actions column and triggers an immediate re-render so the change is visible without waiting for a data refresh. If the action has already been removed, this call is a no-op. After removal the `EasyRowAction` reference is considered dead and cannot be re-added; call `addRowAction` again to create a new action.

---

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

// Removing an action
EasyRowAction<T> adminAction = easyGrid.addRowAction("Purge", VaadinIcon.TRASH, item -> purge(item));
// later:
adminAction.remove();

// Default theme variants applied to every action added afterwards
easyGrid.setDefaultRowActionVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

// Style or theme an individual action's button
easyGrid.addRowAction("Reset", person -> reset(person))
    .addClassName("warning");

// Configure the actions column via the underlying Grid.Column
easyGrid.getActionsColumn()
    .setHeader("Actions")
    .setWidth("150px")
    .setFrozenToEnd(true);
```
