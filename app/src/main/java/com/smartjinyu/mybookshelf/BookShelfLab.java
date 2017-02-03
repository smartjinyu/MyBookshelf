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
                    UUID.fromString(
                            mContext.getResources().getString(R.string.default_book_shelf_uuid))
            );
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
        List<BookShelf> sBookShelf = loadBookShelf();
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

    public void renameBookShelf(UUID id,String newName){
        List<BookShelf> bookShelves = loadBookShelf();
        for(BookShelf bookShelf:bookShelves){
            if(bookShelf.getId().equals(id)){
                bookShelf.setTitle(newName);
                break;
            }
        }
        saveBookShelf(bookShelves);

    }

    /***
     * delete bookshelf
     * @param id the uuid of the bookshelf to delete
     * @param removeFromBooks whether move books on this bookshelf to default bookshelf
     */
    public void deleteBookShelf(UUID id, boolean removeFromBooks){
        List<BookShelf> sBookShelf = loadBookShelf();
        if(removeFromBooks){
            // move to default bookshelf
            List<Book> books = BookLab.get(mContext).getBooks(id,null);
            for(Book book:books){
                book.setBookshelfID(UUID.fromString(mContext.getString(R.string.default_book_shelf_uuid)));
                BookLab.get(mContext).updateBook(book);

            }
        }

        for(BookShelf bookShelf:sBookShelf){
            if(bookShelf.getId().equals(id)){
                sBookShelf.remove(bookShelf);
                break;
            }
        }

        saveBookShelf(sBookShelf);
    }



}
