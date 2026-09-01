package com.rhub.app.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val NOMS_MOIS = listOf(
    "", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
)

@Composable
fun MesBulletinsScreen(viewModel: MesBulletinsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.charger() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Mes bulletins de paie", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("L'historique de vos versements.")
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is MesBulletinsUiState.Loading -> CircularProgressIndicator()
            is MesBulletinsUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is MesBulletinsUiState.Success -> {
                if (s.bulletins.isEmpty()) {
                    Text("Aucun bulletin pour le moment.")
                } else {
                    LazyColumn {
                        items(s.bulletins) { b ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "${NOMS_MOIS.getOrElse(b.mois) { "" }} ${b.annee}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        StatutPill(b.statut_versement)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("${b.salaire_net.toInt()} FCFA net", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "Brut ${b.salaire_brut.toInt()} FCFA · Charges ${b.charges.toInt()} FCFA",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    b.date_versement?.let {
                                        Spacer(Modifier.height(4.dp))
                                        Text("Versé le ${it.take(10)}", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatutPill(statut: String) {
    val (bg, fg) = when (statut) {
        "verse" -> Color(0xFFDFF3EA) to Color(0xFF1F6E4E)
        "echoue" -> Color(0xFFFBE4E1) to Color(0xFF9A2B2B)
        else -> Color(0xFFFBEBD1) to Color(0xFF8A5A16)
    }
    Surface(color = bg, shape = MaterialTheme.shapes.small) {
        Text(
            statut.replace("_", " "),
            color = fg,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}