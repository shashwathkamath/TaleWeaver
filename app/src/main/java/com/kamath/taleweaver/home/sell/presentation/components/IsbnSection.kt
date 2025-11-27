package com.kamath.taleweaver.home.sell.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.BookPageLoadingAnimation
import com.kamath.taleweaver.core.components.ButtonVariant
import com.kamath.taleweaver.core.components.TaleWeaverButton
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.util.Strings

@Composable
fun IsbnSection(
    isbn: String,
    isbnError: String?,
    isFetching: Boolean,
    onIsbnChange: (String) -> Unit,
    onScanClick: () -> Unit,
    onFetchClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                Strings.Labels.STEP_ISBN,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Scan Button
            TaleWeaverButton(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Primary
            ) {
                Text(Strings.Buttons.SCAN_ISBN, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    Strings.Labels.OR,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            TaleWeaverTextField(
                value = isbn,
                onValueChange = onIsbnChange,
                label = Strings.Labels.ENTER_ISBN,
                placeholder = Strings.Placeholders.ISBN_EXAMPLE,
                isError = isbnError != null,
                supportingText = isbnError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            TaleWeaverButton(
                onClick = onFetchClick,
                enabled = isbn.isNotBlank() && !isFetching,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Primary
            ) {
                if (isFetching) {
                    BookPageLoadingAnimation(
                        size = 20.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(Strings.Buttons.FETCH_BOOK_DETAILS, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}