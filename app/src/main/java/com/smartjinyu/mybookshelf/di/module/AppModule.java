package com.smartjinyu.mybookshelf.di.module;

import com.smartjinyu.mybookshelf.app.BookShelfApp;
import com.smartjinyu.mybookshelf.model.http.RetrofitHelper;
import com.smartjinyu.mybookshelf.model.http.api.BookApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * 作者：Neil on 2017/4/13 23:35.
 * 邮箱：cn.neillee@gmail.com
 */
@Module
public class AppModule {
    private final BookShelfApp mBookShelfApp;

    public AppModule(BookShelfApp bookShelfApp) {
        mBookShelfApp = bookShelfApp;
    }

    @Provides
    @Singleton
    public BookShelfApp provideApp() {
        return mBookShelfApp;
    }

    @Provides
    @Singleton
    public RetrofitHelper provideRetrofitHelper(BookApi bookApi) {
        return new RetrofitHelper(bookApi);
    }
}
