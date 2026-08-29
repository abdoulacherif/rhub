package com.rhub.app.ui.director

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EmployesScreen(viewModel: EmployesViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var modalOuvert by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.charger() }

    val lienGenere by viewModel.lienGenere.collectAsState()
    LaunchedEffect(lienGenere) {
        lienGenere?.let { lien ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Rejoignez votre entreprise sur RHub : $lien")
            }
            context.startActivity(Intent.createChooser(intent, "Partager l'invitation"))
            viewModel.effacerLien()
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
            Text("Employés", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Un employé finalise lui-même son inscription via un lien d'invitation.")
            Spacer(Modifier.height(16.dp))

            when (val s = state) {
                is EmployesUiState.Loading -> CircularProgressIndicator()
                is EmployesUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                is EmployesUiState.Success -> {
                    val posteMap = s.postes.associateBy { it.id }

                    Text("Actifs (${s.employes.size})", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (s.employes.isEmpty()) {
                        Text("Aucun employé pour le moment.")
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                            items(s.employes) { e ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("${e.prenom} ${e.nom}", fontWeight = FontWeight.Bold)
                                        Text(posteMap[e.poste_id]?.nom_poste ?: "Sans poste", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Invitations en attente (${s.invitations.size})", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (s.invitations.isEmpty()) {
                        Text("Aucune invitation en attente.")
                    } else {
                        LazyColumn {
                            items(s.invitations) { inv ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(inv.email, fontWeight = FontWeight.Bold)
                                            Text(posteMap[inv.poste_id]?.nom_poste ?: "—", fontSize = 13.sp)
                                        }
                                        TextButton(onClick = { viewModel.annulerInvitation(inv.id) }) {
                                            Text("Annuler")
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
        val postesDispo = (state as? EmployesUiState.Success)?.postes ?: emptyList()
        NouvelleInvitationDialog(
            viewModel = viewModel,
            postes = postesDispo,
            onFermer = { modalOuvert = false }
        )
    }
}

@Composable
private fun NouvelleInvitationDialog(
    viewModel: EmployesViewModel,
    postes: List<com.rhub.app.data.Poste>,
    onFermer: () -> Unit
) {
    var posteId by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var salaire by remember { mutableStateOf("") }
    var menuPosteOuvert by remember { mutableStateOf(false) }
    val enCours by viewModel.invitationEnCours.collectAsState()
    val erreur by viewModel.erreurInvitation.collectAsState()

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Inviter un employé") },
        text = {
            Column {
                Box {
                    OutlinedTextField(
                        value = postes.firstOrNull { it.id == posteId }?.nom_poste ?: "Sélectionner un poste",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Poste") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableSafe { menuPosteOuvert = true }
                    )
                    DropdownMenu(expanded = menuPosteOuvert, onDismissRequest = { menuPosteOuvert = false }) {
                        postes.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.nom_poste) },
                                onClick = { posteId = p.id; menuPosteOuvert = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail de l'employé") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Téléphone (optionnel)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = salaire,
                    onValueChange = { salaire = it },
                    label = { Text("Salaire proposé (optionnel)") },
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
                enabled = !enCours && email.isNotBlank(),
                onClick = {
                    viewModel.inviter(posteId, email.trim(), telephone.ifBlank { null }, salaire.toDoubleOrNull())
                    onFermer()
                }
            ) { Text(if (enCours) "Envoi..." else "Générer l'invitation") }
        },
        dismissButton = {
            TextButton(onClick = onFermer) { Text("Annuler") }
        }
    )
}

// Petit raccourci pour rendre un champ en lecture seule cliquable (ouvre le menu déroulant)
@Composable
private fun Modifier.clickableSafe(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.pointerInputClickable(onClick)
    )
}

@Composable
private fun Modifier.pointerInputClickable(onClick: () -> Unit): Modifier {
    return androidx.compose.foundation.clickable(onClick = onClick).let { this.then(it) }
}