package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.MobileNetwork
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class KioskConfig(
    val kioskName: String = "Kiosque Orange Money",
    val agentName: String = "Agent Gérant",
    val agentPhone: String = "70 00 00 00",
    val location: String = "Ouagadougou, Burkina Faso",
    val currentRole: UserRole = UserRole.ADMIN,
    val cashBalance: Double = 350000.0,
    val orangeBalance: Double = 650000.0,
    val customAdminTitle: String = "Administration & Clôture de Caisse",
    val autoFeeCalculation: Boolean = true,
    val requireIdPhoto: Boolean = true,
    val deletePhotoAfterScan: Boolean = false,
    val pinLockEnabled: Boolean = false,
    val adminPinCode: String = "1234"
)

class KioskPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kiosk_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<KioskConfig> = _config.asStateFlow()

    private fun loadConfig(): KioskConfig {
        val roleStr = prefs.getString("current_role", UserRole.ADMIN.name) ?: UserRole.ADMIN.name
        val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.ADMIN }
        return KioskConfig(
            kioskName = prefs.getString("kiosk_name", "Kiosque Orange Wend-Panga") ?: "Kiosque Orange Wend-Panga",
            agentName = prefs.getString("agent_name", "Agent Gérant") ?: "Agent Gérant",
            agentPhone = prefs.getString("agent_phone", "70 00 00 00") ?: "70 00 00 00",
            location = prefs.getString("location", "Ouagadougou, Burkina Faso") ?: "Ouagadougou, Burkina Faso",
            currentRole = role,
            cashBalance = prefs.getFloat("cash_balance", 350000f).toDouble(),
            orangeBalance = prefs.getFloat("orange_balance", 650000f).toDouble(),
            customAdminTitle = prefs.getString("admin_title", "Administration & Clôture de Caisse") ?: "Administration & Clôture de Caisse",
            autoFeeCalculation = prefs.getBoolean("auto_fee", true),
            requireIdPhoto = prefs.getBoolean("require_id_photo", true),
            deletePhotoAfterScan = prefs.getBoolean("delete_photo_after_scan", false),
            pinLockEnabled = prefs.getBoolean("pin_lock_enabled", false),
            adminPinCode = prefs.getString("admin_pin_code", "1234") ?: "1234"
        )
    }

    fun updateConfig(newConfig: KioskConfig) {
        prefs.edit()
            .putString("kiosk_name", newConfig.kioskName)
            .putString("agent_name", newConfig.agentName)
            .putString("agent_phone", newConfig.agentPhone)
            .putString("location", newConfig.location)
            .putString("current_role", newConfig.currentRole.name)
            .putFloat("cash_balance", newConfig.cashBalance.toFloat())
            .putFloat("orange_balance", newConfig.orangeBalance.toFloat())
            .putString("admin_title", newConfig.customAdminTitle)
            .putBoolean("auto_fee", newConfig.autoFeeCalculation)
            .putBoolean("require_id_photo", newConfig.requireIdPhoto)
            .putBoolean("delete_photo_after_scan", newConfig.deletePhotoAfterScan)
            .putBoolean("pin_lock_enabled", newConfig.pinLockEnabled)
            .putString("admin_pin_code", newConfig.adminPinCode)
            .apply()
        _config.value = newConfig
    }

    fun getBalanceForNetwork(network: String): Double {
        return _config.value.orangeBalance
    }

    fun applyTransactionBalances(
        network: String,
        isCashIn: Boolean,
        amount: Double,
        fee: Double
    ) {
        val current = _config.value
        var cash = current.cashBalance
        var orange = current.orangeBalance

        if (isCashIn) {
            // Dépôt / Transfert / Facture : L'agent encaisse du cash (+cash), déduit de la flotte OM (-flotte)
            cash += (amount + fee)
            orange -= amount
        } else {
            // Retrait : Le client transfère à la flotte OM de l'agent (+flotte), l'agent donne du cash physique (-cash)
            cash -= amount
            orange += (amount + fee)
        }

        val updated = current.copy(
            cashBalance = maxOf(0.0, cash),
            orangeBalance = maxOf(0.0, orange)
        )
        updateConfig(updated)
    }

    fun adjustCash(amountDelta: Double) {
        val current = _config.value
        updateConfig(current.copy(cashBalance = maxOf(0.0, current.cashBalance + amountDelta)))
    }

    fun adjustFlotte(network: String, amountDelta: Double) {
        val current = _config.value
        updateConfig(current.copy(orangeBalance = maxOf(0.0, current.orangeBalance + amountDelta)))
    }

    fun setFlotteBalance(newBalance: Double) {
        val current = _config.value
        updateConfig(current.copy(orangeBalance = maxOf(0.0, newBalance)))
    }
}
