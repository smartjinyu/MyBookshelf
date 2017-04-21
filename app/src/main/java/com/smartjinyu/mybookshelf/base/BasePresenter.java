package com.smartjinyu.mybookshelf.base;

/**
 * 作者：Neil on 2017/4/14 00:19.
 * 邮箱：cn.neillee@gmail.com
 */

public interface BasePresenter<T extends BaseView> {
    void attachView(T view);

    void detachView();
}
