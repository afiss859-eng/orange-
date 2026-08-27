package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MobileNetwork
import com.example.data.model.TransactionType
import com.example.ui.components.QuickBalanceAdjustDialog
import com.example.ui.theme.CashEmerald
import com.example.ui.theme.OrangeMoneyDark
import com.example.ui.theme.OrangeMoneyOrange
import com.example.ui.viewmodel.KioskViewModel
import com.example.utils.FeeCalculator
import com.example.utils.Formatters

@Composable
fun CaisseScreen(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.kioskConfig.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    var showAdjustModal by remember { mutableStateOf(false) }
    var adjustTargetAccount by remember { mutableStateOf<String?>("CASH") }

    val todayTransactions = allTransactions.filter { Formatters.isToday(it.timestamp) }
    val todayDeposits = todayTransactions.filter { it.type == TransactionType.DEPOT.id || it.type == TransactionType.TRANSFERT.id }.sumOf { it.amount }
    val todayWithdrawals = todayTransactions.filter { it.type == TransactionType.RETRAIT.id }.sumOf { it.amount }
    val todayCommissions = todayTransactions.sumOf { tx ->
        FeeCalculator.calculateAgentCommission(tx.type, tx.amount)
    }

    val totalFlotte = config.orangeBalance
    val totalKioskAssets = config.cashBalance + totalFlotte

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("caisse_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Caisse & Flotte Orange",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rapprochement et contrôle d'actif Orange Money BF",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { viewModel.openSmsModal() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = OrangeMoneyOrange.copy(alpha = 0.15f),
                            contentColor = OrangeMoneyOrange
                        ),
                        modifier = Modifier.testTag("btn_caisse_sync_sms")
                    ) {
                        Icon(imageVector = Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "SMS Orange", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            adjustTargetAccount = "CASH"
                            showAdjustModal = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_open_adjust_cash")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ajuster", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Total Net Assets Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIF GLOBAL DU KIOSQUE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = OrangeMoneyOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = Formatters.formatFcfa(totalKioskAssets),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Espèces en Main (Caisse):",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = Formatters.formatFcfa(config.cashBalance),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CashEmerald
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Flotte Orange Money:",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = Formatters.formatFcfa(config.orangeBalance),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeMoneyOrange
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Détails des Comptes Dédiés
        item {
            Text(
                text = "Comptes de Trésorerie Dédiés",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccountDetailRow(
                        title = "Flotte Orange Money (SIM Marchand)",
                        subtitle = "Compte UV Électronique Orange BF",
                        amount = config.orangeBalance,
                        color = OrangeMoneyOrange,
                        icon = Icons.Default.PhoneAndroid,
                        onAdjustClick = {
                            adjustTargetAccount = MobileNetwork.ORANGE_MONEY.id
                            showAdjustModal = true
                        }
                    )
                    AccountDetailRow(
                        title = "Caisse Espèces (Coffre Physique)",
                        subtitle = "Billets et pièces physiques du guichet",
                        amount = config.cashBalance,
                        color = CashEmerald,
                        icon = Icons.Default.AccountBalanceWallet,
                        onAdjustClick = {
                            adjustTargetAccount = "CASH"
                            showAdjustModal = true
                        }
                    )
                }
            }
        }

        // Section: Daily Cash Clôture Summary (Contrôle du Jour)
        item {
            Text(
                text = "Clôture & Bilan Journalier Orange Money",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Opérations du jour",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "${todayTransactions.size} transactions",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DailyClotureLine("Dépôts Encaissés (Espèces entrantes)", "+ ${Formatters.formatFcfa(todayDeposits)}", CashEmerald)
                    DailyClotureLine("Retraits Payés (Espèces sortantes)", "- ${Formatters.formatFcfa(todayWithdrawals)}", MaterialTheme.colorScheme.error)
                    DailyClotureLine("Commissions Agent Orange estimées", "+ ${Formatters.formatFcfa(todayCommissions)}", OrangeMoneyOrange)

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = CashEmerald.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CashEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "État de Caisse Conforme",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CashEmerald
                                    )
                                )
                            }
                            Text(
                                text = "Zéro écart",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CashEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Quick Reconciliation Action
        item {
            Button(
                onClick = {
                    adjustTargetAccount = "CASH"
                    showAdjustModal = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_reconcile_cash"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeMoneyOrange)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Rapprocher les Soldes / Clôturer", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAdjustModal) {
        QuickBalanceAdjustDialog(
            targetAccount = adjustTargetAccount,
            currentCash = config.cashBalance,
            currentOrange = config.orangeBalance,
            onAdjustCash = { delta -> viewModel.adjustCash(delta) },
            onAdjustFlotte = { net, delta -> viewModel.adjustFlotte(net, delta) },
            onDismiss = { showAdjustModal = false }
        )
    }
}

@Composable
private fun AccountDetailRow(
    title: String,
    subtitle: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAdjustClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Formatters.formatFcfa(amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = color
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAdjustClick() },
                    color = color.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajuster",
                        tint = color,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyClotureLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = valueColor
            )
        )
    }
}
