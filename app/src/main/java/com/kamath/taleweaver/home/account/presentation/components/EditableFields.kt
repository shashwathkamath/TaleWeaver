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
import com.kamath.taleweaver.core.components.AddressAutocompleteField
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.order.domain.model.Address

@Composable
fun EditableFields(
    name: String,
    description: String,
    address: String,
    shippingAddress: Address?,
    isCurrentUser: Boolean = true,
    onNameChange: (String) -> Unit,
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

        // Only show shipping address field if this is the current user viewing their own profile
        if (isCurrentUser) {
            Text(
                text = "Shipping Address (Private)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Others will only see: ${if (address.isNotBlank()) address else "City, Country"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            AddressAutocompleteField(
                value = shippingAddress?.addressLine1 ?: "",
                onValueChange = { fullAddress ->
                    // When user selects an address, save it and extract city/country for public display
                    // Google Places will give us the full address
                    // We'll parse it to get city and country
                    onShippingAddressChange(Address(addressLine1 = fullAddress))

                    // For now, extract basic city/country from the address string
                    // Format: "123 Street, City, State, Country"
                    val parts = fullAddress.split(",").map { it.trim() }
                    val publicAddress = when {
                        parts.size >= 2 -> "${parts[parts.size - 2]}, ${parts.last()}" // City, Country
                        parts.size == 1 -> parts[0]
                        else -> fullAddress
                    }
                    onAddressChange(publicAddress)
                },
                label = "Full Shipping Address",
                placeholder = "Enter your complete address",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
