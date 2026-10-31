# Maps migration plan

Status: waiting for approval. No app code has been changed.

The audit is `docs/maps-migration-audit.md`. This plan is the implementation contract. After approval, work follows the phases below and stops billing Maps Platform. It does not add turn-by-turn navigation, walking or cycling modes, ratings, or a new screen design.

## Decisions that need approval

1. **Street map.** MapLibre Native Android SDK, `MapView` hosted with `AndroidView` inside the existing `AppMap` composable. Style URL defaults to `https://tiles.openfreemap.org/styles/liberty`. No Maps Compose replacement library, because a stable MapLibre Compose artifact was not verified for this project.
2. **Satellite, hybrid, and terrain.** The picker stays. Choosing one of those modes keeps the street map and shows a short unavailable message. No unofficial Google imagery and no “live Earth”.
3. **3D control.** Becomes camera pitch on the vector map. Building extrusion is used only when the Liberty style already contains it.
4. **Search.** Photon, behind `PlacesRepository` and `ReverseGeocoder`. Default base URL is the public demo `https://photon.komoot.io`, marked in config as development-only.
5. **Nearby.** Overpass, one request when the user opens a category. Default base URL is the public instance, marked development-only. No request on map move or on each GPS fix.
6. **Routing.** Valhalla `auto`, one optional via, avoid tolls and ferries when the existing settings are on. Default base URL is a public demo, marked development-only. Shapes are converted to the current precision-5 polyline before they are saved, so history and the canvas mini-map keep working.
7. **Start Navigation.** Stops building `https://www.google.com/maps/dir/`. It hands off with a `geo:` URI (coordinates and label). If the device has no handler, the route stays on the preview screen. This app still does not speak directions.
8. **My Location share.** Stops using `https://maps.google.com/?q=`. Same `geo:` URI.
9. **GPS.** Fused Location Provider stays. Play Services Location is not a Maps Platform product.
10. **Saved data.** Room rows are not deleted or rewritten. New place ids look like `node/123`, `way/456`, or `relation/789`. Existing Google ids remain display-only and are not sent to Google.
11. **Config.** One `MapServicesConfig` (URLs, timeouts, cache TTL). No API keys. Later self-hosting means changing those URLs only.
12. **Public servers.** Code comments and the result doc state that Photon, Overpass, Valhalla, and OpenFreeMap public hosts are for development and light testing, not an unlimited production backend.

## What will not be built

- In-app turn-by-turn, rerouting, or spoken guidance. The app does not have them today.
- Walking, cycling, traffic, or alternative routes. Compute Routes is drive-only and asks for one route.
- Phone, website, opening hours, photos, or ratings. Place details today are name, address, and coordinates.
- Famous Places and Weather. Both are placeholders.
- A switch from Fused Location to `LocationManager`, unless you ask for that in a later pass.
- A dark basemap. The map is light today, and Liberty is a light style.

## Architecture

Keep the current repositories. Replace the Google implementations behind them.

| Interface already in the app | Google type removed | New type |
| --- | --- | --- |
| `AppMap` composable | `GoogleMap`, `CameraPositionState`, `Marker`, `Polyline` | MapLibre `MapView` plus a small app camera and overlay model |
| `PlacesRepository` | `PlacesSdkRepository` | `OverpassPlacesRepository` for nearby, Photon for autocomplete and details |
| `ReverseGeocoder` | `AndroidReverseGeocoder` | `PhotonReverseGeocoder` |
| `RoutesRepository` | `RetrofitRoutesRepository` calling `routes.googleapis.com` | `ValhallaRoutesRepository` |
| `LocationRepository` | unchanged | `FusedLocationRepository` stays |
| Start / share URLs | `MapsDirectionsUrl`, maps.google.com share | `GeoUri` |

`AutocompleteSession` stays in the interface so call sites do not change, and the Photon implementation ignores the token.

Screens keep their layout. They stop importing `com.google.android.gms.maps` and `com.google.maps.android.compose`. Overlay data (markers, line, user location) is passed into `AppMap` as plain latitude and longitude.

`MapServicesConfig` holds:

- `styleUrl`
- `photonBaseUrl`
- `overpassBaseUrl`
- `valhallaBaseUrl`
- connect and read timeouts
- nearby cache TTL (the memory cache already exists)

## Phases after approval

### C. MapLibre

- Add the MapLibre Android dependency. Do not add a Maps API key requirement.
- Rewrite `AppMap` and `MapCamera` so My Location, Satellite, Find Location, Nearby, Saved detail, Route Preview, and Voice compile against app types.
- Show the MapLibre attribution control. Do not cover it.
- Street style loads from `styleUrl`.
- Satellite, hybrid, and terrain selection shows the unavailable message and leaves the street style in place.
- 3D toggles pitch and resets pitch, as the buttons do today.

### D. Location on the new map

- Feed `LocationRepository` into the MapLibre location component.
- Permission denied, GPS off, and no-fix behavior stay on `LocationGate` and the existing rationale.
- Do not add a foreground service or background tracking.

### E. Places and geocoding

- Photon autocomplete with the current 300 ms debounce, 3 character minimum, cancellation, and location bias.
- Nearby Overpass query uses the category table in the audit, `around` the same radius the screen already requests, and a max result count. Nodes, ways, and relations are accepted. Ways and relations use their center when Overpass returns `out center`.
- Address is built only from tags that exist (`addr:*`). Missing fields stay null.
- New ids are OSM ids. Saved and recent rows already in Room are left as they are.
- Errors use the existing no-internet dialog and empty states. HTTP 429 and timeouts surface the existing failure path. Retry is limited and exponential, then stop.

### F. Routing

- Valhalla `costing=auto`, locations as lon/lat, one optional via location.
- `use_tolls` and `use_ferry` follow the existing settings.
- Duration and distance come from the Valhalla trip summary. They are not estimated locally.
- The shape is decoded and re-encoded with `EncodedPolyline` (precision 5) before it reaches the map, the cache, or Room.
- Start builds a `geo:` URI. History is still recorded only after a handler accepts the intent. If none does, history is not recorded and the preview stays, matching the current “failed launch” rule.

### G. Map modes

- Confirm at implementation time whether the pinned Liberty style includes fill-extrusion. If it does not, the 3D button only pitches the camera, and the result doc says so.
- Do not add a satellite tile URL.

### H. Remove Maps Platform

After C–G compile:

- Remove `play-services-maps`, `maps-compose`, and `places` from the version catalog and `app/build.gradle.kts`.
- Remove the manifest `com.google.android.geo.API_KEY` meta-data and the `com.google.android.apps.maps` package query.
- Remove `MAPS_API_KEY` from the secrets plugin defaults. Leave `WEATHER_API_KEY`.
- Delete `PlacesSdkRepository`, `PlacesClientProvider`, `RoutesApi` Google headers, and `MapsDirectionsUrl`.
- Search the source tree for `routes.googleapis.com`, `places.googleapis`, `google.android.gms.maps`, `maps.google.com`, and `google.com/maps`. None of those may remain.
- Keep `play-services-location` and `openlocationcode`.

## Tests

Update fakes and unit tests that assume Google place ids, session tokens, or the Maps URL.

Add focused tests for:

- OSM tag mapping for all 21 categories
- Photon JSON to `PlaceSuggestion` / `PlaceDetails` (no extra fields invented)
- Overpass node, way, and relation to `PlaceSummary`
- Valhalla summary to distance and duration, and shape re-encoded at precision 5
- `geo:` URI contents, including avoid flags omitted because a geo URI cannot express them (avoid flags still apply to the Valhalla request)
- Satellite / hybrid / terrain report unavailable without changing the style URL

Run `assembleDebug` and `testDebugUnitTest`. No device is assumed. Manual checks below are listed because they cannot be claimed from compilation.

## Manual checks (device required)

1. Launch and open My Location. The street map loads and attribution is visible.
2. Pinch-zoom and pan.
3. Grant location. The dot follows the fix.
4. Deny location. The rest of the app still opens.
5. Nearby hospital, restaurant, and school return OSM results or a clear empty/error state, and do not fire again on every GPS tick.
6. Find Location autocomplete after 3 characters, then select a place.
7. Route preview shows a road shape, distance, and duration.
8. Start opens a geo handler or stays on the preview if there is none. History updates only after a successful hand-off.
9. Airplane mode shows the existing no-internet dialog.
10. Satellite, hybrid, and terrain show the unavailable message. Street map still pans.
11. Leave and reopen a map screen.
12. Previously saved places and history rows are still listed.

## Self-hosting later

Change only `MapServicesConfig`:

- Tiles: any MapLibre style JSON URL whose sources you are allowed to use.
- Photon: `https://your-host` with the same `/api` and `/reverse` paths.
- Overpass: your interpreter URL.
- Valhalla: your `/route` URL.

No billing account and no Maps key.
