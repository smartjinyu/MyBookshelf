package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * used to fetch books from OpenLibrary
 * https://openlibrary.org/dev/docs/api/books
 * Created by smartjinyu on 2017/2/19.
 */

public class OpenLibraryFetcher extends BookFetcher {
    private static final String TAG = "OpenLibraryFetcher";

    @Override
    public void getBookInfo(final Context context, final String isbn, final int mode) {
        mContext = context;
        // https://openlibrary.org/api/books?bibkeys=ISBN:0201558025,LCCN:93005405&format=json
        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl("https://openlibrary.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OL_API api = mRetrofit.create(OL_API.class);
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("bibkeys", "ISBN:" + isbn);
        queryMap.put("jscmd", "data");
        queryMap.put("format", "json");
        Call<Map<String, OpenLibraryJson>> call = api.getOLResult(queryMap);
        call.enqueue(new Callback<Map<String, OpenLibraryJson>>() {
            @Override
            public void onResponse(Call<Map<String, OpenLibraryJson>> call, Response<Map<String, OpenLibraryJson>> response) {
                if (response.body() != null) {
                    Log.i(TAG, "response code = " + response.code());
                    Log.i(TAG, "response body = " + response.body());
                    OpenLibraryJson OLJ = response.body().get("ISBN:" + isbn);
                    if (OLJ != null) {
                        // get information successfully
                        Log.i(TAG, "GET OpenLibrary information successfully, title = " + OLJ.getTitle());
                        mBook = new Book();
                        mBook.setIsbn(isbn);
                        mBook.setTitle(OLJ.getTitle());
                        List<String> authors = new ArrayList<String>();
                        List<OpenLibraryJson.AuthorsBean> authorsBeen = OLJ.getAuthors();
                        for (OpenLibraryJson.AuthorsBean ab : authorsBeen) {
                            authors.add(ab.getName());
                        }
                        mBook.setAuthors(authors);
                        // Open Library books are almost English books, no translators
                        mBook.getWebIds().put("openLibrary", OLJ.getKey());
                        mBook.setPublisher(OLJ.getPublishers().get(0).getName());
                        String rawDate = OLJ.getPublish_date();
                        int pubYear = 9999;
                        if (rawDate.length() > 4) {
                            pubYear = Integer.parseInt(rawDate.substring(rawDate.length() - 4));
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(pubYear, 0, 1); // Open Library seldom returns month
                        mBook.setPubTime(calendar);
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                        boolean addWebsite = pref.getBoolean("settings_pref_acwebsite", true);
                        if (addWebsite) {
                            mBook.setWebsite("https://openlibrary.org" + OLJ.getKey());
                        }
                        final String imageURL;
                        if(OLJ.getCover()!=null){
                            imageURL = OLJ.getCover().getLarge();
                        }else{
                            imageURL = null;
                        }
                        if (mode == 0) {
                            ((SingleAddActivity) mContext).fetchSucceed(mBook, imageURL);
                        } else if (mode == 1) {
                            ((BatchAddActivity) mContext).fetchSucceed(mBook, imageURL);
                        }
                    } else {
                        Log.e(TAG, "Null OpenLibrary Json " + response.code() + ", isbn = " + isbn);
                        if (mode == 0) {
                            ((SingleAddActivity) mContext).fetchFailed(
                                    BookFetcher.fetcherID_OL, 0, isbn
                            );
                        } else if (mode == 1) {
                            ((BatchAddActivity) mContext).fetchFailed(
                                    BookFetcher.fetcherID_OL, 0, isbn);
                        }
                    }
                } else {
                    Log.e(TAG, "Null Response Body, code = " + response.code() + ", isbn = " + isbn);
                    if (mode == 0) {
                        ((SingleAddActivity) mContext).fetchFailed(
                                BookFetcher.fetcherID_OL, 0, isbn
                        );
                    } else if (mode == 1) {
                        ((BatchAddActivity) mContext).fetchFailed(
                                BookFetcher.fetcherID_OL, 0, isbn);
                    }

                }
            }

            @Override
            public void onFailure(Call<Map<String, OpenLibraryJson>> call, Throwable t) {
                Log.w(TAG, "GET OpenLibrary information failed, " + t.toString());
                if (mode == 0) {
                    ((SingleAddActivity) mContext).fetchFailed(
                            BookFetcher.fetcherID_OL, 1, isbn
                    );
                } else if (mode == 1) {
                    ((BatchAddActivity) mContext).fetchFailed(
                            BookFetcher.fetcherID_OL, 1, isbn);
                }

            }
        });
    }

    private interface OL_API {
        @GET("books")
        Call<Map<String, OpenLibraryJson>> getOLResult(@QueryMap Map<String, String> params);
    }

}
