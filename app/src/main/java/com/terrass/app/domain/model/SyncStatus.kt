package com.terrass.app.domain.model

enum class SyncStatus {
    IDLE,       // pas encore démarré
    SYNCING,    // sync en cours
    UP_TO_DATE, // connecté, à jour
    OFFLINE,    // erreur réseau
}
