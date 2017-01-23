package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
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
    protected Handler mHandler;

    protected abstract void getBookInfo(Context context,String isbn);
}
