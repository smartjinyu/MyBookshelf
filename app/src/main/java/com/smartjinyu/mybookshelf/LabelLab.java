package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/24.
 * manage labels here
 */

public class LabelLab {
    private static final String TAG = "LabelLab";
    private static final String PreferenceName = "labels";

    private static LabelLab sLabelLab;
    private SharedPreferences LabelPreference;
    private Context mContext;

    private  List<Label> sLabel;


    public static LabelLab get(Context context){
        if(sLabelLab ==null){
            sLabelLab = new LabelLab(context);
        }
        return sLabelLab;
    }

    public LabelLab(Context context){
        mContext = context.getApplicationContext();
        LabelPreference = mContext.getSharedPreferences(PreferenceName,0);
        loadLabel();
    }

    private void loadLabel(){
        Type type = new TypeToken<List<Label>>(){}.getType();
        Gson gson = new Gson();
        String toLoad = LabelPreference.getString(PreferenceName,null);
        if(toLoad != null){
            sLabel = gson.fromJson(toLoad,type);
            Log.i(TAG,"JSON to Load = " + toLoad);
        }else{
            sLabel = new ArrayList<>();
            //no default label here
        }

    }

    public final List<Label> getLabels(){
        return sLabel;
    }

    public final Label getLabel(UUID id){
        for(Label label :sLabel){
            if(label.getId().equals(id)){
                return label;
            }
        }
        return null;
    }



    public void addLabel(Label label){
        if(sLabel == null ){
            sLabel = new ArrayList<Label>();
        }
        sLabel.add(label);
        saveLabel();
    }

    private void saveLabel(){
        Gson gson = new Gson();
        String toSave = gson.toJson(sLabel);
        Log.i(TAG,"JSON to Save = " + toSave);
        LabelPreference.edit()
                .putString(PreferenceName,toSave)
                .apply();
    }

    public void removeLabel(Label label){
        sLabel.remove(label);
        saveLabel();
    }

}
