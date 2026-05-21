# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build                    # Build the project
./gradlew assembleDebug            # Build debug APK
./gradlew assembleRelease          # Build release APK (requires local.properties)
./gradlew test                     # Run all unit tests
./gradlew test --tests "LoginViewModelTest"  # Run specific test class
./gradlew connectedAndroidTest     # Run instrumented tests on device/emulator
```

## Project Overview

TaleWeaver is an Android peer-to-peer marketplace for buying and selling used books. Built with Kotlin, Jetpack Compose, Firebase, and Room.

**Key Configuration:**
- Package: `com.kamath.taleweaver`
- Min SDK 24, Target SDK 36, Compile SDK 36
- Kotlin 2.2.20, Java 11
- Requires `google-services.json` in `app/` for Firebase
- API keys in `local.properties`: `MAPS_API_KEY`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`

## Architecture

**MVVM + Clean Architecture** with feature-based packages:

```
feature/
├── presentation/   # Composables, ViewModels, State, Events
├── domain/         # Models, Repository interfaces, Use cases
└── data/           # Repository implementations, Remote/Local data sources
```

**State Management Pattern:**
- ViewModels expose `StateFlow<ScreenState>` for UI state
- UI input via sealed `UiEvent` classes (e.g., `LoginUiEvent`)
- Navigation out of a screen via `SharedFlow<NavigationEvent>` — collected in the Composable with `LaunchedEffect`
- One-off UI side effects (snackbars, focus clear) via `UiEvent.ShowSnackbar` / `UiEvent.ClearFocus`

**Result Wrapper** (`core/util/ApiResult.kt`):
```kotlin
sealed class ApiResult<T> { Success, Error, Loading }
```

## Navigation

Two-level navigation hierarchy:

1. **Root** (`AppNavigation.kt`): `NavHost` with `rootNavController` — manages Splash → Auth flow → Home
2. **Inner** (`HomeScreen.kt`): nested `NavHost` with `tabNavController` — manages the five bottom-tab destinations and detail screens

Routes are string constants in `core/navigation/AppDestination.kt`. Bottom tabs are defined as sealed objects in `core/navigation/HomeTabs.kt`.

Auth navigation (login ↔ registration) is wired via `NavGraphBuilder.authNavGraph()` extension in `NavigationGraphBuilderExtension.kt`.

## Key Feature Modules

| Module | Purpose |
|--------|---------|
| `home/feed/` | Book listings feed with cursor-based pagination |
| `home/search/` | Location-based search using GeoFirestore |
| `home/sell/` | Create listings with barcode scanner (ISBN → Google Books API) |
| `home/listingDetail/` | Detail view for a single listing |
| `home/account/` | User profile, my listings, edit listing |
| `cart/` | Local Room-backed shopping cart |
| `genres/` | Genre caching with WorkManager sync |
| `login/`, `registration/` | Firebase Authentication |
| `order/` | Order creation, shipping label PDF generation |
| `rating/` | Post-checkout seller/buyer ratings |
| `splash/` | Auth state check; navigates to Home or Auth flow |

## Core Domain Model

`Listing` (`home/feed/domain/model/Listing.kt`) is the central entity. Key fields:
- `l: GeoPoint?` — GeoFirestore location field (field name `l` is required by the GeoFirestore library)
- `status: ListingStatus` — `AVAILABLE`, `SOLD`, `RESERVED`
- `@get:Exclude val distanceKm` — computed client-side, never stored in Firestore

## Tech Stack

- **UI:** Jetpack Compose, Material 3
- **DI:** Hilt (annotate with `@HiltViewModel`, `@Inject`)
- **Backend:** Firebase (Firestore, Auth, Storage)
- **Networking:** Retrofit + OkHttp + Gson (Google Books API only)
- **Local DB:** Room — `TaleWeaverDatabase` holds `GenreEntity` and `CartEntity`; uses `fallbackToDestructiveMigration()`
- **Location:** GeoFirestore for geospatial queries, Play Services Location
- **Camera:** CameraX + ML Kit Barcode Scanning
- **Images:** Coil
- **Background:** WorkManager with Hilt integration
- **Logging:** Timber
- **Testing:** JUnit, Mockito Kotlin, Turbine (Flow testing), Truth

## Dependency Injection Setup

Most modules live in `di/` and install into `SingletonComponent`:
- `FirebaseModule` — Firestore, Auth, Storage singletons
- `NetworkModule` — Retrofit, GoogleBooksApi, BookCacheRepository
- `DatabaseModule` — Room database, GenreDao, CartDao
- `ThemeModule` — `SimpleThemeManager` (in-memory dark mode state)

`RepositoryModuleBinder` installs into `ViewModelComponent` (`@ViewModelScoped`) — each ViewModel gets its own repository instance. Repository bindings for features without their own `di/` subdirectory live here.

**Feature-local DI modules** (not in the top-level `di/`):
- `order/di/OrderModule` — `OrderRepository` singleton
- `rating/di/RatingModule` — `RatingRepository` singleton

## Search Architecture

Search uses a "fetch once, filter locally" strategy:
1. GeoFirestore fetches all books within **50 km** (server-side geohash query) — one Firestore round-trip
2. Results and computed `distanceKm` cached in `SearchViewModel` as `allNearbyBooks`
3. Text search, genre filter, and radius changes all filter `allNearbyBooks` client-side (no network)
4. 300 ms debounce on search input via a cancellable `Job`

See `SEARCH_OPTIMIZATION.md` for performance analysis and future options (Algolia, pagination).

## Testing Patterns

```kotlin
@ExperimentalCoroutinesApi
class SomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `test description`() = runTest {
        viewModel.uiState.test {       // Turbine
            assertThat(awaitItem().isLoading).isTrue()  // Truth
        }
    }
}
```

Existing tests are under `app/src/test/` — only `LoginViewModelTest` and `RegistrationViewModelTest` exist currently.
