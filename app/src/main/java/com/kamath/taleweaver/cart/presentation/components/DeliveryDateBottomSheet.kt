package com.kamath.taleweaver.cart.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.ButtonVariant
import com.kamath.taleweaver.core.components.TaleWeaverButton
import com.kamath.taleweaver.core.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDateBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (daysUntilDelivery: Int) -> Unit
) {
    var selectedDays by remember { mutableIntStateOf(7) }

    val deliveryOptions = listOf(
        3 to "3 days",
        5 to "5 days",
        7 to "7 days",
        10 to "10 days",
        14 to "2 weeks",
        21 to "3 weeks"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = Strings.Titles.ESTIMATED_DELIVERY_TIME,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Strings.Messages.DELIVERY_QUESTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            deliveryOptions.forEach { (days, label) ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDays = days }
                        .padding(vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days }
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Strings.Messages.DELIVERY_REMINDER,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TaleWeaverButton(
                text = Strings.Buttons.CONFIRM_ORDER,
                onClick = { onConfirm(selectedDays) },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Primary
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
