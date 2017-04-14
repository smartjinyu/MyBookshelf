package com.smartjinyu.mybookshelf.di.component;

import android.app.Activity;

import com.smartjinyu.mybookshelf.di.module.FragmentModule;
import com.smartjinyu.mybookshelf.di.scope.FragmentScope;
import com.smartjinyu.mybookshelf.ui.BookScanFragment;

import dagger.Component;

/**
 * 作者：Neil on 2017/4/7 15:29.
 * 邮箱：cn.neillee@gmail.com
 */
@FragmentScope
@Component(dependencies = AppComponent.class, modules = FragmentModule.class)
public interface FragmentComponent {

    Activity getActivity();

    void inject(BookScanFragment scanFragment);
//
//    void inject(LatestFragment latestFragment);
//
//    void inject(PastFragment pastFragment);
//
//    void inject(StoryCommentFragment storyCommentFragment);
}
