package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TransactionType
import com.example.ui.theme.CashEmerald
import com.example.ui.theme.OrangeMoneyDark
import com.example.ui.theme.OrangeMoneyOrange
import com.example.utils.Formatters
import com.example.utils.OrangeMoneySmsParser
import com.example.utils.ParsedOrangeSms

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrangeSmsModal(
    onDismiss: () -> Unit,
    onApplyToTransaction: (ParsedOrangeSms) -> Unit,
    onSyncFlotteBalance: ((Double, String?) -> Unit)? = null,
    initialText: String = ""
) {
    val clipboardManager = LocalClipboardManager.current
    var smsInputText by remember { mutableStateOf(initialText) }
    val parsedResult = remember(smsInputText) {
        OrangeMoneySmsParser.parse(smsInputText)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("orange_sms_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OrangeMoneyOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Lecture SMS Orange Money",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Détection auto & validation du solde",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = OrangeMoneyOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // SMS Input Field
                OutlinedTextField(
                    value = smsInputText,
                    onValueChange = { smsInputText = it },
                    label = { Text("Coller le texte du SMS reçu d'Orange") },
                    placeholder = { Text("ex: Vous avez transfere 10000 FCFA au 22670123456... Ref: CI240827...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .testTag("sms_input_field"),
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        if (smsInputText.isNotEmpty()) {
                            IconButton(onClick = { smsInputText = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    }
                )

                // Quick Action: Paste from Clipboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                smsInputText = clipText
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_paste_sms"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = OrangeMoneyOrange.copy(alpha = 0.12f),
                            contentColor = OrangeMoneyOrange
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Coller depuis le presse-papiers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Sample SMS Quick Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Ou tester avec un exemple Orange BF :",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OrangeMoneySmsParser.SAMPLE_SMS_LIST.forEachIndexed { index, sample ->
                            val label = when (index) {
                                0 -> "Dépôt 10.000 F"
                                1 -> "Retrait 25.000 F"
                                2 -> "Transfert 50.000 F"
                                3 -> "Facture SONABEL"
                                4 -> "Crédit 2.000 F"
                                else -> "Exemple ${index + 1}"
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { smsInputText = sample }
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Extracted Analysis Results
                AnimatedVisibility(visible = parsedResult.isValidOrangeSms || smsInputText.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (parsedResult.isValidOrangeSms)
                                OrangeMoneyOrange.copy(alpha = 0.08f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (parsedResult.isValidOrangeSms) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (parsedResult.isValidOrangeSms) CashEmerald else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (parsedResult.isValidOrangeSms) "Données Orange Money Extraites" else "Format non reconnu",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (parsedResult.isValidOrangeSms) CashEmerald else MaterialTheme.colorScheme.error
                                        )
                                    )
                                }

                                parsedResult.detectedType?.let { type ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = OrangeMoneyOrange
                                    ) {
                                        Text(
                                            text = type.displayName,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Details Grid
                            if (parsedResult.amount != null) {
                                ExtractedRow(
                                    label = "Montant Opération:",
                                    value = Formatters.formatFcfa(parsedResult.amount),
                                    valueColor = OrangeMoneyOrange,
                                    isBold = true
                                )
                            }

                            if (!parsedResult.reference.isNullOrBlank()) {
                                ExtractedRow(
                                    label = "N° Réf Transaction:",
                                    value = parsedResult.reference,
                                    valueColor = MaterialTheme.colorScheme.onSurface,
                                    isBold = true
                                )
                            }

                            if (!parsedResult.clientPhone.isNullOrBlank()) {
                                ExtractedRow(
                                    label = "N° Téléphone Client:",
                                    value = Formatters.formatBurkinaPhone(parsedResult.clientPhone),
                                    valueColor = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!parsedResult.clientName.isNullOrBlank()) {
                                ExtractedRow(
                                    label = "Nom / Bénéficiaire:",
                                    value = parsedResult.clientName,
                                    valueColor = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (parsedResult.newBalance != null) {
                                ExtractedRow(
                                    label = "Nouveau Solde Flotte Orange:",
                                    value = Formatters.formatFcfa(parsedResult.newBalance),
                                    valueColor = CashEmerald,
                                    isBold = true
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onApplyToTransaction(parsedResult)
                            onDismiss()
                        },
                        enabled = parsedResult.isValidOrangeSms || smsInputText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_apply_sms"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeMoneyOrange
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remplir l'Opération avec ce SMS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Optional Sync Flotte button if balance detected
                    if (parsedResult.newBalance != null && onSyncFlotteBalance != null) {
                        OutlinedButton(
                            onClick = {
                                onSyncFlotteBalance(parsedResult.newBalance, parsedResult.reference)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_sync_flotte_sms"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = CashEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Synchroniser Solde Flotte (${Formatters.formatAmountShort(parsedResult.newBalance)} F)",
                                fontWeight = FontWeight.Bold,
                                color = CashEmerald,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtractedRow(
    label: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = valueColor
            )
        )
    }
}
