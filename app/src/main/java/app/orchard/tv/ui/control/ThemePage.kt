package app.orchard.tv.ui.control

import android.app.AlertDialog
import android.text.InputFilter
import android.view.WindowManager
import android.widget.EditText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.orchard.tv.CcPage
import app.orchard.tv.HomeViewModel
import app.orchard.tv.Overlay
import app.orchard.tv.R
import app.orchard.tv.settings.HomeThemes
import app.orchard.tv.settings.ThemePack
import app.orchard.tv.settings.ThemeLook
import app.orchard.tv.settings.SavedTheme
import app.orchard.tv.ui.home.MenuRow
import app.orchard.tv.ui.theme.Glyph
import app.orchard.tv.ui.theme.Type
import app.orchard.tv.wallpaper.Wallpapers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class Action { Save, Manage, Rename, Update, Delete }
private data class Prompt(val action: Action, val theme: SavedTheme? = null)

@Composable
internal fun ThemePage(vm: HomeViewModel, page: CcPage, takeFocus: Boolean) {
    val context = LocalContext.current
    val themes = vm.homeThemes
    val current = vm.currentLook()
    val first = remember { FocusRequester() }
    var prompt by remember { mutableStateOf<Prompt?>(null) }
    fun show(target: CcPage) { vm.overlay = Overlay.ControlCenter(target) }

    LaunchedEffect(takeFocus) {
        if (takeFocus) {
            withFrameNanos { }
            runCatching { first.requestFocus() }
        }
    }

    val shown = prompt
    if (takeFocus && shown != null) {
        DisposableEffect(shown) {
            val builder = AlertDialog.Builder(context).setNegativeButton(android.R.string.cancel, null)
            val theme = shown.theme
            val field = if (shown.action == Action.Save || shown.action == Action.Rename) {
                EditText(context).apply {
                    setSingleLine()
                    filters = arrayOf(InputFilter.LengthFilter(HomeThemes.MAX_NAME))
                    setHint(R.string.theme_name)
                    setText(theme?.name.orEmpty())
                    selectAll()
                }
            } else null
            when (shown.action) {
                Action.Save, Action.Rename -> builder
                    .setTitle(if (theme == null) R.string.theme_save else R.string.theme_rename)
                    .setView(field)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val name = field?.text?.toString().orEmpty()
                        if (theme == null) vm.createTheme(name) else themes.update(theme.id, name = name)
                    }
                Action.Manage -> {
                    val actions = listOf(Action.Rename, Action.Update, Action.Delete)
                    val labels = listOf(R.string.theme_rename, R.string.theme_update, R.string.theme_delete)
                    builder.setTitle(theme?.name).setItems(labels.map(context::getString).toTypedArray()) { _, index ->
                        prompt = Prompt(actions[index], theme)
                    }
                }
                Action.Update, Action.Delete -> if (theme != null) {
                    val deleting = shown.action == Action.Delete
                    builder.setTitle(context.getString(if (deleting) R.string.theme_delete_confirm else R.string.theme_update_confirm, theme.name))
                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        if (deleting) {
                            vm.deleteTheme(theme.id)
                            if (themes.saved.isEmpty()) show(CcPage.Themes)
                        } else themes.update(theme.id, look = vm.currentLook())
                    }
                } else Unit
            }
            val dialog = builder.create()
            dialog.setOnDismissListener { if (prompt == shown) prompt = null }
            if (field != null) {
                field.requestFocus()
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
            CcPage.Themes -> {
                MenuRow(Glyph.ChevronLeft, stringResource(R.string.theme_title), { show(vm.themeReturnPage) })
                ThemePack.entries.forEachIndexed { index, pack ->
                    ThemeRow(
                        current.pack == pack && themes.selection.customId == null,
                        stringResource(pack.label),
                        pack.wallpaper,
                        if (index == 0) first else null,
                    ) { vm.applyTheme(ThemeLook(pack.wallpaper, pack)) }
                }
                if (themes.saved.isNotEmpty()) {
                    BasicText(
                        stringResource(R.string.theme_saved),
                        style = Type.Caption,
                        modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 4.dp),
                    )
                    themes.saved.forEach { theme ->
                        ThemeRow(themes.selection.customId == theme.id, theme.name, theme.look.wallpaper) {
                            vm.applyTheme(theme.look, theme.id)
                        }
                    }
                }
                MenuRow(Glyph.Photo, stringResource(R.string.scene_wallpaper), { vm.overlay = Overlay.Wallpaper }, trailing = "›")
                if (themes.selection.customId != null) {
                    BasicText(stringResource(R.string.theme_upload_hint), style = Type.Caption, modifier = Modifier.padding(horizontal = 14.dp))
                }
                MenuRow(Glyph.Pencil, stringResource(R.string.theme_save), { prompt = Prompt(Action.Save) })
                if (themes.saved.isNotEmpty()) {
                    MenuRow(Glyph.Gear, stringResource(R.string.theme_manage), { show(CcPage.ThemeManage) }, trailing = "›")
                }
            }
            CcPage.ThemeManage -> {
                MenuRow(Glyph.ChevronLeft, stringResource(R.string.theme_manage), { show(CcPage.Themes) }, focusRequester = first)
                themes.saved.forEach { theme ->
                    MenuRow(Glyph.Pencil, theme.name, { prompt = Prompt(Action.Manage, theme) }, trailing = "›")
                }
            }
            else -> Unit
        }
    }
}


@Composable
private fun ThemeRow(selected: Boolean, name: String, wallpaper: String, first: FocusRequester? = null, onClick: () -> Unit) {
    val thumbnail by produceState<ImageBitmap?>(null, wallpaper) {
        value = withContext(Dispatchers.IO) { Wallpapers.thumbnail(wallpaper)?.asImageBitmap() }
    }
    MenuRow(if (selected) Glyph.Check else Glyph.Photo, name, onClick, focusRequester = first,
        trailing = if (selected) "✓" else null, preview = thumbnail)
}
