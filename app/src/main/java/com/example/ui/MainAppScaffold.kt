package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.IdPhotoViewerModal
import com.example.ui.components.KioskTopHeader
import com.example.ui.components.OrangeSmsModal
import com.example.ui.components.QuickBalanceAdjustDialog
import com.example.ui.components.ReceiptShareModal
import com.example.ui.screens.AdminSettingsScreen
import com.example.ui.screens.CaisseScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.NewTransactionScreen
import com.example.ui.theme.OrangeMoneyOrange
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.KioskViewModel

@Composable
fun MainAppScaffold(
    viewModel: KioskViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val kioskConfig by viewModel.kioskConfig.collectAsState()
    val lastSavedTx by viewModel.lastSavedTransaction.collectAsState()
    val showReceiptDialog by viewModel.showReceiptDialog.collectAsState()
    val previewPhotoPath by viewModel.previewPhotoPath.collectAsState()
    val showSmsModal by viewModel.showSmsModal.collectAsState()
    val allTx by viewModel.allTransactions.collectAsState()

    var showQuickAdjustDialog by remember { mutableStateOf(false) }
    var adjustTargetAccount by remember { mutableStateOf<String?>("CASH") }

    Scaffold(
        topBar = {
            KioskTopHeader(
                config = kioskConfig,
                onAdjustBalanceClick = { account ->
                    adjustTargetAccount = account ?: "CASH"
                    showQuickAdjustDialog = true
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("app_bottom_nav")
            ) {
                // Tab 1: Dashboard
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.selectTab(AppTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Accueil",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Accueil",
                            fontWeight = if (currentTab == AppTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeMoneyOrange,
                        selectedTextColor = OrangeMoneyOrange,
                        indicatorColor = OrangeMoneyOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )

                // Tab 2: New Transaction
                NavigationBarItem(
                    selected = currentTab == AppTab.NEW_TRANSACTION,
                    onClick = { viewModel.selectTab(AppTab.NEW_TRANSACTION) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.NEW_TRANSACTION) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                            contentDescription = "Opération",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Opération",
                            fontWeight = if (currentTab == AppTab.NEW_TRANSACTION) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeMoneyOrange,
                        selectedTextColor = OrangeMoneyOrange,
                        indicatorColor = OrangeMoneyOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_new_tx")
                )

                // Tab 3: Journal
                NavigationBarItem(
                    selected = currentTab == AppTab.JOURNAL,
                    onClick = { viewModel.selectTab(AppTab.JOURNAL) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (allTx.isNotEmpty()) {
                                    Badge(
                                        containerColor = OrangeMoneyOrange,
                                        contentColor = Color.White
                                    ) {
                                        Text("${allTx.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.JOURNAL) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Registre",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Registre",
                            fontWeight = if (currentTab == AppTab.JOURNAL) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeMoneyOrange,
                        selectedTextColor = OrangeMoneyOrange,
                        indicatorColor = OrangeMoneyOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_journal")
                )

                // Tab 4: Caisse & Soldes
                NavigationBarItem(
                    selected = currentTab == AppTab.CAISSE,
                    onClick = { viewModel.selectTab(AppTab.CAISSE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CAISSE) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Caisse",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Caisse",
                            fontWeight = if (currentTab == AppTab.CAISSE) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeMoneyOrange,
                        selectedTextColor = OrangeMoneyOrange,
                        indicatorColor = OrangeMoneyOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_caisse")
                )

                // Tab 5: Clients VIP
                NavigationBarItem(
                    selected = currentTab == AppTab.CLIENTS,
                    onClick = { viewModel.selectTab(AppTab.CLIENTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CLIENTS) Icons.Filled.People else Icons.Outlined.PeopleOutline,
                            contentDescription = "Clients",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Clients",
                            fontWeight = if (currentTab == AppTab.CLIENTS) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeMoneyOrange,
                        selectedTextColor = OrangeMoneyOrange,
                        indicatorColor = OrangeMoneyOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_clients")
                )

                // Tab 6: Admin / Paramètres
                NavigationBarItem(
                    selected = currentTab == AppTab.ADMIN,
                    onClick = { viewModel.selectTab(AppTab.ADMIN) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Admin",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Admin",
                            fontWeight = if (currentTab == AppTab.ADMIN) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeMoneyOrange,
                        selectedTextColor = OrangeMoneyOrange,
                        indicatorColor = OrangeMoneyOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_admin")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppTab.NEW_TRANSACTION -> NewTransactionScreen(viewModel = viewModel)
                AppTab.JOURNAL -> JournalScreen(viewModel = viewModel)
                AppTab.CAISSE -> CaisseScreen(viewModel = viewModel)
                AppTab.CLIENTS -> ClientsScreen(viewModel = viewModel)
                AppTab.ADMIN -> AdminSettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal: Receipt Share popup after saving transaction
    if (showReceiptDialog && lastSavedTx != null) {
        ReceiptShareModal(
            transaction = lastSavedTx,
            config = kioskConfig,
            onDismiss = { viewModel.dismissReceiptDialog() }
        )
    }

    // Modal: Zoom photo of CNIB
    if (previewPhotoPath != null) {
        IdPhotoViewerModal(
            photoPath = previewPhotoPath,
            onDismiss = { viewModel.closePhotoPreview() }
        )
    }

    // Modal: Orange Money SMS Reader & Balance Validator
    if (showSmsModal) {
        OrangeSmsModal(
            onDismiss = { viewModel.closeSmsModal() },
            onApplyToTransaction = { parsedSms -> viewModel.applyParsedSms(parsedSms) },
            onSyncFlotteBalance = { newBalance, ref -> viewModel.syncFlotteBalanceFromSms(newBalance, ref) }
        )
    }

    // Modal: Quick balance adjustment
    if (showQuickAdjustDialog) {
        QuickBalanceAdjustDialog(
            targetAccount = adjustTargetAccount,
            currentCash = kioskConfig.cashBalance,
            currentOrange = kioskConfig.orangeBalance,
            onAdjustCash = { delta -> viewModel.adjustCash(delta) },
            onAdjustFlotte = { net, delta -> viewModel.adjustFlotte(net, delta) },
            onDismiss = { showQuickAdjustDialog = false }
        )
    }
}
