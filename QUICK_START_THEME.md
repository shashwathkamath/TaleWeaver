# Quick Start: Theme Implementation

## Two Options for Theme Management

### Option 1: Simple Theme Manager (No Persistence) - RECOMMENDED FOR NOW

This is the easiest way to get started. Theme preference won't persist after app restart.

#### Step 1: The SimpleThemeManager is already provided in `ThemeModule.kt`

#### Step 2: Update Your MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var simpleThemeManager: SimpleThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkMode = simpleThemeManager.isDarkModeState()

            TaleWeaverTheme(darkTheme = isDarkMode) {
                // Your app content
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Your navigation or main screen
                }
            }
        }
    }
}
```

#### Step 3: Add Theme Toggle to Any Screen

In your ViewModel:
```kotlin
@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    private val simpleThemeManager: SimpleThemeManager,
    // ... other dependencies
) : ViewModel() {

    val isDarkMode: Boolean
        get() = simpleThemeManager.isDarkMode

    fun toggleTheme() {
        simpleThemeManager.toggleDarkMode()
    }
}
```

In your Screen:
```kotlin
@Composable
fun AccountScreen(
    viewModel: AccountScreenViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account") },
                actions = {
                    ThemeToggleIconButton(
                        isDarkMode = viewModel.isDarkMode,
                        onToggle = { viewModel.toggleTheme() }
                    )
                }
            )
        }
    ) { padding ->
        // Your content
    }
}
```

---

### Option 2: Full Theme Manager (With DataStore Persistence)

The DataStore dependency has been added to your build.gradle. After syncing Gradle, you can use the full ThemeManager.

#### After Gradle Sync:

1. **Update MainActivity**:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themePrefs = themeManager.themePreferencesState()

            TaleWeaverTheme(
                darkTheme = themePrefs.isDarkMode,
                dynamicColor = themePrefs.useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Your navigation or main screen
                }
            }
        }
    }
}
```

2. **Use in ViewModel**:

```kotlin
@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    // ... other dependencies
) : ViewModel() {

    val themePreferences = themeManager.themePreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreferences()
        )

    fun toggleTheme() {
        viewModelScope.launch {
            themeManager.toggleDarkMode()
        }
    }
}
```

3. **Use in Screen**:

```kotlin
@Composable
fun AccountScreen(
    viewModel: AccountScreenViewModel = hiltViewModel()
) {
    val themePrefs by viewModel.themePreferences.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account") },
                actions = {
                    ThemeToggleIconButton(
                        isDarkMode = themePrefs.isDarkMode,
                        onToggle = { viewModel.toggleTheme() }
                    )
                }
            )
        }
    ) { padding ->
        // Your content
    }
}
```

---

## Quick Component Usage Examples

### Using BookButton
```kotlin
BookButton(onClick = { /* action */ }) {
    Icon(Icons.Default.Add, null)
    Spacer(Modifier.width(8.dp))
    Text("Add Book")
}
```

### Using BookCard
```kotlin
BookCard(onClick = { /* navigate */ }) {
    Column(Modifier.padding(16.dp)) {
        Text(
            text = "Book Title",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "by Author",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### Using BookAppBar
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
BookAppBar(
    title = "My Books",
    navigationIcon = {
        BackNavigationIcon { navController.popBackStack() }
    },
    actions = {
        IconButton(onClick = { /* filter */ }) {
            Icon(Icons.Default.FilterList, "Filter")
        }
    }
)
```

---

## Current Setup Status

✅ **DataStore dependency added** to:
- `gradle/libs.versions.toml` (version 1.1.1)
- `app/build.gradle.kts`

✅ **Both theme managers available**:
- `SimpleThemeManager` - For immediate use (no persistence)
- `ThemeManager` - For persistent storage (use after Gradle sync)

✅ **All theme components ready**:
- Colors, Typography, Shapes
- Buttons, Cards, Tabs, AppBars
- Theme toggle components

---

## Next Steps

1. **Sync Gradle** - Click "Sync Now" in Android Studio
2. **Start with SimpleThemeManager** - Works immediately, no persistence needed
3. **Later migrate to ThemeManager** - If you need theme preference to persist

---

## Troubleshooting

### If Gradle sync fails:
- Make sure Android Studio is up to date
- Try "File > Invalidate Caches / Restart"
- Check that internet connection is available for dependency download

### If theme toggle doesn't work:
- Make sure MainActivity is annotated with `@AndroidEntryPoint`
- Verify that the manager is injected correctly
- Check that you're using the manager's state in `TaleWeaverTheme()`

### If colors don't change:
- Always use `MaterialTheme.colorScheme.*` for colors
- Never hardcode colors like `Color.Black` or `Color(0xFF...)`
- Test in both light and dark themes

---

See `THEME_IMPLEMENTATION_GUIDE.md` for more detailed examples and patterns.
