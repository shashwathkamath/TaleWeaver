package com.kamath.taleweaver.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

@Composable
fun TaleWeaverScaffold(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            BookAppBar(
                title = title,
                actions = actions
            )
        },
        content = content
    )
}