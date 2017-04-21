package com.smartjinyu.mybookshelf.util;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.util.Map;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

/**
 * 作者：Neil on 2017/4/20 23:19.
 * 邮箱：cn.neillee@gmail.com
 */

public class AnswersUtil {
    public static void init(Context context) {
        Fabric.with(context, new Crashlytics());
    }

    public static void logContentView(String tag, String contentType,
                                      String contentId, Map<String, String> attrs) {
        ContentViewEvent event = new ContentViewEvent()
                .putContentName(tag)
                .putContentType(contentType)
                .putContentId(contentId);
        if (attrs != null) {
            Set<Map.Entry<String, String>> set = attrs.entrySet();
            for (Map.Entry<String, String> entry : set)
                event.putCustomAttribute(entry.getKey(), entry.getValue());
        }
        Answers.getInstance().logContentView(event);
    }

    public static void logContentView(String tag, String contentType,
                                      String contentId, String attrKey, String attrValue) {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put(attrKey, attrValue);
        logContentView(tag, contentType, contentId, map);
    }
}
