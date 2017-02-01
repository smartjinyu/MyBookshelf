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



    public static BookShelfLab get(Context context){
        if(sBookShelfLab==null){
            sBookShelfLab = new BookShelfLab(context);
        }
        return sBookShelfLab;
    }

    public BookShelfLab(Context context){
        mContext = context.getApplicationContext();
        BookShelfPreference = mContext.getSharedPreferences(PreferenceName,0);
    }

    private List<BookShelf> loadBookShelf(){
        List<BookShelf> sBookShelf = new ArrayList<>();
        Type type = new TypeToken<List<BookShelf>>(){}.getType();
        Gson gson = new Gson();
        String toLoad = BookShelfPreference.getString(PreferenceName,null);
        if(toLoad != null){
            sBookShelf = gson.fromJson(toLoad,type);
            Log.i(TAG,"JSON to Load = " + toLoad);
        }else{
            BookShelf bookShelf = new BookShelf(
                    UUID.fromString("407c4479-5a57-4371-8b94-ad038f1276fe"));
            bookShelf.setTitle(mContext.getResources().getString(R.string.default_book_shelf_name));
            sBookShelf.add(bookShelf);
            saveBookShelf(sBookShelf);
        }
        return sBookShelf;
    }

    public final List<BookShelf> getBookShelves(){
        return loadBookShelf();
    }

    public final BookShelf getBookShelf(UUID id){
        List<BookShelf> sBookShelf = loadBookShelf();
        for(BookShelf bookShelf : sBookShelf){
            if(bookShelf.getId().equals(id)){
                return bookShelf;
            }
        }
        return null;
    }



    public void addBookShelf(BookShelf bookShelf){
        List<BookShelf> sBookShelf = new ArrayList<>();
        sBookShelf.add(bookShelf);
        saveBookShelf(sBookShelf);
    }

    private void saveBookShelf(List<BookShelf> sBookShelf){
        Gson gson = new Gson();
        String toSave = gson.toJson(sBookShelf);
        Log.i(TAG,"JSON to Save = " + toSave);
        BookShelfPreference.edit()
                .putString(PreferenceName,toSave)
                .apply();
    }

    public void removeBookShelf(BookShelf bookShelf){
        List<BookShelf> sBookShelf = loadBookShelf();
        sBookShelf.remove(bookShelf);
        saveBookShelf(sBookShelf);
    }


}
