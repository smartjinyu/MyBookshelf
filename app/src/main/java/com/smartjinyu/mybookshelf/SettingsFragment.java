package com.smartjinyu.mybookshelf;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.appcenter.analytics.Analytics;
import com.opencsv.CSVWriter;
import com.smartjinyu.mybookshelf.database.BookBaseHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * settings fragment
 * Created by smartjinyu on 2017/2/8.
 */

public class SettingsFragment extends PreferenceFragment {
    private static final String TAG = "SettingsFragment";

    private static final int CREATE_BACKUP_FILE_CODE = 6;
    private static final int OPEN_BACKUP_FILE_CODE = 7;
    private static final int EXPORT_CSV_FILE_CODE = 8;


    private Preference backupPreference;
    private Preference restorePreference;
    private Preference webServicesPreference;
    private Preference exportCSVPreference;

    private SharedPreferences sharedPreferences;

    private List<Integer> exportCSVList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preference);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setBackupCategory();
        setWebServicesPreference();
        setExportCSVPreference();
    }

    private void setExportCSVPreference() {
        exportCSVPreference = findPreference("settings_pref_export_to_csv");
        exportCSVPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Map<String, String> logEvents = new HashMap<>();
                logEvents.put("Export CSV", "Click Export to csv");
                Analytics.trackEvent(TAG, logEvents);

                exportToCSV();
                return false;
            }
        });
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
                                    Toast.makeText(getActivity(), R.string.export_csv_dialog_at_least_toast,
                                            Toast.LENGTH_SHORT).show();
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
                                        if (listDialog.getSelectedIndices() != null) {
                                            exportCSVList = Arrays.asList(listDialog.getSelectedIndices());
                                        }
                                        String filename = "Bookshelf_CSV_" + BuildConfig.VERSION_CODE + "_"
                                                + Calendar.getInstance().getTimeInMillis() + ".csv";
                                        Intent backupFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                        backupFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                        backupFileIntent.setType("text/csv");
                                        backupFileIntent.putExtra(Intent.EXTRA_TITLE, filename);

                                        if (backupFileIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                            startActivityForResult(backupFileIntent, EXPORT_CSV_FILE_CODE);
                                        } else {
                                            Log.e(TAG, "No Document Provider Available");
                                            Map<String, String> logEvents = new HashMap<>();
                                            logEvents.put("Export CSV", "No Document Provider Available");
                                            Analytics.trackEvent(TAG, logEvents);

                                            Toast.makeText(getActivity(), R.string.settings_no_document_provider_toast, Toast.LENGTH_LONG)
                                                    .show();
                                        }

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
                                })
                                .show();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .show();
    }


    private void setWebServicesPreference() {
        webServicesPreference = findPreference("settings_pref_web_services");
        String rawWS = sharedPreferences.getString("webServices", null);
        final Integer[] initialSelected;
        if (rawWS != null) {
            Type type = new TypeToken<Integer[]>() {
            }.getType();
            Gson gson = new Gson();
            initialSelected = gson.fromJson(rawWS, type);
        } else {
            initialSelected = new Integer[]{0, 1}; //two webServices currently
        }

        webServicesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.settings_web_services_title)
                        .items(R.array.settings_web_services_entries)
                        .positiveText(android.R.string.ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Gson gson = new Gson();
                                String toSave = gson.toJson(dialog.getSelectedIndices());
                                sharedPreferences.edit().putString("webServices", toSave).apply();
                                setWebServicesPreference(); // refresh initial selected list
                            }
                        })
                        .alwaysCallMultiChoiceCallback()
                        .itemsCallbackMultiChoice(initialSelected, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                boolean allowSelectionChange = which.length >= 1;
                                if (!allowSelectionChange) {
                                    Toast.makeText(getActivity(), R.string.settings_web_services_min_toast, Toast.LENGTH_SHORT)
                                            .show();
                                }
                                return allowSelectionChange;
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .show();

                return false;
            }
        });

    }

    private void setBackupCategory() {
        backupPreference = findPreference("settings_pref_backup");
        backupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Map<String, String> logEvents = new HashMap<>();
                logEvents.put("Backup", "Click Backup");

                String filename = "Bookshelf_backup_" + BuildConfig.VERSION_CODE + "_"
                        + Calendar.getInstance().getTimeInMillis() + ".zip";
                Intent backupFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                backupFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                backupFileIntent.setType("application/zip");
                backupFileIntent.putExtra(Intent.EXTRA_TITLE, filename);
                if (backupFileIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(backupFileIntent, CREATE_BACKUP_FILE_CODE);
                } else {
                    Log.e(TAG, "No Document Provider Available");
                    logEvents.put("Backup", "No Document Provider Available");

                    Toast.makeText(getActivity(), R.string.settings_no_document_provider_toast, Toast.LENGTH_LONG)
                            .show();
                }
                Analytics.trackEvent(TAG, logEvents);
                return false;
            }
        });

        restorePreference = findPreference("settings_pref_restore");
        restorePreference.setOnPreferenceClickListener(preference -> {
            Map<String, String> logEvents = new HashMap<>();
            logEvents.put("Restore", "Click Restore");
            Analytics.trackEvent(TAG, logEvents);

            Intent restoreFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            restoreFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            restoreFileIntent.setType("application/zip");
            // restoreFileIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri); // requires >= API 26
            if (restoreFileIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(restoreFileIntent, OPEN_BACKUP_FILE_CODE);
            } else {
                Log.e(TAG, "No Document Provider Available");
                logEvents.put("Restore", "No Document Provider Available");

                Toast.makeText(getActivity(), R.string.settings_no_document_provider_toast, Toast.LENGTH_LONG)
                        .show();
            }
            Analytics.trackEvent(TAG, logEvents);
            return false;
        });

    }

    /**
     * Uri - uri to the backup file, get from storage access framework
     */
    private class backupTask extends AsyncTask<Uri, Void, Boolean> {
        private MaterialDialog mDialog;
        private Uri zipFileUri;

        @Override
        protected void onPreExecute() {
            mDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.backup_progress_dialog_title)
                    .content(R.string.backup_progress_dialog_content)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .canceledOnTouchOutside(false)
                    .show();
        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            zipFileUri = params[0];
            List<String> fileName = new ArrayList<>();
            File covers = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String coverZipFileName = getActivity().getCacheDir() + "/Covers.zip"; // backupLocationPreference.getSummary() + "/Covers.zip";
            if (covers != null) {
                try {
                    zipFiles(covers.listFiles(), Uri.fromFile(new File(coverZipFileName)));
                    fileName.add(coverZipFileName);
                    Log.i(TAG, "Cover temp zip created " + coverZipFileName);
                } catch (IOException e) {
                    Log.e(TAG, "IOException when zipCovers = " + e.toString());
                    return false;
                }
                fileName.add(getActivity().getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath());
                fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml");
                fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + LabelLab.PreferenceName + ".xml");
                fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + getActivity().getPackageName() + "_preferences.xml");
                try {
                    zipFiles(fileName.toArray(new String[0]), zipFileUri);
                    Log.i(TAG, "Backup created " + zipFileUri);
                } catch (IOException e) {
                    Log.e(TAG, "IOException when zipFiles = " + e.toString());
                    return false;
                }
                File coverZipFile = new File(coverZipFileName);
                boolean deleted = coverZipFile.delete();
                Log.i(TAG, "Cover.zip name = " + coverZipFileName + ", delete result = " + deleted);

                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isSucceed) {
            mDialog.dismiss();
            Map<String, String> logEvents = new HashMap<>();
            logEvents.put("Backup", "Backup Result = " + isSucceed.toString());
            Analytics.trackEvent(TAG, logEvents);

            if (isSucceed) {
                String content = getString(R.string.backup_succeed_toast);
                Toast.makeText(getActivity(), content, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.backup_fail_toast), Toast.LENGTH_LONG).show();
            }
        }

        //zip and unzip code is referenced to http://stackoverflow.com/questions/7485114/how-to-zip-and-unzip-the-files
        private void zipFiles(File[] files, Uri zipFileUri) throws IOException {
            if (files.length != 0) {
                int BUFFER_SIZE = 2048;
                try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(getActivity().getContentResolver().openOutputStream(zipFileUri)))) {
                    byte data[] = new byte[BUFFER_SIZE]; // buffer size
                    for (File file : files) {
                        if (file.exists()) {
                            FileInputStream fi = new FileInputStream(file);
                            try (BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE)) {
                                String fileName = file.getAbsolutePath();
                                ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
                                out.putNextEntry(entry);
                                int count;
                                while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                                    out.write(data, 0, count);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void zipFiles(String[] fileNames, Uri zipFileUri) throws IOException {
            File[] files = new File[fileNames.length];
            for (int i = 0; i < fileNames.length; i++) {
                files[i] = new File(fileNames[i]);
            }
            zipFiles(files, zipFileUri);
        }


    }

    /**
     * Uri - uri to the backup file, get from storage access framework
     */
    private class restoreTask extends AsyncTask<Uri, Void, Boolean> {
        private MaterialDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.restore_progress_dialog_title)
                    .content(R.string.backup_progress_dialog_content)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .canceledOnTouchOutside(false)
                    .show();

        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            Uri backupFileUri = params[0];
            String unZipDir = getActivity().getCacheDir() + "/restoreTemp";
            try {
                unzip(backupFileUri, unZipDir);
            } catch (IOException e) {
                Log.e(TAG, "Unzip failed 1, ioe = " + e.toString());
                return false;
            }
            File unzipDirectory = new File(unZipDir);
            String[] fileName = unzipDirectory.list();
            for (String file : fileName) {
                // restore files
                if (file.equals("Covers.zip")) {
                    try {
                        unzip(Uri.fromFile(new File(unZipDir + "/Covers.zip")), unZipDir + "/Covers");
                        File srcCoverFolder = new File(unZipDir + "/Covers");
                        File[] covers = srcCoverFolder.listFiles();
                        if (getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) != null) {
                            String destCoverFolder = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                            File destCover = new File(destCoverFolder);
                            destCover.delete();
                            for (File cover : covers) {
                                copyFile(cover, new File(destCoverFolder + "/" + cover.getName()));
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Unzip failed 2, ioe = " + e.toString());
                        return false;
                    }
                    continue;
                }

                if (file.equals(BookShelfLab.PreferenceName + ".xml")) {
                    String src = unZipDir + "/" + BookShelfLab.PreferenceName + ".xml";
                    String dest = getActivity().getFilesDir().getParent() + "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml";
                    copyFile(new File(src), new File(dest));
                    continue;
                }

                if (file.equals(LabelLab.PreferenceName + ".xml")) {
                    String src = unZipDir + "/" + LabelLab.PreferenceName + ".xml";
                    String dest = getActivity().getFilesDir().getParent() + "/shared_prefs/" + LabelLab.PreferenceName + ".xml";
                    copyFile(new File(src), new File(dest));
                    continue;
                }

                if (file.equals(getActivity().getPackageName() + "_preferences.xml")) {
                    String src = unZipDir + "/" + getActivity().getPackageName() + "_preferences.xml";
                    String dest = getActivity().getFilesDir().getParent() + "/shared_prefs/" + getActivity().getPackageName() + "_preferences.xml";
                    copyFile(new File(src), new File(dest));
                    continue;
                }

                if (file.equals(BookBaseHelper.DATABASE_NAME)) {
                    String src = unZipDir + "/" + BookBaseHelper.DATABASE_NAME;
                    String dest = getActivity().getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath();
                    File tempFile = new File(dest + "-shm");
                    if (tempFile.exists()) tempFile.delete();
                    tempFile = new File(dest + "-wal");
                    if (tempFile.exists()) tempFile.delete();
                    copyFile(new File(src), new File(dest));
                }
            }

            if (unzipDirectory.exists()) {
                deleteRecursive(unzipDirectory);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSucceed) {
            mDialog.dismiss();
            Map<String, String> logEvents = new HashMap<>();
            logEvents.put("Restore", "Restore Result = " + isSucceed.toString());
            Analytics.trackEvent(TAG, logEvents);

            if (isSucceed) {
                Toast.makeText(getActivity(), getString(R.string.restore_succeed_toast), Toast.LENGTH_LONG).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restartApp();
                    }
                }, 2000);
                Log.i(TAG, "Restore successfully!");
            } else {
                Toast.makeText(getActivity(), getString(R.string.restore_fail_toast), Toast.LENGTH_LONG).show();
            }
        }

        //zip and unzip code is referenced to http://stackoverflow.com/questions/7485114/how-to-zip-and-unzip-the-files
        private void unzip(Uri zipFileUri, String location) throws IOException {
            int size;
            int BUFFER_SIZE = 2048;
            byte[] buffer = new byte[BUFFER_SIZE];
            if (!location.endsWith("/")) {
                location += "/";
            }
            File file = new File(location);
            if (!file.isDirectory()) {
                boolean result = file.mkdirs();
            }
            try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(getActivity().getContentResolver().openInputStream(zipFileUri)))) {
                ZipEntry ze = null;
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    String path = location + ze.getName();
                    File unzipFile = new File(path);
                    if (ze.isDirectory()) {
                        if (!unzipFile.isDirectory()) {
                            boolean result = unzipFile.mkdirs();
                        }
                    } else {
                        // check and create parent
                        File parentDir = unzipFile.getParentFile();
                        if (parentDir != null) {
                            if (!parentDir.isDirectory()) {
                                boolean result = parentDir.mkdirs();
                            }
                        }

                        //unzip the file
                        FileOutputStream out = new FileOutputStream(unzipFile, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                        try {
                            while ((size = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                                fout.write(buffer, 0, size);
                            }
                            zipInputStream.closeEntry();
                        } finally {
                            fout.flush();
                            fout.close();
                        }
                    }
                }
            }

        }

        // http://stackoverflow.com/questions/9292954/how-to-make-a-copy-of-a-file-in-android
        private boolean copyFile(File src, File dst) {
            if (!dst.getParentFile().exists()) {
                dst.getParentFile().mkdirs();
            }
            if (dst.exists()) {
                dst.delete();
            }

            try (FileInputStream inStream = new FileInputStream(src);
                 FileOutputStream outStream = new FileOutputStream(dst)) {

                FileChannel inChannel = inStream.getChannel();
                FileChannel outChannel = outStream.getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
                Log.i(TAG, "Copy file succeed" + ",src = " + src.getAbsolutePath() + ",dest = " + dst.getAbsolutePath());
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Copy file IOException = " + e.toString() + ",src = " + src.getAbsolutePath() + ",dest = " + dst.getAbsolutePath());
                return false;
            }

        }

        //delete folder
        void deleteRecursive(File fileOrDirectory) {
            try {
                if (fileOrDirectory.isDirectory()) {
                    for (File child : fileOrDirectory.listFiles()) {
                        deleteRecursive(child);
                    }
                }
                fileOrDirectory.delete();
            } catch (Exception e) {
                Log.e(TAG, "Exception when deleting restore unzip folder: " + e);
            }
        }


    }

    /**
     * Uri - uri to the CSV file, get from storage access framework
     */
    private class exportCSVTask extends AsyncTask<Uri, Void, Boolean> {
        private MaterialDialog mDialog;
        private Uri csvFileUri;

        @Override
        protected void onPreExecute() {
            mDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.export_progress_dialog_title)
                    .content(R.string.export_progress_dialog_content)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .canceledOnTouchOutside(false)
                    .show();
        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            Calendar mCalendar = Calendar.getInstance();
            csvFileUri = params[0];
            try (OutputStream outputStream = getActivity().getContentResolver().openOutputStream(csvFileUri)) {
                List<Book> mBooks = BookLab.get(getActivity()).getBooks();
                // sort Books
                int sortMethod = sharedPreferences.getInt("SORT_METHOD", 0);
                Comparator<Book> comparator;
                switch (sortMethod) {
                    case 0:
                        comparator = new Book.titleComparator();
                        break;
                    case 1:
                        comparator = new Book.authorComparator();
                        break;
                    case 2:
                        comparator = new Book.publisherComparator();
                        break;
                    case 3:
                        comparator = new Book.pubtimeComparator();
                        break;
                    default:
                        comparator = new Book.titleComparator();
                }
                Collections.sort(mBooks, comparator);

                int[] items = new int[11];
                if (exportCSVList != null) {
                    for (int i : exportCSVList) {
                        items[i] = 1;
                    }
                }
                // items is like [1,0,0,0,0,0,1,1,1,1,1], if one item needs to export, item[i] is 1
                outputStream.write(0xef);
                outputStream.write(0xbb);
                outputStream.write(0xbf);
                // use utf-8 with BOM to avoid messy code in Chinese while using MS Excel
                CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));
                // write the title bar
                List<String> titleBar = new ArrayList<>();
                titleBar.add(getString(R.string.export_csv_file_order));
                for (int i = 0; i < 11; i++) {
                    if (items[i] == 1) {
                        titleBar.add(getResources().getStringArray(R.array.export_csv_dialog_list)[i]);
                    }
                }
                csvWriter.writeNext(titleBar.toArray(new String[0]));

                for (int i = 1; i <= mBooks.size(); i++) {
                    List<String> entry = new ArrayList<>();
                    Book mBook = mBooks.get(i - 1);
                    entry.add(Integer.toString(i));
                    if (items[0] == 1) {
                        // title
                        entry.add(mBook.getTitle());
                    }
                    if (items[1] == 1) {
                        // authors
                        String authors = mBook.getFormatAuthor();
                        if (authors != null) {
                            entry.add(authors);
                        } else {
                            entry.add("");
                        }
                    }
                    if (items[2] == 1) {
                        // translators
                        String translators = mBook.getFormatTranslator();
                        if (translators != null) {
                            entry.add(translators);
                        } else {
                            entry.add("");
                        }
                    }
                    if (items[3] == 1) {
                        // publisher
                        entry.add(mBook.getPublisher());
                    }
                    if (items[4] == 1) {
                        // PubTime
                        Calendar calendar = mBook.getPubTime();
                        int year = calendar.get(Calendar.YEAR);
                        if (year == 9999) {
                            entry.add("");
                        } else {
                            int month = calendar.get(Calendar.MONTH) + 1;
                            String pubTime = year +
                                    " - " +
                                    month;
                            entry.add(pubTime);
                        }
                    }
                    if (items[5] == 1) {
                        // ISBN
                        entry.add(mBook.getIsbn());
                    }
                    if (items[6] == 1) {
                        // readingStatus
                        String[] readingStatus = getResources().getStringArray(R.array.reading_status_array);
                        entry.add(readingStatus[mBook.getReadingStatus()]);
                    }
                    if (items[7] == 1) {
                        // bookshelf
                        BookShelf bookShelf = BookShelfLab.get(getActivity()).getBookShelf(mBook.getBookshelfID());
                        entry.add(bookShelf.getTitle());
                    }
                    if (items[8] == 1) {
                        // labels
                        List<UUID> labelID = mBook.getLabelID();
                        if (labelID.size() != 0) {
                            StringBuilder labelsTitle = new StringBuilder();
                            for (UUID id : labelID) {
                                labelsTitle.append(LabelLab.get(getActivity()).getLabel(id).getTitle());
                                labelsTitle.append(",");
                            }
                            labelsTitle.deleteCharAt(labelsTitle.length() - 1);
                            entry.add(labelsTitle.toString());
                        } else {
                            entry.add("");
                        }
                    }
                    if (items[9] == 1) {
                        // notes
                        entry.add(mBook.getNotes());
                    }
                    if (items[10] == 1) {
                        // website
                        entry.add(mBook.getWebsite());
                    }
                    csvWriter.writeNext(entry.toArray(new String[0]));
                }
                csvWriter.writeNext(new String[]{""});
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEE z");
                String exportTime = format.format(mCalendar.getTime());
                String exportTimeString = String.format(getString(R.string.export_csv_file_time), exportTime);
                csvWriter.writeNext(new String[]{exportTimeString});
                csvWriter.writeNext(new String[]{getString(R.string.export_csv_file_end)});
                csvWriter.writeNext(new String[]{getString(R.string.export_csv_file_copyright)});
                csvWriter.close();

            } catch (Exception e) {
                Log.e(TAG, "csvFileUri = " + csvFileUri + ", exception = " + e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSucceed) {
            Map<String, String> logEvents = new HashMap<>();
            logEvents.put("Export CSV", "Export Result = " + isSucceed.toString());
            Analytics.trackEvent(TAG, logEvents);

            mDialog.dismiss();
            if (isSucceed) {
                String toastText = getString(R.string.export_csv_export_succeed_toast);
                Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.export_csv_export_fail_toast), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void restartApp() {
        Intent i = getActivity().getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        Runtime.getRuntime().exit(0);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == CREATE_BACKUP_FILE_CODE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "Create backup file, uri = " + intent.getData());
            new backupTask().execute(intent.getData());
        }
        if (requestCode == OPEN_BACKUP_FILE_CODE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "Restore backup file, uri = " + intent.getData());
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.restore_confirm_dialog_title)
                    .content(R.string.restore_confirm_dialog_content)
                    .positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Map<String, String> logEvents = new HashMap<>();
                            logEvents.put("Restore", "Confirm Restore");
                            Analytics.trackEvent(TAG, logEvents);
                            new restoreTask().execute(intent.getData());
                        }
                    })
                    .negativeText(android.R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Map<String, String> logEvents = new HashMap<>();
                            logEvents.put("Restore", "Give up Restore");
                            Analytics.trackEvent(TAG, logEvents);
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        if (requestCode == EXPORT_CSV_FILE_CODE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "Export CSV file, uri = " + intent.getData());
            new exportCSVTask().execute(intent.getData());
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }
}
