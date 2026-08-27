package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransactionEntity
import com.example.data.repository.KioskConfig
import com.example.ui.theme.CashEmerald
import com.example.ui.theme.OrangeMoneyOrange
import com.example.utils.ExportHelper
import com.example.utils.Formatters

@Composable
fun ReceiptShareModal(
    transaction: TransactionEntity?,
    config: KioskConfig,
    onDismiss: () -> Unit
) {
    if (transaction == null) return
    val context = LocalContext.current

    val receiptText = ExportHelper.generateReceiptText(
        kioskName = config.kioskName,
        agentPhone = config.agentPhone,
        tx = transaction
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("receipt_share_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge & Header
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CashEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CashEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Transaction Enregistrée !",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Les soldes caisse & flotte ont été mis à jour",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Printable Receipt Paper View
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp)),
                    color = Color(0xFFFFFBEB)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = config.kioskName.uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = "${transaction.network} • ${transaction.type}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = OrangeMoneyOrange
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFFDE68A))
                        Spacer(modifier = Modifier.height(8.dp))

                        ReceiptLine("Montant", Formatters.formatFcfa(transaction.amount), isBold = true)
                        if (transaction.fee > 0) {
                            ReceiptLine("Frais", Formatters.formatFcfa(transaction.fee))
                            ReceiptLine("Total encaissé", Formatters.formatFcfa(transaction.amount + transaction.fee), isBold = true)
                        }
                        ReceiptLine("Client", transaction.clientName.ifBlank { "Client" })
                        ReceiptLine("Téléphone", Formatters.formatBurkinaPhone(transaction.clientPhone))
                        if (transaction.idNumber.isNotBlank()) {
                            ReceiptLine("N° CNIB", transaction.idNumber)
                        }
                        ReceiptLine("Réf SMS", transaction.referenceCode)
                        ReceiptLine("Date", Formatters.formatDateTime(transaction.timestamp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Share Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            ExportHelper.shareReceipt(context, receiptText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_share_receipt"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeMoneyOrange)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Partager Reçu", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("btn_close_receipt"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Fermer")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptLine(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color(0xFF92400E)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            ),
            color = Color(0xFF451A03)
        )
    }
}
