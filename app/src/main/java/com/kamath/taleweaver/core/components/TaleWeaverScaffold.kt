package com.kamath.taleweaver.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.components.TopBars.TaleWeaverAppBar

@Composable
fun TaleWeaverScaffold(
    appBarType: AppBarType,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val snackbarBottomPadding = 80.dp + navigationBarPadding + 16.dp

    Scaffold(
        snackbarHost = {
            Box(modifier = Modifier.padding(bottom = snackbarBottomPadding)) {
                snackbarHost()
            }
        },
        topBar = {
            TaleWeaverAppBar(appBarType = appBarType)
        },
        content = content
    )
}