package com.kamath.taleweaver.home.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kamath.taleweaver.cart.presentation.CartEvent
import com.kamath.taleweaver.cart.presentation.CartViewModel
import com.kamath.taleweaver.core.navigation.AppDestination
import com.kamath.taleweaver.core.navigation.HomeTabs
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.cart.presentation.CartScreen
import com.kamath.taleweaver.home.account.presentation.AccountScreen
import com.kamath.taleweaver.home.account.presentation.UserProfileScreen
import com.kamath.taleweaver.home.account.presentation.MyListingsScreen
import com.kamath.taleweaver.home.feed.presentation.FeedScreen
import com.kamath.taleweaver.home.listingDetail.presentation.screens.ListingDetailScreen
import com.kamath.taleweaver.home.search.presentation.SearchScreen
import com.kamath.taleweaver.home.sell.presentation.SellScreen
import com.kamath.taleweaver.rating.presentation.PostCheckoutRatingScreen
import timber.log.Timber

val baseTabs = listOf(
    HomeTabs.AllTales,
    HomeTabs.SearchBooks,
    HomeTabs.CreateTale,
    HomeTabs.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    rootNavController: androidx.navigation.NavController
) {
    val tabNavController = rememberNavController()
    val cartViewModel: CartViewModel = hiltViewModel()
    val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()
    val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()

    // Dynamic tabs list based on cart state
    val tabs = remember(cartItemCount) {
        if (cartItemCount > 0) {
            baseTabs + HomeTabs.Cart
        } else {
            baseTabs
        }
    }

    // Adjust tab bar height based on number of tabs
    val tabBarHeight = if (tabs.size > 4) 64.dp else 56.dp

    val snackbarHostState = remember { SnackbarHostState() }

    // Tab bar visibility state
    var isTabBarVisible by remember { mutableStateOf(true) }

    // Always show tab bar when cart has items
    LaunchedEffect(cartItemCount) {
        if (cartItemCount > 0) {
            isTabBarVisible = true
        }
    }

    // Animated offset for tab bar
    val tabBarOffset by animateDpAsState(
        targetValue = if (isTabBarVisible) 0.dp else 150.dp,
        animationSpec = tween(durationMillis = 300),
        label = "tabBarOffset"
    )

    // Listen for cart events (snackbar notifications)
    LaunchedEffect(key1 = true) {
        cartViewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
        }
    }

    // Get system navigation bar height (for Samsung/Android devices with bottom nav buttons)
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Add base padding + system navigation bar padding
    val bottomPadding = 24.dp + navigationBarPadding

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .offset(y = tabBarOffset)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = bottomPadding)
                    .height(tabBarHeight)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clip(RoundedCornerShape(28.dp)),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                windowInsets = WindowInsets(0, 0, 0, 0),
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { screen ->
                    // Animate the cart tab when it appears
                    val isCartTab = screen == HomeTabs.Cart

                    NavigationBarItem(
                        icon = {
                            if (isCartTab && cartItemCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(text = cartItemCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        screen.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    screen.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                screen.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        alwaysShowLabel = true,
                        onClick = {
                            val isAlreadySelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                            if (isAlreadySelected && screen == HomeTabs.AllTales) {
                                // If already on Home tab and not at feed screen, pop to feed
                                if (currentDestination?.route != AppDestination.FEED_SCREEN) {
                                    tabNavController.navigate(AppDestination.FEED_SCREEN) {
                                        popUpTo(AppDestination.FEED_SCREEN) { inclusive = true }
                                    }
                                }
                            } else {
                                // Navigate to the tab
                                tabNavController.navigate(screen.route) {
                                    popUpTo(tabNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = HomeTabs.AllTales.route,
        ) {
            navigation(
                startDestination = AppDestination.FEED_SCREEN,
                route = HomeTabs.AllTales.route
            ) {
                composable(route = AppDestination.FEED_SCREEN) {
                    FeedScreen(
                        onListingClick = { listingId ->
                            Timber.d("Listing clicked: $listingId")
                            tabNavController.navigate("${AppDestination.LISTING_DETAIL_SCREEN}/$listingId")
                        }
                    )
                }
                composable(
                    route = "${AppDestination.LISTING_DETAIL_SCREEN}/{${AppDestination.ARG_LISTING_ID}}",
                    arguments = listOf(navArgument(AppDestination.ARG_LISTING_ID) {
                        type = NavType.StringType
                    })
                ) {
                    ListingDetailScreen(
                        onNavigateUp = { tabNavController.navigateUp() },
                        onAddToCart = { listing ->
                            cartViewModel.onEvent(CartEvent.AddToCart(listing))
                        },
                        onSellerClick = { sellerId ->
                            Timber.d("Seller clicked: $sellerId")
                            tabNavController.navigate("${AppDestination.USER_PROFILE_SCREEN}/$sellerId")
                        }
                    )
                }
                composable(
                    route = "${AppDestination.USER_PROFILE_SCREEN}/{${AppDestination.ARG_USER_ID}}",
                    arguments = listOf(navArgument(AppDestination.ARG_USER_ID) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString(AppDestination.ARG_USER_ID) ?: ""
                    UserProfileScreen(
                        userId = userId,
                        onNavigateUp = { tabNavController.navigateUp() },
                        onListingClick = { listingId ->
                            tabNavController.navigate("${AppDestination.LISTING_DETAIL_SCREEN}/$listingId")
                        },
                        onViewAllListingsClick = {
                            tabNavController.navigate("${AppDestination.USER_LISTINGS_SCREEN}/$userId")
                        }
                    )
                }
                composable(
                    route = "${AppDestination.USER_LISTINGS_SCREEN}/{${AppDestination.ARG_USER_ID}}",
                    arguments = listOf(navArgument(AppDestination.ARG_USER_ID) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString(AppDestination.ARG_USER_ID) ?: ""
                    // We need to get the username from the back stack or state
                    // For now, we'll use a placeholder and fetch it in the screen
                    com.kamath.taleweaver.home.account.presentation.UserListingsScreen(
                        userId = userId,
                        username = "User", // Will be loaded by the screen
                        onNavigateUp = { tabNavController.navigateUp() },
                        onListingClick = { listingId ->
                            tabNavController.navigate("${AppDestination.LISTING_DETAIL_SCREEN}/$listingId")
                        }
                    )
                }
            }
            composable(HomeTabs.SearchBooks.route) {
                SearchScreen(
                    onListingClick = { listingId ->
                        Timber.d("Search listing clicked: $listingId")
                        tabNavController.navigate("${AppDestination.LISTING_DETAIL_SCREEN}/$listingId")
                    }
                )
            }
            composable(HomeTabs.CreateTale.route) {
                SellScreen(
                    onCameraStateChanged = { isInCamera ->
                        // Hide tab bar when in camera, show when not (unless cart forces it visible)
                        if (cartItemCount == 0) {
                            isTabBarVisible = !isInCamera
                        }
                    }
                )
            }
            composable(HomeTabs.Settings.route) {
                AccountScreen(
                    navController = rootNavController,
                    onListingClick = { listingId ->
                        tabNavController.navigate("${AppDestination.LISTING_DETAIL_SCREEN}/$listingId")
                    },
                    onViewAllListingsClick = {
                        tabNavController.navigate(AppDestination.MY_LISTINGS_SCREEN)
                    },
                    onBottomNavVisibilityChange = { visible ->
                        isTabBarVisible = visible
                    }
                )
            }
            composable(HomeTabs.Cart.route) {
                // Listen for checkout events
                LaunchedEffect(key1 = true) {
                    cartViewModel.checkoutEventFlow.collect { event ->
                        when (event) {
                            is com.kamath.taleweaver.cart.presentation.CartUiEvent.CheckoutSuccess -> {
                                tabNavController.navigate(HomeTabs.AllTales.route) {
                                    popUpTo(HomeTabs.AllTales.route) { inclusive = true }
                                }
                            }
                            is com.kamath.taleweaver.cart.presentation.CartUiEvent.CheckoutError -> {
                                snackbarHostState.showSnackbar(event.message)
                            }
                        }
                    }
                }

                CartScreen(
                    onItemClick = { listingId ->
                        tabNavController.navigate("${AppDestination.LISTING_DETAIL_SCREEN}/$listingId")
                    },
                    onCheckout = {
                        cartViewModel.onEvent(CartEvent.Checkout)
                    }
                )
            }
            composable(AppDestination.MY_LISTINGS_SCREEN) {
                MyListingsScreen(
                    onNavigateUp = { tabNavController.navigateUp() },
                    onListingClick = { listingId ->
                        tabNavController.navigate("${AppDestination.LISTING_DETAIL_SCREEN}/$listingId")
                    },
                    onEditListing = { listingId ->
                        // TODO: Navigate to edit screen
                        Timber.d("Edit listing: $listingId")
                    }
                )
            }
            composable(AppDestination.POST_CHECKOUT_RATING_SCREEN) {
                val ratingViewModel: com.kamath.taleweaver.rating.presentation.RatingViewModel = hiltViewModel()

                PostCheckoutRatingScreen(
                    cartItems = cartItems,
                    onRatingSubmitted = { sellerId, rating, comment ->
                        ratingViewModel.submitRating(sellerId, rating, comment)
                        Timber.d("Rating submitted for seller $sellerId: $rating - $comment")
                    },
                    onSkipRatings = {
                        cartViewModel.onEvent(CartEvent.ClearCart)
                        tabNavController.navigate(HomeTabs.AllTales.route) {
                            popUpTo(HomeTabs.AllTales.route) { inclusive = true }
                        }
                    },
                    onFinish = {
                        cartViewModel.onEvent(CartEvent.ClearCart)
                        tabNavController.navigate(HomeTabs.AllTales.route) {
                            popUpTo(HomeTabs.AllTales.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}