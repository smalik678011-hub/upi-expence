package com.example.core.security

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesSecureStorage(context: Context) : SecureStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "upi_expense_secure_prefs",
        Context.MODE_PRIVATE
    )

    override fun getString(key: String, defaultValue: String?): String? {
        return prefs.getString(key, defaultValue)
    }

    override fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
