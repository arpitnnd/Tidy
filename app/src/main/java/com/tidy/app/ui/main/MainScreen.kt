package com.tidy.app.ui.main

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tidy.app.FlavorConfig
import com.tidy.app.R
import com.tidy.app.TidyApp
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.UrlCleaner
import com.tidy.app.data.UrlDetection
import com.tidy.app.ui.components.ArrowsOutward
import com.tidy.app.ui.components.CrashReportBottomSheet
import com.tidy.app.ui.components.FeatureRow
import com.tidy.app.ui.components.ParamDetailBottomSheet
import com.tidy.app.ui.components.TidyModalBottomSheet
import com.tidy.app.ui.components.TooltipWrapper
import com.tidy.app.ui.components.shimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// Shared by every bottom-container banner (clipboard suggestion, callout, enabled-success)
// so their fade and size animations run on identical timing and never drift out of sync.
private const val BannerAnimDurationMs = 220
private val BannerEnterTransition = fadeIn(tween(BannerAnimDurationMs)) +
        expandVertically(animationSpec = tween(BannerAnimDurationMs))
private val BannerExitTransition = fadeOut(tween(BannerAnimDurationMs)) +
        shrinkVertically(animationSpec = tween(BannerAnimDurationMs))

// Matches the gap this screen already uses between other distinct stacked sections (the
// results Column below), rather than the tighter 12.dp used for spacing within one section.
private val BannerGap = 16.dp

private const val HomeMorphDurationMs = 480

private enum class HomeBottomStage {
    Input,
    Loading,
    Actions
}

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
    // rememberSaveable, not remember: showPlusUpsell is a constant prop for the Activity's
    // whole lifetime (read once from an intent extra), so a plain remember re-initialised
    // to it on every Settings/History round trip and every rotation, silently reopening a
    // sheet the user had just dismissed.
    var showUpsellSheet by rememberSaveable { mutableStateOf(showPlusUpsell) }
    val trackerDescriptions by settingsRepository.trackerDescriptions.collectAsStateWithLifecycle(
        initialValue = emptyMap()
    )

    var paramToWhitelist by remember { mutableStateOf<String?>(null) }
    val introSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // AnimatedVisibility keeps composing its content lambda while it animates out, so if
    // that lambda reads state.clipboardSuggestionUrl directly it goes blank the instant
    // that's nulled, leaving the exit animation to collapse an empty box. Caching the last
    // non-null value keeps the banner's content visible for the whole exit transition.
    var lastClipboardBannerUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.clipboardSuggestionUrl) {
        if (state.clipboardSuggestionUrl != null) {
            lastClipboardBannerUrl = state.clipboardSuggestionUrl
        }
    }

    // Shown after the user taps "Enable" on the clipboard callout, in place of it.
    var showClipboardEnabledSuccess by remember { mutableStateOf(false) }
    var hasCompletedInitialClipboardCheck by remember { mutableStateOf(false) }

    var hasShownCrashSheetThisSession by rememberSaveable { mutableStateOf(false) }
    // Derived fresh every recomposition rather than captured once in a remember block:
    // dontAskAgainCrash's real DataStore value arrives asynchronously after the first
    // composition (collectAsStateWithLifecycle's initialValue is false), so a one-shot
    // remember could decide to show the sheet before the stored "don't ask again"
    // preference had actually loaded, and never reconsider that decision afterwards.
    val showCrashSheet =
        crashReportText != null && !dontAskAgainCrash && !hasShownCrashSheetThisSession
    val onDismissCrashReport = {
        hasShownCrashSheetThisSession = true
    }

    // Clean URL if shared through Android intent. hasHandledThisSharedUrl is rememberSaveable
    // (keyed on sharedUrl) so it survives Settings/History disposing and recomposing this
    // NavKey entry (without re-cleaning the same shared URL every round-trip) while a
    // genuinely new share still gets its own fresh Main(url) entry and its own fresh flag.
    var hasHandledThisSharedUrl by rememberSaveable(sharedUrl) { mutableStateOf(false) }
    LaunchedEffect(sharedUrl) {
        if (sharedUrl != null && !hasHandledThisSharedUrl) {
            hasHandledThisSharedUrl = true
            viewModel.cleanUrl(sharedUrl, isShared = true)
        }
    }

    // Debounced manual input auto-clean
    LaunchedEffect(state.inputUrl, state.autoCleanOnInput) {
        if (state.autoCleanOnInput && state.inputUrl.isNotEmpty()) {
            val trimmed = state.inputUrl.trim()
            if (UrlDetection.looksLikeUrl(trimmed) && trimmed != state.originalUrl && trimmed != state.cleanedUrl && trimmed != state.expandedUrl) {
                delay(400.milliseconds)
                if (trimmed != state.originalUrl && trimmed != state.cleanedUrl && trimmed != state.expandedUrl) {
                    viewModel.cleanUrl(trimmed)
                }
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
            val sourceClipText = when (event) {
                is MainScreenViewModel.AutomationAction.Copy -> event.sourceClipText
                is MainScreenViewModel.AutomationAction.CopyAndShare -> event.sourceClipText
                is MainScreenViewModel.AutomationAction.CopyAndClose -> event.sourceClipText
            }
            val currentClip = try {
                clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
            } catch (e: Exception) {
                null
            }

            // When this came from an automatic clean of clipboard text that had more than
            // just the URL (e.g. "here's the file <link> enjoy"), splice the cleaned URL
            // back into that text instead of overwriting the whole clipboard and losing
            // the rest of it.
            val newClipText = sourceClipText?.let { source ->
                UrlDetection.findFirstUrl(source)?.let { detected ->
                    UrlDetection.spliceUrl(source, detected, cleanedUrl)
                }
            } ?: cleanedUrl

            // Only the clipboard write and its toast are skipped when there's nothing to
            // change: CopyAndShare/CopyAndClose must always run their own action below
            // regardless of what happens to already be on the clipboard, or identical
            // input would silently produce different outcomes depending on invisible
            // clipboard state.
            if (newClipText != currentClip) {
                val clip = android.content.ClipData.newPlainText("Cleaned URL", newClipText)
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(
                    context,
                    toastCleanedCopied,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
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

    // Reads the clipboard and hands it to the ViewModel, which owns the suggest/auto-clean
    // decision (see MainScreenViewModel.checkClipboardText's KDoc). Reading the clipboard
    // itself stays here because it needs a Context.
    val checkClipboard: suspend () -> Unit = {
        val text = try {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        } catch (_: Exception) {
            null
        }
        viewModel.checkClipboardText(text)
        hasCompletedInitialClipboardCheck = true
    }

    // Hooked into ON_RESUME, which fires right after cold start and again every time this
    // screen is recomposed after a Settings/History round trip: NavDisplay gives each
    // back-stack entry its own child LifecycleOwner that starts INITIALIZED and is driven
    // to RESUMED fresh on every such round trip, so there is no reliable signal here that
    // distinguishes "just returned" from "genuinely resumed"; both call this the same way.
    // That's deliberate: checkClipboardText's own dedup (comparing against what's already
    // displayed) is what keeps this predictable, not a guess at the lifecycle event.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    checkClipboard()
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

    val scrollThresholdPx = with(LocalDensity.current) { 150.dp.toPx() }
    var scrollOffset by rememberSaveable { mutableFloatStateOf(0f) }
    var showUpperLoading by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            delay(32)
            showUpperLoading = state.isLoading
        } else {
            if (showUpperLoading) {
                delay(180)
            }
            showUpperLoading = false
        }
    }
    var lastResultWasCleaned by remember { mutableStateOf(state.isCleaned) }
    SideEffect {
        if (state.isCleaned) {
            lastResultWasCleaned = true
        } else if (state.isLoading) {
            lastResultWasCleaned = false
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (state.isCleaned || state.isLoading) return Offset.Zero
                val delta = available.y
                if (delta < 0 && scrollOffset < scrollThresholdPx) {
                    val newOffset = (scrollOffset - delta).coerceIn(0f, scrollThresholdPx)
                    val consumed = scrollOffset - newOffset
                    scrollOffset = newOffset
                    return Offset(0f, consumed)
                }
                if (delta > 0 && scrollOffset > 0f) {
                    val newOffset = (scrollOffset - delta).coerceIn(0f, scrollThresholdPx)
                    val consumed = scrollOffset - newOffset
                    scrollOffset = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (state.isCleaned || state.isLoading) return Velocity.Zero
                if (available.y < -300f || scrollOffset > scrollThresholdPx / 2) {
                    animate(scrollOffset, scrollThresholdPx) { value, _ -> scrollOffset = value }
                } else {
                    animate(scrollOffset, 0f) { value, _ -> scrollOffset = value }
                }
                return Velocity.Zero
            }
        }
    }

    val targetCollapseFraction by remember {
        derivedStateOf {
            if (state.isCleaned || showUpperLoading) {
                1f
            } else {
                (scrollOffset / scrollThresholdPx).coerceIn(0f, 1f)
            }
        }
    }
    val collapseFraction by animateFloatAsState(
        targetValue = targetCollapseFraction,
        animationSpec = tween(
            durationMillis = 320,
            easing = FastOutSlowInEasing
        ),
        label = "collapseFraction"
    )

    val bottomStage = when {
        state.isCleaned -> HomeBottomStage.Actions
        state.isLoading -> HomeBottomStage.Loading
        else -> HomeBottomStage.Input
    }
    val displayedBottomStage = if (bottomStage == HomeBottomStage.Loading) {
        HomeBottomStage.Input
    } else {
        bottomStage
    }
    var retainedInputState by remember { mutableStateOf(state) }
    SideEffect {
        if (bottomStage == HomeBottomStage.Input) {
            retainedInputState = state
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
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
            // One persistent physical container. Only its contents and top edge move as
            // the UI progresses through input, cleaning, and result actions.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = displayedBottomStage to state.clipboardSuggestionUrl,
                        transitionSpec = {
                            (fadeIn(
                                animationSpec = tween(
                                    durationMillis = 220,
                                    delayMillis = 80,
                                    easing = FastOutSlowInEasing
                                )
                            ) togetherWith fadeOut(
                                animationSpec = tween(
                                    durationMillis = 180,
                                    easing = FastOutSlowInEasing
                                )
                            )).using(
                                SizeTransform(
                                    clip = true,
                                    sizeAnimationSpec = { _, _ ->
                                        tween(
                                            durationMillis = 400,
                                            easing = FastOutSlowInEasing
                                        )
                                    }
                                )
                            )
                        },
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp)
                            .padding(top = 22.dp, bottom = 18.dp)
                            .fillMaxWidth(),
                        label = "bottomBarContent"
                    ) { (stage, suggestionUrl) ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            suggestionUrl?.let { url ->
                                Column {
                                    ClipboardActionBanner(
                                        url = url,
                                        onActionClick = {
                                            viewModel.cleanUrl(
                                                url,
                                                copyResultToClipboard = state.suggestionCopiesOnClean
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(BannerGap))
                                }
                            }

                            if (stage == HomeBottomStage.Actions) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TooltipWrapper(tooltipText = stringResource(R.string.tooltip_clean_new)) {
                                        FilledIconButton(
                                            onClick = {
                                                scrollOffset = 0f
                                                viewModel.clear()
                                                scope.launch {
                                                    checkClipboard()
                                                }
                                            },
                                            modifier = Modifier.size(48.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Clear,
                                                contentDescription = stringResource(R.string.main_clean_new_url)
                                            )
                                        }
                                    }

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
                            } else if (stage == HomeBottomStage.Input) {
                                AnimatedVisibility(
                                    visible = hasCompletedInitialClipboardCheck &&
                                            state.clipboardSuggestionUrl == null &&
                                            !checkClipboardForLinks &&
                                            !clipboardCalloutDismissed &&
                                            !showClipboardEnabledSuccess,
                                    enter = BannerEnterTransition,
                                    exit = BannerExitTransition
                                ) {
                                    Column {
                                        ClipboardCalloutBanner(
                                            onEnable = {
                                                scope.launch {
                                                    settingsRepository.setCheckClipboardForLinks(
                                                        true
                                                    )
                                                }
                                                showClipboardEnabledSuccess = true
                                            },
                                            onDismiss = {
                                                scope.launch {
                                                    settingsRepository.setClipboardCalloutDismissed(
                                                        true
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(BannerGap))
                                    }
                                }

                                AnimatedVisibility(
                                    visible = showClipboardEnabledSuccess,
                                    enter = BannerEnterTransition,
                                    exit = BannerExitTransition
                                ) {
                                    Column {
                                        ClipboardEnabledSuccessBanner(
                                            onViewSettings = {
                                                showClipboardEnabledSuccess = false
                                                onSettingsClick()
                                            },
                                            onLater = { showClipboardEnabledSuccess = false },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(BannerGap))
                                    }
                                }
                                ManualEntryRow(
                                    state = if (
                                        bottomStage != HomeBottomStage.Input &&
                                        suggestionUrl != null
                                    ) {
                                        retainedInputState
                                    } else {
                                        state
                                    },
                                    viewModel = viewModel,
                                    onClear = {
                                        scrollOffset = 0f
                                        viewModel.clear()
                                        scope.launch {
                                            checkClipboard()
                                        }
                                    }
                                )
                            }
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
            var titleWidthPx by remember { mutableIntStateOf(0) }
            var titleHeightPx by remember { mutableIntStateOf(0) }

            // --- THE PHYSICAL SHARED BRAND ELEMENT (LOGO + TITLE) ---
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val screenWidthPx = constraints.maxWidth.toFloat()
                val density = LocalDensity.current

                // Logo geometry:
                // Expanded: 72dp container with 40dp icon inside, centered at (screenWidthPx / 2, 60dp)
                // Collapsed: Container background alpha goes to 0; 40dp icon scales to 30dp crisp render
                // Docked position: Left edge of 30dp icon at 0f (16dp from screen edge), vertically centered in 64dp TopAppBar (Y = -32dp).
                val expandedLogoSizePx = with(density) { 72.dp.toPx() }
                val expandedIconSizePx = with(density) { 40.dp.toPx() }
                val collapsedIconTargetPx = with(density) { 30.dp.toPx() }

                val startLogoXPx = (screenWidthPx - expandedLogoSizePx) / 2f
                val startLogoYPx = with(density) { 24.dp.toPx() }

                val logoScale =
                    1f - (1f - collapsedIconTargetPx / expandedIconSizePx) * collapseFraction
                val logoBgAlpha = (0.4f * (1f - collapseFraction * 2.5f)).coerceAtLeast(0f)

                val iconPaddingPx = (expandedLogoSizePx - expandedIconSizePx) / 2f
                val endLogoXPx = -iconPaddingPx * (collapsedIconTargetPx / expandedIconSizePx)
                val endLogoYPx =
                    with(density) { (-32).dp.toPx() } - (iconPaddingPx + expandedIconSizePx / 2f) * (collapsedIconTargetPx / expandedIconSizePx)

                // Title geometry:
                // Expanded: HeadlineMedium (~28sp) mathematically centered horizontally across all display sizes
                // Collapsed: TitleLarge / TitleMedium (~20sp, scale = 20 / 28 = ~0.714f), docked right next to 30dp icon (endTitleXPx = 40dp)
                // Vertical alignment: Scaled text vertical center is aligned with Y = -32dp in the 64dp TopAppBar to match the Logo and action icons perfectly.
                val startTitleXPx = if (titleWidthPx > 0) {
                    (screenWidthPx - titleWidthPx) / 2f
                } else {
                    (screenWidthPx - with(density) { 80.dp.toPx() }) / 2f
                }
                val startTitleYPx = with(density) { 108.dp.toPx() }

                val titleScale = 1f - (1f - 20f / 28f) * collapseFraction
                val endTitleXPx = with(density) { 40.dp.toPx() }
                val titleHalfHeightPx =
                    if (titleHeightPx > 0) titleHeightPx / 2f else with(density) { 18.dp.toPx() }
                val endTitleYPx = with(density) { (-32).dp.toPx() } - titleHalfHeightPx

                // 1. Physical Shared Logo (GPU-only translation & scale)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            translationX =
                                startLogoXPx + (endLogoXPx - startLogoXPx) * collapseFraction
                            translationY =
                                startLogoYPx + (endLogoYPx - startLogoYPx) * collapseFraction
                            scaleX = logoScale
                            scaleY = logoScale
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = logoBgAlpha)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CleaningServices,
                        contentDescription = stringResource(R.string.app_name),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // 2. Physical Shared Title ("Tidy") (GPU-only translation & scale)
                Text(
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            titleWidthPx = coordinates.size.width
                            titleHeightPx = coordinates.size.height
                        }
                        .graphicsLayer {
                            translationX =
                                startTitleXPx + (endTitleXPx - startTitleXPx) * collapseFraction
                            translationY =
                                startTitleYPx + (endTitleYPx - startTitleYPx) * collapseFraction
                            scaleX = titleScale
                            scaleY = titleScale
                            transformOrigin = TransformOrigin(0f, 0.5f)
                        }
                )
            }

            AnimatedContent(
                targetState = state.isCleaned || showUpperLoading,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(HomeMorphDurationMs, delayMillis = 80)) +
                                slideInVertically(tween(HomeMorphDurationMs)) { it * 3 }) togetherWith
                                (fadeOut(tween(220)) +
                                        slideOutVertically(tween(HomeMorphDurationMs)) { -it / 3 })
                    } else {
                        fadeIn(tween(200, delayMillis = 30)) togetherWith
                                fadeOut(tween(140))
                    }
                },
                label = "homeContentMorph"
            ) { showResult ->
                if (showResult) {
                    // Unified Results Card Container (Morphs smoothly between Loading and Cleaned Result)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 650.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val showCleanedContent = !showUpperLoading &&
                                (state.isCleaned || (!state.isLoading && lastResultWasCleaned))
                        val resultReveal by animateFloatAsState(
                            targetValue = if (showCleanedContent) 1f else 0f,
                            animationSpec = tween(360),
                            label = "resultSurfaceReveal"
                        )
                        val loadingContainerColor =
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        val gradientBrush = Brush.linearGradient(
                            colors = listOf(
                                lerpColor(
                                    loadingContainerColor,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    resultReveal
                                ),
                                lerpColor(
                                    loadingContainerColor,
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                    resultReveal
                                )
                            )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(gradientBrush)
                                .border(
                                    width = 1.dp,
                                    color = lerpColor(
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        resultReveal
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (!showCleanedContent) {
                                    // Loading Content inside the Unified Card
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = stringResource(R.string.main_cleaning_url),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (state.inputUrl.isNotBlank()) {
                                            Text(
                                                text = state.inputUrl,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                if (showCleanedContent) {
                                    // Cleaned Result Content inside the Unified Card
                                    var showDetails by remember { mutableStateOf(false) }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 20.dp,
                                                end = 20.dp,
                                                top = 20.dp,
                                                bottom = 12.dp
                                            ),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
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
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        viewModel.copyToClipboard(context)
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                toastCopied
                                                            )
                                                        }
                                                    }
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
                                                                contentDescription = stringResource(
                                                                    R.string.main_expand_url
                                                                )
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
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.12f
                                        ),
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

                                    AnimatedVisibility(
                                        visible = showDetails,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 20.dp,
                                                    end = 20.dp,
                                                    bottom = 20.dp
                                                )
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
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(
                                                        6.dp
                                                    )
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.main_removed_parameters),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                            alpha = 0.6f
                                                        ),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    FlowRow(
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        ),
                                                        verticalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        state.removedParams.forEach { param ->
                                                            AssistChip(
                                                                onClick = {
                                                                    paramToWhitelist = param
                                                                },
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
                    }
                } else if (!showIntro) {
                    // DECLUTTERED Welcome State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 650.dp)
                            .nestedScroll(nestedScrollConnection)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = (1f - collapseFraction * 2.5f).coerceIn(0f, 1f)
                                }
                        ) {
                            // Reserved space for the floating shared Logo (72dp) + Title (30sp) + Spacers
                            Spacer(modifier = Modifier.height(154.dp))

                            Text(
                                text = stringResource(R.string.welcome_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // --- Group 2: Trust Markers ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
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

                            Spacer(modifier = Modifier.height(20.dp))

                            // --- Group 3: Cleaning Stats Counter ---
                            when {
                                state.isInitialLoading -> {
                                    Box(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .shimmer()
                                    )
                                }

                                state.totalCleanedCount > 0 -> {
                                    Surface(
                                        onClick = onHistoryClick,
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.4f
                                        )
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
                        }

                        // Bulk Clipboard Clean card (gated behind Tidy+)
                        com.tidy.app.FlavorConfig.BulkClipboardCleanCard(
                            bulkClipboardUrls = state.bulkClipboardUrls,
                            onDismiss = { viewModel.clearClipboardSuggestion() },
                            isPlusUnlocked = isPlusUnlocked,
                            onShowUpsell = { showUpsellSheet = true },
                            onCleanExecute = {
                                state.bulkClipboardUrls?.let { urls ->
                                    scope.launch {
                                        val settings = TidyApp.instance.settingsRepository
                                        val whitelist = settings.whitelistedDomains.first()
                                        val customBlacklist = settings.blacklistedParams.first()
                                        val domainParams = settings.domainWhitelistedParams.first()
                                        val removeMobile =
                                            settings.autoRemoveMobileSubdomains.first()
                                        val dropTrailingSlash =
                                            settings.dropTrailingSlash.first()
                                        val trackers = settings.trackers.first()

                                        val cleaned = urls.map { url ->
                                            UrlCleaner().clean(
                                                urlStr = url,
                                                whitelistedDomains = whitelist,
                                                customBlacklistParams = customBlacklist,
                                                domainWhitelistedParams = domainParams,
                                                removeMobileSubdomains = removeMobile,
                                                dropTrailingSlash = dropTrailingSlash,
                                                trackers = trackers
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
                                        viewModel.clearClipboardSuggestion()
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(200.dp))
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
        if (showCrashSheet) {
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
    viewModel: MainScreenViewModel,
    onClear: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = state.inputUrl,
            onValueChange = { viewModel.onUrlInput(it) },
            enabled = !state.isLoading,
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
                        IconButton(
                            onClick = onClear,
                            enabled = !state.isLoading
                        ) {
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
                Icon(
                    imageVector = Icons.Outlined.CleaningServices,
                    contentDescription = stringResource(R.string.button_clean_url),
                    modifier = Modifier.size(20.dp)
                )
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

/**
 * Confirms clipboard checking was enabled, in place of the callout that requested it.
 * Points the user at Settings to customise the tier behaviour, or lets them dismiss it.
 */
@Composable
private fun ClipboardEnabledSuccessBanner(
    onViewSettings: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
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
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.banner_clipboard_enabled_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.banner_clipboard_enabled_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
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
                TextButton(onClick = onLater) {
                    Text(
                        text = stringResource(R.string.banner_later),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = onViewSettings,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.banner_view_settings),
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


