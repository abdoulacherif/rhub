package com.rhub.app.ui.director

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.data.ErrorMessages
import com.rhub.app.data.Poste
import com.rhub.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PostesUiState {
    object Loading : PostesUiState()
    data class Error(val message: String) : PostesUiState()
    data class Success(val postes: List<Poste>) : PostesUiState()
}

class PostesViewModel : ViewModel() {
    private val _state = MutableStateFlow<PostesUiState>(PostesUiState.Loading)
    val state: StateFlow<PostesUiState> = _state

    private var entrepriseId: String? = null

    private val _creationEnCours = MutableStateFlow(false)
    val creationEnCours: StateFlow<Boolean> = _creationEnCours

    private val _erreurCreation = MutableStateFlow<String?>(null)
    val erreurCreation: StateFlow<String?> = _erreurCreation

    fun charger() {
        viewModelScope.launch {
            _state.value = PostesUiState.Loading
            try {
                val utilisateur = Repository.obtenirUtilisateurCourant()
                    ?: throw IllegalStateException("Profil introuvable.")
                val eid = utilisateur.entreprise_id ?: throw IllegalStateException("Aucune entreprise associée.")
                entrepriseId = eid
                val postes = Repository.listerPostes(eid)
                _state.value = PostesUiState.Success(postes)
            } catch (e: Exception) {
                _state.value = PostesUiState.Error(ErrorMessages.friendly(e.message))
            }
        }
    }

    fun creerPoste(nom: String, salaire: Double, description: String?) {
        val eid = entrepriseId ?: return
        viewModelScope.launch {
            _creationEnCours.value = true
            _erreurCreation.value = null
            try {
                Repository.creerPoste(eid, nom, salaire, description)
                charger()
            } catch (e: Exception) {
                _erreurCreation.value = ErrorMessages.friendly(e.message)
            } finally {
                _creationEnCours.value = false
            }
        }
    }
}