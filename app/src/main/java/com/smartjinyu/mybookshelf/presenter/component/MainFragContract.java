package com.smartjinyu.mybookshelf.presenter.component;

import android.content.Context;

import com.smartjinyu.mybookshelf.base.BasePresenter;
import com.smartjinyu.mybookshelf.base.BaseView;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.model.bean.BookShelf;
import com.smartjinyu.mybookshelf.model.bean.Label;

import java.util.List;

/**
 * 作者：Neil on 2017/4/17 09:32.
 * 邮箱：cn.neillee@gmail.com
 */

public interface MainFragContract {
    interface View extends BaseView {
        void showContent(List<Book> books);

        void showError(String errMsg);
    }

    interface Presenter extends BasePresenter<View> {
        void doSearch(Context context, String keyword, BookShelf bookshelf);

        void fetchBooks(Context context, BookShelf bookShelf, Label label);

    }
}
