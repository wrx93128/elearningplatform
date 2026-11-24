package com.example.e_learning_platform.auth.data

import android.content.Context
import android.content.SharedPreferences
import com.example.e_learning_platform.auth.model.User

interface ISessionManager {
    fun saveUserSession(user: User)
    fun clearSession()
    fun isLoggedIn(): Boolean
    fun getCurrentUser(): User?
}

open class SessionManager(context: Context) : ISessionManager {

    private val prefs: SharedPreferences? = try {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    } catch (_: Exception) {
        null
    }

    override fun saveUserSession(user: User) {
        prefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, true)
            ?.putString(KEY_USER_ID, user.id)
            ?.putString(KEY_USER_NAME, user.name)
            ?.putString(KEY_USER_EMAIL, user.email)
            ?.apply()
    }

    override fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }

    override fun isLoggedIn(): Boolean {
        return prefs?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
    }

    override fun getCurrentUser(): User? {
        if (!isLoggedIn()) return null
        val id = prefs?.getString(KEY_USER_ID, null) ?: return null
        val name = prefs?.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs?.getString(KEY_USER_EMAIL, "") ?: ""
        return User(id = id, name = name, email = email)
    }

    companion object {
        private const val PREFS_NAME = "auth_session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }
}
