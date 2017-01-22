package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    protected Context mContext;

    protected Book mBook;


    protected abstract void getBookInfo(Context context,String isbn);

    private int result;

    protected void getAndSaveImg(String url){
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://smartjinyu.com/")//no use here, will use dynamic url for request
                .build();
        downloadImgApi downImgApi = retrofit.create(downloadImgApi.class);
        Call<ResponseBody> call = downImgApi.downloadFileWithDynamicUrlSync(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG,"Get download image response, code = " + response.code());
                if(!saveImgToDisk(response.body())){
                    //TODO
                    //REFRESH THE IMAGE
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.w(TAG,"Fail to download image response," + t.toString());
               // Toast.makeText(mContext,"",Toast.LENGTH_LONG).show();
                //todo
            }
        });



    }
    private interface downloadImgApi {
        @GET
        Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
    }

    private boolean saveImgToDisk(ResponseBody responseBody){
        try{
            Log.d(TAG,"Begin to save cover to external storage");
            InputStream inputStream = null;
            OutputStream outputStream = null;

            inputStream = responseBody.byteStream();
            outputStream = new FileOutputStream(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + mBook.getCoverPhotoFileName());
            try {
                int c;
                while ((c = inputStream.read()) != -1) {
                    outputStream.write(c);
                }
            }catch (IOException ioe){
                Log.e(TAG,"IOException, " + ioe.toString());
                return false;
            }finally {
                if(inputStream!=null){
                    inputStream.close();
                }
                outputStream.close();

            }
        }catch(FileNotFoundException e) {
            Log.e(TAG, "File not found exception, " + e.toString());
            return false;
        }catch (IOException ioe){
            Log.e(TAG,"IOException, " + ioe.toString());
            return false;
        }
        Log.i(TAG,"Save image successfully.");
        mBook.setHasCover(true);
        return true;
    }

}
