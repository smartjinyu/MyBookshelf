package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by smartjinyu on 2017/1/19.
 * This activity is to edit book details.
 */

public class BookEditActivity extends AppCompatActivity{
    private static String TAG = "BookEditActivity";
    private static String mode ="startMode";

    public static Intent newIntent(Context context,int startMode){
        /** startMode is a number of 0,1,2
         * 0: start without a camera
         * 1: start with camera in single book mode
         * 2: start with camera in batch mode
         */
        Intent intent = new Intent(context,BookEditActivity.class);
        intent.putExtra(mode,startMode);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        int startMode = i.getIntExtra(mode,0);
        switch(startMode){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            default:
                Log.e(TAG,"Activity start with wrong startmode "+startMode);
                break;
        }

    }

}
