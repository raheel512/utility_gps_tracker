# Maps migration audit

Audit date: 2026-10-28. No mapping code has been changed. Status for every row is **Not started**.

## Stack (measured from the project)

| Item | What is in the repo |
| --- | --- |
| Language | Kotlin only. No Java, Flutter, or React Native sources. |
| UI | Jetpack Compose, Material 3. Single `MainActivity`. |
| Build | AGP 9.4.1, Kotlin 2.4.20, KSP 2.3.12, Gradle 9.6.0. minSdk 26, compileSdk/targetSdk 37, Java 17. |
| Architecture | MVVM, one `app` module, Hilt, Navigation Compose, StateFlow. |
| Network | Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization. Used for Routes API only. |
| Storage | Room 3.0.3, schema version 5 (`recent_searches`, `saved_places`, `collections`, `route_history`). DataStore for settings. |
| Location | Play Services Fused Location Provider 21.4.0. GNSS satellite count uses Android `LocationManager`. Location on/off uses `LocationManager`. |
| Reverse geocode | `android.location.Geocoder` (`AndroidReverseGeocoder`). On a device with Google Play this backend is often Google's, but the app does not send `MAPS_API_KEY` to it. |
| Maps render | Maps SDK 20.1.0 + Maps Compose 9.0.1, behind `AppMap`. |
| Places | Places SDK 5.3.0 (Places API New): autocomplete, place details, nearby search. |
| Routes | HTTPS `POST https://routes.googleapis.com/directions/v2:computeRoutes` with `MAPS_API_KEY`. |
| Navigation | Not in-app. Start opens a Google Maps URL (`https://www.google.com/maps/dir/?api=1`). |
| Secrets | `secrets-gradle-plugin`. `MAPS_API_KEY` and `WEATHER_API_KEY` in `secrets.defaults.properties` (placeholders). Real keys belong in `local.properties`. |
| Backend / Firebase | None. No Firebase Auth, Messaging, Analytics, or app server. |
| Camera / speech | CameraX and Android `SpeechRecognizer`. Unrelated to Maps billing. |
| Plus codes | `com.google.openlocationcode` 1.0.4. Local library, not a Maps Platform request. |
| Git | This workspace is not a git repository, so a migration branch cannot be created until git is initialized. |

## Google Maps Platform calls that can bill

1. Map tiles, satellite, hybrid, terrain, and 3D buildings through `GoogleMap` whenever `MAPS_API_KEY` is a real key. Without a key, `AppMap` draws an empty surface and does not call Maps.
2. Places Autocomplete (New), Place Details (ID, location, formatted address), and Nearby Search (ID, display name, location, formatted address, primary type).
3. Routes API Compute Routes. Drive only, traffic unaware, one optional via, no alternatives. Field mask: `routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline`.
4. Google Maps URLs for Start Navigation, and `https://maps.google.com/?q=lat,lng` for My Location share. These open Google Maps. They are Maps Platform URL products, not the tile or Routes SDK.

Not present, so nothing to migrate: Navigation SDK, Street View, Distance Matrix, Roads API, Geolocation API, Geocoding API (the REST one), map polygons, map circles, walking or cycling routes, route alternatives, spoken guidance, background location, a foreground service, live location sharing.

## What stays even if Maps Platform is removed

- Fused Location Provider. It does not use a Maps API key and is not Maps Platform billing. Documented separately below. Replacing it with `LocationManager` is optional and not required to stop Maps billing.
- `LocationManager` GNSS status (camera satellite count) and location-enabled checks.
- Room saved places, collections, recent searches, and route history. Rows are kept. Google Place IDs already stored are not converted to OSM ids.
- Open Location Code library.
- Camera, speech, settings, onboarding, paywall placeholder, Famous Places placeholder, Weather placeholder.
- `WEATHER_API_KEY`. Weather is still a placeholder screen and is not a Maps product.
- History mini-map. It is a Compose `Canvas`, not a Google map.

## Feature table

| Existing feature | Current implementation | Google dependency | Affected files | Free replacement | Migration complexity | Expected limitations | Migration status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Street map | `AppMap` wraps Maps Compose `GoogleMap`, `MapType.NORMAL` | Maps SDK + Maps Compose. Needs `MAPS_API_KEY` | `core/map/AppMap.kt`, `MapsApiKey.kt`, `MapCamera.kt`, `AndroidManifest.xml` (`com.google.android.geo.API_KEY`), `app/build.gradle.kts`, `gradle/libs.versions.toml` | MapLibre Native Android `MapView` inside `AndroidView`, style `https://tiles.openfreemap.org/styles/liberty` | High. Seven screens import Maps Compose camera, marker, and polyline types | Style looks different from Google. Must keep OSM and OpenFreeMap attribution. OpenFreeMap is a public tile service, not a private SLA | Not started |
| Current-location dot | `MapProperties.isMyLocationEnabled` when location permission is granted | Maps SDK my-location layer | `AppMap.kt`, `MyLocationScreen.kt`, `SatelliteScreen.kt` | MapLibre location component, fed by the existing `LocationRepository` | Medium | Dot style will not match Google's blue dot exactly | Not started |
| Markers | Maps Compose `Marker` on Find Location, Nearby, Saved detail, Route Preview, Voice results | Maps SDK | `FindLocationScreen.kt`, `NearbyResultsScreen.kt`, `SavedPlaceDetailScreen.kt`, `RoutePreviewScreen.kt`, `VoiceScreen.kt` | MapLibre symbol annotations inside `AppMap` | Medium | Info windows are not used today, so none are migrated | Not started |
| Route polyline | Maps Compose `Polyline` from decoded encoded polyline | Maps SDK | `RoutePreviewScreen.kt`, `VoiceScreen.kt`, `EncodedPolyline.kt` | MapLibre line layer. Keep precision-5 storage by re-encoding the new engine's shape | Medium | Old history rows stay precision 5. New routes must be stored in that same encoding | Not started |
| Camera, zoom, padding, compass | `CameraPositionState`, `CameraUpdateFactory`, `MapUiSettings` | Maps Compose | `MapCamera.kt` and the five map screens above, plus My Location and Satellite | App-owned camera state implemented by MapLibre | High | Gesture feel differs slightly. Zoom controls stay off, matching today | Not started |
| My Location screen | Satellite by default, toggle to street, share link, copy, reverse geocode | Maps SDK + Maps URL + device Geocoder | `feature/map/mylocation/*` | MapLibre street style. Share link becomes a `geo:` URI. Reverse geocode moves to Photon | Medium | Satellite toggle cannot stay as real imagery. See satellite row | Not started |
| Satellite / hybrid / terrain | `AppMapType` maps to `MapType.SATELLITE`, `HYBRID`, `TERRAIN`. Picker on Satellite screen. My Location toggles satellite vs normal | Maps SDK imagery | `AppMap.kt`, `SatelliteScreen.kt`, `SatelliteViewModel.kt`, `MyLocationViewModel.kt` | No verified free worldwide imagery whose license allows this mobile app in production | Low to wire an honest unavailable state. Not a visual replacement | Those modes stay in the picker and show an unavailable message on the working street map. No scraped Google imagery | Not started |
| 3D buildings | `buildingsEnabled` plus camera tilt on the Satellite screen | Maps SDK buildings | `SatelliteScreen.kt`, `SatelliteViewModel.kt`, `MapCamera.kt` | MapLibre camera pitch. Extrusion only if the Liberty style already has building height | Low | Do not claim Google-like 3D buildings. Pitch still works | Not started |
| GPS fix, accuracy, permission, GPS-off | `FusedLocationRepository`, `LocationGate`, permission rationale | Play Services Location, not Maps Platform | `core/location/*`, `LocationModule.kt` | Keep Fused Location | None if kept | Still depends on Play Services for the best fix. `LocationManager` remains a later option | Not started. Recommended: keep |
| Continuous tracking / background | Not implemented. Location is collected while a screen is active | None | — | Do not add | — | — | Out of scope |
| Compass / GNSS count | Accelerometer and magnetometer; `LocationManager` GNSS callback for the camera stamp | None for Maps | `core/sensors/*`, GPS Camera | Keep | None | — | No change |
| Reverse geocode | `AndroidReverseGeocoder` for origin labels, My Location address, camera stamp | Device `Geocoder`. Often Google-backed on Play devices, no API key in this app | `AndroidReverseGeocoder.kt`, callers in My Location, Find Location, Route Preview, Voice, GPS Camera | Photon reverse (`/reverse`), behind the existing `ReverseGeocoder` interface. Base URL configurable | Medium | Public `photon.komoot.io` is a demo, not production. Missing addresses stay null. No invented address parts | Not started |
| Place autocomplete | Places SDK `findAutocompletePredictions`, 3 character minimum, 300 ms debounce, location bias, session token | Places API (New), billed per session | `PlacesSdkRepository.kt`, `PlacesClientProvider.kt`, `AutocompleteSessionManager.kt`, Find Location, Voice | Photon search (`/api`). Session token becomes a no-op. Same debounce and minimum length | Medium | No Google-quality ranking. Photon public demo is not production. Results are OSM places, not businesses Google has and OSM lacks | Not started |
| Place details | `fetchPlace` fields ID, location, formatted address. Name comes from the suggestion text | Places API (New) | `PlacesSdkRepository.kt`, `PlacesModels.kt` | Photon feature already contains coordinates and address. Details call can use that payload. No second Google call | Low | Phone, website, hours, photos, and ratings are not shown today and will not be invented | Not started |
| Nearby categories (21) | `searchNearby` by Places Table A type, distance rank, radius from the caller, memory cache | Places API (New) Nearby | `NearbyCategory.kt`, `PlacesSdkRepository.kt`, `NearbyResultsViewModel.kt`, `NearbyResultsScreen.kt` | Overpass around the same center and radius. OSM tags mapped per category. Query only when the user opens a category | Medium | OSM coverage is uneven. Public Overpass is not production. No ratings. Ways and relations need a representative point | Not started |
| Saved places | Room. Identity is Google `placeId` or rounded lat/lng | Stored Google ids only. Map preview uses Maps SDK | `SavedPlaceEntity`, saved screens, `SavedPlaceDetailScreen.kt` | Keep rows. New saves store an OSM id (`node/1`, `way/2`, `relation/3`). Map preview uses MapLibre. Old Google ids are not looked up and are not converted | Medium | A place saved under a Google id will not match a new OSM id. Coordinate dedupe still works when `placeId` is null | Not started |
| Recent searches | Room, deduped by place id, last 10 | Stored ids | recent search entity and Find Location | Same as saved places. Keep existing rows | Low | Old rows still show their saved text | Not started |
| Route preview | One Compute Routes call, polyline, duration, distance, via, avoid tolls and ferries | Routes API | `RoutesApi.kt`, `RoutesModule.kt`, `RoutesRequestFactory.kt`, `RetrofitRoutesRepository.kt`, `RoutePreview*` | Valhalla `route` costing `auto`. Configurable base URL. Re-encode shape to the existing precision-5 polyline before save | Medium | Public Valhalla (for example the OSM-hosted demo) is not production. No live traffic. No alternative routes, matching today | Not started |
| Start Navigation | Launches Google Maps URL with `dir_action=navigate`. History is written only after the external app actually opens | Maps URLs. The installed Maps app then does guidance | `MapsDirectionsUrl.kt`, `RoutePreviewRoute.kt`, `VoiceRoute.kt` | Hand off with a `geo:` URI built from coordinates, or an OSM directions page. Still not in-app turn-by-turn | Medium | The user's default map app might still be Google Maps. That is their device, not a Maps Platform request from this app. No spoken guidance is added, because the app does not have it | Not started |
| Voice search results map | Same `AppMap` plus Places autocomplete and Routes | Maps SDK + Places + Routes + Maps URL | `feature/voice/ui/*` | Same three replacements. Speech code stays | Medium | Same place and route limits | Not started |
| History list mini-map | Canvas polyline, hidden when `hasRouteStats` is false | None, if the stored polyline stays precision 5 | `MiniRouteCanvas.kt`, history screens | Keep the canvas. Valhalla shapes are re-encoded before insert | Low | Blank stats rows stay blank | Not started |
| Famous Places | Placeholder screen | None | `FamousPlacesScreen.kt` | Leave it | None | — | No change |
| Weather | Placeholder. `WEATHER_API_KEY` is unused | None | `WeatherScreen.kt`, secrets files | Leave the key and the screen | None | — | No change |
| Dark map style | Comment in `AppMap`: map stays light | Maps SDK has no dark style wired | `AppMap.kt` | Liberty is a light street style. No new dark style unless the chosen style provides one | Low | Dark app theme still shows a light basemap, as today | Not started |

## Nearby category to OSM tag (for the later implementation)

| App category | Places type today | OSM tags |
| --- | --- | --- |
| Hospital | hospital | `amenity=hospital` |
| Hotels | lodging | `tourism=hotel` |
| Restaurant | restaurant | `amenity=restaurant` |
| Airport | airport | `aeroway=aerodrome` |
| Bank | bank | `amenity=bank` |
| Atm | atm | `amenity=atm` |
| Post office | post_office | `amenity=post_office` |
| School | school | `amenity=school` |
| University | university | `amenity=university` |
| Bus station | bus_station | `amenity=bus_station` |
| Police | police | `amenity=police` |
| Doctor | doctor | `amenity=doctors` |
| Theater | movie_theater | `amenity=cinema` |
| Shopping | shopping_mall | `shop=mall` |
| Petrol pump | gas_station | `amenity=fuel` |
| Cafe | cafe | `amenity=cafe` |
| Mosques | mosque | `amenity=place_of_worship` and `religion=muslim` |
| Church | church | `amenity=place_of_worship` and `religion=christian` |
| Gym | gym | `leisure=fitness_centre` |
| Spa | spa | `leisure=spa` |
| Garage | car_repair | `shop=car_repair` |

## Fused Location, kept on purpose

`play-services-location` stays. It is already working, it does not take `MAPS_API_KEY`, and removing it does not remove Maps Platform billing. A later swap to `LocationManager` GPS and network providers is possible without touching the map renderer, because screens already use `LocationRepository`.
