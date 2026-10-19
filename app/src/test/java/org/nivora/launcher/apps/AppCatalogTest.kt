package org.nivora.launcher.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class AppCatalogTest {
    private val apps = listOf(
        LaunchableApp(label = "YouTube", packageName = "video.app"),
        LaunchableApp(label = "Calendar", packageName = "calendar.app"),
        LaunchableApp(label = "Camera", packageName = "camera.app"),
        LaunchableApp(label = "Settings", packageName = "settings.app")
    )

    @Test fun blankQueryReturnsAllAppsAlphabetically() {
        assertEquals(
            listOf("Calendar", "Camera", "Settings", "YouTube"),
            AppCatalog.filterAndSort(apps, "").map { it.label }
        )
    }

    @Test fun filteringIsCaseInsensitive() {
        assertEquals(
            listOf("Calendar", "Camera"),
            AppCatalog.filterAndSort(apps, "ca").map { it.label }
        )
    }

    @Test fun queryWhitespaceIsIgnored() {
        assertEquals(
            listOf("Settings"),
            AppCatalog.filterAndSort(apps, "  settings  ").map { it.label }
        )
    }

    @Test fun noMatchesReturnsEmptyList() {
        assertEquals(emptyList<LaunchableApp>(), AppCatalog.filterAndSort(apps, "not-installed"))
    }
}
