package com.gadvasu.livestock.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gadvasu.livestock.AppContainer
import com.gadvasu.livestock.R
import com.gadvasu.livestock.core.LocaleManager
import com.gadvasu.livestock.data.AuthRepository
import com.gadvasu.livestock.data.FarmRepository
import com.gadvasu.livestock.data.UserProfile
import com.gadvasu.livestock.ui.components.AppCard
import com.gadvasu.livestock.ui.components.AppScaffold
import com.gadvasu.livestock.ui.components.ConfirmDialog
import com.gadvasu.livestock.ui.components.DangerButton
import com.gadvasu.livestock.ui.components.GhostButton
import com.gadvasu.livestock.ui.components.MutedText
import com.gadvasu.livestock.ui.components.ScreenTitle
import com.gadvasu.livestock.ui.components.TonalButton
import com.gadvasu.livestock.ui.components.toast
import com.gadvasu.livestock.ui.navigation.Routes
import com.gadvasu.livestock.ui.navigation.navigateTab
import com.gadvasu.livestock.ui.navigation.restartAt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val farmRepo: FarmRepository, private val authRepo: AuthRepository) : ViewModel() {
    val profile: StateFlow<UserProfile?> = farmRepo.profile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun logout() = authRepo.signOut()

    fun clearAll(onDone: () -> Unit) {
        viewModelScope.launch {
            try { farmRepo.clearAll() } catch (_: Exception) { }
            onDone()
        }
    }
}

@Composable
fun SettingsScreen(nav: NavHostController, container: AppContainer) {
    val vm: SettingsViewModel = viewModel(factory = viewModelFactory {
        initializer { SettingsViewModel(container.farmRepo, container.authRepo) }
    })
    val profile by vm.profile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }
    val user = container.auth.currentUser
    val p = profile
    val loginId = p?.phone?.takeIf { it.isNotBlank() } ?: p?.email?.takeIf { it.isNotBlank() }
        ?: user?.phoneNumber ?: user?.email ?: ""

    AppScaffold(
        title = stringResource(R.string.settings),
        currentTab = Routes.SETTINGS, onTab = { nav.navigateTab(it) },
        onLanguage = { nav.navigate(Routes.LANGUAGE) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ScreenTitle(stringResource(R.string.settings))

            AppCard(Modifier.padding(bottom = 12.dp)) {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
                Text(LocaleManager.currentLang().native, modifier = Modifier.padding(vertical = 6.dp))
                TonalButton(stringResource(R.string.change_language), onClick = { nav.navigate(Routes.LANGUAGE) })
            }

            AppCard(Modifier.padding(bottom = 12.dp)) {
                Text(stringResource(R.string.account), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.logged_in_as) + " " + loginId +
                        (p?.name?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""),
                    modifier = Modifier.padding(vertical = 6.dp),
                )
                GhostButton(stringResource(R.string.logout), onClick = {
                    vm.logout()
                    nav.restartAt(Routes.LOGIN)
                })
            }

            AppCard(Modifier.padding(bottom = 12.dp)) {
                Text(stringResource(R.string.data_storage), style = MaterialTheme.typography.titleMedium)
                MutedText(stringResource(R.string.sync_note), Modifier.padding(vertical = 6.dp))
                Spacer(Modifier.height(8.dp))
                DangerButton(stringResource(R.string.clear_data), onClick = { confirmClear = true })
            }

            AppCard {
                Text(stringResource(R.string.about), style = MaterialTheme.typography.titleMedium)
                MutedText(stringResource(R.string.about_text), Modifier.padding(vertical = 6.dp))
                MutedText(stringResource(R.string.daily_formula))
                MutedText(stringResource(R.string.batch_formula))
                MutedText(stringResource(R.string.version) + " 1.0", Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            text = stringResource(R.string.confirm_clear), confirmText = stringResource(R.string.yes_delete),
            onConfirm = {
                confirmClear = false
                vm.clearAll { toast(context, R.string.cleared) }
            },
            onDismiss = { confirmClear = false },
        )
    }
}
