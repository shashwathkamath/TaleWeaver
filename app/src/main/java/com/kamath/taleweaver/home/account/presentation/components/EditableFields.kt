package com.kamath.taleweaver.home.account.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.util.Strings

@Composable
fun EditableFields(
    name: String,
    description: String,
    address: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit
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

        TaleWeaverTextField(
            value = address,
            onValueChange = onAddressChange,
            label = Strings.Labels.ADDRESS,
            leadingIcon = Icons.Default.LocationOn,
            placeholder = Strings.Placeholders.ADDRESS,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
