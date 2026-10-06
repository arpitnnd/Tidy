package com.tidy.app.ui.settings

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tidy.app.R
import com.tidy.app.data.BlocklistSyncer
import com.tidy.app.data.ClipboardCleanTier
import com.tidy.app.data.ShareCleanTier
import com.tidy.app.theme.TidyCardTokens
import com.tidy.app.ui.components.ExpandableSettingRow
import com.tidy.app.ui.components.ScreenSectionHeader
import com.tidy.app.ui.components.SettingCard
import com.tidy.app.ui.components.TidyModalBottomSheet
import com.tidy.app.ui.components.TidyTopAppBar
import com.tidy.app.ui.components.TierCard
import com.tidy.app.ui.components.TierSelectorRow
import com.tidy.app.ui.components.TooltipWrapper
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = com.tidy.app.TidyApp.instance.settingsRepository
    val isPlusUnlocked by com.tidy.app.TidyApp.instance.entitlementManager.isPlusUnlocked
        .collectAsStateWithLifecycle(initialValue = false)
    val isPurchasePending by com.tidy.app.TidyApp.instance.entitlementManager.isPurchasePending
        .collectAsStateWithLifecycle(initialValue = false)
    val selectedTheme by settings.selectedTheme.collectAsStateWithLifecycle(initialValue = "slate")
    val scope = rememberCoroutineScope()
    SettingsScreenContent(
        state, onBackClick, onAboutClick, modifier, viewModel, isPlusUnlocked, selectedTheme,
        onSelectTheme = { theme -> scope.launch { settings.setSelectedTheme(theme) } },
        isPurchasePending = isPurchasePending,
        upgradeRow = { onClick -> com.tidy.app.FlavorConfig.SettingsUpgradeRow(onClick) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SettingsScreenContent(
    state: SettingsScreenViewModel.UiState,
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: SettingsScreenActions = object : SettingsScreenActions {},
    isPlusUnlocked: Boolean = false,
    selectedTheme: String = "slate",
    onSelectTheme: (String) -> Unit = {},
    isPurchasePending: Boolean = false,
    upgradeRow: @Composable (() -> Unit) -> Unit = { onClick ->
        com.tidy.app.FlavorConfig.SettingsUpgradeRow(isPlusUnlocked, isPurchasePending, onClick)
    }
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showMigrationDialog by remember { mutableStateOf(false) }
    var showUpsellSheet by remember { mutableStateOf(false) }
    val crashToastBrowserError = stringResource(R.string.crash_toast_browser_error)
    val toastBlocklistUpToDate = stringResource(R.string.settings_blocklist_up_to_date)
    val toastBlocklistUpdateFailed = stringResource(R.string.settings_blocklist_update_failed)
    val toastBlocklistUpdated = stringResource(R.string.settings_blocklist_updated)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TidyTopAppBar(
                title = stringResource(R.string.settings_title),
                onBackClick = onBackClick
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 650.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                upgradeRow {
                    if (com.tidy.app.FlavorConfig.isPlayFlavor) {
                        showUpsellSheet = true
                    } else {
                        showMigrationDialog = true
                    }
                }

                ScreenSectionHeader(
                    text = stringResource(R.string.rules_title),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                var isBypassListExpanded by remember { mutableStateOf(false) }
                var isParamWhitelistExpanded by remember { mutableStateOf(false) }
                var isCustomParamsExpanded by remember { mutableStateOf(false) }
                var showDefaultBlocklistSheet by remember { mutableStateOf(false) }

                if (showDefaultBlocklistSheet) {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    val lazyListState = rememberLazyListState()

                    TidyModalBottomSheet(
                        onDismissRequest = { showDefaultBlocklistSheet = false },
                        sheetState = sheetState
                    ) { blocklistScrollFix ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .navigationBarsPadding(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = stringResource(R.string.dialog_default_blocklist_title),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = formatBlocklistLastChecked(
                                            state.blocklistLastSuccessfulCheckTime
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TooltipWrapper(tooltipText = stringResource(R.string.dialog_default_blocklist_refresh)) {
                                        IconButton(
                                            onClick = {
                                                actions.refreshBlocklist(context) { result ->
                                                    scope.launch {
                                                        val message = when (result) {
                                                            is BlocklistSyncer.SyncResult.Updated -> {
                                                                if (result.newRulesCount > 0) {
                                                                    resources.getQuantityString(
                                                                        R.plurals.settings_blocklist_rules_updated,
                                                                        result.newRulesCount,
                                                                        result.newRulesCount
                                                                    )
                                                                } else {
                                                                    toastBlocklistUpdated
                                                                }
                                                            }

                                                            is BlocklistSyncer.SyncResult.UpToDate,
                                                            is BlocklistSyncer.SyncResult.Skipped -> toastBlocklistUpToDate

                                                            is BlocklistSyncer.SyncResult.Error -> toastBlocklistUpdateFailed
                                                        }
                                                        snackbarHostState.showSnackbar(message)
                                                    }
                                                }
                                            },
                                            enabled = !state.isRefreshingBlocklist
                                        ) {
                                            if (state.isRefreshingBlocklist) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.Refresh,
                                                    contentDescription = stringResource(R.string.dialog_default_blocklist_refresh),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    TooltipWrapper(tooltipText = stringResource(R.string.dialog_default_blocklist_view_github)) {
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        "https://github.com/arpitnnd/Tidy/commits/main/blocklist/trackers.json".toUri()
                                                    ).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            crashToastBrowserError
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = stringResource(R.string.dialog_default_blocklist_view_github),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.fillMaxWidth()
                            )

                            LazyColumn(
                                state = lazyListState,
                                contentPadding = PaddingValues(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .nestedScroll(blocklistScrollFix)
                            ) {
                                items(state.trackers) { tracker ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tracker.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = tracker.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    }
                }

                SettingCard(modifier = Modifier.animateContentSize()) {
                    Column(
                        modifier = Modifier.padding(TidyCardTokens.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(TidyCardTokens.contentSpacing)
                    ) {
                        // 1. Bypass List Row (Clickable Row Header)
                        val bypassSummary = if (state.whitelistedDomains.isEmpty()) {
                            stringResource(R.string.settings_no_domains_bypassed)
                        } else {
                            val count = state.whitelistedDomains.size
                            pluralStringResource(R.plurals.settings_domains_bypassed, count, count)
                        }
                        ExpandableSettingRow(
                            title = stringResource(R.string.settings_bypass_list),
                            summary = bypassSummary,
                            expanded = isBypassListExpanded,
                            onToggle = { isBypassListExpanded = !isBypassListExpanded }
                        )

                        if (isBypassListExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = TidyCardTokens.rowHorizontalPadding,
                                        end = TidyCardTokens.rowHorizontalPadding,
                                        bottom = 12.dp
                                    )
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_bypass_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = state.domainInput,
                                        onValueChange = { actions.onDomainInputChanged(it) },
                                        placeholder = { Text(stringResource(R.string.settings_placeholder_domain)) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    )

                                    val isDomainValid = remember(state.domainInput) {
                                        state.domainInput.trim().run {
                                            isNotEmpty() && contains(".") && !contains(" ")
                                        }
                                    }

                                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_add_domain)) {
                                        FilledIconButton(
                                            onClick = { actions.addDomain() },
                                            enabled = isDomainValid,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = stringResource(R.string.settings_add_domain_desc)
                                            )
                                        }
                                    }
                                }

                                if (state.whitelistedDomains.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        state.whitelistedDomains.forEach { domain ->
                                            InputChip(
                                                selected = false,
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        domain,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                },
                                                trailingIcon = {
                                                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_remove_domain)) {
                                                        IconButton(
                                                            onClick = {
                                                                actions.removeDomain(
                                                                    domain
                                                                )
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Filled.Delete,
                                                                contentDescription = stringResource(
                                                                    R.string.tooltip_remove_domain
                                                                ),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = stringResource(R.string.settings_no_domains_bypassed_text),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.7f
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            modifier = Modifier.padding(horizontal = TidyCardTokens.rowHorizontalPadding)
                        )

                        // 2. Parameter Whitelist Row (Clickable Row Header)
                        val paramWhitelistSummary = if (state.domainWhitelistedParams.isEmpty()) {
                            stringResource(R.string.settings_no_params_whitelisted)
                        } else {
                            val count = state.domainWhitelistedParams.size
                            pluralStringResource(
                                R.plurals.settings_params_whitelisted,
                                count,
                                count
                            )
                        }
                        ExpandableSettingRow(
                            title = stringResource(R.string.settings_param_whitelist_title),
                            summary = paramWhitelistSummary,
                            expanded = isParamWhitelistExpanded,
                            onToggle = { isParamWhitelistExpanded = !isParamWhitelistExpanded }
                        )

                        if (isParamWhitelistExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = TidyCardTokens.rowHorizontalPadding,
                                        end = TidyCardTokens.rowHorizontalPadding,
                                        bottom = 12.dp
                                    )
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_param_whitelist_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = state.newParamWhitelistDomain,
                                        onValueChange = {
                                            actions.onNewParamWhitelistDomainChanged(
                                                it
                                            )
                                        },
                                        placeholder = { Text(stringResource(R.string.settings_placeholder_domain)) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    )

                                    OutlinedTextField(
                                        value = state.newParamWhitelistParam,
                                        onValueChange = {
                                            actions.onNewParamWhitelistParamChanged(
                                                it
                                            )
                                        },
                                        placeholder = { Text(stringResource(R.string.settings_placeholder_whitelist_param)) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    )

                                    val isParamWhitelistValid = remember(
                                        state.newParamWhitelistDomain,
                                        state.newParamWhitelistParam
                                    ) {
                                        state.newParamWhitelistDomain.trim().run {
                                            isNotEmpty() && contains(".") && !contains(" ")
                                        } && state.newParamWhitelistParam.trim().run {
                                            isNotEmpty() && !contains(" ") && !contains("?") && !contains(
                                                "&"
                                            ) && !contains("=")
                                        }
                                    }

                                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_add_whitelist_param)) {
                                        FilledIconButton(
                                            onClick = { actions.addDomainWhitelistedParam() },
                                            enabled = isParamWhitelistValid,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = stringResource(R.string.settings_add_param_whitelist_desc)
                                            )
                                        }
                                    }
                                }

                                if (state.domainWhitelistedParams.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        state.domainWhitelistedParams.forEach { entry ->
                                            val displayLabel = entry.replace(":", " -> ")
                                            InputChip(
                                                selected = false,
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        displayLabel,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                },
                                                trailingIcon = {
                                                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_remove_whitelist_param)) {
                                                        IconButton(
                                                            onClick = {
                                                                actions.removeDomainWhitelistedParam(
                                                                    entry
                                                                )
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Filled.Delete,
                                                                contentDescription = stringResource(
                                                                    R.string.tooltip_remove_whitelist_param
                                                                ),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = stringResource(R.string.settings_no_params_whitelisted_text),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.7f
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            modifier = Modifier.padding(horizontal = TidyCardTokens.rowHorizontalPadding)
                        )

                        // 3. Custom Blacklisted Parameters Row (Clickable Row Header)
                        val customParamsSummary = if (state.blacklistedParams.isEmpty()) {
                            stringResource(R.string.settings_no_custom_params_text)
                        } else {
                            val count = state.blacklistedParams.size
                            pluralStringResource(R.plurals.settings_param_blacklisted, count, count)
                        }
                        ExpandableSettingRow(
                            title = stringResource(R.string.settings_custom_params_title),
                            summary = customParamsSummary,
                            expanded = isCustomParamsExpanded,
                            onToggle = { isCustomParamsExpanded = !isCustomParamsExpanded }
                        )

                        if (isCustomParamsExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = TidyCardTokens.rowHorizontalPadding,
                                        end = TidyCardTokens.rowHorizontalPadding,
                                        bottom = 12.dp
                                    )
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_custom_params_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = state.paramInput,
                                        onValueChange = { actions.onParamInputChanged(it) },
                                        placeholder = { Text(stringResource(R.string.settings_placeholder_param)) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    )

                                    val isParamValid = remember(state.paramInput) {
                                        state.paramInput.trim().run {
                                            isNotEmpty() && !contains(" ") && !contains("?") && !contains(
                                                "&"
                                            ) && !contains("=")
                                        }
                                    }

                                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_add_custom_param)) {
                                        FilledIconButton(
                                            onClick = { actions.addParam() },
                                            enabled = isParamValid,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = stringResource(R.string.settings_add_param_desc)
                                            )
                                        }
                                    }
                                }

                                if (state.blacklistedParams.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        state.blacklistedParams.forEach { param ->
                                            InputChip(
                                                selected = false,
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        param,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                },
                                                trailingIcon = {
                                                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_remove_custom_param)) {
                                                        IconButton(
                                                            onClick = { actions.removeParam(param) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Filled.Delete,
                                                                contentDescription = stringResource(
                                                                    R.string.tooltip_remove_custom_param
                                                                ),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = stringResource(R.string.settings_no_custom_params_text),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.7f
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                    }
                }

                // Default Blocklist Card (separated reference link)
                SettingCard {
                    Column(modifier = Modifier.padding(TidyCardTokens.contentPadding)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TidyCardTokens.rowCornerRadius))
                                .clickable { showDefaultBlocklistSheet = true }
                                .padding(
                                    horizontal = TidyCardTokens.rowHorizontalPadding,
                                    vertical = TidyCardTokens.rowVerticalPadding
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_default_blocklist_title),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_default_blocklist_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = stringResource(R.string.tooltip_open),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Appearance Theme Selector
                ScreenSectionHeader(
                    text = stringResource(R.string.settings_theme_title),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                SettingCard {
                    Column(
                        modifier = Modifier.padding(TidyCardTokens.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(TidyCardTokens.contentSpacing)
                    ) {

                        val themeSageSlate = stringResource(R.string.theme_sage_slate)
                        val themeMaterialYou = stringResource(R.string.theme_material_you)
                        val themeForestGreen = stringResource(R.string.theme_forest_green)
                        val themeOceanBlue = stringResource(R.string.theme_ocean_blue)
                        val themeDarkVelvet = stringResource(R.string.theme_dark_velvet)

                        val themes = remember(
                            themeSageSlate,
                            themeMaterialYou,
                            themeForestGreen,
                            themeOceanBlue,
                            themeDarkVelvet
                        ) {
                            buildList {
                                add("slate" to themeSageSlate)
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    add("dynamic" to themeMaterialYou)
                                }
                                add("forest" to themeForestGreen)
                                add("ocean" to themeOceanBlue)
                                add("velvet" to themeDarkVelvet)
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth().selectableGroup()
                        ) {
                            themes.forEach { (themeKey, themeName) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(TidyCardTokens.rowCornerRadius))
                                        .selectable(
                                            selected = selectedTheme == themeKey,
                                            role = Role.RadioButton,
                                            onClick = {
                                                if (themeKey == "slate" || isPlusUnlocked) {
                                                    onSelectTheme(themeKey)
                                                } else {
                                                    showUpsellSheet = true
                                                }
                                            }
                                        )
                                        .padding(
                                            start = 0.dp,
                                            end = TidyCardTokens.rowHorizontalPadding,
                                            top = 8.dp,
                                            bottom = 8.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        RadioButton(
                                            selected = (selectedTheme == themeKey),
                                            onClick = null,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                        Text(
                                            text = themeName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (themeKey != "slate" && !isPlusUnlocked) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Outlined.Lock,
                                            contentDescription = stringResource(R.string.tooltip_premium_theme),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                ScreenSectionHeader(
                    text = stringResource(R.string.settings_automation),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                // Clipboard checking: master toggle + tiered selector
                SettingCard(modifier = Modifier.animateContentSize()) {
                    Column(
                        modifier = Modifier.padding(TidyCardTokens.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(TidyCardTokens.contentSpacing)
                    ) {
                        SettingToggleRow(
                            title = stringResource(R.string.settings_check_clipboard_title),
                            description = stringResource(R.string.settings_check_clipboard_desc),
                            checked = state.checkClipboardForLinks,
                            onToggle = { actions.setCheckClipboardForLinks(!state.checkClipboardForLinks) }
                        )

                        if (state.checkClipboardForLinks) {
                            Text(
                                text = stringResource(R.string.settings_clipboard_tier_heading),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start = TidyCardTokens.rowHorizontalPadding,
                                    top = 4.dp
                                )
                            )
                            TierSelectorRow(
                                cards = listOf(
                                    TierCard(
                                        value = ClipboardCleanTier.SUGGEST,
                                        icon = Icons.Outlined.Visibility,
                                        title = stringResource(R.string.tier_suggest_title),
                                        subtitle = stringResource(R.string.tier_suggest_subtitle)
                                    ),
                                    TierCard(
                                        value = ClipboardCleanTier.SUGGEST_AND_COPY,
                                        icon = Icons.Outlined.ContentCopy,
                                        title = stringResource(R.string.tier_suggest_copy_title),
                                        subtitle = stringResource(R.string.tier_suggest_copy_subtitle),
                                        locked = !isPlusUnlocked
                                    ),
                                    TierCard(
                                        value = ClipboardCleanTier.AUTO_CLEAN,
                                        icon = Icons.Outlined.Bolt,
                                        title = stringResource(R.string.tier_auto_clean_title),
                                        subtitle = stringResource(R.string.tier_auto_clean_subtitle),
                                        locked = !isPlusUnlocked
                                    )
                                ),
                                selected = if (isPlusUnlocked) state.clipboardCleanTier else ClipboardCleanTier.SUGGEST,
                                onSelect = { actions.setClipboardCleanTier(it) },
                                onLockedTap = { showUpsellSheet = true },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Share-to-clean tiered selector
                SettingCard(modifier = Modifier.animateContentSize()) {
                    Column(
                        modifier = Modifier.padding(TidyCardTokens.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(TidyCardTokens.contentSpacing)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_share_tier_heading),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = TidyCardTokens.rowHorizontalPadding,
                                top = 4.dp
                            )
                        )
                        val effectiveShareTier =
                            if (isPlusUnlocked) state.shareCleanTier else ShareCleanTier.CLEAN
                        TierSelectorRow(
                            cards = listOf(
                                TierCard(
                                    value = ShareCleanTier.CLEAN,
                                    icon = Icons.Outlined.CleaningServices,
                                    title = stringResource(R.string.tier_clean_title),
                                    subtitle = stringResource(R.string.tier_clean_subtitle)
                                ),
                                TierCard(
                                    value = ShareCleanTier.CLEAN_AND_COPY,
                                    icon = Icons.Outlined.ContentCopy,
                                    title = stringResource(R.string.tier_clean_copy_title),
                                    subtitle = stringResource(R.string.tier_clean_copy_subtitle),
                                    locked = !isPlusUnlocked
                                ),
                                TierCard(
                                    value = ShareCleanTier.CLEAN_COPY_AND_SHARE,
                                    icon = Icons.Outlined.Share,
                                    title = stringResource(R.string.tier_clean_copy_share_title),
                                    subtitle = stringResource(R.string.tier_clean_copy_share_subtitle),
                                    locked = !isPlusUnlocked
                                )
                            ),
                            selected = effectiveShareTier,
                            onSelect = { actions.setShareCleanTier(it) },
                            onLockedTap = { showUpsellSheet = true },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )

                        if (effectiveShareTier == ShareCleanTier.CLEAN_COPY_AND_SHARE) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = TidyCardTokens.rowHorizontalPadding)
                            )
                            SettingToggleRow(
                                title = stringResource(R.string.settings_close_instead_title),
                                description = stringResource(R.string.settings_close_instead_desc),
                                checked = state.closeInsteadOfSharing,
                                onToggle = { actions.setCloseInsteadOfSharing(!state.closeInsteadOfSharing) }
                            )
                        }
                    }
                }

                // Other automation toggles
                SettingCard {
                    Column(
                        modifier = Modifier.padding(TidyCardTokens.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(TidyCardTokens.contentSpacing)
                    ) {
                        SettingToggleRow(
                            title = stringResource(R.string.settings_auto_expand_title),
                            description = stringResource(R.string.settings_auto_expand_desc),
                            checked = state.autoExpandShortUrls,
                            onToggle = { actions.setAutoExpandShortUrls(!state.autoExpandShortUrls) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = TidyCardTokens.rowHorizontalPadding)
                        )

                        SettingToggleRow(
                            title = stringResource(R.string.settings_remove_mobile_subdomains_title),
                            description = stringResource(R.string.settings_remove_mobile_subdomains_desc),
                            checked = state.autoRemoveMobileSubdomains,
                            onToggle = { actions.setAutoRemoveMobileSubdomains(!state.autoRemoveMobileSubdomains) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = TidyCardTokens.rowHorizontalPadding)
                        )

                        SettingToggleRow(
                            title = stringResource(R.string.settings_drop_trailing_slash_title),
                            description = stringResource(R.string.settings_drop_trailing_slash_desc),
                            checked = state.dropTrailingSlash,
                            onToggle = { actions.setDropTrailingSlash(!state.dropTrailingSlash) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = TidyCardTokens.rowHorizontalPadding)
                        )

                        SettingToggleRow(
                            title = stringResource(R.string.settings_auto_clean_input_title),
                            description = stringResource(R.string.settings_auto_clean_input_desc),
                            checked = state.autoCleanOnInput,
                            onToggle = { actions.setAutoCleanOnInput(!state.autoCleanOnInput) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = TidyCardTokens.rowHorizontalPadding)
                        )

                        // "Clean from any app" genuinely toggles the ACTION_PROCESS_TEXT
                        // component; it renders disabled (never hidden) for non-Plus users.
                        var processTextEnabled by remember {
                            mutableStateOf(com.tidy.app.FlavorConfig.isProcessTextEnabled(context))
                        }
                        SettingToggleRow(
                            title = stringResource(R.string.settings_clean_any_app_title),
                            description = stringResource(R.string.settings_clean_any_app_desc),
                            checked = processTextEnabled && isPlusUnlocked,
                            onToggle = {
                                com.tidy.app.FlavorConfig.setProcessTextEnabled(
                                    context,
                                    !processTextEnabled
                                )
                                processTextEnabled =
                                    com.tidy.app.FlavorConfig.isProcessTextEnabled(context)
                            },
                            enabled = isPlusUnlocked,
                            showLock = !isPlusUnlocked,
                            onLockedTap = { showUpsellSheet = true }
                        )
                    }
                }

                // Data & Backup Section
                ScreenSectionHeader(
                    text = stringResource(R.string.settings_data_backup_title),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                SettingCard {
                    Column(
                        modifier = Modifier.padding(TidyCardTokens.contentPadding)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TidyCardTokens.rowCornerRadius))
                                .toggleable(
                                    value = state.allowSystemBackup,
                                    role = Role.Switch,
                                    onValueChange = actions::setAllowSystemBackup
                                )
                                .padding(
                                    horizontal = TidyCardTokens.rowHorizontalPadding,
                                    vertical = TidyCardTokens.rowVerticalPadding
                                ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_allow_system_backup_title),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_allow_system_backup_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.allowSystemBackup,
                                onCheckedChange = null,
                                colors = clearSwitchColors()
                            )
                        }
                    }
                }

                // About Section
                ScreenSectionHeader(
                    text = stringResource(R.string.settings_about_title),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                SettingCard {
                    Column(
                        modifier = Modifier.padding(TidyCardTokens.contentPadding)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TidyCardTokens.rowCornerRadius))
                                .clickable { onAboutClick() }
                                .padding(
                                    horizontal = TidyCardTokens.rowHorizontalPadding,
                                    vertical = TidyCardTokens.rowVerticalPadding
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.about_title),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_about_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = stringResource(R.string.tooltip_open),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMigrationDialog) {
        com.tidy.app.ui.settings.SettingsMigrationViews.MigrationSheet(
            onDismiss = { showMigrationDialog = false }
        )
    }

    if (showUpsellSheet) {
        com.tidy.app.FlavorConfig.ShowUpsellBottomSheet(
            onDismiss = { showUpsellSheet = false }
        )
    }
}

/**
 * A standard settings toggle row: title + description on the left, switch on the right.
 * [enabled] = false disables the switch; the row stays tappable so a locked row can still
 * surface the Tidy+ upsell via [onLockedTap]. [showLock] adds the lock indicator next to
 * the title.
 */
@Composable
private fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean = true,
    showLock: Boolean = false,
    onLockedTap: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TidyCardTokens.rowCornerRadius))
            .then(
                if (enabled) Modifier.toggleable(
                    value = checked,
                    role = Role.Switch,
                    onValueChange = { onToggle() }
                ) else Modifier.clickable(role = Role.Button, onClick = onLockedTap)
            )
            .padding(
                horizontal = TidyCardTokens.rowHorizontalPadding,
                vertical = TidyCardTokens.rowVerticalPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (showLock) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = stringResource(R.string.tooltip_premium_feature),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = clearSwitchColors()
        )
    }
}

@Composable
private fun clearSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    disabledCheckedBorderColor = Color.Transparent,
    disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
    disabledUncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
    disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
)

@Composable
private fun formatBlocklistLastChecked(lastSuccessfulCheckTime: Long): String {
    if (lastSuccessfulCheckTime <= 0L)
        return stringResource(R.string.settings_blocklist_included)
    val now = System.currentTimeMillis()
    val elapsedMinutes = elapsedMinutesSince(lastSuccessfulCheckTime, now)
    val days = calendarDaysSince(lastSuccessfulCheckTime, now)
    return when {
        elapsedMinutes < 1 -> stringResource(R.string.settings_blocklist_checked_moments_ago)
        elapsedMinutes < 60 -> pluralStringResource(
            R.plurals.settings_blocklist_checked_minutes_ago,
            elapsedMinutes,
            elapsedMinutes
        )

        elapsedMinutes < 24 * 60 -> {
            val elapsedHours = elapsedMinutes / 60
            pluralStringResource(
                R.plurals.settings_blocklist_checked_hours_ago,
                elapsedHours,
                elapsedHours
            )
        }

        days <= 1 -> stringResource(R.string.settings_blocklist_checked_yesterday)
        days < 30 -> pluralStringResource(
            R.plurals.settings_blocklist_checked_days_ago,
            days,
            days
        )

        else -> {
            val locale = LocalConfiguration.current.locales[0]
            val date = remember(lastSuccessfulCheckTime, locale) {
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
                    .format(Date(lastSuccessfulCheckTime))
            }
            stringResource(R.string.settings_blocklist_checked_date, date)
        }
    }
}

internal fun elapsedMinutesSince(checkedAt: Long, now: Long): Int =
    ((now - checkedAt).coerceAtLeast(0L) / 60_000L).toInt()

internal fun calendarDaysSince(
    checkedAt: Long,
    now: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Int {
    val checkedDay = startOfDay(checkedAt, timeZone)
    val currentDay = startOfDay(now, timeZone)
    if (!checkedDay.before(currentDay)) return 0

    var days = 0
    while (checkedDay.before(currentDay) && days < 30) {
        checkedDay.add(Calendar.DAY_OF_MONTH, 1)
        days++
    }
    return days
}

private fun startOfDay(timestamp: Long, timeZone: TimeZone): Calendar =
    Calendar.getInstance(timeZone).apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
