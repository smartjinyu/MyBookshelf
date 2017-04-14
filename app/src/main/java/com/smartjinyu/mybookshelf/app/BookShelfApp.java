package com.smartjinyu.mybookshelf.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.smartjinyu.mybookshelf.di.component.AppComponent;
import com.smartjinyu.mybookshelf.di.component.DaggerAppComponent;
import com.smartjinyu.mybookshelf.di.module.AppModule;

import java.util.HashSet;
import java.util.Set;

/**
 * 作者：Neil on 2017/4/13 23:36.
 * 邮箱：cn.neillee@gmail.com
 */

public class BookShelfApp extends Application {
    private static BookShelfApp mInstance;
    public static Context AppContext = null;
    public static AppComponent appComponent;

    private final Set<Activity> allActivities = new HashSet<>();

    public static BookShelfApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        AppContext = getApplicationContext();
    }

    public static AppComponent getAppComponent() {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(mInstance))
                    .build();
        }
        return appComponent;
    }

    public void addActivity(Activity activity) {
        allActivities.add(activity);
    }

    public void removeActivity(Activity activity) {
        allActivities.remove(activity);
    }

    public void exitApp() {
        synchronized (allActivities) {
            for (Activity activity : allActivities) {
                activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
