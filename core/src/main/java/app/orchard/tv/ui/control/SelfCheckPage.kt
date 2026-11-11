package app.orchard.tv.ui.control

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.orchard.tv.CcPage
import app.orchard.tv.HomeViewModel
import app.orchard.tv.Overlay
import app.orchard.tv.R
import app.orchard.tv.SelfCheckTest
import app.orchard.tv.system.NetKind
import app.orchard.tv.ui.components.consumeTaps
import app.orchard.tv.ui.components.smoothBackground
import app.orchard.tv.ui.home.MenuRow
import app.orchard.tv.ui.theme.Glyph
import app.orchard.tv.ui.theme.Palette
import app.orchard.tv.ui.theme.Type
import app.orchard.tv.wallpaper.PhoneServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun SelfCheckPage(vm: HomeViewModel, active: Boolean) {
    val first = remember { FocusRequester() }
    LaunchedEffect(active) {
        if (active) {
            withFrameNanos { }
            first.requestFocus()
        }
    }
    Column {
        MenuRow(Glyph.ChevronLeft, stringResource(R.string.self_check_title), {
            vm.overlay = Overlay.ControlCenter()
        })
        MenuRow(Glyph.Photo, stringResource(R.string.self_check_picture), {
            vm.overlay = Overlay.SelfCheck(SelfCheckTest.Picture)
        }, focusRequester = first)
        CheckHint(R.string.self_check_picture_hint)
        MenuRow(Glyph.Switches, stringResource(R.string.self_check_remote), {
            vm.overlay = Overlay.SelfCheck(SelfCheckTest.Remote)
        })
        CheckHint(R.string.self_check_remote_hint)
        MenuRow(Glyph.Wifi, stringResource(R.string.self_check_network), {
            vm.overlay = Overlay.ControlCenter(CcPage.NetworkCheck)
        }, trailing = "›")
    }
}

@Composable
private fun CheckHint(id: Int) {
    BasicText(stringResource(id), style = Type.Caption, modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 8.dp))
}

@Composable
internal fun NetworkCheckPage(vm: HomeViewModel, active: Boolean) {
    val context = LocalContext.current
    val network = vm.network
    val check = remember(active, network.kind, network.name) { PhoneServer.ConnectionCheck() }
    val reached by check.reached.collectAsState()
    var qr by remember(check) { mutableStateOf<ImageBitmap?>(null) }
    var ready by remember(check) { mutableStateOf(false) }
    val back = remember { FocusRequester() }

    LaunchedEffect(check) {
        if (!active) return@LaunchedEffect
        withFrameNanos { }
        back.requestFocus()
        val url = withContext(Dispatchers.IO) { PhoneServer.start(context) }
        if (url != null) {
            qr = withContext(Dispatchers.Default) {
                PhoneServer.qr("${url}?check=${check.id}#more").asImageBitmap()
            }
        }
        ready = true
    }
    DisposableEffect(check, qr) {
        if (qr != null) PhoneServer.beginConnectionCheck(check)
        onDispose { PhoneServer.endConnectionCheck(check) }
    }

    val kind = stringResource(when (network.kind) {
        NetKind.Wifi -> R.string.net_wifi
        NetKind.Ethernet -> R.string.net_ethernet
        NetKind.Offline -> R.string.net_offline
    })
    Column {
        MenuRow(Glyph.ChevronLeft, stringResource(R.string.self_check_network), {
            vm.overlay = Overlay.ControlCenter(CcPage.SelfCheck)
        }, focusRequester = back)
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val transport = if (network.vpn) "$kind · ${stringResource(R.string.net_vpn)}" else kind
            BasicText(stringResource(R.string.self_check_network_type, transport), style = Type.Body)
            if (!ready || qr == null) {
                BasicText(
                    stringResource(if (ready) R.string.self_check_network_no_address else R.string.self_check_network_preparing),
                    style = Type.Caption,
                )
            }
            qr?.let { image ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        image, contentDescription = stringResource(R.string.self_check_network_qr),
                        modifier = Modifier.size(104.dp).smoothBackground(Color.White, 12.dp).padding(6.dp),
                        contentScale = ContentScale.Fit, filterQuality = FilterQuality.None,
                    )
                    BasicText(
                        stringResource(if (reached) R.string.self_check_network_reached else R.string.self_check_network_scan),
                        style = Type.Caption,
                    )
                }
                BasicText(stringResource(R.string.self_check_diagnostics), style = Type.Caption)
            }
        }
    }
}

private val solidPatterns = listOf(Color.Black, Color.White, Color.Red, Color.Green, Color.Blue)
private const val GRAYSCALE = 5
private const val EDGES = 6
private const val PATTERN_COUNT = 7

@Composable
internal fun SelfCheckScreen(test: SelfCheckTest, onBack: () -> Unit) {
    val focus = remember { FocusRequester() }
    var pattern by remember { mutableIntStateOf(0) }
    var lastKey by remember { mutableStateOf<Int?>(null) }
    var repeats by remember { mutableIntStateOf(0) }
    BackHandler(onBack = onBack)
    LaunchedEffect(Unit) {
        withFrameNanos { }
        focus.requestFocus()
    }

    Box(
        Modifier.fillMaxSize()
            .background(Palette.Background)
            .consumeTaps()
            .focusRequester(focus)
            .onPreviewKeyEvent { event ->
                val key = event.nativeKeyEvent
                if (key.keyCode == KeyEvent.KEYCODE_BACK) return@onPreviewKeyEvent false
                when (test) {
                    SelfCheckTest.Picture -> if (key.action == KeyEvent.ACTION_DOWN && key.repeatCount == 0) {
                        when (key.keyCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT -> pattern = (pattern + PATTERN_COUNT - 1) % PATTERN_COUNT
                            KeyEvent.KEYCODE_DPAD_RIGHT -> pattern = (pattern + 1) % PATTERN_COUNT
                        }
                    }
                    SelfCheckTest.Remote -> {
                        // Some firmware delivers the release of a shortcut but handles its press.
                        if (key.action == KeyEvent.ACTION_DOWN ||
                            key.action == KeyEvent.ACTION_UP && !key.isCanceled && lastKey != key.keyCode
                        ) {
                            lastKey = key.keyCode
                            repeats = key.repeatCount
                        }
                    }
                }
                true
            }
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        when (test) {
            SelfCheckTest.Picture -> PicturePattern(pattern)
            SelfCheckTest.Remote -> RemoteResult(lastKey, repeats)
        }
    }
}

@Composable
private fun PicturePattern(pattern: Int) {
    val name = stringArrayResource(R.array.self_check_patterns)[pattern]
    Canvas(Modifier.fillMaxSize().semantics { contentDescription = name }) {
        when (pattern) {
            GRAYSCALE -> {
                val width = size.width / 11
                for (step in 0..10) {
                    val level = step / 10f
                    drawRect(Color(level, level, level), Offset(step * width, 0f), Size(width, size.height))
                }
            }
            EDGES -> {
                val stroke = (size.minDimension / 240f).coerceAtLeast(1f)
                val inset = Offset(size.width * 0.05f, size.height * 0.05f)
                drawRect(Color.White, Offset(stroke / 2, stroke / 2),
                    Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
                drawRect(Color.White, inset,
                    Size(size.width - 2 * inset.x, size.height - 2 * inset.y), style = Stroke(stroke))
                drawLine(Color.White, Offset(center.x, inset.y), Offset(center.x, size.height - inset.y), stroke)
                drawLine(Color.White, Offset(inset.x, center.y), Offset(size.width - inset.x, center.y), stroke)
            }
            else -> drawRect(solidPatterns[pattern])
        }
    }
}

@Composable
private fun RemoteResult(keyCode: Int?, repeats: Int) {
    Column(
        Modifier.padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BasicText(stringResource(R.string.self_check_remote), style = Type.LargeTitle)
        if (keyCode == null) {
            BasicText(stringResource(R.string.self_check_remote_waiting), style = Type.Body)
        } else {
            val name = KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
            BasicText(stringResource(R.string.self_check_remote_key, name, keyCode), style = Type.Headline)
            BasicText(stringResource(R.string.self_check_remote_repeat, repeats), style = Type.Callout)
        }
        BasicText(
            stringResource(R.string.self_check_remote_reserved),
            style = Type.Caption.copy(textAlign = TextAlign.Center),
        )
    }
}
