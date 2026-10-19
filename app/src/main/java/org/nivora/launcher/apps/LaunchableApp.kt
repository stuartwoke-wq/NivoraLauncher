package org.nivora.launcher.apps

import android.graphics.Bitmap

/** A launchable activity discovered through Android's public package APIs. */
data class LaunchableApp(
    val label: String,
    val packageName: String,
    val bitmap: Bitmap? = null
)

/** Pure filtering/sorting logic so app discovery behavior can be unit-tested. */
object AppCatalog {
    fun filterAndSort(apps: List<LaunchableApp>, query: String): List<LaunchableApp> {
        val normalized = query.trim()
        return apps.asSequence()
            .filter { normalized.isEmpty() || it.label.contains(normalized, ignoreCase = true) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            .toList()
    }
}
