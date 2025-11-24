package com.kamath.taleweaver.cart.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kamath.taleweaver.cart.domain.model.CartItem
import com.kamath.taleweaver.cart.presentation.components.CartItemRow
import com.kamath.taleweaver.cart.presentation.components.CheckoutBottomSheet
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.util.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onItemClick: (String) -> Unit,
    onCheckout: (daysUntilDelivery: Int) -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showDeliveryDateSheet by remember { mutableStateOf(false) }
    val checkoutSheetState = rememberModalBottomSheetState()
    val deliveryDateSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    TaleWeaverScaffold(
        appBarType = AppBarType.Default("My Cart")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cartItems.isEmpty()) {
                // Empty cart state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty cart",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add books to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                // Cart with items
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Cart items
                    items(cartItems, key = { it.listing.id }) { cartItem ->
                        CartItemRow(
                            cartItem = cartItem,
                            onRemove = {
                                viewModel.onEvent(CartEvent.RemoveFromCart(cartItem.listing.id))
                            },
                            onClick = { onItemClick(cartItem.listing.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Bottom section with total and checkout
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total (${cartItems.size} ${if (cartItems.size == 1) "item" else "items"})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = Strings.Formats.price(cartItems.sumOf { it.listing.price }),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCheckoutSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text(
                                    text = "Proceed to Checkout",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Extra spacing for bottom navigation
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }

            // Checkout bottom sheet
            if (showCheckoutSheet) {
                CheckoutBottomSheet(
                    cartItems = cartItems,
                    sheetState = checkoutSheetState,
                    onDismiss = {
                        scope.launch {
                            checkoutSheetState.hide()
                            showCheckoutSheet = false
                        }
                    },
                    onConfirmCheckout = {
                        scope.launch {
                            checkoutSheetState.hide()
                            showCheckoutSheet = false
                        }
                        onCheckout(7) // Default 7 days
                    },
                    onRequestDeliveryDate = {
                        scope.launch {
                            checkoutSheetState.hide()
                            showCheckoutSheet = false
                            showDeliveryDateSheet = true
                        }
                    }
                )
            }

            // Delivery date bottom sheet
            if (showDeliveryDateSheet) {
                com.kamath.taleweaver.cart.presentation.components.DeliveryDateBottomSheet(
                    sheetState = deliveryDateSheetState,
                    onDismiss = {
                        scope.launch {
                            deliveryDateSheetState.hide()
                            showDeliveryDateSheet = false
                        }
                    },
                    onConfirm = { daysUntilDelivery ->
                        scope.launch {
                            deliveryDateSheetState.hide()
                            showDeliveryDateSheet = false
                        }
                        onCheckout(daysUntilDelivery)
                    }
                )
            }
        }
    }
}
