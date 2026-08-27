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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MobileNetwork
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionCardItem
import com.example.ui.theme.CashEmerald
import com.example.ui.theme.OrangeMoneyDark
import com.example.ui.theme.OrangeMoneyOrange
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.KioskViewModel
import com.example.utils.FeeCalculator
import com.example.utils.Formatters

@Composable
fun DashboardScreen(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val config by viewModel.kioskConfig.collectAsState()
    val allTx by viewModel.allTransactions.collectAsState()
    val clients by viewModel.allClients.collectAsState()

    // Daily statistics calculation
    val todayTx = allTx.filter { Formatters.isToday(it.timestamp) }
    val todayCount = todayTx.size
    val todayDeposits = todayTx.filter { it.type == TransactionType.DEPOT.id }
    val todayDepositSum = todayDeposits.sumOf { it.amount }
    val todayWithdrawals = todayTx.filter { it.type == TransactionType.RETRAIT.id }
    val todayWithdrawalSum = todayWithdrawals.sumOf { it.amount }
    val todayTransfers = todayTx.filter { it.type == TransactionType.TRANSFERT.id }
    val todayTransferSum = todayTransfers.sumOf { it.amount }
    val todayTotalVolume = todayTx.sumOf { it.amount }
    val todayCommissions = todayTx.sumOf { tx ->
        FeeCalculator.calculateAgentCommission(tx.type, tx.amount)
    }

    val totalFlotte = config.orangeBalance
    val totalCapital = config.cashBalance + totalFlotte

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Welcome & Status Banner
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tableau de Bord",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${config.kioskName} • ${config.agentName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Orange Money badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OrangeMoneyOrange.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OrangeMoneyOrange)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Orange Money BF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeMoneyOrange,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Solde Global & Caisse Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = OrangeMoneyOrange.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ACTIF TOTAL DISPONIBLE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OrangeMoneyOrange,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = Formatters.formatFcfa(totalCapital),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Espèces Caisse",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Formatters.formatFcfa(config.cashBalance),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CashEmerald
                                        )
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Flotte OM",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Formatters.formatFcfa(totalFlotte),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OrangeMoneyOrange
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Primary Fast Action: Nouvelle Opération
        item {
            Button(
                onClick = { viewModel.selectTab(AppTab.NEW_TRANSACTION) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_dashboard_new_operation"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeMoneyOrange
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NOUVELLE OPÉRATION ORANGE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )
            }
        }

        // 3. Activity Statistics of the Day (Dépôts, Retraits, Transferts, Commissions)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activité du Jour",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$todayCount opérations traitées",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = OrangeMoneyOrange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dépôts Card
                    StatMiniCard(
                        title = "Dépôts (Cash In)",
                        count = todayDeposits.size,
                        amount = todayDepositSum,
                        icon = Icons.Default.ArrowDownward,
                        color = CashEmerald,
                        modifier = Modifier.weight(1f)
                    )

                    // Retraits Card
                    StatMiniCard(
                        title = "Retraits (Cash Out)",
                        count = todayWithdrawals.size,
                        amount = todayWithdrawalSum,
                        icon = Icons.Default.ArrowUpward,
                        color = OrangeMoneyOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Transferts Card
                    StatMiniCard(
                        title = "Transferts",
                        count = todayTransfers.size,
                        amount = todayTransferSum,
                        icon = Icons.Default.Send,
                        color = OrangeMoneyDark,
                        modifier = Modifier.weight(1f)
                    )

                    // Commissions Agent Card
                    StatMiniCard(
                        title = "Commissions Agent",
                        count = todayCount,
                        amount = todayCommissions,
                        icon = Icons.Default.TrendingUp,
                        color = CashEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Breakdown of Flotte and Cash Summary
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
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trésorerie Kiosque Orange",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gérer Caisse",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OrangeMoneyOrange
                            ),
                            modifier = Modifier.clickable { viewModel.selectTab(AppTab.CAISSE) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccountPill(name = "Flotte OM", amount = config.orangeBalance, color = OrangeMoneyOrange, modifier = Modifier.weight(1f))
                        AccountPill(name = "Caisse Cash", amount = config.cashBalance, color = CashEmerald, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 5. Recent Transactions Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dernières Opérations",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { viewModel.selectTab(AppTab.JOURNAL) }) {
                    Text(
                        text = "Voir Tout",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = OrangeMoneyOrange
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = OrangeMoneyOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (allTx.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aucune transaction enregistrée",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val recentFive = allTx.take(4)
            items(recentFive, key = { it.id }) { tx ->
                TransactionCardItem(
                    transaction = tx,
                    onViewPhoto = { path -> viewModel.openPhotoPreview(path) },
                    onShareReceipt = { transaction ->
                        val receipt = com.example.utils.ExportHelper.generateReceiptText(
                            kioskName = config.kioskName,
                            agentPhone = config.agentPhone,
                            tx = transaction
                        )
                        com.example.utils.ExportHelper.shareReceipt(context, receipt)
                    },
                    onDelete = { viewModel.deleteTransaction(tx) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    count: Int,
    amount: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "$count op.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = Formatters.formatFcfa(amount),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AccountPill(
    name: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = Formatters.formatFcfa(amount),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}
