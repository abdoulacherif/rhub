package com.rhub.app.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PresenceScreen(viewModel: PresenceViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val actionEnCours by viewModel.actionEnCours.collectAsState()

    LaunchedEffect(Unit) { viewModel.charger() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Présence", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Pointez votre arrivée et votre départ chaque jour.")
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is PresenceUiState.Loading -> CircularProgressIndicator()
            is PresenceUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is PresenceUiState.Success -> {
                val (libelle, boutonTexte, desactive) = when {
                    s.presenceDuJour?.heure_depart != null -> Triple("Journée terminée", "Déjà pointé", true)
                    s.presenceDuJour?.heure_arrivee != null -> Triple("Présent depuis l'arrivée", "Pointer le départ", false)
                    else -> Triple("Vous n'avez pas pointé aujourd'hui", "Pointer l'arrivée", false)
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(libelle, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.pointer() },
                            enabled = !desactive && !actionEnCours,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (actionEnCours) "..." else boutonTexte)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Historique", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                if (s.historique.isEmpty()) {
                    Text("Aucun pointage enregistré.")
                } else {
                    LazyColumn {
                        items(s.historique) { p ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(p.date, fontSize = 13.sp)
                                    Text(
                                        "${p.heure_arrivee?.let { "arrivée" } ?: "—"}${if (p.heure_depart != null) " · départ" else ""}",
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}