# TaleWeaver Design System

A comprehensive book-themed design system for TaleWeaver - a peer-to-peer book selling platform.

## Overview

The TaleWeaver design system provides a consistent, warm, and approachable visual language inspired by books, libraries, and reading. It includes a complete theming system with dark/light mode support.

## Color System

### Light Theme
- **Primary**: Warm brown tones (#6B4423) - Reminiscent of book covers and leather bindings
- **Secondary**: Natural wood tones (#705942) - Evoking bookshelves and reading spaces
- **Tertiary**: Library green (#516440) - Inspired by classic library aesthetics
- **Surface**: Cream/paper white (#FFFBFF) - Like book pages

### Dark Theme
- **Primary**: Soft peach tones (#FFB68C) - Warm glow of reading lights
- **Secondary**: Muted tan (#E0C0A3) - Aged paper in low light
- **Tertiary**: Sage green (#B7CEA2) - Subdued natural tones
- **Surface**: Deep brown (#1A110E) - Nighttime reading ambiance

## Typography

The typography system uses a clear hierarchy:

- **Display**: Serif fonts for hero sections (57sp - 36sp)
- **Headlines**: Serif fonts for section headers (32sp - 24sp)
- **Titles**: Sans-serif for component headers (22sp - 14sp)
- **Body**: Sans-serif for content (16sp - 12sp)
- **Labels**: Sans-serif for UI elements (14sp - 11sp)

Usage example:
```kotlin
Text(
    text = "Book Title",
    style = MaterialTheme.typography.headlineMedium
)
```

## Shapes

Consistent rounded corners throughout:
- **Extra Small**: 4dp - Chips, badges
- **Small**: 8dp - Small buttons, cards
- **Medium**: 12dp - Cards, text fields (most common)
- **Large**: 16dp - Large cards, bottom sheets
- **Extra Large**: 24dp - Modal sheets

## Components

### Buttons

#### BookButton - Primary Action
```kotlin
BookButton(onClick = { /* action */ }) {
    Text("Add to Cart")
}
```

#### BookOutlinedButton - Secondary Action
```kotlin
BookOutlinedButton(onClick = { /* action */ }) {
    Text("View Details")
}
```

#### BookTextButton - Tertiary Action
```kotlin
BookTextButton(onClick = { /* action */ }) {
    Text("Cancel")
}
```

### Cards

#### BookCard - Standard Content
```kotlin
BookCard(onClick = { /* action */ }) {
    // Content
}
```

#### FeaturedBookCard - Featured Content
```kotlin
FeaturedBookCard(onClick = { /* action */ }) {
    // Featured content
}
```

#### BookContainer - Non-clickable Container
```kotlin
BookContainer {
    // Static content
}
```

### App Bars

#### BookAppBar - Standard Top Bar
```kotlin
BookAppBar(
    title = "My Books",
    navigationIcon = { BackNavigationIcon { navController.popBackStack() } },
    actions = { ThemeToggleIconButton(isDarkMode, onToggle) }
)
```

#### BookCenteredAppBar - Centered Title
```kotlin
BookCenteredAppBar(
    title = "TaleWeaver",
    actions = { /* actions */ }
)
```

#### BookMediumAppBar - Medium Prominence
```kotlin
BookMediumAppBar(
    title = "Browse Books",
    scrollBehavior = scrollBehavior
)
```

#### BookLargeAppBar - Large Prominent Title
```kotlin
BookLargeAppBar(
    title = "Library",
    scrollBehavior = scrollBehavior
)
```

### Tabs

#### BookTabRow with BookTab
```kotlin
BookTabRow(selectedTabIndex = selectedIndex) {
    BookTab(
        selected = selectedIndex == 0,
        onClick = { selectedIndex = 0 },
        text = "Available"
    )
    BookTab(
        selected = selectedIndex == 1,
        onClick = { selectedIndex = 1 },
        text = "Sold"
    )
}
```

## Theme Management

### Using ThemeManager

The `ThemeManager` handles theme persistence and switching:

```kotlin
@Inject
lateinit var themeManager: ThemeManager

// In your composable
val themePrefs = themeManager.themePreferencesState()

TaleWeaverTheme(
    darkTheme = themePrefs.isDarkMode,
    dynamicColor = themePrefs.useDynamicColors
) {
    // App content
}
```

### Theme Toggle Components

#### ThemeToggleIconButton - For App Bars
```kotlin
ThemeToggleIconButton(
    isDarkMode = isDarkMode,
    onToggle = { viewModel.toggleTheme() }
)
```

#### ThemeToggleSwitch - For Settings
```kotlin
ThemeToggleSwitch(
    isDarkMode = isDarkMode,
    onToggle = { viewModel.toggleTheme() }
)
```

#### CompactThemeToggle - Compact Version
```kotlin
CompactThemeToggle(
    isDarkMode = isDarkMode,
    onToggle = { viewModel.toggleTheme() }
)
```

## Implementation in ViewModels

```kotlin
@HiltViewModel
class YourViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {

    fun toggleTheme() {
        viewModelScope.launch {
            themeManager.toggleDarkMode()
        }
    }
}
```

## Best Practices

1. **Always use theme colors**: Never hardcode colors. Use `MaterialTheme.colorScheme.*`
2. **Use typography styles**: Apply appropriate text styles from `MaterialTheme.typography.*`
3. **Use shape system**: Apply shapes from `MaterialTheme.shapes.*`
4. **Use component library**: Prefer `BookButton`, `BookCard`, etc. over raw Material components
5. **Test both themes**: Always test your UI in both light and dark modes
6. **Follow hierarchy**: Use the right component for the right purpose (primary button for main actions, etc.)

## Extending the System

When creating new components:

1. Follow the naming convention: `Book*` for component names
2. Use theme colors, typography, and shapes
3. Support both light and dark themes
4. Add documentation to this file
5. Consider accessibility (touch targets, contrast ratios)

## Color Accessibility

All color combinations meet WCAG 2.1 Level AA standards for contrast:
- Text on backgrounds: Minimum 4.5:1 ratio
- Large text: Minimum 3:1 ratio
- Interactive elements: Clear visual states

## Examples

See the following screens for implementation examples:
- `AccountScreen.kt` - Profile UI with theme integration
- `FeedScreen.kt` - List UI with cards and navigation
- `ListingDetailScreen.kt` - Detail page with app bar

---

**Note**: This design system is living documentation. Update it as the system evolves.
