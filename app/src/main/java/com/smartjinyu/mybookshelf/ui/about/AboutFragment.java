package com.smartjinyu.mybookshelf.ui.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.smartjinyu.mybookshelf.BuildConfig;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.util.AlertUtil;
import com.smartjinyu.mybookshelf.util.AppUtil;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;

/**
 * about fragment
 * Created by smartjinyu on 2017/2/5.
 */

public class AboutFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {
    private static final String TAG = "AboutFragment";

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_preference);

        mContext = getActivity();
        initAllPreferences();
    }

    private void initAllPreferences() {
        Preference namePreference = findPreference("about_pref_name");
        Preference donatePreference = findPreference("about_pref_donate");
        Preference feedbackPreference = findPreference("about_pref_feedback");
        Preference licensePreference = findPreference("about_pref_license");
        Preference termOfServicePreference = findPreference("about_pref_term_of_service");

//        namePreference.setOnPreferenceClickListener(this);
        donatePreference.setOnPreferenceClickListener(this);
        feedbackPreference.setOnPreferenceClickListener(this);
        licensePreference.setOnPreferenceClickListener(this);
        termOfServicePreference.setOnPreferenceClickListener(this);

        namePreference.setSummary(BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
//            case "about_pref_name":
//                break;
            case "about_pref_donate":
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(TAG)
                        .putContentType("Donate")
                        .putContentId("2020")
                        .putCustomAttribute("Donate Clicked", "Donate Clicked"));
                boolean hasInstalledAlipayClient = AlipayZeroSdk.hasInstalledAlipayClient(getActivity());
                if (hasInstalledAlipayClient) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.about_preference_donate_title)
                            .content(R.string.about_donate_dialog_content)
                            .positiveText(R.string.about_donate_dialog_positive0)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    AlipayZeroSdk.startAlipayClient(getActivity(), getString(R.string.about_donate_alipay_qrcode));
                                    Answers.getInstance().logContentView(new ContentViewEvent()
                                            .putContentName(TAG)
                                            .putContentType("Donate")
                                            .putContentId("2021")
                                            .putCustomAttribute("Alipay Clicked", "Alipay Clicked"));
                                    dialog.dismiss();
                                }
                            }).negativeText(R.string.about_donate_dialog_negative0)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    AppUtil.copyText2Clipboard(mContext, "smartjinyu@gmail.com");
                                    Toast.makeText(mContext, getResources().getString(R.string.about_preference_donate_toast), Toast.LENGTH_SHORT).show();
                                    Answers.getInstance().logContentView(new ContentViewEvent()
                                            .putContentName(TAG)
                                            .putContentType("Donate")
                                            .putContentId("2022")
                                            .putCustomAttribute("Copy to clipboard Clicked", "Copy to clipboard Clicked"));
                                    dialog.dismiss();
                                }
                            })
                            .neutralText(android.R.string.cancel)
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Answers.getInstance().logContentView(new ContentViewEvent()
                                            .putContentName(TAG)
                                            .putContentType("Donate")
                                            .putContentId("2023")
                                            .putCustomAttribute("Cancel Clicked", "Cancel Clicked"));
                                    dialog.dismiss();
                                }
                            }).show();
                } else {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.about_preference_rate_title)
                            .content(R.string.about_donate_dialog_content)
                            .positiveText(R.string.about_donate_dialog_negative0)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    AppUtil.copyText2Clipboard(mContext, "smartjinyu@gmail.com");
                                    Toast.makeText(mContext, getResources().getString(R.string.about_preference_donate_toast), Toast.LENGTH_SHORT).show();
                                    Answers.getInstance().logContentView(new ContentViewEvent()
                                            .putContentName(TAG)
                                            .putContentType("Donate")
                                            .putContentId("2022")
                                            .putCustomAttribute("Copy to clipboard Clicked", "Copy to clipboard Clicked"));
                                    dialog.dismiss();
                                }
                            })
                            .negativeText(android.R.string.cancel)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Answers.getInstance().logContentView(new ContentViewEvent()
                                            .putContentName(TAG)
                                            .putContentType("Donate")
                                            .putContentId("2023")
                                            .putCustomAttribute("Cancel Clicked", "Cancel Clicked"));

                                    dialog.dismiss();
                                }
                            }).show();
                }
                break;
            case "about_pref_feedback":
                Intent mail = new Intent(Intent.ACTION_SENDTO);
                mail.setData(Uri.parse("mailto:smartjinyu@gmail.com"));
                mail.putExtra(Intent.EXTRA_SUBJECT, "MyBookshelf Feedback");
                String content = getEmailContent();
                mail.putExtra(Intent.EXTRA_TEXT, content);
                startActivity(mail);
                break;
            case "about_pref_license":
                AlertUtil.alertWebview(mContext, getString(R.string.about_preference_license_dialog), "license.html");
                break;
            case "about_pref_term_of_service":
                AlertUtil.alertWebview(mContext, getString(R.string.about_preference_term_of_service), "termOfService.html");
                break;
        }
        return false;
    }

    private String getEmailContent() {
        String content = "\n\n" + "------------------------" + "\n";
        content += "Package Name: " + getActivity().getPackageName() + "\n";
        content += "App Version: " + BuildConfig.VERSION_NAME + "\n";
        content += "App Version Code: " + BuildConfig.VERSION_CODE + "\n";
        content += "Device Model: " + Build.MODEL + "\n" + "Device Brand: " + Build.BRAND + "\n" + "SDK Version: " + Build.VERSION.SDK_INT + "\n" + "------------------------";
        return content;
    }
}
