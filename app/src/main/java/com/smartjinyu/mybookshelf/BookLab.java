package com.smartjinyu.mybookshelf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.smartjinyu.mybookshelf.database.BookBaseHelper;
import com.smartjinyu.mybookshelf.database.BookCursorWrapper;
import com.smartjinyu.mybookshelf.database.BookDBSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static BookLab get(Context context) {
        if (sBookLab == null) {
            sBookLab = new BookLab(context);
        }
        return sBookLab;
    }

    public BookLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new BookBaseHelper(context).getWritableDatabase();
    }


    private static ContentValues getContentValues(Book book) {
        ContentValues values = new ContentValues();
        values.put(BookDBSchema.BookTable.Cols.TITLE, book.getTitle());
        values.put(BookDBSchema.BookTable.Cols.UUID, book.getId().toString());
        //authors
        Gson gson = new Gson();
        String authors = gson.toJson(book.getAuthors());
        values.put(BookDBSchema.BookTable.Cols.AUTHORS, authors);
        //translators
        String translators = gson.toJson(book.getTranslators());
        values.put(BookDBSchema.BookTable.Cols.TRANSLATORS, translators);
        //webIds
        String webIds = gson.toJson(book.getWebIds());
        values.put(BookDBSchema.BookTable.Cols.WEBIDS, webIds);
        //
        values.put(BookDBSchema.BookTable.Cols.PUBLISHER, book.getPublisher());
        values.put(BookDBSchema.BookTable.Cols.PUB_TIME, book.getPubTime().getTimeInMillis());
        values.put(BookDBSchema.BookTable.Cols.ADD_TIME, book.getAddTime().getTimeInMillis());
        values.put(BookDBSchema.BookTable.Cols.ISBN, book.getIsbn());
        values.put(BookDBSchema.BookTable.Cols.HAS_COVER, book.isHasCover());
        values.put(BookDBSchema.BookTable.Cols.READING_STATUS, book.getReadingStatus());
        values.put(BookDBSchema.BookTable.Cols.BOOKSHELF_ID, book.getBookshelfID().toString());
        values.put(BookDBSchema.BookTable.Cols.NOTES, book.getNotes());
        values.put(BookDBSchema.BookTable.Cols.WEBSITE, book.getWebsite());
        //label id
        String labelID = gson.toJson(book.getLabelID());
        values.put(BookDBSchema.BookTable.Cols.LABEL_ID, labelID);
        return values;
    }

    private BookCursorWrapper queryBooks(String whereClause, String[] whereArgs) {

        Cursor cursor = mDatabase.query(
                BookDBSchema.BookTable.NAME,//TableName
                null,//columns, null select all columns
                whereClause,//where
                whereArgs,//whereArgs
                null,//groupBy
                null,//having
                null//limit
        );
        // for log
        if (whereArgs == null) {
            whereArgs = new String[]{"null"};
        }
        if (whereClause == null) {
            whereClause = "null";
        }
        Log.i(TAG, "Query books whereClause = " + whereClause + ", whereArgs = " + Arrays.toString(whereArgs));

        return new BookCursorWrapper(cursor);

    }

    public Book getBook(UUID id) {
        try (BookCursorWrapper cursor = queryBooks(
                BookDBSchema.BookTable.Cols.UUID + "= ?",
                new String[]{id.toString()})
        ) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getBook();
        }
    }

    public List<Book> getBooks() {
        List<Book> mBooks = new ArrayList<>();

        try (BookCursorWrapper cursor = queryBooks(null, null)
        ) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mBooks.add(cursor.getBook());
                cursor.moveToNext();
            }
        }
        return mBooks;

    }

    /**
     * getBooks by bookshelfID and labelID, all the parameters can be null
     *
     * @param bookShelfID
     * @param labelID
     * @return result
     */
    public List<Book> getBooks(@Nullable UUID bookShelfID, @Nullable UUID labelID) {
        List<Book> mBooks = new ArrayList<>();
        String whereClause;
        String[] whereArgs;
        if (bookShelfID == null && labelID == null) {
            return getBooks();
        } else if (bookShelfID == null) {
            // bookShelfID == null and labelID != null
            whereClause = BookDBSchema.BookTable.Cols.LABEL_ID + " GLOB ?";
            whereArgs = new String[]{"*" + labelID.toString() + "*"};
            // It is WRONG to write ... + "GLOB *?*",new String[](labelID.toString())
        } else if (labelID == null) {
            // bookShelfID != null and labelID == null
            whereClause = BookDBSchema.BookTable.Cols.BOOKSHELF_ID + "= ?";
            whereArgs = new String[]{bookShelfID.toString()};
        } else {
            // bookShelfID != null and labelID != null
            whereClause = BookDBSchema.BookTable.Cols.BOOKSHELF_ID + "= ? AND "
                    + BookDBSchema.BookTable.Cols.LABEL_ID + " GLOB ?";
            whereArgs = new String[]{bookShelfID.toString(), "*" + labelID.toString() + "*"};
        }

        try (BookCursorWrapper cursor
                     = queryBooks(whereClause, whereArgs)
        ) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mBooks.add(cursor.getBook());
                cursor.moveToNext();
            }
        }
        return mBooks;
    }

    /**
     * Search books only supports search on bookshelf currently,
     * keyword will try to match title,authors,translators,publishers,note (case-insensitive)
     *
     * @param keyword     search keyword
     * @param bookshelfID bookshelf id, pass null if on all bookshelves
     * @return search result
     */
    public List<Book> searchBook(String keyword, @Nullable UUID bookshelfID) {
        List<Book> books = new ArrayList<>();
        String whereClause;
        String[] whereArgs;
        if (keyword == null) {
            return getBooks(bookshelfID, null);
        }
        // in sql, "GLOB" is case-sensitive while "LIKE" is case-insensitive
        if (bookshelfID == null) {
            // on all bookshelves
            whereClause = BookDBSchema.BookTable.Cols.TITLE + " LIKE ? OR "
                    + BookDBSchema.BookTable.Cols.AUTHORS + " LIKE ? OR "
                    + BookDBSchema.BookTable.Cols.TRANSLATORS + " LIKE ? OR "
                    + BookDBSchema.BookTable.Cols.PUBLISHER + " LIKE ? OR "
                    + BookDBSchema.BookTable.Cols.NOTES + " LIKE ? ";
            whereArgs = new String[]{"%" + keyword + "%",
                    "%" + keyword + "%",
                    "%" + keyword + "%",
                    "%" + keyword + "%",
                    "%" + keyword + "%"};
        } else {
            // on specified bookshelf
            whereClause = BookDBSchema.BookTable.Cols.BOOKSHELF_ID + " = ? AND "
                    + BookDBSchema.BookTable.Cols.TITLE + " LIKE ? OR "
                    + BookDBSchema.BookTable.Cols.AUTHORS + " LIKE ? OR "
                    + BookDBSchema.BookTable.Cols.TRANSLATORS + " LIKE ? OR "
                    + BookDBSchema.BookTable.Cols.PUBLISHER + " LIKE ? OR "
                    + BookDBSchema.BookTable.Cols.NOTES + " LIKE ? ";
            whereArgs = new String[]{bookshelfID.toString(),
                    "%" + keyword + "%",
                    "%" + keyword + "%",
                    "%" + keyword + "%",
                    "%" + keyword + "%",
                    "%" + keyword + "%"};
        }

        try (BookCursorWrapper cursor
                     = queryBooks(whereClause, whereArgs)
        ) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                books.add(cursor.getBook());
                cursor.moveToNext();
            }
        }

        return books;

    }


    /**
     * whether this book is in the database (check id, not isbn)
     *
     * @param book the book need to check
     * @return
     */
    public boolean isBookExists(Book book) {
        try (BookCursorWrapper cursor = queryBooks(
                BookDBSchema.BookTable.Cols.UUID + "= ?",
                new String[]{book.getId().toString()})
        ) {
            return cursor.getCount() != 0;
        }

    }


    public void addBook(Book book){
        addBook(book, true);
    }

    public void addBook(Book book, boolean refreshBookShelfCnt) {
        ContentValues values = getContentValues(book);
        if (isBookExists(book)) {
            //book still exists, update it
            mDatabase.update(
                    BookDBSchema.BookTable.NAME,
                    values,
                    BookDBSchema.BookTable.Cols.UUID + "= ?",
                    new String[]{book.getId().toString()}
            );
        } else {
            //add a new book
            mDatabase.insert(BookDBSchema.BookTable.NAME, null, values);
        }
        if(refreshBookShelfCnt){
            BookShelfLab.get(mContext).refreshBookCnt();
        }
    }

    public void addBooks(List<Book> books) {
        if (books != null) {
            for (Book book : books) {
                addBook(book, false);
            }
        }
        BookShelfLab.get(mContext).refreshBookCnt();
    }


    public void deleteBook(Book book) {
        deleteBook(book, false);
    }

    public void deleteBook(Book book, boolean deleteCover){
        String uuidString = book.getId().toString();
        if (book.isHasCover() && deleteCover) {
            // delete cover file
            File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + book.getCoverPhotoFileName());
            boolean succeeded = file.delete();
            Log.i(TAG, "Remove cover result = " + succeeded);
        }
        mDatabase.delete(
                BookDBSchema.BookTable.NAME,
                BookDBSchema.BookTable.Cols.UUID + " = ?",
                new String[]{uuidString}
        );
        BookShelfLab.get(mContext).refreshBookCnt();
    }

    public void updateBook(Book book){
        updateBook(book, true);
    }

    public void updateBook(Book book, boolean refreshBookShelfCnt) {
        ContentValues values = getContentValues(book);
        String uuidString = book.getId().toString();
        mDatabase.update(
                BookDBSchema.BookTable.NAME,
                values,
                BookDBSchema.BookTable.Cols.UUID + "= ?",
                new String[]{uuidString}
        );
        if(refreshBookShelfCnt){
            BookShelfLab.get(mContext).refreshBookCnt();
        }
    }

    public void updateBooks(List<Book> books){
        if (books != null) {
            for (Book book : books) {
                updateBook(book, false);
            }
        }
        BookShelfLab.get(mContext).refreshBookCnt();
    }


}
