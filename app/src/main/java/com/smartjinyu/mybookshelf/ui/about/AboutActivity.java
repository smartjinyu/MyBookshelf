package com.smartjinyu.mybookshelf.ui.about;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.base.SimpleActivity;

import butterknife.BindView;

/**
 * about page
 * Created by smartjinyu on 2017/2/7.
 */

public class AboutActivity extends SimpleActivity {
    @BindView(R.id.about_toolbar)
    Toolbar mToolbar;

    @Override
    protected String getTag() {
        return "AboutActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initEventAndData() {
        setupToolbar(mToolbar, R.string.about_preference_category_title);
    }

    @Override
    protected void doSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            AboutFragment aboutFragment = new AboutFragment();
            getFragmentManager().beginTransaction().add(R.id.activity_about_container, aboutFragment).commit();
        }
    }
}
