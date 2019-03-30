package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by smartjinyu on 2017/1/20.
 * This class is used to get information from website like DouBan
 */

public class DoubanFetcher extends BookFetcher {
    private static final String TAG = "DoubanFetcher";


    @Override
    public void getBookInfo(final Context context, final String isbn, final int mode) {
        mContext = context;
        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.douban.com/v2/book/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        DB_API api = mRetrofit.create(DB_API.class);
        //create an instance of douban api
        byte[] data = Base64.decode(BuildConfig.doubanApiKey, Base64.DEFAULT);
        data[0] -= 1; data[1] += 2;
        Call<DouBanJson> call = api.getDBResult(isbn, new String(data, StandardCharsets.UTF_8));

        call.enqueue(new Callback<DouBanJson>() {
            @Override
            public void onResponse(Call<DouBanJson> call, Response<DouBanJson> response) {
                if (response.code() == 200) {
                    Log.i(TAG, "GET Douban information successfully, id = " + response.body().getId()
                            + ", title = " + response.body().getTitle());
                    mBook = new Book();
                    mBook.setTitle(response.body().getTitle());
                    //mBook.setId(Long.parseLong(response.body().getId(),10));
                    mBook.setIsbn(isbn);
                    if (response.body().getAuthor().size() != 0) {
                        mBook.setAuthors(response.body().getAuthor());
                    } else {
                        mBook.setAuthors(new ArrayList<String>());
                    }
                    if (response.body().getTranslator().size() != 0) {
                        mBook.setTranslators(response.body().getTranslator());
                    } else {
                        mBook.setTranslators(new ArrayList<String>());
                    }

                    mBook.getWebIds().put("douban", response.body().getId());
                    mBook.setPublisher(response.body().getPublisher());

                    String rawDate = response.body().getPubdate();
                    Log.i(TAG, "Date raw = " + rawDate);
                    String year, month;
                    if (rawDate.contains("-")) {
                        // 2016-11
                        String[] date = rawDate.split("-");
                        year = date[0];
                        // rawDate sometimes is "2016-11", sometimes is "2000-10-1", sometimes is "2010-1"
                        month = date[1];
                    } else if (rawDate.contains(".")) {
                        String[] date = rawDate.split("\\.");
                        year = date[0];
                        // rawDate sometimes is "2016-11", sometimes is "2000-10-1", sometimes is "2010-1"
                        month = date[1];
                    } else {
                        year = "9999";
                        month = "1";
                    }
                    Log.i(TAG, "Get PubDate Year = " + year + ", month = " + month);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 1);
                    mBook.setPubTime(calendar);
                    final String imageURL = response.body().getImages().getLarge();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean addWebsite = pref.getBoolean("settings_pref_acwebsite", true);
                    if (addWebsite) {
                        mBook.setWebsite("https://book.douban.com/subject/" + response.body().getId());
                    }
                    if (mode == 0) {
                        ((SingleAddActivity) mContext).fetchSucceed(mBook, imageURL);
                    } else if (mode == 1) {
                        ((BatchAddActivity) mContext).fetchSucceed(mBook, imageURL);
                    }
                } else {
                    Log.w(TAG, "Unexpected response code " + response.code() + ", isbn = " + isbn);
                    if (mode == 0) {
                        ((SingleAddActivity) mContext).fetchFailed(
                                BookFetcher.fetcherID_DB, 0, isbn
                        );
                    } else if (mode == 1) {
                        ((BatchAddActivity) mContext).fetchFailed(
                                BookFetcher.fetcherID_DB, 0, isbn);
                    }
                }

            }

            @Override
            public void onFailure(Call<DouBanJson> call, Throwable t) {
                Log.w(TAG, "GET Douban information failed, " + t.toString());
                if (mode == 0) {
                    ((SingleAddActivity) mContext).fetchFailed(
                            BookFetcher.fetcherID_DB, 1, isbn
                    );
                } else if (mode == 1) {
                    ((BatchAddActivity) mContext).fetchFailed(
                            BookFetcher.fetcherID_DB, 1, isbn);
                }
            }
        });


    }

    private interface DB_API {
        @GET("isbn/{isbn}")
        Call<DouBanJson> getDBResult(@Path("isbn") String isbn, @Query("apikey") String apikey);
    }

}






