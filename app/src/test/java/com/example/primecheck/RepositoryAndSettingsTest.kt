package com.example.primecheck

import com.example.primecheck.data.repository.HistoryRepository
import com.example.primecheck.data.repository.SettingsRepository
import com.example.primecheck.model.AppTheme
import com.example.primecheck.model.CheckMode
import com.example.primecheck.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositoryAndSettingsTest {

    private lateinit var fakePrefsHistory: FakeSharedPreferences
    private lateinit var fakePrefsSettings: FakeSharedPreferences
    private lateinit var historyRepo: HistoryRepository
    private lateinit var settingsRepo: SettingsRepository

    @Before
    fun setup() {
        fakePrefsHistory = FakeSharedPreferences()
        fakePrefsSettings = FakeSharedPreferences()
        historyRepo = HistoryRepository(fakePrefsHistory)
        settingsRepo = SettingsRepository(fakePrefsSettings)
    }

    // 38. History save
    @Test
    fun testHistorySave() {
        assertTrue(historyRepo.getHistory().isEmpty())
        val saved = historyRepo.saveCalculation("36", CheckMode.SINGLE)
        assertTrue(saved)
        val list = historyRepo.getHistory()
        assertEquals(1, list.size)
        assertEquals("36", list[0].input)
        assertEquals(CheckMode.SINGLE, list[0].mode)
    }

    // 39. History restore
    @Test
    fun testHistoryRestore() {
        historyRepo.saveCalculation("29", CheckMode.SINGLE)
        val item = historyRepo.getHistory().first()
        assertEquals("29", item.input)
        assertEquals(CheckMode.SINGLE, item.mode)
    }

    // 40. History limit (max 10 entries, evicts oldest)
    @Test
    fun testHistoryLimitMaxTen() {
        for (i in 1..15) {
            historyRepo.saveCalculation("item_$i", CheckMode.SINGLE)
        }
        val history = historyRepo.getHistory()
        assertEquals(10, history.size)
        // Newest should be item_15, oldest remaining item_6
        assertEquals("item_15", history.first().input)
        assertEquals("item_6", history.last().input)
    }

    // 41. Duplicate history (consecutive duplicates rejected)
    @Test
    fun testConsecutiveDuplicateHistoryRejected() {
        val firstSave = historyRepo.saveCalculation("36", CheckMode.SINGLE)
        assertTrue(firstSave)
        val secondSave = historyRepo.saveCalculation("36", CheckMode.SINGLE)
        assertFalse(secondSave)
        assertEquals(1, historyRepo.getHistory().size)

        // But non-consecutive duplicate is allowed
        historyRepo.saveCalculation("29", CheckMode.SINGLE)
        val thirdSave = historyRepo.saveCalculation("36", CheckMode.SINGLE)
        assertTrue(thirdSave)
        assertEquals(3, historyRepo.getHistory().size)
    }

    // 42. History deletion
    @Test
    fun testHistoryDeletionSingleAndClearAll() {
        historyRepo.saveCalculation("11", CheckMode.SINGLE)
        historyRepo.saveCalculation("13", CheckMode.SINGLE)
        val items = historyRepo.getHistory()
        assertEquals(2, items.size)

        // Delete single
        historyRepo.deleteItem(items[0].id)
        val afterDelete = historyRepo.getHistory()
        assertEquals(1, afterDelete.size)
        assertEquals("11", afterDelete[0].input)

        // Clear all
        historyRepo.clearHistory()
        assertTrue(historyRepo.getHistory().isEmpty())
    }

    // 43. Settings persistence
    @Test
    fun testSettingsPersistence() {
        val customSettings = UserSettings(
            theme = AppTheme.DARK,
            dynamicColor = false,
            hapticFeedback = false,
            animations = false,
            confirmDelete = false,
            showFactors = false,
            showFactorPairs = false,
            showCalculationSteps = false
        )
        settingsRepo.updateSettings(customSettings)

        // Re-read from storage
        val loaded = settingsRepo.loadSettings()
        assertEquals(AppTheme.DARK, loaded.theme)
        assertFalse(loaded.dynamicColor)
        assertFalse(loaded.hapticFeedback)
        assertFalse(loaded.animations)
        assertFalse(loaded.confirmDelete)
        assertFalse(loaded.showFactors)
        assertFalse(loaded.showFactorPairs)
        assertFalse(loaded.showCalculationSteps)
    }

    // 44. Settings reset (restores defaults without touching history)
    @Test
    fun testSettingsResetKeepsHistory() {
        historyRepo.saveCalculation("99", CheckMode.SINGLE)
        settingsRepo.updateSettings(
            UserSettings(
                theme = AppTheme.DARK,
                dynamicColor = false,
                hapticFeedback = false
            )
        )

        val resetResult = settingsRepo.resetSettings()
        assertEquals(AppTheme.SYSTEM, resetResult.theme)
        assertTrue(resetResult.dynamicColor)
        assertTrue(resetResult.hapticFeedback)
        assertTrue(resetResult.animations)
        assertTrue(resetResult.confirmDelete)
        assertTrue(resetResult.showFactors)
        assertTrue(resetResult.showFactorPairs)
        assertTrue(resetResult.showCalculationSteps)

        // Verify history is untouched
        assertEquals(1, historyRepo.getHistory().size)
        assertEquals("99", historyRepo.getHistory()[0].input)
    }
}
