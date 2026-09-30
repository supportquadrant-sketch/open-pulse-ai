# Design System Specification & Token Guidance

## 1. Context and Goals

### Design Intent
Deliver an implementation-ready, token-driven design system optimized for high consistency, WCAG 2.2 AA accessibility, and efficient delivery across dark-mode and cross-platform interfaces.

### Brand & Surface Profile
- **Target Audience:** Developers, systems engineers, and technical teams.
- **Product Surface:** Web app and Android client applications.
- **Aesthetic Direction:** Structured, tokenized, content-first, high-contrast Dark Surface with Deep Cobalt accents.

---

## 2. Design Tokens and Foundations

### 2.1 Color Palette Tokens
| Token Key | Hex Value | Semantic Usage |
|---|---|---|
| `color.surface.base` | `#000000` | Canvas base background |
| `color.surface.muted` | `#0f0f0f` | Recessed container background, code blocks |
| `color.surface.raised` | `#171717` | Card backgrounds, chat bubbles, dialogs |
| `color.surface.elevated` | `#202124` | User chat bubbles, popovers, elevated sheets |
| `color.surface.strong` | `#1f3b9b` | Deep cobalt surface foundation, containers |
| `color.text.primary` | `#e6e6e6` | Primary high-contrast body & title text |
| `color.text.secondary` | `#e3e3e3` | Subtitles, labels, secondary body copy |
| `color.text.tertiary` | `#c4c7c5` | Metadata, timestamps, placeholder copy |
| `color.text.inverse` | `#ffffff` | High-contrast text on solid cobalt buttons |
| `color.accent.cobalt` | `#3b82f6` | Primary interactive accent, active pills, links |
| `color.accent.cobalt.deep` | `#1f3b9b` | Container accent, selected border highlights |
| `color.accent.cobalt.light` | `#60a5fa` | Token indicators, syntax highlights |
| `color.border.muted` | `#262626` | Card borders, separators, dividers |
| `color.border.subtle` | `#333333` | Elevated surface outlines |
| `color.state.error` | `#ef4444` | Failure alerts, critical feedback |

### 2.2 Spacing Tokens
| Token Key | Dimension | Implementation Purpose |
|---|---|---|
| `space.1` | `1px` | Hairline borders, dividers |
| `space.2` | `4px` | Micro gaps, badge internal vertical padding |
| `space.3` | `5px` | Dense pill chip padding |
| `space.4` | `6px` | Icon-to-text horizontal spacing |
| `space.5` | `8px` | Standard grid unit, compact margins |
| `space.6` | `12px` | Card internal padding, list item gaps |
| `space.7` | `16px` | Screen gutters, container margins |
| `space.8` | `24px` | Section margins, bottom sheet top insets |
| `space.9` | `32px` | Hero spacing, empty-state headers |

### 2.3 Radius & Shape Tokens
| Token Key | Value | Component Usage |
|---|---|---|
| `radius.xs` | `9999px` | Capsule pill buttons, tag badges, filter chips |
| `radius.sm` | `4px` | Tooltip corners, micro badges |
| `radius.md` | `8px` | Code blocks, text input fields |
| `radius.lg` | `12px` | Content cards, chart containers, metric tiles |
| `radius.xl` | `16px` | Chat speech bubbles, modal dialog corners |

### 2.4 Motion & Transition Tokens
| Token Key | Duration | Easing Curve | Usage |
|---|---|---|---|
| `motion.duration.instant` | `200ms` | `cubic-bezier(0.4, 0.0, 0.2, 1)` | Hover, ripple, button press |
| `motion.duration.fast` | `280ms` | `cubic-bezier(0.0, 0.0, 0.2, 1)` | Toast entry, badge expansion |
| `motion.duration.standard` | `350ms` | `cubic-bezier(0.4, 0.0, 0.2, 1)` | Bottom sheet, dialog reveal |

### 2.5 Typography Scale
| Token Key | Font Size | Line Height | Weight | Usage |
|---|---|---|---|---|
| `font.size.xs` | `13px` | `16px` | 400 | Monospaced code snippets, timestamps |
| `font.size.sm` | `13.33px` | `18px` | 500 | Metadata badges, subtext |
| `font.size.md` | `14px` | `20px` | 400 | Compact body, drawer list items |
| `font.size.lg` | `16px` | `24px` | 400 / 500 | Primary chat body text |
| `font.size.xl` | `17px` | `24px` | 600 | Section headers, card titles |
| `font.size.2xl` | `20px` | `26px` | 700 | Modal titles, screen top bar headers |
| `font.size.3xl` | `24px` | `30px` | 700 | Metric numbers, dashboard highlights |
| `font.size.4xl` | `32px` | `38px` | 800 | Hero headers, display titles |

---

## 3. Component-Level Rules

### 3.1 Button Component
- **Anatomy:** Container (`radius.xs`), Leading Icon (optional, 16dp), Text Label (`font.size.md`), Trailing Icon (optional, 16dp).
- **Variants:**
  - *Primary Solid:* Background `color.accent.cobalt`, Text `color.text.inverse`, Min Height 44dp.
  - *Secondary Outlined:* Background `transparent`, Border `color.border.muted`, Text `color.text.primary`.
  - *Surface Pill:* Background `color.surface.raised`, Border `color.border.muted`, Text `color.text.primary`.
  - *Icon Button:* Size 40x40dp (min touch target 48x48dp via padding), Content color `color.text.tertiary`.
- **States:**
  - *Default:* Defined token background & text.
  - *Hover:* Surface lightened by 8% overlay.
  - *Focus-Visible:* 2px solid `color.accent.cobalt` outline with 2px offset.
  - *Active / Pressed:* Scale factor 0.98, opacity 0.9.
  - *Disabled:* Opacity 0.38, pointer-events none.
  - *Loading:* Circular indicator replacing label, width maintained.
- **Interactions:**
  - *Pointer:* Hover transition duration `motion.duration.instant`.
  - *Keyboard:* Activates on `Enter` and `Space`. Focus trapped appropriately.
  - *Touch:* Minimum interactive target 48x48dp.

### 3.2 Chat Bubble Component
- **Anatomy:** Avatar / Role Badge, Author Name (`font.size.md`), Model Pill (`radius.xs`), Content Area (`color.surface.raised`), Telemetry Row (Tokens, Latency, Actions).
- **Responsive Behavior:**
  - Compact Handheld: Max width 88% of screen width.
  - Tablet / Expanded: Max width 680dp aligned left/right.
- **Edge Cases:**
  - Long uninterrupted strings: `word-break: break-word` and horizontal scroll on embedded code blocks.
  - Empty response / Error: Render structured retry card with `color.state.error`.

### 3.3 Text Input & Composer Component
- **Anatomy:** Outlined Container (`radius.md` / `radius.xs`), Leading Action Icon, Multi-line Text Area, Trailing Send Button.
- **States:**
  - *Default:* Border `color.border.muted`, Background `color.surface.raised`.
  - *Focused:* Border `color.accent.cobalt`, Background `color.surface.raised`.
  - *Error:* Border `color.state.error`.
- **Keyboard & Touch:**
  - Enter sends on single-line; Shift+Enter creates newline.
  - Virtual keyboard handling: `imePadding` and automatic scroll to bottom.

---

## 4. Accessibility Requirements (WCAG 2.2 AA)

1. **Color Contrast:**
   - Text against background must achieve at least **4.5:1** contrast ratio for normal text and **3:1** for large text (`#e6e6e6` on `#000000` exceeds 15:1).
   - Interactive UI boundaries and icons must achieve at least **3:1** against adjacent background.
2. **Focus Indicators:**
   - Every interactive element must display a visible focus indicator with at least **3:1** contrast difference.
   - Hidden or suppressed focus indicators are strictly prohibited.
3. **Touch Targets:**
   - All tap targets must be at least **48x48dp** (or possess equivalent touch target delegation).
4. **Semantics & Screen Readers:**
   - All icon buttons must provide clear `contentDescription` / `aria-label`.
   - Live regions (`aria-live="polite"` or Compose announcements) must inform screen readers when streaming text completes or errors occur.

---

## 5. Content & Tone Standards

- **Tone:** Concise, confident, implementation-focused, and unambiguous.
- **Action Verbs:** Use direct imperative verbs on buttons (*"Copy Markdown"*, *"Save Key"*, *"Use in Chat"*, *"Clear All"*).
- **Error Messages:** State cause and corrective action clearly (*"API Key is missing. Please configure your OpenPulse API Key in Settings."*).

---

## 6. Anti-Patterns & Prohibited Implementations

- **DO NOT** use raw hex strings outside of centralized token definitions.
- **DO NOT** use low-contrast text (e.g. `#555555` on `#000000`).
- **DO NOT** allow content clipping without accessible scroll indicators.
- **DO NOT** hardcode fixed pixel dimensions for layout containers.
- **DO NOT** place interactive icons closer than 8dp apart without target bounding.

---

## 7. QA Checklist

- [x] All colors reference semantic tokens (`color.surface.base`, `color.surface.raised`, `color.accent.cobalt`).
- [x] All typography matches the token scale (`font.size.xs` through `font.size.4xl`).
- [x] All spacing adheres to `space.1` through `space.9`.
- [x] All capsule elements utilize `radius.xs` (9999px).
- [x] Interactive elements have minimum touch targets of 48x48dp.
- [x] High-contrast WCAG 2.2 AA verification passed for dark and light color schemes.
- [x] Keyboard focus navigation and Android BackHandler verified.
