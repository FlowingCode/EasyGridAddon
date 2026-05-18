import { LitElement, css, html, nothing } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import '@vaadin/button';
import '@vaadin/icon';
import '@vaadin/icons';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

export interface IconDefinition {
  /** Icon name in `iconset:name` format, e.g. `"vaadin:check"`. */
  icon?: string | null;
  /** SVG icon wrapped in a Lit template literal. */
  svg?: unknown;
  /** URL to an SVG file, or `data:image/svg+xml,...` string. */
  src?: string | null;
  /** Symbol ID inside the SVG referenced by `src`. */
  symbol?: string | null;
  /** CSS class names for an icon font glyph, e.g. `"fa-solid fa-user"`. */
  iconClass?: string | null;
  /** Hex code point for an icon font glyph, e.g. `"e001"`. */
  char?: string | null;
  /** Ligature name for icon fonts that support ligatures. */
  ligature?: string | null;
  /** Font family for the icon font. */
  fontFamily?: string | null;
  /** Icon size used to set the `viewBox`. */
  size?: number;
}

export interface ButtonDefinition {
  /** Text label rendered inside the button. */
  label?: string;
  /** Maps to the `theme` attribute on `<vaadin-button>`. */
  theme?: string;
  /** Maps to the `disabled` property on `<vaadin-button>`. */
  disabled?: boolean;
  /** Icon configuration applied to a `<vaadin-icon>` in the prefix slot. */
  icon?: IconDefinition;
}

@customElement('fc-dynamic-buttons')
export class DynamicButtons extends LitElement {
  static styles = css`
    :host {
      display: flex;
	  flex-direction: row;
	  flex-wrap: nowrap;
      gap: var(--fc-dynamic-buttons-gap, 0.25rem);
    }
  `;

  @property({ type: Array })
  buttons: ButtonDefinition[] = [];

  render() {
    return html`
      ${this.buttons.map((btn, index) => {
        const iconOnly = btn.icon && !btn.label;
        const theme = [iconOnly ? 'icon' : '', btn.theme ?? ''].join(' ').trim();
        return html`
          <vaadin-button theme="${theme}" ?disabled=${btn.disabled ?? false}
            @click=${() => this.dispatchEvent(new CustomEvent('button-click', { detail: { index }, bubbles: true, composed: true }))}>
            ${btn.icon
              ? html`<vaadin-icon
                  slot="prefix"
                  .icon=${btn.icon.icon ?? nothing}
                  .svg=${btn.icon.svg ?? nothing}
                  .src=${btn.icon.src ?? nothing}
                  .symbol=${btn.icon.symbol ?? nothing}
                  .iconClass=${btn.icon.iconClass ?? nothing}
                  .char=${btn.icon.char ?? nothing}
                  .ligature=${btn.icon.ligature ?? nothing}
                  .fontFamily=${btn.icon.fontFamily ?? nothing}
                  .size=${btn.icon.size ?? nothing}
                ></vaadin-icon>`
              : ''}
            ${btn.label ?? ''}
          </vaadin-button>
        `;
      })}
    `;
  }
}

declare global {
  interface HTMLElementTagNameMap {
    'fc-dynamic-buttons': DynamicButtons;
  }
}
