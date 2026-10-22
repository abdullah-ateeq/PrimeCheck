package com.example.primecheck

import com.example.primecheck.domain.math.MathUtils
import com.example.primecheck.domain.math.PrimeCheckParser
import com.example.primecheck.domain.math.PrimeCheckValidator
import com.example.primecheck.domain.math.PrimeChecker
import com.example.primecheck.domain.math.PrimeFactorizer
import com.example.primecheck.model.FactorPair
import com.example.primecheck.model.PrimeFactor
import com.example.primecheck.model.PrimeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException

class PrimeMathTest {

    // 1. Integer parsing
    @Test
    fun testIntegerParsing() {
        val result = PrimeCheckParser.parseSingleInput("12345678901234567890")
        assertTrue(result.isSuccess)
        assertEquals(BigInteger("12345678901234567890"), result.getOrNull())

        val withPlus = PrimeCheckParser.parseSingleInput("+42")
        assertTrue(withPlus.isSuccess)
        assertEquals(BigInteger("42"), withPlus.getOrNull())
    }

    // 2. Empty input
    @Test
    fun testEmptyInput() {
        val result = PrimeCheckParser.parseSingleInput("")
        assertTrue(result.isFailure)
        assertEquals("Enter a number.", result.exceptionOrNull()?.message)

        val whitespaceResult = PrimeCheckParser.parseSingleInput("   ")
        assertTrue(whitespaceResult.isFailure)
        assertEquals("Enter a number.", whitespaceResult.exceptionOrNull()?.message)
    }

    // 3. Invalid input
    @Test
    fun testInvalidInput() {
        val abcResult = PrimeCheckParser.parseSingleInput("abc")
        assertTrue(abcResult.isFailure)
        assertEquals("Enter a valid whole number.", abcResult.exceptionOrNull()?.message)

        val doubleSigns = PrimeCheckParser.parseSingleInput("--29")
        assertTrue(doubleSigns.isFailure)
        assertEquals("Enter a valid whole number.", doubleSigns.exceptionOrNull()?.message)

        val mixedResult = PrimeCheckParser.parseSingleInput("12a34")
        assertTrue(mixedResult.isFailure)
        assertEquals("Enter a valid whole number.", mixedResult.exceptionOrNull()?.message)
    }

    // 4. Decimal rejection
    @Test
    fun testDecimalRejection() {
        val res1 = PrimeCheckParser.parseSingleInput("10.5")
        assertTrue(res1.isFailure)
        assertEquals("Enter a valid whole number.", res1.exceptionOrNull()?.message)

        val res2 = PrimeCheckParser.parseSingleInput("2.75")
        assertTrue(res2.isFailure)
        assertEquals("Enter a valid whole number.", res2.exceptionOrNull()?.message)

        val res3 = PrimeCheckParser.parseSingleInput("1.2.3")
        assertTrue(res3.isFailure)
        assertEquals("Enter a valid whole number.", res3.exceptionOrNull()?.message)
    }

    // 5. Positive number
    @Test
    fun testPositiveNumber() {
        val result = PrimeChecker.checkPrime(BigInteger.valueOf(17))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(BigInteger.valueOf(17), data.input)
        assertEquals(BigInteger.valueOf(17), data.absoluteInput)
        assertEquals(PrimeStatus.PRIME, data.status)
    }

    // 6. Negative number
    @Test
    fun testNegativeNumber() {
        val result = PrimeChecker.checkPrime(BigInteger.valueOf(-17))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(BigInteger.valueOf(-17), data.input)
        assertEquals(BigInteger.valueOf(17), data.absoluteInput)
        assertEquals(PrimeStatus.PRIME, data.status)
    }

    // 7. Zero
    @Test
    fun testZero() {
        val result = PrimeChecker.checkPrime(BigInteger.ZERO)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(PrimeStatus.NEITHER, data.status)
        assertNull(data.smallestPrimeFactor)
        assertNull(data.factorCount)
        assertTrue(data.explanation.any { it.result.contains("neither prime nor composite") })
    }

    // 8. One
    @Test
    fun testOne() {
        val result = PrimeChecker.checkPrime(BigInteger.ONE)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(PrimeStatus.NEITHER, data.status)
        assertNull(data.smallestPrimeFactor)
        assertEquals(1, data.factorCount)
        assertTrue(data.explanation.any { it.result.contains("only one positive factor") })
    }

    // 9. Two
    @Test
    fun testTwo() {
        val result = PrimeChecker.checkPrime(BigInteger.valueOf(2))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(PrimeStatus.PRIME, data.status)
        assertEquals(2, data.factorCount)
        assertNull(data.smallestPrimeFactor)
        assertEquals(listOf(BigInteger.ONE, BigInteger.valueOf(2)), data.factors)
    }

    // 10. Three
    @Test
    fun testThree() {
        val result = PrimeChecker.checkPrime(BigInteger.valueOf(3))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(PrimeStatus.PRIME, data.status)
        assertEquals(2, data.factorCount)
        assertNull(data.smallestPrimeFactor)
    }

    // 11. Prime number (e.g. 29)
    @Test
    fun testPrimeNumber() {
        val result = PrimeChecker.checkPrime(BigInteger.valueOf(29))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(PrimeStatus.PRIME, data.status)
        assertEquals(2, data.factorCount)
        assertEquals(listOf(BigInteger.ONE, BigInteger.valueOf(29)), data.factors)
        assertEquals(listOf(FactorPair(BigInteger.ONE, BigInteger.valueOf(29))), data.factorPairs)
        assertEquals(listOf(PrimeFactor(BigInteger.valueOf(29), 1)), data.primeFactors)
    }

    // 12. Composite number (e.g. 36)
    @Test
    fun testCompositeNumber() {
        val result = PrimeChecker.checkPrime(BigInteger.valueOf(36))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(PrimeStatus.COMPOSITE, data.status)
        assertEquals(BigInteger.valueOf(2), data.smallestPrimeFactor)
        assertEquals(9, data.factorCount)
        assertEquals(
            listOf(1, 2, 3, 4, 6, 9, 12, 18, 36).map { BigInteger.valueOf(it.toLong()) },
            data.factors
        )
    }

    // 13. Perfect square
    @Test
    fun testPerfectSquare() {
        assertTrue(MathUtils.isPerfectSquare(BigInteger.valueOf(36)))
        assertTrue(MathUtils.isPerfectSquare(BigInteger.valueOf(49)))
        assertTrue(MathUtils.isPerfectSquare(BigInteger.valueOf(0)))
        assertTrue(MathUtils.isPerfectSquare(BigInteger.valueOf(1)))
        assertFalse(MathUtils.isPerfectSquare(BigInteger.valueOf(37)))
        assertFalse(MathUtils.isPerfectSquare(BigInteger.valueOf(47)))
        assertFalse(MathUtils.isPerfectSquare(BigInteger.valueOf(-4)))

        val res49 = PrimeChecker.checkPrime(BigInteger.valueOf(49))
        assertTrue(res49.isSuccess)
        val data49 = res49.getOrThrow()
        assertEquals(PrimeStatus.COMPOSITE, data49.status)
        assertEquals(true, data49.isPerfectSquare)
        assertEquals(BigInteger.valueOf(7), data49.squareRoot)
    }

    // 14. Negative prime
    @Test
    fun testNegativePrime() {
        val res = PrimeChecker.checkPrime(BigInteger.valueOf(-29))
        assertTrue(res.isSuccess)
        val data = res.getOrThrow()
        assertEquals(PrimeStatus.PRIME, data.status)
        assertEquals(BigInteger.valueOf(29), data.absoluteInput)
    }

    // 15. Negative composite
    @Test
    fun testNegativeComposite() {
        val res = PrimeChecker.checkPrime(BigInteger.valueOf(-36))
        assertTrue(res.isSuccess)
        val data = res.getOrThrow()
        assertEquals(PrimeStatus.COMPOSITE, data.status)
        assertEquals(BigInteger.valueOf(36), data.absoluteInput)
        assertEquals(BigInteger.valueOf(2), data.smallestPrimeFactor)
    }

    // 16. Smallest prime factor
    @Test
    fun testSmallestPrimeFactor() {
        assertEquals(BigInteger.valueOf(2), PrimeChecker.findSmallestPrimeFactor(BigInteger.valueOf(4)))
        assertEquals(BigInteger.valueOf(2), PrimeChecker.findSmallestPrimeFactor(BigInteger.valueOf(36)))
        assertEquals(BigInteger.valueOf(7), PrimeChecker.findSmallestPrimeFactor(BigInteger.valueOf(91)))
        assertNull(PrimeChecker.findSmallestPrimeFactor(BigInteger.valueOf(29)))
        assertNull(PrimeChecker.findSmallestPrimeFactor(BigInteger.valueOf(1)))
        assertNull(PrimeChecker.findSmallestPrimeFactor(BigInteger.valueOf(0)))
    }

    // 17. Factor generation
    @Test
    fun testFactorGeneration() {
        val factorsRes = PrimeFactorizer.findFactors(BigInteger.valueOf(36))
        assertTrue(factorsRes.isSuccess)
        val expected = listOf(1L, 2L, 3L, 4L, 6L, 9L, 12L, 18L, 36L).map { BigInteger.valueOf(it) }
        assertEquals(expected, factorsRes.getOrThrow())
    }

    // 18. Factor pairs
    @Test
    fun testFactorPairs() {
        val pairsRes = PrimeFactorizer.findFactorPairs(BigInteger.valueOf(36))
        assertTrue(pairsRes.isSuccess)
        val pairs = pairsRes.getOrThrow()
        assertEquals(5, pairs.size)
        assertEquals(FactorPair(BigInteger.valueOf(1), BigInteger.valueOf(36)), pairs[0])
        assertEquals(FactorPair(BigInteger.valueOf(2), BigInteger.valueOf(18)), pairs[1])
        assertEquals(FactorPair(BigInteger.valueOf(3), BigInteger.valueOf(12)), pairs[2])
        assertEquals(FactorPair(BigInteger.valueOf(4), BigInteger.valueOf(9)), pairs[3])
        assertEquals(FactorPair(BigInteger.valueOf(6), BigInteger.valueOf(6)), pairs[4])
    }

    // 19. Factor count
    @Test
    fun testFactorCount() {
        val pf36 = listOf(
            PrimeFactor(BigInteger.valueOf(2), 2),
            PrimeFactor(BigInteger.valueOf(3), 2)
        )
        // (2 + 1) * (2 + 1) = 9
        assertEquals(9, PrimeFactorizer.calculateFactorCount(pf36))

        val pf84 = listOf(
            PrimeFactor(BigInteger.valueOf(2), 2),
            PrimeFactor(BigInteger.valueOf(3), 1),
            PrimeFactor(BigInteger.valueOf(7), 1)
        )
        // (2+1)*(1+1)*(1+1) = 12
        assertEquals(12, PrimeFactorizer.calculateFactorCount(pf84))
    }

    // 20. Prime factorization
    @Test
    fun testPrimeFactorization() {
        val pf36 = PrimeFactorizer.factorize(BigInteger.valueOf(36)).getOrThrow()
        assertEquals(
            listOf(
                PrimeFactor(BigInteger.valueOf(2), 2),
                PrimeFactor(BigInteger.valueOf(3), 2)
            ),
            pf36
        )

        val pf84 = PrimeFactorizer.factorize(BigInteger.valueOf(84)).getOrThrow()
        assertEquals(
            listOf(
                PrimeFactor(BigInteger.valueOf(2), 2),
                PrimeFactor(BigInteger.valueOf(3), 1),
                PrimeFactor(BigInteger.valueOf(7), 1)
            ),
            pf84
        )
    }

    // 21. Prime factorization verification
    @Test
    fun testPrimeFactorizationVerification() {
        val valid = listOf(
            PrimeFactor(BigInteger.valueOf(2), 2),
            PrimeFactor(BigInteger.valueOf(3), 2)
        )
        assertTrue(PrimeCheckValidator.verifyPrimeFactorization(BigInteger.valueOf(36), valid))
        assertFalse(PrimeCheckValidator.verifyPrimeFactorization(BigInteger.valueOf(35), valid))
    }

    // 22. Factor-list verification
    @Test
    fun testFactorListVerification() {
        val validFactors = listOf(1L, 2L, 4L).map { BigInteger.valueOf(it) }
        assertTrue(PrimeCheckValidator.verifyFactors(BigInteger.valueOf(4), validFactors))

        val invalidFactors = listOf(1L, 2L, 3L, 4L).map { BigInteger.valueOf(it) }
        assertFalse(PrimeCheckValidator.verifyFactors(BigInteger.valueOf(4), invalidFactors))
    }

    // 23. Integer square root
    @Test
    fun testIntegerSquareRoot() {
        assertEquals(BigInteger.valueOf(6), MathUtils.integerSqrt(BigInteger.valueOf(36)))
        assertEquals(BigInteger.valueOf(6), MathUtils.integerSqrt(BigInteger.valueOf(37)))
        assertEquals(BigInteger.valueOf(5), MathUtils.integerSqrt(BigInteger.valueOf(35)))
        assertEquals(BigInteger.valueOf(7), MathUtils.integerSqrt(BigInteger.valueOf(49)))
        assertEquals(BigInteger.valueOf(0), MathUtils.integerSqrt(BigInteger.ZERO))
        assertEquals(BigInteger.valueOf(1), MathUtils.integerSqrt(BigInteger.ONE))

        // Large integer square root
        val large = BigInteger("1000000000000000000000000000000") // 10^30
        val root = MathUtils.integerSqrt(large)
        assertTrue(root.multiply(root) <= large)
        assertTrue(root.add(BigInteger.ONE).multiply(root.add(BigInteger.ONE)) > large)
    }

    // 24. Large integers
    @Test
    fun testLargeIntegersNoOverflow() {
        // Larger than Long.MAX_VALUE (9223372036854775807)
        val bigVal = BigInteger("922337203685477580812345678901234567890")
        val status = PrimeChecker.isPrime(bigVal)
        assertNotNull(status)
    }

    // 25. Large prime
    @Test
    fun testLargePrime() {
        // Known large prime: 2^31 - 1 = 2147483647 (Mersenne prime M31)
        val m31 = BigInteger.valueOf(2147483647L)
        assertEquals(PrimeStatus.PRIME, PrimeChecker.isPrime(m31))

        // Deterministic large prime beyond Long: 2^61 - 1 = 2305843009213693951
        val m61 = BigInteger("2305843009213693951")
        assertEquals(PrimeStatus.PRIME, PrimeChecker.isPrime(m61))
    }

    // 26. Large composite
    @Test
    fun testLargeComposite() {
        // Product of two primes: 2147483647 * 3 = 6442450941
        val largeComp = BigInteger.valueOf(2147483647L).multiply(BigInteger.valueOf(3))
        assertEquals(PrimeStatus.COMPOSITE, PrimeChecker.isPrime(largeComp))
        assertEquals(BigInteger.valueOf(3), PrimeChecker.findSmallestPrimeFactor(largeComp))
    }

    // 27. Miller-Rabin
    @Test
    fun testMillerRabinAccuracy() {
        // Carmichael numbers (pseudoprimes to Fermat test): 561, 1105, 1729
        // Miller-Rabin correctly identifies Carmichael numbers as COMPOSITE!
        assertEquals(PrimeStatus.COMPOSITE, PrimeChecker.isPrime(BigInteger.valueOf(561)))
        assertEquals(PrimeStatus.COMPOSITE, PrimeChecker.isPrime(BigInteger.valueOf(1105)))
        assertEquals(PrimeStatus.COMPOSITE, PrimeChecker.isPrime(BigInteger.valueOf(1729)))
    }

    // 28. Deterministic primality range
    @Test
    fun testDeterministicPrimalityRange() {
        // Number within deterministic range (<= 3.317 * 10^24)
        val primeWithinDetRange = BigInteger("1000000000000000003") // 10^18 + 3
        val status = PrimeChecker.isPrime(primeWithinDetRange)
        assertEquals(PrimeStatus.PRIME, status)
    }

    // 29. Probable-prime handling
    @Test
    fun testProbablePrimeHandling() {
        // Number well beyond 3.317 * 10^24 (e.g. 50-digit probable prime)
        // 10^40 + 121 or generated big prime
        val largeProbablePrime = BigInteger("10000000000000000000000000000000000000000000000000121")
        val status = PrimeChecker.isPrime(largeProbablePrime)
        // Whether it's composite or probable prime, it should NEVER return false "PRIME" if beyond range
        assertTrue(status == PrimeStatus.PROBABLE_PRIME || status == PrimeStatus.COMPOSITE)
    }

    // 30. Calculation timeout
    @Test
    fun testCalculationTimeout() {
        // Start time in the past to trigger timeout
        val pastTime = System.currentTimeMillis() - 6000L
        val timeoutResult = PrimeFactorizer.factorize(
            number = BigInteger.valueOf(123456789),
            startTimeMs = pastTime
        )
        assertTrue(timeoutResult.isFailure)
        assertTrue(timeoutResult.exceptionOrNull() is TimeoutException)
    }

    // 31. Calculation cancellation
    @Test
    fun testCalculationCancellation() {
        val cancelledResult = PrimeFactorizer.factorize(
            number = BigInteger.valueOf(123456789),
            checkCancellation = { true }
        )
        assertTrue(cancelledResult.isFailure)
        assertTrue(cancelledResult.exceptionOrNull() is CancellationException)
    }
}
