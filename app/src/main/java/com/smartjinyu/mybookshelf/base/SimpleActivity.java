package com.smartjinyu.mybookshelf.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 作者：Neil on 2017/4/12 13:08.
 * 邮箱：cn.neillee@gmail.com
 */

public abstract class SimpleActivity extends AppCompatActivity {

    protected String TAG = SimpleActivity.class.getSimpleName();

    private Unbinder mUnBinder;
    protected Activity mContext;

    private View.OnClickListener mUpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doSavedInstanceState(savedInstanceState);
        setContentView(getLayoutId());
        TAG = getTag();
        // log content view
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(TAG)
                .putContentType("Activity")
                .putContentId("1005")
                .putCustomAttribute("onCreate", "onCreate"));

        mUnBinder = ButterKnife.bind(this);
        mContext = this;
        initEventAndData();
    }

    protected void doSavedInstanceState(Bundle savedInstanceState) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }

    protected void setupToolbar(Toolbar toolbar, int titleId) {
        this.setupToolbar(toolbar, mContext.getString(titleId));
    }

    protected void setupToolbar(Toolbar toolbar, String title) {
        setSupportActionBar(toolbar);
        toolbar.setTitle(title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(mUpClickListener);
    }

    protected abstract String getTag();

    protected abstract int getLayoutId();

    protected abstract void initEventAndData();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
