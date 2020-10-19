package com.smartjinyu.mybookshelf;

import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/23.
 * represents a bookshelf
 */

public class BookShelf {
    private UUID id;
    private String title;
    private int cnt; // # of books on this bookshelf
    // Note that this number isn't the real number of db
    // It is the number under current condition (label), used to show on spinner
    private boolean internalBookShelf;

    public BookShelf() {
        id = UUID.randomUUID();
    }

    public BookShelf(UUID uuid) {
        id = uuid;
    }

    @NonNull
    @Override
    public String toString() {
        if(title != null){
            if(internalBookShelf){
                return title;
            }else {
                return title + " (" + cnt + ")";
            }
        }else{
            return " (" + cnt + ")";
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public boolean isInternalBookShelf() {
        return internalBookShelf;
    }

    public void setInternalBookShelf(boolean internalBookShelf) {
        this.internalBookShelf = internalBookShelf;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BookShelf)) {
            return false;
        }
        BookShelf bookshelf = (BookShelf) o;
        return bookshelf.getId().equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
