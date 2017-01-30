package com.smartjinyu.mybookshelf;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Calendar;
import java.util.HashMap;

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


    @Override
    public void getBookInfo(final Context context, final String isbn){
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
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
                    //mBook.setId(Long.parseLong(response.body().getId(),10));
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

                    if(mBook.getWebIds() == null){
                        mBook.setWebIds(new HashMap<String, String>());
                    }
                    mBook.getWebIds().put("douban",response.body().getId());
                    mBook.setAddTime(Calendar.getInstance());

                    mBook.setPublisher(response.body().getPublisher());

                    String rawDate = response.body().getPubdate();
                    Log.i(TAG,"Date raw = " + rawDate);
                    String[] date = rawDate.split("-");
                    String year = date[0];
                    // rawDate sometimes is "2016-11", sometimes is "2000-10-1", sometimes is "2010-1"
                    String month = date[1];
                    Log.i(TAG,"Get PubDate Year = " + year + ", month = " + month);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Integer.parseInt(year),Integer.parseInt(month)-1,1);
                    mBook.setPubTime(calendar);
                    final String imageURL = response.body().getImages().getLarge();
                    mHandler.post(new Runnable() {//on the main thread
                        @Override
                        public void run() {
                            Intent i = new Intent(mContext,BookEditActivity.class);
                            i.putExtra(BookEditActivity.BOOK,mBook);
                            i.putExtra(BookEditActivity.downloadCover,true);
                            i.putExtra(BookEditActivity.imageURL,imageURL);
                            mContext.startActivity(i);
                            ((Activity)mContext).finish();
                        }
                    });
                }else{
                    Log.w(TAG,"Unexpected response code " + response.code() + ", isbn = " + isbn);
                    String dialogCotent = String.format(mContext.getResources().getString(
                            R.string.isbn_unmatched_dialog_content),isbn);
                    MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                            .title(R.string.isbn_unmatched_dialog_title)
                            .content(dialogCotent)
                            .positiveText(R.string.isbn_unmatched_dialog_positive)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    //create a book only with isbn
                                    mBook = new Book();
                                    mBook.setIsbn(isbn);
                                    mBook.setAddTime(Calendar.getInstance());
                                    Intent i = new Intent(mContext,BookEditActivity.class);
                                    i.putExtra(BookEditActivity.BOOK,mBook);
                                    i.putExtra(BookEditActivity.downloadCover,false);
                                    mContext.startActivity(i);
                                    ((Activity)mContext).finish();

                                }
                            })
                            .negativeText(R.string.isbn_unmatched_dialog_negative)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ((SingleAddActivity)mContext).resumeCamera();
                                }
                            })
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    ((SingleAddActivity)mContext).resumeCamera();
                                }
                            })
                            .show();
                }

            }

            @Override
            public void onFailure(Call<DouBanJson> call, Throwable t) {
                Log.w(TAG,"GET Douban information failed, " + t.toString());
                String dialogCotent = String.format(mContext.getResources().getString(
                        R.string.request_failed_dialog_content),isbn);
                MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                        .title(R.string.isbn_unmatched_dialog_title)
                        .content(dialogCotent)
                        .positiveText(R.string.isbn_unmatched_dialog_positive)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                //create a book only with isbn
                                mBook = new Book();
                                mBook.setIsbn(isbn);
                                mBook.setAddTime(Calendar.getInstance());
                                Intent i = new Intent(mContext,BookEditActivity.class);
                                i.putExtra(BookEditActivity.BOOK,mBook);
                                i.putExtra(BookEditActivity.downloadCover,false);
                                mContext.startActivity(i);
                                ((Activity)mContext).finish();

                            }
                        })
                        .negativeText(R.string.isbn_unmatched_dialog_negative)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ((SingleAddActivity)mContext).resumeCamera();
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                ((SingleAddActivity)mContext).resumeCamera();
                            }
                        })
                        .show();
            }
        });




    }

    private interface DB_API{
        @GET ("isbn/{isbn}")
        Call<DouBanJson> getDBResult(@Path("isbn") String isbn);
    }

}






