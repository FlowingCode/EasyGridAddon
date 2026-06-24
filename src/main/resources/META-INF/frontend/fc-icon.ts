/*-
 * #%L
 * Easy Grid Add-on
 * %%
 * Copyright (C) 2020 - 2026 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import { LitElement, html, nothing } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { directive, Directive, ElementPart, PartInfo, PartType } from 'lit/directive.js';
import '@vaadin/icon';

/**
 * A directive that spreads an object's entries as attributes onto an element.
 * Usage: <my-elem ${spreadAttrs({ foo: 1, bar: 'baz' })}></my-elem>
 */
export const spreadAttrs = directive(class extends Directive {
  // Attribute names set on the previous render, so stale ones can be removed when the element is
  // reused for another row whose map omits them (Lit reuses one directive instance per binding).
  private prev = new Set<string>();

  constructor(partInfo: PartInfo) {
    super(partInfo);
    if (partInfo.type !== PartType.ELEMENT) {
      throw new Error('The `spreadAttrs` directive must be used on an element tag.');
    }
  }

  render(attrs: Record<string, any>) {
    return ''; // Nothing to render directly in the template
  }

  update(part: ElementPart, [attrs]: [Record<string, any>]) {
    const element = part.element;
    const next = new Set<string>();

    for (const [key, value] of Object.entries(attrs)) {
      if (value === undefined || value === null) {
        element.removeAttribute(key);
      } else {
        element.setAttribute(key, String(value));
        next.add(key);
      }
    }
    // Remove attributes this directive set on a previous render but no longer sets.
    for (const key of this.prev) {
      if (!next.has(key)) {
        element.removeAttribute(key);
      }
    }
    this.prev = next;
  }
});

/**
 * A directive that spreads an object's entries as properties onto an element.
 * Usage: <my-elem ${spreadProps({ foo: 1, bar: baz })}></my-elem>
 */
export const spreadProps = directive(class extends Directive {
  // Property names set on the previous render, so stale ones can be reset when the element is
  // reused for another row whose map omits them (Lit reuses one directive instance per binding).
  private prev = new Set<string>();

  constructor(partInfo: PartInfo) {
    super(partInfo);
    if (partInfo.type !== PartType.ELEMENT) {
      throw new Error('The `spreadProps` directive must be used on an element tag.');
    }
  }

  render(props: Record<string, any>) {
    return ''; // Nothing to render directly in the template
  }

  update(part: ElementPart, [props]: [Record<string, any>]) {
    const element = part.element as any;
    const next = new Set<string>();

    for (const [key, value] of Object.entries(props)) {
      element[key] = value;
      next.add(key);
    }
    // Reset properties this directive set on a previous render but no longer sets. Setting them
    // to undefined clears the leaked value (it does not restore a component-specific default).
    for (const key of this.prev) {
      if (!next.has(key)) {
        element[key] = undefined;
      }
    }
    this.prev = next;
  }
});

/**
 * Wraps `<vaadin-icon>`. The Java row-actions renderer passes the source icon's attributes and
 * properties as two catch-all maps — `attr` and `prop` — which are forwarded onto the inner
 * `<vaadin-icon>`: `attr` is spread as attributes, `prop` as properties. The component renders
 * nothing unless one of the well-known icon fields is present: `icon`/`src` among the attributes,
 * or `symbol`/`ligature`/`char`/`fontFamily`/`iconClass` among the properties.
 */
@customElement('fc-icon')
export class FcIcon extends LitElement {
  @property({ attribute: false }) attr?: Record<string, unknown> | null;
  @property({ attribute: false }) prop?: Record<string, unknown> | null;

  protected createRenderRoot() {
    return this;
  }

  render() {
    const attr: Record<string, unknown> = this.attr ?? {};
    const prop: Record<string, unknown> = this.prop ?? {};

    // The well-known icon fields tested below must stay in sync with the Java side, see
    // EasyRowAction.PRECEDENCE_ICON_NAMES. That list also includes the "size" attribute, which is
    // intentionally omitted here, since "size" alone does not identify an icon source.
    return attr.icon || attr.src ||
      prop.symbol || prop.ligature || prop.char || prop.fontFamily || prop.iconClass ?
      html`<vaadin-icon
        ${spreadAttrs(attr)}
        ${spreadProps(prop)}
      ></vaadin-icon>` : nothing;
  }
}

declare global {
  interface HTMLElementTagNameMap {
    'fc-icon': FcIcon;
  }
}
