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
import '@vaadin/icon';

export interface IconDescriptor {
  /** Icon name in `iconset:name` format, e.g. `"vaadin:check"`. */
  icon?: string | null;
  /** SVG icon wrapped in a Lit template literal. */
  svg?: unknown;
  /** URL to an SVG file, or `data:image/svg+xml,...` string. */
  src?: string | null;
  /** Symbol ID inside the SVG referenced by `src`. */
  symbol?: string | null;
  /** CSS class names for an icon font glyph. */
  iconClass?: string | null;
  /** Hex code point for an icon font glyph. */
  char?: string | null;
  /** Ligature name for icon fonts that support ligatures. */
  ligature?: string | null;
  /** Font family for the icon font. */
  fontFamily?: string | null;
  /** Icon size used to set the `viewBox`. */
  size?: number;
}

/**
 * Wraps `<vaadin-icon>` so callers can bind either individual icon fields (Constant case — values
 * known at template-build time) or a single composite `descriptor` (dynamic case — one per-row
 * binding). When neither is set the component renders nothing.
 *
 * `descriptor` fields, if present, take precedence over the corresponding individual properties.
 */
@customElement('fc-icon')
export class FcIcon extends LitElement {
  @property() icon?: string;
  @property() svg?: unknown;
  @property() src?: string;
  @property() symbol?: string;
  @property() iconClass?: string;
  @property() char?: string;
  @property() ligature?: string;
  @property() fontFamily?: string;
  @property({ type: Number }) size?: number;

  @property({ attribute: false }) descriptor?: IconDescriptor | null;

  protected createRenderRoot() {
    return this;
  }

  render() {
    const d = this.descriptor;
    const icon = d?.icon ?? this.icon;
    const svg = d?.svg ?? this.svg;
    const src = d?.src ?? this.src;
    const symbol = d?.symbol ?? this.symbol;
    const iconClass = d?.iconClass ?? this.iconClass;
    const char = d?.char ?? this.char;
    const ligature = d?.ligature ?? this.ligature;
    const fontFamily = d?.fontFamily ?? this.fontFamily;
    const size = d?.size ?? this.size;

    if (!icon && !svg && !src && !symbol && !iconClass && !char && !ligature && !fontFamily) {
      return nothing;
    }

    return html`<vaadin-icon
      .icon=${icon ?? nothing}
      .svg=${svg ?? nothing}
      .src=${src ?? nothing}
      .symbol=${symbol ?? nothing}
      .iconClass=${iconClass ?? nothing}
      .char=${char ?? nothing}
      .ligature=${ligature ?? nothing}
      .fontFamily=${fontFamily ?? nothing}
      .size=${size ?? nothing}
    ></vaadin-icon>`;
  }
}

declare global {
  interface HTMLElementTagNameMap {
    'fc-icon': FcIcon;
  }
}
