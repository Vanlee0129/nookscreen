package app.orchard.tv.ui.control

import android.app.AlertDialog
import android.text.InputFilter
import android.view.WindowManager
import android.widget.EditText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import app.orchard.tv.apps.SourceLooks
import app.orchard.tv.settings.AerialSettings
import app.orchard.tv.settings.DesktopScenes
import app.orchard.tv.ui.home.MenuRow
import app.orchard.tv.ui.theme.Glyph
import app.orchard.tv.ui.theme.Type

private enum class SceneDialog { Rename, Delete }

@Composable
internal fun ScenePage(vm: HomeViewModel, page: CcPage, takeFocus: Boolean) {
    val context = LocalContext.current
    val catalog = vm.desktopScenes
    val active = catalog.scenes.first { it.id == catalog.active }
    val first = remember { FocusRequester() }
    var pending by remember { mutableStateOf<SceneDialog?>(null) }
    fun show(target: CcPage) { vm.overlay = Overlay.ControlCenter(target) }

    LaunchedEffect(takeFocus) {
        if (takeFocus) {
            withFrameNanos { }
            runCatching { first.requestFocus() }
        }
    }

    if (takeFocus && pending != null) {
        DisposableEffect(pending, active.id) {
            val builder = AlertDialog.Builder(context)
                .setNegativeButton(android.R.string.cancel, null)
            val nameField = if (pending == SceneDialog.Rename) EditText(context).apply {
                setSingleLine()
                filters = arrayOf(InputFilter.LengthFilter(32))
                setText(active.name)
                selectAll()
            } else null
            val dialog = if (nameField != null) {
                builder.setTitle(R.string.scene_rename)
                    .setView(nameField)
                    .setPositiveButton(android.R.string.ok) { _, _ -> vm.renameScene(nameField.text.toString()) }
                    .create()
            } else {
                builder.setTitle(context.getString(R.string.scene_delete_confirm, active.name))
                    .setPositiveButton(android.R.string.ok) { _, _ -> vm.removeScene() }
                    .create()
            }
            dialog.setOnDismissListener { pending = null }
            if (nameField != null) {
                nameField.requestFocus()
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
            dialog.show()
            onDispose {
                dialog.setOnDismissListener(null)
                dialog.dismiss()
            }
        }
    }

    Column(Modifier.heightIn(max = 470.dp).verticalScroll(rememberScrollState())) {
        when (page) {
            CcPage.Scenes -> {
                MenuRow(Glyph.ChevronLeft, stringResource(R.string.scene_title), { show(CcPage.Main) })
                catalog.scenes.forEach { scene ->
                    val selected = scene.id == active.id
                    MenuRow(
                        if (selected) Glyph.Check else Glyph.Home,
                        scene.name,
                        { vm.switchScene(scene.id) },
                        focusRequester = if (selected) first else null,
                    )
                }
                MenuRow(Glyph.Pencil, stringResource(R.string.scene_edit), { show(CcPage.SceneEdit) }, trailing = "›")
                MenuRow(Glyph.Folder, stringResource(R.string.scene_duplicate), vm::duplicateScene)
            }
            CcPage.SceneEdit -> {
                MenuRow(Glyph.ChevronLeft, active.name, { show(CcPage.Scenes) })
                MenuRow(Glyph.Photo, stringResource(R.string.scene_wallpaper), { vm.overlay = Overlay.Wallpaper }, focusRequester = first, trailing = "›")
                MenuRow(Glyph.Input, stringResource(R.string.scene_sources), { show(CcPage.SceneSources) }, trailing = "›")
                MenuRow(Glyph.Pencil, stringResource(R.string.scene_rename), { pending = SceneDialog.Rename })
                if (active.id != DesktopScenes.ORIGINAL) {
                    MenuRow(Glyph.Trash, stringResource(R.string.scene_delete), { pending = SceneDialog.Delete }, destructive = true)
                }
            }
            CcPage.SceneSources -> {
                MenuRow(Glyph.ChevronLeft, stringResource(R.string.scene_sources), { show(CcPage.SceneEdit) }, focusRequester = first)
                if (vm.shownSources.isEmpty()) {
                    BasicText(
                        stringResource(R.string.cc_no_inputs),
                        style = Type.Callout,
                    )
                }
                vm.shownSources.forEach { source ->
                    val pinned = source.id in AerialSettings.pinnedSources
                    MenuRow(
                        if (pinned) Glyph.Check else Glyph.Input,
                        source.label,
                        { vm.setSourcePinned(source, !pinned) },
                        trailing = SourceLooks.of(source.id)?.name ?: source.deviceName,
                    )
                }
            }
            else -> Unit
        }
    }
}
