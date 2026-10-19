package com.gadvasu.livestock.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.gadvasu.livestock.core.Calc
import com.gadvasu.livestock.core.Enterprise
import com.gadvasu.livestock.core.Fmt
import com.gadvasu.livestock.data.Batch
import com.gadvasu.livestock.data.DailyRecord
import com.gadvasu.livestock.data.FarmRepository
import com.gadvasu.livestock.ui.components.AppCard
import com.gadvasu.livestock.ui.components.AppScaffold
import com.gadvasu.livestock.ui.components.BatchItem
import com.gadvasu.livestock.ui.components.EmptyBox
import com.gadvasu.livestock.ui.components.GrandRow
import com.gadvasu.livestock.ui.components.HelpText
import com.gadvasu.livestock.ui.components.MutedText
import com.gadvasu.livestock.ui.components.PrimaryButton
import com.gadvasu.livestock.ui.components.RowKind
import com.gadvasu.livestock.ui.components.ScreenTitle
import com.gadvasu.livestock.ui.components.SectionHeading
import com.gadvasu.livestock.ui.components.SlipCard
import com.gadvasu.livestock.ui.components.SlipRow
import com.gadvasu.livestock.ui.components.Stat3
import com.gadvasu.livestock.ui.components.SubHeading
import com.gadvasu.livestock.ui.components.TonalButton
import com.gadvasu.livestock.ui.components.appLocale
import com.gadvasu.livestock.ui.navigation.Routes
import com.gadvasu.livestock.ui.navigation.navigateTab
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardData(val daily: List<DailyRecord> = emptyList(), val batches: List<Batch> = emptyList(), val loaded: Boolean = false)

class DashboardViewModel(repo: FarmRepository) : ViewModel() {
    val data: StateFlow<DashboardData> = combine(repo.allDailyRecords(), repo.batches()) { d, b -> DashboardData(d, b, true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardData())
}

/** Step 7: Dashboard — today, this month, running batches, overall totals. */
@Composable
fun DashboardScreen(nav: NavHostController, container: AppContainer) {
    val vm: DashboardViewModel = viewModel(factory = viewModelFactory { initializer { DashboardViewModel(container.farmRepo) } })
    val data by vm.data.collectAsStateWithLifecycle()
    val locale = appLocale()
    val today = Fmt.todayStr()
    val monthStart = today.take(8) + "01"

    AppScaffold(
        title = stringResource(R.string.dashboard),
        currentTab = Routes.DASHBOARD, onTab = { nav.navigateTab(it) },
        onLanguage = { nav.navigate(Routes.LANGUAGE) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ScreenTitle(stringResource(R.string.dashboard))
            HelpText(Fmt.dateLong(today, locale))

            val dailyWithData = Enterprise.DAILY.filter { e -> data.daily.any { it.enterprise == e.key } }
            val running = data.batches.filter { !it.isCompleted }.sortedByDescending { it.startDate }
            val done = data.batches.filter { it.isCompleted }.sortedByDescending { it.saleDate }
            val hasAny = dailyWithData.isNotEmpty() || data.batches.isNotEmpty()

            if (dailyWithData.isNotEmpty()) {
                SectionHeading(stringResource(R.string.daily_enterprises))
                dailyWithData.forEach { e ->
                    val recs = data.daily.filter { it.enterprise == e.key }
                    val todayRec = recs.firstOrNull { it.date == today }
                    val month = Calc.summary(recs.filter { it.date >= monthStart && it.date <= today })
                    AppCard(Modifier.padding(bottom = 12.dp)) {
                        Text(stringResource(e.nameRes), style = MaterialTheme.typography.titleMedium)
                        if (todayRec != null) {
                            val prod = if (todayRec.production > 0) ", " + Fmt.num(todayRec.production) + " " + stringResource(e.productionUnitRes) else ""
                            val animals = if (e.hasAnimalCounts && (todayRec.cows > 0 || todayRec.buffaloes > 0))
                                ", ${todayRec.cows} ${stringResource(R.string.cows)}, ${todayRec.buffaloes} ${stringResource(R.string.buffaloes)}" else ""
                            MutedText(stringResource(R.string.today) + prod + animals, Modifier.padding(top = 4.dp))
                            Stat3(Calc.dailyIncome(todayRec), Calc.dailyExpense(todayRec))
                        } else {
                            MutedText(stringResource(R.string.no_entry_today), Modifier.padding(top = 4.dp))
                            Spacer(Modifier.height(8.dp))
                            TonalButton(stringResource(R.string.add_today), iconRes = R.drawable.ic_plus,
                                onClick = { nav.navigate(Routes.daily(e)) })
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
                        MutedText(stringResource(R.string.this_month) + ", " + month.count + " " +
                            stringResource(if (month.count == 1) R.string.record else R.string.records))
                        Stat3(month.income, month.expense)
                    }
                }
            }

            if (data.batches.isNotEmpty()) {
                SectionHeading(stringResource(R.string.batch_enterprises))
                if (running.isNotEmpty()) {
                    SubHeading(stringResource(R.string.active_batches))
                    AppCard { running.forEach { b -> BatchItem(b, showType = true) { nav.navigate(Routes.batch(b.id)) } } }
                }
                if (done.isNotEmpty()) {
                    SubHeading(stringResource(R.string.completed_batches))
                    AppCard { done.take(5).forEach { b -> BatchItem(b, showType = true) { nav.navigate(Routes.batch(b.id)) } } }
                }
            }

            if (hasAny) {
                val o = Calc.overall(data.daily, data.batches)
                SectionHeading(stringResource(R.string.overall))
                SlipCard(footer = stringResource(R.string.overall_note)) {
                    SlipRow(stringResource(R.string.daily_income), Fmt.money(o.dailyIncome), RowKind.SUB)
                    SlipRow(stringResource(R.string.batch_sales), Fmt.money(o.batchIncome), RowKind.SUB)
                    SlipRow(stringResource(R.string.total_income), Fmt.money(o.totalIncome), RowKind.TOTAL)
                    SlipRow(stringResource(R.string.daily_expenses), Fmt.money(o.dailyExpense), RowKind.SUB)
                    SlipRow(stringResource(R.string.batch_expenses), Fmt.money(o.batchExpense), RowKind.SUB)
                    SlipRow(stringResource(R.string.total_expenses), Fmt.money(o.totalExpense), RowKind.TOTAL)
                    GrandRow(o.profit, stringResource(R.string.net_profit), stringResource(R.string.net_loss))
                }
            } else if (data.loaded) {
                EmptyBox(stringResource(R.string.no_data_yet)) {
                    PrimaryButton(stringResource(R.string.go_home), onClick = { nav.navigateTab(Routes.HOME) })
                }
            } else {
                MutedText(stringResource(R.string.loading))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
