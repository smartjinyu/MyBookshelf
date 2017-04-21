package com.smartjinyu.mybookshelf.di.module;

import android.app.Activity;

import com.smartjinyu.mybookshelf.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

/**
 * 作者：Neil on 2017/4/13 23:30.
 * 邮箱：cn.neillee@gmail.com
 */
@Module
public class ActivityModule {
    private final Activity mActivity;

    public ActivityModule(Activity activity) {
        mActivity = activity;
    }

    @ActivityScope
    @Provides
    public Activity provideActivity() {
        return mActivity;
    }
}
