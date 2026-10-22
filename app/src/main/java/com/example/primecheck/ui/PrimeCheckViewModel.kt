package com.example.primecheck.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.primecheck.data.repository.HistoryRepository
import com.example.primecheck.data.repository.SettingsRepository
import com.example.primecheck.domain.math.PrimeCheckFormatter
import com.example.primecheck.domain.math.PrimeCheckParser
import com.example.primecheck.domain.math.PrimeCheckValidator
import com.example.primecheck.domain.math.PrimeChecker
import com.example.primecheck.model.BatchFilter
import com.example.primecheck.model.BatchSort
import com.example.primecheck.model.CheckMode
import com.example.primecheck.model.PrimeCheckHistory
import com.example.primecheck.model.PrimeCheckItem
import com.example.primecheck.model.PrimeCheckUiState
import com.example.primecheck.model.PrimeStatus
import com.example.primecheck.model.UserSettings
import com.example.primecheck.util.HapticsHelper
import com.example.primecheck.util.ShareHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger

class PrimeCheckViewModel(
    application: Application,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        HistoryRepository(application),
        SettingsRepository(application)
    )

    private val hapticsHelper = HapticsHelper(application)

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, settingsRepository.loadSettings())

    private val _uiState = MutableStateFlow(
        PrimeCheckUiState(
            mode = settingsRepository.getLastMode(),
            numberInput = settingsRepository.getLastInput(),
            history = historyRepository.getHistory()
        )
    )
    val uiState: StateFlow<PrimeCheckUiState> = _uiState.asStateFlow()

    private var calculationJob: Job? = null

    init {
        // If there was a persisted last single input, perform initial check if not empty
        val lastInput = settingsRepository.getLastInput()
        if (lastInput.isNotBlank() && _uiState.value.mode == CheckMode.SINGLE) {
            checkSingleNumber(saveToLastInput = false)
        }
    }

    fun setMode(mode: CheckMode) {
        if (_uiState.value.mode != mode) {
            _uiState.update { it.copy(mode = mode, errorMessage = null, statusMessage = null) }
            settingsRepository.setLastMode(mode)
            hapticsHelper.lightFeedback(settings.value.hapticFeedback)
        }
    }

    fun onNumberInputChange(newInput: String) {
        _uiState.update {
            it.copy(
                numberInput = newInput,
                errorMessage = null,
                statusMessage = null
            )
        }
    }

    fun onBatchInputChange(newInput: String) {
        _uiState.update {
            it.copy(
                batchInput = newInput,
                errorMessage = null,
                statusMessage = null
            )
        }
    }

    fun setBatchFilter(filter: BatchFilter) {
        _uiState.update { it.copy(batchFilter = filter) }
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }

    fun setBatchSort(sort: BatchSort) {
        _uiState.update { it.copy(batchSort = sort) }
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }

    fun checkSingleNumber(saveToLastInput: Boolean = true) {
        val input = _uiState.value.numberInput.trim()
        val parseResult = PrimeCheckParser.parseSingleInput(input)
        if (parseResult.isFailure) {
            val error = parseResult.exceptionOrNull()?.message ?: "Enter a valid whole number."
            _uiState.update {
                it.copy(
                    errorMessage = error,
                    result = null,
                    isCalculating = false
                )
            }
            hapticsHelper.errorFeedback(settings.value.hapticFeedback)
            return
        }

        val number = parseResult.getOrThrow()
        if (saveToLastInput) {
            settingsRepository.setLastInput(input)
        }

        calculationJob?.cancel()
        _uiState.update {
            it.copy(
                isCalculating = true,
                errorMessage = null,
                statusMessage = null,
                isCancelled = false
            )
        }
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)

        calculationJob = viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val result = withContext(Dispatchers.Default) {
                    PrimeChecker.checkPrime(
                        number = number,
                        startTimeMs = startTime,
                        checkCancellation = { !coroutineContext[Job]!!.isActive }
                    )
                }

                if (result.isSuccess) {
                    val primeResult = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            result = primeResult,
                            isCalculating = false,
                            statusMessage = null
                        )
                    }
                    hapticsHelper.lightFeedback(settings.value.hapticFeedback)
                } else {
                    val ex = result.exceptionOrNull()
                    if (ex is CancellationException) {
                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                isCancelled = true,
                                statusMessage = "Calculation cancelled."
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                errorMessage = ex?.message ?: "Calculation error."
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        isCancelled = true,
                        statusMessage = "Calculation cancelled."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = e.message ?: "Unexpected error occurred."
                    )
                }
            }
        }
    }

    fun checkBatchNumbers() {
        val rawInput = _uiState.value.batchInput
        val parseResult = PrimeCheckParser.parseBatchInput(rawInput)
        if (parseResult.isFailure) {
            val error = parseResult.exceptionOrNull()?.message ?: "Invalid batch input."
            _uiState.update {
                it.copy(
                    errorMessage = error,
                    batchResults = emptyList(),
                    isCalculating = false
                )
            }
            hapticsHelper.errorFeedback(settings.value.hapticFeedback)
            return
        }

        val items = parseResult.getOrThrow()
        if (items.isEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Enter at least one number.",
                    batchResults = emptyList(),
                    isCalculating = false
                )
            }
            hapticsHelper.errorFeedback(settings.value.hapticFeedback)
            return
        }

        calculationJob?.cancel()
        _uiState.update {
            it.copy(
                isCalculating = true,
                errorMessage = null,
                statusMessage = "Checking numbers…",
                batchResults = emptyList(),
                calculationProgress = 0,
                isCancelled = false
            )
        }
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)

        calculationJob = viewModelScope.launch {
            try {
                val results = mutableListOf<PrimeCheckItem>()
                val total = items.size

                withContext(Dispatchers.Default) {
                    for ((index, itemStr) in items.withIndex()) {
                        if (!coroutineContext[Job]!!.isActive) {
                            throw CancellationException("Batch cancelled.")
                        }

                        val progressMessage = "Checking ${index + 1} of $total…"
                        _uiState.update {
                            it.copy(
                                statusMessage = progressMessage,
                                calculationProgress = ((index + 1) * 100) / total
                            )
                        }

                        // Validate each single item
                        val singleValidation = PrimeCheckValidator.validateSingleInput(itemStr)
                        if (singleValidation != null) {
                            results.add(
                                PrimeCheckItem(
                                    input = itemStr,
                                    status = PrimeStatus.NEITHER,
                                    absoluteValue = null,
                                    smallestPrimeFactor = null,
                                    factorCount = null,
                                    errorMessage = singleValidation
                                )
                            )
                            continue
                        }

                        val parsedNumber = try {
                            val norm = if (itemStr.startsWith("+")) itemStr.substring(1) else itemStr
                            BigInteger(norm)
                        } catch (e: Exception) {
                            results.add(
                                PrimeCheckItem(
                                    input = itemStr,
                                    status = PrimeStatus.NEITHER,
                                    absoluteValue = null,
                                    smallestPrimeFactor = null,
                                    factorCount = null,
                                    errorMessage = "Enter a valid whole number."
                                )
                            )
                            continue
                        }

                        val absVal = parsedNumber.abs()
                        val status = PrimeChecker.isPrime(absVal)
                        val smallestFactor = if (status == PrimeStatus.COMPOSITE) {
                            PrimeChecker.findSmallestPrimeFactor(absVal)
                        } else null

                        results.add(
                            PrimeCheckItem(
                                input = itemStr,
                                status = status,
                                absoluteValue = absVal,
                                smallestPrimeFactor = smallestFactor,
                                factorCount = if (status == PrimeStatus.PRIME) 2 else null,
                                errorMessage = null
                            )
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        batchResults = results,
                        isCalculating = false,
                        statusMessage = null,
                        calculationProgress = null
                    )
                }
                hapticsHelper.lightFeedback(settings.value.hapticFeedback)
            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        isCancelled = true,
                        statusMessage = "Calculation cancelled.",
                        calculationProgress = null
                    )
                }
            }
        }
    }

    fun cancelCalculation() {
        calculationJob?.cancel()
        _uiState.update {
            it.copy(
                isCalculating = false,
                isCancelled = true,
                statusMessage = "Calculation cancelled.",
                calculationProgress = null
            )
        }
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }

    fun saveCurrentCalculation(): Boolean {
        val currentMode = _uiState.value.mode
        val inputToSave = if (currentMode == CheckMode.SINGLE) {
            if (_uiState.value.result == null) return false
            _uiState.value.numberInput.trim()
        } else {
            if (_uiState.value.batchResults.isEmpty()) return false
            _uiState.value.batchInput.trim()
        }

        if (inputToSave.isBlank()) return false

        val saved = historyRepository.saveCalculation(inputToSave, currentMode)
        if (saved) {
            _uiState.update { it.copy(history = historyRepository.getHistory()) }
            hapticsHelper.lightFeedback(settings.value.hapticFeedback)
        }
        return saved
    }

    fun restoreHistoryItem(item: PrimeCheckHistory) {
        _uiState.update {
            it.copy(
                mode = item.mode,
                numberInput = if (item.mode == CheckMode.SINGLE) item.input else it.numberInput,
                batchInput = if (item.mode == CheckMode.BATCH) item.input else it.batchInput,
                errorMessage = null,
                statusMessage = null
            )
        }
        settingsRepository.setLastMode(item.mode)
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)

        if (item.mode == CheckMode.SINGLE) {
            checkSingleNumber(saveToLastInput = true)
        } else {
            checkBatchNumbers()
        }
    }

    fun deleteHistoryItem(id: String) {
        historyRepository.deleteItem(id)
        _uiState.update { it.copy(history = historyRepository.getHistory()) }
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }

    fun clearAllHistory() {
        historyRepository.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
        hapticsHelper.heavyFeedback(settings.value.hapticFeedback)
    }

    fun clearInputsAndResults() {
        calculationJob?.cancel()
        _uiState.update {
            it.copy(
                numberInput = "",
                batchInput = "",
                result = null,
                batchResults = emptyList(),
                errorMessage = null,
                statusMessage = null,
                isCalculating = false,
                calculationProgress = null
            )
        }
        settingsRepository.setLastInput("")
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }

    fun updateSettings(newSettings: UserSettings) {
        settingsRepository.updateSettings(newSettings)
        hapticsHelper.lightFeedback(newSettings.hapticFeedback)
    }

    fun resetSettings(): UserSettings {
        val defaults = settingsRepository.resetSettings()
        hapticsHelper.heavyFeedback(defaults.hapticFeedback)
        return defaults
    }

    fun copySingleResult(context: android.content.Context) {
        val result = _uiState.value.result ?: return
        val text = PrimeCheckFormatter.formatSingleResultCopy(result)
        ShareHelper.copyToClipboard(context, text)
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }

    fun shareSingleResult(context: android.content.Context) {
        val result = _uiState.value.result ?: return
        val text = PrimeCheckFormatter.formatSingleResultCopy(result)
        ShareHelper.shareText(context, text)
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }

    fun copyBatchResults(context: android.content.Context) {
        val items = _uiState.value.batchResults
        if (items.isEmpty()) return
        val text = PrimeCheckFormatter.formatBatchResultsCopy(items)
        ShareHelper.copyToClipboard(context, text)
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }

    fun shareBatchResults(context: android.content.Context) {
        val items = _uiState.value.batchResults
        if (items.isEmpty()) return
        val text = PrimeCheckFormatter.formatBatchResultsCopy(items)
        ShareHelper.shareText(context, text)
        hapticsHelper.lightFeedback(settings.value.hapticFeedback)
    }
}
