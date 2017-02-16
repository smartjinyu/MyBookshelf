package com.smartjinyu.mybookshelf;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.common.reedsolomon.GenericGF;
import com.smartjinyu.mybookshelf.database.BookBaseHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * settings fragment
 * Created by smartjinyu on 2017/2/8.
 */

public class SettingsFragment extends PreferenceFragment {
    private static final String TAG = "SettingsFragment";

    private static final int STORAGE_PERMISSION = 2;

    private Preference backupLocationPreference;
    private Preference backupPreference;
    private Preference restorePreference;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preference);
        backupLocationPreference = findPreference("settings_pref_backup_location");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String path = sharedPreferences.getString("settings_pref_backup_location",
                Environment.getExternalStorageDirectory().getAbsolutePath()+"/backups");
        backupLocationPreference.setSummary(path);
        backupLocationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // // TODO: 2017/2/15
                return false;
            }
        });


        backupPreference = findPreference("settings_pref_backup");
        backupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    FragmentCompat.requestPermissions(SettingsFragment.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
                }else{
                    backupFiles();
                }
                return false;
            }
        });

        restorePreference = findPreference("settings_pref_restore");

    }
    private void backupFiles(){
        List<String> fileName = new ArrayList<>();
        File covers = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String coverZipFileName = backupLocationPreference.getSummary() + "/Covers.zip";
        if(covers!=null){
            try{
                zipFiles(covers.listFiles(),coverZipFileName);
                fileName.add(coverZipFileName);
                Log.i(TAG,"Cover temp zip created " + coverZipFileName);
                fileName.add(getActivity().getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath());
                fileName.add(getActivity().getFilesDir().getParent()+ "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml");
                fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + LabelLab.PreferenceName + ".xml");
                fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + getActivity().getPackageName() + "_preferences.xml");
                String zipFile = backupLocationPreference.getSummary() + "/Bookshelf_backup_" + Calendar.getInstance().getTimeInMillis() + ".zip";
                try{
                    zipFiles(fileName.toArray(new String[0]),zipFile);
                    Log.i(TAG,"Backup created " + zipFile);
                    Toast.makeText(getActivity(),getString(R.string.backup_succeed_toast),Toast.LENGTH_SHORT).show();
                }catch (IOException e){
                    Log.e(TAG,"IOException when zipFiles = " + e.toString());
                    Toast.makeText(getActivity(),getString(R.string.backup_fail_zip_toast),Toast.LENGTH_SHORT).show();

                }
                File coverZipFile = new File(coverZipFileName);
                boolean deleted = coverZipFile.delete();
                Log.i(TAG,"Cover.zip name = " + coverZipFileName + ", delete result = " + deleted);
            }catch (IOException e){
                Log.e(TAG,"IOException when zipCovers = " + e.toString());
                Toast.makeText(getActivity(),getString(R.string.backup_fail_coverZip_toast),Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity(),getString(R.string.backup_fail_cover_toast),Toast.LENGTH_SHORT).show();
        }
    }

    //zip and unzip code is referenced to http://stackoverflow.com/questions/7485114/how-to-zip-and-unzip-the-files
    private void zipFiles(String[] files,String zipFile) throws IOException{
        int BUFFER_SIZE = 2048;
        try(ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile))))
        {
            byte data[] = new byte[BUFFER_SIZE]; // buffer size
            for(String fileName: files){
                FileInputStream fi = new FileInputStream(fileName);
                try(BufferedInputStream origin = new BufferedInputStream(fi,BUFFER_SIZE)){
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

    private void zipFiles(File[] files,String zipFile) throws IOException{
        int BUFFER_SIZE = 2048;
        try(ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile))))
        {
            byte data[] = new byte[BUFFER_SIZE]; // buffer size
            for(File file: files){
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



    @Override
    public void onRequestPermissionsResult(
            int RequestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults){

        switch (RequestCode){
            case STORAGE_PERMISSION:
                if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    // permission not granted
                    Toast.makeText(getActivity(),getString(R.string.storage_permission_toast),Toast.LENGTH_SHORT)
                            .show();
                    Log.e(TAG,"Storage Permission Denied");
                }else{
                    backupFiles();
                }
        }
    }
}
