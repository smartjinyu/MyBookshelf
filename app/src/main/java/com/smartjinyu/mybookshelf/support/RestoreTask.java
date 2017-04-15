package com.smartjinyu.mybookshelf.support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.model.BookShelfLab;
import com.smartjinyu.mybookshelf.model.LabelLab;
import com.smartjinyu.mybookshelf.model.database.BookBaseHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * String - path of backup zip
 */
public class RestoreTask extends AsyncTask<String, Void, Boolean> {
    private MaterialDialog mDialog;
    private final static String TAG = "RestoreTask";
    private Context mContext;

    public RestoreTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mDialog = new MaterialDialog.Builder(mContext)
                .title(R.string.restore_progress_dialog_title)
                .content(R.string.backup_progress_dialog_content)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String backupFile = params[0];
        String unZipDir = backupFile.substring(0, backupFile.lastIndexOf("."));
        try {
            unzip(backupFile, unZipDir);
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
                    unzip(unZipDir + "/Covers.zip", unZipDir + "/Covers");
                    File srcCoverFolder = new File(unZipDir + "/Covers");
                    File[] covers = srcCoverFolder.listFiles();
                    if (mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) != null) {
                        String destCoverFolder = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
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
                String dest = mContext.getFilesDir().getParent() + "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml";
                copyFile(new File(src), new File(dest));
                continue;
            }

            if (file.equals(LabelLab.PreferenceName + ".xml")) {
                String src = unZipDir + "/" + LabelLab.PreferenceName + ".xml";
                String dest = mContext.getFilesDir().getParent() + "/shared_prefs/" + LabelLab.PreferenceName + ".xml";
                copyFile(new File(src), new File(dest));
                continue;
            }

            if (file.equals(mContext.getPackageName() + "_preferences.xml")) {
                String src = unZipDir + "/" + mContext.getPackageName() + "_preferences.xml";
                String dest = mContext.getFilesDir().getParent() + "/shared_prefs/" + mContext.getPackageName() + "_preferences.xml";
                copyFile(new File(src), new File(dest));
                continue;
            }

            if (file.equals(BookBaseHelper.DATABASE_NAME)) {
                String src = unZipDir + "/" + BookBaseHelper.DATABASE_NAME;
                String dest = mContext.getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath();
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
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(TAG)
                .putContentType("Restore")
                .putContentId("2010")
                .putCustomAttribute("Restore Result = ", isSucceed.toString()));
        if (isSucceed) {
            Toast.makeText(mContext, mContext.getString(R.string.restore_succeed_toast), Toast.LENGTH_LONG).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    restartApp();
                }
            }, 2000);
            Log.i(TAG, "Restore successfully!");
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.restore_fail_toast), Toast.LENGTH_LONG).show();
        }
    }

    //zip and unzip code is referenced to http://stackoverflow.com/questions/7485114/how-to-zip-and-unzip-the-files
    private void unzip(String zipFile, String location) throws IOException {
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
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
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
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    private void restartApp() {
        Intent i = ((Activity)mContext).getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(((Activity)mContext).getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(i);
        Runtime.getRuntime().exit(0);
    }
}