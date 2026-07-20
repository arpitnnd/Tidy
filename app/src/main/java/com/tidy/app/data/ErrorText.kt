package com.tidy.app.data

/**
 * Bounds an exception message before it's interpolated into a Snackbar. Most exception
 * messages are short, but nothing guarantees that, and an unbounded one could stretch a
 * Snackbar well past the usual one-to-two-line budget. 80 characters is a deliberately
 * generous allowance (rather than a strict single-line cut) since error detail here is
 * meaningful enough to deserve the extra room.
 */
fun errorDetail(message: String?, maxLength: Int = 80): String {
    val text = message ?: return ""
    return if (text.length > maxLength) text.take(maxLength) + "…" else text
}
