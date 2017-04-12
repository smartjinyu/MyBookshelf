package com.smartjinyu.mybookshelf.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.smartjinyu.mybookshelf.AboutFragment;
import com.smartjinyu.mybookshelf.R;

/**
 * about page
 * Created by smartjinyu on 2017/2/7.
 */

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(TAG)
                .putContentType("Activity")
                .putContentId("1005")
                .putCustomAttribute("onCreate", "onCreate"));

        Toolbar mToolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.about_preference_category_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            AboutFragment aboutFragment = new AboutFragment();
            getFragmentManager().beginTransaction().add(R.id.activity_about_container, aboutFragment).commit();
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
