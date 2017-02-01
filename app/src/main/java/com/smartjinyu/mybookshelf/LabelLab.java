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



    public static LabelLab get(Context context){
        if(sLabelLab ==null){
            sLabelLab = new LabelLab(context);
        }
        return sLabelLab;
    }

    public LabelLab(Context context){
        mContext = context.getApplicationContext();
        LabelPreference = mContext.getSharedPreferences(PreferenceName,0);
    }

    private List<Label> loadLabel(){
        List<Label> labels = new ArrayList<>();
        Type type = new TypeToken<List<Label>>(){}.getType();
        Gson gson = new Gson();
        String toLoad = LabelPreference.getString(PreferenceName,null);
        if(toLoad != null){
            labels = gson.fromJson(toLoad,type);
            Log.i(TAG,"JSON to Load = " + toLoad);
        }
        return labels;

    }

    public final List<Label> getLabels(){
        return loadLabel();
    }

    public final Label getLabel(UUID id){
        List<Label> labels = loadLabel();
        for(Label label :labels){
            if(label.getId().equals(id)){
                return label;
            }
        }
        return null;
    }



    public void addLabel(Label label){
        List<Label> sLabel = loadLabel();
        sLabel.add(label);
        saveLabel(sLabel);
    }

    private void saveLabel(List<Label> sLabel){
        Gson gson = new Gson();
        String toSave = gson.toJson(sLabel);
        Log.i(TAG,"JSON to Save = " + toSave);
        LabelPreference.edit()
                .putString(PreferenceName,toSave)
                .apply();
    }

    public void removeLabel(Label label){
        List<Label> sLabel = loadLabel();
        sLabel.remove(label);
        saveLabel(sLabel);
    }

}
