# TaleWeaver Theme Implementation Guide

## Quick Start: Adding Dark/Light Mode Toggle

### Step 1: Add ThemeManager Dependency in Your Module

In your Hilt module (e.g., `AppModule.kt`):

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }
}
```

### Step 2: Update MainActivity

Update your `MainActivity.kt` to use the theme system:

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
                // Your app navigation
                NavHost(...)
            }
        }
    }
}
```

### Step 3: Add Theme Toggle to Settings/Account Screen

#### Option A: In AccountScreenViewModel

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

#### Option B: Add to AccountScreen UI

Update `AccountScreen.kt` to include theme toggle in the app bar:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController,
    viewModel: AccountScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themePrefs by viewModel.themePreferences.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account") },
                actions = {
                    // Add theme toggle icon
                    ThemeToggleIconButton(
                        isDarkMode = themePrefs.isDarkMode,
                        onToggle = { viewModel.toggleTheme() }
                    )

                    if (uiState.userProfile != null) {
                        TextButton(onClick = { /* save */ }) {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        // Rest of your UI
    }
}
```

#### Option C: Add to Settings Section

Create a settings section in your account screen:

```kotlin
// In AccountDetails.kt
Column {
    // ... existing profile content

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Preferences",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        ThemeToggleSwitch(
            isDarkMode = isDarkMode,
            onToggle = onThemeToggle
        )
    }
}
```

## Using Theme Components

### Example: Creating a Book Listing Card

```kotlin
@Composable
fun BookListingItem(
    book: Book,
    onClick: () -> Unit
) {
    BookCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Book cover image
            AsyncImage(
                model = book.coverUrl,
                contentDescription = book.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$${book.price}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

### Example: Creating a Screen with App Bar

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    navController: NavController,
    viewModel: BookListViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            BookAppBar(
                title = "My Books",
                navigationIcon = {
                    BackNavigationIcon { navController.popBackStack() }
                },
                actions = {
                    IconButton(onClick = { /* filter */ }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(books) { book ->
                BookListingItem(
                    book = book,
                    onClick = { navController.navigate("book/${book.id}") }
                )
            }
        }
    }
}
```

### Example: Using Tabs

```kotlin
@Composable
fun MyBooksScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Available", "Sold", "Saved")

    Column {
        BookTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                BookTab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = title
                )
            }
        }

        when (selectedTab) {
            0 -> AvailableBooksContent()
            1 -> SoldBooksContent()
            2 -> SavedBooksContent()
        }
    }
}
```

### Example: Using Buttons

```kotlin
@Composable
fun BookDetailActions(
    onAddToCart: () -> Unit,
    onViewSeller: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary action
        BookButton(
            onClick = onAddToCart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ShoppingCart, null)
            Spacer(Modifier.width(8.dp))
            Text("Add to Cart")
        }

        // Secondary action
        BookOutlinedButton(
            onClick = onViewSeller,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Person, null)
            Spacer(Modifier.width(8.dp))
            Text("View Seller")
        }

        // Tertiary action
        BookTextButton(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, null)
            Spacer(Modifier.width(8.dp))
            Text("Share")
        }
    }
}
```

## Testing Both Themes

Always test your UI in both light and dark modes:

1. Use Android Studio's theme preview
2. Test on physical devices with system dark mode
3. Toggle theme within app to verify smooth transitions

## Common Patterns

### Always Use Theme Colors

❌ **Bad:**
```kotlin
Text(
    text = "Hello",
    color = Color.Black  // Hardcoded!
)
```

✅ **Good:**
```kotlin
Text(
    text = "Hello",
    color = MaterialTheme.colorScheme.onSurface
)
```

### Always Use Typography

❌ **Bad:**
```kotlin
Text(
    text = "Title",
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold
)
```

✅ **Good:**
```kotlin
Text(
    text = "Title",
    style = MaterialTheme.typography.headlineSmall
)
```

### Always Use Shapes

❌ **Bad:**
```kotlin
Card(
    shape = RoundedCornerShape(8.dp)  // Custom shape
) { }
```

✅ **Good:**
```kotlin
Card(
    shape = MaterialTheme.shapes.medium
) { }
```

## Troubleshooting

### Theme not persisting after app restart
Make sure DataStore dependencies are added to `build.gradle`:
```gradle
implementation "androidx.datastore:datastore-preferences:1.0.0"
```

### Theme toggle not working
Ensure ThemeManager is provided as a singleton in your Hilt module.

### Colors look wrong
Check that you're using `MaterialTheme.colorScheme.*` instead of hardcoded colors.

---

For more details, see `ui/theme/README.md`
