package com.gadvasu.livestock.ui.reports

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.gadvasu.livestock.data.CsvExporter
import com.gadvasu.livestock.data.FarmRepository
import com.gadvasu.livestock.data.Period
import com.gadvasu.livestock.data.ReportBuilder
import com.gadvasu.livestock.data.ReportData
import com.gadvasu.livestock.data.ReportFilters
import com.gadvasu.livestock.ui.components.AppCard
import com.gadvasu.livestock.ui.components.AppScaffold
import com.gadvasu.livestock.ui.components.BatchItem
import com.gadvasu.livestock.ui.components.ConfirmDialog
import com.gadvasu.livestock.ui.components.DateField
import com.gadvasu.livestock.ui.components.DropdownField
import com.gadvasu.livestock.ui.components.EmptyBox
import com.gadvasu.livestock.ui.components.GrandRow
import com.gadvasu.livestock.ui.components.ListRow
import com.gadvasu.livestock.ui.components.MutedText
import com.gadvasu.livestock.ui.components.RowKind
import com.gadvasu.livestock.ui.components.ScreenTitle
import com.gadvasu.livestock.ui.components.SectionHeading
import com.gadvasu.livestock.ui.components.SlipCard
import com.gadvasu.livestock.ui.components.SlipRow
import com.gadvasu.livestock.ui.components.TonalButton
import com.gadvasu.livestock.ui.components.appLocale
import com.gadvasu.livestock.ui.navigation.Routes
import com.gadvasu.livestock.ui.navigation.navigateTab
import com.gadvasu.livestock.ui.theme.profitColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ReportsViewModel(private val repo: FarmRepository) : ViewModel() {
    val filters = MutableStateFlow(ReportFilters())

    val data: StateFlow<ReportData> = combine(filters, repo.allDailyRecords(), repo.batches()) { f, d, b ->
        ReportBuilder.build(f, d, b, Fmt.todayStr())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportData())

    fun setEnterprise(key: String) { filters.value = filters.value.copy(enterprise = key) }
    fun setPeriod(p: Period) { filters.value = filters.value.copy(period = p) }
    fun setFrom(d: String) { filters.value = filters.value.copy(fromDate = d) }
    fun setTo(d: String) { filters.value = filters.value.copy(toDate = d) }
    fun deleteDaily(id: String) = repo.deleteDaily(id)
}

/** Step 8: History & reports — filter by enterprise and period, export CSV. */
@Composable
fun ReportsScreen(nav: NavHostController, container: AppContainer) {
    val vm: ReportsViewModel = viewModel(factory = viewModelFactory { initializer { ReportsViewModel(container.farmRepo) } })
    val f by vm.filters.collectAsStateWithLifecycle()
    val d by vm.data.collectAsStateWithLifecycle()
    val locale = appLocale()
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    val entOptions = listOf("all" to stringResource(R.string.all)) + Enterprise.ORDER.map { it.key to stringResource(it.nameRes) }
    val periodOptions = listOf(
        Period.LAST7.name to stringResource(R.string.last7),
        Period.LAST30.name to stringResource(R.string.last30),
        Period.MONTH.name to stringResource(R.string.this_month),
        Period.ALL.name to stringResource(R.string.all_time),
        Period.CUSTOM.name to stringResource(R.string.custom),
    )

    AppScaffold(
        title = stringResource(R.string.reports),
        currentTab = Routes.REPORTS, onTab = { nav.navigateTab(it) },
        onLanguage = { nav.navigate(Routes.LANGUAGE) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ScreenTitle(stringResource(R.string.reports))
            AppCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DropdownField(stringResource(R.string.enterprise), entOptions, f.enterprise, vm::setEnterprise, Modifier.weight(1f))
                    DropdownField(stringResource(R.string.period), periodOptions, f.period.name, { vm.setPeriod(Period.valueOf(it)) }, Modifier.weight(1f))
                }
                if (f.period == Period.CUSTOM) {
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DateField(stringResource(R.string.from), f.fromDate.ifBlank { Fmt.todayStr() }, vm::setFrom, max = null, modifier = Modifier.weight(1f))
                        DateField(stringResource(R.string.to), f.toDate.ifBlank { Fmt.todayStr() }, vm::setTo, max = null, modifier = Modifier.weight(1f))
                    }
                }
            }

            val heading = when {
                f.period == Period.ALL || (f.period == Period.CUSTOM && f.fromDate.isBlank() && f.toDate.isBlank()) -> stringResource(R.string.all_time)
                else -> (if (d.fromDate == ReportData.MIN_DATE) "… – " else Fmt.date(d.fromDate, locale) + " – ") +
                    (if (d.toDate == ReportData.MAX_DATE) "…" else Fmt.date(d.toDate, locale))
            }
            SectionHeading(heading)
            SlipCard {
                SlipRow(stringResource(R.string.daily_income), Fmt.money(d.daily.income), RowKind.SUB)
                SlipRow(stringResource(R.string.batch_sales), Fmt.money(d.batchIncome), RowKind.SUB)
                SlipRow(stringResource(R.string.total_income), Fmt.money(d.totalIncome), RowKind.TOTAL)
                SlipRow(stringResource(R.string.daily_expenses), Fmt.money(d.daily.expense), RowKind.SUB)
                SlipRow(stringResource(R.string.batch_expenses), Fmt.money(d.batchExpense), RowKind.SUB)
                SlipRow(stringResource(R.string.total_expenses), Fmt.money(d.totalExpense), RowKind.TOTAL)
                GrandRow(d.profit, stringResource(R.string.net_profit), stringResource(R.string.net_loss))
            }

            if (d.dailyRows.isNotEmpty()) {
                SectionHeading(stringResource(R.string.daily_records) + " (${d.dailyRows.size})")
                AppCard {
                    d.dailyRows.forEach { (ent, r) ->
                        val pr = Calc.dailyProfit(r)
                        ListRow(
                            main = {
                                Text(Fmt.date(r.date, locale), fontWeight = FontWeight.Bold)
                                if (f.enterprise == "all") MutedText(" " + stringResource(ent.nameRes))
                            },
                            sub = stringResource(R.string.income) + " " + Fmt.money(Calc.dailyIncome(r)) + ", " +
                                stringResource(R.string.expenses) + " " + Fmt.money(Calc.dailyExpense(r)),
                            trailing = { Text(Fmt.money(pr), fontWeight = FontWeight.Bold, color = profitColor(pr)) },
                            onClick = { nav.navigate(Routes.daily(ent)) },
                            onDelete = { pendingDelete = r.id },
                        )
                    }
                }
            }
            if (d.sold.isNotEmpty()) {
                SectionHeading(stringResource(R.string.sold_in_period) + " (${d.sold.size})")
                AppCard { d.sold.forEach { b -> BatchItem(b, showType = true) { nav.navigate(Routes.batch(b.id)) } } }
            }
            if (d.running.isNotEmpty()) {
                SectionHeading(stringResource(R.string.running_not_included))
                AppCard { d.running.forEach { b -> BatchItem(b, showType = true) { nav.navigate(Routes.batch(b.id)) } } }
            }
            if (d.isEmpty) EmptyBox(stringResource(R.string.no_records_period))

            Spacer(Modifier.height(20.dp))
            TonalButton(stringResource(R.string.export_csv), onClick = { CsvExporter(context).share(d) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
        }
    }

    pendingDelete?.let { id ->
        ConfirmDialog(
            text = stringResource(R.string.confirm_delete), confirmText = stringResource(R.string.yes_delete),
            onConfirm = { vm.deleteDaily(id); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}
