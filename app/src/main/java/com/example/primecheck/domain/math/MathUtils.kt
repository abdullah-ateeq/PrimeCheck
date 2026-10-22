package com.example.primecheck.domain.math

import java.math.BigInteger

object MathUtils {

    /**
     * Exact integer square root floor(sqrt(N)) using Newton's method with pure BigInteger arithmetic.
     * Guaranteed to never convert to Double/Float/Long.
     */
    fun integerSqrt(number: BigInteger): BigInteger {
        require(number >= BigInteger.ZERO) { "Square root of negative number is undefined for real integers" }
        if (number == BigInteger.ZERO || number == BigInteger.ONE) {
            return number
        }

        // Start with an upper bound: 2^((bitLength + 1) / 2)
        var x0 = BigInteger.ONE.shiftLeft((number.bitLength() + 1) / 2)
        while (true) {
            val x1 = (x0 + number.divide(x0)).shiftRight(1)
            if (x1 >= x0) {
                return x0
            }
            x0 = x1
        }
    }

    /**
     * Determines whether number is a perfect square (N = k^2 for some non-negative integer k).
     */
    fun isPerfectSquare(number: BigInteger): Boolean {
        if (number < BigInteger.ZERO) return false
        if (number == BigInteger.ZERO || number == BigInteger.ONE) return true

        // Quick check of last digit in hex/decimal for performance
        val last4Bits = number.and(BigInteger.valueOf(0xF)).toInt()
        if (last4Bits != 0 && last4Bits != 1 && last4Bits != 4 && last4Bits != 9) {
            return false
        }

        val sqrt = integerSqrt(number)
        return sqrt.multiply(sqrt) == number
    }

    /**
     * Formats approximate square root string for mathematical explanations, e.g. "√29 ≈ 5.38".
     * Calculates two decimal digits using integer arithmetic: floor(sqrt(N * 10000)) / 100.
     */
    fun formatApproxSquareRoot(number: BigInteger): String {
        if (number < BigInteger.ZERO) return "undefined"
        if (number == BigInteger.ZERO) return "0"
        if (number == BigInteger.ONE) return "1"

        // N * 10000 gives 2 decimal places precision: sqrt(N * 10000) = 100 * sqrt(N)
        val scaled = number.multiply(BigInteger.valueOf(10000))
        val scaledSqrt = integerSqrt(scaled)
        val whole = scaledSqrt.divide(BigInteger.valueOf(100))
        val remainder = scaledSqrt.remainder(BigInteger.valueOf(100)).toInt()

        return if (remainder == 0) {
            "$whole"
        } else {
            "≈ $whole.%02d".format(remainder)
        }
    }
}
