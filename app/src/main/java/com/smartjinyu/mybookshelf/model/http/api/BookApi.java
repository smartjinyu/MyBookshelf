package com.smartjinyu.mybookshelf.model.http.api;

import com.smartjinyu.mybookshelf.model.bean.DouBanJson;
import com.smartjinyu.mybookshelf.model.bean.OpenLibraryJson;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * 作者：Neil on 2017/4/12 13:56.
 * 邮箱：cn.neillee@gmail.com
 */

public interface BookApi {
    String DOUBAN_HOST = "https://api.douban.com/";
    String OPEN_LIB_HOST = "https://openlibrary.org/api/";

    @GET(DOUBAN_HOST + "v2/book/isbn/{isbn}")
    Call<DouBanJson> getDBResult(@Path("isbn") String isbn);

    @GET(OPEN_LIB_HOST + "books")
    Call<OpenLibraryJson> getOLResult(@QueryMap Map<String, String> params);

//    @GET
//    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
}
