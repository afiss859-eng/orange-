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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.CashEmerald
import com.example.ui.theme.OrangeMoneyOrange
import com.example.ui.viewmodel.KioskViewModel
import com.example.utils.ExportHelper
import com.example.utils.Formatters

@Composable
fun AdminSettingsScreen(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.kioskConfig.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val auditLogs by viewModel.allAuditLogs.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    var kioskName by remember(config.kioskName) { mutableStateOf(config.kioskName) }
    var agentName by remember(config.agentName) { mutableStateOf(config.agentName) }
    var agentPhone by remember(config.agentPhone) { mutableStateOf(config.agentPhone) }
    var location by remember(config.location) { mutableStateOf(config.location) }
    var customAdminTitle by remember(config.customAdminTitle) { mutableStateOf(config.customAdminTitle) }
    var adminPinCode by remember(config.adminPinCode) { mutableStateOf(config.adminPinCode) }
    var autoFee by remember(config.autoFeeCalculation) { mutableStateOf(config.autoFeeCalculation) }
    var requireIdPhoto by remember(config.requireIdPhoto) { mutableStateOf(config.requireIdPhoto) }
    var deletePhotoAfterScan by remember(config.deletePhotoAfterScan) { mutableStateOf(config.deletePhotoAfterScan) }
    var pinLockEnabled by remember(config.pinLockEnabled) { mutableStateOf(config.pinLockEnabled) }

    var isSavedToast by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }

    if (showAddUserDialog) {
        AddUserModal(
            onDismiss = { showAddUserDialog = false },
            onUserCreated = { newUser ->
                viewModel.createNewUser(newUser)
                showAddUserDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_settings_screen"),
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
                        text = customAdminTitle.ifBlank { "Administration & Gérance" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Contrôle des agents, sécurité locale et traçabilité",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OrangeMoneyOrange.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = config.currentRole.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OrangeMoneyOrange,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 1. Rôle actif & Multi-Profils
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = OrangeMoneyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Profil et Rôle Actuel",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = config.currentRole == role
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.switchUserRole(role) },
                                color = if (isSelected) OrangeMoneyOrange else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = role.displayName.split(" ")[0],
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Gestion des Agents & Utilisateurs
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = OrangeMoneyOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gestion des Agents (${users.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedButton(
                            onClick = { showAddUserDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_add_user")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ajouter", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    users.forEach { user ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(OrangeMoneyOrange.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = OrangeMoneyOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = user.fullName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${user.role.displayName} • Tél: ${user.phone.ifBlank { "Non renseigné" }}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (users.size > 1 && user.role != UserRole.ADMIN) {
                                    IconButton(
                                        onClick = { viewModel.removeUser(user) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Supprimer",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Paramètres Kiosque & Personnalisation Titre
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = OrangeMoneyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Paramètres Agence / Kiosque",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = kioskName,
                        onValueChange = { kioskName = it; isSavedToast = false },
                        label = { Text("Nom du Kiosque / Point de vente") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_kiosk_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = agentName,
                        onValueChange = { agentName = it; isSavedToast = false },
                        label = { Text("Nom du Gérant / Agent Principal") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_agent_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = agentPhone,
                        onValueChange = { agentPhone = it; isSavedToast = false },
                        label = { Text("Téléphone Contact Agence") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_phone"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it; isSavedToast = false },
                        label = { Text("Emplacement (Ville, Quartier, Secteur)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_location"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Nom personnalisé pour la page admin (comme demandé explicitement par l'utilisateur !)
                    OutlinedTextField(
                        value = customAdminTitle,
                        onValueChange = { customAdminTitle = it; isSavedToast = false },
                        label = { Text("Titre Personnalisé de la Page Admin") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_custom_title"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }

        // 4. Confidentialité, Sécurité & Code PIN
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = OrangeMoneyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sécurité & Confidentialité des Données",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Delete photo after extraction option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Supprimer la photo après extraction",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Ne conserve que les données textuelles validées pour libérer l'espace et protéger la vie privée.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = deletePhotoAfterScan,
                            onCheckedChange = { deletePhotoAfterScan = it; isSavedToast = false },
                            colors = SwitchDefaults.colors(checkedThumbColor = OrangeMoneyOrange, checkedTrackColor = OrangeMoneyOrange.copy(alpha = 0.4f))
                        )
                    }

                    // Calcul automatique des commissions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Calcul automatique des commissions",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Applique automatiquement les grilles tarifaires et commissions officielles Orange Money Burkina.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoFee,
                            onCheckedChange = { autoFee = it; isSavedToast = false },
                            colors = SwitchDefaults.colors(checkedThumbColor = OrangeMoneyOrange, checkedTrackColor = OrangeMoneyOrange.copy(alpha = 0.4f))
                        )
                    }

                    // Code PIN Administrateur
                    OutlinedTextField(
                        value = adminPinCode,
                        onValueChange = { if (it.length <= 6) { adminPinCode = it; isSavedToast = false } },
                        label = { Text("Code PIN Administrateur (4 chiffres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_pin"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }

        // 5. Journal d'Audit & Traçabilité des Actions
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = OrangeMoneyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Journal d'Audit & Sécurité (${auditLogs.size} logs)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (auditLogs.isEmpty()) {
                        Text(
                            text = "Aucun événement d'audit enregistré.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        auditLogs.take(5).forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${log.actionType} • ${log.actorName}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = OrangeMoneyOrange,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = Formatters.formatTime(log.timestamp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    Text(
                                        text = log.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Sauvegarde des Paramètres Button
        item {
            Button(
                onClick = {
                    val updated = config.copy(
                        kioskName = kioskName.trim(),
                        agentName = agentName.trim(),
                        agentPhone = agentPhone.trim(),
                        location = location.trim(),
                        customAdminTitle = customAdminTitle.trim(),
                        adminPinCode = adminPinCode.trim(),
                        autoFeeCalculation = autoFee,
                        requireIdPhoto = requireIdPhoto,
                        deletePhotoAfterScan = deletePhotoAfterScan,
                        pinLockEnabled = pinLockEnabled
                    )
                    viewModel.updateKioskConfig(updated)
                    isSavedToast = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_admin_settings"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeMoneyOrange)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSavedToast) "PARAMÈTRES ENREGISTRÉS !" else "ENREGISTRER LES MODIFICATIONS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                )
            }
        }

        // 7. Export CSV Global
        item {
            OutlinedButton(
                onClick = {
                    val file = ExportHelper.generateCsvFile(context, allTransactions)
                    ExportHelper.shareCsvFile(context, file)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_export_csv_admin"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = OrangeMoneyOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exporter le Registre Complet (CSV Excel)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OrangeMoneyOrange)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AddUserModal(
    onDismiss: () -> Unit,
    onUserCreated: (UserEntity) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.AGENT) }
    var pinCode by remember { mutableStateOf("1234") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ajouter un Agent / Caissier",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nom et Prénoms") },
                    placeholder = { Text("ex: Sawadogo Eric") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("N° Téléphone") },
                    placeholder = { Text("ex: 70 00 00 00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text(
                    text = "Rôle & Permissions :",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UserRole.values().forEach { r ->
                        val isSel = role == r
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { role = r },
                            color = if (isSel) OrangeMoneyOrange else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = r.displayName.split(" ")[0],
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { if (it.length <= 4) pinCode = it },
                    label = { Text("Code PIN Initial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fullName.isNotBlank()) {
                                onUserCreated(
                                    UserEntity(
                                        username = fullName.lowercase().replace(" ", "_"),
                                        fullName = fullName.trim(),
                                        phone = phone.trim(),
                                        role = role,
                                        pinCode = pinCode
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeMoneyOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Créer le compte")
                    }
                }
            }
        }
    }
}
