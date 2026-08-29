package com.rhub.app.ui.director

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.data.Conge
import com.rhub.app.data.ErrorMessages
import com.rhub.app.data.Repository
import com.rhub.app.data.Utilisateur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CongesUiState {
    object Loading : CongesUiState()
    data class Error(val message: String) : CongesUiState()
    data class Success(val conges: List<Conge>, val employes: List<Utilisateur>) : CongesUiState()
}

class CongesViewModel : ViewModel() {
    private val _state = MutableStateFlow<CongesUiState>(CongesUiState.Loading)
    val state: StateFlow<CongesUiState> = _state

    private var entrepriseId: String? = null
    private var utilisateurId: String? = null

    fun charger() {
        viewModelScope.launch {
            _state.value = CongesUiState.Loading
            try {
                val utilisateur = Repository.obtenirUtilisateurCourant()
                    ?: throw IllegalStateException("Profil introuvable.")
                val eid = utilisateur.entreprise_id ?: throw IllegalStateException("Aucune entreprise associée.")
                entrepriseId = eid
                utilisateurId = utilisateur.id

                val conges = Repository.listerConges(eid)
                    .sortedWith(compareBy({ it.statut != "en_attente" }, { it.date_debut }))
                val employes = Repository.listerEmployes(eid)

                _state.value = CongesUiState.Success(conges, employes)
            } catch (e: Exception) {
                _state.value = CongesUiState.Error(ErrorMessages.friendly(e.message))
            }
        }
    }

    fun traiter(congeId: String, valide: Boolean) {
        val uid = utilisateurId ?: return
        viewModelScope.launch {
            try {
                Repository.traiterConge(congeId, if (valide) "valide" else "refuse", uid)
            } catch (_: Exception) {
                // Échec silencieux : on recharge simplement l'état réel depuis la base
            }
            charger()
        }
    }
}