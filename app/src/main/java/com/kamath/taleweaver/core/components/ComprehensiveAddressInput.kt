package com.kamath.taleweaver.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.order.domain.model.Address

/**
 * Comprehensive address input component
 * Includes:
 * - Unit Number
 * - Address Line 1 (with autocomplete)
 * - Address Line 2
 * - Phone Number (optional)
 */
@Composable
fun ComprehensiveAddressInput(
    address: Address,
    onAddressChange: (Address) -> Unit,
    modifier: Modifier = Modifier,
    showPhoneField: Boolean = true
) {
    Column(modifier = modifier) {
        // Unit/Apartment Number
        TaleWeaverTextField(
            value = address.unitNumber,
            onValueChange = { onAddressChange(address.copy(unitNumber = it)) },
            label = "Unit/Apartment Number",
            leadingIcon = Icons.Default.Apartment,
            placeholder = "e.g., Apt 4B, Unit 205",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Address Line 1 (with autocomplete)
        Text(
            text = "Street Address",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(4.dp))
        AddressAutocompleteField(
            value = address.addressLine1,
            onValueChange = { onAddressChange(address.copy(addressLine1 = it)) },
            label = "Address Line 1",
            placeholder = "Street address, building name",
            modifier = Modifier.fillMaxWidth(),
            onAddressSelected = { structuredAddress ->
                // Merge the structured address from Google Places with existing address data
                onAddressChange(
                    address.copy(
                        addressLine1 = structuredAddress.addressLine1,
                        addressLine2 = if (structuredAddress.addressLine2.isNotBlank()) structuredAddress.addressLine2 else address.addressLine2,
                        city = structuredAddress.city,
                        state = structuredAddress.state,
                        pincode = structuredAddress.pincode,
                        country = structuredAddress.country
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Address Line 2
        TaleWeaverTextField(
            value = address.addressLine2,
            onValueChange = { onAddressChange(address.copy(addressLine2 = it)) },
            label = "Address Line 2 (Optional)",
            leadingIcon = Icons.Default.Home,
            placeholder = "Area, locality, neighborhood",
            modifier = Modifier.fillMaxWidth()
        )

        // Phone Number (conditional)
        if (showPhoneField) {
            Spacer(modifier = Modifier.height(16.dp))
            TaleWeaverTextField(
                value = address.phone,
                onValueChange = { onAddressChange(address.copy(phone = it)) },
                label = "Phone Number (Optional)",
                leadingIcon = Icons.Default.Phone,
                placeholder = "Enter phone number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
