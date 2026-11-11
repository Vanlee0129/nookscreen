package app.nookscreen.plugin.clock;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextClock;
import app.nookscreen.plugin.PluginApi;

public final class ClockActivity extends Activity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (!PluginApi.FEATURE_ACTION.equals(getIntent().getAction())) {
            finish();
            return;
        }
        TextClock clock = new TextClock(this);
        clock.setTextSize(96);
        clock.setTextColor(Color.WHITE);
        clock.setBackgroundColor(0xff101923);
        clock.setGravity(Gravity.CENTER);
        setContentView(clock);
    }
}
