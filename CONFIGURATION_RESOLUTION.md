# Configuration Resolution: Scope-First vs. Type-First

## The Two Strategies

Given `Foo extends Entity` with configurations at four points in the matrix:

| | Instance | Global |
|---|---|---|
| **Foo** | I·Foo | G·Foo |
| **Entity** | I·Entity | G·Entity |

**Scope-first** exhausts all instance settings before looking at global ones:
```
I·Foo → I·Entity → G·Foo → G·Entity → Default
```

**Type-first** exhausts all Foo-specific settings before looking at Entity:
```
I·Foo → G·Foo → I·Entity → G·Entity → Default
```

Both agree on `I·Foo` being first and `G·Entity` being last. The dispute is the middle two.

---

## Where Scope-First Makes Sense

**Setting: `textAlign` or `nullRepresentation`**

A developer creates a reporting grid and writes:
```java
easyGrid.typeConfiguration(Entity.class).setTextAlign(RIGHT);
```
The intent is *"every Entity-like column in this grid should be right-aligned."* Since `Foo IS-AN Entity`, Foo columns should also be right-aligned. Instance context wins: the developer explicitly opted every Entity column into this layout decision, and Foo, being substitutable for Entity, should inherit it.

If type-first were applied, `G·Foo` would sit between `I·Foo` and `I·Entity`. An unrelated global renderer for Foo would silently block the instance-level alignment — which is the opposite of what the developer intended.

---

## Where Type-First Makes Sense

**Setting: `rendererFactory`**

Globally, `Foo` has a specialized renderer registered:
```java
GlobalEasyGridConfiguration.forType(Foo.class)
    .setRendererFactory(FooRenderers.of(...));
```
Independently, an instance configures Entity columns with a generic formatter:
```java
easyGrid.typeConfiguration(Entity.class).setFormatter(e -> e.getId().toString());
```

Under scope-first, `I·Entity` wins over `G·Foo`. The Foo-specific renderer — which encodes *what a Foo looks like*, arguably a type invariant — gets silently replaced by a generic Entity formatter that the developer may not have intended to apply to Foo.

Type-first preserves the type contract: `G·Foo` sits above `I·Entity`, so Foo's rendering semantics are honoured even across scope boundaries.

---

## Liskov Substitution Principle

LSP says: a `Foo` must be usable wherever an `Entity` is expected, without callers needing to know the difference.

Applied to configuration, it has two implications that pull in opposite directions:

**LSP supports scope-first for behavioural settings.** If you configure `Entity` at instance level — null representation, alignment, a locale-specific formatter — you are specifying how *this grid* handles all Entity values. A `Foo` substituting for `Entity` should satisfy those same observable postconditions. Refusing to inherit `I·Entity` because Foo has a `G·Foo` entry would mean the grid behaves differently for `Foo` vs `Entity` in ways the calling code did not anticipate.

**LSP supports type-first for type-defining settings.** LSP also requires that subtypes honour their own invariants. If `Foo` has a type-level rendering contract — *this is how a Foo is displayed* — then substituting a generic `Entity` renderer for it violates the Foo invariant. The renderer is not just a postcondition on the grid; it is a property of `Foo` itself.

The tension resolves in favour of **scope-first for all properties**. The type-first argument rests on the assumption that `I·Entity` displaces `G·Foo` unintentionally — but instance configuration is always deliberate. A developer who writes `forType(Entity.class).setFormatter(...)` knows `Foo IS-AN Entity`; if they wanted Foo to keep its global renderer they would have set `I·Foo` separately. The argument collapses entirely at broad overrides such as `forType(Object.class).setFormatter(...)`, where the developer has unambiguously stated that everything in this grid uses this formatter and global type-specific registrations cannot claim precedence. **Global registrations are defaults. Instance registrations are decisions. Decisions outrank defaults regardless of type specificity.**

---

## Current Implementation

The implementation uses **scope-first** for all properties. For a `Foo extends Entity` column the effective chain is:

```
per-column → I·Foo → I·Entity → I·Object → G·Foo → G·Entity → G·Object → Default
```

This is built in two pieces:

**Instance chain** — `EasyGridConfigurationClassMap.getOrCreate(Foo)` walks the Java class hierarchy within the same map, producing a `ColumnConfigurationImpl` chain:

```
I·Foo(impl) → I·Entity(impl) → I·Object(impl)
```

**Global chain** — `GlobalEasyGridConfiguration.resolve(Foo)` similarly produces:

```
G·Foo(impl) → G·Entity(impl) → G·Object(impl)
```

**Bridge** — `InstanceEasyGridConfiguration.forType(Foo)` wraps both into a `ColumnConfigurationLink`:

```
ColumnConfigurationLink(primary = I·Foo chain, fallback = G·Foo chain)
```

`ColumnConfigurationLink.get()` consults the primary chain first and only falls back to the global chain when the primary returns `null` for a given property.

**Per-column** — `InstanceEasyGridConfiguration.resolve(Foo)` wraps the link in a fresh `ColumnConfigurationImpl` whose fields are overridden by column-level setters (`setNullRepresentation`, `setFormatter`, `setRendererFactory`).

**Default** — when the entire chain returns `null` for `getRendererFactory()`, `EasyColumn.createRenderer` applies a `ColumnConfigurationTextRenderer` with null-representation support as the last resort.
