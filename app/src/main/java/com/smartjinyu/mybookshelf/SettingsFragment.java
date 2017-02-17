package com.smartjinyu.mybookshelf;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.smartjinyu.mybookshelf.database.BookBaseHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * settings fragment
 * Created by smartjinyu on 2017/2/8.
 */

public class SettingsFragment extends PreferenceFragment{
    private static final String TAG = "SettingsFragment";

    private static final int STORAGE_PERMISSION_LOACTION = 1;
    private static final int STORAGE_PERMISSION_BACKUP = 2;
    private static final int STORAGE_PERMISSION_RESTORE = 3;
    private static final int FOLDER_CHOOSER = 4;

    private Preference backupLocationPreference;
    private Preference backupPreference;
    private Preference restorePreference;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preference);

        backupLocationPreference = findPreference("settings_pref_backup_location");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String path = sharedPreferences.getString("settings_pref_backup_location",
                Environment.getExternalStorageDirectory().getAbsolutePath()+"/backups");
        backupLocationPreference.setSummary(path);
        backupLocationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(TAG)
                        .putContentType("Backup Location")
                        .putContentId("2005")
                        .putCustomAttribute("Click Backup Location", 1));

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    FragmentCompat.requestPermissions(SettingsFragment.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_LOACTION);
                    return false;
                } else {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR,true);
                    i.putExtra(FilePickerActivity.EXTRA_MODE,FilePickerActivity.MODE_DIR);
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH,path);
                    startActivityForResult(i,FOLDER_CHOOSER);
                    return true;
                }
            }
        });


        backupPreference = findPreference("settings_pref_backup");
        backupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(TAG)
                        .putContentType("Backup")
                        .putContentId("2006")
                        .putCustomAttribute("Click Backup", 1));

                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    FragmentCompat.requestPermissions(SettingsFragment.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_BACKUP);
                }else{
                    new backupTask().execute();
                }
                return false;
            }
        });

        restorePreference = findPreference("settings_pref_restore");
        restorePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(TAG)
                        .putContentType("Restore")
                        .putContentId("2007")
                        .putCustomAttribute("Click Restore", 1));

                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    FragmentCompat.requestPermissions(SettingsFragment.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_RESTORE);
                }else{
                    restoreBackup();
                }
                return false;
            }
        });

    }

    private void restoreBackup(){
        File backupFolder = new File(backupLocationPreference.getSummary().toString());
        final String[] files = backupFolder.list();
        List<String> backups = new ArrayList<>();
        for(String name: files){
            if (name.contains("Bookshelf_backup_") && name.contains(".zip")){
                long timeInMills = Long.parseLong(name.substring(name.lastIndexOf("_")+1,name.lastIndexOf(".")));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timeInMills);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String time = format.format(calendar.getTime());
                backups.add(name + "(" + time + ")");
            }
        }
        if(backups.size()!=0){
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.restore_dialog_title)
                    .content(R.string.restore_dialog_content)
                    .items(backups)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(final MaterialDialog listDialog, View itemView, int position,final CharSequence text) {
                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.restore_confirm_dialog_title)
                                    .content(R.string.restore_confirm_dialog_content)
                                    .positiveText(android.R.string.ok)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String path = backupLocationPreference.getSummary().toString() + "/"
                                                    + text.toString().substring(0,text.toString().lastIndexOf(".zip")+4);
                                            new restoreTask().execute(path);
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
                                                    + text.toString().substring(0,text.toString().lastIndexOf(".zip")+4);
                                            File file = new File(path);
                                            if(file.exists()){
                                                file.delete();
                                            }
                                            dialogList.getItems().remove(position);
                                            dialogList.notifyItemsChanged();
                                        }
                                    })
                                    .negativeText(android.R.string.cancel)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();

                            return false;
                        }
                    })
                    .autoDismiss(false)
                    .show();
        }else{
            Toast.makeText(getActivity(),getString(R.string.restore_no_backup_toast),Toast.LENGTH_LONG)
                    .show();
        }

    }



    private class backupTask extends AsyncTask<Void,Void,Boolean>{
        private MaterialDialog mDialog;
        private String zipFile;


        @Override
        protected void onPreExecute(){
            mDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.backup_progress_dialog_title)
                    .content(R.string.backup_progress_dialog_content)
                    .progress(true,0)
                    .progressIndeterminateStyle(false)
                    .show();
        }

        @Override
        protected Boolean doInBackground(Void...params){
            List<String> fileName = new ArrayList<>();
            File covers = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String coverZipFileName = backupLocationPreference.getSummary() + "/Covers.zip";
            if(covers!=null){
                try{
                    zipFiles(covers.listFiles(),coverZipFileName);
                    fileName.add(coverZipFileName);
                    Log.i(TAG,"Cover temp zip created " + coverZipFileName);
                }catch (IOException e){
                    Log.e(TAG,"IOException when zipCovers = " + e.toString());
                    return false;
                }
                fileName.add(getActivity().getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath());
                fileName.add(getActivity().getFilesDir().getParent()+ "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml");
                fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + LabelLab.PreferenceName + ".xml");
                fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + getActivity().getPackageName() + "_preferences.xml");
                zipFile = backupLocationPreference.getSummary() + "/Bookshelf_backup_" + BuildConfig.VERSION_CODE + "_" + Calendar.getInstance().getTimeInMillis() + ".zip";
                try{
                    zipFiles(fileName.toArray(new String[0]),zipFile);
                    Log.i(TAG,"Backup created " + zipFile);
                }catch (IOException e){
                    Log.e(TAG,"IOException when zipFiles = " + e.toString());
                    return false;
                }
                File coverZipFile = new File(coverZipFileName);
                boolean deleted = coverZipFile.delete();
                Log.i(TAG,"Cover.zip name = " + coverZipFileName + ", delete result = " + deleted);
                return true;
            }else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isSucceed){
            mDialog.dismiss();
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName(TAG)
                    .putContentType("Backup")
                    .putContentId("2011")
                    .putCustomAttribute("Backup Result = ",isSucceed.toString()));

            if(isSucceed){
                String content = String.format(getString(R.string.backup_succeed_toast),zipFile);
                Toast.makeText(getActivity(),content,Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getActivity(),getString(R.string.backup_fail_toast),Toast.LENGTH_LONG).show();
            }
        }

        //zip and unzip code is referenced to http://stackoverflow.com/questions/7485114/how-to-zip-and-unzip-the-files
        private void zipFiles(String[] files,String zipFile) throws IOException{
            if(files.length!=0) {
                int BUFFER_SIZE = 2048;
                File folder = new File(zipFile.substring(0,zipFile.lastIndexOf("/")));// the folder
                if (!folder.isDirectory()){
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

        private void zipFiles(File[] files,String zipFile) throws IOException{
            if(files.length!=0){
                int BUFFER_SIZE = 2048;
                File folder = new File(zipFile.substring(0,zipFile.lastIndexOf("/")));// the folder
                if (!folder.isDirectory()){
                    boolean result = folder.mkdirs();
                }

                try(ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile))))
                {
                    byte data[] = new byte[BUFFER_SIZE]; // buffer size
                    for(File file: files){
                        if(file.exists()){
                            FileInputStream fi = new FileInputStream(file);
                            try(BufferedInputStream origin = new BufferedInputStream(fi,BUFFER_SIZE)){
                                String fileName = file.getAbsolutePath();
                                ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/")+1));
                                out.putNextEntry(entry);
                                int count;
                                while((count = origin.read(data,0,BUFFER_SIZE))!=-1){
                                    out.write(data,0,count);
                                }
                            }
                        }
                    }
                }
            }
        }



    }

    /**
     * Sting - path of backup zip
     */
    private class restoreTask extends AsyncTask<String,Void,Boolean>{
        private MaterialDialog mDialog;
        @Override
        protected void onPreExecute(){
            mDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.restore_progress_dialog_title)
                    .content(R.string.backup_progress_dialog_content)
                    .progress(true,0)
                    .progressIndeterminateStyle(false)
                    .show();

        }

        @Override
        protected Boolean doInBackground(String...params){
            String backupFile = params[0];
            String unZipDir = backupFile.substring(0,backupFile.lastIndexOf("."));
            try{
                unzip(backupFile,unZipDir);
            }catch (IOException e){
                Log.e(TAG,"Unzip failed 1, ioe = " +e.toString());
                return false;
            }
            File unzipDirectory = new File(unZipDir);
            String[] fileName = unzipDirectory.list();
            for(String file: fileName){
                // restore files
                if(file.equals("Covers.zip")){
                    try {
                        unzip(unZipDir + "/Covers.zip",unZipDir + "/Covers");
                        File srcCoverFolder = new File(unZipDir + "/Covers");
                        File[] covers = srcCoverFolder.listFiles();
                        if(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!=null){
                            String destCoverFolder = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                            File destCover = new File(destCoverFolder);
                            destCover.delete();
                            for(File cover: covers){
                                copyFile(cover,new File(destCoverFolder + "/" + cover.getName()));
                            }
                        }
                    }catch (IOException e){
                        Log.e(TAG,"Unzip failed 2, ioe = " +e.toString());
                        return false;
                    }

                    continue;
                }

                if(file.equals(BookShelfLab.PreferenceName + ".xml")){
                    String src = unZipDir + "/" + BookShelfLab.PreferenceName + ".xml";
                    String dest = getActivity().getFilesDir().getParent()+ "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml";
                    copyFile(new File(src),new File(dest));
                    continue;
                }

                if(file.equals(LabelLab.PreferenceName + ".xml")){
                    String src = unZipDir + "/" + LabelLab.PreferenceName + ".xml";
                    String dest = getActivity().getFilesDir().getParent()+ "/shared_prefs/" + LabelLab.PreferenceName + ".xml";
                    copyFile(new File(src),new File(dest));
                    continue;
                }

                if(file.equals(getActivity().getPackageName() + "_preferences.xml")){
                    String src = unZipDir + "/" + getActivity().getPackageName() + "_preferences.xml";
                    String dest = getActivity().getFilesDir().getParent() +"/shared_prefs/" +getActivity().getPackageName() + "_preferences.xml";
                    copyFile(new File(src),new File(dest));
                    continue;
                }

                if(file.equals(BookBaseHelper.DATABASE_NAME)){
                    String src = unZipDir + "/" + BookBaseHelper.DATABASE_NAME;
                    String dest = getActivity().getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath();
                    copyFile(new File(src),new File(dest));
                }
            }

            if(unzipDirectory.exists()){
                deleteRecursive(unzipDirectory);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSucceed){
            mDialog.dismiss();
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName(TAG)
                    .putContentType("Restore")
                    .putContentId("2010")
                    .putCustomAttribute("Restore Result = ",isSucceed.toString()));
            if(isSucceed){
                Toast.makeText(getActivity(),getString(R.string.restore_succeed_toast),Toast.LENGTH_LONG).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restartApp();
                    }
                },2000);
                Log.i(TAG,"Restore successfully!");
            }else{
                Toast.makeText(getActivity(),getString(R.string.restore_fail_toast),Toast.LENGTH_LONG).show();
            }
        }

        //zip and unzip code is referenced to http://stackoverflow.com/questions/7485114/how-to-zip-and-unzip-the-files
        private void unzip(String zipFile,String location) throws IOException{
            int size;
            int BUFFER_SIZE = 2048;
            byte[] buffer = new byte[BUFFER_SIZE];
            if(!location.endsWith("/")){
                location += "/";
            }
            File file = new File(location);
            if(!file.isDirectory()){
                boolean result = file.mkdirs();
            }
            try(ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))){
                ZipEntry ze = null;
                while((ze = zipInputStream.getNextEntry())!=null){
                    String path = location + ze.getName();
                    File unzipFile = new File(path);
                    if(ze.isDirectory()){
                        if(!unzipFile.isDirectory()){
                            boolean result = unzipFile.mkdirs();
                        }
                    }else{
                        // check and create parent
                        File parentDir = unzipFile.getParentFile();
                        if(parentDir!=null){
                            if(!parentDir.isDirectory()){
                                boolean result = parentDir.mkdirs();
                            }
                        }

                        //unzip the file
                        FileOutputStream out = new FileOutputStream(unzipFile,false);
                        BufferedOutputStream fout = new BufferedOutputStream(out,BUFFER_SIZE);
                        try{
                            while((size = zipInputStream.read(buffer,0,BUFFER_SIZE)) != -1){
                                fout.write(buffer,0,size);
                            }
                            zipInputStream.closeEntry();
                        }finally {
                            fout.flush();
                            fout.close();
                        }
                    }
                }
            }

        }

        // http://stackoverflow.com/questions/9292954/how-to-make-a-copy-of-a-file-in-android
        private boolean copyFile(File src, File dst)  {
            if(!dst.getParentFile().exists()){
                dst.getParentFile().mkdirs();
            }
            if(dst.exists()){
                dst.delete();
            }

            try(FileInputStream inStream = new FileInputStream(src);
                FileOutputStream outStream = new FileOutputStream(dst)) {

                FileChannel inChannel = inStream.getChannel();
                FileChannel outChannel = outStream.getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
                Log.i(TAG,"Copy file succeed" + ",src = " + src.getAbsolutePath() + ",dest = " + dst.getAbsolutePath());
                return true;
            }catch (IOException e){
                Log.e(TAG,"Copy file IOException = " + e.toString() + ",src = " + src.getAbsolutePath() + ",dest = " + dst.getAbsolutePath());
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


    }

    private void restartApp(){
        Intent i = getActivity().getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getActivity().getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

        Runtime.getRuntime().exit(0);

    }






    @Override
    public void onRequestPermissionsResult(
            int RequestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults){

        switch (RequestCode) {
            case STORAGE_PERMISSION_LOACTION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission not granted
                    Toast.makeText(getActivity(), getString(R.string.storage_permission_toast1), Toast.LENGTH_SHORT)
                            .show();
                    Log.e(TAG, "Storage Permission Denied 1");
                }
                break;

            case STORAGE_PERMISSION_BACKUP:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission not granted
                    Toast.makeText(getActivity(), getString(R.string.storage_permission_toast2), Toast.LENGTH_SHORT)
                            .show();
                    Log.e(TAG, "Storage Permission Denied 2");
                } else {
                    new backupTask().execute();
                }
                break;
            case STORAGE_PERMISSION_RESTORE:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission not granted
                    Toast.makeText(getActivity(), getString(R.string.storage_permission_toast3), Toast.LENGTH_SHORT)
                            .show();
                    Log.e(TAG, "Storage Permission Denied 3");
                }else{
                    restoreBackup();
                }
                break;


        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent intent){
        if(requestCode == FOLDER_CHOOSER && resultCode == Activity.RESULT_OK){
            Uri uri = intent.getData();
            File file = com.nononsenseapps.filepicker.Utils.getFileForUri(uri);
            if(backupLocationPreference!=null){
                String path = file.getAbsolutePath();
                backupLocationPreference.setSummary(path);
                sharedPreferences.edit().putString("settings_pref_backup_location",path).apply();
                Log.i(TAG,"Change backup path to " + path);
            }
        }
        super.onActivityResult(requestCode,resultCode,intent);
    }
}
