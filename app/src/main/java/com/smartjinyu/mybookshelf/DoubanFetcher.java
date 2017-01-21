package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by smartjinyu on 2017/1/20.
 * This class is used to get information from website like DouBan
 */

public class DoubanFetcher extends BookFetcher{
    private static final String TAG = "DoubanFetcher";

    private Book mBook;

    @Override
    public void getBookInfo(final Context context, final String isbn){
        mContext = context;
        Retrofit mRetrofit;
        mRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.douban.com/v2/book/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        DB_API api = mRetrofit.create(DB_API.class);
        //create an instance of douban api
        Call<DouBanJson> call = api.getDBResult(isbn);

        call.enqueue(new Callback<DouBanJson>() {
            @Override
            public void onResponse(Call<DouBanJson> call, Response<DouBanJson> response) {
                if(response.code() == 200) {
                    Log.i(TAG, "GET Douban information successfully, id = " + response.body().getId()
                            +", title = " + response.body().getTitle());
                    mBook = new Book();
                    mBook.setTitle(response.body().getTitle());
                    mBook.setId(Long.parseLong(response.body().getId(),10));
                    mBook.setIsbn(isbn);
                    if(response.body().getAuthor().size()!=0){
                        mBook.setAuthors(response.body().getAuthor());
                    }else{
                        mBook.setAuthors(null);
                    }
                    if(response.body().getTranslator().size()!=0){
                        mBook.setTranslators(response.body().getTranslator());
                    }else{
                        mBook.setTranslators(null);
                    }
                    mBook.setPublisher(response.body().getPublisher());
                    DateFormat df = new SimpleDateFormat("yyyy-MM");
                    Date pubDate = new Date();
                    try {
                        pubDate = df.parse(response.body().getPubdate());
                    }catch (ParseException pe){
                        Log.e(TAG,"Parse Date Exception"+pe);
                    }
                    mBook.setPubtime(pubDate);

                    String imageURL = response.body().getImages().getLarge();
                    getAndSaveImg(imageURL,mBook.getId());
                    //TODO
                }else{
                    Log.w(TAG,"Unexpected response code " + response.code() + ", isbn = " + isbn);
                    //TODO
                }

            }

            @Override
            public void onFailure(Call<DouBanJson> call, Throwable t) {
                Log.w(TAG,"GET Douban information failed, " + t.toString());
                //TODO
            }
        });




    }

    private interface DB_API{
        @GET ("isbn/{isbn}")
        Call<DouBanJson> getDBResult(@Path("isbn") String isbn);
    }

}






