package com.rhub.app.data

/**
 * Traduit toute erreur technique (réseau, Supabase, timeout...) en message
 * clair pour l'utilisateur — jamais d'URL, de nom de service technique,
 * ou de détail d'infrastructure affiché à l'écran.
 */
object ErrorMessages {

    fun friendly(raw: String?): String {
        if (raw == null) return "Une erreur est survenue. Réessayez."
        val msg = raw.lowercase()

        return when {
            msg.contains("timeout") || msg.contains("timed out") ->
                "La connexion a pris trop de temps. Vérifiez votre connexion internet et réessayez."

            msg.contains("failed to connect") || msg.contains("unable to resolve host")
                || msg.contains("unknownhost") || msg.contains("no address associated") ->
                "Impossible de joindre le serveur. Vérifiez votre connexion internet."

            msg.contains("already registered") || msg.contains("already exists") ->
                "Cette adresse e-mail est déjà utilisée."

            msg.contains("invalid login credentials") ->
                "E-mail ou mot de passe incorrect."

            msg.contains("password should be at least") || msg.contains("password") && msg.contains("least") ->
                "Le mot de passe doit contenir au moins 10 caractères."

            msg.contains("rate limit") ->
                "Trop de tentatives. Réessayez dans quelques minutes."

            msg.contains("row level security") || msg.contains("permission") || msg.contains("denied") ->
                "Vous n'avez pas accès à cette information."

            msg.contains("network") ->
                "Problème de connexion réseau. Réessayez."

            // Filet de sécurité : si le message contient une URL ou un détail
            // technique reconnaissable, on masque tout par un message générique
            // plutôt que de risquer d'exposer une adresse de serveur.
            msg.contains("http") || msg.contains("supabase") || msg.contains("://") ->
                "Une erreur technique est survenue. Réessayez dans un instant."

            else -> "Une erreur est survenue. Réessayez."
        }
    }
}