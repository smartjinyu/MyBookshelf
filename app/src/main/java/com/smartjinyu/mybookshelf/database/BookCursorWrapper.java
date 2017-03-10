package com.smartjinyu.mybookshelf.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartjinyu.mybookshelf.Book;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/22.
 * BookCursor Wrapper
 */

public class BookCursorWrapper extends CursorWrapper {
    public BookCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Book getBook() {
        String title = getString(getColumnIndex(BookDBSchema.BookTable.Cols.TITLE));
        String uuid = getString(getColumnIndex(BookDBSchema.BookTable.Cols.UUID));
        //authors
        String authorsJson = getString(getColumnIndex(BookDBSchema.BookTable.Cols.AUTHORS));
        Type typeStringList = new TypeToken<List<String>>() {
        }.getType();
        Gson gson = new Gson();
        List<String> authors = gson.fromJson(authorsJson, typeStringList);
        //translators
        String translatorsJson = getString(getColumnIndex(BookDBSchema.BookTable.Cols.TRANSLATORS));
        List<String> translators = gson.fromJson(translatorsJson, typeStringList);
        //webIDs
        String webIDJson = getString(getColumnIndex(BookDBSchema.BookTable.Cols.WEBIDS));
        Type typeStrStrMap = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> webIDs = gson.fromJson(webIDJson, typeStrStrMap);
        //
        String publisher = getString(getColumnIndex(BookDBSchema.BookTable.Cols.PUBLISHER));
        //pubTime
        Long pubTimeMills = getLong(getColumnIndex(BookDBSchema.BookTable.Cols.PUB_TIME));
        Calendar pubTime = Calendar.getInstance();
        pubTime.setTimeInMillis(pubTimeMills);
        //addTime
        Long addTimeMills = getLong(getColumnIndex(BookDBSchema.BookTable.Cols.ADD_TIME));
        Calendar addTime = Calendar.getInstance();
        addTime.setTimeInMillis(addTimeMills);
        //
        String isbn = getString(getColumnIndex(BookDBSchema.BookTable.Cols.ISBN));
        //hasCover
        int hanCoverInt = getInt(getColumnIndex(BookDBSchema.BookTable.Cols.HAS_COVER));
        boolean hasCover;
        if (hanCoverInt == 0) {
            hasCover = false;
        } else {
            hasCover = true;
        }
        //
        int readingStatus = getInt(getColumnIndex(BookDBSchema.BookTable.Cols.READING_STATUS));
        String bookShelfId = getString(getColumnIndex(BookDBSchema.BookTable.Cols.BOOKSHELF_ID));
        String notes = getString(getColumnIndex(BookDBSchema.BookTable.Cols.NOTES));
        String website = getString(getColumnIndex(BookDBSchema.BookTable.Cols.WEBSITE));
        //labelID
        String labelIDJson = getString(getColumnIndex(BookDBSchema.BookTable.Cols.LABEL_ID));
        Type typeUUID = new TypeToken<List<UUID>>() {
        }.getType();
        List<UUID> labelID = gson.fromJson(labelIDJson, typeUUID);
        //
        //above finish reading from database, then set book
        Book book = new Book(UUID.fromString(uuid));
        book.setTitle(title);
        book.setAuthors(authors);
        book.setTranslators(translators);
        book.setWebIds(webIDs);
        book.setPublisher(publisher);
        book.setPubTime(pubTime);
        book.setAddTime(addTime);
        book.setIsbn(isbn);
        book.setHasCover(hasCover);
        book.setReadingStatus(readingStatus);
        book.setBookshelfID(UUID.fromString(bookShelfId));
        book.setNotes(notes);
        book.setWebsite(website);
        book.setLabelID(labelID);
        return book;
    }
}
