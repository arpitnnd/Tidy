package com.tidy.app.ui.history

import androidx.compose.foundation.style.CommonStyle
import com.tidy.designsystem.TidyComponentStyles
import com.tidy.designsystem.TidyStyle
import com.tidy.designsystem.TidyStyledCard

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tidy.app.R
import com.tidy.app.ui.components.StatusBadge
import com.tidy.app.data.HistoryEntry
import com.tidy.app.data.trackerDescriptionFor
import com.tidy.app.ui.components.ParamDetailBottomSheet
import com.tidy.app.ui.components.ScreenSectionHeader
import com.tidy.app.ui.components.TidyTopAppBar
import com.tidy.app.ui.components.TooltipWrapper
import com.tidy.app.ui.components.shimmer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreenContent(state, onBackClick, modifier, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryScreenContent(
    state: HistoryScreenViewModel.UiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: HistoryScreenActions = object : HistoryScreenActions {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val latestActions by rememberUpdatedState(actions)
    DisposableEffect(Unit) {
        onDispose { latestActions.discardClearUndo() }
    }

    val historyToastExported = stringResource(R.string.history_toast_exported)
    val historyToastExportFailed = stringResource(R.string.history_toast_export_failed)
    val historyToastImportFailed = stringResource(R.string.history_toast_import_failed)
    val historyImportSuccessTemplate = stringResource(R.string.history_import_success)
    val historyImportInvalid = stringResource(R.string.history_import_invalid)
    val historyClearedToast = stringResource(R.string.history_cleared_toast)
    val historyUndoAction = stringResource(R.string.history_undo_action)
    val mainCopied = stringResource(R.string.main_copied)
    val mainCleanedUrl = stringResource(R.string.main_cleaned_url)
    val toastNoBrowserApp = stringResource(R.string.toast_no_browser_app)
    val crashToastBrowserError = stringResource(R.string.crash_toast_browser_error)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = actions.getExportJson()
                    context.contentResolver.openOutputStream(it)?.bufferedWriter()?.use { writer ->
                        writer.write(json)
                    }
                    scope.launch { snackbarHostState.showSnackbar(historyToastExported) }
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar(historyToastExportFailed) }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val json = inputStream?.bufferedReader()?.readText() ?: return@let
                    inputStream.close()
                    actions.importHistory(json) { count ->
                        scope.launch {
                            val msg = if (count >= 0) {
                                String.format(historyImportSuccessTemplate, count)
                            } else {
                                historyImportInvalid
                            }
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar(historyToastImportFailed) }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TidyTopAppBar(
                title = stringResource(R.string.history_title),
                onBackClick = onBackClick,
                actions = {
                    // Import
                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_import)) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                            }
                            importLauncher.launch(intent)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = stringResource(R.string.history_import_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Export
                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_export)) {
                        IconButton(onClick = {
                            val dateStr =
                                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            exportLauncher.launch("tidy_history_$dateStr.json")
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.FileUpload,
                                contentDescription = stringResource(R.string.history_export_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Clear all
                    if (state.history.isNotEmpty()) {
                        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_clear_all)) {
                            IconButton(onClick = { actions.showClearConfirmation() }) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = stringResource(R.string.history_clear_desc),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val fontScale = LocalDensity.current.fontScale
            val layoutDirection = LocalLayoutDirection.current
            val startInset = paddingValues.calculateStartPadding(layoutDirection)
            val endInset = paddingValues.calculateEndPadding(layoutDirection)
            val availableWidth = maxWidth - startInset - endInset
            val horizontalPadding = 16.dp + ((availableWidth - 1000.dp) / 2).coerceAtLeast(0.dp)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(320.dp * fontScale.coerceAtLeast(1f)),
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paddingValues),
                contentPadding = PaddingValues(
                    start = startInset + horizontalPadding,
                    end = endInset + horizontalPadding,
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Analytics Section
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ScreenSectionHeader(
                        text = stringResource(R.string.history_dashboard),
                        modifier = Modifier.padding(start = 0.dp)
                    )
                }

                if (state.isInitialLoading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(108.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .shimmer()
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(108.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .shimmer()
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .shimmer()
                        )
                    }
                } else {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatsCard(
                                title = stringResource(R.string.history_urls_cleaned),
                                value = "${state.totalCleanedCount}",
                                icon = Icons.Outlined.Link,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                title = stringResource(R.string.history_trackers_blocked),
                                value = "${state.totalTrackersBlocked}",
                                icon = Icons.Outlined.Shield,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        BandwidthCard(
                            trackersCount = state.totalTrackersBlocked,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // History Section
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ScreenSectionHeader(
                        text = stringResource(R.string.history_clean_history),
                        modifier = Modifier.padding(start = 0.dp)
                    )
                }

                if (state.isInitialLoading) {
                    items(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .shimmer()
                        )
                    }
                } else if (state.history.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.history_no_history),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.history_no_history_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(state.history, key = { it.id }) { entry ->
                        HistoryItem(
                            entry = entry,
                            trackerDescriptions = state.trackerDescriptions,
                            onShowSnackbar = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            },
                            onWhitelist = actions::addDomainWhitelistedParam,
                            reportIssueFailedMessage = crashToastBrowserError
                        )
                    }
                }

                // Bottom spacing
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Clear confirmation dialog
    if (state.showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { actions.dismissClearConfirmation() },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    stringResource(R.string.history_clear_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    pluralStringResource(
                        R.plurals.history_clear_dialog_text,
                        state.history.size,
                        state.history.size
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        actions.clearHistory()
                        scope.launch {
                            try {
                                val result = snackbarHostState.showSnackbar(
                                    message = historyClearedToast,
                                    actionLabel = historyUndoAction,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    actions.undoClearHistory()
                                }
                            } finally {
                                actions.discardClearUndo()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.history_clear_all), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { actions.dismissClearConfirmation() }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    style: TidyStyle = CommonStyle
) {
    TidyStyledCard(
        modifier = modifier,
        style = CommonStyle(TidyComponentStyles.statisticsCard, style)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color.copy(alpha = 0.15f).compositeOver(MaterialTheme.colorScheme.surface)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BandwidthCard(
    trackersCount: Int,
    modifier: Modifier = Modifier,
    style: TidyStyle = CommonStyle
) {
    val bytes = trackersCount * 52
    val formattedValue = when {
        bytes >= 1_048_576 -> stringResource(
            R.string.history_bandwidth_megabytes,
            bytes / 1_048_576f
        )

        bytes >= 1024 -> stringResource(R.string.history_bandwidth_kilobytes, bytes / 1024f)
        else -> pluralStringResource(R.plurals.history_bandwidth_bytes, bytes, bytes)
    }

    TidyStyledCard(
        modifier = modifier,
        style = CommonStyle(TidyComponentStyles.statisticsCard, style)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            .compositeOver(MaterialTheme.colorScheme.surface)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.history_bandwidth_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HistoryItem(
    entry: HistoryEntry,
    trackerDescriptions: Map<String, String>,
    onShowSnackbar: (String) -> Unit,
    onWhitelist: (domain: String, param: String) -> Unit,
    reportIssueFailedMessage: String,
    modifier: Modifier = Modifier,
    style: TidyStyle = CommonStyle
) {
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }
    var showFullOriginalUrl by rememberSaveable(entry.id) { mutableStateOf(false) }
    var selectedParam by remember { mutableStateOf<String?>(null) }
    val datePattern = stringResource(R.string.history_entry_date_format)
    val dateFormat = remember(datePattern) { SimpleDateFormat(datePattern, Locale.getDefault()) }
    val formattedDate = remember(entry.timestamp, dateFormat) {
        dateFormat.format(Date(entry.timestamp))
    }
    val context = LocalContext.current
    val mainCopied = stringResource(R.string.main_copied)
    val mainCleanedUrl = stringResource(R.string.main_cleaned_url)
    val mainOriginalUrl = stringResource(R.string.main_original_url)
    val toastNoBrowserApp = stringResource(R.string.toast_no_browser_app)

    TidyStyledCard(
        modifier = modifier,
        onClick = { expanded = !expanded },
        style = CommonStyle(TidyComponentStyles.historyCard, style)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = FlexBoxConfig {
                    wrap(FlexWrap.Wrap)
                    alignItems(FlexAlignItems.Center)
                    columnGap(8.dp)
                    rowGap(4.dp)
                }
            ) {
                Text(
                    text = entry.domain,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusBadge(
                    text = if (entry.removedParamsCount > 0) {
                        stringResource(
                            R.string.history_item_removed_count,
                            entry.removedParamsCount
                        )
                    } else {
                        stringResource(R.string.history_item_clean)
                    },
                    containerColor = if (entry.removedParamsCount > 0) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    contentColor = if (entry.removedParamsCount > 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                text = entry.cleanedUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (expanded) 10 else 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            val originalUri = remember(entry.originalUrl) {
                try {
                    entry.originalUrl.toUri()
                } catch (e: Exception) {
                    null
                }
            }
            val cleanedUri = remember(entry.cleanedUrl) {
                try {
                    entry.cleanedUrl.toUri()
                } catch (e: Exception) {
                    null
                }
            }
            val removedParams = remember(originalUri, cleanedUri) {
                if (originalUri != null && cleanedUri != null) {
                    val originalQueryKeys = try {
                        originalUri.queryParameterNames
                    } catch (e: Exception) {
                        emptySet<String>()
                    }
                    val cleanedQueryKeys = try {
                        cleanedUri.queryParameterNames
                    } catch (e: Exception) {
                        emptySet<String>()
                    }
                    originalQueryKeys.filter { it !in cleanedQueryKeys }
                } else {
                    emptyList()
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showFullOriginalUrl = true }
                    ) {
                        Text(
                            text = stringResource(R.string.main_original_url),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = entry.originalUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (removedParams.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.main_removed_parameters),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            removedParams.forEach { param ->
                                AssistChip(
                                    onClick = { selectedParam = param },
                                    label = {
                                        Text(
                                            param,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                    FlexBox(
                        modifier = Modifier.fillMaxWidth(),
                        config = FlexBoxConfig {
                            wrap(FlexWrap.Wrap)
                            alignItems(FlexAlignItems.Center)
                            columnGap(8.dp)
                            rowGap(4.dp)
                        }
                    ) {
                        // Copy Original Button
                        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_copy_original)) {
                            TextButton(
                                onClick = {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText(
                                        mainOriginalUrl,
                                        entry.originalUrl
                                    )
                                    clipboard.setPrimaryClip(clip)
                                    onShowSnackbar(mainCopied)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 1.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.history_copy_original),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        // Copy Button
                        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_copy_clean)) {
                            TextButton(
                                onClick = {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText(
                                        mainCleanedUrl,
                                        entry.cleanedUrl
                                    )
                                    clipboard.setPrimaryClip(clip)
                                    onShowSnackbar(mainCopied)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 1.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.main_copy),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        // Open Button
                        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_open_browser)) {
                            TextButton(
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            entry.cleanedUrl.toUri()
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        onShowSnackbar(toastNoBrowserApp)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 1.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.history_open),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFullOriginalUrl) {
        AlertDialog(
            onDismissRequest = { showFullOriginalUrl = false },
            title = { Text(stringResource(R.string.main_original_url)) },
            text = {
                Text(
                    text = entry.originalUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showFullOriginalUrl = false }) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }

    if (selectedParam != null) {
        val param = selectedParam!!
        val desc = trackerDescriptions.trackerDescriptionFor(param)
            ?: stringResource(R.string.details_no_explanation)
        ParamDetailBottomSheet(
            param = param,
            description = desc,
            domain = entry.domain,
            onDismiss = { selectedParam = null },
            onWhitelist = onWhitelist,
            onReportIssueFailed = { onShowSnackbar(reportIssueFailedMessage) }
        )
    }
}

