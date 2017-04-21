package com.smartjinyu.mybookshelf.base;

/**
 * 作者：Neil on 2017/4/14 11:13.
 * 邮箱：cn.neillee@gmail.com
 */

public class SimplePresenter<T extends BaseView> implements BasePresenter<T> {
    protected T mView;

    @Override
    public void attachView(T view) {
        mView = view;
    }

    @Override
    public void detachView() {
        this.mView = null;
    }
}
