package com.rhub.app.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EmployeeOverviewScreen(viewModel: EmployeeOverviewViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.charger() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        when (val s = state) {
            is EmployeeOverviewUiState.Loading -> CircularProgressIndicator()
            is EmployeeOverviewUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is EmployeeOverviewUiState.Success -> {
                Text("Bonjour, ${s.utilisateur.prenom}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Voici votre espace de travail.")
                Spacer(Modifier.height(20.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Mon poste", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(s.poste?.nom_poste ?: "Aucun poste attribué", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        val salaire = s.utilisateur.salaire_reel ?: s.poste?.salaire_base
                        salaire?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("${it.toInt()} FCFA / mois", fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Aujourd'hui", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        val texte = when {
                            s.presenceDuJour?.heure_depart != null -> "Journée terminée"
                            s.presenceDuJour?.heure_arrivee != null -> "Présent"
                            else -> "Non pointé"
                        }
                        Text(texte, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(14.dp))

                s.poste?.description?.let {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Description du poste", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Text(it, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}