package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun generateCsvFile(context: Context, transactions: List<TransactionEntity>): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val csvFile = File(exportDir, "Registre_Transactions_Kiosque_$dateStr.csv")

        FileWriter(csvFile).use { writer ->
            // CSV Header compliant with West African / Burkina Mobile Money agency records
            writer.append("ID;Date & Heure;Opérateur;Type d'opération;Montant (FCFA);Frais (FCFA);Nom Client;N° Téléphone Client;N° Destinataire;Type Pièce;N° Pièce (CNIB/Passeport);Solde Flotte Avant;Solde Flotte Après;Solde Caisse Avant;Solde Caisse Après;Code Réf / SMS;Photo Pièce;Notes\n")

            for (tx in transactions) {
                val formattedDate = Formatters.formatDateTime(tx.timestamp)
                val line = listOf(
                    tx.id.toString(),
                    formattedDate,
                    tx.network,
                    tx.type,
                    tx.amount.toLong().toString(),
                    tx.fee.toLong().toString(),
                    tx.clientName.replace(";", " "),
                    tx.clientPhone,
                    tx.recipientPhone,
                    tx.idType,
                    tx.idNumber,
                    tx.kioskBalanceBefore.toLong().toString(),
                    tx.kioskBalanceAfter.toLong().toString(),
                    tx.cashBalanceBefore.toLong().toString(),
                    tx.cashBalanceAfter.toLong().toString(),
                    tx.referenceCode.replace(";", " "),
                    if (tx.idPhotoPath != null) "OUI" else "NON",
                    tx.notes.replace(";", " ").replace("\n", " ")
                ).joinToString(";")
                writer.append(line).append("\n")
            }
        }
        return csvFile
    }

    fun shareCsvFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Registre des transactions KiosquePay BF - ${file.name}")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exporter le registre (CSV)"))
    }

    fun generateReceiptText(kioskName: String, agentPhone: String, tx: TransactionEntity): String {
        return """
            ================================
                 $kioskName
               REÇU DE TRANSACTION
            ================================
            Date: ${Formatters.formatDateTime(tx.timestamp)}
            Réf: ${if (tx.referenceCode.isNotBlank()) tx.referenceCode else "TX-${tx.id}"}
            Réseau: ${tx.network}
            Opération: ${tx.type}
            --------------------------------
            Montant: ${Formatters.formatFcfa(tx.amount)}
            Frais/Commission: ${Formatters.formatFcfa(tx.fee)}
            Total: ${Formatters.formatFcfa(tx.amount + tx.fee)}
            --------------------------------
            Client: ${if (tx.clientName.isNotBlank()) tx.clientName else "Non spécifié"}
            Téléphone: ${Formatters.formatBurkinaPhone(tx.clientPhone)}
            ${if (tx.recipientPhone.isNotBlank()) "Destinataire: ${Formatters.formatBurkinaPhone(tx.recipientPhone)}\n" else ""}N° Pièce / CNIB: ${if (tx.idNumber.isNotBlank()) tx.idNumber else "Enregistré"}
            --------------------------------
            Merci pour votre fidélité !
            Contact Kiosque: $agentPhone
            ================================
        """.trimIndent()
    }

    fun shareReceipt(context: Context, receiptText: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Reçu de transaction Mobile Money")
            putExtra(Intent.EXTRA_TEXT, receiptText)
        }
        context.startActivity(Intent.createChooser(intent, "Partager le reçu client"))
    }
}
