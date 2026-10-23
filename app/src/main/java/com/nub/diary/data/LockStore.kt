package com.nub.diary.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Хранит PIN только в виде соленого PBKDF2-хеша. */
class LockStore(context: Context) {
    private val prefs = context.getSharedPreferences("lock", Context.MODE_PRIVATE)

    val hasPin: Boolean get() = prefs.contains(KEY_HASH)

    var biometric: Boolean
        get() = prefs.getBoolean(KEY_BIO, false)
        set(value) = prefs.edit().putBoolean(KEY_BIO, value).apply()

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, Base64.encodeToString(hash(pin, salt), Base64.NO_WRAP))
            .apply()
    }

    fun check(pin: String): Boolean {
        val salt = Base64.decode(prefs.getString(KEY_SALT, null) ?: return false, Base64.NO_WRAP)
        val expected = Base64.decode(prefs.getString(KEY_HASH, null) ?: return false, Base64.NO_WRAP)
        return MessageDigest.isEqual(expected, hash(pin, salt))
    }

    fun clear() = prefs.edit().clear().apply()

    private fun hash(pin: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(pin.toCharArray(), salt, 120_000, 256))
            .encoded

    private companion object {
        const val KEY_SALT = "salt"
        const val KEY_HASH = "hash"
        const val KEY_BIO = "bio"
    }
}
