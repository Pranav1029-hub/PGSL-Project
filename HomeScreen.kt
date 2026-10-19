package com.gadvasu.livestock.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.gadvasu.livestock.AppContainer
import com.gadvasu.livestock.R
import com.gadvasu.livestock.core.Enterprise
import com.gadvasu.livestock.ui.components.AppScaffold
import com.gadvasu.livestock.ui.components.EnterpriseTile
import com.gadvasu.livestock.ui.components.HelpText
import com.gadvasu.livestock.ui.components.MutedText
import com.gadvasu.livestock.ui.components.ScreenTitle
import com.gadvasu.livestock.ui.navigation.Routes
import com.gadvasu.livestock.ui.navigation.navigateTab

/** Step 3: Home — four big enterprise tiles. */
@Composable
fun HomeScreen(nav: NavHostController, container: AppContainer) {
    val profileFlow = remember { container.farmRepo.profile() }
    val profile by profileFlow.collectAsStateWithLifecycle(initialValue = null)
    val p = profile
    val who = p?.name?.takeIf { it.isNotBlank() } ?: p?.phone?.takeIf { it.isNotBlank() } ?: p?.email ?: ""

    AppScaffold(
        title = stringResource(R.string.app_name),
        currentTab = Routes.HOME,
        onTab = { nav.navigateTab(it) },
        onLanguage = { nav.navigate(Routes.LANGUAGE) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            MutedText(stringResource(R.string.welcome) + if (who.isNotBlank()) ", $who" else "")
            ScreenTitle(stringResource(R.string.choose_enterprise))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnterpriseTile(R.drawable.ic_hen, stringResource(R.string.poultry), stringResource(R.string.poultry_note),
                    onClick = { nav.navigate(Routes.POULTRY) }, modifier = Modifier.weight(1f))
                EnterpriseTile(Enterprise.LIVESTOCK.iconRes, stringResource(R.string.livestock), stringResource(R.string.daily_model),
                    onClick = { nav.navigate(Routes.daily(Enterprise.LIVESTOCK)) }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnterpriseTile(Enterprise.GOATERY.iconRes, stringResource(R.string.goatery), stringResource(R.string.batch_model),
                    onClick = { nav.navigate(Routes.batches(Enterprise.GOATERY)) }, modifier = Modifier.weight(1f))
                EnterpriseTile(Enterprise.FISHERY.iconRes, stringResource(R.string.fishery), stringResource(R.string.batch_model),
                    onClick = { nav.navigate(Routes.batches(Enterprise.FISHERY)) }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Poultry has two models: Broiler (batch) and Layer (daily). */
@Composable
fun PoultryScreen(nav: NavHostController) {
    AppScaffold(
        title = stringResource(R.string.poultry),
        canBack = true, onBack = { nav.popBackStack() },
        currentTab = Routes.HOME,
        onTab = { nav.navigateTab(it) },
        onLanguage = { nav.navigate(Routes.LANGUAGE) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ScreenTitle(stringResource(R.string.poultry))
            HelpText(stringResource(R.string.poultry_note))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnterpriseTile(Enterprise.BROILER.iconRes, stringResource(R.string.broiler), stringResource(R.string.batch_model),
                    onClick = { nav.navigate(Routes.batches(Enterprise.BROILER)) }, modifier = Modifier.weight(1f))
                EnterpriseTile(Enterprise.LAYER.iconRes, stringResource(R.string.layer), stringResource(R.string.daily_model),
                    onClick = { nav.navigate(Routes.daily(Enterprise.LAYER)) }, modifier = Modifier.weight(1f))
            }
        }
    }
}
