package com.nub.diary

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nub.diary.ui.DiaryApp
import com.nub.diary.ui.DiaryTheme
import com.nub.diary.ui.PhoneAuth

class MainActivity : FragmentActivity() {
    private val vm: MainViewModel by viewModels()
    private var stoppedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Дневник не должен попадать в скриншоты и список недавних приложений.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            DiaryTheme(vm.themeMode) {
                DiaryApp(vm, onBiometric = ::promptBiometric, canBiometric = ::canBiometric, phone = phoneAuth)
            }
        }
    }

    private val phoneAuth = PhoneAuth(::canPhoneLock, ::promptPhoneLock)

    private fun canPhoneLock(): Boolean =
        BiometricManager.from(this).canAuthenticate(PHONE_LOCK) == BiometricManager.BIOMETRIC_SUCCESS

    private fun promptPhoneLock(title: String, onSuccess: () -> Unit) {
        if (!canPhoneLock()) return
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            },
        )
        // С DEVICE_CREDENTIAL кнопку «Отмена» задавать нельзя: система показывает свою.
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setAllowedAuthenticators(PHONE_LOCK)
                .build()
        )
    }

    override fun onStop() {
        super.onStop()
        stoppedAt = SystemClock.elapsedRealtime()
    }

    override fun onStart() {
        super.onStart()
        val away = SystemClock.elapsedRealtime() - stoppedAt
        if (stoppedAt != 0L && vm.protectionOn && !vm.skipRelock && away > RELOCK_AFTER_MS) vm.locked = true
        vm.skipRelock = false
    }

    private fun canBiometric(): Boolean =
        BiometricManager.from(this).canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    private fun promptBiometric() {
        if (!canBiometric()) return
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    vm.locked = false
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Открыть дневник")
                .setNegativeButtonText("Ввести PIN")
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .build()
        )
    }

    private companion object {
        const val RELOCK_AFTER_MS = 20_000L
        const val PHONE_LOCK = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
    }
}
