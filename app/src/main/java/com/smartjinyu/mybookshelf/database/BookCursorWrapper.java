package com.smartjinyu.mybookshelf.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.smartjinyu.mybookshelf.Book;

/**
 * Created by smartjinyu on 2017/1/22.
 */

public class BookCursorWrapper extends CursorWrapper {
    public BookCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Book getBook(){
        //todo
        return null;
    }
}
