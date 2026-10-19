package com.gadvasu.livestock.ui.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gadvasu.livestock.AppContainer
import com.gadvasu.livestock.R
import com.gadvasu.livestock.core.LocaleManager
import com.gadvasu.livestock.ui.components.AppScaffold
import com.gadvasu.livestock.ui.components.ErrorText
import com.gadvasu.livestock.ui.components.HelpText
import com.gadvasu.livestock.ui.components.MutedText
import com.gadvasu.livestock.ui.components.Notice
import com.gadvasu.livestock.ui.components.PrimaryButton
import com.gadvasu.livestock.ui.components.ScreenTitle
import com.gadvasu.livestock.ui.components.TextLineField
import com.gadvasu.livestock.ui.navigation.Routes
import com.gadvasu.livestock.ui.navigation.restartAt

private fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** Step 2: login with mobile number (OTP) or email ID (password). */
@Composable
fun LoginScreen(nav: NavHostController, container: AppContainer) {
    val vm: LoginViewModel = viewModel(factory = viewModelFactory {
        initializer { LoginViewModel(container.authRepo, container.farmRepo) }
    })
    val st = vm.state
    val activity = LocalContext.current.findActivity()
    val language = LocaleManager.current().ifEmpty { "en" }

    LaunchedEffect(st.done) { if (st.done) nav.restartAt(Routes.HOME) }

    AppScaffold(
        title = stringResource(R.string.app_name),
        showNav = false,
        onLanguage = { nav.navigate(Routes.LANGUAGE) },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp).imePadding(),
        ) {
            Text("GADVASU", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.primary)
            ScreenTitle(stringResource(R.string.login))
            HelpText(stringResource(R.string.login_help))

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                SegmentedButton(
                    selected = st.method == LoginViewModel.Method.MOBILE,
                    onClick = { vm.setMethod(LoginViewModel.Method.MOBILE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text(stringResource(R.string.mobile)) }
                SegmentedButton(
                    selected = st.method == LoginViewModel.Method.EMAIL,
                    onClick = { vm.setMethod(LoginViewModel.Method.EMAIL) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text(stringResource(R.string.email)) }
            }

            if (st.method == LoginViewModel.Method.MOBILE) {
                if (st.step == LoginViewModel.Step.ENTER) {
                    TextLineField(stringResource(R.string.mobile), st.phone, vm::onPhone,
                        keyboardType = KeyboardType.Phone, placeholder = "98765 43210")
                    TextLineField(stringResource(R.string.your_name), st.name, vm::onName)
                    ErrorText(st.error)
                    PrimaryButton(stringResource(R.string.send_otp), enabled = !st.loading,
                        onClick = { vm.sendOtp(activity, language) })
                } else {
                    st.info?.let { Notice(stringResource(it)) }
                    TextLineField(stringResource(R.string.otp), st.otp, vm::onOtp, keyboardType = KeyboardType.NumberPassword)
                    ErrorText(st.error)
                    PrimaryButton(stringResource(R.string.verify_login), enabled = !st.loading,
                        onClick = { vm.verifyOtp(language) })
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        TextButton(onClick = { vm.sendOtp(activity, language) }, enabled = !st.loading) {
                            Text(stringResource(R.string.resend_otp))
                        }
                        TextButton(onClick = vm::changeNumber) { Text(stringResource(R.string.change_number)) }
                    }
                }
            } else {
                TextLineField(stringResource(R.string.email), st.email, vm::onEmail,
                    keyboardType = KeyboardType.Email, placeholder = "name@example.com")
                OutlinedTextField(
                    value = st.password, onValueChange = vm::onPassword,
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
                TextLineField(stringResource(R.string.your_name), st.name, vm::onName)
                MutedText(stringResource(R.string.password_help), Modifier.padding(vertical = 6.dp))
                ErrorText(st.error)
                PrimaryButton(stringResource(R.string.continue_btn), enabled = !st.loading,
                    onClick = { vm.loginEmail(language) })
            }

            if (st.loading) {
                Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    MutedText(stringResource(R.string.please_wait))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
