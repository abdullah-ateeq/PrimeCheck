package com.example.primecheck.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.primecheck.model.CheckMode
import com.example.primecheck.model.PrimeCheckHistory
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class HistoryRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    companion object {
        private const val PREFS_NAME = "prime_check_history"
        private const val KEY_HISTORY = "history_items"
        const val MAX_HISTORY_ITEMS = 10
    }

    /**
     * Loads the saved history items in order (newest first).
     */
    fun getHistory(): List<PrimeCheckHistory> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<PrimeCheckHistory>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val input = obj.getString("input")
                val modeStr = obj.optString("mode", CheckMode.SINGLE.name)
                val mode = try {
                    CheckMode.valueOf(modeStr)
                } catch (e: Exception) {
                    CheckMode.SINGLE
                }
                val savedAt = obj.optLong("savedAt", System.currentTimeMillis())
                list.add(PrimeCheckHistory(id = id, input = input, mode = mode, savedAt = savedAt))
            }
        } catch (e: Exception) {
            // If corrupted, return empty
            return emptyList()
        }
        return list
    }

    /**
     * Saves a calculation.
     * Prevents consecutive duplicates.
     * Caps at maximum 10 items (evicting oldest).
     * Returns true if saved, false if skipped due to consecutive duplicate.
     */
    fun saveCalculation(input: String, mode: CheckMode): Boolean {
        val current = getHistory().toMutableList()

        // Check consecutive duplicate: if top item has same input and mode, skip
        if (current.isNotEmpty() && current.first().input.trim() == input.trim() && current.first().mode == mode) {
            return false
        }

        val newItem = PrimeCheckHistory(
            id = UUID.randomUUID().toString(),
            input = input.trim(),
            mode = mode,
            savedAt = System.currentTimeMillis()
        )

        // Insert at beginning
        current.add(0, newItem)

        // Cap at MAX_HISTORY_ITEMS (10)
        while (current.size > MAX_HISTORY_ITEMS) {
            current.removeAt(current.size - 1)
        }

        saveList(current)
        return true
    }

    /**
     * Deletes a specific history item by ID.
     */
    fun deleteItem(id: String) {
        val current = getHistory().toMutableList()
        current.removeAll { it.id == id }
        saveList(current)
    }

    /**
     * Clears all history items.
     */
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveList(list: List<PrimeCheckHistory>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("input", item.input)
            obj.put("mode", item.mode.name)
            obj.put("savedAt", item.savedAt)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }
}
