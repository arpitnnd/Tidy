package com.tidy.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.tidy.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashReportBottomSheet(
    crashReportText: String,
    onDismiss: () -> Unit,
    showDontAskAgain: Boolean = false,
    dontAskAgainChecked: Boolean = false,
    onDontAskAgainChange: ((Boolean) -> Unit)? = null,
    showDeleteButton: Boolean = false,
    onDeleteClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val crashToastCopied = stringResource(R.string.crash_toast_copied)
    val crashToastBrowserError = stringResource(R.string.crash_toast_browser_error)
    val toastCouldNotShare = stringResource(R.string.toast_could_not_share)

    // Use the standard Tidy sheet — skipPartiallyExpanded = true so it snaps fully open
    TidyModalBottomSheet(onDismissRequest = onDismiss) { scrollFix ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(scrollFix)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Scrollable monospace crash log preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp)
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                Text(
                    text = crashReportText,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // "Don't ask again" toggle — only shown on the home prompt
            if (showDontAskAgain && onDontAskAgainChange != null) {
                var dontAskChecked by remember { mutableStateOf(dontAskAgainChecked) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            dontAskChecked = !dontAskChecked
                            onDontAskAgainChange(dontAskChecked)
                        }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = dontAskChecked,
                        onCheckedChange = {
                            dontAskChecked = it
                            onDontAskAgainChange(it)
                        }
                    )
                    Text(
                        text = stringResource(R.string.crash_dont_ask_again),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Primary action: Report on GitHub (full width)
            TooltipWrapper(tooltipText = stringResource(R.string.crash_report_github)) {
                Button(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Crash Report", crashReportText)
                        )
                        Toast.makeText(context, crashToastCopied, Toast.LENGTH_SHORT).show()
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/arpitnnd/Tidy/issues/new?title=Crash%20Report&body=Paste%20copied%20crash%20log%20here".toUri()
                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, crashToastBrowserError, Toast.LENGTH_SHORT)
                                .show()
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.crash_report_github), fontWeight = FontWeight.Bold)
                }
            }

            // Secondary row: Share + optional Delete
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // weight() is applied to a plain Box rather than directly to TooltipWrapper's
                // TooltipBox: weighting two sibling TooltipBoxes in the same Row breaks Row's
                // weight distribution (one sibling silently collapses to near-zero width).
                Box(modifier = Modifier.weight(1f)) {
                    TooltipWrapper(tooltipText = stringResource(R.string.dialog_share)) {
                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, crashReportText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, toastCouldNotShare, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                stringResource(R.string.dialog_share),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (showDeleteButton && onDeleteClick != null) {
                    Box(modifier = Modifier.weight(1f)) {
                        TooltipWrapper(tooltipText = stringResource(R.string.dialog_delete)) {
                            OutlinedButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(
                                    stringResource(R.string.dialog_delete),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Dismiss
            TooltipWrapper(tooltipText = stringResource(R.string.dialog_cancel)) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        }
    }
}
