package app.nookscreen.plugin;

/** Compatibility contract for independent theme archives and foreground Android features. */
public final class PluginApi {
    public static final int VERSION = 1;
    public static final String FEATURE_ACTION = "app.nookscreen.plugin.OPEN_FEATURE";
    public static final String MIN_API = "app.nookscreen.plugin.MIN_API";
    public static final String MAX_API = "app.nookscreen.plugin.MAX_API";

    private PluginApi() {}

    public static boolean supports(int minimum, int maximum) {
        return minimum > 0 && minimum <= VERSION && VERSION <= maximum;
    }
}
