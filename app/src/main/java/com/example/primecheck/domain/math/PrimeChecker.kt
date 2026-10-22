package com.example.primecheck.domain.math

import com.example.primecheck.model.CalculationStep
import com.example.primecheck.model.FactorPair
import com.example.primecheck.model.PrimeCheckResult
import com.example.primecheck.model.PrimeFactor
import com.example.primecheck.model.PrimeStatus
import java.math.BigInteger
import java.security.SecureRandom
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException

object PrimeChecker {

    // Miller-Rabin deterministic bases for numbers up to 3.317 * 10^24
    private val DETERMINISTIC_BASES = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41)
    private val DETERMINISTIC_LIMIT = BigInteger("3317044064679887385961981")

    // Small prime numbers for fast trial division and trial explanation
    val SMALL_PRIMES = intArrayOf(
        2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47,
        53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113,
        127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191,
        193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251
    )

    private val secureRandom = SecureRandom()

    /**
     * Determines primality status of any BigInteger.
     */
    fun isPrime(number: BigInteger): PrimeStatus {
        val n = number.abs()
        if (n < BigInteger.valueOf(2)) return PrimeStatus.NEITHER
        if (n == BigInteger.valueOf(2) || n == BigInteger.valueOf(3)) return PrimeStatus.PRIME
        if (!n.testBit(0)) return PrimeStatus.COMPOSITE

        // Quick check against small primes
        for (p in SMALL_PRIMES) {
            val bp = BigInteger.valueOf(p.toLong())
            if (n == bp) return PrimeStatus.PRIME
            if (n.mod(bp) == BigInteger.ZERO) return PrimeStatus.COMPOSITE
        }

        // Deterministic range check
        if (n <= DETERMINISTIC_LIMIT) {
            val isDetPrime = millerRabinDeterministic(n)
            return if (isDetPrime) PrimeStatus.PRIME else PrimeStatus.COMPOSITE
        }

        // For very large numbers beyond the deterministic limit:
        // Run Miller-Rabin with 40 rounds
        val passesProbable = millerRabinRandom(n, rounds = 40)
        return if (passesProbable) PrimeStatus.PROBABLE_PRIME else PrimeStatus.COMPOSITE
    }

    /**
     * Finds the smallest non-trivial divisor (which is always a prime factor) for a composite number.
     * Returns null if number < 2 or if number is prime.
     */
    fun findSmallestPrimeFactor(
        number: BigInteger,
        startTimeMs: Long = System.currentTimeMillis(),
        checkCancellation: () -> Boolean = { false }
    ): BigInteger? {
        val n = number.abs()
        if (n < BigInteger.valueOf(2)) return null
        if (n == BigInteger.valueOf(2) || n == BigInteger.valueOf(3)) return null

        // 1. Check 2
        if (!n.testBit(0)) return BigInteger.valueOf(2)

        // 2. Check small primes
        for (p in SMALL_PRIMES) {
            val bp = BigInteger.valueOf(p.toLong())
            if (bp.multiply(bp) > n) break
            if (n.mod(bp) == BigInteger.ZERO) return bp
        }

        // 3. Trial division up to min(sqrt(N), 200,000)
        val sqrt = MathUtils.integerSqrt(n)
        val trialLimit = sqrt.min(BigInteger.valueOf(200_000))
        var candidate = BigInteger.valueOf(253) // after small primes table
        var steps = 0

        while (candidate <= trialLimit) {
            if (++steps % 1000 == 0) {
                if (checkCancellation()) return null
                if (System.currentTimeMillis() - startTimeMs > 4000) break
            }
            if (n.mod(candidate) == BigInteger.ZERO) {
                return candidate
            }
            candidate = candidate.add(BigInteger.valueOf(2))
        }

        // 4. If N <= trialLimit^2, and no divisor found, N is prime
        if (sqrt <= trialLimit) {
            return null
        }

        // 5. If known composite from Miller-Rabin but factor not found yet, try Pollard's rho
        if (isPrime(n) == PrimeStatus.COMPOSITE) {
            val rhoFactor = pollardsRho(n, startTimeMs, checkCancellation)
            if (rhoFactor != null && rhoFactor > BigInteger.ONE && rhoFactor < n) {
                // If rho factor is composite, its smallest factor is even smaller
                val subFactor = findSmallestPrimeFactor(rhoFactor, startTimeMs, checkCancellation)
                return subFactor ?: rhoFactor
            }
        }

        return null
    }

    /**
     * Full primality check with factors, factor pairs, prime factorization, perfect square test,
     * calculation steps, timeout enforcement, and verification.
     */
    fun checkPrime(
        number: BigInteger,
        startTimeMs: Long = System.currentTimeMillis(),
        checkCancellation: () -> Boolean = { false }
    ): Result<PrimeCheckResult> {
        if (checkCancellation()) {
            return Result.failure(CancellationException("Calculation cancelled."))
        }

        val absInput = number.abs()
        val status = isPrime(absInput)
        val isSq = MathUtils.isPerfectSquare(absInput)
        val sqrtVal = if (absInput >= BigInteger.ZERO) MathUtils.integerSqrt(absInput) else null

        // Build explanation steps
        val explanation = buildExplanation(number, absInput, status, sqrtVal)

        // Case: Neither (0 or 1)
        if (status == PrimeStatus.NEITHER) {
            val factors: List<BigInteger>? = if (absInput == BigInteger.ONE) listOf(BigInteger.ONE) else null
            val factorPairs: List<FactorPair>? = if (absInput == BigInteger.ONE) {
                listOf(FactorPair(BigInteger.ONE, BigInteger.ONE))
            } else null
            val primeFactors: List<PrimeFactor>? = if (absInput == BigInteger.ONE) emptyList() else null

            return Result.success(
                PrimeCheckResult(
                    input = number,
                    absoluteInput = absInput,
                    status = status,
                    smallestPrimeFactor = null,
                    factors = factors,
                    factorPairs = factorPairs,
                    factorCount = if (absInput == BigInteger.ONE) 1 else null,
                    primeFactors = primeFactors,
                    isPerfectSquare = isSq,
                    squareRoot = sqrtVal,
                    explanation = explanation,
                    calculatedAt = System.currentTimeMillis()
                )
            )
        }

        // Case: Prime or Probable Prime
        if (status == PrimeStatus.PRIME || status == PrimeStatus.PROBABLE_PRIME) {
            val primeFactors = listOf(PrimeFactor(absInput, 1))
            val (factors, factorPairs) = if (absInput <= BigInteger.valueOf(100_000_000_000L)) {
                listOf(BigInteger.ONE, absInput) to listOf(FactorPair(BigInteger.ONE, absInput))
            } else {
                null to null
            }

            return Result.success(
                PrimeCheckResult(
                    input = number,
                    absoluteInput = absInput,
                    status = status,
                    smallestPrimeFactor = null,
                    factors = factors,
                    factorPairs = factorPairs,
                    factorCount = 2,
                    primeFactors = primeFactors,
                    isPerfectSquare = false,
                    squareRoot = sqrtVal,
                    explanation = explanation,
                    calculatedAt = System.currentTimeMillis()
                )
            )
        }

        // Case: Composite
        val smallestFactor = findSmallestPrimeFactor(absInput, startTimeMs, checkCancellation)

        // Verify composite witness if found
        if (smallestFactor != null && !PrimeCheckValidator.verifyComposite(absInput, smallestFactor)) {
            return Result.failure(IllegalStateException("Internal divisor verification failed."))
        }

        // Attempt factorization and factor generation if within safe limits
        var primeFactors: List<PrimeFactor>? = null
        var factors: List<BigInteger>? = null
        var factorPairs: List<FactorPair>? = null
        var factorCount: Int? = null

        val factorizeResult = PrimeFactorizer.factorize(absInput, startTimeMs, checkCancellation)
        if (factorizeResult.isSuccess) {
            primeFactors = factorizeResult.getOrNull()
            if (primeFactors != null) {
                factorCount = PrimeFactorizer.calculateFactorCount(primeFactors)
            }

            val factorsResult = PrimeFactorizer.findFactors(absInput, startTimeMs, checkCancellation)
            if (factorsResult.isSuccess) {
                factors = factorsResult.getOrNull()
            }

            val pairsResult = PrimeFactorizer.findFactorPairs(absInput, startTimeMs, checkCancellation)
            if (pairsResult.isSuccess) {
                factorPairs = pairsResult.getOrNull()
            }
        }

        return Result.success(
            PrimeCheckResult(
                input = number,
                absoluteInput = absInput,
                status = status,
                smallestPrimeFactor = smallestFactor,
                factors = factors,
                factorPairs = factorPairs,
                factorCount = factorCount,
                primeFactors = primeFactors,
                isPerfectSquare = isSq,
                squareRoot = sqrtVal,
                explanation = explanation,
                calculatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun buildExplanation(
        original: BigInteger,
        absVal: BigInteger,
        status: PrimeStatus,
        sqrtVal: BigInteger?
    ): List<CalculationStep> {
        val steps = mutableListOf<CalculationStep>()

        if (original < BigInteger.ZERO) {
            steps.add(
                CalculationStep(
                    label = "Absolute value",
                    expression = "|$original|",
                    result = "$absVal"
                )
            )
        }

        when (absVal) {
            BigInteger.ZERO -> {
                steps.add(
                    CalculationStep(
                        label = "Definition",
                        expression = "0",
                        result = "0 is neither prime nor composite."
                    )
                )
                return steps
            }
            BigInteger.ONE -> {
                steps.add(
                    CalculationStep(
                        label = "Definition",
                        expression = "1",
                        result = "1 has only one positive factor, so it is not prime."
                    )
                )
                return steps
            }
            BigInteger.valueOf(2) -> {
                steps.add(
                    CalculationStep(
                        label = "Definition",
                        expression = "2",
                        result = "2 is the only even prime number (factors: 1 and 2)."
                    )
                )
                return steps
            }
        }

        // Small practical numbers trial division explanation
        val sqrtStr = MathUtils.formatApproxSquareRoot(absVal)
        steps.add(
            CalculationStep(
                label = "Square root bound",
                expression = "√$absVal",
                result = sqrtStr
            )
        )

        if (sqrtVal != null && sqrtVal <= BigInteger.valueOf(100)) {
            // Explain trial division up to sqrt(N)
            val testedPrimes = SMALL_PRIMES.filter { BigInteger.valueOf(it.toLong()) <= sqrtVal }
            var foundDivisor = false
            for (p in testedPrimes) {
                val bp = BigInteger.valueOf(p.toLong())
                val isDiv = absVal.mod(bp) == BigInteger.ZERO
                if (isDiv) {
                    val quotient = absVal.divide(bp)
                    steps.add(
                        CalculationStep(
                            label = "Test $p",
                            expression = "$absVal ÷ $p = $quotient",
                            result = "Divisible ✓"
                        )
                    )
                    foundDivisor = true
                    break
                } else {
                    steps.add(
                        CalculationStep(
                            label = "Test $p",
                            expression = "$absVal mod $p",
                            result = "Not divisible"
                        )
                    )
                }
            }

            if (!foundDivisor) {
                steps.add(
                    CalculationStep(
                        label = "Conclusion",
                        expression = "No divisor found up to √$absVal",
                        result = "Therefore, $absVal is prime."
                    )
                )
            } else {
                steps.add(
                    CalculationStep(
                        label = "Conclusion",
                        expression = "Divisor found",
                        result = "Therefore, $absVal is composite."
                    )
                )
            }
        } else {
            // Larger numbers explanation
            when (status) {
                PrimeStatus.PRIME -> {
                    steps.add(
                        CalculationStep(
                            label = "Primality test",
                            expression = "Miller-Rabin test across bases 2..41",
                            result = "Passed all deterministic bases. Therefore, prime."
                        )
                    )
                }
                PrimeStatus.COMPOSITE -> {
                    val factor = findSmallestPrimeFactor(absVal)
                    if (factor != null) {
                        val q = absVal.divide(factor)
                        steps.add(
                            CalculationStep(
                                label = "Divisor witness",
                                expression = "$absVal ÷ $factor",
                                result = "= $q"
                            )
                        )
                    }
                    steps.add(
                        CalculationStep(
                            label = "Conclusion",
                            expression = "Has non-trivial divisor",
                            result = "Therefore, composite."
                        )
                    )
                }
                PrimeStatus.PROBABLE_PRIME -> {
                    steps.add(
                        CalculationStep(
                            label = "Primality test",
                            expression = "Miller-Rabin (40 rounds)",
                            result = "This number passed a high-confidence primality test."
                        )
                    )
                }
                else -> {}
            }
        }

        return steps
    }

    /**
     * Deterministic Miller-Rabin test using deterministic bases up to 41.
     */
    private fun millerRabinDeterministic(n: BigInteger): Boolean {
        // Write n - 1 as 2^s * d
        val nMinusOne = n.subtract(BigInteger.ONE)
        var s = 0
        var d = nMinusOne
        while (!d.testBit(0)) {
            s++
            d = d.shiftRight(1)
        }

        for (baseInt in DETERMINISTIC_BASES) {
            val a = BigInteger.valueOf(baseInt.toLong())
            if (a >= n) break
            if (!millerRabinPass(a, d, s, n, nMinusOne)) {
                return false
            }
        }
        return true
    }

    /**
     * Probabilistic Miller-Rabin test for very large integers.
     */
    private fun millerRabinRandom(n: BigInteger, rounds: Int = 40): Boolean {
        val nMinusOne = n.subtract(BigInteger.ONE)
        var s = 0
        var d = nMinusOne
        while (!d.testBit(0)) {
            s++
            d = d.shiftRight(1)
        }

        for (baseInt in DETERMINISTIC_BASES) {
            val a = BigInteger.valueOf(baseInt.toLong())
            if (a >= n) break
            if (!millerRabinPass(a, d, s, n, nMinusOne)) {
                return false
            }
        }

        // Additional random rounds
        val maxRandomAttempts = rounds - DETERMINISTIC_BASES.size
        for (i in 0 until maxRandomAttempts) {
            var a: BigInteger
            do {
                a = BigInteger(n.bitLength(), secureRandom)
            } while (a < BigInteger.valueOf(2) || a >= nMinusOne)

            if (!millerRabinPass(a, d, s, n, nMinusOne)) {
                return false
            }
        }
        return true
    }

    private fun millerRabinPass(
        a: BigInteger,
        d: BigInteger,
        s: Int,
        n: BigInteger,
        nMinusOne: BigInteger
    ): Boolean {
        var x = a.modPow(d, n)
        if (x == BigInteger.ONE || x == nMinusOne) {
            return true
        }
        for (r in 1 until s) {
            x = x.modPow(BigInteger.valueOf(2), n)
            if (x == nMinusOne) {
                return true
            }
            if (x == BigInteger.ONE) {
                return false
            }
        }
        return false
    }

    /**
     * Pollard's rho algorithm with Brent's cycle detection for composite numbers.
     */
    private fun pollardsRho(
        n: BigInteger,
        startTimeMs: Long,
        checkCancellation: () -> Boolean
    ): BigInteger? {
        if (!n.testBit(0)) return BigInteger.valueOf(2)
        var c = BigInteger.ONE
        while (c < BigInteger.valueOf(20)) {
            var y = BigInteger.valueOf(2)
            var x = BigInteger.valueOf(2)
            val cVal = c
            val m = BigInteger.valueOf(128)
            var g = BigInteger.ONE
            var r = 1L
            var q = BigInteger.ONE

            var step = 0
            while (g == BigInteger.ONE) {
                if (++step % 500 == 0) {
                    if (checkCancellation() || System.currentTimeMillis() - startTimeMs > 4500) {
                        return null
                    }
                }
                x = y
                for (i in 1..r) {
                    y = (y.multiply(y).add(cVal)).mod(n)
                }
                var k = 0L
                while (k < r && g == BigInteger.ONE) {
                    val ys = y
                    val condition = minOf(m.toLong(), r - k)
                    for (i in 1..condition) {
                        y = (y.multiply(y).add(cVal)).mod(n)
                        val diff = if (x >= y) x.subtract(y) else y.subtract(x)
                        q = (q.multiply(diff)).mod(n)
                    }
                    g = q.gcd(n)
                    k += m.toLong()
                }
                r *= 2
            }

            if (g == n) {
                // Backtrack if cycle failed
                var ys2 = y
                do {
                    ys2 = (ys2.multiply(ys2).add(cVal)).mod(n)
                    val diff = if (x >= ys2) x.subtract(ys2) else ys2.subtract(x)
                    g = diff.gcd(n)
                } while (g == BigInteger.ONE)
            }

            if (g > BigInteger.ONE && g < n) {
                return g
            }
            c = c.add(BigInteger.ONE)
        }
        return null
    }
}
