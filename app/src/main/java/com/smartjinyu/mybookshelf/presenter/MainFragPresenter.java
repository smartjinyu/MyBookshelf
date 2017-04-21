package com.smartjinyu.mybookshelf.presenter;

import android.content.Context;

import com.smartjinyu.mybookshelf.base.SimplePresenter;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.bean.BookShelf;
import com.smartjinyu.mybookshelf.model.bean.Label;
import com.smartjinyu.mybookshelf.presenter.component.MainFragContract;

import java.util.UUID;

import javax.inject.Inject;

/**
 * 作者：Neil on 2017/4/17 09:41.
 * 邮箱：cn.neillee@gmail.com
 */

public class MainFragPresenter extends SimplePresenter<MainFragContract.View>
        implements MainFragContract.Presenter {

    @Inject
    MainFragPresenter() {
    }

    @Override
    public void doSearch(Context context, String keyword, BookShelf bookshelf) {
        UUID bookShelfId = (bookshelf == null) ? null : bookshelf.getId();
        mView.showContent(BookLab.get(context).searchBook(keyword, bookShelfId));
    }

    @Override
    public void fetchBooks(Context context, BookShelf bookShelf, Label label) {
        UUID bookShelfId = (bookShelf == null) ? null : bookShelf.getId();
        UUID labelId = (label == null) ? null : label.getId();
        mView.showContent(BookLab.get(context).getBooks(bookShelfId, labelId));
    }
}
