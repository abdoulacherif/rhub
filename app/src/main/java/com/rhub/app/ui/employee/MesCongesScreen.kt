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

private val TYPES_CONGE = listOf("paye" to "Congé payé", "maladie" to "Maladie", "exceptionnel" to "Exceptionnel", "sans_solde" to "Sans solde")

@Composable
fun MesCongesScreen(viewModel: MesCongesViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var modalOuvert by remember { mutableStateOf(false) }
    val envoiReussi by viewModel.envoiReussi.collectAsState()

    LaunchedEffect(Unit) { viewModel.charger() }

    LaunchedEffect(envoiReussi) {
        if (envoiReussi) {
            modalOuvert = false
            viewModel.effacerConfirmation()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { modalOuvert = true }) { Text("+") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text("Mes congés", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Faites votre demande — elle sera transmise à votre directeur.")
            Spacer(Modifier.height(16.dp))

            when (val s = state) {
                is MesCongesUiState.Loading -> CircularProgressIndicator()
                is MesCongesUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                is MesCongesUiState.Success -> {
                    if (s.conges.isEmpty()) {
                        Text("Aucune demande envoyée.")
                    } else {
                        LazyColumn {
                            items(s.conges) { c ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                TYPES_CONGE.firstOrNull { it.first == c.type_conge }?.second ?: c.type_conge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            StatutPill(c.statut)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text("${c.date_debut} → ${c.date_fin}", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (modalOuvert) {
        NouvelleDemandeDialog(viewModel = viewModel, onFermer = { modalOuvert = false })
    }
}

@Composable
private fun StatutPill(statut: String) {
    val (bg, fg) = when (statut) {
        "valide" -> Color(0xFFDFF3EA) to Color(0xFF1F6E4E)
        "refuse" -> Color(0xFFFBE4E1) to Color(0xFF9A2B2B)
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

@Composable
private fun NouvelleDemandeDialog(viewModel: MesCongesViewModel, onFermer: () -> Unit) {
    var typeSelectionne by remember { mutableStateOf(TYPES_CONGE[0]) }
    var menuOuvert by remember { mutableStateOf(false) }
    var dateDebut by remember { mutableStateOf("") }
    var dateFin by remember { mutableStateOf("") }
    var motif by remember { mutableStateOf("") }
    val envoiEnCours by viewModel.envoiEnCours.collectAsState()
    val erreur by viewModel.erreurEnvoi.collectAsState()

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Nouvelle demande") },
        text = {
            Column {
                Box {
                    OutlinedTextField(
                        value = typeSelectionne.second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type de congé") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(androidx.compose.ui.Modifier)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .then(Modifier)
                    )
                }
                TextButton(onClick = { menuOuvert = true }) { Text("Changer le type") }
                DropdownMenu(expanded = menuOuvert, onDismissRequest = { menuOuvert = false }) {
                    TYPES_CONGE.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.second) },
                            onClick = { typeSelectionne = t; menuOuvert = false }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = dateDebut,
                    onValueChange = { dateDebut = it },
                    label = { Text("Date de début (AAAA-MM-JJ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = dateFin,
                    onValueChange = { dateFin = it },
                    label = { Text("Date de fin (AAAA-MM-JJ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = motif,
                    onValueChange = { motif = it },
                    label = { Text("Motif (optionnel)") },
                    modifier = Modifier.fillMaxWidth()
                )
                erreur?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !envoiEnCours && dateDebut.isNotBlank() && dateFin.isNotBlank(),
                onClick = {
                    viewModel.demander(typeSelectionne.first, dateDebut.trim(), dateFin.trim(), motif.ifBlank { null })
                }
            ) { Text(if (envoiEnCours) "Envoi..." else "Envoyer la demande") }
        },
        dismissButton = {
            TextButton(onClick = onFermer) { Text("Annuler") }
        }
    )
}