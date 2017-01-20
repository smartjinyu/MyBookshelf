package com.smartjinyu.mybookshelf;

import android.icu.text.DateTimePatternGenerator;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by smartjinyu on 2017/1/19.
 * This class represents a simple book.
 */

public class Book {
    private String title;
    private Long id; // A unique id to identify each book
    private List<String> authors;
    private List<String> translators;
    private String publisher;
    private Date pubtime;
    private Date addTime;// Time the book add to bookshelf
    private String isbn;
    private boolean hasCover;

    public Book(){
        id = getRandomId();
        hasCover = false;
    }

    public static Long getRandomId(){
        Date date = new Date();
        long Ldate  = date.getTime();
        // generate a random int between 0 and 10000
        Random r = new Random();
        int salt = r.nextInt(10000 + 1);
        Ldate += salt;
        return Ldate;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getPubtime() {
        return pubtime;
    }

    public void setPubtime(Date pubtime) {
        this.pubtime = pubtime;
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
}
