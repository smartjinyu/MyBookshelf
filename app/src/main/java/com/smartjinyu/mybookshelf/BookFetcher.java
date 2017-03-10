package com.smartjinyu.mybookshelf;

import android.content.Context;

/**
 * Created by smartjinyu on 2017/1/20.
 * The superclass of Online Book information Fetcher
 */

public abstract class BookFetcher {
    private static final String TAG = "BookFetcher";
    public static final int fetcherID_DB = 0;
    public static final int fetcherID_OL = 1;
    protected Context mContext;

    protected Book mBook;

    /**
     * get book info from webservices
     *
     * @param context
     * @param isbn    book's isbn
     * @param mode    0 single add, 1 batch add
     */
    protected abstract void getBookInfo(Context context, String isbn, final int mode);
}
