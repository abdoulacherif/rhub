package com.rhub.app.ui.employee

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
fun EmployeeHomeScreen() {
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
                    icon = { Text("◷") },
                    label = { Text("Présence") }
                )
                NavigationBarItem(
                    selected = ongletActuel == 2,
                    onClick = { ongletActuel = 2 },
                    icon = { Text("▦") },
                    label = { Text("Congés") }
                )
                NavigationBarItem(
                    selected = ongletActuel == 3,
                    onClick = { ongletActuel = 3 },
                    icon = { Text("◈") },
                    label = { Text("Paie") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (ongletActuel) {
                0 -> EmployeeOverviewScreen()
                1 -> PresenceScreen()
                2 -> MesCongesScreen()
                else -> MesBulletinsScreen()
            }
        }
    }
}