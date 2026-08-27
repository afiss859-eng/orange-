package com.example.utils

import com.example.data.model.TransactionType

data class ParsedOrangeSms(
    val rawText: String,
    val reference: String? = null,
    val amount: Double? = null,
    val fee: Double? = null,
    val newBalance: Double? = null,
    val clientPhone: String? = null,
    val clientName: String? = null,
    val detectedType: TransactionType? = null,
    val isValidOrangeSms: Boolean = false,
    val confidenceMessage: String = ""
)

object OrangeMoneySmsParser {

    /**
     * Analyse un SMS brut pour en extraire automatiquement :
     * - Référence de transaction Orange Money (ex: CI240827.1420.A01234)
     * - Montant en FCFA
     * - Numéro de téléphone du client (8 chiffres BF ou avec indicatif 226)
     * - Nom du client ou bénéficiaire
     * - Nouveau solde flotte Orange Money
     * - Type d'opération (Dépôt, Retrait, Transfert, Facture, Crédit)
     */
    fun parse(smsText: String): ParsedOrangeSms {
        if (smsText.isBlank()) {
            return ParsedOrangeSms(rawText = smsText)
        }

        val text = smsText.trim()
        val lower = text.lowercase()

        // 1. Détection du type d'opération
        val detectedType = when {
            lower.contains("facture") || lower.contains("sonabel") || lower.contains("onea") || lower.contains("canal+") || lower.contains("canal plus") -> {
                TransactionType.FACTURE
            }
            lower.contains("credit") || lower.contains("crédit") || lower.contains("recharge") || lower.contains("pass internet") || lower.contains("forfait") -> {
                TransactionType.CREDIT
            }
            lower.contains("depot") || lower.contains("dépôt") || lower.contains("versement") -> {
                TransactionType.DEPOT
            }
            lower.contains("retrait") || lower.contains("cash out") -> {
                TransactionType.RETRAIT
            }
            lower.contains("transfere") || lower.contains("transféré") || lower.contains("transfert") || lower.contains("envoi") || lower.contains("envoye") || lower.contains("envoyé") -> {
                TransactionType.TRANSFERT
            }
            lower.contains("recu") || lower.contains("reçu") || lower.contains("reception") || lower.contains("réception") -> {
                TransactionType.RETRAIT
            }
            else -> null
        }

        // 2. Extraction de la référence SMS (Patterns Orange Money BF)
        var reference: String? = null
        val refPatterns = listOf(
            // Format typique Orange Money: CI240827.1420.A01234, PP240827.0915.B88219, MP260827.1512.98765, etc.
            Regex("""\b([A-Z]{2,3}\d{6}\.\d{4}\.[A-Za-z0-9]+)\b""", RegexOption.IGNORE_CASE),
            // Pattern avec mot clé: ID Transaction: XXX, Ref: XXX, Reference: XXX, ID Trans: XXX
            Regex("""(?:id\s*transaction|transaction\s*id|ref\b|reference|référence|ref\.|id\s*trans|txn\s*id)\s*[:=]?\s*([A-Za-z0-9\._\-]{6,28})""", RegexOption.IGNORE_CASE),
            // Format compact TXN ou 10-14 caractères alphanumériques
            Regex("""\b([A-Z]{2}\d{8,14})\b""")
        )

        for (pattern in refPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                reference = match.groupValues[1].trim().trimEnd('.')
                break
            }
        }

        // 3. Extraction du nouveau solde (Nouveau solde: XXX FCFA ou Solde: XXX FCFA)
        var newBalance: Double? = null
        val balancePatterns = listOf(
            Regex("""(?:nouveau\s+solde|solde\s+restant|solde\s+actuel|solde\s+apres|solde\s+après|solde\s+disponible)\s*[:=]?\s*([0-9\s\.\,]+)\s*(?:fcfa|cfa|f\b)""", RegexOption.IGNORE_CASE),
            Regex("""(?:solde\s+de\s+votre\s+compte(?:\s+principal)?\s+est\s+de)\s*([0-9\s\.\,]+)\s*(?:fcfa|cfa|f\b)""", RegexOption.IGNORE_CASE),
            Regex("""\bsolde\s*[:=]\s*([0-9\s\.\,]+)\s*(?:fcfa|cfa|f\b)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in balancePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val numStr = cleanNumberString(match.groupValues[1])
                newBalance = numStr.toDoubleOrNull()
                if (newBalance != null) break
            }
        }

        // 4. Extraction du Montant de l'opération
        var amount: Double? = null
        val amountPatterns = listOf(
            Regex("""(?:transfere|transféré|transfert|recu|reçu|montant\s*:?|somme\s*:?|depot|dépôt|retrait|paiement(?:\s+de\s+facture(?:\s+\w+)?)?|credit|crédit|achat(?:\s+de)?(?:\s+credit)?)\s*(?:de\s+|d'un\s+montant\s+de\s+)?([0-9\s\.\,]+)\s*(?:fcfa|cfa|f\b)""", RegexOption.IGNORE_CASE),
            Regex("""([0-9\s\.\,]+)\s*(?:fcfa|cfa|f\s+cfa)\s+(?:vers|au|de|du|pour|sur)""", RegexOption.IGNORE_CASE),
            Regex("""\b([0-9]{3,9})\s*(?:fcfa|cfa)\b""", RegexOption.IGNORE_CASE)
        )

        for (pattern in amountPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val numStr = cleanNumberString(match.groupValues[1])
                val parsed = numStr.toDoubleOrNull()
                // Assurer qu'on ne prend pas le nouveau solde par erreur comme montant
                if (parsed != null && parsed != newBalance) {
                    amount = parsed
                    break
                }
            }
        }

        // 5. Extraction des Frais
        var fee: Double? = null
        val feePattern = Regex("""(?:frais|frais\s*factures|commission)\s*[:=]?\s*([0-9\s\.\,]+)\s*(?:fcfa|cfa|f\b)""", RegexOption.IGNORE_CASE)
        val feeMatch = feePattern.find(text)
        if (feeMatch != null) {
            val numStr = cleanNumberString(feeMatch.groupValues[1])
            fee = numStr.toDoubleOrNull()
        }

        // 6. Extraction du Numéro de téléphone du client
        var clientPhone: String? = null
        val phonePatterns = listOf(
            // Numéro avec indicatif 226: +22670123456 ou 226 70 12 34 56
            Regex("""(?:\+226|226)\s*([0567]\d{7}|[0567]\d(?:\s*\d{2}){3})"""),
            // Numéro standard BF (8 chiffres commençant par 0, 5, 6, 7) après un mot clé
            Regex("""(?:au|vers|de|du|profit\s+du|client|compte|par|pour|sur\s+le\s+compte)\s+(?:\+226|226)?\s*([0567]\d{7}|[0567]\d(?:\s*\d{2}){3})""", RegexOption.IGNORE_CASE),
            // Numéro standard BF seul
            Regex("""\b([0567]\d{7})\b""")
        )

        for (pattern in phonePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val rawPhone = match.groupValues[1].replace(" ", "").trim()
                if (rawPhone.length == 8) {
                    clientPhone = rawPhone
                    break
                }
            }
        }

        // 7. Extraction du Nom du client
        var clientName: String? = null
        val nameInParentheses = Regex("""\(([A-Za-zÀ-ÿ\s\.\-]{3,35})\)""").find(text)
        if (nameInParentheses != null) {
            clientName = nameInParentheses.groupValues[1].trim()
        } else {
            // Après le numéro de téléphone: "au 22670123456 OUEDRAOGO Moussa." ou "compte 76001122 SAWADOGO Fatou."
            val nameAfterPhone = Regex("""(?:(?:\+226|226)?\s*[0567]\d{7})\s+([A-Za-zÀ-ÿ]{2,}(?:\s+[A-Za-zÀ-ÿ]{2,}){1,3})(?=\.|\,|\s*Frais|\s*ID|\s*Ref|\s*Solde|$)""", RegexOption.IGNORE_CASE).find(text)
            if (nameAfterPhone != null) {
                clientName = nameAfterPhone.groupValues[1].trim()
            } else {
                val nameAfterKeywords = Regex("""(?:client|beneficiaire|bénéficiaire|nom)\s*[:=]\s*([A-Za-zÀ-ÿ\s\.\-]{3,30})""", RegexOption.IGNORE_CASE).find(text)
                if (nameAfterKeywords != null) {
                    clientName = nameAfterKeywords.groupValues[1].trim()
                }
            }
        }

        val isValid = (reference != null || amount != null || newBalance != null) &&
                (lower.contains("orange") || lower.contains("solde") || lower.contains("fcfa") || lower.contains("transaction") || lower.contains("ref"))

        val confidenceMessage = buildString {
            if (isValid) {
                append("SMS Orange Money détecté avec succès. ")
                if (reference != null) append("Réf: $reference. ")
                if (amount != null) append("Montant: ${amount.toInt()} F. ")
                if (newBalance != null) append("Nouveau solde: ${newBalance.toInt()} F.")
            } else {
                append("Format de SMS Orange Money non reconnu avec certitude.")
            }
        }

        return ParsedOrangeSms(
            rawText = text,
            reference = reference,
            amount = amount,
            fee = fee,
            newBalance = newBalance,
            clientPhone = clientPhone,
            clientName = clientName,
            detectedType = detectedType,
            isValidOrangeSms = isValid,
            confidenceMessage = confidenceMessage
        )
    }

    private fun cleanNumberString(raw: String): String {
        return raw.replace(" ", "").replace(".", "").replace(",", "").trim()
    }

    /**
     * Exemples types de SMS Orange Money Burkina Faso pour tests et démonstrations
     */
    val SAMPLE_SMS_LIST = listOf(
        "Vous avez transfere 10000 FCFA au 22670123456 (OUEDRAOGO ABDOUL). Frais: 0 FCFA. Nouveau solde: 450000 FCFA. ID Transaction: CI240827.1420.A01234.",
        "Vous avez recu 25000 FCFA de 76112233 (TRAORE FATOU). Frais: 175 FCFA. Nouveau solde: 525000 FCFA. Ref: CO260827.1030.B54321.",
        "Transfert effectue avec succes. Montant: 50 000 FCFA vers 75889900 (SAWADOGO AFIS). Ref: MP260827.1512.98765. Solde: 380 000 FCFA.",
        "Paiement de facture SONABEL de 18500 FCFA effectue avec succes. Ref: FA260827.1700.001. Nouveau solde: 291500 FCFA.",
        "Achat credit de 2000 FCFA pour le 70123456 reussi. Reference: CR260827.1800.002. Solde: 289500 FCFA."
    )
}
