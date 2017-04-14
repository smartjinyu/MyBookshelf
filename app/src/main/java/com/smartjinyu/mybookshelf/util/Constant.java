package com.smartjinyu.mybookshelf.util;

import android.os.Environment;

import com.smartjinyu.mybookshelf.app.BookShelfApp;

import java.io.File;

/**
 * 作者：Neil on 2017/4/14 00:25.
 * 邮箱：cn.neillee@gmail.com
 */

public class Constant {
    //================= PATH ====================

    public static final String PATH_DATA = BookShelfApp.getInstance().getCacheDir().getAbsolutePath() + File.separator + "data";

    public static final String PATH_CACHE = PATH_DATA + "/NetCache";

    public static final String PATH_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "codeest" + File.separator + "GeekNews";
}
