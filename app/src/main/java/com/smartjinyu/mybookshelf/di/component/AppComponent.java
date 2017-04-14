package com.smartjinyu.mybookshelf.di.component;

import com.smartjinyu.mybookshelf.app.BookShelfApp;
import com.smartjinyu.mybookshelf.di.module.AppModule;
import com.smartjinyu.mybookshelf.di.module.HttpModule;
import com.smartjinyu.mybookshelf.model.http.RetrofitHelper;

import javax.inject.Singleton;

import dagger.Component;

/**
 * 作者：Neil on 2017/4/13 23:41.
 * 邮箱：cn.neillee@gmail.com
 */
@Singleton
@Component(modules = {AppModule.class, HttpModule.class})
public interface AppComponent {
    BookShelfApp getContext();

    RetrofitHelper retrofitHelper();
}
