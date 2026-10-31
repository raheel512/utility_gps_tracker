# Maps migration result

Date: 2026-10-28. The app no longer calls the Google Maps SDK, Places SDK, Routes API, or Google Maps URLs.

## A. Original Google Maps dependencies

- `com.google.android.gms:play-services-maps:20.1.0`
- `com.google.maps.android:maps-compose:9.0.1`
- `com.google.android.libraries.places:places:5.3.0`
- Routes API `https://routes.googleapis.com/directions/v2:computeRoutes` with `MAPS_API_KEY`
- Google Maps URLs for Start Navigation and My Location share
- Manifest meta-data `com.google.android.geo.API_KEY`
- Device `Geocoder`, which on Play devices is often Google-backed and did not use the Maps key

## B. Free alternatives implemented

| Need | Replacement |
| --- | --- |
| Street map, gestures, markers, route line, location dot | MapLibre Native Android 13.6.1, style `https://tiles.openfreemap.org/styles/liberty` |
| Place autocomplete and place coordinates | Photon |
| Reverse geocoding | Photon `/reverse` |
| 21 nearby categories | Overpass, OpenStreetMap tags |
| Driving route, distance, duration, one via, avoid tolls and ferries | Valhalla `costing=auto` |
| Start and share | `geo:` URI |

Endpoints live in `MapServicesConfig`. Public Photon, Overpass, Valhalla, and OpenFreeMap hosts are for development and light testing, not a production quota.

## C. Files added or modified

Added: `core/map/MapServicesConfig.kt`, `AppMap.kt` and `MapCamera.kt` rewritten, `core/geo/*` (HTTP, Photon, Overpass, Valhalla request mapping), `PhotonReverseGeocoder.kt`, `ValhallaRoutesRepository.kt`, `GeoUri.kt`, unit tests for those mappers.

Screens that drew a Google map now pass pins and a route into `AppMap`: My Location, Satellite, Find Location, Nearby, Saved place detail, Route Preview, Voice results.

Removed from the manifest: Maps API key and the Google Maps package query.

## D. Google Maps dependencies removed

`play-services-maps`, `maps-compose`, and `places` are gone from Gradle. `MAPS_API_KEY` is gone from `secrets.defaults.properties`. Source no longer references `routes.googleapis.com`, `maps.google.com`, `google.com/maps`, or the Maps and Places SDK types.

Still present, and not Maps Platform billing:

- `play-services-location` (Fused Location Provider)
- `com.google.openlocationcode` (local Plus Code library)
- `WEATHER_API_KEY` placeholder. Weather is still an empty screen.

## E. Features successfully migrated

Street map, pan and zoom, current-location dot (when the screen has a fix and location permission), markers, route polyline, camera recenter, north and 3D pitch buttons, autocomplete, place selection, nearby categories, driving route with distance and duration, one via stop, toll and ferry avoidance, history polyline storage (still precision 5), saved places and history rows left in Room.

## F. Features partially migrated

- Start Navigation opens a `geo:` URI for the destination. The via stop and avoid flags are on the in-app route only. A `geo:` URI cannot carry them. If the device’s default map app is Google Maps, that app may still open. This project does not call the Maps URLs API.
- Satellite coordinate text is a label on the pin. The previous Compose bubble is not hosted inside MapLibre.
- 3D turns on camera pitch and shows fill-extrusion layers when the Liberty style includes them. That was not checked against the live style file.
- Old saved rows may still contain Google place ids. They still display from stored name and coordinates. They are not sent to Google and are not converted to OSM ids. New saves use `node/…`, `way/…`, or `relation/…`.

## G. Unavailable without extra infrastructure or a license

- Satellite, hybrid, and terrain imagery. The picker remains. Choosing one of those modes keeps the street map and shows: “Satellite, hybrid, and terrain imagery aren't available. Showing the street map.” That sentence is not in the design.
- In-app turn-by-turn, spoken guidance, walking, cycling, traffic, and alternative routes. The app did not have them.
- Google ratings, photos, phone, website, and opening hours. The app did not show them.
- A production-grade Photon, Overpass, or Valhalla service. The public hosts are not an SLA.

## H. Public API restrictions

Do not treat these as unlimited:

- OpenFreeMap Liberty tiles
- `https://photon.komoot.io`
- `https://overpass-api.de/api/interpreter`
- `https://valhalla1.openstreetmap.de/route`

Requests are debounced where they already were (autocomplete, 3 characters, 300 ms), cancelled when the caller cancels, timed out, retried up to 3 times on HTTP 429 and 5xx, and cached in memory for autocomplete and nearby. Nearby still runs when a category is opened, not on every GPS tick. The HTTP client sends `User-Agent: GpsTracker/1.0 (Android)` and does not log coordinates.

## I. How to run and test

No phone or emulator was attached. Map loading, GPS, nearby results, and route drawing were not run on a device.

On a device:

1. Open My Location. The street map loads and the MapLibre / OpenStreetMap attribution stays visible.
2. Pinch-zoom and pan.
3. Grant location. The dot or pin follows the fix.
4. Deny location. The rest of the app still opens.
5. Nearby hospital, restaurant, and school return OSM results or a clear empty or error state.
6. Find Location suggests places after 3 characters. Select one.
7. Route preview shows a road shape, distance, and duration.
8. Start opens a geo handler, or stays on the preview if none exists. History updates only after a successful hand-off.
9. Airplane mode shows the existing no-internet dialog.
10. Satellite, hybrid, and terrain show the unavailable message. The street map still pans.
11. Leave a map screen and open it again.
12. Older saved places and history rows are still listed.

## J. Self-hosted services

Change only `core/map/MapServicesConfig.kt`:

- `STYLE_URL` — MapLibre style JSON
- `PHOTON_BASE_URL` — same `/api` and `/reverse` paths
- `OVERPASS_URL` — interpreter URL
- `VALHALLA_ROUTE_URL` — `/route`

No Maps key and no billing account.

## K. Remaining Maps Platform billing

None from this app. Fused Location stays and does not use a Maps key. A `geo:` hand-off can still open Google Maps if that is the user’s default maps app.

## L. Build and test results

`assembleDebug` and `testDebugUnitTest` succeeded. MapLibre’s native library is packaged in the debug APK. Unit tests cover OSM category tags, Photon and Overpass parsing, Valhalla request shape, polyline precision 5 and 6, and the geo URI. They do not call the network.

## M. Problems fixed during the migration

The first compile still saw the deleted Routes API client because `RetrofitRoutesRepository` was left behind. Removing that class let Hilt generate again. MapLibre 13.6.1 was not in the Gradle cache, so the dependency had to be downloaded before the offline build could run.

## Feature comparison

| Feature | Before | After | Status | Limitation |
| --- | --- | --- | --- | --- |
| Street map | Google Maps SDK | MapLibre + OpenFreeMap Liberty | Migrated | Different cartography. Attribution required |
| Satellite, hybrid, terrain | Google imagery | Street map plus unavailable message | Unavailable | No legal free worldwide imagery was added |
| 3D | Google buildings + tilt | Pitch, extrusion only if the style has it | Partial | Live style contents were not verified |
| GPS fix | Fused Location | Fused Location | Unchanged | Still Play Services, not Maps billing |
| Reverse geocode | Android Geocoder | Photon | Migrated | Public demo host. Missing parts stay empty |
| Autocomplete | Places API | Photon | Migrated | OSM coverage, not Google business data |
| Nearby categories | Places Nearby | Overpass | Migrated | Uneven OSM coverage. No ratings |
| Saved places | Room + Google ids | Room kept. New ids are OSM ids | Migrated | Old Google ids are display-only |
| Driving route | Routes API | Valhalla | Migrated | No traffic. Public demo host |
| Start navigation | Google Maps URL | geo URI | Partial | No via or avoid flags in the URI |
| Turn-by-turn | Not in the app | Not in the app | Unchanged | External app only |
| History mini-map | Canvas, precision-5 polyline | Same encoding | Migrated | Valhalla precision 6 is re-encoded to 5 |
