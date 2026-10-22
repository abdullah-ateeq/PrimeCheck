package com.example.primecheck.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primecheck.model.CheckMode
import com.example.primecheck.model.PrimeCheckUiState
import com.example.primecheck.model.UserSettings
import com.example.primecheck.ui.PrimeCheckViewModel

@Composable
fun HomeScreen(
    viewModel: PrimeCheckViewModel,
    uiState: PrimeCheckUiState,
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // App Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "PrimeCheck",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Check whether a number is prime.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Mode Segmented Switch: Single Number vs Multiple Numbers
        PrimaryTabRow(
            selectedTabIndex = if (uiState.mode == CheckMode.SINGLE) 0 else 1,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = uiState.mode == CheckMode.SINGLE,
                onClick = { viewModel.setMode(CheckMode.SINGLE) },
                text = {
                    Text(
                        text = "Single Number",
                        fontWeight = if (uiState.mode == CheckMode.SINGLE) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                }
            )
            Tab(
                selected = uiState.mode == CheckMode.BATCH,
                onClick = { viewModel.setMode(CheckMode.BATCH) },
                text = {
                    Text(
                        text = "Multiple Numbers",
                        fontWeight = if (uiState.mode == CheckMode.BATCH) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content transition
        AnimatedContent(
            targetState = uiState.mode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "HomeModeTransition"
        ) { targetMode ->
            when (targetMode) {
                CheckMode.SINGLE -> {
                    SingleNumberTab(
                        uiState = uiState,
                        settings = settings,
                        onNumberChange = viewModel::onNumberInputChange,
                        onCheckNumber = { viewModel.checkSingleNumber() },
                        onCancelCalculation = viewModel::cancelCalculation,
                        onClear = viewModel::clearInputsAndResults,
                        onSave = viewModel::saveCurrentCalculation,
                        onCopy = { viewModel.copySingleResult(context) },
                        onShare = { viewModel.shareSingleResult(context) }
                    )
                }
                CheckMode.BATCH -> {
                    BatchTab(
                        uiState = uiState,
                        onBatchInputChange = viewModel::onBatchInputChange,
                        onCheckBatch = viewModel::checkBatchNumbers,
                        onCancelCalculation = viewModel::cancelCalculation,
                        onClear = viewModel::clearInputsAndResults,
                        onFilterChange = viewModel::setBatchFilter,
                        onSortChange = viewModel::setBatchSort,
                        onSave = viewModel::saveCurrentCalculation,
                        onCopy = { viewModel.copyBatchResults(context) },
                        onShare = { viewModel.shareBatchResults(context) }
                    )
                }
            }
        }
    }
}
