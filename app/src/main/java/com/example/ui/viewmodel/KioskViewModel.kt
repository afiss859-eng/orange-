package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.ClientEntity
import com.example.data.model.MobileNetwork
import com.example.data.model.SyncStatus
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.ocr.IdCardScannerHelper
import com.example.data.repository.KioskConfig
import com.example.data.repository.KioskPreferences
import com.example.data.repository.KioskRepository
import com.example.utils.FeeCalculator
import com.example.utils.Formatters
import com.example.utils.OrangeMoneySmsParser
import com.example.utils.ParsedOrangeSms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class AppTab(val title: String, val testTag: String) {
    DASHBOARD("Accueil", "tab_dashboard"),
    NEW_TRANSACTION("Opération", "tab_new_tx"),
    JOURNAL("Registre", "tab_journal"),
    CAISSE("Caisse", "tab_caisse"),
    CLIENTS("Clients", "tab_clients"),
    ADMIN("Admin", "tab_admin")
}

data class TransactionFormState(
    val network: String = MobileNetwork.ORANGE_MONEY.id,
    val type: String = TransactionType.RETRAIT.id,
    val amountStr: String = "",
    val feeStr: String = "",
    val clientPhone: String = "",
    val recipientPhone: String = "",
    val clientName: String = "",
    val birthDate: String = "",
    val idType: String = "CNIB",
    val idNumber: String = "",
    val idPhotoPath: String? = null,
    val referenceCode: String = "",
    val notes: String = "",
    val isScanningId: Boolean = false,
    val isOcrDetected: Boolean = false,
    val scanStatusMessage: String? = null,
    val errorMessage: String? = null
)

class KioskViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val preferences = KioskPreferences(application)
    val repository = KioskRepository(
        db.transactionDao(),
        db.clientDao(),
        db.userDao(),
        db.auditLogDao(),
        preferences
    )

    val kioskConfig: StateFlow<KioskConfig> = preferences.config

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _dateFilter = MutableStateFlow("TODAY") // TODAY, YESTERDAY, WEEK, ALL
    val dateFilter: StateFlow<String> = _dateFilter.asStateFlow()

    private val _networkFilter = MutableStateFlow<String?>(null)
    val networkFilter: StateFlow<String?> = _networkFilter.asStateFlow()

    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter.asStateFlow()

    private val _lastSavedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val lastSavedTransaction: StateFlow<TransactionEntity?> = _lastSavedTransaction.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()

    private val _previewPhotoPath = MutableStateFlow<String?>(null)
    val previewPhotoPath: StateFlow<String?> = _previewPhotoPath.asStateFlow()

    private val _showClientPickerModal = MutableStateFlow(false)
    val showClientPickerModal: StateFlow<Boolean> = _showClientPickerModal.asStateFlow()

    private val _showSmsModal = MutableStateFlow(false)
    val showSmsModal: StateFlow<Boolean> = _showSmsModal.asStateFlow()

    private val _lastParsedSms = MutableStateFlow<ParsedOrangeSms?>(null)
    val lastParsedSms: StateFlow<ParsedOrangeSms?> = _lastParsedSms.asStateFlow()

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClients: StateFlow<List<ClientEntity>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _searchQuery,
        _dateFilter,
        _networkFilter,
        _typeFilter
    ) { txList, query, dateFil, netFil, typeFil ->
        var list = txList

        // Date filter
        list = when (dateFil) {
            "TODAY" -> list.filter { Formatters.isToday(it.timestamp) }
            "YESTERDAY" -> list.filter { Formatters.isYesterday(it.timestamp) }
            "WEEK" -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }
                list.filter { it.timestamp >= cal.timeInMillis }
            }
            else -> list
        }

        // Network filter
        if (!netFil.isNullOrBlank()) {
            list = list.filter { it.network == netFil }
        }

        // Type filter
        if (!typeFil.isNullOrBlank()) {
            list = list.filter { it.type == typeFil }
        }

        // Search text
        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            list = list.filter {
                it.clientPhone.contains(q) ||
                it.clientName.lowercase().contains(q) ||
                it.idNumber.lowercase().contains(q) ||
                it.referenceCode.lowercase().contains(q) ||
                it.recipientPhone.contains(q)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.allTransactions.collect { list ->
                if (list.isEmpty()) {
                    seedDemoData()
                }
            }
        }
    }

    private suspend fun seedDemoData() {
        val now = System.currentTimeMillis()
        val sample1 = TransactionEntity(
            timestamp = now - 25 * 60 * 1000,
            network = MobileNetwork.ORANGE_MONEY.id,
            type = TransactionType.RETRAIT.id,
            amount = 25000.0,
            fee = 1000.0,
            clientPhone = "70123456",
            clientName = "OUEDRAOGO Abdoul",
            idType = "CNIB",
            idNumber = "B12874932",
            birthDate = "14/05/1992",
            kioskBalanceBefore = 650000.0,
            kioskBalanceAfter = 676000.0,
            cashBalanceBefore = 350000.0,
            cashBalanceAfter = 325000.0,
            referenceCode = "OM-982341",
            notes = "Retrait espèces régulier",
            syncStatus = SyncStatus.OFFLINE.name
        )
        val sample2 = TransactionEntity(
            timestamp = now - 85 * 60 * 1000,
            network = MobileNetwork.ORANGE_MONEY.id,
            type = TransactionType.DEPOT.id,
            amount = 50000.0,
            fee = 0.0,
            clientPhone = "76987654",
            recipientPhone = "70223344",
            clientName = "KABORE Fatimata",
            idType = "CNIB",
            idNumber = "B09341255",
            birthDate = "22/11/1988",
            kioskBalanceBefore = 700000.0,
            kioskBalanceAfter = 650000.0,
            cashBalanceBefore = 300000.0,
            cashBalanceAfter = 350000.0,
            referenceCode = "OM-443190",
            notes = "Dépôt compte Orange Money",
            syncStatus = SyncStatus.OFFLINE.name
        )
        val sample3 = TransactionEntity(
            timestamp = now - 180 * 60 * 1000,
            network = MobileNetwork.ORANGE_MONEY.id,
            type = TransactionType.TRANSFERT.id,
            amount = 15000.0,
            fee = 400.0,
            clientPhone = "75554433",
            recipientPhone = "70990011",
            clientName = "TRAORE Ibrahim",
            idType = "PASSEPORT",
            idNumber = "PA098124",
            birthDate = "03/08/1995",
            kioskBalanceBefore = 715000.0,
            kioskBalanceAfter = 700000.0,
            cashBalanceBefore = 284600.0,
            cashBalanceAfter = 300000.0,
            referenceCode = "OM-771239",
            notes = "Transfert Orange Money vers Koudougou",
            syncStatus = SyncStatus.OFFLINE.name
        )
        repository.saveTransaction(sample1, updateKioskBalances = false)
        repository.saveTransaction(sample2, updateKioskBalances = false)
        repository.saveTransaction(sample3, updateKioskBalances = false)

        // Seed default users if empty
        val user1 = UserEntity(
            username = "admin",
            fullName = "Sawadogo Afis (Propriétaire)",
            phone = "70000000",
            role = UserRole.ADMIN,
            pinCode = "1234"
        )
        val user2 = UserEntity(
            username = "gerant",
            fullName = "Kaboré Paul (Responsable Caisse)",
            phone = "76112233",
            role = UserRole.RESPONSABLE,
            pinCode = "0000"
        )
        val user3 = UserEntity(
            username = "agent1",
            fullName = "Ouédraogo Aminata (Caissière Guichet 1)",
            phone = "78445566",
            role = UserRole.AGENT,
            pinCode = "1111"
        )
        repository.saveUser(user1)
        repository.saveUser(user2)
        repository.saveUser(user3)

        // Seed initial audit log
        repository.logAudit(
            actor = "Système",
            actionType = "INITIALISATION",
            description = "Démarrage du Kiosque Orange Money Burkina",
            details = "Configuration initiale appliquée."
        )
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openClientPicker() {
        _showClientPickerModal.value = true
    }

    fun closeClientPicker() {
        _showClientPickerModal.value = false
    }

    fun updateNetwork(network: String) {
        val current = _formState.value
        val amount = current.amountStr.toDoubleOrNull() ?: 0.0
        val fee = if (amount > 0) FeeCalculator.calculateCustomerFee(current.type, amount) else 0.0
        _formState.value = current.copy(
            network = MobileNetwork.ORANGE_MONEY.id,
            feeStr = if (fee > 0) fee.toInt().toString() else "0"
        )
    }

    fun updateType(type: String) {
        val current = _formState.value
        val amount = current.amountStr.toDoubleOrNull() ?: 0.0
        val fee = if (amount > 0) FeeCalculator.calculateCustomerFee(type, amount) else 0.0
        _formState.value = current.copy(
            type = type,
            feeStr = if (fee > 0) fee.toInt().toString() else "0"
        )
    }

    fun updateAmount(amountStr: String) {
        val filtered = amountStr.filter { it.isDigit() }
        val current = _formState.value
        val amount = filtered.toDoubleOrNull() ?: 0.0
        val fee = if (amount > 0) FeeCalculator.calculateCustomerFee(current.type, amount) else 0.0
        _formState.value = current.copy(
            amountStr = filtered,
            feeStr = if (fee > 0) fee.toInt().toString() else "0",
            errorMessage = null
        )
    }

    fun applyAmountPreset(preset: Long) {
        updateAmount(preset.toString())
    }

    fun updateFee(feeStr: String) {
        val filtered = feeStr.filter { it.isDigit() }
        _formState.value = _formState.value.copy(feeStr = filtered)
    }

    fun updateClientPhone(phone: String) {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        _formState.value = _formState.value.copy(clientPhone = cleanPhone, errorMessage = null)

        // Automatic lookup in VIP client directory
        if (cleanPhone.length >= 8) {
            viewModelScope.launch {
                val client = repository.findClientByPhone(cleanPhone)
                if (client != null) {
                    _formState.value = _formState.value.copy(
                        clientName = if (_formState.value.clientName.isBlank()) client.fullName else _formState.value.clientName,
                        idNumber = if (_formState.value.idNumber.isBlank()) client.idNumber else _formState.value.idNumber,
                        idType = if (_formState.value.idNumber.isBlank()) client.idType else _formState.value.idType,
                        birthDate = if (_formState.value.birthDate.isBlank() && client.birthDate != null) client.birthDate else _formState.value.birthDate,
                        idPhotoPath = _formState.value.idPhotoPath ?: client.idPhotoPath
                    )
                }
            }
        }
    }

    fun updateRecipientPhone(phone: String) {
        _formState.value = _formState.value.copy(recipientPhone = phone)
    }

    fun updateClientName(name: String) {
        _formState.value = _formState.value.copy(clientName = name, errorMessage = null)
    }

    fun updateBirthDate(birthDate: String) {
        _formState.value = _formState.value.copy(birthDate = birthDate)
    }

    fun updateIdType(type: String) {
        _formState.value = _formState.value.copy(idType = type)
    }

    fun updateIdNumber(number: String) {
        _formState.value = _formState.value.copy(idNumber = number.uppercase(), errorMessage = null)
    }

    fun updateReferenceCode(code: String) {
        _formState.value = _formState.value.copy(referenceCode = code)
    }

    fun updateNotes(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    fun clearIdPhoto() {
        _formState.value = _formState.value.copy(idPhotoPath = null, isOcrDetected = false)
    }

    fun scanIdPhoto(uri: Uri) {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(
                isScanningId = true,
                scanStatusMessage = "Analyse & extraction OCR en cours...",
                errorMessage = null
            )

            val scanResult = IdCardScannerHelper.processIdImage(getApplication(), uri)

            if (scanResult.isSuccess) {
                _formState.value = _formState.value.copy(
                    isScanningId = false,
                    isOcrDetected = true,
                    scanStatusMessage = "Extraction réussie ! Vérifiez les données.",
                    idPhotoPath = scanResult.savedImagePath,
                    idNumber = if (scanResult.idNumber.isNotBlank()) scanResult.idNumber else _formState.value.idNumber,
                    clientName = if (scanResult.fullName.isNotBlank()) scanResult.fullName else _formState.value.clientName,
                    birthDate = if (scanResult.birthDate.isNotBlank()) scanResult.birthDate else _formState.value.birthDate,
                    idType = if (scanResult.idType.isNotBlank()) scanResult.idType else _formState.value.idType
                )
            } else {
                _formState.value = _formState.value.copy(
                    isScanningId = false,
                    isOcrDetected = false,
                    scanStatusMessage = null,
                    idPhotoPath = scanResult.savedImagePath.ifBlank { null },
                    errorMessage = scanResult.errorMessage ?: "Échec du scan OCR. Saisissez manuellement."
                )
            }
        }
    }

    fun openSmsModal() {
        _showSmsModal.value = true
    }

    fun closeSmsModal() {
        _showSmsModal.value = false
    }

    fun parseAndApplySmsText(smsText: String) {
        val parsed = OrangeMoneySmsParser.parse(smsText)
        applyParsedSms(parsed)
    }

    fun applyParsedSms(parsed: ParsedOrangeSms) {
        _lastParsedSms.value = parsed
        val current = _formState.value

        val newType = parsed.detectedType?.id ?: current.type
        val newAmount = parsed.amount?.toInt()?.toString() ?: current.amountStr
        val calculatedFee = if (parsed.amount != null && parsed.amount > 0) {
            parsed.fee?.toInt()?.toString() ?: FeeCalculator.calculateCustomerFee(newType, parsed.amount).toInt().toString()
        } else {
            current.feeStr
        }

        _formState.value = current.copy(
            type = newType,
            amountStr = newAmount,
            feeStr = calculatedFee,
            referenceCode = parsed.reference ?: current.referenceCode,
            clientPhone = parsed.clientPhone ?: current.clientPhone,
            clientName = parsed.clientName ?: current.clientName,
            errorMessage = null
        )

        // If a phone number was detected, trigger VIP lookup if name is not set
        if (!parsed.clientPhone.isNullOrBlank() && parsed.clientName.isNullOrBlank()) {
            updateClientPhone(parsed.clientPhone)
        }

        _currentTab.value = AppTab.NEW_TRANSACTION
    }

    fun syncFlotteBalanceFromSms(newBalance: Double, sourceRef: String?) {
        preferences.setFlotteBalance(newBalance)
        viewModelScope.launch {
            repository.logAudit(
                actor = kioskConfig.value.agentName,
                actionType = "SYNC_SOLDE_SMS",
                description = "Synchronisation solde flotte Orange Money : ${newBalance.toInt()} FCFA",
                details = "Source: ${sourceRef ?: "SMS de confirmation Orange Money"}"
            )
        }
    }

    fun selectClientForTransaction(client: ClientEntity) {
        _formState.value = _formState.value.copy(
            clientPhone = client.phone,
            clientName = client.fullName,
            idNumber = client.idNumber,
            idType = client.idType,
            birthDate = client.birthDate ?: "",
            idPhotoPath = client.idPhotoPath
        )
        _showClientPickerModal.value = false
        _currentTab.value = AppTab.NEW_TRANSACTION
    }

    fun submitTransaction() {
        val form = _formState.value
        val amount = form.amountStr.toDoubleOrNull() ?: 0.0

        if (amount <= 0.0) {
            _formState.value = form.copy(errorMessage = "Veuillez saisir un montant valide supérieur à 0 FCFA.")
            return
        }

        if (form.clientPhone.isBlank()) {
            _formState.value = form.copy(errorMessage = "Le numéro de téléphone du client est obligatoire.")
            return
        }

        val fee = form.feeStr.toDoubleOrNull() ?: 0.0
        val isCashIn = form.type == TransactionType.DEPOT.id || form.type == TransactionType.TRANSFERT.id || form.type == TransactionType.FACTURE.id

        val config = kioskConfig.value
        val currentFlotte = preferences.getBalanceForNetwork(form.network)
        val currentCash = config.cashBalance

        val flotteAfter = if (isCashIn) currentFlotte - amount else currentFlotte + (amount + fee)
        val cashAfter = if (isCashIn) currentCash + (amount + fee) else currentCash - amount

        // If user configured to delete photo after scan for maximum privacy
        val photoPathToSave = if (config.deletePhotoAfterScan && form.idPhotoPath != null) {
            try { File(form.idPhotoPath).delete() } catch (e: Exception) {}
            null
        } else {
            form.idPhotoPath
        }

        val tx = TransactionEntity(
            timestamp = System.currentTimeMillis(),
            network = form.network,
            type = form.type,
            amount = amount,
            fee = fee,
            clientPhone = form.clientPhone.trim(),
            recipientPhone = form.recipientPhone.trim(),
            clientName = form.clientName.trim(),
            birthDate = form.birthDate.trim().ifBlank { null },
            idType = form.idType,
            idNumber = form.idNumber.trim(),
            idPhotoPath = photoPathToSave,
            kioskBalanceBefore = currentFlotte,
            kioskBalanceAfter = maxOf(0.0, flotteAfter),
            cashBalanceBefore = currentCash,
            cashBalanceAfter = maxOf(0.0, cashAfter),
            referenceCode = form.referenceCode.ifBlank { "TX-${System.currentTimeMillis().toString().takeLast(6)}" },
            notes = form.notes.trim(),
            agentName = config.agentName,
            syncStatus = SyncStatus.OFFLINE.name
        )

        viewModelScope.launch {
            val id = repository.saveTransaction(tx, updateKioskBalances = true)
            val savedTx = tx.copy(id = id)
            _lastSavedTransaction.value = savedTx
            _showReceiptDialog.value = true

            // Reset form for next customer
            _formState.value = TransactionFormState(
                network = form.network,
                type = form.type
            )
        }
    }

    fun dismissReceiptDialog() {
        _showReceiptDialog.value = false
    }

    fun openPhotoPreview(path: String?) {
        _previewPhotoPath.value = path
    }

    fun closePhotoPreview() {
        _previewPhotoPath.value = null
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx, actor = kioskConfig.value.agentName)
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.deleteClient(client.phone, actor = kioskConfig.value.agentName)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateFilter(filter: String) {
        _dateFilter.value = filter
    }

    fun setNetworkFilter(network: String?) {
        _networkFilter.value = network
    }

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    fun updateKioskConfig(newConfig: KioskConfig) {
        preferences.updateConfig(newConfig)
        viewModelScope.launch {
            repository.logAudit(
                actor = newConfig.agentName,
                actionType = "MODIF_PARAMETRES",
                description = "Mise à jour des paramètres de l'agence",
                details = "Kiosque: ${newConfig.kioskName}, Rôle: ${newConfig.currentRole.displayName}"
            )
        }
    }

    fun switchUserRole(role: UserRole) {
        val current = kioskConfig.value
        updateKioskConfig(current.copy(currentRole = role))
    }

    fun adjustCash(delta: Double) {
        preferences.adjustCash(delta)
        viewModelScope.launch {
            repository.logAudit(
                actor = kioskConfig.value.agentName,
                actionType = "AJUSTEMENT_CAISSE",
                description = "Ajustement manuel caisse: ${if (delta >= 0) "+$delta" else "$delta"} FCFA",
                details = "Nouveau solde: ${kioskConfig.value.cashBalance} FCFA"
            )
        }
    }

    fun adjustFlotte(network: String, delta: Double) {
        preferences.adjustFlotte(network, delta)
        viewModelScope.launch {
            repository.logAudit(
                actor = kioskConfig.value.agentName,
                actionType = "AJUSTEMENT_FLOTTE",
                description = "Ajustement flotte $network: ${if (delta >= 0) "+$delta" else "$delta"} FCFA",
                details = ""
            )
        }
    }

    fun createNewUser(user: UserEntity) {
        viewModelScope.launch {
            repository.saveUser(user)
            repository.logAudit(
                actor = kioskConfig.value.agentName,
                actionType = "CREATION_AGENT",
                description = "Création du compte ${user.fullName} (${user.role.displayName})",
                details = "Téléphone: ${user.phone}"
            )
        }
    }

    fun removeUser(user: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
            repository.logAudit(
                actor = kioskConfig.value.agentName,
                actionType = "SUPPRESSION_AGENT",
                description = "Suppression de l'utilisateur ${user.fullName}",
                details = ""
            )
        }
    }
}
