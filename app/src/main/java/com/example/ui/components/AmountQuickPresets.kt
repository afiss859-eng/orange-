package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.Formatters

@Composable
fun AmountQuickPresets(
    onPresetSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        1000L,
        2000L,
        5000L,
        10000L,
        25000L,
        50000L,
        100000L,
        200000L
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("amount_quick_presets"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(presets) { amount ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onPresetSelected(amount) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("preset_$amount"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${Formatters.formatAmountShort(amount.toDouble())} F",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
