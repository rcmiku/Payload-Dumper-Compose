package com.rcmiku.payload.dumper.compose.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesUtil {
    private var context = AppContextUtil.context
    private val sharedPreferences: SharedPreferences? =
        context.getSharedPreferences("PayloadDumper", Context.MODE_PRIVATE)

    fun perfSet(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    fun perfGet(key: String): String? {
        return sharedPreferences?.getString(key, null)
    }

    fun perfSet(key: String, value: Boolean) {
        sharedPreferences?.edit()?.putBoolean(key, value)?.apply()
    }

    fun perfGetBoolean(key: String): Boolean? {
        return sharedPreferences?.getBoolean(key, false)
    }

    fun perfRemove(key: String) {
        sharedPreferences?.edit()?.remove(key)?.apply()
    }
}