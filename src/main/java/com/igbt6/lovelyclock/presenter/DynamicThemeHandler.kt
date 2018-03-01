package com.igbt6.lovelyclock.presenter

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.igbt6.lovelyclock.R
import com.igbt6.lovelyclock.alert.AlarmAlertFullScreen

class DynamicThemeHandler(context: Context) {
    private val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val KEY_THEME = "theme"

    init {
        when (sp.getString(KEY_THEME, "light")) {
            "light", "dark" -> {
            }
            else -> {
                sp.edit().putString(KEY_THEME, "dark").apply()
            }
        }
    }

    fun defaultTheme(): Int = when (preference()) {
        "light" -> R.style.DefaultLightTheme
        "dark" -> R.style.DefaultDarkTheme
        else -> R.style.DefaultDarkTheme
    }

    private fun preference(): String = sp.getString(KEY_THEME, "dark")

    fun getIdForName(name: String): Int = when {
        preference() == "light" && name.equals(AlarmAlertFullScreen::class.java.name) -> R.style.AlarmAlertFullScreenLightTheme
        preference() == "light" && name.equals(TimePickerDialogFragment::class.java.name) -> R.style.TimePickerDialogFragmentLight
        preference() == "dark" && name.equals(AlarmAlertFullScreen::class.java.name) -> R.style.AlarmAlertFullScreenDarkTheme
        preference() == "dark" && name.equals(TimePickerDialogFragment::class.java.name) -> R.style.TimePickerDialogFragmentDark
        else -> defaultTheme()
    }
}
