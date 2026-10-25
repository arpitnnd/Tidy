package com.tidy.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle

// Receive shares in the sender's task, including launches for a result. Only the
// text crosses into Tidy's own task; sender-supplied task and result flags do not.
class ShareActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                val sharedText = try {
                    intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
                } catch (_: RuntimeException) {
                    null
                } ?: return
                startActivity(Intent(this, MainActivity::class.java).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, sharedText)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
            }
        } finally {
            finish()
        }
    }
}
