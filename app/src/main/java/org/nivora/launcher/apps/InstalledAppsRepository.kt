package org.nivora.launcher.apps

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {
    suspend fun loadLaunchableApps(): List<LaunchableApp> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        val iconSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            48f,
            context.resources.displayMetrics
        ).toInt().coerceAtLeast(1)

        activities.mapNotNull { resolveInfo ->
            runCatching {
                val activityInfo = resolveInfo.activityInfo ?: return@runCatching null
                val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                if (label.isBlank()) return@runCatching null
                val drawable = resolveInfo.loadIcon(packageManager)
                LaunchableApp(
                    label = label,
                    packageName = activityInfo.packageName,
                    bitmap = drawable.toBitmap(iconSize)
                )
            }.getOrNull()
        }.distinctBy { it.packageName + "/" + it.label }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    private fun Drawable.toBitmap(size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val oldBounds = bounds
        setBounds(0, 0, size, size)
        draw(canvas)
        bounds = oldBounds
        return bitmap
    }
}
