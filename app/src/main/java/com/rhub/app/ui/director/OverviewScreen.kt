package com.rhub.app.ui.director

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OverviewScreen(viewModel: OverviewViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.charger() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Vue d'ensemble", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("L'état de votre entreprise en un coup d'œil.")
        Spacer(Modifier.height(20.dp))

        when (val s = state) {
            is OverviewUiState.Loading -> CircularProgressIndicator()
            is OverviewUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is OverviewUiState.Success -> {
                StatCard("Employés actifs", s.employesActifs.toString())
                Spacer(Modifier.height(12.dp))
                StatCard("Postes créés", s.postes.toString())
                Spacer(Modifier.height(12.dp))
                StatCard("Congés en attente", s.congesEnAttente.toString())
            }
            OverviewUiState.Idle -> {}
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}