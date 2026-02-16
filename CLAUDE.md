# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build                    # Build the project
./gradlew assembleDebug            # Build debug APK
./gradlew assembleRelease          # Build release APK (requires local.properties)
./gradlew test                     # Run all unit tests
./gradlew test --tests "LoginViewModelTest"  # Run specific test
./gradlew connectedAndroidTest     # Run instrumented tests on device/emulator
```

## Project Overview

TaleWeaver is an Android peer-to-peer marketplace for buying and selling used books. Built with Kotlin, Jetpack Compose, Firebase, and Room.

**Key Configuration:**
- Min SDK 24, Target SDK 36, Compile SDK 36
- Kotlin 2.2.20, Java 11
- Requires `google-services.json` in `app/` for Firebase
- API keys stored in `local.properties` (MAPS_API_KEY, release signing)

## Architecture

**MVVM + Clean Architecture** with feature-based modules:

```
feature/
├── presentation/   # Composables, ViewModels, State, Events
├── domain/         # Models, Repository interfaces, Use cases
└── data/           # Repository implementations, Remote/Local data sources
```

**State Management Pattern:**
- ViewModels expose `StateFlow<ScreenState>`
- UI events via sealed interfaces: `sealed interface ScreenEvent`
- Use cases wrap repository calls: `operator fun invoke()`

**Result Wrapper:**
```kotlin
sealed class ApiResult<T> { Success, Error, Loading }
```

## Key Feature Modules

| Module | Purpose |
|--------|---------|
| `home/feed/` | Book listings feed with pagination |
| `home/search/` | Location-based search using GeoFirestore |
| `home/sell/` | Create listings with barcode scanner (ISBN lookup via Google Books API) |
| `home/account/` | User profile management |
| `cart/` | Local Room-backed shopping cart |
| `genres/` | Genre caching with WorkManager sync |
| `login/`, `registration/` | Firebase Authentication |
| `order/`, `rating/` | Order tracking and user ratings |

## Tech Stack

- **UI:** Jetpack Compose, Material 3
- **DI:** Hilt (annotate with `@HiltViewModel`, `@Inject`)
- **Backend:** Firebase (Firestore, Auth, Storage)
- **Networking:** Retrofit + OkHttp, Gson
- **Local DB:** Room (entities in domain, DAOs in data/local)
- **Location:** GeoFirestore for geospatial queries, Play Services Location
- **Camera:** CameraX + ML Kit Barcode Scanning
- **Images:** Coil
- **Background:** WorkManager with Hilt integration
- **Testing:** JUnit, Mockito Kotlin, Turbine (Flow testing), Truth

## Search Architecture

Search uses a "fetch once, filter locally" strategy:
1. GeoFirestore fetches all books within 50km (server-side geohash query)
2. Results cached in ViewModel
3. Text search, genre filter, and radius changes all filter cached data (instant, no network)
4. 300ms debounce on search input

See `SEARCH_OPTIMIZATION.md` for details.

## Dependency Injection Setup

Modules in `di/`:
- `FirebaseModule` - Firebase services (Firestore, Auth, Storage)
- `NetworkModule` - Retrofit, OkHttp, Google Books API
- `DatabaseModule` - Room database and DAOs
- `RepositoryModuleBinder` - Binds repository implementations to interfaces

## Testing Patterns

```kotlin
@ExperimentalCoroutinesApi
class SomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test description`() = runTest {
        viewModel.uiState.test {  // Turbine
            assertThat(awaitItem().isLoading).isTrue()  // Truth
        }
    }
}
```

## Navigation

Routes defined in `core/navigation/AppDestination.kt`. Navigation graph in `AppNavigation.kt`. Each feature's Composable receives a `NavController` for navigation.