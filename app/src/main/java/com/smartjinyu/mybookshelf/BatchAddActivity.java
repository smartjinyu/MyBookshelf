package com.smartjinyu.mybookshelf;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Batch add books activity
 * Created by smartjinyu on 2017/2/8.
 */

public class BatchAddActivity extends AppCompatActivity {
    private static final String TAG = "BatchAddActivity";
    private static final int CAMERA_PERMISSION = 1;

    FragmentPagerAdapter adapter;

    public static List<Book> mBooks;// books added

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_add);

        mBooks = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }


        ViewPager viewPager = (ViewPager) findViewById(R.id.batch_add_view_pager);
        adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.batch_add_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.batch_add_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_close);
        mToolbar.setNavigationContentDescription(R.string.batch_add_navigation_close);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // // TODO: 2017/2/8
                finish();
            }
        });
    }



    public class PagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"TAB 0","TAB 1"};

        public PagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public int getCount(){
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position){
            switch (position){
                case 0:
                    return new BatchScanFragment();
                case 1:
                    return new BatchListFragment();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position){
            return tabTitles[position];
        }
    }




    public void fetchSucceed(final Book mBook,final String imageURL){
        mBooks.add(mBook);
//        Handler mHandler = new Handler(Looper.getMainLooper());
//
//        mHandler.post(new Runnable() {//on the main thread
//            @Override
//            public void run() {
//                Intent i = new Intent(BatchAddActivity.this,BookEditActivity.class);
//                i.putExtra(BookEditActivity.BOOK,mBook);
//                i.putExtra(BookEditActivity.downloadCover,true);
//                i.putExtra(BookEditActivity.imageURL,imageURL);
//                startActivity(i);
//                finish();
//            }
//        });

    }

    public void fetchFailed(int fetcherID,int event,String isbn){
        /**
         * event = 0, unexpected response code
         * event = 1, request failed
         */

        if(fetcherID == BookFetcher.fetcherID_DB){
            if(event == 0){
                event0Dialog(isbn);
            }else if(event == 1){
                event1Dialog(isbn);
            }

        }
    }
    private void event0Dialog(final String isbn){
        String dialogContent = String.format(getResources().getString(
                R.string.isbn_unmatched_dialog_batch_content),isbn);
        new MaterialDialog.Builder(this)
                .title(R.string.isbn_unmatched_dialog_title)
                .content(dialogContent)
                .positiveText(R.string.isbn_unmatched_dialog_negative)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .negativeText(android.R.string.cancel)
                .show();

    }

    private void event1Dialog(final String isbn){
        String dialogContent = String.format(getResources().getString(
                R.string.request_failed_dialog_batch_content),isbn);
        new MaterialDialog.Builder(this)
                .title(R.string.isbn_unmatched_dialog_title)
                .content(dialogContent)
                .positiveText(R.string.isbn_unmatched_dialog_negative)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .negativeText(android.R.string.cancel)
                .show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],@NonNull int[] results){
        switch (requestCode){
            case CAMERA_PERMISSION:
                if(!(results.length>0 && results[0] == PackageManager.PERMISSION_GRANTED)){
                    Toast.makeText(this,R.string.camera_permission_denied,Toast.LENGTH_LONG).show();
                    Log.e(TAG,"Camera Permission Denied");
                    finish();
                }
        }
    }



}

