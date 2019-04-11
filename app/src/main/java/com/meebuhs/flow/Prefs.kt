package com.meebuhs.flow

import android.content.Context
import android.content.SharedPreferences

/**
 * Added enum support to SharedPreference helper from Krupal Shah's work here
 * https://medium.com/@krupalshah55/manipulating-shared-prefs-with-kotlin-just-two-lines-of-code-29af62440285
 */
object Prefs {
    private const val PREFS_FILENAME = "com.meebuhs.flow.prefs"

    fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    inline operator fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T? = null): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Unsupported type")
        }
    }

    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is String? -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Unsupported type")
        }
    }

    inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, default: T) =
        this.getInt(key, -1).let { if (it >= 0) enumValues<T>()[it] else default }

    inline fun <reified T : Enum<T>> SharedPreferences.setEnum(key: String, value: T?) {
        edit { it.putInt(key, value?.ordinal ?: -1) }
    }
}