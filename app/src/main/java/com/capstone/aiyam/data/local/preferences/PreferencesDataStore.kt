package com.capstone.aiyam.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.notificationPreferences: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")
val Context.emailPreferences: DataStore<Preferences> by preferencesDataStore(name = "email_preferences")
val Context.settingsPreferences: DataStore<Preferences> by preferencesDataStore(name = "settings_preferences")
val Context.telegramPreferences: DataStore<Preferences> by preferencesDataStore(name = "telegram_preferences")
val Context.onBoardingPreferences: DataStore<Preferences> by preferencesDataStore(name = "on_boarding_preferences")
