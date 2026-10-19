package com.gadvasu.livestock.ui.login

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gadvasu.livestock.R
import com.gadvasu.livestock.data.AuthRepository
import com.gadvasu.livestock.data.FarmRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepo: AuthRepository,
    private val farmRepo: FarmRepository,
) : ViewModel() {

    enum class Method { MOBILE, EMAIL }
    enum class Step { ENTER, OTP }

    data class State(
        val method: Method = Method.MOBILE,
        val step: Step = Step.ENTER,
        val phone: String = "",
        val email: String = "",
        val password: String = "",
        val name: String = "",
        val otp: String = "",
        val loading: Boolean = false,
        val error: Int? = null,     // string resource
        val info: Int? = null,      // string resource
        val done: Boolean = false,  // signed in → go Home
    )

    var state by mutableStateOf(State())
        private set

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun setMethod(m: Method) { state = state.copy(method = m, error = null, info = null, step = Step.ENTER) }
    fun onPhone(v: String) { state = state.copy(phone = v.take(15), error = null) }
    fun onEmail(v: String) { state = state.copy(email = v, error = null) }
    fun onPassword(v: String) { state = state.copy(password = v, error = null) }
    fun onName(v: String) { state = state.copy(name = v.take(60)) }
    fun onOtp(v: String) { state = state.copy(otp = v.filter { it.isDigit() }.take(6), error = null) }
    fun changeNumber() { verificationId = null; state = state.copy(step = Step.ENTER, otp = "", info = null, error = null) }

    // ---------------------------------------------------------------- mobile number + OTP

    fun sendOtp(activity: Activity?, language: String) {
        val digits = normalizePhone(state.phone)
        if (digits == null) { state = state.copy(error = R.string.login_invalid_mobile); return }
        if (activity == null) { state = state.copy(error = R.string.login_failed); return }
        state = state.copy(loading = true, error = null, info = null)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instant verification / SMS auto-read: no OTP typing needed.
                finishSignIn(credential, language)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                state = state.copy(loading = false, error = R.string.login_failed)
            }
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                resendToken = token
                state = state.copy(loading = false, step = Step.OTP, info = R.string.otp_sent)
            }
        }
        authRepo.startPhoneVerification(activity, "+91$digits", callbacks, resendToken)
    }

    fun verifyOtp(language: String) {
        val id = verificationId
        val code = state.otp.trim()
        if (id == null || code.length != 6) { state = state.copy(error = R.string.login_invalid_otp); return }
        finishSignIn(authRepo.otpCredential(id, code), language)
    }

    // ---------------------------------------------------------------- email + password

    fun loginEmail(language: String) {
        val email = state.email.trim().lowercase()
        val password = state.password
        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email)) { state = state.copy(error = R.string.login_invalid_email); return }
        if (password.length < 6) { state = state.copy(error = R.string.login_invalid_password); return }
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val user = authRepo.signInWithEmail(email, password)
                farmRepo.ensureProfile(user, state.name.trim(), language)
                state = state.copy(loading = false, done = true)
            } catch (e: FirebaseAuthUserCollisionException) {
                state = state.copy(loading = false, error = R.string.wrong_password)
            } catch (e: Exception) {
                state = state.copy(loading = false, error = R.string.login_failed)
            }
        }
    }

    private fun finishSignIn(credential: AuthCredential, language: String) {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val user = authRepo.signInWithCredential(credential)
                farmRepo.ensureProfile(user, state.name.trim(), language)
                state = state.copy(loading = false, done = true)
            } catch (e: Exception) {
                state = state.copy(loading = false, error = R.string.login_failed)
            }
        }
    }

    companion object {
        /** Accepts "98765 43210", "+91 98765 43210", "098765..." → 10 digits starting with 6–9, or null. */
        fun normalizePhone(raw: String): String? {
            var d = raw.filter { it.isDigit() }
            if (d.length == 12 && d.startsWith("91")) d = d.drop(2)
            if (d.length == 11 && d.startsWith("0")) d = d.drop(1)
            return if (Regex("^[6-9]\\d{9}$").matches(d)) d else null
        }
    }
}
