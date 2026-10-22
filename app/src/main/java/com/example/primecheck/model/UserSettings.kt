package com.example.primecheck.model

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val showFactors: Boolean = true,
    val showFactorPairs: Boolean = true,
    val showCalculationSteps: Boolean = true
)
