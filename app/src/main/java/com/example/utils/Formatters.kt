package com.example.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val fcfaFormatter = NumberFormat.getIntegerInstance(Locale.FRENCH)

    fun formatFcfa(amount: Double): String {
        return "${fcfaFormatter.format(amount.toLong())} F CFA"
    }

    fun formatAmountShort(amount: Double): String {
        return fcfaFormatter.format(amount.toLong())
    }

    fun formatAmount(amount: Double): String {
        return fcfaFormatter.format(amount.toLong())
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }

    fun formatFullDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE dd MMMM yyyy à HH:mm", Locale.FRENCH)
        return sdf.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
    }

    fun formatBurkinaPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        if (digits.length == 8) {
            return "${digits.substring(0, 2)} ${digits.substring(2, 4)} ${digits.substring(4, 6)} ${digits.substring(6, 8)}"
        } else if (digits.length == 11 && digits.startsWith("226")) {
            val local = digits.substring(3)
            return "+226 ${local.substring(0, 2)} ${local.substring(2, 4)} ${local.substring(4, 6)} ${local.substring(6, 8)}"
        }
        return phone
    }

    fun isToday(timestamp: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date(timestamp)) == sdf.format(Date())
    }

    fun isYesterday(timestamp: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        return sdf.format(Date(timestamp)) == sdf.format(yesterday)
    }

    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffMin = (now - timestamp) / (60 * 1000)
        return when {
            diffMin < 1 -> "À l'instant"
            diffMin < 60 -> "Il y a ${diffMin} min"
            isToday(timestamp) -> "Aujourd'hui à ${formatTime(timestamp)}"
            isYesterday(timestamp) -> "Hier à ${formatTime(timestamp)}"
            else -> formatDateTime(timestamp)
        }
    }
}
