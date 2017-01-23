package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by smartjinyu on 2017/1/23.
 * Manage all bookshelves
 * a singleton class
 */

public class BookShelfLab {
    private static final String TAG = "BookShelfLab";
    private static final String PreferenceName = "bookshelf";

    private static BookShelfLab sBookShelfLab;
    private SharedPreferences BookShelfPreference;
    private Context mContext;

    private static List<BookShelf> sBookShelf;


    public static BookShelfLab get(Context context){
        if(sBookShelfLab==null){
            sBookShelfLab = new BookShelfLab(context);
        }
        return sBookShelfLab;
    }

    public BookShelfLab(Context context){
        mContext = context.getApplicationContext();
        BookShelfPreference = mContext.getSharedPreferences(PreferenceName,0);
        loadBookShelf();
    }

    private void loadBookShelf(){
        Type type = new TypeToken<List<BookShelf>>(){}.getType();
        Gson gson = new Gson();
        String toLoad = BookShelfPreference.getString(PreferenceName,null);
        if(toLoad != null){
            sBookShelf = gson.fromJson(toLoad,type);
            Log.i(TAG,"JSON to Load = " + toLoad);
        }else{
            BookShelf bookShelf = new BookShelf();
            bookShelf.setTitle(mContext.getResources().getString(R.string.default_book_shelf_name));
            addBookShelf(bookShelf);
        }
    }

    public List<BookShelf> getBookShelves(){
        return sBookShelf;
    }



    public void addBookShelf(BookShelf bookShelf){
        if(sBookShelf == null ){
            sBookShelf = new ArrayList<BookShelf>();
        }
        sBookShelf.add(bookShelf);
        Gson gson = new Gson();
        String toSave = gson.toJson(sBookShelf);
        Log.i(TAG,"JSON to Save = " + toSave);
        BookShelfPreference.edit()
                .putString(PreferenceName,toSave)
                .apply();
    }


}
