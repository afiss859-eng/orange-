package com.example.data.model

enum class MobileNetwork(
    val id: String,
    val displayName: String,
    val brandColorHex: Long,
    val shortCode: String
) {
    ORANGE_MONEY("ORANGE_MONEY", "Orange Money Burkina", 0xFFFF6600, "OM")
}

enum class TransactionType(
    val id: String,
    val displayName: String,
    val isCashIn: Boolean, // True if client gives cash to agent
    val subtitle: String
) {
    DEPOT("DEPOT", "Dépôt d'argent", true, "Cash-In (Client donne espèces)"),
    RETRAIT("RETRAIT", "Retrait d'argent", false, "Cash-Out (Client reçoit espèces)"),
    TRANSFERT("TRANSFERT", "Transfert d'argent", true, "National & Sous-région"),
    FACTURE("FACTURE", "Paiement Facture", true, "SONABEL, ONEA, Canal+"),
    CREDIT("CREDIT", "Achat Crédit / Forfait", true, "Recharge téléphonique IAP")
}

enum class IdType(val displayName: String) {
    CNIB("CNIB (Burkina Faso)"),
    PASSEPORT("Passeport"),
    PERMIS("Permis de conduire"),
    CARTE_CONSULAIRE("Carte Consulaire / Militaire"),
    AUTRE("Autre Pièce d'identité")
}

