package com.example.primecheck

import com.example.primecheck.domain.math.PrimeCheckFormatter
import com.example.primecheck.domain.math.PrimeCheckParser
import com.example.primecheck.domain.math.PrimeCheckValidator
import com.example.primecheck.domain.math.PrimeChecker
import com.example.primecheck.model.FactorPair
import com.example.primecheck.model.PrimeCheckItem
import com.example.primecheck.model.PrimeCheckResult
import com.example.primecheck.model.PrimeFactor
import com.example.primecheck.model.PrimeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class PrimeBatchAndFormattingTest {

    // 32. Batch processing
    @Test
    fun testBatchParsingNormalAndCommaSeparated() {
        val multiline = "29\n36\n41\n49\n51"
        val parsed1 = PrimeCheckParser.parseBatchInput(multiline)
        assertTrue(parsed1.isSuccess)
        assertEquals(listOf("29", "36", "41", "49", "51"), parsed1.getOrThrow())

        val commaSeparated = "29, 36, 41, 49, 51"
        val parsed2 = PrimeCheckParser.parseBatchInput(commaSeparated)
        assertTrue(parsed2.isSuccess)
        assertEquals(listOf("29", "36", "41", "49", "51"), parsed2.getOrThrow())

        // Batch limit max 50
        val over50 = (1..55).joinToString("\n")
        val parsedOver = PrimeCheckParser.parseBatchInput(over50)
        assertTrue(parsedOver.isFailure)
        assertEquals("Maximum 50 numbers per batch.", parsedOver.exceptionOrNull()?.message)
    }

    // 33. Batch invalid values
    @Test
    fun testBatchWithInvalidValuesContinues() {
        val input = "29\nabc\n36.5\n41"
        val parsed = PrimeCheckParser.parseBatchInput(input).getOrThrow()
        val items = mutableListOf<PrimeCheckItem>()

        for (itemStr in parsed) {
            val error = PrimeCheckValidator.validateSingleInput(itemStr)
            if (error != null) {
                items.add(
                    PrimeCheckItem(
                        input = itemStr,
                        status = PrimeStatus.NEITHER,
                        absoluteValue = null,
                        smallestPrimeFactor = null,
                        factorCount = null,
                        errorMessage = error
                    )
                )
            } else {
                val num = BigInteger(itemStr)
                val status = PrimeChecker.isPrime(num)
                items.add(
                    PrimeCheckItem(
                        input = itemStr,
                        status = status,
                        absoluteValue = num.abs(),
                        smallestPrimeFactor = null,
                        factorCount = null
                    )
                )
            }
        }

        assertEquals(4, items.size)
        assertEquals(PrimeStatus.PRIME, items[0].status)
        assertNotNull(items[1].errorMessage)
        assertNotNull(items[2].errorMessage)
        assertEquals(PrimeStatus.PRIME, items[3].status)
    }

    // 34. Batch duplicates
    @Test
    fun testBatchPreservesDuplicates() {
        val input = "29\n29\n36"
        val parsed = PrimeCheckParser.parseBatchInput(input).getOrThrow()
        assertEquals(listOf("29", "29", "36"), parsed)
    }

    // 35. Batch summary
    @Test
    fun testBatchSummary() {
        val items = listOf(
            PrimeCheckItem("0", PrimeStatus.NEITHER, BigInteger.ZERO, null, null),
            PrimeCheckItem("1", PrimeStatus.NEITHER, BigInteger.ONE, null, 1),
            PrimeCheckItem("2", PrimeStatus.PRIME, BigInteger.valueOf(2), null, 2),
            PrimeCheckItem("4", PrimeStatus.COMPOSITE, BigInteger.valueOf(4), BigInteger.valueOf(2), 3),
            PrimeCheckItem("29", PrimeStatus.PRIME, BigInteger.valueOf(29), null, 2),
            PrimeCheckItem("invalid", PrimeStatus.NEITHER, null, null, null, "Invalid")
        )

        val total = items.size
        val primeCount = items.count { it.errorMessage == null && it.status == PrimeStatus.PRIME }
        val compositeCount = items.count { it.errorMessage == null && it.status == PrimeStatus.COMPOSITE }
        val neitherCount = items.count { it.errorMessage == null && it.status == PrimeStatus.NEITHER }
        val errorCount = items.count { it.errorMessage != null }

        assertEquals(6, total)
        assertEquals(2, primeCount)
        assertEquals(1, compositeCount)
        assertEquals(2, neitherCount)
        assertEquals(1, errorCount)
    }

    // 36. Copy formatting
    @Test
    fun testCopyFormattingSingleResult() {
        val res = PrimeCheckResult(
            input = BigInteger.valueOf(36),
            absoluteInput = BigInteger.valueOf(36),
            status = PrimeStatus.COMPOSITE,
            smallestPrimeFactor = BigInteger.valueOf(2),
            factors = listOf(1, 2, 3, 4, 6, 9, 12, 18, 36).map { BigInteger.valueOf(it.toLong()) },
            factorPairs = listOf(FactorPair(BigInteger.ONE, BigInteger.valueOf(36))),
            factorCount = 9,
            primeFactors = listOf(
                PrimeFactor(BigInteger.valueOf(2), 2),
                PrimeFactor(BigInteger.valueOf(3), 2)
            ),
            isPerfectSquare = true,
            squareRoot = BigInteger.valueOf(6),
            explanation = emptyList(),
            calculatedAt = System.currentTimeMillis()
        )

        val copyText = PrimeCheckFormatter.formatSingleResultCopy(res)
        assertTrue(copyText.contains("Number: 36"))
        assertTrue(copyText.contains("Status: Composite"))
        assertTrue(copyText.contains("Smallest Prime Factor: 2"))
        assertTrue(copyText.contains("2^2 × 3^2"))
        assertTrue(copyText.contains("Factor Count:\n9"))
        assertTrue(copyText.contains("36 = 6^2"))
    }

    // 37. Share formatting
    @Test
    fun testShareFormattingBatchResults() {
        val items = listOf(
            PrimeCheckItem("29", PrimeStatus.PRIME, BigInteger.valueOf(29), null, 2),
            PrimeCheckItem("36", PrimeStatus.COMPOSITE, BigInteger.valueOf(36), BigInteger.valueOf(2), 9),
            PrimeCheckItem("41", PrimeStatus.PRIME, BigInteger.valueOf(41), null, 2),
            PrimeCheckItem("49", PrimeStatus.COMPOSITE, BigInteger.valueOf(49), BigInteger.valueOf(7), 3),
            PrimeCheckItem("51", PrimeStatus.COMPOSITE, BigInteger.valueOf(51), BigInteger.valueOf(3), 4)
        )

        val text = PrimeCheckFormatter.formatBatchResultsCopy(items)
        assertTrue(text.contains("29 — Prime"))
        assertTrue(text.contains("36 — Composite"))
        assertTrue(text.contains("41 — Prime"))
        assertTrue(text.contains("49 — Composite"))
        assertTrue(text.contains("51 — Composite"))
    }
}
