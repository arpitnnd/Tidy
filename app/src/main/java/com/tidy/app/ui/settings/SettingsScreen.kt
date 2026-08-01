package com.tidy.app.ui.settings

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tidy.app.R
import com.tidy.app.data.ClipboardCleanTier
import com.tidy.app.data.ShareCleanTier
import com.tidy.app.ui.components.ExpandableSettingRow
import com.tidy.app.ui.components.ScreenSectionHeader
import com.tidy.app.ui.components.SettingCard
import com.tidy.app.ui.components.TidyModalBottomSheet
import com.tidy.app.ui.components.TidyTopAppBar
import com.tidy.app.ui.components.TierCard
import com.tidy.app.ui.components.TierSelectorRow
import com.tidy.app.ui.components.TooltipWrapper
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
    val context = LocalContext.current

    val entitlementManager = com.tidy.app.TidyApp.instance.entitlementManager
    val isPlusUnlocked by entitlementManager.isPlusUnlocked.collectAsStateWithLifecycle(initialValue = false)

    val settingsRepository = com.tidy.app.TidyApp.instance.settingsRepository
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showMigrationDialog by remember { mutableStateOf(false) }
    var showUpsellSheet by remember { mutableStateOf(false) }
    val crashToastBrowserError = stringResource(R.string.crash_toast_browser_error)

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
                .padding(paddingValues),
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
                com.tidy.app.FlavorConfig.SettingsUpgradeRow {
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
                    val showDivider by remember {
                        derivedStateOf {
                            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
                        }
                    }
                    val dividerAlpha by animateFloatAsState(
                        targetValue = if (showDivider) 0.12f else 0f,
                        label = "BlocklistDividerAlpha"
                    )

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
                                Text(
                                    text = stringResource(R.string.dialog_default_blocklist_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
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

                            Text(
                                text = stringResource(R.string.dialog_default_blocklist_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(bottom = 8.dp)
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = dividerAlpha),
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
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Bypass List Row (Clickable Row Header)
                        val bypassSummary = if (state.whitelistedDomains.isEmpty()) {
                            stringResource(R.string.settings_no_domains_bypassed)
                        } else {
                            val count = state.whitelistedDomains.size
                            val unit = when (count) {
                                1 -> stringResource(R.string.settings_domain_bypassed_single)
                                else -> stringResource(R.string.settings_domains_bypassed_plural)
                            }
                            "$count $unit"
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
                                    .padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
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
                                        onValueChange = { viewModel.onDomainInputChanged(it) },
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
                                            onClick = { viewModel.addDomain() },
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
                                                                viewModel.removeDomain(
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
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // 2. Parameter Whitelist Row (Clickable Row Header)
                        val paramWhitelistSummary = if (state.domainWhitelistedParams.isEmpty()) {
                            stringResource(R.string.settings_no_params_whitelisted)
                        } else {
                            val count = state.domainWhitelistedParams.size
                            val unit = when (count) {
                                1 -> stringResource(R.string.settings_param_whitelisted_single)
                                else -> stringResource(R.string.settings_params_whitelisted_plural)
                            }
                            "$count $unit"
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
                                    .padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
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
                                            viewModel.onNewParamWhitelistDomainChanged(
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
                                            viewModel.onNewParamWhitelistParamChanged(
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
                                            onClick = { viewModel.addDomainWhitelistedParam() },
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
                                                                viewModel.removeDomainWhitelistedParam(
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
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // 3. Custom Blacklisted Parameters Row (Clickable Row Header)
                        val customParamsSummary = if (state.blacklistedParams.isEmpty()) {
                            stringResource(R.string.settings_no_custom_params_text)
                        } else {
                            val count = state.blacklistedParams.size
                            val unit = when (count) {
                                1 -> stringResource(R.string.settings_param_blacklisted_single)
                                else -> stringResource(R.string.settings_param_blacklisted_plural)
                            }
                            "$count $unit"
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
                                    .padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
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
                                        onValueChange = { viewModel.onParamInputChanged(it) },
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
                                            onClick = { viewModel.addParam() },
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
                                                            onClick = { viewModel.removeParam(param) },
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
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDefaultBlocklistSheet = true }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
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
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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

                        val selectedThemeState =
                            settingsRepository.selectedTheme.collectAsStateWithLifecycle(
                                initialValue = "slate"
                            )
                        val selectedTheme = selectedThemeState.value

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            themes.forEach { (themeKey, themeName) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (themeKey == "slate" || isPlusUnlocked) {
                                                scope.launch {
                                                    settingsRepository.setSelectedTheme(themeKey)
                                                }
                                            } else {
                                                showUpsellSheet = true
                                            }
                                        }
                                        .padding(
                                            start = 0.dp,
                                            end = 8.dp,
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
                                            onClick = {
                                                if (themeKey == "slate" || isPlusUnlocked) {
                                                    scope.launch {
                                                        settingsRepository.setSelectedTheme(themeKey)
                                                    }
                                                } else {
                                                    showUpsellSheet = true
                                                }
                                            }
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
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingToggleRow(
                            title = stringResource(R.string.settings_check_clipboard_title),
                            description = stringResource(R.string.settings_check_clipboard_desc),
                            checked = state.checkClipboardForLinks,
                            onToggle = { viewModel.setCheckClipboardForLinks(!state.checkClipboardForLinks) }
                        )

                        if (state.checkClipboardForLinks) {
                            Text(
                                text = stringResource(R.string.settings_clipboard_tier_heading),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
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
                                onSelect = { viewModel.setClipboardCleanTier(it) },
                                onLockedTap = { showUpsellSheet = true },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Share-to-clean tiered selector
                SettingCard(modifier = Modifier.animateContentSize()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_share_tier_heading),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
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
                            onSelect = { viewModel.setShareCleanTier(it) },
                            onLockedTap = { showUpsellSheet = true },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )

                        if (effectiveShareTier == ShareCleanTier.CLEAN_COPY_AND_SHARE) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            SettingToggleRow(
                                title = stringResource(R.string.settings_close_instead_title),
                                description = stringResource(R.string.settings_close_instead_desc),
                                checked = state.closeInsteadOfSharing,
                                onToggle = { viewModel.setCloseInsteadOfSharing(!state.closeInsteadOfSharing) }
                            )
                        }
                    }
                }

                // Other automation toggles
                SettingCard {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingToggleRow(
                            title = stringResource(R.string.settings_auto_expand_title),
                            description = stringResource(R.string.settings_auto_expand_desc),
                            checked = state.autoExpandShortUrls,
                            onToggle = { viewModel.setAutoExpandShortUrls(!state.autoExpandShortUrls) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        SettingToggleRow(
                            title = stringResource(R.string.settings_remove_mobile_subdomains_title),
                            description = stringResource(R.string.settings_remove_mobile_subdomains_desc),
                            checked = state.autoRemoveMobileSubdomains,
                            onToggle = { viewModel.setAutoRemoveMobileSubdomains(!state.autoRemoveMobileSubdomains) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        SettingToggleRow(
                            title = stringResource(R.string.settings_drop_trailing_slash_title),
                            description = stringResource(R.string.settings_drop_trailing_slash_desc),
                            checked = state.dropTrailingSlash,
                            onToggle = { viewModel.setDropTrailingSlash(!state.dropTrailingSlash) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        SettingToggleRow(
                            title = stringResource(R.string.settings_auto_clean_input_title),
                            description = stringResource(R.string.settings_auto_clean_input_desc),
                            checked = state.autoCleanOnInput,
                            onToggle = { viewModel.setAutoCleanOnInput(!state.autoCleanOnInput) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
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
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setAllowSystemBackup(!state.allowSystemBackup) }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
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
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onAboutClick() }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
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
 * [enabled] = false renders the row greyed; it stays tappable so a locked row can still
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
    val contentAlpha = if (enabled) 1f else 0.38f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { if (enabled) onToggle() else onLockedTap() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
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
    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
    disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    disabledCheckedBorderColor = Color.Transparent,
    disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
    disabledUncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
    disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
)
