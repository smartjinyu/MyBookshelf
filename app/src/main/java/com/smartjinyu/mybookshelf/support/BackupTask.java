package com.smartjinyu.mybookshelf.support;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartjinyu.mybookshelf.BuildConfig;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.model.BookShelfLab;
import com.smartjinyu.mybookshelf.model.LabelLab;
import com.smartjinyu.mybookshelf.model.database.BookBaseHelper;
import com.smartjinyu.mybookshelf.util.AnswersUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupTask extends AsyncTask<Void, Void, Boolean> {
    private MaterialDialog mDialog;
    private String mZipFile;

    private static final String TAG = "BackupTask";

    private Context mContext;

    private String mBackupLocation;

    public BackupTask(Context context, String backupLocation) {
        this.mContext = context;
        this.mBackupLocation = backupLocation;
    }

    @Override
    protected void onPreExecute() {
        mDialog = new MaterialDialog.Builder(mContext)
                .title(R.string.backup_progress_dialog_title)
                .content(R.string.backup_progress_dialog_content)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        List<String> fileName = new ArrayList<>();
        File covers = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String coverZipFileName = mBackupLocation + "/Covers.zip";
        if (covers != null) {
            try {
                zipFiles(covers.listFiles(), coverZipFileName);
                fileName.add(coverZipFileName);
                Log.i(TAG, "Cover temp zip created " + coverZipFileName);
            } catch (IOException e) {
                Log.e(TAG, "IOException when zipCovers = " + e.toString());
                return false;
            }
            fileName.add(mContext.getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath());
            fileName.add(mContext.getFilesDir().getParent() + "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml");
            fileName.add(mContext.getFilesDir().getParent() + "/shared_prefs/" + LabelLab.PreferenceName + ".xml");
            fileName.add(mContext.getFilesDir().getParent() + "/shared_prefs/" + mContext.getPackageName() + "_preferences.xml");
            mZipFile = mBackupLocation + "/Bookshelf_backup_" + BuildConfig.VERSION_CODE + "_"
                    + Calendar.getInstance().getTimeInMillis() + ".zip";
            try {
                zipFiles(fileName.toArray(new String[0]), mZipFile);
                Log.i(TAG, "Backup created " + mZipFile);
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
        AnswersUtil.logContentView(TAG, "Backup", "2011", "Backup Result =", isSucceed.toString());

        if (isSucceed) {
            String content = String.format(mContext.getString(R.string.backup_succeed_toast), mZipFile);
            Toast.makeText(mContext, content, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.backup_fail_toast), Toast.LENGTH_LONG).show();
        }
    }

    //zip and unzip code is referenced to http://stackoverflow.com/questions/7485114/how-to-zip-and-unzip-the-files
    private void zipFiles(String[] files, String zipFile) throws IOException {
        if (files.length != 0) {
            int BUFFER_SIZE = 2048;
            File folder = new File(zipFile.substring(0, zipFile.lastIndexOf("/")));// the folder
            if (!folder.isDirectory()) {
                boolean result = folder.mkdirs();
            }
            try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
                byte data[] = new byte[BUFFER_SIZE]; // buffer size
                for (String fileName : files) {
                    File file = new File(fileName);
                    if (file.exists()) {
                        FileInputStream fi = new FileInputStream(fileName);
                        try (BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE)) {
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

    private void zipFiles(File[] files, String zipFile) throws IOException {
        if (files.length != 0) {
            int BUFFER_SIZE = 2048;
            File folder = new File(zipFile.substring(0, zipFile.lastIndexOf("/")));// the folder
            if (!folder.isDirectory()) {
                boolean result = folder.mkdirs();
            }

            try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
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
}
