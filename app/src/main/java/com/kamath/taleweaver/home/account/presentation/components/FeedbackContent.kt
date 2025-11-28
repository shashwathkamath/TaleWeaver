package com.kamath.taleweaver.home.account.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.ButtonVariant
import com.kamath.taleweaver.core.components.TaleWeaverButton
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.util.Strings

@Composable
fun FeedbackContent(
    onSubmitFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var feedbackText by remember { mutableStateOf("") }
    val wordCount = feedbackText.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
    val isValid = feedbackText.isNotBlank() && wordCount <= 100

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp),  // Add bottom padding to avoid tab bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = Strings.Messages.FEEDBACK_TITLE,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = Strings.Messages.FEEDBACK_SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback TextField
        TaleWeaverTextField(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            label = "Your Feedback",
            placeholder = Strings.Placeholders.FEEDBACK,
            minLines = 6,
            maxLines = 10,
            singleLine = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Word Counter
        Text(
            text = "$wordCount / 100 words",
            style = MaterialTheme.typography.bodySmall,
            color = if (wordCount > 100) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        TaleWeaverButton(
            onClick = {
                if (isValid) {
                    onSubmitFeedback(feedbackText)
                    feedbackText = "" // Clear after submission
                }
            },
            enabled = isValid,
            variant = ButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = Strings.Buttons.SUBMIT_FEEDBACK,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Helper Text
        if (wordCount > 100) {
            Text(
                text = "Please keep your feedback within 100 words",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}
