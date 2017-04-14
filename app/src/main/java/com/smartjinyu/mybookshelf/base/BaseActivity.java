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
import com.smartjinyu.mybookshelf.app.BookShelfApp;
import com.smartjinyu.mybookshelf.di.component.ActivityComponent;
import com.smartjinyu.mybookshelf.di.component.DaggerActivityComponent;
import com.smartjinyu.mybookshelf.di.module.ActivityModule;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 作者：Neil on 2017/4/12 13:08.
 * 邮箱：cn.neillee@gmail.com
 */

public abstract class BaseActivity<T extends BasePresenter>
        extends AppCompatActivity implements BaseView {
    @Inject
    protected T mPresenter;

    protected Activity mContext;
    private Unbinder mUnBinder;

    protected int mContentId;
    protected String TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getTag();
        logContentView(getContentId(), TAG);
        doSavedInstanceState(savedInstanceState);
        setContentView(getLayoutId());
        mUnBinder = ButterKnife.bind(this);
        mContext = this;
        initInject();
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        BookShelfApp.getInstance().addActivity(this);
        initEventAndData();
    }

    protected void setupToolbar(Toolbar toolbar, int titleId) {
        this.setupToolbar(toolbar, mContext.getString(titleId));
    }

    protected void setupToolbar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDefaultDisplayHomeAsUpEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null)
            mPresenter.detachView();
        mUnBinder.unbind();
        BookShelfApp.getInstance().removeActivity(this);
    }

    protected ActivityComponent getActivityComponent() {
        return DaggerActivityComponent.builder()
                .appComponent(BookShelfApp.getAppComponent())
                .activityModule(getActivityModule())
                .build();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    protected void logContentView(String contentId, String tag) {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(tag)
                .putContentType("Activity")
                .putContentId(contentId)
                .putCustomAttribute("onCreate", "onCreate"));
    }

    protected abstract String getTag();

    protected abstract String getContentId();

    protected abstract void doSavedInstanceState(Bundle savedInstanceState);

    protected abstract int getLayoutId();

    protected abstract void initInject();

    protected abstract void initEventAndData();
}
