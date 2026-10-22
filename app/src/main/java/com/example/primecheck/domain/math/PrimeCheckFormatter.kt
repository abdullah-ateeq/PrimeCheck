package com.example.primecheck.domain.math

import com.example.primecheck.model.FactorPair
import com.example.primecheck.model.PrimeCheckItem
import com.example.primecheck.model.PrimeCheckResult
import com.example.primecheck.model.PrimeFactor
import com.example.primecheck.model.PrimeStatus
import java.math.BigInteger

object PrimeCheckFormatter {

    /**
     * Converts an integer exponent to Unicode superscript string.
     */
    fun toSuperscript(exponent: Int): String {
        val chars = exponent.toString()
        val sb = StringBuilder()
        for (ch in chars) {
            val sup = when (ch) {
                '0' -> '⁰'
                '1' -> '¹'
                '2' -> '²'
                '3' -> '³'
                '4' -> '⁴'
                '5' -> '⁵'
                '6' -> '⁶'
                '7' -> '⁷'
                '8' -> '⁸'
                '9' -> '⁹'
                '-' -> '⁻'
                else -> ch
            }
            sb.append(sup)
        }
        return sb.toString()
    }

    /**
     * Formats prime factorization with Unicode superscripts, e.g. "2² × 3² × 5".
     */
    fun formatFactorizationDisplay(factors: List<PrimeFactor>?): String {
        if (factors == null) return "Unavailable"
        if (factors.isEmpty()) return "1"

        return factors.joinToString(" × ") { pf ->
            if (pf.exponent == 1) {
                pf.prime.toString()
            } else {
                "${pf.prime}${toSuperscript(pf.exponent)}"
            }
        }
    }

    /**
     * Formats prime factorization for plain-text copy/share, e.g. "2^2 × 3^2 × 5".
     */
    fun formatFactorizationPlainText(factors: List<PrimeFactor>?): String {
        if (factors == null) return "Unavailable"
        if (factors.isEmpty()) return "1"

        return factors.joinToString(" × ") { pf ->
            if (pf.exponent == 1) {
                pf.prime.toString()
            } else {
                "${pf.prime}^${pf.exponent}"
            }
        }
    }

    /**
     * Formats single result for copying or sharing.
     */
    fun formatSingleResultCopy(result: PrimeCheckResult): String {
        val sb = StringBuilder()
        sb.appendLine("PrimeCheck")
        sb.appendLine()
        sb.appendLine("Number: ${result.input}")

        val statusText = when (result.status) {
            PrimeStatus.PRIME -> "Prime"
            PrimeStatus.COMPOSITE -> "Composite"
            PrimeStatus.NEITHER -> "Neither Prime nor Composite"
            PrimeStatus.PROBABLE_PRIME -> "Probable Prime"
        }
        sb.appendLine("Status: $statusText")

        if (result.input < BigInteger.ZERO) {
            sb.appendLine("Based on |${result.input}| = ${result.absoluteInput}")
        }

        if (result.smallestPrimeFactor != null) {
            sb.appendLine()
            sb.appendLine("Smallest Prime Factor: ${result.smallestPrimeFactor}")
            val quotient = result.absoluteInput.divide(result.smallestPrimeFactor)
            sb.appendLine("${result.absoluteInput} = ${result.smallestPrimeFactor} × $quotient")
        }

        if (result.primeFactors != null && result.primeFactors.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Prime Factorization:")
            sb.appendLine(formatFactorizationPlainText(result.primeFactors))
        }

        if (result.factorCount != null) {
            sb.appendLine()
            sb.appendLine("Factor Count:")
            sb.appendLine(result.factorCount)
        }

        if (result.isPerfectSquare == true && result.squareRoot != null) {
            sb.appendLine()
            sb.appendLine("Perfect Square:")
            sb.appendLine("Yes")
            sb.appendLine()
            sb.appendLine("${result.absoluteInput} = ${result.squareRoot}^2")
        }

        if (!result.factors.isNullOrEmpty() && result.factors.size <= 50) {
            sb.appendLine()
            sb.appendLine("Factors:")
            sb.appendLine(result.factors.joinToString(", "))
        }

        return sb.toString().trimEnd()
    }

    /**
     * Formats batch results for copying or sharing.
     */
    fun formatBatchResultsCopy(items: List<PrimeCheckItem>): String {
        val sb = StringBuilder()
        sb.appendLine("PrimeCheck")
        sb.appendLine()
        for (item in items) {
            val statusString = when {
                item.errorMessage != null -> "Invalid"
                item.status == PrimeStatus.PRIME -> "Prime"
                item.status == PrimeStatus.COMPOSITE -> "Composite"
                item.status == PrimeStatus.NEITHER -> "Neither"
                item.status == PrimeStatus.PROBABLE_PRIME -> "Probable Prime"
                else -> "Unknown"
            }
            sb.appendLine("${item.input} — $statusString")
        }
        return sb.toString().trimEnd()
    }
}
