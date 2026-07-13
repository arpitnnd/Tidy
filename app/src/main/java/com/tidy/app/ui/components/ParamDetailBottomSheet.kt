package com.tidy.app.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.tidy.app.R

/**
 * Shared removed-parameter detail sheet used by both the Home screen (on the just-cleaned URL)
 * and the History screen (on a past entry) so both present the same information and offer the
 * same "Report Issue" / "Always Keep" actions instead of diverging implementations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamDetailBottomSheet(
    param: String,
    description: String,
    domain: String,
    onDismiss: () -> Unit,
    onWhitelist: (domain: String, param: String) -> Unit,
    onReportIssueFailed: () -> Unit
) {
    val context = LocalContext.current

    TidyModalBottomSheet(onDismissRequest = onDismiss) { scrollFix ->
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
                            onReportIssueFailed()
                        }
                        onDismiss()
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
                        onWhitelist(domain, param)
                        onDismiss()
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

fun launchGitHubBugReport(
    context: Context,
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
