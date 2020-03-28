package com.smartjinyu.mybookshelf;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.microsoft.appcenter.analytics.Analytics;

import java.util.HashMap;
import java.util.Map;

/**
 * settings activity
 * Created by smartjinyu on 2017/2/8.
 */

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Map<String, String> logEvents = new HashMap<>();
        logEvents.put("Activity", TAG);
        Analytics.trackEvent("onCreate", logEvents);

        logEvents.clear();
        logEvents.put("Name", "onCreate");
        Analytics.trackEvent(TAG, logEvents);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            SettingsFragment settingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction().replace(R.id.activity_settings_container, settingsFragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
