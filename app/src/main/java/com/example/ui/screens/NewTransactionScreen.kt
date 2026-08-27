package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MobileNetwork
import com.example.data.model.TransactionType
import com.example.ui.components.AmountQuickPresets
import com.example.ui.components.ClientPickerModal
import com.example.ui.components.IdCardCaptureSection
import com.example.ui.components.TransactionTypeSelector
import com.example.ui.theme.CashEmerald
import com.example.ui.theme.OrangeMoneyDark
import com.example.ui.theme.OrangeMoneyOrange
import com.example.ui.viewmodel.KioskViewModel
import com.example.utils.FeeCalculator
import com.example.utils.Formatters

@Composable
fun NewTransactionScreen(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsState()
    val kioskConfig by viewModel.kioskConfig.collectAsState()
    val allClients by viewModel.allClients.collectAsState()
    val showClientPicker by viewModel.showClientPickerModal.collectAsState()
    val lastParsedSms by viewModel.lastParsedSms.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    val currentFlotte = kioskConfig.orangeBalance
    val currentCash = kioskConfig.cashBalance

    val amount = formState.amountStr.toDoubleOrNull() ?: 0.0
    val fee = formState.feeStr.toDoubleOrNull() ?: 0.0
    val isCashIn = formState.type == TransactionType.DEPOT.id ||
            formState.type == TransactionType.TRANSFERT.id ||
            formState.type == TransactionType.FACTURE.id ||
            formState.type == TransactionType.CREDIT.id

    val simulatedFlotteAfter = if (isCashIn) currentFlotte - amount else currentFlotte + (amount + fee)
    val simulatedCashAfter = if (isCashIn) currentCash + (amount + fee) else currentCash - amount

    val tariffInfo = FeeCalculator.getTariffDetails(formState.type, amount)
    val agentCommission = FeeCalculator.calculateAgentCommission(formState.type, amount)

    if (showClientPicker) {
        ClientPickerModal(
            clients = allClients,
            onClientSelected = { client -> viewModel.selectClientForTransaction(client) },
            onDismiss = { viewModel.closeClientPicker() }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("new_transaction_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Title
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Opération Orange Money",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Saisie rapide : Photo CNIB → OCR → SMS Orange → Commission",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = OrangeMoneyOrange
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OrangeMoneyOrange.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            text = "Orange BF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OrangeMoneyOrange,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // BANNIÈRE : LECTURE AUTOMATIQUE SMS ORANGE
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = OrangeMoneyOrange.copy(alpha = 0.09f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(OrangeMoneyOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Lecture SMS Orange Money",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Remplissage auto via SMS de confirmation",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = OrangeMoneyOrange
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    viewModel.parseAndApplySmsText(clip)
                                } else {
                                    viewModel.openSmsModal()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_quick_paste_sms_banner"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = OrangeMoneyOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Coller SMS reçu",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.openSmsModal() },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_open_sms_reader_modal"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = OrangeMoneyOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ouvrir Lecteur SMS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OrangeMoneyOrange
                                )
                            )
                        }
                    }

                    // Status notification if a SMS was recently decoded
                    if (lastParsedSms != null && lastParsedSms?.isValidOrangeSms == true) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CashEmerald.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = CashEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SMS validé (Réf: ${lastParsedSms?.reference ?: "OK"})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CashEmerald
                                        )
                                    )
                                }

                                if (lastParsedSms?.newBalance != null) {
                                    Text(
                                        text = "Solde: ${Formatters.formatAmountShort(lastParsedSms?.newBalance ?: 0.0)} F",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CashEmerald
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // ÉTAPE 1 : Type d'Opération Orange Money
        // ==========================================
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
                    StepHeader(
                        stepNumber = "1",
                        title = "Type d'Opération Orange Money"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TransactionTypeSelector(
                        selectedType = formState.type,
                        onTypeSelected = { viewModel.updateType(it) }
                    )
                }
            }
        }

        // ==========================================
        // ÉTAPE 2 : Identification du Client & Photo CNIB
        // ==========================================
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
                        StepHeader(
                            stepNumber = "2",
                            title = "Pièce d'Identité (CNIB / Passeport)"
                        )

                        OutlinedButton(
                            onClick = { viewModel.openClientPicker() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_quick_pick_client")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = OrangeMoneyOrange
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Client existant",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = OrangeMoneyOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    IdCardCaptureSection(
                        photoPath = formState.idPhotoPath,
                        isScanning = formState.isScanningId,
                        statusMessage = formState.scanStatusMessage,
                        onPhotoSelected = { uri -> viewModel.scanIdPhoto(uri) },
                        onRemovePhoto = { viewModel.clearIdPhoto() },
                        onViewPhoto = { path -> viewModel.openPhotoPreview(path) }
                    )
                }
            }
        }

        // ==========================================
        // ÉTAPE 3 : Extraction Automatique OCR & Vérification
        // ==========================================
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
                    StepHeader(
                        stepNumber = "3",
                        title = "Vérification des Données OCR"
                    )

                    // Mandatory verification reminder
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = OrangeMoneyOrange.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OrangeMoneyOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Vérification requise : Les données sont extraites par OCR. Vous pouvez les corriger manuellement avant d'enregistrer.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    // Client Full Name
                    OutlinedTextField(
                        value = formState.clientName,
                        onValueChange = { viewModel.updateClientName(it) },
                        label = { Text("Nom et Prénoms du client") },
                        placeholder = { Text("ex: OUEDRAOGO Abdoul") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // ID Number (CNIB)
                    OutlinedTextField(
                        value = formState.idNumber,
                        onValueChange = { viewModel.updateIdNumber(it) },
                        label = { Text("Numéro CNIB / Passeport / Permis *") },
                        placeholder = { Text("ex: B12345678") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = OrangeMoneyOrange)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_id_number"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Birth Date
                    OutlinedTextField(
                        value = formState.birthDate,
                        onValueChange = { viewModel.updateBirthDate(it) },
                        label = { Text("Date de Naissance (Optionnel)") },
                        placeholder = { Text("ex: 15/08/1990") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Cake, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_birth_date"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }

        // ==========================================
        // ÉTAPE 4 : Téléphone, Montant & Barème Orange Money
        // ==========================================
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
                    StepHeader(
                        stepNumber = "4",
                        title = "Montant, Frais & Commission Orange BF"
                    )

                    // Client Phone Number
                    OutlinedTextField(
                        value = formState.clientPhone,
                        onValueChange = { viewModel.updateClientPhone(it) },
                        label = { Text("N° Orange Money Client (Burkina) *") },
                        placeholder = { Text("ex: 70 12 34 56") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = OrangeMoneyOrange)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_phone"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Recipient Phone if Transfer
                    if (formState.type == TransactionType.TRANSFERT.id) {
                        OutlinedTextField(
                            value = formState.recipientPhone,
                            onValueChange = { viewModel.updateRecipientPhone(it) },
                            label = { Text("N° Destinataire / Bénéficiaire *") },
                            placeholder = { Text("ex: 76 00 11 22") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_recipient_phone"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Amount Input
                    OutlinedTextField(
                        value = formState.amountStr,
                        onValueChange = { viewModel.updateAmount(it) },
                        label = { Text("Montant de l'opération *") },
                        placeholder = { Text("0") },
                        trailingIcon = {
                            Text(
                                text = "FCFA",
                                fontWeight = FontWeight.Bold,
                                color = OrangeMoneyOrange,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_amount"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Quick amount presets
                    AmountQuickPresets(
                        onPresetSelected = { viewModel.applyAmountPreset(it) }
                    )

                    // Official Orange Money Tariff & Commission Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = OrangeMoneyOrange.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Paid,
                                        contentDescription = null,
                                        tint = OrangeMoneyOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Barème Officiel Orange Money BF",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OrangeMoneyOrange
                                        )
                                    )
                                }

                                Text(
                                    text = tariffInfo.bracketLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Frais Client Facturés:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Formatters.formatFcfa(fee),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = CashEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Gain Commission Agent:",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = CashEmerald
                                        )
                                    }
                                    Text(
                                        text = "+ ${Formatters.formatFcfa(agentCommission)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CashEmerald
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // SMS Reference code with direct paste/scanner
                    OutlinedTextField(
                        value = formState.referenceCode,
                        onValueChange = { viewModel.updateReferenceCode(it) },
                        label = { Text("N° Référence SMS Transaction (Optionnel)") },
                        placeholder = { Text("ex: CI240827.1420.A01234") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val clip = clipboardManager.getText()?.text
                                        if (!clip.isNullOrBlank()) {
                                            viewModel.parseAndApplySmsText(clip)
                                        } else {
                                            viewModel.openSmsModal()
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_paste_ref_from_sms")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Coller SMS",
                                        tint = OrangeMoneyOrange
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_ref_code"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Dynamic Balance evolution tracker
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Évolution automatique des soldes",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Flotte Orange Money:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${Formatters.formatAmountShort(currentFlotte)} → ${Formatters.formatAmountShort(simulatedFlotteAfter)} F",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (simulatedFlotteAfter < 0) MaterialTheme.colorScheme.error else OrangeMoneyOrange
                                        )
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Caisse Espèces:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${Formatters.formatAmountShort(currentCash)} → ${Formatters.formatAmountShort(simulatedCashAfter)} F",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CashEmerald
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error message banner
        if (formState.errorMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // ÉTAPE 5 : Validation & Enregistrement
        // ==========================================
        item {
            Button(
                onClick = { viewModel.submitTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("btn_submit_transaction"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeMoneyOrange
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VALIDER L'OPÉRATION & ENREGISTRER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StepHeader(
    stepNumber: String,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(OrangeMoneyOrange),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
