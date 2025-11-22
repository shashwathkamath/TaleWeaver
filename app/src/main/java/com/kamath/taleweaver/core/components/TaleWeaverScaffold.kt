package com.kamath.taleweaver.core.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.components.TopBars.TaleWeaverAppBar

/**
 * TaleWeaverScaffold with AppBarType for flexible app bar configurations
 */
@Composable
fun TaleWeaverScaffold(
    appBarType: AppBarType,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            TaleWeaverAppBar(appBarType = appBarType)
        },
        content = content
    )
}