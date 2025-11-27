package com.kamath.taleweaver.home.sell.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.util.Strings


@Composable
fun BookDetailsSection(
    title: String,
    author: String,
    description: String,
    titleError: String?,
    authorError: String?,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
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
                Strings.Labels.BOOK_DETAILS,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            TaleWeaverTextField(
                value = title,
                onValueChange = onTitleChange,
                label = Strings.Labels.TITLE_REQUIRED,
                isError = titleError != null,
                supportingText = titleError,
                modifier = Modifier.fillMaxWidth()
            )

            TaleWeaverTextField(
                value = author,
                onValueChange = onAuthorChange,
                label = Strings.Labels.AUTHOR_REQUIRED,
                isError = authorError != null,
                supportingText = authorError,
                modifier = Modifier.fillMaxWidth()
            )

            TaleWeaverTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = Strings.Labels.DESCRIPTION,
                minLines = 3,
                maxLines = 5,
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}