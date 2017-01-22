package com.smartjinyu.mybookshelf.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.smartjinyu.mybookshelf.Book;

/**
 * Created by smartjinyu on 2017/1/22.
 * BookBaseHelper
 */

public class BookBaseHelper extends SQLiteOpenHelper {
    private static int VERSION = 1;
    private static final String DATABASE_NAME = "bookList.db";

    public BookBaseHelper(Context context){super(context,DATABASE_NAME,null,VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + BookDBSchema.BookTable.NAME + "(" +
                "_id integer primary key autoincrement," +
                BookDBSchema.BookTable.Cols.UUID + ","+
                BookDBSchema.BookTable.Cols.TITLE + ","+
                BookDBSchema.BookTable.Cols.AUTHORS + ","+
                BookDBSchema.BookTable.Cols.TRANSLATORS + ","+
                BookDBSchema.BookTable.Cols.WEBIDS+ ","+
                BookDBSchema.BookTable.Cols.PUBLISHER + ","+
                BookDBSchema.BookTable.Cols.PUBTIME + ","+
                BookDBSchema.BookTable.Cols.ADDTIME + ","+
                BookDBSchema.BookTable.Cols.ISBN + ","+
                BookDBSchema.BookTable.Cols.hasCover +")"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }
}
