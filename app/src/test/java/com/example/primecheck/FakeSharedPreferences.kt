package com.example.primecheck

import android.content.SharedPreferences

class FakeSharedPreferences : SharedPreferences {

    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = HashMap(map)

    override fun getString(key: String?, defValue: String?): String? {
        return (map[key] as? String) ?: defValue
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return (map[key] as? MutableSet<String>) ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return (map[key] as? Int) ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return (map[key] as? Long) ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return (map[key] as? Float) ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return (map[key] as? Boolean) ?: defValue
    }

    override fun contains(key: String?): Boolean {
        return map.containsKey(key)
    }

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    inner class Editor : SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any?>()
        private val removed = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            if (key != null) temp[key] = values
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            if (key != null) removed.add(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clearAll = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearAll) {
                map.clear()
            }
            for (key in removed) {
                map.remove(key)
            }
            map.putAll(temp)
        }
    }
}
