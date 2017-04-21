package com.smartjinyu.mybookshelf.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

/**
 * 作者：Neil on 2017/4/15 17:53.
 * 邮箱：cn.neillee@gmail.com
 */

public class AlertUtil {
    public static void alertWebview(Context context, String title, String url) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);

        WebView wv = new WebView(context);
        if (AppUtil.getCurrentLocale(context).equals(Locale.CHINA)) {
            wv.loadUrl("file:///android_asset/en/" + url);
        } else {
            wv.loadUrl("file:///android_asset/zh/" + url);
        }
        wv.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                final Uri uri = request.getUrl();
                view.loadUrl(uri.toString());
                return true;
            }
        });

        alert.setView(wv);
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
