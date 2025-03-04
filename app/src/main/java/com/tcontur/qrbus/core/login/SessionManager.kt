package com.tcontur.qrbus.core.login

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.tcontur.qrbus.client.models.EmpresaModel

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMPRESA = "empresa_model"
        private const val KEY_REMEMBER_ME = "remember_me"
    }

    fun saveAuthToken(token: String, username: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    fun isRememberMe(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun saveRememberMe(remenberMe: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, remenberMe).apply()
    }


    fun getAuthToken(): String {
        return "Token " + prefs.getString(KEY_TOKEN, null)
    }

    fun isLogged(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }
    fun saveUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun clearSession() {
//        prefs.edit().clear().apply()
        prefs.edit().remove(KEY_TOKEN).apply()

//        prefs.edit().remove(KEY_USERNAME).apply()
//        prefs.edit().remove(KEY_EMPRESA).apply()
    }

    fun saveEmpresa(empresa: EmpresaModel?) {
        val json = gson.toJson(empresa)
        prefs.edit().putString(KEY_EMPRESA, json).apply()
    }

    // Método para recuperar la empresa guardada
    fun getEmpresa(): EmpresaModel? {
        val json = prefs.getString(KEY_EMPRESA, null)
        return if (json != null) {
            gson.fromJson(json, EmpresaModel::class.java)
        } else {
            null
        }
    }
}
