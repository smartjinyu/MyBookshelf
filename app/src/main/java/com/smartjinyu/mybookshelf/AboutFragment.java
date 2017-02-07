package com.smartjinyu.mybookshelf;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * about fragment
 * Created by smartjinyu on 2017/2/5.
 */

public class AboutFragment extends PreferenceFragment {
    private static final String TAG = "AboutFragment";

    private Preference namePreference;
    private Preference donatePreference;
    private Preference feedbackPreference;
    private Preference licensePreference;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_preference);

        namePreference = findPreference("about_pref_name");
        namePreference.setSummary(BuildConfig.VERSION_NAME);

        donatePreference = findPreference("about_pref_donate");
        donatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ClipboardManager clipboardManager =
                        (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                Toast.makeText(
                        getActivity(),
                        getResources().getString(R.string.about_preference_donate_toast),
                        Toast.LENGTH_SHORT)
                        .show();
                ClipData clipData = ClipData.newPlainText(
                        getString(R.string.app_name),
                        "smartjinyu@gmail.com");
                clipboardManager.setPrimaryClip(clipData);
                return true;
            }
        });

        feedbackPreference = findPreference("about_pref_feedback");
        feedbackPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent mail = new Intent(Intent.ACTION_SENDTO);
                mail.setData(Uri.parse("mailto:smartjinyu@gmail.com"));
                mail.putExtra(Intent.EXTRA_SUBJECT,"MyBookshelf Feedback");
                String content=getEmailContent();
                mail.putExtra(Intent.EXTRA_TEXT,content);
                startActivity(mail);
                return true;
            }
        });

        licensePreference = findPreference("about_pref_license");
        licensePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(getString(R.string.about_preference_license_dialog));

                WebView wv = new WebView(getActivity());
                wv.loadUrl("file:///android_asset/license.html");
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);

                        return true;
                    }
                });

                alert.setView(wv);
                alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();

                return true;
            }
        });
    }

    private String getEmailContent(){
        String content="\n\n"+"------------------------"+"\n";
        content+= "Package Name: "+getActivity().getPackageName()+"\n";
        content+= "App Version: "+BuildConfig.VERSION_NAME+"\n";
        content+= "Device Model: "+ Build.MODEL+"\n"+"Device Brand: "+Build.BRAND+"\n"+"SDK Version: "+ Build.VERSION.SDK_INT+"\n"+"------------------------";
        return content;

    }

}
