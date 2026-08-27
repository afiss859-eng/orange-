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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.viewmodel.KioskViewModel
import com.example.utils.ExportHelper
import com.example.utils.FeeCalculator
import com.example.utils.Formatters

@Composable
fun JournalScreen(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.filteredTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()

    // Calculated counters for active filtered view
    val totalVolume = transactions.sumOf { it.amount }
    val totalCommissions = transactions.sumOf { tx ->
        FeeCalculator.calculateAgentCommission(tx.type, tx.amount)
    }
    val totalDeposits = transactions.filter { it.type == TransactionType.DEPOT.id || it.type == TransactionType.TRANSFERT.id }.sumOf { it.amount }
    val totalWithdrawals = transactions.filter { it.type == TransactionType.RETRAIT.id }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("journal_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "Registre Orange Money",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${transactions.size} transaction(s) Orange enregistrée(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Export CSV Button
                FilledTonalButton(
                    onClick = {
                        val csvFile = ExportHelper.generateCsvFile(context, transactions)
                        ExportHelper.shareCsvFile(context, csvFile)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_export_csv")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Exporter CSV",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary KPI Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            text = "Bilan des Opérations",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = CashEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Commissions Agent: +${Formatters.formatFcfa(totalCommissions)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CashEmerald,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiStatCard("Dépôts Encaissés", totalDeposits, CashEmerald, Modifier.weight(1f))
                        KpiStatCard("Retraits Servis", totalWithdrawals, OrangeMoneyOrange, Modifier.weight(1f))
                        KpiStatCard("Volume Global", totalVolume, OrangeMoneyDark, Modifier.weight(1f))
                    }
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Rechercher client, N° tél, CNIB, Réf...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Recherche")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_journal"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Date Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DateFilterChip("TODAY", "Aujourd'hui", dateFilter == "TODAY", Modifier.weight(1f)) {
                    viewModel.setDateFilter("TODAY")
                }
                DateFilterChip("YESTERDAY", "Hier", dateFilter == "YESTERDAY", Modifier.weight(1f)) {
                    viewModel.setDateFilter("YESTERDAY")
                }
                DateFilterChip("WEEK", "7 Jours", dateFilter == "WEEK", Modifier.weight(1f)) {
                    viewModel.setDateFilter("WEEK")
                }
                DateFilterChip("ALL", "Tout", dateFilter == "ALL", Modifier.weight(1f)) {
                    viewModel.setDateFilter("ALL")
                }
            }
        }

        // Operation Type Filters Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    SmallFilterPill(
                        label = "Toutes opérations",
                        isSelected = typeFilter == null,
                        onClick = { viewModel.setTypeFilter(null) }
                    )
                }
                item {
                    SmallFilterPill(
                        label = "Retraits (Cash-out)",
                        isSelected = typeFilter == TransactionType.RETRAIT.id,
                        onClick = {
                            viewModel.setTypeFilter(if (typeFilter == TransactionType.RETRAIT.id) null else TransactionType.RETRAIT.id)
                        }
                    )
                }
                item {
                    SmallFilterPill(
                        label = "Dépôts (Cash-in)",
                        isSelected = typeFilter == TransactionType.DEPOT.id,
                        onClick = {
                            viewModel.setTypeFilter(if (typeFilter == TransactionType.DEPOT.id) null else TransactionType.DEPOT.id)
                        }
                    )
                }
                item {
                    SmallFilterPill(
                        label = "Transferts",
                        isSelected = typeFilter == TransactionType.TRANSFERT.id,
                        onClick = {
                            viewModel.setTypeFilter(if (typeFilter == TransactionType.TRANSFERT.id) null else TransactionType.TRANSFERT.id)
                        }
                    )
                }
                item {
                    SmallFilterPill(
                        label = "Factures",
                        isSelected = typeFilter == TransactionType.FACTURE.id,
                        onClick = {
                            viewModel.setTypeFilter(if (typeFilter == TransactionType.FACTURE.id) null else TransactionType.FACTURE.id)
                        }
                    )
                }
                item {
                    SmallFilterPill(
                        label = "Crédit IAP",
                        isSelected = typeFilter == TransactionType.CREDIT.id,
                        onClick = {
                            viewModel.setTypeFilter(if (typeFilter == TransactionType.CREDIT.id) null else TransactionType.CREDIT.id)
                        }
                    )
                }
            }
        }

        // Transactions List
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Aucune opération trouvée",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Modifiez vos filtres ou effectuez une nouvelle opération.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionCardItem(
                    transaction = tx,
                    onViewPhoto = { path -> viewModel.openPhotoPreview(path) },
                    onShareReceipt = { transaction ->
                        val receipt = ExportHelper.generateReceiptText(
                            kioskName = viewModel.kioskConfig.value.kioskName,
                            agentPhone = viewModel.kioskConfig.value.agentPhone,
                            tx = transaction
                        )
                        ExportHelper.shareReceipt(context, receipt)
                    },
                    onDelete = { viewModel.deleteTransaction(it) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    amount: Double,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = Formatters.formatFcfa(amount),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = accentColor
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DateFilterChip(
    id: String,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("filter_date_$id"),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) OrangeMoneyOrange else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) OrangeMoneyOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SmallFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) OrangeMoneyDark else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) OrangeMoneyDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
