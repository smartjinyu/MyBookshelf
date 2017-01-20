package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by smartjinyu on 2017/1/20.
 * The superclass of Online Book information Fetcher
 */

public abstract class BookFetcher {
    private static final String TAG = "BookFetcher";
    Context mContext;

    protected abstract  Book getBookInfo(Context context,String isbn);

    private boolean saveSuccess;

    protected boolean getAndSaveImg(String url){
        saveSuccess = false;
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        downloadImgApi downImgApi = retrofit.create(downloadImgApi.class);
        Call<ResponseBody> call = downImgApi.downloadFileWithDynamicUrlSync(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG,"Get download image response, code = " + response.code());
                saveImgToDisk(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                saveSuccess = false;
                Log.w(TAG,"Fail to download image response," + t.toString());
                Toast.makeText(mContext,"",Toast.LENGTH_LONG).show();
            }
        });


        return saveSuccess;
    }
    private interface downloadImgApi {
        @GET
        Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
    }

    private boolean saveImgToDisk(ResponseBody responseBody){

        return true;
    }

}
