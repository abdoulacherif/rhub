package com.rhub.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhub.app.SupabaseClientProvider
import com.rhub.app.data.ErrorMessages
import com.rhub.app.data.Utilisateur
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class Success(val role: String) : AuthUiState()
    object ConfirmationRequise : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client
    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    private var inscriptionEnAttente: EntrepriseSignupPayload? = null

    fun connecter(email: String, motDePasse: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = motDePasse
                }

                val uid = client.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("Connexion impossible.")

                val userRow = client.postgrest["utilisateurs"]
                    .select { filter { eq("id", uid) } }
                    .decodeSingleOrNull<Utilisateur>()

                if (userRow == null) {
                    val enAttente = inscriptionEnAttente
                    if (enAttente != null) {
                        finaliserCreationEntreprise(enAttente)
                        inscriptionEnAttente = null
                        _state.value = AuthUiState.Success("directeur")
                    } else {
                        _state.value = AuthUiState.Error(
                            "Votre e-mail est confirmé mais aucune entreprise n'a été trouvée pour ce compte."
                        )
                    }
                } else {
                    _state.value = AuthUiState.Success(userRow.role)
                }
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(ErrorMessages.friendly(e.message))
            }
        }
    }

    fun inscrireEntreprise(payload: EntrepriseSignupPayload, motDePasse: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            try {
                if (motDePasse.length < 10) {
                    _state.value = AuthUiState.Error("Le mot de passe doit contenir au moins 10 caractères.")
                    return@launch
                }

                client.auth.signUpWith(Email) {
                    this.email = payload.email
                    this.password = motDePasse
                }

                val session = client.auth.currentSessionOrNull()
                if (session != null) {
                    finaliserCreationEntreprise(payload)
                    _state.value = AuthUiState.Success("directeur")
                } else {
                    inscriptionEnAttente = payload
                    _state.value = AuthUiState.ConfirmationRequise
                }
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(ErrorMessages.friendly(e.message))
            }
        }
    }

    private suspend fun finaliserCreationEntreprise(p: EntrepriseSignupPayload) {
        client.postgrest.rpc(
            "creer_entreprise_et_directeur",
            buildJsonObject {
                put("p_nom_entreprise", p.entrepriseNom)
                put("p_secteur", p.secteur)
                put("p_prenom", p.prenom)
                put("p_nom", p.nom)
                put("p_email", p.email)
                put("p_telephone", p.telephone)
                put("p_momo", p.momo)
                put("p_ville", p.ville)
                put("p_taille", p.taille)
            }
        )
    }
}

data class EntrepriseSignupPayload(
    val entrepriseNom: String,
    val secteur: String,
    val prenom: String,
    val nom: String,
    val email: String,
    val telephone: String,
    val momo: String? = null,
    val ville: String? = null,
    val taille: String? = null
)
