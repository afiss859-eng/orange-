package com.example.utils

import com.example.data.model.TransactionType

data class OrangeTariffInfo(
    val customerFee: Double,
    val agentCommission: Double,
    val bracketLabel: String,
    val explanation: String
)

object FeeCalculator {

    /**
     * Calcule les frais client officiels Orange Money Burkina Faso.
     */
    fun calculateCustomerFee(type: String, amount: Double): Double {
        if (amount <= 0.0) return 0.0

        return when (type) {
            TransactionType.DEPOT.id -> 0.0 // Le dépôt est 100% GRATUIT pour le client
            TransactionType.RETRAIT.id -> calculateOrangeWithdrawalFee(amount)
            TransactionType.TRANSFERT.id -> calculateOrangeTransferFee(amount)
            TransactionType.FACTURE.id -> 100.0 // Frais de quittance / timbre
            TransactionType.CREDIT.id -> 0.0 // Achat crédit gratuit pour le client
            else -> 0.0
        }
    }

    /**
     * Calcule la commission agent / distributeur Orange Money Burkina Faso.
     */
    fun calculateAgentCommission(type: String, amount: Double): Double {
        if (amount <= 0.0) return 0.0

        return when (type) {
            TransactionType.DEPOT.id -> calculateOrangeDepositCommission(amount)
            TransactionType.RETRAIT.id -> calculateOrangeWithdrawalCommission(amount)
            TransactionType.TRANSFERT.id -> calculateOrangeTransferCommission(amount)
            TransactionType.FACTURE.id -> 50.0 // Rémunération agent par facture
            TransactionType.CREDIT.id -> Math.round(amount * 0.05).toDouble() // 5% sur vente de crédit
            else -> 0.0
        }
    }

    fun getTariffDetails(type: String, amount: Double): OrangeTariffInfo {
        val fee = calculateCustomerFee(type, amount)
        val commission = calculateAgentCommission(type, amount)

        return when (type) {
            TransactionType.DEPOT.id -> {
                val label = "Dépôt Orange Money (Gratuit client)"
                val exp = "Le client ne paie aucun frais. L'agent touche une commission distributeur Orange de ${commission.toInt()} FCFA."
                OrangeTariffInfo(fee, commission, label, exp)
            }
            TransactionType.RETRAIT.id -> {
                val label = "Retrait Cash-Out Orange Money"
                val exp = "Frais prélevés sur le compte client : ${fee.toInt()} FCFA. Part commission agent : ${commission.toInt()} FCFA."
                OrangeTariffInfo(fee, commission, label, exp)
            }
            TransactionType.TRANSFERT.id -> {
                val label = "Transfert Orange Money National"
                val exp = "Frais d'envoi client : ${fee.toInt()} FCFA. Commission distributeur : ${commission.toInt()} FCFA."
                OrangeTariffInfo(fee, commission, label, exp)
            }
            TransactionType.FACTURE.id -> {
                OrangeTariffInfo(100.0, 50.0, "Paiement Facture (SONABEL/ONEA)", "Frais timbre quittance : 100 FCFA. Commission agent : 50 FCFA.")
            }
            TransactionType.CREDIT.id -> {
                OrangeTariffInfo(0.0, commission, "Achat Crédit / Forfait IAP", "Vente de recharge téléphonique : 5% de commission agent (${commission.toInt()} FCFA).")
            }
            else -> OrangeTariffInfo(fee, commission, "Opération Orange Money", "")
        }
    }

    /**
     * Grille officielle des retraits Orange Money Burkina Faso (Cash-Out)
     */
    private fun calculateOrangeWithdrawalFee(amount: Double): Double {
        return when {
            amount < 200 -> 0.0
            amount <= 500 -> 50.0
            amount <= 1000 -> 75.0
            amount <= 2000 -> 100.0
            amount <= 5000 -> 175.0
            amount <= 10000 -> 350.0
            amount <= 20000 -> 600.0
            amount <= 50000 -> 1000.0
            amount <= 100000 -> 1500.0
            amount <= 250000 -> 2500.0
            amount <= 500000 -> 4000.0
            amount <= 1000000 -> 7000.0
            amount <= 1500000 -> 10000.0
            else -> Math.round(amount * 0.007).toDouble()
        }
    }

    /**
     * Grille des commissions distributeurs Orange Burkina sur les Retraits
     */
    private fun calculateOrangeWithdrawalCommission(amount: Double): Double {
        return when {
            amount < 200 -> 0.0
            amount <= 500 -> 25.0
            amount <= 1000 -> 35.0
            amount <= 2000 -> 50.0
            amount <= 5000 -> 85.0
            amount <= 10000 -> 160.0
            amount <= 20000 -> 260.0
            amount <= 50000 -> 420.0
            amount <= 100000 -> 600.0
            amount <= 250000 -> 950.0
            amount <= 500000 -> 1400.0
            amount <= 1000000 -> 2200.0
            else -> Math.round(amount * 0.0025).toDouble()
        }
    }

    /**
     * Grille des commissions distributeurs Orange Burkina sur les Dépôts (Cash-In)
     */
    private fun calculateOrangeDepositCommission(amount: Double): Double {
        return when {
            amount < 1000 -> 15.0
            amount <= 5000 -> 30.0
            amount <= 10000 -> 50.0
            amount <= 25000 -> 75.0
            amount <= 50000 -> 125.0
            amount <= 100000 -> 250.0
            amount <= 250000 -> 400.0
            amount <= 500000 -> 700.0
            amount <= 1000000 -> 1200.0
            else -> 1800.0
        }
    }

    /**
     * Grille officielle des transferts Orange Money Burkina Faso (P2P / Compte à compte)
     */
    private fun calculateOrangeTransferFee(amount: Double): Double {
        return when {
            amount < 200 -> 0.0
            amount <= 1000 -> 50.0
            amount <= 5000 -> 100.0
            amount <= 10000 -> 200.0
            amount <= 25000 -> 400.0
            amount <= 50000 -> 700.0
            amount <= 100000 -> 1200.0
            amount <= 250000 -> 2000.0
            amount <= 500000 -> 3500.0
            else -> Math.round(amount * 0.008).toDouble()
        }
    }

    private fun calculateOrangeTransferCommission(amount: Double): Double {
        return when {
            amount <= 5000 -> 30.0
            amount <= 25000 -> 70.0
            amount <= 50000 -> 150.0
            amount <= 100000 -> 300.0
            else -> 500.0
        }
    }
}

