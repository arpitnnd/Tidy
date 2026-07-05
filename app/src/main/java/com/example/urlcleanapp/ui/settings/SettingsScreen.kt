package com.example.urlcleanapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.urlcleanapp.R
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.ChevronRight

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val entitlementManager = com.example.urlcleanapp.UrlCleanApp.instance.entitlementManager
    val isPlusUnlocked by entitlementManager.isPlusUnlocked.collectAsStateWithLifecycle(initialValue = false)

    val settingsRepository = com.example.urlcleanapp.UrlCleanApp.instance.settingsRepository
    val scope = rememberCoroutineScope()

    var showMigrationDialog by remember { mutableStateOf(false) }
    var showUpsellSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back_desc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
            com.example.urlcleanapp.FlavorConfig.SettingsUpgradeRow {
                if (com.example.urlcleanapp.FlavorConfig.isPlayFlavor) {
                    showUpsellSheet = true
                } else {
                    showMigrationDialog = true
                }
            }

            var isBypassListExpanded by remember { mutableStateOf(false) }

            // Bypass List Collapsible Header
            Surface(
                onClick = { isBypassListExpanded = !isBypassListExpanded },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.settings_bypass_list),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val summaryText = if (state.whitelistedDomains.isEmpty()) {
                            stringResource(R.string.settings_no_domains_bypassed)
                        } else {
                            val count = state.whitelistedDomains.size
                            val unit = if (count == 1) {
                                stringResource(R.string.settings_domain_bypassed_single)
                            } else {
                                stringResource(R.string.settings_domains_bypassed_plural)
                            }
                            "$count $unit"
                        }
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { isBypassListExpanded = !isBypassListExpanded }) {
                        Icon(
                            imageVector = if (isBypassListExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isBypassListExpanded) stringResource(R.string.settings_collapse) else stringResource(R.string.settings_expand)
                        )
                    }
                }
            }

            if (isBypassListExpanded) {
                // Bypass Domains Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            val isDomainValid = state.domainInput.trim().run {
                                isNotEmpty() && contains(".") && !contains(" ")
                            }

                            FilledIconButton(
                                onClick = { viewModel.addDomain() },
                                enabled = isDomainValid,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.settings_add_domain_desc))
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
                                        label = { Text(domain, style = MaterialTheme.typography.bodySmall) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { viewModel.removeDomain(domain) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    contentDescription = stringResource(R.string.settings_collapse),
                                                    modifier = Modifier.size(16.dp)
                                                )
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            var isParamWhitelistExpanded by remember { mutableStateOf(false) }

            // Domain Parameter Whitelist Collapsible Header
            Surface(
                onClick = { isParamWhitelistExpanded = !isParamWhitelistExpanded },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.settings_param_whitelist_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val summaryText = if (state.domainWhitelistedParams.isEmpty()) {
                            stringResource(R.string.settings_no_params_whitelisted)
                        } else {
                            val count = state.domainWhitelistedParams.size
                            val unit = if (count == 1) {
                                stringResource(R.string.settings_param_whitelisted_single)
                            } else {
                                stringResource(R.string.settings_params_whitelisted_plural)
                            }
                            "$count $unit"
                        }
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { isParamWhitelistExpanded = !isParamWhitelistExpanded }) {
                        Icon(
                            imageVector = if (isParamWhitelistExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isParamWhitelistExpanded) stringResource(R.string.settings_collapse) else stringResource(R.string.settings_expand)
                        )
                    }
                }
            }

            if (isParamWhitelistExpanded) {
                // Domain Parameter Whitelist Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                onValueChange = { viewModel.onNewParamWhitelistDomainChanged(it) },
                                placeholder = { Text(stringResource(R.string.settings_placeholder_domain)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            OutlinedTextField(
                                value = state.newParamWhitelistParam,
                                onValueChange = { viewModel.onNewParamWhitelistParamChanged(it) },
                                placeholder = { Text(stringResource(R.string.settings_placeholder_whitelist_param)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            val isParamWhitelistValid = state.newParamWhitelistDomain.trim().run {
                                isNotEmpty() && contains(".") && !contains(" ")
                            } && state.newParamWhitelistParam.trim().run {
                                isNotEmpty() && !contains(" ") && !contains("?") && !contains("&") && !contains("=")
                            }

                            FilledIconButton(
                                onClick = { viewModel.addDomainWhitelistedParam() },
                                enabled = isParamWhitelistValid,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.settings_add_param_whitelist_desc))
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
                                        label = { Text(displayLabel, style = MaterialTheme.typography.bodySmall) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { viewModel.removeDomainWhitelistedParam(entry) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    contentDescription = stringResource(R.string.settings_collapse),
                                                    modifier = Modifier.size(16.dp)
                                                )
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.settings_custom_params_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            // Custom Blacklisted Params Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        val isParamValid = state.paramInput.trim().run {
                            isNotEmpty() && !contains(" ") && !contains("?") && !contains("&") && !contains("=")
                        }

                        FilledIconButton(
                            onClick = { viewModel.addParam() },
                            enabled = isParamValid,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.settings_add_param_desc))
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
                                    label = { Text(param, style = MaterialTheme.typography.bodySmall) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { viewModel.removeParam(param) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = stringResource(R.string.settings_collapse),
                                                modifier = Modifier.size(16.dp)
                                            )
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Appearance Theme Selector
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customize Tidy's visual theme.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val themes = listOf(
                        "slate" to "Sage Slate (Default)",
                        "forest" to "Forest Green (Premium)",
                        "ocean" to "Ocean Blue (Premium)",
                        "velvet" to "Dark Velvet (Premium)"
                    )

                    val selectedThemeState = settingsRepository.selectedTheme.collectAsStateWithLifecycle(initialValue = "slate")
                    val selectedTheme = selectedThemeState.value

                    themes.forEach { (themeKey, themeName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (themeKey == "slate" || isPlusUnlocked) {
                                        scope.launch {
                                            settingsRepository.setSelectedTheme(themeKey)
                                        }
                                    } else {
                                        showUpsellSheet = true
                                    }
                                }
                                .padding(vertical = 4.dp),
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
                                    contentDescription = "Premium Theme",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.settings_automation),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            // Automation Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .run {
                                if (!isPlusUnlocked) {
                                    clickable { showUpsellSheet = true }
                                } else this
                            },
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_copy_shared_title),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!isPlusUnlocked) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Outlined.Lock,
                                        contentDescription = "Premium Feature",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_copy_shared_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.autoCopyOnShare && isPlusUnlocked,
                            onCheckedChange = {
                                if (isPlusUnlocked) {
                                    viewModel.setAutoCopyOnShare(it)
                                } else {
                                    showUpsellSheet = true
                                }
                            },
                            colors = clearSwitchColors()
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .run {
                                if (!isPlusUnlocked) {
                                    clickable { showUpsellSheet = true }
                                } else this
                            },
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_close_shared_title),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!isPlusUnlocked) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Outlined.Lock,
                                        contentDescription = "Premium Feature",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_close_shared_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.autoCloseOnShare && isPlusUnlocked,
                            onCheckedChange = {
                                if (isPlusUnlocked) {
                                    viewModel.setAutoCloseOnShare(it)
                                } else {
                                    showUpsellSheet = true
                                }
                            },
                            enabled = (state.autoCopyOnShare && isPlusUnlocked) || !isPlusUnlocked,
                            colors = clearSwitchColors()
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_auto_expand_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_auto_expand_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.autoExpandShortUrls,
                            onCheckedChange = { viewModel.setAutoExpandShortUrls(it) },
                            colors = clearSwitchColors()
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_remove_mobile_subdomains_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_remove_mobile_subdomains_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.autoRemoveMobileSubdomains,
                            onCheckedChange = { viewModel.setAutoRemoveMobileSubdomains(it) },
                            colors = clearSwitchColors()
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_auto_clean_launch_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_auto_clean_launch_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.autoCleanClipboardOnLaunch,
                            onCheckedChange = { viewModel.setAutoCleanClipboardOnLaunch(it) },
                            colors = clearSwitchColors()
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_auto_clean_input_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_auto_clean_input_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.autoCleanOnInput,
                            onCheckedChange = { viewModel.setAutoCleanOnInput(it) },
                            colors = clearSwitchColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.rules_blocklist_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://github.com/arpitnnd/Tidy/commits/main/blocklist/trackers.json")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                }
                            },
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_view_blocklist_changes_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_view_blocklist_changes_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Open",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Diagnostics & Crashes (if crash report exists)
            var currentCrashReportText by remember {
                mutableStateOf(
                    try {
                        val dir = java.io.File(context.filesDir, "crash_reports")
                        if (dir.exists()) {
                            val files = dir.listFiles()
                            if (!files.isNullOrEmpty()) {
                                files.sortBy { it.lastModified() }
                                files.last().readText()
                            } else null
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                )
            }

            var showSettingsCrashDialog by remember { mutableStateOf(false) }

            if (currentCrashReportText != null) {
                Text(
                    text = stringResource(R.string.settings_diagnostics_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSettingsCrashDialog = true },
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_crash_log_title),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_crash_log_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (showSettingsCrashDialog && currentCrashReportText != null) {
                AlertDialog(
                    onDismissRequest = { showSettingsCrashDialog = false },
                    title = { Text(stringResource(R.string.dialog_crash_log_title)) },
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = currentCrashReportText!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, currentCrashReportText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    try {
                                        context.startActivity(shareIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not share", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.dialog_share))
                            }
                            TextButton(
                                onClick = {
                                    try {
                                        val dir = java.io.File(context.filesDir, "crash_reports")
                                        if (dir.exists()) {
                                            dir.deleteRecursively()
                                        }
                                        currentCrashReportText = null
                                        showSettingsCrashDialog = false
                                        Toast.makeText(context, context.getString(R.string.toast_crash_deleted), Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(stringResource(R.string.dialog_delete))
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSettingsCrashDialog = false }) {
                            Text(stringResource(R.string.dialog_cancel))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val url = if (com.example.urlcleanapp.BuildConfig.FLAVOR == "play") {
                            "https://play.google.com/store/apps/details?id=${context.packageName}"
                        } else {
                            "https://github.com/arpitnnd/Tidy/releases"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidy Version ${com.example.urlcleanapp.BuildConfig.VERSION_NAME} (${com.example.urlcleanapp.BuildConfig.FLAVOR.uppercase()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    }

    if (showMigrationDialog) {
        com.example.urlcleanapp.ui.settings.SettingsMigrationViews.MigrationDialog(
            onDismiss = { showMigrationDialog = false }
        )
    }

    if (showUpsellSheet) {
        com.example.urlcleanapp.FlavorConfig.ShowUpsellBottomSheet(
            onDismiss = { showUpsellSheet = false }
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
