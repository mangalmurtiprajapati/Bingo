---
name: Arcade Bingo Pulse
colors:
  surface: '#f9f9ff'
  surface-dim: '#cfdaf2'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f3ff'
  surface-container: '#e7eeff'
  surface-container-high: '#dee8ff'
  surface-container-highest: '#d8e3fb'
  on-surface: '#111c2d'
  on-surface-variant: '#3f4851'
  inverse-surface: '#263143'
  inverse-on-surface: '#ecf1ff'
  outline: '#707882'
  outline-variant: '#bfc7d2'
  surface-tint: '#00639a'
  primary: '#006096'
  on-primary: '#ffffff'
  primary-container: '#007abc'
  on-primary-container: '#fdfcff'
  inverse-primary: '#96ccff'
  secondary: '#8b5000'
  on-secondary: '#ffffff'
  secondary-container: '#ff9800'
  on-secondary-container: '#653900'
  tertiary: '#b70052'
  on-tertiary: '#ffffff'
  tertiary-container: '#dd2269'
  on-tertiary-container: '#fffbff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#cee5ff'
  primary-fixed-dim: '#96ccff'
  on-primary-fixed: '#001d32'
  on-primary-fixed-variant: '#004a75'
  secondary-fixed: '#ffdcbe'
  secondary-fixed-dim: '#ffb870'
  on-secondary-fixed: '#2c1600'
  on-secondary-fixed-variant: '#693c00'
  tertiary-fixed: '#ffd9df'
  tertiary-fixed-dim: '#ffb1c1'
  on-tertiary-fixed: '#3f0018'
  on-tertiary-fixed-variant: '#8f003f'
  background: '#f9f9ff'
  on-background: '#111c2d'
  surface-variant: '#d8e3fb'
typography:
  display-hero:
    fontFamily: Outfit
    fontSize: 36px
    fontWeight: '900'
    lineHeight: 44px
  display-hero-mobile:
    fontFamily: Outfit
    fontSize: 26px
    fontWeight: '900'
    lineHeight: 32px
  headline-lg:
    fontFamily: Outfit
    fontSize: 24px
    fontWeight: '800'
    lineHeight: 30px
  headline-md:
    fontFamily: Outfit
    fontSize: 20px
    fontWeight: '800'
    lineHeight: 26px
  title-md:
    fontFamily: Outfit
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
  grid-number:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 22px
  grid-number-compact:
    fontFamily: Space Grotesk
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 18px
  body-base:
    fontFamily: Outfit
    fontSize: 15px
    fontWeight: '500'
    lineHeight: 22px
  body-sm:
    fontFamily: Outfit
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
  label-caps:
    fontFamily: Space Grotesk
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.06em
  badge-micro:
    fontFamily: Space Grotesk
    fontSize: 9px
    fontWeight: '700'
    lineHeight: 12px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.375rem
  gutter-mobile: 0.25rem
  margin: 1rem
  margin-tablet: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system channels the kinetic thrill of family-parlor bingo fused with the instant feedback loop of contemporary arcade mobile games. It bridges the tactile nostalgia of handheld daubers, physical ball cages, and red pen scorecards with polished digital surfaces, vibrant pill counters, and celebratory particle milestones.

### Design Movement: Arcade Tactile & Luminous Minimalism
The visual treatment synthesizes modern tactile UI and bold chromatic energy:
- **Glossy Tactile Affordances:** Chunky buttons with physical bevels, deep drop-stroke depths, and spring-loaded interactive states that invite immediate single-thumb tapping.
- **Atmospheric Layering:** Soft ambient cerulean skies form background environments, contrasted against dark slate tactical focus cards (`#1E293B`) to give game boards intense clarity and presence.
- **Physical Metaphors:** Hand-drawn scarlet cross-strikes (`#D50000`), spherical caller orbs with simulated spherical specular sheen, and tournament gold trophies.
- **Emotional Resonance:** High-spirited, optimistic, universally accessible (ages 5 to senior citizens), and celebratory without visual clutter or disorienting visual noise.

## Colors

The color palette leverages high-chroma primary anchors with purposeful gamified functional roles:

### Core Palette
- **Primary (`#0288D1`):** Cerulean skyway. Anchors navigation headers, hero gradient fills, primary interactive triggers, and core branding elements.
- **Secondary (`#FF9800`):** Sunshine tangerine. Signifies intermediate action, turn urgency, alert notifications, and active timers.
- **Tertiary (`#FF4081`):** Electric magenta. Dedicated to the latest game action, highlighting the most recently cut number and dynamic opponent activity.
- **Neutral (`#1E293B`):** Tactical slate. Acts as the high-contrast foundation for 5x5 board containers, ensuring white and red-cut cells stand out under rapid scanning conditions.

### Gamified Accent Roles
- **Tournament Gold (`#FFD700`):** Reserved exclusively for completed lines, jackpot celebrations, coin balances, and win-state card frames.
- **Verification Emerald (`#00E676`):** Highlights valid verified lines during duel dispute checks and active combo sequences.
- **Red Pen Strike (`#D50000` / `#FF1744`):** The signature tactile marker for checked/crossed board numbers.

### Ball Category Tiers (Classic Mode)
- **B (1–15):** Royal Sky (`#2196F3`)
- **I (16–30):** Ruby Pink (`#E91E63`)
- **N (31–45):** Solar Amber (`#FF9800`)
- **G (46–60):** Meadow Green (`#4CAF50`)
- **O (61–75):** Velvet Amethyst (`#9C27B0`)

## Typography

Typography centers on **Outfit** for interface navigation, modal announcements, and button labels, paired with **Space Grotesk** for grid cells, ball callouts, and numerical counters.

### Typographic Principles
- **Instant Numeric Legibility:** Game numbers in the 5x5 matrix use fixed-width figures to prevent horizontal shifts during live match animations.
- **Punchy Case Rules:** Tactical badges, interactive buttons, and B-I-N-G-O progress markers are rendered in uppercase using `Space Grotesk` with `0.04em` to `0.06em` letter spacing for rapid peripheral glanceability.
- **Scalable Weights:** Body copy balances clean geometric clarity at regular weights (500), while titles and interactive callouts employ dense Black/ExtraBold weights (800–900) to stand out over colorful card backgrounds.

## Layout & Spacing

The layout is built for single-thumb mobile ergonomics with a strict horizontal constraint to preserve board scan speed.

### Grid & Clamping Model
- **Max-Width Constraint:** All game canvas elements clamp to a maximum content width of `560px`. On tablets and desktop viewports, the game container is centered with generous neutral backdrop margins.
- **5x5 Grid Proportions:** Board grid items follow a fixed `1:1` square aspect ratio. Cells use `0.375rem` (6px) gutters on desktop/tablet views and step down to `0.25rem` (4px) gutters on compact mobile viewports under 360px width.
- **Rhythm & Touch Targets:** All interactive control targets (buttons, modal dismissals, number cells) maintain a minimum hit area of `44px x 44px`. Vertical spacing utilizes an 8px modular cadence.
- **Safe Zone Accommodations:** Full bleed gradients respect mobile notch cutouts and gesture navigation bars via explicit `24px` top and `16px` bottom inset safe paddings.

## Elevation & Depth

Visual hierarchy does not rely on dark, blurry drop shadows. Instead, depth is established through sharp physical card extrusions, contrasting boundary rims, and luminous highlights:

- **Tactile Base (Level 0):** Full-bleed atmospheric linear canvas gradient (`#E1F5FE` to `#81D4FA`).
- **Tactical Grid Core (Level 1):** Solid slate surface (`#1E293B`) encased in a 2px hairline border of Tournament Gold (`#FFD700` at 60% opacity) or Cyan Sky (`#38BDF8` at 40% opacity).
- **Interactive Tiles (Level 2):** White cells elevated with a solid 2px bottom bevel edge (`#CBD5E1`) creating a physical pushable block sensation.
- **Active Overlay & Modals (Level 3):** Full-viewport backdrop blur (`backdrop-filter: blur(8px)`) with semi-translucent dark slate container (`rgba(15, 23, 42, 0.85)`), layered beneath high-gloss white popup modal cards.
- **Luminous Overlays:** Winning cells and jackpot buttons emit a 12px diffuse radial glow tinted directly to the element's primary color (`rgba(255, 215, 0, 0.45)` for gold victory; `rgba(255, 64, 129, 0.45)` for hot pink callouts).

## Shapes

The shape system adopts a friendly, tactile rounded-corner visual language that softens sharp geometric grids while ensuring cells feel like substantial physical game tiles:

- **Buttons & Pills:** Pill-shaped radius (`rounded-full` / 9999px) for turn counters, level badges, coin chips, and call-to-action buttons.
- **Standard Cards & Modals:** Standard `1rem` (16px) to `1.5rem` (24px) border radius for modal dialogs, scoreboard trays, and match summary envelopes.
- **Grid Tiles:** 8px radius on 5x5 board number cells, giving every number block an individual, rounded token aesthetic.
- **Stroke Weights:** Hairline borders run at 1.5px to 2px; tactile button bevels and winning highlight borders expand to 3px.

## Components

### Buttons
- **Primary Arcade Action:** Height `52px`, pill shape, filled with primary gradient (`#0288D1` to `#01579B`), white bold uppercase typography (`label-caps`), and a 3px darker navy bottom edge bevel (`#003c6e`). On click, translates vertically by `2px` with zero transition lag to simulate a real button press.
- **Gold Rewarded Action:** Golden gradient (`#FFD700` to `#FF9800`) with deep amber bottom edge (`#C67C00`) and dark purple text (`#0F172A`).
- **Secondary Ghost Pill:** Transparent background, 2px stroke tinted with primary skyway, uppercase label, and gentle active opacity drop to 80%.
- **Icon Triggers:** 40px circular buttons with translucent white fill (`rgba(255, 255, 255, 0.9)`), subtle 1px border (`#E2E8F0`), housing high-contrast navy icons.

### 5x5 Grid Cells
- **Default Tile:** Crisp white background (`#FFFFFF`), `1.5px` border in Sky Blue (`#38BDF8`), dark slate text (`#0F172A`) set in `Space Grotesk`.
- **Marked/Cut Tile:** Soft blush pink fill (`#FFEBEE`), subtle dark red border (`#E53935`), text dimmed to dark crimson (`#B71C1C`). Overlaid with two intersecting 3.5px round-capped scarlet lines (`#D50000`) forming a distinct handwritten cross (✕).
- **Latest Action Tile:** Hot pink fill (`#FF4081`), pure white digits, pulsing 2px outer magenta glow ring.
- **Completed Line Tile:** Tournament Gold fill (`#FFD700`), midnight slate digits (`#0F172A`), enclosed in a 2px bright gold border.

### The B-I-N-G-O Progress Bar
- Elevated horizontal card holding five equal-width letter modules (B, I, N, G, O).
- Unstruck letters appear as light cyan pills with dark navy lettering.
- Struck letters instantly illuminate with an energetic gold-to-orange gradient fill, animated strike-through dash, and scale bounce.
- When all 5 letters strike, the entire component loops a celebratory continuous gold shimmer.

### Ball Caller & Countdown Gauge
- 80px circular track gauge featuring a 4px golden countdown timer ring.
- Houses a 3D-styled spherical number ball with directional highlight gradient matching the active ball tier color (B, I, N, G, or O).
- Ball transition features an elastic entry scale (`0.7` to `1.0`) with a smooth horizontal exit queue displaying recent call history in mini 32px orbs.

### Input Fields & Verification Cards
- **Room Code Field:** Large-character centered text input (`Space Grotesk`), 16px corner radius, dark slate fill (`#0F172A`), 2px primary border, high-contrast white numerical readout, accompanied by an inline 1-tap copy/paste badge.
- **Line Verification Modal:** Dual-tab comparison card displaying Player 1 vs Opponent boards. Struck lines are highlighted using verification emerald overlays (`#00E676`) accompanied by coordinate checkmarks.