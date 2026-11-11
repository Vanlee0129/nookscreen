package app.orchard.tv.ui.control

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.orchard.tv.CcPage
import app.orchard.tv.HomeViewModel
import app.orchard.tv.Overlay
import app.orchard.tv.R
import app.orchard.tv.plugins.FeaturePlugins
import app.orchard.tv.plugins.ThemeRelease
import app.orchard.tv.plugins.title
import app.orchard.tv.settings.Restricted
import app.orchard.tv.ui.home.MenuRow
import app.orchard.tv.ui.theme.Glyph
import kotlinx.coroutines.launch

@Composable
internal fun PluginPage(vm: HomeViewModel, takeFocus: Boolean) {
    val context = LocalContext.current
    val themes by vm.themePlugins.installed.collectAsState()
    val first = remember { FocusRequester() }
    val features = remember(context) { FeaturePlugins(context) }
    var discovered by remember { mutableStateOf(features.discover()) }
    val scope = rememberCoroutineScope()
    var downloading by remember { mutableStateOf<ThemeRelease?>(null) }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importTheme(uri)
    }
    fun failed() = vm.notify(context.getString(R.string.plugin_failed), Glyph.Photo)
    LaunchedEffect(takeFocus, vm.resumed, vm.packageChanges) {
        if (takeFocus && vm.resumed) discovered = features.discover()
    }
    LaunchedEffect(takeFocus, vm.resumed, Restricted.active) {
        if (!takeFocus || !vm.resumed) return@LaunchedEffect
        if (Restricted.active) vm.overlay = Overlay.ControlCenter(CcPage.Main)
        else {
            withFrameNanos { }
            runCatching { first.requestFocus() }
        }
    }
    fun download(release: ThemeRelease) {
        if (downloading != null || Restricted.active) return
        scope.launch {
            downloading = release
            try { vm.notify(context.getString(vm.downloadTheme(release).message), Glyph.Photo) }
            finally { downloading = null }
        }
    }
    Column(Modifier.heightIn(max = 470.dp).verticalScroll(rememberScrollState())) {
        MenuRow(Glyph.ChevronLeft, stringResource(R.string.plugins_title),
            { vm.overlay = Overlay.ControlCenter(CcPage.Themes) }, focusRequester = first)
        MenuRow(Glyph.Photo, stringResource(R.string.plugin_import), {
            if (!Restricted.active) runCatching { import.launch(arrayOf("application/zip", "application/octet-stream")) }.onFailure { failed() }
        })
        for (release in ThemeRelease.entries) {
            if (release.id !in themes) {
                MenuRow(Glyph.Photo, stringResource(release.label), { download(release) },
                    trailing = stringResource(if (downloading == release) R.string.plugin_working else R.string.plugin_add))
            }
        }
        for (theme in themes.values.sortedBy { it.manifest.name }) {
            val release = ThemeRelease.entries.firstOrNull { it.id == theme.manifest.id }
            val id = theme.manifest.id
            MenuRow(Glyph.Photo, theme.title(context), {
                val actions = buildList<Pair<Int, () -> Unit>> {
                    if (theme.active) add(R.string.plugin_apply to { vm.applyThemePlugin(id); Unit })
                    if (theme.manifest.compatible) add((if (theme.enabled) R.string.plugin_disable else R.string.plugin_enable) to {
                        if (!vm.themePlugins.setEnabled(id, !theme.enabled)) failed()
                    })
                    if (release != null) add(R.string.plugin_update to { download(release) })
                    add(R.string.plugin_remove to {
                        AlertDialog.Builder(context).setTitle(context.getString(R.string.theme_delete_confirm, theme.title(context)))
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                if (!Restricted.active && !vm.themePlugins.remove(id)) failed()
                            }
                            .show()
                    })
                }
                AlertDialog.Builder(context).setTitle(theme.title(context))
                    .setItems(actions.map { context.getString(it.first) }.toTypedArray()) { _, index ->
                        if (!Restricted.active) actions[index].second()
                    }
                    .show()
            }, trailing = when {
                downloading == release && release != null -> stringResource(R.string.plugin_working)
                !theme.manifest.compatible -> stringResource(R.string.plugin_incompatible_short)
                !theme.enabled -> stringResource(R.string.plugin_disabled)
                else -> "v${theme.manifest.version} ›"
            })
        }
        for (feature in discovered) {
            MenuRow(Glyph.Gear, feature.name, {
                val actions = buildList<Pair<Int, () -> Unit>> {
                    if (feature.compatible && feature.enabled) add(R.string.plugin_open to { if (!features.open(feature)) failed() })
                    if (feature.compatible) add((if (feature.enabled) R.string.plugin_disable else R.string.plugin_enable) to {
                        features.setEnabled(feature, !feature.enabled)
                        discovered = features.discover()
                    })
                    add(R.string.plugin_android_settings to {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", feature.component.packageName, null)))
                        }.onFailure { failed() }
                    })
                }
                AlertDialog.Builder(context).setTitle(feature.name)
                    .setItems(actions.map { context.getString(it.first) }.toTypedArray()) { _, index ->
                        if (!Restricted.active) actions[index].second()
                    }
                    .show()
            }, trailing = when {
                !feature.compatible -> stringResource(R.string.plugin_incompatible_short)
                !feature.enabled -> stringResource(R.string.plugin_disabled)
                else -> feature.version
            })
        }
    }
}
