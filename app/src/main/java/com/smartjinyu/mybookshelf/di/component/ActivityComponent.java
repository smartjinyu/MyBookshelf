package com.smartjinyu.mybookshelf.di.component;

import android.app.Activity;

import com.smartjinyu.mybookshelf.di.module.ActivityModule;
import com.smartjinyu.mybookshelf.di.scope.ActivityScope;
import com.smartjinyu.mybookshelf.ui.SingleAddActivity;

import dagger.Component;

/**
 * 作者：Neil on 2017/4/13 23:29.
 * 邮箱：cn.neillee@gmail.com
 */
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
@ActivityScope
public interface ActivityComponent {
    Activity getActivity();

    void inject(SingleAddActivity singleAddActivity);
}
