package com.rhub.app.data

import com.rhub.app.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

object Repository {
    private val client = SupabaseClientProvider.client

    suspend fun obtenirUtilisateurCourant(): Utilisateur? {
        val uid = client.auth.currentUserOrNull()?.id ?: return null
        return client.postgrest["utilisateurs"]
            .select { filter { eq("id", uid) } }
            .decodeSingleOrNull<Utilisateur>()
    }

    suspend fun compterEmployesActifs(entrepriseId: String): Int =
        listerEmployes(entrepriseId).size

    suspend fun compterPostes(entrepriseId: String): Int =
        listerPostes(entrepriseId).size

    suspend fun compterCongesEnAttente(entrepriseId: String): Int {
        return client.postgrest["conges"]
            .select {
                filter {
                    eq("entreprise_id", entrepriseId)
                    eq("statut", "en_attente")
                }
            }
            .decodeList<Conge>()
            .size
    }

    suspend fun listerPostes(entrepriseId: String): List<Poste> {
        return client.postgrest["postes"]
            .select { filter { eq("entreprise_id", entrepriseId) } }
            .decodeList<Poste>()
    }

    suspend fun creerPoste(entrepriseId: String, nomPoste: String, salaireBase: Double, description: String?) {
        client.postgrest["postes"].insert(
            NouveauPoste(
                entreprise_id = entrepriseId,
                nom_poste = nomPoste,
                salaire_base = salaireBase,
                description = description
            )
        )
    }

    suspend fun listerEmployes(entrepriseId: String): List<Utilisateur> {
        return client.postgrest["utilisateurs"]
            .select {
                filter {
                    eq("entreprise_id", entrepriseId)
                    eq("role", "employe")
                    eq("statut", "actif")
                }
            }
            .decodeList<Utilisateur>()
    }

    suspend fun listerInvitationsEnAttente(entrepriseId: String): List<Invitation> {
        return client.postgrest["invitations"]
            .select {
                filter {
                    eq("entreprise_id", entrepriseId)
                    eq("statut", "en_attente")
                }
            }
            .decodeList<Invitation>()
    }

    suspend fun genererInvitation(
        entrepriseId: String,
        posteId: String?,
        email: String,
        telephone: String?,
        salairePropose: Double?,
        creeParId: String
    ): Invitation {
        return client.postgrest["invitations"]
            .insert(
                NouvelleInvitation(
                    entreprise_id = entrepriseId,
                    poste_id = posteId,
                    email = email,
                    telephone = telephone,
                    salaire_propose = salairePropose,
                    cree_par = creeParId
                )
            ) { select() }
            .decodeSingle<Invitation>()
    }

    suspend fun annulerInvitation(invitationId: String) {
        client.postgrest["invitations"]
            .update({ set("statut", "annulee") }) { filter { eq("id", invitationId) } }
    }
}

@Serializable
data class NouveauPoste(
    val entreprise_id: String,
    val nom_poste: String,
    val salaire_base: Double,
    val description: String? = null
)