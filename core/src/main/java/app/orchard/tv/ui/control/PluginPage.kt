package app.orchard.tv.ui.control

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import app.orchard.tv.settings.Restricted
import app.orchard.tv.ui.home.MenuRow
import app.orchard.tv.ui.theme.Glyph
import app.orchard.tv.ui.theme.Type

@Composable
internal fun PluginPage(vm: HomeViewModel, takeFocus: Boolean) {
    val context = LocalContext.current
    val first = remember { FocusRequester() }
    val features = remember(context) { FeaturePlugins(context) }
    var discovered by remember { mutableStateOf(features.discover()) }
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
    Column(Modifier.heightIn(max = 470.dp).verticalScroll(rememberScrollState())) {
        MenuRow(Glyph.ChevronLeft, stringResource(R.string.plugins_title),
            { vm.overlay = Overlay.ControlCenter(CcPage.Main) }, focusRequester = first)
        if (discovered.isEmpty()) {
            BasicText(
                stringResource(R.string.plugins_empty),
                style = Type.Caption,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
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
