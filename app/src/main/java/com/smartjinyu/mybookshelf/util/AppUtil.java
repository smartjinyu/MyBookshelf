package com.smartjinyu.mybookshelf.util;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * 作者：Neil on 2017/4/12 14:02.
 * 邮箱：cn.neillee@gmail.com
 */

public class AppUtil {

    private static final String TAG = AppUtil.class.getSimpleName();

    public static boolean saveImgToDisk(ResponseBody responseBody, String path) {
        try {
            Log.d(TAG, "Begin to save cover to external storage");
            InputStream inputStream = null;
            OutputStream outputStream = null;

            inputStream = responseBody.byteStream();
            outputStream = new FileOutputStream(path);


            try {
                int c;
                while ((c = inputStream.read()) != -1) {
                    outputStream.write(c);
                }
            } catch (IOException ioe) {
                Log.e(TAG, "IOException, " + ioe.toString());
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                outputStream.close();

            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found exception, " + e.toString());
            return false;
        } catch (IOException ioe) {
            Log.e(TAG, "IOException, " + ioe.toString());
            return false;
        }
        Log.i(TAG, "Save image successfully.");
        return true;
    }
}
