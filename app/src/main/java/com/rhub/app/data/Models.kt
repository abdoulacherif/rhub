package com.rhub.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Utilisateur(
    val id: String,
    val entreprise_id: String? = null,
    val poste_id: String? = null,
    val role: String,
    val nom: String,
    val prenom: String,
    val email: String,
    val telephone: String? = null,
    val salaire_reel: Double? = null,
    val numero_mobile_money: String? = null,
    val statut: String
)

@Serializable
data class Entreprise(
    val id: String,
    val nom: String,
    val secteur: String? = null,
    val statut: String
)

@Serializable
data class Poste(
    val id: String,
    val entreprise_id: String,
    val nom_poste: String,
    val salaire_base: Double,
    val description: String? = null
)

@Serializable
data class Presence(
    val id: String? = null,
    val entreprise_id: String,
    val utilisateur_id: String,
    val date: String,
    val heure_arrivee: String? = null,
    val heure_depart: String? = null,
    val statut: String = "present"
)

@Serializable
data class NouvellePresence(
    val entreprise_id: String,
    val utilisateur_id: String,
    val date: String,
    val heure_arrivee: String,
    val statut: String = "present"
)

@Serializable
data class Conge(
    val id: String? = null,
    val entreprise_id: String,
    val utilisateur_id: String,
    val type_conge: String,
    val date_debut: String,
    val date_fin: String,
    val motif: String? = null,
    val statut: String = "en_attente"
)

@Serializable
data class NouveauConge(
    val entreprise_id: String,
    val utilisateur_id: String,
    val type_conge: String,
    val date_debut: String,
    val date_fin: String,
    val motif: String? = null
)

@Serializable
data class BulletinPaie(
    val id: String,
    val entreprise_id: String,
    val utilisateur_id: String,
    val mois: Int,
    val annee: Int,
    val salaire_brut: Double,
    val charges: Double,
    val salaire_net: Double,
    val statut_versement: String,
    val date_versement: String? = null
)

@Serializable
data class TachePoste(
    val id: String,
    val poste_id: String,
    val titre: String,
    val description: String? = null,
    val poste_validateur_id: String? = null
)

@Serializable
data class TacheTravail(
    val id: String,
    val titre: String,
    val statut: String,
    val date_terminee: String
)

@Serializable
data class Invitation(
    val id: String,
    val entreprise_id: String,
    val poste_id: String? = null,
    val email: String,
    val code: String,
    val statut: String,
    val expire_le: String
)

@Serializable
data class NouvelleInvitation(
    val entreprise_id: String,
    val poste_id: String?,
    val email: String,
    val telephone: String? = null,
    val salaire_propose: Double? = null,
    val cree_par: String
)