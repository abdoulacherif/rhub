package com.rhub.app.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.data.Conge
import com.rhub.app.data.ErrorMessages
import com.rhub.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MesCongesUiState {
    object Loading : MesCongesUiState()
    data class Error(val message: String) : MesCongesUiState()
    data class Success(val conges: List<Conge>) : MesCongesUiState()
}

class MesCongesViewModel : ViewModel() {
    private val _state = MutableStateFlow<MesCongesUiState>(MesCongesUiState.Loading)
    val state: StateFlow<MesCongesUiState> = _state

    private var entrepriseId: String? = null
    private var utilisateurId: String? = null

    private val _envoiEnCours = MutableStateFlow(false)
    val envoiEnCours: StateFlow<Boolean> = _envoiEnCours

    private val _erreurEnvoi = MutableStateFlow<String?>(null)
    val erreurEnvoi: StateFlow<String?> = _erreurEnvoi

    private val _envoiReussi = MutableStateFlow(false)
    val envoiReussi: StateFlow<Boolean> = _envoiReussi

    fun charger() {
        viewModelScope.launch {
            _state.value = MesCongesUiState.Loading
            try {
                val utilisateur = Repository.obtenirUtilisateurCourant()
                    ?: throw IllegalStateException("Profil introuvable.")
                entrepriseId = utilisateur.entreprise_id
                utilisateurId = utilisateur.id

                val conges = Repository.listerMesConges(utilisateur.id)
                    .sortedByDescending { it.date_debut }

                _state.value = MesCongesUiState.Success(conges)
            } catch (e: Exception) {
                _state.value = MesCongesUiState.Error(ErrorMessages.friendly(e.message))
            }
        }
    }

    fun demander(typeConge: String, dateDebut: String, dateFin: String, motif: String?) {
        val eid = entrepriseId ?: return
        val uid = utilisateurId ?: return

        if (dateFin < dateDebut) {
            _erreurEnvoi.value = "La date de fin doit être après la date de début."
            return
        }

        viewModelScope.launch {
            _envoiEnCours.value = true
            _erreurEnvoi.value = null
            try {
                Repository.demanderConge(eid, uid, typeConge, dateDebut, dateFin, motif)
                _envoiReussi.value = true
                charger()
            } catch (e: Exception) {
                _erreurEnvoi.value = ErrorMessages.friendly(e.message)
            } finally {
                _envoiEnCours.value = false
            }
        }
    }

    fun effacerConfirmation() {
        _envoiReussi.value = false
    }
}