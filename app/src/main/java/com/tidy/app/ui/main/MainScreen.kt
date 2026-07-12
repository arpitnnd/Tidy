package com.tidy.app.ui.main

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tidy.app.FlavorConfig
import com.tidy.app.R
import com.tidy.app.TidyApp
import com.tidy.app.data.UrlCleaner
import com.tidy.app.ui.components.FeatureRow
import com.tidy.app.ui.components.TidyModalBottomSheet
import com.tidy.app.ui.components.TooltipWrapper
import com.tidy.app.ui.components.shimmer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    sharedUrl: String?,
    crashReportText: String?,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel(),
    showPlusUpsell: Boolean = false
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as? android.app.Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val toastCleanedCopied = stringResource(R.string.toast_cleaned_copied)
    val toastCopied = stringResource(R.string.toast_copied)
    val toastNoBrowserApp = stringResource(R.string.toast_no_browser_app)
    val plusToastBulkCleanedTemplate = stringResource(R.string.plus_toast_bulk_cleaned)
    val crashToastCopied = stringResource(R.string.crash_toast_copied)
    val crashToastBrowserError = stringResource(R.string.crash_toast_browser_error)
    val dialogShareCrashTitle = stringResource(R.string.dialog_share_crash_title)

    val settingsRepository = TidyApp.instance.settingsRepository
    val dontAskAgainCrash by settingsRepository.dontAskAgainCrash.collectAsStateWithLifecycle(
        initialValue = false
    )

    val entitlementManager = TidyApp.instance.entitlementManager
    val isPlusUnlocked by entitlementManager.isPlusUnlocked.collectAsStateWithLifecycle(initialValue = false)
    var showUpsellSheet by remember { mutableStateOf(showPlusUpsell) }
    var bulkClipboardUrls by remember { mutableStateOf<List<String>?>(null) }
    val trackerDescriptions by settingsRepository.trackerDescriptions.collectAsStateWithLifecycle(
        initialValue = emptyMap()
    )
    val lastCleanedUrl by settingsRepository.lastCleanedUrl.collectAsStateWithLifecycle(initialValue = "")

    // Auto-detect clipboard URL on resume
    var clipboardUrl by remember { mutableStateOf<String?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var paramToWhitelist by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val introSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showViewReportDialog by remember { mutableStateOf(false) }
    var hasShownCrashSheetThisSession by rememberSaveable { mutableStateOf(false) }
    var showCrashSheet by remember { mutableStateOf(crashReportText != null && !dontAskAgainCrash && !hasShownCrashSheetThisSession) }
    val onDismissCrashReport = {
        showCrashSheet = false
        hasShownCrashSheetThisSession = true
    }

    // Clean URL if shared through Android intent
    LaunchedEffect(sharedUrl) {
        if (sharedUrl != null) {
            viewModel.cleanUrl(sharedUrl, isShared = true)
        }
    }

    // Debounced manual input auto-clean
    LaunchedEffect(state.inputUrl, state.autoCleanOnInput) {
        if (state.autoCleanOnInput && state.inputUrl.isNotEmpty()) {
            val trimmed = state.inputUrl.trim()
            val looksLikeUrl = trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true) ||
                    (trimmed.contains(".") && !trimmed.contains(" "))
            if (looksLikeUrl && trimmed != state.originalUrl && trimmed != state.expandedUrl) {
                delay(400.milliseconds)
                viewModel.cleanUrl(trimmed)
            }
        }
    }

    // Auto-dismiss clipboard suggestion banner after 5 seconds
    LaunchedEffect(clipboardUrl) {
        if (clipboardUrl != null) {
            delay(5.seconds)
            clipboardUrl = null
        }
    }

    // Collect and handle automation events (auto-copy & auto-close)
    LaunchedEffect(viewModel.automationEvents) {
        viewModel.automationEvents.collect { event ->
            when (event) {
                is MainScreenViewModel.AutomationAction.CopyAndClose -> {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip =
                        android.content.ClipData.newPlainText("Cleaned URL", event.cleanedUrl)
                    clipboard.setPrimaryClip(clip)
                    android.widget.Toast.makeText(
                        context,
                        toastCleanedCopied,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    if (event.close) {
                        activity?.finishAndRemoveTask()
                    }
                }
            }
        }
    }

    val currentInputUrl by rememberUpdatedState(state.inputUrl)
    val currentIsCleaned by rememberUpdatedState(state.isCleaned)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (currentInputUrl.isNotEmpty() || currentIsCleaned) {
                    clipboardUrl = null
                    bulkClipboardUrls = null
                    return@LifecycleEventObserver
                }
                try {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    if (clipboard.hasPrimaryClip()) {
                        val item = clipboard.primaryClip?.getItemAt(0)
                        val text = item?.text?.toString()?.trim()
                        if (text != null) {
                            val urls = extractUrls(text)
                            when {
                                urls.size > 1 -> {
                                    bulkClipboardUrls = urls
                                    clipboardUrl = null
                                }
                                urls.size == 1 -> {
                                    val singleUrl = urls[0]
                                    if (singleUrl != state.inputUrl && singleUrl != state.originalUrl && singleUrl != state.cleanedUrl && singleUrl != lastCleanedUrl) {
                                        clipboardUrl = singleUrl
                                    }
                                    bulkClipboardUrls = null
                                }
                                else -> {
                                    clipboardUrl = null
                                    bulkClipboardUrls = null
                                }
                            }
                        } else {
                            clipboardUrl = null
                            bulkClipboardUrls = null
                        }
                    } else {
                        clipboardUrl = null
                        bulkClipboardUrls = null
                    }
                } catch (e: Exception) {
                    clipboardUrl = null
                    bulkClipboardUrls = null
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // First-launch intro dialog
    var showIntro by remember { mutableStateOf(false) }
    LaunchedEffect(state.firstLaunchDone) {
        if (!state.firstLaunchDone) {
            showIntro = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_history)) {
                        IconButton(onClick = onHistoryClick) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = stringResource(R.string.main_history_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_settings)) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.main_settings_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            if (state.isCleaned) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TooltipWrapper(
                                tooltipText = stringResource(R.string.tooltip_copy_clean),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.copyToClipboard(context)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(toastCopied)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.copySuccess) {
                                            MaterialTheme.colorScheme.tertiary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        contentColor = if (state.copySuccess) {
                                            MaterialTheme.colorScheme.onTertiary
                                        } else {
                                            MaterialTheme.colorScheme.onPrimary
                                        }
                                    ),
                                    contentPadding = PaddingValues(vertical = 14.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.copySuccess) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (state.copySuccess) stringResource(R.string.main_copied) else stringResource(
                                            R.string.main_copy
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            TooltipWrapper(
                                tooltipText = stringResource(R.string.tooltip_share_clean),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.shareUrl(context) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(vertical = 14.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.main_share),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.isCleaned) {
                TooltipWrapper(tooltipText = stringResource(R.string.tooltip_clean_new)) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.onUrlInput("")
                            showBottomSheet = true
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.CleaningServices,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(stringResource(R.string.main_clean_new_url))
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state.isCleaned) {
                // Results Display
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 650.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    var showDetails by remember { mutableStateOf(false) }
                    val gradientBrush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(gradientBrush)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 20.dp,
                                        bottom = 20.dp
                                    ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val domainToWhitelist = remember(state.originalUrl) {
                                    extractDomain(state.originalUrl)
                                }

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(R.string.main_cleaned_url),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                    val removedCount = state.removedParams.size
                                    val badgeColor = if (removedCount > 0) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                    val badgeTextColor = if (removedCount > 0) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = badgeColor
                                    ) {
                                        Text(
                                            text = if (removedCount > 0) stringResource(
                                                R.string.history_item_removed_count,
                                                removedCount
                                            ) else stringResource(R.string.history_item_clean),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeTextColor,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = state.cleanedUrl,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 5,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (state.canExpand) {
                                        TooltipWrapper(tooltipText = stringResource(R.string.main_expand_url)) {
                                            IconButton(
                                                onClick = { viewModel.expandShortUrl() },
                                                enabled = !state.isExpanding,
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            ) {
                                                if (state.isExpanding) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(20.dp),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Link,
                                                        contentDescription = stringResource(R.string.main_expand_url)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_open_browser)) {
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        state.cleanedUrl.toUri()
                                                    ).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            toastNoBrowserApp
                                                        )
                                                    }
                                                }
                                            },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = stringResource(R.string.tooltip_open_browser)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            )

                            TooltipWrapper(
                                tooltipText = stringResource(if (showDetails) R.string.main_hide_details else R.string.main_show_details),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDetails = !showDetails }
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showDetails) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                    Text(
                                        text = if (showDetails) stringResource(R.string.main_hide_details) else stringResource(
                                            R.string.main_show_details
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.8f
                                        ),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            AnimatedVisibility(visible = showDetails) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.main_original_url),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.6f
                                            ),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = state.originalUrl,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    if (state.expandedUrl != null) {
                                        Column {
                                            Text(
                                                text = stringResource(R.string.main_expanded_url),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                    alpha = 0.6f
                                                ),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = state.expandedUrl!!,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    if (state.removedParams.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = stringResource(R.string.main_removed_parameters),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                    alpha = 0.6f
                                                ),
                                                fontWeight = FontWeight.Bold
                                            )
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                state.removedParams.forEach { param ->
                                                    AssistChip(
                                                        onClick = { paramToWhitelist = param },
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
                                                            color = MaterialTheme.colorScheme.outline.copy(
                                                                alpha = 0.3f
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // DECLUTTERED Welcome State
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val minHeight = maxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 650.dp)
                            .heightIn(min = minHeight)
                            .verticalScroll(rememberScrollState())
                            .animateContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (!showIntro) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                Spacer(modifier = Modifier.height(24.dp))

                                // Logo
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.4f
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CleaningServices,
                                        contentDescription = "Tidy",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))

                                // Title & Tagline
                                Text(
                                    text = stringResource(R.string.welcome_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.welcome_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                // Minimal stat line (with shimmer loading state)
                                when {
                                    state.isInitialLoading -> {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(220.dp)
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .shimmer()
                                        )
                                    }
                                    state.totalCleanedCount > 0 -> {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Surface(
                                            onClick = onHistoryClick,
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    R.string.main_stats_summary,
                                                    state.totalCleanedCount,
                                                    state.totalTrackersBlocked
                                                ),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                )
                                            )
                                        }
                                    }
                                }

                                // Compact Trust Markers Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CompactFeatureRow(
                                        icon = Icons.Outlined.CloudOff,
                                        text = stringResource(R.string.welcome_local_title)
                                    )
                                    VerticalDivider(
                                        modifier = Modifier
                                            .height(16.dp)
                                            .width(1.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                    CompactFeatureRow(
                                        icon = Icons.Outlined.Tune,
                                        text = stringResource(R.string.welcome_privacy_title)
                                    )
                                    VerticalDivider(
                                        modifier = Modifier
                                            .height(16.dp)
                                            .width(1.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                    CompactFeatureRow(
                                        icon = Icons.Outlined.Block,
                                        text = stringResource(R.string.welcome_free_title)
                                    )
                                }
                            }
                        }

                        // Bulk Clipboard Clean card (gated behind Tidy+)
                        com.tidy.app.FlavorConfig.BulkClipboardCleanCard(
                            bulkClipboardUrls = bulkClipboardUrls,
                            onDismiss = { bulkClipboardUrls = null },
                            isPlusUnlocked = isPlusUnlocked,
                            onShowUpsell = { showUpsellSheet = true },
                            onCleanExecute = {
                                bulkClipboardUrls?.let { urls ->
                                    scope.launch {
                                        val settings = TidyApp.instance.settingsRepository
                                        val whitelist = settings.whitelistedDomains.first()
                                        val customBlacklist = settings.blacklistedParams.first()
                                        val domainParams = settings.domainWhitelistedParams.first()
                                        val removeMobile =
                                            settings.autoRemoveMobileSubdomains.first()

                                        val cleaned = urls.map { url ->
                                            UrlCleaner().clean(
                                                urlStr = url,
                                                whitelistedDomains = whitelist,
                                                customBlacklistParams = customBlacklist,
                                                domainWhitelistedParams = domainParams,
                                                removeMobileSubdomains = removeMobile
                                            ).cleanedUrl
                                        }

                                        val joinedCleaned = cleaned.joinToString("\n")

                                        val clipboard =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = android.content.ClipData.newPlainText(
                                            "Cleaned URLs",
                                            joinedCleaned
                                        )
                                        clipboard.setPrimaryClip(clip)

                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                String.format(
                                                    plusToastBulkCleanedTemplate,
                                                    urls.size
                                                )
                                            )
                                        }
                                        bulkClipboardUrls = null
                                    }
                                }
                            }
                        )

                        // Input card on welcome (pushed to bottom)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        ) {
                            UrlInputCard(
                                state = state,
                                viewModel = viewModel,
                                clipboardUrl = clipboardUrl,
                                onDismissClipboard = { clipboardUrl = null }
                            )
                        }
                    }
                }
            }
        }

        // Clean New URL Bottom Sheet
        if (showBottomSheet) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(focusRequester) {
                kotlinx.coroutines.delay(350.milliseconds)
                try {
                    focusRequester.requestFocus()
                } catch (e: Exception) {
                }
            }

            TidyModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) { scrollFix ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .nestedScroll(scrollFix)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.main_clean_new_url),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = state.inputUrl,
                        onValueChange = { viewModel.onUrlInput(it) },
                        placeholder = {
                            Text(
                                stringResource(R.string.welcome_placeholder),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            if (state.inputUrl.isNotEmpty()) {
                                TooltipWrapper(tooltipText = stringResource(R.string.tooltip_clear_input)) {
                                    IconButton(onClick = { viewModel.onUrlInput("") }) {
                                        Icon(
                                            Icons.Filled.Clear,
                                            contentDescription = stringResource(R.string.tooltip_clear_input)
                                        )
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isValidInputUrl(state.inputUrl) && !state.isLoading) {
                                    viewModel.cleanUrl(state.inputUrl)
                                    showBottomSheet = false
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    AnimatedVisibility(visible = clipboardUrl != null) {
                        clipboardUrl?.let { url ->
                            ClipboardActionBanner(
                                url = url,
                                onActionClick = {
                                    viewModel.onUrlInput(url)
                                    viewModel.cleanUrl(url)
                                    clipboardUrl = null
                                    showBottomSheet = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.cleanUrl(state.inputUrl)
                            showBottomSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isValidInputUrl(state.inputUrl) && !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                stringResource(R.string.button_clean_url),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // First-launch Intro Bottom Sheet
        if (showIntro) {
            TidyModalBottomSheet(
                onDismissRequest = {
                    showIntro = false
                    viewModel.markFirstLaunchDone()
                },
                sheetState = introSheetState
            ) { scrollFix ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding()
                        .nestedScroll(scrollFix),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.welcome_welcome_to),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stringResource(R.string.welcome_welcome_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Trust markers
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FeatureRow(
                            icon = Icons.Outlined.CloudOff,
                            title = stringResource(R.string.welcome_local_title),
                            description = stringResource(R.string.welcome_local_subtitle)
                        )
                        FeatureRow(
                            icon = Icons.Outlined.Tune,
                            title = stringResource(R.string.welcome_privacy_title),
                            description = stringResource(R.string.welcome_privacy_subtitle)
                        )
                        FeatureRow(
                            icon = Icons.Outlined.Block,
                            title = stringResource(R.string.welcome_free_title),
                            description = stringResource(R.string.welcome_free_subtitle)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showIntro = false
                            viewModel.markFirstLaunchDone()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            stringResource(R.string.welcome_get_started),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FlavorConfig.OnboardingExtra {
                        showIntro = false
                        viewModel.markFirstLaunchDone()
                    }
                }
            }
        }

        // Crash Report Sheet
        if (showCrashSheet && crashReportText != null) {
            TidyModalBottomSheet(
                onDismissRequest = { onDismissCrashReport() }
            ) { scrollFix ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .nestedScroll(scrollFix)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = stringResource(R.string.crash_sheet_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.crash_sheet_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // Don't ask again toggle
                    var dontAskChecked by remember { mutableStateOf(dontAskAgainCrash) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                dontAskChecked = !dontAskChecked
                                scope.launch {
                                    settingsRepository.setDontAskAgainCrash(dontAskChecked)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = dontAskChecked,
                            onCheckedChange = {
                                dontAskChecked = it
                                scope.launch {
                                    settingsRepository.setDontAskAgainCrash(it)
                                }
                            }
                        )
                        Text(
                            text = stringResource(R.string.crash_dont_ask_again),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TooltipWrapper(
                            tooltipText = stringResource(R.string.crash_share_log),
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, crashReportText)
                                        type = "text/plain"
                                    }
                                    val shareIntent =
                                        Intent.createChooser(
                                            sendIntent,
                                            dialogShareCrashTitle
                                        )
                                    context.startActivity(shareIntent)
                                    onDismissCrashReport()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.crash_share_log),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        TooltipWrapper(
                            tooltipText = stringResource(R.string.crash_report_github),
                            modifier = Modifier.weight(1f)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard =
                                        context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText(
                                        "Crash Report",
                                        crashReportText
                                    )
                                    clipboard.setPrimaryClip(clip)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(crashToastCopied)
                                    }

                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        "https://github.com/arpitnnd/Tidy/issues/new?title=Crash%20Report&body=Paste%20copied%20crash%20log%20here".toUri()
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(crashToastBrowserError)
                                        }
                                    }
                                    onDismissCrashReport()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.crash_report_github),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    TooltipWrapper(tooltipText = stringResource(R.string.crash_view_local)) {
                        TextButton(onClick = { showViewReportDialog = true }) {
                            Text(stringResource(R.string.crash_view_local))
                        }
                    }
                }
            }
        }

        // View Report Details Dialog
        if (showViewReportDialog && crashReportText != null) {
            AlertDialog(
                onDismissRequest = { showViewReportDialog = false },
                title = {
                    Text(
                        stringResource(R.string.dialog_crash_log_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = crashReportText,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showViewReportDialog = false }) {
                        Text(stringResource(R.string.dialog_close))
                    }
                }
            )
        }
    }

    if (paramToWhitelist != null) {
        val param = paramToWhitelist!!
        val domain = extractDomain(state.originalUrl)
        val description =
            trackerDescriptions[param] ?: stringResource(R.string.details_no_explanation)

        TidyModalBottomSheet(
            onDismissRequest = { paramToWhitelist = null }
        ) { scrollFix ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .nestedScroll(scrollFix),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = param,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val success = launchGitHubBugReport(context, param, domain)
                            if (!success) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(crashToastBrowserError)
                                }
                            }
                            paramToWhitelist = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.details_report_issue),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.addDomainWhitelistedParam(domain, param)
                            paramToWhitelist = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.details_always_keep),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showUpsellSheet) {
        com.tidy.app.FlavorConfig.ShowUpsellBottomSheet(
            onDismiss = { showUpsellSheet = false }
        )
    }
}

private fun extractUrls(text: String): List<String> {
    val regex = "(https?://[\\w\\d:#@%/;\\$()~_?\\+-=\\\\\\.&]+)".toRegex()
    return regex.findAll(text).map { it.value }.toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrlInputCard(
    state: MainScreenViewModel.UiState,
    viewModel: MainScreenViewModel,
    clipboardUrl: String?,
    onDismissClipboard: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.welcome_paste_prompt),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.inputUrl,
                    onValueChange = { viewModel.onUrlInput(it) },
                    placeholder = {
                        Text(
                            stringResource(R.string.welcome_placeholder),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        if (state.inputUrl.isNotEmpty()) {
                            TooltipWrapper(tooltipText = stringResource(R.string.tooltip_clear_input)) {
                                IconButton(onClick = { viewModel.clear() }) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = stringResource(R.string.tooltip_clear_input)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValidInputUrl(state.inputUrl) && !state.isLoading) {
                                viewModel.cleanUrl(state.inputUrl)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                AnimatedVisibility(visible = clipboardUrl != null) {
                    clipboardUrl?.let { url ->
                        ClipboardActionBanner(
                            url = url,
                            onActionClick = {
                                viewModel.onUrlInput(url)
                                viewModel.cleanUrl(url)
                                onDismissClipboard()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                TooltipWrapper(tooltipText = stringResource(R.string.tooltip_process_url)) {
                    Button(
                        onClick = { viewModel.cleanUrl(state.inputUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isValidInputUrl(state.inputUrl) && !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                stringResource(R.string.button_clean_url),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipboardActionBanner(
    url: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentPaste,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.banner_found_clipboard),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.banner_paste_clean),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private fun extractDomain(url: String): String {
    var temp = url
    val protoIndex = temp.indexOf("://")
    if (protoIndex != -1) temp = temp.substring(protoIndex + 3)
    val slashIndex = temp.indexOf('/')
    if (slashIndex != -1) temp = temp.substring(0, slashIndex)
    val qIndex = temp.indexOf('?')
    if (qIndex != -1) temp = temp.substring(0, qIndex)
    val hashIndex = temp.indexOf('#')
    if (hashIndex != -1) temp = temp.substring(0, hashIndex)
    val portIndex = temp.indexOf(':')
    if (portIndex != -1) temp = temp.substring(0, portIndex)
    return temp.trim().lowercase()
}

private fun launchGitHubBugReport(
    context: android.content.Context,
    param: String,
    domain: String
): Boolean {
    val title = "Broken parameter: $param on $domain"
    val body = """
**Parameter:** $param
**Domain:** $domain

**Description:**
The parameter `$param` was removed from `$domain`, which broke the site or removed required information. Please review this rule.
""".trimIndent()

    return try {
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
        val encodedBody = java.net.URLEncoder.encode(body, "UTF-8")
        val urlStr =
            "https://github.com/arpitnnd/Tidy/issues/new?title=$encodedTitle&body=$encodedBody&labels=bug-report"

        val intent = Intent(Intent.ACTION_VIEW, urlStr.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}

@Composable
private fun CompactFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun isValidInputUrl(url: String): Boolean {
    val trimmed = url.trim()
    if (trimmed.length < 3) return false
    return trimmed.contains(".") || trimmed.startsWith("http://") || trimmed.startsWith("https://")
}

private fun looksLikeUrl(text: String): Boolean {
    val trimmed = text.trim()
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith(
            "https://",
            ignoreCase = true
        )
    ) {
        return true
    }
    if (trimmed.contains(" ") || !trimmed.contains(".")) return false
    val firstSlash = trimmed.indexOf('/')
    val hostPart = if (firstSlash != -1) trimmed.substring(0, firstSlash) else trimmed
    val lastDot = hostPart.lastIndexOf('.')
    if (lastDot == -1 || lastDot == hostPart.length - 1) return false
    val tld = hostPart.substring(lastDot + 1)
    return tld.length >= 2 && tld.all { it.isLetter() }
}

