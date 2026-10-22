package com.example.primecheck.domain.math

import com.example.primecheck.model.FactorPair
import com.example.primecheck.model.PrimeFactor
import java.math.BigInteger

object PrimeCheckValidator {

    const val MAX_SINGLE_INPUT_LENGTH = 500
    const val MAX_BATCH_INPUT_LENGTH = 10000
    const val MAX_BATCH_ITEMS_COUNT = 50

    /**
     * Validates single-number input string.
     * Returns null if valid, or a human-readable error message.
     */
    fun validateSingleInput(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return "Enter a number."
        }
        if (trimmed.length > MAX_SINGLE_INPUT_LENGTH) {
            return "Number is too long. Maximum 500 characters."
        }
        if (trimmed.contains(".")) {
            return "Enter a valid whole number."
        }

        // Allow optional single leading '+' or '-'
        val body = if (trimmed.startsWith("+") || trimmed.startsWith("-")) {
            trimmed.substring(1)
        } else {
            trimmed
        }

        if (body.isEmpty() || !body.all { it.isDigit() }) {
            return "Enter a valid whole number."
        }

        return null
    }

    /**
     * Validates batch input overall length.
     */
    fun validateBatchInputLength(input: String): String? {
        if (input.length > MAX_BATCH_INPUT_LENGTH) {
            return "Input is too long."
        }
        return null
    }

    /**
     * Verifies that a composite witness divisor is mathematically valid.
     */
    fun verifyComposite(number: BigInteger, divisor: BigInteger): Boolean {
        if (divisor <= BigInteger.ONE || divisor >= number) return false
        return number.mod(divisor) == BigInteger.ZERO
    }

    /**
     * Verifies that all elements in the factor list divide number and include 1 and N.
     */
    fun verifyFactors(number: BigInteger, factors: List<BigInteger>): Boolean {
        if (factors.isEmpty()) return false
        if (factors.first() != BigInteger.ONE) return false
        if (factors.last() != number) return false
        for (f in factors) {
            if (f <= BigInteger.ZERO) return false
            if (number.mod(f) != BigInteger.ZERO) return false
        }
        return true
    }

    /**
     * Verifies that each factor pair multiplies to number.
     */
    fun verifyFactorPairs(number: BigInteger, pairs: List<FactorPair>): Boolean {
        if (pairs.isEmpty()) return false
        for (pair in pairs) {
            if (pair.first.multiply(pair.second) != number) return false
        }
        return true
    }

    /**
     * Verifies that the product of prime factors matches number.
     */
    fun verifyPrimeFactorization(number: BigInteger, primeFactors: List<PrimeFactor>): Boolean {
        if (primeFactors.isEmpty()) return number == BigInteger.ONE
        var product = BigInteger.ONE
        for (pf in primeFactors) {
            if (pf.exponent <= 0) return false
            val term = pf.prime.pow(pf.exponent)
            product = product.multiply(term)
        }
        return product == number
    }
}
