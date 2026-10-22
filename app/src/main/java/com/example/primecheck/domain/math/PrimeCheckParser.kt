package com.example.primecheck.domain.math

import java.math.BigInteger

object PrimeCheckParser {

    /**
     * Parses a single whole number string into a BigInteger.
     * Returns failure if invalid or out of constraints.
     */
    fun parseSingleInput(input: String): Result<BigInteger> {
        val error = PrimeCheckValidator.validateSingleInput(input)
        if (error != null) {
            return Result.failure(IllegalArgumentException(error))
        }

        return try {
            val trimmed = input.trim()
            // BigInteger parses leading '+' in Java/Kotlin or we can strip leading '+'
            val normalized = if (trimmed.startsWith("+")) trimmed.substring(1) else trimmed
            val parsed = BigInteger(normalized)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a valid whole number."))
        }
    }

    /**
     * Parses batch input string into a list of individual items.
     * Supports one number per line or comma-separated values.
     * Ignores empty lines.
     * Preserves input order and duplicates.
     * Enforces batch limit (maximum 50 numbers) and input length limit (10,000 chars).
     */
    fun parseBatchInput(input: String): Result<List<String>> {
        val lengthError = PrimeCheckValidator.validateBatchInputLength(input)
        if (lengthError != null) {
            return Result.failure(IllegalArgumentException(lengthError))
        }

        // Split by lines and commas
        val rawItems = input.split(Regex("[\n\r,]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (rawItems.size > PrimeCheckValidator.MAX_BATCH_ITEMS_COUNT) {
            return Result.failure(IllegalArgumentException("Maximum 50 numbers per batch."))
        }

        return Result.success(rawItems)
    }
}
