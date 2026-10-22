package com.example.primecheck.model

data class PrimeCheckHistory(
    val id: String,
    val input: String,
    val mode: CheckMode,
    val savedAt: Long
)
