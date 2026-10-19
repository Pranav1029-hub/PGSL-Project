package com.gadvasu.livestock.ui.language

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.gadvasu.livestock.AppContainer
import com.gadvasu.livestock.R
import com.gadvasu.livestock.core.LocaleManager
import com.gadvasu.livestock.ui.navigation.Routes
import com.gadvasu.livestock.ui.navigation.restartAt

/** Step 1: pick English / Hindi / Punjabi. Shown on first launch and from Settings. */
@Composable
fun LanguageScreen(nav: NavHostController, container: AppContainer) {
    val current = LocaleManager.current()
    val firstLaunch = current.isEmpty()

    fun choose(code: String) {
        if (code == current) {
            // Nothing to change: just go back where we came from.
            if (!nav.popBackStack()) nav.restartAt(if (container.auth.currentUser == null) Routes.LOGIN else Routes.HOME)
            return
        }
        container.farmRepo.updateLanguage(code)   // no-op when not logged in
        LocaleManager.set(code)                    // recreates the activity in the new language
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(28.dp))
            Icon(painterResource(R.drawable.ic_leaf), contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Text("GADVASU", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            Text(
                if (firstLaunch) "Select your language" else stringResource(R.string.select_language),
                style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            if (firstLaunch) {
                // Before a language is chosen, show the prompt in all three scripts.
                Text("अपनी भाषा चुनें\nਆਪਣੀ ਭਾਸ਼ਾ ਚੁਣੋ", textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp))
            }
            Spacer(Modifier.height(24.dp))
            LocaleManager.supported.forEach { lang ->
                val selected = lang.code == current
                OutlinedButton(
                    onClick = { choose(lang.code) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 6.dp),
                ) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(lang.native, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(lang.english, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
