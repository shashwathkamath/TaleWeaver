package com.kamath.taleweaver.home.account.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.ComprehensiveAddressInput
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.order.domain.model.Address

@Composable
fun EditableFields(
    name: String,
    fullName: String,
    description: String,
    address: String,
    shippingAddress: Address?,
    isCurrentUser: Boolean = true,
    onNameChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onShippingAddressChange: (Address) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        Text(
            text = Strings.Labels.PROFILE_INFORMATION,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TaleWeaverTextField(
            value = name,
            onValueChange = onNameChange,
            label = Strings.Labels.DISPLAY_NAME,
            leadingIcon = Icons.Default.Person,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        TaleWeaverTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = Strings.Labels.BIO,
            leadingIcon = Icons.Default.Edit,
            placeholder = Strings.Placeholders.BIO,
            maxLines = 3,
            singleLine = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Only show shipping-related fields if this is the current user viewing their own profile
        if (isCurrentUser) {
            // Full Name field for shipping/delivery
            TaleWeaverTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = "Full Name",
                leadingIcon = Icons.Default.Person,
                placeholder = "Your full name for delivery",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Delivery Address (Private)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
            )

            Text(
                text = "Where you'll receive books you purchase. Others will only see: ${if (address.isNotBlank()) address else "City, Country"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ComprehensiveAddressInput(
                address = shippingAddress ?: Address(),
                onAddressChange = { newAddress ->
                    onShippingAddressChange(newAddress)

                    // Extract city/country for public display from structured address
                    val publicAddress = if (newAddress.city.isNotBlank() && newAddress.country.isNotBlank()) {
                        "${newAddress.city}, ${newAddress.country}"
                    } else newAddress.city.ifBlank {
                        "City, Country"
                    }
                    onAddressChange(publicAddress)
                },
                showPhoneField = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
