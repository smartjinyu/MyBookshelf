package com.smartjinyu.mybookshelf.ui.setting;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.base.SimpleActivity;

import butterknife.BindView;

/**
 * settings activity
 * Created by smartjinyu on 2017/2/8.
 */

public class SettingsActivity extends SimpleActivity {
    @BindView(R.id.settings_toolbar)
    Toolbar mToolbar;

    @Override
    protected String getTag() {
        return "SettingsActivity";
    }

    @Override
    protected void doSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            SettingsFragment settingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction().replace(R.id.activity_settings_container, settingsFragment).commit();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void initEventAndData() {
        setupToolbar(mToolbar, R.string.settings_settings);
    }
}
