package com.example.primecheck.model

import java.math.BigInteger

data class PrimeCheckResult(
    val input: BigInteger,
    val absoluteInput: BigInteger,
    val status: PrimeStatus,
    val smallestPrimeFactor: BigInteger?,
    val factors: List<BigInteger>?,
    val factorPairs: List<FactorPair>?,
    val factorCount: Int?,
    val primeFactors: List<PrimeFactor>?,
    val isPerfectSquare: Boolean?,
    val squareRoot: BigInteger?,
    val explanation: List<CalculationStep>,
    val calculatedAt: Long
)
