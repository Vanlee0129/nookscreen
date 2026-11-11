package app.orchard.tv.plugins

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import app.nookscreen.plugin.PluginApi
import java.security.MessageDigest

internal data class FeaturePlugin(
    val component: ComponentName,
    val name: String,
    val version: String,
    val identity: String,
    val compatible: Boolean,
    val enabled: Boolean,
)

internal class FeaturePlugins(private val context: Context) {
    private val pm = context.packageManager
    private val prefs = context.getSharedPreferences("feature_plugins", Context.MODE_PRIVATE)

    @Suppress("DEPRECATION")
    fun discover(): List<FeaturePlugin> =
        pm.queryIntentActivities(Intent(PluginApi.FEATURE_ACTION), PackageManager.GET_META_DATA or PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { result ->
                val activity = result.activityInfo
                if (!activity.exported || activity.packageName == context.packageName) return@mapNotNull null
                runCatching {
                    val component = ComponentName(activity.packageName, activity.name)
                    val metadata = activity.metaData
                    val pkg = pm.getPackageInfo(component.packageName, PackageManager.GET_SIGNATURES)
                    val signature = pkg.signatures.orEmpty().map {
                        MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).joinToString("") { byte -> "%02x".format(byte) }
                    }.sorted().joinToString("/")
                    require(signature.isNotEmpty())
                    val identity = "$signature:${pkg.firstInstallTime}"
                    val supported = PluginApi.supports(metadata?.getInt(PluginApi.MIN_API) ?: 0, metadata?.getInt(PluginApi.MAX_API) ?: 0)
                    FeaturePlugin(component, result.loadLabel(pm).toString().take(64), pkg.versionName.orEmpty(), identity,
                        supported, prefs.getString(component.flattenToString(), null) == identity)
                }.getOrNull()
            }.sortedBy { it.name }

    fun setEnabled(plugin: FeaturePlugin, enabled: Boolean) {
        if (enabled && !plugin.compatible) return
        val key = plugin.component.flattenToString()
        prefs.edit().apply { if (enabled) putString(key, plugin.identity) else remove(key) }.apply()
    }

    fun open(plugin: FeaturePlugin): Boolean {
        val current = discover().firstOrNull { it.component == plugin.component && it.identity == plugin.identity }
            ?.takeIf { it.enabled && it.compatible } ?: return false
        return runCatching {
            context.startActivity(Intent(PluginApi.FEATURE_ACTION).setComponent(current.component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.isSuccess
    }
}
