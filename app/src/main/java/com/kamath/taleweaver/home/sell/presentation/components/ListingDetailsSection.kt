package com.kamath.taleweaver.home.sell.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.home.feed.domain.model.BookCondition

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListingDetailsSection(
    price: String,
    condition: BookCondition?,
    shippingOffered: Boolean,
    priceError: String?,
    conditionError: String?,
    onPriceChange: (String) -> Unit,
    onConditionSelect: (BookCondition) -> Unit,
    onShippingToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                Strings.Labels.LISTING_DETAILS,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Price
            TaleWeaverTextField(
                value = price,
                onValueChange = onPriceChange,
                label = Strings.Labels.PRICE_REQUIRED,
                placeholder = Strings.Placeholders.PRICE,
                prefix = "$",
                isError = priceError != null,
                supportingText = priceError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Condition
            Text(
                Strings.Labels.CONDITION_REQUIRED,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (conditionError != null) {
                Text(
                    conditionError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BookCondition.entries.forEach { cond ->
                    FilterChip(
                        selected = condition == cond,
                        onClick = { onConditionSelect(cond) },
                        label = { Text(cond.displayName) }
                    )
                }
            }

            // Shipping toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Strings.Labels.OFFER_SHIPPING)
                Switch(
                    checked = shippingOffered,
                    onCheckedChange = onShippingToggle
                )
            }
        }
    }
}