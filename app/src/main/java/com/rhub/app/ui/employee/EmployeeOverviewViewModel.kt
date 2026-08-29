package com.rhub.app.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.data.Poste
import com.rhub.app.data.Presence
import com.rhub.app.data.Repository
import com.rhub.app.data.Utilisateur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EmployeeOverviewUiState {
    object Loading : EmployeeOverviewUiState()
    data class Error(val message: String) : EmployeeOverviewUiState()
    data class Success(
        val utilisateur: Utilisateur,
        val poste: Poste?,
        val presenceDuJour: Presence?
    ) : EmployeeOverviewUiState()
}

class EmployeeOverviewViewModel : ViewModel() {
    private val _state = MutableStateFlow<EmployeeOverviewUiState>(EmployeeOverviewUiState.Loading)
    val state: StateFlow<EmployeeOverviewUiState> = _state

    fun charger() {
        viewModelScope.launch {
            _state.value = EmployeeOverviewUiState.Loading
            try {
                val utilisateur = Repository.obtenirUtilisateurCourant()
                    ?: throw IllegalStateException("Profil introuvable.")
                val poste = utilisateur.poste_id?.let { Repository.obtenirPoste(it) }
                val presence = Repository.obtenirPresenceDuJour(utilisateur.id)

                _state.value = EmployeeOverviewUiState.Success(utilisateur, poste, presence)
            } catch (e: Exception) {
                _state.value = EmployeeOverviewUiState.Error(e.message ?: "Erreur de chargement.")
            }
        }
    }
}