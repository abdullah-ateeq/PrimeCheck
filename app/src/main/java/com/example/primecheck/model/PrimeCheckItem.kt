package com.example.primecheck.model

import java.math.BigInteger

data class PrimeCheckItem(
    val input: String,
    val status: PrimeStatus,
    val absoluteValue: BigInteger?,
    val smallestPrimeFactor: BigInteger?,
    val factorCount: Int?,
    val errorMessage: String? = null
)
