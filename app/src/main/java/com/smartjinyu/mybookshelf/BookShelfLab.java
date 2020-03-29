package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Created by smartjinyu on 2017/1/23.
 * Manage all bookshelves
 * a singleton class
 */

public class BookShelfLab {
    private static final String TAG = "BookShelfLab";
    public static final String PreferenceName = "bookshelf";

    private static BookShelfLab sBookShelfLab;
    private SharedPreferences BookShelfPreference;
    private Context mContext;
    private List<BookShelf> sBookShelf;

    public static BookShelfLab get(Context context) {
        if (sBookShelfLab == null) {
            sBookShelfLab = new BookShelfLab(context);
        }
        return sBookShelfLab;
    }

    public static BookShelfLab get() {
        return sBookShelfLab;
    }

    public BookShelfLab(Context context) {
        mContext = context.getApplicationContext();
        BookShelfPreference = mContext.getSharedPreferences(PreferenceName, 0);
        loadBookShelf();
    }

    private void loadBookShelf() {
        sBookShelf = new ArrayList<>();
        Type type = new TypeToken<List<BookShelf>>() {
        }.getType();
        Gson gson = new Gson();
        String toLoad = BookShelfPreference.getString(PreferenceName, null);
        if (toLoad != null) {
            sBookShelf = gson.fromJson(toLoad, type);
            Log.i(TAG, "JSON to Load = " + toLoad);
        } else {
            BookShelf bookShelf = new BookShelf(
                    UUID.fromString(
                            mContext.getResources().getString(R.string.default_book_shelf_uuid))
            );
            bookShelf.setTitle(mContext.getResources().getString(R.string.default_book_shelf_name));
            sBookShelf.add(bookShelf);
            saveBookShelf();
        }
        refreshBookCnt();
    }

    public final void refreshBookCnt(){
        // refresh # of books in each bookshelf
        // this function calculates the real # of books in each bookshelf stored in db
        // currently we only need the number to show on spinner, use calculateBookCnt() instead
        if(false){
            // disabled currently
            // the number displayed at spinner is calculated based on calculateBookCnt()
            // the actual book cnt is properly maintained if this function is enabled
            BookLab bookLab = BookLab.get(mContext);
            for(BookShelf bookShelf : sBookShelf){
                //if(bookShelf.getCnt() == 0){
                // recalculate in every load, prevent data inconsistency
                bookShelf.setCnt(bookLab.getBooks(bookShelf.getId(), null).size());
                //}
            }
        }
    }

    public final void calculateBookCnt(List<Book> books){
        // calculate book cnt in each bookshelf from books
        if(books != null){
            for(BookShelf bookShelf : sBookShelf){
                bookShelf.setCnt(0);
            }
            Map<UUID, Integer> bookCnt = new HashMap<>();
            // Map.putIfAbsent needs API >= N
            for(Book book : books){
                if(book.getBookshelfID() != null){
                    bookCnt.put(book.getBookshelfID(), 0);
                }
            }
            for(Book book : books){
                if(book.getBookshelfID() != null){
                    bookCnt.put(book.getBookshelfID(), bookCnt.get(book.getBookshelfID()) + 1);
                }
            }
            for(Map.Entry<UUID, Integer> entry : bookCnt.entrySet()){
                BookShelf shelf = getBookShelf(entry.getKey());
                if(shelf != null){
                    shelf.setCnt(entry.getValue());
                }
            }
        }
    }

    public final List<BookShelf> getBookShelves() {
        refreshBookCnt();
        return new ArrayList<>(sBookShelf);
    }

    public final BookShelf getBookShelf(UUID id) {
        for (BookShelf bookShelf : sBookShelf) {
            if (bookShelf.getId().equals(id)) {
                return bookShelf;
            }
        }
        return null;
    }


    public void addBookShelf(BookShelf bookShelf) {
        sBookShelf.add(bookShelf);
        saveBookShelf();
    }

    private void saveBookShelf() {
        Gson gson = new Gson();
        String toSave = gson.toJson(sBookShelf);
        Log.i(TAG, "JSON to Save = " + toSave);
        BookShelfPreference.edit()
                .putString(PreferenceName, toSave)
                .apply();
    }

    public void renameBookShelf(UUID id, String newName) {
        for (BookShelf bookShelf : sBookShelf) {
            if (bookShelf.getId().equals(id)) {
                bookShelf.setTitle(newName);
                break;
            }
        }
        saveBookShelf();
    }

    /***
     * delete bookshelf
     * @param id the uuid of the bookshelf to delete
     * @param removeFromBooks whether move books on this bookshelf to default bookshelf
     */
    public void deleteBookShelf(UUID id, boolean removeFromBooks) {
        if (removeFromBooks) {
            // move to default bookshelf
            List<Book> books = BookLab.get(mContext).getBooks(id, null);
            for (Book book : books) {
                book.setBookshelfID(UUID.fromString(mContext.getString(R.string.default_book_shelf_uuid)));
            }
            BookLab.get(mContext).updateBooks(books);
        }

        for (BookShelf bookShelf : sBookShelf) {
            if (bookShelf.getId().equals(id)) {
                sBookShelf.remove(bookShelf);
                break;
            }
        }
        saveBookShelf();
    }

}
