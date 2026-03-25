package com.terrass.app.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenProvider @Inject constructor(
    @ApplicationContext ctx: Context,
) {
    private val prefs = ctx.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString("auth_token", null)
    fun getPassword(): String? = prefs.getString("device_password", null)

    fun saveToken(token: String) = prefs.edit().putString("auth_token", token).apply()
    fun savePassword(pwd: String) = prefs.edit().putString("device_password", pwd).apply()
    fun clearToken() = prefs.edit().remove("auth_token").apply()
}
