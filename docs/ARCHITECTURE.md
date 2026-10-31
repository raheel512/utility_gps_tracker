# GPS Tracker — Architecture

Foundation structure for the GPS Tracker Android app. Feature logic (location, camera, maps, billing, ads, APIs) is intentionally not implemented yet.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Single Activity (`MainActivity`) + Hilt
- Navigation Compose with kotlinx.serialization type-safe routes
- Room 3 (DB v1 with temporary `PlaceholderEntity` — Room requires ≥1 entity), DataStore Preferences
- Coroutines / StateFlow (MVVM, UDF)
- Timber, Secrets Gradle Plugin
- minSdk 26, compileSdk/targetSdk 37

## Package layout (single `app` module)

Package root: `com.example.gpstracker`

```
core/
  designsystem/   theme tokens + shared Compose components
  navigation/     Route, AppNavHost, AppBottomBar, AppNavViewModel
  common/         UiState, DispatcherProvider
  data/           SettingsRepository, DataStore, Room AppDatabase, DI
  location/       LocationRepository (interface only)
  permissions/    placeholder package

feature/
  onboarding/     Splash, Consent, Language, Onboarding
  home/           Home layout (fake data) + ViewModel
  saved/ history/ settings/ paywall/
  map/            MyLocation, SatelliteMap, RouteFinder
  camera/         GpsCamera
  places/         NearbyPlaces, FamousPlaces
  compass/ weather/ country/ emergency/
```

Each feature uses `ui/` (Screen, ViewModel, UiState). Add `data/` or `domain/` only when real logic needs them.

## Navigation

- AndroidX SplashScreen stays until DataStore `onboardingCompleted` is read (no wrong-screen flash)
- Start after splash:
  - `false`: Consent → Language → Onboarding → Home (first-run cleared from back stack)
  - `true`: Home
- `AppConfig.SHOW_PAYWALL_AFTER_ONBOARDING` gates optional Paywall before Home (default false)
- Bottom bar (Home, Saved, History, Settings) only on those four tab routes
- Tool screens are pushed from Home and show a back affordance

## Conventions

### MVVM + UDF
- Screen Composables observe `StateFlow<UiState>` via `collectAsStateWithLifecycle`
- UI sends `Event`s into the ViewModel; ViewModel never holds `NavController`
- One-shot effects (navigation) use `EventBus` + `ObserveAsEvents` (Channel / lifecycle-aware collect)
- Stateless `*Content` composables stay previewable without Hilt

### Threading
- Inject `DispatcherProvider` (main / io / default)
- DataStore / disk / future network: `withContext(io)` or `flowOn(io)`
- CPU prep (mapping, fake snapshots): `withContext(default)`
- Never block the main thread in repositories or ViewModels

### Other
- No hardcoded user-facing strings (use `strings.xml`)
- No hardcoded colors/spacing (use design-system tokens)
- Icons need content descriptions; touch targets ≥ 48dp
- Do not add permissions beyond INTERNET / ACCESS_NETWORK_STATE without approval
- Camera, GPS, compass declared as `uses-feature required="false"`
- Tool stubs (map/camera/…) get ViewModels when feature logic is added

## Design tokens

`docs/design.md` and `design/screens` were not available at foundation time. Theme tokens under `core/designsystem` are **placeholders** (dark scheme labeled “derived, needs design review”). Replace when design docs land.

## Secrets

- Real keys: `local.properties` (gitignored)
- Defaults for CI/local: `secrets.defaults.properties` (committed placeholders)
