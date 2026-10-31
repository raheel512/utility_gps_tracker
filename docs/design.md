# GPS Tracker: Design Tokens and Component Specs



Source: Figma file "GPS Tracker App". Values are in dp/sp (Figma px at 393 px frame width = dp).



**Confidence key**

- **[Figma]** read directly from the Home frame and its variables. Use as is.

- **[≈]** estimated from screenshots of other screens. Kept as defaults; verify in Figma before relying on them (`// VERIFY against Figma` in theme code).



The attached screenshots remain the source of truth for layout and copy. This file supplies exact values for what a screenshot cannot show.



---



## 1. Colors



### Brand / primary

| Token | Hex | Notes |

|---|---|---|

| primary500 | `#2563EB` | Main brand blue: links, active nav label, CTA text [Figma] |

| primary0 | `#DBEAFE` | Light tint: My Location icon bg, hero subtitle [Figma] |

| primary700 | `#1D4ED8` | Hero gradient end [Figma] |

| sky600 | `#0284C7` | Hero gradient start [Figma] |



### Text

| Token | Hex | Notes |

|---|---|---|

| textPrimary | `#131B2E` | Used on all Home text [Figma] |

| textSecondary | `#434655` | Inactive nav labels [Figma] |

| textMuted | `#64748B` | Section labels (GPS TOOLS, NEARBY PLACES) [Figma] |

| textOnPrimary | `#FFFFFF` | |



### Surfaces and outlines

| Token | Hex | Notes |

|---|---|---|

| surface | `#FFFFFF` | Cards, tiles [Figma] |

| screenTint | `#F4F5FF` → `#FFFFFF` | [≈] Screen background is white with soft blurred lavender ellipses at top and bottom. Approximate with a vertical gradient |

| navBar | `#FAF8FF` at 90% alpha | Bottom bar, with 12dp backdrop blur [Figma] |

| navIndicator | `#E2E7FF` | Active tab pill [Figma] |

| outlineDefault | `#E6E6E6` | App bar bottom border [Figma] |

| cardBorder | `#F1F5F9` | GPS tool cards and Nearby card [Figma] |

| tileBorder | `#E5E7EB` | "Other feature" tiles [Figma] |



### Feature icon containers (pale tints)

| Use | Background | Border |

|---|---|---|

| Voice Navigation | `#D8F5D9` | none |

| My Location | `#DBEAFE` | none |

| Satellite View | `#EEF2FF` | none |

| GPS Camera | `#ECFEFF` | none |

| Hotels | `#FFF1F2` | `#FFE4E6` |

| Cafes | `#FFFBEB` | `#FEF3C7` |

| Banks | `#EFF6FF` | `#DBEAFE` |

| Hospitals | `#FEF2F2` | `#FEE2E2` |

| Compass | `#EEF2FF` | `#E0E7FF` |

| Famous Places / Country Info | `#F0F9FF` | `#E0F2FE` |

| Country Code | `#EEF2FF` | none |

| Weather | `#FFFBEB` | `#FEF3C7` |

| Emergency | `#FFF1F2` | none |



### Connectivity chip

| State | Background | Border & text | Meaning |

|---|---|---|---|

| Connected | `#ECFDF5` | `#047857` | Network available |

| Offline | `#FEF2F2` | `#B91C1C` | No network |



### Other

| Token | Hex | Notes |

|---|---|---|

| pinStart gradient | `#E11D48` → `#EF4444` at 45° | Start pin [Figma] |

| pinDestination gradient | `#DC2626` → `#F43F5E` at 45° | Destination pin [Figma] |

| countryBadge | bg `#2563EB`, 1dp white border, white text | "+1" badge [Figma] |



### Colors seen on other screens, not yet verified [≈]

Defaults in theme code are marked `// VERIFY against Figma`.

- Success (Free/"Included"/"Low Traffic" chips): green text ≈ `#15803D` on ≈ `#DCFCE7`

- PRO chip: indigo text ≈ `#4F46E5` on ≈ `#EEF2FF`

- Destructive (End navigation button): dark red ≈ `#B91C1C`

- Map maneuver accent (right-turn box): dark green ≈ `#047857`

- Toggle on-state: primary blue



### Gradients

- **Hero card:** `#0284C7` (0%) → `#2563EB` (50%) → `#1D4ED8` (100%) at 160° (mostly top to bottom with a slight left-to-right tilt) [Figma]. Unchanged in dark theme.



### Dark theme (derived, review on device)

Follows the system setting via the existing theme preference (Auto, Light, Dark). Hero gradient and pins are unchanged. Icon-container tints = light tint color at 16% alpha over surface.

| Token | Hex / value |

|---|---|

| background | `#0B1220` |

| surface | `#131B2E` |

| onSurface | `#E6EAF5` |

| onSurfaceVariant | `#A9B1C7` |

| outline | `#2A3550` |

| cardBorder | `#1E2740` |

| primary | `#7AA2FF` |

| onPrimary | `#0B1220` |

| primaryContainer | `#1E3A8A` |

| onPrimaryContainer | `#DBEAFE` |

| navBar | `#131B2E` at 90% |

| navIndicator | `#1E2A4A` |



### Material 3 mapping suggestion

`primary = primary500`, `onPrimary = white`, `primaryContainer = primary0`, `onPrimaryContainer = primary700`, `background/surface = white`, `onSurface = textPrimary`, `onSurfaceVariant = textSecondary`, `outline = outlineDefault`, `outlineVariant = cardBorder`. Put everything else (container tints, gradients, connectivity chip) in a custom `AppColors` object via CompositionLocal.



---



## 2. Typography



Two families: **Roboto** (UI text) and **Plus Jakarta Sans** (hero card and section headers). Both are on Google Fonts. The design also uses Roboto Flex for the nav bar labels: **use Roboto instead.**



| Role | Family / weight | Size / line height | Letter spacing | Color |

|---|---|---|---|---|

| App bar title ("GPS Tracker") | Roboto Bold | 20 / 28 | -0.5 | textPrimary [Figma] |

| Hero title ("Route Finder") | Plus Jakarta Sans Bold | 20 / 25 | -0.5 | white [Figma] |

| Hero subtitle | Plus Jakarta Sans Medium | 11.5 / 14.4 | 0 | primary0, 90% opacity [Figma] |

| Hero CTA ("Find Route") | Plus Jakarta Sans Bold | 12 / 16 | 0 | primary500 [Figma] |

| Section label (uppercase) | Plus Jakarta Sans Bold | 12 / 16 | +0.6 | textMuted [Figma] |

| Section link ("See More") | Plus Jakarta Sans SemiBold | 12 / 16 | 0 | primary500 [Figma] |

| Tool card label (two lines) | Roboto Medium | 14 / 16 | 0 | textPrimary [Figma] (token: Heading/H4) |

| Tile / category label | Roboto SemiBold | 12 / 16 | 0 | textPrimary [Figma] (token: Body/B3) |

| Chip text | Roboto Regular | 12 / 16 | 0 | connected/offline content color [Figma] |

| Badge ("+1") | Roboto Black | 9 / 13.5 | 0 | white [Figma] |

| Nav label (active) | Roboto SemiBold | 11 / 14 | +0.33 | primary500 [Figma] |

| Nav label (inactive) | Roboto Medium | 11 / 14 | +0.33 | textSecondary [Figma] |



Screens other than Home use the same families. Titles are larger and bolder (≈ 20sp bold); check each when building it [≈].



---



## 3. Spacing and shape



| Token | Value | Notes |

|---|---|---|

| App bar horizontal padding | 20dp | [Figma] |

| Content side margin | 22dp | Cards are 350dp wide on a 393dp screen [Figma] |

| Card inner padding | 13dp (+1dp border) | [Figma] |

| Hero card padding | 16dp | [Figma] |

| Grid gap | 12dp | GPS tools 2x2 grid, rows 70dp tall [Figma] |

| Section label to content | 8dp | [Figma] |

| Nearby category items gap | 8dp | [Figma] |

| "Other feature" tile gap | 8dp | Tiles 114 x 89dp [Figma] |

| Bottom bar content height | 64dp + 16dp gesture area | [Figma] |



| Shape | Radius |

|---|---|

| Cards, tiles, hero | 16dp |

| Icon containers | 12dp |

| Chips, buttons, circular icons, nav indicator | full pill |

| Bottom bar | top corners 16dp |



Minimum touch target: 48dp (some nav items are 44dp in the design; pad them to 48dp).



---



## 4. Elevation (shadows)



Compose cannot reproduce CSS blur and spread exactly. Use `Modifier.shadow` with the elevation and tint below, or a custom `drawBehind`.



| Element | Figma shadow | Compose approximation |

|---|---|---|

| Tool cards, Nearby card | `0 4 16 -2 rgba(15,23,42,.06)` + `0 2 6 -1 rgba(15,23,42,.03)` | 4dp, shadow color `#0F172A` |

| "Other feature" tiles | `0 1 1.5 rgba(0,0,0,.08)` + `0 1 1 rgba(0,0,0,.04)` | 2dp |

| Hero card | `0 12 28 -4 rgba(37,99,235,.28)` | 12dp, shadow color `#2563EB` |

| Hero CTA button | `0 4 6 -1 rgba(0,0,0,.1)` | 4dp |

| Bottom bar | `0 -4 20 rgba(15,23,42,.05)` | 8dp |



---



## 5. Home screen component specs [Figma]



**Top app bar:** title left, connectivity chip right. White-ish with 12dp backdrop blur and a 1dp bottom border (outlineDefault). Padding 20dp horizontal, 14dp top, 15dp bottom. The chip is a pill: Connected uses connectedChipBg with 0.5dp `#047857` border, a 13dp wifi icon and 12sp text; Offline uses offlineChipBg with `#B91C1C` border/text. No greeting and no profile avatar.



**Hero ("Route Finder") card:** 350 wide, radius 16, padding 16, gradient above. Left column: title, subtitle, then a white pill CTA (padding 14 x 6, 14dp chevron). Right: a 112 x 96 winding-road illustration with two pins (start 28dp, destination 32dp, each with a white 2dp ring and a white center dot). A blurred white 10% circle (176dp) sits behind the top-right corner.



**GPS Tools:** 2x2 grid of cards. Each: icon container 44dp (radius 12), 12dp gap, two-line label in Roboto Medium 14/16. Order: Voice Navigation, My Location, Satellite View, GPS Camera.



**Nearby Places:** one card with four equal columns. Each: 54dp circle (28dp icon plus 13dp padding, 1dp border), 8dp gap, label. Order: Hotels, Cafes, Banks, Hospitals. "See More" link top right.



**Other Feature:** 3 x 2 tiles, 114 x 89dp. Each: 40–44dp icon container (radius 12), 8dp gap, one-line label. Order: Compass, Famous Places, Country Code (flag emoji plus "+1" badge at bottom-right), Country Info, Weather, Emergency.



**Bottom navigation:** four items (Home, Saved, History, Settings), each min 64 x 44dp. The active item has a 48 x 32dp pill (navIndicator) behind a 20dp icon, with a 2dp gap above an 11sp label. Below the row, a 96 x 4dp pill (`#131B2E` at 20%) sits in the gesture area.



---



## 6. Icons



- Feature icons are multi-color artwork (from icons8). **Never tint them.** Export each as SVG from Figma and add as drawable resources (`ic_feature_voice_navigation`, `ic_feature_my_location`, etc.). Render with `Image`/`painterResource`, not `Icon`.

- Nav icons are simple 20dp line icons (send/navigation, bookmark, history, settings). Material Icons equivalents are fine, tinted with the active/inactive label color.

- Several Figma layer names are template leftovers and do not match the real labels. Use the real labels from the screenshots: "Satellite View", "Famous Places", "Country Info", "Weather".



---



## 7. Resolved decisions



1. **textPrimary** is `#131B2E` everywhere. The old `#0C111B` variable reference is removed.

2. **Primary ramp:** keep `primary0` `#DBEAFE`, `primary500` `#2563EB`, `primary700` `#1D4ED8`. Remove `primary100` and `primary200`.

3. **Connectivity chip:** Connected = network available. Connected colors: bg `#ECFDF5`, border and text `#047857`. Offline colors: bg `#FEF2F2`, border and text `#B91C1C`.

4. **Tool card labels** all use Roboto Medium 14/16.

5. **No "Good morning" greeting** and **no profile avatar** in the app bar or Home.

6. **Dark theme:** derived `ColorScheme` and `AppColors` (mark "derived, review on device") as listed in section 1. Hero gradient and pins unchanged. Icon-container tints in dark = light tint at 16% alpha over surface. Follows system via the existing theme preference (Auto, Light, Dark).

7. Any color marked **[≈]** is kept as a default, commented `// VERIFY against Figma` in theme code. Other screens still need per-screen checks when built.
