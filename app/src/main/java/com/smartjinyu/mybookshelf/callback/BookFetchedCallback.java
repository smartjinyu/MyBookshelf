package com.smartjinyu.mybookshelf.callback;

import com.smartjinyu.mybookshelf.model.bean.Book;

/**
 * 作者：Neil on 2017/4/14 21:01.
 * 邮箱：cn.neillee@gmail.com
 */

public interface BookFetchedCallback {
    void onBookFetched(Book book);
}
