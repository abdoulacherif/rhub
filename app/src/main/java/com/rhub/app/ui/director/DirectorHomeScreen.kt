package com.rhub.app.ui.director

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun DirectorHomeScreen() {
    var ongletActuel by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = ongletActuel == 0,
                    onClick = { ongletActuel = 0 },
                    icon = { Text("◆") },
                    label = { Text("Vue d'ensemble") }
                )
                NavigationBarItem(
                    selected = ongletActuel == 1,
                    onClick = { ongletActuel = 1 },
                    icon = { Text("▤") },
                    label = { Text("Postes") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (ongletActuel) {
                0 -> OverviewScreen()
                else -> PostesScreen()
            }
        }
    }
}