package org.nivora.launcher
import kotlinx.coroutines.launch
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.nivora.launcher.apps.AppCatalog
import org.nivora.launcher.apps.InstalledAppsRepository
import org.nivora.launcher.apps.LaunchableApp
import org.nivora.launcher.ui.NivoraTheme

private val Midnight = Color(0xFF10141D)
private val SurfaceGlass = Color(0xB82A3342)
private val Glacier = Color(0xFF66D9E8)
private val Aurora = Color(0xFFC8B7FF)
private val SecondaryText = Color(0xFFB8C1CF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        enableEdgeToEdge()
        setContent {
            NivoraTheme {
                NivoraApp()
            }
        }
    }
}

@Composable
private fun NivoraApp() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<LaunchableApp>>(emptyList()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }

    suspend fun refreshApps() {
        val showInitialLoading = apps.isEmpty()
        if (showInitialLoading) {
            loading = true
        }
        loadError = false

        try {
            runCatching {
                InstalledAppsRepository(context.applicationContext).loadLaunchableApps()
            }.onSuccess {
                apps = it
            }.onFailure {
                loadError = true
            }
        } finally {
            loading = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    refreshApps()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val filteredApps = remember(apps, searchQuery) { AppCatalog.filterAndSort(apps, searchQuery) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        if (showSettings) {
            SettingsScreen(onBack = { showSettings = false })
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Header(onSettings = { showSettings = true })
                SearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = { searchQuery = "" },
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "YOUR APPS" else "SEARCH RESULTS",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    Text("${filteredApps.size}", color = Glacier, fontSize = 12.sp)
                }

                when {
                    loading && apps.isEmpty() -> EmptyMessage("Preparing your space…", Modifier.weight(1f))
                    loadError && apps.isEmpty() -> EmptyMessage("Apps could not be loaded. Reopen Nivora to try again.", Modifier.weight(1f))
                    filteredApps.isEmpty() -> EmptyMessage(
                        if (searchQuery.isBlank()) "No launchable apps were found." else "No apps match ‘$searchQuery’.",
                        Modifier.weight(1f)
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 76.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredApps, key = { it.packageName + "/" + it.label }) { app ->
                            AppIcon(app = app, onClick = {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                if (launchIntent != null) {
                                    runCatching { context.startActivity(launchIntent) }
                                }
                            })
                        }
                    }
                }

                Dock(
                    apps = apps.take(4),
                    onAppClick = { app ->
                        context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { intent ->
                            runCatching { context.startActivity(intent) }
                        }
                    },
                    onOpenApps = { searchQuery = "" }
                )
            }
        }
    }
}

@Composable
private fun Header(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 18.dp, top = 18.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFBDF7FF), Glacier, Aurora)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = Midnight,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "Nivora",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Your space. Your rules.",
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x55FFFFFF), Color(0x222A3342))
                    )
                )
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(SurfaceGlass)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search apps", tint = Glacier, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(11.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) Text("Search your apps", color = SecondaryText, fontSize = 14.sp)
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = SecondaryText, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun AppIcon(app: LaunchableApp, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (app.bitmap != null) {
            Image(
                bitmap = app.bitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceGlass), contentAlignment = Alignment.Center) {
                Text(app.label.take(1).uppercase(), color = Glacier, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = app.label,
            color = Color.White,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp)
        )
    }
}

@Composable
private fun Dock(
    apps: List<LaunchableApp>,
    onAppClick: (LaunchableApp) -> Unit,
    onOpenApps: () -> Unit
) {
    val dockShape = RoundedCornerShape(30.dp)

    // Outer rim: a subtle glass highlight around the dock.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 12.dp)
            .clip(dockShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x80FFFFFF),
                        Color(0x2266D9E8),
                        Color(0x33101725)
                    )
                )
            )
            .padding(1.dp)
    ) {
        // Inner surface: layered translucency without expensive live blur.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(29.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x665C6A7C),
                            Color(0x55343F50),
                            Color(0x66303B4B)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                apps.forEach { app ->
                    if (app.bitmap != null) {
                        Image(
                            bitmap = app.bitmap.asImageBitmap(),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .clickable { onAppClick(app) }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xAA66778D),
                                    Color(0x99354357)
                                )
                            )
                        )
                        .clickable(onClick = onOpenApps),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = "All apps",
                        tint = Color.White
                    )
                }
            }

            // Fine top-edge reflection.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x66FFFFFF))
            )
        }
    }
}

@Composable
private fun EmptyMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
        Text(message, color = SecondaryText, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 18.dp, bottom = 20.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.width(6.dp))
            Text("Settings", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("PERSONALIZATION", color = Glacier, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
        SettingsRow("Appearance", "Nivora midnight · Clear Glass", "A")
        SettingsRow("Home screen", "Workspace and app layout", "H")
        SettingsRow("Search & app library", "Local app discovery", "S")
        Spacer(Modifier.height(20.dp))
        Text("PRIVACY", color = Glacier, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
        SettingsRow("Offline by default", "Core features need no account or network", "✓")
        SettingsRow("App visibility", "Only query launchable activities", "✓")
        Spacer(Modifier.height(18.dp))
        Surface(
            color = SurfaceGlass,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().clickable {
                runCatching { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
            }
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Set as default launcher", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Choose Nivora in Android's Home app settings.", color = SecondaryText, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Nivora 0.1.0 · Foundation build", color = SecondaryText, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, symbol: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(16.dp)).background(Color(0x552A3342)).padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0x3366D9E8)), contentAlignment = Alignment.Center) {
            Text(symbol, color = Glacier, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = SecondaryText, fontSize = 11.sp)
        }
    }
}
