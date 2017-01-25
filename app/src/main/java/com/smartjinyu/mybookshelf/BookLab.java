package com.smartjinyu.mybookshelf;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.smartjinyu.mybookshelf.database.BookBaseHelper;
import com.smartjinyu.mybookshelf.database.BookDBSchema;

/**
 * Created by smartjinyu on 2017/1/25.
 * This class is use to manage books
 */

public class BookLab {
    private static final String TAG = "BookLab";

    private Context mContext;
    private static BookLab sBookLab;
    private SQLiteDatabase mDatabase;

    public static BookLab get(Context context){
        if(sBookLab == null){
            sBookLab = new BookLab(context);
        }
        return sBookLab;
    }

    public BookLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new BookBaseHelper(context).getWritableDatabase();
    }

    private static ContentValues getContentValues(Book book){
        ContentValues values = new ContentValues();

        values.put(BookDBSchema.BookTable.Cols.TITLE,book.getTitle());
        values.put(BookDBSchema.BookTable.Cols.UUID,book.getId().toString());
        //authors
        Gson gson = new Gson();
        String authors = gson.toJson(book.getAuthors());
        values.put(BookDBSchema.BookTable.Cols.AUTHORS,authors);
        //translators
        String translators = gson.toJson(book.getTranslators());
        values.put(BookDBSchema.BookTable.Cols.TRANSLATORS,translators);
        //webIds
        String webIds = gson.toJson(book.getWebIds());
        values.put(BookDBSchema.BookTable.Cols.WEBIDS,webIds);
        //
        values.put(BookDBSchema.BookTable.Cols.PUBLISHER,book.getPublisher());
        values.put(BookDBSchema.BookTable.Cols.PUB_TIME,book.getPubTime().getTimeInMillis());
        values.put(BookDBSchema.BookTable.Cols.ADD_TIME,book.getAddTime().getTimeInMillis());
        values.put(BookDBSchema.BookTable.Cols.ISBN,book.getIsbn());
        values.put(BookDBSchema.BookTable.Cols.HAS_COVER,book.isHasCover());
        values.put(BookDBSchema.BookTable.Cols.READING_STATUS,book.getReadingStatus());
        values.put(BookDBSchema.BookTable.Cols.BOOKSHELF_ID,book.getBookshelfID().toString());
        values.put(BookDBSchema.BookTable.Cols.NOTES,book.getNotes());
        values.put(BookDBSchema.BookTable.Cols.WEBSITE,book.getWebsite());
        //label id
        String labelID = gson.toJson(book.getLabelID());
        values.put(BookDBSchema.BookTable.Cols.LABEL_ID,labelID);

        return values;
    }

}
