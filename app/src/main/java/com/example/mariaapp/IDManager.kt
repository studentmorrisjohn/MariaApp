package com.example.mariaapp

import android.content.Context
import java.util.Random

object IDManager {
    private const val PREFS_NAME = "com.example.mariaapp"
    private const val KEY_ID = "ID"
    private const val KEY_EXPIRATION_TIME = "ExpirationTime"
    private const val expirationDuration: Long = 24 * 60 * 60 * 1000 // 24 hours in milliseconds

    private fun generateRandomID(): String {
        val validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val sb = StringBuilder(4)

        repeat(4) {
            val randomChar = validChars[random.nextInt(validChars.length)]
            sb.append(randomChar)
        }

        return sb.toString()
    }

    private fun storeID(context: Context, id: String, expirationTime: Long) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_ID, id)
        editor.putLong(KEY_EXPIRATION_TIME, expirationTime)
        editor.apply()
    }

    fun getStoredID(context: Context): Pair<String?, Long> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = sharedPreferences.getString(KEY_ID, null)
        val expirationTime = sharedPreferences.getLong(KEY_EXPIRATION_TIME, 0L)
        return id to expirationTime
    }

    fun isIDValid(context: Context): Boolean {
        val (id, expirationTime) = getStoredID(context)
        val currentTime = System.currentTimeMillis()
        return id != null && currentTime <= expirationTime
    }

    fun generateAndStoreID(context: Context) {
        if (!isIDValid(context)) {
            val id = generateRandomID()
            val expirationTime = System.currentTimeMillis() + expirationDuration
            storeID(context, id, expirationTime)
        }
    }
}