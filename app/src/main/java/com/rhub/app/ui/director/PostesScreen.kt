package com.rhub.app.ui.director

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
fun PostesScreen(viewModel: PostesViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var modalOuvert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.charger() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { modalOuvert = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text("Postes", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Chaque poste porte un salaire de base, hérité par les employés qui y sont rattachés.")
            Spacer(Modifier.height(16.dp))

            when (val s = state) {
                is PostesUiState.Loading -> CircularProgressIndicator()
                is PostesUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                is PostesUiState.Success -> {
                    if (s.postes.isEmpty()) {
                        Text("Aucun poste créé pour le moment.")
                    } else {
                        LazyColumn {
                            items(s.postes) { poste ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(poste.nom_poste, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Text("${poste.salaire_base.toInt()} FCFA / mois")
                                        poste.description?.let {
                                            Spacer(Modifier.height(4.dp))
                                            Text(it, fontSize = 13.sp)
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

    if (modalOuvert) {
        NouveauPosteDialog(viewModel = viewModel, onFermer = { modalOuvert = false })
    }
}

@Composable
private fun NouveauPosteDialog(viewModel: PostesViewModel, onFermer: () -> Unit) {
    var nom by remember { mutableStateOf("") }
    var salaire by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val creationEnCours by viewModel.creationEnCours.collectAsState()
    val erreur by viewModel.erreurCreation.collectAsState()

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Nouveau poste") },
        text = {
            Column {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom du poste") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = salaire,
                    onValueChange = { salaire = it },
                    label = { Text("Salaire de base (FCFA)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") },
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
                enabled = !creationEnCours && nom.isNotBlank() && salaire.toDoubleOrNull() != null,
                onClick = {
                    viewModel.creerPoste(nom.trim(), salaire.toDouble(), description.ifBlank { null })
                    onFermer()
                }
            ) { Text(if (creationEnCours) "Création..." else "Créer") }
        },
        dismissButton = {
            TextButton(onClick = onFermer) { Text("Annuler") }
        }
    )
}