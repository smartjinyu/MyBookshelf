package com.smartjinyu.mybookshelf.presenter.component;

import com.smartjinyu.mybookshelf.base.BasePresenter;
import com.smartjinyu.mybookshelf.base.BaseView;
import com.smartjinyu.mybookshelf.model.bean.Book;

/**
 * 作者：Neil on 2017/4/13 22:57.
 * 邮箱：cn.neillee@gmail.com
 */

public interface BookFetchComponent {
    interface View extends BaseView {
        void showContent(Book book);

        void showNetError(String errMsg, String isbn);

        void showUnMatchError(String errMsg, String isbn);
    }

    interface Presenter extends BasePresenter<View> {
        void fetchBookInfo(String isbn);
    }
}
