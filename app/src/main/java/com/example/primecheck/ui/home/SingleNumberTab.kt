package com.example.primecheck.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primecheck.model.PrimeCheckUiState
import com.example.primecheck.model.UserSettings
import com.example.primecheck.ui.home.components.CalculationStepsView
import com.example.primecheck.ui.home.components.FactorGrid
import com.example.primecheck.ui.home.components.FactorPairsView
import com.example.primecheck.ui.home.components.PrimeFactorizationView
import com.example.primecheck.ui.home.components.ResultCard
import kotlinx.coroutines.launch

@Composable
fun SingleNumberTab(
    uiState: PrimeCheckUiState,
    settings: UserSettings,
    onNumberChange: (String) -> Unit,
    onCheckNumber: () -> Unit,
    onCancelCalculation: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "NUMBER",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter a whole number to test for primality.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.numberInput,
                    onValueChange = onNumberChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Number to test for primality." },
                    placeholder = { Text("e.g. 29, 36, -29") },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onCheckNumber() }
                    ),
                    trailingIcon = {
                        if (uiState.numberInput.isNotEmpty()) {
                            IconButton(onClick = { onNumberChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Check, Cancel, Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.isCalculating) {
                        Button(
                            onClick = onCancelCalculation,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Cancel calculation",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("CANCEL")
                        }
                    } else {
                        Button(
                            onClick = onCheckNumber,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "Check whether this number is prime." },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "CHECK NUMBER",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (uiState.numberInput.isNotEmpty() || uiState.result != null) {
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

        // Status message banner (e.g. Cancelled)
        AnimatedVisibility(
            visible = uiState.statusMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            uiState.statusMessage?.let { status ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Calculating indicator
        if (uiState.isCalculating) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Checking number…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Result Content
        uiState.result?.let { result ->
            ResultCard(
                result = result,
                onSave = {
                    val saved = onSave()
                    scope.launch {
                        if (saved) {
                            snackbarHostState.showSnackbar("Calculation saved to history")
                        } else {
                            snackbarHostState.showSnackbar("Already saved in history")
                        }
                    }
                },
                onCopy = {
                    onCopy()
                    scope.launch {
                        snackbarHostState.showSnackbar("Copied result to clipboard")
                    }
                },
                onShare = onShare
            )

            // Prime Factorization, Smallest Factor, Factor Count, Perfect Square
            PrimeFactorizationView(
                number = result.absoluteInput,
                primeFactors = result.primeFactors,
                factorCount = result.factorCount,
                smallestPrimeFactor = result.smallestPrimeFactor,
                isPerfectSquare = result.isPerfectSquare,
                squareRoot = result.squareRoot
            )

            // Factors List (if enabled in settings)
            if (settings.showFactors && result.factors != null) {
                FactorGrid(
                    number = result.absoluteInput,
                    factors = result.factors
                )
            }

            // Factor Pairs (if enabled in settings)
            if (settings.showFactorPairs && result.factorPairs != null) {
                FactorPairsView(
                    factorPairs = result.factorPairs
                )
            }

            // Calculation Steps (if enabled in settings)
            if (settings.showCalculationSteps && result.explanation.isNotEmpty()) {
                CalculationStepsView(
                    steps = result.explanation
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
