package com.rhub.app.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.data.ErrorMessages
import com.rhub.app.data.Presence
import com.rhub.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PresenceUiState {
    object Loading : PresenceUiState()
    data class Error(val message: String) : PresenceUiState()
    data class Success(val presenceDuJour: Presence?, val historique: List<Presence>) : PresenceUiState()
}

class PresenceViewModel : ViewModel() {
    private val _state = MutableStateFlow<PresenceUiState>(PresenceUiState.Loading)
    val state: StateFlow<PresenceUiState> = _state

    private var entrepriseId: String? = null
    private var utilisateurId: String? = null

    private val _actionEnCours = MutableStateFlow(false)
    val actionEnCours: StateFlow<Boolean> = _actionEnCours

    fun charger() {
        viewModelScope.launch {
            _state.value = PresenceUiState.Loading
            try {
                val utilisateur = Repository.obtenirUtilisateurCourant()
                    ?: throw IllegalStateException("Profil introuvable.")
                entrepriseId = utilisateur.entreprise_id
                utilisateurId = utilisateur.id

                val presenceDuJour = Repository.obtenirPresenceDuJour(utilisateur.id)
                val historique = Repository.listerPresences(utilisateur.id)

                _state.value = PresenceUiState.Success(presenceDuJour, historique)
            } catch (e: Exception) {
                _state.value = PresenceUiState.Error(ErrorMessages.friendly(e.message))
            }
        }
    }

    fun pointer() {
        val eid = entrepriseId ?: return
        val uid = utilisateurId ?: return
        val s = _state.value as? PresenceUiState.Success ?: return

        viewModelScope.launch {
            _actionEnCours.value = true
            try {
                if (s.presenceDuJour?.heure_arrivee == null) {
                    Repository.pointerArrivee(eid, uid)
                } else if (s.presenceDuJour.heure_depart == null) {
                    Repository.pointerDepart(uid)
                }
            } catch (_: Exception) {
                // Échec silencieux : on recharge simplement l'état réel depuis la base
            }
            charger()
            _actionEnCours.value = false
        }
    }
}