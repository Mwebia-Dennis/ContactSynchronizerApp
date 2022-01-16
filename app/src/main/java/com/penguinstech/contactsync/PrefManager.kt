package com.penguinstech.contactsync

import android.content.Context
import android.content.SharedPreferences

class PrefManager(_context: Context) {

    // shared pref mode
    private var PRIVATE_MODE = 0
    var count: Int
        get() = pref.getInt("count", 0)
        set(count) {
            editor.putInt("count", count)
            editor.commit()
        }
    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }

    companion object {
        lateinit var pref: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
        // Shared preferences file name
        private const val PREF_NAME = "welcome"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}