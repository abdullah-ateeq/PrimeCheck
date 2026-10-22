package com.example.primecheck.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primecheck.model.PrimeCheckResult
import com.example.primecheck.model.PrimeStatus
import com.example.primecheck.ui.theme.CompositeBlueContainerDark
import com.example.primecheck.ui.theme.CompositeBlueContainerLight
import com.example.primecheck.ui.theme.CompositeBlueDark
import com.example.primecheck.ui.theme.CompositeBlueLight
import com.example.primecheck.ui.theme.NeitherNeutralContainerDark
import com.example.primecheck.ui.theme.NeitherNeutralContainerLight
import com.example.primecheck.ui.theme.NeitherNeutralDark
import com.example.primecheck.ui.theme.NeitherNeutralLight
import com.example.primecheck.ui.theme.PrimeGreenContainerDark
import com.example.primecheck.ui.theme.PrimeGreenContainerLight
import com.example.primecheck.ui.theme.PrimeGreenDark
import com.example.primecheck.ui.theme.PrimeGreenLight
import java.math.BigInteger

@Composable
fun ResultCard(
    result: PrimeCheckResult,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val (statusLabel, statusIcon, badgeColor, containerColor, accessibleDesc) = when (result.status) {
        PrimeStatus.PRIME -> Tuple5(
            "PRIME NUMBER",
            Icons.Default.Check,
            if (isDark) PrimeGreenDark else PrimeGreenLight,
            if (isDark) PrimeGreenContainerDark else PrimeGreenContainerLight,
            "Prime number."
        )
        PrimeStatus.COMPOSITE -> Tuple5(
            "COMPOSITE NUMBER",
            Icons.Default.Close,
            if (isDark) CompositeBlueDark else CompositeBlueLight,
            if (isDark) CompositeBlueContainerDark else CompositeBlueContainerLight,
            "Composite number."
        )
        PrimeStatus.NEITHER -> Tuple5(
            "NEITHER PRIME NOR COMPOSITE",
            Icons.Default.Remove,
            if (isDark) NeitherNeutralDark else NeitherNeutralLight,
            if (isDark) NeitherNeutralContainerDark else NeitherNeutralContainerLight,
            "Neither prime nor composite."
        )
        PrimeStatus.PROBABLE_PRIME -> Tuple5(
            "PROBABLE PRIME",
            Icons.Default.QuestionMark,
            if (isDark) PrimeGreenDark else PrimeGreenLight,
            if (isDark) PrimeGreenContainerDark else PrimeGreenContainerLight,
            "Probable prime."
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibleDesc },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Badge
            Surface(
                shape = RoundedCornerShape(50),
                color = badgeColor,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Number Display
            Text(
                text = result.input.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (result.input < BigInteger.ZERO) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Based on |${result.input}| = ${result.absoluteInput}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary takeaway explanation
            val summaryText = when (result.status) {
                PrimeStatus.PRIME -> "${result.absoluteInput} has exactly two positive factors:\n1 and ${result.absoluteInput}"
                PrimeStatus.COMPOSITE -> {
                    if (result.smallestPrimeFactor != null) {
                        val other = result.absoluteInput.divide(result.smallestPrimeFactor)
                        "${result.absoluteInput} has more than two positive factors.\n${result.smallestPrimeFactor} × $other = ${result.absoluteInput}"
                    } else {
                        "${result.absoluteInput} has more than two positive factors."
                    }
                }
                PrimeStatus.NEITHER -> {
                    if (result.absoluteInput == BigInteger.ZERO) {
                        "0 is neither prime nor composite."
                    } else {
                        "1 has only one positive factor, so it is not prime."
                    }
                }
                PrimeStatus.PROBABLE_PRIME -> "This number passed a high-confidence primality test."
            }

            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action row: Save, Copy, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Save")
                }

                OutlinedButton(
                    onClick = onCopy,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Copy")
                }

                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Share")
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
