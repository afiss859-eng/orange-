package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.ui.theme.CashEmerald
import com.example.ui.theme.OrangeMoneyDark
import com.example.ui.theme.OrangeMoneyOrange

@Composable
fun TransactionTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_type_selector"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // DEPOT (Cash In)
            TypeCard(
                title = "DÉPÔT",
                subtitle = "Cash-In (Client dépose)",
                icon = Icons.Default.ArrowDownward,
                accentColor = CashEmerald,
                isSelected = selectedType == TransactionType.DEPOT.id,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TransactionType.DEPOT.id) },
                testTag = "type_btn_depot"
            )

            // RETRAIT (Cash Out)
            TypeCard(
                title = "RETRAIT",
                subtitle = "Cash-Out (Client retire)",
                icon = Icons.Default.ArrowUpward,
                accentColor = OrangeMoneyOrange,
                isSelected = selectedType == TransactionType.RETRAIT.id,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TransactionType.RETRAIT.id) },
                testTag = "type_btn_retrait"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // TRANSFERT
            TypeCard(
                title = "TRANSFERT",
                subtitle = "National / Sous-région",
                icon = Icons.Default.Send,
                accentColor = OrangeMoneyDark,
                isSelected = selectedType == TransactionType.TRANSFERT.id,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TransactionType.TRANSFERT.id) },
                testTag = "type_btn_transfert"
            )

            // FACTURE
            TypeCard(
                title = "FACTURE",
                subtitle = "SONABEL / ONEA",
                icon = Icons.Default.ReceiptLong,
                accentColor = Color(0xFF6B46C1),
                isSelected = selectedType == TransactionType.FACTURE.id,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TransactionType.FACTURE.id) },
                testTag = "type_btn_facture"
            )

            // CREDIT
            TypeCard(
                title = "CRÉDIT",
                subtitle = "IAP / Forfait OM",
                icon = Icons.Default.PhoneIphone,
                accentColor = Color(0xFF0D9488),
                isSelected = selectedType == TransactionType.CREDIT.id,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TransactionType.CREDIT.id) },
                testTag = "type_btn_credit"
            )
        }
    }
}

@Composable
private fun TypeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.25f) else accentColor.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.sp
                ),
                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
