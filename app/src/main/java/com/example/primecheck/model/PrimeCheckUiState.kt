package com.example.primecheck.model

enum class BatchFilter {
    ALL,
    PRIME,
    COMPOSITE,
    NEITHER,
    ERRORS
}

enum class BatchSort {
    ORIGINAL,
    SMALLEST_FIRST,
    LARGEST_FIRST
}

data class PrimeCheckUiState(
    val mode: CheckMode = CheckMode.SINGLE,
    val numberInput: String = "",
    val batchInput: String = "",
    val result: PrimeCheckResult? = null,
    val batchResults: List<PrimeCheckItem> = emptyList(),
    val history: List<PrimeCheckHistory> = emptyList(),
    val errorMessage: String? = null,
    val isCalculating: Boolean = false,
    val calculationProgress: Int? = null,
    val batchFilter: BatchFilter = BatchFilter.ALL,
    val batchSort: BatchSort = BatchSort.ORIGINAL,
    val isCancelled: Boolean = false,
    val statusMessage: String? = null
)
