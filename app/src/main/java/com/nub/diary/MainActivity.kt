package com.nub.diary

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nub.diary.ui.DiaryApp
import com.nub.diary.ui.DiaryTheme

class MainActivity : FragmentActivity() {
    private val vm: MainViewModel by viewModels()
    private var stoppedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Дневник не должен попадать в скриншоты и список недавних приложений.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            DiaryTheme { DiaryApp(vm, onBiometric = ::promptBiometric, canBiometric = ::canBiometric) }
        }
    }

    override fun onStop() {
        super.onStop()
        stoppedAt = SystemClock.elapsedRealtime()
    }

    override fun onStart() {
        super.onStart()
        val away = SystemClock.elapsedRealtime() - stoppedAt
        if (stoppedAt != 0L && vm.hasPin && !vm.skipRelock && away > RELOCK_AFTER_MS) vm.locked = true
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
    }
}
