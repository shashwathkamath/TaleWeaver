package com.kamath.taleweaver.home.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kamath.taleweaver.core.util.Strings

@Composable
internal fun PermissionDeniedContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(Strings.Permissions.LOCATION_RATIONALE)
        Button(onClick = onRequestPermission) {
            Text(Strings.Buttons.GRANT_PERMISSION)
        }
    }
}