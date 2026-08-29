package com.rhub.app.ui.director

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OverviewUiState {
    object Idle : OverviewUiState()
    object Loading : OverviewUiState()
    data class Error(val message: String) : OverviewUiState()
    data class Success(val employesActifs: Int, val postes: Int, val congesEnAttente: Int) : OverviewUiState()
}

class OverviewViewModel : ViewModel() {
    private val _state = MutableStateFlow<OverviewUiState>(OverviewUiState.Idle)
    val state: StateFlow<OverviewUiState> = _state

    fun charger() {
        viewModelScope.launch {
            _state.value = OverviewUiState.Loading
            try {
                val utilisateur = Repository.obtenirUtilisateurCourant()
                    ?: throw IllegalStateException("Profil introuvable.")
                val entrepriseId = utilisateur.entreprise_id
                    ?: throw IllegalStateException("Aucune entreprise associée.")

                val employes = Repository.compterEmployesActifs(entrepriseId)
                val postes = Repository.compterPostes(entrepriseId)
                val conges = Repository.compterCongesEnAttente(entrepriseId)

                _state.value = OverviewUiState.Success(employes, postes, conges)
            } catch (e: Exception) {
                _state.value = OverviewUiState.Error(e.message ?: "Erreur de chargement.")
            }
        }
    }
}