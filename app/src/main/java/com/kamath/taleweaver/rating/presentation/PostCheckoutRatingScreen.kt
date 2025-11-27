package com.kamath.taleweaver.rating.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.cart.domain.model.CartItem
import com.kamath.taleweaver.core.components.ButtonVariant
import com.kamath.taleweaver.core.components.TaleWeaverButton
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.rating.presentation.components.RatingBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCheckoutRatingScreen(
    cartItems: List<CartItem>,
    onRatingSubmitted: (sellerId: String, rating: Float, comment: String) -> Unit,
    onSkipRatings: () -> Unit,
    onFinish: () -> Unit
) {
    var showRatingSheet by remember { mutableStateOf(false) }
    var selectedSeller by remember { mutableStateOf<Pair<String, String>?>(null) } // (sellerId, sellerName)
    val ratingSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val ratedSellers = remember { mutableStateOf(setOf<String>()) }

    // Group items by seller
    val itemsBySeller = remember(cartItems) {
        cartItems.groupBy { it.listing.sellerId to it.listing.sellerUsername }
    }

    TaleWeaverScaffold(
        appBarType = AppBarType.Default("Order Complete")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Success message
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = Strings.Titles.ORDER_PLACED_SUCCESSFULLY,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Strings.Messages.RATE_SELLERS_HELP,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sellers list
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(itemsBySeller.toList()) { (sellerInfo, items) ->
                    val (sellerId, sellerName) = sellerInfo
                    val isRated = ratedSellers.value.contains(sellerId)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sellerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${items.size} ${if (items.size == 1) "item" else "items"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isRated) {
                                    Text(
                                        text = "✓ Rated",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    TaleWeaverButton(
                                        onClick = {
                                            selectedSeller = sellerId to sellerName
                                            showRatingSheet = true
                                        },
                                        variant = ButtonVariant.Primary
                                    ) {
                                        Text(Strings.Buttons.RATE_SELLER)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom actions
            Column {
                TaleWeaverButton(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Primary
                ) {
                    Text(
                        text = Strings.Buttons.DONE,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TaleWeaverButton(
                    onClick = onSkipRatings,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Text
                ) {
                    Text(Strings.Buttons.SKIP_FOR_NOW)
                }
            }
        }

        // Rating bottom sheet
        if (showRatingSheet && selectedSeller != null) {
            RatingBottomSheet(
                sellerName = selectedSeller!!.second,
                sheetState = ratingSheetState,
                onDismiss = {
                    scope.launch {
                        ratingSheetState.hide()
                        showRatingSheet = false
                    }
                },
                onSubmitRating = { rating, comment ->
                    scope.launch {
                        ratingSheetState.hide()
                        showRatingSheet = false
                    }
                    onRatingSubmitted(selectedSeller!!.first, rating, comment)
                    ratedSellers.value = ratedSellers.value + selectedSeller!!.first
                }
            )
        }
    }
}
