package com.smartjinyu.mybookshelf.model.http;

import com.smartjinyu.mybookshelf.model.bean.DouBanJson;
import com.smartjinyu.mybookshelf.model.bean.OpenLibraryJson;
import com.smartjinyu.mybookshelf.model.http.api.BookApi;

import java.util.Map;

import retrofit2.Call;

/**
 * 作者：Neil on 2017/4/12 13:55.
 * 邮箱：cn.neillee@gmail.com
 */

public class RetrofitHelper {
    private BookApi mBookApi;

    public RetrofitHelper(BookApi bookApi) {
        mBookApi = bookApi;
    }

    public Call<DouBanJson> fetchDouBanBook(String isbn) {
        return mBookApi.getDBResult(isbn);
    }

    public Call<OpenLibraryJson> fetchOpenLibBook(Map<String, String> params) {
        return mBookApi.getOLResult(params);
    }
}
