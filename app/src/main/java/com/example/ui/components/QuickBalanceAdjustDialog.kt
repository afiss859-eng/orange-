package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MobileNetwork
import com.example.ui.theme.CashEmerald
import com.example.ui.theme.OrangeMoneyOrange
import com.example.utils.Formatters

@Composable
fun QuickBalanceAdjustDialog(
    targetAccount: String?, // "CASH", "ORANGE_MONEY"
    currentCash: Double,
    currentOrange: Double,
    onAdjustCash: (Double) -> Unit,
    onAdjustFlotte: (network: String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTarget by remember { mutableStateOf(targetAccount ?: "CASH") }
    var isAdding by remember { mutableStateOf(true) }
    var amountStr by remember { mutableStateOf("") }
    var reasonStr by remember { mutableStateOf("") }

    val accountName = when (selectedTarget) {
        "CASH" -> "Caisse (Espèces Physiques)"
        "ORANGE_MONEY" -> "Flotte Orange Money"
        else -> "Caisse"
    }

    val currentBalance = when (selectedTarget) {
        "CASH" -> currentCash
        "ORANGE_MONEY" -> currentOrange
        else -> currentCash
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("balance_adjust_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Ajuster un Solde de Caisse",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Alimentation ou retrait de fonds pour le contrôle journalier",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Account Selection Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountChoicePill("CASH", "Espèces Caisse", selectedTarget == "CASH", Modifier.weight(1f)) { selectedTarget = "CASH" }
                    AccountChoicePill("ORANGE_MONEY", "Flotte Orange Money", selectedTarget == "ORANGE_MONEY", Modifier.weight(1f)) { selectedTarget = "ORANGE_MONEY" }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current balance display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Solde actuel $accountName:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatFcfa(currentBalance),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OrangeMoneyOrange
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Type (+ Ajouter / - Déduire)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAdding) CashEmerald else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 10.dp),
                        color = Color.Transparent,
                        onClick = { isAdding = true }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isAdding) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = " Approvisionner (+)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdding) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isAdding) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 10.dp),
                        color = Color.Transparent,
                        onClick = { isAdding = false }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = if (!isAdding) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = " Sortie de caisse (-)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isAdding) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Montant de l'ajustement (FCFA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_adjust_amount"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reason / Note
                OutlinedTextField(
                    value = reasonStr,
                    onValueChange = { reasonStr = it },
                    label = { Text("Motif (ex: Achat UV Orange, Versement banque)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                val delta = if (isAdding) amount else -amount
                                if (selectedTarget == "CASH") {
                                    onAdjustCash(delta)
                                } else {
                                    onAdjustFlotte(MobileNetwork.ORANGE_MONEY.id, delta)
                                }
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_confirm_adjust"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdding) CashEmerald else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = "Enregistrer", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Annuler")
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountChoicePill(
    id: String,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        color = if (isSelected) OrangeMoneyOrange else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
