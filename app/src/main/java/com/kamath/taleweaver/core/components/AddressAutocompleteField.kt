package com.kamath.taleweaver.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.kamath.taleweaver.BuildConfig
import com.kamath.taleweaver.core.util.Strings
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@Composable
fun AddressAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = Strings.Labels.ADDRESS,
    placeholder: String = Strings.Placeholders.ADDRESS
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showDropdown by remember { mutableStateOf(false) }
    var placesClient by remember { mutableStateOf<PlacesClient?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var userHasInteracted by remember { mutableStateOf(false) } // Track if user has typed

    // Initialize Places API
    LaunchedEffect(Unit) {
        try {
            if (!Places.isInitialized()) {
                Timber.d("Initializing Places API with key: ${BuildConfig.MAPS_API_KEY.take(10)}...")
                Places.initialize(context, BuildConfig.MAPS_API_KEY)
            }
            placesClient = Places.createClient(context)
            Timber.d("Places API initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Places API")
        }
    }

    // Fetch autocomplete predictions when user types
    LaunchedEffect(value, userHasInteracted) {
        Timber.d("LaunchedEffect triggered. Value: '$value', Length: ${value.length}, PlacesClient: ${placesClient != null}, UserInteracted: $userHasInteracted")

        // Only fetch if user has actively changed the value
        if (value.length >= 3 && placesClient != null && userHasInteracted) {
            isLoading = true
            Timber.d("Starting to fetch predictions for: '$value'")
            try {
                val token = AutocompleteSessionToken.newInstance()
                Timber.d("Created session token: $token")

                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(value)
                    .build()

                Timber.d("Sending request to Places API...")
                val response = placesClient!!.findAutocompletePredictions(request).await()
                predictions = response.autocompletePredictions
                showDropdown = predictions.isNotEmpty()
                isLoading = false
                Timber.d("SUCCESS: Found ${predictions.size} predictions")
                predictions.forEach { prediction ->
                    Timber.d("  - ${prediction.getFullText(null)}")
                }
            } catch (e: Exception) {
                Timber.e(e, "ERROR: Autocomplete prediction failed")
                Timber.e("Exception type: ${e.javaClass.simpleName}")
                Timber.e("Exception message: ${e.message}")
                predictions = emptyList()
                showDropdown = false
                isLoading = false
            }
        } else {
            if (value.length < 3) {
                Timber.d("Query too short (${value.length} chars), need at least 3")
            }
            if (placesClient == null) {
                Timber.e("PlacesClient is NULL!")
            }
            predictions = emptyList()
            showDropdown = false
            isLoading = false
        }
    }

    Box(modifier = modifier) {
        Column {
            TaleWeaverTextField(
                value = value,
                onValueChange = {
                    Timber.d("TextField value changed to: '$it'")
                    userHasInteracted = true // Mark that user has typed
                    onValueChange(it)
                },
                label = label,
                leadingIcon = Icons.Default.LocationOn,
                placeholder = placeholder,
                modifier = Modifier.fillMaxWidth()
            )

            Timber.d("UI State - isLoading: $isLoading, showDropdown: $showDropdown, predictions: ${predictions.size}")

            // Show loading indicator
            if (isLoading) {
                Timber.d("Showing loading indicator")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Searching locations...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Suggestions List
            if (showDropdown && predictions.isNotEmpty() && !isLoading) {
                Timber.d("Displaying ${predictions.size} suggestions in dropdown")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(max = 250.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 4.dp,
                    tonalElevation = 2.dp
                ) {
                    LazyColumn {
                        items(predictions) { prediction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val displayText = prediction
                                            .getFullText(null)
                                            .toString()
                                        Timber.d("Selected address: $displayText")
                                        userHasInteracted = false // Reset interaction flag
                                        onValueChange(displayText)
                                        showDropdown = false
                                        predictions = emptyList()
                                        focusManager.clearFocus() // Dismiss keyboard
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prediction.getPrimaryText(null).toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = prediction.getSecondaryText(null).toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (prediction != predictions.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }

            // Show message when user types but no results
            if (value.length >= 3 && predictions.isEmpty() && !isLoading && showDropdown) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "No locations found. Try a different search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
