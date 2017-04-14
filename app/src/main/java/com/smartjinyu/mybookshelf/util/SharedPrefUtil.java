package com.smartjinyu.mybookshelf.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.smartjinyu.mybookshelf.app.BookShelfApp;

/**
 * 作者：Neil on 2016/4/16 23:50.
 * 邮箱：cn.neillee@gmail.com
 */
public class SharedPrefUtil {
    private static final String XML_NAME = "SharedPrefUtil";
    public static final String CHECK_UPDATE = "settings_pref_check_update";
    public static final String SORT_METHOD = "SORT_METHOD";
    public static final String LAUNCH_TIMES = "launchTimes";
    public static final String MUTE_RATINGS = "muteRatings";
    public static final String IS_RATED = "isRated";
    public static final String ACCEPT_TERM_OF_SERVICE = "isAcceptTermOfService";
    public static final String DONATE_DRAWER_ITEM_SHOW = "isDonateDrawerItemShow";
    // 0: DOUBAN,1: OL,2: ALL
    public static final String WEB_SERVICES_TYPE = "webServices";
    public static final String AC_WEBSITE = "settings_pref_acwebsite";

    private static SharedPrefUtil sInstance;

    private SharedPreferences mPrefs;

    public static SharedPrefUtil getInstance() {
        if (sInstance == null) {
            sInstance = new SharedPrefUtil(BookShelfApp.AppContext);
        }
        return sInstance;
    }

    private SharedPrefUtil(Context context) {
        mPrefs = context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE);
    }

    public SharedPrefUtil putBoolean(String key, boolean value) {
        mPrefs.edit().putBoolean(key, value).apply();
        return this;
    }

    public boolean getBoolean(String key, boolean def) {
        return mPrefs.getBoolean(key, def);
    }

    public SharedPrefUtil putInt(String key, int value) {
        mPrefs.edit().putInt(key, value).apply();
        return this;
    }

    public int getInt(String key, int defValue) {
        return mPrefs.getInt(key, defValue);
    }

    public SharedPrefUtil putString(String key, String value) {
        mPrefs.edit().putString(key, value).apply();
        return this;
    }

    public String getString(String key, String defValue) {
        return mPrefs.getString(key, defValue);
    }

}
