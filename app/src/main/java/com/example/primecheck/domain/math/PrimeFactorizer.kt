package com.example.primecheck.domain.math

import com.example.primecheck.model.FactorPair
import com.example.primecheck.model.PrimeFactor
import java.math.BigInteger
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException

object PrimeFactorizer {

    private const val FACTORIZATION_TIMEOUT_MS = 5000L
    // Practical threshold for generating exhaustive factor lists without risking memory exhaustion
    private val MAX_FACTOR_GENERATION_LIMIT = BigInteger.valueOf(100_000_000_000L) // 10^11
    private const val MAX_ALLOWABLE_FACTOR_COUNT = 5000

    /**
     * Finds prime factorization of positive integer [number] (greater than 1).
     * If [number] is 1, returns empty list.
     * Enforces timeout and mathematical verification.
     */
    fun factorize(
        number: BigInteger,
        startTimeMs: Long = System.currentTimeMillis(),
        checkCancellation: () -> Boolean = { false }
    ): Result<List<PrimeFactor>> {
        require(number >= BigInteger.ONE) { "Factorization requires positive integer" }

        if (checkCancellation()) {
            return Result.failure(CancellationException("Calculation cancelled."))
        }
        if (System.currentTimeMillis() - startTimeMs > FACTORIZATION_TIMEOUT_MS) {
            return Result.failure(TimeoutException("Prime factorization is taking longer than expected."))
        }

        if (number == BigInteger.ONE) {
            return Result.success(emptyList())
        }

        var n = number
        val primeFactors = mutableListOf<PrimeFactor>()

        // 1. Check factor 2
        var count2 = 0
        while (!n.testBit(0)) {
            if (checkCancellation()) return Result.failure(CancellationException("Calculation cancelled."))
            if (System.currentTimeMillis() - startTimeMs > FACTORIZATION_TIMEOUT_MS) {
                return Result.failure(TimeoutException("Prime factorization is taking longer than expected."))
            }
            count2++
            n = n.shiftRight(1)
        }
        if (count2 > 0) {
            primeFactors.add(PrimeFactor(BigInteger.valueOf(2), count2))
        }

        // 2. Odd divisors
        var d = BigInteger.valueOf(3)
        var stepCount = 0
        while (d.multiply(d) <= n) {
            if (++stepCount % 500 == 0) {
                if (checkCancellation()) return Result.failure(CancellationException("Calculation cancelled."))
                if (System.currentTimeMillis() - startTimeMs > FACTORIZATION_TIMEOUT_MS) {
                    return Result.failure(TimeoutException("Prime factorization is taking longer than expected."))
                }
            }

            if (n.mod(d) == BigInteger.ZERO) {
                var exp = 0
                while (n.mod(d) == BigInteger.ZERO) {
                    exp++
                    n = n.divide(d)
                }
                primeFactors.add(PrimeFactor(d, exp))
            }
            d = d.add(BigInteger.valueOf(2))
        }

        if (n > BigInteger.ONE) {
            primeFactors.add(PrimeFactor(n, 1))
        }

        // Verification
        if (!PrimeCheckValidator.verifyPrimeFactorization(number, primeFactors)) {
            return Result.failure(IllegalStateException("Internal factorization verification failed."))
        }

        return Result.success(primeFactors)
    }

    /**
     * Calculates the exact total number of positive divisors from the prime factorization.
     * If N = p1^a * p2^b * ..., factorCount = (a + 1)(b + 1)...
     */
    fun calculateFactorCount(primeFactors: List<PrimeFactor>): Int {
        if (primeFactors.isEmpty()) return 1
        var count = 1L
        for (pf in primeFactors) {
            count *= (pf.exponent + 1)
            if (count > Int.MAX_VALUE) return Int.MAX_VALUE
        }
        return count.toInt()
    }

    /**
     * Generates all positive divisors of positive integer [number] in ascending order.
     * Restricts execution if the number or expected factor count is too large.
     */
    fun findFactors(
        number: BigInteger,
        startTimeMs: Long = System.currentTimeMillis(),
        checkCancellation: () -> Boolean = { false }
    ): Result<List<BigInteger>> {
        require(number >= BigInteger.ONE) { "Factors defined for positive integers" }
        if (number == BigInteger.ONE) {
            return Result.success(listOf(BigInteger.ONE))
        }

        if (number > MAX_FACTOR_GENERATION_LIMIT) {
            return Result.failure(IllegalStateException("Number too large for exhaustive factor listing."))
        }

        val factorizationResult = factorize(number, startTimeMs, checkCancellation)
        if (factorizationResult.isFailure) {
            return Result.failure(factorizationResult.exceptionOrNull() ?: Exception("Factorization failed"))
        }

        val primeFactors = factorizationResult.getOrThrow()
        val expectedCount = calculateFactorCount(primeFactors)
        if (expectedCount > MAX_ALLOWABLE_FACTOR_COUNT) {
            return Result.failure(IllegalStateException("Factor count exceeds display limit."))
        }

        // Generate factors recursively from prime factors
        val factors = mutableListOf<BigInteger>()

        fun generate(index: Int, current: BigInteger) {
            if (index == primeFactors.size) {
                factors.add(current)
                return
            }
            val pf = primeFactors[index]
            var multiplier = BigInteger.ONE
            for (i in 0..pf.exponent) {
                generate(index + 1, current.multiply(multiplier))
                if (i < pf.exponent) {
                    multiplier = multiplier.multiply(pf.prime)
                }
            }
        }

        generate(0, BigInteger.ONE)
        factors.sort()

        // Verification
        if (!PrimeCheckValidator.verifyFactors(number, factors)) {
            return Result.failure(IllegalStateException("Internal factor list verification failed."))
        }

        return Result.success(factors)
    }

    /**
     * Generates all unique factor pairs (a, b) such that a * b = number and a <= b.
     */
    fun findFactorPairs(
        number: BigInteger,
        startTimeMs: Long = System.currentTimeMillis(),
        checkCancellation: () -> Boolean = { false }
    ): Result<List<FactorPair>> {
        val factorsResult = findFactors(number, startTimeMs, checkCancellation)
        if (factorsResult.isFailure) {
            return Result.failure(factorsResult.exceptionOrNull() ?: Exception("Cannot find factor pairs."))
        }

        val factors = factorsResult.getOrThrow()
        val pairs = mutableListOf<FactorPair>()
        val sqrt = MathUtils.integerSqrt(number)

        for (f in factors) {
            if (f > sqrt) break
            val other = number.divide(f)
            pairs.add(FactorPair(f, other))
        }

        // Verification
        if (!PrimeCheckValidator.verifyFactorPairs(number, pairs)) {
            return Result.failure(IllegalStateException("Internal factor pairs verification failed."))
        }

        return Result.success(pairs)
    }
}
