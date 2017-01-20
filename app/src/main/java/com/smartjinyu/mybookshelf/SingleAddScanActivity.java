package com.smartjinyu.mybookshelf;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by smartjinyu on 2017/1/20.
 * Scan barcode of a single book
 */

public class SingleAddScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private static String TAG = "SingleAddScanActivity";

    private static final int CAMERA_PERMISSION = 1;


    private static final String FLASH_STATE = "FLASH_STATE";
    private ZXingScannerView mScannerView;
    private boolean mFlash;

    public static Intent newIntent(Context context){
        /** startMode is a number of 0,1,2
         * 0: start without a camera
         * 1: start with camera in single book mode
         * 2: start with camera in batch mode
         */
        Intent intent = new Intent(context,SingleAddScanActivity.class);
        return intent;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }

        if(savedInstanceState != null) {
            mFlash = savedInstanceState.getBoolean(FLASH_STATE, false);
        } else {
            mFlash = false;
        }
        setContentView(R.layout.activity_single_add_scan);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.singleScanToolbar);
        mToolbar.setTitle(R.string.single_scan_toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.singleScanFrame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        mScannerView.startCamera();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuItem menuItem;

        if(mFlash) {
            menuItem = menu.add(Menu.NONE, R.id.menu_simple_add_flash, 0, R.string.menu_single_add_flash_on);
            menuItem.setIcon(R.drawable.ic_flash_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_simple_add_flash, 0, R.string.menu_single_add_flash_off);
            menuItem.setIcon(R.drawable.ic_flash_off);
        }

        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_IF_ROOM);



        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.setFlash(mFlash);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_simple_add_flash:
                mFlash = !mFlash;
                if(mFlash) {
                    item.setTitle(R.string.menu_single_add_flash_on);
                    item.setIcon(R.drawable.ic_flash_on);
                } else {
                    item.setTitle(R.string.menu_single_add_flash_off);
                    item.setIcon(R.drawable.ic_flash_off);}
                mScannerView.setFlash(mFlash);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void handleResult(Result rawResult){
        Toast.makeText(this, "Contents = " + rawResult.getText() +
                ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(SingleAddScanActivity.this);
            }
        }, 2000);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Camera Permission Denied");
                    finish();
                }
        }
    }






}
