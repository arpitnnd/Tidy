package com.tidy.app.ui.about

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.tidy.app.BuildConfig
import com.tidy.app.R
import com.tidy.app.ui.components.AppIconBox
import com.tidy.app.ui.components.CardSectionLabel
import com.tidy.app.ui.components.ClickableLinkRow
import com.tidy.app.ui.components.CrashReportBottomSheet
import com.tidy.app.ui.components.SettingCard
import com.tidy.app.ui.components.TidyTopAppBar
import com.tidy.app.ui.components.TooltipWrapper
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Pre-resolve string resources to prevent querying resources in lambda callbacks (fixing LocalContextGetResourceValueCall lint error)
    val toastNoLinkApp = stringResource(R.string.toast_no_link_app)
    val crashToastCopied = stringResource(R.string.crash_toast_copied)
    val crashToastBrowserError = stringResource(R.string.crash_toast_browser_error)
    val toastCouldNotShare = stringResource(R.string.toast_could_not_share)
    val toastCrashDeleted = stringResource(R.string.toast_crash_deleted)

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

    var showCrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TidyTopAppBar(
                title = stringResource(R.string.about_title),
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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Logo Block
                AppIconBox(
                    icon = Icons.Outlined.CleaningServices,
                    boxSize = 64.dp,
                    iconSize = 32.dp,
                    cornerRadius = 16.dp
                )

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(
                        R.string.about_version_format,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                        BuildConfig.FLAVOR.uppercase()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.about_values_statement),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Creator Card
                SettingCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            CardSectionLabel(text = stringResource(R.string.about_built_by))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.about_creator_name),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Licensing Card
                SettingCard {
                    Column(modifier = Modifier.padding(12.dp)) {
                        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_open)) {
                            ClickableLinkRow(
                                label = stringResource(R.string.about_license_desc),
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            "https://github.com/arpitnnd/Tidy/blob/main/LICENSE".toUri()
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch { snackbarHostState.showSnackbar(toastNoLinkApp) }
                                    }
                                }
                            )
                        }
                    }
                }

                // Links Card
                SettingCard {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CardSectionLabel(
                            text = stringResource(R.string.about_links),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_open)) {
                            ClickableLinkRow(
                                label = stringResource(R.string.about_link_github),
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            "https://github.com/arpitnnd/Tidy".toUri()
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch { snackbarHostState.showSnackbar(toastNoLinkApp) }
                                    }
                                }
                            )
                        }

                        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_open)) {
                            ClickableLinkRow(
                                label = stringResource(R.string.about_link_issues),
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            "https://github.com/arpitnnd/Tidy/issues".toUri()
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch { snackbarHostState.showSnackbar(toastNoLinkApp) }
                                    }
                                }
                            )
                        }

                        TooltipWrapper(tooltipText = stringResource(R.string.tooltip_open)) {
                            ClickableLinkRow(
                                label = stringResource(R.string.about_link_releases),
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            "https://github.com/arpitnnd/Tidy/releases".toUri()
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch { snackbarHostState.showSnackbar(toastNoLinkApp) }
                                    }
                                }
                            )
                        }
                    }
                }

                // Diagnostics Card (Only displayed when there is a crash report)
                if (currentCrashReportText != null) {
                    SettingCard {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TooltipWrapper(tooltipText = stringResource(R.string.tooltip_view_crash_log)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showCrashDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
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
                                        contentDescription = stringResource(R.string.tooltip_view_crash_log),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showCrashDialog && currentCrashReportText != null) {
        CrashReportBottomSheet(
            crashReportText = currentCrashReportText!!,
            onDismiss = { showCrashDialog = false },
            showDontAskAgain = false,
            showDeleteButton = true,
            onDeleteClick = {
                try {
                    val dir = java.io.File(context.filesDir, "crash_reports")
                    if (dir.exists()) {
                        dir.deleteRecursively()
                    }
                    currentCrashReportText = null
                    showCrashDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar(toastCrashDeleted)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }
}
