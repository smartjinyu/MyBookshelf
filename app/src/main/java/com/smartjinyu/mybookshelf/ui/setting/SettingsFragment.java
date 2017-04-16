package com.smartjinyu.mybookshelf.ui.setting;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.smartjinyu.mybookshelf.BuildConfig;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.support.BackupTask;
import com.smartjinyu.mybookshelf.support.ExportCSVTask;
import com.smartjinyu.mybookshelf.support.RestoreTask;
import com.smartjinyu.mybookshelf.util.SharedPrefUtil;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * settings fragment
 * Created by smartjinyu on 2017/2/8.
 */

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {
    private static final String TAG = "SettingsFragment";

    private static final int STORAGE_PERMISSION_LOCATION = 1;
    private static final int STORAGE_PERMISSION_BACKUP = 2;
    private static final int STORAGE_PERMISSION_RESTORE = 3;
    private static final int FOLDER_CHOOSER = 4;
    private static final int STORAGE_PERMISSION_EXPORT_CSV = 5;

    private Preference backupLocationPreference;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preference);

        mContext = getActivity();

        initAllPreferences();
    }

    private void initAllPreferences() {
        Preference webServicesPreference = findPreference("settings_pref_web_services");
        backupLocationPreference = findPreference("settings_pref_backup_location");
        Preference exportCSVPreference = findPreference("settings_pref_export_to_csv");
        Preference backupPreference = findPreference("settings_pref_backup");
        Preference restorePreference = findPreference("settings_pref_restore");

        webServicesPreference.setOnPreferenceClickListener(this);
        backupLocationPreference.setOnPreferenceClickListener(this);
        exportCSVPreference.setOnPreferenceClickListener(this);
        backupPreference.setOnPreferenceClickListener(this);
        restorePreference.setOnPreferenceClickListener(this);
        // init backupLocationPreference summary
        String backupLocation = SharedPrefUtil.getInstance().getString(SharedPrefUtil.BACK_LOCATION,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/backups");
        backupLocationPreference.setSummary(backupLocation);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "settings_pref_web_services":
                String rawWS = SharedPrefUtil.getInstance().getString(SharedPrefUtil.WEB_SERVICES_TYPE, null);
                final Integer[] initialSelected;
                if (rawWS != null) {
                    Type type = new TypeToken<Integer[]>() {
                    }.getType();
                    Gson gson = new Gson();
                    initialSelected = gson.fromJson(rawWS, type);
                } else {
                    initialSelected = new Integer[]{0, 1}; //two webServices currently
                }
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.settings_web_services_title)
                        .items(R.array.settings_web_services_entries)
                        .positiveText(android.R.string.ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Gson gson = new Gson();
                                String toSave = gson.toJson(dialog.getSelectedIndices());
                                SharedPrefUtil.getInstance().putString(SharedPrefUtil.WEB_SERVICES_TYPE, toSave);
                                //TODO this
//                                setWebServicesPreference(); // refresh initial selected list
                            }
                        }).alwaysCallMultiChoiceCallback()
                        .itemsCallbackMultiChoice(initialSelected, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                boolean allowSelectionChange = which.length >= 1;
                                if (!allowSelectionChange) {
                                    Toast.makeText(getActivity(), R.string.settings_web_services_min_toast, Toast.LENGTH_SHORT).show();
                                }
                                return allowSelectionChange;
                            }
                        }).canceledOnTouchOutside(false).show();
                break;
            case "settings_pref_export_to_csv":
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(TAG)
                        .putContentType("Export CSV")
                        .putContentId("2030")
                        .putCustomAttribute("Click Export to csv", 1));
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    FragmentCompat.requestPermissions(SettingsFragment.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_EXPORT_CSV);
                } else {
                    exportToCSV();
                }
                break;
            case "settings_pref_backup":
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(TAG)
                        .putContentType("Backup")
                        .putContentId("2006")
                        .putCustomAttribute("Click Backup", 1));

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    FragmentCompat.requestPermissions(SettingsFragment.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_BACKUP);
                } else {
                    String backupLocation = backupLocationPreference.getSummary().toString();
                    new BackupTask(mContext, backupLocation).execute();
                }
                break;
            case "settings_pref_backup_location":
                String path = SharedPrefUtil.getInstance().getString(SharedPrefUtil.BACK_LOCATION,
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/backups");
                backupLocationPreference.setSummary(path);
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(TAG)
                        .putContentType("Backup Location")
                        .putContentId("2005")
                        .putCustomAttribute("Click Backup Location", 1));

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    FragmentCompat.requestPermissions(SettingsFragment.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_LOCATION);
                    return false;
                } else {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                    i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, path);
                    startActivityForResult(i, FOLDER_CHOOSER);
                    return true;
                }
            case "settings_pref_restore":
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(TAG)
                        .putContentType("Restore")
                        .putContentId("2007")
                        .putCustomAttribute("Click Restore", 1));

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    FragmentCompat.requestPermissions(SettingsFragment.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_RESTORE);
                } else {
                    restoreBackup();
                }
                break;
        }
        return false;
    }

    private void exportToCSV() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.export_csv_dialog_title)
                .items(R.array.export_csv_dialog_list)
                .itemsCallbackMultiChoice(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                        new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                if (which.length == 1) {
                                    Toast.makeText(mContext, R.string.export_csv_dialog_at_least_toast, Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                                return true;
                            }
                        })
                .alwaysCallMultiChoiceCallback()
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog listDialog, @NonNull DialogAction which) {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.export_csv_caution_dialog_title)
                                .content(R.string.export_csv_caution_dialog_content)
                                .positiveText(android.R.string.ok)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        String csvName = backupLocationPreference.getSummary() +
                                                "/Bookshelf_CSV_" + BuildConfig.VERSION_CODE + "_"
                                                + Calendar.getInstance().getTimeInMillis() + ".csv";
                                        new ExportCSVTask(mContext, csvName).execute(listDialog.getSelectedIndices());
                                        listDialog.dismiss();
                                    }
                                })
                                .negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                        listDialog.dismiss();
                                    }
                                }).show();
                    }
                }).negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).autoDismiss(false).show();
    }

    private void restoreBackup() {
        File backupFolder = new File(backupLocationPreference.getSummary().toString());
        String[] files = backupFolder.list();
        List<String> backups = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (String name : files) {
            if (name.contains("Bookshelf_backup_") && name.contains(".zip")) {
                long timeInMills = Long.parseLong(name.substring(name.lastIndexOf("_") + 1, name.lastIndexOf(".")));
                calendar.setTimeInMillis(timeInMills);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String time = format.format(calendar.getTime());
                backups.add(name + "(" + time + ")");
            }
        }
        if (backups.size() != 0) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.restore_dialog_title)
                    .content(R.string.restore_dialog_content)
                    .items(backups)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(final MaterialDialog listDialog, View itemView, int position, final CharSequence text) {
                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.restore_confirm_dialog_title)
                                    .content(R.string.restore_confirm_dialog_content)
                                    .positiveText(android.R.string.ok)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String path = backupLocationPreference.getSummary().toString() + "/"
                                                    + text.toString().substring(0, text.toString().lastIndexOf(".zip") + 4);
                                            new RestoreTask(mContext).execute(path);
                                            listDialog.dismiss();
                                        }
                                    }).negativeText(android.R.string.cancel)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                            listDialog.dismiss();
                                        }
                                    }).show();
                        }
                    })
                    .itemsLongCallback(new MaterialDialog.ListLongCallback() {
                        @Override
                        public boolean onLongSelection(final MaterialDialog dialogList, View itemView, final int position, final CharSequence text) {
                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.restore_delete_dialog_title)
                                    .content(R.string.restore_delete_dialog_content)
                                    .positiveText(android.R.string.ok)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String path = backupLocationPreference.getSummary().toString() + "/"
                                                    + text.toString().substring(0, text.toString().lastIndexOf(".zip") + 4);
                                            File file = new File(path);
                                            if (file.exists()) {
                                                file.delete();
                                            }
                                            dialogList.getItems().remove(position);
                                            dialogList.notifyItemsChanged();
                                        }
                                    }).negativeText(android.R.string.cancel)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            return false;
                        }
                    }).autoDismiss(false).show();
        } else {
            Toast.makeText(getActivity(), getString(R.string.restore_no_backup_toast), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int RequestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (RequestCode) {
            case STORAGE_PERMISSION_LOCATION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission not granted
                    Toast.makeText(getActivity(), getString(R.string.storage_permission_toast1), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Storage Permission Denied 1");
                }
                break;
            case STORAGE_PERMISSION_BACKUP:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission not granted
                    Toast.makeText(getActivity(), getString(R.string.storage_permission_toast2), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Storage Permission Denied 2");
                } else {
                    String backupLocation = backupLocationPreference.getSummary().toString();
                    new BackupTask(mContext, backupLocation).execute();
                }
                break;
            case STORAGE_PERMISSION_RESTORE:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission not granted
                    Toast.makeText(getActivity(), getString(R.string.storage_permission_toast3), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Storage Permission Denied 3");
                } else {
                    restoreBackup();
                }
                break;
            case STORAGE_PERMISSION_EXPORT_CSV:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission not granted
                    Toast.makeText(getActivity(), getString(R.string.storage_permission_toast5), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Storage Permission Denied 5");
                } else {
                    exportToCSV();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FOLDER_CHOOSER && resultCode == Activity.RESULT_OK) {
            Uri uri = intent.getData();
            File file = com.nononsenseapps.filepicker.Utils.getFileForUri(uri);
            if (backupLocationPreference != null) {
                String path = file.getAbsolutePath();
                backupLocationPreference.setSummary(path);
                SharedPrefUtil.getInstance().putString(SharedPrefUtil.BACK_LOCATION, path);
                Log.i(TAG, "Change backup path to " + path);
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
}
