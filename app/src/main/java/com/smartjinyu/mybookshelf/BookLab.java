package com.smartjinyu.mybookshelf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.smartjinyu.mybookshelf.database.BookBaseHelper;
import com.smartjinyu.mybookshelf.database.BookCursorWrapper;
import com.smartjinyu.mybookshelf.database.BookDBSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private BookCursorWrapper queryBooks(String whereClause,String[] whereArgs){
        Cursor cursor = mDatabase.query(
                BookDBSchema.BookTable.NAME,//TableName
                null,//columns, null select all columns
                whereClause,//where
                whereArgs,//whereArgs
                null,//groupBy
                null,//having
                null//limit
        );
        return new BookCursorWrapper(cursor);

    }

    public Book getBook(UUID id){
        try (BookCursorWrapper cursor = queryBooks(
                BookDBSchema.BookTable.Cols.UUID + "= ?",
                new String[]{id.toString()})
        ){
            if(cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getBook();
        }
    }

    public List<Book> getBooks(){
        List<Book> mBooks = new ArrayList<>();
        BookCursorWrapper cursor = queryBooks(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                mBooks.add(cursor.getBook());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return mBooks;

    }
    public boolean isBookExists(Book book){
        // return whether the book still exists in the database

        try (BookCursorWrapper cursor = queryBooks(
                BookDBSchema.BookTable.Cols.UUID + "= ?",
                new String[]{book.getId().toString()})
        ){
            return cursor.getCount()!=0;
        }

    }


    public void addBook(Book book){
        ContentValues values = getContentValues(book);
        if(isBookExists(book)){
            //book still exists, update it
            mDatabase.update(
                    BookDBSchema.BookTable.NAME,
                    values,
                    BookDBSchema.BookTable.Cols.UUID + "= ?",
                    new String[]{book.getId().toString()}
            );
        }else{
            //add a new book
            mDatabase.insert(BookDBSchema.BookTable.NAME,null,values);
        }
    }



    public void deleteBook(Book book){
        String uuidString = book.getId().toString();
        mDatabase.delete(
                BookDBSchema.BookTable.NAME,
                BookDBSchema.BookTable.Cols.UUID + " = ?",
                new String[]{uuidString}
        );
    }


}
