package com.example.primecheck.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primecheck.model.BatchFilter
import com.example.primecheck.model.BatchSort
import com.example.primecheck.model.PrimeCheckItem
import com.example.primecheck.model.PrimeCheckUiState
import com.example.primecheck.model.PrimeStatus
import com.example.primecheck.ui.theme.CompositeBlueDark
import com.example.primecheck.ui.theme.CompositeBlueLight
import com.example.primecheck.ui.theme.NeitherNeutralDark
import com.example.primecheck.ui.theme.NeitherNeutralLight
import com.example.primecheck.ui.theme.PrimeGreenDark
import com.example.primecheck.ui.theme.PrimeGreenLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchTab(
    uiState: PrimeCheckUiState,
    onBatchInputChange: (String) -> Unit,
    onCheckBatch: () -> Unit,
    onCancelCalculation: () -> Unit,
    onClear: () -> Unit,
    onFilterChange: (BatchFilter) -> Unit,
    onSortChange: (BatchSort) -> Unit,
    onSave: () -> Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CHECK MULTIPLE NUMBERS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter up to 50 numbers (one per line or comma-separated).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.batchInput,
                    onValueChange = onBatchInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("29\n36\n41\n49\n51")
                    },
                    minLines = 4,
                    maxLines = 8,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 22.sp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.isCalculating) {
                        Button(
                            onClick = onCancelCalculation,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("CANCEL")
                        }
                    } else {
                        Button(
                            onClick = onCheckBatch,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "CHECK ALL",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (uiState.batchInput.isNotEmpty() || uiState.batchResults.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onClear,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CLEAR")
                        }
                    }
                }
            }
        }

        // Error message banner
        AnimatedVisibility(
            visible = uiState.errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            uiState.errorMessage?.let { error ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Calculating Progress
        if (uiState.isCalculating) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.statusMessage ?: "Checking numbers…",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.calculationProgress != null) {
                        LinearProgressIndicator(
                            progress = { (uiState.calculationProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Batch Results Section
        if (uiState.batchResults.isNotEmpty()) {
            val total = uiState.batchResults.size
            val primeCount = uiState.batchResults.count { it.errorMessage == null && (it.status == PrimeStatus.PRIME || it.status == PrimeStatus.PROBABLE_PRIME) }
            val compositeCount = uiState.batchResults.count { it.errorMessage == null && it.status == PrimeStatus.COMPOSITE }
            val neitherCount = uiState.batchResults.count { it.errorMessage == null && it.status == PrimeStatus.NEITHER }
            val errorCount = uiState.batchResults.count { it.errorMessage != null }

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BATCH SUMMARY",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SummaryColumn("Checked", total.toString())
                        SummaryColumn("Prime", primeCount.toString(), if (isDark) PrimeGreenDark else PrimeGreenLight)
                        SummaryColumn("Composite", compositeCount.toString(), if (isDark) CompositeBlueDark else CompositeBlueLight)
                        SummaryColumn("Neither", neitherCount.toString(), if (isDark) NeitherNeutralDark else NeitherNeutralLight)
                        if (errorCount > 0) {
                            SummaryColumn("Errors", errorCount.toString(), MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Action row: Save, Copy, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val saved = onSave()
                        scope.launch {
                            if (saved) {
                                snackbarHostState.showSnackbar("Batch saved to history")
                            } else {
                                snackbarHostState.showSnackbar("Already saved in history")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "Save", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Save")
                }

                OutlinedButton(
                    onClick = {
                        onCopy()
                        scope.launch { snackbarHostState.showSnackbar("Copied batch results") }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Copy")
                }

                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Share")
                }
            }

            // Filter Chips
            Text(
                text = "FILTER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BatchFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = uiState.batchFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Sort Chips
            Text(
                text = "SORT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BatchSort.entries.forEach { sort ->
                    val label = when (sort) {
                        BatchSort.ORIGINAL -> "Original"
                        BatchSort.SMALLEST_FIRST -> "Smallest → Largest"
                        BatchSort.LARGEST_FIRST -> "Largest → Smallest"
                    }
                    FilterChip(
                        selected = uiState.batchSort == sort,
                        onClick = { onSortChange(sort) },
                        label = { Text(label) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Filter and Sort Items
            val filteredItems = uiState.batchResults.filter { item ->
                when (uiState.batchFilter) {
                    BatchFilter.ALL -> true
                    BatchFilter.PRIME -> item.errorMessage == null && (item.status == PrimeStatus.PRIME || item.status == PrimeStatus.PROBABLE_PRIME)
                    BatchFilter.COMPOSITE -> item.errorMessage == null && item.status == PrimeStatus.COMPOSITE
                    BatchFilter.NEITHER -> item.errorMessage == null && item.status == PrimeStatus.NEITHER
                    BatchFilter.ERRORS -> item.errorMessage != null
                }
            }

            val sortedItems = when (uiState.batchSort) {
                BatchSort.ORIGINAL -> filteredItems
                BatchSort.SMALLEST_FIRST -> filteredItems.sortedWith { a, b ->
                    when {
                        a.absoluteValue != null && b.absoluteValue != null -> a.absoluteValue.compareTo(b.absoluteValue)
                        a.absoluteValue != null -> -1
                        b.absoluteValue != null -> 1
                        else -> a.input.compareTo(b.input)
                    }
                }
                BatchSort.LARGEST_FIRST -> filteredItems.sortedWith { a, b ->
                    when {
                        a.absoluteValue != null && b.absoluteValue != null -> b.absoluteValue.compareTo(a.absoluteValue)
                        a.absoluteValue != null -> -1
                        b.absoluteValue != null -> 1
                        else -> b.input.compareTo(a.input)
                    }
                }
            }

            // List of items
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sortedItems.forEach { item ->
                    BatchItemCard(item = item, isDark = isDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun SummaryColumn(title: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color)
        )
    }
}

@Composable
private fun BatchItemCard(item: PrimeCheckItem, isDark: Boolean) {
    val (statusText, statusColor, statusIcon) = when {
        item.errorMessage != null -> Triple("Invalid", MaterialTheme.colorScheme.error, Icons.Default.ErrorOutline)
        item.status == PrimeStatus.PRIME -> Triple("✓ Prime", if (isDark) PrimeGreenDark else PrimeGreenLight, Icons.Default.Check)
        item.status == PrimeStatus.COMPOSITE -> Triple("× Composite", if (isDark) CompositeBlueDark else CompositeBlueLight, Icons.Default.Close)
        item.status == PrimeStatus.NEITHER -> Triple("— Neither", if (isDark) NeitherNeutralDark else NeitherNeutralLight, Icons.Default.Remove)
        item.status == PrimeStatus.PROBABLE_PRIME -> Triple("? Probable Prime", if (isDark) PrimeGreenDark else PrimeGreenLight, Icons.Default.Check)
        else -> Triple("Unknown", MaterialTheme.colorScheme.onSurface, Icons.Default.Remove)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.input,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (item.errorMessage != null) {
                    Text(
                        text = item.errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                    )
                } else if (item.smallestPrimeFactor != null && item.absoluteValue != null) {
                    val q = item.absoluteValue.divide(item.smallestPrimeFactor)
                    Text(
                        text = "Divisible by ${item.smallestPrimeFactor} (${item.smallestPrimeFactor} × $q)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
