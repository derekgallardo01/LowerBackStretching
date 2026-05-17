package com.lowerbackstretching.core

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * A custom-scheme deep link that round-trips a custom routine across
 * devices: the recipient taps the link or scans the QR code, the OS
 * routes back to the app, and the importer creates a copy of the
 * routine locally.
 *
 * Format: `lowerbackstretching://routine?name=<urlenc>&ids=<csv>`
 *
 * The link is self-contained — no server is involved — so it works
 * offline and survives the original sender deleting their copy.
 */

const val ROUTINE_LINK_SCHEME = "lowerbackstretching"
const val ROUTINE_LINK_HOST = "routine"

data class SharedRoutine(val name: String, val stretchIds: List<String>)

fun buildRoutineLink(name: String, stretchIds: List<String>): String {
    val encName = URLEncoder.encode(name, Charsets.UTF_8.name())
    val ids = stretchIds.joinToString(",")
    val encIds = URLEncoder.encode(ids, Charsets.UTF_8.name())
    return "$ROUTINE_LINK_SCHEME://$ROUTINE_LINK_HOST?name=$encName&ids=$encIds"
}

/**
 * Parse [link] back into a [SharedRoutine]. Returns null if the link
 * isn't ours, is missing required params, or carries an empty id list.
 *
 * Uses java.net.URI so this is callable from JVM unit tests, not just
 * instrumented tests.
 */
fun parseRoutineLink(link: String): SharedRoutine? {
    val uri = runCatching { URI(link) }.getOrNull() ?: return null
    if (uri.scheme != ROUTINE_LINK_SCHEME || uri.host != ROUTINE_LINK_HOST) return null
    val params = parseQuery(uri.rawQuery ?: return null)
    val name = params["name"]?.let { decodeOrNull(it) } ?: return null
    val ids = params["ids"]?.let { decodeOrNull(it) }
        ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        ?: return null
    if (ids.isEmpty() || name.isBlank()) return null
    return SharedRoutine(name = name.trim(), stretchIds = ids)
}

private fun parseQuery(raw: String): Map<String, String> =
    raw.split("&").mapNotNull { pair ->
        val eq = pair.indexOf('=')
        if (eq <= 0) null else pair.substring(0, eq) to pair.substring(eq + 1)
    }.toMap()

private fun decodeOrNull(s: String): String? =
    runCatching { URLDecoder.decode(s, Charsets.UTF_8.name()) }.getOrNull()
