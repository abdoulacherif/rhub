package com.rhub.app.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.data.BulletinPaie
import com.rhub.app.data.ErrorMessages
import com.rhub.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MesBulletinsUiState {
    object Loading : MesBulletinsUiState()
    data class Error(val message: String) : MesBulletinsUiState()
    data class Success(val bulletins: List<BulletinPaie>) : MesBulletinsUiState()
}

class MesBulletinsViewModel : ViewModel() {
    private val _state = MutableStateFlow<MesBulletinsUiState>(MesBulletinsUiState.Loading)
    val state: StateFlow<MesBulletinsUiState> = _state

    fun charger() {
        viewModelScope.launch {
            _state.value = MesBulletinsUiState.Loading
            try {
                val utilisateur = Repository.obtenirUtilisateurCourant()
                    ?: throw IllegalStateException("Profil introuvable.")
                val bulletins = Repository.listerMesBulletins(utilisateur.id)
                _state.value = MesBulletinsUiState.Success(bulletins)
            } catch (e: Exception) {
                _state.value = MesBulletinsUiState.Error(ErrorMessages.friendly(e.message))
            }
        }
    }
}