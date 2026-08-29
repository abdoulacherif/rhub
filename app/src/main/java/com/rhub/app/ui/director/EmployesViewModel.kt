package com.rhub.app.ui.director

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.data.Invitation
import com.rhub.app.data.Poste
import com.rhub.app.data.Repository
import com.rhub.app.data.Utilisateur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EmployesUiState {
    object Loading : EmployesUiState()
    data class Error(val message: String) : EmployesUiState()
    data class Success(
        val employes: List<Utilisateur>,
        val invitations: List<Invitation>,
        val postes: List<Poste>
    ) : EmployesUiState()
}

class EmployesViewModel : ViewModel() {
    private val _state = MutableStateFlow<EmployesUiState>(EmployesUiState.Loading)
    val state: StateFlow<EmployesUiState> = _state

    private var entrepriseId: String? = null
    private var utilisateurId: String? = null

    private val _invitationEnCours = MutableStateFlow(false)
    val invitationEnCours: StateFlow<Boolean> = _invitationEnCours

    private val _erreurInvitation = MutableStateFlow<String?>(null)
    val erreurInvitation: StateFlow<String?> = _erreurInvitation

    private val _lienGenere = MutableStateFlow<String?>(null)
    val lienGenere: StateFlow<String?> = _lienGenere

    fun charger() {
        viewModelScope.launch {
            _state.value = EmployesUiState.Loading
            try {
                val utilisateur = Repository.obtenirUtilisateurCourant()
                    ?: throw IllegalStateException("Profil introuvable.")
                val eid = utilisateur.entreprise_id ?: throw IllegalStateException("Aucune entreprise associée.")
                entrepriseId = eid
                utilisateurId = utilisateur.id

                val employes = Repository.listerEmployes(eid)
                val invitations = Repository.listerInvitationsEnAttente(eid)
                val postes = Repository.listerPostes(eid)

                _state.value = EmployesUiState.Success(employes, invitations, postes)
            } catch (e: Exception) {
                _state.value = EmployesUiState.Error(e.message ?: "Erreur de chargement.")
            }
        }
    }

    fun inviter(posteId: String?, email: String, telephone: String?, salaire: Double?) {
        val eid = entrepriseId ?: return
        val uid = utilisateurId ?: return
        viewModelScope.launch {
            _invitationEnCours.value = true
            _erreurInvitation.value = null
            _lienGenere.value = null
            try {
                val invitation = Repository.genererInvitation(eid, posteId, email, telephone, salaire, uid)
                _lienGenere.value = "https://TON-DOMAINE/rejoindre.html?code=${invitation.code}"
                charger()
            } catch (e: Exception) {
                _erreurInvitation.value = e.message ?: "Erreur lors de l'invitation."
            } finally {
                _invitationEnCours.value = false
            }
        }
    }

    fun annulerInvitation(id: String) {
        viewModelScope.launch {
            Repository.annulerInvitation(id)
            charger()
        }
    }

    fun effacerLien() {
        _lienGenere.value = null
    }
}