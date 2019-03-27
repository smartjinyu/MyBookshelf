package com.smartjinyu.mybookshelf;

import androidx.annotation.Nullable;

import com.github.promeg.pinyinhelper.Pinyin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/19.
 * This class represents a simple book.
 */

public class Book implements Serializable {
    private static final String TAG = "Book";

    private String title;
    private UUID id; // A unique id to identify each book
    private List<String> authors;
    private List<String> translators;
    private Map<String, String> WebIds;
    // "douban"
    private String publisher;
    private Calendar pubTime;
    private Calendar addTime;// Time the book add to bookshelf
    private String isbn;
    private boolean hasCover;
    private int readingStatus;
    /**
     * 0 represents not set
     * 1 represents unread
     * 2 represents reading
     * 3 represents read
     */

    private UUID bookshelfID;
    private String notes;
    private String website;
    private List<UUID> labelID;

    public Book() {
        this(UUID.randomUUID());
    }

    public Book(UUID uuid) {
        id = uuid;
        bookshelfID = UUID.fromString("407c4479-5a57-4371-8b94-ad038f1276fe");
        //default bookshelf id
        readingStatus = 0;
        addTime = Calendar.getInstance();

        // initial value
        authors = new ArrayList<>();
        translators = new ArrayList<>();
        labelID = new ArrayList<>();
        WebIds = new HashMap<>();
    }


    public String getCoverPhotoFileName() {
        return "Cover_" + id.toString() + ".jpg";
    }

    public String getCoverPhotoFileNameWithoutExtension() {
        return "Cover_" + id.toString();
    }


    public Calendar getAddTime() {
        return addTime;
    }

    public void setAddTime(Calendar addTime) {//only used for recovering data from database
        this.addTime = addTime;
    }

    public Map<String, String> getWebIds() {
        return WebIds;
    }

    public void setWebIds(Map<String, String> webIds) {
        WebIds = webIds;
    }

    public UUID getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Calendar getPubTime() {
        return pubTime;
    }

    public void setPubTime(Calendar pubTime) {
        this.pubTime = pubTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getTranslators() {
        return translators;
    }

    public void setTranslators(List<String> translators) {
        this.translators = translators;
    }

    public boolean isHasCover() {
        return hasCover;
    }

    public void setHasCover(boolean hasCover) {
        this.hasCover = hasCover;
    }

    public UUID getBookshelfID() {
        return bookshelfID;
    }

    public void setBookshelfID(UUID bookshelfID) {
        this.bookshelfID = bookshelfID;
    }

    /**
     *
     * @return 0 unset, 1 unread, 2 reading, 3 finished
     */
    public int getReadingStatus() {
        return readingStatus;
    }

    /**
     *
     * @param readingStatus 0 unset, 1 unread, 2 reading, 3 finished
     */
    public void setReadingStatus(int readingStatus) {
        this.readingStatus = readingStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<UUID> getLabelID() {
        return labelID;
    }

    public void addLabel(Label label) {
        if (labelID == null) {
            labelID = new ArrayList<>();
        }
        if (!labelID.contains(label.getId())) {
            labelID.add(label.getId());
        }
    }

    public void addLabel(UUID labelID) {
        if (this.labelID == null) {
            this.labelID = new ArrayList<>();
        }
        if (!this.labelID.contains(labelID)) {
            this.labelID.add(labelID);
        }
    }

    public void removeLabel(UUID labelID) {
        if (this.labelID != null) {
            this.labelID.remove(labelID);
        }
    }

    public void removeLabel(Label label) {
        if (labelID != null) {
            labelID.remove(label.getId());
        }
    }


    public void setLabelID(List<UUID> labelID) {
        this.labelID = labelID;
    }

    public static class titleComparator implements Comparator<Book> {
        @Override
        public int compare(Book book1, Book book2) {
            // transfer to Chinese Pinyin
            String title1 = Pinyin.toPinyin(book1.getTitle(), "");
            String title2 = Pinyin.toPinyin(book2.getTitle(), "");
            return title1.compareTo(title2);
        }
    }

    public static class publisherComparator implements Comparator<Book> {
        @Override
        public int compare(Book book1, Book book2) {
            // transfer to Chinese Pinyin
            String publisher1 = Pinyin.toPinyin(book1.getPublisher(), "");
            String publisher2 = Pinyin.toPinyin(book2.getPublisher(), "");
            return publisher1.compareTo(publisher2);
        }
    }

    public static class pubtimeComparator implements Comparator<Book> {
        @Override
        public int compare(Book book1, Book book2) {
            Calendar pubtime1 = book1.getPubTime();
            Calendar pubtime2 = book2.getPubTime();
            return pubtime1.compareTo(pubtime2);
        }
    }

    public static class authorComparator implements Comparator<Book> {
        @Override
        public int compare(Book book1, Book book2) {
            String author1 = Pinyin.toPinyin(book1.getAuthors().toString(), "");
            //Log.d(TAG,"Title1 = " + book1.getTitle() + ", Author1 Pinyin = " + author1);
            String author2 = Pinyin.toPinyin(book2.getAuthors().toString(), "");
            //Log.d(TAG,"Title2 = " + book2.getTitle() + ", Author2 Pinyin = " + author2);
            int result = author1.compareTo(author2);
            //Log.d(TAG,"Compare result is " + result);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book)) {
            return false;
        }
        Book book = (Book) o;
        return book.getId().equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Get Formatted authors information
     * @return A string with different authors divided by ',', e.g.: smartjinyu1,smartjinyu2. Return null if no authors
     */
    @Nullable
    public String getFormatAuthor(){
        if(authors.size()!=0) {
            StringBuilder authorsBuilder = new StringBuilder();
            for (String curAuthor : getAuthors()) {
                authorsBuilder.append(curAuthor);
                authorsBuilder.append(",");
            }
            if (authorsBuilder.length() != 0) {
                authorsBuilder.deleteCharAt(authorsBuilder.length() - 1);
            }
            return authorsBuilder.toString();
        }else{
            return null;
        }
    }

    /**
     * Get Formatted translators information
     * @return A string with translators authors divided by ',', e.g.: smartjinyu1,smartjinyu2. Return null if no translators
     */
    @Nullable
    public String getFormatTranslator(){
        if(translators.size()!=0){
            StringBuilder translators = new StringBuilder();
            for (String translator : getTranslators()) {
                translators.append(translator);
                translators.append(",");
            }
            if (translators.length() != 0) {
                translators.deleteCharAt(translators.length() - 1);
            }
            return translators.toString();
        }else{
            return null;
        }
    }

    /**
     * Get the source of the book info
     * @return "Douban.com","OpenLibrary.org","Manually"
     */
    public String getDataSource() {
        Map<String, String> webIds = getWebIds();
        if (webIds.size() == 0) {
            return "Manually";
        } else {
            for (String key : webIds.keySet()) {
                if (key.equals("douban")) {
                    return "Douban.com";
                }
                if (key.equals("openLibrary")) {
                    return "OpenLibrary.org";
                }
            }
            return "Manually";
        }

    }


}
