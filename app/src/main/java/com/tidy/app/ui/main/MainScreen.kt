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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.tidy.app.data.ClipboardCleanTier
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.UrlCleaner
import com.tidy.app.data.UrlDetection
import com.tidy.app.ui.components.ArrowsOutward
import com.tidy.app.ui.components.FeatureRow
import com.tidy.app.ui.components.CrashReportBottomSheet
import com.tidy.app.ui.components.ParamDetailBottomSheet
import com.tidy.app.ui.components.TidyModalBottomSheet
import com.tidy.app.ui.components.TooltipWrapper
import com.tidy.app.ui.components.shimmer
import kotlin.time.Duration.Companion.milliseconds
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
    val checkClipboardForLinks by settingsRepository.checkClipboardForLinks.collectAsStateWithLifecycle(
        initialValue = false
    )
    val clipboardCalloutDismissed by settingsRepository.clipboardCalloutDismissed.collectAsStateWithLifecycle(
        initialValue = false
    )

    val entitlementManager = TidyApp.instance.entitlementManager
    val isPlusUnlocked by entitlementManager.isPlusUnlocked.collectAsStateWithLifecycle(initialValue = false)
    var showUpsellSheet by remember { mutableStateOf(showPlusUpsell) }
    var bulkClipboardUrls by remember { mutableStateOf<List<String>?>(null) }
    val trackerDescriptions by settingsRepository.trackerDescriptions.collectAsStateWithLifecycle(
        initialValue = emptyMap()
    )

    // Clipboard suggestion state, driven from the ON_RESUME observer below. The
    // suggestion has no auto-hide timeout: it stays until acted on or until the
    // clipboard content changes.
    var clipboardUrl by remember { mutableStateOf<String?>(null) }
    var suggestionCopiesOnClean by remember { mutableStateOf(false) }
    var paramToWhitelist by remember { mutableStateOf<String?>(null) }
    val introSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hasShownCrashSheetThisSession by rememberSaveable { mutableStateOf(false) }
    var showCrashSheet by remember { mutableStateOf(crashReportText != null && !dontAskAgainCrash && !hasShownCrashSheetThisSession) }
    val onDismissCrashReport = {
        showCrashSheet = false
        hasShownCrashSheetThisSession = true
    }

    // Clean URL if shared through Android intent. Guarded against state.originalUrl so
    // returning to this NavKey (e.g. navigating back from Settings/History disposes and
    // recomposes MainScreen, re-running this effect) doesn't re-clean the same shared URL
    // and double-count it in history/analytics.
    LaunchedEffect(sharedUrl) {
        if (sharedUrl != null && sharedUrl != state.originalUrl) {
            viewModel.cleanUrl(sharedUrl, isShared = true)
        }
    }

    // Debounced manual input auto-clean
    LaunchedEffect(state.inputUrl, state.autoCleanOnInput) {
        if (state.autoCleanOnInput && state.inputUrl.isNotEmpty()) {
            val trimmed = state.inputUrl.trim()
            if (UrlDetection.looksLikeUrl(trimmed) && trimmed != state.originalUrl && trimmed != state.expandedUrl) {
                delay(400.milliseconds)
                viewModel.cleanUrl(trimmed)
            }
        }
    }

    // Collect and handle automation events. The shared skip rule is applied once
    // here for every automation path: if the cleaned result is already exactly
    // what's on the clipboard, nothing happens at all.
    LaunchedEffect(viewModel.automationEvents) {
        viewModel.automationEvents.collect { event ->
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val cleanedUrl = when (event) {
                is MainScreenViewModel.AutomationAction.Copy -> event.cleanedUrl
                is MainScreenViewModel.AutomationAction.CopyAndShare -> event.cleanedUrl
                is MainScreenViewModel.AutomationAction.CopyAndClose -> event.cleanedUrl
            }
            val currentClip = try {
                clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
            } catch (e: Exception) {
                null
            }
            if (cleanedUrl == currentClip) return@collect

            val clip = android.content.ClipData.newPlainText("Cleaned URL", cleanedUrl)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(
                context,
                toastCleanedCopied,
                android.widget.Toast.LENGTH_SHORT
            ).show()
            when (event) {
                is MainScreenViewModel.AutomationAction.Copy -> Unit
                is MainScreenViewModel.AutomationAction.CopyAndShare -> {
                    // Deliberately a plain, predictable share sheet: if it lists Tidy
                    // itself and the user picks it again, that's expected.
                    viewModel.shareUrl(context)
                }
                is MainScreenViewModel.AutomationAction.CopyAndClose -> {
                    // The only automation allowed to close the app, and it can only be
                    // reached from a genuine incoming share intent (isShared = true).
                    activity?.finishAndRemoveTask()
                }
            }
        }
    }

    // The single clipboard-check path. Hooked into ON_RESUME (which also fires right
    // after cold start), so launching and resuming Tidy behave identically. This can
    // suggest, clean, or copy — it can never close the app.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    val clearSuggestions = {
                        clipboardUrl = null
                        bulkClipboardUrls = null
                    }
                    if (!settingsRepository.checkClipboardForLinks.first()) {
                        clearSuggestions()
                        return@launch
                    }
                    val text = try {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                    } catch (e: Exception) {
                        null
                    }
                    if (text == null) {
                        clearSuggestions()
                        return@launch
                    }
                    val urls = UrlDetection.findAllUrls(text)
                    when {
                        urls.size > 1 -> {
                            bulkClipboardUrls = urls
                            clipboardUrl = null
                        }
                        urls.size == 1 -> {
                            bulkClipboardUrls = null
                            val candidate = viewModel.evaluateClipboardCandidate(text)
                            if (candidate == null) {
                                clipboardUrl = null
                            } else {
                                when (FlavorConfig.resolveClipboardTier(settingsRepository)) {
                                    ClipboardCleanTier.AUTO_CLEAN -> {
                                        clipboardUrl = null
                                        viewModel.cleanUrl(
                                            candidate,
                                            copyResultToClipboard = true
                                        )
                                    }
                                    ClipboardCleanTier.SUGGEST_AND_COPY -> {
                                        suggestionCopiesOnClean = true
                                        clipboardUrl = candidate
                                    }
                                    ClipboardCleanTier.SUGGEST -> {
                                        suggestionCopiesOnClean = false
                                        clipboardUrl = candidate
                                    }
                                }
                            }
                        }
                        else -> clearSuggestions()
                    }
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
            // The one adaptive bottom action container. Its states: clipboard
            // suggestion, manual entry fallback, the copy/share row for a cleaned
            // result, and the stacked state (new suggestion above the copy/share row).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 22.dp, bottom = 18.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedVisibility(visible = clipboardUrl != null) {
                            clipboardUrl?.let { url ->
                                ClipboardActionBanner(
                                    url = url,
                                    onActionClick = {
                                        clipboardUrl = null
                                        viewModel.cleanUrl(
                                            url,
                                            copyResultToClipboard = suggestionCopiesOnClean
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (state.isCleaned) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
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

                                TooltipWrapper(tooltipText = stringResource(R.string.tooltip_clean_new)) {
                                    IconButton(
                                        onClick = { viewModel.clear() },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CleaningServices,
                                            contentDescription = stringResource(R.string.main_clean_new_url)
                                        )
                                    }
                                }
                            }
                        } else {
                            AnimatedVisibility(visible = !checkClipboardForLinks && !clipboardCalloutDismissed) {
                                ClipboardCalloutBanner(
                                    onEnable = {
                                        scope.launch {
                                            settingsRepository.setCheckClipboardForLinks(true)
                                        }
                                    },
                                    onDismiss = {
                                        scope.launch {
                                            settingsRepository.setClipboardCalloutDismissed(true)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            ManualEntryRow(
                                state = state,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        },
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
                                    HistoryRepository.extractDomain(state.originalUrl)
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
                                                        imageVector = Icons.Outlined.ArrowsOutward,
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

                        Spacer(modifier = Modifier.height(16.dp))
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
            CrashReportBottomSheet(
                crashReportText = crashReportText,
                onDismiss = { onDismissCrashReport() },
                showDontAskAgain = true,
                dontAskAgainChecked = dontAskAgainCrash,
                onDontAskAgainChange = { checked ->
                    scope.launch {
                        settingsRepository.setDontAskAgainCrash(checked)
                    }
                },
                showDeleteButton = false
            )
        }
    }

    if (paramToWhitelist != null) {
        val param = paramToWhitelist!!
        val domain = HistoryRepository.extractDomain(state.originalUrl)
        val description =
            trackerDescriptions[param] ?: stringResource(R.string.details_no_explanation)

        ParamDetailBottomSheet(
            param = param,
            description = description,
            domain = domain,
            onDismiss = { paramToWhitelist = null },
            onWhitelist = { d, p -> viewModel.addDomainWhitelistedParam(d, p) },
            onReportIssueFailed = {
                scope.launch {
                    snackbarHostState.showSnackbar(crashToastBrowserError)
                }
            }
        )
    }

    if (showUpsellSheet) {
        com.tidy.app.FlavorConfig.ShowUpsellBottomSheet(
            onDismiss = { showUpsellSheet = false }
        )
    }
}

/**
 * Manual link entry, the bottom container's fallback state when no clipboard
 * suggestion is showing and nothing has been cleaned yet.
 */
@Composable
private fun ManualEntryRow(
    state: MainScreenViewModel.UiState,
    viewModel: MainScreenViewModel
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
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
            modifier = Modifier.weight(1f),
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
                    if (UrlDetection.looksLikeUrl(state.inputUrl) && !state.isLoading) {
                        viewModel.cleanUrl(state.inputUrl)
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_process_url)) {
            Button(
                onClick = { viewModel.cleanUrl(state.inputUrl) },
                shape = RoundedCornerShape(16.dp),
                enabled = UrlDetection.looksLikeUrl(state.inputUrl) && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CleaningServices,
                        contentDescription = stringResource(R.string.button_clean_url),
                        modifier = Modifier.size(20.dp)
                    )
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


/**
 * Suggests turning on clipboard checking (off by default). Shown above the manual
 * entry fallback until the user enables it or dismisses the callout for good.
 */
@Composable
private fun ClipboardCalloutBanner(
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
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
                        text = stringResource(R.string.banner_clipboard_callout_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.banner_clipboard_callout_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.banner_clipboard_callout_dismiss),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = onEnable,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.banner_clipboard_callout_enable),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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


